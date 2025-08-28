import org.apache.camel.*;
import com.rockwell.integration.messaging.IntegrationResponse;
import com.rockwell.integration.app.util.CustomStringEncoder;

// define the LOGGER we eventually want to use...
LOGGER = org.apache.logging.log4j.LogManager.getLogger("Rockwell")
LOGGER.info("RCVD Response from FTPC")
messageEnvelope = request.getBody()
payload = messageEnvelope.getPayload()
processedpath = camelContext.resolvePropertyPlaceholders("{{archive-processed.path}}").toString()
processpath = camelContext.resolvePropertyPlaceholders("{{archive-process.path}}").toString()
LOGGER.info("processedpath " + processedpath)
LOGGER.info("processpath " + processpath)

def checkFileName(String input) {
   def parts = input.split('_')
   def encodedSecret = parts[-1]  
   def decodedSecret = CustomStringEncoder.decode(encodedSecret)
   def formatedTime = input.substring(0, 21)
   def encodeparts = formatedTime.split("\\.")
   def lastElement = encodeparts[-1]
   if (decodedSecret.equals(lastElement)) {
        result="rest"
     
   } else {
        result="file"
      
   }
   return result
}

	if (payload instanceof IntegrationResponse) 
	{
		filename = payload.getInFileName().trim();
		
		errorMsg = payload.getFirstError();
		if (payload.isSuccessful()) 
		{		
			def result = checkFileName(filename);
		    if(result.equals("rest"))
		    {	
		    	LOGGER.debug("inFile is: " + filename);
				msg = "Payload from Rest '" + filename + "' processed successfully.";   
		    }
		    else
		    {	
				result = "OK";
				def inFile = new File (filename)
				def processInFile = new File(inFile.toString())//new File(processpath + "/" + inFile.getName())
				LOGGER.debug("inFile is: " + inFile);
				LOGGER.debug("processInFile is: " + processInFile);
				if (processInFile.exists()) 
				{
					def processedFilePath = new File(processedpath);
					if(!processedFilePath.exists()){
						processedFilePath.mkdir();
					}
					processedFile = new File (processedpath, inFile.getName());//new File (processedpath);
					LOGGER.info("Moving to: " + processedFile.getAbsolutePath());
					
					boolean moved =	processInFile.renameTo(processedFile);
					if (moved == true) 
					{
						msg = "File '" + filename + "' processed and moved successfully.";
					} 
					else 
					{
						msg = "File '" + filename + "' processed, but move failed.";
					}
				} 
			    else 
			    {
					msg = "File '" + filename + "' reportedly processed, but failed to find it in the archive.";
				}
			}
		} 
		else 
		{
			msg = "File '" + filename + "' failed in processing." +
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
	