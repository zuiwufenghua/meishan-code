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
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class ExtractWordPOSWhetherAnnotated {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		//AD,JJ,NN,VA,VC,VE,VV
		Set<String> validPOS = new TreeSet<String>();
		validPOS.add("AD");validPOS.add("JJ");validPOS.add("NN");validPOS.add("VA");
		validPOS.add("VC");validPOS.add("VE");validPOS.add("VV");validPOS.add("LC");
		Map<String, Map<String, Integer>> lexicon = new HashMap<String, Map<String, Integer>>();
		Map<String, Set<String>> wordsbypos = new HashMap<String, Set<String>>();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] wordposs = sLine.trim().split("\\s+");
			for(String wordpos : wordposs)
			{
				int splitIndex = wordpos.lastIndexOf("_");
				if(splitIndex == -1)
				{
					System.out.println(wordpos + "[in]" + sLine);
					continue;
				}
				String theWord = wordpos.substring(0, splitIndex);
				String thePOS = wordpos.substring(splitIndex+1);
				if(thePOS.equals("URL"))thePOS = "NR";
				if(theWord.length() < 2)continue;
				if(!PinyinComparator.bAllChineseCharacter(theWord) || !validPOS.contains(thePOS))
				{
					if(!wordsbypos.containsKey(thePOS))
					{
						wordsbypos.put(thePOS, new TreeSet<String>());
					}
					wordsbypos.get(thePOS).add(theWord);
					continue;
				}
				if(!lexicon.containsKey(theWord))
				{
					lexicon.put(theWord, new HashMap<String, Integer>());
				}
				if(!lexicon.get(theWord).containsKey(thePOS))
				{
					lexicon.get(theWord).put(thePOS, 0);
				}
				lexicon.get(theWord).put(thePOS, lexicon.get(theWord).get(thePOS)+1);						
			}
			
		}		
		in.close();
		
		
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[1]), "UTF8"));
		Map<String, Tree<String>> wordstructures = new HashMap<String, Tree<String>>();
		sLine = null;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String newLine = sLine.trim();
			//int firstSplit = newLine.indexOf("\t");
			//if(firstSplit == -1)continue;
			//String strWord = newLine.substring(0, firstSplit);
			String strTree = sLine.trim();
			//String strWord = "";
			
			try
			{
				final PennTreeReader reader = new PennTreeReader(
						new StringReader(strTree));
				Tree<String> tree = reader.next();
				if(tree.getLabel().equalsIgnoreCase("root")
						|| tree.getLabel().equalsIgnoreCase("root"))
				{
					tree = tree.getChild(0);
				}
				String theWord = tree.getTerminalStr();
				if(wordstructures.containsKey(theWord))
				{
					if(!tree.toString().equals(wordstructures.get(theWord).toString()))
					{
						System.out.println("this: " + tree.toString());
						System.out.println("in: " + wordstructures.get(theWord).toString());
					}
					continue;
				}
				wordstructures.put(theWord, tree);
			}			catch(Exception ex)
			{
				System.out.println(sLine);
				continue;
			}
		}
		in.close();
		

		
		List<Entry<String, Map<String, Integer>>> chapossortlist = new ArrayList<Entry<String, Map<String, Integer>>>(lexicon.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		}); 
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);
		int iCount = 0;
		for(Entry<String, Map<String, Integer>> curCharPoslist: chapossortlist)
		{
			String theoutStr = curCharPoslist.getKey();
			for(String thePOS : curCharPoslist.getValue().keySet())
			{
				theoutStr = theoutStr + "\t" + String.format("%s:%d", thePOS, curCharPoslist.getValue().get(thePOS));
			}
			out.println(theoutStr);
			iCount++;
		}
		System.out.println(iCount);
		out.close();
		
		out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[3]), "UTF-8"), false);
		PrintWriter out_o = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[3]+ ".out"), "UTF-8"), false);

		
		for(String onePOS : wordsbypos.keySet())
		{
			Set<String> theWords = wordsbypos.get(onePOS);
			for(String theWord : theWords)
			{
				Tree<String> tree = null;
				if(wordstructures.containsKey(theWord))
				{
					tree = wordstructures.get(theWord);
					List<Tree<String>> nonterminals = tree.getNonTerminals();
					for(Tree<String> theTree : nonterminals)
					{
						String lastMark = theTree.getLabel().substring(theTree.getLabel().length()-1);
						theTree.setLabel(onePOS + "#" + lastMark);
					}
					if(CFGWordStructureNormalize.checkWordStructure(tree))
					{
						out.println(tree.toCLTString());
					}
					else
					{
						System.out.println(onePOS + "_" +sLine);
					}
				}
				else
				{
					tree = AutomaticLeftBinarizeWordStut.getLeftInternalStructureTree(theWord, onePOS);
					out_o.println(tree.toCLTString());
				}				
			}
		}
		out.close();
		out_o.close();

	}

}
