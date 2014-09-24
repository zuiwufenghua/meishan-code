package preparation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

public class ExtractDomainDict {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Map<String, Integer> commonwords = new TreeMap<String, Integer>();
		Map<String, Map<String, Integer>> commonwordposs = new TreeMap<String, Map<String, Integer>>();		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8"));
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
					commonwords.put(firstUnit, 0);
					commonwordposs.put(firstUnit, new TreeMap<String, Integer>());
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
					int wordEndIndex = firstUnit.indexOf("] , ");
					String theWord = firstUnit.substring(0, wordEndIndex+1);
					commonwordposs.get(theWord).put(firstUnit, 0);
				}
				
			}
		}
		
		in.close();
		
		Map<String, Integer> domainwords = new TreeMap<String, Integer>();
		Map<String, Map<String, Integer>> domainwordposs = new TreeMap<String, Map<String, Integer>>();
		
		
		
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
				String theWord = "[" + wordpos.substring(0, splitIndex) + "]";
				String thePOS = wordpos.substring(splitIndex+1);
				String theWordPOS = theWord + " , " + thePOS;
				if(!commonwords.containsKey(theWord))
				{
					if(!domainwords.containsKey(theWord))
					{
						domainwords.put(theWord, 0);
						domainwordposs.put(theWord, new TreeMap<String, Integer>());
					}
					domainwords.put(theWord, domainwords.get(theWord)+1);
					if(!domainwordposs.get(theWord).containsKey(theWordPOS))
					{
						domainwordposs.get(theWord).put(theWordPOS, 0);
					}
					domainwordposs.get(theWord).put(theWordPOS, domainwordposs.get(theWord).get(theWordPOS)+1);
					
				}
				else
				{
					commonwords.put(theWord, commonwords.get(theWord)+1);
					if(!commonwordposs.get(theWord).containsKey(theWordPOS))
					{
						//commonwordposs.get(theWord).put(theWordPOS, 0);
						domainwords.put(theWord, commonwords.get(theWord)+1);
						domainwordposs.put(theWord, new TreeMap<String, Integer>());
						for (String theWordPOSKey : commonwordposs.get(theWord).keySet())
						{
							domainwordposs.get(theWord).put(theWordPOSKey,  commonwordposs.get(theWord).get(theWordPOSKey));
						}
						domainwordposs.get(theWord).put(theWordPOS, 1);
						commonwordposs.remove(theWord);
						commonwords.remove(theWord);
					}
					else
					{
						commonwordposs.get(theWord).put(theWordPOS, commonwordposs.get(theWord).get(theWordPOS)+1);
					}
				}
								
			}
			
		}
		
		in.close();
		
		
		
		
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);
		int threshold  = 0;
		if(args.length > 3) threshold  = Integer.parseInt(args[3]);
		out.println("word dictionary");
		//four digit: afpd a=2[annotated],f=1[high],p=1[high],d=2[domain], 0 value denotes nonsense
		for(String theKey : commonwords.keySet())
		{
			out.println(String.format("%s : %d", theKey, 2011));
		}
		for(String theKey : domainwords.keySet())
		{
			if(domainwords.get(theKey) > threshold)
			{
				out.println(String.format("%s : %d", theKey, 2012));
			}
		}
		
		
		out.println();
		
		out.println("word tag dictionary");
		for(String theKey : commonwordposs.keySet())
		{			
			double allfreq = commonwords.get(theKey)+1;
			List<Entry<String, Integer>> wordpospairs = KnowledgeExtractFromAutoSentences.MapIntSort(commonwordposs.get(theKey));
			
			for(Entry<String, Integer> theWordPOS : wordpospairs)
			{
				int entryvalue = 2000 + 1 + probSplit( (theWordPOS.getValue() + 1.0) / allfreq) * 10;
				out.println(String.format("%s : %d", theWordPOS.getKey(), entryvalue));
			}
		}
		
		
		for(String theKey : domainwordposs.keySet())
		{
			if(domainwords.get(theKey) <= threshold) continue;
			double allfreq = domainwords.get(theKey)+1;
			List<Entry<String, Integer>> wordpospairs = KnowledgeExtractFromAutoSentences.MapIntSort(domainwordposs.get(theKey));
			
			for(Entry<String, Integer> theWordPOS : wordpospairs)
			{
				int entryvalue = 2000 + 2 + probSplit( (theWordPOS.getValue() + 1.0) / allfreq) * 10;
				out.println(String.format("%s : %d", theWordPOS.getKey(), entryvalue));
			}
		}

		out.println();				
		
		out.close();
		

	}
	
	
	public static int probSplit(double prob)
	{
		assert(prob < 1 + 1e-15 && prob > 0 - 1e-15);
		if(prob > 0.95)
		{
			return 1;
		}
		else if(prob > 0.75)
		{
			return 2;
		}
		else if(prob > 0.25)
		{
			return 3;
		}
		else if(prob > 0.05)
		{
			return 4;
		}
		else
		{
			return 5;
		}
		
	}
	

}
