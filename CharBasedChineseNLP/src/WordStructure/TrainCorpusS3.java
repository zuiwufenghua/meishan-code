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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

//�� �ս�Word A#Bת����(A=>B)
public class TrainCorpusS3 {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		File file = new File(args[0]); // guided corpus
		File file2 = new File(args[1]); // target corpus

		BufferedReader bf1=new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
				
		String parse1=null;
		Map<String, Tree<String>> treemap=new TreeMap<String, Tree<String>>();
		Map<String, Double> treemapScore =new TreeMap<String, Double>();

		
		while((parse1=bf1.readLine())!=null )
		{	
			if(parse1.trim().startsWith("//"))continue;
			int brackPosition = parse1.indexOf("(");
			double score = 0.0;
			try
			{
				score = Double.parseDouble(parse1.substring(0, brackPosition).trim());
			}
			catch (Exception e)
			{
				score = 10000.0;
			}
			if(brackPosition == -1)continue;
			parse1 = parse1.substring(brackPosition);
			PennTreeReader reader = new PennTreeReader(
					new StringReader(parse1.trim()));
			Tree<String> tree = reader.next();	
			List<String> curChars = tree.getTerminalYield();
			String curWord = "";
			if(curChars.size() > 0)
			{
				for(int idx = 0; idx < curChars.size(); idx++)
				{
					curWord = curWord + " " + curChars.get(idx);
				}
				curWord = curWord.trim();
			}
			if(curWord.equals(""))continue;
			Tree<String> subTree1 = tree.getChild(0);
			
			while(subTree1.getLabel().equalsIgnoreCase("root") 
				|| subTree1.getLabel().equalsIgnoreCase("top")
					)
			{
				tree = tree.getChild(0);
				subTree1 = subTree1.getChild(0);
			}
			
			tree.setLabel("TOP");
			if(!treemap.containsKey(curWord))
			{
				treemap.put(curWord, tree);
				treemapScore.put(curWord, score);
			}
			else if(!tree.toString().equals(treemap.get(curWord).toString()))
			{
				System.out.println(tree.toString());
				System.out.println(treemap.get(curWord).toString());
				System.out.println();
			}
			else
			{
				System.out.println(tree.toString());
			}
			
			
							
		}
		bf1.close();
		
		
		BufferedReader bf2=new BufferedReader(new InputStreamReader(new FileInputStream(file2),"UTF-8"));
		
		PrintWriter output1 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[2]), "UTF-8"),false);
		
		
		Set<String> wordPOS = new HashSet<String>();
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
				

				String oneline = "";
				for(int idxc = 0; idxc < curWordChars.length; idxc++)
				{
					oneline = oneline + " " + curWordChars[idxc] + "#" + curPOS;
				}
				oneline = oneline.trim();
				wordPOS.add(oneline);
				
				if(!treemap.containsKey(oneline))
				{
					output1.println(oneline);
				}
				
			}
		}
		
		
		PrintWriter output2 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[3]), "UTF-8"),false);
		for(String oneLineKey: treemap.keySet())
		{
			if(!wordPOS.contains(oneLineKey))
			{
				//output2.println("E#" + treemap.get(oneLineKey));
				//output2.flush();
			}
			else
			{
				String[] curUnits = oneLineKey.split(" ");
				String theWord = "";
				for(int curIdx = 0; curIdx < curUnits.length; curIdx++)
				{
					theWord = theWord + curUnits[curIdx].substring(0,1);
				}
				
				output2.println(String.format("%f\t%d\t%s\t%s\t%s", treemapScore.get(oneLineKey),
						theWord.length(), theWord, oneLineKey, treemap.get(oneLineKey)));
				output2.flush();
			}
		}
		
		bf2.close();
		output1.close();
		output2.close();
		
	}

}
