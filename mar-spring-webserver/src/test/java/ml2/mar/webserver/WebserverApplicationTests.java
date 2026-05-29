package ml2.mar.webserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Adjust the arguments to actual test file and repo
 */
@SpringBootTest(args= {"target/test.db", "target/test-repo"})
class WebserverApplicationTests {

	@Test	
	void contextLoads() {
	}

}
