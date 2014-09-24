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
import java.util.TreeMap;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class ConllAddWordStructure {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		
		Map<String, Tree<String>> goldTrees = new HashMap<String, Tree<String>>();
		extractTask5Dict(args[0], goldTrees);
		//transTree(goldTrees);
		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();
		sdpCorpusReader.Init(args[1]);
		Map<String, Integer> labelFirstWords = new TreeMap<String, Integer>();
		
		List<DepInstance> depInstances = sdpCorpusReader.m_vecInstances;
		//int totalSentNum = depInstances.size();
		PrintWriter writercomp = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
				args[2]), "UTF-8"));
		//PrintWriter writerpart = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
		//		args[3]), "UTF-8"));
		int processedSentence = 0;
		for(DepInstance inst : depInstances)
		{
			List<String> noStructureWords = assignWordStructure(inst, goldTrees);
			//List<String> outputs = new ArrayList<String>();
			//inst.toGoldListString(outputs, inst.feats1);
			PrintWriter writer = writercomp;
			int printseq = 0;
			
			List<String> outputs = new ArrayList<String>();
			for(int idx = 0; idx < inst.size(); idx++)
			{
				if(inst.feats1.get(idx).equals("_"))
				{
					if(printseq > inst.size()-1)
					{
						printseq = 0;
						for(String curLine : outputs)
						{
							writer.println(curLine);
						}
						writer.println();
						outputs = new ArrayList<String>();
					}
					continue;
				}
				printseq++;
				String oneLine = String.format("%d\t%s\t_\t%s\t_\t%s\t0\t_\t_\t_", printseq,
						inst.forms.get(idx), inst.cpostags.get(idx), inst.feats1.get(idx));
				outputs.add(oneLine);
				
			}
			
			if(noStructureWords.size() > 0)
			{
				//writer = writerpart;
				if(noStructureWords.size() == 1)
				{
					if(!labelFirstWords.containsKey(noStructureWords.get(0)))
					{
						labelFirstWords.put(noStructureWords.get(0), 0);
					}
					labelFirstWords.put(noStructureWords.get(0), labelFirstWords.get(noStructureWords.get(0))+1);
				}
			}
			//for(String curLine : outputs)
			//{
			//	writer.println(curLine);
			//}
			if(printseq > inst.size()-1)
			//if(outputs.size() == 1)
			{
				//printseq = 0;
				for(String curLine : outputs)
				{
					writer.println(curLine);
				}
				writer.println();
			}
			writer.flush();
			processedSentence++;
			if(processedSentence%100 == 0)
			{
				System.out.print(processedSentence);
				System.out.print(" ");
			}
			
			if(processedSentence%2000 == 0)
			{
				System.out.println();
			}
			
		}
		
		writercomp.close();
		//writerpart.close();
		System.out.println(processedSentence);
		for(String curWordPOS : labelFirstWords.keySet())
		{
			System.out.println(curWordPOS + "\t" + labelFirstWords.get(curWordPOS));
		}
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


	
	public static List<String> assignWordStructure(DepInstance inst, Map<String, Tree<String>> goldTrees)
	{
		int wordNum = inst.size();
		List<String> nostructureWords = new ArrayList<String>();
		for(int i = 0; i < wordNum; i++)
		{
			String theWord = inst.forms.get(i);
			String thePos = inst.postags.get(i);
			String key = String.format("[%s][%s]", theWord, thePos);
			Tree<String> curTree = null;
			String strTree = null;
			if(theWord.length() == 1)
			{
				strTree = String.format("(s (# %s))", theWord);
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
					nostructureWords.add(key);
				}
			}
			
			inst.feats1.set(i, "_");
			if(strTree != null)
			{
				inst.feats1.set(i, strTree);
			}
			
		}
		
		return nostructureWords;
	}
	
	public static void transTree(Map<String, Tree<String>> goldTrees)
	{
		for(String theWordPos : goldTrees.keySet())
		{
			
			Tree<String> curTree = goldTrees.get(theWordPos);
			curTree.initParent();
			if(curTree.getChildren().size() != 1 || !curTree.getChild(0).getLabel().equals("S"))
			{
				System.out.println("error:\t" + theWordPos + "\t" + curTree.toString());
				continue;
			}
			int splitIndex = theWordPos.lastIndexOf("_");
			String thePOS = theWordPos.substring(splitIndex+1);
			List<Tree<String>> posTrees = curTree.getPreTerminals();
			boolean bValid = true;
			for(Tree<String> curPosTree : posTrees)
			{
				String theLabel = curPosTree.getLabel();
				if(theLabel.length() < 2 || !theLabel.substring(2).equals(thePOS))
				{
					System.out.println("error:\t" + theWordPos + "\t" + curTree.toString());
					bValid = false;
					break;
				}
			}
			if(!bValid) continue;
			for(Tree<String> curPosTree : posTrees)
			{
				Tree<String> curParent = curPosTree.parent;
				List<Tree<String>> children = curPosTree.getChildren();
				curParent.setChildren(children);
			}
									
			curTree.setLabel(thePOS + "#w");
			curTree.setChildren(curTree.getChild(0).getChildren());
			curTree.initParent();
			disperse(curTree);
		}
	}
	
	public static void disperse(Tree<String> curTree)
	{
		curTree.initParent();
		List<Tree<String>> leaves = curTree.getPreTerminals();
		
		for(Tree<String> leaf : leaves)
		{
			String theLabel = leaf.getLabel();
			
			String theWord = leaf.getChild(0).getLabel();
			if(theWord.length() == 1) continue;
			
			String thePOS = theLabel.substring(0, theLabel.length()-2);
			
			Tree<String> leftChild = new Tree<String>(thePOS + "#c");
			
			Tree<String> leftChildChild = new Tree<String>(theWord.substring(0,1));
			List<Tree<String>> leftChildren = new ArrayList<Tree<String>>();
			leftChildren.add(leftChildChild);
			leftChild.setChildren(leftChildren);

		
			
			Tree<String> rightChild  = null;
			List<Tree<String>> rightchildren = null;
			
			for(int idx = 1; idx < theWord.length()-1; idx++)
			{
				rightChild = new Tree<String>(thePOS + "#c");
				
				Tree<String> rightChildChild = new Tree<String>(theWord.substring(idx,idx+1));
				rightchildren = new ArrayList<Tree<String>>();
				rightchildren.add(rightChildChild);
				rightChild.setChildren(rightchildren);
				
				
				Tree<String> tmpTree = new Tree<String>(thePOS + "#c");
				
				List<Tree<String>> children = new ArrayList<Tree<String>>();
				children.add(leftChild);
				children.add(rightChild);
				tmpTree.setChildren(children);
				
				leftChild = tmpTree;				
			}
			
			rightChild = new Tree<String>(thePOS + "#c");
			
			Tree<String> rightChildChild = new Tree<String>(theWord.substring(theWord.length()-1));
			rightchildren = new ArrayList<Tree<String>>();
			rightchildren.add(rightChildChild);
			rightChild.setChildren(rightchildren);
			
			
			List<Tree<String>> children = new ArrayList<Tree<String>>();
			children.add(leftChild);
			children.add(rightChild);
			leaf.setChildren(children);
			
			
		}
	}
}
