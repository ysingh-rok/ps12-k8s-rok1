import groovy.xml.*;
import groovy.json.*;

def httpRequestResponse = response.getBody();
def receiver = exchange.getProperty("sender")
def sender = exchange.getProperty("receiver")


def writer = new StringWriter();
def xml = new MarkupBuilder(writer);
xml.AcknowledgeCreateDCSBatch('xsi:schemaLocation':'http://www.rockwell.com/mes/dcs/ifc ../../../main/xsd/mes-dcs-interface.xsd', 'xmlns':'http://www.rockwell.com/mes/dcs/ifc',
	'xmlns:xsi':'http://www.w3.org/2001/XMLSchema-instance', 'schemeVersionID':'1.0') {
	ApplicationArea {
		Sender(receiver)
		Receiver(sender)
	}
	DataArea {
		if (httpRequestResponse['status'] == '0') {
		   ResponseCriteria(actionCode:'Accepted')
	    } else {
			ResponseCriteria(actionCode:'Rejected',
				httpRequestResponse['id'])
		}
		InternalBatchID(httpRequestResponse['status'] == '0'? httpRequestResponse['createid']:'')
	}
	OtherInformations {
		OtherInformation {
			ID ('UniqueBatchID')
			ValueString ( httpRequestResponse['status'] == '0'? httpRequestResponse['creatid']:'')
		}
	}
	  
  }
	  
return writer.toString()