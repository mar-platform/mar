package mar.paths;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PathMapSerializer {

	public static byte[] serialize(Map<String, PairInformation> map) throws IOException {
		int count = map.size();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(out);
		dos.writeInt(count);
		
		map.forEach((k, v) -> {
			try {
				dos.writeUTF(k);
				dos.writeInt(v.getnTokensDoc());
				dos.writeInt(v.getNocurrences());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		
		return out.toByteArray();
	}
	
	public static Map<String, PairInformation> deserialize(byte[] bytes) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(in);
		
		int count = dis.readInt();		
		Map<String, PairInformation> r = new HashMap<String, PairInformation>(count);
		
		for(int i = 0; i < count; i++) {
			String key = dis.readUTF();
			int nTokens = dis.readInt();
			int nOccurrences = dis.readInt();
			r.put(key, new PairInformation(nOccurrences, nTokens));
		}
		
		return r;
	}
}
