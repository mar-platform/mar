package mar.rest.api;

import static spark.Spark.post;

import java.io.IOException;

import javax.annotation.Nonnull;

import mar.restservice.services.AbstractAPI;
import mar.restservice.services.IConfigurationProvider;
import mar.restservice.services.InvalidMarRequest;
import mar.restservice.services.ModelDumper;
import mar.restservice.services.SearchOptions;
import mar.restservice.services.SearchOptions.ModelType;
import mar.restservice.services.TransformationService;
import spark.Request;
import spark.Response;

public class TransformationAPI extends AbstractAPI {

	private TransformationService service = new TransformationService(null);
	
	public TransformationAPI(@Nonnull IConfigurationProvider configuration) {
		super(configuration);
	}
	
	public void configure() {
		post("/transformation/ecore-to-emfatic", this::ecoreToEmfatic);
	}
	
	public Object ecoreToEmfatic(Request req, Response res) throws IOException, InvalidMarRequest {		
		String model = req.body();
		try (ModelDumper dumper = new ModelDumper(model, ModelType.ecore)) {
			return service.transformEcoreToEmfatic(dumper.getFile());
		}
	}
}
