package mar.paths.stemming;

import mar.paths.PathFactory.DefaultPathFactory;

public class UMLPathFactory extends DefaultPathFactory {

	@Override
	public ITokenizer getTokenizer() {
		return CamelCaseTokenizer.INSTANCE;
	}
	
}
