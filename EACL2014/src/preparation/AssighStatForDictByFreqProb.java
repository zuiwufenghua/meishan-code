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

public class AssighStatForDictByFreqProb {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		Map<String, Double> commonwords = new TreeMap<String, Double>();
		Map<String, Double> commonwordposs = new TreeMap<String, Double>();	
		Map<String, Double> domainwords = new TreeMap<String, Double>();
		Map<String, Double> domainwordposs = new TreeMap<String, Double>();		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8"));
		double threshold = 0.5;
		if(args.length > 3)threshold = Integer.parseInt(args[3]) + 0.5;
		String sLine = null;
		
		boolean bWord = false;
		boolean bWordPOS = false;
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
				if(wordproperty == 1)
				{
					commonwords.put(firstUnit, 0.0);	
				}
				else
				{
					domainwords.put(firstUnit, 0.0);
				}
			}
			if(bWordPOS)
			{
				int lastmaohaoIndex = sLine.lastIndexOf(":");
				if(lastmaohaoIndex == -1)continue;
				String firstUnit = sLine.substring(0, lastmaohaoIndex).trim();
				String secondunit = sLine.substring(lastmaohaoIndex + 1).trim();
				int wordproperty = Integer.parseInt(secondunit);
				if(wordproperty == 1)
				{
					commonwordposs.put(firstUnit, 0.0);
				}
				else
				{
					domainwordposs.put(firstUnit, 0.0);
				}
				
			}
		}
		
		in.close();
		
		Map<String, Double> domain2words = new TreeMap<String, Double>();
		Map<String, Double> domain2wordposs = new TreeMap<String, Double>();	
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
				
				String wordKey = "[" +theWord+ "]";
				String wordposKey = "[" + theWord + "] , " + thePOS;
				/*
				if(commonwords.containsKey(wordKey))
				{
					commonwords.put(wordKey, commonwords.get(wordKey)+1.0);
				}
				else if(domainwords.containsKey(wordKey))
				{
					domainwords.put(wordKey, domainwords.get(wordKey)+1.0);
				}
				else
				{
					if(!domain2words.containsKey(wordKey))
					{
						domain2words.put(wordKey, 0.0);
					}
					domain2words.put(wordKey, domain2words.get(wordKey)+1.0);
				}*/
				
				if(commonwordposs.containsKey(wordposKey))
				{
					commonwords.put(wordKey, commonwords.get(wordKey)+1.0);
					commonwordposs.put(wordposKey, commonwordposs.get(wordposKey)+1.0);
				}
				else if(domainwordposs.containsKey(wordposKey))
				{
					if(commonwords.containsKey(wordKey))
					{
						commonwords.put(wordKey, commonwords.get(wordKey)+1.0);
					}
					else if(domainwords.containsKey(wordKey))
					{
						domainwords.put(wordKey, domainwords.get(wordKey)+1.0);
					}
					domainwordposs.put(wordposKey, domainwordposs.get(wordposKey)+1.0);
				}
				else if(!commonwords.containsKey(wordKey) && !domainwords.containsKey(wordKey))
				{
					if(!domain2words.containsKey(wordKey))
					{
						domain2words.put(wordKey, 0.0);
					}
					domain2words.put(wordKey, domain2words.get(wordKey)+1.0);
					
					if(!domain2wordposs.containsKey(wordposKey))
					{
						domain2wordposs.put(wordposKey, 0.0);
					}
					domain2wordposs.put(wordposKey, domain2wordposs.get(wordposKey)+1.0);
				}
			}
			
		}
		
		in.close();
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"));
		
		output.println("word dictionary");
		for(String theKey : commonwords.keySet())
		{
			//if(commonwords.get(theKey) > 0.5)
			output.println(String.format("%s : 301", theKey));
		}

		for(String theKey : domainwords.keySet())
		{
			//if(domainwords.get(theKey) > 0.5)
			output.println(String.format("%s : 302",  theKey));
		}
		
		
		{
			List<Entry<String, Double>> wordfreqSort = KnowledgeCharacterPerxitySort.MapDoubleSort(domain2words);
			int totalsize = 0;
			for(int idx = 0; idx < wordfreqSort.size(); idx++)
			{
				if(wordfreqSort.get(idx).getValue() > threshold)
				{
					totalsize++;
				}
				else
				{
					break;
				}
			}
			int middlesize = totalsize * 15 / 100;
			for(int idx = 0; idx < totalsize; idx++)
			{
				if(idx < middlesize)
				{
					output.println(String.format("%s : 202",  wordfreqSort.get(idx).getKey()));
				}
				else
				{
					output.println(String.format("%s : 102",  wordfreqSort.get(idx).getKey()));
				}
			}
		}
		
		output.println();
		output.println("word tag dictionary");	

		
		for(String theKey : commonwordposs.keySet())
		{
			//if(commonwordposs.get(theKey) > 0.5)
			{
				String wordKey = getWordKeyFromDictWordPos(theKey);
				double prob = commonwordposs.get(theKey) /commonwords.get(wordKey);
				int problevel = probLevel(prob);
				int finalvalue = 3*100 + problevel * 10 + 1;
				output.println(String.format("%s : %d", theKey, finalvalue));
			}
		}

		for(String theKey : domainwordposs.keySet())
		{
			String wordKey = getWordKeyFromDictWordPos(theKey);
			double wordcount = 0;
			if(commonwords.containsKey(wordKey))
			{
				wordcount = commonwords.get(wordKey);
			}
			else
			{
				wordcount = domainwords.get(wordKey);
			}
			if(wordcount > 0.5 ) //&& domainwordposs.get(theKey) > 0.5)
			{
				double prob = domainwordposs.get(theKey) /wordcount;
				int problevel = probLevel(prob);
				int finalvalue = 3*100 + problevel * 10 + 2;
				output.println(String.format("%s : %d", theKey, finalvalue));
			}
		}
		
		
		{
			List<Entry<String, Double>> wordfreqSort = KnowledgeCharacterPerxitySort.MapDoubleSort(domain2wordposs);
			int totalsize = 0;
			for(int idx = 0; idx < wordfreqSort.size(); idx++)
			{
				if(wordfreqSort.get(idx).getValue() > threshold)
				{
					totalsize++;
				}
				else
				{
					break;
				}
			}
			int middlesize = totalsize * 15 / 100;
			for(int idx = 0; idx < totalsize; idx++)
			{
				String wordKey = getWordKeyFromDictWordPos(wordfreqSort.get(idx).getKey());
				double wordcount = 0;
				if(commonwords.containsKey(wordKey))
				{
					wordcount = commonwords.get(wordKey);
				}
				else if(domainwords.containsKey(wordKey))
				{
					wordcount = domainwords.get(wordKey);
				}
				else
				{
					wordcount = domain2words.get(wordKey);
				}
				if(wordcount > 0.5) // && wordfreqSort.get(idx).getValue() > 0.5)
				{
					double prob = wordfreqSort.get(idx).getValue() /wordcount;
					int problevel = probLevel(prob);
					if(idx < middlesize)
					{
						int finalvalue = 2*100 + problevel * 10 + 2;
						output.println(String.format("%s : %d", wordfreqSort.get(idx).getKey(), finalvalue));
					}
					else
					{
						int finalvalue = 1*100 + problevel * 10 + 2;
						output.println(String.format("%s : %d", wordfreqSort.get(idx).getKey(), finalvalue));
					}
				}
			}
		}
				
		output.close();
		

	}
	
	
	public static String getWordKeyFromDictWordPos(String wordposkey)
	{
		int endIndex = wordposkey.lastIndexOf("] , ");
		assert(endIndex > 0);
		return wordposkey.substring(0, endIndex+1);
	}
	
	public static int probLevel(double prob)
	{
		if(prob > 0.9)
		{
			return 3;
		}
		//else if (prob > 0.75)
		//{
		//	return 4;
		//}
		//else if (prob > 0.25)
		//{
		//	return 3;
		//}
		else if (prob > 0.1)
		{
			return 2;
		}
		else
		{
			return 1;
		}
	}

}
