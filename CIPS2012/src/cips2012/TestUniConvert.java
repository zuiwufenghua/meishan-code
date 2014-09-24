package cips2012;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;


public class TestUniConvert {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		File file = new File(args[0]);
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");

		BufferedReader bf = null;
		bf = new BufferedReader(isr);
		String sLine = null;
		PrintWriter writer0 = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1] + ".removefunc"), "UTF-8"));
		PrintWriter writer1 = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1] + ".convert"), "UTF-8"));
		PrintWriter writer2 = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1] + ".convertback"), "UTF-8"));
		PrintWriter writer3 = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1] + ".skeleton"), "UTF-8"));
		CKYChartParser cky = new CKYChartParser();
		int k = 1;
		while ((sLine = bf.readLine()) != null) {

			final PennTreeReader reader = new PennTreeReader(
					new StringReader(sLine.trim()));
			final Tree<String> tree = reader.next();			
			if(tree != null)
			{
				Tree<String> subTree = tree;
				removeFunc(subTree);
				writer0.println(subTree.toString());
				k++;
				List<Tree<String>> childlist = subTree.getChildren();
				for (int index = 0; index < childlist.size(); index++) {
					cky.convertTree(subTree, index);
				}
				writer1.println(subTree.toString());
				
				cky.restoreTree(subTree);
				
				writer2.println(subTree.toString());
			}
		}
		writer0.close();
		writer1.close();
		writer2.close();
		writer3.close();
		bf.close();
	}
	
	
	public static void removeFunc(Tree<String> root)
	{
		if(root.isLeaf()) return;
		
		for(Tree<String> curChild : root.getChildren())
		{
			removeFunc(curChild);
		}
		
		String[] miniLabels = root.getLabel().split("-");
		root.setLabel(miniLabels[0]);
	}
	

}
