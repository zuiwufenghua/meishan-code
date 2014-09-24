package preparation;

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

public class OntoNotesParseReading {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		List<String> allFiles = new ArrayList<String>();
		getAllFiles(args[0], allFiles);
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"));
		int fileNum = 0;
		for(String theFile : allFiles)
		{
			if(!theFile.endsWith(".parse"))continue;
			fileNum++;
			InputStreamReader inputData = new InputStreamReader(
					new FileInputStream(theFile), "UTF-8");
			List<Tree<String>> trainTrees = new ArrayList<Tree<String>>();
			PennTreeReader treeReader = new PennTreeReader(inputData);

			while (treeReader.hasNext()) {
				trainTrees.add(treeReader.next());
			}

			Trees.TreeTransformer<String> treeTransformer = new Trees.StandardTreeNormalizer();
			for (Tree<String> tree : trainTrees) {
				Tree<String> normalizedTree = treeTransformer.transformTree(tree);
				normalizedTree.removeUnaryChains();
				normalizedTree.removeEmptyNodes();
				output.println(normalizedTree.toString());
			}
			
			inputData.close();
			
		}
		System.out.println(fileNum);
		output.close();

	}
	
	public static void getAllFiles(String folder, List<String> allFiles)
	{
		File f = null;
		f = new File(folder);
		File[] files = f.listFiles(); // 得到f文件夹下面的所有文件。
		for (File file : files) {
			if(file.isDirectory()) {
				//如何当前路劲是文件夹，则循环读取这个文件夹下的所有文件
				ReadAllFile(file.getAbsolutePath(), allFiles);
			} else {
				allFiles.add(file.getAbsolutePath());
			}
		}
	}
	
	   public static void ReadAllFile(String filePath,  List<String> allFiles) {  
	        File f = null;  
	        f = new File(filePath);  
	        File[] files = f.listFiles(); // 得到f文件夹下面的所有文件。  
	        List<File> list = new ArrayList<File>();  
	        for (File file : files) {  
	            if(file.isDirectory()) {  
	                //如何当前路劲是文件夹，则循环读取这个文件夹下的所有文件  
	                ReadAllFile(file.getAbsolutePath(), allFiles);  
	            } else {  
	            	allFiles.add(file.getAbsolutePath());
	            }  
	        }   
	    }  

}
