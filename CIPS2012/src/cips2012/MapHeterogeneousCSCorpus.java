package cips2012;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class MapHeterogeneousCSCorpus {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		File file = new File(args[0]); // guided corpus
		File file2 = new File(args[1]); // target corpus
		File file3 = new File(args[2]); // output file


		BufferedReader bf1=new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
		BufferedReader bf2=new BufferedReader(new InputStreamReader(new FileInputStream(file2),"UTF-8"));
		
		
		String parse1=null, parse2=null;
		Map<String, TreeMap<String, Integer>> treemap=new TreeMap<String, TreeMap<String, Integer>>();
		CKYChartParser cky = new CKYChartParser();
		while((parse1=bf1.readLine())!=null && (parse2=bf2.readLine())!=null)
		{	
			if(parse1.trim().equals("(())") || parse2.trim().equals("(())"))continue;
			
			PennTreeReader reader = new PennTreeReader(
					new StringReader(parse1.trim()));
			Tree<String> tree = reader.next();
			
						
			Tree<String> subTree1 = tree;
			while(subTree1.getLabel().equalsIgnoreCase("root") 
				|| subTree1.getLabel().equalsIgnoreCase("top")
					)
			{
				subTree1 = subTree1.getChild(0);
			}
			
			reader = new PennTreeReader(
					new StringReader(parse2.trim()));
			tree = reader.next();
			
						
			Tree<String> subTree2 = tree;
			while(subTree2.getLabel().equalsIgnoreCase("root") 
				|| subTree2.getLabel().equalsIgnoreCase("top")
					)
			{
				subTree2 = subTree2.getChild(0);
			}
			
			cky.convert1Tree(subTree1);
			cky.convert1Tree(subTree2);
			
			getCSMap(subTree1, subTree2, treemap);
				
		}
		bf1.close();bf2.close();
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file3), "UTF-8"),false);
		
		for(String key1 : treemap.keySet())
		{
			for(String key2 : treemap.get(key1).keySet())
			{
				int key1mapkey2Count = treemap.get(key1).get(key2);
				output.println(String.format("(%s)\t(%s)\t%d", key1, key2, key1mapkey2Count));
			}
		}
		
		output.close();

	}
	
	
	
	public static void getCSMap(Tree<String> tree1, Tree<String> tree2,
			Map<String, TreeMap<String, Integer>> treemap)
	{
		List<String> words1 = tree1.getTerminalYield();
		List<String> words2 = tree1.getTerminalYield();
		String sentence1 = "";
		for(String curWord : words1)
		{
			sentence1 = sentence1 + " " + curWord;
		}
		
		String sentence2 = "";
		for(String curWord : words2)
		{
			sentence2 = sentence2 + " " + curWord;
		}
		
		if(!sentence2.trim().equals(sentence1.trim()))
		{
			return;
		}
		tree1.annotateSubTrees();
		tree2.annotateSubTrees();
		
		Map<String, String> splitRule1 = new TreeMap<String, String>();
		if(!getCSMapTop(tree1, splitRule1)) return;
		
		Map<String, String> splitRule2 = new TreeMap<String, String>();
		if(!getCSMapTop(tree2, splitRule2)) return;
		
		for(String posInfo : splitRule1.keySet())
		{
			int firstTabIndex = splitRule1.get(posInfo).indexOf("\t");
			int splitIndex1 = Integer.parseInt(splitRule1.get(posInfo).substring(0,firstTabIndex));
			String key1 = splitRule1.get(posInfo).substring(firstTabIndex+1);
			String key2  = "#null#";
			if(splitRule2.containsKey(posInfo))
			{
				firstTabIndex = splitRule2.get(posInfo).indexOf("\t");
				int splitIndex2 = Integer.parseInt(splitRule2.get(posInfo).substring(0,firstTabIndex));
				key2 = splitRule2.get(posInfo).substring(firstTabIndex+1);
				if(splitIndex2 != splitIndex1)
				{
					key2 = key2 + "\t" + "unmatch";
				}
			}
			key1 = key1.trim();
			key2 = key2.trim();
			
			if(!treemap.containsKey(key1))
			{
				treemap.put(key1, new TreeMap<String, Integer>());
			}
			
			Map<String, Integer> key1map = treemap.get(key1);
			if(!key1map.containsKey(key2))
			{
				key1map.put(key2, 0);
			}
			
			key1map.put(key2, key1map.get(key2)+1);
		}
		
		
	}
	
	public static boolean getCSMapTop(Tree<String> tree,
			Map<String, String> splitRule)
	{
		if(tree.isLeaf()) return true;
		List<Tree<String>> children = tree.getChildren();
		
		//ֻ�����һ��Ҷ��ǰ���Ǹ��ڵ����ΪA->B������ڵ��ΪA->BC
		if(children.size() == 1)
		{
			int start = tree.smaller;
			int end = tree.bigger;
			int mid = start;
			String thekey = String.format("p[%d,%d]", start, end);
			String secondKey = String.format("%d\t", mid) + tree.getLabel();
			if(splitRule.containsKey(thekey))
			{
				System.out.println("split duplicated!");
				return false;
			}
			splitRule.put(thekey, secondKey);
			if(!getCSMapTop(children.get(0), splitRule)) return false;
		}
		else if (children.size() == 2)
		{
			int start = tree.smaller;
			int end = tree.bigger;
			int mid = children.get(0).bigger;
			String thekey = String.format("n[%d,%d]", start, end);
			String secondKey = String.format("%d\t", mid) + tree.getLabel() 
					+ "\t"+ children.get(0).getLabel() + "\t"+ children.get(1).getLabel();
			if(splitRule.containsKey(thekey))
			{
				System.out.println("split duplicated!");
				return false;
			}
			splitRule.put(thekey, secondKey);
			if(!getCSMapTop(children.get(0), splitRule)) return false;
			if(!getCSMapTop(children.get(1), splitRule)) return false;
		}
		else
		{
			System.out.println("childern number is bigger than three!");
			return false;
		}
		return true;
	}

}
