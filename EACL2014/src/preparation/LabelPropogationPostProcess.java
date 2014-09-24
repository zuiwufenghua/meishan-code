package preparation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class LabelPropogationPostProcess {

	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception{
		// TODO Auto-generated method stub
		
		Map<String, Map<String, Integer>> lexicon = new HashMap<String, Map<String, Integer>>();		
		Map<String, Integer> lexiconWordFreq = new HashMap<String, Integer>();
		
		
		Map<String, Set<String>> lexicon_left = new HashMap<String, Set<String>>();
		Map<String, Set<String>> lexicon_right = new HashMap<String, Set<String>>();
	
		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		int totalWordCount = 0;
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] wordposs = sLine.trim().split("\\s+");
			totalWordCount = totalWordCount + wordposs.length;
			for(int idx = 0; idx < wordposs.length; idx++)
			{
				String left1WordPOSTAG = "[START]_[START]";
				
				String right1WordPOSTAG = "[END]_[END]";
				if(idx > 0)left1WordPOSTAG = wordposs[idx-1];
				if(idx < wordposs.length -1)right1WordPOSTAG = wordposs[idx+1];
				if(idx < wordposs.length -2)right1WordPOSTAG = wordposs[idx+2];
				String[] left1Units = left1WordPOSTAG.split("_");
				String[] right1Units = right1WordPOSTAG.split("_");
				
				String leftchar = idx > 0 ? left1Units[0].substring(left1Units[0].length()-1) : "[START]";
				String rightchar = idx < wordposs.length -1 ? right1Units[0].substring(0,1) : "[END]";

				
				String wordpos = wordposs[idx];
				int splitIndex = wordpos.lastIndexOf("_");
				if(splitIndex == -1)
				{
					System.out.println(wordpos + "[in]" + sLine);
					continue;
				}
				String theWord = "[" + wordpos.substring(0, splitIndex) + "]";
				
				if(!lexiconWordFreq.containsKey(theWord))
				{
					lexiconWordFreq.put(theWord, 0);
				}
				lexiconWordFreq.put(theWord, lexiconWordFreq.get(theWord)+1);

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
				
				//if(!lexicon_new.containsKey(theWord))
				//{
				//	lexicon_new.put(theWord, new HashMap<String, Double>());
				//}
				//if(!lexicon_new.get(theWord).containsKey(thePOS))
				//{
				//	lexicon_new.get(theWord).put(thePOS, 0.0);
				//}
				
				
				if(!lexicon_left.containsKey(theWord))
				{
					lexicon_left.put(theWord, new HashSet<String>());
				}
				lexicon_left.get(theWord).add(leftchar);
				
				if(!lexicon_right.containsKey(theWord))
				{
					lexicon_right.put(theWord, new HashSet<String>());
				}
				lexicon_right.get(theWord).add(rightchar);
				

				
			}
			
		}
		
		in.close();
		
		in = new BufferedReader(new InputStreamReader(
					new FileInputStream(args[1]), "UTF8"));
		
		Map<String, Map<String, Double>> lexicon_lp = new HashMap<String, Map<String, Double>>();	
		while ((sLine = in.readLine()) != null) {
				if(sLine.trim().equals(""))continue;
				String[] wordposs = sLine.trim().split("\\s+");
				String theWord = wordposs[0].substring(1,  wordposs[0].length()-1);
				lexicon_lp.put(theWord, new HashMap<String, Double>());
				double lastProb = 0;
				double sumProb = 0.0;
				List<String> currentTypes = new ArrayList<String>();
				List<Double> currentTypeValues = new ArrayList<Double>();
				for(int idx = wordposs.length-3; idx >0; idx = idx -2)
				{
					double prob =  Double.parseDouble(wordposs[idx]);
					String type = wordposs[idx-1];
					if(prob < lastProb || lexicon_lp.get(theWord).containsKey(type))
					{
						break;
					}
					sumProb = sumProb + prob;
					//lexicon_lp.get(theWord).put(type, prob);
					currentTypes.add(type);
					currentTypeValues.add(prob);
					lastProb = prob;
				}
				
				int maxCand = 3;
				double regulize = 0.0;
				for(int idx = currentTypes.size() -1; idx >= 0 && idx >= currentTypes.size() - maxCand; idx--)
				{
					regulize += currentTypeValues.get(idx);
				}
				
				for(int idx = currentTypes.size() -1; idx >= 0 && idx >= currentTypes.size() - maxCand; idx--)
				{
					lexicon_lp.get(theWord).put(currentTypes.get(idx), currentTypeValues.get(idx)/regulize);
				}
		}
		
		in.close();
	
		
		Map<String, Map<String, Double>> lexicon_new = new HashMap<String, Map<String, Double>>();	
		
		for(String oneWord :  lexiconWordFreq.keySet())
		{
			String wordContent = oneWord.substring(1, oneWord.length()-1);
			lexicon_new.put(oneWord, new HashMap<String, Double>());
			for(String onePos : lexicon.get(oneWord).keySet())
			{
				boolean bTrueWord = true;
				double curLogProb = 0;
				for(int idx = 0; idx < wordContent.length(); idx ++)
				{
					String curType = "";
					Set<String> leftChars = null;
					if(idx == 0) 
					{
						curType = "B#" + onePos;
						leftChars = lexicon_left.get(oneWord);
					}
					else
					{
						curType = "M#" + onePos;
						leftChars = new HashSet<String>(); leftChars.add(wordContent.substring(idx-1, idx));
					}
					
					Set<String> rightChars = null;
					if(idx == wordContent.length() -1)
					{
						rightChars = lexicon_right.get(oneWord);
						if(curType.equals("B#" + onePos))
						{
							curType = "S#" + onePos;
						}
						else
						{
							curType = "E#" + onePos;
						}
					}
					else
					{
						
						rightChars = new HashSet<String>(); rightChars.add(wordContent.substring(idx+1, idx+2));
					}
					
					
					double sumProb = 0.0;
					int totalCount = 0;
					for(String leftChar : leftChars)
					{
						for(String rightChar : rightChars)
						{
							String theLPKey = leftChar + wordContent.substring(idx, idx+1) + rightChar ;
							if(lexicon_lp.containsKey(theLPKey) && lexicon_lp.get(theLPKey).containsKey(curType))
							{
								double cursubProb = lexicon_lp.get(theLPKey).get(curType);
								if(cursubProb > sumProb)  
								{
									sumProb = cursubProb;
								}
								totalCount++;
							}
							
						}
					}
					
					if(totalCount > 0)
					{
						curLogProb = curLogProb  +  Math.log(sumProb);
					}
					else
					{
						bTrueWord = false;
						break;
					}
				}
				
				if(bTrueWord)
				lexicon_new.get(oneWord).put(onePos, Math.exp(curLogProb));
			}
		}
		

		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"));
		
		for(String theKey : lexicon_new.keySet())
		{
			if(theKey.length() < 4)continue;
			Map<String, Double> wordposprob = lexicon_new.get(theKey);
			double sum = 0.0;
			for(String thePOS: wordposprob.keySet())
			{
				sum += wordposprob.get(thePOS);
			}
			if(sum > 0.00001)
			{
				output.println(String.format("%s\t%f", theKey, sum));
				List<Entry<String, Double>> wordfreqSort = KnowledgeCharacterPerxitySort.MapDoubleSort(wordposprob);
				for(Entry<String, Double> posprob : wordfreqSort)
				{	
					if(posprob.getValue()/sum > 0.0001)
					{
						output.println(String.format("%s\t%s\t%f", theKey, posprob.getKey(), posprob.getValue()));
					}
				}
			}
		}
		
				
		output.close();
	}

}
