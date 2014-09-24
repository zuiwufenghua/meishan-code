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
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class DecomposeWordSyn {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		if(args.length > 2 && args[2].equals("seg"))
		{
			File file = new File(args[0]);
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
	
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(args[1]), "UTF-8"), false);
			BufferedReader bf = null;
			bf = new BufferedReader(isr);
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
				
				tree.removeEmptyNodes();
				tree.removeUnaryChains();
				
				tree.convertBack2Phrase(tree);
				writer.println(tree);
				//List<String> curWords = new ArrayList<String>();
				//List<String> curPoss = new ArrayList<String>();
				
				//if(curPos.length() > 2 && curPos.substring(1,2).equalsIgnoreCase("#"))
				//{
				//	curPos = curPos.substring(2);
				//}
				//for()
			}
			writer.close();
			bf.close();
		}
		else
		{
			File file = new File(args[0]);
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
	
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(args[1]), "UTF-8"), false);
			BufferedReader bf = null;
			bf = new BufferedReader(isr);
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
				
				//tree.removeEmptyNodes();
				//tree.removeUnaryChains();
				Tree<String> subTree = tree.getChildren().get(0);
				if(!subTree.getLabel().equalsIgnoreCase("root"))
				{
					subTree = tree;
				}
				
				subTree.convert2WordSEG();
				writer.println("(TOP " + subTree.toString() + ")");
				writer.flush();
			}
			writer.close();
			bf.close();
		}

	}

}
