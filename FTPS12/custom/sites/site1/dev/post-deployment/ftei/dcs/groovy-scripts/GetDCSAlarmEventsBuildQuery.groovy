import groovy.xml.*;
import org.apache.camel.*;


DATETIMEFORMAT = '''yyyy-MM-dd'T'HH:mm:ss.SSSXXX'''
SIMPLEDATETIMEFORMAT = '''yyyy-MM-dd HH:mm:ss.SSS'''
/*
Important remark:
The implementation requires a simple 1:1 view of the FT Alarms & Events table ConditionEvent in the BatchHistory DB. 

*/
def convertToUTCString(cal) {

   sdf = new java.text.SimpleDateFormat(SIMPLEDATETIMEFORMAT);
   sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
   utcTimeString = sdf.format(cal.getTime());
   return utcTimeString;

}

def selectFields = '''SELECT CE.EventTimeStamp, CE.SourceName, CE.Message, CE.ConditionName, CE.AlarmClass, CE.InputValue, CE.LimitValue, CE.Active, CE.EventCategory, CE.Severity, CE.Priority ''';
def ftaeTable = '''FTAE_ConditionEvent CE ''';
def baseFrom = '''FROM ''';
def baseWhere = '''WHERE CE.Severity> :?severity AND ISNULL(CE.Active,0) = 1''';

def xmlDoc = new XmlSlurper(false,false).parseText(request.getBody().toString());
def sender = xmlDoc.ApplicationArea.Sender.text();
def receiver = xmlDoc.ApplicationArea.Receiver.text();
def batchID = xmlDoc.DataArea.BatchID.text();
def equipmentID = xmlDoc.DataArea.EquipmentID.text();
def startTime =  xmlDoc.DataArea.StartTime.text();
def endTime =  xmlDoc.DataArea.EndTime.text();
def severity = camelContext.resolvePropertyPlaceholders("{{GMPAlarm.severityLevel}}").toString();

// setting base flow vars
//
exchange.setProperty("sender", sender)
exchange.setProperty("receiver", receiver)
exchange.setProperty("batchID", batchID)
exchange.setProperty("equipmentID",equipmentID)

def substMap = [:]

def clientTimezone = null;

substMap['severity']=severity;

def whereOptions='''''';

if (startTime ) { // means it is not null and not empty
   baseWhere += '''AND CE.EventTimeStamp >= :?startTime ''';
    aCal = javax.xml.bind.DatatypeConverter.parseDateTime(startTime);
    clientTimezone = aCal.getTimeZone()
    utcString = convertToUTCString(aCal)
    substMap['startTime'] = java.sql.Timestamp.valueOf(utcString);
}
if (endTime ) { // means it is not null and not empty
   baseWhere += '''AND CE.EventTimeStamp <= :?endTime ''';
	 aCal = javax.xml.bind.DatatypeConverter.parseDateTime(endTime);
	 clientTimezone = aCal.getTimeZone()
	 utcString = convertToUTCString(aCal)
	 substMap['endTime'] = java.sql.Timestamp.valueOf(utcString);
}

exchange.setProperty("clientTimezone", clientTimezone)

if (batchID) {  // means it is not null and not empty

   baseFrom += ''' BHUnit INNER JOIN BHBatch ON BHUnit.uniqueid = BHBatch.uniqueid AND BHBatch.batchid = :?batchId ''';
   baseFrom += 'INNER JOIN FTAE_ConditionEvent CE ON  CE.EventTimeStamp >= BHUnit.starttime_gmt AND CE.EventTimeStamp <= BHUnit.endtime_gmt ';
   substMap['batchId']=batchID;
   if (equipmentID) {
      baseFrom += ''' AND BHUnit.unitname = :?equipmentId ''';
	   substMap['equipmentId']=equipmentID;
   }
   
} else { // no batch
   if (equipmentID) { // we have equipment
 //     baseFrom +=''' BHUnit INNER JOIN FTAE_ConditionEvent CE ON  CE.EventTimeStamp >= BHUnit.starttime AND CE.EventTimeStamp <= BHUnit.endtime ''';
 //     baseFrom += ''' AND BHUnit.unitname =equipmentId ''';
	   baseFrom +=''' FTAE_ConditionEvent CE ''';
       substMap['equipmentId']=equipmentID;
   } else {
   
     baseFrom += ftaeTable;
   
   }

}

def controlModuleMap = [:]
def ctr = 0;
def baseCtrModuleName='actualEquipmentId';
substMap['equipmentId1']="%"+equipmentID+"%";
def tpl1 = ''' CE.SourceName like ''';
def tpl2 =  ''' CE.SourceName like :?''';
xmlDoc.DataArea.RecipeElement.ActualEquipmentID.each { elt ->  
   
   substMap[baseCtrModuleName + ctr.toString()] = "%"+elt.text()+"%";
   
  if (ctr == 0) {
      baseWhere += ''' AND ( ''';
  } else { // greater than
	  baseWhere += ''' OR ''';
  }
  
  if (equipmentID) {
     substMap[baseCtrModuleName + ctr.toString()] ="%"+equipmentID +"%"+elt.text()+"%";
     baseWhere += tpl1+''':?'''+(baseCtrModuleName + ctr.toString()); 
  } else {
  
	  baseWhere += tpl2 +(baseCtrModuleName + ctr.toString());
  }
  ctr++;
   
}
if (ctr > 0) {

   baseWhere += ''')''';
}
if (ctr == 0 && equipmentID) { // no control modules
  
	baseWhere += ''' AND CE.SourceName like :?equipmentId1 ''';
}

def sqlString = selectFields + baseFrom + baseWhere;
response.setHeader("CamelSqlQuery", sqlString)
return substMap
