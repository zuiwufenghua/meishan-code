package acl2014;

import acl2013.CFGWordStructureNormalize;
import mason.utils.PinyinComparator;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class WSLResultRefine {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Map<String, String> orginalWordStructures = new HashMap<String, String>();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));

		String sLine = null;
			
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().length() < 2) continue;
			try
			{
				String[] threeUnits = sLine.trim().split("\t");
				if(threeUnits.length < 2)
				{
					System.out.println(sLine.trim());
					continue;
				}
				PennTreeReader reader = new PennTreeReader(
						new StringReader(threeUnits[1].trim()));
				if(!reader.hasNext() )
				{
					System.out.println(sLine.trim());
					continue;
				}
				String bCare = "";
				if(threeUnits.length > 2 && (
						threeUnits[2].indexOf("s") != -1 || threeUnits[2].indexOf("S") != -1 ||
						threeUnits[2].indexOf("t") != -1 || threeUnits[2].indexOf("T") != -1))
				{
					bCare = "\t[S]";
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
					orginalWordStructures.put(thePOS + "_" + theWord, normalizedTree.toString() + bCare);
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
		
		List<Entry<String, String>> chapossortlist = new ArrayList<Entry<String, String>>(orginalWordStructures.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		}); 
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		
		for(Entry<String, String> theElem : chapossortlist)
		{
			output.println(theElem.getValue());
		}
		
		
		output.close();

	}

}
