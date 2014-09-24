package process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.List;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class ExtractCSYields {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		if (args.length > 2 && args[2].equals("pos")) {
			File file = new File(args[0]);
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");

			PrintWriter writer = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(args[1]), "UTF-8"), false);
			//PrintWriter writer_nor = new PrintWriter(new OutputStreamWriter(
			//		new FileOutputStream(args[0] + ".normalize"), "UTF-8"), false);
			BufferedReader bf = null;
			bf = new BufferedReader(isr);
			String sLine = null;
			int totalWords = 0;
			int totalSentences = 0;
			while ((sLine = bf.readLine()) != null) {
				if(sLine.indexOf("\t") != -1)
				{
					sLine = sLine.substring(sLine.indexOf("\t")+1).trim();
				}
				if(sLine.trim().replaceAll("\\s+", "").equals("(())")
					|| sLine.trim().length() < 4)
				{
					//writer_nor.println(sLine.trim());
					continue;
				}
				try
				{
					sLine = sLine.trim();
					PennTreeReader reader = new PennTreeReader(new StringReader(
							sLine.trim()));
					Tree<String> tree = reader.next();
					Tree<String> subTree = tree;
					while(subTree.getLabel().equalsIgnoreCase("root") 
						|| subTree.getLabel().equalsIgnoreCase("top")
							)
					{
						subTree = subTree.getChild(0);
					}
					subTree = tree;
					if(!tree.toString().replaceAll("\\s+", "").equals(sLine.trim().replaceAll("\\s+", "")))
					{
						System.out.println(totalSentences);
					}
					//writer_nor.println(tree.toString());
					tree.removeEmptyNodes();
					List<String> curWords = tree.getYield();
					List<String> curPoss = tree.getPreTerminalYield();
					if(curWords.size()== 0 || curPoss.size() != curWords.size())continue;
					String oneLine = curWords.get(0) + "_" + curPoss.get(0);
					for(int index = 1; index < curWords.size(); index++)
					{
						oneLine = oneLine + " " + curWords.get(index) + "_" + curPoss.get(index);
					}
					totalWords += curWords.size();
					totalSentences++;
					writer.println(oneLine.trim());
				}
				catch(Exception ex)
				{
					System.out.println(sLine.trim());
				}
			}
			writer.close();
			System.out.println(String.format("sent num : %d, word num: %d",  totalSentences, totalWords));
			bf.close();
			//writer_nor.close();
		} else {
			File file = new File(args[0]);
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");

			PrintWriter writer = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(args[1]), "UTF-8"), false);
			//PrintWriter writer_nor = new PrintWriter(new OutputStreamWriter(
			//		new FileOutputStream(args[0] + ".normalize"), "UTF-8"), false);
			BufferedReader bf = null;
			bf = new BufferedReader(isr);
			String sLine = null;
			while ((sLine = bf.readLine()) != null) {
				if(sLine.trim().replaceAll("\\s+", "").equals("(())"))
				{
					//writer_nor.println(sLine.trim());
					writer.println("");
					continue;
				}
				try{
					sLine = sLine.trim();
					PennTreeReader reader = new PennTreeReader(new StringReader(
							sLine.trim()));
					Tree<String> tree = reader.next();
					Tree<String> subTree = tree;
					while(subTree.getLabel().equalsIgnoreCase("root") 
						|| subTree.getLabel().equalsIgnoreCase("top")
							)
					{
						subTree = subTree.getChild(0);
					}
					subTree = tree;
					
					
					//writer_nor.println(tree.toString());
					List<String> curWords = tree.getYield();
					if(curWords.size()== 0)continue;
					String oneLine = curWords.get(0);
					for(int index = 1; index < curWords.size(); index++)
					{
						oneLine = oneLine + " " + curWords.get(index);
					}
	
					writer.println(oneLine.trim());
				}
				catch(Exception ex)
				{
					System.out.println(sLine.trim());
				}
			}
			writer.close();
			bf.close();
			//writer_nor.close();
		}

	}
}
