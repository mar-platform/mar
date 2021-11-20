package mar.modelling.loader;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.resource.Resource;

/**
 * Given a file or a textual description, a loader is able to load the model
 * of a specific type (each loader handles a type of model) and returns it in 
 * some modelling framework formalism. 
 * 
 * For the moment, only support for EMF models is considered.
 * 
 * @author jesus
 *
 */
public interface ILoader {
	
	@Nonnull
	public Resource toEMF(@Nonnull File file) throws IOException;
	
//	@Nonnull
//	public Resource toEMF(@Nonnull String fileContents);

}
