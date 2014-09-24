package preparation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

public class ExtractDomainSpecificWordsV1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		Map<String, Integer> commonwords = new HashMap<String, Integer>();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().length() < 3)continue;
			sLine = sLine.trim();
			int lastmaohaoIndex = sLine.lastIndexOf(":");
			if(lastmaohaoIndex == -1)continue;
			String firstUnit = sLine.substring(0, lastmaohaoIndex).trim();
			String secondunit = sLine.substring(lastmaohaoIndex + 1).trim();
			commonwords.put(firstUnit, Integer.parseInt(secondunit));			
		}
		in.close();
				
		Map<String, Map<String, Integer>> lexicon = new HashMap<String, Map<String, Integer>>();		
		Map<String, Integer> domainWordFreq = new HashMap<String, Integer>();
	
		
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[1]), "UTF8"));
		int totalWordCount = 0;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] wordposs = sLine.trim().split("\\s+");
			totalWordCount = totalWordCount + wordposs.length;
			for(String wordpos : wordposs)
			{
				int splitIndex = wordpos.lastIndexOf("_");
				if(splitIndex == -1)
				{
					System.out.println(wordpos + "[in]" + sLine);
					continue;
				}
				String theWord = "[" + wordpos.substring(0, splitIndex) + "]";
				
				if(!commonwords.containsKey(theWord))
				{
					if(!domainWordFreq.containsKey(theWord))
					{
						domainWordFreq.put(theWord, 0);
					}
					domainWordFreq.put(theWord, domainWordFreq.get(theWord)+1);
				}
				String thePOS = wordpos.substring(splitIndex+1);
				if(!lexicon.containsKey(theWord))
				{
					lexicon.put(theWord, new HashMap<String, Integer>());
				}
				if(!lexicon.get(theWord).containsKey(thePOS))
				{
					lexicon.get(theWord).put(thePOS, 0);
				}
				lexicon.get(theWord).put(thePOS, lexicon.get(theWord).get(thePOS)+1);
				
			}
			
		}
		
		in.close();
		
		int threshold  = totalWordCount/30000 + 3;
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);
		out.println("word dictionary");
		for(String theKey : commonwords.keySet())
		{
			out.println(String.format("%s : %d", theKey, 2011));
		}
		for(String theKey : domainWordFreq.keySet())
		{
			if(domainWordFreq.get(theKey) > threshold)
			{
				out.println(String.format("%s : %d", theKey, 2012));
			}
		}
		
		out.println();
		
		out.println("word tag distribution dictionary");
		sortandout(out, lexicon, threshold);
		out.println();				
		
		out.close();
		
	}
	
	
	public static void sortandout(PrintWriter out, Map<String, Map<String, Integer>> lexicon, int threshold)
	{
		Map<String, Integer> lexicon_freq = new TreeMap<String, Integer>();
		int totalNum = 0;
		for(String theWord : lexicon.keySet())
		{
			int totalFreq = 0;
			for(String thePOS : lexicon.get(theWord).keySet())
			{
				totalFreq = totalFreq + lexicon.get(theWord).get(thePOS);
			}						
			lexicon_freq.put(theWord, totalFreq);
			totalNum = totalNum + totalFreq;
		}
		
		
		List<Entry<String, Integer>> chapossortlist = new ArrayList<Entry<String, Integer>>(lexicon_freq.entrySet());
		
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				Double values1 = (Double)obj1.getValue();
				Double values2 = (Double)obj2.getValue();
				
				return values2.compareTo(values1);				
            }   
		}); 
		
		int currentFreq = 0;
		for(Entry<String, Integer> curCharPoslist: chapossortlist)
		{
			String theoutStr = curCharPoslist.getKey();		
			double wordFreq = curCharPoslist.getValue() * 1.0;
			if(wordFreq <= threshold) continue;
			for(String thePOS :lexicon.get(theoutStr).keySet())
			{
				double prob = lexicon.get(theoutStr).get(thePOS) * 1.0 / wordFreq;
				String marktag = "1";
				if(prob > 0.25 && prob < 0.75) marktag = "2";
				if(prob >= 0.75) marktag = "3";
				String outstr = String.format("%s , %s : %s", theoutStr, thePOS, marktag);
				out.println(outstr);
				currentFreq = currentFreq +  lexicon.get(theoutStr).get(thePOS);
			}
			
		}
		
		//System.out.println(String.format("%d/%d", currentFreq, totalNum));
	}

}
