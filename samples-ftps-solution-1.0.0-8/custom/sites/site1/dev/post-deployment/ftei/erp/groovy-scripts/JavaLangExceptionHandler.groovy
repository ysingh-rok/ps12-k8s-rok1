import groovy.xml.*;
import org.apache.camel.*;

// define the LOGGER we eventually want to use...
LOGGER = org.apache.logging.log4j.LogManager.getLogger("Rockwell")

exchange.setProperty("subject", "Route exception")

def payload = request.getBody()

if (payload == null) {
    LOGGER.info(
            '\n\tException caught');

    return ('Rockwell: Exception caught');
}
else {

    return ('  Rockwell: Exception caught, class = ' + payload.class.name + ' when processing file: ' + exchange.getProperty("inProcessFileNameWithPath"));
}

