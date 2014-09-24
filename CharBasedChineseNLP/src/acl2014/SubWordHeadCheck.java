package acl2014;

import acl2013.CFGWordStructureNormalize;
import mason.utils.PinyinComparator;

import java.util.List;
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
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Map.Entry;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class SubWordHeadCheck {

	public static void main(String[] args)throws Exception{
		// TODO Auto-generated method stub
		boolean bExcludeNR = false;
		if(args.length > 2 && args[2].endsWith("NR"))bExcludeNR = true;
		Map<String, Map<String, Set<String>>>  subwordstructure = new HashMap<String, Map<String, Set<String>>>();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));

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
					if(bExcludeNR && thePOS.equals("NR"))continue;
					//output.println(normalizedTree.toString());
					List<Tree<String>> allNonTerminals = normalizedTree.getNonTerminals();
					for(Tree<String> oneSubWordTree : allNonTerminals)
					{
						if(oneSubWordTree.isPreTerminal())continue;
						String theSubWord = oneSubWordTree.getTerminalStr();
						if(!subwordstructure.containsKey(theSubWord))
						{
							subwordstructure.put(theSubWord, new HashMap<String, Set<String>>());
						}
						String theSubWordLabel = oneSubWordTree.getLabel();
						int headSplitIndex = theSubWordLabel.lastIndexOf("#");
						assert(headSplitIndex == theSubWordLabel.length()-2);
						String theSubWordHeadLabel = theSubWordLabel.substring(headSplitIndex+1);
						if(!subwordstructure.get(theSubWord).containsKey(theSubWordHeadLabel))
						{
							subwordstructure.get(theSubWord).put(theSubWordHeadLabel, new HashSet<String>());
						}
						subwordstructure.get(theSubWord).get(theSubWordHeadLabel).add(normalizedTree.toString());
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
		
		
		List<Entry<String, Map<String, Set<String>>>> chapossortlist = new ArrayList<Entry<String, Map<String, Set<String>>>>(subwordstructure.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		}); 
		
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		
		int inconsistentnum = 0;
		for(Entry<String, Map<String, Set<String>>> theElem : chapossortlist)
		{
			if(theElem.getValue().keySet().size() > 1)
			{
				//String currentCheckingPOS = "(NN#";
				//boolean bContainCurrentCheckingPOS = false;
				//for(String theKey : theElem.getValue().keySet())
				//{
				//	for(String oneTree : theElem.getValue().get(theKey))
				//	{
				//		if(oneTree.indexOf(currentCheckingPOS) != -1)
				//		{
				//			bContainCurrentCheckingPOS = true;
				//			break;
				//		}
				//	}
					
				//	if(bContainCurrentCheckingPOS)break;
				//}
				//if(!bContainCurrentCheckingPOS)continue;
				inconsistentnum++;
				output.println("#" + inconsistentnum + "\t" +theElem.getKey());
				for(String theKey : theElem.getValue().keySet())
				{
					for(String oneTree : theElem.getValue().get(theKey))
					{
						output.println(oneTree);
					}
				}
				output.println();
			}
		}
		
		output.close();

	}

}
