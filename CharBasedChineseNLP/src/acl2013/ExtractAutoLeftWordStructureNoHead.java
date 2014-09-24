package acl2013;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeSet;

import edu.berkeley.nlp.syntax.Tree;

public class ExtractAutoLeftWordStructureNoHead {

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
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] wordposs = sLine.trim().split("\\s+");
			boolean valid = false;
			for(int idx = 1; idx < wordposs.length; idx++)
			{
				int colonIndex = wordposs[idx].lastIndexOf(":");
				if(colonIndex == -1)continue;
				String pos =  wordposs[idx].substring(0, colonIndex);
				if(validPOS.contains(pos))
				{
					valid = true;
					break;
				}
				
				try
				{
					Integer score = Integer.parseInt(wordposs[idx].substring(colonIndex+1));
				}
				catch (Exception e)
				{
					continue;
				}
			}
			
			if(valid)
			{
				Tree<String> tree = AutomaticLeftBinarizeWordStut.getLeftInternalStructureTree(wordposs[0], "NN");
				String outstr = String.format("( FRAG s ( NN t %s ) )", tree.toCLTString());
				output.println(outstr.trim());
			}
		}
		
		in.close();
		output.close();

	}

}
