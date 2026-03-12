package mar.analysis.duplicates;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.Token;
import org.eclipse.epsilon.egl.parse.EglLexer;
import org.eclipse.epsilon.egl.parse.EglRecognitionException;
import org.eclipse.epsilon.eol.parse.EolLexer;
import org.eclipse.epsilon.epl.parse.EplLexer;
import org.eclipse.epsilon.etl.parse.EtlLexer;
import org.eclipse.epsilon.evl.parse.EvlLexer;


public class EpsilonDuplicateFinder<T> extends DuplicateFinder<T, File> {

	public EpsilonDuplicateFinder() {
		super(new GenericEpsilonTokenizer());
	}

	public static class GenericEpsilonTokenizer implements ITokenExtractor<File> {

		@Override
		public List<String> extract(File resource) {
		    String code;
			try {
				code = new String(Files.readAllBytes(resource.toPath()));
				if (resource.getName().endsWith("egl") || resource.getName().endsWith("egx")) {
					return extractEgl(resource);
				}
			} catch (IOException e) {
				System.out.println("Can't check duplicate file " + resource.getPath() + " - " + e.getMessage());
				return Collections.emptyList();
			}
			
			
			ANTLRStringStream input = new ANTLRStringStream(code);
	        org.antlr.runtime.Lexer lexer = createLexer(resource.getName(), input);
	        if (lexer == null) {
	            throw new IllegalArgumentException("Unsupported Epsilon language: " + resource);
	        }

	        List<String> tokens = new ArrayList<String>();
	        Token token;
	        while ((token = lexer.nextToken()).getType() != Token.EOF) {
	            /*
	        	System.out.printf(
	                "Token: %-15s Type: %-5d Line: %-3d Column: %-3d%n",
	                token.getText(),
	                token.getType(),
	                token.getLine(),
	                token.getCharPositionInLine()
	            );
	            */
	        	addToken(tokens, token.getText());
	        }
	        
	        return tokens;

		}


	    private List<String> extractEgl(File resource) throws IOException {
	    	EglLexer lexer = new EglLexer(new FileInputStream(resource));
	        Token token;
	        List<String> tokens = new ArrayList<String>();
	    	try {
				while ((token = lexer.nextToken()).getType() != Token.EOF) {
					tokens.add(token.getText());
				}
			} catch (EglRecognitionException e) {
				System.out.println("Can't check duplicate file " + resource.getPath() + " - " + e.getMessage());
				return Collections.emptyList();
			}
	    	return null;
		}


		private static org.antlr.runtime.Lexer createLexer(String filePath, ANTLRStringStream input) {
	        if (filePath.endsWith(".eol")) return new EolLexer(input);
	        if (filePath.endsWith(".etl")) return new EtlLexer(input);
	        if (filePath.endsWith(".evl")) return new EvlLexer(input);	        
	        if (filePath.endsWith(".epl")) return new EplLexer(input);

	        return null;
	    }


	}

}
