package mason.dep;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class MergeResult {
	public List<DepInstance> m_vecInstances;

	public MergeResult() {
		m_vecInstances = new ArrayList<DepInstance>();
	}

	//file0 中的7、8列存处着gold result,其余的都认为是预测的结果
	public void Init(List<String> inputFiles) throws Exception {
		if (inputFiles == null || inputFiles.size() == 0)
			return;
		int totalFileSize = inputFiles.size();
		SDPCorpusReader[] ins = new SDPCorpusReader[totalFileSize];
		for(int i = 0; i < totalFileSize; i++)
		{
			ins[i] = new SDPCorpusReader();
			ins[i].Init(inputFiles.get(i));
		}
		int totalInstanceNum = ins[0].m_vecInstances.size();
		int totalPredResult = 0;
		if(ins[0].m_vecInstances.get(0).k_heads != null)
		{
			totalPredResult = ins[0].m_vecInstances.get(0).k_heads.length;
		}
		boolean bAccordance = true;
		
		for(int i = 1; i < totalFileSize; i++)
		{
			if(ins[i].m_vecInstances.size()!= totalInstanceNum)
			{
				bAccordance = false;
				break;
			}
			totalPredResult++;
			if(ins[i].m_vecInstances.get(0).k_heads != null)
			{
				totalPredResult += ins[i].m_vecInstances.get(i).k_heads.length;
			}
		}
		if(!bAccordance)return;
		
		for(int instnum = 0; instnum < totalInstanceNum; instnum++)
		{
			DepInstance tmpInstance = new DepInstance();
			int length = ins[0].m_vecInstances.get(instnum).forms.size();
			tmpInstance.k_scores = new double[totalPredResult];
		    tmpInstance.k_heads = new int[totalPredResult][length];
		    tmpInstance.k_deprels = new String[totalPredResult][length];
		    for (int k = 0; k < totalPredResult; k++) {
		    	tmpInstance.k_scores[k] = 1.0;
		    }
		    //no check in the depInstance
		    for(int j = 0; j < length; j++)
		    {
		    	tmpInstance.forms.add(ins[0].m_vecInstances.get(instnum).forms.get(j));
		    	tmpInstance.lemmas.add(ins[0].m_vecInstances.get(instnum).lemmas.get(j));
		    	tmpInstance.cpostags.add(ins[0].m_vecInstances.get(instnum).cpostags.get(j));
		    	tmpInstance.postags.add(ins[0].m_vecInstances.get(instnum).postags.get(j));		    	
		    	tmpInstance.heads.add(ins[0].m_vecInstances.get(instnum).heads.get(j));
			    tmpInstance.deprels.add(ins[0].m_vecInstances.get(instnum).deprels.get(j));
			    
			    int predId = 0;
			    if(ins[0].m_vecInstances.get(instnum).k_heads != null)
			    {
			    	int predLength0 = ins[0].m_vecInstances.get(instnum).k_heads.length;
			    	for(int curPredId = 0; curPredId < predLength0; curPredId++)
				    {
				    	tmpInstance.k_heads[curPredId][j] = ins[0].m_vecInstances.get(instnum).k_heads[curPredId][j];
				    	tmpInstance.k_deprels[curPredId][j] = ins[0].m_vecInstances.get(instnum).k_deprels[curPredId][j];
				    }
			    	predId += predLength0;
			    }
			    
			   
			    for(int i = 1; i < totalFileSize; i++)
			    {
			    	tmpInstance.k_heads[predId][j] = ins[i].m_vecInstances.get(instnum).heads.get(j);
			    	tmpInstance.k_deprels[predId][j] = ins[i].m_vecInstances.get(instnum).deprels.get(j);
			    	predId++;
			    	
			    	if(ins[i].m_vecInstances.get(instnum).k_heads != null)
				    {
				    	int predLength0 = ins[i].m_vecInstances.get(instnum).k_heads.length;
				    	for(int curPredId = 0; curPredId < predLength0; curPredId++)
					    {
					    	tmpInstance.k_heads[predId+curPredId][j] = ins[i].m_vecInstances.get(instnum).k_heads[curPredId][j];
					    	tmpInstance.k_deprels[predId+curPredId][j] = ins[i].m_vecInstances.get(instnum).k_deprels[curPredId][j];
					    }
				    	predId += predLength0;
				    }
			    }
		    }
		    
		    m_vecInstances.add(tmpInstance);
		}
		
		
	}
	
	
	public void evaluateOral(String outputFile) throws Exception {
		List<DepInstance> vecInstances = m_vecInstances;
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
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8"));
		int i = 0;
		for (; i < totalInstances; i++) {
			DepInstance tmpInstance = vecInstances.get(i);

			tmpInstance.evaluateAll();

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
