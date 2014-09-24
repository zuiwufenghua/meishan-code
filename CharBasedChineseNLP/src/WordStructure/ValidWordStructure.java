package WordStructure;

import mason.utils.PinyinComparator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class ValidWordStructure {

	/**
	 * @param args
	 */
	static final String[] ssCtbTags = { "AD", "AS", "BA", "CC", "CD", "CS",
		"DEC", "DEG", "DER", "DEV", "DT", "ETC", "FW", "IJ", "JJ", "LB",
		"LC", "M", "MSP", "NN", "NR", "NT", "OD", "ON", "P", "PN", "PU",
		"SB", "SP", "VA", "VC", "VE", "VV"};
	static final String[] ssClosedTags = { "AS", "BA", "CC", "CS", "DEC", "DEG",
		"DER", "DEV", "DT", "ETC", "IJ", "LB", "LC", "ON", "OD",  "CD", "P", "PN", "PU",
		"SB", "SP", "VC", "VE", "MSP"};
	static final String[] englishChars = {
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", 
        "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", 
        "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", 
        "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", 
        "w", "x", "y", "z", 
        "Ａ", "Ｂ", "Ｃ", "Ｄ", "Ｅ", "Ｆ", "Ｇ", "Ｈ", "Ｉ", 
        "Ｊ", "Ｋ", "Ｌ", "Ｍ", "Ｎ", "Ｏ", "Ｐ", "Ｑ", "Ｒ", 
        "Ｓ", "Ｔ", "Ｕ", "Ｖ", "Ｗ", "Ｘ", "Ｙ", "Ｚ", "ａ", 
        "ｂ", "ｃ", "ｄ", "ｅ", "ｆ", "ｇ", "ｈ", "ｉ", "ｊ", 
        "ｋ", "ｌ", "ｍ", "ｎ", "ｏ", "ｐ", "ｑ", "ｒ", "ｓ", 
        "ｔ", "ｕ", "ｖ", "ｗ", "ｘ", "ｙ", "ｚ"};
	static final String[] digitsChars = {
		   "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "○",
		   "０", "１", "２", "３", "４", "５", "６", "７", "８", "９"};
	
	public static void main(String[] args)  throws Exception{
		// TODO Auto-generated method stub
		String wordposFile = args[0];
		//String wordHeadFile = args[1];
		String inputFile = args[1];
		
		
		//String outputClosedCharFile = args[3];
		String outputWordFile = args[2];
		String errorFile = args[3];
		
		Map<String, Set<String>> wordposdict = new HashMap<String, Set<String>>();
		/*
		Map<String, Set<String>> closeposdict = new HashMap<String, Set<String>>();
		for(String curClosePOS : ssClosedTags)
		{
			closeposdict.put(curClosePOS, new HashSet<String>());
		}
		Set<String> allPOS = new HashSet<String>();
		
		for(String curPOS : ssCtbTags)
		{
			allPOS.add(curPOS);
		}
		Set<String> digits = new HashSet<String>();
		for(String curChar : digitsChars)
		{
			digits.add(curChar);
		}
		Set<String> engs = new HashSet<String>();
		for(String curChar : englishChars)
		{
			engs.add(curChar);
		}
		*/
		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(wordposFile), "UTF8"));
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] wordposs = sLine.trim().split("\\s+");
			for(int idx = 1; idx < wordposs.length; idx++)
			{
				int colonIndex = wordposs[idx].lastIndexOf(":");
				if(colonIndex == -1)continue;
				String pos =  wordposs[idx].substring(0, colonIndex);
				try
				{
					Integer score = Integer.parseInt(wordposs[idx].substring(colonIndex+1));
				}
				catch (Exception e)
				{
					continue;
				}
				
				if(!wordposdict.containsKey(wordposs[0]))
				{
					wordposdict.put(wordposs[0], new HashSet<String>());
				}				
				wordposdict.get(wordposs[0]).add(pos);
				
				/*
				if(wordposs[0].length() == 1 && closeposdict.containsKey(pos))
				{
					closeposdict.get(pos).add(wordposs[0]);
				}*/
			}
		}
		
		in.close();
		
		/*
		Map<String, String> wordHeadDict = new HashMap<String, String>();
		
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(wordHeadFile), "UTF8"));
		
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] wordposs = sLine.trim().split("\\s+");
			if(wordposs.length == 2 || wordposs.length == 3)
			{
				String lastChar = wordposs[wordposs.length-1].toLowerCase();
				String head = "";
				if(lastChar.equals("f"))
				{
					head = "h";
				}			
				else if(lastChar.equals("s"))
				{
					head = "m";
				}
				else if(lastChar.equals("b"))
				{
					head = "c";
				}
				else
				{
					System.out.println(sLine);
					continue;
				}
				String theWord = "";
				String thePOS = "";
				if(wordposs.length == 2) theWord = wordposs[0];
				else
				{
					theWord = wordposs[0].substring(0,1) + wordposs[1].substring(0,1);
					int jinIndex = wordposs[0].lastIndexOf("#");
					String thePOS1 = wordposs[0].substring(jinIndex+1);
					
					jinIndex = wordposs[1].lastIndexOf("#");
					String thePOS2 = wordposs[1].substring(jinIndex+1);
					
					if(thePOS1.equals(thePOS2))
					{
						thePOS = thePOS1;
					}
					else
					{
						System.out.println(sLine);
						continue;
					}
				}
				String theKey = theWord + " " + thePOS;
				wordHeadDict.put(theKey.trim(), head);
			}
		}
		
		in.close();
		*/
		/*
		PrintWriter writerClosedPOSChar = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputClosedCharFile), "UTF-8"), false);
		for(String curClosedPOS : closeposdict.keySet())
		{
			writerClosedPOSChar.println(curClosedPOS);
			for(String curChar : closeposdict.get(curClosedPOS))
			{
				writerClosedPOSChar.print(curChar);
			}
			writerClosedPOSChar.println();
		}
		
		writerClosedPOSChar.close();
		*/
		
		Set<String> wordposanno = new HashSet<String>();
		PrintWriter outputWordStructure = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputWordFile), "UTF-8"), false);
		PrintWriter outputError = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(errorFile), "UTF-8"), false);
		
		in = new BufferedReader(new InputStreamReader(new FileInputStream(
				inputFile), "UTF-8"));
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			if(sLine.trim().replaceAll("\\s+", "").equals("(())"))
			{
				continue;
			}

			try
			{
				final PennTreeReader reader = new PennTreeReader(
						new StringReader(sLine.trim()));
				Tree<String> tree = reader.next();
				Tree<String> subTree1 = tree.getChild(0);
				
				while(subTree1.getLabel().equalsIgnoreCase("root") 
					|| subTree1.getLabel().equalsIgnoreCase("top")
						)
				{
					tree = tree.getChild(0);
					subTree1 = subTree1.getChild(0);
				}
				
				tree.setLabel("TOP");
				
				if(tree.getChildren().size() != 1 || !tree.getChild(0).getLabel().equalsIgnoreCase("s"))
				{
					outputError.println("not have node S:\t" + tree.toString());
					continue;
				}
				
				//检查非终结节点是否合理
				/*
				if(!checkLabels(tree.getChild(0), allPOS, true))
				{
					outputError.println("check error:\t" + tree.toString());
					continue;
				}
				*/
				List<Tree<String>> prevleaves = tree.getPreTerminals();
				String theWord = "";
				String thePOS = "";
				
				boolean bValid = true;
				for(Tree<String> cuPrevrLeave : prevleaves)
				{
					theWord = theWord + cuPrevrLeave.getChild(0).getLabel();
					String curPOSTmp = cuPrevrLeave.getLabel();
					int jinIndex = curPOSTmp.lastIndexOf("#");
					String curPOS = curPOSTmp.substring(jinIndex+1);
					if(thePOS.equals(""))
					{
						thePOS = curPOS;
					}
					else if(!thePOS.equals(curPOS))
					{
						bValid = false;
						break;
					}
				}
				if(!bValid)
				{
					outputError.println("final pos not agreed:\t" + tree.toString());
					continue;
				}
				
				if(!PinyinComparator.bContainChineseCharacter(theWord))
				{
					//outputError.println("final pos not agreed:\t" + tree.toString());
					continue;
				}
				
				/*
				if(!PinyinComparator.bAllChineseCharacter(theWord))
				{
					//outputError.println("contain non chinese:\t" + tree.toString());
					tree.initParent();
					mergeNodesV2(tree, digits, engs);
					System.out.println(tree.toString());
					//continue;
				}*/
				
								
				
				//是否与词典相符合
				//暂不检查
				
				if(wordposdict.containsKey(theWord) && wordposdict.get(theWord).contains(thePOS))
				{
					
				}
				else
				{
					outputError.println(theWord + "\t" + thePOS + "\t" + tree.toString());
					continue;
				}
				
				/*
				if(wordHeadDict.containsKey(theWord))
				{
					String curLabel = tree.getChild(0).getChild(0).getLabel();
					String[] subLabels = curLabel.split("#");
					if(subLabels.length != 2)bValid = false;
					if(bValid)
					{
						if(!subLabels[1].equals(wordHeadDict.get(theWord)))
						{
							bValid = false;
						}
					}
				}
				if(!bValid)
				{
					outputError.println(theWord + "\t" + wordHeadDict.get(theWord) + "\t" + tree.toString());
					continue;
				}
				
				if(wordHeadDict.containsKey(theWord + " " + thePOS))
				{
					String curLabel = tree.getChild(0).getChild(0).getLabel();
					String[] subLabels = curLabel.split("#");
					if(subLabels.length != 2)bValid = false;
					if(bValid)
					{
						if(!subLabels[1].equals(wordHeadDict.get(theWord + " " + thePOS)))
						{
							bValid = false;
						}
					}
				}
				if(!bValid)
				{
					outputError.println(theWord + "\t" + wordHeadDict.get(theWord) + "\t" + tree.toString());
					continue;
				}
				
				*/
				
				if(wordposanno.contains(theWord + "_" + thePOS))
				{
					outputError.println("DUP: " + theWord + "_" + thePOS + "\t" + tree.toString());
				}
				else
				{
					outputWordStructure.println(tree.toString());
				}
				
			}
			catch( Exception e)
			{
				outputError.println("parse tree error:\t" + sLine);
			}
			
		}
		
		in.close();
		
		outputWordStructure.close();
		outputError.close();
		
		
	}
	
	public static boolean checkLabels(Tree<String> tree, Set<String> validLabels, boolean bRoot)
	{
		if(tree.isPreTerminal())
		{
			if(bRoot)return false;
			return true;
		}
		
		String curLabel = tree.getLabel();
		if(bRoot)
		{
			if(curLabel.equalsIgnoreCase("s"))
			{
				
			}
			else
			{
				return false;
			}
		}
		else
		{
			String[] subLabels = curLabel.split("#");
			if(subLabels.length != 2)
			{
				return false;
			}
			if(!validLabels.contains(subLabels[0]))
			{
				return false;
			}
			
			if(subLabels[1].equals("m") || subLabels[1].equals("h")
					|| subLabels[1].equals("c") || subLabels[1].equals("m"))
			{
		
			}
			else
			{
				return false;
			}
		}
		
		List<Tree<String>> children = tree.getChildren();
		if(children.size() == 1)
		{
			if(children.get(0).isPreTerminal())
			{
				return checkLabels(children.get(0), validLabels, false);
			}
			else
			{
				return false;
			}
		}
		else if(children.size() == 2)
		{
			if(!checkLabels(children.get(0), validLabels, false)) return false;
			if(!checkLabels(children.get(1), validLabels, false)) return false;			
			String leftLabel = children.get(0).getLabel();
			String[] subLabelsLeft = leftLabel.split("#");
			String rightLabel = children.get(1).getLabel();
			String[] subLabelsRight = rightLabel.split("#");
			if(subLabelsLeft[1].equals("c") && !subLabelsRight[1].equals("c")) return false;
			//if(subLabelsLeft[1].equals("c") && !subLabelsLeft[0].equals(subLabelsRight[0]))return false;
			if(subLabelsLeft[1].equals("m") && !subLabelsRight[1].equals("h")) return false;
			if(subLabelsLeft[1].equals("h") && !subLabelsRight[1].equals("m")) return false;	
		}
		else
		{
			return false;
		}
							
		return true;
	}

	public static void mergeNodes(Tree<String> tree, Set<String> digits, Set<String> engChars)
	{
		
		List<String> characters = tree.getTerminalYield();
		List<String> charTypes = tree.getPreTerminalYield();
		String theTmpWord = "";
		for(String curChar : characters)
		{
			theTmpWord = theTmpWord + curChar;
		}
		int type = 0;
		if(digits.contains(theTmpWord.substring(0,1))) type = 1;
		if(engChars.contains(theTmpWord.substring(0,1))) type = 2;
		if(type == 1 )
		{
			boolean bConsistent = true;
			for(int idx = 1; idx < theTmpWord.length();idx++)
			{
				if(!digits.contains(theTmpWord.substring(idx,idx+1)))
				{
					bConsistent = false;
					break;
				}
			}
			if(bConsistent)
			{
				String finalType = charTypes.get(0);
				if(charTypes.get(charTypes.size()-1).startsWith("E"))
				{
					finalType = charTypes.get(charTypes.size()-1);
				}
				Tree<String> curNode = new Tree<String>(finalType);
				Tree<String> curChildNode = new Tree<String>(theTmpWord);
				List<Tree<String>> curNodeChildren = new ArrayList<Tree<String>>();
				curNodeChildren.add(curChildNode);
				curNode.setChildren(curNodeChildren);
				List<Tree<String>> curRootChildren = new ArrayList<Tree<String>>();
				curRootChildren.add(curNode);
				tree.setChildren(curRootChildren);
			}
			else
			{
				List<Tree<String>> children = tree.getChildren();
				for(Tree<String> curChild : children)
				{
					mergeNodes(curChild, digits, engChars);
				}
			}
		}
		else if(type == 2 )
		{
			boolean bConsistent = true;
			for(int idx = 1; idx < theTmpWord.length();idx++)
			{
				if(!engChars.contains(theTmpWord.substring(idx,idx+1)))
				{
					bConsistent = false;
					break;
				}
			}
			if(bConsistent)
			{
				String finalType = charTypes.get(0);
				if(charTypes.get(charTypes.size()-1).startsWith("E"))
				{
					finalType = charTypes.get(charTypes.size()-1);
				}
				Tree<String> curNode = new Tree<String>(finalType);
				Tree<String> curChildNode = new Tree<String>(theTmpWord);
				List<Tree<String>> curNodeChildren = new ArrayList<Tree<String>>();
				curNodeChildren.add(curChildNode);
				curNode.setChildren(curNodeChildren);
				List<Tree<String>> curRootChildren = new ArrayList<Tree<String>>();
				curRootChildren.add(curNode);
				tree.setChildren(curRootChildren);
			}
			else
			{
				List<Tree<String>> children = tree.getChildren();
				for(Tree<String> curChild : children)
				{
					mergeNodes(curChild, digits, engChars);
				}
			}
		}
		else
		{
			List<Tree<String>> children = tree.getChildren();
			for(Tree<String> curChild : children)
			{
				mergeNodes(curChild, digits, engChars);
			}
		}
	}
	
	public static boolean mergeNodesV2(Tree<String> tree, Set<String> digits, Set<String> engChars)
	{
		if(tree.isPreTerminal()) return true;
		List<Tree<String>> children = tree.getChildren();
		if(children.size() == 1)
		{
			return mergeNodesV2(children.get(0), digits, engChars);
		}
		else if(children.size() == 2)
		{
			if(!mergeNodesV2(children.get(1), digits, engChars))
			{
				return false;
			}
			if(children.get(1).getChildren().size() == 1)
			{
				List<Tree<String>> lchildren = children.get(0).getPreTerminals();
				String lastWord = lchildren.get(lchildren.size()-1).getChild(0).getLabel();
				int ltype = 0;
				if(digits.contains(lastWord.substring(0,1))) ltype = 1;
				if(engChars.contains(lastWord.substring(0,1))) ltype = 2;
				
				List<Tree<String>> rchildren = children.get(1).getPreTerminals();
				String firstWord = rchildren.get(0).getChild(0).getLabel();			
				int rtype = 0;
				if(digits.contains(firstWord.substring(0,1))) rtype = 1;
				if(engChars.contains(firstWord.substring(0,1))) rtype = 2;
								
				
				if(ltype == 0 || rtype == 0 || ltype != rtype)
				{
					return mergeNodesV2(children.get(0), digits, engChars);
				}
				else
				{
					lchildren.get(lchildren.size()-1).getChild(0).setLabel(lastWord + firstWord);
					if(!lchildren.get(lchildren.size()-1).getLabel().startsWith("B"))
					{
						lchildren.get(lchildren.size()-1).setLabel( rchildren.get(0).getLabel());
					}
					List<Tree<String>> tmpChildren = children.get(0).getChildren();
					tree.setChildren(tmpChildren);
					return mergeNodesV2(tree, digits, engChars);
				}
			}
			else if(children.get(1).getChildren().size() == 2)
			{
				return mergeNodesV2(children.get(0), digits, engChars);
			}
			else
			{
				return false;
			}				
			
		}
		else
		{
			return false;
		}
		
	}
	

	/*
	public static void changeTerminalNode(Tree<String> tree)
	{
		tree.initParent();
		List<Tree<String>> leaves = tree.getPreTerminals();
		for(int idx = 0; idx < leaves.size(); idx++)
		{
			Tree<String> curTree = leaves.get(idx);
			Tree<String> parent = leaves
		}
	}
	*/
}
