package mar.paths;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class PairSerializer extends Serializer<PairInformation>{

	@Override
	public void write(Kryo kryo, Output output, PairInformation object) {
		output.writeInt(object.getNocurrences());
		output.writeInt(object.getnTokensDoc());
		
	}


	@Override
	public PairInformation read(Kryo kryo, Input input, Class<PairInformation> type) {
		PairInformation pair = new PairInformation();
		pair.setNocurrences(input.readInt());
		pair.setnTokensDoc(input.readInt());
		return pair;
	}

}
