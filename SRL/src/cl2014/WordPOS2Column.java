package cl2014;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class WordPOS2Column {
	public static void main(String[] args) throws Exception {
		BufferedReader in = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(args[0]), "UTF-8"));
		String sLine = null;
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[args.length - 1]), "UTF-8"));

		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().length() < 2) continue;
			String[] wordposs = sLine.split("\\s+");
			String newLine = "";
			for(String curWordPOS : wordposs)
			{
				int splitIndex = curWordPOS.lastIndexOf("_");
				String theWord = curWordPOS.substring(0, splitIndex);
				String thePOS = curWordPOS.substring(splitIndex+1);
				output.println(theWord + "\t" + thePOS);
			}
			output.println();
		}
		in.close();
		output.close();
	}

}
