package cl2014;


import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

public class DEP2WordPOS {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();
		sdpCorpusReader.Init(args[0]);

		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"));
		for(DepInstance depInst :  vecInstances)
		{
			List<String> words = depInst.forms;
			List<String> poss = depInst.cpostags;
			String oneLine = "";
			for(int index = 0; index < words.size(); index++)
			{
				oneLine = oneLine + " " + words.get(index) + "_" + poss.get(index);
			}
			writer.println(oneLine.trim());
		}
		
		writer.close();

	}

}
