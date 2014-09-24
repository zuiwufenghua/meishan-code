package acl2013;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;


public class Berkeley2Zpar {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		BufferedReader bf=new BufferedReader(new InputStreamReader(new FileInputStream(args[0]),"UTF-8"));
		String sLine = null;
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		while ((sLine=bf.readLine())!=null) {
			if(sLine.trim().length() < 3) continue;
			try
			{
				PennTreeReader reader = new PennTreeReader(
						new StringReader(sLine.trim()));
				if(!reader.hasNext() )
				{
					System.out.println(sLine.trim());
					continue;
				}
				Tree<String> normalizedTree = reader.next();
				if(!sLine.trim().replaceAll("\\s+", "").equals(normalizedTree.toString().replaceAll("\\s+", "")))
				{
					System.out.println(sLine.trim());
					continue;
				}
				//normalizedTree.removeUnaryChains();
				//normalizedTree.removeEmptyNodes();
				while(normalizedTree.getLabel().equalsIgnoreCase("root")
						|| normalizedTree.getLabel().equalsIgnoreCase("top"))
				{
					normalizedTree = normalizedTree.getChild(0);
				}
				if(CFGWordStructureNormalize.checkWordStructure(normalizedTree))
				{
					output.println(normalizedTree.toCLTString());
				}
				else
				{
					System.out.println(normalizedTree.toString());
				}
			}
			catch (Exception ex)
			{
				System.out.println(sLine.trim());
			}
		}		
		output.close();
		bf.close();
	}

}
