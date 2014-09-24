package process;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

public class BerkeleyPosCoarsePosStackMerge {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
	
		File file = new File(args[0]);
		String[] subFilenames = file.list();
		for (String subFilename : subFilenames) {
			String inputFile = args[0] + File.separator + subFilename;
			String outputFile = args[1] + File.separator + subFilename;
			processOneFile(inputFile, outputFile);			
		}
	}
	public static void processOneFile(String inputFile, String outputFile) throws Exception{
		// TODO Auto-generated method stub
		System.out.println("Processing file " + inputFile + " ......");
		SDPCorpusReader sdpCorpusReaderBer = new SDPCorpusReader();
		sdpCorpusReaderBer.Init(inputFile);

		List<DepInstance> vecInstances = sdpCorpusReaderBer.m_vecInstances;
		int totalInstances = vecInstances.size();
				
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8"));
		
		for(int i = 0; i < totalInstances; i++)
		{
			DepInstance curProcessInstance =  vecInstances.get(i);
			int curInstSize = curProcessInstance.forms.size();			

			List<String> feats = new ArrayList<String>();
			for(int k = 0; k < curInstSize; k++)
			{
				String curfeat = curProcessInstance.postags.get(k) + "|" + curProcessInstance.cpostags.get(k);
				feats.add(curfeat);
			}
			
			List<String> outputResult = new ArrayList<String>();
			curProcessInstance.toGoldListString(outputResult, feats);
			
			int outsize = outputResult.size();
			if (outsize == 0)
				continue;
			for (int k = 0; k < outsize; k++) {
				writer.println(outputResult.get(k));
			}
			writer.println();
			writer.flush();
			if((i+1)%1000 == 0)
			{
				System.out.println(i+1);
				System.out.flush();
			}
		}
		System.out.println("Finished!");
		writer.close();

	}
		

}
