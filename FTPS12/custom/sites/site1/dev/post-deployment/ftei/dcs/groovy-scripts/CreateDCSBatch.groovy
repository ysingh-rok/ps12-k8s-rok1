import groovy.xml.*;
import groovy.json.*;
import org.apache.camel.*;

def getParameterValue(xmlParameter) {
	if (xmlParameter.ValueNumeric.text()) {
	   return xmlParameter.ValueNumeric.text()
	}
	if (xmlParameter.ValueString.text()) {
	   return xmlParameter.ValueString.text()
	}
	if (xmlParameter.ValueInteger.text()) {
	   return xmlParameter.ValueInteger.text()
	}
	if (xmlParameter.ValueBoolean.text()) {
	   return xmlParameter.ValueBoolean.text()
	}
	if (xmlParameter.ValueDateTime.text()) {
	   return xmlParameter.ValueDateTime.text()
	}
	return "";
}

def processCreateDCSBatch = new XmlSlurper(false,false).parseText(request.getBody().toString());

def sender = processCreateDCSBatch.ApplicationArea.Sender.text();
def receiver = processCreateDCSBatch.ApplicationArea.Receiver.text();

def batchID = processCreateDCSBatch.DataArea.BatchID.text();
def recipeID = processCreateDCSBatch.DataArea.MasterRecipeID.text();
def scaledSize = processCreateDCSBatch.DataArea.ScaledSize.text();
def formulaID = processCreateDCSBatch.DataArea.FormulaID.text();
def description = processCreateDCSBatch.DataArea.Description.text();

exchange.setProperty("sender", sender)
exchange.setProperty("receiver", receiver)

//Steps / Equipment requirements

def stepsSequence = []

processCreateDCSBatch.DataArea.ControlRecipe.EquipmentRequirements.each { elt ->
  elt.children().each { equipmentRequirement ->
	def eqmRequirement = [
		name : equipmentRequirement.Constraint.text(),
		value : equipmentRequirement.ID.text()]
	
	stepsSequence.add(eqmRequirement)
  }
}

//process parameters
def parameterSequence = []
processCreateDCSBatch.DataArea.ControlRecipe.Parameters.each { elt ->
  elt.children().each { parameter ->
	def param = [
		name : parameter.ID.text(),
		value : getParameterValue(parameter)]
	
	parameterSequence.add(param)
  }
}

def materialSequence = []

def jsonBuilder = new JsonBuilder()
jsonBuilder.createValues {
	recipe recipeID
	batchid batchID
    scale  scaledSize
	description:  description
	steps stepsSequence
	parameters parameterSequence
	materials materialSequence
}


request.removeHeaders("JMS*")
request.removeHeaders("breadc*")
request.removeHeaders("User-Agent*")
request.setHeader(exchange.HTTP_METHOD, "POST")
request.setHeader(exchange.CONTENT_TYPE, "application/json")

return jsonBuilder.toString()
// return message provides the body of the http request
