package mason;

import java.io.*;
import java.util.*;

import mason.dep.DepInstance;

import edu.berkeley.nlp.syntax.*;
import edu.berkeley.nlp.syntax.Trees.*;

public class PseudoCFG2Dep {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		InputStreamReader inputData = new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8");
		PennTreeReader treeReader = new PennTreeReader(inputData);
				
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(args[1]), "UTF-8"), false);
		
		while (treeReader.hasNext()) {
			Tree<String> curTree = treeReader.next();			
			DepInstance inst = new DepInstance();
			List<Tree<String>> terminals = curTree.getPreTerminals();
			int length = terminals.size();
			for(int idx = 0 ; idx < length; idx++)
			{
				Tree<String> curTerminal = terminals.get(idx);
				String theWord = curTerminal.getChild(0).getLabel();
				String thePOS = curTerminal.getLabel();
				inst.forms.add(theWord);
				inst.cpostags.add(thePOS);
				inst.heads.add(-1);
				inst.deprels.add("ROOT");
				curTerminal.setLabel(String.format("%d:%d", idx, idx));
			}
			
			cfg2dep(curTree, inst);
			
			for(int idx = 0 ; idx < length; idx++)
			{
				output.println(String.format("%s\t%s\t%d\tROOT", inst.forms.get(idx),
						inst.cpostags.get(idx), inst.heads.get(idx)-1));
			}
			output.println();
			
		}
		
		output.close();
		inputData.close();
		
		
	}
	
	
	public static void cfg2dep(Tree<String> root, DepInstance inst)
	{
		if(root.isLeaf() || root.isPreTerminal())
		{
			return;
		}
		
		List<Tree<String>> curChildren = root.getChildren();
		if(curChildren.size() == 1)
		{
			cfg2dep(root.getChild(0), inst);
			String curLabel = root.getLabel();
			assert(curLabel.equalsIgnoreCase("root"));
			String[] curChildLabelUnits = root.getChild(0).getLabel().split(":");
			String newLabel = "-1:"+curChildLabelUnits[1];
			int rootId = Integer.parseInt(curChildLabelUnits[1]);
			root.setLabel(newLabel);
			inst.heads.set(rootId, 0);
		}
		else
		{
			assert(curChildren.size() == 2);
			cfg2dep(root.getChild(0), inst);
			cfg2dep(root.getChild(1), inst);
			String[] curLeftChildLabelUnits = root.getChild(0).getLabel().split(":");
			String[] curRightChildLabelUnits = root.getChild(1).getLabel().split(":");
			String curLabel = root.getLabel();
			String newLabel = "";
			int lefthead = Integer.parseInt(curLeftChildLabelUnits[1]);
			int righthead = Integer.parseInt(curRightChildLabelUnits[1]);
			if(curLabel.endsWith("#R"))
			{
				newLabel = curLeftChildLabelUnits[1] + ":" + curRightChildLabelUnits[1];
				inst.heads.set(lefthead, righthead+1);
			}
			else
			{
				assert(curLabel.endsWith("#L"));
				newLabel = curRightChildLabelUnits[1] + ":" + curLeftChildLabelUnits[1];
				inst.heads.set(righthead, lefthead+1);
			}
			root.setLabel(newLabel);
		}
	}
	

}
