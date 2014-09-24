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

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;


public class CFGWordStructureNormalize {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		String sLine = "";
		Map<String, Tree<String>> wordstructures = new HashMap<String, Tree<String>>();
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
				if(checkWordStructure(curTree))
				{
					RemovePOSInfo(curTree);
					wordstructures.put(curTree.getTerminalStr(), curTree);
					
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
		
		
		List<Entry<String, Tree<String>>> chapossortlist = new ArrayList<Entry<String, Tree<String>>>(wordstructures.entrySet());
		
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
		for(Entry<String, Tree<String>> curCharPoslist: chapossortlist)
		{
			out.println(curCharPoslist.getKey() + "\t" + curCharPoslist.getValue().toString());
			iCount++;
		}
		System.out.println(iCount);
		out.close();
	}
	
	public static void RemovePOSInfo(Tree<String> curTree)
	{
		curTree.annotateSubTrees();
		List<Tree<String>> allTrees = curTree.getNonTerminals();
		for(Tree<String> childTree : allTrees)
		{
			String theLabel = childTree.getLabel();
			int splitIndex = theLabel.lastIndexOf("#");
			childTree.setLabel(theLabel.substring(splitIndex+1));			
		}
	}
	
	public static boolean checkWordStructure(Tree<String> theTree)
	{
		String toplabel = theTree.getLabel();
		Tree<String> curTree = theTree;
		while(toplabel.indexOf("#") == -1)
		{
			curTree = curTree.getChild(0);
			if(curTree.isPreTerminal())
			{
				return false;
			}
			toplabel = curTree.getLabel();
		}
		if(toplabel.endsWith("#t"))curTree = curTree.getChild(0);
		try{				
			curTree.annotateSubTrees();
			List<Tree<String>> allTrees = curTree.getNonTerminals();
			for(Tree<String> childTree : allTrees)
			{
				String theLabel = childTree.getLabel();
				if(childTree.isPreTerminal())
				{
					if(childTree.getChildren().size() != 1) return false;
					String terminalstr = childTree.getTerminalStr();
					if(terminalstr.length() != 1) return false;
					if(childTree.smaller == 0 && !theLabel.endsWith("#b"))
					{
						return false;
					}
					
					if(childTree.smaller != 0 && !theLabel.endsWith("#i"))
					{
						return false;
					}
					
				}
				else
				{
					int splitIndex = theLabel.lastIndexOf("#");
					String mark = theLabel.substring(splitIndex+1, splitIndex+2);
					if( !mark.equals("x") && !mark.equals("y") && !mark.equals("z"))
					{
						return false;
					}
					
					if(childTree.getChildren().size() != 2) return false;
				}
			}
		}
		catch (Exception ex)
		{
			return false;
		}
		
		
		return true;
	}

}
