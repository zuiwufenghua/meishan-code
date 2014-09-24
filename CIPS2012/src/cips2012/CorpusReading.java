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

public class CorpusReading {

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
		String parse = null;
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(String.format("%s.%s", args[0], "tmp")), "UTF-8"));
		while ((parse = bf.readLine()) != null) {
			final PennTreeReader reader = new PennTreeReader(
					new StringReader(parse.trim()));
			final Tree<String> tree = reader.next();
			out.println(tree.toString());
		}
		
		bf.close();
		out.close();
	}

}
