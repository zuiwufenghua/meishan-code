package mason.rawbracket;

import java.io.*;
import java.util.*;

public class CTB6CorpusReader {
	private BufferedReader reader = null;
	static int invalidCount = 0;

	public void init(String xmlFile) throws Exception {
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(
				xmlFile), "UTF-8"));
	}

	public void uninit() throws Exception {
		if (reader != null) {
			reader.close();
		}
	}

	public List<List<String>> getContent(String keyword) throws Exception {
		List<List<String>> listContents = new ArrayList<List<String>>();
		String sLine = null;

		if ((sLine = reader.readLine()) != null) {
			if (sLine.trim().equals("<DOC>")) {
				while ((sLine = reader.readLine()) != null) {
					if (sLine.trim().startsWith("<" + keyword + " ")
							&& sLine.trim().endsWith(">")) {
						List<String> oneContent = new ArrayList<String>();
						while ((sLine = reader.readLine()) != null) {
							if (sLine.trim().equals("</" + keyword + ">")) {
								listContents.add(oneContent);
								break;
							} else {
								oneContent.add(sLine);
							}
						}
					}
				}
			} else {
				List<String> oneContent = new ArrayList<String>();
				oneContent.add(sLine);
				while ((sLine = reader.readLine()) != null) {
					if (!sLine.trim().equals("")) {
						oneContent.add(sLine);
					}
				}
				listContents.add(oneContent);
			}
		}

		return listContents;
	}

	public List<String> getContent() throws Exception {
		List<String> listContents = new ArrayList<String>();
		String sLine = null;
		
		boolean bShouldAppend = false;
		while ((sLine = reader.readLine()) != null) {
			String[] smartParts = sLine.split("\\s+");
			String firstPart = smartParts[0];
			String lastPart = smartParts[smartParts.length-1];
			boolean allWordPos = true;
			for(String curPart : smartParts)
			{
				if(firstPart.indexOf("_") == -1)
				{
					allWordPos = false;
					break;
				}
			}
			if(firstPart.startsWith("<DATE_TIME>") || firstPart.startsWith("<END_TIME>"))
			{
				allWordPos = false;
			}			
			
			if (allWordPos) {
				/*
				if(!bShouldAppend)
				{
					listContents.add(sLine.trim());
				}
				else
				{
					String tempStr = listContents.get(listContents.size()-1);
					tempStr = tempStr.trim() + " " + sLine.trim();
					listContents.set(listContents.size()-1, tempStr);
				}
				bShouldAppend = true;*/
				listContents.add(sLine.trim());
			}
			else
			{
				bShouldAppend = false;
			}

		}	
		return listContents;
	}

	public List<String> getBracketContent() throws Exception {
		List<String> listContents = new ArrayList<String>();
		String sLine = null;
		
		boolean bShouldAppend = false;
		while ((sLine = reader.readLine()) != null) {
			String sTempLine = sLine.trim();
			//if(sTempLine.startsWith("<S ID=") || sTempLine.startsWith("</S>"))
			//{
			//	listContents.add("");
			//}
			boolean invalidSentence = false;
			if(sTempLine.startsWith("("))
			{
				String theContent = sLine;
				if(sLine.startsWith("(IP "))
				{
					//System.out.println("IP_START");
					invalidCount++;
					theContent = "( " + sLine;
					invalidSentence = true;
				}
				listContents.add(theContent);
			}
			if(invalidSentence && sTempLine.startsWith("</S>"))
			{
				String lastSentence = listContents.get(listContents.size()-1);
				listContents.set(listContents.size()-1, lastSentence + ")");
				invalidSentence = false;
			}
			
		}	
		return listContents;
	}


}
