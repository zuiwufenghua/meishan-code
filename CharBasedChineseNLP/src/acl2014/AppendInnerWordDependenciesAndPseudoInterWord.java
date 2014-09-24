package acl2014;

import acl2013.AutomaticLeftBinarizeWordStut;
import mason.utils.PinyinComparator;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import edu.berkeley.nlp.syntax.Tree;
import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;


public class AppendInnerWordDependenciesAndPseudoInterWord {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		//Set<String> annotatedPOSTags = new HashSet<String>();
		//annotatedPOSTags.add("NR"); annotatedPOSTags.add("AD");
		
		int stadtArg = 0;
		String outGoldStackFile = "";
		if(args[0].equals("-stack"))
		{
			stadtArg = 2;
			outGoldStackFile = args[1];
		}
		
		boolean bDefaultHeadLeft = false;
		String outputDictFile = args[stadtArg+2];
		//if(outputDictFile.endsWith("rightdict"))
		//{
		//	bDefaultHeadLeft = false;
		//}
		Map<String, CharDependency> annotatedWordStructures = new TreeMap<String, CharDependency>();
		if(args.length > stadtArg+3)
		{
			CharDependencyReader cdpCorpusReader = new CharDependencyReader();
			cdpCorpusReader.Init(args[stadtArg+3]);
			for(CharDependency curCharDep : cdpCorpusReader.m_vecInstances)
			{
				String theWord = "";
				for(String theChar : curCharDep.forms)
				{
					theWord = theWord + theChar;
				}
				String thePOS = curCharDep.postags.get(0).substring(0, curCharDep.postags.get(0).length()-2);
				String theKey = theWord + "_" + thePOS;
				annotatedWordStructures.put(theKey, curCharDep);
			}
		}
		
		
		Map<String, CharDependency> unAnnotatedWordStructures = new TreeMap<String, CharDependency>();
		
		SDPCorpusReader depCorpusReader = new SDPCorpusReader();
		depCorpusReader.Init(args[stadtArg]);
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[stadtArg+1]), "UTF-8"), false);
		
		PrintWriter outputgoldstrut = null;
		if(outGoldStackFile.trim().length() > 0)
		{
			outputgoldstrut = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outGoldStackFile), "UTF-8"), false);
		}
		
		for(DepInstance depInst : depCorpusReader.m_vecInstances)
		{
			for(int idx = 0; idx < depInst.forms.size(); idx++)
			{
				depInst.heads.set(idx, idx+2);
				if(idx == depInst.forms.size()-1)
				{
					depInst.heads.set(idx, 0);
				}
			}
			
			CharDependency inst = new CharDependency();
			
			List<Integer> wordHeads = new ArrayList<Integer>();
			int curWordStartIndex = 0;
			
			for(int idx = 0; idx < depInst.forms.size(); idx++)
			{
				//String thePOS = depInst.postags.get(idx);
				//String theWord = depInst.forms.get(idx);
				String theStartPOS = depInst.postags.get(idx) + "#b";
				String theFollowPOS = depInst.postags.get(idx) + "#i";
				CharDependency wordStrut = null;
				if(annotatedWordStructures.containsKey(depInst.forms.get(idx) + "_" + depInst.postags.get(idx)))
				{
					wordStrut = annotatedWordStructures.get(depInst.forms.get(idx) + "_" + depInst.postags.get(idx));
				}
				else
				{
					if(unAnnotatedWordStructures.containsKey(depInst.forms.get(idx) + "_" + depInst.postags.get(idx)))
					{
						wordStrut = unAnnotatedWordStructures.get(depInst.forms.get(idx) + "_" + depInst.postags.get(idx));
					}
					else
					{
						
						wordStrut = new CharDependency();
						
						/*if(bDefaultHeadLeft)
						{
							for(int idy = 0; idy < depInst.forms.get(idx).length(); idy++)
							{
								wordStrut.forms.add(depInst.forms.get(idx).substring(idy, idy+1));
								wordStrut.lemmas.add("_");
								//if(idy == 0)
								{
									wordStrut.postags.add(theStartPOS);
									wordStrut.cpostags.add(theStartPOS);
									wordStrut.heads.add(idy);
								}
								//else
								//{
								//	wordStrut.postags.add(theFollowPOS);
								//	wordStrut.cpostags.add(theFollowPOS);
								//	wordStrut.heads.add(1);
								//}
								wordStrut.deprels.add("#in");
								wordStrut.p1heads.add(-1);
								wordStrut.p1deprels.add("_");
								wordStrut.feats.add("_");
							}
						}
						else*/
						{
							for(int idy = 0; idy < depInst.forms.get(idx).length(); idy++)
							{
								wordStrut.forms.add(depInst.forms.get(idx).substring(idy, idy+1));
								wordStrut.lemmas.add("_");
								if(idy == depInst.forms.get(idx).length()-1)
								{
									wordStrut.postags.add(theStartPOS);
									wordStrut.cpostags.add(theStartPOS);
									wordStrut.heads.add(0);
								}
								else
								{
									wordStrut.postags.add(theFollowPOS);
									wordStrut.cpostags.add(theFollowPOS);
									wordStrut.heads.add(idy+2);
								}
								wordStrut.deprels.add("#in");
								wordStrut.p1heads.add(-1);
								wordStrut.p1deprels.add("_");
								wordStrut.feats.add("_");
							}
						}
					
						unAnnotatedWordStructures.put(depInst.forms.get(idx) + "_" + depInst.postags.get(idx), wordStrut);
					}
					
				}
				
				
				int iHeadIndex = findTreeHead(wordStrut);
				int iHeadLeftMost = findLeftMostChild(wordStrut, iHeadIndex);
				int iHeadRightMost = findRightMostChild(wordStrut, iHeadIndex);
				
				int iHeadLeftMostLeftStart = iHeadLeftMost == -1 ? -1 : findLeftMostGrandChild(wordStrut, iHeadLeftMost);
				int iHeadLeftMostRightEnd = iHeadLeftMost == -1 ? -1 : findRightMostGrandChild(wordStrut, iHeadLeftMost);
				
				int iHeadRightMostLeftStart = iHeadRightMost == -1 ? -1 : findLeftMostGrandChild(wordStrut, iHeadRightMost);
				int iHeadRightMostRightEnd = iHeadRightMost == -1 ? -1 : findRightMostGrandChild(wordStrut, iHeadRightMost);
				
				String theHeadChar = depInst.forms.get(idx).substring(iHeadIndex, iHeadIndex+1);
				String leftMostSpan = iHeadLeftMost == -1 ? "_" : depInst.forms.get(idx).substring(iHeadLeftMostLeftStart, iHeadLeftMostRightEnd+1);
				String rightMostSpan = iHeadRightMost == -1 ? "_" : depInst.forms.get(idx).substring(iHeadRightMostLeftStart, iHeadRightMostRightEnd+1);
				if(outputgoldstrut != null)
				{
					outputgoldstrut.println(String.format("%s\t%d\t%s\t%d\t%s", theHeadChar, iHeadLeftMost, leftMostSpan, iHeadRightMost, rightMostSpan));
				}
				for(int idy = 0; idy < depInst.forms.get(idx).length(); idy++)
				{
					inst.forms.add(depInst.forms.get(idx).substring(idy, idy+1));
					if(idy == 0)
					{
						inst.postags.add(theStartPOS);
						inst.cpostags.add(theStartPOS);
					}
					else
					{
						inst.postags.add(theFollowPOS);
						inst.cpostags.add(theFollowPOS);
					}
					int relativeHead = wordStrut.heads.get(idy);
					if(relativeHead == 0)
					{
						wordHeads.add(curWordStartIndex + idy);
						inst.heads.add(0);
					}
					else
					{						
						inst.heads.add(curWordStartIndex + relativeHead);
					}
					inst.deprels.add("#in");
					inst.p1heads.add(-1);
					inst.p1deprels.add("_");
					inst.feats.add("_");
					inst.lemmas.add("_");
					
					
				}
				
				curWordStartIndex = curWordStartIndex + depInst.forms.get(idx).length();
			}
			
			if(outputgoldstrut != null)
			{
				outputgoldstrut.println();
			}
			
			for(int idx = 0; idx < depInst.forms.size(); idx++)
			{
				int wordbaseHead = depInst.heads.get(idx);
				String label = depInst.deprels.get(idx);
				int curWordHeadCharIndex = wordHeads.get(idx);
				assert(inst.heads.get(curWordHeadCharIndex) == 0);
				if(wordbaseHead != 0)
				{
					int curNewhead = wordHeads.get(wordbaseHead-1) + 1;
					inst.heads.set(curWordHeadCharIndex, curNewhead);
					inst.deprels.set(curWordHeadCharIndex, label);
				}
				else
				{
					inst.heads.set(curWordHeadCharIndex, 0);
					inst.deprels.set(curWordHeadCharIndex, label);
				}
			}
			
			List<String> outstrs = inst.toListString();
			for(String curline : outstrs)
			{
				output.println(curline);
			}
			output.println();
		}
		
		output.close();
		if(outputgoldstrut != null)
		{
			outputgoldstrut.close();
		}
		
		

		Map<String, Tree<String>> remainWordStruts = new HashMap<String, Tree<String>>();
		for(String theKey  : unAnnotatedWordStructures.keySet())
		{
			CharDependency inst = unAnnotatedWordStructures.get(theKey);
			int theWordEndIndex = theKey.lastIndexOf("_");
			String theWord = theKey.substring(0, theWordEndIndex);
			String thePos = theKey.substring(theWordEndIndex+1);
			List<String> outstrs = inst.toListString();
			if(outstrs.size() > 1 && PinyinComparator.bContainChineseCharacter(theWord))
			{
				Tree<String> thewordstructure = AutomaticLeftBinarizeWordStut.getLeftInternalStructureTree(theWord, thePos);
				//for(String curline : outstrs)
				//{
				//	output.println(curline);
				//}
				remainWordStruts.put(thePos + "_" + theWord, thewordstructure);
				
				
			}
			
		}
		
		
		List<Entry<String, Tree<String>>> chapossortlist = new ArrayList<Entry<String, Tree<String>>>(remainWordStruts.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		}); 
		
		output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputDictFile), "UTF-8"), false);
		
		for(Entry<String, Tree<String>> theElem : chapossortlist)
		{
			output.println(theElem.getValue().toString());
		}
		
		
		output.close();


	}
	
	
	public static int findLeftMostGrandChild(CharDependency inst, int index)
	{
		assert(index >= 0 && index < inst.forms.size());
		for(int idx = 0; idx < index; idx++)
		{
			if(inst.heads.get(idx) == index +1)
			{
				return findLeftMostGrandChild(inst, idx);
			}
		}
		
		return index;
	}
	
	
	public static int findRightMostGrandChild(CharDependency inst, int index)
	{
		assert(index >= 0 && index < inst.forms.size());
		for(int idx = inst.forms.size()-1; idx > index; idx--)
		{
			if(inst.heads.get(idx) == index +1)
			{
				return findRightMostGrandChild(inst, idx);
			}
		}
		
		return index;
	}
	
	public static int findLeftMostChild(CharDependency inst, int index)
	{
		assert(index >= 0 && index < inst.forms.size());
		for(int idx = 0; idx < index; idx++)
		{
			if(inst.heads.get(idx) == index +1)
			{
				return idx;
			}
		}
		
		return -1;
	}
	
	
	public static int findRightMostChild(CharDependency inst, int index)
	{
		assert(index >= 0 && index < inst.forms.size());
		for(int idx = inst.forms.size()-1; idx > index; idx--)
		{
			if(inst.heads.get(idx) == index +1)
			{
				return idx;
			}
		}
		
		return -1;
	}
	
	public static int findTreeHead(CharDependency inst)
	{
		for(int idx = 0; idx < inst.forms.size(); idx++)
		{
			if(inst.heads.get(idx) == 0)
			{
				return  idx;
			}
		}
		
		return -1;
	}

}
