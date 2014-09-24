package preparation;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

import mason.dep.*;

public class FindNonProjectiveTrees {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();
		sdpCorpusReader.Init(args[0]);
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[1]), "UTF-8"));
		for(DepInstance inst: sdpCorpusReader.m_vecInstances)
		{
			if(!IsProjectiveDependencyTree(inst))
			{
				int length = inst.forms.size();
				
				for (int i = 0; i < length; i++) {
					String tmpOut = String.format(
							"%d\t%s\t_\t%s\t%s\t_\t%d\t%s\t_\t_",i+1, inst.forms.get(i),
							inst.cpostags.get(i), inst.cpostags.get(i), 
							inst.heads.get(i), inst.deprels.get(i));

					writer.println(tmpOut);
				}
				
				writer.println();
			}
		}
		
		writer.close();

	}
	
	
	public static boolean IsProjectiveDependencyTree(DepInstance inst)
	{
		int length = inst.forms.size();
		for ( int i=0; i<length; ++i ) {
			int mini = Math.min(i, inst.heads.get(i)-1);
			int maxi = Math.max(i, inst.heads.get(i)-1);
			for ( int j=mini+1; j<maxi; ++j ) 
			{
				if (inst.heads.get(j)-1<mini||inst.heads.get(j)-1>maxi)
				{
					return false;
				}
			}
		}
		
		return true;

	}
}
