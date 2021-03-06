package hackathon.pro.kabaddi;

import java.io.File;
import java.io.IOException;

import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Builder;

public class JsonToCsvConverter {
	public static void main(String[] args) throws JsonProcessingException, IOException {
		JsonNode jsonTree = new ObjectMapper().readTree(new File(KabaddiContants.DATA_DIR_PATH + "2683_player.json"));

		Builder csvSchemaBuilder = CsvSchema.builder();
		JsonNode firstObject = jsonTree.elements().next();
		firstObject.fieldNames().forEachRemaining(fieldName -> {
			csvSchemaBuilder.addColumn(fieldName);
		});
		CsvSchema csvSchema = csvSchemaBuilder.build().withHeader();

		CsvMapper csvMapper = new CsvMapper();
		csvMapper.writerFor(JsonNode.class).with(csvSchema).writeValue(new File(KabaddiContants.DATA_DIR_PATH + "2683_player.csv"),
				jsonTree);
		
		 /*JSONObject output;
		 File jsonFile = new File(KabaddiContants.DATA_DIR_PATH + "stat_player.json");
	        try {
	            output = new JSONObject(jsonFile.toString());


	            JSONArray docs = output.getJSONArray();

	            File file=new File("/tmp2/fromJSON.csv");
	            String csv = CDL.toString(docs);
	           // FileUtils.writeStringToFile(file, csv);
	        } catch (JSONException e) {
	            e.printStackTrace();
	        }        
	    }*/
	}
}
