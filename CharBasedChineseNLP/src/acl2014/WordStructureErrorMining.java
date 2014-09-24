package acl2014;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

public class WordStructureErrorMining {

	public static void main(String[] args) throws Exception {
	
		int startarg = 0;		
		CharDependencyReader cdpCorpusReader1 = new CharDependencyReader();
		cdpCorpusReader1.Init(args[startarg]);
		CharDependencyReader cdpCorpusReader2 = new CharDependencyReader();
		cdpCorpusReader2.Init(args[startarg+1]);
		
		PrintWriter writer  = new PrintWriter(System.out);
		if(args.length > startarg+2)
		{
			writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[startarg+2]), "UTF-8"));
		}
		
		analysisDep(cdpCorpusReader1.m_vecInstances, cdpCorpusReader2.m_vecInstances,  writer);

		writer.close();
	}
	
	public static void analysisDep(List<CharDependency> vecInstances1, List<CharDependency> vecInstances2, PrintWriter output) throws Exception {

		int totalInstances = vecInstances1.size();
		if(totalInstances != vecInstances2.size()) 
		{
			output.println("Sentence Num do not match.");
			output.close();
			return;
		}
		
		Map<String, Map<String, Integer>> analysisResult = new TreeMap<String, Map<String, Integer>>(
				new Comparator<String>(){  
					public int compare(String o1, String o2) {                
						return o1.compareTo(o2);  
					}     
        });
		
		int i = 0;
		for (; i < totalInstances; i++) {
			CharDependency tmpInstance = vecInstances1.get(i);
			tmpInstance.init();
			CharDependency other = vecInstances2.get(i);
			other.init();

			Map<String, Map<String, Integer>> analysisResultpart = new TreeMap<String, Map<String, Integer>>(
					new Comparator<String>(){  
						public int compare(String o1, String o2) {                
							return o1.compareTo(o2);  
						}     
	        });
			
			if(!tmpInstance.segmentedCorrectStructureWrong(other, analysisResultpart))
			{
				output.println(String.format("Sentence %d is not matched.", i+1));
				output.close();
				return;
			}
			
			for(String theKey : analysisResultpart.keySet())
			{
				if(!analysisResult.containsKey(theKey))
				{
					analysisResult.put(theKey, new HashMap<String, Integer>());
				}
				
				for(String theSKey : analysisResultpart.get(theKey).keySet())
				{
					if(!analysisResult.get(theKey).containsKey(theSKey))
					{
						analysisResult.get(theKey).put(theSKey, 0);
					}
					analysisResult.get(theKey).put(theSKey, analysisResult.get(theKey).get(theSKey)+1);
				}
			}
		}
		
		List<Entry<String, Map<String, Integer>>> chapossortlist = new ArrayList<Entry<String, Map<String, Integer>>>(analysisResult.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		});
		
		for(Entry<String, Map<String, Integer>> curCharPoslist: chapossortlist)
		{
			List<Entry<String, Integer>> synsortlist = new ArrayList<Entry<String, Integer>>(curCharPoslist.getValue().entrySet());
			
			Collections.sort(synsortlist, new Comparator(){   
				public int compare(Object o1, Object o2) {    
					Map.Entry obj1 = (Map.Entry) o1;
					Map.Entry obj2 = (Map.Entry) o2;
					Integer s1 = (Integer) obj1.getValue();
					Integer s2 = (Integer) obj1.getValue();
					return s1.compareTo(s2)	;
	            }   
			});
			
			if(synsortlist.size() > 1)
			{
				output.print(curCharPoslist.getKey());
				for(Entry<String, Integer> curSyn: synsortlist)
				{
					output.print("\t" + curSyn.getKey() + String.format("[%d]", curSyn.getValue()));
				}
				
				output.println();
			}
		}
		
	}

}
