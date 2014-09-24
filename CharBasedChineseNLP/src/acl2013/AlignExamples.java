package acl2013;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class AlignExamples {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		String primeFile = args[0];
		String changeFile = args[1];
		
		Map<String, String> wordAnswerPair = new HashMap<String, String>();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(changeFile), "UTF8"));
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals("") || sLine.trim().length() < 2)continue;
			String[] wordposs = sLine.trim().split("\\s+");
			String rawSent = "";
			for(int i = 0; i < wordposs.length; i++)
			{
				int posStartIndex = wordposs[i].lastIndexOf("_");
				String word = wordposs[i].substring(0, posStartIndex);
				rawSent = rawSent + " " + word;
			}
			wordAnswerPair.put(rawSent.trim(), sLine.trim());
		}
		
		in.close();
		
		
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(primeFile), "UTF8"));
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals("") || sLine.trim().length() < 2)continue;
			String[] wordposs = sLine.trim().split("\\s+");
			String rawSent = "";
			for(int i = 0; i < wordposs.length; i++)
			{
				int posStartIndex = wordposs[i].lastIndexOf("_");
				String word = wordposs[i].substring(0, posStartIndex);
				rawSent = rawSent + " " + word;
			}
			if(wordAnswerPair.containsKey(rawSent.trim()))
			{
				output.println(wordAnswerPair.get(rawSent.trim()));
			}
			else
			{
				output.println(sLine.trim());
			}
		}
		
		in.close();
		output.close();
	}

}
