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

public class CorpusSelect {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		String wordposdictFile = args[0];
		String alreadystructure = args[1];
		
		String outputfile = args[2];
		String[] twoUnits =  args[3].split("-");
		int lower = Integer.parseInt(twoUnits[0]);
		int higher = Integer.parseInt(twoUnits[1]);
		
		Set<String> wordposanno = new HashSet<String>();
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(
				alreadystructure), "UTF-8"));
		String sLine = null;
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
					System.out.println("not have node S:\t" + tree.toString());
					continue;
				}
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
					System.out.println("final pos not agreed:\t" + tree.toString());
					continue;
				}
				if(wordposanno.contains(theWord + "_" + thePOS))
				{
					System.out.println("DUP: " + theWord + "_" + thePOS + "\t" + tree.toString());
				}
				else
				{
					wordposanno.add(theWord + "_" + thePOS);
				}
				
			}
			catch( Exception e)
			{
				System.out.println("parse tree error:\t" + sLine);
			}
		}
		
		in.close();
		
		Set<String> digits = new HashSet<String>();
		for(String curChar : ValidWordStructure.digitsChars)
		{
			digits.add(curChar);
		}
		Set<String> engs = new HashSet<String>();
		for(String curChar : ValidWordStructure.englishChars)
		{
			engs.add(curChar);
		}
		
		Map<String, String> wordInput = new HashMap<String, String>();
		in = new BufferedReader(new InputStreamReader(new FileInputStream(
				wordposdictFile), "UTF-8"));
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] wordposs = sLine.trim().split("\\s+");
			if(wordposs.length < 2) continue;
			if(!PinyinComparator.bContainChineseCharacter(wordposs[0]))continue;
			List<String>  curUnits = new ArrayList<String>();
			curUnits.add(wordposs[0].substring(0,1));
			int lasttype = 0;
			if(digits.contains(wordposs[0].substring(0,1))) lasttype = 1;
			if(engs.contains(wordposs[0].substring(0,1))) lasttype = 2;
			for(int idx  = 1; idx < wordposs[0].length(); idx++)
			{
				int curtype = 0;
				if(digits.contains(wordposs[0].substring(idx,idx+1))) curtype = 1;
				if(engs.contains(wordposs[0].substring(idx,idx+1))) curtype = 2;
				if(lasttype == 0 || curtype == 0 || lasttype != curtype)
				{					
					curUnits.add(wordposs[0].substring(idx,idx+1));
				}
				else if(lasttype == 2 || lasttype == 1)
				{
					curUnits.set(curUnits.size()-1, curUnits.get(curUnits.size()-1) + wordposs[0].substring(idx,idx+1));
				}
				else
				{
					System.out.println("Impossible!");
					curUnits.add(wordposs[0].substring(idx,idx+1));
				}
				lasttype = curtype;
			}
			if(curUnits.size() == 1) continue;
			String theWord = wordposs[0];
			
			
			for(int idx = 1; idx < wordposs.length; idx++)
			{
				int colonIndex = wordposs[idx].lastIndexOf(":");
				if(colonIndex == -1)continue;
				String thePOS =  wordposs[idx].substring(0, colonIndex);
				if(thePOS.equals("NR") || !PinyinComparator.bAllChineseCharacter(theWord))
				{
					
				}
				else
				{
					continue;
				}
				try
				{
					Integer score = Integer.parseInt(wordposs[idx].substring(colonIndex+1));
				}
				catch (Exception e)
				{
					continue;
				}
				if(wordposanno.contains(theWord + "_" + thePOS))
				{
					
				}
				else
				{
					String valueLine = curUnits.get(0) + "_B#" + thePOS;
					for(int idy = 1; idy < curUnits.size()-1; idy++)
					{
						valueLine = valueLine + " " + curUnits.get(idy) + "_M#" + thePOS;
					}
					valueLine = valueLine + " " + curUnits.get(curUnits.size()-1) + "_E#" + thePOS;
					wordInput.put(theWord, valueLine);
				}
				
				
				
				/*
				if(!wordposdict.containsKey(wordposs[0]))
				{
					wordposdict.put(wordposs[0], new HashSet<String>());
				}				
				wordposdict.get(wordposs[0]).add(pos);
				
				if(wordposs[0].length() == 1 && closeposdict.containsKey(pos))
				{
					closeposdict.get(pos).add(wordposs[0]);
				}*/
			}
		}
		
		in.close();
		
		List<Entry<String, String>> chapossortlist = new ArrayList<Entry<String, String>>(wordInput.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.Compare((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		});
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputfile), "UTF-8"), false);
		
		for(Entry<String, String> curCharPoslist: chapossortlist)
		{
			int curWordLength = curCharPoslist.getKey().length();
			if(curWordLength >= lower && curWordLength <= higher)
			{
				output.println(curCharPoslist.getValue().trim());
			}
		}
		
		output.close();

	}

}
