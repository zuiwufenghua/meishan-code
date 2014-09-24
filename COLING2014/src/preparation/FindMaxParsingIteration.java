package preparation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FindMaxParsingIteration {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		String mark1 = "LAS (excluding punc):";
		int maxIteration = 1000;
		String modelName = null;
		int schemaNum = Integer.parseInt(args[1]);
		if(args.length > 2)
		{
			try
			{
				maxIteration = Integer.parseInt(args[2]);
			}
			catch (Exception e)
			{
				modelName = args[2];
			}
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		String sLine = null;

		List<Double> bestDevParse = new ArrayList<Double>();
		List<Double> bestTestParse = new ArrayList<Double>();
		
		for(int idx = 0; idx < schemaNum; idx++)
		{
			bestDevParse.add(0.0);
			bestTestParse.add(0.0);
		}
		
		double bestTotalParseScore = 0.0;
		
		int bestIteration = -1;
		
		while ((sLine = in.readLine()) != null) {
			sLine = sLine.trim();
			if(sLine.startsWith("Test iteration "))
			{
				boolean bDevParse = false;
				int curIteration = -1;
				String[] units = sLine.split("\\s+");
				curIteration = Integer.parseInt(units[units.length-1]);
				if(curIteration > maxIteration) break;
				
				List<Double> curDevParse = new ArrayList<Double>();
				List<Double> curTestParse = new ArrayList<Double>();
				
				for(int idx = 0; idx < schemaNum; idx++)
				{
					curDevParse.add(0.0);
					curTestParse.add(0.0);
				}
				
				double curscore = 0.0;
				
				int schema = -1;
				while ((sLine = in.readLine()) != null) {
					sLine = sLine.trim();
					if(sLine.indexOf("Parsing has finished successfully.") != -1)
					{
						bDevParse = !bDevParse;
						schema = 0;
					}
									

					if(sLine.startsWith(mark1))
					{
						int sfscoreIndex = sLine.lastIndexOf("=");
						String sscorestr = sLine.substring(sfscoreIndex + 1).trim();
						curscore = Double.parseDouble(sscorestr);
						if(bDevParse)
						{
							curDevParse.set(schema, curscore);
							schema++;
						}
						else
						{
							curTestParse.set(schema, curscore);
							schema++;
						}
					}

					
					
					if(schema == schemaNum && !bDevParse)
					{
						//String outStr = String.format("cur iter: %2d:  DEV(%.2f)\tTest(%.2f)", 
						//		curIteration, curDevSEG, curTestSEG);
						//System.out.println(outStr);
						break;
					}

				}
				
				double curTotalDevScore = 0.0;
				for(int curschema = 0 ; curschema < schemaNum; curschema++)
				{
					curTotalDevScore = curTotalDevScore + curDevParse.get(curschema);
				}
				
				if( curTotalDevScore > bestTotalParseScore)
				{
					bestIteration = curIteration;
					for(int curschema = 0 ; curschema < schemaNum; curschema++)
					{
						bestDevParse.set(curschema, curDevParse.get(curschema));
						bestTestParse.set(curschema, curTestParse.get(curschema));
					}

					bestTotalParseScore = curTotalDevScore;
					/*
					if(modelName != null)
					{
						String modelFileName = String.format("%s.%d", modelName, curIteration);
						
						File file = new File(modelFileName);
						String absoluteFilePath = file.getAbsolutePath();
						int lastIndex = absoluteFilePath.lastIndexOf(File.separator);
						String targetFileName = absoluteFilePath.substring(0, lastIndex+1) + "joint.best";
						
						File filebest = new File(targetFileName);
						if (file.isFile() && file.exists()) {
							if (filebest.isFile() && filebest.exists()) { 
								filebest.delete();  							
							    file.renameTo(filebest);
						    }
					    }
						
						//System.out.println("Rename file " + modelFileName + " to " + targetFileName);
						
					}*/
				}
				else
				{
					/*
					if(modelName != null)
					{
						String modelFileName = String.format("%s.%d", modelName, curIteration);
						//System.out.println("Delete file " + modelFileName + "......");
						File file = new File(modelFileName);
						if (file.isFile() && file.exists()) {  
					        file.delete();   
					    } 
					}*/
				}
				
				
			}

		}
		
		in.close();
		
		System.out.println(String.format("best iter: %2d: ",  bestIteration));
		
		for(int idx = 0; idx < schemaNum; idx++)
		{
			String outStr = String.format("Schema %d:\tDEV(%.2f)\tTest(%.2f)", 
					idx, bestDevParse.get(idx) , bestTestParse.get(idx) );
			System.out.println(outStr);
		}
		
		


	}

}
