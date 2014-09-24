package acl2014;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Stack;

import edu.berkeley.nlp.syntax.Tree;

public class CharDependency {

	public List<String> forms;
	public List<String> lemmas;
	public List<String> cpostags;
	public List<String> postags;
	public List<Integer> heads;
	public List<String> deprels;
	public List<String> feats;
	public List<Integer> p1heads;
	public List<String> p1deprels;
	
	public List<Integer> wordboundarystarts;  // the start boundary of the word including the current character
	public List<Integer> wordboundaryends;    // the end boundary of the word including the current character
	




	// for evaluate
	// 0 cur_sent_word_num;  1 other_sent_word_num;
	// 2 out_uas_correct_num; 3 out_las_correct_num;  
	// 4 root_correct; (above the word); 5 gold_dep_num; 6 pred_dep_num;
	// 7 in_uas_correct_num; 8 gold_in_uas_num; 9 pred_in_uas_num
	// 10 word_structure_correct; 11 word_correct;  12 pos_correct; 
	// 
	public int[] eval_res;

	public CharDependency() {
		forms = new ArrayList<String>();
		lemmas = new ArrayList<String>();
		cpostags = new ArrayList<String>();
		postags = new ArrayList<String>();
		heads = new ArrayList<Integer>();
		deprels = new ArrayList<String>();
		feats = new ArrayList<String>();
		p1heads = new ArrayList<Integer>();
		p1deprels = new ArrayList<String>();
		wordboundarystarts = new ArrayList<Integer>();
		wordboundaryends = new ArrayList<Integer>();
		


		eval_res = new int[30];
		for (int i = 0; i < 30; i++) {
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
		feats = new ArrayList<String>();
		p1heads = new ArrayList<Integer>();
		p1deprels = new ArrayList<String>();
		wordboundarystarts = new ArrayList<Integer>();
		wordboundaryends = new ArrayList<Integer>();
		


		eval_res = new int[20];
		for (int i = 0; i < 20; i++) {
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
				else if(idx == 5)curContent = feats.get(index);
				else if(idx == 6)curContent = String.format("%d", heads.get(index));
				else if(idx == 7)curContent = deprels.get(index);
				else if(idx == 8)curContent = String.format("%d", p1heads.get(index));
				else if(idx == 9)curContent = p1deprels.get(index);
				else
				{

				}
				
			}
			
			result = result + "\t" + curContent;
		}
		
		return result;
	}
	
	
	public List<String> toListString() {
		List<String> outputs = new ArrayList<String>();
		int length = forms.size();
		for (int i = 0; i < length; i++) {
			String tmpOut = String.format(
					"%d\t%s\t%s\t%s\t%s\t%s\t%d\t%s\t_\t_", i + 1, forms.get(i),
					lemmas.get(i), cpostags.get(i), postags.get(i),feats.get(i),
					heads.get(i), deprels.get(i));

			outputs.add(tmpOut);
		}
		
		return outputs;
	}

	public int size() {
		return forms.size();
	}
	
	public void init()
	{
		// mainly for boundaries
		int curstartboundary = 0;
		int curendboundary = -1;
		int length = forms.size();
		for (int i = 1; i <= length; i++) {
			if(i == length || cpostags.get(i).endsWith("#b"))
			{
				curendboundary = i-1;
				for(int idx = curstartboundary; idx <= curendboundary; idx++)
				{
					wordboundarystarts.add(curstartboundary);
					wordboundaryends.add(curendboundary);
				}
				curstartboundary = i;
				curendboundary = -1;
			}
		}
	}

	
	public boolean segmentedCorrectStructureWrong(CharDependency other, 
			Map<String, Map<String, Integer>> analysisResult){
		
		analysisResult.clear();
		
		

		Map<String, Set<String>>  goldwordstructuresbyattributes = new HashMap<String, Set<String>>();
		
		
		Map<String, String> analysisResultg = new HashMap<String, String>();
		
		int length = forms.size();
		if(other.forms.size() != length)return false;
		for (int i = 0; i < length; i++) {
			if(!deprels.get(i).endsWith("#in"))
			{
				String curWord = String.format("[%d,%d]", wordboundarystarts.get(i), wordboundaryends.get(i));
				String curWordName = "";
				for(int idy = wordboundarystarts.get(i); idy <= wordboundaryends.get(i); idy++)
				{
					curWordName = curWordName + forms.get(idy);
				}
				int headseq = heads.get(i)-1;
				String headWord = "ROOT";
				if(headseq >= 0 && headseq < length)
				{
					headWord = String.format("[%d,%d]", wordboundarystarts.get(headseq), wordboundaryends.get(headseq));
				}
				
				String curPOS = cpostags.get(i).substring(0, cpostags.get(i).length()-2);
				String curwordstructure = wordstructurewithpos(i);
				curwordstructure = curwordstructure.trim();
				String curwordpos = curWord + "_" + curPOS;
				curwordpos = curwordpos.trim();
				{
					String theWSTmpKey = curPOS + "|" + curWordName;
					analysisResultg.put(theWSTmpKey, curwordstructure);
					if(!analysisResult.containsKey(theWSTmpKey))
					{
						analysisResult.put(theWSTmpKey, new HashMap<String, Integer>());
					}
					if(!analysisResult.get(theWSTmpKey).containsKey(curwordstructure+"[g]"))
					{
						analysisResult.get(theWSTmpKey).put(curwordstructure+"[g]", 1);
					}
					//analysisResult.get(theWSTmpKey).put(curwordstructure, analysisResult.get(theWSTmpKey).get(curwordstructure)+1);
				}
				
				{
					String theTAGTmpKey = "TAG=ALL";
					if(!goldwordstructuresbyattributes.containsKey(theTAGTmpKey))
					{
						goldwordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
					}
					goldwordstructuresbyattributes.get(theTAGTmpKey).add(curwordpos);
				}
			}
		}
		
		
		
		
		for (int i = 0; i < length; i++) {
			if(!other.deprels.get(i).endsWith("#in"))
			{
				String curWord = String.format("[%d,%d]", other.wordboundarystarts.get(i), other.wordboundaryends.get(i));
				String curWordName = "";
				for(int idy = other.wordboundarystarts.get(i); idy <= other.wordboundaryends.get(i); idy++)
				{
					curWordName = curWordName + other.forms.get(idy);
				}
				int headseq = other.heads.get(i)-1;
				String headWord = "ROOT";
				if(headseq >= 0 && headseq < length)
				{
					headWord = String.format("[%d,%d]", other.wordboundarystarts.get(headseq), other.wordboundaryends.get(headseq));
				}
				String curPOS = other.cpostags.get(i).substring(0, other.cpostags.get(i).length()-2);
				String curwordstructure = other.wordstructurewithpos(i);
				curwordstructure = curwordstructure.trim();
				String curwordpos = curWord + "_" + curPOS;
				curwordpos = curwordpos.trim();
				if(goldwordstructuresbyattributes.get("TAG=ALL").contains(curwordpos))
				{
					String theWSTmpKey = curPOS + "|" + curWordName;
					String goldMark = "";
					if(analysisResultg.get(theWSTmpKey).equals(curwordstructure))
					{
						goldMark = "[g]";
					}
					
					if(!analysisResult.get(theWSTmpKey).containsKey(curwordstructure + goldMark))
					{
						analysisResult.get(theWSTmpKey).put(curwordstructure + goldMark, 0);
					}
					analysisResult.get(theWSTmpKey).put(curwordstructure + goldMark, analysisResult.get(theWSTmpKey).get(curwordstructure + goldMark)+1);

				}			
			}
		}
						
		return true;			
	}

	
	public boolean evaluateWithOther(CharDependency other) {

		// for evaluate
		// 0 cur_sent_word_num;  1 other_sent_word_num;
		// 2 out_uas_correct_num; 3 out_las_correct_num;  
		// 4 root_correct; (above the word); 5 gold_dep_num; 6 pred_dep_num;
		// 7 in_uas_correct_num; 8 gold_in_uas_num; 9 pred_in_uas_num
		// 10 word_structure_correct; 11 word_correct;  12 pos_correct; 
		// 13 out_uarc_correct_num; 14 out_larc_correct_num;
		
		for (int i = 0; i < eval_res.length; i++) {
			eval_res[i] = 0;
		}
		Set<String> goldwords = new TreeSet<String>();
		Set<String> goldwordposs = new TreeSet<String>();
		Set<String> goldwordstructures = new TreeSet<String>();
		Set<String> goldoutdeps = new TreeSet<String>();
		Set<String> goldoutdeplabels = new TreeSet<String>();
		Set<String> goldindeps = new TreeSet<String>();
		Set<String> goldoutarcs = new TreeSet<String>();
		Set<String> goldoutarclabels = new TreeSet<String>();
		
		Set<String> predwords = new TreeSet<String>();
		Set<String> predwordposs = new TreeSet<String>();
		Set<String> predwordstructures = new TreeSet<String>();
		Set<String> predoutdeps = new TreeSet<String>();
		Set<String> predoutdeplabels = new TreeSet<String>();
		Set<String> predindeps = new TreeSet<String>();
		Set<String> predoutarcs = new TreeSet<String>();
		Set<String> predoutarclabels = new TreeSet<String>();
		
		int length = forms.size();
		if(other.forms.size() != length)return false;
		for (int i = 0; i < length; i++) {
			if(!deprels.get(i).endsWith("#in"))
			{
				String curWord = String.format("[%d,%d]", wordboundarystarts.get(i), wordboundaryends.get(i));
				int headseq = heads.get(i)-1;
				String headWord = "ROOT";
				if(headseq >= 0 && headseq < length)
					headWord = String.format("[%d,%d]", wordboundarystarts.get(headseq), wordboundaryends.get(headseq));
				String curPOS = cpostags.get(i).substring(0, cpostags.get(i).length()-2);
				goldwords.add(curWord);
				goldwordposs.add(curWord + "_" + curPOS);
				//goldwordstructures.add(curWord + "_" + curPOS+"|"+wordstructure(i));
				goldwordstructures.add(curWord +"|"+wordstructure(i));
				if (!isPunc(curPOS)) 
				{
					goldoutdeps.add(String.format("%s<==%s", curWord, headWord));
					goldoutdeplabels.add(String.format("%s<==%s[%s]", curWord, headWord, deprels.get(i)));
				
					if(heads.get(i) != 0)
					{
						goldoutarcs.add(String.format("[%d,%d]", wordboundarystarts.get(i), wordboundarystarts.get(heads.get(i)-1)));
						goldoutarclabels.add(String.format("%s[%d,%d]", deprels.get(i), wordboundarystarts.get(i), wordboundarystarts.get(heads.get(i)-1)));
					}
					else
					{
						goldoutarcs.add(String.format("[%d,%d]", wordboundarystarts.get(i), -1));
						goldoutarclabels.add(String.format("%s[%d,%d]", deprels.get(i), wordboundarystarts.get(i), -1));
					}
				}

			}
			else
			{
				goldindeps.add(String.format("%d<==%d", i, heads.get(i)-1));
			}
		}
		
		
		for (int i = 0; i < length; i++) {
			if(!other.deprels.get(i).endsWith("#in"))
			{
				String curWord = String.format("[%d,%d]", other.wordboundarystarts.get(i), other.wordboundaryends.get(i));
				int headseq = other.heads.get(i)-1;
				String headWord = "ROOT";
				if(headseq >= 0 && headseq < length)
					headWord = String.format("[%d,%d]", other.wordboundarystarts.get(headseq), other.wordboundaryends.get(headseq));
				String curPOS = other.cpostags.get(i).substring(0, other.cpostags.get(i).length()-2);
				predwords.add(curWord);
				predwordposs.add(curWord + "_" + curPOS);
				//predwordstructures.add(curWord + "_" + curPOS+"|"+other.wordstructure(i));
				predwordstructures.add(curWord + "|"+other.wordstructure(i));
				if (!isPunc(curPOS)) 
				{
					predoutdeps.add(String.format("%s<==%s", curWord, headWord));
					predoutdeplabels.add(String.format("%s<==%s[%s]", curWord, headWord, other.deprels.get(i)));
				
					if(other.heads.get(i) != 0)
					{
						predoutarcs.add(String.format("[%d,%d]", other.wordboundarystarts.get(i), other.wordboundarystarts.get(other.heads.get(i)-1)));
						predoutarclabels.add(String.format("%s[%d,%d]", other.deprels.get(i), other.wordboundarystarts.get(i), other.wordboundarystarts.get(other.heads.get(i)-1)));
					}
					else
					{
						predoutarcs.add(String.format("[%d,%d]", other.wordboundarystarts.get(i), -1));
						predoutarclabels.add(String.format("%s[%d,%d]", other.deprels.get(i), other.wordboundarystarts.get(i), -1));
					}
				}
			}
			else
			{
				predindeps.add(String.format("%d<==%d", i, other.heads.get(i)-1));
			}
		}
		
		eval_res[0] = goldwords.size();
		eval_res[1] = predwords.size();
		for(String keystr : goldwords)
		{
			if(predwords.contains(keystr))
			{
				eval_res[11]++;
			}
		}
		
		for(String keystr : goldwordposs)
		{
			if(predwordposs.contains(keystr))
			{
				eval_res[12]++;
			}
		}
		
		for(String keystr : goldwordstructures)
		{
			if(predwordstructures.contains(keystr))
			{
				eval_res[10]++;
			}
		}
		
		eval_res[8] = goldindeps.size();
		eval_res[9] = predindeps.size();
		for(String keystr : goldindeps)
		{
			if(predindeps.contains(keystr))
			{
				eval_res[7]++;
			}
		}
		
		eval_res[5] = goldoutdeps.size();
		eval_res[6] = predoutdeps.size();
		
		for(String keystr : goldoutdeps)
		{
			if(predoutdeps.contains(keystr))
			{
				eval_res[2]++;
				if(keystr.endsWith("ROOT"))
				{
					eval_res[4]++;
				}
			}
		}
		
		for(String keystr : goldoutdeplabels)
		{
			if(predoutdeplabels.contains(keystr))
			{
				eval_res[3]++;
			}
		}
		
		for(String keystr : goldoutarcs)
		{
			if(predoutarcs.contains(keystr))
			{
				eval_res[13]++;
			}
		}
		
		for(String keystr : goldoutarclabels)
		{
			if(predoutarclabels.contains(keystr))
			{
				eval_res[14]++;
			}
		}
		
		

		
		return true;
	}
	
	public boolean McTestData(CharDependency other1, CharDependency other2,Map<String, Map<String, Integer>> analysisResult)
	{
		analysisResult.clear();
		
		

		Map<String, Set<String>>  goldwordstructuresbyattributes = new HashMap<String, Set<String>>();
		
		Set<String> analysisedKey = new HashSet<String>();
		
		int length = forms.size();
		if(other1.forms.size() != length || other2.forms.size() != length)return false;
		
		for (int i = 0; i < length; i++) {
			if(!deprels.get(i).endsWith("#in"))
			{
				String curWord = String.format("[%d,%d]", wordboundarystarts.get(i), wordboundaryends.get(i));
				String curWordName = "";
				for(int idy = wordboundarystarts.get(i); idy <= wordboundaryends.get(i); idy++)
				{
					curWordName = curWordName + forms.get(idy);
				}
				int headseq = heads.get(i)-1;
				String headWord = "ROOT";
				if(headseq >= 0 && headseq < length)
				{
					headWord = String.format("[%d,%d]", wordboundarystarts.get(headseq), wordboundaryends.get(headseq));
				}
				
				String curPOS = cpostags.get(i).substring(0, cpostags.get(i).length()-2);

				//if(curWordName.length() >= 2)
				{	
					
					String curword = curWord;
					curword = curword.trim();				
					//SEG analysis					
					{
						String theTAGTmpKey = "SEG=ALL";
						analysisedKey.add(theTAGTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theTAGTmpKey))
						{
							goldwordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theTAGTmpKey).add(curword);
					}
					
					
					String curwordpos = curWord + "_" + curPOS;
					
					curwordpos = curwordpos.trim();
					//TAG analysis					
					{
						String theTAGTmpKey = "TAG=ALL";
						analysisedKey.add(theTAGTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theTAGTmpKey))
						{
							goldwordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theTAGTmpKey).add(curwordpos);
					}
										
					//word structure analysis
					String curwordstructure = curWord +"|"+wordstructure(i);
					curwordstructure = curwordstructure.trim();
					
					{
						String theWSTmpKey = "WS=ALL";
						analysisedKey.add(theWSTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theWSTmpKey))
						{
							goldwordstructuresbyattributes.put(theWSTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theWSTmpKey).add(curwordstructure);
					}
				}
				
				
				if (!isPunc(curPOS)) 
				{
					String curworddep = String.format("%s<==%s", curWord, headWord);
					curworddep = curworddep.trim();
					
					{
						String theDEPTmpKey = "DEP=ALL";
						analysisedKey.add(theDEPTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theDEPTmpKey))
						{
							goldwordstructuresbyattributes.put(theDEPTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theDEPTmpKey).add(curworddep);
					}
				}

			
			}

		}
		
		Map<String, Set<String>>  pred1wordstructuresbyattributes = new HashMap<String, Set<String>>();
				
		for (int i = 0; i < length; i++) {
			if(!other1.deprels.get(i).endsWith("#in"))
			{
				String curWord = String.format("[%d,%d]", other1.wordboundarystarts.get(i), other1.wordboundaryends.get(i));
				String curWordName = "";
				for(int idy = other1.wordboundarystarts.get(i); idy <= other1.wordboundaryends.get(i); idy++)
				{
					curWordName = curWordName + other1.forms.get(idy);
				}
				int headseq = other1.heads.get(i)-1;
				String headWord = "ROOT";
				if(headseq >= 0 && headseq < length)
				{
					headWord = String.format("[%d,%d]", other1.wordboundarystarts.get(headseq), other1.wordboundaryends.get(headseq));
				}
				String curPOS = other1.cpostags.get(i).substring(0, other1.cpostags.get(i).length()-2);
				{
					String curword = curWord;
					curword = curword.trim();					
					{
						String theTAGTmpKey = "SEG=ALL";
						analysisedKey.add(theTAGTmpKey);
						if(!pred1wordstructuresbyattributes.containsKey(theTAGTmpKey))
						{
							pred1wordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
						}
						pred1wordstructuresbyattributes.get(theTAGTmpKey).add(curword);
					}
					
					String curwordpos = curWord + "_" + curPOS;
					curwordpos = curwordpos.trim();					
					{
						String theTAGTmpKey = "TAG=ALL";
						analysisedKey.add(theTAGTmpKey);
						if(!pred1wordstructuresbyattributes.containsKey(theTAGTmpKey))
						{
							pred1wordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
						}
						pred1wordstructuresbyattributes.get(theTAGTmpKey).add(curwordpos);
					}
					
					
					String curwordstructure = curWord+"|"+other1.wordstructure(i);
					curwordstructure = curwordstructure.trim();
					
					{
						String theWSTmpKey = "WS=ALL";
						analysisedKey.add(theWSTmpKey);
						if(!pred1wordstructuresbyattributes.containsKey(theWSTmpKey))
						{
							pred1wordstructuresbyattributes.put(theWSTmpKey, new HashSet<String>());
						}
						pred1wordstructuresbyattributes.get(theWSTmpKey).add(curwordstructure);
					}
				}
				
				String goldPOS = cpostags.get(i).substring(0, cpostags.get(i).length()-2);
				//dep analysis
				if (!isPunc(goldPOS)) 
				{
					String curworddep = String.format("%s<==%s", curWord, headWord);
					curworddep = curworddep.trim();
					
					{
						String theDEPTmpKey = "DEP=ALL";
						analysisedKey.add(theDEPTmpKey);
						if(!pred1wordstructuresbyattributes.containsKey(theDEPTmpKey))
						{
							pred1wordstructuresbyattributes.put(theDEPTmpKey, new HashSet<String>());
						}
						pred1wordstructuresbyattributes.get(theDEPTmpKey).add(curworddep);
					}
				}

			}			
		}
		
		
		Map<String, Set<String>>  pred2wordstructuresbyattributes = new HashMap<String, Set<String>>();
		
		for (int i = 0; i < length; i++) {
			if(!other2.deprels.get(i).endsWith("#in"))
			{
				String curWord = String.format("[%d,%d]", other2.wordboundarystarts.get(i), other2.wordboundaryends.get(i));
				String curWordName = "";
				for(int idy = other2.wordboundarystarts.get(i); idy <= other2.wordboundaryends.get(i); idy++)
				{
					curWordName = curWordName + other2.forms.get(idy);
				}
				int headseq = other2.heads.get(i)-1;
				String headWord = "ROOT";
				if(headseq >= 0 && headseq < length)
				{
					headWord = String.format("[%d,%d]", other2.wordboundarystarts.get(headseq), other2.wordboundaryends.get(headseq));
				}
				String curPOS = other2.cpostags.get(i).substring(0, other2.cpostags.get(i).length()-2);
				{
					String curword = curWord;
					curword = curword.trim();					
					{
						String theTAGTmpKey = "SEG=ALL";
						analysisedKey.add(theTAGTmpKey);
						if(!pred2wordstructuresbyattributes.containsKey(theTAGTmpKey))
						{
							pred2wordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
						}
						pred2wordstructuresbyattributes.get(theTAGTmpKey).add(curword);
					}
					
					String curwordpos = curWord + "_" + curPOS;
					curwordpos = curwordpos.trim();					
					{
						String theTAGTmpKey = "TAG=ALL";
						analysisedKey.add(theTAGTmpKey);
						if(!pred2wordstructuresbyattributes.containsKey(theTAGTmpKey))
						{
							pred2wordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
						}
						pred2wordstructuresbyattributes.get(theTAGTmpKey).add(curwordpos);
					}
					
					
					String curwordstructure = curWord+"|"+other2.wordstructure(i);
					curwordstructure = curwordstructure.trim();
					
					{
						String theWSTmpKey = "WS=ALL";
						analysisedKey.add(theWSTmpKey);
						if(!pred2wordstructuresbyattributes.containsKey(theWSTmpKey))
						{
							pred2wordstructuresbyattributes.put(theWSTmpKey, new HashSet<String>());
						}
						pred2wordstructuresbyattributes.get(theWSTmpKey).add(curwordstructure);
					}
				}
				
				String goldPOS = cpostags.get(i).substring(0, cpostags.get(i).length()-2);
				//dep analysis
				if (!isPunc(goldPOS)) 
				{
					String curworddep = String.format("%s<==%s", curWord, headWord);
					curworddep = curworddep.trim();
					
					{
						String theDEPTmpKey = "DEP=ALL";
						analysisedKey.add(theDEPTmpKey);
						if(!pred2wordstructuresbyattributes.containsKey(theDEPTmpKey))
						{
							pred2wordstructuresbyattributes.put(theDEPTmpKey, new HashSet<String>());
						}
						pred2wordstructuresbyattributes.get(theDEPTmpKey).add(curworddep);
					}
				}

			}			
		}
		
		
		for(String theAnsKey : analysisedKey)
		{
			analysisResult.put(theAnsKey, new HashMap<String, Integer>());
			analysisResult.get(theAnsKey).put("1Y2Y", 0);
			analysisResult.get(theAnsKey).put("1N2N", 0);
			analysisResult.get(theAnsKey).put("1Y2N", 0);
			analysisResult.get(theAnsKey).put("1N2Y", 0);
			
			if(pred1wordstructuresbyattributes.containsKey(theAnsKey)
			  && pred2wordstructuresbyattributes.containsKey(theAnsKey)
			  && goldwordstructuresbyattributes.containsKey(theAnsKey))
			{
				for(String theKeyVaules : goldwordstructuresbyattributes.get(theAnsKey))
				{
					if(pred1wordstructuresbyattributes.get(theAnsKey).contains(theKeyVaules)
						&& pred2wordstructuresbyattributes.get(theAnsKey).contains(theKeyVaules))
					{
						analysisResult.get(theAnsKey).put("1Y2Y", analysisResult.get(theAnsKey).get("1Y2Y")+1);
					}
					else if(pred1wordstructuresbyattributes.get(theAnsKey).contains(theKeyVaules)
							&& !pred2wordstructuresbyattributes.get(theAnsKey).contains(theKeyVaules))
					{
						analysisResult.get(theAnsKey).put("1Y2N", analysisResult.get(theAnsKey).get("1Y2N")+1);
					}
					else if(!pred1wordstructuresbyattributes.get(theAnsKey).contains(theKeyVaules)
							&& pred2wordstructuresbyattributes.get(theAnsKey).contains(theKeyVaules))
					{
						analysisResult.get(theAnsKey).put("1N2Y", analysisResult.get(theAnsKey).get("1N2Y")+1);
					}
					else if(!pred1wordstructuresbyattributes.get(theAnsKey).contains(theKeyVaules)
							&& !pred2wordstructuresbyattributes.get(theAnsKey).contains(theKeyVaules))
					{
						analysisResult.get(theAnsKey).put("1N2N", analysisResult.get(theAnsKey).get("1N2N")+1);
					}
					else
					{
						System.out.println("impossible!");
					}
						
				}
			}

		}
				
		
		
		return true;
	}
	

	
	public boolean TTestDataBySent(CharDependency other1, CharDependency other2,Map<String, List<Double>> analysisResult)
	{
		analysisResult.clear();
		
		

		Map<String, Set<String>>  goldwordstructuresbyattributes = new HashMap<String, Set<String>>();
		
		Set<String> analysisedKey = new HashSet<String>();
		
		int length = forms.size();
		if(other1.forms.size() != length || other2.forms.size() != length)return false;
		
		for (int i = 0; i < length; i++) {
			if(!deprels.get(i).endsWith("#in"))
			{
				String curWord = String.format("[%d,%d]", wordboundarystarts.get(i), wordboundaryends.get(i));
				String curWordName = "";
				for(int idy = wordboundarystarts.get(i); idy <= wordboundaryends.get(i); idy++)
				{
					curWordName = curWordName + forms.get(idy);
				}
				int headseq = heads.get(i)-1;
				String headWord = "ROOT";
				if(headseq >= 0 && headseq < length)
				{
					headWord = String.format("[%d,%d]", wordboundarystarts.get(headseq), wordboundaryends.get(headseq));
				}
				
				String curPOS = cpostags.get(i).substring(0, cpostags.get(i).length()-2);

				//if(curWordName.length() >= 2)
				{	
					
					String curword = curWord;
					curword = curword.trim();				
					//SEG analysis					
					{
						String theTAGTmpKey = "SEG=ALL";
						analysisedKey.add(theTAGTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theTAGTmpKey))
						{
							goldwordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theTAGTmpKey).add(curword);
					}
					
					
					String curwordpos = curWord + "_" + curPOS;
					
					curwordpos = curwordpos.trim();
					//TAG analysis					
					{
						String theTAGTmpKey = "TAG=ALL";
						analysisedKey.add(theTAGTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theTAGTmpKey))
						{
							goldwordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theTAGTmpKey).add(curwordpos);
					}
										
					//word structure analysis
					String curwordstructure = curWord +"|"+wordstructure(i);
					curwordstructure = curwordstructure.trim();
					
					{
						String theWSTmpKey = "WS=ALL";
						analysisedKey.add(theWSTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theWSTmpKey))
						{
							goldwordstructuresbyattributes.put(theWSTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theWSTmpKey).add(curwordstructure);
					}
				}
				
				
				if (!isPunc(curPOS)) 
				{
					String curworddep = String.format("%s<==%s", curWord, headWord);
					curworddep = curworddep.trim();
					
					{
						String theDEPTmpKey = "DEP=ALL";
						analysisedKey.add(theDEPTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theDEPTmpKey))
						{
							goldwordstructuresbyattributes.put(theDEPTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theDEPTmpKey).add(curworddep);
					}
				}

			
			}

		}
		
		Map<String, Set<String>>  pred1wordstructuresbyattributes = new HashMap<String, Set<String>>();
				
		for (int i = 0; i < length; i++) {
			if(!other1.deprels.get(i).endsWith("#in"))
			{
				String curWord = String.format("[%d,%d]", other1.wordboundarystarts.get(i), other1.wordboundaryends.get(i));
				String curWordName = "";
				for(int idy = other1.wordboundarystarts.get(i); idy <= other1.wordboundaryends.get(i); idy++)
				{
					curWordName = curWordName + other1.forms.get(idy);
				}
				int headseq = other1.heads.get(i)-1;
				String headWord = "ROOT";
				if(headseq >= 0 && headseq < length)
				{
					headWord = String.format("[%d,%d]", other1.wordboundarystarts.get(headseq), other1.wordboundaryends.get(headseq));
				}
				String curPOS = other1.cpostags.get(i).substring(0, other1.cpostags.get(i).length()-2);
				{
					String curword = curWord;
					curword = curword.trim();					
					{
						String theTAGTmpKey = "SEG=ALL";
						analysisedKey.add(theTAGTmpKey);
						if(!pred1wordstructuresbyattributes.containsKey(theTAGTmpKey))
						{
							pred1wordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
						}
						pred1wordstructuresbyattributes.get(theTAGTmpKey).add(curword);
					}
					
					String curwordpos = curWord + "_" + curPOS;
					curwordpos = curwordpos.trim();					
					{
						String theTAGTmpKey = "TAG=ALL";
						analysisedKey.add(theTAGTmpKey);
						if(!pred1wordstructuresbyattributes.containsKey(theTAGTmpKey))
						{
							pred1wordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
						}
						pred1wordstructuresbyattributes.get(theTAGTmpKey).add(curwordpos);
					}
					
					
					String curwordstructure = curWord+"|"+other1.wordstructure(i);
					curwordstructure = curwordstructure.trim();
					
					{
						String theWSTmpKey = "WS=ALL";
						analysisedKey.add(theWSTmpKey);
						if(!pred1wordstructuresbyattributes.containsKey(theWSTmpKey))
						{
							pred1wordstructuresbyattributes.put(theWSTmpKey, new HashSet<String>());
						}
						pred1wordstructuresbyattributes.get(theWSTmpKey).add(curwordstructure);
					}
				}
				
				String goldPOS = cpostags.get(i).substring(0, cpostags.get(i).length()-2);
				//dep analysis
				if (!isPunc(goldPOS)) 
				{
					String curworddep = String.format("%s<==%s", curWord, headWord);
					curworddep = curworddep.trim();
					
					{
						String theDEPTmpKey = "DEP=ALL";
						analysisedKey.add(theDEPTmpKey);
						if(!pred1wordstructuresbyattributes.containsKey(theDEPTmpKey))
						{
							pred1wordstructuresbyattributes.put(theDEPTmpKey, new HashSet<String>());
						}
						pred1wordstructuresbyattributes.get(theDEPTmpKey).add(curworddep);
					}
				}

			}			
		}
		
		
		Map<String, Set<String>>  pred2wordstructuresbyattributes = new HashMap<String, Set<String>>();
		
		for (int i = 0; i < length; i++) {
			if(!other2.deprels.get(i).endsWith("#in"))
			{
				String curWord = String.format("[%d,%d]", other2.wordboundarystarts.get(i), other2.wordboundaryends.get(i));
				String curWordName = "";
				for(int idy = other2.wordboundarystarts.get(i); idy <= other2.wordboundaryends.get(i); idy++)
				{
					curWordName = curWordName + other2.forms.get(idy);
				}
				int headseq = other2.heads.get(i)-1;
				String headWord = "ROOT";
				if(headseq >= 0 && headseq < length)
				{
					headWord = String.format("[%d,%d]", other2.wordboundarystarts.get(headseq), other2.wordboundaryends.get(headseq));
				}
				String curPOS = other2.cpostags.get(i).substring(0, other2.cpostags.get(i).length()-2);
				{
					String curword = curWord;
					curword = curword.trim();					
					{
						String theTAGTmpKey = "SEG=ALL";
						analysisedKey.add(theTAGTmpKey);
						if(!pred2wordstructuresbyattributes.containsKey(theTAGTmpKey))
						{
							pred2wordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
						}
						pred2wordstructuresbyattributes.get(theTAGTmpKey).add(curword);
					}
					
					String curwordpos = curWord + "_" + curPOS;
					curwordpos = curwordpos.trim();					
					{
						String theTAGTmpKey = "TAG=ALL";
						analysisedKey.add(theTAGTmpKey);
						if(!pred2wordstructuresbyattributes.containsKey(theTAGTmpKey))
						{
							pred2wordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
						}
						pred2wordstructuresbyattributes.get(theTAGTmpKey).add(curwordpos);
					}
					
					
					String curwordstructure = curWord+"|"+other2.wordstructure(i);
					curwordstructure = curwordstructure.trim();
					
					{
						String theWSTmpKey = "WS=ALL";
						analysisedKey.add(theWSTmpKey);
						if(!pred2wordstructuresbyattributes.containsKey(theWSTmpKey))
						{
							pred2wordstructuresbyattributes.put(theWSTmpKey, new HashSet<String>());
						}
						pred2wordstructuresbyattributes.get(theWSTmpKey).add(curwordstructure);
					}
				}
				
				String goldPOS = cpostags.get(i).substring(0, cpostags.get(i).length()-2);
				//dep analysis
				if (!isPunc(goldPOS)) 
				{
					String curworddep = String.format("%s<==%s", curWord, headWord);
					curworddep = curworddep.trim();
					
					{
						String theDEPTmpKey = "DEP=ALL";
						analysisedKey.add(theDEPTmpKey);
						if(!pred2wordstructuresbyattributes.containsKey(theDEPTmpKey))
						{
							pred2wordstructuresbyattributes.put(theDEPTmpKey, new HashSet<String>());
						}
						pred2wordstructuresbyattributes.get(theDEPTmpKey).add(curworddep);
					}
				}

			}			
		}
		
		
		for(String theAnsKey : analysisedKey)
		{
			analysisResult.put(theAnsKey+ "=1F", new ArrayList<Double>());
			analysisResult.put(theAnsKey+ "=2F", new ArrayList<Double>());

			
			if(pred1wordstructuresbyattributes.containsKey(theAnsKey)
			  && pred2wordstructuresbyattributes.containsKey(theAnsKey)
			  && goldwordstructuresbyattributes.containsKey(theAnsKey))
			{
				int correct1Num = 0;
				int correct2Num = 0;
				for(String theKeyVaules : goldwordstructuresbyattributes.get(theAnsKey))
				{
					if(pred1wordstructuresbyattributes.get(theAnsKey).contains(theKeyVaules))
					{						
						correct1Num++;
					}
					
					if(pred2wordstructuresbyattributes.get(theAnsKey).contains(theKeyVaules))
					{
						
						correct2Num++;
					}
						
				}
				for(int idx = 0 ;idx < correct1Num; idx++)
				{
					analysisResult.get(theAnsKey+ "=1F").add(1.0);
					analysisResult.get(theAnsKey+ "=1F").add(1.0);
				}				
				for(int idx = 0; idx < goldwordstructuresbyattributes.get(theAnsKey).size()-correct1Num; idx++)
				{
					analysisResult.get(theAnsKey+ "=1F").add(0.0);
				}
				for(int idx = 0; idx < pred1wordstructuresbyattributes.get(theAnsKey).size()-correct1Num; idx++)
				{
					analysisResult.get(theAnsKey+ "=1F").add(0.0);
				}
				
				
				for(int idx = 0 ;idx < correct2Num; idx++)
				{
					analysisResult.get(theAnsKey+ "=2F").add(1.0);
					analysisResult.get(theAnsKey+ "=2F").add(1.0);
				}
				
				for(int idx = 0; idx < goldwordstructuresbyattributes.get(theAnsKey).size()-correct2Num; idx++)
				{
					analysisResult.get(theAnsKey+ "=2F").add(0.0);
				}
				for(int idx = 0; idx < pred2wordstructuresbyattributes.get(theAnsKey).size()-correct2Num; idx++)
				{
					analysisResult.get(theAnsKey+ "=2F").add(0.0);
				}				
							
			}
						
		}
				
		
		
		return true;
	}

	public boolean innerWordDependencyAnalysis(CharDependency other, Set<String> inDict,
			Map<String, Map<String, Integer>> analysisResult, int sentId)
	{
		//wordlegnth: LEN#I#GOLD#TOTAL   LEN#I#PRED#TOTAL, LEN#I#CORRECT
		//ivoov:      OOV#GOLD#TOTAL   OOV#PRED#TOTAL, OOV#CORRECT
		//ivoov:      IV#GOLD#TOTAL   IV#PRED#TOTAL, IV#CORRECT
		//POS:      POS=[..]#GOLD#TOTAL    POS=[..]#PRED#TOTAL,  POS=[..]#CORRECT
		analysisResult.clear();
		
		

		Map<String, Set<String>>  goldwordstructuresbyattributes = new HashMap<String, Set<String>>();
		
		Set<String> analysisedKey = new HashSet<String>();
		
		int length = forms.size();
		if(other.forms.size() != length)return false;
		for (int i = 0; i < length; i++) {
			if(!deprels.get(i).endsWith("#in"))
			{
				String curWord = String.format("[%d,%d]", wordboundarystarts.get(i), wordboundaryends.get(i));
				String curWordName = "";
				for(int idy = wordboundarystarts.get(i); idy <= wordboundaryends.get(i); idy++)
				{
					curWordName = curWordName + forms.get(idy);
				}
				int headseq = heads.get(i)-1;
				String headWord = "ROOT";
				if(headseq >= 0 && headseq < length)
				{
					headWord = String.format("[%d,%d]", wordboundarystarts.get(headseq), wordboundaryends.get(headseq));
				}
				
				String curPOS = cpostags.get(i).substring(0, cpostags.get(i).length()-2);

				//if(curWordName.length() >= 2)
				{				
					//String curwordpos = curWord + "_" + curPOS;
					String curwordpos = curWord;
					curwordpos = curwordpos.trim();
					//TAG analysis
					if(!inDict.contains(curWordName))
					{
						String theTAGTmpKey = "TAG=DICT=OOV";
						analysisedKey.add(theTAGTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theTAGTmpKey))
						{
							goldwordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theTAGTmpKey).add(curwordpos);
					}
					else
					{
						String theTAGTmpKey = "TAG=DICT=IV";
						analysisedKey.add(theTAGTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theTAGTmpKey))
						{
							goldwordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theTAGTmpKey).add(curwordpos);
					}
					
					{
						int normalizedLen = curWordName.length() > 6 ? 6 : curWordName.length();
						String theTAGTmpKey = String.format("TAG=LEN=%d", normalizedLen);
						analysisedKey.add(theTAGTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theTAGTmpKey))
						{
							goldwordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theTAGTmpKey).add(curwordpos);
					}
					
					
					{
						String theTAGTmpKey = String.format("TAG=POS=[%s]", curPOS);
						analysisedKey.add(theTAGTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theTAGTmpKey))
						{
							goldwordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theTAGTmpKey).add(curwordpos);
					}
					
					{
						String theTAGTmpKey = String.format("TAG=ST=[%d]", sentId);
						analysisedKey.add(theTAGTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theTAGTmpKey))
						{
							goldwordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theTAGTmpKey).add(curwordpos);
					}
					
					{
						String theTAGTmpKey = "TAG=ALL";
						analysisedKey.add(theTAGTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theTAGTmpKey))
						{
							goldwordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theTAGTmpKey).add(curwordpos);
					}
					
					
					
					//word structure analysis
					//String curwordstructure = curWord + "_" + curPOS+"|"+wordstructure(i);
					String curwordstructure = curWord +"|"+wordstructure(i);
					curwordstructure = curwordstructure.trim();
					if(!inDict.contains(curWordName))
					{
						String theWSTmpKey = "WS=DICT=OOV";
						analysisedKey.add(theWSTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theWSTmpKey))
						{
							goldwordstructuresbyattributes.put(theWSTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theWSTmpKey).add(curwordstructure);
					}
					else
					{
						String theWSTmpKey = "WS=DICT=IV";
						analysisedKey.add(theWSTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theWSTmpKey))
						{
							goldwordstructuresbyattributes.put(theWSTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theWSTmpKey).add(curwordstructure);
					}
					
					{
						int normalizedLen = curWordName.length() > 6 ? 6 : curWordName.length();
						String theWSTmpKey = String.format("WS=LEN=%d", normalizedLen);
						analysisedKey.add(theWSTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theWSTmpKey))
						{
							goldwordstructuresbyattributes.put(theWSTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theWSTmpKey).add(curwordstructure);
					}
					
					
					{
						String theWSTmpKey = String.format("WS=POS=[%s]", curPOS);
						analysisedKey.add(theWSTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theWSTmpKey))
						{
							goldwordstructuresbyattributes.put(theWSTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theWSTmpKey).add(curwordstructure);
					}
					
					{
						String theWSTmpKey = String.format("WS=ST=[%d]", sentId);
						analysisedKey.add(theWSTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theWSTmpKey))
						{
							goldwordstructuresbyattributes.put(theWSTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theWSTmpKey).add(curwordstructure);
					}
					
					{
						String theWSTmpKey = "WS=ALL";
						analysisedKey.add(theWSTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theWSTmpKey))
						{
							goldwordstructuresbyattributes.put(theWSTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theWSTmpKey).add(curwordstructure);
					}
				}
				
				
				if (!isPunc(curPOS)) 
				{
					String curworddep = String.format("%s<==%s", curWord, headWord);
					curworddep = curworddep.trim();
					boolean bDirectionRight = headseq > i ? false : true;
					boolean bRoot = true;
					if(headseq >= 0) bRoot = false;
					if(bDirectionRight && !bRoot)
					{
						String theDEPTmpKey = "DEP=DIR=RIGHT";
						analysisedKey.add(theDEPTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theDEPTmpKey))
						{
							goldwordstructuresbyattributes.put(theDEPTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theDEPTmpKey).add(curworddep);
					}
					else if( !bDirectionRight && !bRoot)
					{
						String theDEPTmpKey = "DEP=DIR=LEFT";
						analysisedKey.add(theDEPTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theDEPTmpKey))
						{
							goldwordstructuresbyattributes.put(theDEPTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theDEPTmpKey).add(curworddep);
					}
					else
					{
						String theDEPTmpKey = "DEP=DIR=ROOT";
						analysisedKey.add(theDEPTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theDEPTmpKey))
						{
							goldwordstructuresbyattributes.put(theDEPTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theDEPTmpKey).add(curworddep);
					}
					
					{
						//int normalizedLen = curWordName.length() > 6 ? 6 : curWordName.length();
						int normalizedLen = 0;
						if(!bRoot)
						{
							if(!bDirectionRight)
							{
								for (int idn = i+1; idn <= headseq; idn++) {
									if(!deprels.get(idn).endsWith("#in"))
									{
										normalizedLen++;
										if(normalizedLen >= 8)break;
									}
								}
							}
							else
							{
								for (int idn = i-1; idn >= headseq; idn--) {
									if(!deprels.get(idn).endsWith("#in"))
									{
										normalizedLen++;
										if(normalizedLen >= 8)break;
									}
								}
							}
						}
						String theDEPTmpKey = String.format("DEP=LEN=%d", normalizedLen);
						analysisedKey.add(theDEPTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theDEPTmpKey))
						{
							goldwordstructuresbyattributes.put(theDEPTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theDEPTmpKey).add(curworddep);
					}
					
					
					{
						String theDEPTmpKey = String.format("DEP=POS=[%s]", curPOS);
						analysisedKey.add(theDEPTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theDEPTmpKey))
						{
							goldwordstructuresbyattributes.put(theDEPTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theDEPTmpKey).add(curworddep);
					}
					
					{
						String theDEPTmpKey = String.format("DEP=ST=[%d]", sentId);
						analysisedKey.add(theDEPTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theDEPTmpKey))
						{
							goldwordstructuresbyattributes.put(theDEPTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theDEPTmpKey).add(curworddep);
					}
					
					{
						String theDEPTmpKey = "DEP=ALL";
						analysisedKey.add(theDEPTmpKey);
						if(!goldwordstructuresbyattributes.containsKey(theDEPTmpKey))
						{
							goldwordstructuresbyattributes.put(theDEPTmpKey, new HashSet<String>());
						}
						goldwordstructuresbyattributes.get(theDEPTmpKey).add(curworddep);
					}
				}

			
			}
			else
			{
				//goldindeps.add(String.format("%d<==%d", i, heads.get(i)-1));
			}
		}
		
		Map<String, Set<String>>  predwordstructuresbyattributes = new HashMap<String, Set<String>>();
		
		
		for (int i = 0; i < length; i++) {
			if(!other.deprels.get(i).endsWith("#in"))
			{
				String curWord = String.format("[%d,%d]", other.wordboundarystarts.get(i), other.wordboundaryends.get(i));
				String curWordName = "";
				for(int idy = other.wordboundarystarts.get(i); idy <= other.wordboundaryends.get(i); idy++)
				{
					curWordName = curWordName + other.forms.get(idy);
				}
				int headseq = other.heads.get(i)-1;
				String headWord = "ROOT";
				if(headseq >= 0 && headseq < length)
				{
					headWord = String.format("[%d,%d]", other.wordboundarystarts.get(headseq), other.wordboundaryends.get(headseq));
				}
				String curPOS = other.cpostags.get(i).substring(0, other.cpostags.get(i).length()-2);
				//if(curWordName.length() >= 2)
				{
					//String curwordpos = curWord + "_" + curPOS;
					String curwordpos = curWord;
					curwordpos = curwordpos.trim();
					if(!inDict.contains(curWordName))
					{
						String theTAGTmpKey = "TAG=DICT=OOV";
						analysisedKey.add(theTAGTmpKey);
						if(!predwordstructuresbyattributes.containsKey(theTAGTmpKey))
						{
							predwordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
						}
						predwordstructuresbyattributes.get(theTAGTmpKey).add(curwordpos);
					}
					else
					{
						String theTAGTmpKey = "TAG=DICT=IV";
						analysisedKey.add(theTAGTmpKey);
						if(!predwordstructuresbyattributes.containsKey(theTAGTmpKey))
						{
							predwordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
						}
						predwordstructuresbyattributes.get(theTAGTmpKey).add(curwordpos);
					}
					
					{
						int normalizedLen = curWordName.length() > 6 ? 6 : curWordName.length();
						String theTAGTmpKey = String.format("TAG=LEN=%d", normalizedLen);
						analysisedKey.add(theTAGTmpKey);
						if(!predwordstructuresbyattributes.containsKey(theTAGTmpKey))
						{
							predwordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
						}
						predwordstructuresbyattributes.get(theTAGTmpKey).add(curwordpos);
					}
					
					
					{
						String theTAGTmpKey = String.format("TAG=POS=[%s]", curPOS);
						analysisedKey.add(theTAGTmpKey);
						if(!predwordstructuresbyattributes.containsKey(theTAGTmpKey))
						{
							predwordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
						}
						predwordstructuresbyattributes.get(theTAGTmpKey).add(curwordpos);
					}
					
					
					{
						String theTAGTmpKey = String.format("TAG=ST=[%d]", sentId);
						analysisedKey.add(theTAGTmpKey);
						if(!predwordstructuresbyattributes.containsKey(theTAGTmpKey))
						{
							predwordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
						}
						predwordstructuresbyattributes.get(theTAGTmpKey).add(curwordpos);
					}
					
					{
						String theTAGTmpKey = "TAG=ALL";
						analysisedKey.add(theTAGTmpKey);
						if(!predwordstructuresbyattributes.containsKey(theTAGTmpKey))
						{
							predwordstructuresbyattributes.put(theTAGTmpKey, new HashSet<String>());
						}
						predwordstructuresbyattributes.get(theTAGTmpKey).add(curwordpos);
					}
					
					
					//String curwordstructure = curWord + "_" + curPOS+"|"+other.wordstructure(i);
					String curwordstructure = curWord+"|"+other.wordstructure(i);
					curwordstructure = curwordstructure.trim();
					if(!inDict.contains(curWordName))
					{
						String theWSTmpKey = "WS=DICT=OOV";
						analysisedKey.add(theWSTmpKey);
						if(!predwordstructuresbyattributes.containsKey(theWSTmpKey))
						{
							predwordstructuresbyattributes.put(theWSTmpKey, new HashSet<String>());
						}
						predwordstructuresbyattributes.get(theWSTmpKey).add(curwordstructure);
					}
					else
					{
						String theWSTmpKey = "WS=DICT=IV";
						analysisedKey.add(theWSTmpKey);
						if(!predwordstructuresbyattributes.containsKey(theWSTmpKey))
						{
							predwordstructuresbyattributes.put(theWSTmpKey, new HashSet<String>());
						}
						predwordstructuresbyattributes.get(theWSTmpKey).add(curwordstructure);
					}
					
					{
						int normalizedLen = curWordName.length() > 6 ? 6 : curWordName.length();
						String theWSTmpKey = String.format("WS=LEN=%d", normalizedLen);
						analysisedKey.add(theWSTmpKey);
						if(!predwordstructuresbyattributes.containsKey(theWSTmpKey))
						{
							predwordstructuresbyattributes.put(theWSTmpKey, new HashSet<String>());
						}
						predwordstructuresbyattributes.get(theWSTmpKey).add(curwordstructure);
					}
					
					
					{
						String theWSTmpKey = String.format("WS=POS=[%s]", curPOS);
						analysisedKey.add(theWSTmpKey);
						if(!predwordstructuresbyattributes.containsKey(theWSTmpKey))
						{
							predwordstructuresbyattributes.put(theWSTmpKey, new HashSet<String>());
						}
						predwordstructuresbyattributes.get(theWSTmpKey).add(curwordstructure);
					}
					
					{
						String theWSTmpKey = String.format("WS=ST=[%d]", sentId);
						analysisedKey.add(theWSTmpKey);
						if(!predwordstructuresbyattributes.containsKey(theWSTmpKey))
						{
							predwordstructuresbyattributes.put(theWSTmpKey, new HashSet<String>());
						}
						predwordstructuresbyattributes.get(theWSTmpKey).add(curwordstructure);
					}
					
					{
						String theWSTmpKey = "WS=ALL";
						analysisedKey.add(theWSTmpKey);
						if(!predwordstructuresbyattributes.containsKey(theWSTmpKey))
						{
							predwordstructuresbyattributes.put(theWSTmpKey, new HashSet<String>());
						}
						predwordstructuresbyattributes.get(theWSTmpKey).add(curwordstructure);
					}
				}
				
				String goldPOS = cpostags.get(i).substring(0, cpostags.get(i).length()-2);
				//dep analysis
				if (!isPunc(goldPOS)) 
				{
					String curworddep = String.format("%s<==%s", curWord, headWord);
					curworddep = curworddep.trim();
					boolean bDirectionRight = headseq > i ? false : true;
					boolean bRoot = true;
					if(headseq >= 0) bRoot = false;
					if(bDirectionRight && !bRoot)
					{
						String theDEPTmpKey = "DEP=DIR=RIGHT";
						analysisedKey.add(theDEPTmpKey);
						if(!predwordstructuresbyattributes.containsKey(theDEPTmpKey))
						{
							predwordstructuresbyattributes.put(theDEPTmpKey, new HashSet<String>());
						}
						predwordstructuresbyattributes.get(theDEPTmpKey).add(curworddep);
					}
					else if(!bDirectionRight && !bRoot)
					{
						String theDEPTmpKey = "DEP=DIR=LEFT";
						analysisedKey.add(theDEPTmpKey);
						if(!predwordstructuresbyattributes.containsKey(theDEPTmpKey))
						{
							predwordstructuresbyattributes.put(theDEPTmpKey, new HashSet<String>());
						}
						predwordstructuresbyattributes.get(theDEPTmpKey).add(curworddep);
					}
					else
					{
						String theDEPTmpKey = "DEP=DIR=ROOT";
						analysisedKey.add(theDEPTmpKey);
						if(!predwordstructuresbyattributes.containsKey(theDEPTmpKey))
						{
							predwordstructuresbyattributes.put(theDEPTmpKey, new HashSet<String>());
						}
						predwordstructuresbyattributes.get(theDEPTmpKey).add(curworddep);
					}
					
					{
						//int normalizedLen = curWordName.length() > 6 ? 6 : curWordName.length();
						int normalizedLen = 0;
						if(!bRoot)
						{
							if(!bDirectionRight)
							{
								for (int idn = i+1; idn <= headseq; idn++) {
									if(!other.deprels.get(idn).endsWith("#in"))
									{
										normalizedLen++;
										if(normalizedLen >= 8)break;
									}
								}
							}
							else
							{
								for (int idn = i-1; idn >= headseq; idn--) {
									if(!other.deprels.get(idn).endsWith("#in"))
									{
										normalizedLen++;
										if(normalizedLen >= 8)break;
									}
								}
							}
						}
						String theDEPTmpKey = String.format("DEP=LEN=%d", normalizedLen);
						analysisedKey.add(theDEPTmpKey);
						if(!predwordstructuresbyattributes.containsKey(theDEPTmpKey))
						{
							predwordstructuresbyattributes.put(theDEPTmpKey, new HashSet<String>());
						}
						predwordstructuresbyattributes.get(theDEPTmpKey).add(curworddep);
					}
					
					
					{
						String theDEPTmpKey = String.format("DEP=POS=[%s]", goldPOS);
						analysisedKey.add(theDEPTmpKey);
						if(!predwordstructuresbyattributes.containsKey(theDEPTmpKey))
						{
							predwordstructuresbyattributes.put(theDEPTmpKey, new HashSet<String>());
						}
						predwordstructuresbyattributes.get(theDEPTmpKey).add(curworddep);
					}
					
					{
						String theDEPTmpKey = String.format("DEP=ST=[%d]", sentId);
						analysisedKey.add(theDEPTmpKey);
						if(!predwordstructuresbyattributes.containsKey(theDEPTmpKey))
						{
							predwordstructuresbyattributes.put(theDEPTmpKey, new HashSet<String>());
						}
						predwordstructuresbyattributes.get(theDEPTmpKey).add(curworddep);
					}
					
					{
						String theDEPTmpKey = "DEP=ALL";
						analysisedKey.add(theDEPTmpKey);
						if(!predwordstructuresbyattributes.containsKey(theDEPTmpKey))
						{
							predwordstructuresbyattributes.put(theDEPTmpKey, new HashSet<String>());
						}
						predwordstructuresbyattributes.get(theDEPTmpKey).add(curworddep);
					}
				}

			}
			else
			{
				//predindeps.add(String.format("%d<==%d", i, heads.get(i)-1));
			}				
		}
		
		for(String theAnsKey : analysisedKey)
		{
			analysisResult.put(theAnsKey, new HashMap<String, Integer>());
			analysisResult.get(theAnsKey).put("GOLD", 0);
			analysisResult.get(theAnsKey).put("PRED", 0);
			analysisResult.get(theAnsKey).put("CORRECT", 0);
			
			int correct = 0;
			if(predwordstructuresbyattributes.containsKey(theAnsKey) && goldwordstructuresbyattributes.containsKey(theAnsKey))
			{
				for(String theKeyVaules : predwordstructuresbyattributes.get(theAnsKey))
				{
					if(goldwordstructuresbyattributes.get(theAnsKey).contains(theKeyVaules))
					{
						correct++;
					}
				}
			}
			analysisResult.get(theAnsKey).put("CORRECT", correct);
			if(predwordstructuresbyattributes.containsKey(theAnsKey))
			{
				analysisResult.get(theAnsKey).put("PRED", predwordstructuresbyattributes.get(theAnsKey).size());
			}
			
			if(goldwordstructuresbyattributes.containsKey(theAnsKey))
			{
				analysisResult.get(theAnsKey).put("GOLD", goldwordstructuresbyattributes.get(theAnsKey).size());
			}
		}
				
		return true;
	}
	
	
	public boolean analysisWithOther(CharDependency other, Set<String> inDict) {

		// for evaluate
		// 0 cur_sent_word_num;  1 other_sent_word_num;
		// 2 out_uas_correct_num; 3 out_las_correct_num;  
		// 4 root_correct; (above the word); 5 gold_dep_num; 6 pred_dep_num;
		// 7 in_uas_correct_num; 8 gold_in_uas_num; 9 pred_in_uas_num
		// 10 word_structure_correct; 11 word_correct;  12 pos_correct; 
		// 13 out_uarc_correct_num; 14 out_larc_correct_num;
		
		for (int i = 0; i < eval_res.length; i++) {
			eval_res[i] = 0;
		}
		Set<String> goldwords = new TreeSet<String>();
		Set<String> goldwordposs = new TreeSet<String>();
		Set<String> goldwordstructures = new TreeSet<String>();
		Set<String> goldoutdeps = new TreeSet<String>();
		Set<String> goldoutdeplabels = new TreeSet<String>();
		Set<String> goldindeps = new TreeSet<String>();
		Set<String> goldoutarcs = new TreeSet<String>();
		Set<String> goldoutarclabels = new TreeSet<String>();
		
		//Set<String> predwords = new TreeSet<String>();
		//Set<String> predwordposs = new TreeSet<String>();
		//Set<String> predwordstructures = new TreeSet<String>();
		//Set<String> predoutdeps = new TreeSet<String>();
		//Set<String> predoutdeplabels = new TreeSet<String>();
		//Set<String> predindeps = new TreeSet<String>();
		//Set<String> predoutarcs = new TreeSet<String>();
		//Set<String> predoutarclabels = new TreeSet<String>();
		
		int length = forms.size();
		if(other.forms.size() != length)return false;
		for (int i = 0; i < length; i++) {
			if(!deprels.get(i).endsWith("#in"))
			{
				String curWord = String.format("[%d,%d]", wordboundarystarts.get(i), wordboundaryends.get(i));
				String curWordName = "";
				for(int idy = wordboundarystarts.get(i); idy <= wordboundaryends.get(i); idy++)
				{
					curWordName = curWordName + forms.get(idy);
				}
				int headseq = heads.get(i)-1;
				String headWord = "ROOT";
				if(headseq >= 0 && headseq < length)
					headWord = String.format("[%d,%d]", wordboundarystarts.get(headseq), wordboundaryends.get(headseq));
				String curPOS = cpostags.get(i).substring(0, cpostags.get(i).length()-2);
				goldwords.add(curWord);
				if(!inDict.contains(curWordName))
				{
					eval_res[22]++;
				}
				goldwordposs.add(curWord + "_" + curPOS);
				goldwordstructures.add(curWord + "_" + curPOS+"|"+wordstructure(i));
				if (!isPunc(curPOS)) 
				{
					goldoutdeps.add(String.format("%s<==%s", curWord, headWord));
					goldoutdeplabels.add(String.format("%s<==%s[%s]", curWord, headWord, deprels.get(i)));
				
					if(heads.get(i) != 0)
					{
						goldoutarcs.add(String.format("[%d,%d]", wordboundarystarts.get(i), wordboundarystarts.get(heads.get(i)-1)));
						goldoutarclabels.add(String.format("%s[%d,%d]", deprels.get(i), wordboundarystarts.get(i), wordboundarystarts.get(heads.get(i)-1)));
					}
					else
					{
						goldoutarcs.add(String.format("[%d,%d]", wordboundarystarts.get(i), -1));
						goldoutarclabels.add(String.format("%s[%d,%d]", deprels.get(i), wordboundarystarts.get(i), -1));
					}
				}

			}
			else
			{
				goldindeps.add(String.format("%d<==%d", i, heads.get(i)-1));
			}
		}
		
		
		for (int i = 0; i < length; i++) {
			String keystr;
			if(!other.deprels.get(i).endsWith("#in"))
			{
				String curWord = String.format("[%d,%d]", other.wordboundarystarts.get(i), other.wordboundaryends.get(i));
				String curWordName = "";
				for(int idy = other.wordboundarystarts.get(i); idy <= other.wordboundaryends.get(i); idy++)
				{
					curWordName = curWordName + other.forms.get(idy);
				}
				int headseq = other.heads.get(i)-1;
				String headWord = "ROOT";
				if(headseq >= 0 && headseq < length)
					headWord = String.format("[%d,%d]", other.wordboundarystarts.get(headseq), other.wordboundaryends.get(headseq));
				String curPOS = other.cpostags.get(i).substring(0, other.cpostags.get(i).length()-2);
				//predwords.add(curWord);
				eval_res[1]++;
				if(goldwords.contains(curWord))
				{
					if(!inDict.contains(curWordName))
					{
						eval_res[23]++;
					}
					eval_res[11]++;
				}
				if(goldwordposs.contains(curWord + "_" + curPOS))
				{
					eval_res[12]++;
					if(!inDict.contains(curWordName))
					{
						eval_res[24]++;
					}
				}
				if(goldwordstructures.contains(curWord + "_" + curPOS+"|"+other.wordstructure(i)))
				{
					eval_res[10]++;
					if(!inDict.contains(curWordName))
					{
						eval_res[25]++;
					}
				}
				if (!isPunc(curPOS)) 
				{
					eval_res[6]++;
					keystr = String.format("%s<==%s", curWord, headWord);
					
					if(goldwordstructures.contains(curWord + "_" + curPOS+"|"+other.wordstructure(i)))
					{						
						if(goldoutdeps.contains(keystr))
						{
							eval_res[15]++;
							if(keystr.endsWith("ROOT"))
							{
								eval_res[16]++;
							}
						}
					}
					
					if(goldoutdeps.contains(keystr))
					{
						eval_res[2]++;
						if(keystr.endsWith("ROOT"))
						{
							eval_res[4]++;
						}
					}
					else
					{
						if(goldwordposs.contains(curWord + "_" + curPOS))
						{
							eval_res[18]++;
						}
						if(goldwordstructures.contains(curWord + "_" + curPOS+"|"+other.wordstructure(i)))
						{
							eval_res[19]++;
						}
					}
					
					
					keystr = String.format("%s<==%s[%s]", curWord, headWord, other.deprels.get(i));
					if(goldwordstructures.contains(curWord + "_" + curPOS+"|"+other.wordstructure(i)))
					{	
						if(goldoutdeplabels.contains(keystr))
						{
							eval_res[17]++;
						}
					}
					
					
					if(goldoutdeplabels.contains(keystr))
					{
						eval_res[3]++;
					}
					else
					{
						if(goldwordposs.contains(curWord + "_" + curPOS))
						{
							eval_res[20]++;
						}
						if(goldwordstructures.contains(curWord + "_" + curPOS+"|"+other.wordstructure(i)))
						{
							eval_res[21]++;
						}
					}
					
				}
			}
			else
			{
				eval_res[9]++;
				keystr = String.format("%d<==%d", i, other.heads.get(i)-1);
				if(goldindeps.contains(keystr))
				{
					eval_res[7]++;
				}
			}
		}
		
		eval_res[0] = goldwords.size();		
		eval_res[8] = goldindeps.size();		
		eval_res[5] = goldoutdeps.size();

		

		
		

		
		return true;
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
	
	public String wordstructurewithpos(int root)
	{
		int startbound = wordboundarystarts.get(root);
		int endbound = wordboundaryends.get(root);
		String thePOS = cpostags.get(root).substring(0, cpostags.get(root).length()-2);
		//String thePOS = "PSU";

		
		Stack<Tree<String>> stackTrees = new Stack<Tree<String>>();
		int curPOSI = startbound; 
		while(true)
		{
			if(stackTrees.size() == 0)
			{
				Tree<String> nextChild = new Tree<String>(thePOS + "#b");
				Tree<String> nextChildChild = new Tree<String>(forms.get(curPOSI));
				List<Tree<String>> nextChildChildren = new ArrayList<Tree<String>>();
				nextChildChildren.add(nextChildChild);
				nextChild.setChildren(nextChildChildren);
				nextChild.root = curPOSI;
				nextChild.smaller = curPOSI;
				nextChild.bigger = curPOSI;
				stackTrees.push(nextChild);
				curPOSI++;
			}
			else if(stackTrees.size() == 1 && curPOSI <= endbound)
			{
				Tree<String> nextChild = new Tree<String>(thePOS + "#i");
				Tree<String> nextChildChild = new Tree<String>(forms.get(curPOSI));
				List<Tree<String>> nextChildChildren = new ArrayList<Tree<String>>();
				nextChildChildren.add(nextChildChild);
				nextChild.setChildren(nextChildChildren);
				nextChild.root = curPOSI;
				nextChild.smaller = curPOSI;
				nextChild.bigger = curPOSI;
				stackTrees.push(nextChild);
				curPOSI++;
			}
			else if(stackTrees.size() == 1 && curPOSI == endbound + 1)
			{
				break;
			}
			else
			{
				Tree<String> topTree = stackTrees.pop();
				Tree<String> top2Tree = stackTrees.pop();
				int topTreeHead = heads.get(topTree.root) -1;
				int top2TreeHead = heads.get(top2Tree.root) -1;
				if(topTreeHead <= top2Tree.bigger && topTreeHead >= top2Tree.smaller)
				{
					Tree<String> headtree = top2Tree;
					Tree<String> parenttree = null;
					while(headtree.root != topTreeHead)
					{
						parenttree = headtree;
						Tree<String> rightSubTree = headtree.getChild(1);
						headtree = rightSubTree;
						parenttree.bigger = topTree.bigger;
					}
					
					Tree<String> nextChild = new Tree<String>(thePOS + "#l");
					List<Tree<String>> nextChildChildren = new ArrayList<Tree<String>>();
					nextChildChildren.add(headtree);
					nextChildChildren.add(topTree);
					nextChild.setChildren(nextChildChildren);
					nextChild.smaller = headtree.smaller;
					nextChild.bigger = topTree.bigger;
					nextChild.root = headtree.root;
					
					if(parenttree == null)
					{					
						stackTrees.push(nextChild);
					}
					else
					{
						parenttree.setChild(1, nextChild);
						stackTrees.push(top2Tree);
					}
				}
				else if(top2TreeHead <= topTree.bigger && top2TreeHead >= topTree.smaller)
				{
					Tree<String> headtree = topTree;
					Tree<String> parenttree = null;
					while(headtree.root != top2TreeHead)
					{
						parenttree = headtree;
						Tree<String> leftSubTree = headtree.getChild(0);
						headtree = leftSubTree;
						parenttree.smaller = top2Tree.smaller;
					}
					
					Tree<String> nextChild = new Tree<String>(thePOS + "#r");
					List<Tree<String>> nextChildChildren = new ArrayList<Tree<String>>();
					nextChildChildren.add(top2Tree);
					nextChildChildren.add(headtree);
					nextChild.setChildren(nextChildChildren);
					nextChild.smaller = top2Tree.smaller;
					nextChild.bigger = headtree.bigger;
					nextChild.root = headtree.root;
					
					if(parenttree == null)
					{					
						stackTrees.push(nextChild);
					}
					else
					{
						parenttree.setChild(0, nextChild);
						stackTrees.push(topTree);
					}
				}
				else if(curPOSI <= endbound)
				{
					Tree<String> nextChild = new Tree<String>(thePOS + "#i");
					Tree<String> nextChildChild = new Tree<String>(forms.get(curPOSI));
					List<Tree<String>> nextChildChildren = new ArrayList<Tree<String>>();
					nextChildChildren.add(nextChildChild);
					nextChild.setChildren(nextChildChildren);
					nextChild.root = curPOSI;
					nextChild.smaller = curPOSI;
					nextChild.bigger = curPOSI;
					stackTrees.push(top2Tree);
					stackTrees.push(topTree);
					stackTrees.push(nextChild);
					curPOSI++;
				}
				else
				{
					System.out.println("error word structure");
					return "";
				}
			}
		}
		
				
		return stackTrees.pop().toString();
	}
	
	
	public String wordstructure(int root)
	{
		int startbound = wordboundarystarts.get(root);
		int endbound = wordboundaryends.get(root);
		//String thePOS = cpostags.get(root).substring(0, cpostags.get(root).length()-2);
		String thePOS = "PSU";

		
		Stack<Tree<String>> stackTrees = new Stack<Tree<String>>();
		int curPOSI = startbound; 
		while(true)
		{
			if(stackTrees.size() == 0)
			{
				Tree<String> nextChild = new Tree<String>(thePOS + "#b");
				Tree<String> nextChildChild = new Tree<String>(forms.get(curPOSI));
				List<Tree<String>> nextChildChildren = new ArrayList<Tree<String>>();
				nextChildChildren.add(nextChildChild);
				nextChild.setChildren(nextChildChildren);
				nextChild.root = curPOSI;
				nextChild.smaller = curPOSI;
				nextChild.bigger = curPOSI;
				stackTrees.push(nextChild);
				curPOSI++;
			}
			else if(stackTrees.size() == 1 && curPOSI <= endbound)
			{
				Tree<String> nextChild = new Tree<String>(thePOS + "#i");
				Tree<String> nextChildChild = new Tree<String>(forms.get(curPOSI));
				List<Tree<String>> nextChildChildren = new ArrayList<Tree<String>>();
				nextChildChildren.add(nextChildChild);
				nextChild.setChildren(nextChildChildren);
				nextChild.root = curPOSI;
				nextChild.smaller = curPOSI;
				nextChild.bigger = curPOSI;
				stackTrees.push(nextChild);
				curPOSI++;
			}
			else if(stackTrees.size() == 1 && curPOSI == endbound + 1)
			{
				break;
			}
			else
			{
				Tree<String> topTree = stackTrees.pop();
				Tree<String> top2Tree = stackTrees.pop();
				int topTreeHead = heads.get(topTree.root) -1;
				int top2TreeHead = heads.get(top2Tree.root) -1;
				if(topTreeHead <= top2Tree.bigger && topTreeHead >= top2Tree.smaller)
				{
					Tree<String> headtree = top2Tree;
					Tree<String> parenttree = null;
					while(headtree.root != topTreeHead)
					{
						parenttree = headtree;
						Tree<String> rightSubTree = headtree.getChild(1);
						headtree = rightSubTree;
						parenttree.bigger = topTree.bigger;
					}
					
					Tree<String> nextChild = new Tree<String>(thePOS + "#l");
					List<Tree<String>> nextChildChildren = new ArrayList<Tree<String>>();
					nextChildChildren.add(headtree);
					nextChildChildren.add(topTree);
					nextChild.setChildren(nextChildChildren);
					nextChild.smaller = headtree.smaller;
					nextChild.bigger = topTree.bigger;
					nextChild.root = headtree.root;
					
					if(parenttree == null)
					{					
						stackTrees.push(nextChild);
					}
					else
					{
						parenttree.setChild(1, nextChild);
						stackTrees.push(top2Tree);
					}
				}
				else if(top2TreeHead <= topTree.bigger && top2TreeHead >= topTree.smaller)
				{
					Tree<String> headtree = topTree;
					Tree<String> parenttree = null;
					while(headtree.root != top2TreeHead)
					{
						parenttree = headtree;
						Tree<String> leftSubTree = headtree.getChild(0);
						headtree = leftSubTree;
						parenttree.smaller = top2Tree.smaller;
					}
					
					Tree<String> nextChild = new Tree<String>(thePOS + "#r");
					List<Tree<String>> nextChildChildren = new ArrayList<Tree<String>>();
					nextChildChildren.add(top2Tree);
					nextChildChildren.add(headtree);
					nextChild.setChildren(nextChildChildren);
					nextChild.smaller = top2Tree.smaller;
					nextChild.bigger = headtree.bigger;
					nextChild.root = headtree.root;
					
					if(parenttree == null)
					{					
						stackTrees.push(nextChild);
					}
					else
					{
						parenttree.setChild(0, nextChild);
						stackTrees.push(topTree);
					}
				}
				else if(curPOSI <= endbound)
				{
					Tree<String> nextChild = new Tree<String>(thePOS + "#i");
					Tree<String> nextChildChild = new Tree<String>(forms.get(curPOSI));
					List<Tree<String>> nextChildChildren = new ArrayList<Tree<String>>();
					nextChildChildren.add(nextChildChild);
					nextChild.setChildren(nextChildChildren);
					nextChild.root = curPOSI;
					nextChild.smaller = curPOSI;
					nextChild.bigger = curPOSI;
					stackTrees.push(top2Tree);
					stackTrees.push(topTree);
					stackTrees.push(nextChild);
					curPOSI++;
				}
				else
				{
					System.out.println("error word structure");
					return "";
				}
			}
		}
		
				
		return stackTrees.pop().toString();
	}
	

	/*
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
	}	*/

}
