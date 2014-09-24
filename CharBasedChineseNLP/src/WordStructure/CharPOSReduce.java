package WordStructure;

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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.TreeMap;
import java.util.Map.Entry;

public class CharPOSReduce {

	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception{
		// TODO Auto-generated method stub
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		Map<String, TreeSet<String>> charPOS = new TreeMap<String, TreeSet<String>>();
		//Set<String> poss = new HashSet<String>();
		TreeSet<String> poss=new TreeSet<String>(new Comparator(){
			public int compare(Object obj1, Object obj2) {
				return PinyinComparator.Compare((String) obj1, (String) obj2);
			}
		});
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] wordposs = sLine.trim().split("\\s+");
			if(wordposs.length < 2) continue;
			if(wordposs[0].length() > 1)continue;
			charPOS.put(wordposs[0], new TreeSet<String>(new Comparator(){
				public int compare(Object obj1, Object obj2) {
					return PinyinComparator.Compare((String) obj1, (String) obj2);
				}
			})
			);
			for(int idx = 1; idx < wordposs.length; idx++)
			{
				if(wordposs[idx].length() > 1)
				{
					System.out.println(wordposs[idx]);
				}
				else if(wordposs[idx].length() == 1)
				{
					charPOS.get(wordposs[0]).add(wordposs[idx]);
					poss.add(wordposs[idx]);
				}
			}
		}
		
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		for(String curPOS : poss)
		{
			writer.println(curPOS);
		}
		writer.close();
		
		
		List<Entry<String, TreeSet<String>>> chapossortlist = new ArrayList<Entry<String, TreeSet<String>>>(charPOS.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.Compare((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		}); 
		
		writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);
		
		for(Entry<String, TreeSet<String>> curCharPoslist: chapossortlist)
		{			
			String outline = curCharPoslist.getKey();
			for (String curPosInfo : curCharPoslist.getValue())
			{
				outline = outline + " " + curPosInfo;
			}
			writer.println(outline.trim());
		}
		
		writer.close();
	}
}
