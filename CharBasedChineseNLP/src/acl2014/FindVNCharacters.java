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

public class FindVNCharacters {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		
		//String thePOSfix = args[3];
		
		//Map<String, Map<String, Integer>> subwordstructure = new HashMap<String, Map<String, Integer>>();
		Map<String, Set<String>> onePOSWords = new HashMap<String, Set<String>>();
		Map<String, Map<String, Integer>> verbs = new HashMap<String, Map<String, Integer>>();
		Map<String, Map<String, Integer>> nouns = new HashMap<String, Map<String, Integer>>();
		Map<String, Map<String, Integer>> jjs = new HashMap<String, Map<String, Integer>>();
		Map<String, Map<String, Integer>> vas = new HashMap<String, Map<String, Integer>>();
		Map<String, Map<String, Integer>> ads = new HashMap<String, Map<String, Integer>>();
		
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			String[] theUnits = sLine.trim().split("\\s+");
			if(theUnits.length == 2)
			{
				if(!PinyinComparator.bContainChineseCharacter(theUnits[0]))continue;
				int posEndPosition = theUnits[1].indexOf(":");
				String thePOS = theUnits[1].substring(0, posEndPosition);
				int freq = Integer.parseInt(theUnits[1].substring(posEndPosition+1));
				if(!onePOSWords.containsKey(thePOS))
				{
					onePOSWords.put(thePOS, new HashSet<String>());
				}
				onePOSWords.get(thePOS).add(theUnits[0]);
				if(freq < 5)continue;
				if(theUnits[1].startsWith("VV") || theUnits[1].startsWith("VE") || theUnits[1].startsWith("VC"))
				{
					verbs.put(theUnits[0], new HashMap<String, Integer>());
					verbs.get(theUnits[0]).put("y", 3000);
					verbs.get(theUnits[0]).put("n", 0);
				}
				if(theUnits[1].startsWith("NN") || theUnits[1].startsWith("NR"))
				{
					nouns.put(theUnits[0], new HashMap<String, Integer>());
					nouns.get(theUnits[0]).put("y", 3000);
					nouns.get(theUnits[0]).put("n", 0);
				}
				
				if(theUnits[1].startsWith("JJ"))
				{
					jjs.put(theUnits[0], new HashMap<String, Integer>());
					jjs.get(theUnits[0]).put("y", 3000);
					jjs.get(theUnits[0]).put("n", 0);
				}
				
				if(theUnits[1].startsWith("AD"))
				{
					ads.put(theUnits[0], new HashMap<String, Integer>());
					ads.get(theUnits[0]).put("y", 3000);
					ads.get(theUnits[0]).put("n", 0);
				}
				
				if(theUnits[1].startsWith("VA"))
				{
					vas.put(theUnits[0], new HashMap<String, Integer>());
					vas.get(theUnits[0]).put("y", 3000);
					vas.get(theUnits[0]).put("n", 0);
				}
			}
			
		}
		
		
		
		in.close();
		
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[1]), "UTF8"));
			
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
					//if(!thePOS.startsWith("N") && !thePOS.startsWith("V"))continue;
					if(!onePOSWords.get(thePOS).contains(theWord))continue;
					String theSubWordLabel = normalizedTree.getLabel();
					int headSplitIndex = theSubWordLabel.lastIndexOf("#");
					assert(headSplitIndex == theSubWordLabel.length()-2);
					String theLeftSubWord = normalizedTree.getChild(0).getTerminalStr();
					String theRightSubWord = normalizedTree.getChild(1).getTerminalStr();
					if(!PinyinComparator.bContainChineseCharacter(theLeftSubWord))continue;
					if(!PinyinComparator.bContainChineseCharacter(theLeftSubWord))continue;
					String theSubWordHeadLabel = theSubWordLabel.substring(headSplitIndex+1);
					if(thePOS.startsWith("NN") || thePOS.startsWith("NR"))
					{
						if(theSubWordHeadLabel.endsWith("y"))
						{
							if(theRightSubWord.length() == 1)
							{
								if(!nouns.containsKey(theRightSubWord))
								{
									nouns.put(theRightSubWord, new HashMap<String, Integer>());
									nouns.get(theRightSubWord).put("y", 0);
									nouns.get(theRightSubWord).put("n", 0);
								}
								nouns.get(theRightSubWord).put("y", nouns.get(theRightSubWord).get("y") + 1);
							}
						}
						else 
						if(theSubWordHeadLabel.endsWith("z"))
						{
							if(theRightSubWord.length() == 1)
							{
								if(!nouns.containsKey(theRightSubWord))
								{
									nouns.put(theRightSubWord, new HashMap<String, Integer>());
									nouns.get(theRightSubWord).put("y", 0);
									nouns.get(theRightSubWord).put("n", 0);
								}
								nouns.get(theRightSubWord).put("n", nouns.get(theRightSubWord).get("n") + 1);
							}
							if(theLeftSubWord.length() == 1)
							{
								if(!nouns.containsKey(theLeftSubWord))
								{
									nouns.put(theLeftSubWord, new HashMap<String, Integer>());
									nouns.get(theLeftSubWord).put("y", 0);
									nouns.get(theLeftSubWord).put("n", 0);
								}
								nouns.get(theLeftSubWord).put("y", nouns.get(theLeftSubWord).get("y") + 1);
							}
						}
					}
					
					
					if(thePOS.startsWith("NN") || thePOS.startsWith("NR"))
					{
						if(theSubWordHeadLabel.endsWith("y"))
						{
							if(theRightSubWord.length() == 1)
							{
								if(!nouns.containsKey(theRightSubWord))
								{
									nouns.put(theRightSubWord, new HashMap<String, Integer>());
									nouns.get(theRightSubWord).put("y", 0);
									nouns.get(theRightSubWord).put("n", 0);
								}
								nouns.get(theRightSubWord).put("y", nouns.get(theRightSubWord).get("y") + 1);
							}
						}
						else 
						if(theSubWordHeadLabel.endsWith("z"))
						{
							if(theRightSubWord.length() == 1)
							{
								if(!nouns.containsKey(theRightSubWord))
								{
									nouns.put(theRightSubWord, new HashMap<String, Integer>());
									nouns.get(theRightSubWord).put("y", 0);
									nouns.get(theRightSubWord).put("n", 0);
								}
								nouns.get(theRightSubWord).put("n", nouns.get(theRightSubWord).get("n") + 1);
							}
						}
					}
					
					
					if(thePOS.startsWith("VA"))
					{
						if(theSubWordHeadLabel.endsWith("y"))
						{
							if(theRightSubWord.length() == 1)
							{
								if(!vas.containsKey(theRightSubWord))
								{
									vas.put(theRightSubWord, new HashMap<String, Integer>());
									vas.get(theRightSubWord).put("y", 0);
									vas.get(theRightSubWord).put("n", 0);
								}
								vas.get(theRightSubWord).put("y", vas.get(theRightSubWord).get("y") + 1);
							}
						}
						else 
						if(theSubWordHeadLabel.endsWith("z"))
						{
							if(theRightSubWord.length() == 1)
							{
								if(!vas.containsKey(theRightSubWord))
								{
									vas.put(theRightSubWord, new HashMap<String, Integer>());
									vas.get(theRightSubWord).put("y", 0);
									vas.get(theRightSubWord).put("n", 0);
								}
								vas.get(theRightSubWord).put("n", vas.get(theRightSubWord).get("n") + 1);
							}
						}
					}
					
					
					if(thePOS.startsWith("JJ"))
					{
						if(theSubWordHeadLabel.endsWith("y"))
						{
							if(theRightSubWord.length() == 1)
							{
								if(!jjs.containsKey(theRightSubWord))
								{
									jjs.put(theRightSubWord, new HashMap<String, Integer>());
									jjs.get(theRightSubWord).put("y", 0);
									jjs.get(theRightSubWord).put("n", 0);
								}
								jjs.get(theRightSubWord).put("y", jjs.get(theRightSubWord).get("y") + 1);
							}
						}
						else 
						if(theSubWordHeadLabel.endsWith("z"))
						{
							if(theRightSubWord.length() == 1)
							{
								if(!jjs.containsKey(theRightSubWord))
								{
									jjs.put(theRightSubWord, new HashMap<String, Integer>());
									jjs.get(theRightSubWord).put("y", 0);
									jjs.get(theRightSubWord).put("n", 0);
								}
								jjs.get(theRightSubWord).put("n", jjs.get(theRightSubWord).get("n") + 1);
							}
						}
					}
					
					if(thePOS.startsWith("AD"))
					{
						if(theSubWordHeadLabel.endsWith("y"))
						{
							if(theRightSubWord.length() == 1)
							{
								if(!ads.containsKey(theRightSubWord))
								{
									ads.put(theRightSubWord, new HashMap<String, Integer>());
									ads.get(theRightSubWord).put("y", 0);
									ads.get(theRightSubWord).put("n", 0);
								}
								ads.get(theRightSubWord).put("y", ads.get(theRightSubWord).get("y") + 1);
							}
						}
						else 
						if(theSubWordHeadLabel.endsWith("z"))
						{
							if(theRightSubWord.length() == 1)
							{
								if(!ads.containsKey(theRightSubWord))
								{
									ads.put(theRightSubWord, new HashMap<String, Integer>());
									ads.get(theRightSubWord).put("y", 0);
									ads.get(theRightSubWord).put("n", 0);
								}
								ads.get(theRightSubWord).put("n", ads.get(theRightSubWord).get("n") + 1);
							}
						}
					}
					
					
					if(thePOS.startsWith("VV") || thePOS.startsWith("VC")  || thePOS.startsWith("VE"))
					{
						
						if(theSubWordHeadLabel.endsWith("y"))
						{
							if(theRightSubWord.length() == 1)
							{
								if(!verbs.containsKey(theRightSubWord))
								{
									verbs.put(theRightSubWord, new HashMap<String, Integer>());
									verbs.get(theRightSubWord).put("y", 0);
									verbs.get(theRightSubWord).put("n", 0);
								}
								verbs.get(theRightSubWord).put("y", verbs.get(theRightSubWord).get("y") + 1);
							}
							if(theLeftSubWord.length() == 1)
							{
								if(!verbs.containsKey(theLeftSubWord))
								{
									verbs.put(theLeftSubWord, new HashMap<String, Integer>());
									verbs.get(theLeftSubWord).put("y", 0);
									verbs.get(theLeftSubWord).put("n", 0);
								}
								verbs.get(theLeftSubWord).put("n", verbs.get(theLeftSubWord).get("n") + 1);
							}
						}
						else 
						if(theSubWordHeadLabel.endsWith("z"))
						{
							if(theLeftSubWord.length() == 1)
							{
								if(!verbs.containsKey(theLeftSubWord))
								{
									verbs.put(theLeftSubWord, new HashMap<String, Integer>());
									verbs.get(theLeftSubWord).put("y", 0);
									verbs.get(theLeftSubWord).put("n", 0);
								}
								verbs.get(theLeftSubWord).put("y", verbs.get(theLeftSubWord).get("y") + 1);
							}
						}
					}
					//output.println(normalizedTree.toString());
					//List<Tree<String>> allNonTerminals = normalizedTree.getNonTerminals();
					/*for(Tree<String> oneSubWordTree : allNonTerminals)
					{
						if(oneSubWordTree.isPreTerminal())continue;
						//String theSubWord = oneSubWordTree.getTerminalStr();
						String theSubWordLabel = oneSubWordTree.getLabel();
						int headSplitIndex = theSubWordLabel.lastIndexOf("#");
						assert(headSplitIndex == theSubWordLabel.length()-2);
						String theLeftSubWord = oneSubWordTree.getChild(0).getTerminalStr();
						String theRightSubWord = oneSubWordTree.getChild(1).getTerminalStr();
						String theSubWordHeadLabel = theSubWordLabel.substring(headSplitIndex+1);
						
						if(!subwordstructure.containsKey(theLeftSubWord))
						{
							subwordstructure.put(theLeftSubWord, new HashMap<String, Integer>());
							subwordstructure.get(theLeftSubWord).put("lx", 0);
							subwordstructure.get(theLeftSubWord).put("lh", 0);
							subwordstructure.get(theLeftSubWord).put("lc", 0);
							subwordstructure.get(theLeftSubWord).put("rx", 0);
							subwordstructure.get(theLeftSubWord).put("rh", 0);
							subwordstructure.get(theLeftSubWord).put("rc", 0);
						}
						
						if(!subwordstructure.containsKey(theRightSubWord))
						{
							subwordstructure.put(theRightSubWord, new HashMap<String, Integer>());
							subwordstructure.get(theRightSubWord).put("lx", 0);
							subwordstructure.get(theRightSubWord).put("lh", 0);
							subwordstructure.get(theRightSubWord).put("lc", 0);
							subwordstructure.get(theRightSubWord).put("rx", 0);
							subwordstructure.get(theRightSubWord).put("rh", 0);
							subwordstructure.get(theRightSubWord).put("rc", 0);
						}
						
						if(theSubWordHeadLabel.equals("x"))
						{
							subwordstructure.get(theLeftSubWord).put("lx", subwordstructure.get(theLeftSubWord).get("lx")+1);
							subwordstructure.get(theRightSubWord).put("rx", subwordstructure.get(theRightSubWord).get("rx")+1);
						}
						if(theSubWordHeadLabel.equals("y"))
						{
							subwordstructure.get(theLeftSubWord).put("lc", subwordstructure.get(theLeftSubWord).get("lc")+1);
							subwordstructure.get(theRightSubWord).put("rh", subwordstructure.get(theRightSubWord).get("rh")+1);
						}						
						if(theSubWordHeadLabel.equals("z"))
						{
							subwordstructure.get(theLeftSubWord).put("lh", subwordstructure.get(theLeftSubWord).get("lh")+1);
							subwordstructure.get(theRightSubWord).put("rc", subwordstructure.get(theRightSubWord).get("rc")+1);
						}
					}*/
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
		
		
 
		
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);

		output.println("##################VERB##############");
		List<Entry<String, Map<String, Integer>>> chapossortlist = new ArrayList<Entry<String, Map<String, Integer>>>(verbs.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				Map<String, Integer> s1 = (Map<String, Integer>)obj1.getValue();
				Map<String, Integer> s2 = (Map<String, Integer>)obj2.getValue();
				
				Integer freq1 = s1.get("y") + s1.get("n");
				Integer freq2 = s2.get("y") + s2.get("n");
				
				
				return freq2.compareTo(freq1);				
            }   
		});
		for(Entry<String, Map<String, Integer>> theElem : chapossortlist)
		{
			if(theElem.getKey().length() > 1)continue;
			String outline = String.format("%s\tY\t%d\tN\t%d", theElem.getKey(), theElem.getValue().get("y"),theElem.getValue().get("n"));
			output.println(outline);
		}
		
		
		output.println();
		output.println("##################NOUN##############");
		chapossortlist = new ArrayList<Entry<String, Map<String, Integer>>>(nouns.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				Map<String, Integer> s1 = (Map<String, Integer>)obj1.getValue();
				Map<String, Integer> s2 = (Map<String, Integer>)obj2.getValue();
				
				Integer freq1 = s1.get("y") + s1.get("n");
				Integer freq2 = s2.get("y") + s2.get("n");
				
				
				return freq2.compareTo(freq1);				
            }   
		});
		for(Entry<String, Map<String, Integer>> theElem : chapossortlist)
		{
			if(theElem.getKey().length() > 1)continue;
			String outline = String.format("%s\tY\t%d\tN\t%d", theElem.getKey(), theElem.getValue().get("y"),theElem.getValue().get("n"));
			output.println(outline);
		}
		
		
		output.println();
		output.println("##################JJ##############");
		chapossortlist = new ArrayList<Entry<String, Map<String, Integer>>>(jjs.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				Map<String, Integer> s1 = (Map<String, Integer>)obj1.getValue();
				Map<String, Integer> s2 = (Map<String, Integer>)obj2.getValue();
				
				Integer freq1 = s1.get("y") + s1.get("n");
				Integer freq2 = s2.get("y") + s2.get("n");
				
				
				return freq2.compareTo(freq1);				
            }   
		});
		for(Entry<String, Map<String, Integer>> theElem : chapossortlist)
		{
			if(theElem.getKey().length() > 1)continue;
			String outline = theElem.getKey();
			output.print(outline);
		}
		
		output.println();
		output.println("##################VA##############");
		chapossortlist = new ArrayList<Entry<String, Map<String, Integer>>>(vas.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				Map<String, Integer> s1 = (Map<String, Integer>)obj1.getValue();
				Map<String, Integer> s2 = (Map<String, Integer>)obj2.getValue();
				
				Integer freq1 = s1.get("y") + s1.get("n");
				Integer freq2 = s2.get("y") + s2.get("n");
				
				
				return freq2.compareTo(freq1);				
            }   
		});
		for(Entry<String, Map<String, Integer>> theElem : chapossortlist)
		{
			if(theElem.getKey().length() > 1)continue;
			String outline = theElem.getKey();
			output.print(outline);
		}
		
		output.println();
		output.println("##################AD##############");
		chapossortlist = new ArrayList<Entry<String, Map<String, Integer>>>(ads.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				Map<String, Integer> s1 = (Map<String, Integer>)obj1.getValue();
				Map<String, Integer> s2 = (Map<String, Integer>)obj2.getValue();
				
				Integer freq1 = s1.get("y") + s1.get("n");
				Integer freq2 = s2.get("y") + s2.get("n");
				
				
				return freq2.compareTo(freq1);				
            }   
		});
		for(Entry<String, Map<String, Integer>> theElem : chapossortlist)
		{
			if(theElem.getKey().length() > 1)continue;
			String outline = theElem.getKey();
			output.print(outline);
		}
		
		
		output.close();
		

	}
	
	
	

}
