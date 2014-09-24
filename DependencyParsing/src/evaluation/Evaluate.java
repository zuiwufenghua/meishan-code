package evaluation;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;


public class Evaluate {

	public static void main(String[] args) throws Exception {
		SDPCorpusReader sdpCorpusReader1 = new SDPCorpusReader();
		sdpCorpusReader1.Init(args[0]);
		SDPCorpusReader sdpCorpusReader2 = new SDPCorpusReader();
		sdpCorpusReader2.Init(args[1]);	
		
		String outputFilePos = args[2] + ".pos";
		String outputFileDep = args[2] + ".dep";
		int iType = Integer.parseInt(args[3]);
		if(iType == 0)
		{
			EvaluatePosTagger(sdpCorpusReader1.m_vecInstances,sdpCorpusReader2.m_vecInstances, outputFilePos);
			evaluateDep(sdpCorpusReader1.m_vecInstances,sdpCorpusReader2.m_vecInstances, outputFileDep);
		}
		else if(iType == 1)
		{
			EvaluatePosTagger(sdpCorpusReader1.m_vecInstances,sdpCorpusReader2.m_vecInstances, outputFilePos);
		}
		else if(iType == 2)
		{
			evaluateDep(sdpCorpusReader1.m_vecInstances,sdpCorpusReader2.m_vecInstances, outputFileDep);
		}
		
	}
	
	public static void EvaluatePosTagger(List<DepInstance> vecInstances1, List<DepInstance> vecInstances2, String outputFile) throws Exception
	{
		int totalWords = 0; int totalCorrectWords = 0; 
		int corrsent = 0;
		int numsent = vecInstances1.size();
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
				outputFile), "UTF-8"));
		if(numsent != vecInstances2.size()) 
		{
			writer.println("Sentence Num do not match.");
			writer.close();
			return;
		}
		
		for(int i = 0; i < numsent; i++)
		{
			DepInstance inst1 = vecInstances1.get(i);
			DepInstance inst2 = vecInstances2.get(i);
			int wordsnum = inst1.forms.size();
			if(inst2.forms.size() != wordsnum)
			{
				writer.println(String.format("Sentence %d is not matched.", i+1));
				writer.close();
				return;
			}
			totalWords += wordsnum;
			int curCorrectNum = 0;
			for(int j = 0; j < wordsnum; j++)
			{
				//if(inst1.postags.get(j).equals(anObject))
				if(inst1.postags.get(j).equals(inst2.postags.get(j)))
				{
					curCorrectNum++;
				}
			}
			totalCorrectWords += curCorrectNum;
			if(curCorrectNum == wordsnum)corrsent++;
			if ((i + 1) % 1000 == 0) {
				writer.println(String.format(
						"Pos Accuracy: \t\t%d/%d=%f",
						totalCorrectWords, totalWords,
						totalCorrectWords * 100.0
								/ totalWords));
				writer.println(String.format(
						"All sentence accuracy: \t\t%d/%d=%f",
						corrsent, (i+1),
						corrsent * 100.0
								/ (i+1)));
			}
		}
		
		{
			writer.println(String.format(
					"Pos Accuracy: \t\t%d/%d=%f",
					totalCorrectWords, totalWords,
					totalCorrectWords * 100.0
							/ totalWords));
			writer.println(String.format(
					"All sentence accuracy: \t\t%d/%d=%f",
					corrsent, numsent,
					corrsent * 100.0
							/ numsent));
		}
		
		writer.close();
		
	}
	
	public static void evaluateDep(List<DepInstance> vecInstances1, List<DepInstance> vecInstances2, String outputFile) throws Exception {

		int totalInstances = vecInstances1.size();
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8"));
		if(totalInstances != vecInstances2.size()) 
		{
			output.println("Sentence Num do not match.");
			output.close();
			return;
		}

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
		
		int i = 0;
		for (; i < totalInstances; i++) {
			DepInstance tmpInstance = vecInstances1.get(i);
			DepInstance other = vecInstances2.get(i);

			if(!tmpInstance.evaluateWithOther(other))
			{
				output.println(String.format("Sentence %d is not matched.", i+1));
				output.close();
				return;
			}

			word_num_nopunc_cur = tmpInstance.eval_res[0];
			word_num_nopunc_dep_correct_cur = tmpInstance.eval_res[1];
			word_num_nopunc_deplabel_correct_cur = tmpInstance.eval_res[2];
			sent_num_word_all_nopunc_dep_correct_cur = tmpInstance.eval_res[3];
			sent_num_word_all_nopunc_deplabel_correct_cur = tmpInstance.eval_res[4];
			sent_num_root_correct_cur = tmpInstance.eval_res[5];

			word_num_nopunc_total += word_num_nopunc_cur;
			word_num_nopunc_dep_correct_total += word_num_nopunc_dep_correct_cur;
			word_num_nopunc_deplabel_correct_total += word_num_nopunc_deplabel_correct_cur;
			sent_num_word_all_nopunc_dep_correct_total += sent_num_word_all_nopunc_dep_correct_cur;
			sent_num_word_all_nopunc_deplabel_correct_total += sent_num_word_all_nopunc_deplabel_correct_cur;
			sent_num_root_correct_total += sent_num_root_correct_cur;

			if ((i + 1) % 1000 == 0) {
				output.println(String.format(
						"CM (excluding punc): \t\t%d/%d=%f",
						sent_num_word_all_nopunc_dep_correct_total, i + 1,
						sent_num_word_all_nopunc_dep_correct_total * 100.0
								/ (i + 1)));
				output.println(String.format(
						"CM_L (excluding punc): \t\t%d/%d=%f",
						sent_num_word_all_nopunc_deplabel_correct_total, i + 1,
						sent_num_word_all_nopunc_deplabel_correct_total * 100.0
								/ (i + 1)));
				output.println(String.format(
						"UAS (excluding punc): \t\t%d/%d=%f",
						word_num_nopunc_dep_correct_total,
						word_num_nopunc_total,
						word_num_nopunc_dep_correct_total * 100.0
								/ word_num_nopunc_total));
				output.println(String.format(
						"LAS (excluding punc): \t\t%d/%d=%f",
						word_num_nopunc_deplabel_correct_total,
						word_num_nopunc_total,
						word_num_nopunc_deplabel_correct_total * 100.0
								/ word_num_nopunc_total));
				output.println(String.format(
						"ROOT (excluding punc): \t\t%d/%d=%f",
						sent_num_root_correct_total, i + 1,
						sent_num_root_correct_total * 100.0 / (i + 1)));
				output.println();
			}
		}

		{
			output.println(String.format("CM (excluding punc): \t\t%d/%d=%f",
					sent_num_word_all_nopunc_dep_correct_total, i,
					sent_num_word_all_nopunc_dep_correct_total * 100.0 / (i)));
			output.println(String.format("CM_L (excluding punc): \t\t%d/%d=%f",
					sent_num_word_all_nopunc_deplabel_correct_total, i,
					sent_num_word_all_nopunc_deplabel_correct_total * 100.0
							/ (i)));
			output.println(String.format("UAS (excluding punc): \t\t%d/%d=%f",
					word_num_nopunc_dep_correct_total, word_num_nopunc_total,
					word_num_nopunc_dep_correct_total * 100.0
							/ word_num_nopunc_total));
			output.println(String.format("LAS (excluding punc): \t\t%d/%d=%f",
					word_num_nopunc_deplabel_correct_total,
					word_num_nopunc_total,
					word_num_nopunc_deplabel_correct_total * 100.0
							/ word_num_nopunc_total));
			output.println(String.format("ROOT (excluding punc): \t\t%d/%d=%f",
					sent_num_root_correct_total, i, sent_num_root_correct_total
							* 100.0 / (i)));
		}

		output.close();

	}
}
