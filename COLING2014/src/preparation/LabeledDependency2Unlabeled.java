package preparation;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;


public class LabeledDependency2Unlabeled {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		SDPCorpusReader sdpCorpusReader_target = new SDPCorpusReader(true);
		sdpCorpusReader_target.Init(args[0]);
		
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[1]), "UTF-8"));
		
		int outType = Integer.parseInt(args[2]);  // 0, penn, 1 stanford, 2 mixed
		
		int iCount = 0;
		
		for(DepInstance inst : sdpCorpusReader_target.m_vecInstances)
		{
			int length = inst.forms.size();
			List<String> output = new ArrayList<String>();
			output.clear();
			
			String rootLabel = "ROOT";
			
			if(outType == 1 || (outType == 2 && iCount%2 == 1))
			{
				rootLabel = "root";
			}
			
			for (int i = 0; i < length; i++) {
				String tmpOut = String.format(
						"%s\t%s\t%d\t%s",inst.forms.get(i),
						inst.cpostags.get(i), 
						inst.heads.get(i)-1, rootLabel);

				output.add(tmpOut);
			}
			
			for(String theStr : output)
			{
				writer.println(theStr);
			}
			
			writer.println();
			iCount++;
		}
		
		
		writer.close();

	}

}
