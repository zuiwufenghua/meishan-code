package process;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

public class JointDPAnalyze {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Map<String, Integer> dict = new HashMap<String, Integer>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
				args[0]), "UTF-8"));
		
		String sLine = null;
		
		while((sLine = reader.readLine()) != null) {
			String[] curUnits = sLine.trim().split("\\s+");
			if(curUnits.length > 1)
			{
				String curWord = curUnits[0];
				int curWordFreq = 0;
				for(int idx = 1; idx < curUnits.length; idx++)
				{
					String[] fineUnits = curUnits[idx].split(":");
					try{
						curWordFreq += Integer.parseInt(fineUnits[1]);
					}
					catch (Exception x)
					{
						continue;
					}
				}
				if(curWordFreq > 0)
				{
					dict.put(curWord, curWordFreq);
				}
			}		
		}
		reader.close();

		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();
		sdpCorpusReader.Init(args[1]);

		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		int totalInstances = vecInstances.size();
		
		Set<Integer> keyIds = new HashSet<Integer>();
		keyIds.add(0);

		
		Map<String, Integer> resultStat = new HashMap<String, Integer>();
		Map<String, Integer> keyValueStat = new HashMap<String, Integer>();
		for (int i = 0; i < totalInstances; i++) {
			DepInstance tmpInstance = vecInstances.get(i);
			tmpInstance.analysisSTAll(resultStat, keyValueStat, dict);			
		}
		
		List<String> resultKeySet = new ArrayList<String>();
		for(String theKey : resultStat.keySet())
		{
			resultKeySet.add(theKey);
		}
		Collections.sort(resultKeySet);
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"));
		
		for(String theKey : resultKeySet)
		{
			output.println(String.format("%s\t%d\t%d", theKey, resultStat.get(theKey), keyValueStat.get(analyzeKeyValue(theKey, keyIds))));
		}
				
		output.close();
	}
	
	public static String analyzeKeyValue(String theKey, Set<Integer> keyIds)
	{
		String keyVaule = "";
		String[] units = theKey.split("\\s+");
		
		for(Integer i = 0; i < units.length; i++)
		{
			if(keyIds.contains(i))
			{
				keyVaule = keyVaule + "\t" + units[i];
			}
		}
		
		
		return keyVaule.trim();
	}

}
