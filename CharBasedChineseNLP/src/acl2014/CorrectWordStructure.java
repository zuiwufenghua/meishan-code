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

public class CorrectWordStructure {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Map<String, Tree<String>> orginalWordStructures = new HashMap<String, Tree<String>>();
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
					orginalWordStructures.put(thePOS + "_" + theWord, normalizedTree);
					//output.println(normalizedTree.toString());
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
		
		
		Map<String, Tree<String>> correctWordStructures = new HashMap<String, Tree<String>>();
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
					correctWordStructures.put(thePOS + "_" + theWord, normalizedTree);
					if(!orginalWordStructures.containsKey(thePOS + "_" + theWord))
					{
						orginalWordStructures.put(thePOS + "_" + theWord, normalizedTree);
					}
					//output.println(normalizedTree.toString());
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
				new FileOutputStream(args[2]), "UTF-8"), false);
		
		for(Entry<String, Tree<String>> theElem : chapossortlist)
		{
			if(correctWordStructures.containsKey(theElem.getKey()))
			{
				output.println(correctWordStructures.get(theElem.getKey()).toString());				
			}
			else
			{
				output.println(theElem.getValue().toString());				
			}
		}
		
		
		output.close();

	}

}
