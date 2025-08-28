import org.apache.camel.*;
import com.rockwell.integration.messaging.IntegrationResponse;
import groovy.xml.*;
import groovy.transform.*;

// define the LOGGER we eventually want to use...
LOGGER = org.apache.logging.log4j.LogManager.getLogger("Rockwell")
LOGGER.info("RCVD Response from FTPC")
messageEnvelope = request.getBody()
payload = messageEnvelope.getPayload()

	if (payload instanceof IntegrationResponse) 
	{
		filename = payload.getInFileName().trim();
		
		errorMsg = payload.getFirstError();
		if (payload.isSuccessful()) 
		{
				LOGGER.debug("inFile is: " + filename);
				msg = "Payload '" + filename + "' processed successfully.";    	
		} 
		else 
		{
			msg = "Payload '" + filename + "' failed in processing." +
				"\n\tError message = " + errorMsg;
		}
	} 
	else 
	{
		LOGGER.info(
			'\n\tTransform to unknown class = ' + payload.class.name);
		msg = "Transform failure, to unknown class = " + payload.class.name;
	}

	LOGGER.info(
		'\n\tResponse From FTPC: msg = ' + msg);
	
	return msg;
	