import groovy.xml.*;
import groovy.transform.*;
import org.apache.camel.*;
import java.util.*;
import java.text.SimpleDateFormat;
import com.rockwell.integration.sap.ConsumedPartInfo;


LOGGER = org.apache.logging.log4j.LogManager.getLogger("Rockwell")

def calendar2str(aCalendar, datePattern) {
	return aCalendar.format(datePattern)
}

def isnull(aValue) {
	return (aValue == null)
}

def generateEDI_DOC40_Map() {

	def edi_doc40Map = [
		MESTYP:'WMMBXY',
		IDOCTYP:'WMMBID02'
	]


	return edi_doc40Map
}

// TODO
// output.BUDAT = (isnull(input.creationTime) ? null : calendar2str(str2calendar(s,"yyyyMMdd"),"yyyyMMdd"));

def generateE1MBXYH_Map(pojo) {

	def confMap = [
		BUDAT:pojo.getCreationTime().format("yyyymmdd")
	]
	return confMap
}


// TODO
// ERFMG: (isnull(pojo.getQuantity()) ? null : decimal2double((pojo.getQuantity()),

def generateE1MBXYI_Map(pojo) {

	def confMap = [
		EBELN: pojo.getOrderNumber(),
		MATNR: pojo.getPartNumber(),
		ERFMG: pojo.getPartQuantity(),
		WERKS: pojo.getSiteNumber()
	]
	return confMap
}


// get the java POJO !!
def consumedPartInfoPOJO = request.getBody(ConsumedPartInfo.class)
def now = new Date()
// part of generating the XML document
def writer = new StringWriter()
def xmlIdoc = new MarkupBuilder(writer)

xmlIdoc.WMMBID02 {

	IDOC('BEGIN':'1') {
		EDI_DC40('SEGMENT':'1') {
			generateEDI_DOC40_Map().each { key, value ->
				"$key"(value)
			}
		}
	}

	E1MBXYH('SEGMENT':'"1"') {
		generateE1MBXYH_Map(consumedPartInfoPOJO).each { key, value ->
			"$key"(value)
		}

		E1MBXYI(SEGMENT="1") {
			generateE1MBXYI_Map(consumedPartInfoPOJO).each { key, value ->
				"$key"(value)
			}
		}
	}
}

return writer.toString()
