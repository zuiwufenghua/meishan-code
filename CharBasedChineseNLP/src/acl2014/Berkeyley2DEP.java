package acl2014;

import acl2013.CFGWordStructureNormalize;
import mason.utils.PinyinComparator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.List;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;


public class Berkeyley2DEP {

	/**
	 * @param args
	 */
	public static void main(String[] args)throws Exception{
		// TODO Auto-generated method stub
		
		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		String sLine = null;
		
		
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().length() < 2) continue;
			try
			{
				PennTreeReader reader = new PennTreeReader(
						new StringReader(sLine.trim()));
				if(!reader.hasNext() )
				{
					System.out.println(sLine.trim());
					continue;
				}
				Tree<String> normalizedTree = reader.next();
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
					String theWord = normalizedTree.getTerminalStr();
					if(!PinyinComparator.bContainChineseCharacter(theWord))continue;
					CharDependency inst = Tree2Dependency(normalizedTree);
					List<String> outstrs = inst.toListString();
					for(String curline : outstrs)
					{
						output.println(curline);
					}
					output.println();
				}
				else
				{
					System.out.println(normalizedTree.toString());
				}
			}
			catch (Exception ex)
			{
				System.out.println(sLine);
			}
		}		
		output.close();
		in.close();
	}
	
	public static CharDependency Tree2Dependency(Tree<String> normalizedTree)
	{
		
		CharDependency inst = new CharDependency();
		List<String> chars = normalizedTree.getTerminalYield();
		List<String> postags = normalizedTree.getPreTerminalYield();
		for(int idx = 0; idx < chars.size(); idx++)
		{
			inst.forms.add(chars.get(idx));
			inst.lemmas.add("_");
			inst.postags.add(postags.get(idx));
			inst.cpostags.add(postags.get(idx));
			inst.heads.add(0);
			inst.deprels.add("#in");
			inst.p1heads.add(-1);
			inst.p1deprels.add("_");
			inst.feats.add("_");
		}
		normalizedTree.annotateSubTrees();
		normalizedTree.initParent();
		AnnotateHead(normalizedTree);
		List<Tree<String>> alltrees = normalizedTree.getNonTerminals();
		for(Tree<String> curChild : alltrees)
		{
			//upper
			if(curChild.getChildren().size() == 1) continue;
			assert(curChild.getChildren().size() == 2);
			int lastIndexSharp = curChild.getLabel().lastIndexOf("#");			
			int headIndex = Integer.parseInt(curChild.getLabel().substring(lastIndexSharp+1));
			lastIndexSharp = curChild.getChild(0).getLabel().lastIndexOf("#");
			int leftIndex = Integer.parseInt(curChild.getChild(0).getLabel().substring(lastIndexSharp+1));
			int rightIndex = Integer.parseInt(curChild.getChild(1).getLabel().substring(lastIndexSharp+1));
			if(headIndex == leftIndex)
			{
				if(inst.heads.get(rightIndex) == 0)
				{
					inst.heads.set(rightIndex,  headIndex +1);
				}
				else
				{
					System.out.println("error");
				}
			}
			else if(headIndex == rightIndex)
			{
				if(inst.heads.get(leftIndex) == 0)
				{
					inst.heads.set(leftIndex,  headIndex +1);
				}
				else
				{
					System.out.println("error");
				}
			}
			else
			{
				System.out.println("error");
			}
			
		}
				
		return inst;
	}
	
	public static void AnnotateHead(Tree<String> normalizedTree)
	{					
		String theLabel = normalizedTree.getLabel();
		int thePOSEndIndex = theLabel.lastIndexOf("#");
		String thePOS = theLabel.substring(0, thePOSEndIndex).trim();
		if(normalizedTree.isPreTerminal())
		{
			assert(normalizedTree.smaller == normalizedTree.bigger);
			String curLabel = String.format("%s#%d", normalizedTree.getLabel(), normalizedTree.smaller);
			normalizedTree.setLabel(curLabel);
		}
		else 
		{
			assert(normalizedTree.getChildren().size() == 2);
			for(Tree<String> curChild : normalizedTree.getChildren())
			{
				AnnotateHead(curChild);
			}
			String curLabel;
			//
			if(normalizedTree.getLabel().endsWith("#x"))
			{
				
				if(thePOS.startsWith("N"))
				{
					int lastIndexSharp = normalizedTree.getChild(1).getLabel().lastIndexOf("#");
					curLabel =  normalizedTree.getLabel() + 
							normalizedTree.getChild(1).getLabel().substring(lastIndexSharp);
				}
				else
				{
					int lastIndexSharp = normalizedTree.getChild(0).getLabel().lastIndexOf("#");
					curLabel = normalizedTree.getLabel() + 
							normalizedTree.getChild(0).getLabel().substring(lastIndexSharp);
				}
			}
			else if(normalizedTree.getLabel().endsWith("#z"))
			{
				int lastIndexSharp = normalizedTree.getChild(0).getLabel().lastIndexOf("#");
				curLabel = normalizedTree.getLabel() + 
						normalizedTree.getChild(0).getLabel().substring(lastIndexSharp);
			}
			else
			{
				int lastIndexSharp = normalizedTree.getChild(1).getLabel().lastIndexOf("#");
				curLabel =  normalizedTree.getLabel() + 
						normalizedTree.getChild(1).getLabel().substring(lastIndexSharp);
			}
			normalizedTree.setLabel(curLabel);
		}
				
	}

}
