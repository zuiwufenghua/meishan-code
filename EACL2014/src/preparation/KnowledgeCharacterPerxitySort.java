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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

public class KnowledgeCharacterPerxitySort {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Set<String> domainwords = new TreeSet<String>();
		Map<String, Set<String>> domainwordposs = new TreeMap<String, Set<String>>();		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8"));
		String sLine = null;
		
		boolean bWord = false;
		boolean bWordPOS = false;
		int maxDomainWordLength = 0;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().length() < 3)continue;
			sLine = sLine.trim();
			if(sLine.indexOf("word dictionary") != -1)
			{
				bWord = true;
				bWordPOS = false;
			}
			
			if(sLine.indexOf("word tag dictionary") != -1)
			{
				bWord = false;
				bWordPOS = true;
			}
			if(bWord)
			{
				int lastmaohaoIndex = sLine.lastIndexOf(":");
				if(lastmaohaoIndex == -1)continue;
				String firstUnit = sLine.substring(0, lastmaohaoIndex).trim();
				String secondunit = sLine.substring(lastmaohaoIndex + 1).trim();
				int wordproperty = Integer.parseInt(secondunit);
				if(wordproperty%10 == 1)
				{
					//commonwords.add(firstUnit);	
				}
				else
				{
					domainwords.add(firstUnit.substring(1, firstUnit.length()-1));
					if(firstUnit.length()-2 > maxDomainWordLength)
					{
						maxDomainWordLength = firstUnit.length()-2;
					}
				}
			}
			if(bWordPOS)
			{
				int lastmaohaoIndex = sLine.lastIndexOf(":");
				if(lastmaohaoIndex == -1)continue;
				String firstUnit = sLine.substring(0, lastmaohaoIndex).trim();
				String secondunit = sLine.substring(lastmaohaoIndex + 1).trim();
				int wordproperty = Integer.parseInt(secondunit);
				if(wordproperty%10 == 1)
				{
					//commonwordposs.add(firstUnit);
				}
				else
				{
					int wordEndIndex = firstUnit.indexOf("] , ");
					String theWord = firstUnit.substring(1, wordEndIndex).trim();
					String thePOS = firstUnit.substring(wordEndIndex+4).trim();
					if(!domainwordposs.containsKey(theWord))
					{
						domainwordposs.put(theWord, new TreeSet<String>());
					}
					domainwordposs.get(theWord).add(thePOS);
					//domainwordposs.add(firstUnit.substring(1, wordEndIndex));
					if(theWord.length() > maxDomainWordLength)
					{
						maxDomainWordLength = theWord.length();
					}
				}
				
			}
		}
		
		in.close();
		
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[1]), "UTF-8"));
		Map<String, Map<String, Double>> trigramProb = new HashMap<String, Map<String, Double>>();
		Map<String, Map<String, Double>> bigramProb = new HashMap<String, Map<String, Double>>();
		Map<String, Double> unigramProb = new HashMap<String,Double>();
		double totalCharNum = 0.0;
		while ((sLine = in.readLine()) != null) {
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
		
		in.close();
		
		
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
		
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[2]), "UTF-8"));
		
		Map<String, Double> newSentencesProb = new HashMap<String, Double>();
		
		
		
		String wordposline = null;
		
		while ((wordposline = in.readLine()) != null) {
			if (wordposline.trim().equals(""))
				continue;
			wordposline = wordposline.trim();
			
			String[] wordposivtags = wordposline.split("\\s+");
			sLine = "";
			boolean invalidSentence = false;
			Map<String, String> segIndexPOSs = new TreeMap<String, String>();
			
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
					invalidSentence = true;
					break;
				}
				
				String cursegIndex = String.format("[%d %d]", sLine.length(), sLine.length()+curUnits[0].length()-1);
				String cursegpos = curUnits[1];
				segIndexPOSs.put(cursegIndex, cursegpos);
				sLine = sLine + curUnits[0];
				
			}
			if(invalidSentence)
			{
				continue;
			}
			
			
			double scoreSent = 0.0;
			for(int i = 0; i < sLine.length(); i++)
			{
				String charPrev1 = "[start]";
				String charPrev2 = "[start]";
				if(i > 0) charPrev1 = sLine.substring(i-1, i);
				if(i > 1) charPrev2 = sLine.substring(i-2, i-1);
				String curChar = sLine.substring(i, i+1);
				String trigram = charPrev2 + "\t" + charPrev1 + "\t" + curChar;
				int j =  i+maxDomainWordLength;
				if(j > sLine.length()) j = sLine.length();
				for(; j > i; j--)
				{
					String curWord = sLine.substring(i, j);
					if(domainwordposs.containsKey(curWord) || domainwords.contains(curWord))
					{
						break;
					}
					
				}
				if(j > i)
				{
					String cursegIndex = String.format("[%d %d]", i, j-1);
					String cursegWord = sLine.substring(i, j);
					
					if(!segIndexPOSs.containsKey(cursegIndex))
					{
						scoreSent = scoreSent + cursegWord.length() *  Math.log(0.00005);
					}
					else
					{
						String curPOS = segIndexPOSs.get(cursegIndex);
						if(domainwordposs.containsKey(cursegWord) && !domainwordposs.get(cursegWord).contains(curPOS))
						{
							scoreSent = scoreSent + cursegWord.length() *  Math.log(0.0005);
						}
					}
															
					i = j-1;
					//if(j < sLine.length())
					//{
					//	String unigram = sLine.substring(j, j+1);
					//	scoreSent = scoreSent + Math.log(CharacterPerxitySort.score(unigram, trigramProb_N, bigramProb_N, unigramProb_N));
					//}
					//if(j < sLine.length()-1)
					//{
					//	String bigram = sLine.substring(j, j+1) + "\t" + sLine.substring(j+1, j+2);
					//	scoreSent = scoreSent + Math.log(CharacterPerxitySort.score(bigram, trigramProb_N, bigramProb_N, unigramProb_N));
					//}
				}
				else
				{
					scoreSent = scoreSent + Math.log(CharacterPerxitySort.score(trigram, trigramProb_N, bigramProb_N, unigramProb_N));
				}
								
			}
			
			scoreSent = scoreSent / sLine.length();
			
			//out.println(String.format("%s\t%f", sLine, scoreSent));
			newSentencesProb.put(wordposline, scoreSent);
			
			//newSentencesProb.put(sLine, scoreSent);
			
		}
		
		in.close();
		
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[3]), "UTF-8"), false);
		
		List<Entry<String, Double>> sentSort = MapDoubleSort(newSentencesProb);
		for(int idx = 0; idx < sentSort.size(); idx++)
		{
			out.println(sentSort.get(idx).getKey());
			//out.print(" ");
			//out.println(sentSort.get(idx).getKey());
		}
		
		out.close();


	}
	
	public static List<Entry<String, Double>> MapDoubleSort(Map<String, Double> input)
	{
		List<Entry<String, Double>> mapintsort = new ArrayList<Entry<String, Double>>(input.entrySet());
		
		Collections.sort(mapintsort, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				Double a1 = (Double)obj1.getValue();
				Double a2 = (Double)obj2.getValue();
				
				return a2.compareTo(a1);				
            }   
		}); 
		
		return mapintsort;
	}

}
