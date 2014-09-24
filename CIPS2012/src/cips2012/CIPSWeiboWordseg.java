package cips2012;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class CIPSWeiboWordseg {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		FileInputStream fis = new FileInputStream(args[0]);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");

		BufferedReader bf = null;
		bf = new BufferedReader(isr);
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"));
		
		String sLine = null;
		boolean sentenceStart = false;
		String lastLine = "";
		String last2Line = "";
		String curSentence = "";
		while ((sLine = bf.readLine()) != null) {
			
			boolean bstartMark = true;
			if(last2Line.equals("") && sLine.equals("��"))
			{
				try{
					
					int sentId = Integer.parseInt(lastLine);					
				}
				catch (Exception e)
				{
					bstartMark = false;
				}				
			}
			else
			{
				bstartMark = false;
			}
			
			if(bstartMark)
			{
				curSentence = lastLine+sLine.trim();
				sentenceStart = true;
			}
			else if(sentenceStart && !sLine.trim().equals(""))
			{
				curSentence = curSentence + sLine.trim();
			}
			else if(sentenceStart && sLine.trim().equals(""))
			{
				writer.println(curSentence);
				sentenceStart = false;
			}
			
			last2Line = lastLine;
			lastLine = sLine.trim();
		}
		writer.close();
	}

}
