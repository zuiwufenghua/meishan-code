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
import java.util.Set;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class NomarlizePhraseStructure {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		
		// TODO Auto-generated method stub
		if(args.length > 2 && args[2].equals("folder"))
		{
			List<String> allFiles = new ArrayList<String>();
			getAllFilesInFolder(args[0], allFiles);
			int processedFileNum = 0;
			for(String oneFile : allFiles)
			{
				processOneFile(oneFile, args[1], true);
				processedFileNum++;
				if(processedFileNum%2000 == 0)
				{
					System.out.println(processedFileNum);
				}
			}
		}
		else
		{
			processOneFile(args[0], args[1], false);
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
	
	public static void processOneFile(String inputFile, String outFolder, boolean bFolderMArk) throws Exception
	{		
		File file = new File(inputFile);
		String finalName = file.getName();
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
		BufferedReader bf = null;
		bf = new BufferedReader(isr);
		
		PrintWriter writer = null;
		if(bFolderMArk)
		{				
			String writeFilename = outFolder + File.separator + finalName;
			File writeFile = new File(writeFilename);
			int dupCount = 0;
			while(writeFile.exists())
			{
				dupCount++;
				writeFilename = String.format("%s%s%s.%d", outFolder, File.separator, finalName, dupCount);
				writeFile = new File(writeFilename);
			}	
			writer = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(writeFilename), "UTF-8"), false);
		}
		else
		{
			writer = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(outFolder), "UTF-8"), false);
		}
			
		String sLine = null;
		while ((sLine = bf.readLine()) != null) {	
			if(sLine.trim().replaceAll("\\s+", "").equals("(())"))
			{
				writer.println(sLine.trim());
				continue;
			}
			PennTreeReader reader = new PennTreeReader(
					new StringReader(sLine.trim()));
			Tree<String> tree = reader.next();
			
						
			Tree<String> subTree = tree;
			while(subTree.getLabel().equalsIgnoreCase("root") 
				|| subTree.getLabel().equalsIgnoreCase("top")
					)
			{
				subTree = subTree.getChild(0);
			}
			
			subTree.removeEmptyNodes();
			//subTree.removeUnaryChains();
			removeRedundancyRules(subTree);			
			writer.println("(TOP " + subTree.toString() + ")");
			writer.flush();
		}
		writer.close();
		bf.close();			
	}
	
	public static void removeRedundancyRules(Tree<String> rootTree)
	{
		
		if(rootTree.isLeaf() || rootTree.isPreTerminal()) return;
		List<Tree<String>> curChildren = rootTree.getChildren();
		if(curChildren.size() == 1 
		&& curChildren.get(0).getLabel().equals(rootTree.getLabel()))
		{
			rootTree.setChildren(curChildren.get(0).getChildren());
			removeRedundancyRules(rootTree);
		}
		else
		{
			for(Tree<String> curChild : curChildren)
			{
				removeRedundancyRules(curChild);
			}
		}
	}

}
