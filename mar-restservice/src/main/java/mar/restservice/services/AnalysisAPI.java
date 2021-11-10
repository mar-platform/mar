package mar.restservice.services;

import static spark.Spark.post;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import mar.analysis.smells.Smell;
import mar.analysis.smells.ecore.EcoreSmellCatalog;
import mar.restservice.services.SearchOptions.ModelType;
import mar.validation.AnalysisResult;
import spark.Request;
import spark.Response;

public class AnalysisAPI extends AbstractAPI {
	
	@Nonnull
	private final AnalysisService analysisService;	
	
	public AnalysisAPI(@Nonnull IConfigurationProvider configuration) {
		super(configuration);
		this.analysisService = new AnalysisService();
	}

	public void configure() {	       
		post("/v1/analysis", this::analysis);
		post("/v1/analysis/smells", this::smells);
	}

	public Object analysis(Request req, Response res) throws IOException, InvalidMarRequest {		
		String model = req.body();
		ModelType type = SearchOptions.getModelType(req);
		File temp = File.createTempFile("uploaded-file", type.name());
		try {			
			try (FileWriter writer = new FileWriter(temp)) {			
				writer.append(model);
			}
			AnalysisResult result = analysisService.analyse(temp, type);
			return toJson(res, result);
		} finally {
			temp.delete();
		}
	}
	
	public Object smells(Request req, Response res) throws IOException, InvalidMarRequest {
		// String model = SearchOptions.getUploadedFile(req);
		String model = req.body();
		ResourceSet rs = new ResourceSetImpl();
		Resource r = rs.createResource(URI.createURI("uploaded"));
		r.load(new ByteArrayInputStream(model.getBytes()), null);
		
		Map<String, List<Smell>> smells = EcoreSmellCatalog.INSTANCE.detectSmells(r);
		Map<String, Object> result = new HashMap<>();
		
		smells.forEach((k, v) -> {
			List<String> uris = new ArrayList<>();
			result.put(k, uris);
			for (Smell smell : v) {
				uris.addAll(smell.getSmellyObjectURIs());
			}
		});
		
		return toJson(res, result);
	}

}
