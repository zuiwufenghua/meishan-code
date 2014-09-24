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

public class HMMDictionary {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
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
		
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[1]), "UTF-8"));

		String lastPOS = "";
		Map<String, Map<String, Double>> newdomainWordposs = new TreeMap<String, Map<String, Double>>();
		Map<String, Double> newdomainWordFreq = new TreeMap<String, Double>();
		while ((sLine = in.readLine()) != null) {
			String[] units =  sLine.split("\\s+");
			if(units.length != 3 || !units[1].equals("->"))
			{
				lastPOS = "";
				continue;				
			}
				
			if(units[2].equals("DefaultedMultinomial"))
			{
				lastPOS = units[0];
			}
			
			if(!lastPOS.equals("") && !units[2].equals("DefaultedMultinomial"))
			{
				String wordKey = "[" +units[0]+ "]";
				String wordposKey = "[" + units[0] + "] , " + lastPOS;
				double freq = Double.parseDouble(units[2]);
				if(!newdomainWordFreq.containsKey(wordKey))
				{
					newdomainWordFreq.put(wordKey, 0.0);
					newdomainWordposs.put(wordKey, new TreeMap<String, Double>());
				}
				
				if(!newdomainWordposs.get(wordKey).containsKey(wordposKey))
				{
					newdomainWordposs.get(wordKey).put(wordposKey, 0.0);
				}
				newdomainWordFreq.put(wordKey, newdomainWordFreq.get(wordKey)+freq);
				newdomainWordposs.get(wordKey).put(wordposKey, newdomainWordposs.get(wordKey).get(wordposKey)+freq);
				
			}

		}
		
		in.close();
		
		
		Map<String, Map<String, Integer>> lexicon = new TreeMap<String, Map<String, Integer>>();
		
		int allWordsNum = 0;
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[2]), "UTF8"));
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
				String theWord = "[" + wordpos.substring(0, splitIndex) + "]";
				String thePOS = theWord + " , " + wordpos.substring(splitIndex+1);
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
		
		Set<String> refinedcommonwords = new TreeSet<String>();
		Set<String> refinedcommonwordposs = new TreeSet<String>();	
		Set<String> newdomainwords = new TreeSet<String>();
		Set<String> newdomainwordposs = new TreeSet<String>();	
		

		
		List<Entry<String, Double>> wordfreqSort = KnowledgeCharacterPerxitySort.MapDoubleSort(newdomainWordFreq);
		for(Entry<String, Double> theWordFreq : wordfreqSort)
		{
			String theWord = theWordFreq.getKey();
			double theFreq = theWordFreq.getValue();
			//if(theFreq < 50)
			/*
			boolean bValidWord = true;
			if(theWord.length()>6)
			{
				bValidWord = false;				
			}
			if(theWord.length()==5)
			{
				if(domainwords.contains(theWord.substring(0,3) + "]") || commonwords.contains(theWord.substring(0,3) + "]"))bValidWord = false;	
				if(domainwords.contains("[" +theWord.substring(2)) || commonwords.contains("[" +theWord.substring(2)))bValidWord = false;	
			}
			
			if(theWord.length()==6)
			{
				if(domainwords.contains(theWord.substring(0,4) + "]") || commonwords.contains(theWord.substring(0,4) + "]"))bValidWord = false;	
				if(domainwords.contains("[" +theWord.substring(2)) || commonwords.contains("[" +theWord.substring(2)))bValidWord = false;	
				
				if(domainwords.contains(theWord.substring(0,3) + "]") || commonwords.contains(theWord.substring(0,3) + "]"))bValidWord = false;	
				if(domainwords.contains("[" +theWord.substring(3)) || commonwords.contains("[" +theWord.substring(3)))bValidWord = false;
				if(domainwords.contains("[" +theWord.substring(2,4) + "]") || commonwords.contains("[" +theWord.substring(2,4) + "]"))bValidWord = false;
			}
			if(theWord.length()==1) bValidWord = false;
			*/	
			List<Entry<String, Double>> wordposfreqSort = KnowledgeCharacterPerxitySort.MapDoubleSort(newdomainWordposs.get(theWord));
			for(Entry<String, Double> theWordPOSFreq : wordposfreqSort)
			{
				double prob = theWordPOSFreq.getValue()/theFreq;
				double curPosFreq = theWordPOSFreq.getValue();
				if(prob < 0.0-1e-20)continue;
				String theWordpPOS = theWordPOSFreq.getKey();
				//String theOurput = String.format("%s : %f\t%f", theWordPOSFreq.getKey(), prob,theFreq);
				//output.println(theOurput);
				if(commonwords.contains(theWord) && commonwordposs.contains(theWordpPOS))
				{
					refinedcommonwords.add(theWord);
					refinedcommonwordposs.add(theWordpPOS);
				}
				if(!commonwords.contains(theWord) && !domainwords.contains(theWord) && lexicon.containsKey(theWord)
						&&  theWord.length() > 3 && theWord.length() < 7 && lexicon.get(theWord).containsKey(theWordpPOS)
						&& theFreq > 5)
				{
						newdomainwords.add(theWord);
						newdomainwordposs.add(theWordpPOS);
				}
			}
		}
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[3]), "UTF-8"));
		output.println("word dictionary");
		for(String theKey : commonwords)
		{
			output.println(String.format("%s : 1", theKey));
		}

		for(String theKey : domainwords)
		{
			output.println(String.format("%s : 2",  theKey));
		}
		for(String theKey : newdomainwords)
		{
			output.println(String.format("%s : 2",  theKey));
		}
		
		output.println();
		output.println("word tag dictionary");	
		for(String thefirstKey : commonwordposs)
		{
			output.println(String.format("%s : 1",  thefirstKey));
		}
		for(String thefirstKey : domainwordposs)
		{
			output.println(String.format("%s : 2",  thefirstKey));
		}
		for(String thefirstKey : newdomainwordposs)
		{
			output.println(String.format("%s : 2",  thefirstKey));
		}
				
		output.close();
	}

}
