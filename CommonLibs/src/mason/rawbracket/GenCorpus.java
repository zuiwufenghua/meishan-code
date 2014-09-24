package mason.rawbracket;

import java.io.*;
import java.util.*;
import edu.berkeley.nlp.syntax.*;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class GenCorpus {

	/**
	 * @param args
	 */
	public static int[] fileId2TrDeTe;

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		//String inputFolder = "I:\\mzhang_workspace\\ctb7.0\\data\\utf-8\\postagged";
		//String outputFolder = "I:\\mzhang_workspace\\ctb7.0\\data\\utf-8\\process";
		String inputFolder = "I:\\ctb_v6\\data\\utf8\\bracketed";
		String outputFolder = "I:\\ctb_v6\\data\\utf8\\constituent";
		File file = new File(inputFolder);
		String trainCorpusFileName = String.format("%s\\train.corpus",
				outputFolder);
		String devCorpusFileName = String
				.format("%s\\dev.corpus", outputFolder);
		String testCorpusFileName = String.format("%s\\test.corpus",
				outputFolder);
		String otherCorpusFileName = String.format("%s\\other.corpus",
				outputFolder);
		PrintWriter[] output = new PrintWriter[4];
		output[0] = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(otherCorpusFileName), "UTF-8"));
		output[1] = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(trainCorpusFileName), "UTF-8"));
		output[2] = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(devCorpusFileName), "UTF-8"));
		output[3] = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(testCorpusFileName), "UTF-8"));
		output[0].println();
		output[1].println();
		output[2].println();
		output[3].println();

		String[] subFilenames = file.list();
		configure_ctb6();
		for (String subFilename : subFilenames) {
			String entirePath = String.format("%s\\%s", inputFolder,
					subFilename);
			int underlineIndex = subFilename.indexOf("_");
			int lastdotIndex = subFilename.indexOf(".", underlineIndex);
			String fileNameIdstr = subFilename.substring(underlineIndex + 1,
					lastdotIndex);
			//if(fileNameIdstr.)
			int fileNameId = -1;
			try{
				fileNameId = Integer.parseInt(fileNameIdstr);
			}
			catch(Exception x)
			{
				continue;
			}
			if (fileNameId <= 0 || fileNameId >= 10000) {
				System.out.println(String
						.format("Invalid File %s", subFilename));
				continue;
			}
			int saveFileId = fileId2TrDeTe[fileNameId];

			if (saveFileId == 0 || saveFileId == 1 || 
				saveFileId == 2 || saveFileId == 3) {
				
				CTB6CorpusReader ctb6CorpusReader = new CTB6CorpusReader();
				ctb6CorpusReader.init(entirePath);

				List<String> contentItems = ctb6CorpusReader
						.getBracketContent();
				// output[saveFileId].println(fileNameIdstr);
				String allContent = "";
				for (String oneLine : contentItems) {
					allContent = allContent + " " + oneLine;
				}
				allContent = allContent.trim();
				
				PennTreeReader reader = new PennTreeReader(new StringReader(allContent));
				
				
				
				while(reader.hasNext()) {
					//output[saveFileId].println(oneLine);
					//output[saveFileId].flush();
					Tree<String> tree = reader.next();
					//Tree<String> tree = PennTreeReader.parseEasy(oneLine, false);
					
					if(tree == null)
					{
						System.out.println(allContent);
						continue;
					}
					List<String> forms = tree.getYield();
					int n = forms.size() + 1;
					if(tree.toString().trim().startsWith("(ROOT "))
					{
						output[saveFileId].println(tree.getChild(0));
						output[saveFileId].flush();
					}
				}
				


				ctb6CorpusReader.uninit();
			} else {
				System.out.println(String
						.format("Invalid File %s", subFilename));
				continue;
			}
		}
		output[0].println();
		output[1].println();
		output[2].println();
		output[3].println();
		output[0].close();
		output[1].close();
		output[2].close();
		output[3].close();
		//System.out.println(String.format("%d", CTB6CorpusReader.invalidCount));
	}

	public static void configure_ctb6() throws Exception {
		fileId2TrDeTe = new int[10000];
		for (int i = 0; i < 10000; i++) {
			fileId2TrDeTe[i] = -1; // -1 denote the file of no use
		}

		String flist = "I:\\mszhang\\其它数据\\flist\\split.txt";
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(flist), "UTF-8"));
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

	}
	
	
	public static void configure_ctb62() throws Exception {
		fileId2TrDeTe = new int[10000];
		for (int i = 0; i < 10000; i++) {
			fileId2TrDeTe[i] = -1; // -1 denote the file of no use
		}

		String flist = "I:\\mszhang\\其它数据\\flist-ctb6\\split.txt";
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(flist), "UTF-8"));
		String sLine = null;
		int idmark = 0;
		boolean bGiveTrainCorpusId = false;
		while ((sLine = reader.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] smartParts = sLine.split("\\s+");
			String subFilename = smartParts[0];
			int fileId = 0;
			if(smartParts[0].equals("!") && smartParts[1].startsWith("chtb"))
			{
				idmark = 3;
				subFilename = smartParts[1];
			}
			else if(smartParts[0].equals("*") && smartParts[1].startsWith("chtb"))
			{
				idmark = 2;
				subFilename = smartParts[1];
			}
			else if(smartParts[0].startsWith("chtb"))
			{
				idmark = 1;
			}
			else
			{
				continue;
			}
			
			int underlineIndex = subFilename.indexOf("_");
			String fileNameIdstr = subFilename.substring(underlineIndex + 1,
					underlineIndex + 5);
			int fileNameId = Integer.parseInt(fileNameIdstr);
			fileId2TrDeTe[fileNameId] = idmark;
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

	}

}
