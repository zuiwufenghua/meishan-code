package cl2014;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class DingYuFormat2Conll {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		BufferedReader in = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(args[0]), "UTF-8"));
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"));
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			
			String newLine = sLine.trim();
			if(newLine.equals("") || newLine.indexOf("\t") != -1)
			{
				output.println(newLine);
			}
		}
		

		in.close();
		output.close();

	}

}
