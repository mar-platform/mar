package mar.models.lilypond;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.eclipse.emf.ecore.resource.Resource;
import org.junit.Test;

import mar.common.test.TestUtils;
import mar.models.elysium.LilypondLoader;

public class LilypondLoaderTest {

	@Test
	public void testLoad() {
		File file = TestUtils.getExternalResource("lilypond", "lilyinstarlight/we-are-the-champions.ly");
		
		LilypondLoader loader = new LilypondLoader();
		Resource r = loader.toEMF(file);
		
		assertEquals(1, r.getContents().size());
	}

}
