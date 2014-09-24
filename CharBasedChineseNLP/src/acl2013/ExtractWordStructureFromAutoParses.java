package acl2013;

import mason.utils.PinyinComparator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
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
import edu.berkeley.nlp.syntax.Trees;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class ExtractWordStructureFromAutoParses {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		Map<String, String> validWordTree = new HashMap<String, String>();
		InputStreamReader inputData = new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8");
		PennTreeReader treeReader = new PennTreeReader(inputData);

		Trees.TreeTransformer<String> treeTransformer = new Trees.StandardTreeNormalizer();
		while (treeReader.hasNext()) {
			Tree<String> normalizedTree = treeTransformer.transformTree(treeReader.next());
			while(normalizedTree.getLabel().equalsIgnoreCase("root")
					|| normalizedTree.getLabel().equalsIgnoreCase("top"))
			{
				normalizedTree = normalizedTree.getChild(0);
			}
			
			assert(normalizedTree.getLabel().endsWith("#x") || normalizedTree.getLabel().endsWith("#y") || normalizedTree.getLabel().endsWith("#z"));
			
			String theword = normalizedTree.getTerminalStr();
			String thepos = normalizedTree.getLabel().substring(0, normalizedTree.getLabel().indexOf("#"));
			theword = theword+"_"+thepos;
			validWordTree.put(theword, normalizedTree.toString());
		}
		inputData.close();
		
		inputData = new InputStreamReader(
				new FileInputStream(args[1]), "UTF-8");
		
		treeReader = new PennTreeReader(inputData);
		Map<String, Map<String, Integer>> newWordTree = new HashMap<String, Map<String, Integer>>();
		while (treeReader.hasNext()) {
			Tree<String> normalizedTree = treeTransformer.transformTree(treeReader.next());
			
			List<Tree<String>> allNonterminals =  normalizedTree.getNonTerminals();
			
			for(Tree<String> oneTree : allNonterminals)
			{
				if(oneTree.getLabel().endsWith("#t"))
				{
					oneTree = oneTree.getChild(0);
					
					String theword = oneTree.getTerminalStr();
					if(theword.length() < 2) continue;
					String thepos = oneTree.getLabel().substring(0, oneTree.getLabel().indexOf("#"));
					theword = "[" + thepos+"]" +theword+"_"+thepos;
					String theTree = oneTree.toString();
					if(!newWordTree.containsKey(theword))
					{
						newWordTree.put(theword, new HashMap<String, Integer>());
					}
					
					if(!newWordTree.get(theword).containsKey(theTree))
					{
						newWordTree.get(theword).put(theTree, 0);
					}
					
					newWordTree.get(theword).put(theTree, newWordTree.get(theword).get(theTree)+1);
				}
			}
			
		}
		
		inputData.close();
		
		List<Entry<String, Map<String, Integer>>> chapossortlist = new ArrayList<Entry<String, Map<String, Integer>>>(newWordTree.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		}); 
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);
		int iCount = 0;
		for(Entry<String, Map<String, Integer>> curCharPoslist: chapossortlist)
		{
			String strout = curCharPoslist.getKey();
			strout = strout.substring(strout.indexOf("]")+1);
			String goldtree = "---";
			if(validWordTree.containsKey(strout))
			{
				goldtree = validWordTree.get(strout);
				strout = strout + "[wg]";				
			}
			
			List<Entry<String, Integer>> synsortlist = new ArrayList<Entry<String, Integer>>(curCharPoslist.getValue().entrySet());
			
			Collections.sort(synsortlist, new Comparator(){   
				public int compare(Object o1, Object o2) {    
					Map.Entry obj1 = (Map.Entry) o1;
					Map.Entry obj2 = (Map.Entry) o2;
					Integer s1 = (Integer) obj1.getValue();
					Integer s2 = (Integer) obj2.getValue();
					return s2.compareTo(s1)	;
	            }   
			});	
			
			for(Entry<String, Integer> curSyn: synsortlist)
			{
				strout = strout + "\t" + String.format("%s\t%d", curSyn.getKey(), curSyn.getValue());
				if(curSyn.getKey().equals(goldtree))
				{
					strout = strout + "[g]";
				}
			}	
			out.println(strout);
			iCount++;
		}
		System.out.println(iCount);
		out.close();
		

	}

}
