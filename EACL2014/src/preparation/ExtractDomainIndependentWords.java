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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


public class ExtractDomainIndependentWords {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		
		Set<String> hownet = new TreeSet<String>();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().length() < 3)continue;
			sLine = sLine.trim();
			String[] units = sLine.split("\\s+");
			if(units.length != 2)continue;
			hownet.add(units[0]);			
		}
		in.close();
		// TODO Auto-generated method stub
		Map<String, Map<String, Integer>> lexicon = new TreeMap<String, Map<String, Integer>>();
		
		int allWordsNum = 0;
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[1]), "UTF8"));
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] wordposs = sLine.trim().split("\\s+");
			allWordsNum += wordposs.length;
			for(String wordpos : wordposs)
			{
				int splitIndex = wordpos.lastIndexOf("_");
				if(splitIndex == -1)
				{
					System.out.println(wordpos + "[in]" + sLine);
					continue;
				}
				String theWord = wordpos.substring(0, splitIndex);
				String thePOS = wordpos.substring(splitIndex+1);
				if(!lexicon.containsKey(theWord))
				{
					lexicon.put(theWord, new TreeMap<String, Integer>());
				}
				if(!lexicon.get(theWord).containsKey(thePOS))
				{
					lexicon.get(theWord).put(thePOS, 0);
				}
				lexicon.get(theWord).put(thePOS, lexicon.get(theWord).get(thePOS)+1);						
			}
			
		}
		
		in.close();
		
/*		
		Map<String, Map<String, Integer>> lexicon_target = new HashMap<String, Map<String, Integer>>();
		
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[1]), "UTF8"));
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] wordposs = sLine.trim().split("\\s+");
			for(String wordpos : wordposs)
			{
				int splitIndex = wordpos.lastIndexOf("_");
				if(splitIndex == -1)
				{
					System.out.println(wordpos + "[in]" + sLine);
					continue;
				}
				String theWord = wordpos.substring(0, splitIndex);
				String thePOS = wordpos.substring(splitIndex+1);
				if(!lexicon_target.containsKey(theWord))
				{
					lexicon_target.put(theWord, new HashMap<String, Integer>());
				}
				if(!lexicon_target.get(theWord).containsKey(thePOS))
				{
					lexicon_target.get(theWord).put(thePOS, 0);
				}
				lexicon_target.get(theWord).put(thePOS, lexicon_target.get(theWord).get(thePOS)+1);						
			}
			
		}
		
		in.close();
*/		
		
		
		
		

		
		Map<String, Integer> lexicon_freq = new TreeMap<String, Integer>();
		for(String theWord : lexicon.keySet())
		{
			int totalFreq = 0;
			for(String thePOS : lexicon.get(theWord).keySet())
			{
				totalFreq = totalFreq + lexicon.get(theWord).get(thePOS);
			}							
			lexicon_freq.put(theWord, totalFreq);
		}
		
		
		
		
		/*
		List<Entry<String, Integer>> chapossortlist = new ArrayList<Entry<String, Integer>>(lexicon_freq.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				Integer values1 = (Integer)obj1.getValue();
				Integer values2 = (Integer)obj2.getValue();
				
				return values2.compareTo(values1);				
            }   
		}); */
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);
		out.println("word dictionary");
		int iCount = 0;
		
		int currentFreq = 0;
		Set<String> closedposs = new HashSet<String>();
		closedposs.add("AS");closedposs.add("BA");closedposs.add("CC");closedposs.add("CD");
		closedposs.add("CS");closedposs.add("DEC");closedposs.add("DEG");closedposs.add("DER");
		closedposs.add("DEV");closedposs.add("DT");closedposs.add("ETC");closedposs.add("IJ");
		closedposs.add("LB");closedposs.add("LC");closedposs.add("P");closedposs.add("PN");
		closedposs.add("PU");closedposs.add("SB");closedposs.add("SP");closedposs.add("VC");
		closedposs.add("NT");
		Set<String> commonWordsets = new HashSet<String>();
		int threshold = allWordsNum/50000+5; 
		System.out.println(String.format("%d/%d", threshold, allWordsNum));
		for(String theoutStr: lexicon.keySet())
		{
			//String theoutStr = curCharPoslist.getKey();
			int commonWords = 2; // not common
			if(hownet.contains(theoutStr) && lexicon_freq.get(theoutStr) > threshold)
			{
				commonWords = 1;
			}
			for(String thePOS :lexicon.get(theoutStr).keySet())
			{
				if(	closedposs.contains(thePOS) )
				{
					commonWords = 1;
					break;
				}
			}
					
			if(commonWords == 1)
			{
				String outstr = String.format("[%s] : %d", theoutStr, commonWords);
				out.println(outstr);
				currentFreq = currentFreq +  lexicon_freq.get(theoutStr);
				commonWordsets.add(theoutStr);
			}
			iCount++;
			
		}
		
		System.out.println(String.format("%d/%d", currentFreq, allWordsNum));
		
		//int threshold = allWordsNum/20000+3; 
		
		out.println();
		out.println("word tag dictionary");	
		
		for(String theFirstKey : lexicon.keySet())
		{
			if(commonWordsets.contains(theFirstKey))
			{
				for(String theSecondKey : lexicon.get(theFirstKey).keySet())
				{
					int theValue = lexicon.get(theFirstKey).get(theSecondKey);
					//if(theValue >= threshold)
					{
						String outstr = String.format("[%s] , %s : %d", theFirstKey, theSecondKey, 1);
						out.println(outstr);
					}
				}
			}
		}
		
		//closed tag words.
		
		System.out.println(iCount);
		out.close();

	}

}
