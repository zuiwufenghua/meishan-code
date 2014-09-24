package preparation;

import mason.dep.*;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

public class GetStanfordLabels {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();
		sdpCorpusReader.Init(args[0]);
		
		Map<String, DepInstance> sentenceDepMap = new HashMap<String, DepInstance>();
		
		Set<String> labels = new TreeSet<String>();
		
		for(DepInstance inst: sdpCorpusReader.m_vecInstances)
		{
			for(String theLabel : inst.deprels)
			{
				labels.add(theLabel);
			}
		}
		
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[1]), "UTF-8"));
		for(String theLabel : labels)
		{
			writer.println("\""+ theLabel +"\",");
		}
		
		writer.println();
		for(String theLabel : labels)
		{
			writer.println("PENN_DEP_"+theLabel.toLowerCase()+",");
		}
		
		writer.close();

	}

}
