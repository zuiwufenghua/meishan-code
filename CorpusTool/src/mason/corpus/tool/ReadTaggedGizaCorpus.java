package mason.corpus.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;

public class ReadTaggedGizaCorpus {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		String inputFolder = args[0];
		String outputFile = args[1];
		int maxSentNum = Integer.parseInt(args[2]);
		File file = new File(inputFolder);
		
		String[] subFilenames = file.list();
		
		PrintWriter mainout = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8"));
		
		int senNum = 0;
		for (String subFilename : subFilenames) {
			String entirePath = String.format("%s\\%s", inputFolder,
					subFilename);
			
			List<String> theSents = readOneFile(entirePath);
			senNum += theSents.size();
			for(String oneSent : theSents)
			{
				mainout.println(oneSent);
			}
			
			if(senNum > maxSentNum)
			{
				break;
			}
		}
		
		mainout.close();
		
		System.out.println(senNum);

	}
	
	
	public static List<String> readOneFile (String inFile) throws Exception
	{
		List<String> results = new ArrayList<String>();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(inFile), "UTF-8"));
		
		boolean bStart = false;
		String newLine = "";
		String sLine = "";
		while ((sLine = reader.readLine()) != null) {
			String sTempLine = sLine.trim();
			if(sLine.equalsIgnoreCase("<HEADLINE>")
			  || sLine.equalsIgnoreCase("<DATELINE>")
			  || sLine.equalsIgnoreCase("<P>"))
			{
				bStart = true;
				sLine = "";
			}
			else if(sLine.equalsIgnoreCase("</HEADLINE>")
					|| sLine.equalsIgnoreCase("</DATELINE>")
					 || sLine.equalsIgnoreCase("</P>"))
			{
				bStart = false;
				//results.add(newLine.trim());
				List<String> curSentences =  processOnePara(newLine.trim());
				for(String curSent : curSentences)
				{
					results.add(curSent);
				}
				newLine = "";
			}
			else if(bStart && sTempLine.endsWith(")"))
			{
				newLine = newLine.trim() + " " + sTempLine;
			}
			
		}
		
		reader.close();
		
		
		return results;
	}
	
	
	public static List<String> processOnePara(String oneLine)
	{
		
		oneLine = oneLine.replace("　", " ");
		List<String> results = new ArrayList<String>();
		
		String[] theSmallUnits = oneLine.split("\\s+");
		String oneSentence = "";
		for(int idx = 0; idx < theSmallUnits.length; idx++)
		{
			String oneUnit = theSmallUnits[idx];
			int lastBrackStartIndex = oneUnit.lastIndexOf("(");
			String theWord = "";
			String theTag = "";
			try
			{
				theWord = oneUnit.substring(0, lastBrackStartIndex);
				theTag = oneUnit.substring(lastBrackStartIndex+1, oneUnit.length()-1);
				oneSentence = oneSentence + " " + theWord;
			}
			catch(Exception ex)
			{
				continue;
			}
			if(idx == theSmallUnits.length-1 || theTag.equalsIgnoreCase("PERIODCATEGORY"))
			{
				results.add(oneSentence.trim());
				oneSentence = "";
			}
		}
		
		return results;
	}

}
