package acl2013;

import mason.utils.PinyinComparator;
import WordStructure.MergeTwoDict;

import java.io.BufferedReader;
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


public class ApplyGoldDict {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Map<String, Tree<String>> goldTrees = new HashMap<String, Tree<String>>();
		LastAnnotation.extractTask5Dict(args[0], goldTrees);
		
		Map<String, Map<String, Integer>> partiWordAnnotated = new HashMap<String, Map<String, Integer>>();
		LoadNewDict(args[1], partiWordAnnotated);
		
		Map<String, String> goldStrTrees = new HashMap<String, String>();
		for(String key: goldTrees.keySet())
		{
			String wordpos = key.trim();
			int lastBracketIndex = wordpos.lastIndexOf("][");
			if(lastBracketIndex == -1)
			{
				System.out.println(key);
				continue;
			}
			String pos = wordpos.substring(lastBracketIndex+2, wordpos.length()-1);
			if(pos.equals("NR"))
			{
				goldStrTrees.put(key, goldTrees.get(key).toString().trim());
				continue;
			}
			
			List<Tree<String>> subTrees = goldTrees.get(key).getNonTerminals();
			for(Tree<String> subTree : subTrees)
			{
				String theWord = subTree.getTerminalStr();
				if(theWord.length() > 1)
				{
					String theTag = subTree.getLabel().trim();
					theTag = theTag.substring(theTag.length()-1);
					if(partiWordAnnotated.containsKey(theWord)
					&& !partiWordAnnotated.get(theWord).containsKey(theTag))
					{						
						List<Entry<String, Integer>> tagfreqlist = new ArrayList<Entry<String, Integer>>(partiWordAnnotated.get(theWord).entrySet());					
						String newTag = tagfreqlist.get(0).getKey();
						String newLabel = subTree.getLabel().trim().substring(0, subTree.getLabel().trim().length()-1) + newTag;
						subTree.setLabel(newLabel);						
					}
					
					
				}
			}			
			goldStrTrees.put(key, goldTrees.get(key).toString().trim());			
		}
		
		List<Entry<String, String>> chapossortlist = new ArrayList<Entry<String, String>>(goldStrTrees.entrySet());
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		});
		
		PrintWriter output_gold = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);
		
		for(Entry<String, String> curCharPoslist: chapossortlist)
		{								
			output_gold.println(curCharPoslist.getKey() + "\t" + curCharPoslist.getValue());
		}
				
		output_gold.close();
	}

	
	public static void LoadNewDict(String inFile, Map<String, Map<String, Integer>> partiWordAnnotated ) throws Exception
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(inFile), "UTF8"));
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals("") || sLine.trim().length() < 2)continue;
			String[] wordposs = sLine.trim().split("\\s+");
			int start = 0;
			if(wordposs[0].equals("g"))
			{
				start = 1;
			}
			Map<String, Integer> tags = new HashMap<String, Integer>();
			for(int idx = start+1; idx < wordposs.length;idx++)
			{
				tags.put(wordposs[idx].substring(0,1), 1);
			}
			
			if(!partiWordAnnotated.containsKey(wordposs[start]))
			{
				partiWordAnnotated.put(wordposs[start], tags);
			}
			else if(start == 1)
			{
				partiWordAnnotated.put(wordposs[start], tags);
			}
		}
		
		in.close();
	}

}
