package acl2013;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeSet;

public class WordStructureNoHeadFakePOS {

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
			if(!sLine.trim().startsWith("( "))continue;
			int splitSpace = sLine.substring(2).indexOf(" ");
			String poslabel = sLine.substring(2, 2+splitSpace);
			if(validPOS.contains(poslabel))
			{
				String newline = sLine.trim().trim().replace(String.format("( %s ", poslabel), "( NN ");
				newline = newline.replace("( NN y ", "( NN x ");
				newline = newline.replace("( NN z ", "( NN x ");
				String outstr = String.format("( FRAG s ( NN t %s ) )", newline);
				output.println(outstr.trim());
			}
			
		}
		in.close();
		output.close();

	}

}
