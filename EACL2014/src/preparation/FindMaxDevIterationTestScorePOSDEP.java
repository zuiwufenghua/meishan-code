package preparation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class FindMaxDevIterationTestScorePOSDEP {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		String markDev1 = args[1];
		String markTest1 = args[3];
		String markDev2 = args[2];
		String markTest2 = args[4];
		int maxIteration = 1000;
		String modelName = null;
		if(args.length > 5)
		{
			try
			{
				maxIteration = Integer.parseInt(args[5]);
			}
			catch (Exception e)
			{
				modelName = args[5];
			}
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		String sLine = null;

		double bestDevPOS = 0.0, bestDevDEP = 0.0;
		double bestTestPOS = 0.0, bestTestDEP = 0.0;		
		int bestIteration = -1;
		while ((sLine = in.readLine()) != null) {
			sLine = sLine.trim();
			if(sLine.startsWith("Test iteration "))
			{
				int curIteration = -1;
				String[] units = sLine.split("\\s+");
				curIteration = Integer.parseInt(units[units.length-1]);
				if(curIteration > maxIteration) break;
				
				double curDevPOS = 0.0, curDevDEP = 0.0;
				double curTestPOS = 0.0, curTestDEP = 0.0;
				
				while ((sLine = in.readLine()) != null) {
					sLine = sLine.trim();
					int firstSplitIndex = sLine.indexOf("/");
					if(sLine.startsWith("Pos Accuracy") && sLine.substring(firstSplitIndex+1).startsWith(markDev1))
					{
						int sfscoreIndex = sLine.lastIndexOf("=");
						String sscorestr = sLine.substring(sfscoreIndex + 1);
						curDevPOS = Double.parseDouble(sscorestr);
					}
					else if(sLine.startsWith("UAS (excluding punc)")&& sLine.substring(firstSplitIndex+1).startsWith(markDev2))
					{
						int sfscoreIndex = sLine.lastIndexOf("=");
						String sscorestr = sLine.substring(sfscoreIndex + 1);
						curDevDEP = Double.parseDouble(sscorestr);
						
					}
					else if(sLine.startsWith("Pos Accuracy") && sLine.substring(firstSplitIndex+1).startsWith(markTest1))
					{
						int sfscoreIndex = sLine.lastIndexOf("=");
						String sscorestr = sLine.substring(sfscoreIndex + 1);
						curTestPOS = Double.parseDouble(sscorestr);
					}
					else if(sLine.startsWith("UAS (excluding punc)")&& sLine.substring(firstSplitIndex+1).startsWith(markTest2))
					{
						int sfscoreIndex = sLine.lastIndexOf("=");
						String sscorestr = sLine.substring(sfscoreIndex + 1);
						curTestDEP = Double.parseDouble(sscorestr);
						
						break;
					}
				}
				
				if(curDevDEP > bestDevDEP || (curDevDEP == bestDevDEP && curDevPOS > bestDevPOS))
				{
					bestIteration = curIteration;
					bestDevPOS = curDevPOS;
					bestDevDEP = curDevDEP;
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
				
				
			}

		}
		
		in.close();
		
		bestDevPOS = (bestDevPOS * 100) /100;
		bestDevDEP = (bestDevDEP * 100) /100;
		bestTestPOS = (bestTestPOS * 100) /100;
		bestTestDEP = (bestTestDEP * 100) /100;
		/*
		bestDevPOS = tmpbestDevPOS*1.0/100;
		bestDevDEP = tmpbestDevDEP*1.0/100;
		bestTestPOS = tmpbestTestPOS*1.0/100;
		bestTestDEP = tmpbestTestDEP*1.0/100;
		*/
		String outStr = String.format("best iter: %2d:  %.2f %.2f %.2f %.2f", 
				bestIteration, bestDevPOS, bestDevDEP, bestTestPOS, bestTestDEP);
		System.out.println(outStr);
		


	}

}
