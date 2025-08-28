import groovy.xml.*;

def reportParamTypeMap = [:];
def shareMap = [:];
def sqlString = '''SELECT Recipe,Descript, PValue from BHBatchHis where batchID like :?batchId and Event like 'Report%' ''';
def sqlWhereConditionTemplate= '''(Recipe like :?ID AND Descript like :?ParameterID) ''' ;

LOGGER = org.apache.logging.log4j.LogManager.getLogger("Rockwell")

def getDCSBatchValues = new XmlSlurper(false,false).parseText(request.getBody().toString());
def sender = getDCSBatchValues.ApplicationArea.Sender.text();
def receiver = getDCSBatchValues.ApplicationArea.Receiver.text();
def batchID = getDCSBatchValues.DataArea.BatchID.text();

shareMap['batchId']=batchID

exchange.setProperty("sender", sender)
exchange.setProperty("receiver", receiver)
exchange.setProperty("batchID", batchID)

def whereCondition = '''''';
def ctr = 0;
getDCSBatchValues.DataArea.ControlRecipe.RecipeElement.each { elt ->  
   def pMap = [:]
   pMap.put(elt.ID.name(), elt.ID.text());
   def ID=elt.ID.name()+""+ctr;
   shareMap[elt.ID.name()+""+ctr]="%"+elt.ID.text();
   pMap.put(elt.ParameterID.name(), elt.ParameterID.text())
   def ParameterID=elt.ParameterID.name()+""+ctr;
   shareMap[elt.ParameterID.name()+""+ctr]=elt.ParameterID.text();
   reportParamTypeMap.put([ elt.ID.text(),  elt.ParameterID.text()], elt.DataType.text());
   if (ctr == 0) {
      whereCondition += ''' AND ( ''';
	} else { // greater than
	  whereCondition += ''' OR ''';
    }
  whereCondition += " (Recipe like :?${ID} AND Descript like :?${ParameterID}) ";
  ctr++;
}

exchange.setProperty('reportParamTypeMap',reportParamTypeMap);

sqlString += whereCondition + ''')'''
response.setHeader("CamelSqlQuery", sqlString)
LOGGER.info(sqlString);
return shareMap


