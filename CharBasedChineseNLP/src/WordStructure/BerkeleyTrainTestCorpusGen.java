package WordStructure;

import mason.utils.PinyinComparator;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.berkeley.nlp.syntax.Tree;


public class BerkeleyTrainTestCorpusGen {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Map<String, Tree<String>> goldTrees = new HashMap<String, Tree<String>>();
		ApplyDict.extractTask5Dict(args[0], goldTrees);
		
		List<Entry<String, Tree<String>>> chapossortlist = new ArrayList<Entry<String, Tree<String>>>(goldTrees.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		});
		
		PrintWriter output_gold = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);
		
		for(Entry<String, Tree<String>> curWordTree: chapossortlist)
		{
			String newStrTree = String.format("( (ROOT %s))", curWordTree.getValue().toString());
			//String newStrTree = curWordTree.getKey() + "\t" + curWordTree.getValue().toString();
			output_gold.println(newStrTree);
		}
		
		output_gold.close();
		
		Map<String, Tree<String>> autoTrees = new HashMap<String, Tree<String>>();
		ApplyDict.extractTask5Dict(args[1], autoTrees);
		
		chapossortlist = new ArrayList<Entry<String, Tree<String>>>(autoTrees.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		});
		
		PrintWriter output_auto = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[3]), "UTF-8"), false);
		
		for(Entry<String, Tree<String>> curWordTree: chapossortlist)
		{
			//String newStrTree = String.format("((ROOT %s))", curWordTree.getValue().toString());
			Tree<String> curTree = curWordTree.getValue();
			List<Tree<String>> preTerminals = curTree.getPreTerminals();
			String newStrTree = preTerminals.get(0).getTerminalStr() + "_" + preTerminals.get(0).getLabel();
			for(int idx = 1; idx < preTerminals.size(); idx++)
			{
				newStrTree = newStrTree + " " + preTerminals.get(idx).getTerminalStr() + "_" + preTerminals.get(idx).getLabel();
			}
			output_auto.println(newStrTree);
		}
		
		output_auto.close();

	}

}
