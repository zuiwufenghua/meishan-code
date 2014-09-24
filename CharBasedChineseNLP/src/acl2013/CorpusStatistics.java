package acl2013;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CorpusStatistics {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Map<Integer, Set<String>> statistics = new HashMap<Integer, Set<String>>();
		for(int i = 1; i <= 16; i++ )
		{
			statistics.put(i, new HashSet<String>());
		}
		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals("") || sLine.trim().length() < 2)continue;
			String[] wordposs = sLine.trim().split("\\s+");
			
			for(String wordpos : wordposs)
			{
				int splitIndex = wordpos.lastIndexOf("_");
				if(splitIndex == -1)
				{
					System.out.println(sLine);
					break;
				}
				else
				{
					String word = wordpos.substring(0, splitIndex);
					int length = splitIndex; 
					if(length > 16) length = 16;
					statistics.get(length).add(word);
				}
			}			
		}
		
		
		in.close();
		
		int totalWords = 0;
		for(int i = 2; i <= 16; i++ )
		{
			totalWords += statistics.get(i).size();
		}
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		output.println(String.format("Total multi-character words : %d",  totalWords));
		
		for(int i = 1; i <= 16; i++ )
		{
			output.println(String.format("%d  %d",  i, statistics.get(i).size()));
		}
		
		output.close();

	}

}
