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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class MineAutoSyns {

	/**
	 * @param args
	 */
	//0. annotated, 1. wordpos, 2. autosyn, 3. output
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		// goldtrees
		Map<String, Tree<String>> goldTrees = new HashMap<String, Tree<String>>();
		extractTask5Dict(args[0], goldTrees);
		
		Map<String, String> goldStrTrees = new HashMap<String, String>();
		for(String key: goldTrees.keySet())
		{
			goldStrTrees.put(key, goldTrees.get(key).toString().trim());
		}
		
		Map<String, Map<String, Integer>> autoTrees = new HashMap<String, Map<String, Integer>>();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[1]), "UTF8"));
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] wordposs = sLine.trim().split("\\s+");
			for(int idx = 1; idx < wordposs.length; idx++)
			{
				int colonIndex = wordposs[idx].lastIndexOf(":");
				if(colonIndex == -1)continue;
				String pos =  wordposs[idx].substring(0, colonIndex);
				try
				{
					Integer score = Integer.parseInt(wordposs[idx].substring(colonIndex+1));
				}
				catch (Exception e)
				{
					continue;
				}
				String key = String.format("[%s][%s]", wordposs[0], pos);
				autoTrees.put(key, new HashMap<String, Integer>());
				
				if(goldStrTrees.containsKey(key))
				{
					autoTrees.get(key).put(goldStrTrees.get(key), 1);
				}
				//if(!wordposdict.containsKey(wordposs[0]))
				//{
				//	wordposdict.put(wordposs[0], new HashSet<String>());
				//}				
				//wordposdict.get(wordposs[0]).add(pos);
				
				/*
				if(wordposs[0].length() == 1 && closeposdict.containsKey(pos))
				{
					closeposdict.get(pos).add(wordposs[0]);
				}*/
			}
		}
		
		in.close();
		
		// auto trees
		InputStreamReader inputData = new InputStreamReader(
				new FileInputStream(args[2]), "UTF-8");
		PennTreeReader treeReader = new PennTreeReader(inputData);
		Trees.TreeTransformer<String> treeTransformer = new Trees.StandardTreeNormalizer();
		while (treeReader.hasNext()) {
			Tree<String> tree = treeTransformer.transformTree(treeReader.next());
			List<Tree<String>> results = extractWordsFromTree(tree);
			for(Tree<String> oneResult : results)
			{
				String pos = oneResult.getLabel().substring(0,oneResult.getLabel().length()-2);
				String word = oneResult.getTerminalStr();
				String key = String.format("[%s][%s]", word, pos);
				if(autoTrees.containsKey(key))
				{
					String strTree = oneResult.getChild(0).toString().trim();
					if(!autoTrees.get(key).containsKey(strTree))
					{
						autoTrees.get(key).put(strTree, 0);
					}
					autoTrees.get(key).put(strTree, autoTrees.get(key).get(strTree)+1);
				}
			}
		}
		
		inputData.close();
		
		
		List<Entry<String, Map<String, Integer>>> chapossortlist = new ArrayList<Entry<String, Map<String, Integer>>>(autoTrees.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		});
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[3]), "UTF-8"), false);
		
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
			output.print(curCharPoslist.getKey());
			if(synsortlist.size() > 0)
			{
				for(Entry<String, Integer> curSyn: synsortlist)
				{
					String newLabel = String.format("%s[+%d]", curSyn.getKey(), curSyn.getValue());
					if(goldStrTrees.containsKey(curCharPoslist.getKey())
					&& goldStrTrees.get(curCharPoslist.getKey()).equals(curSyn.getKey()))
					{
						newLabel = String.format("%s[%d]", curSyn.getKey(), -curSyn.getValue());
					}					
					output.print("\t" + newLabel);
				}
				output.println();
			}
			else
			{
				String topLabel = curCharPoslist.getKey().trim();
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
					pos = topLabel.substring(posStartIndex, topLabel.length()-1);
				}
				if(word.length() == 1)
				{
					String newStrTree = String.format("(%s#s (%s#b %s))[-1]", pos, pos, word);
					output.println("\t" + newStrTree);
				}
				else
				{
					output.println("\t[NOPREDICT]");
				}
			}
		}
		
		
		output.close();
	}
	
	public static List<Tree<String>> extractWordsFromTree(Tree<String> curTree)
	{
		List<Tree<String>> results = new ArrayList<Tree<String>>();
		String curLabel = curTree.getLabel();
		if(curTree.isLeaf() || curTree.isPreTerminal()
			|| curLabel.endsWith("#r") || curLabel.endsWith("#l")
			|| curLabel.endsWith("#c") || curLabel.endsWith("#s")
			|| curLabel.endsWith("#b") || curLabel.endsWith("#i")
				)
		{
			
		}
		else if(curLabel.endsWith("#w"))
		{
			results.add(curTree);
		}
		else
		{
			for(Tree<String> oneTree : curTree.getChildren())
			{
				List<Tree<String>> oneresults = extractWordsFromTree(oneTree);
				for(Tree<String> wordTree : oneresults)
				{
					results.add(wordTree);
				}
			}
		}
		
		return results;
	}

	public static void extractTask5Dict(String inputFile, Map<String, Tree<String>> wordstructure) throws Exception
	{
		BufferedReader bf=new BufferedReader(new InputStreamReader(new FileInputStream(inputFile),"UTF-8"));
		String sLine = null;
		
		while((sLine=bf.readLine())!=null )
		{	
			if(sLine.trim().length() < 2)continue;
			if(sLine.trim().equals(""))continue;
			String[] wordposs = sLine.trim().split("\t");
			if(wordposs.length != 2)
			{
				System.out.println(sLine);
				continue;
			}
			//parser [word][pos]
			wordposs[0] = wordposs[0].trim();
			int lastBracketIndex = wordposs[0].lastIndexOf("][");
			if(lastBracketIndex == -1)
			{
				System.out.println(sLine);
				continue;
			}
			String word = wordposs[0].substring(1, lastBracketIndex);
			String pos = wordposs[0].substring(lastBracketIndex+2, wordposs[0].length()-1);
			
			final PennTreeReader reader = new PennTreeReader(
					new StringReader(wordposs[1].trim()));
			Tree<String> tree = reader.next();
			Tree<String> subTree1 = tree;

			while (subTree1.getLabel().equalsIgnoreCase("root")
					|| subTree1.getLabel().equalsIgnoreCase("top")) {
				tree = tree.getChild(0);
				subTree1 = subTree1.getChild(0);
			}
									
			String key = String.format("[%s][%s]", word, pos);

			subTree1.annotateSubTrees();
			subTree1.initParent();
			if(!wordstructure.containsKey(key))
			{
				wordstructure.put(key, subTree1);
			}
			else
			{
				System.out.println(sLine + "\t( " + key + " " + wordstructure.get(key).toString() + ")");
				continue;
			}			
		}
		
		bf.close();
	}
	
}
