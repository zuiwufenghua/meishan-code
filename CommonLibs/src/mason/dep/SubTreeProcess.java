package mason.dep;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;

public class SubTreeProcess {
	SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();

	// SDPCorpusReader sdpCorpusReader_ex = new SDPCorpusReader();

	public void init(String inputFile) throws Exception {
		sdpCorpusReader.Init(inputFile);
	}
	
	public void subtreeExtractFeature(String outputFile) throws Exception {
		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		int totalInstances = vecInstances.size();
		//Map<String, Double> errorContents = new HashMap<String, Double>();
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8"));
		for (int i = 0; i < totalInstances; i++) {
			DepInstance tmpInstance = vecInstances.get(i);

			Integer[] predheads= new Integer[tmpInstance.heads.size()];
			tmpInstance.heads.toArray(predheads);
			String[] predlabels = new String[tmpInstance.deprels.size()];
			tmpInstance.deprels.toArray(predlabels);
			/*
			for (int j1 = 0; j1 < tmpInstance.size(); j1++) 
				for (int j2 = j1+1; j2 < tmpInstance.size(); j2++)
			{
				int[] subIds2 = new int[2]; subIds2[0] = j1; subIds2[1] = j2;
				String[] outInfos = new String[3];
				boolean perfectTree = findTargetMinSubTree(tmpInstance.forms, tmpInstance.deprels,
						tmpInstance.cpostags, predheads, subIds2, outInfos);
				if(perfectTree)
				{
					String errorContent = getModeType(tmpInstance.forms,subIds2) +"#=#" + outInfos[0];
					Process.addMapItem(errorContents, errorContent, 1.0);
				}
			}*/
			
			
			
			for (int j1 = 0; j1 < tmpInstance.size(); j1++) 
				for (int j2 = j1+1; j2 < tmpInstance.size(); j2++)
					for (int j3 = j2+1; j3 < tmpInstance.size(); j3++)
			{
				int[] subIds3 = new int[3]; subIds3[0] = j1; subIds3[1] = j2;
				subIds3[2] = j3; 
				String[] outInfos = new String[3];
				boolean perfectTree = findTargetMinSubTree(tmpInstance.forms, tmpInstance.deprels,
						tmpInstance.cpostags, predheads, subIds3, outInfos);
				if(perfectTree)
				{
					String errorContent = getModeType(tmpInstance.forms,subIds3) +"#=#" + outInfos[0];
				}
			}
			/*
			for (int j1 = 0; j1 < tmpInstance.size(); j1++) 
				for (int j2 = j1+1; j2 < tmpInstance.size(); j2++)
					for (int j3 = j2+1; j3 < tmpInstance.size(); j3++)
						for (int j4 = j3+1; j4 < tmpInstance.size(); j4++)
			{
				int[] subIds4 = new int[4]; subIds4[0] = j1; subIds4[1] = j2;
				subIds4[2] = j3; subIds4[3] = j4; 
				String[] outInfos = new String[3];
				boolean perfectTree = findTargetMinSubTree(tmpInstance.forms, tmpInstance.deprels,
						tmpInstance.cpostags, predheads, subIds4, outInfos);
				if(perfectTree)
				{
					String errorContent = getModeType(tmpInstance.forms,subIds4) +"#=#" + outInfos[0];
					Process.addMapItem(errorContents, errorContent, 1.0);
				}
			}
			
			for (int j1 = 0; j1 < tmpInstance.size(); j1++) 
				for (int j2 = j1+1; j2 < tmpInstance.size(); j2++)
					for (int j3 = j2+1; j3 < tmpInstance.size(); j3++)
						for (int j4 = j3+1; j4 < tmpInstance.size(); j4++)
							for (int j5 = j4+1; j5 < tmpInstance.size(); j5++)
			{
				int[] subIds5 = new int[5]; subIds5[0] = j1; subIds5[1] = j2;
				subIds5[2] = j3; subIds5[3] = j4; subIds5[4] = j5;
				String[] outInfos = new String[3];
				boolean perfectTree = findTargetMinSubTree(tmpInstance.forms, tmpInstance.deprels,
						tmpInstance.cpostags, predheads, subIds5, outInfos);
				if(perfectTree)
				{
					String errorContent = getModeType(tmpInstance.forms,subIds5) +"#=#" + outInfos[0];
					Process.addMapItem(errorContents, errorContent, 1.0);
				}
			}*/
		}


		output.close();
	}
	
	
	
	public void subtreeExtract(String outputFile) throws Exception {
		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		int totalInstances = vecInstances.size();
		Map<String, Double> errorContents = new HashMap<String, Double>();

		for (int i = 0; i < totalInstances; i++) {
			DepInstance tmpInstance = vecInstances.get(i);

			Integer[] predheads= new Integer[tmpInstance.heads.size()];
			tmpInstance.heads.toArray(predheads);
			String[] predlabels = new String[tmpInstance.deprels.size()];
			tmpInstance.deprels.toArray(predlabels);
			/*
			for (int j1 = 0; j1 < tmpInstance.size(); j1++) 
				for (int j2 = j1+1; j2 < tmpInstance.size(); j2++)
			{
				int[] subIds2 = new int[2]; subIds2[0] = j1; subIds2[1] = j2;
				String[] outInfos = new String[3];
				boolean perfectTree = findTargetMinSubTree(tmpInstance.forms, tmpInstance.deprels,
						tmpInstance.cpostags, predheads, subIds2, outInfos);
				if(perfectTree)
				{
					String errorContent = getModeType(tmpInstance.forms,subIds2) +"#=#" + outInfos[0];
					Process.addMapItem(errorContents, errorContent, 1.0);
				}
			}*/
			
			
			
			for (int j1 = 0; j1 < tmpInstance.size(); j1++) 
				for (int j2 = j1+1; j2 < tmpInstance.size(); j2++)
					for (int j3 = j2+1; j3 < tmpInstance.size(); j3++)
			{
				int[] subIds3 = new int[3]; subIds3[0] = j1; subIds3[1] = j2;
				subIds3[2] = j3; 
				String[] outInfos = new String[3];
				boolean perfectTree = findTargetMinSubTree(tmpInstance.forms, tmpInstance.deprels,
						tmpInstance.cpostags, predheads, subIds3, outInfos);
				if(perfectTree)
				{
					String errorContent = getModeType(tmpInstance.forms,subIds3) +"#=#" + outInfos[0];
					Process.addMapItem(errorContents, errorContent, 1.0);
				}
			}
			/*
			for (int j1 = 0; j1 < tmpInstance.size(); j1++) 
				for (int j2 = j1+1; j2 < tmpInstance.size(); j2++)
					for (int j3 = j2+1; j3 < tmpInstance.size(); j3++)
						for (int j4 = j3+1; j4 < tmpInstance.size(); j4++)
			{
				int[] subIds4 = new int[4]; subIds4[0] = j1; subIds4[1] = j2;
				subIds4[2] = j3; subIds4[3] = j4; 
				String[] outInfos = new String[3];
				boolean perfectTree = findTargetMinSubTree(tmpInstance.forms, tmpInstance.deprels,
						tmpInstance.cpostags, predheads, subIds4, outInfos);
				if(perfectTree)
				{
					String errorContent = getModeType(tmpInstance.forms,subIds4) +"#=#" + outInfos[0];
					Process.addMapItem(errorContents, errorContent, 1.0);
				}
			}
			
			for (int j1 = 0; j1 < tmpInstance.size(); j1++) 
				for (int j2 = j1+1; j2 < tmpInstance.size(); j2++)
					for (int j3 = j2+1; j3 < tmpInstance.size(); j3++)
						for (int j4 = j3+1; j4 < tmpInstance.size(); j4++)
							for (int j5 = j4+1; j5 < tmpInstance.size(); j5++)
			{
				int[] subIds5 = new int[5]; subIds5[0] = j1; subIds5[1] = j2;
				subIds5[2] = j3; subIds5[3] = j4; subIds5[4] = j5;
				String[] outInfos = new String[3];
				boolean perfectTree = findTargetMinSubTree(tmpInstance.forms, tmpInstance.deprels,
						tmpInstance.cpostags, predheads, subIds5, outInfos);
				if(perfectTree)
				{
					String errorContent = getModeType(tmpInstance.forms,subIds5) +"#=#" + outInfos[0];
					Process.addMapItem(errorContents, errorContent, 1.0);
				}
			}*/
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
	
	
	public static boolean findTargetMinSubTree(List<String> forms, List<String> labels,
			List<String> cpostags, Integer[] heads, int[] subIds, String[] outInfos) {
		if (subIds.length < 1 || heads.length != cpostags.size()
				|| cpostags.size() != forms.size())
			return false;
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

			String curTag = String.format("%s", forms.get(commonfatherId));
			treeNodes.put(commonfatherId, curTag);
		}

		for (int i = 0; i < subIds.length; i++) {
			int curId = subIds[i];
			String curTag = String.format("(%s)%s", labels.get(curId), forms.get(curId));
			treeNodes.put(curId, curTag);
			if(treeNodes.size()>subIds.length)return false;
			while (curId != commonfatherId) {
				curId = heads[curId] - 1;
				curTag = String.format("(%s)%s", labels.get(curId), forms.get(curId));
				treeNodes.put(curId, curTag);
				if(treeNodes.size()>subIds.length)return false;
			}
		}
		if(treeNodes.size() > subIds.length)return false;

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

		return true;
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
	}
	
	public static String getModeType(List<String> forms, int[] subIds)
	{
		String modeType = "";
		String[] subWords = new String[subIds.length];
		int insert_pos = 0;
		for (int i = 0; i < subIds.length; i++) {
			String curSubWord = forms.get(subIds[i]);
			for (int k = 0; k < subWords.length; k++) {
				if (subWords[k] == null) {
					insert_pos = k;
					break;
				}
				if (subWords[k].compareTo(curSubWord) > 0) {
					insert_pos = k;
					break;
				}
			}

			for (int k = subWords.length - 1; k > insert_pos; k--) {
				subWords[k] = subWords[k - 1];
			}
			subWords[insert_pos] = curSubWord;
		}
		
		for (int i = 0; i < subWords.length; i++) {
			modeType = modeType + "[" + subWords[i] + "]";
		}
		
		return modeType;
	}
	
	
	public void convert(String outputFile) throws Exception 
	{
		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		int totalInstances = vecInstances.size();
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8"));
		for (int i = 0; i < totalInstances; i++) {
			DepInstance tmpInstance = vecInstances.get(i);
						
			for (int j1 = 0; j1 < tmpInstance.size(); j1++) 
			{
				tmpInstance.deprels.set(j1, depRealtionMap(tmpInstance.deprels.get(j1)));
			}
			List<String> curOutPuts = new ArrayList<String>();
			tmpInstance.toGoldListString(curOutPuts);
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

	public static String depRealtionMap(String srcRel)
	{
		Map<String, String> relationMap = new HashMap<String, String>();

		relationMap.put("aux-depend", "particle");
		relationMap.put("prep-depend", "particle");
		relationMap.put("PU", "pu");
		relationMap.put("ROOT", "root");
		
		relationMap.put("isa", "object");
		relationMap.put("content", "object");
		relationMap.put("possession", "object");
		relationMap.put("patient", "object");
		relationMap.put("beneficiary", "object");
		
		relationMap.put("contrast", "neighbour");
		relationMap.put("partner", "neighbour");
		
		relationMap.put("scope", "environment");
		relationMap.put("basis", "environment");
		
		
		relationMap.put("frequency", "attribute");
		relationMap.put("material", "resort");
		relationMap.put("instrument", "resort");
		relationMap.put("means", "resort");
		relationMap.put("times", "attribute");
		relationMap.put("angle", "resort");
		relationMap.put("sequence", "attribute");
		relationMap.put("sequence-p", "attribute");		
		relationMap.put("aspect", "logic");	
		relationMap.put("negation", "resort");
		relationMap.put("degree", "resort");
		relationMap.put("modal", "logic");
		relationMap.put("emphasis", "resort");
		relationMap.put("manner", "resort");
		
		
		relationMap.put("LocationFin", "environment");		
		relationMap.put("LocationIni", "environment");	
		relationMap.put("LocationThru", "environment");
		relationMap.put("StateFin", "logic");
		relationMap.put("StateIni", "logic");
		relationMap.put("direction", "environment");
		relationMap.put("distance", "environment");
		relationMap.put("state", "logic");
		relationMap.put("location", "environment");
		
		relationMap.put("duration", "environment");
		relationMap.put("TimeFin", "environment");
		relationMap.put("TimeIni", "environment");
		relationMap.put("time", "environment");
		relationMap.put("TimeAdv", "environment");
		
		
		relationMap.put("OfPart", "object");
		relationMap.put("cause", "resort");
		relationMap.put("cost", "resort");		
		relationMap.put("concerning", "composition");
		relationMap.put("accompaniment", "composition");
		relationMap.put("succeeding", "composition");
		relationMap.put("comment", "resort");
		
		
		relationMap.put("d-deno", "environment");
		relationMap.put("d-deno-p", "environment");
		relationMap.put("d-TimePhrase", "environment");
		relationMap.put("d-LocPhrase", "environment");
		relationMap.put("d-material", "resort");
		relationMap.put("qp-mod", "attribute");
		
		relationMap.put("agent", "subject");
		relationMap.put("experiencer", "subject");
		relationMap.put("causer", "subject");
		relationMap.put("possessor", "subject");
		relationMap.put("existent", "subject");
		relationMap.put("whole", "subject");
		relationMap.put("relevant", "subject");
		
		

		
		String result = "invalid";
		if(relationMap.containsKey(srcRel))
		{
			result = relationMap.get(srcRel);
		}
		else if(srcRel.startsWith("j-"))
		{
			String srcRelMod = srcRel.substring(2);
			if(relationMap.containsKey(srcRelMod))
			{
				result = relationMap.get(srcRelMod);
			}
		}
		else if(srcRel.startsWith("d-") || srcRel.startsWith("c-")
				 || srcRel.startsWith("r-"))
		{
			result = "attribute";
		}
		else if(srcRel.startsWith("s-") )
		{
			result = "composition";
		}
		
		if(result.equals("invalid"))
		{
			result = srcRel;
		}
		
		return result;
	}
}
