package mason.corpus.tool;


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
import edu.berkeley.nlp.syntax.Trees;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class ReadPTBCorpus {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		
		// TODO Auto-generated method stub
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		if(args.length > 2 && args[2].equals("folder"))
		{
			List<String> allFiles = new ArrayList<String>();
			getAllFilesInFolder(args[0], allFiles);
			int processedFileNum = 0;
			for(String oneFile : allFiles)
			{
				processOneFile(oneFile, writer, true);
				processedFileNum++;
				if(processedFileNum%2000 == 0)
				{
					System.out.println(processedFileNum);
				}
			}
		}
		else
		{
			processOneFile(args[0], writer, false);
		}
		
		
		writer.close();
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
	
	public static void processOneFile(String inputFile, PrintWriter writer, boolean bFolderMArk) throws Exception
	{		
		File file = new File(inputFile);
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
		PennTreeReader treeReader = new PennTreeReader(isr);
		
		
		Trees.TreeTransformer<String> treeTransformer = new Trees.StandardTreeNormalizer();
		int iCount = 0;
		while (treeReader.hasNext()) {
			Tree<String> normalizedTree = treeTransformer.transformTree(treeReader.next());	
						
									
			normalizedTree.removeEmptyNodes();
			normalizedTree.removeUnaryChains();
			normalizedTree.removeDuplicate();
			
			writer.println(normalizedTree.toString());
			writer.flush();
			iCount++;
		}
		
		isr.close();
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
