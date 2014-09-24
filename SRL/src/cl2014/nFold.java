package cl2014;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class nFold {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		List<List<String>> examples = new ArrayList<List<String>>();
		int readFilePos = 0;
		if(args[0].equals("emptyline"))readFilePos = 1;
		
		BufferedReader in = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(args[readFilePos]), "UTF-8"));
		String sLine = null;
		
		List<String> aSent = new ArrayList<String>();
		while ((sLine = in.readLine()) != null) {
			String newLine = sLine.trim();
			if(readFilePos == 1)
			{
				if(newLine.equals(""))
				{
					examples.add(aSent);
					aSent = new ArrayList<String>();
				}
				else
				{
					aSent.add(newLine);
				}
			}
			else
			{
				aSent.add(newLine);
				examples.add(aSent);
				aSent = new ArrayList<String>();
			}
		}
		in.close();
		
		int exampleSize = examples.size();
		int foldNum = Integer.parseInt(args[readFilePos+1]);
		int oneFoldSentNum = exampleSize/foldNum;
		
		for(int idx = 0; idx < foldNum; idx++)
		{
			String currentTestFile = args[readFilePos] + String.format(".test%d", idx+1);
			String currentTrainFile = args[readFilePos] + String.format(".train%d", idx+1);
			
			PrintWriter outputTest = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(currentTestFile), "UTF-8"));
			
			PrintWriter outputTrain = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(currentTrainFile), "UTF-8"));
			int startidx = idx * oneFoldSentNum;
			int endidx = idx * oneFoldSentNum + oneFoldSentNum -1;
			if(idx == foldNum - 1) endidx = exampleSize-1;
			
			for(int iy = 0; iy < exampleSize; iy++)
			{
				PrintWriter output = outputTrain;
				if(iy >= startidx && iy <= endidx)
				{
					output = outputTest;
				}
				
				List<String> curEx = examples.get(iy);
				for(String aLine : curEx)
				{
					output.println(aLine);
				}
				if(readFilePos == 1)
				{
					output.println();
				}
				
			}
			
			
			
			outputTest.close();
			outputTrain.close();
			
		}
		

	}

}
