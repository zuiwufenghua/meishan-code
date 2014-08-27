package mason;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;


import mason.dep.*;
import mason.utils.MapSort;

public class GraphPureCorpusPrepare {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader(true);
		sdpCorpusReader.Init(args[0]);
		
		BufferedReader posreader = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(args[1]), "UTF-8"));
		
		BufferedReader depreader = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(args[2]), "UTF-8"));
		
		
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[3]), "UTF-8"));
		
		boolean bForTrain = false;
		if(args.length > 4 && args[4].equalsIgnoreCase("train"))
		{
			bForTrain = true;
		}
		
		List<DepInstance> allInstances = sdpCorpusReader.m_vecInstances;
		
		int totalCount_pos = 0;
		int correctCount_pos = 0;
		int wrongCount_pos = 0;
		
		int totalCount_dep = 0;
		int correctCount_dep = 0;
		int wrongCount_dep = 0;
		
		String sLine = "";
		int error_count = 0;
		
		for(int num = 0; num < allInstances.size(); num++)
		{
			DepInstance curInst = allInstances.get(num);
			int length =  curInst.size();
			List<String[]> posInputs = new ArrayList<String[]>();
			
			while((sLine = posreader.readLine()) != null)
			{
				error_count++;
				sLine = sLine.trim();
				if(sLine.isEmpty())
				{
					if(posInputs.size() == 0)
					{
						continue;
					}
					else
					{
						break;
					}
				}
				
				posInputs.add(sLine.split("\\s+"));
			}
			
			if(posInputs.size() != length)
			{
				System.out.println(String.format("error line in pos file: %d, the inst size: %d",  error_count, length));
			}
			
			List<String[]> depInputs = new ArrayList<String[]>();
			
			while((sLine = depreader.readLine()) != null)
			{
				sLine = sLine.trim();
				if(sLine.isEmpty())
				{
					if(depInputs.size() == 0)
					{
						continue;
					}
					else
					{
						break;
					}
				}
				
				depInputs.add(sLine.split("\\s+"));
			}
			
			if(depInputs.size() != length)
			{
				System.out.println(String.format("error line in dep file: %d, the inst size: %d",  error_count, length));
			}	
			
			for(int idx = 0; idx < length; idx++)
			{
				Map<String, Double> curPOSProbs = new HashMap<String, Double>();
				for(int idy = 1; idy < posInputs.get(idx).length; idy++)
				{
					String oneUnit = posInputs.get(idx)[idy];
					int splitIndex = oneUnit.lastIndexOf(":");
					String curPOS = oneUnit.substring(0, splitIndex);
					double curProb = Double.parseDouble(oneUnit.substring(splitIndex+1));
					if(curProb > 0.01)
					{
						curPOSProbs.put(curPOS, curProb);
					}
				}
				totalCount_pos++;
				String goldPOS = curInst.cpostags.get(idx);
				List<Entry<String, Double>> sortedPOSProbs = MapSort.MapDoubleSort(curPOSProbs);
				String posCand = sortedPOSProbs.get(0).getKey();
				boolean bContainGoldPOS = false;
				if(posCand.equals(goldPOS))
				{
					bContainGoldPOS = true;
				}

				for(int idy = 1; idy < sortedPOSProbs.size() && idy < 3; idy++)
				{
					String curPOSCand = sortedPOSProbs.get(idy).getKey();
					if(curPOSCand.equals(goldPOS))
					{
						bContainGoldPOS = true;
					}
					posCand = posCand + "_" + curPOSCand;
				}
				
				if(bContainGoldPOS)
				{
					correctCount_pos++;
				}
				else
				{
					wrongCount_pos++;
					if(bForTrain)
					{
						posCand = posCand + "_" + goldPOS;
					}
				}
				
				totalCount_dep++;				
				String depCand = depInputs.get(idx)[5];
				String[] depCandidates = depCand.split("_");
				boolean bContainGoldDEP = false;
				String goldDEP = String.format("%d", curInst.heads.get(idx));
				for(int idy = 0; idy < depCandidates.length; idy++)
				{
					if(goldDEP.contains(depCandidates[idy]))
					{
						bContainGoldDEP = true;
					}
				}
				
				if(bContainGoldDEP)
				{
					correctCount_dep++;
				}
				else
				{
					wrongCount_dep++;
					if(bForTrain)
					{
						depCand = depCand + "_" + goldDEP;
					}
				}
				
				writer.println(String.format("%d\t%s\t_\t%s\t_\t%s\t%s\tROOT\t_\t%s", idx+1,
						curInst.forms.get(idx), goldPOS, depCand, goldDEP, posCand));
			}
			writer.println();
						
		}
		
		System.out.println(String.format("POS Similarity: \t\t%d/%d=%f",
				correctCount_pos, totalCount_pos, correctCount_pos * 100.0 / totalCount_pos));
		System.out.println(String.format("DEP Similarity: \t\t%d/%d=%f",
				correctCount_dep, totalCount_dep, correctCount_dep * 100.0 / totalCount_dep));
		
		posreader.close();
		depreader.close();
		writer.close();
		
	}

}
