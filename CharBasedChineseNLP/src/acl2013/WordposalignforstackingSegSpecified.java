package acl2013;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class WordposalignforstackingSegSpecified {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		BufferedReader instacking = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		BufferedReader inseg = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[1]), "UTF8"));
		
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);
		String sLine = null;
		sLine = instacking.readLine(); //第一行为空行
		sLine = inseg.readLine(); //
		while ((sLine = instacking.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String theSegLine = sLine.trim();
			String theStackingLine = "";
			while ((sLine = instacking.readLine()) != null) {
				if(sLine.trim().equals(""))continue;
				else
				{
					theStackingLine = sLine.trim();
					break;
				}
			}
			String theSegCheckLine = "";
			while ((sLine = inseg.readLine()) != null) {
				if(sLine.trim().equals(""))continue;
				else
				{
					theSegCheckLine = sLine.trim();
					break;
				}
			}
			//checking
			String[] wordposs = theStackingLine.split("\\s+");
			String theStackingSentence = "";
			for(String theUnit : wordposs)
			{
				theStackingSentence = theStackingSentence + theUnit.substring(0,1);
				if(!theUnit.substring(1,2).equals("_"))
				{
					System.out.println("error sentence");
					System.out.println(theStackingLine);
					return;
				}
			}
			
			String theSegSentence = theSegLine.replaceAll("\\s+", "");
			String theSegCheckSentence = theSegCheckLine.replaceAll("\\s+", "");
			if(!theSegSentence.equals(theSegCheckSentence) || !theSegCheckSentence.equals(theStackingSentence))
			{
				System.out.println("error sentence");
				System.out.println(theSegCheckSentence);
				System.out.println(theStackingSentence);
				System.out.println(theSegSentence);
				return;
			}
			
			out.println(theSegCheckLine);
			out.println(theStackingLine);
		}
		
		instacking.close();
		inseg.close();
		out.close();

	}

}
