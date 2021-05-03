package mar.models.archimate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;

import com.archimatetool.model.impl.ArchimatePackage;
import com.archimatetool.model.util.ArchimateXMLProcessor;

public class ArchimateLoader {
	
	@Nonnull
	public Resource load(@Nonnull File f) throws IOException {
		String uri = ArchimatePackage.eINSTANCE.getNsURI();
		if (EPackage.Registry.INSTANCE.containsKey(uri)) {
			EPackage.Registry.INSTANCE.put(uri, ArchimatePackage.eINSTANCE);
		}
		ArchimateXMLProcessor processor = new ArchimateXMLProcessor();
		Resource r = processor.load(new FileInputStream(f.getAbsolutePath()), null);		
		return r;
	}
}
