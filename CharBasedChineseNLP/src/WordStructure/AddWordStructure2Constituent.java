package WordStructure;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;


public class AddWordStructure2Constituent {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		Map<String, Tree<String>> goldTrees = new HashMap<String, Tree<String>>();
		extractTask5Dict(args[1], goldTrees);
		// TODO Auto-generated method stub

		InputStreamReader inputData = new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8");
		PennTreeReader treeReader = new PennTreeReader(inputData);

		Trees.TreeTransformer<String> treeTransformer = new Trees.StandardTreeNormalizer();
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[0]+".annotated"), "UTF-8"), false);
		
		PrintWriter output1 = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[0]+".cons"), "UTF-8"), false);
		
		PrintWriter output2 = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[0]+".charpos"), "UTF-8"), false);
		
		PrintWriter output3 = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[0]+".chars"), "UTF-8"), false);
		
		while (treeReader.hasNext()) {
			Tree<String> tree = treeTransformer.transformTree(treeReader.next());
			output1.println(tree.toString());
			List<Tree<String>> preLeaves = tree.getPreTerminals();
			boolean allParsed = true;
			String output_charpos = "";
			String output_char = "";
			
			for(Tree<String> oneTree : preLeaves)
			{
				String thePos = oneTree.getLabel();
				String theWord = oneTree.getTerminalStr();
				
				
				String key = String.format("[%s][%s]", theWord, thePos);
				Tree<String> curTree = null;
				String strTree = null;
				if(theWord.length() == 1)
				{
					strTree = String.format("(%s#s (%s#b %s))", thePos, thePos, theWord);
					output_charpos = output_charpos + " " + theWord + "_" + thePos + "#b";
					output_char = output_char + " " + theWord;
				}
				else
				{
					if(goldTrees.containsKey(key))
					{
						curTree = goldTrees.get(key);
						strTree = curTree.toString();
					}
					else
					{
						//nostructureWords.add(key);
					}
					output_charpos = output_charpos + " " + theWord.substring(0,1) + "_" + thePos + "#b";
					output_char = output_char + " " + theWord.substring(0,1);
					for(int idx = 1; idx < theWord.length(); idx++)
					{
						output_charpos = output_charpos + " " + theWord.substring(idx,idx+1) + "_" + thePos + "#i";
						output_char = output_char + " " + theWord.substring(idx,idx+1);
					}
				}
				
				
				if(strTree != null)
				{
					final PennTreeReader reader = new PennTreeReader(
							new StringReader(strTree.trim()));
					Tree<String> annotree = reader.next();
					Tree<String> subTree1 = annotree;

					while (subTree1.getLabel().equalsIgnoreCase("root")
							|| subTree1.getLabel().equalsIgnoreCase("top")) {
						annotree = annotree.getChild(0);
						subTree1 = subTree1.getChild(0);
					}
					
					oneTree.setLabel(thePos + "#w");
					oneTree.setChild(0, subTree1);
				}
				else
				{
					allParsed = false;
				}
			}
			
			if(allParsed)
			{
				output.println( tree.toString());
			}
			
			output2.println(output_charpos.trim());
			output3.println(output_char.trim());
			
		}

			
		inputData.close();
		
		output1.close();
		output2.close();
		
		
		
		
		for(String topLabel : goldTrees.keySet())
		{
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
			String newStrTree = String.format("(ROOT (SWP (%s#w %s)))", pos, goldTrees.get(topLabel));
			output.println(newStrTree);
		}
		
		output.close();
	}
	
	
	public static void extractTask5Dict(String inputFile, Map<String, Tree<String>> wordstructure) throws Exception
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
				pos = topLabel.substring(posStartIndex, topLabel.length()-1);
			}
								
			
			String key = String.format("[%s][%s]", word, pos);
			subTree1.annotateSubTrees();
			subTree1.initParent();
			transformtree(pos, subTree1.getChild(0));
			if(!wordstructure.containsKey(key))
			{
				wordstructure.put(key, subTree1.getChild(0));
			}
			else
			{
				System.out.println(sLine + "\t( " + key + " " + wordstructure.get(key).toString() + ")");
				continue;
			}			
		}
		
		bf.close();
	}
	
	
	public static void transformtree(String pos, Tree<String> curTree)
	{
		if(curTree.isLeaf())return;
		
		if(curTree.isPreTerminal())
		{
			assert(curTree.smaller == curTree.bigger);
			if(curTree.smaller == 0)
			{
				curTree.setLabel(pos+"#b");
			}
			else
			{
				curTree.setLabel(pos+"#i");
			}
			return;
		}
		String curLabel = curTree.getLabel();
		curTree.setLabel(pos + "#" +curLabel);
		for(Tree<String> curChild : curTree.getChildren())
		{
			transformtree(pos, curChild);
		}
	}


}
