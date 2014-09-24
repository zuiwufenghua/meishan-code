package cl2014;


import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

public class Conn06ToZpar {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();
		sdpCorpusReader.Init(args[0]);

		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		int totalInstances = vecInstances.size();
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[args.length - 1]), "UTF-8"));
		for(int idx = 0; idx < totalInstances; idx++)
		{
			DepInstance tmpInstance = vecInstances.get(idx);
			List<String> forms = tmpInstance.forms;
			List<String> poss = tmpInstance.cpostags;
			List<Integer> heads = tmpInstance.heads;
			List<String> deprels = tmpInstance.deprels;
			
			for(int index = 0; index < forms.size(); index++)
			{
				String outputLine = String.format("%s\t%s\t%d\t%s", forms.get(index),
						poss.get(index),heads.get(index)-1,deprels.get(index));
				output.println(outputLine);
			}
			output.println();
		}
		
		output.close();
	}

}
