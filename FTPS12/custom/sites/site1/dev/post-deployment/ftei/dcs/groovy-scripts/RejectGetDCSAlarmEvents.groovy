import groovy.xml.*;
import org.apache.camel.*;
import java.util.*;


def receiver = exchange.getProperty("sender") != null ? exchange.getProperty("sender"): "UNKNOWN"

def sender = exchange.getProperty("receiver") != null ? exchange.getProperty("receiver"): "UNKNOWN"

def cause = exchange.getProperty(org.apache.camel.Exchange.EXCEPTION_CAUGHT, Exception.class);

def excClassName = cause.getClass().getName()
def errorMessage = excClassName + ': Please check log files on MessageBroker for more details'

def writer = new StringWriter()
def xml = new MarkupBuilder(writer)
xml.ShowDCSAlarmEvents('xsi:schemaLocation':'http://www.rockwell.com/mes/dcs/ifc ../../../main/xsd/mes-dcs-interface.xsd',
                       'xmlns':'http://www.rockwell.com/mes/dcs/ifc',
                       'xmlns:xsi':'http://www.w3.org/2001/XMLSchema-instance', 'schemeVersionID':'1.0') {
    ApplicationArea {
        Sender(sender)
        Receiver(receiver)
    }
    DataArea {
        ResponseCriteria(actionCode:'Rejected', errorMessage)                      
     }
        
  }


return writer.toString()