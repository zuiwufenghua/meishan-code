package acl2013;

import mason.utils.PinyinComparator;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class ExtractWordStructureFromAutomaticSynAnalyze {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Map<String, Tree<String>> validWordTree = new HashMap<String, Tree<String>>();
		String invalidPOS = "IVD";
		InputStreamReader inputData = new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8");
		PennTreeReader treeReader = new PennTreeReader(inputData);

		Trees.TreeTransformer<String> treeTransformer = new Trees.StandardTreeNormalizer();
		while (treeReader.hasNext()) {
			Tree<String> normalizedTree = treeTransformer.transformTree(treeReader.next());
			while(normalizedTree.getLabel().equalsIgnoreCase("root")
					|| normalizedTree.getLabel().equalsIgnoreCase("top"))
			{
				normalizedTree = normalizedTree.getChild(0);
			}
			while(normalizedTree != null && !normalizedTree.getLabel().endsWith("#t"))
			{
				normalizedTree = normalizedTree.getChild(0);
			}
			normalizedTree = normalizedTree.getChild(0);
			String theword = normalizedTree.getTerminalStr();
			normalizedTree.initParent();
			validWordTree.put(theword, normalizedTree);
		}
		inputData.close();
		
		
		Set<String> validPOS = new TreeSet<String>();
		validPOS.add("AD");validPOS.add("JJ");validPOS.add("NN");validPOS.add("VA");
		validPOS.add("VC");validPOS.add("VE");validPOS.add("VV");validPOS.add("LC");
		inputData = new InputStreamReader(
				new FileInputStream(args[1]), "UTF-8");
	
		int iCount = 0;
		BufferedReader in_guess = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[2]), "UTF8"));
		
		Set<String> labelsToIgnore = new HashSet<String>();
		labelsToIgnore.add("ROOT");
		labelsToIgnore.add("TOP");
		WSStructureResultCompare wsc = new WSStructureResultCompare(
				labelsToIgnore, new HashSet<String>());
		Map<String, Map<String, Integer>> results = new HashMap<String, Map<String, Integer>>();
		treeReader = new PennTreeReader(inputData);
		String sLinePred = null;
		while (treeReader.hasNext()) {
			Tree<String> goldtree = treeTransformer.transformTree(treeReader.next());
			List<String> words = goldtree.getTerminalYield();
			List<String> postags = goldtree.getPreTerminalYield();
			String primesentence = goldtree.getTerminalStr();
			if(words.size() != postags.size())
			{
				System.out.println("error line :" + goldtree.toString());
				continue;
			}
			Map<String, Set<String>> validwordposs = new HashMap<String, Set<String>>();
			String newLine = "";
			
			for(int idw = 0; idw < words.size();idw++)
			{				
				String theWord = words.get(idw);
				String thePOS = postags.get(idw);
				if(thePOS.equals("URL"))thePOS = "NR";
				newLine = newLine + " " + theWord + "_" + thePOS;
				if(theWord.length() < 2)continue;
				if(!PinyinComparator.bAllChineseCharacter(theWord) || !validPOS.contains(thePOS))
				{
					continue;
				}
				if(validWordTree.containsKey(theWord))
				{
					if(!validwordposs.containsKey(theWord))
					{
						validwordposs.put(theWord, new TreeSet<String>());
					}
					validwordposs.get(theWord).add(thePOS);
				}
				else
				{
					System.out.println(theWord + " need to be annotated");
				}
			}
			
			
			for(String theWord : validwordposs.keySet())
			{
				Tree<String> curTree = validWordTree.get(theWord);
				List<Tree<String>> subtrees = curTree.getNonTerminals();
				List<String> replaceStr = new ArrayList<String>();
				for(Tree<String> theSubTree : subtrees)
				{
					if(theSubTree.isPreTerminal())continue;
					if(theSubTree.getChildren().size() != 2)
					{						
						System.out.println("error: " + curTree.toString());
						continue;
					}
					
					Tree<String> leftChild = theSubTree.getChild(0);
					Tree<String> rightChild = theSubTree.getChild(1);
					Tree<String> brotherleftChild = null;
					Tree<String> brotherrightChild = null;
					if(theSubTree.parent != null)
					{
						if(theSubTree.parent.getChildren().size() != 2)
						{						
							System.out.println("error: " + curTree.toString());
							continue;
						}
						if(theSubTree.parent.getChild(0).equals(theSubTree))
						{
							brotherrightChild = theSubTree.parent.getChild(1);
						}
						else if(theSubTree.parent.getChild(1).equals(theSubTree))
						{
							brotherleftChild = theSubTree.parent.getChild(0);
						}
						else
						{
							System.out.println("error: " + curTree.toString());
							continue;
						}
					}
					
					String curReplace = "";
					//if(brotherleftChild != null)
					//{
					//	curReplace = curReplace + "_" + brotherleftChild.getTerminalStr();
					//}
					if(leftChild != null)
					{
						curReplace = curReplace + "_" + leftChild.getTerminalStr();
					}
					if(rightChild != null)
					{
						curReplace = curReplace + "_" + rightChild.getTerminalStr();
					}
					//if(brotherrightChild != null)
					//{
					//	curReplace = curReplace + "_" + brotherrightChild.getTerminalStr();
					//}
					
					replaceStr.add(curReplace);
					
				}
				
				for(String thePOS : validwordposs.get(theWord))
				{
					String prime = " " +theWord + "_" + thePOS;
					for(String curRelace : replaceStr)
					{
						String outline = newLine.replace(prime, curRelace);
						if(outline.equals(newLine))
						{
							System.out.println("error");
							continue;
						}
						
						
						Tree<String> besttree = null;
						int bestScore = 10000;
						boolean bEnd = true;
						while ((sLinePred = in_guess.readLine()) != null) {
							bEnd = false;
							if(sLinePred.trim().isEmpty())
							{
								break;
							}
							if(sLinePred.equals("(())")) 
							{
								continue;
							}
							PennTreeReader reader = new PennTreeReader(
									new StringReader(sLinePred.trim()));
							Tree<String> predtree = reader.next();
							String newsentence = predtree.getTerminalStr();
							if(!primesentence.equals(newsentence))
							{
								System.out.println("sentence not match error: ");
								System.out.println("gold : "  + goldtree.toString());
								System.out.println("pred : "  + sLinePred);
								continue;
							}
							List<Tree<String>> anaresult = new ArrayList<Tree<String>>();
							int score = wsc.getHammingDistance(predtree, goldtree, anaresult);
							if(anaresult.size() > 1)
							{
								System.out.println("error");
							}
							if(score >= 0 && bestScore > score)
							//if(score >= 0)
							{
								if(anaresult.size() != 1)
								{
									System.out.println("error");
								}
								bestScore = score;
								besttree = anaresult.get(0);
								/*
								{
									
									List<String> newwords = besttree.getTerminalYield();
									String keymark = besttree.getTerminalStr();
									String treelabel =  besttree.getLabel();
									int splitIndex =  treelabel.lastIndexOf("#");
									String phraselabel = treelabel.substring(0, splitIndex);
									String thenewPOS = treelabel.substring(splitIndex+1);
									keymark = keymark + "_" +  thenewPOS;
									keymark = keymark + curRelace;

									besttree.setLabel(phraselabel);
									if(!results.containsKey(keymark))
									{
										results.put(keymark, new HashMap<String, Integer>());
									}
									if(!results.get(keymark).containsKey(besttree.toString()))
									{
										results.get(keymark).put(besttree.toString(), 0);
									}
									results.get(keymark).put(besttree.toString(), results.get(keymark).get(besttree.toString())+1);
								}*/
								
							}
						}
						if(bEnd)
						{
							break;
						}
						if(besttree == null)
						{
							continue;
						}
						
						List<String> newwords = besttree.getTerminalYield();
						String keymark = besttree.getTerminalStr();
						String treelabel =  besttree.getLabel();
						int splitIndex =  treelabel.lastIndexOf("#");
						String phraselabel = treelabel.substring(0, splitIndex);
						String thenewPOS = treelabel.substring(splitIndex+1);
						keymark = keymark + "_" +  thenewPOS;
						keymark = keymark + curRelace;
						besttree.setLabel(phraselabel);
						if(!results.containsKey(keymark))
						{
							results.put(keymark, new HashMap<String, Integer>());
						}
						if(!results.get(keymark).containsKey(besttree.toString()))
						{
							results.get(keymark).put(besttree.toString(), 0);
						}
						results.get(keymark).put(besttree.toString(), results.get(keymark).get(besttree.toString())+1);
						 
						iCount++;
						if(iCount%5000 == 0)
						{
							System.out.print(iCount);
							System.out.print(" ");
							if(iCount%10000 == 0)
							{
								System.out.println();
							}
						}
						
					}
				}
			}
		}
		System.out.println(iCount);
		in_guess.close();
		
		List<Entry<String, Map<String, Integer>>> chapossortlist = new ArrayList<Entry<String, Map<String, Integer>>>(results.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		}); 
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[3]), "UTF-8"), false);
		iCount = 0;
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
	
	static class LabeledWSConstituent {
		String label;
		int start;
		int end;
		//LabeledWSConstituent parent;

		public String getLabel() {
			return label;
		}

		public int getStart() {
			return start;
		}

		public int getEnd() {
			return end;
		}
		
		//public LabeledWSConstituent getParent() {
		//	return parent;
		//}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof LabeledWSConstituent))
				return false;

			final LabeledWSConstituent LabeledWSConstituent = (LabeledWSConstituent) o;

			if (end != LabeledWSConstituent.end)
				return false;
			if (start != LabeledWSConstituent.start)
				return false;
			if (label != null ? !label.equals(LabeledWSConstituent.label)
					: LabeledWSConstituent.label != null)
				return false;
			

			return true;
		}

		@Override
		public int hashCode() {
			int result;
			result = (label != null ? label.hashCode() : 0);
			result = 29 * result + start;
			result = 29 * result + end;
			return result;
		}

		@Override
		public String toString() {
			return label + "[" + start + "," + end + "]";
		}

		public LabeledWSConstituent(String label, int start, int end) {
			this.label = label;
			this.start = start;
			this.end = end;
		}
	}
	
	
	public static class WSStructureResultCompare {

		Set<String> labelsToIgnore;
		Set<String> punctuationTags;

		static  Tree<String> stripLeaves(Tree<String> tree) {
			if (tree.isLeaf())
				return new Tree<String>(tree.getLabel());
			List<Tree<String>> children = new ArrayList<Tree<String>>();
			for (Tree<String> child : tree.getChildren()) {
				children.add(stripLeaves(child));
			}
			return new Tree<String>(tree.getLabel(), children);
		}


		public Set<LabeledWSConstituent> makeLabeledWSConstituent(Tree<String> tree) {
			Tree<String> noLeafTree = stripLeaves(tree);
			Set<LabeledWSConstituent> set = new HashSet<LabeledWSConstituent>();
			addConstituents(noLeafTree, set, 0);
			return set;
		}

		private int addConstituents(Tree<String> tree, Set<LabeledWSConstituent> set, int start) {
			if (tree == null)
				return 0;
			if (tree.isLeaf())
				return 0;
			
			if(tree.isPreTerminal())
			{
				String token = tree.getTerminalStr();
				if(punctuationTags.contains(tree.getLabel()))
					return 0;
				else
				{
					set.add(new LabeledWSConstituent("[POS]"+tree.getLabel(), start, start + token.length()));
					return token.length();
				}
			}
			
			int end = start;
			for (Tree<String> child : tree.getChildren()) {
				int childSpan = addConstituents(child, set, end);
				end += childSpan;
			}
			
			String label = tree.getLabel();
			if (!labelsToIgnore.contains(label)) {
				set.add(new LabeledWSConstituent(label, start, end));
			}

			return end - start;
		}

		public WSStructureResultCompare(Set<String> labelsToIgnore,
				Set<String> punctuationTags) {
			this.labelsToIgnore = labelsToIgnore;
			this.punctuationTags = punctuationTags;
		}

		public int getHammingDistance(Tree<String> guess, Tree<String> gold, List<Tree<String>> anaresult) {
			Set<LabeledWSConstituent> guessedSet = makeLabeledWSConstituent(guess);
			Set<LabeledWSConstituent> goldSet = makeLabeledWSConstituent(gold);
			Set<LabeledWSConstituent> correctSet = new HashSet<LabeledWSConstituent>();
			boolean bWSSplitInSingleBlock = true;
			Set<LabeledWSConstituent> segwrongset = new HashSet<LabeledWSConstituent>();
			for(LabeledWSConstituent curlabelcons : goldSet)
			{
				if(curlabelcons.label.startsWith("[POS]"))
				{
					segwrongset.add(curlabelcons);
				}
			}
			for(LabeledWSConstituent curlabelcons : guessedSet)
			{
				if(!curlabelcons.label.startsWith("[POS]"))continue;
				LabeledWSConstituent goldlabelcons = null;
				for(LabeledWSConstituent thelabelcons : segwrongset)
				{
					if(thelabelcons.start == curlabelcons.start && thelabelcons.end == curlabelcons.end)
					{
						goldlabelcons = thelabelcons;
					}					
				}
				if(goldlabelcons != null)
				{
					segwrongset.remove(goldlabelcons);
				}

			}
			//if(segwrongset.size() != 1)
			//{
			//	System.out.println("error seg gold: " + gold.toString());
			//	System.out.println("error seg pred: " + guess.toString());
			//	return -1;
			//}
			
			LabeledWSConstituent theGoldWord = null;
			guess.initParent();
			guess.annotateSubTreesByChar();
			for(LabeledWSConstituent curGoldWord : segwrongset)
			{
				theGoldWord = curGoldWord;
			}
			int start = theGoldWord.start;
			int end = theGoldWord.end-1;

			Tree<String> targetTree = guess;
			while(targetTree.smaller <= start && targetTree.bigger >= end)
			{
				boolean bFind = false;
				Tree<String> targetTargetTree = null;
				for(Tree<String> oneTree : targetTree.getChildren())
				{
					if(oneTree.smaller <= start && oneTree.bigger >= end)
					{
						targetTargetTree = oneTree;
						bFind = true;
						break;
					}
				}
				if(bFind)
				{
					targetTree = targetTargetTree;
				}
				else
				{
					break;
				}
			}
			
			Tree<String> generate = new Tree<String>(targetTree.getLabel() + "#" + theGoldWord.label.substring(5));
			List<Tree<String>> theChildren = new ArrayList<Tree<String>>();
			for(Tree<String> oneTree : targetTree.getChildren())
			{
				if( (oneTree.smaller < start && oneTree.bigger >= start)
					|| (oneTree.smaller <= end && oneTree.bigger > end))
				{
					bWSSplitInSingleBlock = false;
					break;
				}
				if(oneTree.smaller >= start && oneTree.smaller <= end)
				{
					theChildren.add(oneTree);
				}
			}
			generate.setChildren(theChildren);
						
			correctSet.addAll(goldSet);
			correctSet.retainAll(guessedSet);
			
			if(bWSSplitInSingleBlock)
			{
				anaresult.add(generate);
				return (guessedSet.size() - correctSet.size())
						+ (goldSet.size() - correctSet.size());
			}
			else
			{
				return -((guessedSet.size() - correctSet.size())
						+ (goldSet.size() - correctSet.size()));
			}
		}
		


	}

}


