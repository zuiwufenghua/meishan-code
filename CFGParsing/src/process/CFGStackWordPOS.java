package process;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;
import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;


public class CFGStackWordPOS {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Tree<String>> senCFGMap = new HashMap<String, Tree<String>>();
		BufferedReader cfgreader = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8"));

		String sLine = null;
		while ((sLine = cfgreader.readLine()) != null) {
			PennTreeReader reader = new PennTreeReader(new StringReader(
					sLine.trim()));

			while (reader.hasNext()) {
				Tree<String> tree = reader.next();
				List<String> forms = tree.getYield();
				String sentence = "";
				for (String theWord : forms) {
					sentence = sentence + theWord;
				}
				sentence = sentence.trim();
				senCFGMap.put(sentence, tree);
			}
		}
		cfgreader.close();

		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();
		sdpCorpusReader.Init(args[1]);

		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		int totalInstances = vecInstances.size();
		
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"));
		for (int senId = 0; senId < totalInstances; senId++) {
			if ((senId + 1) % 500 == 0) {
				System.out.println(String.format(
						"process instance %d", senId + 1));
			}
			DepInstance tmpInstance = vecInstances.get(senId);
			String[] forms = new String[tmpInstance.forms.size()];
			tmpInstance.forms.toArray(forms);

			String sentence = "";
			for (String theWord : forms) {
				sentence = sentence + theWord;
			}
			sentence = sentence.trim();
			Tree<String> tree = senCFGMap.get(sentence);
			if(tree == null)continue;			
			List<String> feats = tmpInstance.feats1;
			
			feats.set(0, tree.toString().replace("\t", " "));
					
			try {
				List<String> outputSentence = new ArrayList<String>();
				tmpInstance.toGoldListString(outputSentence, feats);

				for (String oneLine : outputSentence) {
					writer.println(oneLine);
				}
				writer.println();
			} catch (Exception ex) {

				continue;
			}
			
			
		}
		
		writer.close();

	}

}
