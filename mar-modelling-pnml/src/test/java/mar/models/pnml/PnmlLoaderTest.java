package mar.models.pnml;

import java.io.File;

import org.junit.Test;

import mar.common.test.TestUtils;

public class PnmlLoaderTest {

	@Test
	public void testLoad() {
		File file = TestUtils.getExternalResource("pnml-documents", "philo.pnml");
		PnmlLoader loader = new PnmlLoader();
		loader.toEMF(file);
	}

}
