package mar.artefacts.acceleo;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class AcceleoInspectorTest {

	@Test
	public void testURIExtraction() {
		String dcl = "[module myModule('http://myDSL', 'anotherDSL')]";
		List<String> uris = AcceleoInspector.getURIs(dcl);
		assertEquals("http://myDSL", uris.get(0));
		assertEquals("anotherDSL", uris.get(1));

		dcl = "[for i in mylist]";
		uris = AcceleoInspector.getURIs(dcl);
		assertEquals(0, uris.size());
	}

}
