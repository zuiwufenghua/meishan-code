package WordStructure;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MergeTwoDict {

	/**
	 * @param args
	 */
	//以从标注的语料中提出来的结构为gold
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Map<String, Map<String, Integer>> partiWordAnnotated = new HashMap<String, Map<String, Integer>>();
		LoadNewDict(args[0], partiWordAnnotated);
		LoadOldDict(args[1], partiWordAnnotated);
		CheckAnnotatedGenerateNew.printPartialDict(partiWordAnnotated, args[2]);
	}
	
	// please the g mark is at end
	public static void LoadOldDict(String inFile, Map<String, Map<String, Integer>> partiWordAnnotated ) throws Exception
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(inFile), "UTF8"));
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals("") || sLine.trim().length() < 2)continue;
			String[] wordposs = sLine.trim().split("\\s+");
			int start = 0;
			if(wordposs[0].equals("g"))
			{
				start = 1;
			}
			Map<String, Integer> tags = new HashMap<String, Integer>();
			for(int idx = start+1; idx < wordposs.length;idx++)
			{
				tags.put(wordposs[idx].substring(0,1), 1);
			}
			
			if(!partiWordAnnotated.containsKey(wordposs[start]))
			{
				partiWordAnnotated.put(wordposs[start], tags);
			}
			else if(start == 1)
			{
				partiWordAnnotated.put(wordposs[start], tags);
			}
		}
		
		in.close();
	}
	
	// please the g mark is at end
	public static void LoadNewDict(String inFile, Map<String, Map<String, Integer>> partiWordAnnotated ) throws Exception
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(inFile), "UTF8"));
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals("") || sLine.trim().length() < 2)continue;
			String[] wordposs = sLine.trim().split("\\s+");
			int start = 0;
			if(wordposs[0].equals("g"))
			{
				start = 1;
			}
			Map<String, Integer> tags = new HashMap<String, Integer>();
			for(int idx = start+1; idx < wordposs.length;idx++)
			{
				tags.put(wordposs[idx].substring(0,1), 1);
			}
			
			if(!partiWordAnnotated.containsKey(wordposs[start]))
			{
				partiWordAnnotated.put(wordposs[start], tags);
			}
			else if(start == 1)
			{
				partiWordAnnotated.put(wordposs[start], tags);
			}
		}
		
		in.close();
	}

}
