package mar.analysis.backend.megamodel.inspectors;

import java.util.List;

import org.junit.Test;

public class EmftextInspectorTest {

	@Test
	public void testParser() throws Exception {
		String text = """
SYNTAXDEF hymapping
FOR <http://hyvar-project.eu/feature/mapping/1.0>
START HyMappingModel

IMPORTS {
	hyexpression : <http://hyvar-project.eu/feature/expression/1.0> WITH SYNTAX expression <../../eu.hyvar.feature.expression/model/Expression.cs>
}				
				""";
		List<String> lines = List.of(text.split("\n"));
		var r = EmftextInspector.EmfTextParser.parse(lines);
		
		System.out.println("mainURI: " + r.mainUri);
		System.out.println("imports: " + r.imports);
		System.out.println("syntaxURIs: " + r.syntaxUris);
	}

}
