package preparation;

import mason.utils.PinyinComparator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

public class LabelPropagationCorpus {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		
		//Map<String, Integer> allwords = new TreeMap<String, Integer>();
		//Map<String, Map<String, Integer>> allwordposs = new TreeMap<String, Map<String, Integer>>();		
		//Map<String, Integer> domainwords = new TreeMap<String, Integer>();
		//Map<String, Map<String, Integer>> domainwordposs = new TreeMap<String, Map<String, Integer>>();
		BufferedReader in = null;
		//BufferedReader in = new BufferedReader(new InputStreamReader(
		//		new FileInputStream(args[0]), "UTF-8"));
		String sLine = null;
		
		int minFreq = 0;
		if(args.length > 4) minFreq = Integer.parseInt(args[4]);
		
		//boolean bWord = false;
		//boolean bWordPOS = false;
	/*	while ((sLine = in.readLine()) != null) {
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
					allwords.put(firstUnit, 0);
					allwordposs.put(firstUnit, new TreeMap<String, Integer>());
				}
				else if(wordproperty%10 == 2)
				{
					allwords.put(firstUnit, 0);
					allwordposs.put(firstUnit, new TreeMap<String, Integer>());
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
					int wordEndIndex = firstUnit.indexOf("] , ");
					String theWord = firstUnit.substring(0, wordEndIndex+1);
					allwordposs.get(theWord).put(firstUnit, 0);
				}
				else if(wordproperty%10 == 2)
				{
					int wordEndIndex = firstUnit.indexOf("] , ");
					String theWord = firstUnit.substring(0, wordEndIndex+1);
					allwordposs.get(theWord).put(firstUnit, 0);
				}
				
			}
		}
		
		in.close();
		
		*/

		
	
		
		//System.out.println("Dictionary reading finished!......");
		
		// TODO Auto-generated method stub
		
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8"));
		

		
		Map<String, Map<String, Integer>>  vertex = new TreeMap<String, Map<String, Integer>>();
		
		Map<String, Integer>  vertexfreq = new TreeMap<String, Integer>();
		Map<String, Integer>  vertexfeatfreq = new TreeMap<String, Integer>();
		
		
		
		
		int sentCount = 0;
		int totalWordCount = 0;
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
				String left2word = "[START]";
				String right1word = "[END]";
				String right2word = "[END]";
				if(idx > 0)left1word = newnormline.substring(idx-1, idx);
				if(idx > 1)left2word = newnormline.substring(idx-2, idx-1);
				if(idx < newnormline.length() -1)right1word = newnormline.substring(idx+1, idx+2);
				if(idx < newnormline.length() -2)right2word = newnormline.substring(idx+2, idx+3);
				String curword = newnormline.substring(idx, idx+1);
				
				String wordKey ="[" + left1word+curword+right1word + "]";
				
			
				if(!PinyinComparator.bAllChineseCharacter(curword))continue;
				
				if(!vertex.containsKey(wordKey))
				{
					vertex.put(wordKey, new TreeMap<String, Integer>());
					vertexfreq.put(wordKey, 0);
				}
				
				vertexfreq.put(wordKey, vertexfreq.get(wordKey)+1);
				List<String> curFeats = new ArrayList<String>();
				curFeats.add("5gram="+left2word+left1word+curword+right1word+right2word);
				curFeats.add("3gram="+left1word+curword+right1word);
				curFeats.add("w[-1]="+left1word);
				curFeats.add("w[1]="+right1word);
				curFeats.add("w[-2][-1]="+left2word + left1word);
				curFeats.add("w[1][2]="+right1word + right2word);
				curFeats.add("w[0]="+curword);
				curFeats.add("w[-1][1]="+left1word + " " + right1word);
				curFeats.add("w[-2][-1][1]="+left2word + left1word + right1word);
				curFeats.add("w[-1]w[1][2]="+left1word +right1word + " " + right2word);
				String left1Type = charType(left1word);
				String curType = charType(curword);
				String right1Type = charType(right1word);
				curFeats.add("T[-1]T[0]T[1]="+left1Type +curType + " " + right1Type);
				
								
				for(int i = 1; i <= curword.length() && i <= 2; i++)
				{
					curFeats.add(String.format("p%d=%s", i, curword.substring(0, i)));
					curFeats.add(String.format("s%d=%s", i, curword.substring(curword.length()-i)));
				}
				
				
								
				for(String curFeat : curFeats)
				{
					if(!vertex.get(wordKey).containsKey(curFeat))
					{
						vertex.get(wordKey).put(curFeat, 0);
					}
					vertex.get(wordKey).put(curFeat, vertex.get(wordKey).get(curFeat)+1);
					
					if(!vertexfeatfreq.containsKey(curFeat))
					{
						vertexfeatfreq.put(curFeat, 0);
					}
					vertexfeatfreq.put(curFeat, vertexfeatfreq.get(curFeat)+1);
				}
				
				//String curGoldLabel = AssignLabelByDict(allwords, allwordposs, left2word, left1word, curword, right1word, right2word);
				
				
				totalWordCount++;
						
			}
			
			sentCount++;
			//if(sentCount > minFreq) break;
			if(sentCount%500 == 0)
			{
				System.out.println(String.format("%d tagged sentence reading finished, %d vertexes", sentCount, vertex.size()));				
			}
		}
		
		in.close();
		
		System.out.println(String.format("All %d tagged sentence reading finished, %d vertexes", sentCount, vertex.size()));
		
		
		sentCount = 0;
		Map<String, Map<String, Double>>  goldvertex = new TreeMap<String, Map<String, Double>>();
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[1]), "UTF8"));
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] wordposs = sLine.trim().split("\\s+");
			List<String> sentences = new ArrayList<String>();
			List<String> sentenceTypes = new ArrayList<String>();
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
				if(theWord.length() == 1)
				{
					sentences.add(theWord);
					sentenceTypes.add("S#"+thePOS);
				}
				else
				{
					sentences.add(theWord.substring(0,1));
					sentenceTypes.add("B#"+thePOS);
					for(int idx = 1; idx < theWord.length()-1; idx++)
					{
						sentences.add(theWord.substring(idx,idx+1));
						sentenceTypes.add("M#"+thePOS);
					}
					sentences.add(theWord.substring(theWord.length()-1,theWord.length()));
					sentenceTypes.add("E#"+thePOS);
					
				}								
			}
			
			for(int sentId = 0; sentId < sentences.size(); sentId++)
			{
				String left1word = "[START]";	
				//String left2word = "[START]";
				String right1word = "[END]";
				//String right2word = "[END]";
				if(sentId > 0)left1word = sentences.get(sentId-1);
				//if(sentId > 1)left2word = sentences.get(sentId-2);
				if(sentId < sentences.size() -1)right1word = sentences.get(sentId+1);
				//if(sentId < sentences.size() -2)right2word = sentences.get(sentId+2);
				String curword = sentences.get(sentId);
				
				if(!PinyinComparator.bAllChineseCharacter(curword))continue;
				
				String wordKey ="[" + left1word+curword+right1word + "]";
				String theType = sentenceTypes.get(sentId);
				if(!goldvertex.containsKey(wordKey))
				{
					goldvertex.put(wordKey, new TreeMap<String, Double>());
				}
				if(!goldvertex.get(wordKey).containsKey(theType))
				{
					goldvertex.get(wordKey).put(theType, 0.0);
				}
				
				goldvertex.get(wordKey).put(theType, goldvertex.get(wordKey).get(theType)+1.0);
			}
			
			sentCount++;
			//if(sentCount > minFreq) break;
			if(sentCount%500 == 0)
			{
				System.out.println(String.format("%d tagged sentence reading finished, %d vertexes, %d gold vertexes", sentCount, vertex.size(), goldvertex.size()));				
			}
			
		}
		
		in.close();
		
		System.out.println(String.format("All %d tagged sentence reading finished, %d vertexes, %d gold words", sentCount, vertex.size(), goldvertex.size()));
		
		int removedCount = 0;
		
		for(String theKey : vertexfreq.keySet())
		{
			if(vertexfreq.get(theKey) < minFreq)
			{
				if(vertex.containsKey(theKey))
				{
					vertex.remove(theKey);
					removedCount++;
				}
				
				if(goldvertex.containsKey(theKey))
				{
					goldvertex.remove(theKey);
				}
			}
		}
		
		
		System.out.println(String.format("%d low freq vertexes removed, remain %d vertexes, %d gold words", removedCount, vertex.size(), goldvertex.size()));
		
		System.out.println("Computing PMI values.....");
		
		Map<String, Map<String, Double>>  vertexdouble = new TreeMap<String, Map<String, Double>>();
		
		Map<String, Boolean>  vertexmark = new TreeMap<String, Boolean>();
		
		for(String theKey1 : vertex.keySet())
		{
			Map<String, Integer> feature1 = vertex.get(theKey1);	
			vertexdouble.put(theKey1, new TreeMap<String, Double>());
			for(String theKey2 : feature1.keySet())
			{
				Double theScore = Math.log( (feature1.get(theKey2) * 1.0 * totalWordCount) / (vertexfeatfreq.get(theKey2)*vertexfreq.get(theKey1)) );
				if(theScore > 1e-30)
				{
					vertexdouble.get(theKey1).put(theKey2, theScore);
				}
			}
			
			if(goldvertex.containsKey(theKey1))
			{
				vertexmark.put(theKey1, true);
			}
			else
			{
				vertexmark.put(theKey1, false);
			}
		}
		
		
		/*
		PrintWriter output1 = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[4]), "UTF-8"));
		
		int vecCount = 0;
		for(String theKey1 : vertexdouble.keySet())
		{

			Map<String, Double> vertexsimkey1 = vertexdouble.get(theKey1);			
			List<Entry<String, Double>> wordfreqSort = KnowledgeCharacterPerxitySort.MapDoubleSort(vertexsimkey1);
			for(Entry<String, Double> key2sim : wordfreqSort)
			{				
				output1.println(String.format("%s\t%s\t%f", theKey1, key2sim.getKey(), key2sim.getValue()));				
			}
			
			vecCount++;
			if(vecCount%100 == 0)
			{
				System.out.print(vecCount); System.out.print(" "); 
				if(vecCount%1000 == 0) System.out.println();
			}
		}
				
		output1.close();
		*/
		
		System.out.println("Computing PMI values.....finished......");
		
		
		//vertex similarity
		
		Map<String, Double>  vertexdotsvalue = new TreeMap<String, Double>();
		int vecCount = 0;
		for(String theKey : vertexdouble.keySet())
		{
			vertexdotsvalue.put(theKey, Math.sqrt(dotProduct(vertexdouble.get(theKey), vertexdouble.get(theKey))));
			vecCount++;
			if(vecCount%100 == 0)
			{
				System.out.print(vecCount); System.out.print(" "); 
				if(vecCount%1000 == 0) System.out.println();
			}
		}
		System.out.println(String.format("All %d vertexes regularized", vecCount));
		
		Map<String, Map<String, Double>>  vertexsim = new TreeMap<String, Map<String, Double>>();
		vecCount = 0;
		for(String theKey1 : vertexdouble.keySet())
		{
			vertexsim.put(theKey1, new TreeMap<String, Double>());
			for(String theKey2 : vertexdouble.keySet())
			{
				if(vertexsim.containsKey(theKey2)) continue;
				double sim = dotProduct(vertexdouble.get(theKey1), vertexdouble.get(theKey2))/(vertexdotsvalue.get(theKey1)* vertexdotsvalue.get(theKey2));
				if(sim <= 1.0 + 1e-30)
				{
					if(theKey1.equals(theKey2) && (sim > 1.0 + 1e-30 || sim < 1.0 - 1e-30))
					{
						System.out.print("error sim value: "); 
						System.out.println(String.format("%s\t%s\t%f", theKey1, theKey2, sim));
					}
					if(sim > 1e-30) vertexsim.get(theKey1).put(theKey2, sim);
					if(sim > 0.8 && !theKey1.equals(theKey2))
					{
						System.out.println(String.format("%s\t%s\t%f", theKey1, theKey2, sim));
					}
				}
				else if (sim > 1.0 + 1e-30)
				{
					System.out.print("error sim value: "); 
					System.out.println(sim);
				}
				
				
				vecCount++;
				if(vecCount%10000 == 0)
				{
					System.out.print(vecCount); System.out.print(" "); 
					if(vecCount%100000 == 0) System.out.println();
				}
			}
		}
		
		System.out.println(String.format("All %d edges sim computed....", vecCount));
		
		Map<String, Map<String, Double>>  vertexKsim = new TreeMap<String, Map<String, Double>>();
		for(String theKey1 : vertexsim.keySet())
		{
			Map<String, Double> vertexsimkey1 = vertexsim.get(theKey1);		
			vertexKsim.put(theKey1, new TreeMap<String, Double>());
			List<Entry<String, Double>> wordfreqSort = KnowledgeCharacterPerxitySort.MapDoubleSort(vertexsimkey1);
			for(Entry<String, Double> key2sim : wordfreqSort)
			{
				if(key2sim.getKey().equals(theKey1))
				{
					continue;
				}
				
				if(key2sim.getValue() < 0.000001)
				{
					break;
				}
								
				vertexKsim.get(theKey1).put(key2sim.getKey(), key2sim.getValue());
	
			}
		}
		
		for(int iteration = 0; iteration < Integer.MAX_VALUE; iteration++)
		{
			System.out.println(String.format("iter %d.....", iteration));
			int addedCount = 0;
			for(String theKey1: vertexKsim.keySet())
			{
				if(vertexmark.get(theKey1))
				{
					Map<String, Double> vertexsimkey1 = vertexKsim.get(theKey1);
					for(String theKey2 : vertexsimkey1.keySet())
					{
						if(!vertexmark.get(theKey2))
						{
							vertexmark.put(theKey2, true);
							addedCount++;
						}
					}
				}
			}
			
			if(addedCount > 0)
			{
				System.out.println(String.format("verified %d vertices.....", addedCount));
			}
			else
			{
				System.out.println(String.format("verified %d vertices.....", addedCount));
				break;
			}
		}
		
		removedCount = 0;
		int edgeCount = 0;
		for(String theKey1: vertexmark.keySet())
		{
			if(!vertexmark.get(theKey1))
			{
				vertexKsim.remove(theKey1);
				removedCount++;
			}
			else
			{
				edgeCount += vertexKsim.get(theKey1).size();
			}
		}
		
		System.out.println(String.format("%d unconnected vertices removed, remain %d vertices, %d edges.", removedCount, vertexKsim.size(), edgeCount));
		
		
		
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"));
		
		for(String theKey1 : vertexKsim.keySet())
		{
			Map<String, Double> vertexsimkey1 = vertexKsim.get(theKey1);			
			List<Entry<String, Double>> wordfreqSort = KnowledgeCharacterPerxitySort.MapDoubleSort(vertexsimkey1);
			for(Entry<String, Double> theEntry : wordfreqSort)
			{
				String theKey2 = theEntry.getKey();
				if(theKey2.equals(theKey1))
				{
					continue;
				}
				if(vertexsimkey1.get(theKey2) > 0.0001)
				{
					output.println(String.format("%s\t%s\t%f", theKey1, theKey2, vertexsimkey1.get(theKey2)));
				}				
			}
		}
				
		output.close();
		
		
		output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[3]), "UTF-8"));
		
		for(String theKey : goldvertex.keySet())
		{
			Map<String, Double> wordposprob = goldvertex.get(theKey);
			double sum = 0.0;
			for(String thePOS: wordposprob.keySet())
			{
				sum += wordposprob.get(thePOS);
			}
			
			List<Entry<String, Double>> wordfreqSort = KnowledgeCharacterPerxitySort.MapDoubleSort(wordposprob);
			for(Entry<String, Double> posprob : wordfreqSort)
			{	
				if(posprob.getValue()/sum > 0.0001)
				{
					output.println(String.format("%s\t%s\t%f", theKey, posprob.getKey(), posprob.getValue()/sum));
				}
			}
		}
		
				
		output.close();
	}
	
	
	public static double  dotProduct(Map<String, Double> vec1, Map<String, Double> vec2)
	{
		double sum = 0.0;
		for(String theKey : vec1.keySet())
		{
			if(vec2.containsKey(theKey))
			{
				sum += vec1.get(theKey)*vec2.get(theKey);
			}
		}
		
		return sum;
	}
	
	public static String charType(String curWord)
	{
		if(curWord.length() > 1)
		{
			return curWord;
		}
		else
		{

			if(PinyinComparator.bAllChineseCharacter(curWord))
			{
				return "Chn";
			}
			else if("1234567890１２３４５６７８９０".indexOf(curWord) != 0)
			{
				return "Dig";
			}
			else if("abcdefghijklmnopqrstuvwxyz".indexOf(curWord.toLowerCase()) != 0)
			{
				return "Eng";
			}
			else
			{
				return "Oth";
			}
		}
	}

}
