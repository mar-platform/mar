package mar.artefacts.epsilon;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
import mar.artefacts.RecoveredPath;
import mar.artefacts.XMLProjectInspector;
import mar.artefacts.graph.RecoveryGraph;
import mar.artefacts.graph.RecoveryStats;

/**
 * <pre>
 * <?xml version="1.0" encoding="UTF-8" standalone="no"?>
 * <launchConfiguration type="org.epsilon.evl.eclipse.dt.launching.EvlLaunchConfigurationDelegate">
 *	 <booleanAttribute key="fine_grained_profiling" value="false"/>
 *	 <listAttribute key="models">
 *		<listEntry value="#&#13;&#10;#Wed Jun 18 12:26:16 BST 2014&#13;&#10;isMetamodelFileBased=false&#13;&#10;name=BPMN2&#13;&#10;readOnLoad=true&#13;&#10;storeOnDisposal=false&#13;&#10;aliases=&#13;&#10;cached=true&#13;&#10;fileBasedMetamodelUri=&#13;&#10;metamodelUri=http\://www.eclipse.org/emf/2002/Ecore,http\://www.omg.org/spec/BPMN/20100524/MODEL-XMI&#13;&#10;modelUri=platform\:/resource/BPMN%20Normalization/models/Preprocessing_with_DataObjects_WithNoIO.bpmn2&#13;&#10;type=EMF&#13;&#10;modelFile=/BPMN Normalization/models/Preprocessing_with_DataObjects_WithNoIO.bpmn2&#13;&#10;expand=true&#13;&#10;metamodelFile=&#13;&#10;"/>
 * 	 </listAttribute>
 *	 <booleanAttribute key="profile_model_loading" value="false"/>
 *	 <booleanAttribute key="profiling_enabled" value="false"/>
 *	 <booleanAttribute key="reset_profiler" value="false"/>
 *	 <stringAttribute key="source" value="/BPMN Normalization/src/BPMN_Validation.evl"/>
 * </launchConfiguration>
 *
 * </pre>
 */
public class EpsilonLaunchInspector extends XMLProjectInspector {

	private final XPathExpression FIND_LOAD_MODEL;
	private final XPathExpression FIND_PROGRAMS;
    private final FileSearcher searcher;
    
	public EpsilonLaunchInspector(Path repoFolder, Path projectSubPath) {
		super(repoFolder, projectSubPath);
		this.searcher = new FileSearcher(repoFolder, getProjectFolder());
		
		try {
			XPathFactory xpathfactory = XPathFactory.newInstance();
			XPath xpath = xpathfactory.newXPath();
			FIND_LOAD_MODEL = xpath.compile("//listAttribute[@key = 'models']/listEntry[@value]");
			FIND_PROGRAMS = xpath.compile("//stringAttribute[@key = 'source']");
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}	    		
	}

	@Override
	public RecoveryGraph process(File f) throws Exception {
		RecoveryStats.PerFile stats = new RecoveryStats.PerFile(f.toPath(), "eclipse-launcher");
		RecoveryGraph graph = new RecoveryGraph(stats);
				
		Document doc = loadDocument(new FileInputStream(f));		
		Node root = doc.getFirstChild();
		if (root == null)
			return null;
		
		if (! "launchConfiguration".equals(root.getNodeName()))
			return null;
		
		Node type = root.getAttributes().getNamedItem("type");
		if (! type.getNodeValue().startsWith("org.epsilon"))
			return null;
		
		stats.setPotentialPrograms(1);
		
		NodeList models = (NodeList) FIND_LOAD_MODEL.evaluate(doc, XPathConstants.NODESET);
		NodeList programs = (NodeList) FIND_PROGRAMS.evaluate(doc, XPathConstants.NODESET);

		if (models.getLength() == 0)
			return null;
		
		if (programs.getLength() == 0)
			return null;		
		
		Node node = models.item(0);
		//String properties = node.getAttributes().getNamedItem("value").getTextContent();
		Properties properties = new Properties();
		properties.load(new ByteArrayInputStream(node.getAttributes().getNamedItem("value").getTextContent().getBytes()));
		
		String metamodelDefinition = properties.getProperty("metamodelUri");
		if (metamodelDefinition == null) {
			// This typically happen with excel, xml, etc. that are processed "dynamically" via Epsilon drivers
			// Example:
			//   quratulain-york/org.eclipse.epsilon/examples/org.eclipse.epsilon.examples.emc.simulink.emf/run/liveValidation.launch
			// We could analyse the "type = simulink" info and determine the meta-model 
			// We just return an empty graph			
			return graph;
		}
		String[] uris = metamodelDefinition.split(",");
		if (uris.length == 0)
			return null;
				
		String program = programs.item(0).getAttributes().getNamedItem("value").getTextContent();
		if (program.startsWith("/")) {
			program = program.substring(1);
		}
		
		RecoveredPath programPath = searcher.findFile(Paths.get(program));
		
		EpsilonProgram epsilonProgram = new EpsilonProgram(programPath);		
		graph.addProgram(epsilonProgram);
		for (String uri : uris) {
			Metamodel mm = Metamodel.fromURI(uri, uri);
			epsilonProgram.addMetamodel(mm, MetamodelReference.Kind.TYPED_BY);
			graph.addMetamodel(mm);
		}
		
		stats.addRecoveredProgram(epsilonProgram);
		
		return graph;
	}

}
