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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

import edu.berkeley.nlp.parser.EnglishPennTreebankParseEvaluator.*;

public class CLTWSErrorAnalyze {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		InputStreamReader inputData = new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8");
		PennTreeReader treeReader = new PennTreeReader(inputData);
		List<Tree<String>> goldTrees = new ArrayList<Tree<String>>();
		Trees.TreeTransformer<String> treeTransformer = new Trees.StandardTreeNormalizer();
		while (treeReader.hasNext()) {
			goldTrees.add(treeTransformer.transformTree(treeReader.next()));
		}
		inputData.close();
		
		inputData = new InputStreamReader(
				new FileInputStream(args[1]), "UTF-8");
		treeReader = new PennTreeReader(inputData);
		List<Tree<String>> guessTrees = new ArrayList<Tree<String>>();
		while (treeReader.hasNext()) {
			guessTrees.add(treeTransformer.transformTree(treeReader.next()));
		}
		inputData.close();
		
		PrintWriter outputsys = new PrintWriter(System.out);
		
		Set<String> labelsToIgnore = new HashSet<String>();
		labelsToIgnore.add("ROOT");
		labelsToIgnore.add("TOP");
		
		Map<String, Map<String, Integer>> errors = new HashMap<String, Map<String, Integer>>();
		
		WordStructureByWordEval<String> eval_wsbyword = new WordStructureByWordEval<String>(
				labelsToIgnore, new HashSet<String>());
		eval_wsbyword.analyzeMultiple(guessTrees, goldTrees, outputsys, errors);
		outputsys.close();
		
		List<Entry<String, Map<String, Integer>>> chapossortlist = new ArrayList<Entry<String, Map<String, Integer>>>(errors.entrySet());
		
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
			
			if(synsortlist.size() == 1 && synsortlist.get(0).getKey().startsWith("[R]"))
			{
				continue;
			}
			for(Entry<String, Integer> curSyn: synsortlist)
			{
				strout = strout + "\t" + String.format("%s\t%d", curSyn.getKey(), curSyn.getValue());
			}	
			out.println(strout);
			iCount++;
		}
		System.out.println(iCount);
		out.close();
	}

}
