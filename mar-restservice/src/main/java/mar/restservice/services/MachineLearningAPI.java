package mar.restservice.services;

import static spark.Spark.post;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.ws.rs.POST;

import org.eclipse.emf.ecore.resource.Resource;

import io.swagger.annotations.ApiOperation;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import mar.MarConfiguration;
import mar.model2text.Utils;
import mar.restservice.services.SearchOptions.ModelType;
import spark.Request;
import spark.Response;

public class MachineLearningAPI extends AbstractAPI {
	
	public MachineLearningAPI(@Nonnull IConfigurationProvider configuration) {
		super(configuration);
	}

	public void configure() {	       
		post("/v1/analysis/classify", this::classify);
	}
	
	private static final String INFERENCE_URL = "http://localhost:5000/";
	
	@POST
	@ApiOperation(value = "Infer category and tags for a given model", nickname="classify")
	public Object classify(Request req, Response res) throws IOException, InvalidMarRequest {
		//String type = req.queryParams("type");
		//if (type == null) {
		//throw new InvalidMarRequest(req, "No parameter 'type' given");
		//}
		String type = "ecore";
		
		// TODO: Check the type of the model
		String model = req.body();
		Resource r = loadXMI(model, ModelType.ecore);
		
		MarConfiguration config = getConfiguration(ModelType.ecore);
		
		String text = Utils.model2document(r, config.getPathComputation().getFilter(), config.getPathComputation().getPathFactory());
		
		String category = invokeService(text, "category");
		String tags = invokeService(text, "tags");
		List<String> tagList = new ArrayList<>();
		if (tags != null) {
			for(String t : tags.split(",")) {
				String s = t.trim();
				if (! s.isEmpty())
					tagList.add(s);
			}
		}
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("category", category);
		result.put("tags", tagList);
		return toJson(res, result);	
	}

	@Nonnull
	private String invokeService(String text, String service) {
		HttpResponse<String> result = Unirest.get(INFERENCE_URL + service)	
				.queryString("text", text)
				.asString();
		if (result.isSuccess()) {
			return result.getBody();
		} else {
			throw new RuntimeException("Error connecting the inference service");
		}
	}

	private String getInferenceURL(String type) {
		// TODO Auto-generated method stub
		return null;
	}

}
