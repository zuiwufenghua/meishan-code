package acl2013;

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
import java.util.Set;
import java.util.TreeSet;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class WordStructureSelection {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Set<String> validPOS = new TreeSet<String>();
		validPOS.add("AD");validPOS.add("JJ");validPOS.add("NN");validPOS.add("VA");
		validPOS.add("VC");validPOS.add("VE");validPOS.add("VV");validPOS.add("LC");
		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		String sLine = "";
		Map<String, Tree<String>> wordstructures = new HashMap<String, Tree<String>>();
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			
			try
			{
				String newLine = sLine.trim();
				int lastLength = newLine.length();
				
				while(true)
				{
					newLine = newLine.replace("( ", "(");
					newLine = newLine.replace(" )", ")");
					newLine = newLine.replace(") ", ")");
					if(newLine.length() == lastLength)
					{
						break;
					}
					lastLength = newLine.length();
				}
				
				
				while(true)
				{
					newLine = newLine.replace(" x ", "#x ");
					newLine = newLine.replace(" y ", "#y ");
					newLine = newLine.replace(" z ", "#z ");
					newLine = newLine.replace(" b ", "#b ");
					newLine = newLine.replace(" i ", "#i ");
					if(newLine.length() == lastLength)
					{
						break;
					}
					lastLength = newLine.length();
				}
								
				final PennTreeReader reader = new PennTreeReader(
						new StringReader(newLine));
				Tree<String> curTree = reader.next();
				
				String theWord = curTree.getTerminalStr();
				String theLabel = curTree.getLabel();
				int splitIndex = theLabel.lastIndexOf("#");
				if(splitIndex == -1)
				{
					System.out.println(sLine);
					continue;
				}
				String thePOS = theLabel.substring(0, splitIndex);
				if(theWord.length() < 2)continue;
				if(!PinyinComparator.bAllChineseCharacter(theWord) || !validPOS.contains(thePOS))
				{
					continue;
				}
				out.println(curTree.toString());
			}
			catch(Exception ex)
			{
				System.out.println(sLine);
				continue;
			}
		}
		
		in.close();
		out.close();

	}

}
