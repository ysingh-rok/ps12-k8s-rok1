import groovy.xml.*;
import groovy.transform.*;
import org.apache.camel.*;
import com.rockwell.integration.sap.ISAPConstants;
import groovy.xml.slurpersupport.NodeChild;
import com.rockwell.integration.sap.TransferWorkOrder;
import com.rockwell.integration.sap.TransferWorkOrderItem;

// define the LOGGER we eventually want to use...
LOGGER = org.apache.logging.log4j.LogManager.getLogger("Rockwell")

// closure getNodes using two parameters
@groovy.transform.Field def getNodes = { doc, path ->
	def nodes = doc
	path.split("\\.").each { nodes = nodes."${it}" }
	return nodes
}

def isnull(object) {
	return (object == null)
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

def str2calendar (dateStringValue, datePattern) {
	def date = Date.parse(datePattern, dateStringValue) // groovy date !!
	return date.toCalendar()
}

def str2decimal(decimalString) {
	decVal = decimalString.toBigDecimal()
	decVal.setScale(3)
	return decVal
}


def orderMap = [
	AUFNR:	'/ZLOIPRO01/IDOC/E1AFKOL/AUFNR',
	WERKS:	'/ZLOIPRO01/IDOC/E1AFKOL/WERKS',
	GAMNG:	'/ZLOIPRO01/IDOC/E1AFKOL/GAMNG',
	MATNR:	'/ZLOIPRO01/IDOC/E1AFKOL/MATNR'
]

def orderItemPathMap = [
	POSNR:	'/ZLOIPRO01/IDOC/E1AFKOL/E1AFFLL/E1AFVOL/E1RESBL/POSNR',
	MATNR:	'/ZLOIPRO01/IDOC/E1AFKOL/E1AFFLL/E1AFVOL/E1RESBL/MATNR',
	MEINS:  '/ZLOIPRO01/IDOC/E1AFKOL/E1AFFLL/E1AFVOL/E1RESBL/MEINS'
]


//exchange:
//-> the filename that is currently processed...
def fileName = exchange.getProperty("inProcessFileNameWithPath");


//the incoming document
def workOrderIdoc = new XmlSlurper(false,false).parseText(request.getBody(String.class));

LOGGER.debug (workOrderIdoc.dump());

//the outgoing POJO
def transferWorkOrderBTO = new TransferWorkOrder();
// TODO sanity checks on the bom document upfront


// mapping starts now
def workOrderMap  = [:]
workOrderMap = generateTransferMap(workOrderIdoc, orderMap, "/ZLOIPRO01/")

LOGGER.debug ("workOrderMap: " + workOrderMap);

def siteNumberStr = workOrderMap['WERKS']
def siteNumber = siteNumberStr.isInteger() ?  (siteNumberStr as int) : 0

transferWorkOrderBTO.setVerb("CREATE")
transferWorkOrderBTO.setInFileName(fileName)
transferWorkOrderBTO.setName(workOrderMap['AUFNR'])
transferWorkOrderBTO.setSiteNumber(siteNumber);

def bomName = workOrderMap['AUFNR']
if(bomName=="")
{
	LOGGER.debug ("empty value of AUFNR is not allowed");
	msg = "The Filename '" + fileName + "' contains empty AUFNR value";
  	throw new Exception(msg);
}
// Set WorkOrderItems
def workOrderItemList = []

def i = 0
workOrderIdoc.IDOC.E1AFKOL.E1AFFLL.E1AFVOL.E1RESBL.each { e1resbl ->

	transferWorkOrderItem = new TransferWorkOrderItem()
	workOrderItemMap = [:]
	workOrderItemMap = generateTransferMap(e1resbl, orderItemPathMap, "/ZLOIPRO01/IDOC/E1AFKOL/E1AFFLL/E1AFVOL/E1RESBL/")
	LOGGER.debug ("workOrderItemMap " + i++ + ":  " +  workOrderItemMap)

	transferWorkOrderItem.setPartNumber(workOrderItemMap['MATNR'])
	transferWorkOrderItem.setName(workOrderItemMap['POSNR'])
	transferWorkOrderItem.setUnitOfMeasure(workOrderItemMap['MEINS']) // Extra mapping
	transferWorkOrderItem.setBomName(bomName)

	workOrderItemList.add(transferWorkOrderItem)
}
transferWorkOrderBTO.setOrderItems(workOrderItemList)


return transferWorkOrderBTO;
