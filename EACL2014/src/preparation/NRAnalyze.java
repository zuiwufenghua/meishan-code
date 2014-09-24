package preparation;

import mason.utils.PinyinComparator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class NRAnalyze {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		BufferedReader in = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(args[0]), "UTF-8"));
		String sLine = null;
		//distribution of left word oov

		
		Map<String, Integer> allWordPOSLAuxFreq = new TreeMap<String, Integer>();
		Map<String, Integer> allWordPOSRAuxFreq = new TreeMap<String, Integer>();
		Map<String, Integer> allWordPOSFreq = new TreeMap<String, Integer>();
		

	/*	
		Set<String> posBeforeClues = new TreeSet<String>();
		
		posBeforeClues.add("DT");
		//posBeforeClues.add("CD");
		//posBeforeClues.add("OD");
		//posBeforeClues.add("AS");
		posBeforeClues.add("BA");
		posBeforeClues.add("CC");
		//posBeforeClues.add("DEC");
		//posBeforeClues.add("DEV");
		//posBeforeClues.add("DEG");
		//posBeforeClues.add("DER");
		//posBeforeClues.add("ETC");
		//posBeforeClues.add("FW");
		//posBeforeClues.add("IJ");
		posBeforeClues.add("LB");
		//posBeforeClues.add("LC");
		//posBeforeClues.add("MSP");
		//posBeforeClues.add("ON");
		posBeforeClues.add("P");
		//posBeforeClues.add("PN");
		//posBeforeClues.add("PU");
		//posBeforeClues.add("SB");
		//posBeforeClues.add("SP");
		//posBeforeClues.add("VC");
		//posBeforeClues.add("VE");
		//posBeforeClues.add("[START]");
		//posBeforeClues.add("[END]");
		
		
		Set<String> posAfterClues = new TreeSet<String>();
		
		//posAfterClues.add("DT");
		//posAfterClues.add("CD");
		//posAfterClues.add("OD");
		//posAfterClues.add("AS");
		//posAfterClues.add("BA");
		posAfterClues.add("CC");
		//posAfterClues.add("DEC");
		//posAfterClues.add("DEV");
		posAfterClues.add("DEG");
		//posAfterClues.add("DER");
		posAfterClues.add("ETC");
		//posAfterClues.add("FW");
		//posAfterClues.add("IJ");
		//posAfterClues.add("LB");
		//posAfterClues.add("LC");
		//posAfterClues.add("MSP");
		//posAfterClues.add("ON");
		//posAfterClues.add("P");
		//posAfterClues.add("PN");
		//posAfterClues.add("PU");
		//posAfterClues.add("SB");
		//posAfterClues.add("SP");
		posAfterClues.add("VC");
		posAfterClues.add("VE");
		//posAfterClues.add("[START]");
		//posAfterClues.add("[END]");
		
*/		
		while ((sLine = in.readLine()) != null) {
			String newLine = sLine.trim();
			if(newLine.equals(""))
			{
				continue;
			}
			String[] wordposivtags = newLine.split("\\s+");
			for(int idx = 0; idx < wordposivtags.length; idx++)
			{
				String[] curUnits = wordposivtags[idx].split("_");
				
				if(wordposivtags[idx].endsWith("_OOV"))
				{
					String left1WordPOSTAG = "[START]_[START]_IV";
					String left2WordPOSTAG = "[START]_[START]_IV";
					
					String right1WordPOSTAG = "[END]_[END]_IV";
					String right2WordPOSTAG = "[END]_[END]_IV";
					if(idx > 0)left1WordPOSTAG = wordposivtags[idx-1];
					if(idx > 1)left2WordPOSTAG = wordposivtags[idx-2];
					if(idx < wordposivtags.length -1)right1WordPOSTAG = wordposivtags[idx+1];
					if(idx < wordposivtags.length -2)right1WordPOSTAG = wordposivtags[idx+2];
					String[] left1Units = left1WordPOSTAG.split("_");
					String[] left2Units = left2WordPOSTAG.split("_");
					String[] right1Units = right1WordPOSTAG.split("_");
					String[] right2Units = right2WordPOSTAG.split("_");
					
					if(left1Units.length != 3 || left2Units.length != 3
					|| right1Units.length != 3 || right2Units.length != 3
					|| curUnits.length != 3)
					{
						System.out.println("error _ num:\t" + left2WordPOSTAG + " " + left1WordPOSTAG + " "
								+ wordposivtags[idx] + " " + right1WordPOSTAG + " " + right2WordPOSTAG);
						continue;
					}
					if(!PinyinComparator.bAllChineseCharacter(curUnits[0]))
					{
						continue;
					}
					if(!curUnits[1].equals("NR"))continue;
					
					/*
					if(posBeforeClues.contains(left1Units[1])
					 )
					{
						addElements(curUnits[0] + "_" + curUnits[1], 1, allWordPOSLAuxFreq);
					}
					
					if(posAfterClues.contains(right1Units[1])
							 )
					{
						addElements(curUnits[0] + "_" + curUnits[1], 1, allWordPOSRAuxFreq);
					}
					*/
					addElements(curUnits[0] + "_" + curUnits[1], 1, allWordPOSFreq);
				}
			}
		}
		in.close();
		
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"));
		
		for(String theWordPOS : allWordPOSFreq.keySet())
		{
			//if(allWordPOSFreq.get(theWordPOS) < 15) continue;
			int leftAux = 0;
			if(allWordPOSLAuxFreq.containsKey(theWordPOS))leftAux = allWordPOSLAuxFreq.get(theWordPOS);
			int rightAux = 0;
			if(allWordPOSRAuxFreq.containsKey(theWordPOS))rightAux = allWordPOSRAuxFreq.get(theWordPOS);
								
			output.println(String.format("%s\t%d\t%d\t%d", theWordPOS, allWordPOSFreq.get(theWordPOS), 
					leftAux, rightAux));
		}
		
		
		output.close();
		

	}
	
	public  static void addElements(String key1, String key2, double value, Map<String, Map<String, Double>> curMap)
	{
		if(!curMap.containsKey(key1))
		{
			curMap.put(key1, new TreeMap<String, Double>());			
		}
		if(!curMap.get(key1).containsKey(key2))
		{
			curMap.get(key1).put(key2, 0.0);
		}
		curMap.get(key1).put(key2, curMap.get(key1).get(key2)+ value);
	}
	
	public static void addElements(String key, double value, Map<String, Double> curMap)
	{
		if(!curMap.containsKey(key))
		{
			curMap.put(key, 0.0);
		}
		curMap.put(key, curMap.get(key) + value);
	}
	
	public static void addElements(String key, int value, Map<String, Integer> curMap)
	{
		if(!curMap.containsKey(key))
		{
			curMap.put(key, 1);
		}
		curMap.put(key, curMap.get(key) + value);
	}
	
	public static String gradePerc(double ratio)
	{
		if (ratio < 0.7)
		{
			return "L";
		}
		else
		{
			return "H";
		}
	}
	
	public static String gradeFreq(double freq)
	{
		return String.format("%d", (int)(freq));
	}

}
