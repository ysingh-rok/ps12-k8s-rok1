import groovy.xml.*;
import org.apache.camel.*;

// define the LOGGER we eventually want to use...
LOGGER = org.apache.logging.log4j.LogManager.getLogger("Rockwell")

exchange.setProperty("subject", "File input exception")

def payload = request.getBody()

if (payload == null) {
    LOGGER.info(
            '\n\tException caught');

    return ('Rockwell: Exception caught');
}
else if (payload instanceof java.io.FileNotFoundException) {
    LOGGER.info(
            '\n\tFile input exception caught, payload.class = ' + payload.class.name +
            ', payload = ' + payload);

    LOGGER.info('\n\tFile input exception caught, payload.class = ' + payload.class.name +
            ', payload = ' + payload + ',\n\texception = ' + payload.getException() +
            ', \n\tcomponentName = ' + payload.getComponentName());

    return ('Rockwell: File input exception caught, class = ' + payload.class.name + ' when processing file: ' + exchange.getProperty("inProcessFileNameWithPath"));
}
else {
    LOGGER.info(
            '\n\tFile input exception caught, payload.class = ' + payload.class.name +
            ', payload = ' + payload);

    return ('Rockwell: File input exception caught, class = ' + payload.class.name + ' when processing file: ' + exchange.getProperty("inProcessFileNameWithPath"));
}

