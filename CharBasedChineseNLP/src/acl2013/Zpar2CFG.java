package acl2013;

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

import edu.berkeley.nlp.syntax.*;
import edu.berkeley.nlp.syntax.Trees.*;

public class Zpar2CFG {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		boolean bZparFormat = false;
		if(args.length > 2 && args[2].equals("zpar")) bZparFormat = true;
		String sLine = "";
		Map<String, String> wordstructures = new HashMap<String, String>();
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			
			try
			{
				String newLine = sLine.trim();
				int lastLength = newLine.length();
				
				while(true)
				{
					newLine = newLine.replace("( ", "(");
					newLine = newLine.replace(" )", ")");
					newLine = newLine.replace(") ", ")");
					if(newLine.length() == lastLength)
					{
						break;
					}
					lastLength = newLine.length();
				}
				
				
				while(true)
				{
					newLine = newLine.replace(" x ", "#x ");
					newLine = newLine.replace(" t ", "#t ");
					newLine = newLine.replace(" y ", "#y ");
					newLine = newLine.replace(" z ", "#z ");
					newLine = newLine.replace(" b ", "#b ");
					newLine = newLine.replace(" i ", "#i ");
					if(newLine.length() == lastLength)
					{
						break;
					}
					lastLength = newLine.length();
				}
								
				final PennTreeReader reader = new PennTreeReader(
						new StringReader(newLine));
				Tree<String> curTree = reader.next();
				if(curTree.getLabel().endsWith("#t"))
				{
					curTree = curTree.getChild(0);
				}
				if(CFGWordStructureNormalize.checkWordStructure(curTree))
				{
					//if(!PinyinComparator.bAllChineseCharacter(curTree.getTerminalStr()))
					//{
						//CFGWordStructureNormalize.RemovePOSInfo(curTree);
						//if(curTree.getTerminalStr().indexOf("．") != -1)continue;
					String theWord = curTree.getTerminalStr();
					String theLabel = curTree.getLabel();
					int splitIndex = theLabel.lastIndexOf("#");
					String thePOS = theLabel.substring(0, splitIndex);
					if(bZparFormat)
					{
						//out.println(curTree.toCLTString());
						wordstructures.put( thePOS+ "_" + theWord, curTree.toCLTString());
					}
					else
					{
						//out.println(curTree.toString());
						wordstructures.put(thePOS+ "_" + theWord, curTree.toString());
					}
					//out.println(curTree.toCLTString());
						//if(curTree.getTerminalStr().indexOf(".") != -1)
						//{
						//	out.println("(ROOT " + curTree.toString().replace(".", "．") + ")");
						//}						
					//}
				}
				else
				{
					System.out.println(curTree.toString());
				}
			}
			catch(Exception ex)
			{
				System.out.println(sLine);
				continue;
			}
		}
		
		in.close();
		
		List<Entry<String,String>> chapossortlist = new ArrayList<Entry<String, String>>(wordstructures.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		}); 
		
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		int iCount = 0;
		for(Entry<String, String> curCharPoslist: chapossortlist)
		{
			String theoutStr = curCharPoslist.getValue();
			out.println(theoutStr);
			iCount++;
		}
		System.out.println(iCount);
		
		out.close();

	}

}
