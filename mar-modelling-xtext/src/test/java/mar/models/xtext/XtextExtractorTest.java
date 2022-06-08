package mar.models.xtext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class XtextExtractorTest {

	@Test
	public void testExtractURI() {
		String realURI = "http://www.example.org/domainmodel/Domainmodel";
		String uri = XtextLoader.extractGeneratedURI("generate domainmodel \"" + realURI + "\"");
		assertEquals(uri, realURI);
		
		String noUri = XtextLoader.extractGeneratedURI("generate whatever");
		assertNull(noUri);
	}
	
	@Test
	public void testExtractImportedURI() {
		String line = "import \"http://www.eclipse.org/emf/2002/Ecore\" as ecore";
		String uri = XtextLoader.extractImportedURI(line);
		assertEquals("http://www.eclipse.org/emf/2002/Ecore", uri);
	}
}
