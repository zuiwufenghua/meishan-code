package acl2014;

import acl2013.CFGWordStructureNormalize;
import mason.utils.PinyinComparator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class RuleFineWordStructure {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub

		Set<String> shuzi = new HashSet<String>();
		shuzi.add("百");
		shuzi.add("十");
		shuzi.add("亿");
		shuzi.add("一");
		shuzi.add("万");
		shuzi.add("点");
		shuzi.add("二");
		shuzi.add("五");
		shuzi.add("三");
		shuzi.add("千");
		shuzi.add("四");
		shuzi.add("六");
		shuzi.add("八");
		shuzi.add("七");
		shuzi.add("九");
		shuzi.add("零");

		
		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));

		String sLine = null;
		
		Map<String, Set<String>> onePOSWords = new HashMap<String, Set<String>>();
		
		while ((sLine = in.readLine()) != null) {
			String[] theUnits = sLine.trim().split("\\s+");
			if(theUnits.length == 2)
			{
				int posEndPosition = theUnits[1].indexOf(":");
				String thePOS = theUnits[1].substring(0, posEndPosition);
				if(!onePOSWords.containsKey(thePOS))
				{
					onePOSWords.put(thePOS, new HashSet<String>());
				}
				onePOSWords.get(thePOS).add(theUnits[0]);
			}
		}
		
		in.close();
		
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[1]), "UTF8"));
		Set<String> rules = new HashSet<String>();
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().length() < 2) continue;
			String[] theUnits = sLine.trim().split("\\s+");
			if(theUnits.length == 3)
			{
				String theRule = theUnits[0] + "\t" +  theUnits[1] + "\t" + theUnits[2];
				rules.add(theRule);
			}
		}
		
		
		in.close();
		
		Map<String, Tree<String>> orginalWordStructures = new HashMap<String, Tree<String>>();
		
		Map<String, Tree<String>> violateWordStructures = new HashMap<String, Tree<String>>();
		
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[2]), "UTF8"));
			
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().length() < 2) continue;
			try
			{
				PennTreeReader reader = new PennTreeReader(
						new StringReader(sLine.trim()));
				if(!reader.hasNext() )
				{
					System.out.println(sLine.trim());
					continue;
				}
				Tree<String> normalizedTree = reader.next();
				//normalizedTree.removeUnaryChains();
				//normalizedTree.removeEmptyNodes();
				while(normalizedTree.getLabel().equalsIgnoreCase("root")
						|| normalizedTree.getLabel().equalsIgnoreCase("top"))
				{
					normalizedTree = normalizedTree.getChild(0);
				}
				if(CFGWordStructureNormalize.checkWordStructure(normalizedTree))
				{
					String theWord = normalizedTree.getTerminalStr();
					if(!PinyinComparator.bContainChineseCharacter(theWord))continue;
					String theLabel = normalizedTree.getLabel();
					int thePOSEndIndex = theLabel.lastIndexOf("#");
					String thePOS = theLabel.substring(0, thePOSEndIndex).trim();
					
					
					//output.println(normalizedTree.toString());
					if(theWord.length() > 2) continue;
					if(!onePOSWords.get(thePOS).contains(theWord))continue;
					if(!thePOS.equals("VV") && !thePOS.equals("NN") && !thePOS.equals("NR"))
					{
						continue;
					}
					List<Tree<String>> allNonTerminals = normalizedTree.getNonTerminals();
					boolean bValid = true;
					for(Tree<String> oneSubWordTree : allNonTerminals)
					{
						if(oneSubWordTree.isPreTerminal())continue;
						//String theSubWord = oneSubWordTree.getTerminalStr();
						String theSubWordLabel = oneSubWordTree.getLabel();
						int headSplitIndex = theSubWordLabel.lastIndexOf("#");
						assert(headSplitIndex == theSubWordLabel.length()-2);
						String theLeftSubWord = oneSubWordTree.getChild(0).getTerminalStr();
						String theRightSubWord = oneSubWordTree.getChild(1).getTerminalStr();
						String theSubWordHeadLabel = theSubWordLabel.substring(headSplitIndex+1);
						//rule 1:
						String newHeadOrg = theSubWordLabel;
						String newHeadByLeft = theSubWordLabel;
						String newHeadByRight = theSubWordLabel;
						int appliedRuleNum = 0;
						String theRefinedPOS = thePOS;

						
						/*if(theRightSubWord.equals("子"))
						{
							oneSubWordTree.setLabel(thePOS + "#x");
						}
						
						if(thePOS.equals("AD") || thePOS.equals("JJ") || thePOS.equals("VA") || thePOS.equals("VE"))
						{
							theRefinedPOS = "VV";
						}
						*/
						if(theLeftSubWord.length() == 1)
						{
							if(rules.contains(theRefinedPOS + "\tlc\t" + theLeftSubWord))
							{
								appliedRuleNum++;
								newHeadByLeft = thePOS + "#y";
							}
							if(rules.contains(theRefinedPOS + "\tlh\t" + theLeftSubWord))
							{
								appliedRuleNum++;
								newHeadByLeft = thePOS + "#z";
							}
						}
						
						if(theRightSubWord.length() == 1)
						{
							if(rules.contains(theRefinedPOS + "\trc\t" + theRightSubWord))
							{
								appliedRuleNum++;
								newHeadByRight = thePOS + "#z";
							}
							if(rules.contains(theRefinedPOS + "\trh\t" + theRightSubWord))
							{
								appliedRuleNum++;
								newHeadByRight = thePOS + "#y";
							}
						}
						
						if(!PinyinComparator.bContainChineseCharacter(theLeftSubWord) && PinyinComparator.bContainChineseCharacter(theRightSubWord))
						{
							oneSubWordTree.setLabel(thePOS + "#y");
						}
						else if(PinyinComparator.bContainChineseCharacter(theLeftSubWord) && !PinyinComparator.bContainChineseCharacter(theRightSubWord))
						{
							oneSubWordTree.setLabel(thePOS + "#z");
						}
						else if(!PinyinComparator.bContainChineseCharacter(theLeftSubWord) && !PinyinComparator.bContainChineseCharacter(theRightSubWord))
						{
							oneSubWordTree.setLabel(thePOS + "#x");
						}
						else if(theLeftSubWord.length() > 1 && theRightSubWord.length() > 1)
						{
							//
							if(!newHeadByLeft.equals(newHeadOrg) || !newHeadByRight.equals(newHeadOrg))
							{
								bValid = false;
							}
						}
						else if(theLeftSubWord.length() == 1 || theRightSubWord.length() == 1)
						{
							if(appliedRuleNum == 0)
							{
								if(thePOS.startsWith("VV"))
								{
									oneSubWordTree.setLabel(thePOS + "#z");
								}
								else if(thePOS.startsWith("NN") || thePOS.startsWith("NR"))
								{
									oneSubWordTree.setLabel(thePOS + "#y");
								}
								else
								{
									oneSubWordTree.setLabel(thePOS + "#y");
								}
							}
							else
							{
								//bValid = false;

								if(!newHeadByLeft.equals(newHeadOrg) && newHeadByRight.equals(newHeadOrg))
								{
									oneSubWordTree.setLabel(newHeadByLeft);
								}
								else if(newHeadByLeft.equals(newHeadOrg) && !newHeadByRight.equals(newHeadOrg))
								{
									oneSubWordTree.setLabel(newHeadByRight);
								}
								else if(newHeadByLeft.equals(newHeadByRight) && !newHeadByRight.equals(newHeadOrg))
								{
									oneSubWordTree.setLabel(newHeadByRight);
								}
								else
								{
									if(thePOS.startsWith("VV"))
									{
										oneSubWordTree.setLabel(thePOS + "#z");
									}
									else if(thePOS.startsWith("NN") || thePOS.startsWith("NR"))
									{
										oneSubWordTree.setLabel(thePOS + "#y");
									}
									else
									{
										oneSubWordTree.setLabel(thePOS + "#y");
									}
								}
							}
						}
						//else if(theLeftSubWord.length() == 1 && theRightSubWord.length() != 1)
						//{
						//	
						//}
						//else if(theLeftSubWord.length() != 1 && theRightSubWord.length() == 1)
						//{
						//	
						//}

						

					}
					if(bValid)
					{
						orginalWordStructures.put(thePOS + "_" + theWord, normalizedTree);
					}
					else
					{
						violateWordStructures.put(thePOS + "_" + theWord, normalizedTree);
					}
				}
				else
				{
					System.out.println(normalizedTree.toString());
				}
			}
			catch(Exception ex)
			{
				System.out.println(sLine);
			}
		}
		
		in.close();
		
		List<Entry<String, Tree<String>>> chapossortlist = new ArrayList<Entry<String, Tree<String>>>(orginalWordStructures.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				String o1s = (String) obj1.getKey();
				String o2s = (String) obj2.getKey();
				if(o1s.startsWith("VV")) o1s = "AAA" + o1s;
				if(o1s.startsWith("N")) o1s = "AAB" + o1s;
				if(o1s.startsWith("CD")) o1s = "AAC" + o1s;
				if(o1s.startsWith("OD")) o1s = "ABA" + o1s;
				
				if(o2s.startsWith("VV")) o2s = "AAA" + o2s;
				if(o2s.startsWith("N")) o2s = "AAB" + o2s;
				if(o2s.startsWith("CD")) o2s = "AAC" + o2s;
				if(o2s.startsWith("OD")) o2s = "ABA" + o2s;
				
				return PinyinComparator.CompareModify(o1s, o2s);				
            }   
		}); 
		
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[3]), "UTF-8"), false);
		
		for(Entry<String, Tree<String>> theElem : chapossortlist)
		{
			output.println(theElem.getValue().toString());				
		}
		
		
		output.close();
		
		
		chapossortlist = new ArrayList<Entry<String, Tree<String>>>(violateWordStructures.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				String o1s = (String) obj1.getKey();
				String o2s = (String) obj2.getKey();
				if(o1s.startsWith("VV")) o1s = "AAA" + o1s;
				if(o1s.startsWith("N")) o1s = "AAB" + o1s;
				if(o1s.startsWith("CD")) o1s = "AAC" + o1s;
				if(o1s.startsWith("OD")) o1s = "ABA" + o1s;
				
				if(o2s.startsWith("VV")) o2s = "AAA" + o2s;
				if(o2s.startsWith("N")) o2s = "AAB" + o2s;
				if(o2s.startsWith("CD")) o2s = "AAC" + o2s;
				if(o2s.startsWith("OD")) o2s = "ABA" + o2s;
				
				return PinyinComparator.CompareModify(o1s, o2s);				
            }   
		}); 
		
		
		output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[4]), "UTF-8"), false);
		
		for(Entry<String, Tree<String>> theElem : chapossortlist)
		{
			output.println(theElem.getValue().toString());				
		}
		
		
		output.close();
	}

}
