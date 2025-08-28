import groovy.xml.*;
import org.apache.camel.*;
import com.rockwell.integration.b2mml.B2mmlProductionSchedule;
import com.rockwell.integration.b2mml.B2mmlMaterialInformation;
import com.rockwell.integration.b2mml.B2mmlProductDefinition;

// define the LOGGER we eventually want to use...
LOGGER = org.apache.logging.log4j.LogManager.getLogger("Rockwell")

exchange.setProperty("subject", "Routing Service Error")

def payload = request.getBody()

if ((payload instanceof B2mmlProductionSchedule) ||
        (payload instanceof B2mmlMaterialInformation) ||
        (payload instanceof B2mmlProductDefinition))
    LOGGER.info(
            '\n\tRouting Service Error: ' + payload.class.name +
            '\n\tTo Site: ' + payload.location.equipmentID)
else LOGGER.info(
    '\n\tRoutingSvcError Caught, payload class = ' + payload.class.name)
return message


