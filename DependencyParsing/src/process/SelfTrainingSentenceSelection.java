package process;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

public class SelfTrainingSentenceSelection {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
			
		int threshold = Integer.parseInt(args[4]);
		File file1 = new File(args[0]);
		String[] subFilenames1 = file1.list();
		File file2 = new File(args[1]);
		String[] subFilenames2 = file2.list();
		File file3 = new File(args[2]);
		String[] subFilenames3 = file3.list();
		if(subFilenames1.length != subFilenames2.length || subFilenames1.length != subFilenames3.length)
		{
			System.out.println("Files number not equal in the two folders!");
			return;
		}
		
		Map<String, String> subFilenames1Info1 = new HashMap<String, String>();
		for (String subFilename : subFilenames1)
		{
			int splitIndex = subFilename.indexOf("split.");
			if(splitIndex == -1)
			{
				System.out.println("Filename" + subFilename +" not match standard.");
				return;
			}
			
			String keyIndex = subFilename.substring(splitIndex, splitIndex + 7);
			subFilenames1Info1.put(keyIndex, subFilename);
		}
		
		Map<String, String> subFilenames2Info2 = new HashMap<String, String>();
		for (String subFilename : subFilenames2)
		{
			int splitIndex = subFilename.indexOf("split.");
			if(splitIndex == -1)
			{
				System.out.println("Filename" + subFilename +" not match standard.");
				return;
			}
			
			String keyIndex = subFilename.substring(splitIndex, splitIndex + 7);
			subFilenames2Info2.put(keyIndex, subFilename);
		}
		
		Map<String, String> subFilenames3Info3 = new HashMap<String, String>();
		for (String subFilename : subFilenames3)
		{
			int splitIndex = subFilename.indexOf("split.");
			if(splitIndex == -1)
			{
				System.out.println("Filename" + subFilename +" not match standard.");
				return;
			}
			
			String keyIndex = subFilename.substring(splitIndex, splitIndex + 7);
			subFilenames3Info3.put(keyIndex, subFilename);
		}
		
		
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
				args[3]), "UTF-8"));
		for (String keyIndex : subFilenames1Info1.keySet()) {
			if(!subFilenames2Info2.containsKey(keyIndex) || !subFilenames3Info3.containsKey(keyIndex))
			{
				System.out.println("Filename " + subFilenames1Info1.get(keyIndex) +" does not have corresponding file in fold2.");
				writer.close();
				return;
			}
			String inputFile1 = args[0] + File.separator + subFilenames1Info1.get(keyIndex);
			String inputFile2 = args[1] + File.separator + subFilenames2Info2.get(keyIndex);
			String inputFile3 = args[2] + File.separator + subFilenames3Info3.get(keyIndex);
			
			processOneFile(inputFile1, inputFile2, inputFile3, writer, threshold);			
		}
		System.out.println("Finished!");
		writer.close();
	}
	
	public static void processOneFile(String inputFile1, String inputFile2, String inputFile3, PrintWriter writer, int threshold) throws Exception{
		// TODO Auto-generated method stub
		System.out.println("Processing file " + inputFile1 + " and " + inputFile2 + " and " + inputFile3 + " ......");
		SDPCorpusReader sdpCorpusReader1 = new SDPCorpusReader();
		sdpCorpusReader1.Init(inputFile1);

		List<DepInstance> vecInstances1 = sdpCorpusReader1.m_vecInstances;
		int totalInstances = vecInstances1.size();
		
		SDPCorpusReader sdpCorpusReader2 = new SDPCorpusReader();
		sdpCorpusReader2.Init(inputFile2);
		
		SDPCorpusReader sdpCorpusReader3 = new SDPCorpusReader();
		sdpCorpusReader3.Init(inputFile3);

		List<DepInstance> vecInstances2 = sdpCorpusReader2.m_vecInstances;
		int totalInstances2 = vecInstances2.size();
		
		List<DepInstance> vecInstances3 = sdpCorpusReader3.m_vecInstances;
		int totalInstances3 = vecInstances3.size();
		
		if(totalInstances != totalInstances2 || totalInstances != totalInstances3) 
		{
			System.out.println("Sentence Num do not match.");
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
		
		for(; i < totalInstances; i++)
		{
			DepInstance tmpInstance = vecInstances1.get(i);
			List<DepInstance> others = new ArrayList<DepInstance>();
			others.add(vecInstances2.get(i));
			others.add(vecInstances3.get(i));

			if(!tmpInstance.evaluateWithOthers(others))
			{
				System.out.println(String.format("Sentence %d is not matched.", i+1));
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
			
			if(sent_num_word_all_nopunc_deplabel_correct_cur == 1)
			{
				List<String> outputResult = new ArrayList<String>();
				tmpInstance.toGoldListString(outputResult);
				
				int outsize = outputResult.size();
				if (outsize <= threshold)
				{
					continue;
				}
				for (int k = 0; k < outsize; k++) {
					writer.println(outputResult.get(k));
				}
				writer.println();
				writer.flush();
			}
			
			if ((i + 1) % 1000 == 0) {
				System.out.println(String.format(
						"CM (excluding punc): \t\t%d/%d=%f",
						sent_num_word_all_nopunc_dep_correct_total, i + 1,
						sent_num_word_all_nopunc_dep_correct_total * 100.0
								/ (i + 1)));
				System.out.println(String.format(
						"CM_L (excluding punc): \t\t%d/%d=%f",
						sent_num_word_all_nopunc_deplabel_correct_total, i + 1,
						sent_num_word_all_nopunc_deplabel_correct_total * 100.0
								/ (i + 1)));
				System.out.println(String.format(
						"UAS (excluding punc): \t\t%d/%d=%f",
						word_num_nopunc_dep_correct_total,
						word_num_nopunc_total,
						word_num_nopunc_dep_correct_total * 100.0
								/ word_num_nopunc_total));
				System.out.println(String.format(
						"LAS (excluding punc): \t\t%d/%d=%f",
						word_num_nopunc_deplabel_correct_total,
						word_num_nopunc_total,
						word_num_nopunc_deplabel_correct_total * 100.0
								/ word_num_nopunc_total));
				System.out.println(String.format(
						"ROOT (excluding punc): \t\t%d/%d=%f",
						sent_num_root_correct_total, i + 1,
						sent_num_root_correct_total * 100.0 / (i + 1)));
				System.out.println();
				System.out.flush();
			}

		}
		
		{
			System.out.println(String.format(
					"CM (excluding punc): \t\t%d/%d=%f",
					sent_num_word_all_nopunc_dep_correct_total, i + 1,
					sent_num_word_all_nopunc_dep_correct_total * 100.0
							/ (i + 1)));
			System.out.println(String.format(
					"CM_L (excluding punc): \t\t%d/%d=%f",
					sent_num_word_all_nopunc_deplabel_correct_total, i + 1,
					sent_num_word_all_nopunc_deplabel_correct_total * 100.0
							/ (i + 1)));
			System.out.println(String.format(
					"UAS (excluding punc): \t\t%d/%d=%f",
					word_num_nopunc_dep_correct_total,
					word_num_nopunc_total,
					word_num_nopunc_dep_correct_total * 100.0
							/ word_num_nopunc_total));
			System.out.println(String.format(
					"LAS (excluding punc): \t\t%d/%d=%f",
					word_num_nopunc_deplabel_correct_total,
					word_num_nopunc_total,
					word_num_nopunc_deplabel_correct_total * 100.0
							/ word_num_nopunc_total));
			System.out.println(String.format(
					"ROOT (excluding punc): \t\t%d/%d=%f",
					sent_num_root_correct_total, i + 1,
					sent_num_root_correct_total * 100.0 / (i + 1)));
			System.out.println();
			System.out.flush();
		}
		
	}

}
