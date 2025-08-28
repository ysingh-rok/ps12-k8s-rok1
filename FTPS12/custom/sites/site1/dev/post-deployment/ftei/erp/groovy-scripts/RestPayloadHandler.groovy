import groovy.xml.*;
import org.apache.camel.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.rockwell.integration.app.util.CustomStringEncoder;
import com.rockwell.integration.app.constant.ExceptionConstants;

def formattedDateTime = getFormattedDateTime()
def url = exchange.in.getHeader("CamelHttpPath")
def type = url.split("/")[url.split("/").length-1]
if(!isAlphanumeric(type)) 
{	
	RuntimeException exception = new RuntimeException(ExceptionConstants.INVALID_PATH_PARAM);
	exception.setStackTrace(new StackTraceElement[0]);
	throw  exception;
}

def encodeparts = formattedDateTime.split("\\.")
def lastElement = encodeparts[-1]
def encodedRest = CustomStringEncoder.encode(lastElement)

//inProcessFileNameWithPath property will be set with 65 Characters
def subType = type.length() >= 39 ? type.substring(0, 38) : type
def fileName = "${formattedDateTime}-${subType}_${encodedRest}"
exchange.setProperty("inProcessFileNameWithPath", fileName)

def getFormattedDateTime() {
    def now = LocalDateTime.now()

    def formatter = DateTimeFormatter.ofPattern("dd-MM-yy_HH-mm-ss.SSS")

    return now.format(formatter)
}

def isAlphanumeric(String str) {
   str ==~ /^[a-zA-Z0-9]+$/
}
