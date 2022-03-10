package mar.artefacts;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public abstract class XMLProjectInspector extends ProjectInspector {

	public XMLProjectInspector(Path repoFolder, Path projectSubPath) {
		super(repoFolder, projectSubPath);
	}

	protected Document loadDocument(InputStream stream) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setNamespaceAware(true); // never forget this!
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document doc = builder.parse(stream);
		return doc;
	}

	
}
