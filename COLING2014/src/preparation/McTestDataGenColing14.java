package preparation;


import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

import org.apache.commons.math3.stat.inference.*;

public class McTestDataGenColing14 {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub

		SDPCorpusReader sdpCorpusReader1 = new SDPCorpusReader(true);
		sdpCorpusReader1.Init(args[0]);
		SDPCorpusReader sdpCorpusReader2 = new SDPCorpusReader(true);
		sdpCorpusReader2.Init(args[1]);	
		SDPCorpusReader sdpCorpusReader3 = new SDPCorpusReader(true);
		sdpCorpusReader3.Init(args[2]);
		
		int multi = Integer.parseInt(args[3]);
		
		PrintWriter writer  = new PrintWriter(System.out);
		if(args.length > 4)
		{
			writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[4]), "UTF-8"));
		}
		
		List<List<DepInstance>> goldinstances = new ArrayList<List<DepInstance>>();
		List<List<DepInstance>> predinstances1 = new ArrayList<List<DepInstance>>();
		List<List<DepInstance>> predinstances2 = new ArrayList<List<DepInstance>>();
		
		for(int idx = 0; idx < multi; idx++)
		{
			goldinstances.add(new ArrayList<DepInstance>());
			predinstances1.add(new ArrayList<DepInstance>());
			predinstances2.add(new ArrayList<DepInstance>());
		}
		
		int totalInstances = sdpCorpusReader1.m_vecInstances.size();
		if(totalInstances%multi != 0)
		{
			System.out.println("Some sentences lack some results...");
			return;
		}
		
		for(int idx = 0; idx < totalInstances; idx++)
		{
			goldinstances.get(idx%multi).add(sdpCorpusReader1.m_vecInstances.get(idx));
			predinstances1.get(idx%multi).add(sdpCorpusReader2.m_vecInstances.get(idx));
			predinstances2.get(idx%multi).add(sdpCorpusReader3.m_vecInstances.get(idx));
		}
		//int iType = Integer.parseInt(args[2]);
		//if(iType == 0)
		//{
		//EvaluatePosTagger(sdpCorpusReader1.m_vecInstances,sdpCorpusReader2.m_vecInstances, writer);
		for(int idx = 0; idx < multi; idx++)
		{
			writer.println(String.format("Evaluate schema %d" , idx));
			analysisDep(goldinstances.get(idx),predinstances1.get(idx),predinstances2.get(idx), writer);
		}
		
		//analysisDep(cdpCorpusReader1.m_vecInstances, cdpCorpusReader2.m_vecInstances, cdpCorpusReader3.m_vecInstances, writer);

		writer.close();
	}
	
	
	public static void analysisDep(List<DepInstance> vecInstances1, List<DepInstance> vecInstances2, List<DepInstance> vecInstances3, PrintWriter output) throws Exception {

		int totalInstances = vecInstances1.size();
		if(totalInstances != vecInstances2.size() || totalInstances != vecInstances3.size()) 
		{
			output.println("Sentence Num do not match.");
			output.close();
			return;
		}
		
		Map<String, List<Double>> analysisResult = new TreeMap<String, List<Double>>(
				new Comparator<String>(){  
					public int compare(String o1, String o2) {                
						return o1.compareTo(o2);  
					}     
        });
		
		int i = 0;
		for (; i < totalInstances; i++) {
			DepInstance tmpInstance = vecInstances1.get(i);
			DepInstance other1 = vecInstances2.get(i);
			DepInstance other2 = vecInstances3.get(i);

			Map<String, List<Double>> analysisResultpart = new TreeMap<String, List<Double>>(
					new Comparator<String>(){  
						public int compare(String o1, String o2) {                
							return o1.compareTo(o2);  
						}     
	        });
			
			if(!tmpInstance.TTestDataBySent(other1, other2, analysisResultpart))
			{
				output.println(String.format("Sentence %d is not matched.", i+1));
				output.close();
				return;
			}
			
			for(String theTmpKey : analysisResultpart.keySet())
			{
				if(!analysisResult.containsKey(theTmpKey))
				{
					analysisResult.put(theTmpKey, new ArrayList<Double>());	
				}
				for(Double oneRest : analysisResultpart.get(theTmpKey))
				{
					analysisResult.get(theTmpKey).add(oneRest);
				}

			}
			
		}
		
		
		TTest ttest = new TTest();
		for(String theTmpKey : analysisResult.keySet())
		{

			//compute average
			if(theTmpKey.endsWith("=2F"))continue;
			
			
			double[] results1 = new double[analysisResult.get(theTmpKey).size()];
			//double average = 0.0;
			int ids = 0;
			for(Double oneRest : analysisResult.get(theTmpKey))
			{
				//average =average + oneRest;
				results1[ids] = oneRest;
				ids++;
			}
			
			
			
			//average = average / analysisResult.get(theTmpKey).size();
			
			//double biaozhuncha = 0.0;
			
			//for(Double oneRest : analysisResult.get(theTmpKey))
			//{
			//	biaozhuncha =biaozhuncha + (oneRest - average) * (oneRest - average);
			//}
			//biaozhuncha = Math.sqrt(biaozhuncha / (analysisResult.get(theTmpKey).size() - 1));
			
			
			String theTmpKeyOther = theTmpKey.replace("=1F", "=2F");
			double[] results2 = new double[analysisResult.get(theTmpKeyOther).size()];
			//double average = 0.0;
			ids = 0;
			for(Double oneRest : analysisResult.get(theTmpKeyOther))
			{
				//average =average + oneRest;
				results2[ids] = oneRest;
				ids++;
			}
			
			output.println(theTmpKey + "\t" +  "p-value =" + ttest.tTest(results1, results2));
														
			//output.println(theTmpKey + "\t" + "AVG=" + average  + "\t" + "STD=" + biaozhuncha + "\t" + "SNT=" + analysisResult.get(theTmpKey).size());
		}
		
		
		
		
	}

	


}
