package mar.analysis.duplicates;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.google.common.io.BaseEncoding;

public class HashDuplicates {

	private Map<String, Map<String, Set<String>>> hashesByType = new HashMap<>();
	private Map<String, String> idToHash = new HashMap<String, String>();
	
	public void addFile(String id, String hash, String type) {
		idToHash.put(id, hash);
		Map<String, Set<String>> hashToId = hashesByType.computeIfAbsent(type, (k) -> new HashMap<>());
		Set<String> artefacts = hashToId.computeIfAbsent(hash, (k) -> new HashSet<>());
		artefacts.add(id);
	}	
	
	@CheckForNull
	public Set<? extends String> getDuplicationGroup(String kind, String id) {
		Map<String, Set<String>> hashToId = hashesByType.get(kind);
		if (hashToId == null)
			return null;
		String hash = idToHash.get(id);
		if (hash == null) 
			return null;
		
		Set<String> artefacts = hashToId.get(hash);
		if (artefacts.size() > 1) {
			return artefacts;
		}
		return null;
	}
	
	@CheckForNull
	public static String toHash(@Nonnull File file) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] contents = Files.readAllBytes(file.getAbsoluteFile().toPath());
			md.update(contents);
			byte[] digest = md.digest();
			String myHash = BaseEncoding.base16().lowerCase().encode(digest).toUpperCase();
			// String myHash = DatatypeConverter.printHexBinary(digest).toUpperCase(); // No longer available in Java 11
			return myHash;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	

}
