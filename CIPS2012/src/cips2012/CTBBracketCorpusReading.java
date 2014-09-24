package cips2012;

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

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class CTBBracketCorpusReading {

	/**
	 * @param args
	 */
	public static int[] fileId2TrDeTe;

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String inputFolder = args[0];
		String outputFolder = args[1];
		String configFile = args[2];
		boolean bInputUTF8 = true;
		if (args.length > 3 && args[3].equals("ANSI")) {
			bInputUTF8 = false;
		}
		File file = new File(inputFolder);
		//File filepos = new File(inputFolderPOS);
		String trainCorpusFileName = String.format("%s\\train.corpus",
				outputFolder);
		String devCorpusFileName = String
				.format("%s\\dev.corpus", outputFolder);
		String testCorpusFileName = String.format("%s\\test.corpus",
				outputFolder);
		String otherCorpusFileName = String.format("%s\\other.corpus",
				outputFolder);
		PrintWriter[] output = new PrintWriter[4];
		output[0] = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(otherCorpusFileName), "UTF-8"));
		output[1] = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(trainCorpusFileName), "UTF-8"));
		output[2] = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(devCorpusFileName), "UTF-8"));
		output[3] = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(testCorpusFileName), "UTF-8"));
		output[0].println();
		output[1].println();
		output[2].println();
		output[3].println();

		String[] subFilenames = file.list();
		//String[] subFilenamespos = filepos.list();
		configure_ctb(configFile);
		//configure_ctb50();
		//configure_alltemp();
		int invalidCount = 0;
		for (String subFilename : subFilenames) {
			String entirePath = String.format("%s\\%s", inputFolder,
					subFilename);
			int underlineIndex = subFilename.indexOf("_");
			int dotIndex = subFilename.lastIndexOf(".");
			String fileNameIdstr = subFilename.substring(underlineIndex + 1,
					dotIndex);
			// if(fileNameIdstr.)
			int fileNameId = -1;
			try {
				fileNameId = Integer.parseInt(fileNameIdstr);
			} catch (Exception x) {
				continue;
			}
			if (fileNameId <= 0 || fileNameId >= 10000) {
				System.out.println(String
						.format("Invalid File %s", subFilename));
				continue;
			}
			int saveFileId = fileId2TrDeTe[fileNameId];
			

			if (saveFileId == 0 || saveFileId == 1 || saveFileId == 2
					|| saveFileId == 3) {
				
				List<Tree<String>> contentItems = new ArrayList<Tree<String>>();
				invalidCount += getBracketContent(entirePath, contentItems,
						bInputUTF8);

				// output[saveFileId].println(fileNameIdstr);
				for (Tree<String> curTree : contentItems) {

					/*
					Tree<String> tree = PennTreeReader.parseEasy(oneLine.trim());
					if(tree == null)
					{
						invalidCount--;
						continue;
					}
					String line1 = oneLine.replaceAll("\\s+", "");
					line1 = line1.substring(1,line1.length()-1);
					String line2 = tree.getChild(0).toString().replaceAll("\\s+", "");
					if(!line1.equals(line2))
					{
						System.out.println(oneLine);
					}
					if (tree.getLabel().equalsIgnoreCase("root")) {
						tree = tree.getChild(0);
					}*/
					/*
					String outputstr = oneLine.trim().replaceAll("\\s+", " ");
					outputstr.replaceAll("  ", " ");
					
					StringReader sr = new StringReader(outputstr);
					Tree<String> theTree = new Tree<String>("ROOT");
					PennTreeReader reader = new PennTreeReader(sr);
					List<Tree<String>> children = new ArrayList<Tree<String>>();
					while (reader.hasNext()) {
						Tree<String> curTree = reader.next();
						output[saveFileId].println(curTree.toString());
						while(curTree.getLabel().equalsIgnoreCase("root"))curTree = curTree.getChild(0);
						children.add(curTree);
					}
					if(children.size() == 1)
					{
						theTree.setChildren(children);
					}
					else
					{
						Tree<String> childTree = new Tree<String>("IP");
						childTree.setChildren(children);
						List<Tree<String>> newChildChildren = new ArrayList<Tree<String>>();
						newChildChildren.add(childTree);
						theTree.setChildren(newChildChildren);
					}*/
					
					//output[saveFileId].println(theTree.toString());
					output[saveFileId].println(curTree.toString());
					output[saveFileId].flush();

				}

			} else {
				//System.out.println(String
				//		.format("Invalid File %s", subFilename));
				continue;
			}
		}
		output[0].println();
		output[1].println();
		output[2].println();
		output[3].println();
		output[0].close();
		output[1].close();
		output[2].close();
		output[3].close();
		System.out.println(String.format("%d", invalidCount));
	}
	

	
	public static int getBracketContent(String xmlFile, 
			List<Tree<String>> listContents, boolean bInputUTF8) throws Exception {
		
		
		BufferedReader reader = null;
		if (bInputUTF8) {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(xmlFile), "UTF-8"));
		} else {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(xmlFile), "GBK"));
		}
		List<String> oldlistContents = new ArrayList<String>();
		String sLine = null;
		
		//boolean bShouldAppend = false;
		String paragraph = "";
		while ((sLine = reader.readLine()) != null) {
			String sTempLine = sLine.trim();
			//if(sTempLine.startsWith("<S ID=") || sTempLine.startsWith("</S>"))
			//{
			//	listContents.add("");
			//}
			if(sTempLine.equals("") || sTempLine.startsWith("<") || sTempLine.endsWith(">"))
			{
				continue;
			}

			paragraph = paragraph + " " + sTempLine;
			
		}	
		//String paragraph = "";
		//for(String theLine : oldlistContents)
		//{
		//	paragraph = paragraph + " " + theLine;
		//}
		//for(int idx = 0; idx < oldlistContents.size();idx++)
		{
			//String oneTree = oldlistContents.get(idx);
			
			try
			{
				PennTreeReader treereader = new PennTreeReader(new StringReader(paragraph));
				int curshift = 0;
				while(treereader.hasNext())
				{
					Tree<String> curTree = treereader.next();
					
					//curTree.removeEmptyNodes();
					//curTree.removeUnaryChains();
					//curTree.removeDuplicate();				
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
					
					listContents.add(curTree);	
					wordposss = wordposss.trim();					
				}
			}
			catch(Exception ex)
			{
				System.out.println("invalid file : " + xmlFile);
				return 1;
			}
		}
		
		reader.close();
		return 0;
	}

	public static void configure_alltemp()
	{
		fileId2TrDeTe = new int[10000];
		for(int i = 1; i < 10000; i++)fileId2TrDeTe[i] = 1;

		for (int i = 4300; i <= 4360; i++) {
			fileId2TrDeTe[i] = 3; // -1 denote the file of no use
		}
		for (int i = 4361; i <= 4411; i++) {
			fileId2TrDeTe[i] = 2; // -1 denote the file of no use
		}
	}

	public static void configure_ctb50()
	{
		fileId2TrDeTe = new int[10000];
		for(int i = 1; i < 10000; i++)fileId2TrDeTe[i] = -1;
		for (int i = 1; i <= 270; i++) {
			fileId2TrDeTe[i] = 1; // -1 denote the file of no use
		}
		for (int i = 271; i <= 300; i++) {
			fileId2TrDeTe[i] = 3; // -1 denote the file of no use
		}
		for (int i = 301; i <= 325; i++) {
			fileId2TrDeTe[i] = 2; // -1 denote the file of no use
		}
		for (int i = 400; i <= 931; i++) {
			fileId2TrDeTe[i] = 1; // -1 denote the file of no use
		}
		for (int i = 1001; i <= 1151; i++) {
			fileId2TrDeTe[i] = 1; // -1 denote the file of no use
		}
	}
	public static void configure_ctb(String configureFile) throws Exception {
		fileId2TrDeTe = new int[10000];
		for (int i = 0; i < 10000; i++) {
			fileId2TrDeTe[i] = -1; // -1 denote the file of no use
		}
		boolean bContainTrain = false;
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(configureFile), "UTF-8"));
		String sLine = null;
		int idmark = 0;
		while ((sLine = reader.readLine()) != null) {
			if (sLine.trim().equals(""))
				continue;
			if (sLine.endsWith("dev")) {
				idmark = 2;
			} else if (sLine.endsWith("test")) {
				idmark = 3;
			} else if (sLine.endsWith("train")) {
				idmark = 1;
				bContainTrain=true;
			} else {
				int underlineIndex = sLine.indexOf("-");
				int fileNameIdStart = 0;
				if (underlineIndex == -1) {
					fileNameIdStart = Integer.parseInt(sLine.trim());
				} else {
					String fileNameIdstrStart = sLine.substring(0,
							underlineIndex).trim();
					fileNameIdStart = Integer.parseInt(fileNameIdstrStart);
				}

				int fileNameIdEnd = fileNameIdStart;
				if (underlineIndex != -1) {
					String fileNameIdstrEnd = sLine.substring(
							underlineIndex + 1).trim();
					fileNameIdEnd = Integer.parseInt(fileNameIdstrEnd);
				}
				for (int fileNameId = fileNameIdStart; fileNameId <= fileNameIdEnd; fileNameId++) {
					if (fileId2TrDeTe[fileNameId] == -1) {
						fileId2TrDeTe[fileNameId] = idmark;
					} else {
						fileId2TrDeTe[fileNameId] = idmark;
					}
				}
			}
		}
		reader.close();
		if(!bContainTrain)
		{
			for (int i = 0; i < 10000; i++) {
				if(fileId2TrDeTe[i] == -1) // -1 denote the file of no use
				{
					fileId2TrDeTe[i] = 1;
				}
			}
		}

	}
}
