package WordStructure;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;
import mason.utils.*;



public class AnalyzeCons {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		Map<String, Map<String, Integer>> wordstructure = new HashMap<String, Map<String, Integer>>();
		for(int idx = 0; idx < args.length-2; idx++)
		{
			RemoveDupTask5.extractTask5Dict(args[idx], wordstructure, idx==0);
		}
		Map<String, Map<String, Integer>> newWordstructure = new HashMap<String, Map<String, Integer>>();
		analyze(args[args.length-2], wordstructure, newWordstructure);
		//RemoveDupTask5.printDict(args[args.length-1], newWordstructure);
		//RemoveDupTask5.printDict(args[args.length-1], wordstructure);

	}
	
	
	public static List<String> findKernels(Tree<String> syn)
	{
		//List<String> kernels = new ArrayList<String>();
		if(syn.isPreTerminal() || syn.isLeaf() || syn.getLabel().equals("s"))
		{
			List<String> kernels = new ArrayList<String>();
			kernels.add(syn.getTerminalStr());
			return kernels;
		}
		else if(syn.getChildren().size() != 2)
		{
			return null;
		}
	    else if(syn.getLabel().equals("r"))
		{
			return findKernels(syn.getChild(1));
		}
		else if(syn.getLabel().equals("l"))
		{
			return findKernels(syn.getChild(0));
		}
		else if(syn.getLabel().equals("b"))
		{
			List<String> kernels = new ArrayList<String>();
			List<String> kernelsleft = findKernels(syn.getChild(0));
			List<String> kernelsright = findKernels(syn.getChild(1));
			if(kernelsleft == null || kernelsright == null) return null;
			for(String curChar : kernelsleft)
			{
				kernels.add(curChar);
			}
			for(String curChar : kernelsright)
			{
				kernels.add(curChar);
			}
			return kernels;
		}
		else if(syn.getLabel().equals("#W"))
		{
			List<String> kernels = new ArrayList<String>();
			kernels.add("$");
			return kernels;
		}
		else
		{
			System.out.println("Some thing error in findKernels.");
			return null;
		}
		
		//return kernels;
	}


	
	
	public static void analyze(String outputFile, Map<String, Map<String, Integer>> wordstructure
			,Map<String, Map<String, Integer>> newWordstructure) throws Exception
	{
		Map<String, Map<String, Integer>> partdict = new LinkedHashMap<String, Map<String, Integer>>();
		Map<String, Map<String, Integer>> transdict = new LinkedHashMap<String, Map<String, Integer>>();
		//StepCounter sc = new StepCounter();
		
		//first step, extract rules from two-character words
		
		for(String firstKey : wordstructure.keySet())
		{
			for(String secondKey: wordstructure.get(firstKey).keySet())
			{
				if(secondKey.trim().length() < 4)continue;
				
				final PennTreeReader reader = new PennTreeReader(
						new StringReader(secondKey.trim()));
				Tree<String> tree = reader.next();
				Tree<String> subTree1 = tree;
				if(subTree1 == null) continue;
				while (subTree1.getLabel().equalsIgnoreCase("root")
						|| subTree1.getLabel().equalsIgnoreCase("top")) {
					tree = tree.getChild(0);
					subTree1 = subTree1.getChild(0);
				}
				String terminalWord = subTree1.getTerminalStr();
				//if(terminalWord.length() != 2) continue;
				
				String topLabel = subTree1.getLabel().trim();
				int freqStartIndex = topLabel.indexOf("]#") + 2;
				int posStartIndex = topLabel.lastIndexOf("][") + 2;
				String word = topLabel.substring(1, posStartIndex-2);
				String pos = "";
				int freq = 1;
				if(freqStartIndex != 1)
				{
					pos = topLabel.substring(posStartIndex, freqStartIndex-2);
					freq = Integer.parseInt(topLabel.substring(freqStartIndex));
				}
				else
				{
					pos = topLabel.substring(posStartIndex, topLabel.length()-1);
				}
				if(pos.equals("NR"))continue;
				subTree1.annotateSubTrees();
				subTree1.initParent();
				subTree1 = subTree1.getChild(0);
				List<Tree<String>> allTrees = subTree1.getNonTerminals();
				for(Tree<String> curTree : allTrees)
				{
					if(curTree.isPreTerminal())continue;
					
					String partialWord = curTree.getTerminalStr();						
					String curLabel = curTree.getLabel();
					if (!partdict.containsKey(partialWord))
						partdict.put(partialWord, new TreeMap<String, Integer>());
					Statics.<String> increment(partdict.get(partialWord), curLabel);
					if(partialWord.length() == 1 && !curLabel.equals("s"))
					{
						System.out.println(secondKey);
						break;
					}
					
					if(curTree.getChildren().size() == 2)
					{
						List<String> leftKernels = findKernels(curTree.getChild(0));
						List<String> rightKernels = findKernels(curTree.getChild(1));
						if(leftKernels == null || rightKernels == null)
						{
							System.out.println(secondKey);
							break;
						}
						int leftKernelSize = leftKernels.size();
						int righttKernelSize = rightKernels.size();
						for(int idlx = 0; idlx < leftKernelSize; idlx++)
						{
							String leftKey = leftKernels.get(idlx);
							for(int idrx = 0; idrx < righttKernelSize; idrx++)
							{
								String rightKey = rightKernels.get(idrx);
								String transKey = leftKey+rightKey;
								
								if (!transdict.containsKey(transKey))
									transdict.put(transKey, new TreeMap<String, Integer>());
								Statics.<String> increment(transdict.get(transKey), curLabel);
								
							}
						}
					}
				}
				//sc.increment();
			}
		}
		
		// check
		for(String firstKey : wordstructure.keySet())
		{
			for(String secondKey: wordstructure.get(firstKey).keySet())
			{
				if(secondKey.trim().length() < 4)continue;
				
				final PennTreeReader reader = new PennTreeReader(
						new StringReader(secondKey.trim()));
				Tree<String> tree = reader.next();
				Tree<String> subTree1 = tree;
				if(subTree1 == null) continue;
				while (subTree1.getLabel().equalsIgnoreCase("root")
						|| subTree1.getLabel().equalsIgnoreCase("top")) {
					tree = tree.getChild(0);
					subTree1 = subTree1.getChild(0);
				}
				String terminalWord = subTree1.getTerminalStr();
				//if(terminalWord.length() != 2) continue;
				
				String topLabel = subTree1.getLabel().trim();
				int freqStartIndex = topLabel.indexOf("]#") + 2;
				int posStartIndex = topLabel.lastIndexOf("][") + 2;
				String word = topLabel.substring(1, posStartIndex-2);
				String pos = "";
				int freq = 1;
				if(freqStartIndex != 1)
				{
					pos = topLabel.substring(posStartIndex, freqStartIndex-2);
					freq = Integer.parseInt(topLabel.substring(freqStartIndex));
				}
				else
				{
					pos = topLabel.substring(posStartIndex, topLabel.length()-1);
				}
				if(pos.equals("NR"))continue;
				subTree1.annotateSubTrees();
				subTree1.initParent();
				subTree1 = subTree1.getChild(0);
				List<Tree<String>> allTrees = subTree1.getNonTerminals();
				for(Tree<String> curTree : allTrees)
				{
					if(curTree.isPreTerminal())continue;
					
					String partialWord = curTree.getTerminalStr();						
					String curLabel = curTree.getLabel();
					//if (!partdict.containsKey(partialWord))
					//	partdict.put(partialWord, new TreeMap<String, Integer>());
					//Statics.<String> increment(partdict.get(partialWord), curLabel);
					if(partialWord.length() == 1 && !curLabel.equals("s"))
					{
						System.out.println(secondKey);
						break;
					}
					
					if(curTree.getChildren().size() == 2)
					{
						List<String> leftKernels = findKernels(curTree.getChild(0));
						List<String> rightKernels = findKernels(curTree.getChild(1));
						if(leftKernels == null || rightKernels == null)
						{
							System.out.println(secondKey);
							break;
						}
						int leftKernelSize = leftKernels.size();
						int righttKernelSize = rightKernels.size();
						for(int idlx = 0; idlx < leftKernelSize; idlx++)
						{
							String leftKey = leftKernels.get(idlx);
							for(int idrx = 0; idrx < righttKernelSize; idrx++)
							{
								String rightKey = rightKernels.get(idrx);
								String transKey = leftKey+rightKey;
								
								//if (!transdict.containsKey(transKey))
								//	transdict.put(transKey, new TreeMap<String, Integer>());
								//Statics.<String> increment(transdict.get(transKey), curLabel);
								if(!transdict.containsKey(transKey))
								{
									System.out.println("error transkey");
									continue;
								}
								
								if(transdict.containsKey(transKey)
										&& transdict.get(transKey).keySet().size() > 1)
								{
									System.out.println(transKey + "\t" + subTree1.toString());
								}
							}
						}
					}
				}
				//sc.increment();
			}
		}
		
		printDict(outputFile+".dict", partdict, "part");
		printDict(outputFile+".trans", transdict, "trans");
		
		
		
		/*
		LoadDict(outputFile+".dict", partdict);
		// apply first rule
		
		for(String firstKey : wordstructure.keySet())
		{
			newWordstructure.put(firstKey, new HashMap<String, Integer>());
			for(String secondKey: wordstructure.get(firstKey).keySet())
			{
				if(secondKey.trim().length() < 4)continue;
				
				final PennTreeReader reader = new PennTreeReader(
						new StringReader(secondKey.trim()));
				Tree<String> tree = reader.next();
				Tree<String> subTree1 = tree;
				if(subTree1 == null) continue;
				while (subTree1.getLabel().equalsIgnoreCase("root")
						|| subTree1.getLabel().equalsIgnoreCase("top")) {
					tree = tree.getChild(0);
					subTree1 = subTree1.getChild(0);
				}
				
				String topLabel = subTree1.getLabel().trim();
				int freqStartIndex = topLabel.indexOf("]#") + 2;
				int posStartIndex = topLabel.lastIndexOf("][") + 2;
				String word = topLabel.substring(1, posStartIndex-2);
				String pos = "";
				int freq = 1;
				if(freqStartIndex != 1)
				{
					pos = topLabel.substring(posStartIndex, freqStartIndex-2);
					freq = Integer.parseInt(topLabel.substring(freqStartIndex));
				}
				else
				{
					pos = topLabel.substring(posStartIndex, topLabel.length()-1);
				}
				if(pos.equals("NR") || pos.equals("CD"))
				{
					newWordstructure.get(firstKey).put(secondKey, 1);
					continue;
				}
				
				List<Tree<String>> allTrees = subTree1.getChild(0).getNonTerminals();
				for(Tree<String> curTree : allTrees)
				{
					if(curTree.isPreTerminal() )continue;
					
					//String partialWord = curTree.getTerminalStr();
					applypartWordRules(curTree, partdict);
				}
				newWordstructure.get(firstKey).put(subTree1.toString(), 1);
			}
		}
		*/
		
		/*
		LoadDict(outputFile+".trans", transdict);
		// apply first rule
		
		for(String firstKey : wordstructure.keySet())
		{
			newWordstructure.put(firstKey, new HashMap<String, Integer>());
			for(String secondKey: wordstructure.get(firstKey).keySet())
			{
				if(secondKey.trim().length() < 4)continue;
				
				final PennTreeReader reader = new PennTreeReader(
						new StringReader(secondKey.trim()));
				Tree<String> tree = reader.next();
				Tree<String> subTree1 = tree;
				if(subTree1 == null) continue;
				while (subTree1.getLabel().equalsIgnoreCase("root")
						|| subTree1.getLabel().equalsIgnoreCase("top")) {
					tree = tree.getChild(0);
					subTree1 = subTree1.getChild(0);
				}
				
				String topLabel = subTree1.getLabel().trim();
				int freqStartIndex = topLabel.indexOf("]#") + 2;
				int posStartIndex = topLabel.lastIndexOf("][") + 2;
				String word = topLabel.substring(1, posStartIndex-2);
				String pos = "";
				int freq = 1;
				if(freqStartIndex != 1)
				{
					pos = topLabel.substring(posStartIndex, freqStartIndex-2);
					freq = Integer.parseInt(topLabel.substring(freqStartIndex));
				}
				else
				{
					pos = topLabel.substring(posStartIndex, topLabel.length()-1);
				}
				if(pos.equals("NR") || pos.equals("CD"))
				{
					newWordstructure.get(firstKey).put(secondKey, 1);
					continue;
				}
				
				List<Tree<String>> allTrees = subTree1.getChild(0).getNonTerminals();
				for(Tree<String> curTree : allTrees)
				{
					if(curTree.isPreTerminal() )continue;
					
					//String partialWord = curTree.getTerminalStr();
					applytransWordRules(curTree, transdict);
				}
				newWordstructure.get(firstKey).put(subTree1.toString(), 1);
			}
		}
		*/
	}

	// print general dictionary
	public static void printDict(String outputFile, Map<String, Map<String, Integer>> outDict, String mark) throws Exception
	{
		List<Entry<String, Map<String, Integer>>> chapossortlist = new ArrayList<Entry<String, Map<String, Integer>>>(outDict.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		});
		
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8"), false);
		for(Entry<String, Map<String, Integer>> curCharPoslist: chapossortlist)
		{
			List<Entry<String, Integer>> synsortlist = new ArrayList<Entry<String, Integer>>(curCharPoslist.getValue().entrySet());
			
			Collections.sort(synsortlist, new Comparator(){   
				public int compare(Object o1, Object o2) {    
					Map.Entry obj1 = (Map.Entry) o1;
					Map.Entry obj2 = (Map.Entry) o2;
					Integer s1 = (Integer) obj1.getValue();
					Integer s2 = (Integer) obj1.getValue();
					return s1.compareTo(s2)	;
	            }   
			});
			out.print(mark + "\t" + curCharPoslist.getKey());
			int icount = 0;
			for(Entry<String, Integer> curSyn: synsortlist)
			{	
				if(icount == 0)
				{
					out.print(String.format("\t%s:%d", curSyn.getKey(), 5*curSyn.getValue()));
				}
				else
				{
					out.print(String.format("\t%s:%d", curSyn.getKey(), 5*curSyn.getValue(), icount));
				}
				icount++;
			}
			
			out.println();
			
		}
		
		out.close();
	}
	
	public static void LoadDict(String inputFile, Map<String, Map<String, Integer>> partdict) throws Exception
	{
		BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile),"UTF-8"));
		String sLine = null;
		
		
		
		while((sLine=bf.readLine())!=null )
		{	
			if(sLine.trim().length() < 2)continue;
			
			sLine = Statics.trimSpecial(sLine);
			String[] p = sLine.split("\t");
			Map<String, Integer> lsOpen = new HashMap<String, Integer>();
			//int iTotalCount = 0;
			for (int i = 1; i < p.length; ++i)
			{
				String[] pp = p[i].split(":");
				String sTag = pp[0];
				if(sTag.length() > 2 
				&& sTag.substring(sTag.length()-2).startsWith("#"))
				{
					sTag = sTag.substring(0, sTag.length()-2);
				}
				
				int iCount = Integer.parseInt(pp[1]);

				lsOpen.put(pp[0], iCount);
			}
			
			partdict.put(p[0], lsOpen);
		}
		
		bf.close();
	}
	
	public static void applytransWordRules(Tree<String> tree, Map<String, Map<String, Integer>> transdict)
	{
		if(tree.isPreTerminal() || tree.isLeaf() || tree.getChildren().size() != 2)return;
		//String terminalWord = tree.getTerminalStr();
		//if(terminalWord.length() == 1) return;
		
		List<String> leftKernels = findKernels(tree.getChild(0));
		List<String> rightKernels = findKernels(tree.getChild(1));
		
		Set<String> candidatesLabels = new HashSet<String>();
		
		int leftKernelSize = leftKernels.size();
		int righttKernelSize = rightKernels.size();
		for(int idlx = 0; idlx < leftKernelSize; idlx++)
		{
			String leftKey = leftKernels.get(idlx);
			for(int idrx = 0; idrx < righttKernelSize; idrx++)
			{
				String rightKey = rightKernels.get(idrx);
				String transKey = leftKey+rightKey;
				
				if(transdict.containsKey(transKey) && transdict.get(transKey).keySet().size()== 1)
				{
					String[] labels = new String[1];
					transdict.get(transKey).keySet().toArray(labels);
					candidatesLabels.add(labels[0]);
				}				
			}
		}
		
		
		if(candidatesLabels.size()== 1)
		{
			String[] labels = new String[1];
			candidatesLabels.toArray(labels);
			//if(labels.length == 1)
			//{
				tree.setLabel(labels[0]);
			//}
		}
		for(Tree<String> oneTree : tree.getChildren())
		{
			applytransWordRules(oneTree, transdict);
		}
	}
	
	
	public static void applypartWordRules(Tree<String> tree, Map<String, Map<String, Integer>> partdict)
	{
		if(tree.isPreTerminal() || tree.isLeaf())return;
		String terminalWord = tree.getTerminalStr();
		if(terminalWord.length() == 1) return;
		if(partdict.containsKey(terminalWord) && partdict.get(terminalWord).keySet().size()== 1)
		{
			
			String[] labels = new String[1];
			partdict.get(terminalWord).keySet().toArray(labels);
			//if(labels.length == 1)
			//{
				tree.setLabel(labels[0]);
			//}
		}
		for(Tree<String> oneTree : tree.getChildren())
		{
			applypartWordRules(oneTree, partdict);
		}
	}

}
