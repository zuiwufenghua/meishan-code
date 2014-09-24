package preparation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

public class AnalyzeWordPropertyAuto {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		int wordposIdx = 0;
		Map<String, Map<String, Integer>> inputWords = new TreeMap<String, Map<String, Integer>>();
		BufferedReader in = null;
		String sLine = null;
		if(args.length == 3)
		{
			wordposIdx = 1;
			
			in = new BufferedReader(new InputStreamReader(
					new FileInputStream(args[0]), "UTF8"));
			
			
			while ((sLine = in.readLine()) != null) {
				if(sLine.trim().length() < 3)continue;
				sLine = sLine.trim();
				int lastmaohaoIndex = sLine.lastIndexOf(":");
				if(lastmaohaoIndex == -1)continue;
				String firstUnit = sLine.substring(0, lastmaohaoIndex).trim();
				inputWords.put(firstUnit.substring(1, firstUnit.length()-1), new TreeMap<String, Integer>());
			}
			
			in.close();
		}
			
		int allWordsNum = 0;
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[wordposIdx]), "UTF8"));
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().length() < 3)continue;
			String newLine = sLine.trim();
			
			if(newLine.equals(""))
			{
				continue;
			}
			String[] wordposivtags = newLine.split("\\s+");
			allWordsNum += wordposivtags.length;
			for(int idx = 0; idx < wordposivtags.length; idx++)
			{
				String[] curUnits = wordposivtags[idx].split("_");
				
				String left1WordPOSTAG = "[START]_[START]";
				String left2WordPOSTAG = "[START]_[START]";
				
				String right1WordPOSTAG = "[END]_[END]";
				String right2WordPOSTAG = "[END]_[END]";
				if(idx > 0)left1WordPOSTAG = wordposivtags[idx-1];
				if(idx > 1)left2WordPOSTAG = wordposivtags[idx-2];
				if(idx < wordposivtags.length -1)right1WordPOSTAG = wordposivtags[idx+1];
				if(idx < wordposivtags.length -2)right1WordPOSTAG = wordposivtags[idx+2];
				String[] left1Units = left1WordPOSTAG.split("_");
				String[] left2Units = left2WordPOSTAG.split("_");
				String[] right1Units = right1WordPOSTAG.split("_");
				String[] right2Units = right2WordPOSTAG.split("_");
				
				if(left1Units.length != 2 || left2Units.length != 2
				|| right1Units.length != 2 || right2Units.length != 2
				|| curUnits.length != 2)
				{
					System.out.println("error _ num:\t" + left2WordPOSTAG + " " + left1WordPOSTAG + " "
							+ wordposivtags[idx] + " " + right1WordPOSTAG + " " + right2WordPOSTAG);
					continue;
				}
				
				if(wordposIdx == 0 && !inputWords.containsKey(curUnits[0]))
				{
					inputWords.put(curUnits[0], new TreeMap<String, Integer>());
				}
				
				if(inputWords.containsKey(curUnits[0]))
				{
					if(!inputWords.get(curUnits[0]).containsKey(curUnits[1]))
					{
						inputWords.get(curUnits[0]).put(curUnits[1], 0);
					}
					
					inputWords.get(curUnits[0]).put(curUnits[1], inputWords.get(curUnits[0]).get(curUnits[1]) + 1);
				}

			}
		}
		
		in.close();
		

		//int threshold = allWordsNum/20000+3;
		//if(threshold > 8) threshold = 10;
		int threshold = 10;
		int thresholdWord = 80;
		System.out.println(threshold);
		Set<String> validWords = new TreeSet<String>();
		Set<String> validWordposs = new TreeSet<String>(); 
		for(String thefirstKey : inputWords.keySet())
		{
			List<Entry<String, Integer>> temp = KnowledgeExtractFromAutoSentences.MapIntSort(inputWords.get(thefirstKey));
			//output.println(thefirstKey);
			int currentFreq = 0;
			for(int idx = 0; idx < temp.size(); idx++)
			{
				Entry<String, Integer> theEntry = temp.get(idx);
				String theKey = theEntry.getKey();
				int theValue  = theEntry.getValue();
				
				if(theValue < threshold) continue;
				//output.println(String.format("%s : %d",  theKey, theValue));
				currentFreq = currentFreq + theValue;
				if(theKey.equals("NR"))
				{
					validWordposs.add("[" + thefirstKey + "] , " + theKey + " : 2");	
					currentFreq = thresholdWord + 100;
					break;
				}
			}
			
			if(currentFreq > thresholdWord)
			{
				validWords.add("[" + thefirstKey + "] : 2");	
			}
		}
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[wordposIdx+1]), "UTF-8"));
		
		output.println("word dictionary");
		for(String theKey : validWords)
		{
			output.println(theKey);
		}
			
		
		output.println();
		output.println("word tag dictionary");	
		
		for(String theKey : validWordposs)
		{
			output.println(theKey);
		}
		
		
		output.close();

	}

}
