package process;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;


public class SemSynDiffAnalyze {

	public static void main(String[] args) throws Exception {
		SDPCorpusReader sdpCorpusReader1 = new SDPCorpusReader();
		sdpCorpusReader1.Init(args[0]);
		SDPCorpusReader sdpCorpusReader2 = new SDPCorpusReader();
		sdpCorpusReader2.Init(args[1]);	
		
		String outputFileDep = args[2];
		
		evaluateDep(sdpCorpusReader1.m_vecInstances,sdpCorpusReader2.m_vecInstances, outputFileDep + ".pospair");
		evaluateLabel(sdpCorpusReader1.m_vecInstances,sdpCorpusReader2.m_vecInstances, outputFileDep + ".label");
		
	}
		
	public static void evaluateDep(List<DepInstance> vecInstances1, List<DepInstance> vecInstances2, String outputFile) throws Exception {

		int totalInstances = vecInstances1.size();
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8"));
		if(totalInstances != vecInstances2.size()) 
		{
			output.println("Sentence Num do not match.");
			output.close();
			return;
		}
		
		Map<String, Integer> errStats = new HashMap<String, Integer>();
		
		Map<String, Integer> modTotal = new HashMap<String, Integer>();
		modTotal.put("all", 0);

		Map<String, String> errorKey = new HashMap<String, String>();
		errorKey.put("true", "all");
		errorKey.put("false", "all");

		
		int i = 0;
		for (; i < totalInstances; i++) {
			DepInstance tmpInstance = vecInstances1.get(i);
			DepInstance other = vecInstances2.get(i);
			
			for(int idx = 0; idx < tmpInstance.forms.size(); idx++)
			{
				String goldPosCur = tmpInstance.postags.get(idx);
				String goldPOSHeadSyn = "ROOT";
				if(goldPosCur.equals("PU"))continue;
				String matchMark = "true";
				int goldHead1 = tmpInstance.heads.get(idx);
				int goldHead2 = other.heads.get(idx);
				if(goldHead2-1 < tmpInstance.forms.size() && goldHead2-1 >= 0 )
				{
					goldPOSHeadSyn = tmpInstance.postags.get(goldHead2-1);
				}
				String goldPos = goldPosCur + "\t" + goldPOSHeadSyn;
				if(goldHead1 != goldHead2) matchMark = "false";
				if(!errStats.containsKey(matchMark))
				{
					errStats.put(matchMark, 0);
				}
				
				if(!errStats.containsKey(goldPos + "\t" + matchMark))
				{
					errStats.put(goldPos + "\t" + matchMark, 0);
				}
				
				errStats.put(goldPos + "\t" + matchMark, errStats.get(goldPos + "\t" + matchMark) + 1);
				errStats.put(matchMark, errStats.get(matchMark) + 1);
				
				modTotal.put("all", modTotal.get("all") + 1);
				
				if(!modTotal.containsKey(goldPos))
				{
					modTotal.put(goldPos, 0);
				}
				
				modTotal.put(goldPos, modTotal.get(goldPos)+1);
				errorKey.put(goldPos + "\t" + matchMark, goldPos);
			}

		}
		
		for(String theKey : errStats.keySet())
		{
			String modKey = errorKey.get(theKey);
			output.println(theKey + "\t" + errStats.get(theKey) + "\t" + modTotal.get(modKey));
		}

		
		output.close();

	}


	public static void evaluateLabel(List<DepInstance> vecInstances1, List<DepInstance> vecInstances2, String outputFile) throws Exception {

		int totalInstances = vecInstances1.size();
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8"));
		if(totalInstances != vecInstances2.size()) 
		{
			output.println("Sentence Num do not match.");
			output.close();
			return;
		}
		
		Map<String, Integer> errStats = new HashMap<String, Integer>();
		
		Map<String, Integer> modTotal = new HashMap<String, Integer>();

		Map<String, String> errorKey = new HashMap<String, String>();


		
		int i = 0;
		for (; i < totalInstances; i++) {
			DepInstance tmpInstance = vecInstances1.get(i);
			DepInstance other = vecInstances2.get(i);
			
			for(int idx = 0; idx < tmpInstance.forms.size(); idx++)
			{
				String goldLabelSyn = other.deprels.get(idx);
				String goldLabelSem = tmpInstance.deprels.get(idx);
				String goldPosCur = tmpInstance.postags.get(idx);
				if(goldPosCur.equals("PU"))continue;
				

				
				
				int goldHead1 = tmpInstance.heads.get(idx);
				int goldHead2 = other.heads.get(idx);
				
				String matchMark = "false";
				if(goldHead1 == goldHead2 && goldLabelSyn.equals(goldLabelSem))
				{
					matchMark = "true";
				}

				String goldPos = goldLabelSyn + "\t" + matchMark;
				
				if(!errStats.containsKey(goldPos))
				{
					errStats.put(goldPos, 0);
				}
				
				errStats.put(goldPos, errStats.get(goldPos) + 1);

				
				if(!modTotal.containsKey(goldLabelSyn))
				{
					modTotal.put(goldLabelSyn, 0);
				}
				
				modTotal.put(goldLabelSyn, modTotal.get(goldLabelSyn)+1);
				errorKey.put(goldPos, goldLabelSyn);
			}

		}
		
		for(String theKey : errStats.keySet())
		{
			String modKey = errorKey.get(theKey);
			output.println(theKey + "\t" + errStats.get(theKey) + "\t" + modTotal.get(modKey));
		}

		
		output.close();

	}


}
