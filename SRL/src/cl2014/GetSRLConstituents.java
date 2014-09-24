package cl2014;

import mason.rawbracket.CTB6CorpusReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class GetSRLConstituents {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		

		// from ctb6
		String ctbConstituentFold = args[0];
		int largest = 1200;
		Map<String, List<Tree<String>>> allTrees = new HashMap<String, List<Tree<String>>>();

		List<String> allsentences = new ArrayList<String>();

		File file = new File(ctbConstituentFold);

		String[] subFilenames = file.list();

		for (String subFilename : subFilenames) {
			String entirePath = String.format("%s\\%s", ctbConstituentFold,
					subFilename);
			int underlineIndex = subFilename.indexOf("_");
			int lastdotIndex = subFilename.indexOf(".", underlineIndex);
			String fileNameIdstr = subFilename.substring(underlineIndex + 1,
					lastdotIndex);
			// if(fileNameIdstr.)
			int fileNameId = -1;
			try {
				fileNameId = Integer.parseInt(fileNameIdstr);
			} catch (Exception x) {
				continue;
			}
			if (fileNameId > largest)
				continue;

			CTB6CorpusReader ctb6CorpusReader = new CTB6CorpusReader();
			ctb6CorpusReader.init(entirePath);

			allTrees.put(subFilename, new ArrayList<Tree<String>>());

			List<String> contentItems = ctb6CorpusReader.getBracketContent();
			// output[saveFileId].println(fileNameIdstr);
			String allContent = "";
			for (String oneLine : contentItems) {
				allContent = allContent + " " + oneLine;
			}
			allContent = allContent.trim();

			PennTreeReader reader = new PennTreeReader(new StringReader(
					allContent));

			while (reader.hasNext()) {
				// output[saveFileId].println(oneLine);
				// output[saveFileId].flush();
				Tree<String> tree = reader.next();
				tree.annotateSubTrees();
				tree.initParent();

				List<String> poss = tree.getPreTerminalYield();
				List<String> words = tree.getTerminalYield();
				String sentence = "";
				for (int index = 0; index < poss.size(); index++) {
					String curPOS = poss.get(index);
					String curWord = words.get(index);
					if (!curPOS.equalsIgnoreCase(("-NONE-"))) {
						int splitIndex = curPOS.indexOf("-");
						if (splitIndex != -1)
							curPOS = curPOS.substring(0, splitIndex);
						if (curPOS.equals("X"))
							curPOS = "M";
						if (curPOS.equals("NP"))
							curPOS = "NN";
						if (curPOS.equals("VP"))
							curPOS = "VV";
						sentence = sentence + " " + curWord;
					}
				}

				allsentences.add(sentence.trim());
				// Tree<String> tree = PennTreeReader.parseEasy(oneLine, false);
				allTrees.get(subFilename).add(tree);

			}

			ctb6CorpusReader.uninit();
		}

		//
		Map<String, Map<Integer, String>> srlLabels = new HashMap<String, Map<Integer, String>>();

		// verb srl
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[1]), "UTF-8"));
		String sLine = null;
		


		while ((sLine = in.readLine()) != null) {
			try {
				boolean containError = false;
				String newLine = sLine.trim();
				String[] theUnits = newLine.split("\\s+");
				if (theUnits.length < 6)
					continue;
				Tree<String> curTree = allTrees.get(theUnits[0]).get(
						Integer.parseInt(theUnits[1]));

				String headform = theUnits[4];
				int formEnd = headform.lastIndexOf(".");
				if (formEnd != -1) {
					headform = headform.substring(0, formEnd);
				}

				List<Tree<String>> posTrees = curTree.getPreTerminals();
				List<String> words = curTree.getTerminalYield();
				List<String> srllabel = new ArrayList<String>();
				for (String curWord : words) {
					srllabel.add("O");
				}

				for (int idx = 6; idx < theUnits.length; idx++) {
					int labelsplitIndex = theUnits[idx].indexOf("-");
					String curLabel = theUnits[idx]
							.substring(labelsplitIndex + 1);
					curLabel = refineSRLLabel(curLabel);
					if (curLabel.equalsIgnoreCase("rel")) {
						curLabel = "vy";
					}
					else
					{
						if(curLabel.equals(""))
						{
							containError = true;
							break;
						}
					}
					String labelstr = theUnits[idx].substring(0,
							labelsplitIndex);
					labelstr = labelstr.replace("*", " ");
					labelstr = labelstr.replace(",", " ");
					String[] smallunits = labelstr.split("\\s+");
					for (String theSmallUnit : smallunits) {
						int seperateIndex = theSmallUnit.indexOf(":");
						int position = Integer.parseInt(theSmallUnit.substring(
								0, seperateIndex));
						int height = Integer.parseInt(theSmallUnit
								.substring(seperateIndex + 1));
						if(curLabel.equals("ny")) height = 0;
						Tree<String> currentPOSTree = posTrees.get(position);
						while (height > 0) {
							currentPOSTree = currentPOSTree.parent;
							if (currentPOSTree == null) {
								// System.out.println(sLine);
								System.out.println(theUnits[idx]);
								containError = true;
								break;
							}
							height--;
						}

						if (containError) {
							break;
						}
						for (int idy = currentPOSTree.smaller; idy <= currentPOSTree.bigger; idy++) {
							if (srllabel.get(idy).equals("O")
									|| srllabel.get(idy).equals(curLabel)) {
								srllabel.set(idy, curLabel);
							} else {
								// System.out.println(sLine);
								System.out.println(String.format("%d, %s", idy,
										srllabel.get(idy)));
								containError = true;
								break;
							}
						}

						if (containError) {
							break;
						}
					}

				}

				if (!containError) {
					List<String> forms = new ArrayList<String>();
					List<String> poss = new ArrayList<String>();
					List<String> srls = new ArrayList<String>();
					int headword = -1;
					Set<String> argsnum = new HashSet<String>();
					String sentence = "";
					String sentencesrl = "";
					for (int index = 0; index < posTrees.size(); index++) {
						Tree<String> postree = posTrees.get(index);
						String curPOS = postree.getLabel();
						String curWord = words.get(index);
						String curSRL = srllabel.get(index);
						if (!curPOS.equalsIgnoreCase(("-NONE-"))) {
							forms.add(curWord);
							int splitIndex = curPOS.indexOf("-");
							if (splitIndex != -1)
								curPOS = curPOS.substring(0, splitIndex);
							if (curPOS.equals("X"))
								curPOS = "M";
							if (curPOS.equals("NP"))
								curPOS = "NN";
							if (curPOS.equals("VP"))
								curPOS = "VV";
							poss.add(curPOS);
							String lastSRL = "O";
							if (srls.size() > 0) {
								lastSRL = srls.get(srls.size() - 1);
							}
							if (curSRL.equals("O") || curSRL.equals("vy")) {
								if (curSRL.equals("vy")) {
									headword = srls.size();
									if (!headform.equals(curWord)) {
										containError = true;
										System.out.println("correct: " + headform + " wrong: " + curWord);
										break;
									}
								}
							} else {
								if (lastSRL.equals("O") || lastSRL.equals("vy")) {
									curSRL = "B-" + curSRL;
								} else if (!lastSRL.endsWith(curSRL)) {
									curSRL = "B-" + curSRL;
								} else if (lastSRL.endsWith(curSRL)) {
									curSRL = "I-" + curSRL;
								}
							}
							
							if (curSRL.equalsIgnoreCase("B-ARG1")
									|| curSRL.equalsIgnoreCase("B-ARG2")
									|| curSRL.equalsIgnoreCase("B-ARG3")
									|| curSRL.equalsIgnoreCase("B-ARG4")
									|| curSRL.equalsIgnoreCase("B-ARG0")
									|| curSRL.equalsIgnoreCase("B-ARG5")) {
								if (argsnum.contains(curSRL)) {
									// System.out.println(sLine);
									curSRL = "C-" + curSRL.substring(2);
									//System.out.println(curSRL + "duplicated.");
									//break;
								} else {
									argsnum.add(curSRL);
								}
							}
							srls.add(curSRL);

							sentence = sentence + " " + curWord;
							sentencesrl = sentencesrl + " " + curWord + "_"
									+ curSRL;
						}
					}
					
					if(headword == -1)
					{
						containError = true;
						System.out.println("correct: " + headform);
					}
					

					if (!containError) {
						sentence = sentence.trim();
						sentencesrl = sentencesrl.trim();

						if (!srlLabels.containsKey(sentence)) {
							srlLabels.put(sentence,
									new TreeMap<Integer, String>());
						}

						srlLabels.get(sentence).put(headword, sentencesrl);
					}
				}

				if (containError) {
					System.out.println(curTree);
					System.out.println(sLine);
				}

			} catch (Exception ex) {
				System.out.println(sLine);
			}

		}

		in.close();

		
		in = new BufferedReader(new InputStreamReader(new FileInputStream(
				args[2]), "UTF-8"));
		sLine = null;

		while ((sLine = in.readLine()) != null) {
			try {
				boolean containError = false;
				String newLine = sLine.trim();
				String[] theUnits = newLine.split("\\s+");
				if (theUnits.length < 6)
					continue;
				Tree<String> curTree = allTrees.get(theUnits[0]).get(
						Integer.parseInt(theUnits[1]));

				String headform = theUnits[4];
				int formEnd = headform.lastIndexOf(".");
				if (formEnd != -1) {
					headform = headform.substring(0, formEnd);
				}

				List<Tree<String>> posTrees = curTree.getPreTerminals();
				List<String> words = curTree.getTerminalYield();
				List<String> srllabel = new ArrayList<String>();
				for (String curWord : words) {
					srllabel.add("O");
				}

				for (int idx = 6; idx < theUnits.length; idx++) {
					int labelsplitIndex = theUnits[idx].indexOf("-");
					String curLabel = theUnits[idx]
							.substring(labelsplitIndex + 1);
					curLabel = refineSRLLabel(curLabel);
					if (curLabel.equalsIgnoreCase("rel")) {
						curLabel = "ny";
					}
					else
					{
						if(curLabel.equals(""))
						{
							containError = true;
							break;
						}
					}
					
					String labelstr = theUnits[idx].substring(0,
							labelsplitIndex);
					labelstr = labelstr.replace("*", " ");
					labelstr = labelstr.replace(",", " ");
					String[] smallunits = labelstr.split("\\s+");
					for (String theSmallUnit : smallunits) {
						int seperateIndex = theSmallUnit.indexOf(":");
						int position = Integer.parseInt(theSmallUnit.substring(
								0, seperateIndex));
						int height = Integer.parseInt(theSmallUnit
								.substring(seperateIndex + 1));
						if(curLabel.equals("ny")) height = 0;
						Tree<String> currentPOSTree = posTrees.get(position);
						while (height > 0) {
							currentPOSTree = currentPOSTree.parent;
							if (currentPOSTree == null) {
								// System.out.println(sLine);
								System.out.println(theUnits[idx]);
								containError = true;
								break;
							}
							height--;
						}

						if (containError) {
							break;
						}
						for (int idy = currentPOSTree.smaller; idy <= currentPOSTree.bigger; idy++) {
							if (srllabel.get(idy).equals("O")
									|| srllabel.get(idy).equals(curLabel)) {
								srllabel.set(idy, curLabel);
							} else {
								// System.out.println(sLine);
								System.out.println(String.format("%d, %s", idy,
										srllabel.get(idy)));
								containError = true;
								break;
							}
						}

						if (containError) {
							break;
						}
					}

				}

				if (!containError) {
					List<String> forms = new ArrayList<String>();
					List<String> poss = new ArrayList<String>();
					List<String> srls = new ArrayList<String>();
					int headword = -1;
					Set<String> argsnum = new HashSet<String>();
					String sentence = "";
					String sentencesrl = "";
					for (int index = 0; index < posTrees.size(); index++) {
						Tree<String> postree = posTrees.get(index);
						String curPOS = postree.getLabel();
						String curWord = words.get(index);
						String curSRL = srllabel.get(index);
						if (!curPOS.equalsIgnoreCase(("-NONE-"))) {
							forms.add(curWord);
							int splitIndex = curPOS.indexOf("-");
							if (splitIndex != -1)
								curPOS = curPOS.substring(0, splitIndex);
							if (curPOS.equals("X"))
								curPOS = "M";
							if (curPOS.equals("NP"))
								curPOS = "NN";
							if (curPOS.equals("VP"))
								curPOS = "VV";
							poss.add(curPOS);
							String lastSRL = "O";
							if (srls.size() > 0) {
								lastSRL = srls.get(srls.size() - 1);
							}
							if (curSRL.equals("O") || curSRL.equals("ny")) {
								if (curSRL.equals("ny")) {
									headword = srls.size();
									if (!headform.equals(curWord)) {
										containError = true;
										System.out.println("correct: " + headform + " wrong: " + curWord);
										break;
									}
								}
							} else {
								if (lastSRL.equals("O") || lastSRL.equals("ny")) {
									curSRL = "B-" + curSRL;
								} else if (!lastSRL.endsWith(curSRL)) {
									curSRL = "B-" + curSRL;
								} else if (lastSRL.endsWith(curSRL)) {
									curSRL = "I-" + curSRL;
								}
							}
							
							if (curSRL.equalsIgnoreCase("B-ARG1")
									|| curSRL.equalsIgnoreCase("B-ARG2")
									|| curSRL.equalsIgnoreCase("B-ARG3")
									|| curSRL.equalsIgnoreCase("B-ARG4")
									|| curSRL.equalsIgnoreCase("B-ARG0")
									|| curSRL.equalsIgnoreCase("B-ARG5")) {
								if (argsnum.contains(curSRL)) {
									// System.out.println(sLine);
									curSRL = "C-" + curSRL.substring(2);
									//System.out.println(curSRL + "duplicated.");
									//break;
								} else {
									argsnum.add(curSRL);
								}
							}
							srls.add(curSRL);
							sentence = sentence + " " + curWord;
							sentencesrl = sentencesrl + " " + curWord + "_"
									+ curSRL;
						}
					}
					
					if(headword == -1)
					{
						containError = true;
						System.out.println("correct: " + headform);
					}

					if (!containError) {
						sentence = sentence.trim();
						sentencesrl = sentencesrl.trim();

						if (!srlLabels.containsKey(sentence)) {
							srlLabels.put(sentence,
									new TreeMap<Integer, String>());
						}

						srlLabels.get(sentence).put(headword, sentencesrl);
					}
				}

				if (containError) {
					System.out.println(curTree);
					System.out.println(sLine);
				}

			} catch (Exception ex) {
				System.out.println(sLine);
			}

		}

		in.close(); 

		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[3]), "UTF-8"));
		for (String sentence : allsentences) {
			if (srlLabels.containsKey(sentence)) {
				for (int theKey : srlLabels.get(sentence).keySet()) {
					//output.println(sentence);
					//output.println(theKey);
					output.println(String.format("%d\t%s", theKey, srlLabels.get(sentence).get(theKey)));
					output.println();
				}
			}
		}

		output.close();

	}
	
	public static String refineSRLLabel(String curlabel)
	{
		Set<String> argnums = new HashSet<String>();
		argnums.add("ARG0");argnums.add("ARG1");argnums.add("ARG2");argnums.add("ARG3");argnums.add("ARG4");
		argnums.add("ARG5");argnums.add("SUP");argnums.add("REL");
		Set<String> argsFunctional = new HashSet<String>();
		argsFunctional.add("ADV");argsFunctional.add("BNF");argsFunctional.add("CND");argsFunctional.add("DIR");argsFunctional.add("DIS");
		argsFunctional.add("DGR");argsFunctional.add("EXT");argsFunctional.add("FRQ");argsFunctional.add("LOC");argsFunctional.add("MNR");
		argsFunctional.add("PRP");argsFunctional.add("TMP");argsFunctional.add("TPC");argsFunctional.add("NEG");
		String newLabel = curlabel.toUpperCase();
		int lastIndex = newLabel.lastIndexOf("-");
		if(lastIndex == -1)
		{
			if(argnums.contains(newLabel))
			{
				return newLabel;
			}
			else
			{
				System.out.println("error label : " + curlabel);
				return "";
			}
		}
		else
		{
			String firstPart = newLabel.substring(0, lastIndex);
			String lastPart = newLabel.substring(lastIndex+1);
			if(firstPart.equals("REL"))
			{
				return "REL";
			}
			if(argsFunctional.contains(lastPart))
			{
				return "ARGM-"+lastPart;
			}
			
			if(argnums.contains(firstPart))
			{
				return firstPart;
			}
			System.out.println("error label : " + curlabel);
			return "";
		}
	}

}
