package mar.restservice.services;

import static spark.Spark.get;
import static spark.Spark.post;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;

import org.eclipse.emf.ecore.resource.Resource;

import edu.umd.cs.findbugs.annotations.NonNull;
import mar.indexer.common.configuration.IndexJobConfigurationData;
import mar.renderers.PlantUmlCollection;
import mar.renderers.PlantUmlCollection.PlantUmlImage;
import mar.renderers.ecore.EcorePlantUMLRenderer;
import mar.renderers.uml.UmlPlantUMLRenderer;
import mar.restservice.HBaseGetInfo;
import mar.restservice.HBaseLog;
import mar.restservice.HBaseStats;
import mar.restservice.services.SearchOptions.ModelType;
import mar.restservice.services.SearchService.SearchException;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.freemarker.FreeMarkerEngine;

public class API extends AbstractAPI {

	@NonNull
	private final HBaseStats stats = new HBaseStats();
	@NonNull
	private final HBaseLog hbaseLog = new HBaseLog();
	@Nonnull
	private final SearchService searchService;
	
	public API(@Nonnull IConfigurationProvider configuration) {
		super(configuration);
        post("/search", this::search);
        post("/search-full", this::searchList);
        post("/search-text", this::textSearch);
        
        get("/content", this::getContent);
        
        get("/render/info", this::renderInfo);
        get("/render/diagram", this::doRender);
        get("/render", this::doRender);
        
        post("/v1/search/keyword", this::textSearch);
        post("/v1/search/example", this::searchList);
        get("/v1/search/metadata", this::metadata);
        
        get("/openapi", this::swagger, new FreeMarkerEngine());

        new AnalysisAPI(configuration).configure();
        new MachineLearningAPI(configuration).configure();
        
        get("/status", this::doStatus);
        
        this.searchService = new SearchService(getTextSearcher());
	}
	
	// Build swagger json description
	public ModelAndView swagger(Request req, Response res) throws IOException, ServletException {
		// Return model and view
		return new ModelAndView(null, "openapi.ftl");
	}
	
	public Object textSearch(Request req, Response res) throws SearchException, IOException {
		String query = req.body();
		List<ResultItem> items = searchService.textSearch(query);
		return toJson(res, items);
	}
	
    public Object searchList(Request req, Response res) throws IOException, InvalidMarRequest {
    	Map<String, Double> scores = doSearch(req, res);
		List<ResultItem> items = new ArrayList<ResultItem>(scores.size());
		scores.forEach((k, v) -> items.add(new ResultItem(k, v)));

		// Update the information of each model in batch
		try(HBaseGetInfo info = new HBaseGetInfo()) {
			info.updateInformation(items);
		}
		
		IndexJobConfigurationData configuration = getConfiguration();
		
		for (ResultItem item : items) {
			String type = item.getImplicitType();
			Map<? extends String, Double> mrankConfiguration = configuration.getMrankConfiguration(type);
			double mrankScore = computeMRank(item, mrankConfiguration);
			item.setMrankScore(mrankScore);
		}
		
		return toJson(res, items);
    }
	
    public Object search(Request req, Response res) throws IOException, InvalidMarRequest {
    	Map<String, Double> scores = doSearch(req, res);
    	//Map<String,List<Double>> new_scores = obtainSmells(scores);
	    return toJson(res, scores);
    }
    
    public Map<String, Double> doSearch(Request req, Response res) throws IOException, InvalidMarRequest {
		SearchOptions options = SearchOptions.get(req);
		Map<String, Double> scores = searchAndScore(options.getModel(), options.getModelType(), options.getSyntaxType());
		return firtsElements(scores, options.getMaxResults());		
	}
    
    public double computeMRank(ResultItem item, Map<? extends String, Double> mrank){
    	double total = Math.log(item.getScore() + 1) + mrank.get("similarity");
    	for (Entry<? extends String, List<String>> entry : item.getMetadata().getSmells().entrySet()) {
    		String smellId = entry.getKey();
    		Double smellWeight = mrank.get(smellId);
    		if (smellWeight == null)
    			continue;
    		
    		double value = entry.getValue().size();
    		total = total + Math.log(value + 1) * smellWeight;
		}	
    	return total;
    }
    
	public Object getContent(Request req, Response res) throws IOException, InvalidMarRequest {
		String id = req.queryParams("id");
		if (id == null)
			throw new InvalidMarRequest(req, "Missing 'id' param");
		
		String file = getModelFile(id);
		if (file == null) {
			res.status(404);
			return "Model not found";
		}
		
		res.type("text/xmi");
		String content = Files.readString(Paths.get(file));
		return content;
	}

	@GET
//	@ApiOperation(value = "Gets the metadata associated to a given model", nickname="metadata")
//	@ApiImplicitParams({ //
//			@ApiImplicitParam(required = true, dataType="string", name="id", paramType = "header")
//	}) //
//	@ApiResponses({
//		@ApiResponse(code = 404, message = "Model not found") /* , response=ApiError.class) */
//	})
	public Object metadata(Request req, Response res) throws IOException, InvalidMarRequest {
		String id = req.queryParams("id");
		if (id == null) {
			throw new InvalidMarRequest(req, "Expected id argument");
		}
						
		try(HBaseGetInfo info = new HBaseGetInfo()) {
			String metadata = info.getMetadata(id);
			if (metadata == null) {
				res.status(404);				
			}
			return metadata;
		}		
	}
	
	public Object doRender(Request req, Response res) throws IOException, InvalidMarRequest {
		ModelType type = SearchOptions.getModelType(req);
		String id = req.queryParams("id");
		
		int pos = req.queryParams("pos") == null ? 0 : Integer.parseInt(req.queryParams("pos"));		
		if (pos < 0)
			throw new InvalidMarRequest(req, "'pos' parameter must be positive");
		
		String file = getModelFile(id);
		if (file == null)
			throw new IllegalArgumentException("Couldn't find file with id = " + id);
				
		PlantUmlCollection diagrams = getDiagrams(type, file);
		if (diagrams == null || diagrams.isEmpty()) {
			res.status(500);
			return null;			
		}
		
		if (pos >= diagrams.size())
			throw new InvalidMarRequest(req, "There are " + diagrams.size() + " but 'pos' is " + pos);
		
		
		HttpServletResponse raw = res.raw();	
		diagrams.get(pos).toImage(raw.getOutputStream());
		raw.getOutputStream().flush();
		raw.getOutputStream().close();

		raw.setContentType("image/png");
		
		return res;
	}

	public Object renderInfo(Request req, Response res) throws IOException, InvalidMarRequest {
		ModelType type = SearchOptions.getModelType(req);
		String id = req.queryParams("id");
		
		String file = getModelFile(id);
		if (file == null)
			throw new IllegalArgumentException("Couldn't find file with id = " + id);
				
		PlantUmlCollection diagrams = getDiagrams(type, file);
		if (diagrams == null || diagrams.isEmpty()) {
			res.status(500);
			return null;			
		}

		Map<Object, Object> result = new HashMap<Object, Object>();
		result.put("size", diagrams.size());
		List<Map<Object, Object>> diagramList = new ArrayList<Map<Object,Object>>();
		for (PlantUmlImage image : diagrams) {
			Map<Object, Object> obj = new HashMap<Object, Object>();
			obj.put("href", "/render/diagram?id=" + id + "&type=" + type.name() + "&pos=" + image.getIndex());
			obj.put("index", image.getIndex());
			diagramList.add(obj);
		}
		result.put("diagrams", diagramList);
		
		return toJson(res, result);
	}
 
	@Nonnull
	private PlantUmlCollection getDiagrams(ModelType type, String file) throws IOException {
		Resource r = loadXMI(new File(file), type);
		PlantUmlCollection diagrams;
		switch (type) {
		case ecore:
			EcorePlantUMLRenderer r1 = new EcorePlantUMLRenderer();
			diagrams = r1.render(r);			
			break;
		case uml:
			UmlPlantUMLRenderer r2 = new UmlPlantUMLRenderer();
			diagrams = r2.render(r);						
			break;
		default:
			return null;
		}
		return diagrams;
	}
	
	public Object doStatus(Request req, Response res) throws Exception {	
		boolean forceReload = req.queryParams("forceReload") != null;
		
		Map<? extends String, Integer> models = stats.getStats(forceReload).getCounters();
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> modelList = new ArrayList<>();
		models.forEach((m, v) -> {
			Map<String, Object> model = new HashMap<>();
			model.put("name", m);
			model.put("count", v);
			modelList.add(model);
		});
		result.put("models", modelList);
		return toJson(res, result);
	}
	
}
