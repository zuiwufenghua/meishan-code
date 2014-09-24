package cips2012;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

import edu.berkeley.nlp.syntax.*;

public class CSBagging {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String orginalFileName = args[0];
		int maxBaggingNum = Integer.parseInt(args[2]);
		String outBaggingFile = args[1];
		List<List<Tree<String>>> inputResults = new ArrayList<List<Tree<String>>>();
		boolean guideCS = false;
		String guideCSFileName = null;
		String guideModelName = null;
		int maxGuideBaggingNum = 0;
		if(args.length > 3)
		{			
			guideCSFileName = args[3];
			guideModelName = args[4];
			maxGuideBaggingNum = Integer.parseInt(args[5]);
			if(maxGuideBaggingNum > 0) guideCS = true;
		}
		String guidePOSFileName = null;
		int  maxGuidePOSBaggingNum = 0;
		//boolean guidePOS = false;
		if(args.length > 6)
		{
			//guidePOS = true;
			guidePOSFileName = args[6];
			maxGuidePOSBaggingNum = Integer.parseInt(args[7]);
			//if(maxGuidePOSBaggingNum > 0) guidePOS = true;
		}

		int minNum = Integer.MAX_VALUE;
		int maxNum = 0;
		for (int i = 1; i <= maxBaggingNum; i++) {
			String fileName = orginalFileName + String.format(".%d", i);
			File file = new File(fileName);
			if(!file.exists())continue;
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");

			BufferedReader bf = null;
			bf = new BufferedReader(isr);
			List<Tree<String>> trees = new ArrayList<Tree<String>>();
			String sLine = null;
			while ((sLine = bf.readLine()) != null) {

				final PennTreeReader reader = new PennTreeReader(
						new StringReader(sLine.trim()));
				final Tree<String> tree = reader.next();
				Tree<String> subTree = tree;
				while(subTree.getLabel().equalsIgnoreCase("root") 
					|| subTree.getLabel().equalsIgnoreCase("top")
						)
				{
					subTree = subTree.getChild(0);
				}
				//if(i == 16)
				//{
				//	System.out.println();
				//}
				if(tree != null)
				{
					trees.add(subTree);	
				}
			}
			inputResults.add(trees);
			bf.close();
			if(trees.size() > maxNum)
			{
				maxNum = trees.size();
			}
			if(trees.size() < minNum)
			{
				minNum = trees.size();
			}
		}
		
		if(maxNum != minNum)
		{
			System.out.println("Test number mismatch.");
			return;
		}
		
		
		List<List<Tree<String>>> guideResults = new ArrayList<List<Tree<String>>>();
		
		for (int i = 1; i <= maxGuideBaggingNum; i++) {
			String fileName = guideCSFileName + String.format(".%d", i);
			File file = new File(fileName);
			if(!file.exists())continue;
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");

			BufferedReader bf = null;
			bf = new BufferedReader(isr);
			List<Tree<String>> trees = new ArrayList<Tree<String>>();
			String sLine = null;
			while ((sLine = bf.readLine()) != null) {

				final PennTreeReader reader = new PennTreeReader(
						new StringReader(sLine.trim()));
				final Tree<String> tree = reader.next();
				//if(i == 16)
				//{
				//	System.out.println();
				//}
				Tree<String> subTree = tree;
				while(subTree.getLabel().equalsIgnoreCase("root") 
						|| subTree.getLabel().equalsIgnoreCase("top")
							)
					{
						subTree = subTree.getChild(0);
					}
				if(tree != null)
				{
					trees.add(subTree);	
				}
			}
			guideResults.add(trees);
			bf.close();
			if(trees.size() > maxNum)
			{
				maxNum = trees.size();
			}
			if(trees.size() < minNum)
			{
				minNum = trees.size();
			}
		}
		
		if(maxNum != minNum)
		{
			System.out.println("Test number mismatch.");
			return;
		}
		
		List<List<Tree<String>>> posGuideResults = new ArrayList<List<Tree<String>>>();
		for (int i = 1; i <= maxGuidePOSBaggingNum; i++) {
			String fileName = guidePOSFileName + String.format(".%d", i);
			File file = new File(fileName);
			if(!file.exists())continue;
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");

			BufferedReader bf = null;
			bf = new BufferedReader(isr);
			List<Tree<String>> trees = new ArrayList<Tree<String>>();
			String sLine = null;
			while ((sLine = bf.readLine()) != null) {
				Tree<String> newTree = new Tree<String>("POS");
				List<Tree<String>> curChildren = new ArrayList<Tree<String>>();
				
				String[] wordposs = sLine.trim().split("\\s+");
				boolean bvalidInput = true;
				for(int idx = 0; bvalidInput && idx < wordposs.length; idx++)
				{
					int lastsplitIndex = wordposs[idx].lastIndexOf("_");
					if(lastsplitIndex != -1)
					{
						Tree<String> newWordTree = new Tree<String>(wordposs[idx].substring(lastsplitIndex+1));
						List<Tree<String>> curWordChildren = new ArrayList<Tree<String>>();
						curWordChildren.add(new Tree<String>(wordposs[idx].substring(0, lastsplitIndex)));
						newWordTree.setChildren(curWordChildren);
						curChildren.add(newWordTree);
					}
					else
					{
						System.out.println(fileName + " format error: " + sLine);
						bvalidInput = false;
						break;
					}
				}
				if(bvalidInput)
				{
					newTree.setChildren(curChildren);
				}
				
				trees.add(newTree);	
				
			}
			posGuideResults.add(trees);
			bf.close();
			if(trees.size() > maxNum)
			{
				maxNum = trees.size();
			}
			if(trees.size() < minNum)
			{
				minNum = trees.size();
			}
		}
		
		if(maxNum != minNum)
		{
			System.out.println("Test number mismatch.");
			return;
		}
		
		CKYChartParser cky = new CKYChartParser();
		Map<String, TreeMap<String, Double>> treemapRule = null;
		Map<String, TreeMap<String, Double>> treemapPos = null;
		if(guideCS)
		{
			treemapRule = new TreeMap<String, TreeMap<String, Double>>();
			treemapPos = new TreeMap<String, TreeMap<String, Double>>();
			readModel(guideModelName, treemapRule, treemapPos);
			cky.setGuideModelParameters(treemapRule);
		}
		
		//PrintWriter writerpos = new PrintWriter(new OutputStreamWriter(
		//		new FileOutputStream(outBaggingFile + ".pos"), "UTF-8"));
		PrintWriter writersyn = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outBaggingFile + ".syn"), "UTF-8"));
		
		//PrintWriter writerlog = new PrintWriter(new OutputStreamWriter(
		//		new FileOutputStream(outBaggingFile + ".log"), "UTF-8"));
		
		
		List<Integer> examples = new ArrayList<Integer>();
		examples.add(5); examples.add(75); examples.add(1729);
		
		for(int k = 0; k < maxNum; k++)
		//for(int k : examples)
		{
			List<Tree<String>> oneTreeResults = new ArrayList<Tree<String>>();
			
			List<String> curWords = new ArrayList<String>();
			List<Map<String, Double>> curWordPoss = new ArrayList<Map<String, Double>>();
			for (int i = 0; i < inputResults.size(); i++)
			{
				Tree<String> curTree = inputResults.get(i).get(k);
				if(curTree.getTerminalYield().size() < 2)continue;
				//if(i < 15)
				//{
					//curTree = curTree.getChild(0);
				//}
				List<Tree<String>> childlist = curTree.getChildren();
				for (int index = 0; index < childlist.size(); index++) {
					cky.convertTree(curTree, index);
				}
				if(childlist.size() == 1)
				{
					String rootLabel = curTree.getLabel();
					curTree = curTree.getChild(0);
					curTree.setLabel(rootLabel + "@" + curTree.getLabel());
				}
				if(curWords.size() == 0)
				{
					for(String curWord : curTree.getTerminalYield())
					{
						curWords.add(curWord);
					}
					for(String curPos : curTree.getPreTerminalYield())
					{
						Map<String, Double> curWordPos = new HashMap<String, Double>();
						curWordPos.put(curPos, 1.0);
						curWordPoss.add(curWordPos);
					}
				}
				else
				{
					List<String> theWords = curTree.getTerminalYield();
					boolean bmatch = true;
					if(theWords.size() != curWords.size())
					{
						bmatch = false;
						continue;
					}
					
					for(int idx = 0; idx < theWords.size(); idx++)
					{
						if(!theWords.get(idx).equals(curWords.get(idx)))
						{
							bmatch = false;
							break;
						}
					}
					if(!bmatch)continue;
					List<String> theWordPoss = curTree.getPreTerminalYield();
					for(int idx = 0; idx < theWords.size(); idx++)
					{
						Map<String, Double> curWordPos = curWordPoss.get(idx);
						if(!curWordPos.containsKey(theWordPoss.get(idx)))
						{
							curWordPos.put(theWordPoss.get(idx), 0.0);
						}
						curWordPos.put(theWordPoss.get(idx), curWordPos.get(theWordPoss.get(idx))+1);
					}
					
				}
				oneTreeResults.add(curTree);
			}
			if(oneTreeResults.size() == 0)
			{
				//writerpos.println("");
				writersyn.println("(())");
				continue;
			}
			
			List<Tree<String>> oneTreeGuidedResults = new ArrayList<Tree<String>>();
			
			for (int i = 0; i < guideResults.size(); i++)
			{
				Tree<String> curTree = guideResults.get(i).get(k);
				if(curTree.getTerminalYield().size() < 2)continue;
				//if(i < 15)
				//{
				//curTree = curTree.getChild(0);
				//}
				List<Tree<String>> childlist = curTree.getChildren();
				for (int index = 0; index < childlist.size(); index++) {
					cky.convertTree(curTree, index);
				}
				if(childlist.size() == 1)
				{
					String rootLabel = curTree.getLabel();
					curTree = curTree.getChild(0);
					curTree.setLabel(rootLabel + "@" + curTree.getLabel());
				}
				List<String> theWords = curTree.getTerminalYield();
				boolean bmatch = true;
				if(theWords.size() != curWords.size())
				{
					bmatch = false;
					continue;
				}
				
				/*
				for(int idx = 0; idx < theWords.size(); idx++)
				{
					if(!theWords.get(idx).equals(curWords.get(idx)))
					{
						bmatch = false;
						break;
					}
				}
				if(!bmatch)continue;
				*/
				List<String> theWordPoss = curTree.getPreTerminalYield();
				for(int idx = 0; idx < theWords.size(); idx++)
				{
					String theGuidePos = theWordPoss.get(idx);
					if(treemapPos.containsKey(theGuidePos))
					{
						for(String curTargetPOS : treemapPos.get(theGuidePos).keySet())
						{
							if(curWordPoss.get(idx).containsKey(curTargetPOS))
							{
								double primeScore = curWordPoss.get(idx).get(curTargetPOS);
								//curWordPoss.get(idx).put(curTargetPOS, primeScore + treemapPos.get(theGuidePos).get(curTargetPOS));
								curWordPoss.get(idx).put(curTargetPOS, primeScore + 1.0);
							}
						}
					}
				}
					
				oneTreeGuidedResults.add(curTree);
			}
			if(oneTreeResults.size() +  oneTreeGuidedResults.size() == 0)
			{
				//writerpos.println("");
				writersyn.println("(())");
				continue;
			}
			
			for (int i = 0; i < posGuideResults.size(); i++)
			{
				Tree<String> curTree = posGuideResults.get(i).get(k);
				if(curTree.getTerminalYield().size() < 2)continue;
				
				List<String> theWords = curTree.getTerminalYield();
				if(theWords.size() != curWords.size())
				{
					continue;
				}
				
				List<String> theWordPoss = curTree.getPreTerminalYield();
				for(int idx = 0; idx < theWords.size(); idx++)
				{
					String theGuidePos = theWordPoss.get(idx);
					List<String> addedKeys = new ArrayList<String>();
					for(String curPos : curWordPoss.get(idx).keySet())
					{
						String[] curPOSs = curPos.trim().split("@");
						if(curPOSs[curPOSs.length-1].equals(theGuidePos))
						{
							addedKeys.add(curPos);
						}
					}
					for(String curPOS : addedKeys)
					{
						double primeScore = curWordPoss.get(idx).get(curPOS);
						curWordPoss.get(idx).put(curPOS, primeScore + 1.0);
					}
									
				}
					
			}
			
			
			
			
			//��ȡ��õ�pos
			List<String> curPoss = new ArrayList<String>();
			List<Tree<String>> leavesTrees = new ArrayList<Tree<String>>();
			List<Double> leavesWeight = new ArrayList<Double>();
			List<Map<Tree<String>, Double>> leavesTreeMaps = new ArrayList<Map<Tree<String>, Double>>();
			String oneLine = "";
			for(int i = 0; i < curWords.size(); i++)
			{
				Map<String, Double> curWordPos = curWordPoss.get(i);
				double maxFreq = 0;
				String bestPos = null;
				Tree<String> child = new Tree<String>(curWords.get(i));
				List<Tree<String>> tmpConstructChilds = new ArrayList<Tree<String>>();
				tmpConstructChilds.add(child);
				
				Map<Tree<String>, Double> curCandTrees = new HashMap<Tree<String>, Double>();
				for(String curPos : curWordPos.keySet())
				{
					if(curWordPos.get(curPos) > maxFreq)
					{
						maxFreq = curWordPos.get(curPos);
						bestPos = curPos;
					}
					Tree<String> candTree = new Tree<String>(curPos);
					candTree.setChildren(tmpConstructChilds);
					curCandTrees.put(candTree, curWordPos.get(curPos)+ 0.0);
				}
				curPoss.add(bestPos);
				Tree<String> parent = new Tree<String>(bestPos);
				parent.setChildren(tmpConstructChilds);
				leavesTrees.add(parent);
				leavesTreeMaps.add(curCandTrees);
				leavesWeight.add(1.0);
				oneLine = oneLine + curWords.get(i) + "_" + bestPos + "\t";
			}
			
			//writerpos.println(oneLine.trim());
			
			if(oneTreeGuidedResults.size() > 0)
			{
				cky.reinitCKYByMultiResults(oneTreeResults, oneTreeGuidedResults);
			}
			else
			{
				cky.reinitCKYByMultiResults(oneTreeResults, null);
			}
			//for(Tree<String> tempTree : oneTreeResults)
			//{
			//	writerlog.println(tempTree.toString());
			//}
			//Tree<String> baggingResult = cky.CKYVersion4(leavesTrees, leavesWeight, "");
			//Tree<String> baggingResult = cky.CKYVersion5(leavesTrees, leavesWeight, "", writerlog);
			//Tree<String> baggingResult = cky.CKYVersion6(leavesTrees, leavesWeight, "", writerlog);
			Tree<String> baggingResult = cky.CKYVersion5(leavesTreeMaps, "");
			//writerlog.println("===========================================");
			if(baggingResult == null)
			{
				for(Tree<String> firstValidtree : oneTreeResults)
				{
					if(firstValidtree.getTerminalYield().size() >= 2)
					{
						baggingResult = firstValidtree;
						System.out.println(k);
						break;
					}
				}
			}
			cky.restoreTree(baggingResult);
			
			writersyn.println(baggingResult.toString());
		}
		
		//writerpos.close();
		writersyn.close();		
		//writerlog.close();
	}
	
	
	public static void readModel(String inputFile, Map<String, TreeMap<String, Double>> treemapRule, 
			Map<String, TreeMap<String, Double>> treemapPos) throws Exception
	{
		File file = new File(inputFile);
		if(!file.exists())return;
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");

		BufferedReader bf = null;
		bf = new BufferedReader(isr);
		String sLine = null;
		while ((sLine = bf.readLine()) != null) {
			sLine = sLine.trim();
			if(sLine.equals(""))continue;
			String[] units = sLine.split("\\)");
			if(units.length != 3)
			{
				System.out.println(sLine);
				continue;
			}
			String key1 = units[0].trim().substring(1);
			String key2 = units[1].trim().substring(1);
			double freq = 0.0;
			try
			{
				freq = Double.parseDouble(units[2].trim());
			}
			catch (Exception e)
			{
				System.out.println(sLine);
				continue;
			}
			
			String[] finegraiedUnits1 = key1.split("\t");
			String[] finegraiedUnits2 = key2.split("\t");
			if( (finegraiedUnits1.length == 1 && finegraiedUnits2.length != 1)
				|| (finegraiedUnits1.length == 3 && finegraiedUnits2.length == 4 && !finegraiedUnits2[3].equals("unmatch"))
				|| (finegraiedUnits1.length == 3 && finegraiedUnits2.length == 1 && !finegraiedUnits2[0].equals("#null#"))
				|| (finegraiedUnits1.length == 3 && finegraiedUnits2.length == 2)
				|| (finegraiedUnits1.length == 3 && finegraiedUnits2.length > 4 ))
			{
				System.out.println(sLine);
				continue;
			}
			
			if(finegraiedUnits1.length == 3 && finegraiedUnits2.length != 3 && freq < 14.5)
			{
				continue;
			}
			
			if(finegraiedUnits1.length == 3 && finegraiedUnits2.length == 3 && freq < 0.5)
			{
				continue;
			}
			
			boolean bPOS = false;
			if(finegraiedUnits1.length == 1)
			{
				bPOS = true;
			}
			else if(finegraiedUnits1.length != 3)
			{
				System.out.println(sLine);
				continue;
			}
			
			if(bPOS)
			{
				if(!treemapPos.containsKey(key1))
				{
					treemapPos.put(key1, new TreeMap<String, Double>());
				}
				
				Map<String, Double> key1map = treemapPos.get(key1);
				if(!key1map.containsKey(key2))
				{
					key1map.put(key2, 0.0);
				}
				
				key1map.put(key2, key1map.get(key2)+1);
			}
			else
			{
				if(!treemapRule.containsKey(key1))
				{
					treemapRule.put(key1, new TreeMap<String, Double>());
				}
				
				Map<String, Double> key1map = treemapRule.get(key1);
				if(!key1map.containsKey(key2))
				{
					key1map.put(key2, 0.0);
				}
				
				key1map.put(key2, key1map.get(key2)+freq);
			}
		}
		
		for(String key1 : treemapRule.keySet())
		{
			double totalCounts = 0.0;
			List<String> candMaps = new ArrayList<String>();
			for(String key2 : treemapRule.get(key1).keySet())
			{
				candMaps.add(key2);
				totalCounts = totalCounts + treemapRule.get(key1).get(key2);
			}
			
			for(String key2 : candMaps)
			{
				treemapRule.get(key1).put(key2, treemapRule.get(key1).get(key2)/totalCounts);
			}
		}
		
		for(String key1 : treemapPos.keySet())
		{
			double totalCounts = 0.0;
			List<String> candMaps = new ArrayList<String>();
			for(String key2 : treemapPos.get(key1).keySet())
			{
				candMaps.add(key2);
				totalCounts = totalCounts + treemapPos.get(key1).get(key2);
			}
			
			for(String key2 : candMaps)
			{
				treemapPos.get(key1).put(key2, treemapPos.get(key1).get(key2)/totalCounts);
			}
		}
	}
	
	

}
