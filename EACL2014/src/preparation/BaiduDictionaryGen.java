package preparation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class BaiduDictionaryGen {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		
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
		
		
		Set<String> newdomainwords = new HashSet<String>();
		Set<String> newdomainwordposs = new HashSet<String>();
		
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[2]), "UTF8"));
		
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().length() < 2)continue;
			String[] theunits = sLine.trim().split("\\s+");
			if(lexicon.containsKey(theunits[0]) && !domainwords.contains(theunits[0]))
			{
				newdomainwords.add(theunits[0]);
				for(String thepos : lexicon.get(theunits[0]).keySet())
				{
					newdomainwordposs.add(thepos);
				}
			}			
		}
				
		in.close();
		
		
		
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
