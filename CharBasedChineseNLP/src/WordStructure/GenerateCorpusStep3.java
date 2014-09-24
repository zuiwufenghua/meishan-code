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
import java.util.Set;
import java.util.Map.Entry;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class GenerateCorpusStep3 {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		//读入已标注的数据以及未标注的数据
		Map<String, Map<String, Tree<String>>> goldStrTrees = new HashMap<String, Map<String, Tree<String>>>();
		Map<String, Set<String>> unAnnotatedWords = new HashMap<String, Set<String>>();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
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
			
			if("[NOPREDICT]".equals(wordposs[1].trim()))
			{
				if(!unAnnotatedWords.containsKey(word))
				{
					unAnnotatedWords.put(word, new HashSet<String>());
				}
				unAnnotatedWords.get(word).add(pos);
			}
			else
			{
				int treeEndIndex = wordposs[1].lastIndexOf(")");
				String strTree = wordposs[1].substring(0, treeEndIndex+1);
				Tree<String> subTree1 = null;
				
				try
				{
					final PennTreeReader reader = new PennTreeReader(
							new StringReader(strTree.trim()));
					Tree<String> tree = reader.next();
					subTree1 = tree;
	
					while (subTree1.getLabel().equalsIgnoreCase("root")
							|| subTree1.getLabel().equalsIgnoreCase("top")) {
						tree = tree.getChild(0);
						subTree1 = subTree1.getChild(0);
					}
				}
				catch(Exception e)
				{
					System.out.println(sLine);
					continue;
				}
				
				List<Tree<String>> nonTerminalTrees = subTree1.getNonTerminals();
				boolean bValidTree = true;
				for(Tree<String> child : nonTerminalTrees)
				{
					String treeLabel = child.getLabel();
					if(treeLabel.endsWith("#c") || treeLabel.endsWith("#r")
					  || treeLabel.endsWith("#l"))
					{
						if(child.getChildren().size() != 2)
						{
							bValidTree = false;
							break;
						}
					}
					else if (treeLabel.endsWith("#s") || treeLabel.endsWith("#b")
							|| treeLabel.endsWith("#i"))
					{
						if(child.getChildren().size() != 1)
						{
							bValidTree = false;
							break;
						}
					}
					else
					{
						bValidTree = false;
						break;
					}
				}
				
				if(!bValidTree)
				{
					System.out.println(sLine);
					continue;
				}
				else
				{
					if(!goldStrTrees.containsKey(word))
					{
						goldStrTrees.put(word, new HashMap<String, Tree<String>>());
					}
					goldStrTrees.get(word).put(pos, subTree1);
				}
			}
			
			
			
		}
		
		in.close();
		
		//解决部分未标注的词，该词的其他词性已经标注
		Set<String> newAnnotatedWords = new HashSet<String>();
		for(String word : unAnnotatedWords.keySet())
		{
			if(goldStrTrees.containsKey(word))
			{
				String primePOS = "";
				String primeTree = "";
				for(String thePOS : goldStrTrees.get(word).keySet())
				{
					primePOS = thePOS;
					primeTree = goldStrTrees.get(word).get(thePOS).toString();
					if(!primePOS.equals(""))break;
				}
				
				for(String thePOS : unAnnotatedWords.get(word))
				{
					String primePOSMatch = "\\("+primePOS+"#";
					String targetPOSMatch = "\\("+thePOS+"#";
					String strTree = primeTree.replaceAll(primePOSMatch, targetPOSMatch);
					Tree<String> subTree1 = null;
					
					try
					{
						final PennTreeReader reader = new PennTreeReader(
								new StringReader(strTree.trim()));
						Tree<String> tree = reader.next();
						subTree1 = tree;
		
						while (subTree1.getLabel().equalsIgnoreCase("root")
								|| subTree1.getLabel().equalsIgnoreCase("top")) {
							tree = tree.getChild(0);
							subTree1 = subTree1.getChild(0);
						}
					}
					catch(Exception e)
					{
						System.out.println(sLine);
						continue;
					}
					
					List<Tree<String>> nonTerminalTrees = subTree1.getNonTerminals();
					boolean bValidTree = true;
					for(Tree<String> child : nonTerminalTrees)
					{
						String treeLabel = child.getLabel();
						if(treeLabel.endsWith("#c") || treeLabel.endsWith("#r")
						  || treeLabel.endsWith("#l"))
						{
							if(child.getChildren().size() != 2)
							{
								bValidTree = false;
								break;
							}
						}
						else if (treeLabel.endsWith("#s") || treeLabel.endsWith("#b")
								|| treeLabel.endsWith("#i"))
						{
							if(child.getChildren().size() != 1)
							{
								bValidTree = false;
								break;
							}
						}
						else
						{
							bValidTree = false;
							break;
						}
					}
					
					if(!bValidTree)
					{
						System.out.println(sLine);
						continue;
					}
					else
					{
						if(!goldStrTrees.containsKey(word))
						{
							goldStrTrees.put(word, new HashMap<String, Tree<String>>());
						}
						goldStrTrees.get(word).put(thePOS, subTree1);
					}
				}
				
				newAnnotatedWords.add(word);
			}
		}

		//打印goldTree
		List<Entry<String, Map<String, Tree<String>>>> chapossortlist = new ArrayList<Entry<String, Map<String, Tree<String>>>>(goldStrTrees.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		});
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		
		PrintWriter output1 = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]+".berk"), "UTF-8"), false);
		
		for(Entry<String, Map<String, Tree<String>>> curCharPoslist: chapossortlist)
		{
			List<Entry<String, Tree<String>>> synsortlist = new ArrayList<Entry<String, Tree<String>>>(curCharPoslist.getValue().entrySet());
			
			Collections.sort(synsortlist, new Comparator(){   
				public int compare(Object o1, Object o2) {       
						Map.Entry obj1 = (Map.Entry) o1;
						Map.Entry obj2 = (Map.Entry) o2;
						
						return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
		            }  
			});
			
			for(Entry<String, Tree<String>> curPOSTree : synsortlist)
			{
				String outLine = String.format("[%s][%s]\t%s", curCharPoslist.getKey()
						, curPOSTree.getKey(), curPOSTree.getValue().toString());
				output.println(outLine);
				String newStrTree = String.format("(ROOT (SWP (%s#w %s)))", curPOSTree.getKey(), curPOSTree.getValue().toString());
				output1.println(newStrTree);
			}
		}
		
		output.close();
		output1.close();
		
		
		//打印未标注词
		List<Entry<String, Set<String>>> untaggedWords = new ArrayList<Entry<String, Set<String>>>(unAnnotatedWords.entrySet());
		
		Collections.sort(untaggedWords, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		});
		output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);
		
		for(Entry<String, Set<String>> curCharPoslist: untaggedWords)
		{
			if(newAnnotatedWords.contains(curCharPoslist.getKey()))
			{
				continue;
			}
			
			String curWord = curCharPoslist.getKey();
			for(String curPOS  : curCharPoslist.getValue())
			{
				String outPutLine = curWord.substring(0,1) + "_" + curPOS + "#b";
				for(int idx = 1; idx < curWord.length(); idx++)
				{
					outPutLine = outPutLine + " " + curWord.substring(idx, idx+1) + "_" + curPOS + "#i";
				}
				
				output.println(outPutLine);
			}
		}
		
		output.close();
		
	}

}
