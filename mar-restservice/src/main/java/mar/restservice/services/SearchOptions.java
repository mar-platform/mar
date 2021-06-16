package mar.restservice.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;

import spark.Request;
import spark.utils.IOUtils;

/**
 * Parse a request and ensures that the search options are
 * correct.
 *  
 * @author jesus
 *
 */
public class SearchOptions {

	public static enum ModelType {
		ecore, uml, bpmn2, sculptor, pnml, archimate, rds, simulink, xtext
	}
	
	public static enum SyntaxType {
		xmi, 
		emfatic, 
		paths
	}

	private final ModelType modelType;
	private final SyntaxType syntaxType;
	private final int maxResults;
	private final String model;
	
	public SearchOptions(String model, @Nonnull ModelType modelType, @Nonnull SyntaxType syntaxType, @Nonnegative int maxResults) {
		this.model = model;
		this.modelType = modelType;
		this.syntaxType = syntaxType;
		this.maxResults = maxResults;
	}

	public static SearchOptions get(Request req) throws InvalidMarRequest {
		SyntaxType syntaxType;
		String syntax = req.queryParams("syntax");
		if (syntax == null) {
			syntaxType = SyntaxType.xmi;
		} else {
			syntaxType = SyntaxType.valueOf(syntax);
		}
		
		return get(req, syntaxType);
	}
	
	public static SearchOptions get(Request req, @Nonnull SyntaxType syntaxType) throws InvalidMarRequest {
		System.out.println("Request...");
		String max = req.queryParams("max");
		ModelType modelType = getModelType(req);
				
		String model;
		if (syntaxType == SyntaxType.xmi) {
			model = getUploadedFile(req);			
		} else {
			model = req.body();
		}
		
		int maxResults = 100;
		if (max != null) {
			maxResults = Integer.parseInt(max);
			if (maxResults < 0) {
				maxResults = Integer.MAX_VALUE;
			}
		}
				
		System.out.println("Model type: "+ modelType);
	    			
		return new SearchOptions(model, modelType, syntaxType, maxResults);
	}

	public static String getUploadedFile(@Nonnull Request req) throws InvalidMarRequest {
		req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));		
		try (InputStream input = req.raw().getPart("uploaded_file").getInputStream()) {
			if (input == null)
				throw new IllegalStateException();
		                       
			String model = IOUtils.toString(input);
		    if (model.trim().isEmpty()) {
		    	throw new InvalidMarRequest(req, "Empty model");
		    }
		    return model;
		} catch (IOException e) {
			throw new InvalidMarRequest(req, e.getMessage());
		} catch (ServletException e) {
			throw new InvalidMarRequest(req, e.getMessage());
		}
	}
	
	private static ModelType getModelType(Request req) throws InvalidMarRequest {
		String modelType = req.queryParams("model");
		if (modelType == null)
			modelType = req.queryParams("type");
		
		if (modelType == null)
			throw new InvalidMarRequest(req, "Parameter 'type' not given. Possible values: " + Arrays.stream(ModelType.values()).map(ModelType::name).collect(Collectors.joining(", ")));

		modelType = modelType.toLowerCase();		
		return ModelType.valueOf(modelType.trim());
	}

	@Nonnull
	public ModelType getModelType() {
		return this.modelType;
	}

	@Nonnull
	public SyntaxType getSyntaxType() {
		return syntaxType;
	}

	public int getMaxResults() {
		return maxResults;
	}
	
	public String getModel() {
		return model;
	}

}
