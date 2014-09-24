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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class RemoveNonChinese {


	public static void main(String[] args)throws Exception{
		// TODO Auto-generated method stub
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		
		Map<String, Tree<String>> orginalWordStructures = new HashMap<String, Tree<String>>();
		String sLine = null;
			
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
						//String theSubWordHeadLabel = theSubWordLabel.substring(headSplitIndex+1);
						if(!PinyinComparator.bContainChineseCharacter(theRightSubWord))
						{
							if(thePOS.startsWith("NR")  || thePOS.startsWith("CD")
									|| thePOS.startsWith("OD") || thePOS.startsWith("NT"))
							{
								oneSubWordTree.setLabel(thePOS + "#y");
							}
							else
							{
								oneSubWordTree.setLabel(thePOS + "#z");
							}
						}
						
						/*
						if(theLeftSubWord.equals(theRightSubWord))
						{
							if(thePOS.startsWith("N") || thePOS.startsWith("PN") || thePOS.startsWith("M") || thePOS.startsWith("CD")
									|| thePOS.startsWith("OD") || thePOS.startsWith("CD"))
							{
								oneSubWordTree.setLabel(thePOS + "#y");
							}
							else
							{
								oneSubWordTree.setLabel(thePOS + "#z");
							}
						}*/
					}
					
					
					
					
					//output.println(normalizedTree.toString());
					orginalWordStructures.put(thePOS + "_" + theWord, normalizedTree);
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
				new FileOutputStream(args[1]), "UTF-8"), false);
		
		for(Entry<String, Tree<String>> theElem : chapossortlist)
		{
			output.println(theElem.getValue().toString());				
		}
		
		output.close();
		
	}

}
