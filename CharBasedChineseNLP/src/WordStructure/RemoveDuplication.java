package WordStructure;

import mason.utils.PinyinComparator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class RemoveDuplication {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Set<String> allPOS = new HashSet<String>();
		
		for(String curPOS : ValidWordStructure.ssCtbTags)
		{
			allPOS.add(curPOS);
		}
		
		Map<String, String> wordposanno = new HashMap<String, String>();
				
		for(int idx = 0; idx < args.length; idx++)
		{
			//idx = 0，为gold文件
			removeDulpication(args[idx], wordposanno, allPOS, idx == 0);
		}

	}
	
	
	public static void removeDulpication(String inputFile, Map<String, String> wordposanno, Set<String> allPOS, boolean bGold) throws Exception
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(
				inputFile), "UTF-8"));
				
		Map<String, String> wordpostemp = new HashMap<String, String>();
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
				
				if(!ValidWordStructure.checkLabels(tree.getChild(0), allPOS, true))
				{
					System.out.println("check error:\t" + tree.toString());
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
				
				if(wordposanno.containsKey(theWord + "_" + thePOS))
				{
					if(!wordposanno.get(theWord + "_" + thePOS).replace("\\s+", "").equals(tree.toString().replace("\\s+", "")) && bGold)
					{
						System.out.println("DUP: " + theWord + "_" + thePOS + ":\t0\t" + wordposanno.get(theWord + "_" + thePOS) + "\t1\t" + tree.toString());
					}
				}
				else
				{
					wordpostemp.put(theWord + "_" + thePOS,  tree.toString());
					wordposanno.put(theWord + "_" + thePOS, tree.toString());
				}
			}
			catch( Exception e)
			{
				System.out.println("parse tree error:\t" + sLine);
			}
			
		}
		
		in.close();
		List<Entry<String, String>> chapossortlist = new ArrayList<Entry<String, String>>(wordpostemp.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		}); 
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(inputFile + ".nodup"), "UTF-8"), false);
		for(Entry<String, String> curCharPoslist: chapossortlist)
		{
			out.println(curCharPoslist.getValue());
		}
		
		out.close();
	}

}
