import groovy.xml.*;
import org.apache.camel.*;
import com.rockwell.integration.messaging.MessageEnvelope;
import com.rockwell.integration.messaging.IntegrationResponse;

// define the LOGGER we eventually want to use...
LOGGER = org.apache.logging.log4j.LogManager.getLogger("Rockwell")



// Lookup eventId
eventIdObj = null;
eventId = null
siteNumberObj = null;
siteNumber = -1;
try {
    eventIdObj = exchange.getProperty("EventId")
    siteNumberObj = exchange.getProperty("SiteNumber")
    if (eventIdObj instanceof String) {
        eventId = (String) eventIdObj
    }
    if (siteNumberObj instanceof Integer) {
        siteNumber = ((Integer) siteNumberObj).intValue()
    }
    LOGGER.info("ConfirmationResponseToMessageEnvelope: Get Message Property EventId.");
    LOGGER.info("  Message: " + request.getBody());
    LOGGER.info("  EventId: " + eventId);
    LOGGER.info("  SiteNumber: " + siteNumber);
}
catch (Exception e) {
    LOGGER.info("ConfirmationResponseToMessageEnvelope::transform");
    LOGGER.error(e);
}

// Construct IntegrationResponse
if (eventId == null || eventId.isEmpty()) {
    LOGGER.info(request.getBody());
    throw new RuntimeException("Invalid EventId/SiteNumber");
}
else {
    IntegrationResponse integrationResponse = new IntegrationResponse(true);
    integrationResponse.setEventId(eventId);
    integrationResponse.setSiteNumber(siteNumber);
    envelope = new MessageEnvelope(integrationResponse);
    envelope.setIsFurtherRequest(true);
    envelope.setIsInbound(true);

    LOGGER.info(integrationResponse);



    return envelope;
}



