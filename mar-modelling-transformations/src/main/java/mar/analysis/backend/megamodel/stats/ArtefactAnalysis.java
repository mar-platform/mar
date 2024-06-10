package mar.analysis.backend.megamodel.stats;

import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import mar.analysis.backend.megamodel.AnalyserConfiguration;
import mar.analysis.backend.megamodel.Error;
import mar.analysis.backend.megamodel.MegamodelDB;
import mar.analysis.backend.megamodel.RawRepositoryDB;
import mar.analysis.backend.megamodel.RawRepositoryDB.RawFile;
import mar.analysis.megamodel.model.Artefact;

/**
 * Compares the arterfacts in the RawDb and the MegamodelDb.
 * 
 * @author jesus
 */
public class ArtefactAnalysis {

	private RawRepositoryDB rawDb;
	private MegamodelDB megamodelDb;
	private AnalyserConfiguration configuration;

	public ArtefactAnalysis(RawRepositoryDB raw, MegamodelDB mega, AnalyserConfiguration configuration) {
		this.rawDb = raw;
		this.megamodelDb = mega;
		this.configuration = configuration;			
	}
	
	public Result analyse(Set<String> artefactTypes) throws SQLException {
		Result notFoundInMegamodel = filesNotFoundInMegamodel(artefactTypes);
		Result notFoundInRawDb = filesNotFoundInRawDb(artefactTypes);
		
		Preconditions.checkState(notFoundInRawDb.totalArtefacts == notFoundInMegamodel.totalArtefacts);
		Preconditions.checkState(notFoundInRawDb.totalRawFiles == notFoundInMegamodel.totalRawFiles);
		
		
		return new Result(notFoundInRawDb.totalRawFiles, notFoundInRawDb.totalArtefacts, 
				notFoundInMegamodel.notFoundInMegamodel, notFoundInRawDb.notFoundInRawDb,
				notFoundInRawDb.missingArtefacts, notFoundInMegamodel.buildFiles, notFoundInMegamodel.filesWithError, notFoundInMegamodel.typesNotConsidered);
	}
	
	/**
	 * For each type of artefact, it returns which files in the raw repository have no correspondence with an
	 * artefact processed in the mega-model.
	 */
	private Result filesNotFoundInMegamodel(Set<String> artefactTypes) throws SQLException {
		Set<String> ignoredFiles = megamodelDb.getIgnoredFileIds();
		List<RawFile> files = rawDb.getFiles().stream().
				filter(p -> ! configuration.isIgnored(Paths.get(p.getFilepath()))).
				filter(p -> ! ignoredFiles.contains(p.getFilepath())).
				collect(Collectors.toList());

		Map<? extends String, ? extends Artefact> artefacts = megamodelDb.getAllArtefacts();
		
		Map<String, Error> allErrorsById = megamodelDb.getErrors();		
		
		Multimap<String, String> buildFiles = MultimapBuilder.hashKeys().arrayListValues().build();
		
		Multimap<String, String> typesNotConsidered = MultimapBuilder.hashKeys().arrayListValues().build();
		
		Multimap<String, RawFile> byType = MultimapBuilder.hashKeys().arrayListValues().build();
		for (RawFile rawFile : files) {
			if (! artefactTypes.contains(rawFile.getType())) {
				switch(rawFile.getType()) {
				case "maven":
				case "eclipse-launcher":
				case "ant":
					buildFiles.put(rawFile.getType(), rawFile.getId());
					continue;
				}
				
				typesNotConsidered.put(rawFile.getType(), rawFile.getId());				
				// System.out.println("Type not considered: " + rawFile.getType());
				continue;
			}
				
			String artefactId = rawFile.getId();
						
			Artefact artefact = artefacts.get(artefactId);
			// If it is a missing artefact, let's make sure that it is not an erroring artefact
			if (artefact == null && !allErrorsById.containsKey(artefactId)) {
				//out.println("File " + rawFile.getFilepath() + " not found in MegamodelDB");
				byType.put(rawFile.getType(), rawFile);
				continue;
			} 			
		}

		System.out.println("Errors ==> " + allErrorsById.size());
		return new Result(files.size(), artefacts.size(), byType, null, null, buildFiles, megamodelDb.getErrors().keySet(), typesNotConsidered);
	}
	
	/**
	 * For each type of artefact, it returns which files in the megamodel repository have no correspondence with an
	 * artefact in the raw repository.
	 */
	private Result filesNotFoundInRawDb(Set<String> artefactTypes) throws SQLException {
		Set<String> ignoredFiles = megamodelDb.getIgnoredFileIds();
		
		Map<String, RawFile> files = rawDb.getFiles().stream().collect(Collectors.toMap(RawFile::getId, r -> r));
		
		Multimap<String, Artefact> byType = MultimapBuilder.hashKeys().arrayListValues().build();
		Multimap<String, Artefact> missingArtefacts = MultimapBuilder.hashKeys().arrayListValues().build();
		
		Map<? extends String, ? extends Artefact> artefacts = megamodelDb.getAllArtefacts();
		artefacts.forEach((id, artefact) -> {
			if (! ignoredFiles.contains(id)) {
				if (! files.containsKey(id) && !artefact.getFileStatus().equals(Artefact.MISSING_STATUS)) {
					byType.put(artefact.getType(), artefact);
				} 
				if (artefact.getFileStatus().equals(Artefact.MISSING_STATUS)) {
					missingArtefacts.put(artefact.getType(), artefact);
				}
			}
		});
		
		return new Result(files.size(), artefacts.size(), null, byType, missingArtefacts, null, null, null);
	}

	public static class Result {

		@JsonIgnore
		private Multimap<String, Artefact> notFoundInRawDb;
		@JsonIgnore
		private Multimap<String, RawFile> notFoundInMegamodel;
		@JsonIgnore
		private Multimap<String, Artefact> missingArtefacts;
		
		@JsonProperty
		private int totalArtefacts;
		@JsonProperty
		private int totalRawFiles;
		
		@JsonProperty
		private List<String> filesWithError;
		
		@JsonIgnore
		private Multimap<String, String> buildFiles;
		@JsonIgnore
		private Multimap<String, String> typesNotConsidered;
		

		public Result(int totalRawFiles, int totalArtefacts, Multimap<String, RawFile> notFoundInMegamodel, Multimap<String, Artefact> notFoundInRawDb, Multimap<String, Artefact> missingArtefacts, Multimap<String, String> buildFiles, Collection<String> errors, Multimap<String, String> typesNotConsidered) {
			this.notFoundInMegamodel = notFoundInMegamodel;			
			this.notFoundInRawDb = notFoundInRawDb;
			this.totalArtefacts = totalArtefacts;
			this.totalRawFiles = totalRawFiles;
			this.missingArtefacts = missingArtefacts;
			this.buildFiles = buildFiles;
			this.filesWithError = errors != null ? new ArrayList<>(errors) : null;
			if (filesWithError != null)
				Collections.sort(filesWithError);
			
			this.typesNotConsidered = typesNotConsidered;
		}
		
		@JsonProperty
		public int getTotalNotFoundInMegamodel() {
			return notFoundInMegamodel.size();
		}
		
		@JsonProperty
		public int getTotalNotFoundInRawDb() {
			return notFoundInRawDb.size();
		}
		
		@JsonProperty
		public Map<String, Collection<String>> getNotFoundInMegamodel() {
			Map<String, Collection<String>> notFound = new TreeMap<>();			
			notFoundInMegamodel.entries().forEach(entry -> {
				Collection<String> list = notFound.computeIfAbsent(entry.getKey(), (k) -> new ArrayList<String>());
				list.add(entry.getValue().getId());
			});
			return notFound;
		}

		@JsonProperty
		public Map<String, Collection<String>> getNotFoundInRawDb() {
			Map<String, Collection<String>> notFound = new TreeMap<>();			
			notFoundInRawDb.entries().forEach(entry -> {
				Collection<String> list = notFound.computeIfAbsent(entry.getKey(), (k) -> new ArrayList<String>());
				list.add(entry.getValue().getId());
			});
			return notFound;
		}
		
		@JsonProperty
		public Map<String, Collection<String>> getMissingArtefacts() {
			Map<String, Collection<String>> missing = new TreeMap<>();			
			missingArtefacts.entries().forEach(entry -> {
				Collection<String> list = missing.computeIfAbsent(entry.getKey(), (k) -> new ArrayList<String>());
				list.add(entry.getValue().getId());
			});
			return missing;
		}
		
		@JsonProperty
		public int totalMissingArtefacts() {
			return missingArtefacts.size();
		}

		@JsonProperty
		public int totalBuildFiles() {
			return buildFiles.size();
		}
		
		@JsonProperty
		public int totalTypesNotConsidered() {
			return typesNotConsidered.size();
		}
		
		@JsonProperty
		public Set<String> typesNotConsidered() {
			return typesNotConsidered.keySet();
		}
		
		@JsonProperty
		public int totalFilesWithError() {
			return filesWithError.size();
		}
		
		@JsonIgnore
		@JsonProperty
		public Map<String, Double> getArtefactCompletionStats() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Not implemented");
		}

		@JsonProperty
		public boolean isCoherent() {
			int files = totalRawFiles - totalBuildFiles() - totalFilesWithError() - totalTypesNotConsidered();
			int artefats = totalArtefacts - getTotalNotFoundInRawDb() - totalMissingArtefacts();
			return files == artefats;
		}
	}
	
	public static class MegamodelFiles {
		
	}
	
	public static class RawDbFiles {
		
	}
}
