package process;


import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

public class ComputeOracleDep {

	/**
	 * @param args
	 */
	// arc filter����feat1��,label����feat2�У�pos filter����feat3��
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();
		sdpCorpusReader.Init(args[0]);

		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		int totalInstances = vecInstances.size();
		

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
		
		int word_num_pos_right = 0;
		int word_num_pos_total = 0;
		int sent_num_pos_right = 0;
		
		int word_num_pos_right_cur = 0;
		int word_num_pos_total_cur = 0;
		int sent_num_pos_right_cur = 0;
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"));
		PrintWriter output_containgold = null;
		if(args.length > 2)
		{
			output_containgold = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"));
		}
		int i = 0;
		Set<Integer> printParams = new HashSet<Integer>();
		for(int idx = 1; idx < 12; idx++)
		{
			if(idx == 4) continue;
			printParams.add(idx);
		}
		for (; i < totalInstances; i++) {
			DepInstance tmpInstance = vecInstances.get(i);

			List<String> oHeads = new ArrayList<String>();
			List<String> oPostags = new ArrayList<String>();
			List<String> oDeprels = new ArrayList<String>();
			
			tmpInstance.evaluateAll(oHeads,oDeprels,oPostags);
			word_num_pos_total_cur = tmpInstance.forms.size();
			word_num_pos_right_cur = tmpInstance.eval_res[6];
			sent_num_pos_right_cur = tmpInstance.eval_res[7];
			
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
			
			word_num_pos_total += word_num_pos_total_cur;
			word_num_pos_right += word_num_pos_right_cur;
			sent_num_pos_right += sent_num_pos_right_cur;
			
			if(output_containgold != null)
			{
				//if(!oHeads.equals("_") && oHeads.get(index))
				
				for(int idx = 0; idx < tmpInstance.forms.size(); idx++)
				{
					if(!oHeads.get(idx).equals("_") 
					&& !oHeads.get(idx).equals(tmpInstance.heads.get(idx).toString()))
					{
						String newfeat1 = tmpInstance.feats1.get(idx) + "_" + tmpInstance.heads.get(idx).toString();
						tmpInstance.feats1.set(idx, newfeat1);
					}
					
					if(!oDeprels.get(idx).equals("_") 
					&& !oDeprels.get(idx).equals(tmpInstance.deprels.get(idx).toString()))
					{
						String newfeat2 = tmpInstance.feats2.get(idx) + "_" + tmpInstance.deprels.get(idx);
						tmpInstance.feats2.set(idx, newfeat2);
					}
					String goldpos = tmpInstance.postags.get(idx);
					if(goldpos.equals("_"))goldpos = tmpInstance.cpostags.get(idx);
					if(!oPostags.get(idx).equals("_") 
					&& !oPostags.get(idx).equals(goldpos))
					{
						String newfeat3 = tmpInstance.feats3.get(idx) + "_" + goldpos;
						tmpInstance.feats3.set(idx, newfeat3);
					}	
					
					output_containgold.println(tmpInstance.toString(printParams, 12, idx));
				}
				
				output_containgold.println();
			}
			

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
				
				output.println(String.format(
						"Pos Accuracy: \t\t%d/%d=%f",
						word_num_pos_right, word_num_pos_total,
						word_num_pos_right * 100.0
								/ word_num_pos_total));
				output.println(String.format(
						"All sentence accuracy: \t\t%d/%d=%f",
						sent_num_pos_right, (i+1),
						sent_num_pos_right * 100.0
								/ (i+1)));
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
			output.println(String.format(
					"Pos Accuracy: \t\t%d/%d=%f",
					word_num_pos_right, word_num_pos_total,
					word_num_pos_right * 100.0
							/ word_num_pos_total));
			output.println(String.format(
					"All sentence accuracy: \t\t%d/%d=%f",
					sent_num_pos_right, (i),
					sent_num_pos_right * 100.0
							/ (i)));
		}

		output.close();
		if(output_containgold != null)output_containgold.close();
						
	}

}
