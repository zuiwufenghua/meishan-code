package preparation;

import mason.utils.PinyinComparator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class OOVWordCorrectPredicateFeature {

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
		Map<String, Map<String, Double>> OOVWordRecoLeftOOV = new TreeMap<String, Map<String, Double>>();
		//iv left word length
		Map<String, Map<String, Double>> OOVWordRecoLeftIVLength = new TreeMap<String, Map<String, Double>>();
		// left iv word distribution
		Map<String, Map<String, Double>> OOVWordRecoLeftIVWordDistribution = new TreeMap<String, Map<String, Double>>();
		// left oov word distribution
		Map<String, Map<String, Double>> OOVWordRecoLeftOOVWordDistribution = new TreeMap<String, Map<String, Double>>();
		// left word pos distribution
		Map<String, Map<String, Double>> OOVWordRecoLeftPOSDistribution = new TreeMap<String, Map<String, Double>>();
		// left biword pos distribution
		Map<String, Map<String, Double>> OOVWordRecoLeftBIPOSDistribution = new TreeMap<String, Map<String, Double>>();
		//distribution of right word oov
		Map<String, Map<String, Double>> OOVWordRecoRightOOV = new TreeMap<String, Map<String, Double>>();
		//iv right word length
		Map<String, Map<String, Double>> OOVWordRecoRightIVLength = new TreeMap<String, Map<String, Double>>();
		// right word distribution
		Map<String, Map<String, Double>> OOVWordRecoRightIVWordDistribution = new TreeMap<String, Map<String, Double>>();
		// right word distribution
		Map<String, Map<String, Double>> OOVWordRecoRightOOVWordDistribution = new TreeMap<String, Map<String, Double>>();
		// right word pos distribution
		Map<String, Map<String, Double>> OOVWordRecoRightPOSDistribution = new TreeMap<String, Map<String, Double>>();
		// right biword pos distribution
		Map<String, Map<String, Double>> OOVWordRecoRightBIPOSDistribution = new TreeMap<String, Map<String, Double>>();
		//left right word bipos distribution
		Map<String, Map<String, Double>> OOVWordRecoLeftRightBIPOSDistribution = new TreeMap<String, Map<String, Double>>();
		// current word's frequency
		//Map<String, Double> OOVWordRecoFreq = new TreeMap<String, Double>();
		// current word's frequency
		Map<String, Double> OOVWordPOSRecoFreq = new TreeMap<String, Double>();
		// current word's pos tags
		Map<String, Map<String, Double>> OOVWordRecoPOSFreq = new TreeMap<String, Map<String, Double>>();
		
		while ((sLine = in.readLine()) != null) {
			String newLine = sLine.trim();
			if(newLine.equals(""))
			{
				continue;
			}
			String[] wordposivtags = newLine.split("\\s+");
			for(int idx = 0; idx < wordposivtags.length; idx++)
			{
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
					String[] curUnits = wordposivtags[idx].split("_");
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
					addElements(curUnits[0] + "_" + curUnits[1], left1Units[2], 1.0, OOVWordRecoLeftOOV);
					if(left1Units[2].equals("IV"))
					{
						addElements(curUnits[0] + "_" + curUnits[1], left1Units[2] + "@" + String.format("%d", Math.min(left1Units[0].length(), 6)), 1.0, OOVWordRecoLeftIVLength);
						if(left1Units[2].equals("IV"))addElements(curUnits[0] + "_" + curUnits[1], left1Units[0], 1.0, OOVWordRecoLeftIVWordDistribution);
						else addElements(curUnits[0] + "_" + curUnits[1], left1Units[0], 1.0, OOVWordRecoLeftOOVWordDistribution);
						addElements(curUnits[0] + "_" + curUnits[1], left1Units[2] + "@" + left1Units[1] + "_" + curUnits[1], 1.0, OOVWordRecoLeftPOSDistribution);
						if(left2Units[2].equals("IV"))
						{
							addElements(curUnits[0] + "_" + curUnits[1], left2Units[2] + "@" + left1Units[2] + "@" + left2Units[1] + "_" + left1Units[1] + "_" + curUnits[1], 1.0, OOVWordRecoLeftBIPOSDistribution);
						}
					}
					
					addElements(curUnits[0] + "_" + curUnits[1], right1Units[2], 1.0, OOVWordRecoRightOOV);
					if(right1Units[2].equals("IV"))
					{
						addElements(curUnits[0] + "_" + curUnits[1], right1Units[2] + "@" + String.format("%d", Math.min(right1Units[0].length(), 6)), 1.0, OOVWordRecoRightIVLength);
						if(right1Units[2].equals("IV"))addElements(curUnits[0] + "_" + curUnits[1], right1Units[2] + "@" + right1Units[0], 1.0, OOVWordRecoRightIVWordDistribution);
						else addElements(curUnits[0] + "_" + curUnits[1], right1Units[2] + "@" + right1Units[0], 1.0, OOVWordRecoRightOOVWordDistribution);
						addElements(curUnits[0] + "_" + curUnits[1], right1Units[2] + "@" + right1Units[1] + "_" + curUnits[1], 1.0, OOVWordRecoRightPOSDistribution);
						if(right2Units[2].equals("IV"))
						{
							addElements(curUnits[0] + "_" + curUnits[1], right2Units[2] + "@" + right1Units[2] + "@" + right2Units[1] + "_" + right1Units[1] + "_" + curUnits[1], 1.0, OOVWordRecoRightBIPOSDistribution);
						}
					}
					
					if(left1Units[2].equals("IV") && right1Units[2].equals("IV"))
					{
						addElements(curUnits[0] + "_" + curUnits[1], left1Units[2] + "@" + right1Units[2] + "@" + left1Units[1] + "_" + right1Units[1] + "_" + curUnits[1], 1.0, OOVWordRecoLeftRightBIPOSDistribution);
					}
					addElements(curUnits[0] + "_" + curUnits[1], 1.0, OOVWordPOSRecoFreq);
					//addElements(curUnits[0], 1.0, OOVWordRecoFreq);
					addElements(curUnits[0], curUnits[1], 1.0, OOVWordRecoPOSFreq);
				}
			}
		}
		in.close();
		
		Map<String, Map<String, Double>> wordposDict = null;
		
		if(args.length > 2)
		{
			wordposDict = new TreeMap<String, Map<String, Double>>();
			in = new BufferedReader(new InputStreamReader(new FileInputStream(args[2]), "UTF-8"));
			while ((sLine = in.readLine()) != null) {
				String newLine = sLine.trim();
				if(newLine.equals(""))
				{
					continue;
				}
				String[] wordposs = newLine.split("\\s+");
				for(String wordpos : wordposs)
				{
					String[] units = wordpos.split("_");
					if(units.length != 2)continue;
					addElements(units[0], units[1],  1.0, wordposDict);
				}
			}
			
			in.close();
		}
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"));
		
		for(String theWordPOS : OOVWordPOSRecoFreq.keySet())
		{
			if(OOVWordPOSRecoFreq.get(theWordPOS) < 4.0) continue;
			String[] theUnits = theWordPOS.split("_");
			String theWord = theUnits[0];
			String thePOS = theUnits[1];
			
			String outputlne = theWordPOS;
			if(wordposDict != null)
			{
				if(wordposDict.containsKey(theWord) && wordposDict.get(theWord).containsKey(thePOS)
				 && wordposDict.get(theWord).get(thePOS)> 1) //&& wordposDict.get(theWord).size() < 3
				{
					outputlne = "1";
				}
				else
				{
					outputlne = "0";
				}
			}
			//cur word pos freq
			//outputlne = outputlne + "\t" + "WordPOSFreq=" + gradeFreq(OOVWordPOSRecoFreq.get(theWordPOS));
			outputlne = outputlne + "\t" + "WordPOSFreq=" + gradeFreq(OOVWordPOSRecoFreq.get(theWordPOS)) + "@" + thePOS;
			//cur word pos freq
			//outputlne = outputlne + "\t" + "WordLength=" + String.format("%d", Math.min(theWord.length(), 6));
			//cur word pos freq
			outputlne = outputlne + "\t" + "WordLength=" + String.format("%d", Math.min(theWord.length(), 6)) + "@" + thePOS;
			//cur word's POS tags
			for(String theKey : OOVWordRecoPOSFreq.get(theWord).keySet())
			{
				//outputlne = outputlne + "\t" + "CurWordPOS=" + theKey + "@" + gradeFreq(OOVWordRecoPOSFreq.get(theWord).get(theKey));
				if(OOVWordRecoPOSFreq.get(theWord).get(theKey) > 3)
				{
					outputlne = outputlne + "\t" + "CurWordPOS=" + theKey + "@" + gradeFreq(OOVWordRecoPOSFreq.get(theWord).get(theKey)) + "@" + thePOS;
				}
			}
			// left word iv percentage
			Map<String, Double> LeftIVOOVRation = OOVWordRecoLeftOOV.get(theWordPOS);
			double ivLeftRation = 0.0;
			double iLeftTotalWords = 0.0;
			if(LeftIVOOVRation.containsKey("IV"))
			{
				ivLeftRation = LeftIVOOVRation.get("IV");
				iLeftTotalWords =  iLeftTotalWords + LeftIVOOVRation.get("IV");
			}
			if (LeftIVOOVRation.containsKey("OOV") )
			{
				iLeftTotalWords =  iLeftTotalWords + LeftIVOOVRation.get("OOV");
			}
			//left iv freq
			//outputlne = outputlne + "\t" + "LeftIVFreq=" + gradeFreq(ivLeftRation);
			outputlne = outputlne + "\t" + "LeftIVFreq=" + gradeFreq(ivLeftRation) + "@" + thePOS;
			//left iv perc
			ivLeftRation = ivLeftRation / iLeftTotalWords;
			//outputlne = outputlne + "\t" + "LeftIVPerc=" + gradePerc(ivLeftRation);
			//outputlne = outputlne + "\t" + "LeftIVPerc=" + gradePerc(ivLeftRation) + "@" + thePOS;

			Map<String, Double> leftWordLengthDistribution = OOVWordRecoLeftIVLength.get(theWordPOS);
			if(leftWordLengthDistribution != null)
			{
				for(String theKey : leftWordLengthDistribution.keySet())
				{
					// left iv word length
					//outputlne = outputlne + "\t" + "LeftIVLegnthFreq=" + theKey + "@" + gradeFreq(leftWordLengthDistribution.get(theKey));
					//outputlne = outputlne + "\t" + "LeftIVLegnthFreq=" + theKey + "@" + gradeFreq(leftWordLengthDistribution.get(theKey)) + "@" + String.format("%d", Math.min(theWord.length(), 6));
					//outputlne = outputlne + "\t" + "LeftIVLegnthFreq=" + theKey + "@" + gradeFreq(leftWordLengthDistribution.get(theKey)) + "@" + thePOS;
					//outputlne = outputlne + "\t" + "LeftIVLegnthFreq=" + theKey + "@" + gradeFreq(leftWordLengthDistribution.get(theKey)) + "@" + String.format("%d", Math.min(theWord.length(), 6)) + "@" + thePOS;
				}
			}
			Map<String, Double> leftIVWordDistribution = OOVWordRecoLeftIVWordDistribution.get(theWordPOS);
			if(leftIVWordDistribution!=null)
			{
				//outputlne = outputlne + "\t" + "LeftIVDifferentWordNum=" + gradeFreq(leftIVWordDistribution.size());
				outputlne = outputlne + "\t" + "LeftIVDifferentWordNum=" + gradeFreq(leftIVWordDistribution.size()) + "@" + thePOS;
			}
			//Map<String, Double> leftOOVWordDistribution = OOVWordRecoLeftOOVWordDistribution.get(theWordPOS);
			//if(leftOOVWordDistribution!=null)
			//{
				//outputlne = outputlne + "\t" + "LeftOOVDifferentWordNum=" + gradeFreq(leftOOVWordDistribution.size());
			//	outputlne = outputlne + "\t" + "LeftOOVDifferentWordNum=" + gradeFreq(leftOOVWordDistribution.size()) + "@" + thePOS;
			//}
			
			Map<String, Double> leftPOSDistribution = OOVWordRecoLeftPOSDistribution.get(theWordPOS);
			if( leftPOSDistribution != null){
				for(String theKey : leftPOSDistribution.keySet())
				{
					if(leftPOSDistribution.get(theKey) > 3)
					{
						outputlne = outputlne + "\t" + "LeftIVPOSNum=" + theKey + "@" + gradeFreq(leftPOSDistribution.get(theKey));
					}
				}
			}
			Map<String, Double> leftBiPOSDistribution = OOVWordRecoLeftBIPOSDistribution.get(theWordPOS);
			if(leftBiPOSDistribution != null)
			{
				for(String theKey : leftBiPOSDistribution.keySet())
				{
					if(leftBiPOSDistribution.get(theKey) > 3)
					{
						outputlne = outputlne + "\t" + "LeftIVBIPOSNum=" + theKey + "@" + gradeFreq(leftBiPOSDistribution.get(theKey));
					}
				}
			}
			
			// right word iv percentage
			Map<String, Double> RightIVOOVRation = OOVWordRecoRightOOV.get(theWordPOS);
			double ivRightRation = 0.0;
			double iRightTotalWords = 0.0;
			if(RightIVOOVRation.containsKey("IV"))
			{
				ivRightRation = RightIVOOVRation.get("IV");
				iRightTotalWords =  iRightTotalWords + RightIVOOVRation.get("IV");
			}
			if (RightIVOOVRation.containsKey("OOV") )
			{
				iRightTotalWords =  iRightTotalWords + RightIVOOVRation.get("OOV");
			}
			//right iv freq
			//outputlne = outputlne + "\t" + "RightIVFreq=" + gradeFreq(ivRightRation);
			outputlne = outputlne + "\t" + "RightIVFreq=" + gradeFreq(ivRightRation) + "@" + thePOS;
			//right iv perc
			ivRightRation = ivRightRation / iRightTotalWords;
			//outputlne = outputlne + "\t" + "RightIVPerc=" + gradePerc(ivRightRation);
			//outputlne = outputlne + "\t" + "RightIVPerc=" + gradePerc(ivRightRation) + "@" + thePOS;

			Map<String, Double> rightWordLengthDistribution = OOVWordRecoRightIVLength.get(theWordPOS);
			if(rightWordLengthDistribution != null)
			{
				for(String theKey : rightWordLengthDistribution.keySet())
				{
					// right iv word length
					//outputlne = outputlne + "\t" + "RightIVLegnthFreq=" + theKey + "@" + gradeFreq(rightWordLengthDistribution.get(theKey));
					//outputlne = outputlne + "\t" + "RightIVLegnthFreq=" + theKey + "@" + gradeFreq(rightWordLengthDistribution.get(theKey)) + "@" + String.format("%d", Math.min(theWord.length(), 6));
					//outputlne = outputlne + "\t" + "RightIVLegnthFreq=" + theKey + "@" + gradeFreq(rightWordLengthDistribution.get(theKey)) + "@" + thePOS;
					//outputlne = outputlne + "\t" + "RightIVLegnthFreq=" + theKey + "@" + gradeFreq(rightWordLengthDistribution.get(theKey)) + "@" + String.format("%d", Math.min(theWord.length(), 6)) + "@" + thePOS;
				}
			}
			Map<String, Double> rightIVWordDistribution = OOVWordRecoRightIVWordDistribution.get(theWordPOS);
			if(rightIVWordDistribution!=null)
			{
				//outputlne = outputlne + "\t" + "RightIVDifferentWordNum=" + gradeFreq(rightIVWordDistribution.size());
				outputlne = outputlne + "\t" + "RightIVDifferentWordNum=" + gradeFreq(rightIVWordDistribution.size()) + "@" + thePOS;
			}
			//Map<String, Double> rightOOVWordDistribution = OOVWordRecoRightOOVWordDistribution.get(theWordPOS);
			//if(rightOOVWordDistribution!=null)
			//{
				//outputlne = outputlne + "\t" + "RightOOVDifferentWordNum=" + gradeFreq(rightOOVWordDistribution.size());
			//	outputlne = outputlne + "\t" + "RightOOVDifferentWordNum=" + gradeFreq(rightOOVWordDistribution.size()) + "@" + thePOS;
			//}
			
			Map<String, Double> rightPOSDistribution = OOVWordRecoRightPOSDistribution.get(theWordPOS);
			if( rightPOSDistribution != null){
				for(String theKey : rightPOSDistribution.keySet())
				{
					if(rightPOSDistribution.get(theKey) > 3)
					{
						outputlne = outputlne + "\t" + "RightIVPOSNum=" + theKey + "@" + gradeFreq(rightPOSDistribution.get(theKey));
					}
				}
			}
			Map<String, Double> rightBiPOSDistribution = OOVWordRecoRightBIPOSDistribution.get(theWordPOS);
			if(rightBiPOSDistribution != null)
			{
				for(String theKey : rightBiPOSDistribution.keySet())
				{
					if(rightBiPOSDistribution.get(theKey) > 3)
					{
						outputlne = outputlne + "\t" + "RightIVBIPOSNum=" + theKey + "@" + gradeFreq(rightBiPOSDistribution.get(theKey));
					}
				}
			}
			
			Map<String, Double> leftrightBiPOSDistribution = OOVWordRecoLeftRightBIPOSDistribution.get(theWordPOS);
			if(leftrightBiPOSDistribution != null)
			{
				for(String theKey : leftrightBiPOSDistribution.keySet())
				{
					if(leftrightBiPOSDistribution.get(theKey) > 3)
					{
						outputlne = outputlne + "\t" + "LeftRightIVBIPOSNum=" + theKey + "@"  + gradeFreq(leftrightBiPOSDistribution.get(theKey));
					}
				}
			}
			
			output.println(outputlne);
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
		if(freq < 5)
		{
			return "VL";
		}
		else if(freq < 10)
		{
			return "L";
		}
		else
		{
			return "H";
		}
	}

}
