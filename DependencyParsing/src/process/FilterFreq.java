package process;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class FilterFreq {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		int column = Integer.parseInt(args[2]);
		int freqThreshold = Integer.parseInt(args[3]);
		String sLine = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8"));
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
				args[1]), "UTF-8"));
		while ((sLine = reader.readLine()) != null) {
			if (sLine.trim().equals(""))
				continue;
			String[] curUnits = sLine.trim().split("\\s+");
			
			try {
				int featfreq = Integer.parseInt(curUnits[column-1]);
				if(featfreq > freqThreshold)
				{
					writer.println(sLine);
				}				
			} catch (Exception ex) {
				System.out.println("Error reading feats file: +" + sLine
						+ "\n");
				return;
			}
		}
		
		reader.close();
		writer.close();


	}

}
