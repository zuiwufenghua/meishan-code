package acl2013;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class WordStructure2Zpar {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub

		BufferedReader bf=new BufferedReader(new InputStreamReader(new FileInputStream(args[0]),"UTF-8"));
		String sLine = null;
		
		PrintWriter output_auto = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		
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
			if(word.length() < 2) continue;	
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
				
				output_auto.println(subTree1.toZparString());
				
			}
			catch (Exception e)
			{
				System.out.println(sLine);
			}
		}
		
		bf.close();
		output_auto.close();
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
