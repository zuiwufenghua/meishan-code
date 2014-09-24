package process;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.*;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class DependencySplit {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub


		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();
		sdpCorpusReader.Init(args[0]);

		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		
		int nFold = Integer.parseInt(args[2]);
		boolean bRandom = false;
		if(nFold < 0)
		{
			bRandom = true;
			nFold = -nFold;
		}
		
				
				
		if(bRandom)
		{
			Collections.shuffle(vecInstances,  new Random(0));
		}
			
		int totalInstancesNum = vecInstances.size();
		int intervalNum = (totalInstancesNum + nFold - 1)/nFold;
		
		for(int curFold = 0; curFold < nFold; curFold++)
		{
			boolean[] bTrain = new boolean[totalInstancesNum];
			for(int idx = 0; idx < totalInstancesNum; idx++)
			{
				bTrain[idx] = true;
			}
			
			for(int idx = curFold * intervalNum; idx < (curFold+1) * intervalNum && idx < totalInstancesNum; idx++)
			{
				bTrain[idx] = false;
			}
			
			String outputFile1 = args[1] + String.format(".%d.%d", curFold+1, nFold-1);
			String outputFile2 = args[1] + String.format(".%d.%d", curFold+1, 1);
			
			PrintWriter writer1 = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile1), "UTF-8"));
			PrintWriter writer2 = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile2), "UTF-8"));
			
			for(int idx = 0; idx < totalInstancesNum; idx++)
			{
				DepInstance curInstance = vecInstances.get(idx);
				if(bTrain[idx])
				{
					for(int idxi = 0; idxi < curInstance.forms.size(); idxi++)
					{
						writer1.println(curInstance.toString(curInstance.loadParams, curInstance.maxColumn, idxi));
					}
					writer1.println();
				}
				else
				{
					for(int idxi = 0; idxi < curInstance.forms.size(); idxi++)
					{
						writer2.println(curInstance.toString(curInstance.loadParams, curInstance.maxColumn, idxi));
					}
					writer2.println();
				}
			}
			writer1.close();
			writer2.close();
		}
	}

}
