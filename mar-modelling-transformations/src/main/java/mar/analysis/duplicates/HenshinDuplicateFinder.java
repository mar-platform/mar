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

public class HenshinDuplicateFinder<T> extends DuplicateFinder<T, File> {

	public HenshinDuplicateFinder() {
		super(new HenshinTokenExtractor());
	}

	private static class HenshinTokenExtractor implements ITokenExtractor<File> {
		
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
				System.out.println("Can't check duplicate file " + resource.getPath() + " - " + e.getMessage());
				return Collections.emptyList();
			}
		}		

	    private void traverse(Node node, List<String> tokens) {
	        if (node.getNodeType() == Node.ELEMENT_NODE) {
	            Element element = (Element) node;

	            // Extract "name" attribute
	            if (element.hasAttribute("name")) {
	                tokens.add(element.getAttribute("name"));
	            }

	            // Extract href references
	            if (element.hasAttribute("href")) {
	                String href = element.getAttribute("href");
	                int idx = href.indexOf("#//");
	                if (idx != -1) {
	                	tokens.add(href.substring(idx + 3));
	                }
	            }
	        }

	        NodeList children = node.getChildNodes();
	        for (int i = 0; i < children.getLength(); i++) {
	            traverse(children.item(i), tokens);
	        }
	    }

	}
	
}
