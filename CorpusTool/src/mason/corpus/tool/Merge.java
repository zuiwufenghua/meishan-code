package mason.corpus.tool;

import mason.corpus.tool.BufferInfo;

import java.io.*;
import java.util.*;

public class Merge {

	/**
	 * @param args
	 */
	
		public static void main(String[] args) throws Exception {
		List<BufferInfo> fileBuffer = new ArrayList<BufferInfo>();
		BufferedReader configureReader = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(args[0]), "UTF-8"));
		String sLine = null;
		int maxFileId = 0;
		BufferInfo tmpBuff = new BufferInfo();
		while ((sLine = configureReader.readLine()) != null) {
			if (sLine.trim().equals("") || sLine.indexOf("#") != -1)
				continue;
			String[] smartParts = sLine.trim().split(":");
			if (smartParts.length == 2 )
			{
				try {
					int fileId = Integer.parseInt(smartParts[0]);
					int bufferStartId = Integer.parseInt(smartParts[1]);
					//int bufferEndId = bufferStartId;
					if(fileId > maxFileId)
					{
						maxFileId = fileId;
					}
					if (maxFileId > args.length - 2 || fileId <= 0)
					{
						tmpBuff.fileIds.add(-1);
						tmpBuff.bufferStartIds.add(-1);
						tmpBuff.bufferEndIds.add(-1);
						continue;
					}
					
					tmpBuff.fileIds.add(fileId-1);
					tmpBuff.bufferStartIds.add(bufferStartId);
					tmpBuff.bufferEndIds.add(bufferStartId);		
				} 
				catch (Exception ex) {
					continue;
				}
			}
						
			if (smartParts.length == 3 )
			{
				try {
					int fileId = Integer.parseInt(smartParts[0]);
					int bufferStartId = Integer.parseInt(smartParts[1]);
					int bufferEndId = Integer.parseInt(smartParts[2]);
					if(fileId > maxFileId)
					{
						maxFileId = fileId;
					}
					if (maxFileId > args.length - 2 || fileId <= 0)
					{
						tmpBuff.fileIds.add(-1);
						tmpBuff.bufferStartIds.add(-1);
						tmpBuff.bufferEndIds.add(-1);
						continue;
					}
					
					tmpBuff.fileIds.add(fileId-1);
					tmpBuff.bufferStartIds.add(bufferStartId);
					tmpBuff.bufferEndIds.add(bufferEndId);		
				} 
				catch (Exception ex) {
					continue;
				}
			}
			
			
		}
		configureReader.close();

		// TODO Auto-generated method stub
		BufferedReader[] reader = new BufferedReader[maxFileId];
		for (int fileId = 0; fileId < maxFileId; fileId++) {
			reader[fileId] = new BufferedReader(new InputStreamReader(
					new FileInputStream(args[fileId+1]), "UTF-8"));
		}
		
		List<Integer> maxColumns = new ArrayList<Integer>();
		for (int fileId = 0; fileId < maxFileId; fileId++) {
			maxColumns.add(-1);
		}
		
		for(int columnNum = 0; columnNum < tmpBuff.fileIds.size(); columnNum++)
		{
			if(tmpBuff.fileIds.get(columnNum) == -1)continue;
			int absColumn = tmpBuff.bufferStartIds.get(columnNum);
			if(absColumn < 0) absColumn = -absColumn - 1;
			if(absColumn > maxColumns.get(tmpBuff.fileIds.get(columnNum)))
			{
				maxColumns.set(tmpBuff.fileIds.get(columnNum),absColumn);
			}
			
			absColumn = tmpBuff.bufferStartIds.get(columnNum);
			if(absColumn < 0) absColumn = -absColumn -1;
			if(absColumn > maxColumns.get(tmpBuff.fileIds.get(columnNum)))
			{
				maxColumns.set(tmpBuff.fileIds.get(columnNum),absColumn);
			}
		}
		

		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[args.length - 1]), "UTF-8"));

		String[] sLines = new String[reader.length];
		

		boolean noError = true;
		while (noError) {
			String[][] sItems = new String[reader.length][];
			boolean bAllEmpty = true;
			boolean bSomeEmptyLine = false;
			for (int fileId = 0; fileId < reader.length; fileId++) {
				sLines[fileId] = reader[fileId].readLine();
				if (sLines[fileId] == null) {
					noError = false;
					break;
				}
				sItems[fileId] = sLines[fileId].split("\\s+");
				
				if(!sLines[fileId].trim().equals("") )
				{
					bAllEmpty = false;
					if (sItems[fileId].length < maxColumns.get(fileId) +1) {
						noError = false;
						System.out.println(String.format("%d\t%s", fileId, sLines[fileId]));
						break;
					}
				}
				else
				{
					bSomeEmptyLine = true;					
				}
				
				/*
				for(int idx = 0; idx < sItems[fileId].length; idx++)
				{
					if(sItems[fileId][idx].equals("_"))continue;
					String[] splitunits = sItems[fileId][idx].split("_");
					sItems[fileId][idx] = splitunits[0];
				}*/
			}
			if(bAllEmpty)
			{
				output.println();
				continue;
			}
			else if(bSomeEmptyLine)
			{
				noError = false;
			}
			
			if(!noError)break;
			String outputLine = "";
			for(int columnNum = 0; columnNum < tmpBuff.fileIds.size(); columnNum++)
			{
				if(tmpBuff.fileIds.get(columnNum) != -1)
				{
					int startId = tmpBuff.bufferStartIds.get(columnNum);
					if(startId < 0) startId = sItems[tmpBuff.fileIds.get(columnNum)].length + startId;
					
					int endId = tmpBuff.bufferEndIds.get(columnNum);
					if(endId < 0) endId = sItems[tmpBuff.fileIds.get(columnNum)].length + endId;
					
					for(int tmpIdx = startId; tmpIdx <= endId; tmpIdx++)
					{
						outputLine = outputLine + "\t" + sItems[tmpBuff.fileIds.get(columnNum)][tmpIdx];
					}
					
				}
				else
				{
					outputLine = outputLine + "\t" + "_";
				}
			}	
			if(!noError)break;
			
			outputLine = outputLine.trim();
			
			output.println(outputLine);

		}

		for (int fileId = 0; fileId < reader.length; fileId++) {
			reader[fileId].close();
		}
		output.close();

	}

}
