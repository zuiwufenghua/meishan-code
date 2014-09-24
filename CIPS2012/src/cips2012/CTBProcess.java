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

import edu.berkeley.nlp.syntax.*;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class CTBProcess {

	private static void Tongji(Tree<String> tree,
			TreeMap<String, Integer> treemap, Map<String, Double> rules,
			Set<String> nonTerminals) {
		if (tree.isLeaf() ) {
			return;
		}
		if (tree.isPreTerminal() ) {
			nonTerminals.add(tree.getFirstLabel());
			return;
		}
		List<String> yieldX = new ArrayList<String>();
		String temp1 = tree.getFirstLabel();
		nonTerminals.add(temp1);
		List<Tree<String>> childrenlist = tree.getChildren();
		for (Tree<String> child : childrenlist) {
			yieldX.add(child.getFirstLabel());
			nonTerminals.add(child.getFirstLabel());
		}
		String temp2 = yieldX.toString();
		String rule = String.format("%d\t", yieldX.size())
				+ temp1.concat(temp2);

		Integer value = treemap.get(rule);
		if (value == null) {
			treemap.put(rule, 1);
		} else {
			value = value + 1;
			treemap.put(rule, value);
		}

		if (yieldX.size() == 2) {
			String binaryRule = temp1 + "\t" + yieldX.get(0) + "\t"
					+ yieldX.get(1);

			Double iValue = rules.get(binaryRule);
			if (iValue == null) {
				rules.put(binaryRule, 1.0);
			} else {
				iValue = iValue + 1;
				rules.put(binaryRule, iValue);
			}
		}

		for (Tree<String> child : tree.getChildren()) {
			Tongji(child, treemap, rules, nonTerminals);
		}
	}

	/*
	private static void RemoveUniChild(Tree<String> parent, int index) {
		List<Tree<String>> childrenlist = parent.getChildren();

		Tree<String> child = childrenlist.get(index);
		if (child.isLeaf() || child.isPreTerminal()) {
			return;
		}

		List<Tree<String>> grandchildlist = child.getChildren();
		if (grandchildlist.size() == 1) {
			parent.setChild(index, grandchildlist.get(0));
			RemoveUniChild(parent, index);
		} else {
			for (int i = 0; i < grandchildlist.size(); i++) {
				RemoveUniChild(child, i);
			}
		}

	}
    */
	/*
	private static boolean Binarize(Tree<String> parent, int index,
			CKYChartParser cky, Map<String, Integer> midStatics) {
		Tree<String> tree = parent.getChild(index);
		if (tree.isLeaf() || tree.isPreTerminal()) {
			return true;
		}

		// List<String> yieldX=new ArrayList<String>();
		// String temp1=tree.getLabel();
		List<Tree<String>> childrenlist = tree.getChildren();
		// for (Tree<String> child : childrenlist) {
		// yieldX.add(child.getLabel());
		// }
		// String temp2=yieldX.toString();
		// String orginalrule= String.format("%d\t",
		// yieldX.size())+temp1.concat(temp2);

		if (childrenlist.size() == 2) {
			if(!Binarize(tree, 0, cky, midStatics)) return false;
			if(!Binarize(tree, 1, cky, midStatics))return false;
		} else if (childrenlist.size() > 2) {
			// ��������map������һ����ôת���ġ�
			String rootLabel = tree.getLabel();
			Tree<String> orgTree = new Tree<String>(tree.getLabel());
			List<Tree<String>> newChildrenlist = new ArrayList<Tree<String>>();
			List<Double> childWeights = new ArrayList<Double>();
			int curIndex = 0;
			List<Tree<String>> tmpConstructChilds = new ArrayList<Tree<String>>();
						
			for (Tree<String> curChild : childrenlist) {
				if(!Binarize(tree, curIndex, cky, midStatics))return false;
				newChildrenlist.add(new Tree<String>(curChild.getLabel()));
				tmpConstructChilds.add(new Tree<String>(curChild.getLabel()));
				childWeights.add(1.0);
				curIndex++;
			}
			orgTree.setChildren(tmpConstructChilds);

			Tree<String> newTree = cky.CKYVersion1(rootLabel, childrenlist,
					childWeights, "*");
			Tree<String> auxTree = cky.CKYVersion1(rootLabel, newChildrenlist,
					childWeights, "*");
			
			parent.setChild(index, newTree);
			if(newTree != null)
			{
				String rule = orgTree.toString() + "====>" + auxTree.toString();
	
				Integer value = midStatics.get(rule);
				if (value == null) {
					midStatics.put(rule, 1);
				} else {
					value = value + 1;
					midStatics.put(rule, value);
				}
			}
			else
			{
				String rule = orgTree.toString() + "====> error!";
				
				Integer value = midStatics.get(rule);
				if (value == null) {
					midStatics.put(rule, 1);
				} else {
					value = value + 1;
					midStatics.put(rule, value);
				}
				return false;
			}

		} else {
			System.out.println("Still exist rule A->B.");
			return false;
		}
		
		return true;
	}

	*/
	/**
	 * @param args
	 */
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		
		try {
			File modelFile = new File(args[0]);
			File transFile = new File(args[1]);
			File rulesFile = new File(args[2]);
			//remove empty and univary
			File file11 = new File(transFile + ".temp1");
			//remove rule A->B
			File file12 = new File(transFile + ".temp2");
			//remove rule A->B1B2...Bn
			File file13 = new File(transFile + ".binarytree");
			


			FileInputStream fis = new FileInputStream(modelFile);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");

			BufferedReader bf = null;
			bf = new BufferedReader(isr);

			String parse = null;
			TreeMap<String, Integer> treemap = new TreeMap<String, Integer>();
			HashMap<String, Double> rules = new HashMap<String, Double>();
			Set<String> nonTerminals = new HashSet<String>();
			CKYChartParser cky = new CKYChartParser();
			while ((parse = bf.readLine()) != null) {

				final PennTreeReader reader = new PennTreeReader(
						new StringReader(parse.trim()));
				final Tree<String> tree = reader.next();
				tree.removeEmptyNodes();
				tree.removeUnaryChains();
				//Tree.removeFunction(tree);
				Tree<String> subTree = tree.getChildren().get(0);
				if(!subTree.getLabel().equalsIgnoreCase("root")
						&& !tree.getLabel().equalsIgnoreCase("top"))
				{
					subTree = tree;
				}
				if (subTree.getTerminalYield().size() == 1) {
					//System.out.println(subTree);
					continue;
				}

				//List<Tree<String>> childlist = subTree.getChildren();
				//for (int index = 0; index < childlist.size(); index++) {
				//	RemoveUniChild(subTree, index);
				//}
				cky.convert1Tree(subTree);
				
				Tongji(subTree, treemap, rules, nonTerminals);


				// System.out.println(PennTreeRenderer.render(tree));
				// System.out.println(tree);

			}
			isr.close();
			fis.close();
			
			
			
			
			cky.setNonTerminals(nonTerminals);
			cky.setGrammars(rules);
			//TreeMap<String, Integer> transmap = new TreeMap<String, Integer>();
			int processCount = 0;
			//for(Tree<String> curTree : allReadTrees)
			PrintWriter output11 = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(file11), "UTF-8"), false);
			PrintWriter output12 = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(file12), "UTF-8"), false);
			PrintWriter output13 = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(file13), "UTF-8"), false);
			
			bf = new BufferedReader(new InputStreamReader(new FileInputStream(
					transFile), "UTF-8"));
			while ((parse = bf.readLine()) != null) {
				if(parse.trim().equals("(())"))
				{
					output11.println("(())");
					output12.println("(())");
					output13.println("(())");
					continue;
				}
				final PennTreeReader reader = new PennTreeReader(
						new StringReader(parse.trim()));
				final Tree<String> tree = reader.next();
				tree.removeEmptyNodes();
				tree.removeUnaryChains();
				Tree<String> subTree = tree.getChildren().get(0);
				if(!tree.getLabel().equalsIgnoreCase("root")
					&& !tree.getLabel().equalsIgnoreCase("top"))
				{
					subTree = tree;
				}
				if (subTree.getTerminalYield().size() == 1) {
					//System.out.println(subTree);
					continue;
				}
				output11.println(subTree);
				
				cky.convert1Tree(subTree);				
				output12.println(subTree);
				
				Tree<String> newTree = new Tree<String>("ROOT");
				List<Tree<String>> tmpConstructChilds = new ArrayList<Tree<String>>();
				tmpConstructChilds.add(subTree);
				newTree.setChildren(tmpConstructChilds);
				if(cky.Binarize(newTree, 0))
				{
					subTree = newTree.getChild(0);
					output13.println(subTree);
					output13.flush();
				}
				else
				{
					System.out.println(processCount);
					continue;
				}
								
				//}
				processCount++;
				if(processCount%500 == 0)
				{
					System.out.print(processCount);
					System.out.print(" ");
					if(processCount%5000 == 0)System.out.println();
					System.out.flush();
				}
			}
			output11.close();
			output12.close();
			output13.close();
			bf.close();
			
			

			// ����
			List<Map.Entry<String, Integer>> infoIds = new ArrayList<Map.Entry<String, Integer>>(
					treemap.entrySet());
			Collections.sort(infoIds,
					new Comparator<Map.Entry<String, Integer>>() {
						public int compare(Map.Entry<String, Integer> o1,
								Map.Entry<String, Integer> o2) {
							return (o2.getValue() - o1.getValue());
							// return
							// (o1.getKey()).toString().compareTo(o2.getKey());
						}
					});
			// �����
			PrintWriter output = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(rulesFile), "UTF-8"), false);
			for (int i = 0; i < infoIds.size(); i++) {
				String id = infoIds.get(i).toString();
				// System.out.println(id);
				output.write(id + "\r\n");
			}
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
