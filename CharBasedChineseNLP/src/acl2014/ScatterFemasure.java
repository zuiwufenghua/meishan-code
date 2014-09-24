package acl2014;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class ScatterFemasure {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		String keyWord = args[2];
		Map<String, Double>  values1 = new HashMap<String, Double>();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals("") || sLine.trim().length() < 2)continue;
			if(!sLine.trim().startsWith(keyWord))continue;
			String[] wordposs = sLine.trim().split("\\s+");
			
			try
			{
				for(int idx = 0; idx < wordposs.length; idx++)
				{
					if(wordposs[idx].equals("F1:"))
					{
						double theValue = Double.parseDouble(wordposs[idx+1]);						
						values1.put(wordposs[0], theValue/100);
						break;
					}
				}
			}
			catch(Exception ex)
			{
				System.out.println(sLine);
			}
		}
		in.close();
		
		
		Map<String, Double>  values2 = new HashMap<String, Double>();
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[1]), "UTF8"));
		sLine = null;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals("") || sLine.trim().length() < 2)continue;
			if(!sLine.trim().startsWith(keyWord))continue;
			String[] wordposs = sLine.trim().split("\\s+");
			
			try
			{
				for(int idx = 0; idx < wordposs.length; idx++)
				{
					if(wordposs[idx].equals("F1:"))
					{
						double theValue = Double.parseDouble(wordposs[idx+1]);						
						values2.put(wordposs[0], theValue/100);
						break;
					}
				}
			}
			catch(Exception ex)
			{
				System.out.println(sLine);
			}
		}
		in.close();
		
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[3]), "UTF-8"));
		writer.println("first\tsecond\tlabel");
		for(String theKey1 : values1.keySet())
		{
			if(values1.containsKey(theKey1))
			{
				writer.println(String.format("%f\t%f\ta", values1.get(theKey1), values2.get(theKey1)));
			}
		}
				
		writer.close();


	}

}
