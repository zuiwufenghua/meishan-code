package WordStructure;

import mason.utils.PinyinComparator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class ChineseName {

	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception{
		// TODO Auto-generated method stub
		String firstNameFile = args[0];
		
		Set<String> firstName = new HashSet<String>();
		Set<String> indvalidSuffix = new HashSet<String>();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(firstNameFile), "UTF8"));
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] firstNames = sLine.trim().split("\\s+");
			if(!firstNames[0].equals("INSUFFIX"))
			{
				for(String theName : firstNames)
				{
					firstName.add(theName);
				}
			}
			else
			{
				for(String theName : firstNames)
				{
					indvalidSuffix.add(theName);
				}
			}
		}
		
		in.close();
		
		String wordposFile = args[1];
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(wordposFile), "UTF8"));
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);
		
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] wordposs = sLine.trim().split("_");
			if(wordposs.length != 2)continue;
			String theWord = wordposs[0];
			String fistname1 = theWord.substring(0,1);
			String fistname2 = theWord.substring(0,2);
			int matchNum = 0;
			if(theWord.length() == 3 && firstName.contains(fistname1))
			{
				String result = String.format("(TOP (S (NR#c (B#NR %s)) (NR#c (NR#c (M#NR %s)) (NR#c (E#NR %s)))))"
						, theWord.substring(0,1), theWord.substring(1,2), theWord.substring(2,3) );
				matchNum++;
				output.println(result);
			}
			
			if(theWord.length() == 3 && firstName.contains(fistname2))
			{
				String result = String.format("(TOP (S (NR#c (NR#c (B#NR %s)) (NR#c (M#NR %s))) (NR#c (E#NR %s))))"
						, theWord.substring(0,1), theWord.substring(1,2), theWord.substring(2,3) );
				matchNum++;
				output.println(result);
				if(matchNum > 1)
				{
					System.out.println(result);
				}
			}
			
			if(theWord.length() == 4 && firstName.contains(fistname2))
			{
				String result = String.format("(TOP (S (NR#c (NR#c (B#NR %s)) (NR#c (M#NR %s))) (NR#c (NR#c (M#NR %s)) (NR#c (E#NR %s)))))"
						, theWord.substring(0,1), theWord.substring(1,2), theWord.substring(2,3), theWord.substring(3,4) );
				matchNum++;
				output.println(result);
				if(matchNum > 1)
				{
					System.out.println(result);
				}
			}
			
		}
		
		
		
		in.close();
		output.close();
		
		

		
		// for check
		/*
		String wordposFile = args[1];
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(wordposFile), "UTF8"));
		
		List<String> names = new ArrayList<String>();
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] wordposs = sLine.trim().split("\\s+");
			String theWord = wordposs[0];
			String prefix = "";
			if(theWord.length() < 3 || theWord.length() > 4)
			{
				continue;
			}
			if(!firstName.contains(theWord.substring(0,1)) && !firstName.contains(theWord.substring(0,2)))
			{
				continue;
			}
			if(indvalidSuffix.contains(theWord.substring(theWord.length()-1)))
			{
				continue;
			}
			for(int idx = 1; idx < wordposs.length; idx++)
			{
				int colonIndex = wordposs[idx].lastIndexOf(":");
				if(colonIndex == -1)continue;
				String pos =  wordposs[idx].substring(0, colonIndex);
				try
				{
					Integer score = Integer.parseInt(wordposs[idx].substring(colonIndex+1));
				}
				catch (Exception e)
				{
					continue;
				}
				
				if(pos.startsWith("NR"))
				{
					names.add(theWord + "_" + pos);
				}
				
				
			}
		}
		
		in.close();
		
		Collections.sort(names, new Comparator(){   
			public int compare(Object o1, Object o2) {    				
				return PinyinComparator.Compare((String) o1, (String) o2);				
            }   
		});
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);
		
		for(String curNamePos: names)
		{
			output.println(curNamePos.trim());
		}
		
		output.close();
		*/

	}

}
