package mar.analysis.duplicates;

import java.io.File;
import java.util.Collections;
import java.util.List;

//import org.eclipse.acceleo.internal.parser.compiler.AcceleoLexer;

public class AcceleoDuplicateFinder<T> extends DuplicateFinder<T, File> {

	public AcceleoDuplicateFinder() {
		super(new OclTokenExtractor());
	}

	private static class OclTokenExtractor implements ITokenExtractor<File> {
	
		@Override
		public List<String> extract(File resource) {	      
			return Collections.emptyList();
		}		
	}
	

}
