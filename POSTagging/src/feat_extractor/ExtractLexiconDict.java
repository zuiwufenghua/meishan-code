package feat_extractor;

import mason.utils.PinyinComparator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ExtractLexiconDict {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Map<String, Map<String, Integer>> lexicon = new HashMap<String, Map<String, Integer>>();
		
		for(int i = 0; i < args.length -1; i++)
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(args[i]), "UTF8"));
			String sLine = null;
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
					String theWord = wordpos.substring(0, splitIndex);
					String thePOS = wordpos.substring(splitIndex+1);
					if(!lexicon.containsKey(theWord))
					{
						lexicon.put(theWord, new HashMap<String, Integer>());
					}
					if(!lexicon.get(theWord).containsKey(thePOS))
					{
						lexicon.get(theWord).put(thePOS, 0);
					}
					lexicon.get(theWord).put(thePOS, lexicon.get(theWord).get(thePOS)+1);						
				}
				
			}
			
			in.close();
		}
		
		List<Entry<String, Map<String, Integer>>> chapossortlist = new ArrayList<Entry<String, Map<String, Integer>>>(lexicon.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		}); 
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[args.length -1]), "UTF-8"), false);
		int iCount = 0;
		for(Entry<String, Map<String, Integer>> curCharPoslist: chapossortlist)
		{
			String theoutStr = curCharPoslist.getKey();
			for(String thePOS : curCharPoslist.getValue().keySet())
			{
				theoutStr = theoutStr + "\t" + String.format("%s:%d", thePOS, curCharPoslist.getValue().get(thePOS));
			}
			out.println(theoutStr);
			iCount++;
		}
		System.out.println(iCount);
		out.close();

	}

}
