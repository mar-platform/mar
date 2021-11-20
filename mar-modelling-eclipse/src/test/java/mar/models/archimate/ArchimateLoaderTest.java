package mar.models.archimate;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.eclipse.emf.ecore.resource.Resource;
import org.junit.Test;

import mar.common.test.TestUtils;

public class ArchimateLoaderTest {

	@Test
	public void testLoad() throws IOException {
		File file = TestUtils.getExternalResource("archimate", "alastria/Alastria.archimate");
		ArchimateLoader loader = new ArchimateLoader();
		Resource r = loader.toEMF(file);
		
		assertEquals(1, r.getContents().size());
	}

}
