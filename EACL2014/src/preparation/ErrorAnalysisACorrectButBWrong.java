package preparation;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class ErrorAnalysisACorrectButBWrong {

	private static class Sentence {
		String[] words;
		String[] poss;
		Map<String, Integer> segposResults = new TreeMap<String, Integer>();
		String chars;
		public String toString()
		{
			String output = "";
			
			for(int idx = 0; idx < words.length; idx++)
			{
				output = output + " " + words[idx] + "_" + poss[idx];
			}
			
			return output.trim();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		boolean bContainPOS = true;
		if(args.length > 4 && args[4].equals("seg"))bContainPOS = false;
		List<Sentence> goldSentences = new ArrayList<Sentence>();
		readCorpus(args[0],goldSentences, bContainPOS);
		List<Sentence> pred1Sentences = new ArrayList<Sentence>();
		readCorpus(args[1],pred1Sentences, bContainPOS);		
		List<Sentence> pred2Sentences = new ArrayList<Sentence>();
		readCorpus(args[2],pred2Sentences, bContainPOS);
		
		PrintWriter output = new PrintWriter(System.out);
		
		if(args.length > 3 && !args[3].equals("console"))
		{
			output = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(args[3]), "UTF-8"), false);
		}
		
		AnalyzeACorrectBWrong(output, goldSentences, pred1Sentences, pred2Sentences, bContainPOS);
		output.close();
	}

	public static void readCorpus(String inputFile, List<Sentence> sentences,
			boolean bContainPOS) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(inputFile), "UTF-8"));
		String sLine = null;
		while ((sLine = reader.readLine()) != null) {
			if (sLine.trim().equals(""))
				continue;
			String[] wordElems = sLine.trim().split("\\s+");
			Sentence sentence = new Sentence();
			int wordNum = wordElems.length;
			sentence.words = new String[wordNum];
			sentence.poss = new String[wordNum];
			sentence.chars = "";
			boolean validSentence = true;
			int start = 0;
			for (int idx = 0; validSentence && idx < wordNum; idx++) {
				String curWord = wordElems[idx].trim();
				String curPOS = "FAKEPOS";
				if (bContainPOS) {
					int lastSplitIndex = wordElems[idx].lastIndexOf("_");
					if (lastSplitIndex == -1) {
						validSentence = false;
						break;
					}
					curWord = wordElems[idx].substring(0, lastSplitIndex)
							.trim();
					curPOS = wordElems[idx].substring(lastSplitIndex + 1)
							.trim();
				}
				sentence.words[idx] = curWord;
				sentence.poss[idx] = curPOS;
				sentence.chars = sentence.chars + curWord;
				sentence.segposResults.put(String.format("[%d %d]%s", start, start+curWord.length()-1, curPOS), idx);
				start = start + curWord.length();
			}
			if (!validSentence) {
				System.out.println("Error: " + sLine);
				continue;
			}
			sentences.add(sentence);
		}
		
		reader.close();
	}



	
	public static void AnalyzeACorrectBWrong(PrintWriter writer, 	List<Sentence> goldSentences, 
			List<Sentence> predSentences1, List<Sentence> predSentences2, boolean bContainPOS) throws Exception{
	
		int goldNum = goldSentences.size();

		for(int idx = 0; idx < goldNum; idx++)
		{
			Map<String, Integer> goldRes = goldSentences.get(idx).segposResults;
			Map<String, Integer> pred1Res = predSentences1.get(idx).segposResults;
			Map<String, Integer> pred2Res = predSentences2.get(idx).segposResults;
			boolean bErrorFind = false;
			for(String theKey : pred1Res.keySet())
			{
				if(goldRes.containsKey(theKey) && !pred2Res.containsKey(theKey))
				{					
					if(!bErrorFind)
					{
						writer.println("gold: " + goldSentences.get(idx).toString());
						writer.println("pred1: " + predSentences1.get(idx).toString());
						writer.println("pred2: " + predSentences2.get(idx).toString());
					}
					writer.println(predSentences1.get(idx).words[pred1Res.get(theKey)] + "_" + predSentences1.get(idx).poss[pred1Res.get(theKey)]);
					bErrorFind = true;
				}
			}
		}
		
			
	}
}
