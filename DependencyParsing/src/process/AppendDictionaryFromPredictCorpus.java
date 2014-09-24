package process;


import java.util.*;
import java.io.*;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

public class AppendDictionaryFromPredictCorpus {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[2]), "UTF-8"));	
		Map<String,String> primeWords = new HashMap<String,String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
				args[0]), "UTF-8"));
		
		String sLine = null;
		
		while((sLine = reader.readLine()) != null) {
			String[] curUnits = sLine.trim().split("\\s+");
			if(curUnits.length > 2 && curUnits[0].equals("word"))
			{
				String posvalue = curUnits[2];
				for(int k = 3; k < curUnits.length; k++)
				{
					posvalue = posvalue + " " + curUnits[k];
				}
				posvalue = posvalue.trim();
				primeWords.put(curUnits[1], posvalue);
				writer.println(sLine.trim());
			}
		}
		reader.close();
		
		Map<String, Map<String, Integer>> lexicon = new HashMap<String, Map<String, Integer>>();		
		File file = new File(args[1]);
		String[] subFilenames = file.list();
		int totalInstance = 0;
		for (String subFilename : subFilenames) {			
			String inputFile = args[1] + File.separator + subFilename;
			System.out.println("Processing File " + inputFile + " ......");
			SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();
			sdpCorpusReader.InitNextInstance(inputFile);
			List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
			DepInstance tmpInstance = 	sdpCorpusReader.m_nextInstance;		
			//int totalInstances = vecInstances.size();
			//System.out.println(String.format("Total %d instances", totalInstances));
			int i  = 0;
			while(tmpInstance != null) {
				String[] postags = new String[tmpInstance.postags.size()];
				tmpInstance.postags.toArray(postags);
				String[] forms = new String[tmpInstance.forms.size()];
				tmpInstance.forms.toArray(forms);
				for(int j = 0; j < forms.length; j++)
				{
					if(primeWords.containsKey(forms[j]))continue;
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
				totalInstance++;
				i++;
				sdpCorpusReader.NextInstance();
				tmpInstance = sdpCorpusReader.m_nextInstance;
				if((totalInstance)%1000 == 0)
				{
					System.out.println(totalInstance);
					System.out.flush();
				}
			}	
			System.out.println(String.format("Total %d instances", i));
			System.out.flush();
		}
		
		System.out.println(totalInstance);
		System.out.flush();
		int threshold = Integer.parseInt(args[3]);
				
		for(String curWord : lexicon.keySet())
		{
			if(primeWords.containsKey(curWord))continue;
			String primePos = "";
			if(primeWords.containsKey(curWord.toLowerCase()))
			{
				primePos = primeWords.get(curWord.toLowerCase());
			}
			else if(primeWords.containsKey(curWord.toUpperCase()))
			{
				primePos = primeWords.get(curWord.toUpperCase());
			}
			else
			{
				primePos = "";
			}
			String outLine = "word " + curWord;
			int maxLabelCount = 0;
			String maxLabel = "";
			for (String curLabel : lexicon.get(curWord).keySet()) {
				if(lexicon.get(curWord).get(curLabel) >= maxLabelCount)
				{
					maxLabelCount = lexicon.get(curWord).get(curLabel);
					maxLabel = curLabel;
				}
			}
			if(primePos.equals("") && maxLabelCount>=threshold)
			{
				writer.println(outLine + " " + maxLabel);
			}
			else if(!primePos.equals("") && maxLabelCount>=threshold)
			{
				writer.println(outLine + " " + primePos);
			}
			else if(!primePos.equals("") && maxLabelCount<threshold)
			{
				writer.println(outLine + " " + primePos);
			}
			
		}
		
		
		
		
		
		//List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		System.out.println("Finished");	
		writer.close();
		
		
	}

}
