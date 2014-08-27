package feat_extractor;

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


public class JointSTEvaluate {

	private static class Sentence {
		String[] words;// ����
		String[] poss;// ���Ա�ע
		String chars;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		boolean bContainPOS = true;
		if(args.length > 3 && args[3].equals("seg"))bContainPOS = false;
		List<Sentence> goldSentences = new ArrayList<Sentence>();
		readCorpus(args[0],goldSentences, bContainPOS);
		List<Sentence> predSentences = new ArrayList<Sentence>();
		readCorpus(args[1],predSentences, bContainPOS);
		
		PrintWriter output = new PrintWriter(System.out);
		
		if(args.length > 2 && !args[2].equals("console"))
		{
			output = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(args[2]), "UTF-8"), false);
		}
		
		Evaluate(output, goldSentences, predSentences, bContainPOS);
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
			for (int idx = 0; validSentence && idx < wordNum; idx++) {
				String curWord = wordElems[idx].trim();
				String curPOS = "FAKEPOS";
				if (bContainPOS) {
					int lastSplitIndex = wordElems[idx].lastIndexOf("_");
					if (lastSplitIndex == -1) {
						lastSplitIndex = wordElems[idx].lastIndexOf("_");
					}
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
			}
			if (!validSentence) {
				System.out.println("Error: " + sLine);
				continue;
			}
			sentences.add(sentence);
		}
		
		reader.close();
	}

	public static int[] alignSentence(List<Sentence> goldSentences,
			List<Sentence> predSentences) {
		int goldNum = goldSentences.size();
		int predNum = predSentences.size();
		int[] alignResults = new int[goldNum];
		if(goldNum == predNum)
		{
			for (int idx = 0; idx < alignResults.length; idx++)
				alignResults[idx] = idx;
			
			return alignResults; 
		}
		for (int idx = 0; idx < alignResults.length; idx++)
			alignResults[idx] = -1;

		for (int idx = 0, idy = 0; idx < goldNum && idy < predNum;) {
			int maxMatchNum = 10;
			boolean[][] invalidMap = new boolean[maxMatchNum][maxMatchNum];
			for (int tempIdx = 0; tempIdx < maxMatchNum; tempIdx++) {
				for (int tempIdy = 0; tempIdy < maxMatchNum; tempIdy++) {
					invalidMap[tempIdx][tempIdy] = false;
				}
			}
			for (int tempIdx = idx; tempIdx < goldNum
					&& tempIdx < idx + maxMatchNum; tempIdx++) {
				String curGoldSentence = goldSentences.get(tempIdx).chars;
				for (int tempIdy = idy; tempIdy < predNum
						&& tempIdy < idy + maxMatchNum; tempIdy++) {
					String curPredSentence = predSentences.get(tempIdy).chars;
					if (curGoldSentence.equals(curPredSentence))
						invalidMap[tempIdx-idx][tempIdy-idy] = true;
				}
			}
			int maxIdx = idx - 1, maxIdy = idy - 1;
			for (int tempIdx = 0; tempIdx < maxMatchNum; tempIdx++) {
				for (int tempIdy = 0; tempIdy < maxMatchNum; tempIdy++) {
					if (invalidMap[tempIdx][tempIdy]
							&& alignResults[idx + tempIdx] == -1) {
						alignResults[idx + tempIdx] = idy + tempIdy;
						if (idx + tempIdx > maxIdx) {
							maxIdx = idx + tempIdx;
						}
						if (idy + tempIdy > maxIdy) {
							maxIdy = idy + tempIdy;
						}
					}
				}
			}
			if (maxIdx == idx - 1) {
				idx++;
			} else {
				idx = maxIdx + 1;
				idy = maxIdy + 1;
			}

		}

		return alignResults;
	}

	public static void Evaluate(PrintWriter writer, 	List<Sentence> goldSentences, 
			List<Sentence> predSentences, boolean bContainPOS) throws Exception{

		Map<String, Set<String>> goldlexicon = new HashMap<String, Set<String>>();
		
		int[] aligns = alignSentence(goldSentences, predSentences);

		int totalPredWords = 0;
		int totalRecoWords = 0;
		int totalGoldWords = 0;
		int totalPredIVWords = 0;
		int totalRecoIVWords = 0;
		int totalGoldIVWords = 0;
		int totalPredOOVWords = 0;
		int totalRecoOOVWords = 0;
		int totalGoldOOVWords = 0;
		int numsent = 0;
		int corrsent = 0;
		int corrsentL = 0;
		int correctLabels = 0;
		int correctLabelsIV = 0;
		int correctLabelsOOV = 0;
		int cnt = 0;
		
		int goldNum = goldSentences.size();

		for(int idx = 0; idx < goldNum; idx++)
		{
			if(aligns[idx] == -1)continue;
			int[] evalRes = reco(goldlexicon, goldSentences.get(idx), predSentences.get(aligns[idx]));
			
			totalGoldWords += evalRes[0]; totalPredWords += evalRes[1];
			totalGoldIVWords += evalRes[2]; totalPredIVWords += evalRes[3];
			totalGoldOOVWords += evalRes[4]; totalPredOOVWords += evalRes[5];
			
			totalRecoWords += evalRes[6]; totalRecoIVWords += evalRes[7];totalRecoOOVWords += evalRes[8];
			correctLabels += evalRes[9]; correctLabelsIV += evalRes[10]; correctLabelsOOV += evalRes[11];
			
			if(evalRes[9] == goldSentences.get(idx).words.length)
			{
			    corrsentL++;
			}
			
			if(evalRes[6] == goldSentences.get(idx).words.length)
			{
			    corrsent++;
			}
			
			writer.println(String.format("%d: R=%d/%d=%f, P=%d/%d=%f, F=%f", numsent,
					evalRes[9],evalRes[0],evalRes[9]*1.0/evalRes[0],
					evalRes[9],evalRes[1],evalRes[9]*1.0/evalRes[1],
					evalRes[9]*2.0/(evalRes[0] +evalRes[1])
				    ));
			
			numsent++;
		}
		
		writer.println();
		writer.println();
		writer.println();
		writer.println();
		//
		{
			writer.println(String.format("Total %d sentences", cnt));
		    writer.println(String.format("SEG_AC: R=%d/%d=%f, P=%d/%d=%f, F=%f", 
			    totalRecoWords,totalGoldWords,totalRecoWords*1.0/totalGoldWords,
			    totalRecoWords,totalPredWords,totalRecoWords*1.0/totalPredWords,
			    totalRecoWords*2.0/(totalGoldWords +totalPredWords)
			    ));
		    writer.println(String.format("SEG_IV_AC: R=%d/%d=%f, P=%d/%d=%f, F=%f", 
			    totalRecoIVWords,totalGoldIVWords,totalRecoIVWords*1.0/totalGoldIVWords,
			    totalRecoIVWords,totalPredIVWords,totalRecoIVWords*1.0/totalPredIVWords,
			    totalRecoIVWords*2.0/(totalGoldIVWords +totalPredIVWords)
			    ));
		    writer.println(String.format("SEG_OOV_AC: R=%d/%d=%f, P=%d/%d=%f, F=%f", 
			    totalRecoOOVWords,totalGoldOOVWords,totalRecoOOVWords*1.0/totalGoldOOVWords,
			    totalRecoOOVWords,totalPredOOVWords,totalRecoOOVWords*1.0/totalPredOOVWords,
			    totalRecoOOVWords*2.0/(totalGoldOOVWords +totalPredOOVWords)
			    ));
		    if(!bContainPOS)
		    {
		    	writer.println(String.format("SENT_SEG_AC: %d/%d = %f",
					    corrsent, numsent, corrsent*1.0/numsent));
		    }
		    if(bContainPOS)
		    {
			    writer.println(String.format("LABEL_AC: R=%d/%d=%f, P=%d/%d=%f, F=%f", 
				    correctLabels,totalGoldWords,correctLabels*1.0/totalGoldWords,
				    correctLabels,totalPredWords,correctLabels*1.0/totalPredWords,
				    correctLabels*2.0/(totalGoldWords +totalPredWords)
				    ));
			    writer.println(String.format("LABEL_IV_AC: R=%d/%d=%f, P=%d/%d=%f, F=%f", 
				    correctLabelsIV,totalGoldIVWords,correctLabelsIV*1.0/totalGoldIVWords,
				    correctLabelsIV,totalPredIVWords,correctLabelsIV*1.0/totalPredIVWords,
				    correctLabelsIV*2.0/(totalGoldIVWords +totalPredIVWords)
				    ));
			    writer.println(String.format("LABEL_OOV_AC: R=%d/%d=%f, P=%d/%d=%f, F=%f", 
				    correctLabelsOOV,totalGoldOOVWords,correctLabelsOOV*1.0/totalGoldOOVWords,
				    correctLabelsOOV,totalPredOOVWords,correctLabelsOOV*1.0/totalPredOOVWords,
				    correctLabelsOOV*2.0/(totalGoldOOVWords +totalPredOOVWords)
				    ));
			    writer.println(String.format("SENT_SEG_AC: %d/%d = %f, SENT_SEG&Label_AC: %d/%d = %f",
				    corrsent, numsent, corrsent*1.0/numsent,
				    corrsentL, numsent, corrsentL*1.0/numsent));
		    }
		}
		
		
	}

	public static int[] reco(Map<String, Set<String>> goldlexicon, Sentence goldSentence, Sentence predSentence) {
		// seg: 0 goldWords 1 predWords
		// seg: 2 goldIVWords 3 predIVWords
		// seg: 4 goldOOVWords 5 predOOVWords
		// seg: 6 recoWords 7 recoIVWords 8 recoOOVWords
		// tag: 9 recoPos 10 recoIVPos 11 recoOOVPos
		int[] predRes = new int[12];

		for (int i = 0; i < 12; i++) {
			predRes[i] = 0;
		}

		String[] goldWords = goldSentence.words;
		String[] goldLabels = goldSentence.poss;
		String[] predWords = predSentence.words;
		String[] predLabels = predSentence.poss;

		int m = 0, n = 0;
		for (int i = 0; i < goldWords.length; i++) {
			predRes[0]++;
			if (goldlexicon.containsKey(goldWords[i])) {
				predRes[2]++;
			} else {
				predRes[4]++;
			}
		}

		for (int i = 0; i < predWords.length; i++) {
			predRes[1]++;
			if (goldlexicon.containsKey(predWords[i])) {
				predRes[3]++;
			} else {
				predRes[5]++;
			}
		}

		while (m < predWords.length && n < goldWords.length) {
			if (predWords[m].equals(goldWords[n])) {
				predRes[6]++;
				boolean bTagMatch = false;

				if (predLabels[m].equals(goldLabels[n])) {
					bTagMatch = true;
					predRes[9]++;
				}
				if (goldlexicon.containsKey(predWords[m])) {
					predRes[7]++;
					if (bTagMatch
							&& goldlexicon.get(predWords[m]).contains(
									predLabels[m])) {
						predRes[10]++;
					} else {
						predRes[11]++;
					}
				} else {
					predRes[8]++;
					if (bTagMatch) {
						predRes[11]++;
					}
				}
				m++;
				n++;
			} else {
				int lgold = goldWords[n].length();
				int lpred = predWords[m].length();
				int lm = m + 1;
				int ln = n + 1;
				int sm = m;
				int sn = n;

				while (lm < predWords.length || ln < goldWords.length) {
					if (lgold > lpred && lm < predWords.length) {
						lpred = lpred + predWords[lm].length();
						sm = lm;
						lm++;
					} else if (lgold < lpred && ln < goldWords.length) {
						lgold = lgold + goldWords[ln].length();
						sn = ln;
						ln++;
					} else {
						break;
					}
				}

				m = sm + 1;
				n = sn + 1;
			}
		}
		return predRes;
	}

}
