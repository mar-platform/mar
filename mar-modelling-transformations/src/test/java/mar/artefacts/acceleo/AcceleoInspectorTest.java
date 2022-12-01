package mar.artefacts.acceleo;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.junit.Test;

public class AcceleoInspectorTest {

	@Test
	public void testURIExtraction() throws IOException {
		String dcl = "[module myModule('http://myDSL', 'anotherDSL')]";
		List<String> uris = AcceleoInspector.getURIs(new StringReader(dcl));
		assertEquals("http://myDSL", uris.get(0));
		assertEquals("anotherDSL", uris.get(1));

		dcl = "[for i in mylist]";
		uris = AcceleoInspector.getURIs(new StringReader(dcl));
		assertEquals(0, uris.size());
	}

	@Test
	public void testURIExtraction_WithLines() throws IOException {
		String dcl = 
				"[module test::module::MyTrafo("
				+ "'http://myDSL')]";
		List<String> uris = AcceleoInspector.getURIs(new StringReader(dcl));
		assertEquals("http://myDSL", uris.get(0));
		
		dcl = "/** This is a comment */" + "\n" +
				"[module test::module::MyTrafo('http://myDSL', " + "\n" + 
				" 'anotherDSL')" + "\n" +
				"]";
		uris = AcceleoInspector.getURIs(new StringReader(dcl));
		assertEquals("http://myDSL", uris.get(0));
		assertEquals("anotherDSL", uris.get(1));
	}
	
	@Test
	public void testMoreMultiline() throws IOException {
		String dcl = "[comment encoding = UTF-8 /]\n"
				+ "[module generateStandardLibraryAsTextile(\n"
				+ "'http://www.eclipse.org/emf/2002/Ecore',\n"
				+ "'http://www.eclipse.org/ocl/3.1.0/Pivot',\n"
				+ "'http://www.eclipse.org/ocl/3.2.0/Markup')/]\n"
				+ "";
		List<String> uris = AcceleoInspector.getURIs(new StringReader(dcl));
		assertEquals(3, uris.size());
		
	}

}
