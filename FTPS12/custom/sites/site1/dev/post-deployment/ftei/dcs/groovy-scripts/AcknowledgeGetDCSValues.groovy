import groovy.xml.*;
import org.apache.camel.*;

def convertNumeric(stringValue) {
   bdValue = new BigDecimal(stringValue);
   roundedBDValue = new BigDecimal(bdValue).setScale(camelContext.resolvePropertyPlaceholders("{{getdcsvalues.numericScale}}").toInteger(), 
	   BigDecimal.ROUND_HALF_UP);
   return roundedBDValue.toString();
}


def queryResult = request.getBody();
def receiver = exchange.getProperty("sender")
def sender = exchange.getProperty("receiver")

// payload is a list of Maps i.e. List<Map<String, Object>>
queryResult.each  {record ->
   record['Recipe'] = record['Recipe'].subSequence(record['Recipe'].indexOf(":") + 1, record['Recipe'].length())

}
def writer = new StringWriter()
def xml = new MarkupBuilder(writer)
def  tpMap = exchange.getProperty('reportParamTypeMap')
xml.ShowDCSBatchValues('xsi:schemaLocation':'http://www.rockwell.com/mes/dcs/ifc ../../../main/xsd/mes-dcs-interface.xsd', 'xmlns':'http://www.rockwell.com/mes/dcs/ifc',
	'xmlns:xsi':'http://www.w3.org/2001/XMLSchema-instance', 'schemeVersionID':'1.0') {
	ApplicationArea {
		Sender(sender)
		Receiver(receiver)
	}
	DataArea {
		ResponseCriteria(actionCode:'Accepted')
		ControlRecipe {
		   queryResult.each  {record ->
		   RecipeElement{
			 ID(record['Recipe'])
			 ParameterID(record['Descript'])
			
			 if (tpMap.get([record['Recipe'], record['Descript']]) == 'string') {
				ValueString(record['PValue']);
			 } else if  (tpMap.get([record['Recipe'], record['Descript']]) == 'boolean') {
				ValueBoolean(record['PValue']);
			 }  else if  (tpMap.get([record['Recipe'], record['Descript']]) == 'integer') {
				ValueInteger(record['PValue']);
			 }  else if  (tpMap.get([record['Recipe'], record['Descript']]) == 'decimal') {
				ValueNumeric(convertNumeric(record['PValue']));
			 }  else if  (tpMap.get([record['Recipe'], record['Descript']]) == 'dateTime') {
				ValueDatetime(record['PValue']);
			 }
			}
	  }
	  }
	  }
	  }
return writer.toString()