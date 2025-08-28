import groovy.xml.*;
import org.apache.camel.*;
import java.util.regex.Matcher;

// define the LOGGER we eventually want to use...
LOGGER = org.apache.logging.log4j.LogManager.getLogger("Rockwell")

//exchange:
//-> remember the filename that is currently processed...

fileNameWithPath = request.getHeader("CamelFileAbsolutePath")

inProcessPath = camelContext.resolvePropertyPlaceholders("{{archive-process.path}}").toString()

fileName = request.getHeader("CamelFileName")

dateFormat = camelContext.resolvePropertyPlaceholders("{{date.format}}").toString()
timeFormat = camelContext.resolvePropertyPlaceholders("{{time.format}}").toString()
fieldSeparator = camelContext.resolvePropertyPlaceholders("{{field.separator}}").toString()
def currentDateTime = new Date().format(dateFormat + fieldSeparator + timeFormat);

inProcessFileNameOnly = currentDateTime + "-" + fileName;

inProcessFileNameWithPath = "";
if(inProcessPath.contains("/")) {
    inProcessFileNameWithPath = inProcessPath + "/" + inProcessFileNameOnly;
    inProcessFileNameWithPath = inProcessFileNameWithPath.replaceAll("/", Matcher.quoteReplacement(File.separator));
}
else if(inProcessPath.contains("\\")) {
    inProcessFileNameWithPath = inProcessPath + "\\" + inProcessFileNameOnly;
    inProcessFileNameWithPath = inProcessFileNameWithPath.replaceAll("\\", Matcher.quoteReplacement(File.separator));
}

inProcessCurrentDateTime = currentDateTime;

exchange.setProperty("inProcessFileNameOnly", inProcessFileNameOnly)
exchange.setProperty("inProcessFileNameWithPath", inProcessFileNameWithPath)
exchange.setProperty("inProcessCurrentDateTime", inProcessCurrentDateTime)
LOGGER.debug("\n\tReadFromFileSystemAndTransform_PreCheck: inProcessFileNameWithPath='" + inProcessFileNameWithPath + "'");

length = fileName.length();
LOGGER.debug("Detected length: " + length)

if(length > 255) {
    LOGGER.error("Verifying the File Length:");
    msg = "The Filename '" + fileName + "' length is too long";
    throw new Exception(msg);
}


//no return message
