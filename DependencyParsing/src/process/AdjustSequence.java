package process;


import java.io.*;
import java.util.*;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

public class AdjustSequence {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Map<String, DepInstance> sentDep = new HashMap<String, DepInstance>();
		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();
		sdpCorpusReader.Init(args[1]);
		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		int totalInstances = vecInstances.size();
		for (int senId = 0; senId < totalInstances; senId++) {
			if ((senId + 1) % 500 == 0) {
				System.out.println(String.format(
						"process instance %d", senId + 1
						));
			}
			
			DepInstance tmpInstance = vecInstances.get(senId);

			Integer[] predheads = new Integer[tmpInstance.heads.size()];
			tmpInstance.heads.toArray(predheads);
			String[] predlabels = new String[tmpInstance.deprels.size()];
			tmpInstance.deprels.toArray(predlabels);
			String[] predForms = new String[tmpInstance.forms.size()];
			tmpInstance.forms.toArray(predForms);

			String sentence = "";
			for (String theWord : predForms) {
				sentence = sentence + " " + theWord;
			}
			sentence = sentence.trim();
			sentDep.put(sentence, tmpInstance);
		}
			
		
		
		BufferedReader sentencereader = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8"));
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
				args[2]), "UTF-8"));

		String sLine = null;
		while ((sLine = sentencereader.readLine()) != null) {
			if(sentDep.containsKey(sLine.trim()))
			{
				DepInstance tmpInstance = sentDep.get(sLine.trim());
				for(int i = 0; i < tmpInstance.forms.size(); i++)
				{
					String theForm = tmpInstance.forms.get(i);
					String thePos = tmpInstance.postags.get(i);
					int theHead = tmpInstance.heads.get(i);
					String theLabel = tmpInstance.deprels.get(i);
					writer.println(String.format("%d\t%s\t_\t%s\t%s\t_\t%d\t%s\t_\t_", i+1, theForm, thePos, thePos, theHead, theLabel));
				}
				writer.println();
				writer.flush();
			}
			else
			{
				System.out.println(sLine.trim());
			}
		}
		sentencereader.close();
		writer.close();		
	}

}
