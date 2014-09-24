package cips2012;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;


public class CIPSEvaluateToolFormat {

	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception{
		// TODO Auto-generated method stub

		File file = new File(args[0]);
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");

		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1])), false);
		//PrintWriter writer_nor = new PrintWriter(new OutputStreamWriter(
		//		new FileOutputStream(args[0] + ".normalize"), "UTF-8"), false);
		BufferedReader bf = null;
		bf = new BufferedReader(isr);
		String sLine = null;
		int count = 1;
		while ((sLine = bf.readLine()) != null) {
			if(sLine.trim().length() < 10)
			{
				//writer_nor.println(sLine.trim());
				writer.println(String.format("%d  (())", count));
				count++;
				continue;
			}
			PennTreeReader reader = new PennTreeReader(new StringReader(
					sLine.trim()));
			Tree<String> tree = reader.next();
			while (tree.getLabel().equalsIgnoreCase("root")
					|| tree.getLabel().equalsIgnoreCase("top")) {
				tree = tree.getChild(0);
			}
			writer.println(String.format("%d  %s", count, tree.toString()));
			count++;
		}
		
		bf.close();
		writer.close();
	}

}
