package mar.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.File;

import org.junit.Test;

import mar.validation.IFileInfo.FileInfo;

public class ResourceAnalyserTest {

	@Test
	public void testRelative() {
		File root = new File("/home/jesus/mydataset/");
		File file = new File("/home/jesus/mydataset/data/myfile.ecore");
		FileInfo info = new FileInfo(root, file);
		
		assertEquals("data/myfile.ecore", info.getModelId());
	}

	@Test
	public void testNonRelative() {
		File root = new File("/home/jesus/mydataset/");
		File file = new File("/home/josea/mydataset/data/myfile.ecore");
		assertThrows("File must be a child of root", IllegalArgumentException.class, () -> {
			new FileInfo(root, file);			
		});
	}

	@Test
	public void testName() {
		File root = new File("/home/jesus/mydataset/");
		FileInfo info = new FileInfo(root, "data/myfile.ecore");
		
		assertEquals("data/myfile.ecore", info.getModelId());
	}
}
