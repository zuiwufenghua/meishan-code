package acl2014;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class InnerDependencyAnalysis {

	/**
	 * @param args
	 */
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
	
		int startarg = 0;
		Set<String> worddict = new HashSet<String>();
		if(args[0].equals("-dict"))
		{
			startarg = 2;
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(args[1]), "UTF8"));
			String sLine = null;
			while ((sLine = in.readLine()) != null) {
				if(sLine.trim().equals("") || sLine.trim().length() < 2)continue;
				String[] wordposs = sLine.trim().split("\\s+");
				worddict.add(wordposs[0]);
			}
			in.close();
		}
		
		CharDependencyReader cdpCorpusReader1 = new CharDependencyReader();
		cdpCorpusReader1.Init(args[startarg]);
		CharDependencyReader cdpCorpusReader2 = new CharDependencyReader();
		cdpCorpusReader2.Init(args[startarg+1]);
		
		PrintWriter writer  = new PrintWriter(System.out);
		if(args.length > startarg+2)
		{
			writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[startarg+2]), "UTF-8"));
		}
		
		analysisDep(cdpCorpusReader1.m_vecInstances, cdpCorpusReader2.m_vecInstances, worddict, writer);

		writer.close();
	}
	
	
	
	public static void analysisDep(List<CharDependency> vecInstances1, List<CharDependency> vecInstances2, Set<String> worddict, PrintWriter output) throws Exception {

		int totalInstances = vecInstances1.size();
		if(totalInstances != vecInstances2.size()) 
		{
			output.println("Sentence Num do not match.");
			output.close();
			return;
		}
		
		Map<String, Map<String, Integer>> analysisResult = new TreeMap<String, Map<String, Integer>>(
				new Comparator<String>(){  
					public int compare(String o1, String o2) {                
						return o1.compareTo(o2);  
					}     
        });
		
		int i = 0;
		for (; i < totalInstances; i++) {
			CharDependency tmpInstance = vecInstances1.get(i);
			tmpInstance.init();
			CharDependency other = vecInstances2.get(i);
			other.init();

			Map<String, Map<String, Integer>> analysisResultpart = new TreeMap<String, Map<String, Integer>>(
					new Comparator<String>(){  
						public int compare(String o1, String o2) {                
							return o1.compareTo(o2);  
						}     
	        });
			
			if(!tmpInstance.innerWordDependencyAnalysis(other, worddict, analysisResultpart, i))
			{
				output.println(String.format("Sentence %d is not matched.", i+1));
				output.close();
				return;
			}
			
			for(String theTmpKey : analysisResultpart.keySet())
			{
				if(!analysisResult.containsKey(theTmpKey))
				{
					analysisResult.put(theTmpKey, new HashMap<String, Integer>());
					analysisResult.get(theTmpKey).put("GOLD", 0);
					analysisResult.get(theTmpKey).put("PRED", 0);
					analysisResult.get(theTmpKey).put("CORRECT", 0);					
				}
				analysisResult.get(theTmpKey).put("GOLD", analysisResult.get(theTmpKey).get("GOLD") + analysisResultpart.get(theTmpKey).get("GOLD"));
				analysisResult.get(theTmpKey).put("PRED", analysisResult.get(theTmpKey).get("PRED") + analysisResultpart.get(theTmpKey).get("PRED"));
				analysisResult.get(theTmpKey).put("CORRECT", analysisResult.get(theTmpKey).get("CORRECT") + analysisResultpart.get(theTmpKey).get("CORRECT"));				
			}
			
		}
		
		
		for(String theTmpKey : analysisResult.keySet())
		{
			displayPRF(theTmpKey, analysisResult.get(theTmpKey).get("CORRECT"), analysisResult.get(theTmpKey).get("PRED")
					, analysisResult.get(theTmpKey).get("GOLD"), output);
			if(theTmpKey.startsWith("WS="))
			{
				int correctws = analysisResult.get(theTmpKey).get("CORRECT");
				int correcttag = analysisResult.get(theTmpKey.replace("WS=", "TAG=")).get("CORRECT");
				double recallws = (correcttag > 0 ? correctws / (double) correcttag : 1.0);
				String lasteval = "\tT:" +correctws + "/" + correcttag + "=" +((int) (recallws * 10000)) / 100.0;
				output.print(lasteval);
			}
			output.println();
		}
		
	}

	
	public static double displayPRF(String prefixStr, int correct, int guessed,
			int gold,  PrintWriter pw) {
		double precision = (guessed > 0 ? correct / (double) guessed : 1.0);
		double recall = (gold > 0 ? correct / (double) gold : 1.0);
		double f1 = (precision > 0.0 && recall > 0.0 ? 2.0 / (1.0 / precision + 1.0 / recall)
				: 0.0);


		String displayStr = " P: " + correct + "/" + guessed + "=" +((int) (precision * 10000)) / 100.0
				+ " R: " + correct + "/" + gold + "="  + ((int) (recall * 10000)) / 100.0 + " F1: "
				+ ((int) (f1 * 10000)) / 100.0 ;

		if (pw != null)
			pw.print(prefixStr + displayStr);
		
		return f1;
	}
}
