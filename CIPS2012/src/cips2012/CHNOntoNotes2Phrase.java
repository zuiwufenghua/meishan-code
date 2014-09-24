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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;


public class CHNOntoNotes2Phrase {
	
	private  static class WordElem {
		String word;//����
		List<String> attirbutes;//����ֵ
		String propMark;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		List<String> allFiles = new ArrayList<String>();
		getAllFilesInFolder(args[0], allFiles);
		Set<String> markOfNoUse = new HashSet<String>();
		int processedFileNum = 0;
		for(String oneFile : allFiles)
		{
			if(oneFile.trim().endsWith(".onf"))
			{
				processOneFile(oneFile, args[1], markOfNoUse);
				processedFileNum++;
				if(processedFileNum%2000 == 0)
				{
					System.out.println(processedFileNum);
				}
			}
		}
		System.out.println(processedFileNum);
		System.out.println("Mark we haven't used yet:");
		for(String curMark : markOfNoUse)
		{
			System.out.println(curMark);
		}
		
	}
	public static void getAllFilesInFolder(String inputFolder, List<String> allFiles)
	{
		File file = new File(inputFolder); 
		if(file.exists()) 
		{
			File[] files = file.listFiles();
			 for(int i = 0; i < files.length; i++)  
             {  
                 if(files[i].isFile())  
                 {  
                	 allFiles.add(files[i].getAbsolutePath());  
                 }  
                 else if(files[i].isDirectory())  
                 {  
                	 getAllFilesInFolder(files[i].getAbsolutePath(), allFiles);  
                 }  
             }  
		}
	}
	
	public static void processOneFile(String inputFile, String outFolder, Set<String> markOfNoUse) throws Exception
	{		
		File file = new File(inputFile);
		String finalName = file.getName();
		
		String writeFilename = outFolder + File.separator + finalName;
		File writeFile = new File(writeFilename);
		int dupCount = 0;
		while(writeFile.exists())
		{
			dupCount++;
			writeFilename = String.format("%s%s%s.%d", outFolder, File.separator, finalName, dupCount);
			writeFile = new File(writeFilename);
		}
		
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(writeFilename), "UTF-8"), false);
		
		List<Tree<String>> parsers = new ArrayList<Tree<String>>();
		List<List<WordElem>> wordDess = new ArrayList<List<WordElem>>();
		readONFFile(inputFile, parsers, wordDess);
		int parsersNum = parsers.size();
		if(wordDess.size() != parsersNum) 
		{
			System.out.println("sentence number in parsers and wordDess not match");
		}
		
		for(int idx = 0; idx < parsersNum; idx++)
		{
			refinePhraseStructure(String.format("file:%s, %d", writeFile.getName(), idx), parsers.get(idx), wordDess.get(idx), 
					 markOfNoUse);
			writer.println(parsers.get(idx).toString());
		}
		
		writer.close();
		
	}
	
	public static void readONFFile(String inputFile, List<Tree<String>> parsers, 
			List<List<WordElem>> wordDess) throws Exception
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(inputFile), "UTF-8"));
		String sLine = null;
		while ((sLine = reader.readLine()) != null) {
			String sTempLine = sLine.trim();
			if(sTempLine.equals("Tree:"))
			{
				sLine = reader.readLine();
				if(!sLine.trim().startsWith("--"))
				{
					System.out.println("Error file: " + inputFile);
					break;
				}
				String csLine = "";
				while ((sLine = reader.readLine()) != null)
				{
					if(sLine.trim().equals("Leaves:"))
					{
						break;
					}
					csLine = csLine + " " + sLine.trim();
				}
				csLine = csLine.trim();
				Tree<String> tree = PennTreeReader.parseEasy(csLine.trim());
				if(tree == null)
				{
					System.out.println("Error file: " + inputFile);
					System.out.println("Error sentences: " + csLine.trim());
					break;
				}
				if (tree.getLabel().equalsIgnoreCase("root")) {
					tree = tree.getChild(0);
				}
				
				sLine = reader.readLine();
				if(sLine == null || !sLine.trim().startsWith("--"))
				{
					System.out.println("Error file: " + inputFile);
					break;
				}
				
				List<String> curWords = tree.getTerminalYield();
				
				boolean bMatched = true;
				String lastLine = reader.readLine();
				while(lastLine != null && lastLine.trim().equals(""))
				{
					lastLine = reader.readLine();
				}
				if(lastLine == null)
				{
					bMatched = false;
				}
				List<WordElem> curWordElems = new ArrayList<WordElem>();
				for(int index = 0; bMatched && index < curWords.size(); index++)
				{
					String[] twoUnits = lastLine.trim().split("\\s+");
					if(twoUnits.length == 2 
						&& String.format("%d", index).equals(twoUnits[0])
						&& curWords.get(index).trim().equals(twoUnits[1].trim()))
					{
						WordElem tempElem = new WordElem();
						tempElem.word = twoUnits[1];
						tempElem.attirbutes = new ArrayList<String>();
						tempElem.propMark = "FAKEPROP";
						//lastLine = reader.readLine();
						
						while((lastLine = reader.readLine()) != null)
						{					
							if(lastLine.trim().equals(""))continue;			
							if(lastLine.trim().substring(0,1).equals(lastLine.substring(0,1)))
							{
								if(index != curWords.size()-1)bMatched = false;
								break;
							}
							twoUnits = lastLine.trim().split("\\s+");
							if(String.format("%d", index+1).equals(twoUnits[0]))
							{
								break;
							}
							tempElem.attirbutes.add(lastLine.trim());
							String curAttribute = lastLine.trim();
							if(curAttribute.startsWith("prop"))
							{
								//String[] fineLabels = curUnits[1].trim().split(".");
								String[] subLabels = curAttribute.split("\\s+");
								if(subLabels.length == 2) tempElem.propMark =   subLabels[1];
							}
							
						}
						if(bMatched)
						{
							Collections.sort(tempElem.attirbutes);
							curWordElems.add(tempElem);
						}
						else
						{
							break;
						}
					}
					else
					{
						bMatched = false;
						break;
					}
				}
				
				if(bMatched)
				{
					parsers.add(tree);
					wordDess.add(curWordElems);
				}
				
			}
		}
		reader.close();
	}
	
	public  static void refinePhraseStructure(String errorLog, Tree<String> theTree, List<WordElem> theWordElems, 
			Set<String> markOfNoUse)
	{
		theTree.initParent();
		List<Tree<String>> prevTerminals = theTree.getPreTerminals();
		int termNum = prevTerminals.size();
		if(theWordElems.size() != termNum)
		{
			System.out.println("Error:" + errorLog + ". " +theTree.toString());	
			theTree.setLabel(theTree.getLabel() + "#E#");
			return;
		}
		
		for(int index = 0; index < termNum; index++)
		{
			for(String oneAttribute : theWordElems.get(index).attirbutes)
			{
				prevTerminals = theTree.getPreTerminals();
				String[] curUnits = oneAttribute.toLowerCase().trim().split("\\s+");
				if(curUnits.length == 0)continue;
				if(curUnits[0].startsWith("coref"))
				{
					if(curUnits.length < 4)
					{
						System.out.println("Error attribute: "  + errorLog + ". " + oneAttribute);
						theTree.setLabel(theTree.getLabel() + "#E#");
						continue;
					}
					String curType = curUnits[1];
					int rangId = curUnits.length-1;
					int subLength = -1;
					for(;rangId >= 3; rangId--)
					{
						subLength = bRangeFormart(curUnits[rangId]);
						if(subLength != -1)
						{
							break;
						}
								
					}
					if(subLength == -1)
					{
						System.out.println("Error attribute: " + errorLog + ". "  + oneAttribute);
						theTree.setLabel(theTree.getLabel() + "#E#");
						continue;
					}
					
					for(int sdx = 2; sdx < rangId-1; sdx++)curType = curType + curUnits[sdx];
					
					String curMark = curUnits[rangId-1];					
					String curRange = curUnits[rangId].substring(0, subLength);			
					String[] rangeIds = curRange.trim().split("-");
					int id1 = -1, id2 = -1;
					try
					{
						id1 = Integer.parseInt(rangeIds[0]);
						id2 = Integer.parseInt(rangeIds[1]);
					}
					catch(Exception e)
					{
						System.out.println("Error attribute: " + errorLog + ". "  + oneAttribute);
						theTree.setLabel(theTree.getLabel() + "#E#");
						continue;
					}
					if(id1 > id2)
					{
						int tempId = id1; id1 = id2; id2 = id1;
					}
					/*
					int numWords = Math.abs(id1-id2)+1;
					if(curUnits.length != 4 + numWords+extraLength)
					{
						System.out.println("Error attribute: " + errorLog + ". "  + oneAttribute);
						theTree.setLabel(theTree.getLabel() + "#E#");
						continue;
					}
					*/
					List<String> curInvolveWords = new ArrayList<String>();
					for(int idx = id1; idx < id2+1; idx++)
					{				
						curInvolveWords.add(prevTerminals.get(idx).getChild(0).getLabel());
					}
					
					Tree<String> curYield = prevTerminals.get(index);
					Tree<String> perfectNonTerminal = perfectCover(curYield, curInvolveWords);
					if(perfectNonTerminal == null)
					{
						System.out.println("Error attribute: " + errorLog + ". "  + oneAttribute + curYield.toString());
						theTree.setLabel(theTree.getLabel() + "#E#");
						continue;
					}
					//Tree<String> perfectNonTerminalParent = perfectNonTerminal.parent;
					Tree<String> newTmpTree = new Tree<String>(perfectNonTerminal.getLabel());
					newTmpTree.setChildren(perfectNonTerminal.getChildren());
					for(Tree<String> curChildTree : newTmpTree.getChildren())
					{
						curChildTree.parent = newTmpTree;
					}
					newTmpTree.parent = perfectNonTerminal;
					List<Tree<String>> curTmpChildren = new ArrayList<Tree<String>>();
					curTmpChildren.add(newTmpTree);
					perfectNonTerminal.setChildren(curTmpChildren);
					String simpMark = curMark;
					if(simpMark.length() > 6) simpMark = curMark.substring(0,6);
					String simpType = curType;
					if(simpMark.length() > 6) simpType = curType.substring(0,6);
					perfectNonTerminal.setLabel("[coref."+simpType+"."+simpMark + "]");					
				}
				else if(curUnits[0].startsWith("name"))
				{
					//�ݲ�����				
				}
				else if(curUnits[0].startsWith("sense"))
				{
					if(curUnits.length != 2)
					{
						System.out.println("Error attribute: " + errorLog + ". "  + oneAttribute);
						theTree.setLabel(theTree.getLabel() + "#E#");
						continue;
					}
					String[] fineLabels = curUnits[1].trim().split("-");
					Tree<String> curYield = prevTerminals.get(index);
					Tree<String> curYieldChild = curYield.getChild(0);
					int wordLength = fineLabels[0].length();
					if(!curYieldChild.getLabel().substring(0,wordLength).equalsIgnoreCase(fineLabels[0]))
					{
						System.out.println("Error attribute: " + errorLog + ". "  + oneAttribute);
						theTree.setLabel(theTree.getLabel() + "#E#");
						continue;
					}
					curYieldChild.setLabel(curYieldChild.getLabel() + "[sense-" + fineLabels[1] +"]");
					
				}
				else if(curUnits[0].startsWith("prop"))
				{
					if(curUnits.length != 2)
					{
						System.out.println("Error attribute: " + errorLog + ". "  + oneAttribute);
						theTree.setLabel(theTree.getLabel() + "#E#");
						continue;
					}
					//String[] fineLabels = curUnits[1].trim().split(".");
					int lastIndex =  curUnits[1].lastIndexOf(".");
					String curYieldWord = curUnits[1].substring(0, lastIndex).trim();
					String curYieldSeq =  curUnits[1].substring(lastIndex+1).trim();
					//for(int i)
					Tree<String> curYield = prevTerminals.get(index);
					Tree<String> curYieldChild = curYield.getChild(0);
					int wordLength = curYieldWord.length();
					if(curYieldChild.getLabel().length() < wordLength || !curYieldChild.getLabel().substring(0,wordLength).equalsIgnoreCase(curYieldWord))
					{
						System.out.println("Error attribute: " + errorLog + ". "  + oneAttribute);
						theTree.setLabel(theTree.getLabel() + "#E#");
						continue;
					}
					curYieldChild.setLabel(curYieldChild.getLabel() + "[prop" + curYieldSeq +"]");
				}
				else if(curUnits[0].startsWith("arg"))
				{
					if(theWordElems.get(index).propMark.equals("FAKEPROP"))
					{
						System.out.println("Error attribute: " + errorLog + ". "  + oneAttribute);
						theTree.setLabel(theTree.getLabel() + "#E#");
						continue;
					}
					if(curUnits.length < 2)
					{
						System.out.println("Error attribute: " + errorLog + ". "  + oneAttribute);
						theTree.setLabel(theTree.getLabel() + "#E#");
						continue;
					}
					if(curUnits[0].length() == 4)
					{
						Tree<String> curYield = prevTerminals.get(index);
						Tree<String> curYieldChild = curYield.getChild(0);					
						curYieldChild.setLabel(curYieldChild.getLabel() + "["+curUnits[0] +"]");
					}
					
					for(int idx = 1; idx < curUnits.length-1; idx++)
					{
						if(curUnits[idx].equals("->"))
						{
							String[] rangeIds = curUnits[idx+1].replaceAll(",", "").trim().split(":");
							int id1 = -1, id2 = -1;
							try
							{
								id1 = Integer.parseInt(rangeIds[0]);
								id2 = Integer.parseInt(rangeIds[1]);
							}
							catch(Exception e)
							{
								System.out.println("Error attribute: " + errorLog + ". "  + oneAttribute);
								theTree.setLabel(theTree.getLabel() + "#E#");
								continue;
							}
							Tree<String> curYield = prevTerminals.get(id1);
							Tree<String> perfectNonTerminal = getParent(curYield, id2);
							//Tree<String> perfectNonTerminalParent = perfectNonTerminal.parent;
							Tree<String> newTmpTree = new Tree<String>(perfectNonTerminal.getLabel());
							newTmpTree.setChildren(perfectNonTerminal.getChildren());
							for(Tree<String> curChildTree : newTmpTree.getChildren())
							{
								curChildTree.parent = newTmpTree;
							}
							newTmpTree.parent = perfectNonTerminal;
							List<Tree<String>> curTmpChildren = new ArrayList<Tree<String>>();
							curTmpChildren.add(newTmpTree);
							perfectNonTerminal.setChildren(curTmpChildren);
							perfectNonTerminal.setLabel("["+theWordElems.get(index).propMark+"-"+curUnits[0].toUpperCase() + "]");
						}				
					}
				}
				else
				{
					markOfNoUse.add(curUnits[0]);
				}
			}
		}
	}
	
	public static Tree<String> perfectCover(Tree<String> bottom, List<String> words)
	{
		List<String> curYields = bottom.getYield();
		Tree<String> curTree = bottom;
		while(curTree != null && curYields.size() < words.size())
		{
			curTree = curTree.parent;
			curYields = curTree.getYield();
		}
		
		if(curYields.size() == words.size())
		{
			boolean bMatch = true;
			for(int index = 0; index < curYields.size(); index++)
			{
				int wordLength = words.get(index).length();
				if(!curYields.get(index).substring(0,wordLength).equalsIgnoreCase(words.get(index)))
				{
					bMatch = false;
					break;
				}
			}
			if(bMatch)
			{
				return curTree;
			}
		}
			
		return null;
	}
	
	public static Tree<String> getParent(Tree<String> bottom, int height)
	{
		int curHeight = 0;
		Tree<String> curTree = bottom;
		while(curTree != null && curHeight < height)
		{
			curTree = curTree.parent;
			if(curTree.getLabel().startsWith("[") && curTree.getLabel().endsWith("]"))
			{
				continue;
			}
			curHeight++;
		}
		if(curHeight == height) return curTree;
		
		return null;
	}
	
	public static int bRangeFormart(String curMark)
	{
		String[] rangeIds = curMark.trim().split("-");		
		if(rangeIds.length != 2) return -1;
		int index = 0;
		char[] secondChars = rangeIds[1].toCharArray();
		for(; index < rangeIds[1].length(); index++)
		{
			if(secondChars[index] < '0' || secondChars[index] > '9')
			{
				break;
			}
		}
		if(index == 0) return -1;
		
		String secondDigit = rangeIds[1].substring(0, index);
		try
		{
			int id1 = Integer.parseInt(rangeIds[0]);
			int id2 = Integer.parseInt(secondDigit);
		}
		catch(Exception e)
		{
			return -1;
		}
		
		return rangeIds[0].length() + index + 1;
	}

}
