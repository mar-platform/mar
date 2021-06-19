package mar.restservice.services;

import static spark.Spark.get;
import static spark.Spark.post;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.eclipse.emf.ecore.resource.Resource;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import mar.indexer.common.configuration.IndexJobConfigurationData;
import mar.indexer.common.configuration.ModelLoader;
import mar.indexer.lucene.core.LuceneUtils;
import mar.indexer.lucene.core.Searcher;
import mar.paths.PathFactory.DefaultPathFactory;
import mar.renderers.ecore.EcorePlantUMLRenderer;
import mar.restservice.HBaseGetInfo;
import mar.restservice.HBaseLog;
import mar.restservice.HBaseModelAccessor;
import mar.restservice.HBaseStats;
import mar.restservice.swagger.SwaggerParser;
import spark.Request;
import spark.Response;

@Api
@Path("/v1/search")
@Produces("application/json")
public class API extends AbstractService {
	@NonNull
	private final HBaseStats stats = new HBaseStats();
	@NonNull
	private final HBaseLog hbaseLog = new HBaseLog();	
	
	public API(@Nonnull IConfigurationProvider configuration) {
		super(configuration);
        post("/search", this::search);
        post("/search-full", this::searchList);
        post("/search-text", this::textSearch);
        get("/content", this::getContent);
        get("/render", this::doRender);
        
        post("/v1/search/keyword", this::textSearch);
        post("/v1/search/example", this::searchList);
        get("/v1/search/metadata", this::metadata);
        
        get("/v1/search/swagger", this::swagger);

        new AnalysisAPI(configuration).configure();
        new MachineLearningAPI(configuration).configure();
        
        get("/status", this::doStatus);
	}
	
	// Build swagger json description
	public Object swagger(Request req, Response res) throws IOException, ServletException {
		try {
			String json = SwaggerParser.getSwaggerJson("mar.restservice.services");
			return json;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}
	
	public Object textSearch(Request req, Response res) throws IOException, ServletException {
		// log(req);
		Searcher searcher = getTextSearcher();
		String query = req.body();
		try {
			TopDocs docs = searcher.topDocs(query, DefaultPathFactory.INSTANCE);
			List<ResultItem> items = new ArrayList<ResultItem>();
			for (ScoreDoc doc : docs.scoreDocs) {
				Document document = searcher.getDoc(doc.doc);
				String id = document.getField(LuceneUtils.ID).stringValue();
				String type = document.getField(LuceneUtils.TYPE).stringValue();
				items.add(new ResultItem(id, doc.score, type));				
			}

			// Update the information of each model in batch
			try(HBaseGetInfo info = new HBaseGetInfo()) {
				info.updateInformation(items);
			}
		
			return toJson(res, items);
		} catch (ParseException e) {
			e.printStackTrace();
			// TODO: Return an error
			return null;
		}
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
    	for (Entry<? extends String, Integer> entry : item.getMetadata().getSmells().entrySet()) {
    		String smellId = entry.getKey();
    		Double smellWeight = mrank.get(smellId);
    		if (smellWeight == null)
    			continue;
    		
    		double value = entry.getValue();
    		total = total + Math.log(value + 1) * smellWeight;
		}	
    	return total;
    }
    
	public Object getContent(Request req, Response res) throws IOException {
		String id = req.queryParams("id");
		System.out.println("Content of " + id);
		String content = new HBaseModelAccessor().getModel(id);
		res.type("text/xmi");
		return content;
	}

	@GET
	@ApiOperation(value = "Gets the metadata associated to a given model", nickname="metadata")
	@ApiImplicitParams({ //
			@ApiImplicitParam(required = true, dataType="string", name="id", paramType = "header")
	}) //
	@ApiResponses({
		@ApiResponse(code = 404, message = "Model not found") /* , response=ApiError.class) */
	})
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
	
	public Object doRender(Request req, Response res) throws IOException {
		String id = req.queryParams("id");
		
		String file = getModelFile(id);
		if (file == null)
			throw new IllegalArgumentException("Couldn't find file with id = " + id);
				
		Resource r = ModelLoader.DEFAULT.load(new File(file));
		// For the moment this only works for Ecore diagrams
		EcorePlantUMLRenderer renderer = new EcorePlantUMLRenderer();

		HttpServletResponse raw = res.raw();
		renderer.renderTo(r, raw.getOutputStream());
		raw.getOutputStream().flush();
		raw.getOutputStream().close();

		raw.setContentType("image/png");
		
		return res;
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
