package mar.artefacts.sirius;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Preconditions;

import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
import mar.artefacts.ProjectInspector;
import mar.artefacts.RecoveredPath;
import mar.artefacts.XMLProjectInspector;
import mar.artefacts.atl.ATLProgram;
import mar.artefacts.epsilon.FileSearcher;
import mar.artefacts.graph.RecoveryGraph;
import mar.artefacts.graph.RecoveryStats;

/**
 * Sirius is a dynamic framework, in the sense that it is only when the .aird file is created
 * to link a concrete model to the editor that types are checked. This means that you can write
 * any domain class name that you want in a e.g., Node Mapping.
 * 
 * There are three scenarios:
 *  * <metamodel href="path to file" />
 *  * <metamodel href="http://your-url/#" />
 *  * There are no metamodel tag. It is possible to construct a tentative meta-model from the node and edge mappings. 
 *    We could look for quasi-compatible meta-models in the project. 
 * 
 * @author jesus
 *
 */
public class SiriusInspector extends XMLProjectInspector {


	private final XPathExpression FIND_METAMODEL;
	private final FileSearcher searcher;
	
	public SiriusInspector(Path repositoryDataFolder, Path projectPath) {
		super(repositoryDataFolder, projectPath);
		this.searcher = new FileSearcher(repoFolder, getProjectFolder());
		
		try {
			XPathFactory xpathfactory = XPathFactory.newInstance();
			XPath xpath = xpathfactory.newXPath();
			FIND_METAMODEL= xpath.compile(".//metamodel/@href");
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}	    
		
	}
	
	@Override
	public RecoveryGraph process(@Nonnull File f) throws Exception {
		Path path = f.getParentFile().toPath();
		Path relativeBuildFolder = repoFolder.relativize(path);
		return process(relativeBuildFolder, f, new FileInputStream(f));
	}
	
	public RecoveryGraph process(@Nonnull Path odesignFolder, @Nonnull File f, @Nonnull InputStream stream) throws Exception {
		Preconditions.checkState(! odesignFolder.isAbsolute());
				
		
		
		RecoveryStats.PerFile stats = new RecoveryStats.PerFile(f.toPath(), "sirius");
		RecoveryGraph graph = new RecoveryGraph(stats);
		
		SiriusProgram program = new SiriusProgram(new RecoveredPath(getRepositoryPath(f)));
		graph.addProgram(program);
		
		Document doc = loadDocument(stream);
		
		NodeList result = (NodeList) FIND_METAMODEL.evaluate(doc, XPathConstants.NODESET);
		
		// Map<String, Metamodel> metamodels = new HashMap<>();
		for(int i = 0, len = result.getLength(); i < len; i++) {
			Node metamodelRef = result.item(i);
			String ref = metamodelRef.getTextContent();
			ref = ref.replace("#/", "");
			Metamodel metamodel;
			if (ref.contains(".ecore")) {
				
				Path path = Paths.get(ref);
				RecoveredPath recovered = searcher.findFile(path);
				
				metamodel = Metamodel.fromFile(path.toFile().getName(), recovered);
			} else {
				// Assume it is a URI
				metamodel = Metamodel.fromURI(ref, ref);
			}

			graph.addMetamodel(metamodel);
			program.addMetamodel(metamodel, MetamodelReference.Kind.TYPED_BY);
		}
		
		// If we can't find any meta-model, the alternative is to extract every domainClass="packageName::ClassName"
		// and try to match other meta-models
		if (graph.getMetamodels().isEmpty()) {
			// 
		}
		
		return graph;
	}

}
