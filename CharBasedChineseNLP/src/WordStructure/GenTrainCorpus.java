package WordStructure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class GenTrainCorpus {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		File file = new File(args[0]); // guided corpus
		File file2 = new File(args[1]); // target corpus

		BufferedReader bf1=new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
				
		String parse1=null;
		Map<String, List<Tree<String>>> treemap=new TreeMap<String, List<Tree<String>>>();

		while((parse1=bf1.readLine())!=null )
		{	
			PennTreeReader reader = new PennTreeReader(
					new StringReader(parse1.trim()));
			Tree<String> tree = reader.next();	
			List<String> curChars = tree.getTerminalYield();
			String curWord = "";
			if(curChars.size() > 0)
			{
				for(String curChar : curChars)
				{
					curWord = curWord + curChar.trim();
				}
				curWord = curWord.trim();
			}
			if(curWord.equals(""))continue;
			Tree<String> subTree1 = tree;
			/*
			while(subTree1.getLabel().equalsIgnoreCase("root") 
				|| subTree1.getLabel().equalsIgnoreCase("top")
					)
			{
				subTree1 = subTree1.getChild(0);
			}
			*/
			if(!treemap.containsKey(curWord))
			{
				treemap.put(curWord, new ArrayList<Tree<String>> ());
			}
			treemap.get(curWord).add(subTree1);
							
		}
		bf1.close();
		
		BufferedReader bf2=new BufferedReader(new InputStreamReader(new FileInputStream(file2),"UTF-8"));
		
		PrintWriter output1 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[2]), "UTF-8"),false);
		PrintWriter output2 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[3]), "UTF-8"),false);
		
		while((parse1=bf2.readLine())!=null )
		{	
			String[] wordPOSs = parse1.trim().split("\\s+");
			if(wordPOSs.length < 2)continue;
			if(wordPOSs[0].length() < 2)continue;
			char[] curWordChars = wordPOSs[0].toCharArray();
			
			for(int idx = 1; idx < wordPOSs.length; idx++)
			{
				int lastColonIndex = wordPOSs[idx].indexOf(":");
				if(lastColonIndex == -1)continue;
				String curPOS = wordPOSs[idx].substring(0, lastColonIndex);
				output2.println(curWordChars[0] + "\tB#" + curPOS);
				for(int idxc = 1; idxc < curWordChars.length -1; idxc++)
				{
					output2.println(curWordChars[idxc] + "\tM#" + curPOS);
				}
				output2.println(curWordChars[curWordChars.length -1] + "\tE#" + curPOS);
				output2.println();
				if(!treemap.containsKey(wordPOSs[0]))continue;
				for(Tree<String> curOrginalTree : treemap.get(wordPOSs[0]))
				{
					Tree<String> curTree = curOrginalTree.shallowClone();
					List<Tree<String>> preTerminals = curTree.getPreTerminals();
					if(preTerminals.size() != curWordChars.length)break;
					for(int idxc = 0; idxc < curWordChars.length; idxc++)
					{
						
						String curLabel = "M#" + curPOS;
						if(idxc == 0)
						{
							curLabel = "B#" + curPOS;
						}
						if(idxc == curWordChars.length -1)
						{
							curLabel = "E#" + curPOS;
						}
						Tree<String> curInsertChild = new Tree<String>(curLabel);
						curInsertChild.setChildren(preTerminals.get(idxc).getChildren());
						List<Tree<String>> newChildren = new ArrayList<Tree<String>>();
						newChildren.add(curInsertChild);
						preTerminals.get(idxc).setChildren(newChildren);
					}
					
					output1.println(curTree.toString());
					output1.flush();
				}
			}
		}
		
		bf2.close();
		output1.close();
		output2.close();
	}

}
