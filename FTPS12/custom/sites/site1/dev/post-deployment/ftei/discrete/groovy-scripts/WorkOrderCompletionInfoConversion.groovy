import groovy.xml.*;
import groovy.transform.*;
import org.apache.camel.*;
import java.util.*;
import java.text.SimpleDateFormat;
import com.rockwell.integration.sap.TransferWorkOrder;
import com.rockwell.integration.sap.TransferWorkOrderItem;
import com.rockwell.integration.sap.WorkOrderCompletionInfo;


//define the LOGGER we eventually want to use...
LOGGER = org.apache.logging.log4j.LogManager.getLogger("Rockwell")

def calendar2str(aCalendar, datePattern) {
	return aCalendar.format(datePattern)
}

def isnull(aValue) {
	return (aValue == null)
}

def generateEDI_DOC40_Map() {

	def edi_doc40Map = [
		IDOCTYP:'CONF21',
		MESTYP:'WMMBXY'
	]

	//  def now = new Date()
	//  def dateString = now.format("yyyyMMMdd")
	//  def timeString = now.format("HHmmss")
	//  edi_doc40Map['CREDAT'] = dateString
	//  edi_doc40Map['CRETIM'] = timeString

	return edi_doc40Map
}


// LMNGA: (isnull(pojo.getOrderItems().get(0).getQuantityOrdered()) ? " " : num2str(pojo.getQuantity())),
def generateECONF2_Map(pojo, msgDate) {

	def e1conf2Map = [
		ERDAT: msgDate.format("yyyymmdd"),
		WERKS: pojo.getSiteNumber(),
		MATNR: pojo.workOrder.orderItems[0].partNumber,
		AUFNR: pojo.workOrder.name,
		LMNGA: pojo.workOrder.orderItems[0].getQuantityOrdered()
	]
	return e1conf2Map
}


// get the java POJO !!
def workOrderCompletionInfoPOJO = request.getBody(WorkOrderCompletionInfo.class)
def now = new Date()
// part of generating the XML document
def writer = new StringWriter()
def xmlIdoc = new MarkupBuilder(writer)

xmlIdoc.CONF21 {

		IDOC('BEGIN':'1') {
			EDI_DC40('SEGMENT':'1') {
				generateEDI_DOC40_Map().each { key, value ->
					"$key"(value)
				}
			}

			E1CONF2('SEGMENT':'"1"') {
				generateECONF2_Map(workOrderCompletionInfoPOJO, now).each { key, value ->
					"$key"(value)
				}
			}
		}
	}

	return writer.toString()
