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

public class Add3kToWordDictionary {

	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception{
		// TODO Auto-generated method stub
		Set<String> commonwords = new TreeSet<String>();
		Set<String> commonwordposs = new TreeSet<String>();	
		Set<String> domainwords = new TreeSet<String>();
		Set<String> domainwordposs = new TreeSet<String>();		
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
					commonwords.add(firstUnit);	
				}
				else
				{
					domainwords.add(firstUnit);
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
					commonwordposs.add(firstUnit);
				}
				else
				{
					domainwordposs.add(firstUnit);
				}
				
			}
		}
		
		in.close();
		
		
		in = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(args[1]), "UTF-8"));
		
		Map<String, Map<String, Integer>> newdomainWordposs = new TreeMap<String, Map<String, Integer>>();
		Map<String, Integer> newdomainWordFreq = new TreeMap<String, Integer>();
		
		String lastWord = "";		
		while ((sLine = in.readLine()) != null) {
			sLine = sLine.trim();
			if(sLine.length() < 3)
			{
				continue;
			}
			String[] smallunits = sLine.split("\\s+");
			if(smallunits.length == 2)
			{
				newdomainWordFreq.put(smallunits[0], Integer.parseInt(smallunits[1]));
				newdomainWordposs.put(smallunits[0], new TreeMap<String, Integer>());
				lastWord = smallunits[0];								
			}
			else if(smallunits.length == 4)
			{
				if(!smallunits[0].equals(lastWord))
				{
					System.out.println("error line : " + sLine);
				}
				
				newdomainWordposs.get(smallunits[0]).put(smallunits[0] + " " + smallunits[1] + " " + smallunits[2], Integer.parseInt(smallunits[3]));
			}
			else
			{
				System.out.println("error line : " + sLine);
			}
			
		}
		
		in.close();
		
		List<Entry<String, Integer>>  freqsortlexicon = KnowledgeExtractFromAutoSentences.MapIntSort(newdomainWordFreq);
		Set<String> newDomainWords = new TreeSet<String>();
		Set<String> newDomainWordPoss = new TreeSet<String>();
		Set<String> newCommonWordPoss = new TreeSet<String>();
		//int wordposAddedNum = 0;
		boolean bFull = false;
		for(Entry<String, Integer> theEntry : freqsortlexicon)
		{
			String theWord = theEntry.getKey();
			if(commonwords.contains(theWord))
			{
				for(String theWordPOS : newdomainWordposs.get(theWord).keySet())
				{
					if(!commonwordposs.contains(theWordPOS))
					{
						newCommonWordPoss.add(theWordPOS);
						//if(newDomainWordPoss.size() >= 3000)
						//{
						//	bFull = true;
						//	break;
						//}
					}
				}
			}
			else if(domainwords.contains(theWord))
			{
				for(String theWordPOS : newdomainWordposs.get(theWord).keySet())
				{
					if(!domainwordposs.contains(theWordPOS))
					{
						newDomainWordPoss.add(theWordPOS);
						//if(newDomainWordPoss.size() >= 3000)
						//{
						//	bFull = true;
						//	break;
						//}
					}
				}
			}
			else
			{
				newDomainWords.add(theWord);
				for(String theWordPOS : newdomainWordposs.get(theWord).keySet())
				{
					newDomainWordPoss.add(theWordPOS);
					//if(newDomainWordPoss.size() >= 3000)
					//{
					//	bFull = true;
					//	break;
					//}
				}
				
			}
			
			if(bFull)
			{
				break;
			}
		}
		
		System.out.println(newDomainWordPoss.size());
		System.out.println(newCommonWordPoss.size());
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"));
		output.println("word dictionary");
		for(String theKey : commonwords)
		{
			output.println(String.format("%s : 1", theKey));
		}

		for(String theKey : domainwords)
		{
			output.println(String.format("%s : 2",  theKey));
		}
		
		for(String theKey : newDomainWords)
		{
			output.println(String.format("%s : 2",  theKey));
		}
		
		output.println();
		output.println("word tag dictionary");	
		for(String thefirstKey : commonwordposs)
		{
			output.println(String.format("%s : 1",  thefirstKey));
		}
		for(String thefirstKey : newCommonWordPoss)
		{
			output.println(String.format("%s : 1",  thefirstKey));
		}
		
		for(String thefirstKey : domainwordposs)
		{
			output.println(String.format("%s : 2",  thefirstKey));
		}
		
		for(String thefirstKey : newDomainWordPoss)
		{
			output.println(String.format("%s : 2",  thefirstKey));
		}
		
		
		output.close();
		
		

	}

}
