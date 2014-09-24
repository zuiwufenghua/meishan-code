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

public class WordPOSSelection {

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
		
		
		in = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(args[1]), "UTF-8"));
		
		Map<String, Map<String, Integer>> newdomainWordposs = new TreeMap<String, Map<String, Integer>>();
		Map<String, Integer> newdomainWordFreq = new TreeMap<String, Integer>();
		
		
		int allWordsNum = 0;		
		while ((sLine = in.readLine()) != null) {
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
				
				String wordKey ="[" + curUnits[0] + "]";
				String wordposKey = "[" + curUnits[0] + "] , " + curUnits[1];
				if(!commonwordposs.contains(wordposKey) && !domainwordposs.contains(wordposKey))
				{
					if(!newdomainWordFreq.containsKey(wordKey))
					{
						newdomainWordFreq.put(wordKey, 0);
						newdomainWordposs.put(wordKey, new TreeMap<String, Integer>());
					}
					
					if(!newdomainWordposs.get(wordKey).containsKey(wordposKey))
					{
						newdomainWordposs.get(wordKey).put(wordposKey, 0);
					}
					newdomainWordFreq.put(wordKey, newdomainWordFreq.get(wordKey)+1);
					newdomainWordposs.get(wordKey).put(wordposKey, newdomainWordposs.get(wordKey).get(wordposKey)+1);
				}
			}
		}
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"));
		List<Entry<String, Integer>>  freqsortlexicon = KnowledgeExtractFromAutoSentences.MapIntSort(newdomainWordFreq);
		for(int idx = 0; idx < freqsortlexicon.size(); idx++)
		{
			output.println(String.format("%s\t%d", freqsortlexicon.get(idx).getKey(), freqsortlexicon.get(idx).getValue()));
			List<Entry<String, Integer>> wordpossort = KnowledgeExtractFromAutoSentences.MapIntSort(newdomainWordposs.get(freqsortlexicon.get(idx).getKey()));
			for(int idy = 0; idy < wordpossort.size(); idy++)
			{
				if(wordpossort.get(idy).getValue() > 1)
				{
					output.println(String.format("%s\t%d", wordpossort.get(idy).getKey(), wordpossort.get(idy).getValue()));
				}
			}
			output.println();
		}

		output.close();
	}

}
