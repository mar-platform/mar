package mar.paths.stemming;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;

import org.junit.Test;

import junit.framework.TestCase;

public class WhiteSpaceANDCamelCaseTokenizerTest extends TestCase {
	@Test
	public void testTokenize() {
		WhiteSpaceANDCamelCaseTokenizer tokenizer = WhiteSpaceANDCamelCaseTokenizer.INSTANCE;
		String[] actual = tokenizer.tokenize("getUserProfile_Message");
		System.out.println(Arrays.toString(actual));
	}
}
