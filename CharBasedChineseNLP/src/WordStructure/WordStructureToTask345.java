package WordStructure;


import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class WordStructureToTask345 {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();
		sdpCorpusReader.Init(args[0]);
		int taskType = Integer.parseInt(args[2]);
		
		List<DepInstance> depInstances = sdpCorpusReader.m_vecInstances;

		PrintWriter writercomp = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
				args[1]), "UTF-8"));
		for(DepInstance inst : depInstances)
		{
			int wordNum = inst.size();
			for(int i = 0; i < wordNum; i++)
			{
				String theWord = inst.forms.get(i);
				String thePos = inst.postags.get(i);
				
				Tree<String> curTree = PennTreeReader.parseEasy(inst.feats1.get(i));

				if(curTree == null)
				{
					curTree = new Tree<String>("#W");
					System.out.println("error.....");
				}
				
				if(taskType == 3)
				{
					transformTreeFrom2ForTask3(curTree);
				}
				if(taskType == 4)
				{
					transformTreeFrom2ForTask4(curTree);
				}
				
				
				String oneLine = String.format("%d\t%s\t_\t%s\t_\t%s\t%d\t_\t_\t_", i+1,
						inst.forms.get(i), inst.cpostags.get(i), curTree.toString(), inst.heads.get(i));
				writercomp.println(oneLine);
			}
			writercomp.println();
			writercomp.flush();
		}
		
		writercomp.close();

	}
	
	public static void transformTreeFrom2ForTask3(Tree<String> curTree)
	{
		if(curTree != null)
		{
			List<Tree<String>> nonTerminalTrees = curTree.getNonTerminals();
			for(Tree<String> theSubTree : nonTerminalTrees)
			{
				theSubTree.setLabel("#");
			}
		}
		curTree.setLabel("#W");
		if(curTree.isPreTerminal())
		{
			Tree<String> middleTree = new Tree<String>("#");
			middleTree.setChildren(curTree.getChildren());
			List<Tree<String>> tempChildren = new ArrayList<Tree<String>>();
			tempChildren.add(middleTree);
			curTree.setChildren(tempChildren);
		}
		
		curTree.initParent();
		curTree.annotateSubTrees();
		
	}
	
	public static void transformTreeFrom2ForTask4(Tree<String> curTree)
	{
		if(curTree == null) return;
		if(curTree.isPreTerminal())
		{
			Tree<String> middleTree = new Tree<String>("#");
			middleTree.setChildren(curTree.getChildren());
			List<Tree<String>> tempChildren = new ArrayList<Tree<String>>();
			tempChildren.add(middleTree);
			curTree.setChildren(tempChildren);
			String leftLabel = curTree.getLabel();
			curTree.setLabel(leftLabel.substring(0, leftLabel.length()-2) + "#s");
		}		
		else
		{
			List<Tree<String>> nonTerminalTrees = curTree.getNonTerminals();
			List<String> newLabels = new ArrayList<String>();
			for(int idx = 0; idx < nonTerminalTrees.size();  idx++)
			{
				newLabels.add("");
			}
			for(int idx = 0; idx < nonTerminalTrees.size();  idx++)
			{
				Tree<String> theSubTree = nonTerminalTrees.get(idx);
				if(theSubTree.getChildren().size() == 1)
				{
					newLabels.set(idx, "#");
				}
				else if(theSubTree.getChildren().size() == 2)
				{
					String leftLabel = theSubTree.getChild(0).getLabel();
					String rightLabel = theSubTree.getChild(1).getLabel();
					if(leftLabel.endsWith("#h"))
					{
						newLabels.set(idx, leftLabel.substring(0, leftLabel.length()-2) + "#l");
					}
					else if(leftLabel.endsWith("#c")) 
					{
						newLabels.set(idx, leftLabel.substring(0, leftLabel.length()-2) + "#b");
					}
					else if(rightLabel.endsWith("#h")) 
					{
						newLabels.set(idx, rightLabel.substring(0, rightLabel.length()-2) + "#r");
					}
					else
					{
						System.out.println("error....");
					}
				}
				else
				{
					System.out.println("error....");
				}
			}
			
			for(int idx = 0; idx < nonTerminalTrees.size();  idx++)
			{
				Tree<String> theSubTree = nonTerminalTrees.get(idx);
				String curTreeLabel = newLabels.get(idx);
				if(curTreeLabel.equals(""))
				{
					System.out.println("error....");
				}
				theSubTree.setLabel(curTreeLabel);
			}
		}
		//curTree.setLabel("#W");
		
		
		curTree.initParent();
		curTree.annotateSubTrees();
		
	}

}
