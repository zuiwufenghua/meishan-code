package cl2014;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

import mason.srl.*;
import mason.dep.*;

public class DepYueFormatReplaceDependencies {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader(true);
		sdpCorpusReader.Init(args[0]);
		
		
		SDPSRLCorpusReader sdpsrlCorpusReader = new SDPSRLCorpusReader();
		sdpsrlCorpusReader.Init(args[1]);
		
		if(sdpCorpusReader.m_vecInstances.size() != sdpsrlCorpusReader.m_vecInstances.size())
		{
			System.out.println(String.format("The number of instances is not equal: dep: %d  v.s. depsrl:%d ", 
					sdpCorpusReader.m_vecInstances.size(), sdpsrlCorpusReader.m_vecInstances.size()));
			return;
		}
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"));
		
		for(int idx = 0; idx < sdpCorpusReader.m_vecInstances.size(); idx++)
		{
			DepInstance depInst = sdpCorpusReader.m_vecInstances.get(idx);
			DepInstanceSRL depInstSRL = sdpsrlCorpusReader.m_vecInstances.get(idx);
			if(depInst.forms.size() != depInstSRL.forms.size())
			{
				System.out.println(String.format("The inst size is not equal: dep: %d  v.s. depsrl:%d ", 
						depInst.forms.size(), depInstSRL.forms.size()));
				return;
			}
			
			for(int idy = 0; idy < depInst.forms.size(); idy++)
			{
				if(!depInst.forms.get(idy).equals(depInstSRL.forms.get(idy)))
				{
					System.out.println(String.format("The inst form is not equal: dep: %d[]  v.s. depsrl:%d[] ", 
							idy, depInst.forms.get(idy), idy, depInstSRL.forms.get(idy)));
					return;
				}
				
				//if(!depInstSRL.postags.get(idy).equals(depInst.postags.get(idy)))
				//{
				//	System.out.println(String.format("The inst postags is not equal: dep: %d[]  v.s. depsrl:%d[] ", 
				//			idy, depInst.postags.get(idy), idy, depInstSRL.postags.get(idy)));
				//	return;
				//}
				
				depInstSRL.postags.set(idy, depInst.postags.get(idy));
				depInstSRL.heads.set(idy, depInst.heads.get(idy));
				depInstSRL.deprels.set(idy, depInst.deprels.get(idy));
			}
			
			List<String> stroutputs = new ArrayList<String>();
			
			depInstSRL.toString(stroutputs);
			
			for(String oneline : stroutputs)
			{
				output.println(oneline);
			}
			output.println();
		}
		
		output.close();

	}
	
	


}
