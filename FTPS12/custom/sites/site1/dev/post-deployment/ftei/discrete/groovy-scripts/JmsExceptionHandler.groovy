import groovy.xml.*;
import org.apache.camel.*;
import com.rockwell.integration.messaging.MessageEnvelope;

// define the LOGGER we eventually want to use...
LOGGER = org.apache.logging.log4j.LogManager.getLogger("Rockwell")

exchange.setProperty("subject", "Communication exception")

def payload = request.getBody()

if(payload == null)
{
	LOGGER.info(
		'\n\tCommunication  exception caught, payload is null');
	
	return ('Rockwell: Communication exception caught, payload is null');
}
else
{
	if (payload instanceof MessageEnvelope) 
	{
		LOGGER.info(
		'\n\tCommunication exception caught, payload.class = ' + payload.class.name +
		', payload = ' + payload.getDataInfo());
	
	return ('Rockwell: Communication exception caught, class = ' + payload.class.name + ' when processing file: ' + exchange.getProperty("inProcessFileNameWithPath"));
	} 
	else 
	{
		LOGGER.error(payload.getClass().getName());
		
		LOGGER.info(
		'\n\tCommunication exception caught, payload.class = ' + payload.class.name +
		', payload = ' + payload);
		return ('Rockwell: Communication exception caught, class = ' + payload.class.name + ' when processing file: ' + exchange.getProperty("inProcessFileNameWithPath"));
	}
}