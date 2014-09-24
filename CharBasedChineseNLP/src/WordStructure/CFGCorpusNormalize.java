package WordStructure;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;


public class CFGCorpusNormalize {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		List<Tree<String>> trainTrees = new ArrayList<Tree<String>>();
		InputStreamReader inputData = new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8");
		PennTreeReader treeReader = new PennTreeReader(inputData);

		while (treeReader.hasNext()) {
			trainTrees.add(treeReader.next());
		}

		Trees.TreeTransformer<String> treeTransformer = new Trees.StandardTreeNormalizer();
		ArrayList<Tree<String>> normalizedTreeList = new ArrayList<Tree<String>>();
		for (Tree<String> tree : trainTrees) {
			Tree<String> normalizedTree = treeTransformer.transformTree(tree);
			normalizedTree.removeUnaryChains();
			normalizedTree.removeEmptyNodes();
			normalizedTreeList.add(normalizedTree);
		}
		
		inputData.close();
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		for (Tree<String> tree : trainTrees) {
			
			output.println(tree.toString());
		}	
		output.close();

	}

}
