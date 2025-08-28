import groovy.xml.*;
import org.apache.camel.*;
import com.rockwell.integration.messaging.MessageEnvelope;

// define the LOGGER we eventually want to use...
LOGGER = org.apache.logging.log4j.LogManager.getLogger("Rockwell")


return new MessageEnvelope(request.getBody())
