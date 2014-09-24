package cl2014;


import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

public class CTBPOSCheck {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();
		sdpCorpusReader.Init(args[0]);

		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		int totalInstances = vecInstances.size();
		
		Set<String> poslabels = new TreeSet<String>();
		for(int idx = 0; idx < totalInstances; idx++)
		{
			DepInstance tmpInstance = vecInstances.get(idx);

			List<String> poss = tmpInstance.cpostags;
			for(String thePOS : poss)
			{
				poslabels.add(thePOS);
			}

		}
		
		for(String thePOS : poslabels)
		{
			System.out.println(thePOS);
		}
	}
		
		
}
