import groovy.xml.*;
import groovy.transform.*;
import org.apache.camel.*;
import java.util.*;
import java.text.SimpleDateFormat;
import com.rockwell.integration.sap.ISAPConstants;
import groovy.xml.slurpersupport.NodeChild;
import com.rockwell.integration.sap.TransferParts;
import com.rockwell.integration.sap.TransferPart;

//define the LOGGER we eventually want to use...
LOGGER = org.apache.logging.log4j.LogManager.getLogger("Rockwell")

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
def transferPartsPOJO = new TransferParts();

// we eventually want to some sanity checks on the matmas document upfront


// mapping starts now

def xpathMap = [ MAKTX:	'/ZRA_MATMAS_PARTS/IDOC/E1MARAM/E1MAKTM/MAKTX',
                 MATNR:	'/ZRA_MATMAS_PARTS/IDOC/E1MARAM/MATNR',
                 MEINS: '/ZRA_MATMAS_PARTS/IDOC/E1MARAM/MEINS',
                 WERKS: '/ZRA_MATMAS_PARTS/IDOC/E1MARAM/E1MARCM/WERKS',
                 MTART:	'/ZRA_MATMAS_PARTS/IDOC/E1MARAM/MTART',
                 BESKZ:	'/ZRA_MATMAS_PARTS/IDOC/E1MARAM/E1MARCM/BESKZ',
                 SOBSL:	'/ZRA_MATMAS_PARTS/IDOC/E1MARAM/E1MAKTM/Z1MAT/SOBSL',
                 CLASS: '/ZRA_MATMAS_PARTS/IDOC/E1MARAM/E1MAKTM/Z1MAT/CLASS',
                 PULLT: '/ZRA_MATMAS_PARTS/IDOC/E1MARAM/E1MAKTM/Z1MAT/PULLT'
				 ]



transferPartsPOJO.setVerb("CREATE")
transferPartsPOJO.setInFileName(fileName)

//List<com.rockwell.integration.sap.TransferPart> transferPartList =  new ArrayList<com.rockwell.integration.sap.TransferPart>()
def transferPartList = []

matmasIdoc.IDOC.E1MARAM.each { e1maram ->

    transferMap = [:]

	transferMap = generateTransferMap(e1maram, xpathMap, "/ZRA_MATMAS_PARTS/IDOC/E1MARAM/")

    transferPart = new TransferPart()

	// Note was not set in Mule transformer
	def siteNumberStr = transferMap['WERKS'] 
	def siteNumber = siteNumberStr.isInteger() ?  (siteNumberStr as int) : 0
	transferPart.setSiteNumber(siteNumber);

	transferPart.setDescription(transferMap['MAKTX']);
	transferPart.setPartNumber(transferMap['MATNR']);
	transferPart.setUnitOfMeasure(transferMap['MEINS']);
	transferPart.setReplacementType(transferMap['BESKZ']);
	
	transferPart.setVerb("CREATE")
	transferPart.setInFileName(fileName)
	transferPart.setPartRevision("1");
    transferPart.setConsumptionType(ISAPConstants.CONSUMPTION_TYPE_SERIAL_NUMBER)

	transferPartList.add(transferPart)	
}
transferPartsPOJO.setParts(transferPartList)


//LOGGER.debug(transferPartsPOJO.toString());

return transferPartsPOJO;
