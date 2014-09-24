package mason.corpus.tool;

import java.io.*;
import java.util.*;
import java.util.Map.*;

public class corpusbgram {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		int ngram = Integer.parseInt(args[1]);
		List<Map<String, Integer>> ngrams = new ArrayList<Map<String, Integer>>();		
		for(int i = 0; i < ngram; i++)
		{
			ngrams.add(new HashMap<String, Integer>());
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));		
		String sLine = null;
		
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] words = sLine.trim().split("\\s+");
			if(words.length == 0)continue;
			
			for(int i = 0; i < words.length; i++)
			{
				String curNGram = words[i];
				if(ngrams.get(0).containsKey(curNGram))
				{
					ngrams.get(0).put(curNGram, ngrams.get(0).get(curNGram)+1);
				}
				else
				{
					ngrams.get(0).put(curNGram, 1);
				}
				for(int j = 1; j < ngram && i+j < words.length; j++)
				{
					curNGram = curNGram + " " + words[i+j];
					if(ngrams.get(j).containsKey(curNGram))
					{
						ngrams.get(j).put(curNGram, ngrams.get(j).get(curNGram)+1);
					}
					else
					{
						ngrams.get(j).put(curNGram, 1);
					}
				}
			}
		}
		in.close();
		
		for(int i=0; i < ngram; i++)
		{
			PrintWriter output = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(String.format("%s.%d", args[2], i+1)), "UTF-8"));
			
			Map<String, Integer> ingram = ngrams.get(i);

			ArrayList<Entry<String,Integer>> ingramInfo = new ArrayList<Entry<String,Integer>>(ingram.entrySet());  
					
			Collections.sort(ingramInfo, new Comparator<Map.Entry<String, Integer>>() {   
			    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {       
			    	return (o2.getValue() - o1.getValue());
			    }
			}); 
					
			for(Entry<String,Integer> e : ingramInfo) {
				output.println(e.getKey() + "\t" + e.getValue());
			}
			
			output.close();
		}

	}

}
