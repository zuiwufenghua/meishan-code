package cips2012;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.berkeley.nlp.syntax.Tree;

public class POSBagging {
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		int maxGuidePOSBaggingNum = 0;
		String guidePOSFileName = args[0];
		String posResultFile = args[1];
		maxGuidePOSBaggingNum = Integer.parseInt(args[2]);
		int maxNum = 0, minNum = Integer.MAX_VALUE;
		List<List<Tree<String>>> posGuideResults = new ArrayList<List<Tree<String>>>();
		for (int i = 1; i <= maxGuidePOSBaggingNum; i++) {
			String fileName = guidePOSFileName + String.format(".%d", i);
			File file = new File(fileName);
			if (!file.exists())
				continue;
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
				for (int idx = 0; bvalidInput && idx < wordposs.length; idx++) {
					int lastsplitIndex = wordposs[idx].lastIndexOf("_");
					if (lastsplitIndex != -1) {
						Tree<String> newWordTree = new Tree<String>(
								wordposs[idx].substring(lastsplitIndex + 1));
						List<Tree<String>> curWordChildren = new ArrayList<Tree<String>>();
						curWordChildren.add(new Tree<String>(wordposs[idx]
								.substring(0, lastsplitIndex)));
						newWordTree.setChildren(curWordChildren);
						curChildren.add(newWordTree);
					} else {
						System.out
								.println(fileName + " format error: " + sLine);
						bvalidInput = false;
						break;
					}
				}
				if (bvalidInput) {
					newTree.setChildren(curChildren);
				}

				trees.add(newTree);

			}
			posGuideResults.add(trees);
			bf.close();
			if (trees.size() > maxNum) {
				maxNum = trees.size();
			}
			if (trees.size() < minNum) {
				minNum = trees.size();
			}
		}

		if (maxNum != minNum) {
			System.out.println("Test number mismatch.");
			return;
		}

		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(posResultFile), "UTF-8"));

		for (int k = 0; k < maxNum; k++) {
			List<Tree<String>> oneTreeResults = new ArrayList<Tree<String>>();

			List<String> curWords = new ArrayList<String>();
			List<Map<String, Double>> curWordPoss = new ArrayList<Map<String, Double>>();
			for (int i = 0; i < posGuideResults.size(); i++) {
				Tree<String> curTree = posGuideResults.get(i).get(k);
				if (curTree.getTerminalYield().size() < 2)
					continue;

				if (curWords.size() == 0) {
					for (String curWord : curTree.getTerminalYield()) {
						curWords.add(curWord);
					}
					for (String curPos : curTree.getPreTerminalYield()) {
						Map<String, Double> curWordPos = new HashMap<String, Double>();
						curWordPos.put(curPos, 1.0);
						curWordPoss.add(curWordPos);
					}
				} else {
					List<String> theWords = curTree.getTerminalYield();
					boolean bmatch = true;
					if (theWords.size() != curWords.size()) {
						bmatch = false;
						continue;
					}

					for (int idx = 0; idx < theWords.size(); idx++) {
						if (!theWords.get(idx).equals(curWords.get(idx))) {
							bmatch = false;
							break;
						}
					}
					if (!bmatch)
						continue;
					List<String> theWordPoss = curTree.getPreTerminalYield();
					for (int idx = 0; idx < theWords.size(); idx++) {
						Map<String, Double> curWordPos = curWordPoss.get(idx);
						if (!curWordPos.containsKey(theWordPoss.get(idx))) {
							curWordPos.put(theWordPoss.get(idx), 0.0);
						}
						curWordPos.put(theWordPoss.get(idx),
								curWordPos.get(theWordPoss.get(idx)) + 1);
					}

				}
				oneTreeResults.add(curTree);				
			}
			
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
			
			writer.println(oneLine.trim());
		}
		
		writer.close();

	}

}
