package WordStructure;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

import edu.berkeley.nlp.syntax.Tree;


public class ConllLeftWordStructure {

	/**
	 * @param args
	 */
	/*
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();
		sdpCorpusReader.Init(args[0]);
		
		List<DepInstance> depInstances = sdpCorpusReader.m_vecInstances;

		PrintWriter writercomp = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
				args[1]), "UTF-8"));
		int processedSentence = 0;
		for(DepInstance inst : depInstances)
		{
			int wordNum = inst.size();
			for(int i = 0; i < wordNum; i++)
			{
				String theWord = inst.forms.get(i);
				String thePos = inst.postags.get(i);
				
				//Tree<String> curTree = ConllAddWordStructure.getSingleCharacterTree(theWord, thePos);
				
				String oneLine = String.format("%d\t%s\t_\t%s\t_\t%s\t%d\t_\t_\t_", i+1,
						inst.forms.get(i), inst.cpostags.get(i), curTree.toString(), inst.heads.get(i));
				writercomp.println(oneLine);
			}
			writercomp.println();
			writercomp.flush();
		}
		
		writercomp.close();

	}*/

}
