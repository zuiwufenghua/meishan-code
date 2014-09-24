package cl2014;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CorpusMapping {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		//second as a key to map
		Map<String, List<String>> corpusmap = new HashMap<String, List<String>>();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		String sLine = null;
		
		boolean bEnd = false;
		List<String> oneSentence = null;
		while (!bEnd) {
			oneSentence = new ArrayList<String>();
			while ((sLine = in.readLine()) != null) {
				if(sLine.trim().equals(""))
				{
					continue;
				}
				else
				{
					break;
				}
			}
			if(bEnd || sLine == null)break;			

			oneSentence.add(sLine.trim());
			while ((sLine = in.readLine()) != null) {
				if(sLine.trim().equals(""))
				{					
					break;
				}
				else
				{
					oneSentence.add(sLine.trim());
				}
			}
			if(sLine == null)bEnd = true;
			
			String key = "";
			for(String oneline : oneSentence)
			{
				String[] units = oneline.split("\\s+");
				key = key + " " + units[1];
			}
			key = key.trim();
			corpusmap.put(key, oneSentence);
			
		}
		
		in.close();
		
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[1]), "UTF8"));
		sLine = null;
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"));
		
		PrintWriter outputremain = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2] + ".remain"), "UTF-8"));
		
		bEnd = false;
		while (!bEnd) {
			oneSentence = new ArrayList<String>();
			while ((sLine = in.readLine()) != null) {
				if(sLine.trim().equals(""))
				{
					continue;
				}
				else
				{
					break;
				}
			}
			if(bEnd || sLine == null)break;			

			oneSentence.add(sLine.trim());
			while ((sLine = in.readLine()) != null) {
				if(sLine.trim().equals(""))
				{					
					break;
				}
				else
				{
					oneSentence.add(sLine.trim());
				}
			}
			if(sLine == null)bEnd = true;
			
			String key = "";
			for(String oneline : oneSentence)
			{
				String[] units = oneline.split("\\s+");
				key = key + " " + units[1];
			}
			key = key.trim();
			if(corpusmap.containsKey(key))
			{
				for(String oneline : corpusmap.get(key))
				{
					output.println(oneline);
				}
				output.println();
			}
			else
			{
				for(String oneline : oneSentence)
				{
					outputremain.println(oneline);
				}
				outputremain.println();
			}
			
		}
		
		in.close();
		output.close();
		outputremain.close();

	}

}
