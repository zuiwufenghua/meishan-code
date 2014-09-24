package acl2013;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;


public class CFGStructure2Zpar {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Map<String, Set<String>> wordposdict = new HashMap<String, Set<String>>();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
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
					wordposdict.put(wordposs[0], new TreeSet<String>());
				}				
				wordposdict.get(wordposs[0]).add(pos);				
			}
		}
		
		in.close();
		
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[1]), "UTF8"));
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);
		sLine = null;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String newLine = sLine.trim();
			int firstSplit = newLine.indexOf("\t");
			if(firstSplit == -1)continue;
			String strWord = newLine.substring(0, firstSplit);
			String strTree = newLine.substring(firstSplit+1);
			
			try
			{
				final PennTreeReader reader = new PennTreeReader(
						new StringReader(strTree));
				Tree<String> tree = reader.next();
				String theWord = tree.getTerminalStr();
				if(!theWord.equals(strWord))
				{
					System.out.println(sLine);
					continue;
				}
				if(wordposdict.containsKey(theWord))
				{
					for(String thePOS : wordposdict.get(theWord))
					{
						List<Tree<String>> nonterminals = tree.getNonTerminals();
						for(Tree<String> theTree : nonterminals)
						{
							String lastMark = theTree.getLabel().substring(theTree.getLabel().length()-1);
							theTree.setLabel(thePOS + "#" + lastMark);
						}
						if(CFGWordStructureNormalize.checkWordStructure(tree))
						{
							output.println(tree.toCLTString());
						}
						else
						{
							System.out.println(thePOS + "_" +sLine);
						}
					}
				}
				
			}
			catch(Exception ex)
			{
				System.out.println(sLine);
				continue;
			}
			
		}
		
		in.close();
		output.close();

	}

}
