package process;

import mason.utils.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

public class ExtractNewDictionary {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Set<String> commonwords = new TreeSet<String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
				args[0]), "UTF-8"));
		
		String sLine = null;
		
		while((sLine = reader.readLine()) != null) {
			String[] curUnits = sLine.trim().split("\\s+");
			for(String oneWord: curUnits)
			{
				commonwords.add(oneWord);
			}
			
		}
		reader.close();
		
		Map<String, Integer> domainWords = new HashMap<String, Integer>();
		
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(
				args[1]), "UTF-8"));
				
		while((sLine = reader.readLine()) != null) {
			String[] curUnits = sLine.trim().split("\\s+");
			for(String oneWord: curUnits)
			{
				if(!commonwords.contains(oneWord) && PinyinComparator.bAllChineseCharacter(oneWord))
				{
					if(!domainWords.containsKey(oneWord))
					{
						domainWords.put(oneWord, 0);
					}
					
					domainWords.put(oneWord, domainWords.get(oneWord)+1);
				}
			}
			
		}
		reader.close();
		
		
		List<Entry<String, Integer>> wordfreqSort = MapSort.MapIntegerSort(domainWords);
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"));
		
		for(Entry<String, Integer> curWordFreq: wordfreqSort)
		{
			if(curWordFreq.getValue() > 2)
			{
				output.println(curWordFreq.getKey());
			}
		}
		
		
		output.close();
		

	}

}
