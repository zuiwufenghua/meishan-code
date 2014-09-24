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

public class Binarize2Orginary {

	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception{
		// TODO Auto-generated method stub
		File transFile = new File(args[0]);
		// remove non-terminals end with "*"  CKY binarize back A->B1B2...Bn
		File file14 = new File(transFile + ".temp");
		// back A->B rules
		File file15 = new File(transFile + ".orginal");
		
		PrintWriter output14 = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(file14), "UTF-8"), false);
		PrintWriter output15 = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(file15), "UTF-8"), false);
		
		CKYChartParser cky = new CKYChartParser();
		
		int processCount = 0;
		String parse = null;
		BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(
				transFile), "UTF-8"));
		while ((parse = bf.readLine()) != null) {
		
			if(parse.trim().replaceAll("\\s+", "").equals("(())"))
			{
				output14.println(parse.trim());
				output15.println(parse.trim());
				continue;
			}

			final PennTreeReader reader = new PennTreeReader(
					new StringReader(parse.trim()));
			final Tree<String> tree = reader.next();
			tree.removeEmptyNodes();
			tree.removeUnaryChains();
			Tree<String> subTree = tree;
			while(subTree.getLabel().equalsIgnoreCase("root")
					|| subTree.getLabel().equalsIgnoreCase("top")
					)
			{
				subTree = subTree.getChildren().get(0);
			}
			//if(!subTree.getLabel().equalsIgnoreCase("root")
			//	&& !subTree.getLabel().equalsIgnoreCase("top"))
			//{
			//	subTree = tree;
			//}
			if (subTree.getTerminalYield().size() == 1) {
				//System.out.println(subTree);
				continue;
			}
			
			cky.restore2Tree(subTree);
			output14.println("(TOP " + subTree.toString() + ")");
			output14.flush();
			
			cky.restoreTree(subTree);
			output15.println("(TOP " + subTree.toString() + ")");
			output15.flush();
			
			processCount++;
			if(processCount%500 == 0)
			{
				System.out.print(processCount);
				System.out.print(" ");
				if(processCount%5000 == 0)System.out.println();
				System.out.flush();
			}
		}
		
		output14.close();
		output15.close();
		bf.close();
				

	}

}
