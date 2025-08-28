import groovy.xml.*;
import groovy.transform.*;
import org.apache.camel.*;
import java.util.*;
import java.text.SimpleDateFormat;
import com.rockwell.integration.sap.ISAPConstants;
import groovy.xml.slurpersupport.NodeChild;
import com.rockwell.integration.sap.TransferPart;

//define the LOGGER we eventually want to use...
LOGGER = org.apache.logging.log4j.LogManager.getLogger("Rockwell")

// Utility function for removing leading zeros
def int2str(maybeANumber) {
	try {
		intVal = Integer.parseInt(maybeANumber)
		return (intVal + "")
	} catch (all) {  
		return maybeANumber
	}
}


// closure getNodes using two parameters
@groovy.transform.Field def getNodes = { doc, path ->
    def nodes = doc
    path.split("\\.").each { nodes = nodes."${it}" }
    return nodes
}


def isnull(aValue) {
 
   return (aValue == null)

}

def generateTransferMap(NodeChild nodeBase, Map xpathMap, String pathBase) {


   resultMap = [:]

   xpathMap.each { entry ->
     xpath = entry.value
     def trimXPath = xpath.replace(pathBase, "").replace("/",".")
     getNodes(nodeBase, trimXPath).each {  
         resultMap[entry.key]=it.text()        
     }
     
  }
   
  return resultMap

}

//exchange:
//-> the filename that is currently processed...
def fileName = exchange.getProperty("inProcessFileNameWithPath");

//the incoming document
def matmasIdoc = new XmlSlurper(false,false).parseText(request.getBody(String.class));

LOGGER.debug (matmasIdoc.dump());

//the outgoing POJO
def transferPartPOJO = new TransferPart();

// we eventually want to some sanity checks on the matmas document upfront


// mapping starts now

def xpathMap = [ MAKTX:	'/MATMAS05/IDOC/E1MARAM/E1MAKTM/MAKTX',
                 MATNR:	'/MATMAS05/IDOC/E1MARAM/MATNR',
                 MEINS: '/MATMAS05/IDOC/E1MARAM/MEINS',
                 WERKS: '/MATMAS05/IDOC/E1MARAM/E1MARCM/WERKS',
                 MTART:	'/MATMAS05/IDOC/E1MARAM/MTART',
                 BESKZ:	'/MATMAS05/IDOC/E1MARAM/E1MARCM/BESKZ',
                 SOBSL:	'/MATMAS05/IDOC/E1MARAM/E1MAKTM/Z1MAT/SOBSL',
                 CLASS: '/MATMAS05/IDOC/E1MARAM/E1MAKTM/Z1MAT/CLASS',
                 PULLT: '/MATMAS05/IDOC/E1MARAM/E1MAKTM/Z1MAT/PULLT'
				 ]


transferPartPOJO.setVerb("CREATE")
transferPartPOJO.setInFileName(fileName)
transferPartPOJO.setPartRevision("1");
transferPartPOJO.setConsumptionType(ISAPConstants.CONSUMPTION_TYPE_SERIAL_NUMBER)

matmasIdoc.IDOC.E1MARAM.each { e1maram ->

    transferMap = [:]

	transferMap = generateTransferMap(e1maram, xpathMap, "/MATMAS05/IDOC/E1MARAM/")
	def siteNumberStr = transferMap['WERKS'] 
	def siteNumber = siteNumberStr.isInteger() ?  (siteNumberStr as int) : 0
	transferPartPOJO.setSiteNumber(siteNumber);
	transferPartPOJO.setDescription(transferMap['MAKTX']);
	transferPartPOJO.setPartNumber(transferMap['MATNR']);
	transferPartPOJO.setUnitOfMeasure(transferMap['MEINS']);
	transferPartPOJO.setReplacementType(transferMap['BESKZ']);
	
}

//LOGGER.debug(transferPartPOJO.toString());

return transferPartPOJO;
