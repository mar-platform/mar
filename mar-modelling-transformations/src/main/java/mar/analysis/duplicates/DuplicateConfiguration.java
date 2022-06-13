package mar.analysis.duplicates;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.m2m.internal.qvt.oml.cst.UnitCS;

import anatlyzer.atl.model.ATLModel;
import anatlyzer.atl.tests.api.AtlLoader;
import mar.analysis.backend.megamodel.ArtefactType;
import mar.analysis.duplicates.DuplicateComputation.DuplicateFinderConfiguration;
import mar.analysis.megamodel.model.RelationshipsGraph;
import mar.artefacts.FileProgram;
import mar.artefacts.graph.RecoveryGraph;
import mar.artefacts.qvto.QvtoLoader;

public class DuplicateConfiguration {

	private final Path repositoryDataFolder;
	private final Function<FileProgram, String> toId;
	private final Function<FileProgram, String> toName;

	public DuplicateConfiguration(Path repositoryDataFolder, Function<FileProgram, String> toId, Function<FileProgram, String> toName) {
		this.repositoryDataFolder = repositoryDataFolder;
		this.toId = toId;
		this.toName = toName;
	}
	
	public DuplicateComputation newComputation(Map<ArtefactType, Collection<RecoveryGraph>> miniGraphs, RelationshipsGraph graph) {
		DuplicateComputation computation = new DuplicateComputation(miniGraphs, graph);

		computation.addType(ArtefactType.ATL, new DuplicateFinderConfiguration<FileProgram, ATLModel>() {
			@Override
			public ATLModel toResource(FileProgram p) throws Exception {
				Resource r = AtlLoader.load(p.getFilePath().getCompletePath(repositoryDataFolder).toString());
				ATLModel model = new ATLModel(r, p.getFilePath().getPath().toString());
				return model;
			}

			@Override
			public DuplicateFinder<FileProgram, ATLModel> toFinder() {
				return new ATLDuplicateFinder();
			}

			@Override
			public String toId(FileProgram p) {
				return toId.apply(p);
			}			
			
			@Override
			public String toName(FileProgram p) {
				return toName.apply(p);
			}
		});
		
		computation.addType(ArtefactType.QVTO, new DuplicateFinderConfiguration<FileProgram, UnitCS>() {
			@Override
			public UnitCS toResource(FileProgram p) throws Exception {
				return QvtoLoader.INSTANCE.parse(p.getFilePath().getCompletePath(repositoryDataFolder).toString());
			}

			@Override
			public DuplicateFinder<FileProgram, UnitCS> toFinder() {
				return new QVToDuplicateFinder();
			}

			@Override
			public String toId(FileProgram p) {
				return toId.apply(p);
			}			
			
			@Override
			public String toName(FileProgram p) {
				return toName.apply(p);
			}
		});		
		
		return computation;
	}

}
