package common.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import mar.paths.stemming.ITokenizer;
import mar.paths.stemming.WhiteSpaceANDCamelCaseTokenizer;

public class NLPTest {

	@Test
	public void test() {
		ITokenizer itok = new WhiteSpaceANDCamelCaseTokenizer();
		String str = "EString";
		
		
		 String tokens[] = itok.tokenize(str); 
		 System.out.println(tokens.length);
		 System.out.println(tokens[0]);
		 System.out.println(tokens[1]);
		 
		 assertEquals(2, tokens.length);
	}

}
