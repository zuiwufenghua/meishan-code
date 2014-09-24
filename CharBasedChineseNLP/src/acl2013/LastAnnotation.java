package acl2013;

import mason.utils.PinyinComparator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class LastAnnotation {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Map<String, Tree<String>> goldTrees = new HashMap<String, Tree<String>>();
		extractTask5Dict(args[0], goldTrees);
		
		BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(args[1]),"UTF-8"));
		String line = null;
		Map<String, String> remainWords = new HashMap<String, String>();
		Map<String, String> charmaps = new HashMap<String, String>();
		charmaps.put("著", "着");
		charmaps.put("麽", "么");
		charmaps.put("於", "于");
		charmaps.put("後", "后");
		charmaps.put("徵", "征");
		charmaps.put("馀", "余");
		charmaps.put("藉", "借");
		
		Map<String, String> autoTrees = new HashMap<String, String>();
		
		for(String curKey : goldTrees.keySet())
		{
			autoTrees.put(curKey, goldTrees.get(curKey).toString().trim());
		}
		while ((line = bf.readLine()) != null){
			line = line.trim();
			if (line.equals(""))
				continue;
			String theWord = "";
			String theRelaceWord = "";
			String thePOS = "";
			List<String> tmp = Arrays.asList(line.split("\\s+"));
			for(String curWordPOS : tmp)
			{
				int lastSplitIndex = curWordPOS.lastIndexOf("_");
				if(lastSplitIndex != 1)
				{
					System.out.println(line);
					continue;
				}
				int lastPOSSplitIndex = curWordPOS.lastIndexOf("#");
				if(lastPOSSplitIndex != curWordPOS.length()-2)
				{
					System.out.println(line);
					continue;
				}
				theWord = theWord + curWordPOS.substring(0,1);
				if(charmaps.containsKey(curWordPOS.substring(0,1)))
				{
					theRelaceWord = theRelaceWord + charmaps.get(curWordPOS.substring(0,1));
				}
				else
				{
					theRelaceWord = theRelaceWord + curWordPOS.substring(0,1);
				}

				thePOS = curWordPOS.substring(2,lastPOSSplitIndex);
			}
			
			String key = String.format("[%s][%s]", theWord, thePOS);
			String replacekey = String.format("[%s][%s]", theRelaceWord, thePOS);
			if(key.equals(replacekey))
			{
				autoTrees.put(key, "none");
				remainWords.put(key, line);
			}
			else if (goldTrees.containsKey(replacekey))
			{
				String strTree = goldTrees.get(replacekey).toString();
				for(String curChar : charmaps.keySet())
				{
					String orgChar = curChar;
					String replaceChar = charmaps.get(orgChar);
					strTree = strTree.replace(replaceChar, orgChar);
				}
				autoTrees.put(key, strTree);
			}
			else
			{
				autoTrees.put(key, "none");
				remainWords.put(key, line);
			}
										
		}
		
		bf.close();
		
		List<Entry<String, String>> chapossortlist = new ArrayList<Entry<String, String>>(autoTrees.entrySet());
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		});
		
		PrintWriter output_gold = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);
		PrintWriter output_auto = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[3]), "UTF-8"), false);
		
		for(Entry<String, String> curCharPoslist: chapossortlist)
		{								
			if(curCharPoslist.getValue().equals("none"))
			{
				
				output_auto.println(remainWords.get(curCharPoslist.getKey()));
				
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
	

	public static boolean checkTask5Tree(Tree<String> tree)
	{
		if(tree.isLeaf())
		{
			if(tree.getLabel().length()==1)
			{
				return true;
			}
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
					return true;
				}
				else if(tree.getLabel().endsWith("#i"))
				{
					tree.setLabel(tree.getLabel().substring(0, tree.getLabel().length()-1) + "b");
					return true;
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
					return true;
				}
				else if(tree.getLabel().endsWith("#b"))
				{
					tree.setLabel(tree.getLabel().substring(0, tree.getLabel().length()-1) + "i");
					return true;
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
