package cl2014;

import mason.srl.DepInstanceSRL;
import mason.srl.SDPSRLCorpusReader;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;

public class RemoveNounSRLs {

	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception{
		// TODO Auto-generated method stub
		SDPSRLCorpusReader sdpCorpusReader = new SDPSRLCorpusReader();
		sdpCorpusReader.Init(args[0]);
		
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"));	
		List<DepInstanceSRL> vecInstances = sdpCorpusReader.m_vecInstances;
		for(DepInstanceSRL depInst :  vecInstances)
		{
			
			if(depInst.predicateslabels.size()==0)continue;
			
			depInst.removeOneSRLType(true);
			
			if(depInst.predicateslabels.size()==0)continue;
			
			List<String> outputs = new ArrayList<String>();
			depInst.toString(outputs);
			
			for(String oneout : outputs)
			{
				writer.println(oneout);
			}
			writer.println();
		}
		
		writer.close();

	}

}
