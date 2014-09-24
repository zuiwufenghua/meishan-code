package process;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class ExtractSentenceFromCons {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		BufferedReader cfgreader = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0])));
		
		PennTreeReader reader = new PennTreeReader(cfgreader);
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"));
		while (reader.hasNext()) {
			Tree<String> tree = reader.next();
			List<String> forms = tree.getYield();
			String sentence = "";
			for (String theWord : forms) {
				sentence = sentence + " " + theWord;
			}
			sentence = sentence.trim();
			writer.println(sentence);
		}
		writer.close();
	}


}
