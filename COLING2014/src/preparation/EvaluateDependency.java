package preparation;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;


// Zhang Yue's input
public class EvaluateDependency {

	public static void main(String[] args) throws Exception {
		SDPCorpusReader sdpCorpusReader1 = new SDPCorpusReader(true);
		sdpCorpusReader1.Init(args[0]);
		SDPCorpusReader sdpCorpusReader2 = new SDPCorpusReader(true);
		sdpCorpusReader2.Init(args[1]);	
		
		int multi = Integer.parseInt(args[2]);

		PrintWriter writer  = new PrintWriter(System.out);
		if(args.length > 3)
		{
			writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[3]), "UTF-8"));
		}
		
		List<List<DepInstance>> goldinstances = new ArrayList<List<DepInstance>>();
		List<List<DepInstance>> predinstances = new ArrayList<List<DepInstance>>();
		
		for(int idx = 0; idx < multi; idx++)
		{
			goldinstances.add(new ArrayList<DepInstance>());
			predinstances.add(new ArrayList<DepInstance>());
		}
		
		int totalInstances = sdpCorpusReader1.m_vecInstances.size();
		if(totalInstances%multi != 0)
		{
			System.out.println("Some sentences lack some results...");
			return;
		}
		
		for(int idx = 0; idx < totalInstances; idx++)
		{
			goldinstances.get(idx%multi).add(sdpCorpusReader1.m_vecInstances.get(idx));
			predinstances.get(idx%multi).add(sdpCorpusReader2.m_vecInstances.get(idx));
		}
		//int iType = Integer.parseInt(args[2]);
		//if(iType == 0)
		//{
		//EvaluatePosTagger(sdpCorpusReader1.m_vecInstances,sdpCorpusReader2.m_vecInstances, writer);
		for(int idx = 0; idx < multi; idx++)
		{
			writer.println(String.format("Evaluate schema %d" , idx));
			evaluateDep(goldinstances.get(idx),predinstances.get(idx), writer);
		}
			
		//}
		//else if(iType == 1)
		//{
		//	EvaluatePosTagger(sdpCorpusReader1.m_vecInstances,sdpCorpusReader2.m_vecInstances, outputFilePos);
		//}
		//else if(iType == 2)
		//{
		//	evaluateDep(sdpCorpusReader1.m_vecInstances,sdpCorpusReader2.m_vecInstances, outputFileDep);
		//}
		writer.close();
	}
	
	
	public static void evaluateDep(List<DepInstance> vecInstances1, List<DepInstance> vecInstances2, PrintWriter output) throws Exception {

		int totalInstances = vecInstances1.size();
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
			
			/*
			if ((i + 1) % 1000 == 0) {
				output.println(String.format(
						"CM (excluding punc): \t\t%d/%d=%f",
						sent_num_word_all_nopunc_dep_correct_total, i + 1,
						sent_num_word_all_nopunc_dep_correct_total * 100.0
								/ (i + 1)));
				//output.println(String.format(
				//		"CM_L (excluding punc): \t\t%d/%d=%f",
				//		sent_num_word_all_nopunc_deplabel_correct_total, i + 1,
				//		sent_num_word_all_nopunc_deplabel_correct_total * 100.0
				//				/ (i + 1)));
				output.println(String.format(
						"UAS (excluding punc): \t\t%d/%d=%f",
						word_num_nopunc_dep_correct_total,
						word_num_nopunc_total,
						word_num_nopunc_dep_correct_total * 100.0
								/ word_num_nopunc_total));
				//output.println(String.format(
				//		"LAS (excluding punc): \t\t%d/%d=%f",
				//		word_num_nopunc_deplabel_correct_total,
				//		word_num_nopunc_total,
				//		word_num_nopunc_deplabel_correct_total * 100.0
				//				/ word_num_nopunc_total));
				output.println(String.format(
						"ROOT (excluding punc): \t\t%d/%d=%f",
						sent_num_root_correct_total, i + 1,
						sent_num_root_correct_total * 100.0 / (i + 1)));
				output.println();
			}*/
		}

		{
			output.println(String.format("CM (excluding punc): \t\t%d/%d=%f",
					sent_num_word_all_nopunc_dep_correct_total, i,
					sent_num_word_all_nopunc_dep_correct_total * 100.0 / (i)));
			//output.println(String.format("CM_L (excluding punc): \t\t%d/%d=%f",
			//		sent_num_word_all_nopunc_deplabel_correct_total, i,
			//		sent_num_word_all_nopunc_deplabel_correct_total * 100.0
			//				/ (i)));
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


	}
}
