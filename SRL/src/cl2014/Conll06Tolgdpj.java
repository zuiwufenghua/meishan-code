package cl2014;


import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

public class Conll06Tolgdpj {

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
				String curWord = forms.get(index);
				String curCharSplit = curWord.substring(0,1);
				for(int idy = 1; idy < curWord.length(); idy++)
				{
					curCharSplit = curCharSplit + "##" + curWord.substring(idy,idy+1);
				}
				String outputLine = String.format("%d\t%s\t%s\t%s\t_\t_\t%d\t%s\t_\t%s", 
						index + 1, forms.get(index),curCharSplit, 
						poss.get(index),heads.get(index),deprels.get(index), poss.get(index));
				output.println(outputLine);
			}
			output.println();
		}
		
		output.close();
	}

}
