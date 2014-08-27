package mason.dep;

//import ir.hit.edu.util.UniversPostager;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DepInstance {
	public List<String> forms;
	public List<String> lemmas;
	public List<String> cpostags;
	public List<String> postags;
	public List<Integer> heads;
	public List<String> deprels;
	public List<String> feats1;
	public List<String> feats2;
	public List<String> feats3;
	public List<Integer> p1heads;
	public List<Integer> p2heads;
	public List<String> p1deprels;
	public List<String> p2deprels;
	
	public Set<Integer> loadParams;
	public int maxColumn;

	public String[][] k_deprels = null;
	public int[][] k_heads = null;
	public double[] k_scores = null;

	// for active learning
	public double[] k_probs = null;
	public double[][] k_uas_probs = null;
	public double[][] k_las_probs = null;

	// active learning score
	public double leastConfidence;
	public double minMargin;
	public double tte;
	public double avgte;
	public double minte;
	public double nse;
	public double longLength;
	
	public String smark = "";

	// active learning by committee
	public double tve;
	public double tkl;
	public double sve;
	public double skl;
	public double f_complement;
	
	public List<Integer> bshared;

	// for evaluate
	// 0 sent length; 1 uas_correct_num; 2 las_correct_num; 3
	// sentence_all_uas_correct
	// 4 sentence_all_las_correct; 5 root_correct
	public int[] eval_res;

	public DepInstance() {
		forms = new ArrayList<String>();
		lemmas = new ArrayList<String>();
		cpostags = new ArrayList<String>();
		postags = new ArrayList<String>();
		heads = new ArrayList<Integer>();
		deprels = new ArrayList<String>();
		feats1 = new ArrayList<String>();
		feats2 = new ArrayList<String>();
		feats3 = new ArrayList<String>();
		p1heads = new ArrayList<Integer>();
		p2heads = new ArrayList<Integer>();
		p1deprels = new ArrayList<String>();
		p2deprels = new ArrayList<String>();
		loadParams = new HashSet<Integer>();
		
		maxColumn = 10;

		k_deprels = null;
		k_heads = null;
		k_scores = null;

		// for active learning
		k_probs = null;
		k_uas_probs = null;
		k_las_probs = null;

		eval_res = new int[8];
		for (int i = 0; i < 8; i++) {
			eval_res[i] = 0;
		}
	}

	public void reset() {
		forms = new ArrayList<String>();
		lemmas = new ArrayList<String>();
		cpostags = new ArrayList<String>();
		postags = new ArrayList<String>();
		heads = new ArrayList<Integer>();
		deprels = new ArrayList<String>();
		feats1 = new ArrayList<String>();
		feats2 = new ArrayList<String>();
		feats3 = new ArrayList<String>();
		p1heads = new ArrayList<Integer>();
		p2heads = new ArrayList<Integer>();
		p1deprels = new ArrayList<String>();
		p2deprels = new ArrayList<String>();
		loadParams = new HashSet<Integer>();
		
		maxColumn = 10;

		k_deprels = null;
		k_heads = null;
		k_scores = null;

		// for active learning
		k_probs = null;
		k_uas_probs = null;
		k_las_probs = null;

		eval_res = new int[8];
		for (int i = 0; i < 8; i++) {
			eval_res[i] = 0;
		}
	}
	
	public String toString(Set<Integer> columns, int maxColumn, int index)
	{
		String result = String.format("%d", index+1);
		//1form,2lemma,3cpostag,4postag,5feat1,6head,7deprel,8feat2,9feat3,10...
		for(Integer idx = 1; idx < maxColumn; idx++)
		{
			String curContent = "_";
			if(columns.contains(idx))
			{
				if(idx == 1)curContent = forms.get(index);
				else if(idx == 2)curContent = lemmas.get(index);
				else if(idx == 3)curContent = cpostags.get(index);
				else if(idx == 4)curContent = postags.get(index);
				else if(idx == 5)curContent = feats1.get(index);
				else if(idx == 6)curContent = String.format("%d", heads.get(index));
				else if(idx == 7)curContent = deprels.get(index);
				else if(idx == 8)curContent = feats2.get(index);
				else if(idx == 9)curContent = feats3.get(index);
				else if(idx == 10)curContent = String.format("%d", p1heads.get(index));
				else if(idx == 11)curContent = p1deprels.get(index);
				else if(idx == 12)curContent = String.format("%d", p2heads.get(index));
				else if(idx == 13)curContent = p2deprels.get(index);
				else
				{
					int pIdx = (idx-14)/2;
					if(k_heads == null || k_heads.length < pIdx + 1)continue;
					boolean bHead = (idx-14)%2 == 0 ? true : false;
					if(bHead)
					{
						curContent = String.format("%d", k_heads[pIdx][idx]);
					}
					else
					{
						curContent = k_deprels[pIdx][idx];
					}
				}
				
			}
			
			result = result + "\t" + curContent;
		}
		
		return result;
	}

	public int size() {
		return forms.size();
	}

	public void toPredListString(List<String> outputs) {
		outputs.clear();
		if (k_probs == null || k_probs.length < 1) {
			return;
		}
		// outputs.add(String.format("kprob\t1\t%f", k_probs[0]));
		int length = forms.size();
		for (int i = 0; i < length; i++) {
			String tmpOut = String.format(
					"%d\t%s\t%s\t%s\t%s\t_\t%d\t%s\t_\t_", i + 1, forms.get(i),
					lemmas.get(i), cpostags.get(i), postags.get(i),
					k_heads[0][i], k_deprels[0][i]);

			outputs.add(tmpOut);
		}
	}

	public void toGoldListString(List<String> outputs) {
		outputs.clear();
		int length = forms.size();
		for (int i = 0; i < length; i++) {
			String tmpOut = String.format(
					"%d\t%s\t%s\t%s\t%s\t_\t%d\t%s\t_\t_", i + 1, forms.get(i),
					lemmas.get(i), cpostags.get(i), postags.get(i),
					heads.get(i), deprels.get(i));

			outputs.add(tmpOut);
		}
	}
	
	
	public void toZhangYueGoldListString(List<String> outputs) {
		outputs.clear();
		int length = forms.size();
		for (int i = 0; i < length; i++) {
			String tmpOut = String.format(
					"%s\t%s\t%d\t%s",forms.get(i),
					cpostags.get(i), 
					heads.get(i)-1, deprels.get(i));

			outputs.add(tmpOut);
		}
	}
	
	public void toGoldListString(List<String> outputs, List<String> feats) {
		outputs.clear();
		int length = forms.size();
		for (int i = 0; i < length; i++) {
			String tmpOut = String.format(
					"%d\t%s\t%s\t%s\t%s\t%s\t%d\t%s\t_\t_", i + 1, forms.get(i),
					lemmas.get(i), cpostags.get(i), postags.get(i),feats.get(i),
					heads.get(i), deprels.get(i));

			outputs.add(tmpOut);
		}
	}
	
	public void toBestProbListString(List<String> outputs) {
		if (k_scores == null || k_scores.length < 1) {
			return;
		}
		ComputeProb();
		int K = k_scores.length;
		int length = forms.size();
		outputs.clear();
		for (int i = 0; i < length; i++) {
			int maxMarginId = 0;
			double maxMarginProb = k_las_probs[0][i];
			for (int k = 1; k < K; k++) {
				if (k_las_probs[k][i] > maxMarginProb + Double.MIN_VALUE) {
					maxMarginProb = k_las_probs[k][i];
					maxMarginId = k;
				}
			}			
			String tmpOut = String.format(
					"%d\t%s\t%s\t%s\t%s\t_\t%d\t%s\t_\t_", i + 1, forms.get(i),
					lemmas.get(i), cpostags.get(i), postags.get(i),
					k_heads[maxMarginId][i],k_deprels[maxMarginId][i]);
			outputs.add(tmpOut);
		}


	}
	
	public void toBestListString(List<String> outputs) {
		outputs.clear();
		int length = forms.size();
		for (int i = 0; i < length; i++) {
			String tmpOut = String.format(
					"%d\t%s\t%s\t%s\t%s\t_\t%d\t%s\t_\t_\t%d\t%s", i + 1, forms.get(i),
					lemmas.get(i), cpostags.get(i), postags.get(i),
					heads.get(i), deprels.get(i),k_heads[0][i], k_deprels[0][i]);

			outputs.add(tmpOut);
		}
	}

	public void toAllPredListString(List<String> outputs) {
		outputs.clear();
		if (k_probs == null || k_probs.length < 1) {
			return;
		}
		int K = k_probs.length;
		String head_out = String.format("kprob\t%d\t%f", K, k_probs[0]);
		for (int k = 1; k < K; k++) {
			head_out = head_out + String.format("\t%f", k_probs[k]);
		}
		outputs.add(head_out);

		int length = forms.size();
		for (int i = 0; i < length; i++) {
			String tmpOut = String.format(
					"%d\t%s\t%s\t%s\t%s\t_\t%d\t%s\t_\t_", i + 1, forms.get(i),
					lemmas.get(i), cpostags.get(i), postags.get(i),
					heads.get(i), deprels.get(i));

			for (int k = 1; k < K; k++) {
				tmpOut = tmpOut
						+ String.format("\t%d\t%s\t%f\t%f", k_heads[k][i],
								k_deprels[k][i], k_uas_probs[k][i],
								k_las_probs[k][i]);
			}
			outputs.add(tmpOut);
		}
	}
	

	public void ComputeProb() {
		int length = forms.size();
		if (k_scores == null || k_scores.length < 1) {
			return;
		}
		int K = k_scores.length;

		k_probs = new double[K];
		k_uas_probs = new double[K][length];
		k_las_probs = new double[K][length];

		// initialize
		double Z_norm = 1;
		for (int k = 1; k < K; k++) {
			Z_norm += Math.exp(k_scores[k] - k_scores[0]);
		}
		for (int k = 0; k < K; k++) {
			k_probs[k] = Math.exp(k_scores[k] - k_scores[0]) / Z_norm;
		}

		double[] modiProbs = new double[K];

		for (int k = 0; k < K; k++) {
			modiProbs[k] = k_probs[k];
		}

		for (int i = 0; i < length; i++) {
			for (int k = 0; k < K; k++) {
				k_uas_probs[k][i] = 0.0;
				k_las_probs[k][i] = 0.0;
			}
		}

		for (int i = 0; i < length; i++) {
			if (isPunc(forms.get(i), cpostags.get(i)) || isPunc(forms.get(i), postags.get(i))) {
				for (int k = 0; k < K; k++) {
					k_uas_probs[k][i] = 1.0;
					k_las_probs[k][i] = 1.0;
				}
				continue;
			}
			for (int k1 = 0; k1 < K; k1++) {
				for (int k2 = 0; k2 < K; k2++) {
					if (k_heads[k2][i] == k_heads[k1][i]) {
						k_uas_probs[k1][i] += modiProbs[k2];
					}
					if (k_heads[k2][i] == k_heads[k1][i]
							&& k_deprels[k2][i].equals(k_deprels[k1][i])) {
						k_las_probs[k1][i] += modiProbs[k2];
					}
				}
			}
		}

	}

	public void evaluate1Best() {
		for (int i = 0; i < 8; i++) {
			eval_res[i] = 0;
		}
		if (k_scores == null || k_scores.length < 1) {
			return;
		}
		int length = forms.size();
		for (int i = 0; i < length; i++) {
			if (isPunc(forms.get(i), cpostags.get(i)) || isPunc(forms.get(i), postags.get(i))) {
				continue;
			}
			eval_res[0]++;

			if (k_heads[0][i] == heads.get(i)) {
				eval_res[1]++;
			}

			if (k_heads[0][i] == heads.get(i)
					&& k_deprels[0][i].equals(deprels.get(i))) {
				eval_res[2]++;
			}

			if (k_heads[0][i] == 0 && heads.get(i) == 0) {
				eval_res[5] = 1;
			}
		}

		if (eval_res[1] == eval_res[0]) {
			eval_res[3] = 1;
		}

		if (eval_res[2] == eval_res[0]) {
			eval_res[4] = 1;
		}
	}
	
	public boolean evaluateWithOther(DepInstance other) {
		for (int i = 0; i < 8; i++) {
			eval_res[i] = 0;
		}

		int length = forms.size();
		if(other.forms.size() != length)return false;
		for (int i = 0; i < length; i++) {
			if (isPunc(forms.get(i), cpostags.get(i)) || isPunc(forms.get(i), postags.get(i))) {
				continue;
			}
			eval_res[0]++;
			int curHead = heads.get(i);
			int otherHead = other.heads.get(i);
			if (otherHead == curHead) {
				eval_res[1]++;
			}

			

			if (otherHead == curHead
					&& other.deprels.get(i).equals(deprels.get(i))) {
				eval_res[2]++;
			}

			if (other.heads.get(i) == 0 && heads.get(i) == 0) {
				eval_res[5] = 1;
			}
		}

		if (eval_res[1] == eval_res[0]) {
			eval_res[3] = 1;
		}

		if (eval_res[2] == eval_res[0]) {
			eval_res[4] = 1;
		}

		return true;
	}
	
	
	public boolean evaluateWithOtherLasPos(DepInstance other) {
		for (int i = 0; i < 8; i++) {
			eval_res[i] = 0;
		}

		int length = forms.size();
		if(other.forms.size() != length)return false;
		for (int i = 0; i < length; i++) {
			if (isPunc(forms.get(i), cpostags.get(i)) || isPunc(forms.get(i), postags.get(i))) {
				continue;
			}
			eval_res[0]++;
			int curHead = heads.get(i);
			int otherHead = other.heads.get(i);
			if (otherHead == curHead) {
				eval_res[1]++;
			}
			
			if (otherHead == curHead
					&& other.deprels.get(i).equals(deprels.get(i))) {
				eval_res[2]++;
			}

			if (other.heads.get(i) == 0 && heads.get(i) == 0) {
				eval_res[5] = 1;
			}
		}

		if (eval_res[1] == eval_res[0]) {
			eval_res[3] = 1;
		}

		if (eval_res[2] == eval_res[0]) {
			eval_res[4] = 1;
		}

		return true;
	}
	
	
	public boolean evaluateWithOthers(List<DepInstance> others) {
		if(others.size() == 0)
		{
			return false;
		}
		for (int i = 0; i < 8; i++) {
			eval_res[i] = 0;
		}

		int length = forms.size();
		
		for(DepInstance other : others)
		{
			if(other.forms.size() != length)return false;
		}
		boolean rootallright = true;
		for (int i = 0; i < length; i++) {
			//if (isPunc(forms.get(i), cpostags.get(i)) || isPunc(forms.get(i), postags.get(i))) {
			//	continue;
			//}
			eval_res[0]++;
			boolean headallright = true;
			boolean headlabelallright = true;
				
			for(DepInstance other : others)
			{
				if (other.heads.get(i) != heads.get(i)) {
					headallright = false;
					//eval_res[1]++;
				}

				if (other.heads.get(i) != heads.get(i)
						|| (!other.deprels.get(i).equals("_") && !other.deprels.get(i).equals(deprels.get(i)))) {
					//eval_res[2]++;
					headlabelallright = false;
				}

				if ((other.heads.get(i) != 0 && heads.get(i) == 0)
						|| (other.heads.get(i) == 0 && heads.get(i) != 0)
						) {
					//eval_res[5] = 1;
					rootallright = false;
				}
			}
			if(headallright)eval_res[1]++;
			if(headlabelallright)eval_res[2]++;			
		}
		if(rootallright)eval_res[5] = 1;
		if (eval_res[1] == eval_res[0]) {
			eval_res[3] = 1;
		}

		if (eval_res[2] == eval_res[0]) {
			eval_res[4] = 1;
		}
		return true;
	}
	
	public void evaluateAll() {
		for (int i = 0; i < 8; i++) {
			eval_res[i] = 0;
		}
		if (k_scores == null || k_scores.length < 1) {
			return;
		}
		int length = forms.size();
		int kBest = k_scores.length;
		
		for (int i = 0; i < length; i++) {
			if (isPunc(forms.get(i), cpostags.get(i)) || isPunc(forms.get(i), postags.get(i))) {
				continue;
			}
			eval_res[0]++;
			
			boolean uasRight = false;
			for(int k = 0;k < kBest; k++)
			{
				if(k_heads[k][i] == heads.get(i))
				{
					uasRight = true;
					break;
				}
			}

			if (uasRight) {
				eval_res[1]++;
			}
			
			boolean lasRight = false;
			for(int k = 0;k < kBest; k++)
			{
				if(k_heads[k][i] == heads.get(i)
				&& k_deprels[k][i].equals(deprels.get(i)))
				{
					lasRight = true;
					break;
				}
			}
			
			if (lasRight) {
				eval_res[2]++;
			}

			if (uasRight && heads.get(i) == 0) {
				eval_res[5] = 1;
			}
		}

		if (eval_res[1] == eval_res[0]) {
			eval_res[3] = 1;
		}

		if (eval_res[2] == eval_res[0]) {
			eval_res[4] = 1;
		}
	}
	
	
	public void compare1Best(int option, List<String> errorWords) 
	{
		ComputeProb();
		errorWords.clear();
		for (int i = 0; i < 8; i++) {
			eval_res[i] = 0;
		}
		if (k_scores == null || k_scores.length < 1) {
			return;
		}
		int length = forms.size();
		int K = k_scores.length;
		for (int i = 0; i < length; i++) {
			if (isPunc(forms.get(i), cpostags.get(i)) || isPunc(forms.get(i), postags.get(i))) {
				continue;
			}
			eval_res[0]++;

			if (k_heads[0][i] == heads.get(i)) {
				eval_res[1]++;
			}
			else
			{
				if(option == 0)
				{
					Set<String> theCurrentRes = new HashSet<String>();
					String tmpOut = String.format(
							"%d\t%s\t%s\t%s\t%s\t_\t%d\t%s\t_\t_", i + 1, forms.get(i),
							lemmas.get(i), cpostags.get(i), postags.get(i),
							heads.get(i), deprels.get(i));

					for (int k = 0; k < K; k++) {
						String tempKey = String.format("\t%d\t%f", k_heads[k][i],	k_uas_probs[k][i]);
						if(!theCurrentRes.contains(tempKey))
						{
							tmpOut = tmpOut + tempKey;
							theCurrentRes.add(tempKey);
						}
					}
					
					errorWords.add(tmpOut);
					
				}
			}

			if (k_heads[0][i] == heads.get(i)
					&& k_deprels[0][i].equals(deprels.get(i))) {
				eval_res[2]++;
			}
			else 
			{
				if(option == 1)
				{
					String tmpOut = String.format(
							"%d\t%s\t%s\t%s\t%s\t_\t%d\t%s\t_\t_", i + 1, forms.get(i),
							lemmas.get(i), cpostags.get(i), postags.get(i),
							heads.get(i), deprels.get(i));

					Set<String> theCurrentRes = new HashSet<String>();
					for (int k = 0; k < K; k++) {
						String tempKey = String.format("\t%d\t%s\t%f", k_heads[k][i],	k_deprels[k][i], k_las_probs[k][i]);
						if(!theCurrentRes.contains(tempKey))
						{
							tmpOut = tmpOut + tempKey;
							theCurrentRes.add(tempKey);
						}
					}
					
					errorWords.add(tmpOut);
				}
			}

			if (k_heads[0][i] == 0 && heads.get(i) == 0) {
				eval_res[5] = 1;
			}
		}

		if (eval_res[1] == eval_res[0]) {
			eval_res[3] = 1;
		}

		if (eval_res[2] == eval_res[0]) {
			eval_res[4] = 1;
		}
	}

	
	public void evaluateMarginBest(double marginvalue) {
		for (int i = 0; i < 8; i++) {
			eval_res[i] = 0;
		}
		if (k_scores == null || k_scores.length < 1) {
			return;
		}
		ComputeProb();
		int K = k_scores.length;
		int length = forms.size();
		for (int i = 0; i < length; i++) {
			if (isPunc(forms.get(i), cpostags.get(i)) || isPunc(forms.get(i), postags.get(i))) {
				continue;
			}

			int maxMarginId = 0;
			double maxMarginProb = k_las_probs[0][i];
			for (int k = 1; k < K; k++) {
				if (k_las_probs[k][i] > maxMarginProb + Double.MIN_VALUE) {
					maxMarginProb = k_las_probs[k][i];
					maxMarginId = k;
				}
			}
			if (maxMarginProb < marginvalue)
				continue;
			eval_res[0]++;
			if (k_heads[maxMarginId][i] == heads.get(i)) {
				eval_res[1]++;
			}

			if (k_heads[maxMarginId][i] == heads.get(i)
					&& k_deprels[maxMarginId][i].equals(deprels.get(i))) {
				eval_res[2]++;
			}

			if (k_heads[maxMarginId][i] == 0 && heads.get(i) == 0) {
				if (eval_res[5] == 0) {
					eval_res[5] = 1;
				} else {
					System.out.println("Too many roots!");
					eval_res[5] = 0;
				}
			}
		}

		if (eval_res[1] == eval_res[0]) {
			eval_res[3] = 1;
		}

		if (eval_res[2] == eval_res[0]) {
			eval_res[4] = 1;
		}
	}

	public void computeActiveConfidence() {
		longLength = 0;
		int length = forms.size();
		for (int i = 0; i < length; i++) {
			if (isPunc(forms.get(i), cpostags.get(i)) || isPunc(forms.get(i), postags.get(i))) {
				continue;
			}
			longLength++;
		}
		if (k_probs == null || k_probs.length < 2) {
			return;
		}
		leastConfidence = 1 - k_probs[0];

		minMargin = k_probs[1] - k_probs[0];

		tte = 0.0;
		minte = 0.0;
		int K = k_scores.length;
		for (int i = 0; i < length; i++) {
			double curTE = 0.0;
			for (int k = 0; k < K; k++) {
				boolean bFind = false;
				for (int k1 = 0; k1 < k; k1++) {
					if (k_heads[k1][i] == k_heads[k][i]
							&& k_deprels[k1][i].equals(k_deprels[k][i])) {
						bFind = true;
						break;
					}
				}
				if (!bFind) {
					tte += k_las_probs[k][i] * Math.log(k_las_probs[k][i]);
					curTE += k_las_probs[k][i] * Math.log(k_las_probs[k][i]);
				}
			}

			if (curTE < minte)
				minte = curTE;
		}
		minte = -minte;
		tte = -tte;
		avgte = tte / longLength;

		nse = 0.0;
		for (int k = 0; k < K; k++) {
			nse += k_probs[k] * Math.log(k_probs[k]);
		}
		nse = -nse;
	}
	
	public List<String> GetSemanticLabels(String head, String modifier)
	{
		List<String> resultLabels = new ArrayList<String>();
		
		for(int i = 0; i< forms.size(); i++)
		{
			int headId = heads.get(i)-1;
			if(headId < 0)continue;
			String theHead = forms.get(headId);
			String theModifier = forms.get(i);
			if(head.equals(theHead) && modifier.equals(theModifier))
			{
				resultLabels.add(deprels.get(i));
			}
		}
		
		return resultLabels;
	}
	
	public Set<String> GetSemanticLabels(int head)
	{
		Set<String> resultLabels = new TreeSet<String>();
		
		if(head >= 0 && head < forms.size())
		{
			for(int i = 0; i< forms.size(); i++)
			{
				int headId = heads.get(i)-1;
			
				if(head == headId && !deprels.get(i).endsWith("depend") 
				&& !deprels.get(i).endsWith("PU") && !deprels.get(i).startsWith("s-"))
				{
					resultLabels.add(deprels.get(i));
				}
			}
		}
		
		
		
		return resultLabels;
	}

	public void computeActiveConfidenceCommitte(DepInstance[] kPredictResults) {
		tve = 0.0;
		tkl = 0.0;
		sve = 0.0;
		skl = 0.0;
		f_complement = 0.0;
		int C = kPredictResults.length;
		int T = forms.size();

		// TVE
		for (int t = 0; t < T; t++) {
			if (isPunc(forms.get(t), cpostags.get(t)) || isPunc(forms.get(t), postags.get(t))) {
				continue;
			}
			Map<String, Double> voteResults = new HashMap<String, Double>();
			for (int c = 0; c < C; c++) {
				String theResult = String.format("%d->%s",
						kPredictResults[c].k_heads[0][t],
						kPredictResults[c].k_deprels[0][t]);
				if (voteResults.containsKey(theResult)) {
					voteResults
							.put(theResult, voteResults.get(theResult) + 1.0);
				} else {
					voteResults.put(theResult, 1.0);
				}
			}
			for (String theResult : voteResults.keySet()) {
				double vote_measure = voteResults.get(theResult) / C;
				tve += vote_measure * Math.log(vote_measure);
			}
		}

		tve = -tve;

		// TKL
		for (int t = 0; t < T; t++) {
			if (isPunc(forms.get(t), cpostags.get(t))) {
				continue;
			}

			Map<String, List<Double>> voteResults = new HashMap<String, List<Double>>();
			for (int c = 0; c < C; c++) {
				int K = kPredictResults[c].k_scores.length;
				Map<String, Double> probResults = new HashMap<String, Double>();
				for (int k = 0; k < K; k++) {
					String theResult = String.format("%d->%s",
							kPredictResults[c].k_heads[0][t],
							kPredictResults[c].k_deprels[0][t]);
					if (!probResults.containsKey(theResult)) {
						probResults.put(theResult,
								kPredictResults[c].k_las_probs[k][t]);
					}
				}
				for (String theResult : probResults.keySet()) {
					if (voteResults.containsKey(theResult)) {
						voteResults.get(theResult).add(
								probResults.get(theResult));
					} else {
						List<Double> probs = new ArrayList<Double>();
						for (int c1 = 1; c1 < c; c1++) {
							probs.add(0.0);
						}
						probs.add(probResults.get(theResult));
						voteResults.put(theResult, probs);
					}
				}
			}

			for (String theResult : voteResults.keySet()) {
				List<Double> probs = voteResults.get(theResult);
				double sumProbs = 0.0;
				for (double theProb : probs) {
					sumProbs += theProb;
				}
				double avgProbs = sumProbs / C;

				for (double theProb : probs) {
					if (theProb < Double.MIN_VALUE)
						continue;
					tkl += theProb * Math.log(theProb / avgProbs);
				}
			}
		}

		{
			Map<String, List<Double>> voteResults = new HashMap<String, List<Double>>();
			for (int c = 0; c < C; c++) {
				int K = kPredictResults[c].k_scores.length;
				for (int k = 0; k < K; k++) {
					String theResult = "";
					for (int t = 0; t < T; t++) {
						theResult = theResult
								+ String.format("#%d->%s",
										kPredictResults[c].k_heads[k][t],
										kPredictResults[c].k_deprels[k][t]);
					}
					if (voteResults.containsKey(theResult)) {
						voteResults.get(theResult).add(
								kPredictResults[c].k_probs[k]);
					} else {
						List<Double> probs = new ArrayList<Double>();
						probs.add(kPredictResults[c].k_probs[k]);
						voteResults.put(theResult, probs);
					}
				}
			}

			for (String theResult : voteResults.keySet()) {
				List<Double> probs = voteResults.get(theResult);
				double sumProbs = 0.0;
				for (double theProb : probs) {
					sumProbs += theProb;
				}
				double avgProbs = sumProbs / C;
				sve += avgProbs * Math.log(avgProbs);
				for (double theProb : probs) {
					if (theProb < Double.MIN_VALUE)
						continue;
					skl += theProb * Math.log(theProb / avgProbs);
				}
			}
			sve = -sve;
		}

		{
			int validLength = 0;
			for (int t = 0; t < T; t++) {
				if (isPunc(forms.get(t), cpostags.get(t))) {
					continue;
				}
				validLength++;
			}
			for (int c1 = 0; c1 < C; c1++) {
				for (int c2 = c1 + 1; c2 < C; c2++) {
					int correct_labels = 0;
					for (int t = 0; t < T; t++) {
						if (isPunc(forms.get(t), cpostags.get(t))) {
							continue;
						}
						if (kPredictResults[c1].k_heads[0][t] == kPredictResults[c2].k_heads[0][t]
								&& kPredictResults[c1].k_deprels[0][t]
										.equals(kPredictResults[c2].k_deprels[0][t])) {
							correct_labels++;
						}
					}
					f_complement += 1.0 - correct_labels * 1.0 / validLength;
				}
			}
		}

	}
	
	public void evaluateAll(List<String> oHeads, List<String> oDeprels, List<String> oPostags) {
		oHeads.clear();
		oDeprels.clear();
		oPostags.clear();
		for (int i = 0; i < 8; i++) {
			eval_res[i] = 0;
		}
		
		int length = forms.size();
		
		for (int i = 0; i < length; i++) {
			
			String[] pposs = feats3.get(i).split("_");
			boolean bContainGoldPos = false;
			String goldpostag = postags.get(i);
			if(goldpostag.equals("_"))goldpostag = cpostags.get(i);
			for(String thePos : pposs)
			{
				if(cpostags.get(i).equals(thePos)
				|| postags.get(i).equals(thePos))
				{
					bContainGoldPos = true;
					break;
				}
			}
			if(bContainGoldPos)
			{
				eval_res[6]++;
				oPostags.add(goldpostag);
			}
			else
			{
				if(pposs.length > 0)
				{
					oPostags.add(pposs[0]);
				}
				else
				{
					oPostags.add("_");
				}
			}
			
			String[] pHeads = feats1.get(i).split("_");
			String[] pDeprels = feats2.get(i).split("_");
			
			boolean bPunc = false;
						
			if (isPunc(forms.get(i), goldpostag)) bPunc = true;
			
			if(!bPunc)eval_res[0]++;
			
			boolean uasRight = false;
									
			for(int k = 0;k < pHeads.length; k++)
			{
				if(heads.get(i).toString().equals(pHeads[k]))
				{
					uasRight = true;
					break;
				}
			}

			if (uasRight && !bPunc) {
				eval_res[1]++;
			}
			
			boolean lasRight = false;
			for(int k = 0;k < pDeprels.length; k++)
			{
				if(pDeprels[k].equals(deprels.get(i)))
				{
					lasRight = true;
					break;
				}
			}
			
			if (uasRight && lasRight && !bPunc) {
				eval_res[2]++;
			}

			if (uasRight && heads.get(i) == 0 && !bPunc) {
				eval_res[5] = 1;
			}
			
			if(uasRight)
			{
				oHeads.add(heads.get(i).toString());
			}
			else
			{
				if(pHeads.length > 0)
				{
					oHeads.add(pHeads[0]);
				}
				else
				{
					oHeads.add("_");	
				}
			}
			
			if(lasRight)
			{
				oDeprels.add(deprels.get(i));
			}
			else
			{
				if(pDeprels.length > 0)
				{
					oDeprels.add(pDeprels[0]);
				}
				else
				{
					oDeprels.add("_");
				}
			}
		}

		if (eval_res[1] == eval_res[0]) {
			eval_res[3] = 1;
		}

		if (eval_res[2] == eval_res[0]) {
			eval_res[4] = 1;
		}
		
		if(eval_res[6] == length)
		{
			eval_res[7] = 1;
		}
	}
	
	
	public static boolean isPunc(String theWord, String thePostag)
	{

		if(thePostag.equals("PU") || thePostag.equals("``")
				 || thePostag.equals("''") || thePostag.equals(",")
				 || thePostag.equals(".") || thePostag.equals(":") 
				 || thePostag.equals("-LRB-") || thePostag.equals("-RRB-")
				 || thePostag.equals("$") || thePostag.equals("#"))
		{
			return true;
		}
		else
		{
			return false;
		}
		
		
	}
	
	public static boolean isPunc(String thePostag)
	{

		if(thePostag.equals("PU") || thePostag.equals("``")
		 || thePostag.equals("''") || thePostag.equals(",")
		 || thePostag.equals(".") || thePostag.equals(":"))
		{
			return true;
		}
		else
		{
			return false;
		}
		
		
	}
	
	public void analysisST1(Map<String, Integer> resultStat)
	{
		
		int length = forms.size();
		if(p1deprels.size() != length || p1heads.size() != length)return;
		for (int i = 0; i < length; i++) {
			String goldpostag = postags.get(i);
			if(goldpostag.equals("_"))goldpostag = cpostags.get(i);
			String predpostag = p1deprels.get(i);
			String posRel = "1";
			if(!goldpostag.equals(predpostag))
			{
				posRel = "0";
			}
			int ghead = heads.get(i);
			int p1head = p1heads.get(i);
			String headRel = "1";
			if(ghead != p1head)
			{
				headRel = "0";
			}
			
			String theKey = goldpostag + "\t" + predpostag + "\t" + posRel + "\t" + headRel;
			if(resultStat.containsKey(theKey))
			{
				resultStat.put(theKey, resultStat.get(theKey)+1);
			}
			else
			{
				resultStat.put(theKey, 1);
			}
		}
	}
	
	
	public void analysisSTAll(Map<String, Integer> resultStat, Map<String, Integer> keyValueStat, Map<String, Integer> dict)
	{
		
		int length = forms.size();
		if(p1deprels.size() != length || p1heads.size() != length)return;
		for (int i = 0; i < length; i++) {
			String goldpostag = postags.get(i);
			if(goldpostag.equals("_"))goldpostag = cpostags.get(i);
			String predpostag = p1deprels.get(i);
			String posRel = "1";
			if(!goldpostag.equals(predpostag))
			{
				posRel = "0";
			}

			int ghead = heads.get(i);
			int p1head = p1heads.get(i);
			String headRel = "1";
			if(ghead != p1head)
			{
				headRel = "0";
			}
			
			
			
			int rdist = 1;
			while(ghead != 0 && rdist < 7)
			{
				rdist++;
				ghead = heads.get(ghead-1);
			}
			String grootDist = String.format("%s", rdist);
			
			int prdist = 1;
			while(p1head != 0 && prdist < 7)
			{
				prdist++;
				p1head = p1heads.get(p1head-1);
			}
			String prootDist = String.format("%s", prdist);
			
			ghead = heads.get(i);
			p1head = p1heads.get(i);
			int gdepdist = ghead - i -1;
			if(gdepdist > 10)gdepdist = 10;
			if(gdepdist < -10)gdepdist = -10;
			String strgDepDist = String.format("%s", Math.abs(gdepdist));
			
			String goldArcDirection = "error";
			if(ghead == 0)
			{
				goldArcDirection = "mid";
			}
			else if(ghead - i -1 > 0)
			{
				goldArcDirection = "right";
			}
			else if(ghead - i -1 < 0)
			{
				goldArcDirection = "left";
			}
			
			String predArcDirection = "error";
			if(p1head == 0)
			{
				predArcDirection = "mid";
			}
			else if(p1head - i -1 > 0)
			{
				predArcDirection = "right";
			}
			else if(p1head - i -1 < 0)
			{
				predArcDirection = "left";
			}
			
			//String gheadPOS = "ROOT";
			//if(ghead  > 0 )
			//{
			//	gheadPOS = postags.get(ghead-1);
			//}
			
			int pdepdist = p1head - i -1;
			if(pdepdist > 10)pdepdist = 10;
			if(pdepdist < -10)pdepdist = -10;
			String strpDepDist = String.format("%s", Math.abs(pdepdist));
			
			int predChildDegree = ChildDegree(p1heads, i);
			if(predChildDegree > 5) predChildDegree = 5;
			String pChildDegree = String.format("%s", predChildDegree);
			
			int goldChildDegree = ChildDegree(heads, i);;
			if(goldChildDegree > 5) goldChildDegree = 5;
			String gChildDegree = String.format("%s", goldChildDegree);
			
			int sentLength = length / 5;
			if(sentLength > 14)sentLength = 14;
			String strSenLen = String.format("%s", sentLength);
			
			int dicFreq = 0;
			String sDictFreq = "F0";
			if(dict.containsKey(forms.get(i)))dicFreq = dict.get(forms.get(i));
			if(dicFreq > 0 && dicFreq <= 50)sDictFreq = "F1";
			//else if(dicFreq > 5 && dicFreq <= 10)sDictFreq = "F2";
			else if(dicFreq > 50 && dicFreq <= 500)sDictFreq = "F2";
			//else if(dicFreq > 100 && dicFreq <= 500)sDictFreq = "F4";
			//else if(dicFreq > 500 && dicFreq <= 1000)sDictFreq = "F5";
			else if(dicFreq > 500)sDictFreq = "F3";
			//String pdepDist = String.format("%s", pdepdist);
			
			List<String> theKeys = new ArrayList<String>();
			List<String> theKeyMarks = new ArrayList<String>();
			//analysis1:pos-pos, key:goldpos
			String theKeyMark = "type1_" + goldpostag;
			String theKey = theKeyMark + "\t" + predpostag + "\t" + posRel;
			theKeyMarks.add(theKeyMark);
			theKeys.add(theKey);
			
			//analysis2:pos-arcbool, key:arcbool
			theKeyMark = "type2_" + headRel;
			theKey = theKeyMark+ "\t" + posRel;
			theKeyMarks.add(theKeyMark);
			theKeys.add(theKey);
			
			//analysis3:pos-pos-arc, key:goldpos+arcbool
			theKeyMark = "type3_" + goldpostag + "_" + headRel;
			theKey = theKeyMark + "\t" + predpostag + "\t" + posRel;
			theKeyMarks.add(theKeyMark);
			theKeys.add(theKey);
			
			//analysis4:pos-freq, key:freq
			theKeyMark = "type4_" + sDictFreq;
			theKey = theKeyMark + "\t" + posRel;
			theKeyMarks.add(theKeyMark);
			theKeys.add(theKey);
			
			//analysis12:pos-arcdirection, key:arcdirection
			theKeyMark = "typeC_" + goldArcDirection;
			theKey = theKeyMark + "\t" + posRel;
			theKeyMarks.add(theKeyMark);
			theKeys.add(theKey);
			
			//analysis18:pos-gChildDegree, key:gChildDegree
			theKeyMark = "typeI_" + gChildDegree;
			theKey = theKeyMark + "\t" + posRel;
			theKeyMarks.add(theKeyMark);
			theKeys.add(theKey);
			
			//analysis19:pos-pChildDegree, key:pChildDegree
			theKeyMark = "typeJ_" + pChildDegree;
			theKey = theKeyMark + "\t" + posRel;
			theKeyMarks.add(theKeyMark);
			theKeys.add(theKey);
			
			if(ghead != 0)
			{
				//analysis22:dep-pdepdist, key:pdepdist
				theKeyMark = "typeM_" + strgDepDist;
				theKey = theKeyMark+ "\t" + posRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
			}
			
			if(p1head != 0)
			{
				//analysis23:dep-pdepdist, key:pdepdist
				theKeyMark = "typeN_" + strpDepDist;
				theKey = theKeyMark+ "\t" + posRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
			}
			
			//analysis24:dep-grootDist, key:grootDist
			theKeyMark = "typeO_" + grootDist;
			theKey = theKeyMark+ "\t" + posRel;
			theKeyMarks.add(theKeyMark);
			theKeys.add(theKey);
			
			//analysis25:dep-prootDist, key:prootDist
			theKeyMark = "typeP_" + prootDist;
			theKey = theKeyMark+ "\t" + posRel;
			theKeyMarks.add(theKeyMark);
			theKeys.add(theKey);
			
			if(!goldpostag.equals("PU"))
			{
				//analysis5:dep-posbool, key:posbool
				theKeyMark = "type5_" + posRel;
				theKey = theKeyMark+ "\t" + headRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
				
				//analysis6:dep-pos, key:goldpos
				theKeyMark = "type6_" + goldpostag;
				theKey = theKeyMark+ "\t" + headRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
				
				//analysis7:dep-senL, key:strSenLen
				theKeyMark = "type7_" + strSenLen;
				theKey = theKeyMark+ "\t" + headRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
				
				//analysis8:dep-gdepdist, key:gdepdist
				
				if(ghead != 0)
				{
					theKeyMark = "type8_" + strgDepDist;
					if(theKeyMark.endsWith("_0"))
					{
						System.out.print("");
					}
					theKey = theKeyMark+ "\t" + headRel;
					theKeyMarks.add(theKeyMark);
					theKeys.add(theKey);
				}
				
				if(p1head != 0)
				{
					//analysis9:dep-pdepdist, key:pdepdist
					theKeyMark = "type9_" + strpDepDist;
					theKey = theKeyMark+ "\t" + headRel;
					theKeyMarks.add(theKeyMark);
					theKeys.add(theKey);
				}
				
				//analysis10:dep-grootDist, key:grootDist
				theKeyMark = "typeA_" + grootDist;
				theKey = theKeyMark+ "\t" + headRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
				
				//analysis11:dep-prootDist, key:prootDist
				theKeyMark = "typeB_" + prootDist;
				theKey = theKeyMark+ "\t" + headRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
				
				//analysis13:dep-arcdirection, key:arcdirection
				theKeyMark = "typeD_" + goldArcDirection;
				theKey = theKeyMark+ "\t" + headRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
				
				//analysis14:dep-arcdirection, key:arcdirection
				theKeyMark = "typeE_" + predArcDirection;
				theKey = theKeyMark+ "\t" + headRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
				
				//analysis15:dep-pos, key:goldpos
				theKeyMark = "typeF_" + goldpostag + "_" + goldArcDirection;
				theKey = theKeyMark+ "\t" + headRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
				
				//analysis16:dep-pos, key:goldpos
				theKeyMark = "typeG_" + goldpostag + "_" + predArcDirection;
				theKey = theKeyMark+ "\t" + headRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
				
				//analysis17:dep-pos, key:goldpos
				theKeyMark = "typeH_" + goldpostag + "_" + predpostag;
				theKey = theKeyMark+ "\t" + headRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
				
				//analysis20:pos-gChildDegree, key:gChildDegree
				theKeyMark = "typeK_" + gChildDegree;
				theKey = theKeyMark + "\t" + headRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
				
				//analysis21:pos-pChildDegree, key:pChildDegree
				theKeyMark = "typeL_" + pChildDegree;
				theKey = theKeyMark + "\t" + headRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
				
				theKeyMark = "typeQ_" + sDictFreq;
				theKey = theKeyMark + "\t" + headRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
			}
			
			//String theKey = goldpostag + "\t" + predpostag + "\t" + posRel + "\t" + headRel;
			//String theKey = headRel + "\t" + posRel;
			for(String curKey : theKeys)
			{
				if(resultStat.containsKey(curKey))
				{
					resultStat.put(curKey, resultStat.get(curKey)+1);
				}
				else
				{
					resultStat.put(curKey, 1);
				}
			}
			
			for(String curKey : theKeyMarks)
			{
				if(keyValueStat.containsKey(curKey))
				{
					keyValueStat.put(curKey, keyValueStat.get(curKey)+1);
				}
				else
				{
					keyValueStat.put(curKey, 1);
				}
			}
		}
	}
	
	public int Hight(int i)
	{
		Set<Integer> leaves = new HashSet<Integer>();
		for(int idx = 0; idx < forms.size(); idx++)
		{
			int ghead = heads.get(idx);
			if(ghead - 1 == i)
			{
				leaves.add(idx);
			}
		}
		if(leaves.size() == 0)return 1;
		
		int maxDepth = -1;
		for(Integer idx : leaves)
		{
			int curDepth = Hight(idx);
			if(curDepth > maxDepth)
			{
				maxDepth = curDepth;
			}
		}
		
		return maxDepth + 1;
	}
	
	public static int ChildDegree(List<Integer> curHeads, int curId)
	{
		if(curId < 0 || curId > curHeads.size())
		{
			return -1;
		}
		
		int childCount = 0;
		
		for(int curHead : curHeads)
		{
			if(curHead == curId + 1)childCount++;
		}
		
		return childCount;
	}
	
	
	public void SigEvaluate(List<Integer> evalMetrics)
	{
		evalMetrics.clear();		
		int length = forms.size();
		if(p1deprels.size() != length || p1heads.size() != length)return;
		
		
		int scoreLength = 0;
		int headCorrect = 0;
		int  posCorrect = 0;
				
		for (int i = 0; i < length; i++) {
			String goldpostag = postags.get(i);
			if(goldpostag.equals("_"))goldpostag = cpostags.get(i);
			String predpostag = p1deprels.get(i);
			if(goldpostag.equals(predpostag))
			{
				posCorrect++;
			}			
			if(!goldpostag.equals("PU"))
			{
				int ghead = heads.get(i);
				int p1head = p1heads.get(i);
				scoreLength++;
				if(ghead == p1head )
				{
					headCorrect++;
				}
			}
		}
		evalMetrics.add(length);
		evalMetrics.add(scoreLength);
		evalMetrics.add(posCorrect);
		evalMetrics.add(headCorrect);		
	}
	
	
	
	public void analysisForColing14(Map<String, Integer> resultStat, Map<String, Integer> keyValueStat, DepInstance other)
	{
		
		int length = forms.size();
		if(other.deprels.size() != length || other.heads.size() != length)return;
		for (int i = 0; i < length; i++) {
			//String goldpostag = UniversPostager.GetEnglishUniverPOSTag(postags.get(i));
			String goldpostag = postags.get(i);

			int ghead = heads.get(i);
			String glabel = deprels.get(i);
			int p1head = other.heads.get(i);	
			String p1label = other.deprels.get(i);
			String headRel = "0";
			if(ghead == p1head)
			{
				headRel = "1";
			}
			
			String headlabelRel = "0";
			if(ghead == p1head && glabel.equals(p1label))
			{
				headlabelRel = "1";
			}
			
			
			
			int rdist = 1;
			while(ghead != 0 && rdist < 7)
			{
				rdist++;
				ghead = heads.get(ghead-1);
			}
			String grootDist = String.format("%s", rdist);
			
			int prdist = 1;
			while(p1head != 0 && prdist < 7)
			{
				prdist++;
				p1head = other.heads.get(p1head-1);
			}
			String prootDist = String.format("%s", prdist);
			
			ghead = heads.get(i);
			p1head = other.heads.get(i);
			int gdepdist = ghead - i -1;
			if(gdepdist > 7)gdepdist = 7;
			if(gdepdist < -7)gdepdist = -7;
			String strgDepDist = String.format("%s", Math.abs(gdepdist));
						
			
			int pdepdist = p1head - i -1;
			if(pdepdist > 7)pdepdist = 7;
			if(pdepdist < -7)pdepdist = -7;
			String strpDepDist = String.format("%s", Math.abs(pdepdist));
			
			
			int sentLength = length / 10;
			if(sentLength > 6)sentLength = 6;
			String strSenLen = String.format("%s", sentLength);
			
	
			
			List<String> theKeys = new ArrayList<String>();
			List<String> theKeyMarks = new ArrayList<String>();
			String theKeyMark = null;
			String theKey = null;

			
			if( !isPunc(forms.get(i), cpostags.get(i)))
			{
				//analysis1:shared structure, key:shared structure
				theKeyMark = "uas_shared_" + String.format("%d", bshared.get(i));
				theKey = theKeyMark+ "\t" + headRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
				
				//analysis2:dep-pos, key:goldpos
				theKeyMark = "uas_goldpos_" + goldpostag;
				theKey = theKeyMark+ "\t" + headRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
				
				//analysis3:dep-senL, key:strSenLen
				theKeyMark = "uas_sentlen_" + strSenLen;
				theKey = theKeyMark+ "\t" + headRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
				
				//analysis4:dep-gdepdist, key:gdepdist				
				if(ghead != 0)
				{
					theKeyMark = "uas_dist_" + strgDepDist + "_recall";
					if(theKeyMark.endsWith("_0"))
					{
						System.out.print("error...");
					}
					theKey = theKeyMark+ "\t" + headRel;
					theKeyMarks.add(theKeyMark);
					theKeys.add(theKey);
				}
				
				if(p1head != 0)
				{
					//analysis5:dep-pdepdist, key:pdepdist
					theKeyMark = "uas_dist_" + strpDepDist + "_prec";
					theKey = theKeyMark+ "\t" + headRel;
					theKeyMarks.add(theKeyMark);
					theKeys.add(theKey);
				}
				
				//analysis6:dep-grootDist, key:grootDist
				//theKeyMark = "uas_depth_" + grootDist + "_recall";
				//theKey = theKeyMark+ "\t" + headRel;
				//theKeyMarks.add(theKeyMark);
				//theKeys.add(theKey);
				
				//analysis7:dep-prootDist, key:prootDist
				//theKeyMark = "uas_depth_" + prootDist + "_prec";
				//theKey = theKeyMark+ "\t" + headRel;
				//theKeyMarks.add(theKeyMark);
				//theKeys.add(theKey);
				

				//analysis1:shared structure, key:shared structure
				theKeyMark = "las_shared_" + String.format("%d", bshared.get(i));
				theKey = theKeyMark+ "\t" + headlabelRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
				
				//analysis2:dep-pos, key:goldpos
				theKeyMark = "las_goldpos_" + goldpostag;
				theKey = theKeyMark+ "\t" + headlabelRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
				
				//analysis3:dep-senL, key:strSenLen
				theKeyMark = "las_sentlen_" + strSenLen;
				theKey = theKeyMark+ "\t" + headlabelRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
				
				//analysis4:dep-gdepdist, key:gdepdist				
				if(ghead != 0)
				{
					theKeyMark = "las_dist_" + strgDepDist + "_recall";
					if(theKeyMark.endsWith("_0"))
					{
						System.out.print("error...");
					}
					theKey = theKeyMark+ "\t" + headlabelRel;
					theKeyMarks.add(theKeyMark);
					theKeys.add(theKey);
				}
				
				if(p1head != 0)
				{
					//analysis5:dep-pdepdist, key:pdepdist
					theKeyMark = "las_dist_" + strpDepDist + "_prec";
					theKey = theKeyMark+ "\t" + headlabelRel;
					theKeyMarks.add(theKeyMark);
					theKeys.add(theKey);
				}
				
				//analysis6:dep-grootDist, key:grootDist
				//theKeyMark = "las_depth_" + grootDist + "_recall";
				//theKey = theKeyMark+ "\t" + headlabelRel;
				//theKeyMarks.add(theKeyMark);
				//theKeys.add(theKey);
				
				//analysis7:dep-prootDist, key:prootDist
				//theKeyMark = "las_depth_" + prootDist + "_prec";
				//theKey = theKeyMark+ "\t" + headlabelRel;
				//theKeyMarks.add(theKeyMark);
				//theKeys.add(theKey);
				
			}
			
			//String theKey = goldpostag + "\t" + predpostag + "\t" + posRel + "\t" + headRel;
			//String theKey = headRel + "\t" + posRel;
			for(String curKey : theKeys)
			{
				if(resultStat.containsKey(curKey))
				{
					resultStat.put(curKey, resultStat.get(curKey)+1);
				}
				else
				{
					resultStat.put(curKey, 1);
				}
			}
			
			for(String curKey : theKeyMarks)
			{
				if(keyValueStat.containsKey(curKey))
				{
					keyValueStat.put(curKey, keyValueStat.get(curKey)+1);
				}
				else
				{
					keyValueStat.put(curKey, 1);
				}
			}
		}
	}
	
	
	public boolean TTestDataBySent(DepInstance other1, DepInstance other2,Map<String, List<Double>> analysisResult)
	{
		analysisResult.clear();
		
		

		Map<String, Set<String>>  goldwordstructuresbyattributes = new HashMap<String, Set<String>>();
		
		Set<String> analysisedKey = new HashSet<String>();
		
		int length = forms.size();
		if(other1.forms.size() != length || other2.forms.size() != length)return false;
		
		int[] other1_eval_res = new int[8];
		for (int i = 0; i < 8; i++) {
			other1_eval_res[i] = 0;
		}
		


		for (int i = 0; i < length; i++) {
			if (isPunc(forms.get(i), cpostags.get(i)) || isPunc(forms.get(i), postags.get(i))) {
				continue;
			}
			other1_eval_res[0]++;
			int curHead = heads.get(i);
			int otherHead = other1.heads.get(i);
			if (otherHead == curHead) {
				other1_eval_res[1]++;
			}

			

			if (otherHead == curHead
					&& other1.deprels.get(i).equals(deprels.get(i))) {
				other1_eval_res[2]++;
			}

			if (other1.heads.get(i) == 0 && heads.get(i) == 0) {
				other1_eval_res[5] = 1;
			}
		}

		if (other1_eval_res[1] == other1_eval_res[0]) {
			other1_eval_res[3] = 1;
		}

		if (other1_eval_res[2] == other1_eval_res[0]) {
			other1_eval_res[4] = 1;
		}
		

		
		int[] other2_eval_res = new int[8];
		for (int i = 0; i < 8; i++) {
			other2_eval_res[i] = 0;
		}
		


		for (int i = 0; i < length; i++) {
			if (isPunc(forms.get(i), cpostags.get(i)) || isPunc(forms.get(i), postags.get(i))) {
				continue;
			}
			other2_eval_res[0]++;
			int curHead = heads.get(i);
			int otherHead = other2.heads.get(i);
			if (otherHead == curHead) {
				other2_eval_res[1]++;
			}

			

			if (otherHead == curHead
					&& other2.deprels.get(i).equals(deprels.get(i))) {
				other2_eval_res[2]++;
			}

			if (other2.heads.get(i) == 0 && heads.get(i) == 0) {
				other2_eval_res[5] = 1;
			}
		}

		if (other2_eval_res[1] == other2_eval_res[0]) {
			other2_eval_res[3] = 1;
		}

		if (other2_eval_res[2] == other2_eval_res[0]) {
			other2_eval_res[4] = 1;
		}
		
		analysisResult.put("UAS=1F", new ArrayList<Double>());
		//analysisResult.get("UAS=1F").add(other1_eval_res[1]*1.0/other1_eval_res[0]);
		for(int idx = 0; idx < other1_eval_res[1]; idx++)
		{
			analysisResult.get("UAS=1F").add(1.0);
		}
		for(int idx = 0; idx < other1_eval_res[0] - other1_eval_res[1]; idx++)
		{
			analysisResult.get("UAS=1F").add(0.0);
		}
		analysisResult.put("UAS=2F", new ArrayList<Double>());
		//analysisResult.get("UAS=2F").add(other2_eval_res[1]*1.0/other2_eval_res[0]);
		for(int idx = 0; idx < other2_eval_res[1]; idx++)
		{
			analysisResult.get("UAS=2F").add(1.0);
		}
		for(int idx = 0; idx < other2_eval_res[0] - other2_eval_res[1]; idx++)
		{
			analysisResult.get("UAS=2F").add(0.0);
		}

		
		analysisResult.put("LAS=1F", new ArrayList<Double>());
		//analysisResult.get("LAS=1F").add(other1_eval_res[2]*1.0/other1_eval_res[0]);
		for(int idx = 0; idx < other1_eval_res[2]; idx++)
		{
			analysisResult.get("LAS=1F").add(1.0);
		}
		for(int idx = 0; idx < other1_eval_res[0] - other1_eval_res[2]; idx++)
		{
			analysisResult.get("LAS=1F").add(0.0);
		}
		analysisResult.put("LAS=2F", new ArrayList<Double>());
		//analysisResult.get("LAS=2F").add(other2_eval_res[2]*1.0/other2_eval_res[0]);
		for(int idx = 0; idx < other2_eval_res[2]; idx++)
		{
			analysisResult.get("LAS=2F").add(1.0);
		}
		for(int idx = 0; idx < other2_eval_res[0] - other2_eval_res[2]; idx++)
		{
			analysisResult.get("LAS=2F").add(0.0);
		}
		
		
		analysisResult.put("CM=1F", new ArrayList<Double>());
		analysisResult.get("CM=1F").add(other1_eval_res[4]*1.0);
		analysisResult.put("CM=2F", new ArrayList<Double>());
		analysisResult.get("CM=2F").add(other2_eval_res[4]*1.0);

					
		return true;
	}
	
	
	public void analysisForPhdSection2(Map<String, Integer> resultStat, Map<String, Integer> keyValueStat, DepInstance other)
	{
		
		int length = forms.size();
		if(other.deprels.size() != length || other.heads.size() != length)return;
		for (int i = 0; i < length; i++) {
			String goldpostag = postags.get(i);
			int ghead = heads.get(i);
			String glabel = deprels.get(i);
			int p1head = other.heads.get(i);	
			String p1label = other.deprels.get(i);
			String headRel = "0";
			if(ghead == p1head)
			{
				headRel = "1";
			}
			
			String headlabelRel = "0";
			if(ghead == p1head && glabel.equals(p1label))
			{
				headlabelRel = "1";
			}
			
			String headlabelMap = glabel + "#" + p1label;
			
			String posMap = "";
			if(ghead-1 >= 0 )
			{
				posMap = postags.get(i) + "#" + postags.get(ghead-1);
			}
			else
			{
				posMap = postags.get(i) + "#START";
			}
			
			
			int rdist = 1;
			while(ghead != 0 && rdist < 7)
			{
				rdist++;
				ghead = heads.get(ghead-1);
			}
			String grootDist = String.format("%s", rdist);
			
			int prdist = 1;
			while(p1head != 0 && prdist < 7)
			{
				prdist++;
				p1head = other.heads.get(p1head-1);
			}
			String prootDist = String.format("%s", prdist);
			
			ghead = heads.get(i);
			p1head = other.heads.get(i);
			int gdepdist = ghead - i -1;
			if(gdepdist > 10)gdepdist = 10;
			if(gdepdist < -10)gdepdist = -10;
			String strgDepDist = String.format("%s", Math.abs(gdepdist));
						
			
			int pdepdist = p1head - i -1;
			if(pdepdist > 10)pdepdist = 10;
			if(pdepdist < -10)pdepdist = -10;
			String strpDepDist = String.format("%s", Math.abs(pdepdist));
			
			
			int sentLength = length / 10;
			if(sentLength > 6)sentLength = 6;
			String strSenLen = String.format("%s", sentLength);
			
	
			
			List<String> theKeys = new ArrayList<String>();
			List<String> theKeyMarks = new ArrayList<String>();
			String theKeyMark = null;
			String theKey = null;

			
			if( !isPunc(forms.get(i), cpostags.get(i)))
			{
				//analysis1:labelpair, key:strSenLen
				theKeyMark = "uas_labelpair_" + headlabelMap;
				theKey = theKeyMark+ "\t" + headRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
				
				//analysis1.5:labelpair, key:strSenLen
				theKeyMark = "uas_pospair_" + posMap;
				theKey = theKeyMark+ "\t" + headRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
				
				//analysis2:dep-pos, key:goldpos
				theKeyMark = "uas_goldpos_" + goldpostag;
				theKey = theKeyMark+ "\t" + headRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
				
				//analysis3:dep-senL, key:strSenLen
				theKeyMark = "uas_sentlen_" + strSenLen;
				theKey = theKeyMark+ "\t" + headRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
				
				//analysis4:dep-gdepdist, key:gdepdist				
				if(ghead != 0)
				{
					theKeyMark = "uas_dist_" + strgDepDist + "_recall";
					if(theKeyMark.endsWith("_0"))
					{
						System.out.print("error...");
					}
					theKey = theKeyMark+ "\t" + headRel;
					theKeyMarks.add(theKeyMark);
					theKeys.add(theKey);
				}
				
				if(p1head != 0)
				{
					//analysis5:dep-pdepdist, key:pdepdist
					theKeyMark = "uas_dist_" + strpDepDist + "_prec";
					theKey = theKeyMark+ "\t" + headRel;
					theKeyMarks.add(theKeyMark);
					theKeys.add(theKey);
				}
				
				//analysis6:dep-grootDist, key:grootDist
				//theKeyMark = "uas_depth_" + grootDist + "_recall";
				//theKey = theKeyMark+ "\t" + headRel;
				//theKeyMarks.add(theKeyMark);
				//theKeys.add(theKey);
				
				//analysis7:dep-prootDist, key:prootDist
				//theKeyMark = "uas_depth_" + prootDist + "_prec";
				//theKey = theKeyMark+ "\t" + headRel;
				//theKeyMarks.add(theKeyMark);
				//theKeys.add(theKey);
				
				
				
				//analysis2:dep-pos, key:goldpos
				theKeyMark = "las_goldpos_" + goldpostag;
				theKey = theKeyMark+ "\t" + headlabelRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
				
				//analysis3:dep-senL, key:strSenLen
				theKeyMark = "las_sentlen_" + strSenLen;
				theKey = theKeyMark+ "\t" + headlabelRel;
				theKeyMarks.add(theKeyMark);
				theKeys.add(theKey);
				
				//analysis4:dep-gdepdist, key:gdepdist				
				if(ghead != 0)
				{
					theKeyMark = "las_dist_" + strgDepDist + "_recall";
					if(theKeyMark.endsWith("_0"))
					{
						System.out.print("error...");
					}
					theKey = theKeyMark+ "\t" + headlabelRel;
					theKeyMarks.add(theKeyMark);
					theKeys.add(theKey);
				}
				
				if(p1head != 0)
				{
					//analysis5:dep-pdepdist, key:pdepdist
					theKeyMark = "las_dist_" + strpDepDist + "_prec";
					theKey = theKeyMark+ "\t" + headlabelRel;
					theKeyMarks.add(theKeyMark);
					theKeys.add(theKey);
				}
				
				//analysis6:dep-grootDist, key:grootDist
				//theKeyMark = "las_depth_" + grootDist + "_recall";
				//theKey = theKeyMark+ "\t" + headlabelRel;
				//theKeyMarks.add(theKeyMark);
				//theKeys.add(theKey);
				
				//analysis7:dep-prootDist, key:prootDist
				//theKeyMark = "las_depth_" + prootDist + "_prec";
				//theKey = theKeyMark+ "\t" + headlabelRel;
				//theKeyMarks.add(theKeyMark);
				//theKeys.add(theKey);
				
			}
			
			//String theKey = goldpostag + "\t" + predpostag + "\t" + posRel + "\t" + headRel;
			//String theKey = headRel + "\t" + posRel;
			for(String curKey : theKeys)
			{
				if(resultStat.containsKey(curKey))
				{
					resultStat.put(curKey, resultStat.get(curKey)+1);
				}
				else
				{
					resultStat.put(curKey, 1);
				}
			}
			
			for(String curKey : theKeyMarks)
			{
				if(keyValueStat.containsKey(curKey))
				{
					keyValueStat.put(curKey, keyValueStat.get(curKey)+1);
				}
				else
				{
					keyValueStat.put(curKey, 1);
				}
			}
		}
	}
	

	
	
}
