package acl2014;

import acl2013.CFGWordStructureNormalize;
import mason.utils.PinyinComparator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class VVGuideNNWordStructure {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		Map<String, String> VVWordStructures = new HashMap<String, String>();
		
		
		Map<String, String> NNWordStructures = new HashMap<String, String>();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		
		String sLine = null;
		// read vv structures
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().length() < 2) continue;
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
				//normalizedTree.removeUnaryChains();
				//normalizedTree.removeEmptyNodes();
				while(normalizedTree.getLabel().equalsIgnoreCase("root")
						|| normalizedTree.getLabel().equalsIgnoreCase("top"))
				{
					normalizedTree = normalizedTree.getChild(0);
				}
				if(CFGWordStructureNormalize.checkWordStructure(normalizedTree))
				{
					String theWord = normalizedTree.getTerminalStr();
					if(!PinyinComparator.bContainChineseCharacter(theWord))continue;
					String theLabel = normalizedTree.getLabel();
					int thePOSEndIndex = theLabel.lastIndexOf("#");
					String thePOS = theLabel.substring(0, thePOSEndIndex).trim();
					
					if(!thePOS.equals("VV"))
					{
						System.out.println(sLine);
						continue;
					}
					
					VVWordStructures.put(thePOS+"_" + theWord, normalizedTree.toString().replace("VV#", "TT#"));
				}
			}
			catch(Exception ex)
			{
				System.out.println(sLine);
			}
		}
		
		in.close();
		
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[1]), "UTF8"));
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);
		// read nn structures
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().length() < 2) continue;
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
				//normalizedTree.removeUnaryChains();
				//normalizedTree.removeEmptyNodes();
				while(normalizedTree.getLabel().equalsIgnoreCase("root")
						|| normalizedTree.getLabel().equalsIgnoreCase("top"))
				{
					normalizedTree = normalizedTree.getChild(0);
				}
				if(CFGWordStructureNormalize.checkWordStructure(normalizedTree))
				{
					String theWord = normalizedTree.getTerminalStr();
					if(!PinyinComparator.bContainChineseCharacter(theWord))continue;
					String theLabel = normalizedTree.getLabel();
					int thePOSEndIndex = theLabel.lastIndexOf("#");
					String thePOS = theLabel.substring(0, thePOSEndIndex).trim();
					
					if(!thePOS.equals("NN") && !thePOS.equals("NR"))
					{
						System.out.println(sLine);
						continue;
					}
					
					String refinedstructure = normalizedTree.toString();
					if(VVWordStructures.containsKey("VV_" + theWord)
						&& 	!refinedstructure.replace(thePOS+"#", "TT#").equals(VVWordStructures.get("VV_" + theWord)))
					{
						refinedstructure = VVWordStructures.get("VV_" + theWord).replace("TT#", thePOS+"#");
					}
					output.println(refinedstructure);
				}
			}
			catch(Exception ex)
			{
				System.out.println(sLine);
			}
		}
		
		in.close();
		output.close();

	}

}
