package mar.analysis.duplicates;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import mar.models.xtext.XtextTokenizer;

public class XtextDuplicateFinder<T> extends DuplicateFinder<T, File> {

	public XtextDuplicateFinder() {
		super(new XtextTokenExtractor());
	}

	private static class XtextTokenExtractor implements ITokenExtractor<File> {
		private XtextTokenizer tokenizer = new XtextTokenizer();

		@Override
		public List<String> extract(File resource) {
			try {
				List<String> tokens = tokenizer.getTokens(resource.getAbsolutePath());
				return tokens;
			} catch (IOException e) {
				return Collections.emptyList();
			}
		}		
	}
	

}
