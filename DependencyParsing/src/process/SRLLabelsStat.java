package process;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;

public class SRLLabelsStat {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
				args[0]), "UTF-8"));
		String sLine = null;
		Map<String, Integer> semFreq = new HashMap<String, Integer>();
		while((sLine = reader.readLine()) != null) {
			List<String> curSems = getSemanticLabels(sLine);
			
			for(String theSem : curSems)
			{
				if(!semFreq.containsKey(theSem))semFreq.put(theSem, 0);
				
				semFreq.put(theSem, semFreq.get(theSem)+1);
			}
		}
		
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"));
		
		for(String sem: semFreq.keySet())
		{
			writer.println(String.format("%s\t%d", sem, semFreq.get(sem)));
		}
			
		writer.close();
		
	}
	
	public static List<String> getSemanticLabels(String input)
	{
		List<String> sems = new ArrayList<String>();
		int indexStart = input.indexOf("-- ");
		if(indexStart == -1) return sems;
		String newInput = input.substring(indexStart + 3).trim();
		
		String[] unitSems = newInput.split("\\s+");
		
		for(String oneSem : unitSems)
		{
			int indexSlash = oneSem.indexOf("-");
			if(indexSlash != -1)
			{
				sems.add(oneSem.substring(indexSlash+1).trim());
			}
		}
		
		return sems;
	}

}
