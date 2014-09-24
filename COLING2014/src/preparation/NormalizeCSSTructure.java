package preparation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class NormalizeCSSTructure {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		BufferedReader bf = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8"));
		
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		String sLine = null;
		while ((sLine = bf.readLine()) != null) {	
			if(sLine.trim().replaceAll("\\s+", "").equals("(())"))
			{
				//writer.println(sLine.trim());
				continue;
			}
			try
			{
				PennTreeReader reader = new PennTreeReader(
						new StringReader(sLine.trim()));
				Tree<String> tree = reader.next();
				
							
				Tree<String> subTree = tree;
				/*
				while(subTree.getLabel().equalsIgnoreCase("root") 
					|| subTree.getLabel().equalsIgnoreCase("top")
						)
				{
					subTree = subTree.getChild(0);
				}*/
				
				subTree.removeEmptyNodes();
				subTree.removeDuplicate();
				subTree.removeUnaryChains();
				writer.println(subTree.toString());
			}
			catch (Exception ex)
			{
				continue;
			}
		}
		
		bf.close();
		writer.close();

	}

}
