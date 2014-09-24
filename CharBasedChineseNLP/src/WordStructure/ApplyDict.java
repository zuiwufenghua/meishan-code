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


public class ApplyDict {

	//0. annotated, 1. wordpos, 2. output partial dict, 
	//3. output unannotated characters words(left binarized)
	//4. output annotated trees
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		// goldtrees
		Map<String, Tree<String>> goldTrees = new HashMap<String, Tree<String>>();
		extractTask5Dict(args[0], goldTrees);
		
		Map<String, Map<String, Integer>> partiWordAnnotated = new HashMap<String, Map<String, Integer>>();
		MergeTwoDict.LoadNewDict(args[2], partiWordAnnotated);
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
			String word = wordpos.substring(1, lastBracketIndex);
			String pos = wordpos.substring(lastBracketIndex+2, wordpos.length()-1);
			if(pos.equals("NR"))
			{
				goldStrTrees.put(key, goldTrees.get(key).toString().trim());
				continue;
			}
			
			List<Tree<String>> subTrees = goldTrees.get(key).getNonTerminals();
			boolean bSuccedApplied = true;
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
						if(tagfreqlist.size()==1)
						{
							String newTag = tagfreqlist.get(0).getKey();
							String newLabel = subTree.getLabel().trim().substring(0, subTree.getLabel().trim().length()-1) + newTag;
							subTree.setLabel(newLabel);
						}
						else
						{
							System.out.println("wrongtag: " + theWord + "\t" + theTag);
							bSuccedApplied = false;
							break;
						}
						
					}
					
					
				}
			}
			if(bSuccedApplied)
			{
				goldStrTrees.put(key, goldTrees.get(key).toString().trim());
			}
			else
			{
				System.out.println("wrongannotation: " + key + "\t" + goldTrees.get(key).toString().trim());
			}
		}
			
		Map<String, String> autoTrees = new HashMap<String, String>();
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
				
				if(goldStrTrees.containsKey(key))
				{
					autoTrees.put(key, goldStrTrees.get(key));
				}
				else if(wordposs[0].length()== 1)
				{
					String theStrTree = String.format("(%s#s (%s#b %s))", pos, pos, wordposs[0]);
					autoTrees.put(key, theStrTree);
				}
				else if(wordposs[0].length()== 2 && partiWordAnnotated.containsKey(wordposs[0]))
				{
					String theTag = "";
					int maxFreq = 0;
					for(String curTag : partiWordAnnotated.get(wordposs[0]).keySet())
					{
						if( partiWordAnnotated.get(wordposs[0]).get(curTag) > maxFreq)
						{
							maxFreq = partiWordAnnotated.get(wordposs[0]).get(curTag);
							theTag = curTag;
						}
					}
					String theStrTree = String.format("(%s#%s (%s#b %s) (%s#i %s))",
							pos, theTag, pos, wordposs[0].substring(0,1),
							pos, wordposs[0].substring(1,2));
					autoTrees.put(key, theStrTree);
				}
				else
				{
					autoTrees.put(key, "none");
				}
			}
		}			
		in.close();
		
					
		
		List<Entry<String, String>> chapossortlist = new ArrayList<Entry<String, String>>(autoTrees.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		});
		
		PrintWriter output_gold = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[3]), "UTF-8"), false);
		PrintWriter output_auto = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[4]), "UTF-8"), false);
		
		for(Entry<String, String> curCharPoslist: chapossortlist)
		{								
			if(curCharPoslist.getValue().equals("none"))
			{
				String wordpos = curCharPoslist.getKey().trim();
				int lastBracketIndex = wordpos.lastIndexOf("][");
				if(lastBracketIndex == -1)
				{
					System.out.println(sLine);
					continue;
				}
				String word = wordpos.substring(1, lastBracketIndex);
				String pos = wordpos.substring(lastBracketIndex+2, wordpos.length()-1);
				String[] wordChars = new String[word.length()];
				for(int idx = 0; idx < wordChars.length; idx++)
				{
					wordChars[idx] = word.substring(idx, idx+1);
				}
				Tree<String> leftBTree = left2rightBinary(wordChars, pos);
				output_auto.println(curCharPoslist.getKey() + "\t" + leftBTree.toString().trim());
			}
			else
			{
				output_gold.println(curCharPoslist.getKey() + "\t" + curCharPoslist.getValue());
			}
		}
		
		
		output_gold.close();
		output_auto.close();
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
			
			try
			{
				String strTree = wordposs[1].trim();
				if(strTree.endsWith("]"))
				{
					int lastMBIndex = strTree.lastIndexOf("[");
					if(lastMBIndex == -1)
					{
						System.out.println(sLine);
						continue;
					}
					strTree = strTree.substring(0, lastMBIndex);
				}
				final PennTreeReader reader = new PennTreeReader(
						new StringReader(strTree));
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
				
				if(!checkTask5Tree(subTree1))
				{
					System.out.println(sLine);
					continue;
				}
				
				if(!wordstructure.containsKey(key))
				{
					wordstructure.put(key, subTree1);
				}
				else if(!wordstructure.get(key).toString().trim().equals(subTree1.toString().trim()))
				{
					System.out.println(sLine + "\t( " + key + " " + wordstructure.get(key).toString() + ")");
					continue;
				}
				else
				{
					continue;
				}
			}
			catch (Exception e)
			{
				System.out.println(sLine);
			}
		}
		
		bf.close();
	}
	
	
	public static void printPartialDict(Map<String, Map<String, Integer>> wordstructure, String outFile) throws Exception
	{
		List<Entry<String, Map<String, Integer>>> chapossortlist = new ArrayList<Entry<String, Map<String, Integer>>>(wordstructure.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		}); 
			PrintWriter out = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(outFile), "UTF-8"), false);
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
				out.print(curCharPoslist.getKey());
				int iCount = 0;
				for(Entry<String, Integer> curSyn: synsortlist)
				{						
					String newLabel = String.format("%s#%d#%d", curSyn.getKey(), curSyn.getValue(), iCount);
					out.print(" "+newLabel);
					iCount++;
				}
				out.println();
			}
			
			out.close();
	}

	public static Tree<String> left2rightBinary(String[] characters, String thePOS )
	{
		Tree<String> leftChild = new Tree<String>(thePOS+"#b");
		
		Tree<String> leftChildChild = new Tree<String>(characters[0]);
		List<Tree<String>> leftChildren = new ArrayList<Tree<String>>();
		leftChildren.add(leftChildChild);
		leftChild.setChildren(leftChildren);
		
		Tree<String> left = leftChild;
		
		
		
		for(int idx = 1; idx < characters.length; idx++)
		{
			Tree<String> rightChild = new Tree<String>(thePOS + "#i");				
			Tree<String> rightChildChild = new Tree<String>(characters[idx]);
			List<Tree<String>> rightchildren = new ArrayList<Tree<String>>();
			rightchildren.add(rightChildChild);
			rightChild.setChildren(rightchildren);
			
			Tree<String> right = rightChild;
			
			List<Tree<String>> children = new ArrayList<Tree<String>>();
			Tree<String> curTree = new Tree<String>(thePOS + "#c");				
			children = new ArrayList<Tree<String>>();
			children.add(left);
			children.add(right);
			curTree.setChildren(children);
			
			left = curTree;				
		}
		
		//Tree<String> curTree = new Tree<String>("TOP");
		//children = new ArrayList<Tree<String>>();
		//children.add(left);
		//curTree.setChildren(children);
		
		return left;
	}
	
	public static boolean checkTask5Tree(Tree<String> tree)
	{
		if(tree.isLeaf())
		{
			if(tree.getLabel().length()==1)
			{
				return true;
			}
			//very very temp
			//else if(tree.getLabel().length()==2)
			//{
			//	tree.setLabel(tree.getLabel().substring(1));
			//	return true;
			//}
			else
			{
				return false;
			}
		}
		else if(tree.isPreTerminal())
		{
			if(tree.smaller == 0)
			{
				if(tree.getLabel().endsWith("#b"))
				{
					return checkTask5Tree(tree.getChild(0));
				}
				else if(tree.getLabel().endsWith("#i"))
				{
					tree.setLabel(tree.getLabel().substring(0, tree.getLabel().length()-1) + "b");
					return checkTask5Tree(tree.getChild(0));
				}
				else
				{
					return false;
				}
			}
			else
			{
				if(tree.getLabel().endsWith("#i"))
				{
					return checkTask5Tree(tree.getChild(0));
				}
				else if(tree.getLabel().endsWith("#b"))
				{
					tree.setLabel(tree.getLabel().substring(0, tree.getLabel().length()-1) + "i");
					return checkTask5Tree(tree.getChild(0));
				}
				else
				{
					return false;
				}
			}
		}
		else
		{
			if( tree.getLabel().endsWith("#c")
			|| tree.getLabel().endsWith("#l")
			|| tree.getLabel().endsWith("#r"))
			{
				if(tree.getChildren().size() == 2
				&& checkTask5Tree(tree.getChild(0))
				&& checkTask5Tree(tree.getChild(1)))
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			else if( tree.getLabel().endsWith("#s"))
			{
				if(tree.getChildren().size() == 1
					&& checkTask5Tree(tree.getChild(0))
					&& tree.parent == null)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}
	}

}
