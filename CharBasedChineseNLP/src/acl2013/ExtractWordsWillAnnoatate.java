package acl2013;

import mason.utils.PinyinComparator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
import edu.berkeley.nlp.syntax.Trees;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class ExtractWordsWillAnnoatate {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		//AD,JJ,NN,VA,VC,VE,VV
		Set<String> validPOS = new TreeSet<String>();
		validPOS.add("AD");validPOS.add("JJ");validPOS.add("NN");validPOS.add("VA");
		validPOS.add("VC");validPOS.add("VE");validPOS.add("VV");validPOS.add("LC");
		
		Map<String, Tree<String>> specialWordPOSTree = new HashMap<String, Tree<String>>();
		InputStreamReader inputData = new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8");
		PennTreeReader treeReader = new PennTreeReader(inputData);

		Trees.TreeTransformer<String> treeTransformer = new Trees.StandardTreeNormalizer();
		while (treeReader.hasNext()) {
			Tree<String> normalizedTree = treeTransformer.transformTree(treeReader.next());
			//normalizedTree.removeUnaryChains();
			//normalizedTree.removeEmptyNodes();
			while(normalizedTree.getLabel().equalsIgnoreCase("root")
					|| normalizedTree.getLabel().equalsIgnoreCase("top"))
			{
				normalizedTree = normalizedTree.getChild(0);
			}
			String theword = normalizedTree.getTerminalStr();
			List<Tree<String>> posTrees = normalizedTree.getPreTerminals();
			String thePOSWithAffix = posTrees.get(0).getLabel();
			String thePOS = thePOSWithAffix.substring(0, thePOSWithAffix.length()-2);
			specialWordPOSTree.put(theword + "_" + thePOS, normalizedTree);
		}
		inputData.close();
		
		
		Map<String, Tree<String>> validWordTree = new HashMap<String, Tree<String>>();
		
		inputData = new InputStreamReader(
				new FileInputStream(args[1]), "UTF-8");
		treeReader = new PennTreeReader(inputData);

		while (treeReader.hasNext()) {
			Tree<String> normalizedTree = treeTransformer.transformTree(treeReader.next());
			//normalizedTree.removeUnaryChains();
			//normalizedTree.removeEmptyNodes();
			while(normalizedTree.getLabel().equalsIgnoreCase("root")
					|| normalizedTree.getLabel().equalsIgnoreCase("top"))
			{
				normalizedTree = normalizedTree.getChild(0);
			}
			String theword = normalizedTree.getTerminalStr();
			validWordTree.put(theword, normalizedTree);
		}
		inputData.close();
		
		
		
		Map<String, String> sepcialwordposstrtree = new HashMap<String, String>();
		Map<String, String> validwordtree = new HashMap<String, String>();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[2]), "UTF8"));
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
					if(!specialWordPOSTree.containsKey(theWord + "_" + thePOS))
					{
						Tree<String> tree = AutomaticLeftBinarizeWordStut.getLeftInternalStructureTree(theWord, thePOS);
						String outstr = String.format("(FRAG (%s#t  %s ))", thePOS, tree.toString());
						sepcialwordposstrtree.put(theWord + "_" + thePOS, outstr);
					}
				}
				else
				{
					if(!validWordTree.containsKey(theWord))
					{
						Tree<String> tree = AutomaticLeftBinarizeWordStut.getLeftInternalStructureTree(theWord, "NN");
						String outstr = String.format("(FRAG (NN#t  %s ))", tree.toString());
						validwordtree.put(theWord, outstr);
					}
				}

						
			}
			
		}		
		in.close();
		
		
		List<Entry<String, String>> chapossortlist = new ArrayList<Entry<String, String>>(sepcialwordposstrtree.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		}); 
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[3]), "UTF-8"), false);
		int iCount = 0;
		for(Entry<String, String> curCharPoslist: chapossortlist)
		{
			out.println(curCharPoslist.getValue());
			iCount++;
		}
		System.out.println(iCount);
		out.close();
		
		
		chapossortlist = new ArrayList<Entry<String, String>>(validwordtree.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		}); 
		out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[4]), "UTF-8"), false);
		iCount = 0;
		for(Entry<String, String> curCharPoslist: chapossortlist)
		{
			out.println(curCharPoslist.getValue());
			iCount++;
		}
		System.out.println(iCount);
		out.close();

	}

}
