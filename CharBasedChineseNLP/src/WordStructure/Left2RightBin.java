package WordStructure;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.syntax.Tree;

public class Left2RightBin {

	/**
	 * @param args
	 */
	//仅仅针对NR
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(
				args[0]), "UTF-8"));
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
				
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] wordposs = sLine.trim().split("\\s+");
			if(wordposs.length < 3)continue;
			List<String> characters = new ArrayList<String>();
			String thePOS = "";
			boolean bValid = true;
			for(int idx = 0; idx < wordposs.length; idx++)
			{
				int wordSplitIndex = wordposs[idx].lastIndexOf("_");
				String curChar = wordposs[idx].substring(0, wordSplitIndex);
				int posSplitIndex = wordposs[idx].lastIndexOf("#");
				String curPOS = wordposs[idx].substring(posSplitIndex+1);
				if(!thePOS.equals("") && !thePOS.equals(curPOS))
				{
					bValid = false;
					break;
				}
				thePOS = curPOS;
				characters.add(curChar);
			}
			
			if(characters.size() != wordposs.length || !bValid)
			{
				System.out.println(sLine);
				continue;
			}
			
			Tree<String> leftChild = new Tree<String>("B#" + thePOS);
			
			Tree<String> leftChildChild = new Tree<String>(characters.get(0));
			List<Tree<String>> leftChildren = new ArrayList<Tree<String>>();
			leftChildren.add(leftChildChild);
			leftChild.setChildren(leftChildren);
			
			Tree<String> left = new Tree<String>(thePOS + "#c");
			List<Tree<String>> children = new ArrayList<Tree<String>>();
			children.add(leftChild);
			left.setChildren(children);
			
			for(int idx = 1; idx < wordposs.length; idx++)
			{
				Tree<String> rightChild = new Tree<String>("M#" + thePOS);
				if(idx == wordposs.length-1) rightChild.setLabel("E#" + thePOS);
				
				Tree<String> rightChildChild = new Tree<String>(characters.get(idx));
				List<Tree<String>> rightchildren = new ArrayList<Tree<String>>();
				rightchildren.add(rightChildChild);
				rightChild.setChildren(rightchildren);
				
				Tree<String> right = new Tree<String>(thePOS + "#c");
				children = new ArrayList<Tree<String>>();
				children.add(rightChild);
				right.setChildren(children);
				
				Tree<String> curTree = new Tree<String>(thePOS + "#c");
				if(idx == wordposs.length-1) curTree.setLabel("S");
				
				children = new ArrayList<Tree<String>>();
				children.add(left);
				children.add(right);
				curTree.setChildren(children);
				
				left = curTree;				
			}
			
			Tree<String> curTree = new Tree<String>("TOP");
			children = new ArrayList<Tree<String>>();
			children.add(left);
			curTree.setChildren(children);
			
			output.println(curTree.toString());
		}
		
		in.close();
		output.close();
			

	}

}
