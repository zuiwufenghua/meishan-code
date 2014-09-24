package acl2014;

import acl2013.CFGWordStructureNormalize;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class UnsupervisedWordStructureAnnotate {

	/**
	 * @param args
	 */
	public static void main(String[] args)throws Exception{
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[2]), "UTF-8"));
		Map<String, Integer> subwordfreq = new HashMap<String, Integer>();
		String sLine = null;
		while ((sLine = reader.readLine()) != null) {
			String[] theUnits = sLine.trim().split("\\s+");
			if(!subwordfreq.containsKey(theUnits[0]))
			{
				subwordfreq.put(theUnits[0], 0);
			}
			
			int freq = 0;
			for(int idx = 1; idx < theUnits.length; idx++)
			{
				int numsplit = theUnits[idx].lastIndexOf(":");
				assert(numsplit != -1);
				freq = freq + Integer.parseInt(theUnits[idx].substring(numsplit+1));
			}
			subwordfreq.put(theUnits[0], subwordfreq.get(theUnits[0]) + freq);
		}
		
		reader.close();
		
		// TODO Auto-generated method stub
		InputStreamReader inputData = new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8");

		PennTreeReader treeReader = new PennTreeReader(inputData);
		
		List<Tree<String>> allWordTrees = new ArrayList<Tree<String>>();
		Trees.TreeTransformer<String> treeTransformer = new Trees.StandardTreeNormalizer();
		while (treeReader.hasNext()) {
			Tree<String> normalizedTree = treeTransformer.transformTree(treeReader.next());
			//normalizedTree.removeUnaryChains();
			//normalizedTree.removeEmptyNodes();
			while(normalizedTree.getLabel().equalsIgnoreCase("root")
					|| normalizedTree.getLabel().equalsIgnoreCase("top"))
			{
				normalizedTree = normalizedTree.getChild(0);
			}
			if(CFGWordStructureNormalize.checkWordStructure(normalizedTree))
			{
				//output.println(normalizedTree.toCLTString());
				//CharDependency inst = Tree2Dependency(normalizedTree);
				//List<String> outstrs = inst.toListString();
				//for(String curline : outstrs)
				//{
				//	output.println(curline);
				//}
				//output.println();
				List<Tree<String>> nonTerminalTrees =  normalizedTree.getNonTerminals();
				String theWord = normalizedTree.getTerminalStr();
				int freq = 0; 
				if(subwordfreq.containsKey(theWord))
				{
					freq = subwordfreq.get(theWord);
				}
				
				allWordTrees.add(normalizedTree);
				
				if(freq == 0) continue;
				
				for(Tree<String> oneSubTree : nonTerminalTrees)
				{
					String theSubWord = oneSubTree.getTerminalStr();
					
					if(!subwordfreq.containsKey(theSubWord))
					{
						subwordfreq.put(theSubWord, 0);
					}
					subwordfreq.put(theSubWord, subwordfreq.get(theSubWord) + freq);
				}
			}
			else
			{
				System.out.println(normalizedTree.toString());
			}
		}
		inputData.close();
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		
		for(Tree<String> oneWordTree : allWordTrees)
		{
			String thePOS = oneWordTree.getLabel();
			int thePOSEND = thePOS.lastIndexOf("#");
			assert(thePOSEND > 0);
			thePOS = thePOS.substring(0, thePOSEND);
			List<Tree<String>> nonTerminalTrees =  oneWordTree.getNonTerminals();
			for(Tree<String> oneSubTree : nonTerminalTrees)
			{
				if(oneSubTree.isPreTerminal())continue;
				assert(oneSubTree.getChildren().size() == 2);
				int leftFreq = 0;
				int rightFreq = 0;
				String leftWord = oneSubTree.getChild(0).getTerminalStr();
				String rightWord = oneSubTree.getChild(1).getTerminalStr();
				if(subwordfreq.containsKey(leftWord)) leftFreq = subwordfreq.get(leftWord);
				if(subwordfreq.containsKey(rightWord)) rightFreq = subwordfreq.get(rightWord);
				if(leftFreq > rightFreq)
				{
					oneSubTree.setLabel(thePOS + "#z");
				}
				else if(leftFreq < rightFreq)
				{
					oneSubTree.setLabel(thePOS + "#y");
				}
				else
				{
					oneSubTree.setLabel(thePOS +"#x");
				}
			}
			
			output.println(oneWordTree.toString());
		}
		output.close();
		
	}



}
