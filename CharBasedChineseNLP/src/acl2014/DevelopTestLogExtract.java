package acl2014;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class DevelopTestLogExtract {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		String mark1 = "segmentation:";
		String mark2 = "tagging:";
		String mark3 = "las:";
		String mark4 = "wordstructure:";
		int maxIteration = 1000;
		String modelName = null;
		if(args.length > 1)
		{
			try
			{
				maxIteration = Integer.parseInt(args[1]);
			}
			catch (Exception e)
			{
				modelName = args[5];
			}
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		String sLine = null;

		double bestDevSEG = 0.0, bestDevPOS = 0.0, bestDevDEP = 0.0, bestDevWS = 0.0;
		double bestTestSEG = 0.0, bestTestPOS = 0.0, bestTestDEP = 0.0, bestTestWS = 0.0;	
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
				
				double curDevSEG = 0.0, curDevPOS = 0.0, curDevDEP = 0.0, curDevWS = 0.0;
				double curTestSEG = 0.0, curTestPOS = 0.0, curTestDEP = 0.0, curTestWS = 0.0;
				double curscore = 0.0;
				
				while ((sLine = in.readLine()) != null) {
					sLine = sLine.trim();
					if(sLine.indexOf("Parsing has finished successfully.") != -1)
					{
						bDevParse = !bDevParse;
					}
									

					if(sLine.startsWith(mark1))
					{
						int sfscoreIndex = sLine.lastIndexOf("F1:");
						String sscorestr = sLine.substring(sfscoreIndex + 3).trim();
						curscore = Double.parseDouble(sscorestr);
						if(bDevParse)
						{
							curDevSEG = curscore;
						}
						else
						{
							curTestSEG = curscore;
						}
					}
					else if(sLine.startsWith(mark2))
					{
						int sfscoreIndex = sLine.lastIndexOf("F1:");
						String sscorestr = sLine.substring(sfscoreIndex + 3).trim();
						curscore = Double.parseDouble(sscorestr);
						if(bDevParse)
						{
							curDevPOS = curscore;
						}
						else
						{
							curTestPOS = curscore;
						}
						
					}
					else if(sLine.startsWith(mark3))
					{
						int sfscoreIndex = sLine.lastIndexOf("F1:");
						String sscorestr = sLine.substring(sfscoreIndex + 3).trim();
						curscore = Double.parseDouble(sscorestr);
						if(bDevParse)
						{
							curDevDEP = curscore;
						}
						else
						{
							curTestDEP = curscore;
						}
					}
					else if(sLine.startsWith(mark4))
					{
						int sfscoreIndex = sLine.lastIndexOf("F1:");
						String sscorestr = sLine.substring(sfscoreIndex + 3).trim();
						curscore = Double.parseDouble(sscorestr);
						if(bDevParse)
						{
							curDevWS = curscore;
						}
						else
						{
							curTestWS = curscore;
						}
					}
					
					if(curDevSEG > 0.0001 && curDevPOS > 0.0001 && curDevDEP > 0.0001 &&
					   curTestSEG > 0.0001 && curTestPOS > 0.0001 && curTestDEP > 0.0001)
					{
						break;
					}

				}
				

				
				if(curDevDEP > bestDevDEP || (curDevDEP == bestDevDEP && curDevPOS > bestDevPOS)
				  || (curDevDEP == bestDevDEP && curDevPOS == bestDevPOS && curDevSEG > bestDevSEG))
				{
					bestIteration = curIteration;
					bestDevSEG = curDevSEG;
					bestDevPOS = curDevPOS;
					bestDevDEP = curDevDEP;
					bestTestSEG = curTestSEG;
					bestTestPOS = curTestPOS;
					bestTestDEP = curTestDEP;
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
						
					}
				}
				else
				{
					if(modelName != null)
					{
						String modelFileName = String.format("%s.%d", modelName, curIteration);
						//System.out.println("Delete file " + modelFileName + "......");
						File file = new File(modelFileName);
						if (file.isFile() && file.exists()) {  
					        file.delete();   
					    } 
					}
				}
				
				
				curDevSEG = (curDevSEG * 100) /100;
				curDevPOS = (curDevPOS * 100) /100;
				curDevDEP = (curDevDEP * 100) /100;
				curDevWS = (curDevWS * 100) /100;
				curTestSEG = (curTestSEG * 100) /100;
				curTestPOS = (curTestPOS * 100) /100;
				curTestDEP = (curTestDEP * 100) /100;
				curTestWS = (curTestWS * 100) /100;
				
				String outStr = String.format("cur iter: %2d:  DEV(%.2f %.2f %.2f %.2f)\tTest(%.2f %.2f %.2f %.2f)", 
						curIteration, curDevSEG, curDevPOS, curDevDEP, curDevWS, curTestSEG, curTestPOS, curTestDEP, curTestWS);
				System.out.println(outStr);
				
				
				
				
			}

		}
		
		in.close();
		
		bestDevSEG = (bestDevSEG * 100) /100;
		bestDevPOS = (bestDevPOS * 100) /100;
		bestDevDEP = (bestDevDEP * 100) /100;
		bestTestSEG = (bestTestSEG * 100) /100;
		bestTestPOS = (bestTestPOS * 100) /100;
		bestTestDEP = (bestTestDEP * 100) /100;
		/*
		bestDevPOS = tmpbestDevPOS*1.0/100;
		bestDevDEP = tmpbestDevDEP*1.0/100;
		bestTestPOS = tmpbestTestPOS*1.0/100;
		bestTestDEP = tmpbestTestDEP*1.0/100;
		*/
		String outStr = String.format("best iter: %2d:  %.2f %.2f %.2f %.2f %.2f %.2f", 
				bestIteration, bestDevSEG, bestDevPOS, bestDevDEP, bestTestSEG, bestTestPOS, bestTestDEP);
		System.out.println(outStr);
		


	}

}
