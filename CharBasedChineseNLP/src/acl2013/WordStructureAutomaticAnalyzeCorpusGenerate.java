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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class WordStructureAutomaticAnalyzeCorpusGenerate {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Map<String, Tree<String>> validWordTree = new HashMap<String, Tree<String>>();
		String invalidPOS = "IVD";
		
		InputStreamReader inputData = new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8");
		PennTreeReader treeReader = new PennTreeReader(inputData);

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
			while(normalizedTree != null && !normalizedTree.getLabel().endsWith("#t"))
			{
				normalizedTree = normalizedTree.getChild(0);
			}
			normalizedTree = normalizedTree.getChild(0);
			String theword = normalizedTree.getTerminalStr();
			normalizedTree.initParent();
			validWordTree.put(theword, normalizedTree);
		}
		inputData.close();
		
		
		Set<String> validPOS = new TreeSet<String>();
		validPOS.add("AD");validPOS.add("JJ");validPOS.add("NN");validPOS.add("VA");
		validPOS.add("VC");validPOS.add("VE");validPOS.add("VV");validPOS.add("LC");
		inputData = new InputStreamReader(
				new FileInputStream(args[1]), "UTF-8");
		
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);
		PrintWriter out_cfg = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2] + "cfg"), "UTF-8"), false);
		int iCount = 0;
		treeReader = new PennTreeReader(inputData);
		
		while (treeReader.hasNext()) {
			Tree<String> tree = treeTransformer.transformTree(treeReader.next());
			List<String> words = tree.getTerminalYield();
			List<String> postags = tree.getPreTerminalYield();
			if(words.size() != postags.size())
			{
				System.out.println("error line :" + tree.toString());
				continue;
			}
			Map<String, Set<String>> validwordposs = new HashMap<String, Set<String>>();
			String newLine = "";
			
			for(int idw = 0; idw < words.size();idw++)
			{				
				String theWord = words.get(idw);
				String thePOS = postags.get(idw);
				if(thePOS.equals("URL"))thePOS = "NR";
				newLine = newLine + " " + theWord + "_" + thePOS;
				if(theWord.length() != 2)continue;
				if(!PinyinComparator.bAllChineseCharacter(theWord) || !validPOS.contains(thePOS))
				{
					continue;
				}
				if(validWordTree.containsKey(theWord))
				{
					if(!validwordposs.containsKey(theWord))
					{
						validwordposs.put(theWord, new TreeSet<String>());
					}
					validwordposs.get(theWord).add(thePOS);
				}
				else
				{
					System.out.println(theWord + " need to be annotated");
				}
			}
			
			
			for(String theWord : validwordposs.keySet())
			{
				Tree<String> curTree = validWordTree.get(theWord);
				List<Tree<String>> subtrees = curTree.getNonTerminals();
				List<String> replaceStr = new ArrayList<String>();
				for(Tree<String> theSubTree : subtrees)
				{
					if(theSubTree.isPreTerminal())continue;
					if(theSubTree.getChildren().size() != 2)
					{						
						System.out.println("error: " + curTree.toString());
						continue;
					}
					
					Tree<String> leftChild = theSubTree.getChild(0);
					Tree<String> rightChild = theSubTree.getChild(1);
					Tree<String> brotherleftChild = null;
					Tree<String> brotherrightChild = null;
					if(theSubTree.parent != null)
					{
						if(theSubTree.parent.getChildren().size() != 2)
						{						
							System.out.println("error: " + curTree.toString());
							continue;
						}
						if(theSubTree.parent.getChild(0).equals(theSubTree))
						{
							brotherrightChild = theSubTree.parent.getChild(1);
						}
						else if(theSubTree.parent.getChild(1).equals(theSubTree))
						{
							brotherleftChild = theSubTree.parent.getChild(0);
						}
						else
						{
							System.out.println("error: " + curTree.toString());
							continue;
						}
					}
					
					String curReplace = "";
					if(brotherleftChild != null)
					{
						curReplace = curReplace + " " + brotherleftChild.getTerminalStr() + "_" + invalidPOS;
					}
					if(leftChild != null)
					{
						curReplace = curReplace + " " + leftChild.getTerminalStr() + "_" + invalidPOS;
					}
					if(rightChild != null)
					{
						curReplace = curReplace + " " + rightChild.getTerminalStr() + "_" + invalidPOS;
					}
					if(brotherrightChild != null)
					{
						curReplace = curReplace + " " + brotherrightChild.getTerminalStr() + "_" + invalidPOS;
					}
					
					replaceStr.add(curReplace);
					
				}
				
				for(String thePOS : validwordposs.get(theWord))
				{
					String prime = " " +theWord + "_" + thePOS;
					for(String curRelace : replaceStr)
					{
						String outline = newLine.replace(prime, curRelace);
						if(outline.equals(newLine))
						{
							System.out.println("error");
							continue;
						}
						out.println(outline.trim());
						out_cfg.println(tree.toString());
						iCount++;
					}
				}
			}
		}
		System.out.println(iCount);
		inputData.close();
		out.close();
		out_cfg.close();
	}
	

	
	

}
