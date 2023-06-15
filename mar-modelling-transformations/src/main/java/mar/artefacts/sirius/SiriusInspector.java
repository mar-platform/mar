package mar.artefacts.sirius;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
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

import anatlyzer.atl.util.ATLUtils.ModelInfo;
import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
import mar.artefacts.RecoveredPath;
import mar.artefacts.XMLProjectInspector;
import mar.artefacts.db.RepositoryDB;
import mar.artefacts.graph.RecoveryGraph;
import mar.artefacts.graph.RecoveryStats;
import mar.artefacts.search.MetamodelSeacher;
import mar.artefacts.search.MetamodelSeacher.RecoveredMetamodelFile;
import mar.validation.AnalysisDB;

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
	private final XPathExpression FIND_DOMAIN_CLASS;
	
	public SiriusInspector(Path repositoryDataFolder, Path projectPath, AnalysisDB analysisDb, RepositoryDB repoDb) {
		super(repositoryDataFolder, projectPath, analysisDb, repoDb);
		
		try {
			XPathFactory xpathfactory = XPathFactory.newInstance();
			XPath xpath = xpathfactory.newXPath();
			FIND_METAMODEL= xpath.compile(".//metamodel/@href");
			FIND_DOMAIN_CLASS = xpath.compile(".//@domainClass");
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
		RecoveryGraph graph = new RecoveryGraph(getProject(), stats);
		
		SiriusProgram program = new SiriusProgram(new RecoveredPath(getRepositoryPath(f)));
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
			RecoveredMetamodelFile metamodel = recoverImplicitMetamodel(doc);
			if (metamodel.isValid()) {
				Metamodel mm = metamodel.getBestMetamodel();

				// TODO: Possibly mark this metamodel specially
				graph.addMetamodel(mm);
				program.addMetamodel(mm, MetamodelReference.Kind.TYPED_BY);
			}
		}
		
		return graph;
	}

	private RecoveredMetamodelFile recoverImplicitMetamodel(Document doc) throws XPathExpressionException {
		NodeList result = (NodeList) FIND_DOMAIN_CLASS.evaluate(doc, XPathConstants.NODESET);
		Set<String> classFootprints = new HashSet<>();
		for(int i = 0, len = result.getLength(); i < len; i++) {
			Node attr = result.item(i);
			String qname = attr.getTextContent();
			String[] parts = qname.split("\\.");			
			classFootprints.add(parts[parts.length - 1]);
		}
		
		MetamodelSeacher ms = getMetamodelSearcher();
		return ms.search(classFootprints);
	}

}
