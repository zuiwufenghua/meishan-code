package cl2014;



import mason.srl.DepInstanceSRL;
import mason.srl.SDPSRLCorpusReader;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ChunkSRL2DEPSRL {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		SDPSRLCorpusReader sdpCorpusReader = new SDPSRLCorpusReader();
		sdpCorpusReader.Init(args[0]);

		List<DepInstanceSRL> vecInstances = sdpCorpusReader.m_vecInstances;
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"));

		for(DepInstanceSRL depInst :  vecInstances)
		{
			List<String> outputs = new ArrayList<String>();
			depInst.toDepFormat(outputs);
			
			for(String oneline : outputs)
			{
				writer.println(oneline);
			}
			
			writer.println();
		}
		
		
		writer.close();
	}

}
