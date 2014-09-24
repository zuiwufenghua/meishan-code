package preparation;

import mason.utils.PinyinComparator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class KnowledgeExtractFromAutoSentences {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		
		Map<String, Integer> commonwords = new TreeMap<String, Integer>();
		Map<String, Integer> commonwordposnos = new TreeMap<String, Integer>();
		
		Map<String, Map<String, Integer>> commonwordposs = new TreeMap<String, Map<String, Integer>>();
		
		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
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
				commonwords.put(firstUnit, Integer.parseInt(secondunit));	
			}
			if(bWordPOS)
			{
				int lastmaohaoIndex = sLine.lastIndexOf(":");
				if(lastmaohaoIndex == -1)continue;
				String firstUnit = sLine.substring(0, lastmaohaoIndex).trim();
				String secondunit = sLine.substring(lastmaohaoIndex + 1).trim();
				commonwordposnos.put(firstUnit, Integer.parseInt(secondunit));
			}
		}
		in.close();
		// TODO Auto-generated method stub
		in = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(args[1]), "UTF-8"));
		
		Map<String, Integer> domainWords = new TreeMap<String, Integer>();
		Map<String, Map<String, Integer>> domainWordposs = new TreeMap<String, Map<String, Integer>>();
		

		
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

				
				if(!commonwords.containsKey("["+curUnits[0] +"]"))
				{	
					if(!PinyinComparator.bAllChineseCharacter(curUnits[0]))
					{
						continue;
					}
					int saveId = curUnits[0].length();
					if(saveId > 1)
					{
						if(!domainWords.containsKey("["+curUnits[0] +"]"))
						{
							domainWords.put("["+curUnits[0] +"]", 0);
						}
						domainWords.put("["+curUnits[0] +"]", domainWords.get("["+curUnits[0] +"]")+1);		

						
						if(!domainWordposs.containsKey("["+curUnits[0] +"]"))
						{
							domainWordposs.put("["+curUnits[0] +"]", new TreeMap<String, Integer>());
						}
						
						if(!domainWordposs.get("["+curUnits[0] +"]").containsKey("["+curUnits[0] +"] , " + curUnits[1]))
						{
							domainWordposs.get("["+curUnits[0] +"]").put("["+curUnits[0] +"] , " + curUnits[1], 0);
						}
						domainWordposs.get("["+curUnits[0] +"]").put("["+curUnits[0] +"] , " + curUnits[1], domainWordposs.get("["+curUnits[0] +"]").get("["+curUnits[0] +"] , " + curUnits[1])+1);		
						
						
					}
									
				}				
				else
				{
					if(!commonwordposs.containsKey("["+curUnits[0] +"]"))
					{
						commonwordposs.put("["+curUnits[0] +"]", new TreeMap<String, Integer>());
					}
					
					if(!commonwordposs.get("["+curUnits[0] +"]").containsKey("["+curUnits[0] +"] , " + curUnits[1]))
					{
						commonwordposs.get("["+curUnits[0] +"]").put("["+curUnits[0] +"] , " + curUnits[1], 0);
					}
					commonwordposs.get("["+curUnits[0] +"]").put("["+curUnits[0] +"] , " + curUnits[1], commonwordposs.get("["+curUnits[0] +"]").get("["+curUnits[0] +"] , " + curUnits[1])+1);	
				}
			}
			
		}
		
		in.close();
		
		int threshold = 1;
		//int threshold = allWordsNum/20000+3;
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"));
		output.println("word dictionary");
		for(String theKey : commonwords.keySet())
		{
			output.println(String.format("%s : 1", theKey));
		}

		for(String theKey : domainWords.keySet())
		{
			if(domainWords.get(theKey) < threshold) continue;
			output.println(String.format("%s : 2",  theKey));
		}
		
		output.println();
		output.println("word tag dictionary");	
		for(String thefirstKey : commonwordposnos.keySet())
		{
			//for(String theKey : commonwordposs.get(thefirstKey).keySet())
			//{
			//	if(commonwordposs.get(thefirstKey).get(theKey) < threshold) continue;
				output.println(String.format("%s : 1",  thefirstKey));
			//}
		}
		for(String thefirstKey : domainWordposs.keySet())
		{
			for(String theKey : domainWordposs.get(thefirstKey).keySet())
			{
				if(domainWordposs.get(thefirstKey).get(theKey) < threshold) continue;
				output.println(String.format("%s : 2",  theKey));
			}
		}
		
		/*
		output.println();
		output.println("word tag priority dictionary");	
		
		for(String thefirstKey : commonwordposs.keySet())
		{
			List<Entry<String, Integer>> temp = MapIntSort(commonwordposs.get(thefirstKey));
			for(int idx = 0; idx < temp.size(); idx++)
			{
				Entry<String, Integer> theEntry = temp.get(idx);
				String theKey = theEntry.getKey();
				int theValue  = theEntry.getValue();
				if(theValue < threshold) continue;
				output.println(String.format("%s : %d",  theKey, (idx+1)*10 + 1));
			}
		}
		for(String thefirstKey : domainWordposs.keySet())
		{
			List<Entry<String, Integer>> temp = MapIntSort(domainWordposs.get(thefirstKey));
			for(int idx = 0; idx < temp.size(); idx++)
			{
				Entry<String, Integer> theEntry = temp.get(idx);
				String theKey = theEntry.getKey();
				int theValue  = theEntry.getValue();
				if(theValue < threshold) continue;
				output.println(String.format("%s : %d",  theKey, (idx+1)*10 + 2));
			}
		}
*/
		output.close();		
		

	}
	
	
	public static List<Entry<String, Integer>> MapIntSort(Map<String, Integer> input)
	{
		List<Entry<String, Integer>> mapintsort = new ArrayList<Entry<String, Integer>>(input.entrySet());
		
		Collections.sort(mapintsort, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				Integer a1 = (Integer)obj1.getValue();
				Integer a2 = (Integer)obj2.getValue();
				
				return a2.compareTo(a1);				
            }   
		}); 
		
		return mapintsort;
	}

}
