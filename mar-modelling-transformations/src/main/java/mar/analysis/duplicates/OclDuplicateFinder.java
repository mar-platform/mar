package mar.analysis.duplicates;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;
import org.eclipse.ocl.parser.OCLLexer;

import lpg.runtime.PrsStream;

public class OclDuplicateFinder<T> extends DuplicateFinder<T, File> {

	public OclDuplicateFinder() {
		super(new OclTokenExtractor());
	}

	private static class OclTokenExtractor implements ITokenExtractor<File> {
	
		@Override
		public List<String> extract(File resource) {	      
			try {
		        var reader = new FileReader(resource);
		        var env = EcoreEnvironmentFactory.INSTANCE.createEnvironment();
		        OCLLexer lexer = new OCLLexer(env, reader, resource.getAbsolutePath());
		        
		        PrsStream prsStream = new PrsStream(lexer.getILexStream());
	
		        /*
				 // Required so token ids match the parser table
		        
				 prsStream.remapTerminalSymbols(
				         lexer.getOrderedTerminalSymbols(),
				         lexer.getEOFToken());
				*/
	
				// Traverse tokens
		        List<String> tokens = new ArrayList<String>();
				for (int i = 1; i <= prsStream.getSize(); i++) {
					//var token = prsStream.getToken(i);
					String text = prsStream.getTokenText(i);
					addToken(tokens, text);
				}
				return tokens;				
			} catch (IOException e) {
				e.printStackTrace();
				return Collections.emptyList();
			}
		}		
	}
	

}
