package mason.corpus.tool;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class NormalizeCS {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		InputStreamReader inputData = new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8");
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		PennTreeReader treeReader = new PennTreeReader(inputData);
		boolean bContainRoot = false;
		if(args.length > 2 && args[2].equals("-root")) bContainRoot = true;

		
		Trees.TreeTransformer<String> treeTransformer = new Trees.StandardTreeNormalizer();
		int iCount = 0;
		while (treeReader.hasNext()) {
			Tree<String> normalizedTree = treeTransformer.transformTree(treeReader.next());	
						
			while(normalizedTree.getLabel().equalsIgnoreCase("root")
					|| normalizedTree.getLabel().equalsIgnoreCase("top"))
			{
				normalizedTree = normalizedTree.getChild(0);
			}
									
			normalizedTree.removeEmptyNodes();
			normalizedTree.removeUnaryChains();
			normalizedTree.removeDuplicate();
			
			if(!bContainRoot)
			{
				output.println(normalizedTree.toString());
			}
			else
			{
				output.println("(ROOT " + normalizedTree.toString() + ")");
			}
			//output.println(normalizedTree.toString());
			output.flush();
			iCount++;
		}
		System.out.println(iCount);
		output.close();
		inputData.close();
	}

}
