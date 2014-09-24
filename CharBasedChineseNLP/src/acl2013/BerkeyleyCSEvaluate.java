package acl2013;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

import edu.berkeley.nlp.parser.EnglishPennTreebankParseEvaluator.*;

public class BerkeyleyCSEvaluate {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		InputStreamReader inputData = new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8");
		PennTreeReader treeReader = new PennTreeReader(inputData);
		List<Tree<String>> goldTrees = new ArrayList<Tree<String>>();
		while (treeReader.hasNext()) {
			goldTrees.add(treeReader.next());
		}
		inputData.close();
		
		inputData = new InputStreamReader(
				new FileInputStream(args[1]), "UTF-8");
		treeReader = new PennTreeReader(inputData);
		List<Tree<String>> guessTrees = new ArrayList<Tree<String>>();
		while (treeReader.hasNext()) {
			guessTrees.add(treeReader.next());
		}
		inputData.close();
		
		PrintWriter output = new PrintWriter(System.out);
		
		if(args.length > 2)
		{
			output = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(args[2]), "UTF-8"), false);
		}
		
		Set<String> labelsToIgnore = new HashSet<String>();
		labelsToIgnore.add("ROOT");
		labelsToIgnore.add("TOP");
		
		LabeledConstituentEval<String> eval = new LabeledConstituentEval<String>(
				labelsToIgnore, new HashSet<String>());
		eval.evaluateMultiple(guessTrees, goldTrees, output);
		
		output.close();
	}

}
