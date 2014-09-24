package preparation;

import mason.utils.PinyinComparator;

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
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

public class LabelPropagationDictGen {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub

		Map<String, Integer> commonwords = new TreeMap<String, Integer>();
		Map<String, Integer> commonwordposs = new TreeMap<String, Integer>();	
		Map<String, Integer> domainwords = new TreeMap<String, Integer>();
		Map<String, Integer> domainwordposs = new TreeMap<String, Integer>();		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8"));

		String sLine = null;
		
		boolean bWord = false;
		boolean bWordPOS = false;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().length() < 3)continue;
			sLine = sLine.trim();
			if(sLine.indexOf("word dictionary") != -1)
			{
				bWord = true;
				bWordPOS = false;
			}
			
			if(sLine.indexOf("word tag dictionary") != -1)
			{
				bWord = false;
				bWordPOS = true;
			}
			if(bWord)
			{
				int lastmaohaoIndex = sLine.lastIndexOf(":");
				if(lastmaohaoIndex == -1)continue;
				String firstUnit = sLine.substring(0, lastmaohaoIndex).trim();
				String secondunit = sLine.substring(lastmaohaoIndex + 1).trim();
				int wordproperty = Integer.parseInt(secondunit);
				if(wordproperty%10 == 1)
				{
					commonwords.put(firstUnit, wordproperty);	
				}
				else if(wordproperty%10 == 2)
				{
					domainwords.put(firstUnit, wordproperty);
				}
			}
			if(bWordPOS)
			{
				int lastmaohaoIndex = sLine.lastIndexOf(":");
				if(lastmaohaoIndex == -1)continue;
				String firstUnit = sLine.substring(0, lastmaohaoIndex).trim();
				String secondunit = sLine.substring(lastmaohaoIndex + 1).trim();
				int wordproperty = Integer.parseInt(secondunit);
				if(wordproperty%10 == 1)
				{
					commonwordposs.put(firstUnit, wordproperty);
				}
				else if(wordproperty%10 == 2)
				{
					domainwordposs.put(firstUnit, wordproperty);
				}
				
			}
		}
		
		in.close();
		
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[1]), "UTF8"));
		
		Map<String, Map<String, Double>> lexicon_lp = new HashMap<String, Map<String, Double>>();
		
		Set<String> bigrams = new HashSet<String>();
		while ((sLine = in.readLine()) != null) {
				if(sLine.trim().equals(""))continue;
				String[] wordposs = sLine.trim().split("\\s+");
				String theWord = wordposs[0].substring(1,  wordposs[0].length()-1);
				
				if(theWord.endsWith("[END]"))
				{
					bigrams.add(theWord.substring(0, theWord.length()-5));
				}
				else
				{
					bigrams.add(theWord.substring(0, theWord.length()-1));
				}
				
				if(theWord.startsWith("[START]"))
				{
					bigrams.add(theWord.substring(7));
				}
				else
				{
					bigrams.add(theWord.substring(1));
				}
				
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
				
				int maxCand = 5;
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
		
		
		
		Map<String, Set<String>> lexicon_end = new HashMap<String,  Set<String>>();
		Map<String, Set<String>> lexicon_start = new HashMap<String,  Set<String>>();
		
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[2]), "UTF8"));
		
		while ((sLine = in.readLine()) != null) {
			String newLine = sLine.trim();
			if(newLine.equals(""))
			{
				continue;
			}
			String newnormline = newLine.trim().replace("\\s+", "");
			for(int idx = 0; idx < newnormline.length(); idx++)
			{
				
				String left1word = "[START]";	
				String right1word = "[END]";
				String right2word = "[END]";
				String right3word = "[END]";
				String right4word = "[END]";
				if(idx > 0)left1word = newnormline.substring(idx-1, idx);
				if(idx < newnormline.length() -1)right1word = newnormline.substring(idx+1, idx+2);
				if(idx < newnormline.length() -2)right2word = newnormline.substring(idx+2, idx+3);
				if(idx < newnormline.length() -3)right3word = newnormline.substring(idx+3, idx+4);
				if(idx < newnormline.length() -4)right4word = newnormline.substring(idx+4, idx+5);
				String leftChar = left1word;
				String rightChar = "";
				String curword = newnormline.substring(idx, idx+1);
				
				//biword
				if(idx < newnormline.length() -1)
				{
					String newWord = curword + right1word;
					if(bigrams.contains(newWord))
					{
						rightChar = right2word;
						if(!lexicon_start.containsKey(newWord))
						{
							lexicon_start.put(newWord, new TreeSet<String>());
						}
						lexicon_start.get(newWord).add(leftChar );
						
						if(!lexicon_end.containsKey(newWord))
						{
							lexicon_end.put(newWord, new TreeSet<String>());
						}
						lexicon_end.get(newWord).add(rightChar );
					}
				}
				
				if(idx < newnormline.length() -2)
				{
					String newWord = curword + right1word + right2word;
					if(lexicon_lp.containsKey(newWord))
					{
						rightChar = right3word;
						if(!lexicon_start.containsKey(newWord))
						{
							lexicon_start.put(newWord, new TreeSet<String>());
						}
						lexicon_start.get(newWord).add(leftChar );
						
						if(!lexicon_end.containsKey(newWord))
						{
							lexicon_end.put(newWord, new TreeSet<String>());
						}
						lexicon_end.get(newWord).add(rightChar );
					}
				}
				
				if(idx < newnormline.length() -3)
				{
					String newWord = curword + right1word + right2word +right3word;
					if(lexicon_lp.containsKey(curword + right1word + right2word)
						&& lexicon_lp.containsKey(right1word + right2word +right3word)
						)
					{
						rightChar = right4word;
						if(!lexicon_start.containsKey(newWord))
						{
							lexicon_start.put(newWord, new TreeSet<String>());
						}
						lexicon_start.get(newWord).add(leftChar );
						
						if(!lexicon_end.containsKey(newWord))
						{
							lexicon_end.put(newWord, new TreeSet<String>());
						}
						lexicon_end.get(newWord).add(rightChar );
					}
				}
			}
		}
		
		
		
		String[] thePoss = {"NN", "VV",
				   "NR", "AD",
				   "P", "CD", "M", "JJ",
				   "DEC", "DEG",
				   "NT", "CC", "VA", "LC",
				   "PN", "DT", "VC", "AS", "VE",
				   "OD", "IJ","ON",
				   "ETC", "MSP", "CS", "BA",
				   "DEV", "SB", "SP", "LB",
				   "FW", "DER", "PU"};
		//bigram
		Map<String, Map<String, Double>> lexicon_new = new HashMap<String, Map<String, Double>>();	
		
		for(String oneWord :  lexicon_start.keySet())
		{
			String wordContent = oneWord;
			if(!PinyinComparator.bAllChineseCharacter(wordContent))
			{
				continue;
			}
			lexicon_new.put(wordContent, new HashMap<String, Double>());
			for(String onePos : thePoss)
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
						leftChars = lexicon_start.get(wordContent);
					}
					else
					{
						curType = "M#" + onePos;
						leftChars = new HashSet<String>(); leftChars.add(wordContent.substring(idx-1, idx));
					}
					
					Set<String> rightChars = null;
					if(idx == wordContent.length() -1)
					{
						rightChars = lexicon_end.get(wordContent);
						if(curType.equals("B#" + onePos))
						{
							curType = "S#" + onePos;
							System.out.println("Impossible.......");
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
					
					
					if(leftChars == null || rightChars == null)
					{
						continue;
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
								//if(cursubProb > sumProb)  
								{
									sumProb += cursubProb;
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
				lexicon_new.get(wordContent).put(onePos, Math.exp(curLogProb));
			}
		}
		
		
		

		
		
		Map<String, Integer> domain2words = new TreeMap<String, Integer>();
		Map<String, Integer> domain2wordposs = new TreeMap<String, Integer>();
		
		Map<String, Double> domain2wordsvalue = new TreeMap<String, Double>();
		Map<String, Double> domain2wordpossvalue = new TreeMap<String, Double>();
		for(String theKey : lexicon_new.keySet())
		{
			//if(theKey.length() < 4)continue;
			String thenewKey = "[" + theKey + "]";
			if(commonwords.containsKey(thenewKey) || domainwords.containsKey(thenewKey))continue;
			Map<String, Double> wordposprob = lexicon_new.get(theKey);
			double sum = 0.0;
			for(String thePOS: wordposprob.keySet())
			{
				sum += wordposprob.get(thePOS);
			}
			if(sum > 0.0001)
			{
				//output.println(String.format("%s\t%f", theKey, sum));
				domain2words.put(thenewKey, 1013);
				domain2wordsvalue.put(theKey, sum);
				List<Entry<String, Double>> wordfreqSort = KnowledgeCharacterPerxitySort.MapDoubleSort(wordposprob);
				for(Entry<String, Double> posprob : wordfreqSort)
				{	
					if(posprob.getValue()/sum > 0.0001)
					{
						String wordposKey = "[" + theKey + "] , " + posprob.getKey();
						domain2wordposs.put(wordposKey, 1013);
						domain2wordpossvalue.put(theKey + "_" + posprob.getKey(), posprob.getValue());
						//output.println(String.format("%s\t%s\t%f", theKey, posprob.getKey(), posprob.getValue()));
					}
				}
			}
		}
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[3]), "UTF-8"));
		
		output.println("word dictionary");
		for(String theKey : commonwords.keySet())
		{
			output.println(String.format("%s : %d", theKey, commonwords.get(theKey)));
		}

		for(String theKey : domainwords.keySet())
		{
			output.println(String.format("%s : %d",  theKey, domainwords.get(theKey)));
		}
		
		for(String theKey : domain2words.keySet())
		{
			output.println(String.format("%s : %d",  theKey, domain2words.get(theKey)));
		}
			
		output.println();
		output.println("word tag dictionary");
		for(String theKey : commonwordposs.keySet())
		{
			output.println(String.format("%s : %d", theKey, commonwordposs.get(theKey)));
		}

		for(String theKey : domainwordposs.keySet())
		{
			output.println(String.format("%s : %d",  theKey, domainwordposs.get(theKey)));
		}
		
		for(String theKey : domain2wordposs.keySet())
		{
			output.println(String.format("%s : %d",  theKey, domain2wordposs.get(theKey)));
		}
		
		output.close();
		
		output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[4]), "UTF-8"));
		List<Entry<String, Double>> wordfreqSort = KnowledgeCharacterPerxitySort.MapDoubleSort(domain2wordsvalue);
		
		output.println("word dictionary");
		for(Entry<String, Double> theKeyValue : wordfreqSort)
		{
			output.println(String.format("%s : %f",  theKeyValue.getKey(), theKeyValue.getValue()));
		}
		
		output.println("word tag dictionary");
		
		wordfreqSort = KnowledgeCharacterPerxitySort.MapDoubleSort(domain2wordpossvalue);
		for(Entry<String, Double> theKeyValue : wordfreqSort)
		{
			output.println(String.format("%s : %f",  theKeyValue.getKey(), theKeyValue.getValue()));
		}
		
		
		output.close();
	}
	
	
	
	



}
