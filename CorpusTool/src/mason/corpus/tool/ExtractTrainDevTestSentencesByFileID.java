package mason.corpus.tool;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.List;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class ExtractTrainDevTestSentencesByFileID {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		String mark = "";
		if(args.length> 3) mark = args[3];
		
		int[] fileId2TrDeTe = configure(args[1]);
		
		String trainCorpusFileName = String.format("%s\\train.%s.corpus",
				args[2], mark);
		String devCorpusFileName = String
				.format("%s\\dev.%s.corpus", args[2], mark);
		String testCorpusFileName = String.format("%s\\test.%s.corpus",
				args[2], mark);
		String otherCorpusFileName = String.format("%s\\other.%s.corpus",
				args[2], mark);
		
		PrintWriter[] output = new PrintWriter[4];
		output[0] = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(otherCorpusFileName), "UTF-8"));
		output[1] = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(trainCorpusFileName), "UTF-8"));
		output[2] = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(devCorpusFileName), "UTF-8"));
		output[3] = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(testCorpusFileName), "UTF-8"));
		
		BufferedReader cfgreader = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8"));
		
		
		PrintWriter outout = null;
		String sLine = null;
		while ((sLine = cfgreader.readLine()) != null) {
			sLine =sLine.trim();
			int sentenceIdEnd = sLine.indexOf("\t");
			if(sentenceIdEnd == -1)
			{
				continue;				
			}
			String firstPart = sLine.substring(0, sentenceIdEnd);
			int sentenceId = Integer.parseInt(firstPart);
			if(sentenceId >= 0 && sentenceId < fileId2TrDeTe.length)
			{
				outout = output[fileId2TrDeTe[sentenceId]];
			}
			else
			{
				continue;
			}
			String secondPart = sLine.substring(sentenceIdEnd +1).trim();
			PennTreeReader reader = new PennTreeReader(new StringReader(
					secondPart));

			
			while (reader.hasNext()) {
				Tree<String> tree = reader.next();
				outout.println(tree.toString());
			}
		}
		cfgreader.close();
		
		output[0].close();
		output[1].close();
		output[2].close();
		output[3].close();


	}
	
	
	public static int[] configure(String filesplit) throws Exception {
		int[] fileId2TrDeTe = new int[10000];
		for (int i = 0; i < 10000; i++) {
			fileId2TrDeTe[i] = 0; // -1 denote the file of no use
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(filesplit), "UTF-8"));
		String sLine = null;
		int idmark = 0;
		boolean bGiveTrainCorpusId = false;
		while ((sLine = reader.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			if (sLine.startsWith("#dev")) {
				idmark = 2;
			} else if (sLine.startsWith("#test")) {
				idmark = 3;
			} else if (sLine.startsWith("#train")) {
				idmark = 1;
				bGiveTrainCorpusId = true;
			} else {

				int underlineIndex = sLine.indexOf("-");
				int fileNameIdStart = 0;
				if(underlineIndex == -1)
				{
					fileNameIdStart = Integer.parseInt(sLine.trim());
				}
				else
				{
					String fileNameIdstrStart = sLine.substring(0, underlineIndex).trim();
					fileNameIdStart = Integer.parseInt(fileNameIdstrStart);
				}
				
				

				int fileNameIdEnd = fileNameIdStart;
				if (underlineIndex != -1) {
					String fileNameIdstrEnd = sLine
							.substring(underlineIndex + 1).trim();
					fileNameIdEnd = Integer.parseInt(fileNameIdstrEnd);
				}
				for (int fileNameId = fileNameIdStart; fileNameId <= fileNameIdEnd; fileNameId++) {
					if(fileId2TrDeTe[fileNameId] == -1)
					{
						fileId2TrDeTe[fileNameId] = idmark;
					}
					else
					{
						fileId2TrDeTe[fileNameId] = idmark;
					}
				}
			}
		}
		reader.close();
		
		if(!bGiveTrainCorpusId)
		{
			for (int i = 0; i < 10000; i++) {
				if(fileId2TrDeTe[i] == -1)
				{
					fileId2TrDeTe[i] = 1; // -1 denote the file of no use
				}
			}
		}
		
		return fileId2TrDeTe;

	}

}
