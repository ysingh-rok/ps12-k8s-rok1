import groovy.xml.*;
import groovy.transform.*;
import org.apache.camel.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import com.rockwell.integration.sap.ISAPConstants;
import com.rockwell.integration.sap.TransferBOM;
import com.rockwell.integration.sap.TransferBomItem;
import groovy.xml.slurpersupport.NodeChild;

//define the LOGGER we eventually want to use...
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

def str2calendar (dateStringValue, datePattern) {
	def date = Date.parse(datePattern, dateStringValue) // groovy date !!
	return date.toCalendar()
}


def calendar2str(aCalendar, datePattern) {

	return aCalendar.format(datePattern)
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

def bomPathMap = [WERKS:  '/BOMMAT01/IDOC/E1STZUM/E1MASTM/WERKS',
	RCVPRN: '/BOMMAT01/IDOC/EDI_DC40/RCVPRN',
	STLNR: '/BOMMAT01/IDOC/E1STZUM/STLNR']


def bomItemPathMap = [IDNRK:  '/BOMMAT01/IDOC/E1STZUM/E1STPOM/IDNRK',
	MENGE:   '/BOMMAT01/IDOC/E1STZUM/E1STPOM/MENGE',
	MEINS:    '/BOMMAT01/IDOC/E1STZUM/E1STPOM/MEINS',
	DATUV:    '/BOMMAT01/IDOC/E1STZUM/E1STPOM/DATUV',
	POSNR:   '/BOMMAT01/IDOC/E1STZUM/E1STPOM/POSNR']


//exchange:
//-> the filename that is currently processed...
def fileName = exchange.getProperty("inProcessFileNameWithPath");

//the incoming document
def bomMatIdoc = new XmlSlurper(false,false).parseText(request.getBody(String.class));

//LOGGER.debug (bomMatIdoc.dump());

//the outgoing POJO
def transferBomPOJO = new TransferBOM();

// mapping starts now
def bomMap = [:]
bomMap = generateTransferMap(bomMatIdoc, bomPathMap, "/BOMMAT01/")

def siteNumberStr = bomMap['WERKS']
def siteNumber = siteNumberStr.isInteger() ?  (siteNumberStr as int) : 0

transferBomPOJO.setVerb("CREATE");
transferBomPOJO.setInFileName(fileName);
transferBomPOJO.setSiteNumber(siteNumber);
transferBomPOJO.setSiteId(bomMap['RCVPRN']);
transferBomPOJO.setBomName(bomMap['STLNR']);


def bomItemList =  []

bomMatIdoc.IDOC.E1STZUM.E1STPOM.each { e1stpom ->

	transferBomItem = new TransferBomItem()
	bomItemMap = [:]
	bomItemMap = generateTransferMap(e1stpom, bomItemPathMap, "/BOMMAT01/IDOC/E1STZUM/E1STPOM/")

	def quantityStr = bomItemMap['MENGE']
	def quantity = new BigDecimal(quantityStr)

	// TODO (isnull(input.IDNRK) ? null : num2str(input.IDNRK));
	transferBomItem.setName(bomItemMap['IDNRK'])
	transferBomItem.setPartNumber(bomItemMap['IDNRK']?:null)
	transferBomItem.setQuantity(quantity)
	transferBomItem.setUnitOfMeasure(bomItemMap['MEINS'])

	bomItemList.add(transferBomItem)
}
transferBomPOJO.setBomItems(bomItemList)


return transferBomPOJO;
