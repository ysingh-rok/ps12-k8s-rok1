import groovy.xml.*;
import org.apache.camel.*;
import java.util.*;
import java.text.SimpleDateFormat;

DATETIMEFORMAT = '''yyyy-MM-dd'T'HH:mm:ss.SSSXXX'''
SIMPLEDATETIMEFORMAT = '''yyyy-MM-dd HH:mm:ss.SSS'''

def generateAlarmValue(record) {
   def pattern = '''Category:$eventCategory;Severity:$severity;Priority:$priority;Condition:$condition;InputValue:$inputValue;LimitValue:$limitValue;$message'''
   def recordMap = [:]
   recordMap['eventCategory']=record.EventCategory
   recordMap['severity']=record.Severity
   recordMap['priority']=record.Priority
   recordMap['condition']=record.ConditionName
   recordMap['inputValue']=record.InputValue
   recordMap['limitValue']=record.LimitValue
   recordMap['message']=record.Message
   def alarmValueEngine = new groovy.text.SimpleTemplateEngine();
   def alarmTpl = alarmValueEngine.createTemplate(pattern).make(recordMap);
   return alarmTpl.toString()

}

def generateEquipmentId(equipmentId, record) {

   if (equipmentId){
     return equipmentId + "/" + record.SourceName
     
   }
   return record.SourceName
}

def generateDateTime(record, timezone) {
 cal = new GregorianCalendar();
 cal.setTimeZone(TimeZone.getTimeZone('UTC'))
 cal.setTime(record.EventTimeStamp);
 
 return cal.format(DATETIMEFORMAT);
}

def convertToUTCString(cal) {

   sdf = new java.text.SimpleDateFormat(SIMPLEDATETIMEFORMAT);
   sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
   utcTimeString = sdf.format(cal.getTime());
   return utcTimeString;

}

def convertNumeric(stringValue) {
   bdValue = new BigDecimal(stringValue);
   roundedBDValue = new BigDecimal(bdValue).setScale(camelContext.resolvePropertyPlaceholders("{{getdcsvalues.numericScale}}").toInteger(), 
       BigDecimal.ROUND_HALF_UP);
   return roundedBDValue.toString();
}

def queryResult = request.getBody();
def receiver = exchange.getProperty("sender")
def sender = exchange.getProperty("receiver")

def writer = new StringWriter()
def xml = new MarkupBuilder(writer)
xml.ShowDCSAlarmEvents('xsi:schemaLocation':'http://www.rockwell.com/mes/dcs/ifc ../../../main/xsd/mes-dcs-interface.xsd',
                       'xmlns':'http://www.rockwell.com/mes/dcs/ifc',
                       'xmlns:xsi':'http://www.w3.org/2001/XMLSchema-instance', 'schemeVersionID':'1.0') {
    ApplicationArea {
        Sender(sender)
        Receiver(receiver)
    }
    DataArea {
        ResponseCriteria(actionCode:"Accepted")
        Events {
           queryResult.each  {record ->
              AlarmEvent {
                 TimeStamp(generateDateTime(record, exchange.getProperty('clientTimezone'))) 
                 Value(generateAlarmValue(record))
                 EquipmentID(generateEquipmentId(exchange.getProperty('equipmentID'), record))
                 MessageText(record.Message)
              }
            }
           
        }
        
        
     }
        
  }


return writer.toString()