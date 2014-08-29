package ir.hit.edu.main;

//import ir.hit.edu.main.PinyinComparator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.berkeley.nlp.PCFGLA.Binarization;
import edu.berkeley.nlp.PCFGLA.CoarseToFineMaxRuleParser;
import edu.berkeley.nlp.PCFGLA.Corpus;
import edu.berkeley.nlp.PCFGLA.Grammar;
import edu.berkeley.nlp.PCFGLA.Lexicon;
import edu.berkeley.nlp.PCFGLA.Option;
import edu.berkeley.nlp.PCFGLA.OptionParser;
import edu.berkeley.nlp.PCFGLA.ParserData;
import edu.berkeley.nlp.PCFGLA.TreeAnnotations;
import edu.berkeley.nlp.PCFGLA.BerkeleyParser.Options;
import edu.berkeley.nlp.PCFGLA.StateSetTreeList;
import edu.berkeley.nlp.syntax.StateSet;
import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;
import edu.berkeley.nlp.util.Numberer;

public class SkeletonConstrained {
	
	public static class Options {

		@Option(name = "-gr", required = true, usage = "Grammarfile (Required)\n")
		public String grFileName;

		@Option(name = "-inputFile", usage = "Read input from this file instead of reading it from STDIN.")
		public String inputFile;

		@Option(name = "-maxLength", usage = "Maximum sentence length (Default = 200).")
		public int maxLength = 200;

		@Option(name = "-outputFile", usage = "Store output in this file instead of printing it to STDOUT.")
		public String outputFile;


	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		OptionParser optParser = new OptionParser(Options.class);
		Options opts = (Options) optParser.parse(args, true);
		
		short[] nSub = new short[2];
		nSub[0] = 1;
		nSub[1] = 1;
		
		String inFileName = opts.grFileName;
		ParserData pData = ParserData.Load(inFileName);
		if (pData == null) {
			System.out.println("Failed to load grammar from file"
					+ inFileName + ".");
			System.exit(1);
		}
		Grammar grammar = pData.getGrammar();
		Lexicon lexicon = pData.getLexicon();
		Numberer.setNumberers(pData.getNumbs());
			
		
		CoarseToFineMaxRuleParser parser = new CoarseToFineMaxRuleParser(grammar, lexicon,
				1, -1, false, false, false, false, false, true, true);
		parser.binarization = pData.getBinarization();
		
		//Grammar curGrammar = parser.getCurGrammar();
		int numOfStates = grammar.numStates;
		System.out.println(numOfStates);
		
		Numberer tagNumberer = parser.getCurNumberer();
		System.out.println(parser.getCurNumberer().toString());
		
		try {
			BufferedReader inputData = (opts.inputFile == null) ? new BufferedReader(
					new InputStreamReader(System.in)) : new BufferedReader(
					new InputStreamReader(new FileInputStream(opts.inputFile),
							"UTF-8"));
					
			Map<String, Map<String, Integer>> errors = new HashMap<String, Map<String, Integer>>();
			PrintWriter outputData = (opts.outputFile == null) ? new PrintWriter(
					new OutputStreamWriter(System.out)) : new PrintWriter(
					new OutputStreamWriter(
							new FileOutputStream(opts.outputFile), "UTF-8"),
					true);
					
			PrintWriter outputDataOrg = (opts.outputFile == null) ? new PrintWriter(
					new OutputStreamWriter(System.out)) : new PrintWriter(
					new OutputStreamWriter(
							new FileOutputStream(opts.outputFile+".org"), "UTF-8"),
					true);
					
			String line = "";
			while ((line = inputData.readLine()) != null) {
				line = line.trim();
				
				boolean bFind = true;
				int lastSplitIndex = -1;
				
				while(bFind)
				{
					bFind = false;
					PennTreeReader reader = new PennTreeReader(new StringReader(
							line.trim()));
					Tree<String> tree = reader.next();
					Tree<String> readTree = tree;

					
					List<Tree<String>> posLabelTrees = readTree.getPreTerminals();
					int length = posLabelTrees.size() + 1;
					if (length > opts.maxLength) {
						outputData.write("(())\n");

						System.err.println("Skipping sentence with "
								+ length + " words since it is too long.");
						continue;
					}
					String thePOS = "";
					String theWord = "";
					for(int i = lastSplitIndex + 1; i < length-1; i++)
					{
						Tree<String> wordposTree = posLabelTrees.get(i);
						thePOS = wordposTree.getLabel();
						theWord = wordposTree.getChild(0).getLabel();
						if(theWord.length() == 2 && ( thePOS.equals("NN") || thePOS.equals("VV")
								|| thePOS.equals("VA") || thePOS.equals("JJ") || thePOS.equals("AD")
								))
						{
							lastSplitIndex = i; bFind = true;
							Tree<String> leftChild = new Tree<String>(theWord.substring(0, 1));
							Tree<String> rightChild = new Tree<String>(theWord.substring(1, 2));
							List<Tree<String>> newChildrenLeft = new ArrayList<Tree<String>>();
							List<Tree<String>> newChildrenRight = new ArrayList<Tree<String>>();
							newChildrenLeft.add(leftChild);
							newChildrenRight.add(rightChild);
							Tree<String> leftChildPreTerminal = new Tree<String>("#F");
							leftChildPreTerminal.setChildren(newChildrenLeft);
							Tree<String> rightChildPreTerminal = new Tree<String>("#F");
							rightChildPreTerminal.setChildren(newChildrenRight);
							
							List<Tree<String>> newChildren = new ArrayList<Tree<String>>();
							newChildren.add(leftChildPreTerminal);
							newChildren.add(rightChildPreTerminal);
							wordposTree.setChildren(newChildren);
							wordposTree.setLabel("#F");
							break;
						}
					}
					
					if(!bFind)break;
					
					
					
					//tmp.annotateSubTrees();	
					//readTree.annotateSubTrees();
					//System.out.println(tmp.toString());;
					//List<Tree<String>> allReadTrees = new ArrayList<Tree<String>>();
					//allReadTrees.add(readTree);
					//List<Tree<String>> newReadTrees = Corpus.binarizeAndFilterTrees(
					//		allReadTrees, 1, 0,  opts.maxLength, parser.binarization,
					//		false, false);
					readTree = TreeAnnotations.processTree(readTree, pData.getV_markov(),
							pData.getH_markov(), pData.getBinarization(), false);
					
					//Tree<String> newReadTree = newReadTrees.get(0);
					//System.out.println(readTree.toString());
					
					
					//Tree<StateSet> newReadTree = StateSetTreeList
					//		.stringTreeToStatesetTree(readTree, grammar.numSubStates, false,
					//				tagNumberer);
					Tree<String> newReadTree = readTree;
					newReadTree.annotateSubTrees();
					
					boolean[][][][] allowedSubStates = new boolean[length][length+1][numOfStates][];
					for(int start = 0; start < length; start++){
						for(int end = start+1; end <= length; end++){
							for(int state = 0; state < numOfStates; state++){
								allowedSubStates[start][end][state] = new boolean[grammar.numSubStates[state]];
								for(int substate = 0; substate < grammar.numSubStates[state]; substate++)
									allowedSubStates[start][end][state][substate] = false;
							}							
						}						
					}
					
					List<Tree<String>> allNonTerminals = newReadTree.getNonTerminals();
					for(Tree<String> oneTree : allNonTerminals){
						int curState = -1;
						if(!oneTree.getLabel().toString().trim().startsWith("#F"))
						{
							try{
								
								curState = tagNumberer.number(oneTree.getLabel().toString().trim());
								if(curState < 0 || curState >= numOfStates)
								{
									System.out.println(oneTree.getLabel() + String.format("\t%d", curState));
								}
							}
							catch (Exception e)
							{
								curState = -1;
							}
						}
						else
						{
							System.out.println(oneTree);
						}
						
						int start = oneTree.smaller;
						int end = oneTree.bigger+1;
						if(curState == -1){					
							for(int state = 0; state < numOfStates; state++){
								for(int substate = 0; substate < grammar.numSubStates[state]; substate++)
									allowedSubStates[start][end][state][substate] = true;
							}	
						}
						else{
							for(int substate = 0; substate < grammar.numSubStates[curState]; substate++)
								allowedSubStates[start][end][curState][substate] = true;
						}
					}
					
					Tree<String> parsedTree = parser.getBestConstrainedParse(readTree.getTerminalYield(), null,	allowedSubStates);
					if(parsedTree.toString().length() <= 10)
					{
						continue;
					}
					parsedTree.annotateSubTrees();
					List<Tree<String>> newNonTerminals = parsedTree.getNonTerminals();
					try
					{
					for(Tree<String> oneTree : newNonTerminals)
					{
						if(oneTree.smaller == lastSplitIndex && oneTree.bigger == lastSplitIndex+1)
						{
							String theNewWord = oneTree.getChild(0).getChild(0).getLabel() + oneTree.getChild(1).getChild(0).getLabel();
							if(!theNewWord.equals(theWord))
							{
								System.out.println("split error");
								
							}
							String newLabel = oneTree.getLabel().replace("@", "");
							newLabel = newLabel.replace("^g", "");
							oneTree.setLabel(newLabel);
							
							String curError = theWord + "_" + thePOS;
							if(!errors.containsKey(curError))
							{
								errors.put(curError, new HashMap<String, Integer>());
							}
							String errorname = oneTree.toString();						
							if(!errors.get(curError).containsKey(errorname))
							{
								errors.get(curError).put(errorname, 0);
							}
							errors.get(curError).put(errorname, errors.get(curError).get(errorname)+1);
							break;
						}						
						
						}
					}
					catch(Exception ex)
					{
						break;
					}
					
					parsedTree = TreeAnnotations.unAnnotateTree(parsedTree,
							false);
					if(parsedTree.toString().length() > 10)
					{
						outputDataOrg.println(line);
						outputDataOrg.flush();
						outputData.println(parsedTree.toString());
						outputData.flush();
					}
				}								
			}
			inputData.close();
			outputData.close();
			outputDataOrg.close();
			
			
			List<Entry<String, Map<String, Integer>>> chapossortlist = new ArrayList<Entry<String, Map<String, Integer>>>(errors.entrySet());
			
			Collections.sort(chapossortlist, new Comparator(){   
				public int compare(Object o1, Object o2) {    
					Map.Entry obj1 = (Map.Entry) o1;
					Map.Entry obj2 = (Map.Entry) o2;
					String str1 = (String) obj1.getKey();
					String str2 = (String) obj2.getKey();
					
					return str1.compareTo(str2);				
	            }   
			}); 
			PrintWriter out = (opts.outputFile == null) ? new PrintWriter(
					new OutputStreamWriter(System.out)) : new PrintWriter(
					new OutputStreamWriter(
							new FileOutputStream(opts.outputFile+".predict"), "UTF-8"),
					true);
			int iCount = 0;
			for(Entry<String, Map<String, Integer>> curCharPoslist: chapossortlist)
			{
				String strout = curCharPoslist.getKey();
				
				List<Entry<String, Integer>> synsortlist = new ArrayList<Entry<String, Integer>>(curCharPoslist.getValue().entrySet());
				
				Collections.sort(synsortlist, new Comparator(){   
					public int compare(Object o1, Object o2) {    
						Map.Entry obj1 = (Map.Entry) o1;
						Map.Entry obj2 = (Map.Entry) o2;
						Integer s1 = (Integer) obj1.getValue();
						Integer s2 = (Integer) obj2.getValue();
						return s2.compareTo(s1)	;
		            }   
				});	
				

				for(Entry<String, Integer> curSyn: synsortlist)
				{
					strout = strout + "\t" + String.format("%s\t%d", curSyn.getKey(), curSyn.getValue());
				}	
				out.println(strout);
				iCount++;
			}
			System.out.println(iCount);
			out.close();

		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		System.exit(0);

	}

}
