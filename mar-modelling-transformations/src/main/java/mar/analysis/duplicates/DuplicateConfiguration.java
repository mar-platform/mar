package mar.analysis.duplicates;

import java.io.File;
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
import mar.analysis.duplicates.DuplicateComputation.HashFinderConfiguration;
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
	
	public DuplicateComputation newComputation(Map<ArtefactType, Collection<RecoveryGraph>> miniGraphs, HashDuplicates hashDuplicates) {
		DuplicateComputation computation = new DuplicateComputation(miniGraphs, hashDuplicates);

		computation.addType(ArtefactType.ACCELEO, new HashFinderConfiguration(ArtefactType.ACCELEO, toName, toId));
		computation.addType(ArtefactType.OCL, new HashFinderConfiguration(ArtefactType.OCL, toName, toId));
		computation.addType(ArtefactType.GMF, new HashFinderConfiguration(ArtefactType.GMF, toName, toId));
		computation.addType(ArtefactType.EMFATIC, new HashFinderConfiguration(ArtefactType.EMFATIC, toName, toId));
		computation.addType(ArtefactType.EMFTEXT, new HashFinderConfiguration(ArtefactType.EMFTEXT, toName, toId));
		
		computation.addType(ArtefactType.ATL, new DuplicateFinderConfiguration<FileProgram, ATLModel>() {
			@Override
			public ATLModel toResource(FileProgram p) throws Exception {
				Resource r = AtlLoader.load(p.getFilePath().getCompletePath(repositoryDataFolder).toString());
				ATLModel model = new ATLModel(r, p.getFilePath().getPath().toString());
				return model;
			}

			@Override
			public ArtefactType getType() {
				return ArtefactType.ATL;
			}
			
			@Override
			public DuplicateFinder<FileProgram, ATLModel> toFinder() {
				return new ATLDuplicateFinder<FileProgram>();
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
			public ArtefactType getType() {
				return ArtefactType.QVTO;
			}
			
			@Override
			public DuplicateFinder<FileProgram, UnitCS> toFinder() {
				return new QVToDuplicateFinder<FileProgram>();
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
		
		computation.addType(ArtefactType.XTEXT, new DuplicateFinderConfiguration<FileProgram, File>() {
			@Override
			public File toResource(FileProgram p) throws Exception {
				return p.getFilePath().getCompletePath(repositoryDataFolder).toFile();
			}

			@Override
			public ArtefactType getType() {
				return ArtefactType.XTEXT;
			}
			
			@Override
			public DuplicateFinder<FileProgram, File> toFinder() {
				return new XtextDuplicateFinder<FileProgram>();
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
		
		computation.addType(ArtefactType.EPSILON, new DuplicateFinderConfiguration<FileProgram, File>() {
			@Override
			public File toResource(FileProgram p) throws Exception {
				return p.getFilePath().getCompletePath(repositoryDataFolder).toFile();
			}
			
			@Override
			public ArtefactType getType() {
				return ArtefactType.EPSILON;
			}

			@Override
			public DuplicateFinder<FileProgram, File> toFinder() {
				return new EpsilonDuplicateFinder<FileProgram>();
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
		
		/*
		 * Doesn't work
		computation.addType(ArtefactType.OCL, new DuplicateFinderConfiguration<FileProgram, File>() {
			@Override
			public File toResource(FileProgram p) throws Exception {
				return p.getFilePath().getCompletePath(repositoryDataFolder).toFile();
			}

			@Override
			public ArtefactType getType() {
				return ArtefactType.OCL;
			}
			
			@Override
			public DuplicateFinder<FileProgram, File> toFinder() {
				return new OclDuplicateFinder<FileProgram>();
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
		*/
		
		computation.addType(ArtefactType.HENSHIN, new DuplicateFinderConfiguration<FileProgram, File>() {
			@Override
			public File toResource(FileProgram p) throws Exception {
				return p.getFilePath().getCompletePath(repositoryDataFolder).toFile();
			}
			
			@Override
			public ArtefactType getType() {
				return ArtefactType.HENSHIN;
			}

			@Override
			public DuplicateFinder<FileProgram, File> toFinder() {
				return new HenshinDuplicateFinder<FileProgram>();
			}

			@Override
			public String toId(FileProgram p) {
				return toId.apply(p);
			}			
			
			@Override
			public String toName(FileProgram p) {
				return toName.apply(p);
			}
			
			@Override
			public double default_t0() {
				return 0.7;
			}
			
			@Override
			public double default_t1() {
				return 0.6;
			}
		});
		
		computation.addType(ArtefactType.SIRIUS, new DuplicateFinderConfiguration<FileProgram, File>() {
			@Override
			public File toResource(FileProgram p) throws Exception {
				return p.getFilePath().getCompletePath(repositoryDataFolder).toFile();
			}
			
			@Override
			public ArtefactType getType() {
				return ArtefactType.SIRIUS;
			}

			@Override
			public DuplicateFinder<FileProgram, File> toFinder() {
				return new SiriusDuplicateFinder<FileProgram>();
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
