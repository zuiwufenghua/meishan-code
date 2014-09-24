package cl2014;

import mason.srl.DepInstanceSRL;
import mason.srl.SDPSRLCorpusReader;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class DEPSRL2Evaluate {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		SDPSRLCorpusReader sdpCorpusReader = new SDPSRLCorpusReader();
		sdpCorpusReader.Init(args[0]);

		List<DepInstanceSRL> vecInstances = sdpCorpusReader.m_vecInstances;
		//PrintWriter writer_noun = new PrintWriter(new OutputStreamWriter(
		//		new FileOutputStream(args[1]+".noun"), "UTF-8"));
		PrintWriter writer_verb = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"));
		for(DepInstanceSRL depInst :  vecInstances)
		{
			List<String> outputsVerb = new ArrayList<String>();
			List<String> outputsNoun = new ArrayList<String>();
			depInst.classiffypredicateslabels();
			depInst.toEvaluateFormat(outputsVerb, outputsNoun);
			if(outputsVerb.size() != 0)
			{
				for(String oneline : outputsVerb)
				{
					writer_verb.println(oneline);
				}
			
			
				writer_verb.println();
			}
			
			//if(outputsNoun.size() != 0)
			//{
			//	for(String oneline : outputsNoun)
			//	{
			//		writer_noun.println(oneline);
			//	}
			//	
			//	writer_noun.println();
			//}
		}
		
		//writer_noun.close();
		writer_verb.close();

	}

}
