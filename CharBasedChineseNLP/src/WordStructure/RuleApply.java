package WordStructure;

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
import java.util.Map.Entry;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class RuleApply {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		Set<String> allPOS = new HashSet<String>();

		for (String curPOS : ValidWordStructure.ssCtbTags) {
			allPOS.add(curPOS);
		}
		// TODO Auto-generated method stub
		String ruleFile = args[0];
		String inputFile = args[1];
		String outFileSearch = args[2];
		String applySearch = args[3];

		List<Tree<String>> sortedTrees = new ArrayList<Tree<String>>();
		removeDulpication(inputFile, allPOS, sortedTrees);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(ruleFile), "UTF-8"));
		String sLine = null;
		PrintWriter outSearch = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outFileSearch), "UTF-8"), false);
		PrintWriter outApply = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(applySearch), "UTF-8"), false);
		while ((sLine = in.readLine()) != null) {
			if (sLine.trim().equals(""))
				continue;
			String normazeLine1 = sLine.trim();
			String searchRule = normazeLine1.trim();
			String applyRule = null;
			int splitIndex = normazeLine1.indexOf("||");
			if (splitIndex != -1) {
				searchRule = normazeLine1.substring(0, splitIndex).trim();
				applyRule = normazeLine1.substring(splitIndex + 2).trim();
			}

			List<Tree<String>> filterRules = new ArrayList<Tree<String>>();
			for(Tree<String> oneTree : sortedTrees)
			{
				filterRules.add(oneTree);
			}
			String[] searchUnits = searchRule.split("\\s+");
			for (String curRuleUnit : searchUnits) {
				String[] curRuleUnitValues = curRuleUnit.split("_");
				if (curRuleUnitValues.length != 4)
					continue;
				try {
					int position = Integer.parseInt(curRuleUnitValues[0]);
					int height = Integer.parseInt(curRuleUnitValues[1]);
					int type = -1;
					if (curRuleUnitValues[2].equalsIgnoreCase("w")
							&& height == 0) {
						type = 0; // 匹配word
					} else if (curRuleUnitValues[2].equalsIgnoreCase("p")
							&& height == 1) {
						type = 1; // 匹配pos
					} else if (curRuleUnitValues[2].equalsIgnoreCase("l")
							&& height > 1) {
						type = height; // 匹配非终结符
					} else {
						continue;
					}

					String matchValue = curRuleUnitValues[3];
					for (int idx = filterRules.size() - 1; idx >= 0; idx--) {
						if (!bMatchRule(position, height, type, matchValue,
								filterRules.get(idx))) {
							filterRules.remove(idx);
						}
					}
				} catch (Exception e) {
					continue;
				}

			}

			for (int idx = 0; idx < filterRules.size(); idx++) {
				outSearch.println(filterRules.get(idx));
				if (applyRule != null) {
					String[] applyUnits = applyRule.split("\\s+");
					for (String curRuleUnit : applyUnits) {
						String[] curRuleUnitValues = curRuleUnit.split("_");
						if (curRuleUnitValues.length != 4)
							continue;
						try {
							int position = Integer
									.parseInt(curRuleUnitValues[0]);
							int height = Integer.parseInt(curRuleUnitValues[1]);
							int type = -1;
							if (curRuleUnitValues[2].equalsIgnoreCase("w")
									&& height == 0) {
								type = 0; // 匹配word
							} else if (curRuleUnitValues[2]
									.equalsIgnoreCase("p") && height == 1) {
								type = 1; // 匹配pos
							} else if (curRuleUnitValues[2]
									.equalsIgnoreCase("l") && height > 1) {
								type = height; // 匹配非终结符
							} else {
								continue;
							}

							String matchValue = curRuleUnitValues[3];

							ApplyRule(position, height, type, matchValue,
									filterRules.get(idx));
						} catch (Exception e) {
							continue;
						}
					}

				}
				outApply.println(filterRules.get(idx));
			}
		}

		in.close();
		outSearch.close();
		outApply.close();

	}

	public static void removeDulpication(String inputFile, Set<String> allPOS,
			List<Tree<String>> sortedTrees) throws Exception {
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(inputFile), "UTF-8"));

		Map<String, Tree<String>> wordpostemp = new HashMap<String, Tree<String>>();
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			if (sLine.trim().equals(""))
				continue;
			if (sLine.trim().replaceAll("\\s+", "").equals("(())")) {
				continue;
			}

			try {
				final PennTreeReader reader = new PennTreeReader(
						new StringReader(sLine.trim()));
				Tree<String> tree = reader.next();
				Tree<String> subTree1 = tree.getChild(0);

				while (subTree1.getLabel().equalsIgnoreCase("root")
						|| subTree1.getLabel().equalsIgnoreCase("top")) {
					tree = tree.getChild(0);
					subTree1 = subTree1.getChild(0);
				}

				tree.setLabel("TOP");

				if (tree.getChildren().size() != 1
						|| !tree.getChild(0).getLabel().equalsIgnoreCase("s")) {
					System.out.println("not have node S:\t" + tree.toString());
					continue;
				}

				if (!ValidWordStructure.checkLabels(tree.getChild(0), allPOS,
						true)) {
					System.out.println("check error:\t" + tree.toString());
					continue;
				}

				List<Tree<String>> prevleaves = tree.getPreTerminals();
				String theWord = "";
				String thePOS = "";

				boolean bValid = true;
				for (Tree<String> cuPrevrLeave : prevleaves) {
					theWord = theWord + cuPrevrLeave.getChild(0).getLabel();
					String curPOSTmp = cuPrevrLeave.getLabel();
					int jinIndex = curPOSTmp.lastIndexOf("#");
					String curPOS = curPOSTmp.substring(jinIndex + 1);
					if (thePOS.equals("")) {
						thePOS = curPOS;
					} else if (!thePOS.equals(curPOS)) {
						bValid = false;
						break;
					}
				}
				if (!bValid) {
					System.out.println("final pos not agreed:\t"
							+ tree.toString());
					continue;
				}

				if (wordpostemp.containsKey(theWord + "_" + thePOS)) {
					if (!wordpostemp.get(theWord + "_" + thePOS).toString()
							.replace("\\s+", "")
							.equals(tree.toString().replace("\\s+", ""))) {
						System.out
								.println("DUP: "
										+ theWord
										+ "_"
										+ thePOS
										+ ":\t0\t"
										+ wordpostemp.get(
												theWord + "_" + thePOS)
												.toString() + "\t1\t"
										+ tree.toString());
					}
				} else {
					tree.initParent();
					wordpostemp.put(theWord + "_" + thePOS, tree);

				}
			} catch (Exception e) {
				System.out.println("parse tree error:\t" + sLine);
			}

		}

		in.close();
		List<Entry<String, Tree<String>>> chapossortlist = new ArrayList<Entry<String, Tree<String>>>(
				wordpostemp.entrySet());

		Collections.sort(chapossortlist, new Comparator() {
			public int compare(Object o1, Object o2) {
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;

				return PinyinComparator.CompareModify((String) obj1.getKey(),
						(String) obj2.getKey());
			}
		});
		for (Entry<String, Tree<String>> curCharPoslist : chapossortlist) {
			sortedTrees.add(curCharPoslist.getValue());
		}

	}

	public static boolean bMatchRule(int position, int height, int type,
			String matchValue, Tree<String> root) {
		boolean bEqual = true;
		if(position < 0) 
		{
			position = -position;
			bEqual = false;
			
		}
		List<Tree<String>> leaves = root.getTerminals();
		if (position > leaves.size() && position <= 0)
			return true;
		if (height != type) {
			return true;
		}
		if (height == 0) {
			if (!matchValue.equals(leaves.get(position - 1).getLabel())) {
				return !bEqual;
			} else {
				return bEqual;
			}
		} else if (height == 1) {
			Tree<String> thePOSTree = leaves.get(position - 1).parent;
			String thePOS = thePOSTree.getLabel().substring(2).toLowerCase();
			if (!thePOS.startsWith(matchValue.toLowerCase())) {
				return !bEqual;
			} else {
				return bEqual;
			}
		} else if (height > 1) {
			int startHeight = 0;
			Tree<String> curTree = leaves.get(position - 1);
			while (startHeight < height) {
				if (curTree.parent != null) {
					curTree = curTree.parent;
					startHeight++;
				} else {
					break;
				}
			}
			if (startHeight < height)
				return true;
			String theLabel = curTree.getLabel().toLowerCase();
			if (!theLabel.startsWith(matchValue.toLowerCase())) {
				return !bEqual;
			} else {
				return bEqual;
			}
		}

		return true;
	}

	public static int ApplyRule(int position, int height, int type,
			String matchValue, Tree<String> root) {
		List<Tree<String>> leaves = root.getTerminals();
		if (position > leaves.size() && position <= 0)
			return 0;
		if (height != type) {
			return 0;
		}
		if (height == 0) {
			leaves.get(position - 1).setLabel(matchValue);
			return 1;
		} else if (height == 1) {
			Tree<String> thePOSTree = leaves.get(position - 1).parent;
			thePOSTree.setLabel(matchValue);
			return 1;
		} else if (height > 1) {
			int startHeight = 0;
			Tree<String> curTree = leaves.get(position - 1);
			while (startHeight < height) {
				if (curTree.parent != null) {
					curTree = curTree.parent;
					startHeight++;
				} else {
					break;
				}
			}
			if (startHeight < height)
				return 0;
			curTree.setLabel(matchValue);
			return 1;

		}

		return 0;
	}
}
