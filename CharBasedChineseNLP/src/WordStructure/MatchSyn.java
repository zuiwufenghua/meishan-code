package WordStructure;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class MatchSyn {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Map<String, String> wordtags = new HashMap<String, String>();
		Map<String, String> inputWords = new HashMap<String, String>();
		Map<String, String> inputTags = new HashMap<String, String>();
		
		LoadInputsBerkeleyInputFormat(args[0], wordtags, inputWords, inputTags);
		//List<Tree<String>> syns = new ArrayList<Tree<String>>();
		
		BufferedReader bf=new BufferedReader(new InputStreamReader(new FileInputStream(args[1]),"UTF-8"));
		String sLine = null;
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);
		int iCount = 1;
		while((sLine=bf.readLine())!=null )
		{	
			if(sLine.trim().length() < 2)continue;
			if(sLine.trim().equals(""))continue;
			
			final PennTreeReader reader = new PennTreeReader(
					new StringReader(sLine.trim()));
			Tree<String> tree = reader.next();
			Tree<String> subTree1 = tree;

			while (subTree1.getLabel().equalsIgnoreCase("root")
					|| subTree1.getLabel().equalsIgnoreCase("top")) {
				tree = tree.getChild(0);
				subTree1 = subTree1.getChild(0);
			}
			
			List<Tree<String>> preTerminals = subTree1.getPreTerminals();
			String newStrTree = preTerminals.get(0).getTerminalStr() + "_" + preTerminals.get(0).getLabel();
			for(int idx = 1; idx < preTerminals.size(); idx++)
			{
				newStrTree = newStrTree + " " + preTerminals.get(idx).getTerminalStr() + "_" + preTerminals.get(idx).getLabel();
			}
			newStrTree = newStrTree.trim();
			if(wordtags.containsKey(newStrTree))
			{
				output.println(wordtags.get(newStrTree) + "\t" + subTree1.toString());
			}
			else
			{
				System.out.println(String.format("%d\t%s", iCount, subTree1.toString()));
			}
			
			iCount++;
		}
		
		output.close();
		bf.close();

	}
	
	public static void LoadInputs(String inputFile, Map<String, String> wordtags, 
			Map<String, String> inputWords, Map<String, String> inputTags) throws Exception
	{
		BufferedReader bf=new BufferedReader(new InputStreamReader(new FileInputStream(inputFile),"UTF-8"));
		String sLine = null;
		
		while((sLine=bf.readLine())!=null )
		{	
			if(sLine.trim().length() < 2)continue;
			if(sLine.trim().equals(""))continue;
			String[] wordposs = sLine.trim().split("\\s+");
			
			String theWord = "";
			String thePos = "";
			for(int idx = 0; idx < wordposs.length; idx++)
			{
				theWord = theWord + wordposs[idx].substring(0,1);
				if(idx == 0)
				{
					thePos = wordposs[idx].substring(2,wordposs[idx].length()-2);
				}
			}
			
			String key = String.format("[%s][%s]", theWord, thePos);
			wordtags.put(sLine.trim(), key);
			inputWords.put(sLine.trim(), theWord);
			inputTags.put(sLine.trim(), thePos);			
		}
		
		bf.close();
	}
	
	
	public static void LoadInputsBerkeleyInputFormat(String inputFile, Map<String, String> wordtags, 
			Map<String, String> inputWords, Map<String, String> inputTags) throws Exception
	{
		BufferedReader bf=new BufferedReader(new InputStreamReader(new FileInputStream(inputFile),"UTF-8"));
		String line = null;		
		while ((line = bf.readLine()) != null){
			line = line.trim();
			if (line.equals(""))
				continue;
			String theWord = "";
			String thePOS = "";
			List<String> tmp = Arrays.asList(line.split("\\s+"));
			for(String curWordPOS : tmp)
			{
				int lastSplitIndex = curWordPOS.lastIndexOf("_");
				if(lastSplitIndex != 1)
				{
					System.out.println(line);
					continue;
				}
				int lastPOSSplitIndex = curWordPOS.lastIndexOf("#");
				if(lastPOSSplitIndex != curWordPOS.length()-2)
				{
					System.out.println(line);
					continue;
				}
				theWord = theWord + curWordPOS.substring(0,1);
				thePOS = curWordPOS.substring(2,lastPOSSplitIndex);
			}			
			String key = String.format("[%s][%s]", theWord, thePOS);
			wordtags.put(line.trim(), key);
			inputWords.put(line.trim(), theWord);
			inputTags.put(line.trim(), thePOS);			
		}
		
		bf.close();
	}

}
