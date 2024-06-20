package mar.artefacts.epsilon;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Preconditions;

import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
import mar.artefacts.RecoveredPath;
import mar.artefacts.XMLProjectInspector;
import mar.artefacts.db.RepositoryDB;
import mar.artefacts.graph.RecoveryGraph;
import mar.artefacts.graph.RecoveryStats;
import mar.artefacts.utils.AntUtils;
import mar.validation.AnalysisDB;

public class BuildFileInspector extends XMLProjectInspector {

	private final XPathExpression FIND_LOAD_MODEL;
	private final XPathExpression FIND_PROGRAMS;
	private final XPathExpression MODEL_REFS;
	
	public BuildFileInspector(@Nonnull Path repoFolder, @Nonnull Path projectSubPath, AnalysisDB db, RepositoryDB repoDb) {
		super(repoFolder, projectSubPath, db, repoDb);
		
		try {
			XPathFactory xpathfactory = XPathFactory.newInstance();
			XPath xpath = xpathfactory.newXPath();
			FIND_LOAD_MODEL = xpath.compile("//project/target/*[name(.) = 'epsilon.emf.loadModel']");
			// TODO: Include more epsilon languages (evl, flock, etc.)
			FIND_PROGRAMS = xpath.compile("//project/target/*[name(.) = 'epsilon.eol' or name(.) = 'epsilon.etl' or name(.) = 'epsilon.egl' or name(.) = 'epsilon.egx' or name(.) = 'epsilon.evl']");
			MODEL_REFS    = xpath.compile("./model/@ref");
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
	
	public RecoveryGraph process(@Nonnull Path buildFileFolder, @Nonnull File f, @Nonnull InputStream stream) throws Exception {
		Preconditions.checkState(! buildFileFolder.isAbsolute());
				
		RecoveryStats.PerFile stats = new RecoveryStats.PerFile(f.toPath(), "build.xml");
		RecoveryGraph graph = new RecoveryGraph(getProject(), stats);
		
		Document doc = loadDocument(stream);
	 
	    NodeList result = (NodeList) FIND_LOAD_MODEL.evaluate(doc, XPathConstants.NODESET);
	    
	    Map<String, ExtractedMetamodel> metamodels = extractMetamodels(buildFileFolder, result);    
	    Pair<Map<String, String>, Map<String, List<Metamodel>>> aliases = getAliases(result, metamodels);
	    Map<String, String> nameToAlias = aliases.getLeft();
	    Map<String, List<Metamodel>> aliasToModel = aliases.getRight();
	    
	    metamodels.forEach((k, m) -> graph.addMetamodel(m.metamodel));
	    
	    result = (NodeList) FIND_PROGRAMS.evaluate(doc, XPathConstants.NODESET);
	    
	    stats.setPotentialPrograms(result.getLength());
	    
	    for(int i = 0, len = result.getLength(); i < len; i++) {	    	
	    	Node item = result.item(i);
	    	NamedNodeMap attrs = item.getAttributes();
	    	
	      	Node fileNode = attrs.getNamedItem("src");
	    	if (fileNode == null)
	    		continue;
	      	
	    	// TODO: Identify basedir if given in the build.xml
	    	Path basedir = buildFileFolder;	    	
	    	
	    	//RecoveredPath file = AntUtils.parseAntPath(buildFileFolder, basedir, fileNode.getTextContent());
	    	String potentialFileName = AntUtils.parseAntPath2(buildFileFolder, basedir, fileNode.getTextContent());
	    	RecoveredPath file = getFileSearcher().findFile(Paths.get(potentialFileName));
	    	
	    	EpsilonProgram program = new EpsilonProgram(file);
	    	graph.addProgram(program);
	    	
	    	stats.addRecoveredProgram(program);
	    	
	    	NodeList list = (NodeList) MODEL_REFS.evaluate(item, XPathConstants.NODESET);
		    for(int j = 0, lenj = list.getLength(); j < lenj; j++) {
		    	Node mitem = list.item(j);
		    	String modelName = mitem.getTextContent();
		    	if (metamodels.containsKey(modelName)) {
		    		ExtractedMetamodel metamodel = metamodels.get(modelName);
		    		
					List<MetamodelReference.Kind> kinds = new ArrayList<>();
					kinds.add(MetamodelReference.Kind.TYPED_BY);
					if (metamodel.read) 
						kinds.add(MetamodelReference.Kind.INPUT_OF);
					if (metamodel.store) 
						kinds.add(MetamodelReference.Kind.OUTPUT_OF);
		    		
		    		program.addMetamodel(metamodel.metamodel, kinds.toArray(MetamodelReference.EMPTY_KIND));
		    		
		    		String alias = nameToAlias.get(modelName);
		    		if (alias != null && aliasToModel.containsKey(alias)) {
		    			for(Metamodel aliasedMetamodel : aliasToModel.get(alias)) {
		    				if (aliasedMetamodel != metamodel.metamodel)
		    					metamodel.metamodel.addDependent(aliasedMetamodel);		    				
		    			}
		    		}
		    	}
		    }	    	
	    }
	    
	    return graph;
	}

	@Nonnull
	private Pair<Map<String, String>, Map<String, List<Metamodel>>> getAliases(NodeList result, Map<String, ExtractedMetamodel> metamodels) {
		// Try to match aliases
		HashMap<String, String> nameToAlias = new HashMap<>();
		HashMap<String, List<Metamodel>> aliases = new HashMap<>();
	    for(int i = 0, len = result.getLength(); i < len; i++) {	    	
	    	Node item = result.item(i);
	    	NamedNodeMap attrs = item.getAttributes();
	    	
	    	Node aliasesNode = ObjectUtils.firstNonNull(attrs.getNamedItem("aliases"), attrs.getNamedItem("alias"));
	    	if (aliasesNode == null)
	    		continue;

	    	String name  = attrs.getNamedItem("name").getTextContent();
	    	String alias = aliasesNode.getTextContent();
	    	ExtractedMetamodel root = metamodels.get(name);
	    	if (root == null)
	    		continue;
	    	
	    	nameToAlias.put(name, alias);
	    	List<Metamodel> list = aliases.computeIfAbsent(alias, (k) -> new ArrayList<>());
	    	list.add(root.metamodel);	    	
	    }
	    
	    return Pair.of(nameToAlias, aliases);
	}

	
//	private void refineAliases(NodeList result, Map<String, Metamodel> metamodels) {
//		// Try to match aliases
//		HashMap<String, Metamodel> aliases = new HashMap<>();
//	    for(int i = 0, len = result.getLength(); i < len; i++) {	    	
//	    	Node item = result.item(i);
//	    	NamedNodeMap attrs = item.getAttributes();
//	    	
//	    	Node aliasesNode = ObjectUtils.firstNonNull(attrs.getNamedItem("aliases"), attrs.getNamedItem("alias"));
//	    	if (aliasesNode == null)
//	    		continue;
//
//	    	String name  = attrs.getNamedItem("name").getTextContent();
//	    	String alias = aliasesNode.getTextContent();
//	    	Metamodel root = metamodels.get(name);
//	    	Metamodel dependent = metamodels.get(name);
//	    	
//	    	if (!aliases.containsKey(alias)) {
//	    		aliases.put(alias, root);
//	    		continue;
//	    	}
//	    	
//	    	// The alias has already been added, so we assume that the first one (which is already added act as root and others as dependent)
//	    	
//	    	
//	    	if (root != null && dependent != null)
//	    		root.addDependent(dependent);
//	    	
//	    }
//	}

	/**
	 *  Example loadModel tags (from njhurtado/generadorCodigo)
	 *  <epsilon.emf.loadModel name="Sql" modelfile="${models.location}/modeloContribuyentes.xmi" metamodeluri="${.metamodel.uri}" read="true" store="false" />
     *  <epsilon.emf.loadModel name="Arq" modelfile="${models.location}/arqdestino-generado.xmi" metamodeluri="${destinoarq.metamodel.uri}" read="false" store="true" />
	 */
	@Nonnull
	private Map<String, ExtractedMetamodel> extractMetamodels(Path buildFileFolder, NodeList result) {
		Map<String, ExtractedMetamodel> metamodels = new HashMap<>();
		for(int i = 0, len = result.getLength(); i < len; i++) {	    	
	    	Node item = result.item(i);
	    	NamedNodeMap attrs = item.getAttributes();
	    	
	    	Node nameNode = attrs.getNamedItem("name");
	    	Node metamodelfileNode = attrs.getNamedItem("metamodelfile");
	    	Node metamodelURINode  = attrs.getNamedItem("metamodeluri");
	    	
	    	if (nameNode == null || ObjectUtils.firstNonNull(metamodelfileNode, metamodelURINode) == null)
	    		continue;
	    	
	    	String name = nameNode.getTextContent();
	    	Metamodel metamodel;
	    	if (metamodelfileNode != null) {
	    		Path basedir = buildFileFolder;
	    		RecoveredPath path = AntUtils.parseAntPath(buildFileFolder, basedir, metamodelfileNode.getTextContent());
	    		if (! Files.exists(path.getCompletePath(this.repoFolder))) {
	    			// TODO: Somehow loosely match the given file. We need to strip the builddir, etc. from metamodelfileNode.getTextContent()
	    			String loosyPath = AntUtils.stripUnknownElements(metamodelfileNode.getTextContent());
	    			path = getFileSearcher().findFile(Paths.get(loosyPath));
	    		}
	    		metamodel = Metamodel.fromFile(name, path);
	    	} else {
	    		metamodel = Metamodel.fromURI(name, metamodelURINode.getTextContent());
	    	}
	    	
	    	Node readAttr = attrs.getNamedItem("read");
	    	Node storeAttr = attrs.getNamedItem("store");
	    	
	    	boolean read  = readAttr != null && readAttr.getTextContent().toLowerCase().equals("true");
	    	boolean store = readAttr != null && storeAttr.getTextContent().toLowerCase().equals("true");
	    	
	    	metamodels.put(name, new ExtractedMetamodel(metamodel, read, store));
	    }
		return metamodels;
	}
	
	private static class ExtractedMetamodel {
		private final Metamodel metamodel;
		private final boolean read;
		private final boolean store;

		public ExtractedMetamodel(Metamodel metamodel, boolean read, boolean store) {
			this.metamodel = metamodel;
			this.read = read;
			this.store = store;
		}
	}
	
}
