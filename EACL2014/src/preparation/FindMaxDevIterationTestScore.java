package preparation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class FindMaxDevIterationTestScore {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		String markDev = args[1];
		String markTest = args[2];
		int maxIteration = 1000;
		String modelName = null;
		if(args.length > 3)
		{
			try
			{
				maxIteration = Integer.parseInt(args[3]);
			}
			catch (Exception e)
			{
				modelName = args[3];
			}
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		String sLine = null;

		double bestDevSeg = 0.0, bestDevTag = 0.0;
		double bestTestSeg = 0.0, bestTestTag = 0.0;		
		int bestIteration = -1;
		while ((sLine = in.readLine()) != null) {
			sLine = sLine.trim();
			if(sLine.startsWith("Test iteration "))
			{
				int curIteration = -1;
				String[] units = sLine.split("\\s+");
				curIteration = Integer.parseInt(units[units.length-1]);
				if(curIteration > maxIteration) break;
				
				double curDevSeg = 0.0, curDevTag = 0.0;
				double curTestSeg = 0.0, curTestTag = 0.0;
				
				while ((sLine = in.readLine()) != null) {
					sLine = sLine.trim();
					int firstSplitIndex = sLine.indexOf("/");
					if(sLine.startsWith("SEG_AC: R=") && sLine.substring(firstSplitIndex+1).startsWith(markDev))
					{
						int sfscoreIndex = sLine.indexOf(", F=");
						String sscorestr = sLine.substring(sfscoreIndex + 4);
						curDevSeg = Double.parseDouble(sscorestr);
					}
					else if(sLine.startsWith("LABEL_AC: R=")&& sLine.substring(firstSplitIndex+1).startsWith(markDev))
					{
						int sfscoreIndex = sLine.indexOf(", F=");
						String sscorestr = sLine.substring(sfscoreIndex + 4);
						curDevTag = Double.parseDouble(sscorestr);
						
					}
					else if(sLine.startsWith("SEG_AC: R=") && sLine.substring(firstSplitIndex+1).startsWith(markTest))
					{
						int sfscoreIndex = sLine.indexOf(", F=");
						String sscorestr = sLine.substring(sfscoreIndex + 4);
						curTestSeg = Double.parseDouble(sscorestr);
					}
					else if(sLine.startsWith("LABEL_AC: R=")&& sLine.substring(firstSplitIndex+1).startsWith(markTest))
					{
						int sfscoreIndex = sLine.indexOf(", F=");
						String sscorestr = sLine.substring(sfscoreIndex + 4);
						curTestTag = Double.parseDouble(sscorestr);
						
						break;
					}
				}
				
				if(curDevTag > bestDevTag)
				{
					bestIteration = curIteration;
					bestDevSeg = curDevSeg;
					bestDevTag = curDevTag;
					bestTestSeg = curTestSeg;
					bestTestTag = curTestTag;
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
		
		bestDevSeg = (bestDevSeg * 100);
		bestDevTag = (bestDevTag * 100);
		bestTestSeg = (bestTestSeg * 100);
		bestTestTag = (bestTestTag * 100);
		/*
		bestDevSeg = tmpbestDevSeg*1.0/100;
		bestDevTag = tmpbestDevTag*1.0/100;
		bestTestSeg = tmpbestTestSeg*1.0/100;
		bestTestTag = tmpbestTestTag*1.0/100;
		*/
		String outStr = String.format("best iter: %2d:  %.2f %.2f %.2f %.2f", 
				bestIteration, bestDevSeg, bestDevTag, bestTestSeg, bestTestTag);
		System.out.println(outStr);
		


	}

}
