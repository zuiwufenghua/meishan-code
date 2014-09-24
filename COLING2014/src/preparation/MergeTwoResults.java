package preparation;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;


public class MergeTwoResults {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		SDPCorpusReader sdpCorpusReader1 = new SDPCorpusReader(true);
		sdpCorpusReader1.Init(args[0]);
		SDPCorpusReader sdpCorpusReader2 = new SDPCorpusReader(true);
		sdpCorpusReader2.Init(args[1]);
		
		assert(sdpCorpusReader1.m_vecInstances.size() == sdpCorpusReader2.m_vecInstances.size());
		
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[2]), "UTF-8"));
		
		PrintWriter writer1 = null;
		PrintWriter writer2 = null;
		boolean basicout  = false; 
		if(args.length == 5)
		{
			writer1 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[3]), "UTF-8"));
			writer2 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[4]), "UTF-8"));
			basicout = true;
		}
			
		int count  = 0;
		for(int idx = 0; idx < sdpCorpusReader1.m_vecInstances.size(); idx++)
		{
			DepInstance inst1 = sdpCorpusReader1.m_vecInstances.get(idx);
			DepInstance inst2 = sdpCorpusReader2.m_vecInstances.get(idx);
			int length = inst1.forms.size();
			List<String> output1 = new ArrayList<String>();
			output1.clear();
			
			for (int i = 0; i < length; i++) {
				String tmpOut = String.format(
						"%s\t%s\t%d\t%s",inst2.forms.get(i),
						inst2.cpostags.get(i), 
						inst1.heads.get(i)-1, inst1.deprels.get(i));

				output1.add(tmpOut);
			}
			
			List<String> output2 = new ArrayList<String>();
			//sentenceDepMap.get(theSentence).toZhangYueGoldListString(ourput2);
			output2.clear();
			
			for (int i = 0; i < length; i++) {
				String tmpOut = String.format(
						"%s\t%s\t%d\t%s",inst2.forms.get(i),
						inst2.cpostags.get(i), 
						inst2.heads.get(i)-1, inst2.deprels.get(i));

				output2.add(tmpOut);
			}
			
			for(String theStr : output1)
			{
				writer.println(theStr);
				if(basicout)
				{
					writer1.println(theStr);
				}
			}
			
			writer.println();
			
			for(String theStr : output2)
			{
				writer.println(theStr);
				if(basicout)
				{
					writer2.println(theStr);
				}
			}
			
			writer.println();
			if(basicout)
			{
				writer1.println();
				writer2.println();
			}
			count++;
		}
		
		writer.close();
		if(basicout)
		{
			writer1.close();
			writer2.close();
		}
		System.out.println(count);

	}
	
	

}
