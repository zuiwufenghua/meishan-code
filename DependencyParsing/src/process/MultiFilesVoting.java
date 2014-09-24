package process;

import java.io.*;
import java.util.*;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

import mason.dep.*;

public class MultiFilesVoting {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub		
		List<String> foldList = new ArrayList<String>();
		int nSamples = Integer.parseInt(args[0]);
		
		for(int i = 0; i < nSamples; i++)
			foldList.add(String.format("sample.%d", i));
		int argsLength = args.length;
		
		String lastName = args[argsLength-3];
		String inputFile = args[argsLength-2];
		String outFile = args[argsLength-1];
		SDPCorpusReader sdpCorpusReader_gold = new SDPCorpusReader();
		sdpCorpusReader_gold.Init(inputFile);	
		List<DepInstance> sentences= sdpCorpusReader_gold.m_vecInstances;
		
		/*BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
				inputFile), "UTF-8"));
		
		String sLine = null;		
		while((sLine = reader.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			sentences.add(sLine.trim()); 
		}		
		reader.close();*/
		int totalSenNum = sentences.size();
		List<List<DepInstance>> allInstances = new ArrayList<List<DepInstance>>();
		for(int i = 0; i < totalSenNum; i++)
		{
			allInstances.add(new ArrayList<DepInstance>());
		}
		
		for(int i = 1; i < argsLength-3; i++)
		{
			String inFolderName = args[i].substring(1);
			int weight = Integer.parseInt(args[i].substring(0,1));
			File file = new File(inFolderName);
			String[] subFiles = file.list();
			Set<String> subFilesSet = new HashSet<String>();
			for(String curfoldName : subFiles)
			{
				subFilesSet.add(curfoldName);
			}
			
			String foldermark = String.format("%d", i);
			for(String curfoldName : foldList)
			{
				
				if(!subFilesSet.contains(curfoldName))
				{
					System.out.println("Foder " + inFolderName + File.separator + curfoldName + " does not exist. Please check.");
					continue;
				}
				File subfile = new File(inFolderName + File.separator + curfoldName + File.separator + lastName);
				if(subfile.exists() && subfile.isFile())
				{
					SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();
					sdpCorpusReader.Init(inFolderName + File.separator + curfoldName + File.separator + lastName);

					List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
					int totalInstances = vecInstances.size();
					
					for(int j = 0, k = 0; j < totalInstances && k < totalSenNum; j++)
					{
						DepInstance tmpInstance = vecInstances.get(j);
						String[] theForms = new String[tmpInstance.forms.size()];
						tmpInstance.forms.toArray(theForms);

						String thesentence = "";
						for (String theWord : theForms) {
							thesentence = thesentence + " " + theWord;
						}
						thesentence = thesentence.trim();
						
						
						DepInstance goldInstance = sentences.get(k);
						List<String> theGoldForms = goldInstance.forms;

						String thesentenceGold = "";
						for (String theWord : theGoldForms) {
							thesentenceGold = thesentenceGold + " " + theWord;
						}
						thesentenceGold = thesentenceGold.trim();
						
						int prevK = k;
						while(!thesentenceGold.equals(thesentence))
						{
							k++;
							if(totalInstances-j > totalSenNum -k)
							{
								System.out.println(thesentence);
								k = prevK;
								break;
							}
							goldInstance = sentences.get(k);
							theGoldForms = goldInstance.forms;

							thesentenceGold = "";
							for (String theWord : theGoldForms) {
								thesentenceGold = thesentenceGold + " " + theWord;
							}
							thesentenceGold = thesentenceGold.trim();
						}
						
						if(!thesentenceGold.equals(thesentence))continue;
						tmpInstance.avgte = weight;
						tmpInstance.smark = foldermark;
						allInstances.get(k).add(tmpInstance);
						k++;
					}
				}
			}
		}
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
				outFile), "UTF-8"));
		PrintWriter writer_log = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
				outFile+".log"), "UTF-8"));
		PrintWriter writer_eval = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
				outFile+".eval"), "UTF-8"));
		
		int totalWords = 0; int totalCorrectWords = 0; 
		int corrsent = 0;
		int numsent = totalSenNum;
		
		int sent_num_word_all_nopunc_dep_correct_total = 0;
		int sent_num_root_correct_total = 0;
		int word_num_nopunc_total = 0;
		int word_num_nopunc_dep_correct_total = 0;

		int sent_num_word_all_nopunc_deplabel_correct_total = 0;
		int word_num_nopunc_deplabel_correct_total = 0;

		int sent_num_word_all_nopunc_dep_correct_cur = 0;
		int sent_num_root_correct_cur = 0;
		int word_num_nopunc_cur = 0;
		int word_num_nopunc_dep_correct_cur = 0;

		int sent_num_word_all_nopunc_deplabel_correct_cur = 0;
		int word_num_nopunc_deplabel_correct_cur = 0;
		
		
		
		for(int i = 0; i < totalSenNum; i++)
		{
			List<DepInstance> curSenResults = allInstances.get(i);
			DepInstance goldInst = sentences.get(i);
			if(curSenResults.size() == 0)
			{
				System.out.println("Sentence " + String.format("%d", i) + " do not have results.");
			}
			DepInstance firstInst = curSenResults.get(0);
			
			int wordsnum = firstInst.forms.size();
			totalWords += wordsnum;
			int curCorrectNum = 0;
			
			DepInstance predictResult = new DepInstance();
			predictResult.forms.addAll(firstInst.forms);
			predictResult.lemmas.addAll(firstInst.lemmas);
			predictResult.cpostags.addAll(firstInst.cpostags);
					
			for(int j = 0; j < firstInst.forms.size(); j++)
			{
				String theForm = firstInst.forms.get(j);
				Map<String, Double> posStat = new HashMap<String, Double>();
				Map<Integer, Double> headStat = new HashMap<Integer, Double>();
				Map<String, Double> labelStat = new HashMap<String, Double>();
				
				Map<String, Set<String>> posStatMark = new HashMap<String, Set<String>>();
				Map<Integer, Set<String>> headStatMark = new HashMap<Integer, Set<String>>();
				Map<String, Set<String>> labelStatMark = new HashMap<String, Set<String>>();
				
				for(DepInstance curInst : curSenResults)
				{
					if(!curInst.postags.get(j).equals("_"))
					{
						if(posStat.containsKey(curInst.postags.get(j)))
						{
							posStat.put(curInst.postags.get(j), posStat.get(curInst.postags.get(j)) +1*curInst.avgte);
							if(!posStatMark.get(curInst.postags.get(j)).contains(curInst.smark))
							{
								posStatMark.get(curInst.postags.get(j)).add(curInst.smark);
							}
						}
						else
						{
							posStat.put(curInst.postags.get(j), 1*curInst.avgte);
							Set<String> curMarks = new HashSet<String>();
							curMarks.add(curInst.smark);
							posStatMark.put(curInst.postags.get(j), curMarks);
						}
					}
					
					//if(!curInst.heads.get(j).equals("_"))
					//{
						if(headStat.containsKey(curInst.heads.get(j)))
						{
							headStat.put(curInst.heads.get(j), headStat.get(curInst.heads.get(j)) + 1*curInst.avgte);
							if(!headStatMark.get(curInst.heads.get(j)).contains(curInst.smark))
							{
								headStatMark.get(curInst.heads.get(j)).add(curInst.smark);
							}
						}
						else
						{
							headStat.put(curInst.heads.get(j), 1*curInst.avgte);
							Set<String> curMarks = new HashSet<String>();
							curMarks.add(curInst.smark);
							headStatMark.put(curInst.heads.get(j), curMarks);
						}
					//}
					
					if(!curInst.deprels.get(j).equals("_"))
					{
						if(labelStat.containsKey(curInst.deprels.get(j)))
						{
							labelStat.put(curInst.deprels.get(j), labelStat.get(curInst.deprels.get(j)) + 1*curInst.avgte);
							if(!labelStatMark.get(curInst.deprels.get(j)).contains(curInst.smark))
							{
								labelStatMark.get(curInst.deprels.get(j)).add(curInst.smark);
							}
						}
						else
						{
							labelStat.put(curInst.deprels.get(j), 1*curInst.avgte);
							Set<String> curMarks = new HashSet<String>();
							curMarks.add(curInst.smark);
							labelStatMark.put(curInst.deprels.get(j), curMarks);
						}
					}
				}
				
				String thePos = "_";
				double highfreq = -1;
				String pos_out = "";
				for(String curPos : posStat.keySet())
				{
					String cur_pos_mark = "";
					for(String themark : posStatMark.get(curPos))
					{
						cur_pos_mark = cur_pos_mark + "_" + themark;
					}
					pos_out = pos_out + "##" + curPos + cur_pos_mark + String.format("(%d)", posStat.get(curPos).intValue());
					if(posStat.get(curPos) > highfreq)
					{
						highfreq = posStat.get(curPos);
						thePos = curPos;
					}
				}
				
				int theHead = -1;
				highfreq = -1;
				String head_out = "";
				for(int curHead : headStat.keySet())
				{
					String cur_head_mark = "";
					for(String themark : headStatMark.get(curHead))
					{
						cur_head_mark = cur_head_mark + "_" + themark;
					}
					head_out = head_out + "##" + String.format("%d", curHead) + cur_head_mark + String.format("(%d)", headStat.get(curHead).intValue());
					
					if(headStat.get(curHead) > highfreq)
					{
						highfreq = headStat.get(curHead);
						theHead = curHead;
					}
				}
				
				String theLabel = "_";
				highfreq = -1;
				String label_out = "";
				for(String curLabel : labelStat.keySet())
				{
					String cur_label_mark = "";
					for(String themark : labelStatMark.get(curLabel))
					{
						cur_label_mark = cur_label_mark + "_" + themark;
					}
					label_out = label_out + "##" + curLabel + cur_label_mark + String.format("(%d)", labelStat.get(curLabel).intValue());
					
					
					if(labelStat.get(curLabel) > highfreq)
					{
						highfreq = labelStat.get(curLabel);
						theLabel = curLabel;
					}
				}
				
				if(thePos.equals(goldInst.postags.get(j)))
				{
					curCorrectNum++;
				}
				predictResult.postags.add(thePos);
				predictResult.heads.add(theHead);
				predictResult.deprels.add(theLabel);
				writer.println(String.format("%d\t%s\t_\t%s\t%s\t_\t%d\t%s\t_\t_", j+1, theForm, thePos, thePos, theHead, theLabel));
				writer_log.println(String.format("%d\t%s\t_\t%s\t%s\t_\t%d\t%s\t%s\t%s\t%s", j+1, theForm, thePos, thePos, theHead, theLabel, pos_out.substring(2), head_out.substring(2), label_out.substring(2)));
			}
			
			totalCorrectWords += curCorrectNum;
			if(curCorrectNum == wordsnum)corrsent++;
			
			if(!goldInst.evaluateWithOther(predictResult))
			{
				System.out.println(String.format("Sentence %d is not matched.", i+1));
				//output.close();
				//return;
			}
			
			word_num_nopunc_cur = goldInst.eval_res[0];
			word_num_nopunc_dep_correct_cur = goldInst.eval_res[1];
			word_num_nopunc_deplabel_correct_cur = goldInst.eval_res[2];
			sent_num_word_all_nopunc_dep_correct_cur = goldInst.eval_res[3];
			sent_num_word_all_nopunc_deplabel_correct_cur = goldInst.eval_res[4];
			sent_num_root_correct_cur = goldInst.eval_res[5];

			word_num_nopunc_total += word_num_nopunc_cur;
			word_num_nopunc_dep_correct_total += word_num_nopunc_dep_correct_cur;
			word_num_nopunc_deplabel_correct_total += word_num_nopunc_deplabel_correct_cur;
			sent_num_word_all_nopunc_dep_correct_total += sent_num_word_all_nopunc_dep_correct_cur;
			sent_num_word_all_nopunc_deplabel_correct_total += sent_num_word_all_nopunc_deplabel_correct_cur;
			sent_num_root_correct_total += sent_num_root_correct_cur;
			
			
			if ((i + 1) % 1000 == 0) {
				writer_eval.println(String.format(
						"Pos Accuracy: \t\t%d/%d=%f",
						totalCorrectWords, totalWords,
						totalCorrectWords * 100.0
								/ totalWords));
				writer_eval.println(String.format(
						"All sentence accuracy: \t\t%d/%d=%f",
						corrsent, (i+1),
						corrsent * 100.0
								/ (i+1)));
				writer_eval.println(String.format(
						"CM (excluding punc): \t\t%d/%d=%f",
						sent_num_word_all_nopunc_dep_correct_total, i + 1,
						sent_num_word_all_nopunc_dep_correct_total * 100.0
								/ (i + 1)));
				writer_eval.println(String.format(
						"CM_L (excluding punc): \t\t%d/%d=%f",
						sent_num_word_all_nopunc_deplabel_correct_total, i + 1,
						sent_num_word_all_nopunc_deplabel_correct_total * 100.0
								/ (i + 1)));
				writer_eval.println(String.format(
						"UAS (excluding punc): \t\t%d/%d=%f",
						word_num_nopunc_dep_correct_total,
						word_num_nopunc_total,
						word_num_nopunc_dep_correct_total * 100.0
								/ word_num_nopunc_total));
				writer_eval.println(String.format(
						"LAS (excluding punc): \t\t%d/%d=%f",
						word_num_nopunc_deplabel_correct_total,
						word_num_nopunc_total,
						word_num_nopunc_deplabel_correct_total * 100.0
								/ word_num_nopunc_total));
				writer_eval.println(String.format(
						"ROOT (excluding punc): \t\t%d/%d=%f",
						sent_num_root_correct_total, i + 1,
						sent_num_root_correct_total * 100.0 / (i + 1)));
				writer_eval.println();
			}
			
			
			writer.println();
			writer.flush();
			writer_log.println();
			writer_log.flush();
			
			
		}
		
		{
			writer_eval.println(String.format(
					"Pos Accuracy: \t\t%d/%d=%f",
					totalCorrectWords, totalWords,
					totalCorrectWords * 100.0
							/ totalWords));
			writer_eval.println(String.format(
					"All sentence accuracy: \t\t%d/%d=%f",
					corrsent, numsent,
					corrsent * 100.0
							/ numsent));
			writer_eval.println(String.format(
					"CM (excluding punc): \t\t%d/%d=%f",
					sent_num_word_all_nopunc_dep_correct_total, numsent,
					sent_num_word_all_nopunc_dep_correct_total * 100.0
							/ numsent));
			writer_eval.println(String.format(
					"CM_L (excluding punc): \t\t%d/%d=%f",
					sent_num_word_all_nopunc_deplabel_correct_total, numsent,
					sent_num_word_all_nopunc_deplabel_correct_total * 100.0
							/ numsent));
			writer_eval.println(String.format(
					"UAS (excluding punc): \t\t%d/%d=%f",
					word_num_nopunc_dep_correct_total,
					word_num_nopunc_total,
					word_num_nopunc_dep_correct_total * 100.0
							/ word_num_nopunc_total));
			writer_eval.println(String.format(
					"LAS (excluding punc): \t\t%d/%d=%f",
					word_num_nopunc_deplabel_correct_total,
					word_num_nopunc_total,
					word_num_nopunc_deplabel_correct_total * 100.0
							/ word_num_nopunc_total));
			writer_eval.println(String.format(
					"ROOT (excluding punc): \t\t%d/%d=%f",
					sent_num_root_correct_total, numsent,
					sent_num_root_correct_total * 100.0 / (numsent)));
			writer_eval.println();
		}
		
		writer.close();
		writer_log.println();
		writer_log.close();
		writer_eval.close();
		
	}

}
