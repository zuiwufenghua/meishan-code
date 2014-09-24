package process;


import java.util.*;
import java.io.*;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

public class ExtractDictionaryFromGoldCorpus {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Map<String, Map<String, Integer>> lexicon = new HashMap<String, Map<String, Integer>>();
		
		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();
		sdpCorpusReader.Init(args[0]);
		
		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		int totalInstances = vecInstances.size();
		
		for (int i = 0; i < totalInstances; i++) {
			DepInstance tmpInstance = vecInstances.get(i);

			String[] postags = new String[tmpInstance.postags.size()];
			tmpInstance.postags.toArray(postags);
			String[] forms = new String[tmpInstance.forms.size()];
			tmpInstance.forms.toArray(forms);
			for(int j = 0; j < forms.length; j++)
			{
				if(lexicon.containsKey(forms[j]))
				{
					if(lexicon.get(forms[j]).containsKey(postags[j]))
					{
						lexicon.get(forms[j]).put(postags[j], lexicon.get(forms[j]).get(postags[j])+1);
					}
					else
					{
						lexicon.get(forms[j]).put(postags[j], 1);
					}
				}
				else
				{
					lexicon.put(forms[j], new HashMap<String, Integer>());
					lexicon.get(forms[j]).put(postags[j], 1);
				}
			}
		}
		
		int threshold = Integer.parseInt(args[2]);
		
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[1]), "UTF-8"));		
		for(String curWord : lexicon.keySet())
		{
			String outLine = "word " + curWord;
			int labelsCount = 0;
			for (String curLabel : lexicon.get(curWord).keySet()) {
				if(lexicon.get(curWord).get(curLabel) >= threshold)
				{
					outLine = outLine + " " + curLabel;
					labelsCount++;
				}
			}
			if(labelsCount==0)continue;
			writer.println(outLine);
		}
		writer.close();
		
		
	}

}
