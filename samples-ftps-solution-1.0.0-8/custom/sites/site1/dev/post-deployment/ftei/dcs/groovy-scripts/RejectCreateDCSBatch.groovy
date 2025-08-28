import groovy.xml.*;
import groovy.json.*;


def receiver = exchange.getProperty("sender") != null ? exchange.getProperty("sender"): "UNKNOWN"

def sender = exchange.getProperty("receiver") != null ? exchange.getProperty("receiver"): "UNKNOWN"

def cause = exchange.getProperty(org.apache.camel.Exchange.EXCEPTION_CAUGHT, Exception.class);


cause.printStackTrace();

def excClassName = cause.getClass().getName()
def errorMessage = excClassName + ': Please check Console for more details'

def writer = new StringWriter();
def xml = new MarkupBuilder(writer);
xml.AcknowledgeCreateDCSBatch('xsi:schemaLocation':'http://www.rockwell.com/mes/dcs/ifc ../../../main/xsd/mes-dcs-interface.xsd', 'xmlns':'http://www.rockwell.com/mes/dcs/ifc',
	'xmlns:xsi':'http://www.w3.org/2001/XMLSchema-instance', 'schemeVersionID':'1.0') {
	ApplicationArea {
		Sender(receiver)
		Receiver(sender)
	}
	DataArea {
		ResponseCriteria(actionCode:'Rejected',
		errorMessage)
		InternalBatchID('')
	}
  
  }
	  
return writer.toString()