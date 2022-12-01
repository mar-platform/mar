package mar.artefacts;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import mar.validation.AnalysisDB;

public abstract class XMLProjectInspector extends ProjectInspector {

	public XMLProjectInspector(Path repoFolder, Path projectSubPath, AnalysisDB db) {
		super(repoFolder, projectSubPath, db);
	}

	protected Document loadDocument(InputStream stream) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setNamespaceAware(true); // never forget this!
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document doc = builder.parse(stream);
		return doc;
	}
	
	protected Metamodel getMetamodelFromHRef(String ref) {
		ref = ref.replace("#/", "");
		Metamodel metamodel;
		if (ref.contains(".ecore")) {
			
			Path path = Paths.get(ref);
			RecoveredPath recovered = getFileSearcher().findFile(path);
			
			metamodel = Metamodel.fromFile(path.toFile().getName(), recovered);
		} else {
			// Assume it is a URI
			metamodel = Metamodel.fromURI(ref, ref);
		}
		return metamodel;
	}

	
}
