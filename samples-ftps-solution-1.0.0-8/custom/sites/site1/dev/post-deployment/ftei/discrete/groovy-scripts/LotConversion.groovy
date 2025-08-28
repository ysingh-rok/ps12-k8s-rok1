import groovy.xml.*;
import groovy.transform.*;
import org.apache.camel.*;
import com.rockwell.integration.sap.ISAPConstants;
import com.rockwell.integration.sap.TransferLot;
import groovy.xml.slurpersupport.NodeChild;

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

def lotPathMap = [
	AUFNR:	'/ZRA_LOT/IDOC/E1AFKOL/AUFNR',
	WERKS:	'/ZRA_LOT/IDOC/E1AFKOL/WERKS',
	GAMNG:	'/ZRA_LOT/IDOC/E1AFKOL/GAMNG',
	MATNR:	'/ZRA_LOT/IDOC/E1AFKOL/MATNR'
]

//exchange:
//-> the filename that is currently processed...
def fileName = exchange.getProperty("inProcessFileNameWithPath");


//the incoming document
def zralot = new XmlSlurper(false,false).parseText(request.getBody(String.class));

LOGGER.debug (zralot.dump());

def transferLot = new TransferLot();

def lotMap = [:]

lotMap = generateTransferMap(zralot, lotPathMap, "/ZRA_LOT/")

LOGGER.debug (lotMap);

// setting work order attributes (some)
transferLot.setVerb("CREATE")
transferLot.setInFileName(fileName)

transferLot.setOrderNumber(lotMap['AUFNR'])
transferLot.setOrderItem(lotMap['AUFNR'])
transferLot.setName(lotMap['AUFNR'])

def siteNumberStr = lotMap['WERKS']
def siteNumber = siteNumberStr.isInteger() ?  (siteNumberStr as int) : 0
transferLot.setSiteNumber(siteNumber)

transferLot.setPartNumber(lotMap['MATNR']);

def quantityStr = lotMap['GAMNG']
def quantity = new BigDecimal(quantityStr)
transferLot.setQuantity(quantity)

return transferLot
