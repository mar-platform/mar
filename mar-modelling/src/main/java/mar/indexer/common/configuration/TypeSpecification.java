package mar.indexer.common.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TypeSpecification {

	public static final TypeSpecification DEFAULT_SPEC = new TypeSpecification();
	
	@JsonProperty(required = false)
	private Map<String, Double> mrank = new HashMap<String, Double>();
	
	public TypeSpecification() {
		mrank.put("similarity", 1.0d);
	}
	
	public Map<? extends String, Double> getMrank() {
		return Collections.unmodifiableMap(mrank);
	}
	
	
}
