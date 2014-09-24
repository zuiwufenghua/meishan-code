package WordStructure;

import mason.utils.PinyinComparator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class CorpusTask4to5 {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Map<String, Map<String, Integer>> wordstructure = new HashMap<String, Map<String, Integer>>();
		extractTask5Dict(args[0], wordstructure);
		
		printDict(args[1], wordstructure);
	}
	
	
	public static void extractTask5Dict(String inputFile, Map<String, Map<String, Integer>> wordstructure) throws Exception
	{
		BufferedReader bf=new BufferedReader(new InputStreamReader(new FileInputStream(inputFile),"UTF-8"));
		String sLine = null;
		
		while((sLine=bf.readLine())!=null )
		{	
			if(sLine.trim().length() < 4)continue;
			
			final PennTreeReader reader = new PennTreeReader(
					new StringReader(sLine.trim()));
			Tree<String> tree = reader.next();
			Tree<String> subTree1 = tree;

			while (subTree1.getLabel().equalsIgnoreCase("root")
					|| subTree1.getLabel().equalsIgnoreCase("top")) {
				tree = tree.getChild(0);
				subTree1 = subTree1.getChild(0);
			}
			String topLabel = subTree1.getLabel().trim();
			int freqStartIndex = topLabel.indexOf("]#") + 2;
			int posStartIndex = topLabel.lastIndexOf("][") + 2;
			String word = topLabel.substring(1, posStartIndex-2);
			String pos = "";
			int freq = 1;
			if(freqStartIndex != 1)
			{
				pos = topLabel.substring(posStartIndex, freqStartIndex-2);
				freq = Integer.parseInt(topLabel.substring(freqStartIndex));
			}
			else
			{
				pos = topLabel.substring(posStartIndex);
			}
								
			
			String key = String.format("[%s][%s]", word, pos);
			if(!wordstructure.containsKey(key))
			{
				wordstructure.put(key, new HashMap<String, Integer>());
			}
			
			for(Tree<String> oneNonTerminalTree :subTree1.getNonTerminals())
			{
				String curNonTerminalLabel = oneNonTerminalTree.getLabel();
				oneNonTerminalTree.setLabel(curNonTerminalLabel.substring(curNonTerminalLabel.length()-1));
			}
			//Tree<String> newRoot = new Tree<String>(key);
			//List<Tree<String>> children = new ArrayList<Tree<String>>();
			//children.add(subTree1);
			//newRoot.setChildren(children);
			subTree1.setLabel(key);
			String secondKey = subTree1.toString().trim();
			
			if(!wordstructure.get(key).containsKey(secondKey))
			{
				wordstructure.get(key).put(secondKey, freq);
			}
			else
			{
				wordstructure.get(key).put(secondKey, wordstructure.get(key).get(secondKey) +freq);
			}			
		}
		
		bf.close();
	}
	
	
	public static void printDict(String outputFile, Map<String, Map<String, Integer>> outDict) throws Exception
	{
		List<Entry<String, Map<String, Integer>>> chapossortlist = new ArrayList<Entry<String, Map<String, Integer>>>(outDict.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		});
		
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8"), false);
		for(Entry<String, Map<String, Integer>> curCharPoslist: chapossortlist)
		{
			List<Entry<String, Integer>> synsortlist = new ArrayList<Entry<String, Integer>>(curCharPoslist.getValue().entrySet());
			
			Collections.sort(synsortlist, new Comparator(){   
				public int compare(Object o1, Object o2) {    
					Map.Entry obj1 = (Map.Entry) o1;
					Map.Entry obj2 = (Map.Entry) o2;
					Integer s1 = (Integer) obj1.getValue();
					Integer s2 = (Integer) obj1.getValue();
					return s1.compareTo(s2)	;
	            }   
			});
			if(synsortlist.size() > 1)
			{
				for(Entry<String, Integer> curSyn: synsortlist)
				{
					final PennTreeReader reader = new PennTreeReader(
							new StringReader(curSyn.getKey().trim()));
					Tree<String> tree = reader.next();
					Tree<String> subTree1 = tree;
	
					while (subTree1.getLabel().equalsIgnoreCase("root")
							|| subTree1.getLabel().equalsIgnoreCase("top")) {
						tree = tree.getChild(0);
						subTree1 = subTree1.getChild(0);
					}
					String newLabel = String.format("%s#%d", subTree1.getLabel(), curSyn.getValue());
					subTree1.setLabel(newLabel);
					out.println(subTree1.toString());
				}
			}
			else
			{
				out.println(synsortlist.get(0).getKey());
			}
		}
		
		out.close();
	}
	
	


}
