package mar.artefacts.henshin;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;

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
import mar.artefacts.RecoveredPath;
import mar.artefacts.XMLProjectInspector;
import mar.artefacts.graph.RecoveryGraph;
import mar.artefacts.graph.RecoveryStats;
import mar.validation.AnalysisDB;

public class HenshinInspector extends XMLProjectInspector {
	
	private final XPathExpression FIND_METAMODEL;
	
	public HenshinInspector(Path repositoryDataFolder, Path projectPath, AnalysisDB analysisDb) {
		super(repositoryDataFolder, projectPath, analysisDb);
		
		try {
			XPathFactory xpathfactory = XPathFactory.newInstance();
			XPath xpath = xpathfactory.newXPath();
			FIND_METAMODEL= xpath.compile(".//imports/@href");					
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
				
		RecoveryStats.PerFile stats = new RecoveryStats.PerFile(f.toPath(), "henshin");
		RecoveryGraph graph = new RecoveryGraph(getProject(), stats);
		
		HenshinProgram program = new HenshinProgram(new RecoveredPath(getRepositoryPath(f)));
		graph.addProgram(program);
		
		Document doc = loadDocument(stream);
		
		NodeList result = (NodeList) FIND_METAMODEL.evaluate(doc, XPathConstants.NODESET);
		
		// Map<String, Metamodel> metamodels = new HashMap<>();
		for(int i = 0, len = result.getLength(); i < len; i++) {
			Node metamodelRef = result.item(i);
			String ref = metamodelRef.getTextContent();
			Metamodel metamodel = getMetamodelFromHRef(ref);

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