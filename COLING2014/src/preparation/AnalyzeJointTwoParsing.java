package preparation;


import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

public class AnalyzeJointTwoParsing {

	public static void main(String[] args) throws Exception {
		SDPCorpusReader sdpCorpusReader1 = new SDPCorpusReader(true);
		sdpCorpusReader1.Init(args[0]);
		SDPCorpusReader sdpCorpusReader2 = new SDPCorpusReader(true);
		sdpCorpusReader2.Init(args[1]);	
		

		PrintWriter writer  = new PrintWriter(System.out);
		if(args.length > 2)
		{
			writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[2]), "UTF-8"));
		}
		
		List<List<DepInstance>> goldinstances = new ArrayList<List<DepInstance>>();
		List<List<DepInstance>> predinstances = new ArrayList<List<DepInstance>>();
		
		for(int idx = 0; idx < 2; idx++)
		{
			goldinstances.add(new ArrayList<DepInstance>());
			predinstances.add(new ArrayList<DepInstance>());
		}
		
		int totalInstances = sdpCorpusReader1.m_vecInstances.size();
		if(totalInstances%2 != 0)
		{
			System.out.println("Some sentences lack some results...");
			return;
		}
		
		for(int idx = 0; idx < totalInstances/2; idx++)
		{
			DepInstance cutTmpInst1 = sdpCorpusReader1.m_vecInstances.get(2*idx);
			DepInstance cutTmpInst2 = sdpCorpusReader1.m_vecInstances.get(2*idx+1);
			int length = cutTmpInst1.size();
			assert(cutTmpInst2.size() == length);
			cutTmpInst1.bshared = new  ArrayList<Integer>();
			cutTmpInst2.bshared = new  ArrayList<Integer>();
			for(int idy = 0; idy < length; idy++)
			{
				if(cutTmpInst1.heads.get(idy) == cutTmpInst2.heads.get(idy))
				{
					cutTmpInst1.bshared.add(1);
					cutTmpInst2.bshared.add(1);
				}
				else
				{
					cutTmpInst1.bshared.add(0);
					cutTmpInst2.bshared.add(0);
				}
			}
			goldinstances.get(0).add(cutTmpInst1);
			goldinstances.get(1).add(cutTmpInst2);
			predinstances.get(0).add(sdpCorpusReader2.m_vecInstances.get(2*idx));
			predinstances.get(1).add(sdpCorpusReader2.m_vecInstances.get(2*idx+1));
		}
		//int iType = Integer.parseInt(args[2]);
		//if(iType == 0)
		//{
		//EvaluatePosTagger(sdpCorpusReader1.m_vecInstances,sdpCorpusReader2.m_vecInstances, writer);
		for(int idx = 0; idx < 2; idx++)
		{
			writer.println(String.format("Evaluate schema %d" , idx));
			evaluateDep(goldinstances.get(idx),predinstances.get(idx), writer);
		}
			

		writer.close();
	}
	
	
	
	public static void evaluateDep(List<DepInstance> vecInstances1, List<DepInstance> vecInstances2, PrintWriter output) throws Exception {

		int totalInstances = vecInstances1.size();
		if(totalInstances != vecInstances2.size()) 
		{
			output.println("Sentence Num do not match.");
			output.close();
			return;
		}
		Set<Integer> keyIds = new HashSet<Integer>();
		keyIds.add(0);
		
		Map<String, Integer> resultStat = new HashMap<String, Integer>();
		Map<String, Integer> keyValueStat = new HashMap<String, Integer>();
		for (int i = 0; i < totalInstances; i++) {
			DepInstance tmpInstance = vecInstances1.get(i);
			tmpInstance.analysisForColing14(resultStat, keyValueStat, vecInstances2.get(i));			
		}
		
		List<String> resultKeySet = new ArrayList<String>();
		for(String theKey : resultStat.keySet())
		{
			resultKeySet.add(theKey);
		}
		Collections.sort(resultKeySet);
		
		for(String theKey : resultKeySet)
		{
			//output.println(String.format("%s\t%d\t%d", theKey, resultStat.get(theKey), keyValueStat.get(analyzeKeyValue(theKey, keyIds))));
			if(theKey.endsWith("_prec\t1"))
			{
				int indexsuffix = theKey.lastIndexOf("_prec\t1");
				String theNewKey = theKey.substring(0, indexsuffix);
				String theOtherKey = theKey.substring(0, indexsuffix) + "_recall\t1";
				int correct1 = resultStat.get(theKey);
				int correct2 = resultStat.get(theOtherKey);
				if(correct1 != correct2)
				{
					System.out.println("error....");
					continue;
				}
				int predall = keyValueStat.get(analyzeKeyValue(theKey, keyIds));
				int goldall = keyValueStat.get(analyzeKeyValue(theOtherKey, keyIds));
				String curOut = String.format("%s\t%.4f\t%d", theNewKey, (2.0 * correct1)/(predall + goldall), (predall + goldall+1)/2);
				output.println(curOut);
			}
			else if(theKey.endsWith("_recall\t1"))
			{
				continue;
			}
			else if(theKey.endsWith("\t1"))
			{
				int indexsuffix = theKey.lastIndexOf("\t1");
				String theNewKey = theKey.substring(0, indexsuffix);
				int correct = resultStat.get(theKey);
				int total = keyValueStat.get(analyzeKeyValue(theKey, keyIds));
				output.println(String.format("%s\t%.4f\t%d", theNewKey, (correct*1.0)/total, total));
			}
			else if(theKey.endsWith("\t0"))
			{
				continue;
			}
			else
			{
				System.out.println("error...");
			}
		}
				
		
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
