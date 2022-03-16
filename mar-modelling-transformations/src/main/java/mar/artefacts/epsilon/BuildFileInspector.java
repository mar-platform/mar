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
import mar.artefacts.ProjectInspector;
import mar.artefacts.RecoveredPath;
import mar.artefacts.XMLProjectInspector;
import mar.artefacts.graph.RecoveryGraph;
import mar.artefacts.graph.RecoveryStats;
import mar.artefacts.utils.AntUtils;

public class BuildFileInspector extends XMLProjectInspector {

	private final XPathExpression FIND_LOAD_MODEL;
	private final XPathExpression FIND_PROGRAMS;
	private final XPathExpression MODEL_REFS;
	
	private final FileSearcher searcher;
	
	public BuildFileInspector(@Nonnull Path repoFolder, @Nonnull Path projectSubPath) {
		super(repoFolder, projectSubPath);
		this.searcher = new FileSearcher(repoFolder, getProjectFolder());
		
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
		RecoveryGraph graph = new RecoveryGraph(stats);
		
		Document doc = loadDocument(stream);
	 
	    NodeList result = (NodeList) FIND_LOAD_MODEL.evaluate(doc, XPathConstants.NODESET);
	    
	    Map<String, Metamodel> metamodels = extractMetamodels(buildFileFolder, result);    
	    Pair<Map<String, String>, Map<String, List<Metamodel>>> aliases = getAliases(result, metamodels);
	    Map<String, String> nameToAlias = aliases.getLeft();
	    Map<String, List<Metamodel>> aliasToModel = aliases.getRight();
	    
	    metamodels.forEach((k, m) -> graph.addMetamodel(m));
	    
	    result = (NodeList) FIND_PROGRAMS.evaluate(doc, XPathConstants.NODESET);
	    
	    stats.setPotentialPrograms(result.getLength());
	    
	    for(int i = 0, len = result.getLength(); i < len; i++) {	    	
	    	Node item = result.item(i);
	    	NamedNodeMap attrs = item.getAttributes();
	    	
	      	Node fileNode = attrs.getNamedItem("src");
	    	if (fileNode == null)
	    		continue;
	      	
	    	RecoveredPath file = AntUtils.parseAntPath(buildFileFolder, fileNode.getTextContent());
	    	EpsilonProgram program = new EpsilonProgram(file);
	    	graph.addProgram(program);
	    	
	    	stats.addRecoveredProgram(program);
	    	
	    	NodeList list = (NodeList) MODEL_REFS.evaluate(item, XPathConstants.NODESET);
		    for(int j = 0, lenj = list.getLength(); j < lenj; j++) {
		    	Node mitem = list.item(j);
		    	String modelName = mitem.getTextContent();
		    	if (metamodels.containsKey(modelName)) {
		    		Metamodel metamodel = metamodels.get(modelName);
		    		program.addMetamodel(metamodel, MetamodelReference.Kind.TYPED_BY);
		    		
		    		String alias = nameToAlias.get(modelName);
		    		if (alias != null && aliasToModel.containsKey(alias)) {
		    			for(Metamodel aliasedMetamodel : aliasToModel.get(alias)) {
		    				if (aliasedMetamodel != metamodel)
		    					metamodel.addDependent(aliasedMetamodel);		    				
		    			}
		    		}
		    	}
		    }	    	
	    }
	    
	    return graph;
	}

	@Nonnull
	private Pair<Map<String, String>, Map<String, List<Metamodel>>> getAliases(NodeList result, Map<String, Metamodel> metamodels) {
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
	    	Metamodel root = metamodels.get(name);
	    	if (root == null)
	    		continue;
	    	
	    	nameToAlias.put(name, alias);
	    	List<Metamodel> list = aliases.computeIfAbsent(alias, (k) -> new ArrayList<>());
	    	list.add(root);	    	
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

	@Nonnull
	private Map<String, Metamodel> extractMetamodels(Path buildFileFolder, NodeList result) {
		Map<String, Metamodel> metamodels = new HashMap<>();
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
	    		RecoveredPath path = AntUtils.parseAntPath(buildFileFolder, metamodelfileNode.getTextContent());
	    		if (! Files.exists(path.getCompletePath(this.repoFolder))) {
	    			// TODO: Somehow loosely match the given file. We need to strip the builddir, etc. from metamodelfileNode.getTextContent()
	    			String loosyPath = AntUtils.stripUnknownElements(metamodelfileNode.getTextContent());
	    			path = searcher.findFile(Paths.get(loosyPath));
	    		}
	    		metamodel = Metamodel.fromFile(name, path);
	    	} else {
	    		metamodel = Metamodel.fromURI(name, metamodelURINode.getTextContent());
	    	}
	    		
	    	metamodels.put(name, metamodel);
	    }
		return metamodels;
	}
	
}
