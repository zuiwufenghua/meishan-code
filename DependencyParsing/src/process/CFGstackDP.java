package process;


import java.io.*;
import java.util.*;

import edu.berkeley.nlp.syntax.Constituent;
import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;
import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

import edu.berkeley.nlp.syntax.*;

public class CFGstackDP {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Tree<String>> senCFGMap = new HashMap<String, Tree<String>>();
		BufferedReader cfgreader = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8"));

		String sLine = null;
		while ((sLine = cfgreader.readLine()) != null) {
			PennTreeReader reader = new PennTreeReader(new StringReader(
					sLine.trim()));

			while (reader.hasNext()) {
				Tree<String> tree = reader.next();
				List<String> forms = tree.getYield();
				String sentence = "";
				for (String theWord : forms) {
					sentence = sentence + " " + theWord;
				}
				sentence = sentence.trim();
				senCFGMap.put(sentence, tree);
			}
		}
		cfgreader.close();

		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();
		sdpCorpusReader.Init(args[1]);

		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		int totalInstances = vecInstances.size();

		Map<String, Integer> treeTypes = new HashMap<String, Integer>();
		Map<String, Integer> tree2Int = new HashMap<String, Integer>();

		int argLength = args.length;
		boolean bFeatsAlready = false;
		boolean bFeatsFix = false;
		int maxFeaturesId = -1;
		if (args[argLength - 1].equals("-feats")) {
			bFeatsAlready = true;
		}
		
		if (args[argLength - 1].equals("-featsfix")) {
			bFeatsAlready = true;
			bFeatsFix = true;
		}

		if (bFeatsAlready) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(args[2]), "UTF-8"));
			while ((sLine = reader.readLine()) != null) {
				if (sLine.trim().equals(""))
					continue;
				String[] curUnits = sLine.trim().split("\\s+");
				if(!bFeatsFix)
				{
					try {
						int featId = Integer.parseInt(curUnits[1]);
						int featfreq = Integer.parseInt(curUnits[2]);
						tree2Int.put(curUnits[0], featId);
						treeTypes.put(curUnits[0], featfreq);
					} catch (Exception ex) {
						System.out.println("Error reading feats file: +" + sLine
								+ "\n");
						return;
					}
				}
				else
				{
					try {
						int featId = Integer.parseInt(curUnits[1]);
						//int featfreq = Integer.parseInt(curUnits[2]);
						tree2Int.put(curUnits[0], featId);
						if(featId > maxFeaturesId)
						{
							maxFeaturesId = featId;
						}
						treeTypes.put(curUnits[0], 1);
					} catch (Exception ex) {
						System.out.println("Error reading feats file: +" + sLine
								+ "\n");
						return;
					}
				}
			}
		}
		System.out.println(String.format("MaxFeatures ID: %d", maxFeaturesId));
		
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[3]), "UTF-8"));
		for (int senId = 0; senId < totalInstances; senId++) {
			if ((senId + 1) % 500 == 0) {
				System.out.println(String.format(
						"process instance %d, feat size: %d", senId + 1,
						tree2Int.size()));
			}
			DepInstance tmpInstance = vecInstances.get(senId);

			Integer[] predheads = new Integer[tmpInstance.heads.size()];
			tmpInstance.heads.toArray(predheads);
			String[] predlabels = new String[tmpInstance.deprels.size()];
			tmpInstance.deprels.toArray(predlabels);
			String[] predForms = new String[tmpInstance.forms.size()];
			tmpInstance.forms.toArray(predForms);

			String sentence = "";
			for (String theWord : predForms) {
				sentence = sentence + " " + theWord;
			}
			sentence = sentence.trim();

			Tree<String> tree = senCFGMap.get(sentence);
			tree.annotateSubTrees();

			if (tree == null) {
				System.out.println(sentence);
				continue;
			}
			List<String> words = tree.getYield();
			int n = tree.getYield().size() + 1;
			if (n != predForms.length + 1) {
				System.out.println(sentence + "\t" + tree.toString());
				continue;
			}
			List<String> feats = new ArrayList<String>();
			for (int i = 0; i < n - 2; i++) {
				int totalSize = (n - i - 1) * (n - i - 2);
				//int totalSize = (n - i - 2);
				int[] featsId = new int[totalSize];
				for (int j = 0; j < totalSize; j++)
					featsId[j] = -1;
				for (int j = i + 1; j < n - 1; j++) {
					Constituent<String> tmpTree0 = tree
							.getLeastCommonAncestorConstituent(i, j + 1);
					Tree<String> tmpTree1 = tree.getTopTreeForSpan(
							tmpTree0.getStart(), tmpTree0.getEnd());
					List<Integer> tmpIds = new ArrayList<Integer>();
					tmpIds.add(i);
					tmpIds.add(j);
					if(-tmpTree0.getStart()+1 != 0)
					{
						System.out.print("");
					}
					Tree<String> biTree1 = tmpTree1.getSubTrees(tmpIds, -tmpTree0.getStart()+1);
					Tree<String> biTree2 = tmpTree1.getSubTreeStructure(tmpIds, -tmpTree0.getStart()+1);
					
					String tmpOutput = simplify(biTree1.toString(4)) + simplify(biTree2.toString(4));
					tmpOutput = tmpOutput.replaceAll(" ", "");
					
					Integer curId = tree2Int.get(tmpOutput);

					if(!bFeatsFix)
					{
						if (curId == null) {
							curId = tree2Int.size();
							tree2Int.put(tmpOutput, curId);
							treeTypes.put(tmpOutput, 0);
						}
						treeTypes.put(tmpOutput, treeTypes.get(tmpOutput) + 1);
					}
					else
					{
						if(curId != null)
						{
							treeTypes.put(tmpOutput, treeTypes.get(tmpOutput) + 1);
						}
						else
						{
							curId = maxFeaturesId+1;
						}
					}
					featsId[2*(j - i - 1)] = curId;
					
					tmpOutput = tmpTree1.getContext(tmpIds);

					curId = tree2Int.get(tmpOutput);

					if(!bFeatsFix)
					{
						if (curId == null) {
							curId = tree2Int.size();
							tree2Int.put(tmpOutput, curId);
							treeTypes.put(tmpOutput, 0);
						}
						treeTypes.put(tmpOutput, treeTypes.get(tmpOutput) + 1);
					}
					else
					{
						if(curId != null)
						{
							treeTypes.put(tmpOutput, treeTypes.get(tmpOutput) + 1);
						}
						else
						{
							curId = maxFeaturesId+1;
						}
					}
					featsId[2*(j - i - 1)+1] = curId;
					
					for (int k = i + 1; k < j; k++) {
						List<Integer> tmpIdjs = new ArrayList<Integer>();
						tmpIdjs.add(i);
						tmpIdjs.add(k);
						tmpIdjs.add(j);
						
						Tree<String> triTree1 = tmpTree1.getSubTrees(tmpIdjs, -tmpTree0.getStart()+1);
						Tree<String> triTree2 = tmpTree1.getSubTreeStructure(tmpIdjs, -tmpTree0.getStart()+1);
						
						tmpOutput = simplify(triTree1.toString(4)) + simplify(triTree2.toString(4));
						tmpOutput = tmpOutput.replaceAll(" ", "");
						curId = tree2Int.get(tmpOutput);
						if(!bFeatsFix)
						{
							if (curId == null) {
								curId = tree2Int.size();
								tree2Int.put(tmpOutput, curId);
								treeTypes.put(tmpOutput, 0);
							}
							treeTypes.put(tmpOutput, treeTypes.get(tmpOutput) + 1);
						}
						else
						{
							if(curId != null)
							{
								treeTypes.put(tmpOutput, treeTypes.get(tmpOutput) + 1);
							}
							else
							{
								curId = maxFeaturesId+1;
							}
						}
						// tmpfeatures = tmpfeatures + "_" + String.format("%d",
						// tree2Int.get(tmpOutput));
						int thecurId = 2*((k - i) * (2 * n - i - k - 3) / 2 + j
								- k - 1);
						if (featsId[thecurId] > 0) {
							System.out.println();
						}
						featsId[thecurId] = curId;
						
						tmpOutput = tmpTree1.getContext(tmpIdjs);

						curId = tree2Int.get(tmpOutput);
						if(!bFeatsFix)
						{
							if (curId == null) {
								curId = tree2Int.size();
								tree2Int.put(tmpOutput, curId);
								treeTypes.put(tmpOutput, 0);
							}
							treeTypes.put(tmpOutput, treeTypes.get(tmpOutput) + 1);
						}
						else
						{
							if(curId != null)
							{
								treeTypes.put(tmpOutput, treeTypes.get(tmpOutput) + 1);
							}
							else
							{
								curId = maxFeaturesId+1;
							}
						}
						// tmpfeatures = tmpfeatures + "_" + String.format("%d",
						// tree2Int.get(tmpOutput));
						thecurId = 2*((k - i) * (2 * n - i - k - 3) / 2 + j
								- k - 1)+1;
						if (featsId[thecurId] > 0) {
							System.out.println();
						}
						featsId[thecurId] = curId;
					}
				}
				// tmpfeatures = tmpfeatures.substring(1);

				String tmpfeatures = String.format("p%d", featsId[0]);
				for (int j = 1; j < totalSize; j++)
					tmpfeatures = tmpfeatures + "_"
							+ String.format("p%d", featsId[j]);

				feats.add(tmpfeatures);

			}

			feats.add("_");
			try {
				List<String> outputSentence = new ArrayList<String>();
				tmpInstance.toGoldListString(outputSentence, feats);

				for (String oneLine : outputSentence) {
					writer.println(oneLine);
				}
				writer.println();
			} catch (Exception ex) {
				System.out.println(sentence + "\t" + tree.toString());
				continue;
			}
		}

		writer.close();
		if(!bFeatsFix)
		{
			String outFeatFile = args[2];
			if (bFeatsAlready)
				outFeatFile = outFeatFile + ".new";
			writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
					outFeatFile), "UTF-8"));
			for (String type : tree2Int.keySet()) {
				writer.println(String.format("%s\t%d\t%d", type,
						tree2Int.get(type), treeTypes.get(type)));
			}
			writer.close();
		}

	}

	public static String simplify(String input) {
		int lastIndex = 0;
		String output = "";
		while (lastIndex < input.length()) {
			int bracketfirstIndex = input.indexOf(")", lastIndex);
			if (bracketfirstIndex < 0)
				break;
			int i = bracketfirstIndex - 1;
			for (; i >= lastIndex; i--) {
				if (input.substring(i, i + 1).equals(" ")) {
					break;
				}
			}
			if (i == lastIndex-1)
				break;

			output = output + input.substring(lastIndex, i);
			i = bracketfirstIndex + 1;

			for (; i < input.length(); i++) {
				if (!input.substring(i, i + 1).equals(")")) {
					break;
				}
			}
			output = output + input.substring(bracketfirstIndex, i);
			lastIndex = i;
		}

		int slashIndex = output.indexOf("-");
		while (slashIndex != -1) {
			int endIndex = slashIndex + 1;
			for (; endIndex < output.length(); endIndex++) {
				if (output.substring(endIndex, endIndex + 1).equals(")")
						|| output.substring(endIndex, endIndex + 1).equals("(")) {
					break;
				}
			}
			output = output.substring(0, slashIndex)
					+ output.substring(endIndex);
			slashIndex = output.indexOf("-");
		}

		return output;
	}

}
