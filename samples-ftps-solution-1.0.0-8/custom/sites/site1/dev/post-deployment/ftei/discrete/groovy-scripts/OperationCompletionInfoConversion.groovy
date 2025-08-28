import groovy.xml.*;
import groovy.transform.*;
import org.apache.camel.*;
import java.util.*;
import java.text.SimpleDateFormat;
import com.rockwell.integration.sap.OperationCompletionInfo;
import com.rockwell.integration.sap.*;

LOGGER = org.apache.logging.log4j.LogManager.getLogger("Rockwell")

def calendar2str(aCalendar, datePattern) {
	return aCalendar.format(datePattern)
}

def isnull(aValue) {
	return (aValue == null)
}

def generateEDI_DOC40_Map() {

	def edi_doc40Map = [
		RCVPRT:'LS',
		RCVPRN:'ED1CLNT530',
		IDOCTYP:'WMMBID02',
		SNDPRT:'LS',
		MESTYP:'WMMBXY',
		SNDPRN:'ID3CLNT800',
		CREDAT:'20130822',
		CRETIM:'112203'
	]

	def now = new Date()
	def dateString = now.format("yyyyMMMdd")
	def timeString = now.format("HHmmss")
	edi_doc40Map['CREDAT'] = dateString
	edi_doc40Map['CRETIM'] = timeString
	return edi_doc40Map
}

// TODO 
// output.BUDAT = (isnull(input.completeTime) ? null : calendar2str(str2calendar(s,"yyyyMMdd"),"yyyyMMdd"));

def generateE1MBXYH_Map(pojo) {

	def confMap = [
		BUDAT:pojo.getCompleteTime().format("yyyymmdd")
	]
	return confMap
}


def generateE1MBXYI_Map(pojo) {

	def confMap = [
		EBELN: pojo.trackedLot.getOrderNumber(),
		BWART: pojo.trackedLot.getType(),
		MATNR: pojo.trackedLot.getPartNumber(),
		ERFMG: pojo.trackedLot.getQuantity(),
		WERKS: pojo.getSiteNumber(),
		LGORT: pojo.trackedLot.getWorkCenterName(),
		EBELP: pojo.getCompleteCount()
	]
	return confMap
}


// get the java POJO !!
def operartionCompletionInfoPojo = request.getBody(OperationCompletionInfo.class)
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
		    generateE1MBXYH_Map(operartionCompletionInfoPojo).each { key, value ->
                   "$key"(value)
				  }
				  
			E1MBXYI(SEGMENT="1") {
			  generateE1MBXYI_Map(operartionCompletionInfoPojo).each { key, value ->
                   "$key"(value)
			  }
            }	  
        }
}

return writer.toString()


