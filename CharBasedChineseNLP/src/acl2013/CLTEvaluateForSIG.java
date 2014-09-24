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
import edu.berkeley.nlp.syntax.Trees;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

import edu.berkeley.nlp.parser.EnglishPennTreebankParseEvaluator.CLTLabeledConstituentEval;
import edu.berkeley.nlp.parser.EnglishPennTreebankParseEvaluator.TotalTreeEval;
import edu.berkeley.nlp.parser.EnglishPennTreebankParseEvaluator.WordEval;
import edu.berkeley.nlp.parser.EnglishPennTreebankParseEvaluator.WordPOSEval;
import edu.berkeley.nlp.parser.EnglishPennTreebankParseEvaluator.WordStructureByWordEval;

public class CLTEvaluateForSIG {

	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception{
		// TODO Auto-generated method stub
		InputStreamReader inputData = new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8");
		PennTreeReader treeReader = new PennTreeReader(inputData);
		List<Tree<String>> goldTrees = new ArrayList<Tree<String>>();
		Trees.TreeTransformer<String> treeTransformer = new Trees.StandardTreeNormalizer();
		while (treeReader.hasNext()) {
			goldTrees.add(treeTransformer.transformTree(treeReader.next()));
		}
		inputData.close();
		
		inputData = new InputStreamReader(
				new FileInputStream(args[1]), "UTF-8");
		treeReader = new PennTreeReader(inputData);
		List<Tree<String>> guessTrees = new ArrayList<Tree<String>>();
		while (treeReader.hasNext()) {
			guessTrees.add(treeTransformer.transformTree(treeReader.next()));
		}
		inputData.close();
		
		int evaluateType = 0;
		if(args[2].equals("seg"))
		{
			evaluateType = 0;
		}
		else if(args[2].equals("pos"))
		{
			evaluateType = 1;
		}
		else if(args[2].equals("syn"))
		{
			evaluateType = 2;
		}
		else if(args[2].equals("ws"))
		{
			evaluateType = 3;
		}
		else
		{
			System.out.println("error args 3");
			return;
		}
		PrintWriter output = new PrintWriter(System.out);
		
		if(args.length > 3)
		{
			output = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(args[3]), "UTF-8"), false);
		}
		
		Set<String> labelsToIgnore = new HashSet<String>();
		labelsToIgnore.add("ROOT");
		labelsToIgnore.add("TOP");
		
		if(evaluateType == 0)
		{
			WordEval<String> eval_word = new WordEval<String>(
					labelsToIgnore, new HashSet<String>());
			eval_word.evaluateMultipleForSIG(guessTrees, goldTrees, output);
		}
		else if(evaluateType == 1)
		{
			WordPOSEval<String> eval_wordpos = new WordPOSEval<String>(
					labelsToIgnore, new HashSet<String>());
			eval_wordpos.evaluateMultipleForSIG(guessTrees, goldTrees, output);
		}
		else if(evaluateType == 2)
		{
			CLTLabeledConstituentEval<String> eval_syn = new CLTLabeledConstituentEval<String>(
					labelsToIgnore, new HashSet<String>());
			eval_syn.evaluateMultipleForSIG(guessTrees, goldTrees, output);
		}
		else if(evaluateType == 3)
		{
			WordStructureByWordEval<String> eval_wsbyword = new WordStructureByWordEval<String>(
					labelsToIgnore, new HashSet<String>());
			eval_wsbyword.evaluateMultipleForSIG(guessTrees, goldTrees, output);
		}
				
		output.close();
	}

}
