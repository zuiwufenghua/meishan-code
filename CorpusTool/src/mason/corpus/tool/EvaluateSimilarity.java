package mason.corpus.tool;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

public class EvaluateSimilarity {

	/**
	 * @param args
	 */
	// arg0:config, arc1:gold, arg2:pred arg3:out
	//config example:
	//map 2(column) 2(column)
	//map 3 8
	//exception 2(column) 2(value) (always from gold file).
	//exception 3 5
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Map<Integer, Integer> evaluateItems = new HashMap<Integer, Integer>();
		Map<Integer, Set<String>> evaluateExceptions = new HashMap<Integer, Set<String>>();
		
		
		BufferedReader configureReader = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(args[0]), "UTF-8"));
		String sLine = "";
		while ((sLine = configureReader.readLine()) != null) {
			if (sLine.trim().equals(""))
				continue;
			String[] smallunits = sLine.trim().split("\\s+");
			if(smallunits[0].equals("map"))
			{
				evaluateItems.put(Integer.parseInt(smallunits[1]), Integer.parseInt(smallunits[2]));
			}
			else if(smallunits[0].equals("exception"))
			{
				int goldcolumn = Integer.parseInt(smallunits[1]);
				if(!evaluateExceptions.containsKey(goldcolumn))
				{
					evaluateExceptions.put(goldcolumn, new HashSet<String>());
				}
				evaluateExceptions.get(goldcolumn).add(smallunits[2]);
			}
			
		}
		
		configureReader.close();
		
		BufferedReader goldReader = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(args[1]), "UTF-8"));
		
		BufferedReader predReader = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(args[2]), "UTF-8"));
		String sLineGold = "";
		String sLinePred = "";
		
		int totalCount = 0;
		int correctCount = 0;
		int wrongcount = 0;
		int line = 0;

		while ((sLineGold = goldReader.readLine()) != null
				&& (sLinePred = predReader.readLine()) != null) {
			line++;
			if(sLineGold.trim().equals(""))
			{
				if(!sLinePred.trim().equals(""))
				{
					System.out.println("error pred line: " + sLinePred.trim());
					return;
				}
				continue;
			}
			
			String[] smallunitsGold = sLineGold.trim().split("\\s+");
			String[] smallunitsPred = sLinePred.trim().split("\\s+");
			try
				{
				boolean bException = false;
				for(Integer col : evaluateExceptions.keySet())
				{
					if(evaluateExceptions.get(col).contains(smallunitsGold[col]))
					{
						bException = true;
						break;
					}
				}
				if(!bException)
				{
					totalCount++;
					boolean bPredCorrect = true;
					
					for(Integer goldCol : evaluateItems.keySet())
					{
						boolean bCurrentPredCorrect = false;
						int predCol = evaluateItems.get(goldCol);
						String goldResult = smallunitsGold[goldCol];
						
						String[] predResults = smallunitsPred[predCol].split("_");
						for(String predResult : predResults)
						{
							if(predResult.equalsIgnoreCase(goldResult))
							{
								bCurrentPredCorrect = true;
								break;
							}
						}
						
						if(!bCurrentPredCorrect)
						{
							bPredCorrect = false;
							wrongcount++;
							break;
						}
					}
					
					if(bPredCorrect)
					{
						correctCount++;
					}
					else
					{
						
					}
				}
				}
			catch (Exception ex)
			{
				System.out.println("error gold line: " + sLineGold.trim());
				System.out.println("error pred line: " + sLinePred.trim());
				System.out.println("error pred line: " +String.format("%d", line));
			}
		}
		
		System.out.println(String.format("Similarity: \t\t%d/%d=%f",
				correctCount, totalCount, correctCount * 100.0 / totalCount));
		
		goldReader.close();
		predReader.close();
	}

}
