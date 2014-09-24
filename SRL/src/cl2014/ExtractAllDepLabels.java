package cl2014;


import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

public class ExtractAllDepLabels {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();
		sdpCorpusReader.Init(args[0]);

		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		int totalInstances = vecInstances.size();
		Set<String> deplabels = new TreeSet<String>();
		String rootLabel = "";
		
		
		for(int idx = 0; idx < totalInstances; idx++)
		{
			DepInstance tmpInstance = vecInstances.get(idx);
			List<Integer> heads = tmpInstance.heads;
			List<String> deprels = tmpInstance.deprels;
			for(int idy = 0; idy < heads.size(); idy++)
			{
				if(heads.get(idy) == 0)
				{
					if(rootLabel.equals(""))
					{
						rootLabel = deprels.get(idy);
					}
					else
					{
						if(!rootLabel.equals(deprels.get(idy)))
						{
							System.out.println("Incorrect ROOT Label: " + deprels.get(idy));
						}
					}
				}
				else
				{
					deplabels.add(deprels.get(idy));
				}
			}
		
		}
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"));
		
		output.println("// Copyright (C) University of Oxford 2010");
		output.println("/****************************************************************");
		output.println(" *                                                              * ");
		output.println("* xxxx.h - the penn treebank style dependency labels           * ");
		output.println("*                                                              * ");
		output.println("* Author: xxx  xxxx                                            * ");
		output.println(" *                                                              * ");
		output.println("* Computing Laboratory, Oxford. 2008.07                        * ");
		output.println(" *                                                              * ");
		output.println(" ****************************************************************/");
		output.println("const std::string PENN_DEP_STRINGS[] = {");
		output.println("   \"-NONE-\",");
		output.println(String.format("   \"%s\",", rootLabel));
		for(String deplabel : deplabels)
		{
			output.println(String.format("   \"%s\",", deplabel));
		}		
		output.println("};");
		output.println();
		output.println();
		
		output.println("enum PENN_DEP_LABELS {");
		output.println("   PENN_DEP_NONE=0,");
		output.println(String.format("   PENN_DEP_%s,", rootLabel));
		for(String deplabel : deplabels)
		{
			output.println(String.format("   PENN_DEP_%s,", deplabel.replace("-", "_")));
		}
		output.println("   PENN_DEP_COUNT");
		output.println("};");
		output.println();
		output.println();
		
		int totalLabel = deplabels.size()+2;
		int bits = 0;
		while(totalLabel != 0)
		{
			totalLabel = totalLabel/2;
			bits++;
		}
		
		output.println(String.format("const unsigned long PENN_DEP_COUNT_BITS = %d;",bits));
		
		output.close();

	}

}
