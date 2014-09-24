package process;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class AddClusters2Dictionary {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"));
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
				args[0]), "UTF-8"));
		
		String sLine = null;
		
		while((sLine = reader.readLine()) != null) {
			writer.println(sLine);
		}
		reader.close();
		
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(
				args[1]), "UTF-8"));
		
		while((sLine = reader.readLine()) != null) {
			writer.println("cluster " + sLine.trim());
		}
		reader.close();	
		writer.close();

	}

}
