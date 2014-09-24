package preparation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeSet;

public class Dictionary2JuntoMAD {
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Set<String> commonwords = new TreeSet<String>();
		Set<String> commonwordposs = new TreeSet<String>();	
		Set<String> domainwords = new TreeSet<String>();
		Set<String> domainwordposs = new TreeSet<String>();		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8"));
		String sLine = null;
		
		boolean bWord = false;
		boolean bWordPOS = false;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().length() < 3)continue;
			sLine = sLine.trim();
			if(sLine.indexOf("word dictionary") != -1)
			{
				bWord = true;
				bWordPOS = false;
			}
			
			if(sLine.indexOf("word tag dictionary") != -1)
			{
				bWord = false;
				bWordPOS = true;
			}
			if(bWord)
			{
				int lastmaohaoIndex = sLine.lastIndexOf(":");
				if(lastmaohaoIndex == -1)continue;
				String firstUnit = sLine.substring(0, lastmaohaoIndex).trim();
				String secondunit = sLine.substring(lastmaohaoIndex + 1).trim();
				int wordproperty = Integer.parseInt(secondunit);
				if(wordproperty == 1)
				{
					commonwords.add(firstUnit);	
				}
				else
				{
					domainwords.add(firstUnit);
				}
			}
			if(bWordPOS)
			{
				int lastmaohaoIndex = sLine.lastIndexOf(":");
				if(lastmaohaoIndex == -1)continue;
				String firstUnit = sLine.substring(0, lastmaohaoIndex).trim();
				String secondunit = sLine.substring(lastmaohaoIndex + 1).trim();
				int wordproperty = Integer.parseInt(secondunit);
				if(wordproperty == 1)
				{
					commonwordposs.add(firstUnit);
				}
				else
				{
					domainwordposs.add(firstUnit);
				}
				
			}
		}
		
		in.close();
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"));
		int count = 0;
		int totalcount = domainwordposs.size() + commonwordposs.size();
		for(String theKey : commonwordposs)
		{
			String[] theunits = theKey.split("\\s+");
			String theWord = theunits[0].substring(1, theunits[0].length()-1);
			String thePOS = theunits[2];
			count++;
			if(count%30==0)
			{
				output.println(theWord+"|"+thePOS);
			}
			else
			{
				output.print(theWord+"|"+thePOS + " ");
			}
		}
		
		for(String theKey : domainwordposs)
		{
			String[] theunits = theKey.split("\\s+");
			String theWord = theunits[0].substring(1, theunits[0].length()-1);
			String thePOS = theunits[2];
			count++;
			if(count%30==0 || count == totalcount)
			{
				output.println(theWord+"|"+thePOS);
			}
			else
			{
				output.print(theWord+"|"+thePOS + " ");
			}
		}
		
		output.close();
	}

}
