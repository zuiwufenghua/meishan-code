package acl2014;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import acl2014.*;

public class CharDependencyAnalysis {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
	
		int startarg = 0;
		Set<String> worddict = new HashSet<String>();
		if(args[0].equals("-dict"))
		{
			startarg = 2;
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(args[1]), "UTF8"));
			String sLine = null;
			while ((sLine = in.readLine()) != null) {
				if(sLine.trim().equals("") || sLine.trim().length() < 2)continue;
				String[] wordposs = sLine.trim().split("\\s+");
				worddict.add(wordposs[0]);
			}
			in.close();
		}
		
		CharDependencyReader cdpCorpusReader1 = new CharDependencyReader();
		cdpCorpusReader1.Init(args[startarg]);
		CharDependencyReader cdpCorpusReader2 = new CharDependencyReader();
		cdpCorpusReader2.Init(args[startarg+1]);
		
		PrintWriter writer  = new PrintWriter(System.out);
		if(args.length > startarg+2)
		{
			writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[startarg+2]), "UTF-8"));
		}
		
		analysisDep(cdpCorpusReader1.m_vecInstances, cdpCorpusReader2.m_vecInstances, worddict, writer);

		writer.close();
	}
	
	
	
	public static void analysisDep(List<CharDependency> vecInstances1, List<CharDependency> vecInstances2, Set<String> worddict, PrintWriter output) throws Exception {

		int totalInstances = vecInstances1.size();
		if(totalInstances != vecInstances2.size()) 
		{
			output.println("Sentence Num do not match.");
			output.close();
			return;
		}
		
		// for evaluate
		// 0 cur_sent_word_num;  1 other_sent_word_num;
		// 2 out_uas_correct_num; 3 out_las_correct_num;  
		// 4 root_correct; (above the word); 5 gold_dep_num; 6 pred_dep_num;
		// 7 in_uas_correct_num; 8 gold_in_uas_num; 9 pred_in_uas_num
		// 10 word_structure_correct; 11 word_correct;  12 pos_correct; 
		//
		int total_gold_word_num  = 0, total_pred_word_num = 0;
		int total_out_uas_correct_num = 0, total_out_las_correct_num = 0, total_out_uas_wrong_num_wordposcorrect = 0, total_out_las_wrong_num_wordposcorrect = 0;
		int total_out_uas_wrong_num_wordstructurecorrect = 0, total_out_las_wrong_num_wordstructurecorrect = 0;
		int total_out_uas_correct_num_wordstructure_correct = 0, total_out_las_correct_num_wordstructure_correct = 0;
		int total_root_correct_num = 0, total_root_correct_num_wordstructure_correct = 0, total_sent_num = 0;
		int total_gold_dep_num = 0, total_pred_dep_num = 0;
		int total_in_uas_correct_num = 0, total_gold_in_uas_num = 0, total_pred_in_uas_num = 0; 
		int total_word_structure_correct = 0, total_word_correct = 0, total_pos_correct = 0; 
		int total_oovword_num = 0, total_oovword_structure_correct = 0, total_oovword_correct = 0, total_oovpos_correct = 0; 

		

		
		int i = 0;
		for (; i < totalInstances; i++) {
			CharDependency tmpInstance = vecInstances1.get(i);
			tmpInstance.init();
			CharDependency other = vecInstances2.get(i);
			other.init();

			if(!tmpInstance.analysisWithOther(other, worddict))
			{
				output.println(String.format("Sentence %d is not matched.", i+1));
				output.close();
				return;
			}
			total_sent_num++;
			int gold_word_num  = tmpInstance.eval_res[0], pred_word_num = tmpInstance.eval_res[1];
			int out_uas_correct_num = tmpInstance.eval_res[2], out_las_correct_num = tmpInstance.eval_res[3];
			int root_correct_num = tmpInstance.eval_res[4];
			int gold_dep_num = tmpInstance.eval_res[5], pred_dep_num = tmpInstance.eval_res[6];
			int in_uas_correct_num = tmpInstance.eval_res[7], gold_in_uas_num = tmpInstance.eval_res[8], pred_in_uas_num = tmpInstance.eval_res[9]; 
			int word_structure_correct = tmpInstance.eval_res[10], word_correct = tmpInstance.eval_res[11], pos_correct = tmpInstance.eval_res[12];
			//int out_arc_correct_num = tmpInstance.eval_res[13], out_arclabel_correct_num = tmpInstance.eval_res[14];
			int out_uas_correct_num_wordstructure_correct = tmpInstance.eval_res[15];
			int root_correct_num_wordstructure_correct = tmpInstance.eval_res[16];
			int out_las_correct_num_wordstructure_correct = tmpInstance.eval_res[17];
			int out_uas_wrong_num_wordposcorrect = tmpInstance.eval_res[18];
			int out_uas_wrong_num_wordstructurecorrect = tmpInstance.eval_res[19];
			int out_las_wrong_num_wordposcorrect = tmpInstance.eval_res[20];
			int out_las_wrong_num_wordstructurecorrect = tmpInstance.eval_res[21];
			int oovword_num = tmpInstance.eval_res[22];
			int oovword_correct = tmpInstance.eval_res[23];
			int oovpos_correct = tmpInstance.eval_res[24];
			int oovword_structure_correct = tmpInstance.eval_res[25];
			

			total_gold_word_num  += gold_word_num;
			total_pred_word_num += pred_word_num;
			total_out_uas_correct_num += out_uas_correct_num;
			total_out_las_correct_num += out_las_correct_num;
			total_root_correct_num += root_correct_num;
			total_gold_dep_num += gold_dep_num;
			total_pred_dep_num += pred_dep_num;
			total_in_uas_correct_num += in_uas_correct_num;
			total_gold_in_uas_num += gold_in_uas_num;
			total_pred_in_uas_num += pred_in_uas_num; 
			total_word_structure_correct += word_structure_correct;
			total_word_correct += word_correct;
			total_pos_correct += pos_correct; 
			
			total_out_uas_correct_num_wordstructure_correct += out_uas_correct_num_wordstructure_correct;
			total_root_correct_num_wordstructure_correct += root_correct_num_wordstructure_correct;
			total_out_las_correct_num_wordstructure_correct += out_las_correct_num_wordstructure_correct;
			total_out_uas_wrong_num_wordposcorrect += out_uas_wrong_num_wordposcorrect;
			total_out_uas_wrong_num_wordstructurecorrect += out_uas_wrong_num_wordstructurecorrect;
			total_out_las_wrong_num_wordposcorrect += out_las_wrong_num_wordposcorrect;
			total_out_las_wrong_num_wordstructurecorrect += out_las_wrong_num_wordstructurecorrect;
			total_oovword_num += oovword_num;
			total_oovword_correct += oovword_correct;
			total_oovpos_correct += oovpos_correct;
			total_oovword_structure_correct += oovword_structure_correct;

			
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
			displayPRF("segmentation:\t", total_word_correct, total_pred_word_num, total_gold_word_num, output);
			displayPRF("tagging:\t", total_pos_correct, total_pred_word_num, total_gold_word_num, output);
			displayPRF("wordstructure:\t", total_word_structure_correct, total_pred_word_num, total_gold_word_num, output);
			
			displayPRF("oov segmentation:\t", total_oovword_correct, total_oovword_num, total_oovword_num, output);
			displayPRF("oov tagging:\t", total_oovpos_correct, total_oovword_num, total_oovword_num, output);
			displayPRF("oov wordstructure:\t", total_oovword_structure_correct, total_oovword_num, total_oovword_num, output);
			
			displayPRF("inworduas:\t", total_in_uas_correct_num, total_pred_in_uas_num, total_gold_in_uas_num, output);
			
			
			displayPRF("uas:\t", total_out_uas_correct_num, total_pred_dep_num, total_gold_dep_num, output);
			displayPRF("uas word structure correct:\t", total_out_uas_correct_num_wordstructure_correct, total_out_uas_correct_num, total_out_uas_correct_num, output);
			
			displayPRF("las:\t", total_out_las_correct_num, total_pred_dep_num, total_gold_dep_num, output);
			displayPRF("las word structure correct:\t", total_out_las_correct_num_wordstructure_correct, total_out_las_correct_num, total_out_las_correct_num, output);
			
			displayPRF("root:\t", total_root_correct_num, total_sent_num, total_sent_num, output);
			displayPRF("root word structure correct:\t", total_root_correct_num_wordstructure_correct, total_root_correct_num, total_root_correct_num, output);
			
			displayPRF("uas wrong wordpos correct:\t", total_out_uas_wrong_num_wordposcorrect, total_pred_dep_num - total_out_uas_correct_num, total_gold_dep_num - total_out_uas_correct_num, output);
			displayPRF("uas wrong word structure correct:\t", total_out_uas_wrong_num_wordstructurecorrect, total_pred_dep_num - total_out_uas_correct_num, total_gold_dep_num - total_out_uas_correct_num, output);

			displayPRF("las wrong wordpos correct:\t", total_out_las_wrong_num_wordposcorrect, total_pred_dep_num - total_out_las_correct_num, total_gold_dep_num - total_out_las_correct_num, output);
			displayPRF("las wrong word structure correct:\t", total_out_las_wrong_num_wordstructurecorrect, total_pred_dep_num - total_out_las_correct_num, total_gold_dep_num - total_out_las_correct_num, output);


		}


	}
	
	
	public static double displayPRF(String prefixStr, int correct, int guessed,
			int gold,  PrintWriter pw) {
		double precision = (guessed > 0 ? correct / (double) guessed : 1.0);
		double recall = (gold > 0 ? correct / (double) gold : 1.0);
		double f1 = (precision > 0.0 && recall > 0.0 ? 2.0 / (1.0 / precision + 1.0 / recall)
				: 0.0);


		String displayStr = " P: " + correct + "/" + guessed + "=" +((int) (precision * 10000)) / 100.0
				+ " R: " + correct + "/" + gold + "="  + ((int) (recall * 10000)) / 100.0 + " F1: "
				+ ((int) (f1 * 10000)) / 100.0 ;

		if (pw != null)
			pw.println(prefixStr + displayStr);
		
		return f1;
	}

}
