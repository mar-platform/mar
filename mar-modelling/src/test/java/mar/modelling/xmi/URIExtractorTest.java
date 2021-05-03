package mar.modelling.xmi;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class URIExtractorTest {

	@Test
	public void testWithPrefix() throws Exception {
		String header = "<BlockDiagram:BlockDiagram xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:BlockDiagram=\"http://www.ni.com/LabVIEW.VI\" Id=\"acd15747dc7443edbb21f502473cf08e\">";
		List<String> uris = URIExtractor.matchURIs(header);
		assertTrue(uris.contains("http://www.ni.com/LabVIEW.VI"));
	}
	
	@Test
	public void testWithoutPrefix() {
		String header = "<xmi:XMI xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.ni.com/LabVIEW.VI\">";
		List<String> uris = URIExtractor.matchURIs(header);
		assertTrue(uris.contains("http://www.ni.com/LabVIEW.VI"));
	}

}
