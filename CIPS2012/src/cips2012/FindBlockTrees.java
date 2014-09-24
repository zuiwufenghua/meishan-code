package cips2012;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class FindBlockTrees {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		InputStreamReader inputData = new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8");
		PennTreeReader treeReader = new PennTreeReader(inputData);		
		int iCount = 0;
		List<Tree<String>> alltrees = new ArrayList<Tree<String>>();
		List<Integer> sentIndex = new ArrayList<Integer>();
		List<Integer> sentFind = new ArrayList<Integer>();
		String allSentWordPOS = "";
		while (treeReader.hasNext()) {
			Tree<String> curTree = treeReader.next();
			alltrees.add(curTree);
			sentFind.add(0);
			
			List<Tree<String>> posTrees = curTree.getPreTerminals();
			String wordposss = "";
			
			for(Tree<String> onePOSTree : posTrees)
			{
				String curpos = onePOSTree.getLabel();
				int trimpos = curpos.indexOf("-");
				if(trimpos != -1)
				{
					curpos = curpos.substring(0, trimpos);
				}
				wordposss = wordposss + " " + onePOSTree.getChild(0).getLabel() + "_" + curpos;
			}
			wordposss = wordposss.trim();
			int newContSize = wordposss.length();
			for(int tidx = 0; tidx < newContSize; tidx++)
			{
				sentIndex.add(iCount);
			}
			
			sentIndex.add(iCount);			
			allSentWordPOS = allSentWordPOS + wordposss + " ";

			iCount++;
		}
		sentIndex.add(iCount);
		
		System.out.println(iCount);
		inputData.close();
		
		
		String inputFolder = args[1];		
		File file = new File(inputFolder);		
		String outputFolder = args[2];	
		String remaintrees = args[3];	
		boolean bInputUTF8 = true;
		if (args.length > 4 && args[4].equals("ANSI")) {
			bInputUTF8 = false;
		}
		
		
		String[] subFilenames = file.list();
		
		
		for (String subFilename : subFilenames) {
			String entirePath = String.format("%s\\%s", inputFolder,
					subFilename);
			int dotIndex = subFilename.lastIndexOf(".");
			String outputtempFile = subFilename.substring(0, dotIndex);
			String outputFile = String.format("%s\\%s", outputFolder,
					outputtempFile);
			String blockstr = getWordPOSContent(entirePath, bInputUTF8);
			getandwriteconstitient(outputFile,blockstr,alltrees, sentIndex, allSentWordPOS, sentFind );
		}
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(remaintrees), "UTF-8"));
		for(int idx = 0; idx < sentFind.size(); idx++)
		{
			if(sentFind.get(idx) == 0)
			{
				output.println(alltrees.get(idx));
			}
		}
		
		output.close();
	}
	
	
	
	public static String getWordPOSContent(String xmlFile, boolean bInputUTF8) throws Exception {
		String result = "";
		BufferedReader reader = null;
		if (bInputUTF8) {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(xmlFile), "UTF-8"));
		} else {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(xmlFile), "GBK"));
		}
		String sLine = null;
		
		while ((sLine = reader.readLine()) != null) {
			String sTempLine = sLine.trim();
			if(sTempLine.equals("") || sTempLine.startsWith("<") || sTempLine.endsWith(">"))
			{
				continue;
			}
			String[] wordpossunits = sTempLine.split("\\s+");
			String refinedLine = "";
			for(String oneWordPOS: wordpossunits)
			{
				if(oneWordPOS.endsWith("-NONE-"))continue;
				int lastsplit = oneWordPOS.lastIndexOf("_");
				int trimpos = oneWordPOS.indexOf("-", lastsplit);
				if(trimpos != -1)
				{
					oneWordPOS = oneWordPOS.substring(0, trimpos);
				}
				refinedLine = refinedLine + " " + oneWordPOS;
			}
			result = result + refinedLine.trim() + " ";
			
		}
		reader.close();
		return result.trim();		
	}

	
	public static boolean getandwriteconstitient(String outputfile, String bolckstr, 
			List<Tree<String>> alltrees, List<Integer> sentIndex, String allSentWordPOS, List<Integer> sentFind) throws Exception
	{

		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputfile), "UTF-8"));
		int sPosStart = allSentWordPOS.indexOf(bolckstr);
		if(sPosStart == -1) 
		{
			output.close();
			System.out.println("error: " + outputfile);
			return false;
		}
		int sPosEnd = sPosStart + bolckstr.length() + 1;
		int startTree =  sentIndex.get(sPosStart);
		int endTree = sentIndex.get(sPosEnd);
		
		for(int idx = startTree; idx < endTree; idx++)
		{
			output.println(alltrees.get(idx));
			sentFind.set(idx, 1);
		}
			
		output.close();		
		
		return true;
	}
}
