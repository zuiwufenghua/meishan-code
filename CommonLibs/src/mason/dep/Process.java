package mason.dep;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.*;

public class Process {
	SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();

	// SDPCorpusReader sdpCorpusReader_ex = new SDPCorpusReader();

	public void init(String inputFile) throws Exception {
		sdpCorpusReader.Init(inputFile);
		// sdpCorpusReader_ex.Init(excludeFile, null);
	}

	public void errorStatistic(String outputFile) throws Exception {
		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		int totalInstances = vecInstances.size();
		Map<String, Double> errorContents = new HashMap<String, Double>();
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8"));
		for (int i = 0; i < totalInstances; i++) {
			DepInstance tmpInstance = vecInstances.get(i);
			tmpInstance.ComputeProb();
			boolean bValid = true;
			for (int j = 0; j < tmpInstance.size(); j++) {
				int goldHeadId = tmpInstance.heads.get(j) - 1;
				int predHeadId = tmpInstance.k_heads[0][j] - 1;
				String goldHead = "ROOT";
				String predHead = "ROOT";
				String goldHeadPos = "ROOT";
				String predHeadPos = "ROOT";
				if (goldHeadId < tmpInstance.size() && goldHeadId >= 0) {
					goldHead = tmpInstance.forms.get(goldHeadId);
					goldHeadPos = tmpInstance.cpostags.get(goldHeadId);
				}
				if (predHeadId < tmpInstance.size() && predHeadId >= 0) {
					predHead = tmpInstance.forms.get(predHeadId);
					predHeadPos = tmpInstance.cpostags.get(predHeadId);
				}

				if (DepInstance.isPunc(goldHeadPos)
						|| DepInstance.isPunc(predHeadPos)) {
					output.println(String.format("error word number:%d", j + 1));
					List<String> curOutPuts = new ArrayList<String>();
					tmpInstance.toAllPredListString(curOutPuts);
					int outsize = curOutPuts.size();
					if (outsize == 0)
						continue;
					for (int k = 0; k < outsize; k++) {
						output.println(curOutPuts.get(k));
					}
					output.println();
					output.flush();
					bValid = false;
					break;
				}
			}
			if (!bValid)
				continue;
			for (int j = 0; j < tmpInstance.size(); j++) {
				if (DepInstance.isPunc(tmpInstance.cpostags.get(j)))
					continue;
				int goldHeadId = tmpInstance.heads.get(j) - 1;
				int predHeadId = tmpInstance.k_heads[0][j] - 1;
				String goldHead = "ROOT";
				String predHead = "ROOT";
				String goldHeadPos = "ROOT";
				String predHeadPos = "ROOT";
				if (goldHeadId < tmpInstance.size() && goldHeadId >= 0) {
					goldHead = tmpInstance.forms.get(goldHeadId);
					goldHeadPos = tmpInstance.cpostags.get(goldHeadId);
				}
				if (predHeadId < tmpInstance.size() && predHeadId >= 0) {
					predHead = tmpInstance.forms.get(predHeadId);
					predHeadPos = tmpInstance.cpostags.get(predHeadId);
				}
				String goldLabel = tmpInstance.deprels.get(j);
				String predLabel = tmpInstance.k_deprels[0][j];
				String prefix = tmpInstance.forms.get(j);
				if (!goldHead.equals(predHead)) {
					String errorContent = "!head#=#" + prefix + "#=#" + goldHead + "#=#"
							+ predHead;
					if (errorContents.containsKey(errorContent)) {
						errorContents.put(errorContent,
								errorContents.get(errorContent) + 1.0);
					} else {
						errorContents.put(errorContent, 1.0);
					}
				} else {
					String errorContent = "head#=#" + prefix + "#=#" + goldHead + "#=#"
							+ predHead;
					if (errorContents.containsKey(errorContent)) {
						errorContents.put(errorContent,
								errorContents.get(errorContent) + 1.0);
					} else {
						errorContents.put(errorContent, 1.0);
					}
				}

				if (!goldLabel.equals(predLabel)) {
					String errorContent = "!label#=#" + prefix + "#=#" + goldLabel + "#=#"
							+ predLabel;
					if (errorContents.containsKey(errorContent)) {
						errorContents.put(errorContent,
								errorContents.get(errorContent) + 1.0);
					} else {
						errorContents.put(errorContent, 1.0);
					}
				} else {
					String errorContent = "label#=#" + prefix + "#=#" + goldLabel + "#=#"
							+ predLabel;
					if (errorContents.containsKey(errorContent)) {
						errorContents.put(errorContent,
								errorContents.get(errorContent) + 1.0);
					} else {
						errorContents.put(errorContent, 1.0);
					}
				}

				if (!goldLabel.equals(predLabel) || !goldHead.equals(predHead)) {
					String errorContent = "!head&label#=#" + prefix + "#=#" + goldHead + "__"
							+ goldLabel + "#=#" + predHead + "__" + predLabel;
					if (errorContents.containsKey(errorContent)) {
						errorContents.put(errorContent,
								errorContents.get(errorContent) + 1.0);
					} else {
						errorContents.put(errorContent, 1.0);
					}
				} else {
					String errorContent = "head_label#=#" + prefix + "#=#" + goldHead + "__"
							+ goldLabel + "#=#" + predHead + "__" + predLabel;
					if (errorContents.containsKey(errorContent)) {
						errorContents.put(errorContent,
								errorContents.get(errorContent) + 1.0);
					} else {
						errorContents.put(errorContent, 1.0);
					}
				}

				if (!goldLabel.equals(predLabel) && goldHead.equals(predHead)) {
					String errorContent = "head_!label#=#" + prefix + "#=#" + goldLabel + "#=#"
							+ predLabel;
					if (errorContents.containsKey(errorContent)) {
						errorContents.put(errorContent,
								errorContents.get(errorContent) + 1.0);
					} else {
						errorContents.put(errorContent, 1.0);
					}
				}

				if (goldLabel.equals(predLabel) && !goldHead.equals(predHead)) {
					String errorContent = "!head_label#=#" + prefix + "#=#" + goldHead + "#=#"
							+ predHead;
					if (errorContents.containsKey(errorContent)) {
						errorContents.put(errorContent,
								errorContents.get(errorContent) + 1.0);
					} else {
						errorContents.put(errorContent, 1.0);
					}
				}

				if (!goldLabel.equals(predLabel) && !goldHead.equals(predHead)) {
					String errorContent = "!head_!label#=#" + prefix + "#=#" + goldHead + "__"
							+ goldLabel + "#=#" + predHead + "__" + predLabel;
					if (errorContents.containsKey(errorContent)) {
						errorContents.put(errorContent,
								errorContents.get(errorContent) + 1.0);
					} else {
						errorContents.put(errorContent, 1.0);
					}
				}
			}

		}
		List<DepErrors> newDepErrors = new ArrayList<DepErrors>();
		for (String strContent : errorContents.keySet()) {
			newDepErrors.add(new DepErrors(strContent, errorContents
					.get(strContent)));
		}
		Collections.sort(newDepErrors, new DepErrorsCompare());

		for (DepErrors depError : newDepErrors) {
			output.println(depError.print());
		}

		output.close();

	}

	public void errorAnalysis(String outputFile, String clause)
			throws Exception {
		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		int totalInstances = vecInstances.size();
		// Map<String, Double> errorContents = new HashMap<String, Double>();
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8"));
		String sepString = "#=#";

		for (int i = 0; i < totalInstances; i++) {
			DepInstance tmpInstance = vecInstances.get(i);

			for (int j = 0; j < tmpInstance.size(); j++) {
				if (DepInstance.isPunc(tmpInstance.cpostags.get(j))
						|| DepInstance.isPunc(tmpInstance.postags.get(j)))
					continue;
				int goldHeadId = tmpInstance.heads.get(j) - 1;
				int predHeadId = tmpInstance.k_heads[0][j] - 1;
				String goldHead = "ROOT";
				String predHead = "ROOT";
				String goldHeadPos = "ROOT";
				String predHeadPos = "ROOT";
				if (goldHeadId < tmpInstance.size() && goldHeadId >= 0) {
					goldHead = tmpInstance.forms.get(goldHeadId);
					goldHeadPos = tmpInstance.cpostags.get(goldHeadId);
				}
				if (predHeadId < tmpInstance.size() && predHeadId >= 0) {
					predHead = tmpInstance.forms.get(predHeadId);
					predHeadPos = tmpInstance.cpostags.get(predHeadId);
				}
				String goldLabel = tmpInstance.deprels.get(j);
				String predLabel = tmpInstance.k_deprels[0][j];
				String curWord = tmpInstance.forms.get(j);
				String curPos = tmpInstance.cpostags.get(j);

				goldHead = "ghf:" + goldHead;
				predHead = "phf:" + predHead;

				goldLabel = "gl:" + goldLabel;
				predLabel = "pl:" + predLabel;

				curWord = "cf:" + curWord;
				curPos = "ct:" + curPos;

				goldHeadPos = "ght:" + goldHeadPos;
				predHeadPos = "pht:" + predHeadPos;

				Set<String> errorType = new HashSet<String>();
				// �뵽ʲô��ʲô
				errorType.add(goldLabel + sepString + predLabel);
				if (errorType.contains(clause)) {
					output.println(String.format("error word number:%d", j + 1));
					List<String> curOutPuts = new ArrayList<String>();
					tmpInstance.toBestListString(curOutPuts);
					int outsize = curOutPuts.size();
					if (outsize == 0)
						continue;
					for (int k = 0; k < outsize; k++) {
						output.println(curOutPuts.get(k));
					}
					output.println();
					output.flush();
					break;
				}

			}

		}

		output.close();

	}

	public void evaluate(String outputFile, int option, double marginal) throws Exception {
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
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8"));
		int i = 0;
		for (; i < totalInstances; i++) {
			DepInstance tmpInstance = vecInstances.get(i);
			if (option == 0) {
				tmpInstance.evaluate1Best();
			} else {
				tmpInstance.evaluateMarginBest(marginal);
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

	public void Output(String outputFile, int options) throws Exception {
		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		int totalInstances = vecInstances.size();
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8"));

		int i = 0;
		for (; i < totalInstances; i++) {
			DepInstance tmpInstance = vecInstances.get(i);
			tmpInstance.ComputeProb();
			List<String> curOutPuts = new ArrayList<String>();
			if (options == 1) {
				tmpInstance.toGoldListString(curOutPuts);
			} else if (options == 2) {
				tmpInstance.toPredListString(curOutPuts);
			} else if (options == 3) {
				tmpInstance.toAllPredListString(curOutPuts);
			} else if (options == 4) {
				tmpInstance.toBestProbListString(curOutPuts);
			} else {

			}
			int outsize = curOutPuts.size();
			if (outsize == 0)
				continue;
			for (int j = 0; j < outsize; j++) {
				output.println(curOutPuts.get(j));
			}
			output.println();
		}

		output.close();
	}

	public void RandSplit(String outputFile, int K) throws Exception {
		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		int totalInstances = vecInstances.size();
		
		

		int i = 0;
		//for (; i < totalInstances; i++) {
		//	DepInstance tmpInstance = vecInstances.get(i);
		//	tmpInstance.ComputeProb();
		//	tmpInstance.computeActiveConfidence();
		//}
		int eachFileInstanceNum = (totalInstances + K - 1) / K;
		List<DepInstance> newVecInstances = vecInstances.subList(0,
				vecInstances.size());
		Collections.shuffle(newVecInstances);
		for (int k = 0; k < K; k++) {
			PrintWriter output = new PrintWriter(
					new OutputStreamWriter(new FileOutputStream(String.format(
							"%s.%d", outputFile, k)), "UTF-8"));
			PrintWriter output_remain = new PrintWriter(
					new OutputStreamWriter(new FileOutputStream(String.format(
							"%s.%d.remain", outputFile, k)), "UTF-8"));
			for (i = 0; i < totalInstances; i++) {
				
				DepInstance tmpInstance = newVecInstances.get(i);
				List<String> curOutPuts = new ArrayList<String>();

				tmpInstance.toGoldListString(curOutPuts);

				int outsize = curOutPuts.size();
				if (outsize == 0)
					continue;
				if(i < (k+1) * eachFileInstanceNum && i >= k * eachFileInstanceNum)
				{
					for (int s = 0; s < outsize; s++) {
						output.println(curOutPuts.get(s));
					}
					output.println();
				}
				else
				{
					for (int s = 0; s < outsize; s++) {
						output_remain.println(curOutPuts.get(s));
					}
					output_remain.println();
				}

			}
			output.close();
			output_remain.close();
		}
	}

	public void deptree_erroranalysis(String outputFile) throws Exception {
		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		int totalInstances = vecInstances.size();
		Map<String, Double> errorContents = new HashMap<String, Double>();

		for (int i = 0; i < totalInstances; i++) {
			DepInstance tmpInstance = vecInstances.get(i);
			int[] predheads = tmpInstance.k_heads[0];
			String[] predlabels = tmpInstance.k_deprels[0];
			for (int j = 0; j < tmpInstance.size(); j++) {
				if (DepInstance.isPunc(tmpInstance.cpostags.get(j))
						|| DepInstance.isPunc(tmpInstance.postags.get(j)))
					continue;
				int m = j;
				int h = tmpInstance.heads.get(m) - 1;
				String mword = tmpInstance.forms.get(m);
				String hword = "ROOT";
				String mpos = tmpInstance.cpostags.get(m);
				String hpos = "ROOT";
				if (h < tmpInstance.size() && h >= 0) {
					hword = tmpInstance.forms.get(h);
					hpos = tmpInstance.cpostags.get(h);
				}
				String label = tmpInstance.deprels.get(m);

				int[] subIds = new int[2];
				subIds[0] = h;
				subIds[1] = m;
				String[] subTags = new String[2];
				subTags[0] = "h";
				subTags[1] = "m";
				String[] outInfos = new String[3];
				if(j == 133)
				{
					System.out.println("......");
				}
				findTargetMinSubTree(tmpInstance.forms, tmpInstance.cpostags,
						predheads, subIds, subTags, outInfos);
				String prefix = outInfos[0];
				String errorContent = "S0#=#" + prefix;
				addMapItem(errorContents, errorContent, 1.0);
				errorContent = "SW#=#" + prefix + "#=#[" + hword + "_" + mword
						+ "]#=#" + outInfos[1];
				addMapItem(errorContents, errorContent, 1.0);
				errorContent = "SP#=#" + prefix + "#=#[" + hpos + "_" + mpos
						+ "]#=#" + outInfos[2];
				addMapItem(errorContents, errorContent, 1.0);
				errorContent = "SL#=#" + prefix + "#=#" + label;
				addMapItem(errorContents, errorContent, 1.0);
				errorContent = "SLW#=#" + prefix + "#=#" + label + "#=#["
						+ hword + "_" + mword + "]#=#" + outInfos[1];
				addMapItem(errorContents, errorContent, 1.0);
				errorContent = "SLP#=#" + prefix + "#=#" + label + "#=#["
						+ hpos + "_" + mpos + "]#=#" + outInfos[2];
				addMapItem(errorContents, errorContent, 1.0);

			}
		}

		List<DepErrors> newDepErrors = new ArrayList<DepErrors>();
		for (String strContent : errorContents.keySet()) {
			newDepErrors.add(new DepErrors(strContent, errorContents
					.get(strContent)));
		}
		Collections.sort(newDepErrors, new DepErrorsCompare());
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8"));
		for (DepErrors depError : newDepErrors) {
			output.println(depError.print());
		}

		output.close();
	}

	public static void addMapItem(Map<String, Double> keyValues, String theKey,
			double theValue) {
		if (keyValues.containsKey(theKey)) {
			keyValues.put(theKey, keyValues.get(theKey) + theValue);
		} else {
			keyValues.put(theKey, theValue);
		}
	}

	public void sibtree_erroranalysis(String outputFile) throws Exception {
		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		int totalInstances = vecInstances.size();
		Map<String, Double> errorContents = new HashMap<String, Double>();

		for (int i = 0; i < totalInstances; i++) {
			DepInstance tmpInstance = vecInstances.get(i);
			List<List<Integer>> childs = new ArrayList<List<Integer>>();
			for (int j = 0; j < tmpInstance.size(); j++) {
				childs.add(new ArrayList<Integer>());
			}
			for (int j = 0; j < tmpInstance.size(); j++) {
				int head = tmpInstance.heads.get(j) - 1;
				if (head >= 0)
					childs.get(head).add(j);
			}
			int[] predheads = tmpInstance.k_heads[0];
			String[] predlabels = tmpInstance.k_deprels[0];
			for (int j = 0; j < tmpInstance.size(); j++) {
				if (DepInstance.isPunc(tmpInstance.cpostags.get(j))
						|| DepInstance.isPunc(tmpInstance.postags.get(j)))
					continue;
				int h = j;
				List<Integer> curChild = childs.get(h);
				for (int lm = 0; lm < curChild.size() - 1; lm++) {
					int m = curChild.get(lm);
					// for (int ls = lm + 1; ls < curChild.size(); ls++)
					{
						int ls = lm + 1;
						int s = curChild.get(ls);
						String mword = tmpInstance.forms.get(m);
						String sword = tmpInstance.forms.get(s);
						String hword = tmpInstance.forms.get(h);
						String mpos = tmpInstance.cpostags.get(m);
						String spos = tmpInstance.cpostags.get(s);
						String hpos = tmpInstance.cpostags.get(h);
						String label = tmpInstance.deprels.get(m) + "_"
								+ tmpInstance.deprels.get(s);

						int[] subIds = new int[3];
						subIds[0] = h;
						subIds[1] = m;
						subIds[2] = s;
						String[] subTags = new String[3];
						subTags[0] = "h";
						subTags[1] = "m";
						subTags[2] = "m";
						String[] outInfos = new String[3];
						;
						findTargetMinSubTree(tmpInstance.forms,
								tmpInstance.cpostags, predheads, subIds,
								subTags, outInfos);
						String prefix = outInfos[0];
						String errorContent = "S0#=#" + prefix;
						addMapItem(errorContents, errorContent, 1.0);
						errorContent = "SW#=#" + prefix + "#=#[" + hword + "_"
								+ mword + "|" + hword + "_" + sword + "]#=#"
								+ outInfos[1];
						addMapItem(errorContents, errorContent, 1.0);
						errorContent = "SP#=#" + prefix + "#=#[" + hpos + "_"
								+ mpos + "|" + hpos + "_" + spos + "]#=#"
								+ outInfos[2];
						addMapItem(errorContents, errorContent, 1.0);
						errorContent = "SL#=#" + prefix + "#=#" + label;
						addMapItem(errorContents, errorContent, 1.0);
						errorContent = "SLW#=#" + prefix + "#=#" + label
								+ "#=#[" + hword + "_" + mword + "|" + hword
								+ "_" + sword + "]#=#" + outInfos[1];
						addMapItem(errorContents, errorContent, 1.0);
						errorContent = "SLP#=#" + prefix + "#=#" + label
								+ "#=#[" + hpos + "_" + mpos + "|" + hpos + "_"
								+ spos + "]#=#" + outInfos[2];
						addMapItem(errorContents, errorContent, 1.0);

					}

				}
			}
		}

		List<DepErrors> newDepErrors = new ArrayList<DepErrors>();
		for (String strContent : errorContents.keySet()) {
			newDepErrors.add(new DepErrors(strContent, errorContents
					.get(strContent)));
		}
		Collections.sort(newDepErrors, new DepErrorsCompare());
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8"));
		for (DepErrors depError : newDepErrors) {
			output.println(depError.print());
		}

		output.close();
	}

	public void gchtree_erroranalysis(String outputFile) throws Exception {
		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		int totalInstances = vecInstances.size();
		Map<String, Double> errorContents = new HashMap<String, Double>();

		for (int i = 0; i < totalInstances; i++) {
			DepInstance tmpInstance = vecInstances.get(i);

			int[] predheads = tmpInstance.k_heads[0];
			String[] predlabels = tmpInstance.k_deprels[0];
			for (int j = 0; j < tmpInstance.size(); j++) {
				if (DepInstance.isPunc(tmpInstance.cpostags.get(j))
						|| DepInstance.isPunc(tmpInstance.postags.get(j)))
					continue;
				int m = j;
				int h = tmpInstance.heads.get(m) - 1;
				int g = -1;
				String mword = tmpInstance.forms.get(m);
				String hword = "ROOT";
				String gword = "ROOT";
				String mpos = tmpInstance.cpostags.get(m);
				String hpos = "ROOT";
				String gpos = "ROOT";

				if (h < tmpInstance.size() && h >= 0) {
					hword = tmpInstance.forms.get(h);
					hpos = tmpInstance.cpostags.get(h);
					g = tmpInstance.heads.get(h) - 1;
				} else {
					continue;
				}
				if (g < tmpInstance.size() && g >= 0) {
					gword = tmpInstance.forms.get(g);
					gpos = tmpInstance.cpostags.get(g);
				}
				String label = tmpInstance.deprels.get(m) + "_"
						+ tmpInstance.deprels.get(h);

				int[] subIds = new int[3];
				subIds[0] = g;
				subIds[1] = h;
				subIds[2] = m;
				String[] subTags = new String[3];
				subTags[0] = "g";
				subTags[1] = "h";
				subTags[2] = "m";
				String[] outInfos = new String[3];
				;
				findTargetMinSubTree(tmpInstance.forms, tmpInstance.cpostags,
						predheads, subIds, subTags, outInfos);
				String prefix = outInfos[0];
				String errorContent = "S0#=#" + prefix;
				addMapItem(errorContents, errorContent, 1.0);
				errorContent = "SW#=#" + prefix + "#=#[" + gword + "_" + hword
						+ "_" + mword + "]#=#" + outInfos[1];
				addMapItem(errorContents, errorContent, 1.0);
				errorContent = "SP#=#" + prefix + "#=#[" + gpos + "_" + hpos
						+ "_" + mpos + "]#=#" + outInfos[2];
				addMapItem(errorContents, errorContent, 1.0);
				errorContent = "SL#=#" + prefix + "#=#" + label;
				addMapItem(errorContents, errorContent, 1.0);
				errorContent = "SLW#=#" + prefix + "#=#" + label + "#=#["
						+ gword + "_" + hword + "_" + mword + "]#=#"
						+ outInfos[1];
				addMapItem(errorContents, errorContent, 1.0);
				errorContent = "SLP#=#" + prefix + "#=#" + label + "#=#["
						+ gpos + "_" + hpos + "_" + mpos + "]#=#" + outInfos[2];
				addMapItem(errorContents, errorContent, 1.0);
			}
		}

		List<DepErrors> newDepErrors = new ArrayList<DepErrors>();
		for (String strContent : errorContents.keySet()) {
			newDepErrors.add(new DepErrors(strContent, errorContents
					.get(strContent)));
		}
		Collections.sort(newDepErrors, new DepErrorsCompare());
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8"));
		for (DepErrors depError : newDepErrors) {
			output.println(depError.print());
		}

		output.close();
	}

	public static int findTargetMinSubTree(List<String> forms,
			List<String> cpostags, int[] heads, int[] subIds, String[] subTags,
			String[] outInfos) {
		if (subIds.length < 1 || heads.length != cpostags.size()
				|| cpostags.size() != forms.size()
				|| subTags.length != subIds.length)
			return -1;
		// outInfos = new String[3];
		List<List<Integer>> jfathers = new ArrayList<List<Integer>>();
		for (int i = 0; i < subIds.length; i++) {
			jfathers.add(new ArrayList<Integer>());
		}

		for (int i = 0; i < subIds.length; i++) {
			jfathers.get(i).add(subIds[i]);
			int curId = subIds[i];
			while (curId >= 0) {
				curId = heads[curId] - 1;
				jfathers.get(i).add(curId);
			}
		}
		int commonfatherId = -1;
		for (int j = 0; j < jfathers.get(0).size(); j++) {
			boolean bCommonFather = true;
			int current0Node = jfathers.get(0).get(j);
			for (int i = 1; i < subIds.length; i++) {
				boolean bFind = false;
				for (int k = 0; k < jfathers.get(i).size(); k++) {
					int currentInode = jfathers.get(i).get(k);
					if (current0Node == currentInode) {
						bFind = true;
						break;
					}
				}
				if (!bFind) {
					bCommonFather = false;
					break;
				}

			}
			if (bCommonFather) {
				commonfatherId = current0Node;
				break;
			}
		}

		Map<Integer, String> treeNodes = new HashMap<Integer, String>();
		{
			String curTag = "o";
			for (int t = 0; t < subIds.length; t++) {
				if (subIds[t] == commonfatherId) {
					curTag = subTags[t];
					break;
				}
			}
			treeNodes.put(commonfatherId, curTag);
		}

		for (int i = 0; i < subIds.length; i++) {
			int curId = subIds[i];
			String curTag = "o";
			for (int t = 0; t < subIds.length; t++) {
				if (subIds[t] == curId) {
					curTag = subTags[t];
					break;
				}
			}
			treeNodes.put(curId, curTag);
			while (curId != commonfatherId) {
				curId = heads[curId] - 1;
				curTag = "o";
				for (int t = 0; t < subIds.length; t++) {
					if (subIds[t] == curId) {
						curTag = subTags[t];
						break;
					}
				}
				treeNodes.put(curId, curTag);
			}
		}

		Map<Integer, Set<Integer>> childNodes = new HashMap<Integer, Set<Integer>>();
		for (Integer father : treeNodes.keySet()) {
			childNodes.put(father, new HashSet<Integer>());
		}

		for (int i = 0; i < subIds.length; i++) {
			int curId = subIds[i];
			while (curId != commonfatherId) {
				int preId = curId;
				curId = heads[curId] - 1;
				childNodes.get(curId).add(preId);
			}
		}

		int initNode = commonfatherId;

		drawMinSubTree(forms, cpostags, treeNodes, childNodes, initNode,
				outInfos);

		//outInfos[0] = outInfos[0] + String.format("[%d]", treeNodes.size());

		return treeNodes.size();
	}

	public static void drawMinSubTree(List<String> forms,
			List<String> cpostags, Map<Integer, String> treeNodes,
			Map<Integer, Set<Integer>> childNodes, int headId, String[] outInfos) {
		int childSize = childNodes.get(headId).size();
		String[] modeInfos = new String[childSize];
		String[] wordInfos = new String[childSize];
		String[] postagInfos = new String[childSize];

		for (Integer childId : childNodes.get(headId)) {
			String curModeInfo = treeNodes.get(childId);
			String curPosTagInfo = (childId >= 0 ? cpostags.get(childId)
					: "ROOT");
			String curWordInfo = (childId >= 0 ? forms.get(childId) : "ROOT");

			int curChildId = childId;

			if (childNodes.get(childId).size() >= 2) {
				String[] subOutInfos = new String[3];
				drawMinSubTree(forms, cpostags, treeNodes, childNodes,
						curChildId, subOutInfos);
				curModeInfo = subOutInfos[0];
				curPosTagInfo = subOutInfos[2];
				curWordInfo = subOutInfos[1];
			} else {

				while (true) {
					Set<Integer> curChilds = childNodes.get(curChildId);
					if (curChilds.size() == 0) {
						break;
					} else if (curChilds.size() == 1) {
						for (Integer theChild : curChilds) {
							curChildId = theChild;
							if (childNodes.get(curChildId).size() <= 1) {
								curModeInfo = curModeInfo + "->"
										+ treeNodes.get(curChildId);
								curPosTagInfo = curPosTagInfo
										+ "_"
										+ (curChildId >= 0 ? cpostags
												.get(curChildId) : "ROOT");
								curWordInfo = curWordInfo
										+ "_"
										+ (curChildId >= 0 ? forms
												.get(curChildId) : "ROOT");
							}
						}
					} else {
						String[] subOutInfos = new String[3];
						drawMinSubTree(forms, cpostags, treeNodes, childNodes,
								curChildId, subOutInfos);

						curModeInfo = curModeInfo + "->" + subOutInfos[0];
						curPosTagInfo = curPosTagInfo + "_" + subOutInfos[2];
						curWordInfo = curWordInfo + "_" + subOutInfos[1];
						break;
					}
				}
			}
			// int tmpIndex = curModeInfo.indexOf("->o->o->o->o");
			String[] units_m = curModeInfo.split("->");
			String[] units_p = curPosTagInfo.split("_");
			String[] units_w = curWordInfo.split("_");
			Set<Integer> deleteIds = new HashSet<Integer>();
			for (int start = 1; start < units_m.length - 4; start++) {
				if (!units_m[start].equals("o"))
					continue;
				boolean bDelete = true;
				for (int end = start + 1; end < start + 3; end++) {
					if (!units_m[start].equals(units_m[end])) {
						bDelete = false;
						break;
					}
				}
				if (bDelete)
					deleteIds.add(start);
			}
			curModeInfo = units_m[0];
			curWordInfo = units_w[0];
			curPosTagInfo = units_p[0];
			for (int start = 1; start < units_m.length;) {

				if (deleteIds.contains(start)) {
					int end = start + 1;
					for (; end < units_m.length; end++) {
						if (!deleteIds.contains(end))
							break;
					}
					curModeInfo = curModeInfo + "->*";
					curWordInfo = curWordInfo + "_*";
					curPosTagInfo = curPosTagInfo + "_*";
					start = end;
				} else {
					curModeInfo = curModeInfo + "->" + units_m[start];
					curWordInfo = curWordInfo + "_" + units_w[start];
					curPosTagInfo = curPosTagInfo + "_" + units_p[start];
					start++;
				}
			}

			int insert_pos = 0;
			for (int k = 0; k < modeInfos.length; k++) {
				if (modeInfos[k] == null) {
					insert_pos = k;
					break;
				}
				if (modeInfos[k].compareTo(curModeInfo) > 0) {
					insert_pos = k;
					break;
				}
			}

			for (int k = modeInfos.length - 1; k > insert_pos; k--) {
				modeInfos[k] = modeInfos[k - 1];
				wordInfos[k] = wordInfos[k - 1];
				postagInfos[k] = postagInfos[k - 1];
			}

			modeInfos[insert_pos] = curModeInfo;
			wordInfos[insert_pos] = curWordInfo;
			postagInfos[insert_pos] = curPosTagInfo;
		}

		String modeInfo = "";
		String postagInfo = "";
		String wordInfo = "";
		if (modeInfos.length > 1) {
			for (int k = 0; k < modeInfos.length; k++) {
				modeInfo = modeInfo + "[" + modeInfos[k] + "]";
				wordInfo = wordInfo + "[" + wordInfos[k] + "]";
				postagInfo = postagInfo + "[" + postagInfos[k] + "]";
			}
			String headModeInfo = treeNodes.get(headId);
			String headPosTagInfo = (headId >= 0 ? cpostags.get(headId)
					: "ROOT");
			String headWordInfo = (headId >= 0 ? forms.get(headId) : "ROOT");
			modeInfo = "[" + headModeInfo + modeInfo + "]";
			wordInfo = "[" + headWordInfo + wordInfo + "]";
			postagInfo = "[" + headPosTagInfo + postagInfo + "]";
		} else {
			String headModeInfo = treeNodes.get(headId);
			String headPosTagInfo = (headId >= 0 ? cpostags.get(headId)
					: "ROOT");
			String headWordInfo = (headId >= 0 ? forms.get(headId) : "ROOT");
			modeInfo = "[" + headModeInfo + "->" + modeInfos[0] + "]";
			wordInfo = "[" + headWordInfo + "_" + wordInfos[0] + "]";
			postagInfo = "[" + headPosTagInfo + "_" + postagInfos[0] + "]";

		}

		outInfos[0] = modeInfo;
		outInfos[1] = wordInfo;
		outInfos[2] = postagInfo;
		
		if(outInfos[0].equals("[o->o->h->m]"))
		{
			System.out.println(".....");
		}

	}

	public void word_stat(String outputFile) throws Exception {
		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		int totalInstances = vecInstances.size();
		Map<String, Double> errorContents = new HashMap<String, Double>();

		for (int i = 0; i < totalInstances; i++) {
			DepInstance tmpInstance = vecInstances.get(i);
			List<List<Integer>> childs = new ArrayList<List<Integer>>();
			for (int j = 0; j < tmpInstance.size(); j++) {
				childs.add(new ArrayList<Integer>());
			}
			for (int j = 0; j < tmpInstance.size(); j++) {
				int head = tmpInstance.heads.get(j) - 1;
				if (head >= 0)
					childs.get(head).add(j);
			}

			for (int j = 0; j < tmpInstance.size(); j++) {
				if (DepInstance.isPunc(tmpInstance.cpostags.get(j))
						|| DepInstance.isPunc(tmpInstance.postags.get(j)))
					continue;
				int h = j;
				List<Integer> curChild = childs.get(h);

				String hword = tmpInstance.forms.get(h);
				String hpos = tmpInstance.cpostags.get(h);

				String prefix = hword;
				if (prefix.trim().isEmpty()) {
					System.out.println("......");
				}
				String errorContent = prefix + "#=#Count";
				addMapItem(errorContents, errorContent, 1.0);
				errorContent = prefix + "#=#ChildCount";
				addMapItem(errorContents, errorContent, curChild.size());

				for (int lm = 0; lm < curChild.size(); lm++) {
					int m = curChild.get(lm);
					String mword = tmpInstance.forms.get(m);
					String mpos = tmpInstance.cpostags.get(m);
					String mlabel = tmpInstance.deprels.get(m);
					String direction = "L";
					if (m > h)
						direction = "R";

					errorContent = prefix + "#=#" + "ChildPCount#=#" + mpos;
					addMapItem(errorContents, errorContent, 1.0);
					errorContent = prefix + "#=#" + "ChildWCount#=#" + mword;
					addMapItem(errorContents, errorContent, 1.0);
					errorContent = prefix + "#=#" + "ChildLCount#=#" + mlabel;
					addMapItem(errorContents, errorContent, 1.0);
					errorContent = prefix + "#=#" + "ChildLPCount#=#" + mlabel
							+ "#=#" + mpos;
					addMapItem(errorContents, errorContent, 1.0);
					errorContent = prefix + "#=#" + "ChildLWCount#=#" + mlabel
							+ "#=#" + mword;
					addMapItem(errorContents, errorContent, 1.0);

					errorContent = prefix + "#=#" + "ChildPCount" + direction
							+ "#=#" + mpos;
					addMapItem(errorContents, errorContent, 1.0);
					errorContent = prefix + "#=#" + "ChildWCount" + direction
							+ "#=#" + mword;
					addMapItem(errorContents, errorContent, 1.0);
					errorContent = prefix + "#=#" + "ChildLCount" + direction
							+ "#=#" + mlabel;
					addMapItem(errorContents, errorContent, 1.0);
					errorContent = prefix + "#=#" + "ChildLPCount" + direction
							+ "#=#" + mlabel + "#=#" + mpos;
					addMapItem(errorContents, errorContent, 1.0);
					errorContent = prefix + "#=#" + "ChildLWCount" + direction
							+ "#=#" + mlabel + "#=#" + mword;
					addMapItem(errorContents, errorContent, 1.0);
				}

				prefix = hpos;
				if (prefix.trim().isEmpty()) {
					System.out.println("......");
				}
				errorContent = prefix + "#=#PCount";
				addMapItem(errorContents, errorContent, 1.0);
				errorContent = prefix + "#=#PChildCount";
				addMapItem(errorContents, errorContent, curChild.size());

				for (int lm = 0; lm < curChild.size(); lm++) {
					int m = curChild.get(lm);
					String mword = tmpInstance.forms.get(m);
					String mpos = tmpInstance.cpostags.get(m);
					String mlabel = tmpInstance.deprels.get(m);
					String direction = "L";
					if (m > h)
						direction = "R";

					errorContent = prefix + "#=#" + "PChildPCount#=#" + mpos;
					addMapItem(errorContents, errorContent, 1.0);
					errorContent = prefix + "#=#" + "PChildWCount#=#" + mword;
					addMapItem(errorContents, errorContent, 1.0);
					errorContent = prefix + "#=#" + "PChildLCount#=#" + mlabel;
					addMapItem(errorContents, errorContent, 1.0);
					errorContent = prefix + "#=#" + "PChildLPCount#=#" + mlabel
							+ "#=#" + mpos;
					addMapItem(errorContents, errorContent, 1.0);
					errorContent = prefix + "#=#" + "PChildLWCount#=#" + mlabel
							+ "#=#" + mword;
					addMapItem(errorContents, errorContent, 1.0);

					errorContent = prefix + "#=#" + "PChildPCount" + direction
							+ "#=#" + mpos;
					addMapItem(errorContents, errorContent, 1.0);
					errorContent = prefix + "#=#" + "PChildWCount" + direction
							+ "#=#" + mword;
					addMapItem(errorContents, errorContent, 1.0);
					errorContent = prefix + "#=#" + "PChildLCount" + direction
							+ "#=#" + mlabel;
					addMapItem(errorContents, errorContent, 1.0);
					errorContent = prefix + "#=#" + "PChildLPCount" + direction
							+ "#=#" + mlabel + "#=#" + mpos;
					addMapItem(errorContents, errorContent, 1.0);
					errorContent = prefix + "#=#" + "PChildLWCount" + direction
							+ "#=#" + mlabel + "#=#" + mword;
					addMapItem(errorContents, errorContent, 1.0);
				}

			}

		}

		List<DepErrors> newDepErrors = new ArrayList<DepErrors>();
		for (String strContent : errorContents.keySet()) {
			DepErrors depError = new DepErrors(strContent,
					errorContents.get(strContent));
			double frequence = 0;
			if (depError.m_contents[1].startsWith("PChild")) {
				String freqKey = depError.m_type + "#=#PCount";
				frequence = errorContents.get(freqKey);
				depError.m_times = depError.m_times
						/ errorContents.get(freqKey);
			}

			if (depError.m_contents[1].startsWith("Child")) {
				String freqKey = depError.m_type + "#=#Count";
				frequence = errorContents.get(freqKey);
				depError.m_times = depError.m_times
						/ errorContents.get(freqKey);
			}
			if (frequence > 10) {
				newDepErrors.add(depError);
			}
		}
		Collections.sort(newDepErrors, new DepErrorsCompare2());
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8"));
		for (DepErrors depError : newDepErrors) {
			output.println(depError.print());
		}

		output.close();
	}

	public void errorLocation(String outputFile, int option) throws Exception {
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
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8"));
		int i = 0;
		for (; i < totalInstances; i++) {
			DepInstance tmpInstance = vecInstances.get(i);
			//if (option == 0) {
			//	tmpInstance.evaluate1Best();
			//} else {
			//	tmpInstance.evaluateMarginBest(marginal);
			//}
			List<String> errorWords = new ArrayList<String>();
			tmpInstance.compare1Best(option, errorWords);

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

			if(errorWords.size() > 0)
			{
				output.println(String.format("Sentence %d is not correct", i+1));
				for(int j = 0; j < errorWords.size(); j++)
				{
					output.println(String.format("%s", errorWords.get(j)));
				}
				
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
