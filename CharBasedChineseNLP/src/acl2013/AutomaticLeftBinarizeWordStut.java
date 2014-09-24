package acl2013;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;


public class AutomaticLeftBinarizeWordStut {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		String parse = null;
		BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(
				args[0]), "UTF-8"));
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		while ((parse = bf.readLine()) != null) {

			try{
			final PennTreeReader reader = new PennTreeReader(
					new StringReader(parse.trim()));
			
			Tree<String> tree = reader.next();
			while(tree.getLabel().equalsIgnoreCase("root")) tree = tree.getChild(0);
			tree.removeUnaryChains();
			List<Tree<String>> prevLeafNodes = tree.getPreTerminals();
			for(Tree<String> prevLeaf : prevLeafNodes)
			{
				
				List<Tree<String>> children = new ArrayList<Tree<String>>();
				String theWord = prevLeaf.getChild(0).getLabel();
				String curLabel = prevLeaf.getLabel();
				
				Tree<String> newNode = getLeftInternalStructureTree(theWord, curLabel);
				children.add(newNode);
				prevLeaf.setLabel( curLabel + "#t");
				prevLeaf.setChildren(children);
			}
			
			output.println("(ROOT " + tree.toString() + ")");
			
			}
			catch(Exception e)
			{
				System.out.println(parse);
				continue;
			}
		}
		
		bf.close();
		output.close();

	}

	public static Tree<String> getLeftInternalStructureTree(String theWord,
			String thePOS) {
		Tree<String> leftChild = new Tree<String>(thePOS + "#b");

		Tree<String> leftChildChild = new Tree<String>(theWord.substring(0, 1));
		List<Tree<String>> leftChildren = new ArrayList<Tree<String>>();
		leftChildren.add(leftChildChild);
		leftChild.setChildren(leftChildren);

		for (int idx = 1; idx < theWord.length(); idx++) {
			Tree<String> rightChild = new Tree<String>(thePOS + "#i");

			Tree<String> rightChildChild = new Tree<String>(theWord.substring(
					idx, idx + 1));
			List<Tree<String>> rightchildren = new ArrayList<Tree<String>>();
			rightchildren.add(rightChildChild);
			rightChild.setChildren(rightchildren);

			Tree<String> parent = new Tree<String>(thePOS + "#x");
			List<Tree<String>> children = new ArrayList<Tree<String>>();
			children.add(leftChild);
			children.add(rightChild);
			parent.setChildren(children);

			leftChild = parent;
		}

		return leftChild;
	}

}
