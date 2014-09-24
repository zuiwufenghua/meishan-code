package acl2013;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class Zpar1WordTrain {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			if(!sLine.trim().startsWith("( "))continue;
			int splitSpace = sLine.substring(2).indexOf(" ");
			String poslabel = sLine.substring(2, 2+splitSpace);
			String outstr = String.format("( FRAG s ( %s t %s ) )", poslabel, sLine.trim());
			output.println(outstr.trim());
		}
		in.close();
		output.close();

	}

}
