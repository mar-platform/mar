package mar.models.pnml;

import java.io.File;

import javax.annotation.Nonnull;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

import fr.lip6.move.pnml.framework.general.PNType;
import fr.lip6.move.pnml.framework.hlapi.HLAPIRootClass;
import fr.lip6.move.pnml.framework.utils.PNMLUtils;
import fr.lip6.move.pnml.framework.utils.exception.ImportException;
import fr.lip6.move.pnml.framework.utils.exception.InvalidIDException;

/**
 * 
 * Doc: https://dev.lip6.fr/trac/research/ISOIEC15909/wiki/English/User/Import
 * 
 * @author jesus
 *
 */
public class PnmlLoader {

	public Resource load(@Nonnull File f) {
		try {
			// Load the document. No fall back to any compatible type (false).
			// Fall back takes place between an unknown Petri Net type and the CoreModel.
			HLAPIRootClass rc = PNMLUtils.importPnmlDocument(f, true);
			// PNType type = PNMLUtils.determinePNType(rc);
			EObject obj = (EObject) rc.getContainedItem();
			XMIResourceImpl r = new XMIResourceImpl(URI.createFileURI(f.getPath()));
			r.getContents().add(obj);
			return r;
		} catch (ImportException | InvalidIDException e) {
			throw new RuntimeException(e);
		}
	}

}
