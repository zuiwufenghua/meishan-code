package cips2012;


import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.syntax.Tree;
import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;


public class CFGPOSCorpus {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();
		sdpCorpusReader.Init(args[0]);

		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		int totalInstances = vecInstances.size();
		
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		for (int senId = 0; senId < totalInstances; senId++) {
			if ((senId + 1) % 500 == 0) {
				System.out.println(String.format(
						"process instance %d", senId + 1));
			}
			DepInstance tmpInstance = vecInstances.get(senId);
			
			String[] forms = new String[tmpInstance.forms.size()];
			tmpInstance.forms.toArray(forms);
						
			String[] postags = new String[tmpInstance.cpostags.size()];
			tmpInstance.cpostags.toArray(postags);
			
			
			Tree<String> leftTree = new Tree<String>(postags[0]);
			List<Tree<String>> curChildren = new ArrayList<Tree<String>>();
			curChildren.add(new Tree<String>(forms[0]));
			leftTree.setChildren(curChildren);
			
			for(int idx = 1; idx < forms.length; idx++)
			{
				Tree<String> rightTree = new Tree<String>(postags[idx]);
				curChildren = new ArrayList<Tree<String>>();
				curChildren.add(new Tree<String>(forms[idx]));
				rightTree.setChildren(curChildren);
				
				Tree<String> curTree = new Tree<String>(postags[idx]+"[H]");
				curChildren = new ArrayList<Tree<String>>();
				curChildren.add(leftTree);
				curChildren.add(rightTree);
				curTree.setChildren(curChildren);
					
				leftTree = curTree;
			}
			
			writer.println("(TOP " + leftTree.toString() + ")");
		}
		
		writer.close();

	}

}
