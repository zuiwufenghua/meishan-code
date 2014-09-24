package process;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.*;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class DeleteErrorCons {

	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception {
		// TODO Auto-generated method stub
		File file = new File(args[0]);
		String[] subFilenames = file.list();
		for (String subFilename : subFilenames) {
			String inputFile = args[0] + File.separator + subFilename;
			String outputFile = args[1] + File.separator + subFilename;
			String output2File = args[1] + File.separator + subFilename+".needCheck";
			deleteAllInvalidTree(inputFile, outputFile, output2File);			
		}
		
		
	}
	
	public static void deleteAllInvalidTree(String inputFile, String outputFile, String output2File)throws Exception {
		System.out.println("Processing file " + inputFile + " ......");
		BufferedReader cfgreader = new BufferedReader(new InputStreamReader(
				new FileInputStream(inputFile), "UTF-8"));
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8"));
		PrintWriter writer_invalid = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(output2File), "UTF-8"));
		List<Tree<String>> allTrees = new  ArrayList<Tree<String>>();
		String sLine = null;
		int nLine = 0;
		while ((sLine = cfgreader.readLine()) != null) {
			nLine++;
			if(nLine%1000 == 0)
			{
				System.out.println(nLine);
				System.out.flush();
			}
			if(sLine.trim().indexOf(" ") == -1)
			{
				writer_invalid.println(sLine.trim());
				continue;
			}
			Tree<String> tree = null;
			int count = 0;
			try{
				PennTreeReader reader = new PennTreeReader(new StringReader(
						sLine.trim()));
							
				while (reader.hasNext()) {
					tree = reader.next();
					count++;
				}
			}
			catch(Exception ex)
			{
				tree = null; count = 0;
			}
			if(tree == null || count != 1)
			{
				writer_invalid.println(sLine.trim());
				continue;
			}
			/*
			String topLabel = tree.getLabel();
			int childNum = tree.size();*/
			if(tree.getLabel().equals("ROOT"))
			{
				tree.setLabel("");
			}
			
			String s1 = tree.toString().trim().substring(1,tree.toString().trim().length()-1).trim();
			String s2 = sLine.trim().substring(sLine.trim().indexOf(" "), sLine.trim().length()-1).trim();
			
			if(s1.indexOf(s2) == -1 )
			{
				writer_invalid.println(sLine.trim());
			}
			else
			{
				writer.println(sLine.trim());
			}
			
			
		}
		System.out.println(nLine);
		cfgreader.close();
		writer.close();
		writer_invalid.close();
	}

}
