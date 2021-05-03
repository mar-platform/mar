package mar.neural.search.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.resource.Resource;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import mar.model2graph.MetaFilter;

public class GetEmbeddings {
	
	public Embeddings getEmbeddings (Resource r, MetaFilter mf) throws IOException{
		
		Gson gson = new Gson();
		URL url = new URL ("http://127.0.0.1:5000/getEmb");
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json; utf-8");
		con.setDoOutput(true);
		
		try(OutputStream os = con.getOutputStream()) {
			GraphModel gm = new GraphModel(r, new LinkedList<>(),mf);
		    byte[] input = gm.generateJson().getBytes("utf-8");
		    os.write(input, 0, input.length);			
		}
		
		
		try(BufferedReader br = new BufferedReader(
				  new InputStreamReader(con.getInputStream(), "utf-8"))) {
				    StringBuilder response = new StringBuilder();
				    String responseLine = null;
				    while ((responseLine = br.readLine()) != null) {
				        response.append(responseLine.trim());
				    }
				    
				    List<String> embString = gson.fromJson(response.toString(),
				    		new TypeToken<List<String>>(){}.getType());
				    
				    List<Double> finalEmb = embString.stream().map(s -> Double.parseDouble(s)).collect(Collectors.toList());
				    
				    
				    return new Embeddings(finalEmb);
				}
		
	}
}
