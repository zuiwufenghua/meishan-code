package acl2014;


import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

public class Conll2PSUDOConll {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		SDPCorpusReader depCorpusReader = new SDPCorpusReader();
		depCorpusReader.Init(args[0]);
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
			
		for(DepInstance depInst : depCorpusReader.m_vecInstances)
		{
			for(int idx = 0; idx < depInst.forms.size(); idx++)
			{
				depInst.heads.set(idx, idx+2);
				if(idx == depInst.forms.size()-1)
				{
					depInst.heads.set(idx, 0);
				}
			}
			
			List<String> outputs = new ArrayList<String>();
			depInst.toGoldListString(outputs);
			for(String oneLine : outputs)
			{
				output.println(oneLine);
			}
			
			output.println();
		}
		
		output.close();
	}

}
