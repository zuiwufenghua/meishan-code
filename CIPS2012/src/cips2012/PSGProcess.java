package cips2012;

import java.io.*;
import java.util.List;

import edu.berkeley.*;
import edu.berkeley.nlp.*;
import edu.berkeley.nlp.syntax.*;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;
import edu.berkeley.nlp.syntax.Trees.PennTreeRenderer;
import edu.berkeley.nlp.util.*;

public class PSGProcess {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length < 3) {
			System.out.println("parameters error.");
		}
		if (args[2].trim().equals("plain0")) {
			File file = new File(args[0]);
			File file2 = new File(args[1]);

			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");

			BufferedReader bf = null;
			bf = new BufferedReader(isr);

			PrintWriter output = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(file2), "UTF-8"), false);

			String parse = null;

			while ((parse = bf.readLine()) != null) {
				if(parse.trim().equals(""))continue;
				String[] splitUnits = parse.trim().split("\\s+");

				int seq = 0;
				try {
					seq = Integer.parseInt(splitUnits[0]);
				} catch (Exception e) {
					System.out.println("error sentence :" + parse);
					continue;
				}
				if (splitUnits.length < 2) {
					System.out.println("error sentence :" + parse);
					continue;
				}
				String sentence = splitUnits[1];
				for (int i = 2; i < splitUnits.length; i++) {
					sentence = sentence + " " + splitUnits[i];
				}

				output.println(sentence.trim());
			}
			output.close();
			bf.close();

		} else {
			try {
				File file = new File(args[0]);
				File file2 = new File(args[1]);

				FileInputStream fis = new FileInputStream(file);
				InputStreamReader isr = new InputStreamReader(fis, "UTF-8");

				BufferedReader bf = null;
				bf = new BufferedReader(isr);

				PrintWriter output = new PrintWriter(new OutputStreamWriter(
						new FileOutputStream(file2), "UTF-8"), false);

				String parse = null;
				while ((parse = bf.readLine()) != null) {

					int startIndex = parse.indexOf(" ");
					parse = parse.substring(startIndex);
					parse = parse.trim();

					// System.out.println(parse);

					final PennTreeReader reader = new PennTreeReader(
							new StringReader(parse));
					final Tree<String> tree = reader.next();

					// System.out.println(PennTreeRenderer.render(tree));
					// System.out.println(tree);

					String list = tree.toString();
					// output.write(list+"\r\n");
					output.println(list.trim());

					// System.out.println(list);
				}

				output.close();
				bf.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
