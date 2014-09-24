package acl2014;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.math3.stat.inference.*;

public class McTestDataGen {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub

		CharDependencyReader cdpCorpusReader1 = new CharDependencyReader();
		cdpCorpusReader1.Init(args[0]);
		CharDependencyReader cdpCorpusReader2 = new CharDependencyReader();
		cdpCorpusReader2.Init(args[1]);
		CharDependencyReader cdpCorpusReader3 = new CharDependencyReader();
		cdpCorpusReader3.Init(args[2]);
		
		PrintWriter writer  = new PrintWriter(System.out);
		if(args.length > 3)
		{
			writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[3]), "UTF-8"));
		}
		
		analysisDep(cdpCorpusReader1.m_vecInstances, cdpCorpusReader2.m_vecInstances, cdpCorpusReader3.m_vecInstances, writer);

		writer.close();
	}
	
	
	public static void analysisDep(List<CharDependency> vecInstances1, List<CharDependency> vecInstances2, List<CharDependency> vecInstances3, PrintWriter output) throws Exception {

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
			CharDependency tmpInstance = vecInstances1.get(i);
			tmpInstance.init();
			CharDependency other1 = vecInstances2.get(i);
			other1.init();
			CharDependency other2 = vecInstances3.get(i);
			other2.init();

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
