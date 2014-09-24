package preparation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class SampleKnowledge {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		List<String> commonwords = new ArrayList<String>();
		List<String> commonwordposs = new ArrayList<String>();
		List<String> domainwords = new ArrayList<String>();
		List<String> domainwordposs = new ArrayList<String>();
		List<String> domainwordpossOnlyWord = new ArrayList<String>();
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
					int wordEndIndex = firstUnit.indexOf("] , ");
					domainwordpossOnlyWord.add(firstUnit.substring(1, wordEndIndex));
				}
				
			}
		}
		
		in.close();
		
		int count = Integer.parseInt(args[2]);
		List<Integer> seq = new ArrayList<Integer>();
		for(int i = 0; i < domainwordposs.size(); i++)
		{
			seq.add(i);
		}
		
		Collections.shuffle(seq, new Random(0));
		Set<String> newDomainWords = new TreeSet<String>();
		for(int i = 0; i < count; i++)
		{
			int curSeq = seq.get(i);
			newDomainWords.add(domainwordpossOnlyWord.get(curSeq));
		}
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"));
		output.println("word dictionary");
		for(String theKey : commonwords)
		{
			output.println(String.format("%s : 1", theKey));
		}
		
		for(String theKey : newDomainWords)
		{
			output.println(String.format("[%s] : 2", theKey));
		}
		
		output.println();
		output.println("word tag dictionary");	
		for(String thefirstKey : commonwordposs)
		{
			output.println(String.format("%s : 1",  thefirstKey));
		}
		
		for(int i = 0; i < count; i++)
		{
			int curSeq = seq.get(i);			
			output.println(String.format("%s : 2",  domainwordposs.get(curSeq)));
		}
		
		output.close();
		
	}

}
