package mar.artefacts.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;

import org.junit.Test;

import mar.artefacts.RecoveredPath.Ant;
import mar.artefacts.utils.AntUtils;

public class RecoveryGraphTests {

	@Test
	public void testFileRecovery_BaseDir() {		
		Ant path = AntUtils.parseAntPath(Paths.get("test/folder"), "${basedir}/sm/sm.ecore");		
		assertFalse(path.isLoosyFilePath());
		assertEquals("test/folder/sm/sm.ecore", path.getPath());
	}

	@Test
	public void testFileRecovery_Other() {		
		Ant path = AntUtils.parseAntPath(Paths.get("test/folder"), "${mypath}/sm/sm.ecore");
		assertTrue(path.isLoosyFilePath());
		assertEquals("sm/sm.ecore", path.getPath());
	}

}
