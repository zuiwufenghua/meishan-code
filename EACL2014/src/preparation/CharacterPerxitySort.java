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
import java.util.Map.Entry;

public class CharacterPerxitySort {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8"));
		String sLine = null;
		Map<String, Map<String, Double>> trigramProb = new HashMap<String, Map<String, Double>>();
		Map<String, Map<String, Double>> bigramProb = new HashMap<String, Map<String, Double>>();
		Map<String, Double> unigramProb = new HashMap<String,Double>();
		double totalCharNum = 0.0;
		while ((sLine = reader.readLine()) != null) {
			if (sLine.trim().equals(""))
				continue;
			
			sLine = sLine.trim();
			for(int i = 0; i < sLine.length(); i++)
			{
				String charPrev1 = "[start]";
				String charPrev2 = "[start]";
				if(i > 0) charPrev1 = sLine.substring(i-1, i);
				if(i > 1) charPrev2 = sLine.substring(i-2, i-1);
				String curChar = sLine.substring(i, i+1);
				String twoChar = charPrev2 + "\t" + charPrev1;
				if(!trigramProb.containsKey(twoChar))
				{
					trigramProb.put(twoChar, new HashMap<String,Double>());
				}
				if(!trigramProb.get(twoChar).containsKey(curChar))
				{
					trigramProb.get(twoChar).put(curChar, 0.0);
				}
				trigramProb.get(twoChar).put(curChar, trigramProb.get(twoChar).get(curChar) + 1.0);
				
				if(!bigramProb.containsKey(charPrev1))
				{
					bigramProb.put(charPrev1, new HashMap<String,Double>());
				}
				if(!bigramProb.get(charPrev1).containsKey(curChar))
				{
					bigramProb.get(charPrev1).put(curChar, 0.0);
				}
				bigramProb.get(charPrev1).put(curChar, bigramProb.get(charPrev1).get(curChar) + 1.0);
				
				if(!unigramProb.containsKey(curChar))
				{
					unigramProb.put(curChar, 0.0);
				}
				unigramProb.put(curChar, unigramProb.get(curChar) + 1.0);
				
			}
			
			totalCharNum = totalCharNum + sLine.length();
			
			
		}
		
		reader.close();
		
		
		Map<String, Map<String, Double>> trigramProb_N = new HashMap<String, Map<String, Double>>();
		Map<String, Map<String, Double>> bigramProb_N = new HashMap<String, Map<String, Double>>();
		Map<String, Double> unigramProb_N = new HashMap<String,Double>();
		
		for(String theKey : trigramProb.keySet())
		{
			double total_occur = 0.0;
			for(String theSubKey : trigramProb.get(theKey).keySet())
			{
				total_occur = total_occur + trigramProb.get(theKey).get(theSubKey);
			}
			trigramProb_N.put(theKey, new HashMap<String, Double>());
			
			for(String theSubKey : trigramProb.get(theKey).keySet())
			{
				trigramProb_N.get(theKey).put(theSubKey, (trigramProb.get(theKey).get(theSubKey) + 1.0 )/ (total_occur + 1.0));
			}
		}
		
		for(String theKey : bigramProb.keySet())
		{
			double total_occur = 0.0;
			for(String theSubKey : bigramProb.get(theKey).keySet())
			{
				total_occur = total_occur + bigramProb.get(theKey).get(theSubKey);
			}
			bigramProb_N.put(theKey, new HashMap<String, Double>());
			for(String theSubKey : bigramProb.get(theKey).keySet())
			{
				bigramProb_N.get(theKey).put(theSubKey, (bigramProb.get(theKey).get(theSubKey) + 1.0 )/ (total_occur + 1.0));
			}
		}
		
		for(String theKey : unigramProb.keySet())
		{
			unigramProb_N.put(theKey, (unigramProb.get(theKey) + 1.0 ) /(totalCharNum + 1.0));
		}
		
		reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[1]), "UTF-8"));
		
		//Map<String, Double> newSentencesProb = new HashMap<String, Double>();
		
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);
		
		
		
		while ((sLine = reader.readLine()) != null) {
			if (sLine.trim().equals(""))
				continue;
			sLine = sLine.trim();
			//if(newSentencesProb.containsKey(sLine))
			//{
			//	continue;
			//}
			
			double scoreSent = 0.0;
			for(int i = 0; i < sLine.length(); i++)
			{
				String charPrev1 = "[start]";
				String charPrev2 = "[start]";
				if(i > 0) charPrev1 = sLine.substring(i-1, i);
				if(i > 1) charPrev2 = sLine.substring(i-2, i-1);
				String curChar = sLine.substring(i, i+1);
				String trigram = charPrev2 + "\t" + charPrev1 + "\t" + curChar;
				scoreSent = scoreSent + Math.log(score(trigram, trigramProb_N, bigramProb_N, unigramProb_N));				
			}
			
			scoreSent = scoreSent / sLine.length();
			
			out.println(String.format("%s\t%f", sLine, scoreSent));
			
			//newSentencesProb.put(sLine, scoreSent);
			
		}
		
		reader.close();
		out.close();
		
		/*
		List<Entry<String, Double>> chapossortlist = new ArrayList<Entry<String, Double>>(newSentencesProb.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				Double values1 = (Double)obj1.getValue();
				Double values2 = (Double)obj2.getValue();
				
				return values2.compareTo(values1);				
            }   
		});
		
		
		
		
		for(Entry<String, Double> theKey : chapossortlist)
		{
			out.println(theKey.getKey());
		}
		
		
		*/
	}
	
	
	
	public static double score(String trigram, Map<String, Map<String, Double>> trigramProb, 
			Map<String, Map<String, Double>>bigramProb, Map<String, Double> unigramProb)
	{
		double prob = 0.00005;
		
		String[] threeUnits = trigram.split("\\s+");
		if(threeUnits.length > 3)
		{
			return -1;
		}
		if(threeUnits.length == 3)
		{		
			String twoUnit = threeUnits[0] + "\t" + threeUnits[1];
			if(trigramProb.containsKey(twoUnit) && trigramProb.get(twoUnit).containsKey(threeUnits[2]))
			{
				prob = prob + 0.6 * trigramProb.get(twoUnit).get(threeUnits[2]);
			}
			
			if(bigramProb.containsKey(threeUnits[1]) && bigramProb.get(threeUnits[1]).containsKey(threeUnits[2]))
			{
				prob = prob + 0.3 *  bigramProb.get(threeUnits[1]).get(threeUnits[2]);
			}
			
			if(unigramProb.containsKey(threeUnits[2]))
			{
				prob = prob + 0.1 * unigramProb.get(threeUnits[2]);
			}
		}
		else if(threeUnits.length == 2)
		{					
			if(bigramProb.containsKey(threeUnits[0]) && bigramProb.get(threeUnits[0]).containsKey(threeUnits[1]))
			{
				prob = prob + 0.7 *  bigramProb.get(threeUnits[0]).get(threeUnits[1]);
			}
			
			if(unigramProb.containsKey(threeUnits[1]))
			{
				prob = prob + 0.3 * unigramProb.get(threeUnits[1]);
			}
		}
		else
		{
			if(unigramProb.containsKey(threeUnits[0]))
			{
				prob = prob + unigramProb.get(threeUnits[0]);
			}
			
		}
				
		
		return prob;
	}

}
