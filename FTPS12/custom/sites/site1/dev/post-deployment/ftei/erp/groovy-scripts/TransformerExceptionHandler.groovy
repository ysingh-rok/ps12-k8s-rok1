import groovy.xml.*;
import org.apache.camel.*;
import com.rockwell.integration.b2mml.B2mmlMaterialInformation;
import com.rockwell.integration.b2mml.B2mmlProductionSchedule;
import com.rockwell.integration.b2mml.B2mmlProductDefinition;

// define the LOGGER we eventually want to use...
LOGGER = org.apache.logging.log4j.LogManager.getLogger("Rockwell")

exchange.setProperty("subject", "Transformer exception")

def payload = request.getBody()

if(payload == null) {
    LOGGER.info(
            '\n\tTransformer exception caught, payload is null');

    return ('Rockwell: Transformer exception caught, payload is null');
}
else {
    LOGGER.error('payload.class = ' + payload.getClass().getName());

    LOGGER.info(
            '\n\tTransformer exception caught, payload.class = ' + payload.class.name +
            ', payload = ' + payload);

    return ('Rockwell: Transformer exception caught, class = ' + payload.class.name + ' when processing file: ' + exchange.getProperty("inProcessFileNameWithPath"));
}


