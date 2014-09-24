package cl2014;

import mason.srl.DepInstanceSRL;
import mason.srl.SDPSRLCorpusReader;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;

public class ExtractSRLFeatures {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		SDPSRLCorpusReader sdpCorpusReader = new SDPSRLCorpusReader();
		sdpCorpusReader.Init(args[0]);
		
		List<DepInstanceSRL> vecInstances = sdpCorpusReader.m_vecInstances;
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"));	
		
		int multiroot = 0;
		for(DepInstanceSRL depInst :  vecInstances)
		{
			
			if(depInst.predicateslabels.size()==0)continue;
			
			int length = depInst.forms.size();
			
			List<String>  DepRelation = new ArrayList<String>();
			List<String>  HeadwordPOS = new ArrayList<String>();
			List<String>  DepwordPOS = new ArrayList<String>();
			List<String>  Headword = new ArrayList<String>();
			List<String>  Depword = new ArrayList<String>();
			List<String>  HeadwordLemma = new ArrayList<String>();
			List<String>  DepwordLemma = new ArrayList<String>();
			List<String>  FirstWord = new ArrayList<String>();
			List<String>  LastWord = new ArrayList<String>();
			List<String>  FirstPOS = new ArrayList<String>();
			List<String>  LastPOS = new ArrayList<String>();
			List<String>  FirstLemma = new ArrayList<String>();
			List<String>  LastLemma = new ArrayList<String>();
			List<String>  ConstituentPOSPattern = new ArrayList<String>();
			List<String>  ChildrenPOS = new ArrayList<String>();
			List<String>  ChildrenPOSNoDup = new ArrayList<String>();
			List<String>  ChildrenREL = new ArrayList<String>();
			List<String>  ChildrenRELNoDup = new ArrayList<String>();
			List<String>  SiblingsPOS = new ArrayList<String>();
			List<String>  SiblingsPOSNoDup = new ArrayList<String>();
			List<String>  SiblingsREL = new ArrayList<String>();
			List<String>  SiblingsRELNoDup  = new ArrayList<String>();
			List<String>  LeftSiblingsPOS = new ArrayList<String>();
			List<String>  RightSiblingsPOS = new ArrayList<String>();
			List<String>  LeftSiblingsWord = new ArrayList<String>();
			List<String>  RightSiblingsWord = new ArrayList<String>();
			List<String>  LeftSiblingsRel = new ArrayList<String>();
			List<String>  RightSiblingsRel = new ArrayList<String>();
			
			List<String>  IsConstFirstWord = new ArrayList<String>();
			
			List<List<Integer>>  path2roots  = new ArrayList<List<Integer>>();			
			
			
			boolean bMultiRoot = false;
		
			
			for(int idx = 0; idx < length; idx++)
			{
				int headwordid = depInst.heads.get(idx) -1;
				Depword.add(depInst.forms.get(idx));
				DepwordPOS.add(depInst.cpostags.get(idx));
				DepRelation.add(depInst.deprels.get(idx));
				DepwordLemma.add(depInst.lemmas.get(idx));
				if(headwordid >= 0)
				{
					Headword.add(depInst.forms.get(headwordid));
					HeadwordPOS.add(depInst.cpostags.get(headwordid));
					HeadwordLemma.add(depInst.lemmas.get(headwordid));
				}
				else
				{
					Headword.add("ROOT");
					HeadwordPOS.add("ROOT");
					HeadwordLemma.add("ROOT");
				}
				
				Set<Integer>  curCons = new TreeSet<Integer>
				(
					new Comparator(){   
						public int compare(Object o1, Object o2) {    
							
							Integer a1 = (Integer)o1;
							Integer a2 = (Integer)o2;
							
							return a2.compareTo(a1);				
			            }   
					}
				);
			     
				depInst.getDescendants(idx, curCons);
				
				Set<String> innerPOSs = new TreeSet<String>
				(
					new Comparator(){   
						public int compare(Object o1, Object o2) {    								
							String a1 = (String)o1;
							String a2 = (String)o2;
							
							return a2.compareTo(a1);				
			            }   
					}
				);
				
				int innerid = 0; 
				String conpatternFirst = "";
				String conpatternLast = "";
				if(curCons.size() == 0)
				{
					System.out.println("error");
				}
				for(int subid : curCons)
				{
					if(innerid == 0)
					{
						FirstWord.add(depInst.forms.get(subid));
						FirstPOS.add(depInst.cpostags.get(subid));
						conpatternFirst = depInst.cpostags.get(subid);
						FirstLemma.add(depInst.lemmas.get(subid));
						if(subid == idx)
						{
							IsConstFirstWord.add("true");
						}
						else
						{
							IsConstFirstWord.add("false");
						}
					}
					
					if (innerid == curCons.size() -1)
					{
						LastWord.add(depInst.forms.get(subid));
						LastPOS.add(depInst.cpostags.get(subid));
						conpatternLast = depInst.cpostags.get(subid);
						LastLemma.add(depInst.lemmas.get(subid));						
					}
					
					if (innerid < curCons.size() -1 && innerid > 0)
					{
						innerPOSs.add(depInst.cpostags.get(subid));
					}
					innerid++;	
				}
				
				String conpattern = conpatternFirst;
				for(String curPOS : innerPOSs)
				{
					conpattern = conpattern + "_" + curPOS;
				}
				
				conpattern = conpattern + "_" + conpatternLast;
				ConstituentPOSPattern.add(conpattern);
							
				
				Set<Integer>  curChds = new TreeSet<Integer>
				(
						new Comparator(){   
							public int compare(Object o1, Object o2) {    								
								Integer a1 = (Integer)o1;
								Integer a2 = (Integer)o2;
								
								return a2.compareTo(a1);				
				            }   
						}
					);
				
				depInst.getChildren(idx, curChds);
				
				
				
				Set<String> innerDEPs = new TreeSet<String>
				(
					new Comparator(){   
						public int compare(Object o1, Object o2) {    
							
							String a1 = (String)o1;
							String a2 = (String)o2;
							
							return a2.compareTo(a1);				
			            }   
					}
				);
				
				innerPOSs.clear();
				
				String innerPOSPattern = "";
				String innerDEPPattern = "";
				for(int subid : curChds)
				{
					innerPOSPattern = innerPOSPattern + "_" + depInst.cpostags.get(subid);
					innerDEPPattern = innerDEPPattern + "_" + depInst.deprels.get(subid);
					innerPOSs.add(depInst.cpostags.get(subid));
					innerDEPs.add(depInst.deprels.get(subid));
				}
				
				String innerPOSPatternNoDup = "";
				for(String curPOS : innerPOSs)
				{
					innerPOSPatternNoDup  = innerPOSPatternNoDup + "_" + curPOS;
				}
				
				String innerDEPPatternNoDup = "";	
				for(String curDEP : innerDEPs)
				{
					innerDEPPatternNoDup  = innerDEPPatternNoDup + "_" + curDEP;
				}
				
				if(curChds.size() > 0)
				{
					ChildrenPOS.add(innerPOSPattern.substring(1));
					ChildrenPOSNoDup.add(innerPOSPatternNoDup.substring(1));
					ChildrenREL.add(innerPOSPattern.substring(1));
					ChildrenRELNoDup.add(innerPOSPatternNoDup.substring(1));
				}
				else
				{
					ChildrenPOS.add("#EMPTY#");
					ChildrenPOSNoDup.add("#EMPTY#");
					ChildrenREL.add("#EMPTY#");
					ChildrenRELNoDup.add("#EMPTY#");
				}
				
				if(headwordid >= 0)
				{
					curChds.clear();
					depInst.getChildren(headwordid, curChds);
					
					innerPOSs.clear();
					innerDEPs.clear();
					
					innerPOSPattern = "";
					innerDEPPattern = "";
					int leftsib = -1;
					int rightsib = length;
					for(int subid : curChds)
					{
						if(subid > idx && subid < rightsib)
						{
							rightsib = subid;
						}
						
						if(subid < idx && subid > leftsib)
						{
							leftsib = subid;
						}
						innerPOSPattern = innerPOSPattern + "_" + depInst.cpostags.get(subid);
						innerDEPPattern = innerDEPPattern + "_" + depInst.deprels.get(subid);
						innerPOSs.add(depInst.cpostags.get(subid));
						innerDEPs.add(depInst.deprels.get(subid));
					}
					
					innerPOSPatternNoDup = "";
					for(String curPOS : innerPOSs)
					{
						innerPOSPatternNoDup  = innerPOSPatternNoDup + "_" + curPOS;
					}
					
					innerDEPPatternNoDup = "";	
					for(String curDEP : innerDEPs)
					{
						innerDEPPatternNoDup  = innerDEPPatternNoDup + "_" + curDEP;
					}
					SiblingsPOS.add(innerPOSPattern.substring(1));
					SiblingsPOSNoDup.add(innerPOSPatternNoDup.substring(1));
					SiblingsREL.add(innerPOSPattern.substring(1));
					SiblingsRELNoDup.add(innerPOSPatternNoDup.substring(1));
					
					if(leftsib >= 0)
					{
						LeftSiblingsPOS.add(depInst.cpostags.get(leftsib));
						LeftSiblingsWord.add(depInst.forms.get(leftsib));
						LeftSiblingsRel.add(depInst.deprels.get(leftsib));
					}
					else
					{
						LeftSiblingsPOS.add("#NONE#");
						LeftSiblingsWord.add("#NONE#");
						LeftSiblingsRel.add("#NONE#");
					}
					
					if(rightsib < length)
					{
						RightSiblingsPOS.add(depInst.cpostags.get(rightsib));
						RightSiblingsWord.add(depInst.forms.get(rightsib));
						RightSiblingsRel.add(depInst.deprels.get(rightsib));
					}
					else
					{
						RightSiblingsPOS.add("#NONE#");
						RightSiblingsWord.add("#NONE#");
						RightSiblingsRel.add("#NONE#");
					}
					
				}
				else
				{
					SiblingsPOS.add("ROOT");
					SiblingsPOSNoDup.add("ROOT");
					SiblingsREL.add("ROOT");
					SiblingsRELNoDup.add("ROOT");
					
					LeftSiblingsPOS.add("ROOT");
					LeftSiblingsWord.add("ROOT");
					LeftSiblingsRel.add("ROOT");
					
					RightSiblingsPOS.add("ROOT");
					RightSiblingsWord.add("ROOT");
					RightSiblingsRel.add("ROOT");
				}
				
				path2roots.add(depInst.path2Root(idx));
			}
			
			
			
			
			int srlCount = 0;
			for(int i = 0; i < depInst.predicates_type.size(); i++)
			{
				if(depInst.predicates_type.get(i).equals("N"))
				{
					srlCount++;
				}
				else if(depInst.predicates_type.get(i).equals("V"))
				{
					List<String> srllabels = depInst.predicateslabels.get(srlCount);
					
					for(int subidx = 0; subidx < srllabels.size(); subidx++)
					{						
						String strout = srllabels.get(subidx);
						
						strout = strout + " " + "#DepRelation#" + DepRelation.get(subidx);
						strout = strout + " " + "#HeadwordPOS#" + HeadwordPOS.get(subidx);
						strout = strout + " " + "#DepwordPOS#" + DepwordPOS.get(subidx);
						strout = strout + " " + "#Headword#" + Headword.get(subidx);
						strout = strout + " " + "#Depword#" + Depword.get(subidx);
						
						strout = strout + " " + "#Depword#DepRelation#Headword#" + Depword.get(subidx) + "@" + DepRelation.get(subidx) + "@" + Headword.get(subidx);
						//strout = strout + " " + "#HeadwordLemma#" + HeadwordLemma.get(subidx);
						//strout = strout + " " + "#DepwordLemma#" + DepwordLemma.get(subidx);
						strout = strout + " " + "#FirstWord#" + FirstWord.get(subidx);
						strout = strout + " " + "#LastWord#" + LastWord.get(subidx);
						strout = strout + " " + "#FirstPOS#" + FirstPOS.get(subidx);
						strout = strout + " " + "#LastPOS#" + LastPOS.get(subidx);
						//strout = strout + " " + "#FirstLemma#" + FirstLemma.get(subidx);
						//strout = strout + " " + "#LastLemma#" + LastLemma.get(subidx);
						strout = strout + " " + "#ConstituentPOSPattern#" + ConstituentPOSPattern.get(subidx);						
						strout = strout + " " + "#ConstituentPOSPattern#Headword#" + ConstituentPOSPattern.get(subidx) + "@" + Headword.get(subidx);
						
						strout = strout + " " + "#IsConstFirstWord#" + IsConstFirstWord.get(subidx);
						
						strout = strout + " " + "#LeftSiblingsPOS#" + LeftSiblingsPOS.get(subidx);
						strout = strout + " " + "#LeftSiblingsWord#" + LeftSiblingsWord.get(subidx);
						strout = strout + " " + "#LeftSiblingsRel#" + LeftSiblingsRel.get(subidx);
						
						strout = strout + " " + "#RightSiblingsPOS#" + RightSiblingsPOS.get(subidx);
						strout = strout + " " + "#RightSiblingsWord#" + RightSiblingsWord.get(subidx);
						strout = strout + " " + "#RightSiblingsRel#" + RightSiblingsRel.get(subidx);
						

						
						strout = strout + " " + "#ChildrenPOS#" + ChildrenPOS.get(subidx);
						strout = strout + " " + "#ChildrenPOSNoDup#" + ChildrenPOSNoDup.get(subidx);
						strout = strout + " " + "#ChildrenREL#" + ChildrenREL.get(subidx);
						strout = strout + " " + "#ChildrenRELNoDup#" + ChildrenRELNoDup.get(subidx);
						strout = strout + " " + "#SiblingsPOS#" + SiblingsPOS.get(subidx);
						strout = strout + " " + "#SiblingsPOSNoDup#" + SiblingsPOSNoDup.get(subidx);
						strout = strout + " " + "#SiblingsREL#" + SiblingsREL.get(subidx);
						strout = strout + " " + "#SiblingsRELNoDup#" + SiblingsRELNoDup.get(subidx);
						
						//strout = strout + " " + "#PrecidateChildrenPOS#" + ChildrenPOS.get(i);
						//strout = strout + " " + "#PrecidateChildrenPOSNoDup#" + ChildrenPOSNoDup.get(i);
						//strout = strout + " " + "#PrecidateChildrenREL#" + ChildrenREL.get(i);
						//strout = strout + " " + "#PrecidateChildrenRELNoDup#" + ChildrenRELNoDup.get(i);
						//strout = strout + " " + "#PrecidateSiblingsPOS#" + SiblingsPOS.get(i);
						//strout = strout + " " + "#PrecidateSiblingsPOSNoDup#" + SiblingsPOSNoDup.get(i);
						//strout = strout + " " + "#PrecidateSiblingsREL#" + SiblingsREL.get(i);
						strout = strout + " " + "#PrecidateSiblingsRELNoDup#" + SiblingsRELNoDup.get(i);
						
						//strout = strout + " " + "#PrecidateLemma#" + DepwordLemma.get(i);
						strout = strout + " " + "#Precidate#" + Depword.get(i);
						
						
						//strout = strout + " " + "#DepRelation#Precidate#" + DepRelation.get(subidx) + "@" + Depword.get(i);
						//strout = strout + " " + "#HeadwordPOS#Precidate#" + HeadwordPOS.get(subidx) + "@" + Depword.get(i);
						//strout = strout + " " + "#DepwordPOS#Precidate#" + DepwordPOS.get(subidx) + "@" + Depword.get(i);
						//strout = strout + " " + "#Headword#Precidate#" + Headword.get(subidx) + "@" + Depword.get(i);
						//strout = strout + " " + "#Depword#Precidate#" + Depword.get(subidx) + "@" + Depword.get(i);
						//strout = strout + " " + "#HeadwordLemma#" + HeadwordLemma.get(subidx);
						//strout = strout + " " + "#DepwordLemma#" + DepwordLemma.get(subidx);
						//strout = strout + " " + "#FirstWord#Precidate#" + FirstWord.get(subidx) + "@" + Depword.get(i);
						//strout = strout + " " + "#LastWord#Precidate#" + LastWord.get(subidx) + "@" + Depword.get(i);
						//strout = strout + " " + "#FirstPOS#Precidate#" + FirstPOS.get(subidx) + "@" + Depword.get(i);
						//strout = strout + " " + "#LastPOS#Precidate#" + LastPOS.get(subidx) + "@" + Depword.get(i);
						
						List<Integer> curId2Root = depInst.path2Root(subidx);
						List<Integer> predicateId2Root = depInst.path2Root(i);
						
						if(curId2Root.get(0) != predicateId2Root.get(0))
						{
							curId2Root.add(0, predicateId2Root.get(0));
							bMultiRoot = true;
						}
						
						int downidx = 0;
						for(; downidx < curId2Root.size() && downidx < predicateId2Root.size(); downidx++)
						{
							int idx1 = curId2Root.get(downidx);
							int idx2 = predicateId2Root.get(downidx);
							if(idx1 != idx2)
							{
								break;
							}
						}
						
						String upPath = DepwordPOS.get(curId2Root.get(downidx-1));
						String upPathREL = DepRelation.get(curId2Root.get(downidx-1));
						int upPathLen = 1;
						String downPath = DepwordPOS.get(predicateId2Root.get(downidx-1));
						String downPathREL = DepRelation.get(predicateId2Root.get(downidx-1));
						int downPathLen = 1;
						for(int startIdx = downidx; startIdx < curId2Root.size(); startIdx++)
						{
							upPath = upPath + "_" + DepwordPOS.get(curId2Root.get(startIdx));
							upPathREL = upPathREL + "_" + DepRelation.get(curId2Root.get(startIdx));
							upPathLen++;
						}
						
						for(int startIdx = downidx; startIdx < predicateId2Root.size(); startIdx++)
						{
							downPath = downPath + "_" + DepwordPOS.get(predicateId2Root.get(startIdx));
							downPathREL = downPathREL + "_" + DepRelation.get(predicateId2Root.get(startIdx));
							downPathLen++;
						}
						
						strout = strout + " " + "#Path#" + upPath + "@" + downPath;
						strout = strout + " " + "#UpPath#" + upPath;
						strout = strout + " " + "#DownPath#" + downPath;
						
						
						strout = strout + " " + "#Headword#Path#" + Headword.get(subidx) + "@" + upPath + "@" + downPath;
						strout = strout + " " + "#Depword#Path#" + Depword.get(subidx) + "@" + upPath + "@" + downPath;
						strout = strout + " " + "#HeadwordPOS#Path#" + HeadwordPOS.get(subidx) + "@" + upPath + "@" + downPath;
						strout = strout + " " + "#DepwordPOS#Path#" + DepwordPOS.get(subidx) + "@" + upPath + "@" + downPath;
						
						//strout = strout + " " + "#Headword#UpPath#" + Headword.get(subidx) + "@" + upPath;
						//strout = strout + " " + "#Depword#UpPath#" + Depword.get(subidx) + "@" + upPath;
						//strout = strout + " " + "#HeadwordPOS#UpPath#" + HeadwordPOS.get(subidx) + "@" + upPath;
						//strout = strout + " " + "#DepwordPOS#UpPath#" + DepwordPOS.get(subidx) + "@" + upPath;
						
						//strout = strout + " " + "#PathREL#" + upPathREL + "@" + downPathREL;
						//strout = strout + " " + "#UpPathREL#" + upPathREL;
						//strout = strout + " " + "#DownPathREL#" + downPathREL;
						
						//strout = strout + " " + "#Headword#PathREL#" + Headword.get(subidx) + "@" + upPathREL + "@" + downPathREL;
						//strout = strout + " " + "#Depword#PathREL#" + Depword.get(subidx) + "@" + upPathREL + "@" + downPathREL;
						//strout = strout + " " + "#HeadwordPOS#PathREL#" + HeadwordPOS.get(subidx) + "@" + upPathREL + "@" + downPathREL;
						//strout = strout + " " + "#DepwordPOS#PathREL#" + DepwordPOS.get(subidx) + "@" + upPathREL + "@" + downPathREL;
						
						//strout = strout + " " + "#Headword#UpPathREL#" + Headword.get(subidx) + "@" + upPathREL;
						//strout = strout + " " + "#Depword#UpPathREL#" + Depword.get(subidx) + "@" + upPathREL;
						//strout = strout + " " + "#HeadwordPOS#UpPathREL#" + HeadwordPOS.get(subidx) + "@" + upPathREL;
						//strout = strout + " " + "#DepwordPOS#UpPathREL#" + DepwordPOS.get(subidx) + "@" + upPathREL;
						
						strout = strout + " " + "#PathLEN#" + String.format("%d", upPathLen + downPathLen -1);
						//strout = strout + " " + "#UpPathLEN#" + String.format("%d", upPathLen);
						//strout = strout + " " + "#DownPathLEN#" + String.format("%d", downPathLen);
						
						//strout = strout + " " + "#DescendantOfPredicate#" + ConstituentPOSPattern.get(i);
						String Position = "Self";
						if(i > subidx)
						{
							Position = "Left";
						}
						if(i < subidx)
						{
							Position = "Right";
						}
						
						strout = strout + " " + "#Position#" + Position;
						strout = strout + " " + "#Position#Precidate#" + Position + "@" + Depword.get(i);
						
						String PredicateFamilyship = "Notrelative";
						if(upPathLen == 1 && downPathLen == 1)
						{
							PredicateFamilyship = "Self";
							
						}
						if(upPathLen == 1 && downPathLen == 2)
						{
							PredicateFamilyship = "Child";
						}
						if(upPathLen == 2 && downPathLen == 1)
						{
							PredicateFamilyship = "Parent";
						}
						if(upPathLen == 1 && downPathLen > 2)
						{
							PredicateFamilyship = "Descendant";
						}
						if(upPathLen > 2 && downPathLen == 1)
						{
							PredicateFamilyship = "Ancestor";
						}
						if(upPathLen == 2 && downPathLen == 2)
						{
							PredicateFamilyship = "Sibling";
						}
						
						strout = strout + " " + "#PredicateFamilyship#" + PredicateFamilyship;
						strout = strout + " " + "#PredicateFamilyship#Precidate#" + PredicateFamilyship + "@" + Depword.get(i);

						
						writer.println(strout);
																		
					}
					
					writer.println();
					srlCount++;
				}
				else
				{
					
				}
			}
			
			if(bMultiRoot)multiroot++;
		}
		
		
		if(multiroot > 0)
		{
			System.out.println(multiroot);
		}
		writer.close();

	}

}
