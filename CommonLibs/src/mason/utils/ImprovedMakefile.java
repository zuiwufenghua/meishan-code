package mason.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class ImprovedMakefile {

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
		String lastLine = "";
		while ((sLine = in.readLine()) != null) {
			String newLine = "";
			while(sLine.startsWith(" ") || sLine.startsWith("\t"))
			{
				newLine = newLine + sLine.substring(0,1);
				sLine = sLine.substring(1);
			}
			String[] smallunits = sLine.trim().split("\\s+");
			if(smallunits[0].startsWith("$(DIST_DIR)/")) lastLine = smallunits[0];
			for(String oneunit : smallunits)
			{
				String changeunit = oneunit;
				if(oneunit.endsWith(".o") || oneunit.endsWith(".o:"))
				{
					changeunit = "$(OBJECT_DIR)/" + changeunit;
				}
				if(oneunit.endsWith(".h") || oneunit.endsWith(".hpp") || oneunit.endsWith(".c") || oneunit.endsWith(".cpp"))
				{
					changeunit = "$(SRC_DIR)/" + changeunit;
				}
				
				newLine = newLine + changeunit + " ";

			}
			output.println(newLine.substring(0, newLine.length()-1));
		}

		output.println();
		output.println("all: " + lastLine);
		in.close();
		output.close();
		
	}

}
