package mason.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MapSort {

	/**
	 * @param args
	 */
	
	public static List<Entry<String, Double>> MapDoubleSort(Map<String, Double> input)
	{
		List<Entry<String, Double>> mapintsort = new ArrayList<Entry<String, Double>>(input.entrySet());
		
		Collections.sort(mapintsort, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				Double a1 = (Double)obj1.getValue();
				Double a2 = (Double)obj2.getValue();
				
				return a2.compareTo(a1);				
            }   
		}); 
		
		return mapintsort;
	}
	
	
	public static List<Entry<String, Integer>> MapIntegerSort(Map<String, Integer> input)
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
