import groovy.xml.*;
import groovy.transform.*;
import org.apache.camel.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import com.rockwell.integration.b2mml.B2mmlUoMConversion;
import com.rockwell.integration.b2mml.B2mmlLocation;
import com.rockwell.integration.b2mml.B2mmlEquipmentElementLevel;
import com.rockwell.integration.b2mml.B2mmlMaterialUoMConversion;
import com.rockwell.integration.b2mml.B2mmlMaterialUoMConversionItem;
import com.rockwell.integration.b2mml.B2mmlMaterialUoMConversionItems;

//define the LOGGER we eventually want to use...
LOGGER = org.apache.logging.log4j.LogManager.getLogger("Rockwell")

//define timestamp format string used in XML
@Field def sdf = new SimpleDateFormat("yyyy-M-d'T'H:m:s");

// convenient function to convert timestamp string in sdf-format to Calendar object
def xmlTimeStampToCalendar (timestampString) {
    return sdf.parse(timestampString).toCalendar();
}

// convenient function to convert long string to Long object
def xmlLongtoLong (longString) {
    return new Long(longString);
}


//exchange:
//-> the filename that is currently processed...
def fileName = exchange.getProperty("inProcessFileNameWithPath");

//the incoming document
def uomConversionDoc = new XmlSlurper(false,false).parseText(request.getBody(String.class));

LOGGER.debug (uomConversionDoc.dump());

//the outgoing POJO
def bmmlUomConversionBean = new B2mmlUoMConversion();

// we eventually want to some sanity checks on the uomConversionDoc upfront to the mapping


// let's map uomConversionDoc->bmmlUomConversionBean
// remark: uomConversionDoc is a hierarchical document. We need to iterate over the structure
bmmlUomConversionBean.with  {
    // root level: uomConversionDoc -> bmmlUomConversionBean mapping
    id 	= uomConversionDoc.ID.text();
    inFileName = fileName;

    any = [
        uomConversionDoc.Any.EIG.text()
    ];

    /*if (uomConversionDoc.Any.EIG.size() == 0) {
     verb = "CREATE";
     } else {
     verb = uomConversionDoc.Any.EIG.text().substring(5,6);
     }*/
    verb = "CREATE";

    location = new B2mmlLocation();


    location.equipmentElementLevel = new B2mmlEquipmentElementLevel();
    location.equipmentElementLevel.otherValue = uomConversionDoc.Location.EquipmentElementLevel.text();

    location.equipmentID = uomConversionDoc.Location.EquipmentID.text();



    // second level: MaterialUomConversion -> materialUoMConversion
    materialUoMConversion = []
    uomConversionDoc.MaterialUomConversion.each { entry->
        LOGGER.debug ("entry: " + entry.dump());
        matConversionEntry = new B2mmlMaterialUoMConversion();

        matConversionEntry.lastChange = xmlTimeStampToCalendar(entry.LastChange.text());
        matConversionEntry.createdOn = xmlTimeStampToCalendar(entry.CreatedOn.text());
        matConversionEntry.materialID = entry.MaterialID.text();
        matConversionEntry.baseUom = entry.BaseUom.text();

        LOGGER.debug ("matConversionEntry: " + matConversionEntry.dump());
        // third level: the actual items; item -> ItemList
        materialUoMConversionItems = [];
        entry.MaterialUomConversionItems.item.each { itemEntry->
            conversionItem = new B2mmlMaterialUoMConversionItem();
            conversionItem.altUnit = itemEntry.AltUnit.text();
            conversionItem.altUnitIso = itemEntry.AltUnitIso.text();
            conversionItem.numerator = xmlLongtoLong(itemEntry.Numerator.text());
            conversionItem.denominator = xmlLongtoLong(itemEntry.Denominator.text());
            LOGGER.debug ("conversionItem: " + conversionItem.dump());
            materialUoMConversionItems << conversionItem;
        }
        matConversionEntry.materialUoMConversionItems = new B2mmlMaterialUoMConversionItems();
        matConversionEntry.materialUoMConversionItems.setItemList(materialUoMConversionItems);

        materialUoMConversion << matConversionEntry;
    }

    //any -> any
    any = [
        uomConversionDoc.Any.EIG.text()
    ];
}

LOGGER.debug(bmmlUomConversionBean.dump());

return bmmlUomConversionBean;

