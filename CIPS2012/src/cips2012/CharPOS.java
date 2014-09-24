package cips2012;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map;
import mason.utils.*;

public class CharPOS {

	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception{
		Map<String, Map<String, Integer>> charposlist = new TreeMap<String, Map<String, Integer>>();
		// TODO Auto-generated method stub
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] wordposs = sLine.trim().split("\\s+");
			for(int idx = 1; idx < wordposs.length; idx++)
			{
				int colonIndex = wordposs[idx].lastIndexOf(":");
				if(colonIndex == -1)continue;
				String pos =  wordposs[idx].substring(0, colonIndex);
				Integer score = 0;
				try
				{
					score = Integer.parseInt(wordposs[idx].substring(colonIndex+1));
				}
				catch (Exception e)
				{
					continue;
				}
				for(int idc = 0; idc < wordposs[0].length(); idc++)
				{
					String curChar = wordposs[0].substring(idc, idc+1);
					if(!charposlist.containsKey(curChar))
					{
						charposlist.put(curChar, new HashMap<String, Integer>());
					}
					if(!charposlist.get(curChar).containsKey(pos))
					{
						charposlist.get(curChar).put(pos, 0);
					}
					charposlist.get(curChar).put(pos, charposlist.get(curChar).get(pos) + score);
				}
			}
		}
		in.close();
		
		List<Entry<String, Map<String, Integer>>> chapossortlist = new ArrayList<Entry<String, Map<String, Integer>>>(charposlist.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.Compare((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		}); 
		
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		for(Entry<String, Map<String, Integer>> curCharPoslist: chapossortlist)
		{
			
			List<Entry<String, Integer>> poslist = new ArrayList<Entry<String, Integer>>(curCharPoslist.getValue().entrySet());
			Collections.sort(poslist, new Comparator(){   
				public int compare(Object o1, Object o2) {    
					Map.Entry obj1 = (Map.Entry) o1;
					Map.Entry obj2 = (Map.Entry) o2;
					return ((Integer) obj2.getValue()).compareTo((Integer)obj1.getValue());
	            }   
			});
			
			String outline = curCharPoslist.getKey();
			for (Entry<String, Integer> curPosInfo : poslist)
			{
				outline = outline + " " + curPosInfo.getKey() + ":" + curPosInfo.getValue();
			}
			writer.println(outline.trim());
		}
		
		writer.close();

 
		
		
	}

}
