package mason;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;

import edu.berkeley.nlp.syntax.Tree;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

public class Dep2PseudoCFG {
	public static void main(String[] args) throws Exception {
				
		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader(true);
		sdpCorpusReader.Init(args[0]);
		
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[1]), "UTF-8"));
		List<DepInstance> allInstances = sdpCorpusReader.m_vecInstances;
		
		for(DepInstance inst : allInstances)
		{
			Tree<String> resultTree = dep2cfg(inst, -1);
			writer.println(resultTree);
		}
		writer.close();

	}
	
	public static Tree<String> dep2cfg(DepInstance inst, int head)
	{
		int length = inst.size();
		if(head < -1 || head >= length) return null;
		Tree<String> result = null;
		List<Tree<String>> childrentree = null;
		if(head == -1)
		{
			result = new Tree<String>("ROOT");
			childrentree = new ArrayList<Tree<String>>();
			for(int idx = 0; idx < length; idx++)
			{
				if(inst.heads.get(idx) == head+1)
				{
					Tree<String> curChild = dep2cfg(inst, idx);
					childrentree.add(curChild);
					break;
				}
			}
			result.setChildren(childrentree);
		}
		else
		{
			String curLabel = inst.cpostags.get(head).trim();
			curLabel = curLabel.replace("(", "-LRB-");
			curLabel = curLabel.replace(")", "-RRB-");
			String curWord = inst.forms.get(head).trim();
			curWord = curWord.replace("(", "-LRB-");
			curWord = curWord.replace(")", "-RRB-");
			result = new Tree<String>(curLabel);
			childrentree = new ArrayList<Tree<String>>();
			childrentree.add(new Tree<String>(curWord));
			result.setChildren(childrentree);
			
			for(int idx = head-1; idx >= 0; idx--)
			{
				if(inst.heads.get(idx) == head+1)
				{
					Tree<String> parent = new Tree<String>(curLabel+"#R");
					childrentree = new ArrayList<Tree<String>>();
					childrentree.add(dep2cfg(inst, idx));
					childrentree.add(result);
					parent.setChildren(childrentree);
					result = parent;
				}
			}
			
			for(int idx = head+1; idx < length; idx++)
			{
				if(inst.heads.get(idx) == head+1)
				{
					Tree<String> parent = new Tree<String>(curLabel+"#L");
					childrentree = new ArrayList<Tree<String>>();					
					childrentree.add(result);
					childrentree.add(dep2cfg(inst, idx));
					parent.setChildren(childrentree);
					result = parent;
				}
			}			
			
		}
		
		return result;
	}
}
