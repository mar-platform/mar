package mar.restservice.tree;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class TreeResultsTest {

	@Test
	public void test() {
		Map<String,List<String>> BoP = new HashMap<String,List<String>>();
		ArrayList<String> l1 = new ArrayList<>();
		l1.add("1");l1.add("2");l1.add("3");l1.add("4");
		ArrayList<String> l2 = new ArrayList<>();
		l2.add("1");l2.add("2");
		ArrayList<String> l3 = new ArrayList<>();
		l3.add("1");l3.add("3");l3.add("4");
		BoP.put("P1", l1);
		BoP.put("P2", l2);
		BoP.put("P3", l3);
		
		TreeResults tr = new TreeResults(BoP, 3);
		System.out.println(tr);
	}

}
