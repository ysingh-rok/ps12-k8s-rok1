import groovy.xml.*;
import org.apache.camel.*;
import com.rockwell.integration.messaging.BasePayload;
import com.rockwell.integration.messaging.MessageEnvelope;
import jakarta.jms.ObjectMessage;

// define the LOGGER we eventually want to use...
LOGGER = org.apache.logging.log4j.LogManager.getLogger("Rockwell")

result = request.getBody()
try {
    if (result instanceof ObjectMessage) {
        result = result.getObject();
    }
    if (result instanceof MessageEnvelope) {
        result = result.getPayload();
    }
    if (result instanceof BasePayload) {
        eventId = result.getEventId();
        siteNumber = result.getSiteNumber();
        // setting the event id and site number in the exchange for further use.
        exchange.setProperty("EventId", eventId)
        exchange.setProperty("SiteNumber", siteNumber)

        LOGGER.info("Set message Properties");
        LOGGER.info("  EventId: " + eventId);
        LOGGER.info("  SiteNumber: " + siteNumber);
    }
}
catch (Exception x) {
    LOGGER.error(result, x);
}

return result;

