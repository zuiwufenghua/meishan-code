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

public class GetCommonRightHead {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		
		String thePOSfix = args[3];
		
		Map<String, Map<String, Integer>> subwordstructure = new HashMap<String, Map<String, Integer>>();
		Map<String, Set<String>> onePOSWords = new HashMap<String, Set<String>>();
		
		String sLine = null;
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
					if(!thePOS.startsWith(thePOSfix))continue;
					if(!onePOSWords.get(thePOS).contains(theWord))continue;
					//output.println(normalizedTree.toString());
					List<Tree<String>> allNonTerminals = normalizedTree.getNonTerminals();
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
		
		
		List<Entry<String, Map<String, Integer>>> chapossortlist = new ArrayList<Entry<String, Map<String, Integer>>>(subwordstructure.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				Map<String, Integer> s1 = (Map<String, Integer>)obj1.getValue();
				Map<String, Integer> s2 = (Map<String, Integer>)obj2.getValue();
				
				Integer freq1 = s1.get("lx") + s1.get("lh") + s1.get("lc") + s1.get("rx") + s1.get("rh") + s1.get("rc");
				Integer freq2 = s2.get("lx") + s2.get("lh") + s2.get("lc") +s2.get("rx") + s2.get("rh") + s2.get("rc");
				
				
				return freq2.compareTo(freq1);				
            }   
		}); 
		
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);
		
		for(Entry<String, Map<String, Integer>> theElem : chapossortlist)
		{
			if(theElem.getKey().length() > 1)continue;
			String outline = String.format("%s\tlh\t%d\t%d\tlc\t%d\t%d\trh\t%d\t%d\trc\t%d\t%d", theElem.getKey(), theElem.getValue().get("lh"), theElem.getValue().get("lc")+theElem.getValue().get("lx"),
					theElem.getValue().get("lc"),  theElem.getValue().get("lh") + theElem.getValue().get("lx"), theElem.getValue().get("rh"), theElem.getValue().get("rc")+theElem.getValue().get("rx"),
					theElem.getValue().get("rc"), theElem.getValue().get("rh") + theElem.getValue().get("rx"));
			output.println(outline);
		}
		
		
		output.close();
		

	}
	
	
	

}
