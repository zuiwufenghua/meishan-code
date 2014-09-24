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
import java.util.TreeMap;
import java.util.Map.Entry;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class RemoveDupTask5 {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Map<String, Map<String, Integer>> wordstructure = new HashMap<String, Map<String, Integer>>();
		for(int idx = args.length-2; idx >= 0 ; idx--)
		{
			extractTask5Dict(args[idx], wordstructure, idx==0);
		}
		//extractTask5Dict(args[0], wordstructure, false);
		//check(args[1],args[2], wordstructure);
		printDict(args[args.length-1], wordstructure);
	}
	
	
	public static void extractTask5Dict(String inputFile, Map<String, Map<String, Integer>> wordstructure, boolean bGold) throws Exception
	{
		BufferedReader bf=new BufferedReader(new InputStreamReader(new FileInputStream(inputFile),"UTF-8"));
		String sLine = null;
		
		while((sLine=bf.readLine())!=null )
		{	
			if(sLine.trim().length() < 4)continue;
			
			try
			{
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
					pos = topLabel.substring(posStartIndex, topLabel.length()-1);
				}
									
				
				String key = String.format("[%s][%s]", word, pos);
				if(!wordstructure.containsKey(key) || bGold)
				{
					wordstructure.put(key, new HashMap<String, Integer>());
				}
				//else
				//{
					//continue;
				//}
				
				
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
					// This is immpossible here
					wordstructure.get(key).put(secondKey, wordstructure.get(key).get(secondKey) +freq);
				}	
			}
			catch(Exception e)
			{
				System.out.println(sLine);
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

	
	public static void check(String wordFile, String allWordFile, Map<String, Map<String, Integer>> wordstructure) throws Exception
	{
		
		Map<String, Map<String, Integer>> charposlist = new TreeMap<String, Map<String, Integer>>();
		// TODO Auto-generated method stub
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(allWordFile), "UTF8"));
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] wordposs = sLine.trim().split("\\s+");
			for(int idx = 1; idx < wordposs.length; idx++)
			{
				int colonIndex = wordposs[idx].lastIndexOf(":");
				if(colonIndex == -1)continue;
				String pos =  wordposs[idx].substring(0, colonIndex);
				Integer score = 0;
				try
				{
					score = Integer.parseInt(wordposs[idx].substring(colonIndex+1));
				}
				catch (Exception e)
				{
					continue;
				}
				
				String curChar = wordposs[0];
				if(!charposlist.containsKey(curChar))
				{
					charposlist.put(curChar, new HashMap<String, Integer>());
				}
				if(!charposlist.get(curChar).containsKey(pos))
				{
					charposlist.get(curChar).put(pos, 0);
				}
				charposlist.get(curChar).put(pos, charposlist.get(curChar).get(pos) + score);				
			}
		}
		in.close();
		
		BufferedReader bf=new BufferedReader(new InputStreamReader(new FileInputStream(wordFile),"UTF-8"));
		
		while((sLine=bf.readLine())!=null )
		{	
			if(sLine.trim().length() < 3)continue;
			String[] wordlabel = sLine.trim().split("\\s+");
			if(!charposlist.containsKey(wordlabel[0]))
			{
				continue;
			}
			String word = wordlabel[0];
			if(word.length() != 2)
			{
				System.out.println(sLine);
				continue;
			}
			boolean bAccepted = false;
			for(String pos : charposlist.get(word).keySet())
			{
				String key = String.format("[%s][%s]", word, pos);
				if(pos.equals("NR"))continue;
				if(wordstructure.containsKey(key))
				{
					Map<String, Integer> tempReslt = wordstructure.get(key);
					if(tempReslt.size() > 1)continue;
						
					for(String theTree : tempReslt.keySet())
					{
						String partial = String.format("(%s (# %s) (# %s))", wordlabel[1], word.substring(0,1), word.substring(1,2));
						if(theTree.indexOf(partial) != -1)
						{
							bAccepted = true;
							break;
						}
					}
				}
			}
			
			
			
			for(String pos : charposlist.get(word).keySet())
			{
				String key = String.format("[%s][%s]", word, pos);
				if(pos.equals("NR"))continue;
				String parse = String.format("(%s (%s (# %s) (# %s)))", key, wordlabel[1], word.substring(0,1), word.substring(1,2));								
				
				if(!wordstructure.containsKey(key) || bAccepted)
				{
					wordstructure.put(key, new HashMap<String, Integer>());
				}
				
				String secondKey = parse.trim();
				
				if(!wordstructure.get(key).containsKey(secondKey))
				{
					wordstructure.get(key).put(secondKey, 2);
				}
				else					
				{		
					wordstructure.get(key).put(secondKey, wordstructure.get(key).get(secondKey) + 2);
				}	
				
			}
						
		}
		
		bf.close();
	}
}
