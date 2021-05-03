package mar.models.pnml;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.eclipse.emf.ecore.resource.Resource;
import org.junit.Test;

import mar.common.test.TestUtils;

public class SculptorLoaderTest {

	@Test
	public void testLoad() {
		File file = TestUtils.getExternalResource("sculptor", "IcelyFramework/model.btdesign");
		SculptorLoader loader = new SculptorLoader();
		Resource r = loader.load(file);
		
		assertEquals(1, r.getContents().size());
	}

}
