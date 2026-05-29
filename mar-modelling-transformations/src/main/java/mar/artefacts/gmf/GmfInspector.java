package mar.artefacts.gmf;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

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

import mar.analysis.duplicates.HashDuplicates;
import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
import mar.artefacts.RecoveredPath;
import mar.artefacts.XMLProjectInspector;
import mar.artefacts.db.RepositoryDB;
import mar.artefacts.graph.RecoveryGraph;
import mar.artefacts.graph.RecoveryStats;
import mar.validation.AnalysisDB;

public class GmfInspector extends XMLProjectInspector {
	
	private final XPathExpression FIND_METAMODEL;
	
	public GmfInspector(Path repositoryDataFolder, Path projectPath, AnalysisDB analysisDb, RepositoryDB repoDb) {
		super(repositoryDataFolder, projectPath, analysisDb, repoDb);
		
		try {
			XPathFactory xpathfactory = XPathFactory.newInstance();
			XPath xpath = xpathfactory.newXPath();
			FIND_METAMODEL = xpath.compile(
				    ".//containmentFeature/@href | " +
				    ".//domainMetaElement/@href | " +
				    ".//features/@href | " +
				    ".//editableFeatures/@href"
				);			
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
	
	// Similar to Henshin
	public RecoveryGraph process(@Nonnull Path artefactFolder, @Nonnull File f, @Nonnull InputStream stream) throws Exception {
		Preconditions.checkState(! artefactFolder.isAbsolute());
				
		RecoveryStats.PerFile stats = new RecoveryStats.PerFile(f.toPath(), "gmf");
		RecoveryGraph graph = new RecoveryGraph(getProject(), stats);
		
		GmfProgram program = new GmfProgram(RecoveredPath.newExistingPath(getRepositoryPath(f), repoFolder));
		program.setHash(HashDuplicates.toHash(f));
		graph.addProgram(program);
		
		Document doc = loadDocument(stream);
		
		NodeList result = (NodeList) FIND_METAMODEL.evaluate(doc, XPathConstants.NODESET);
		
		// Map<String, Metamodel> metamodels = new HashMap<>();
		Set<Metamodel> metamodels = new HashSet<>();
		for(int i = 0, len = result.getLength(); i < len; i++) {
			Node metamodelRef = result.item(i);
			String ref = metamodelRef.getTextContent();
			Metamodel metamodel = toMetamodel(ref, getRepositoryPath(f).getParent());
			metamodels.add(metamodel);
			//Metamodel metamodel = getMetamodelFromHRef(ref);
		}
		for (Metamodel metamodel : metamodels) {
			graph.addMetamodel(metamodel);
			program.addMetamodel(metamodel, MetamodelReference.Kind.TYPED_BY);
		}
		
		return graph;
	}

}
