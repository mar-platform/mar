package mar.models.xtext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.Token;
import org.eclipse.xtext.XtextStandaloneSetup;
import org.eclipse.xtext.XtextStandaloneSetupGenerated;
import org.eclipse.xtext.parser.antlr.Lexer;

import com.google.inject.Injector;

public class XtextTokenizer {

	@CheckForNull
	private static Injector injector = null;

	private static Injector getInjector() {
		if (injector == null) {
			XtextStandaloneSetup.doSetup();
			injector = new XtextStandaloneSetupGenerated().createInjectorAndDoEMFRegistration();
		}
		return injector;
	}
	
	public List<String> getTokens(String f) throws IOException {
		Lexer x = getInjector().getInstance(Lexer.class);
		x.setCharStream(new ANTLRFileStream(f));
		
		
		List<String> tokens = new ArrayList<String>();
		while (true) {
			Token t = x.nextToken();
			if (t == Token.EOF_TOKEN)
				break;
			tokens.add(t.getText());
		}
		
		return tokens;
	}
}
