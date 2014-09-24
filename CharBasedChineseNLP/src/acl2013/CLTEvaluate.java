package acl2013;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

import edu.berkeley.nlp.parser.EnglishPennTreebankParseEvaluator.*;

public class CLTEvaluate {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		int startarg = 0;
		Set<String> punctuationTags = new HashSet<String>();
		if(args[0].equals("-dict"))
		{
			startarg = 2;
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(args[1]), "UTF8"));
			String sLine = null;
			while ((sLine = in.readLine()) != null) {
				if(sLine.trim().equals("") || sLine.trim().length() < 2)continue;
				String[] wordposs = sLine.trim().split("\\s+");
				punctuationTags.add(wordposs[0]);
			}
			in.close();
		}
		InputStreamReader inputData = new InputStreamReader(
				new FileInputStream(args[startarg]), "UTF-8");
		PennTreeReader treeReader = new PennTreeReader(inputData);
		List<Tree<String>> goldTrees = new ArrayList<Tree<String>>();
		Trees.TreeTransformer<String> treeTransformer = new Trees.StandardTreeNormalizer();
		while (treeReader.hasNext()) {
			goldTrees.add(treeTransformer.transformTree(treeReader.next()));
		}
		inputData.close();
		
		inputData = new InputStreamReader(
				new FileInputStream(args[startarg+1]), "UTF-8");
		treeReader = new PennTreeReader(inputData);
		List<Tree<String>> guessTrees = new ArrayList<Tree<String>>();
		while (treeReader.hasNext()) {
			guessTrees.add(treeTransformer.transformTree(treeReader.next()));
		}
		inputData.close();
		
		PrintWriter output = new PrintWriter(System.out);
		
		if(args.length > startarg+2)
		{
			output = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(args[startarg+2]), "UTF-8"), false);
		}
		
		Set<String> labelsToIgnore = new HashSet<String>();
		labelsToIgnore.add("ROOT");
		labelsToIgnore.add("TOP");
		
		
		WordEval<String> eval_word = new WordEval<String>(
				labelsToIgnore, new HashSet<String>());
		eval_word.evaluateMultiple(guessTrees, goldTrees, output);
		
		WordPOSEval<String> eval_wordpos = new WordPOSEval<String>(
				labelsToIgnore, new HashSet<String>());
		eval_wordpos.evaluateMultiple(guessTrees, goldTrees, output);
		
		WordStructureByWordEval<String> eval_wsbyword = new WordStructureByWordEval<String>(
				labelsToIgnore, new HashSet<String>());
		eval_wsbyword.evaluateMultiple(guessTrees, goldTrees, output);
		
		if(startarg == 2)
		{
			WordEval<String> eval_word_oov = new WordEval<String>(
					labelsToIgnore, punctuationTags);
			eval_word_oov.evaluateMultiple(guessTrees, goldTrees, output);
			
			WordPOSEval<String> eval_wordpos_oov = new WordPOSEval<String>(
					labelsToIgnore, punctuationTags);
			eval_wordpos_oov.evaluateMultiple(guessTrees, goldTrees, output);
			
			WordStructureByWordEval<String> eval_wsbyword_oov = new WordStructureByWordEval<String>(
					labelsToIgnore, punctuationTags);
			eval_wsbyword_oov.evaluateMultiple(guessTrees, goldTrees, output);
		}
		
				
		CLTLabeledConstituentEval<String> eval_syn = new CLTLabeledConstituentEval<String>(
				labelsToIgnore, new HashSet<String>());
		eval_syn.evaluateMultiple(guessTrees, goldTrees, output);
				
		TotalTreeEval<String> eval_tree = new TotalTreeEval<String>(
				labelsToIgnore, new HashSet<String>());
		eval_tree.evaluateMultiple(guessTrees, goldTrees, output);
				
		output.close();
	}

}
