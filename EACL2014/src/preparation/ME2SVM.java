package preparation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ME2SVM {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		String alphaFile = args[0];
		String transfile = args[1];
		Map<String, Integer> featsMap = new HashMap<String, Integer>();
		Map<String, Integer> labelsMap = new HashMap<String, Integer>();
		
		BufferedReader in = null;
		String sLine = null;
		
		try
		{
			in = new BufferedReader(new InputStreamReader( new FileInputStream(alphaFile), "UTF-8"));
			while ((sLine = in.readLine()) != null) {
				String newLine = sLine.trim();
				if(!newLine.equals(""))
				{
					String[] units = newLine.split("\\s+");
					if(units.length != 3) continue;
					try{
						if(units[0].equalsIgnoreCase("feat"))
						{
							featsMap.put(units[1], Integer.parseInt(units[2]));
						}
						else if(units[0].equalsIgnoreCase("label"))
						{
							labelsMap.put(units[1], Integer.parseInt(units[2]));
						}
						else
						{
							
						}
					}
					catch(Exception ex)
					{
						continue;
					}
				}
			}
			
			in.close();
		}
		catch(Exception ex)
		{
			System.out.println("No alpha file found!");
		}
		
		int[] featsValueCheck = new int[featsMap.size()];
		for(int idx = 0; idx < featsValueCheck.length; idx++)featsValueCheck[idx] = 0;
		for(String theKey : featsMap.keySet())
		{
			int theValue = featsMap.get(theKey);
			if(theValue < 1 || theValue > featsValueCheck.length || featsValueCheck[theValue-1] == 1)
			{
				System.out.println("Invalid Feature Map");
				return;
			}
			else
			{
				featsValueCheck[theValue-1] = 1;
			}
		}
		
		int[] labelsValueCheck = new int[labelsMap.size()];
		for(int idx = 0; idx < labelsValueCheck.length; idx++)labelsValueCheck[idx] = 0;
		for(String theKey : labelsMap.keySet())
		{
			int theValue = labelsMap.get(theKey);
			if(theValue < 0 || theValue >= labelsValueCheck.length || labelsValueCheck[theValue] == 1)
			{
				System.out.println("Invalid label Map");
				return;
			}
			else
			{
				labelsValueCheck[theValue] = 1;
			}
		}
		
		
		in = new BufferedReader(new InputStreamReader( new FileInputStream(transfile), "UTF-8"));
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"));
		while ((sLine = in.readLine()) != null) {
			sLine = sLine.trim();
			if(sLine.equals(""))
			{
				continue;
			}
			
			String[] theUnits = sLine.split("\\s+");
			if(theUnits.length < 2)continue;
			//{
				int label = -1;
				if(labelsMap.containsKey(theUnits[0]))
				{
					label = labelsMap.get(theUnits[0]);
				}
				else
				{
					label = labelsMap.size();
					labelsMap.put(theUnits[0], label);
				}
			//}
			
			Map<Integer, Double> feats = new TreeMap<Integer, Double>();
			for(int idx = 1; idx < theUnits.length; idx++)
			{
				int curFeat = 0;
				if(featsMap.containsKey(theUnits[idx]))
				{
					curFeat = featsMap.get(theUnits[idx]);
				}
				else
				{
					curFeat = featsMap.size()+1;
					featsMap.put(theUnits[idx], curFeat);
				}
				
				if(!feats.containsKey(curFeat))
				{
					feats.put(curFeat, 0.0);
				}
				feats.put(curFeat, feats.get(curFeat) + 1.0);
			}
			
			String outputline = String.format("%d", label);
			
			for(Integer curFeat : feats.keySet())
			{
				outputline = outputline + " " + String.format("%d:%f", curFeat, feats.get(curFeat));
			}
			
			output.println(outputline);			
		}
		

		output.close();
		
		output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(alphaFile), "UTF-8"));
		output.println("#####alpha@file########");
		for(String theLabel : labelsMap.keySet())
		{
			output.println(String.format("label %s %d", theLabel, labelsMap.get(theLabel)));
		}
		output.println();
		for(String theFeat : featsMap.keySet())
		{
			output.println(String.format("feat %s %d", theFeat, featsMap.get(theFeat)));
		}
		
		
		output.close();
		
	}

}
