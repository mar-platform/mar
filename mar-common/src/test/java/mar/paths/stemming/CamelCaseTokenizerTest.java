package mar.paths.stemming;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CamelCaseTokenizerTest extends CamelCaseTokenizer {

    @Parameters(name = "{index} = {0}")	
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {     
                 { "hangUp", s("hang", "Up") }, 
                 { "hang up thePhone", s("hang", "up", "the", "Phone") },
                 { "this is a sentence", s("this", "is", "a", "sentence") },
                 { "this is\na paragraph", s("this", "is", "a", "paragraph") },                 
                 { "MyClass", s("My", "Class") }, 
                 { "Person", s("Person") },
                 { "lowercase", s("lowercase") },
                 { "Class", s("Class") },           
                 { "HTML", s("HTML") },
                 { "PDFLoader", s("PDF", "Loader") },
                 { "AString", s("A", "String") },
                 { "SimpleXMLParser", s("Simple", "XML", "Parser") },
                 { "GL11Version", s("GL", "11", "Version") },
                 { "99Bottles", s("99", "Bottles") },
                 { "May5", s("May", "5") },
                 { "BFG9000", s("BFG", "9000") },
                 // Weird names, just for stressing the method and making sure that it doesn't crash
                 { "_@2dd#2ad", s("_@2dd", "#2ad") }
           });
    }

	private static String[] s(String... strings) {
		return strings;
	}

	private final String[] expected;
	private final String original;
    
    public CamelCaseTokenizerTest(String original, String[] expected) {
    	this.expected = expected;
    	this.original = original;
	}
	
    @Ignore
	@Test
	public void testTokenize() {
		CamelCaseTokenizer tokenizer = CamelCaseTokenizer.INSTANCE;
		String[] actual = tokenizer.tokenize(original);
		assertArrayEquals(expected, actual);
	}

}
