package mar.analysis.duplicates;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.*;
import java.util.regex.*;

public class SiriusDuplicateFinder<T> extends DuplicateFinder<T, File> {

	public SiriusDuplicateFinder() {
		super(new OdesignTokenExtractor());
	}

	private static class OdesignTokenExtractor implements ITokenExtractor<File> {
		
		@Override
		public List<String> extract(File resource) {
			try {

		        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        Document doc = builder.parse(resource);

		        doc.getDocumentElement().normalize();
		        
		        List<String> tokens = new ArrayList<String>(); 
		        
		        traverse(doc.getDocumentElement(), tokens);
				return tokens;
			} catch (IOException | SAXException | ParserConfigurationException e) {
				return Collections.emptyList();
			}
		}		

		private static List<String> featureNames = List.of("name", "featureName", "typeName", "domainClass");
		private static List<String> featureExpressions = List.of("valueExpression", "conditionExpression", "labelExpression", "preconditionExpression", "semanticCandidatesExpression", "predicateExpression", "sizeComputationExpression");
		
	    private void traverse(Node node, List<String> tokens) {
	        if (node.getNodeType() == Node.ELEMENT_NODE) {
	            Element element = (Element) node;

	            // Extract "name" attribute
	            for (String attrName : featureNames) {
	            	if (element.hasAttribute(attrName)) {
	            		tokens.add(element.getAttribute(attrName));
	            	}
				}
	            
	            for (String attrName : featureExpressions) {
	            	if (element.hasAttribute(attrName)) {
	            		String expression = element.getAttribute(attrName);
	            		processExpression(expression, tokens);
	            	}
				}
	        }

	        NodeList children = node.getChildNodes();
	        for (int i = 0; i < children.getLength(); i++) {
	            traverse(children.item(i), tokens);
	        }
	    }

	}

    // var, feature, aql
	private static void processExpression(String expression, List<String> tokens) {
		if (expression.startsWith("var:")) {
			tokens.add(expression.substring("var:".length()));
		} else if (expression.startsWith("feature:")) {
			tokens.add(expression.substring("feature:".length()));
		} else if (expression.startsWith("aql:")) {
			String aql = expression.substring("aql:".length());
			tokens.addAll(AQLClassExtractor.extractClassNames(aql));
		} else {
			// Just a literal expression
		}		
	}


	private static class AQLClassExtractor {

	    // Regex patterns for common AQL type references
	    private static final Pattern CLASS_PATTERN = Pattern.compile(
	        "(?:\\w+::)?([A-Z][A-Za-z0-9_]*)"
	    );

	    public static List<String> extractClassNames(String aqlExpression) {
	        List<String> classes = new ArrayList<>();

	        Matcher matcher = CLASS_PATTERN.matcher(aqlExpression);

	        while (matcher.find()) {
	            classes.add(matcher.group(1));
	        }

	        return classes;
	    }

	    public static void main(String[] args) {

	        String aql = "aql:self.oclIsTypeOf(MyPackage::MyClass) "
	                   + "and self.oclIsKindOf(AnotherClass) "
	                   + "and someVar.oclAsType(ThirdClass)";

	        List<String> classNames = extractClassNames(aql);

	        System.out.println("Extracted classes:");
	        for (String name : classNames) {
	            System.out.println(name);
	        }
	    }
	}
	
}
