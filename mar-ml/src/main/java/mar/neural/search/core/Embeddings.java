package mar.neural.search.core;

import java.util.List;

public class Embeddings {
	private List<Double> emb;

	public Embeddings(List<Double> emb) {
		super();
		this.emb = emb;
	}

	public List<Double> getEmb() {
		return emb;
	}

	public void setEmb(List<Double> emb) {
		this.emb = emb;
	}
	
	public double cosSim(Embeddings e) {
		double sc = 0;
		double normthis = 0;
		double norme = 0;
		if (e.emb.size() == this.emb.size()) {
			for(int i = 0; i < e.emb.size(); i++){
	            sc = sc + e.emb.get(i) * this.emb.get(i);
	            normthis = normthis + Math.pow(this.emb.get(i), 2);
	            norme = norme + Math.pow(e.emb.get(i), 2);
	        }
		}
		
		if (norme == 0 || normthis == 0)
			return 0;
		
		return sc/(Math.sqrt(normthis*norme));
	}
}
