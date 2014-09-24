package preparation;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;


public class GenStanfordTrainingCorpus {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();
		sdpCorpusReader.Init(args[0]);
		
		Map<String, DepInstance> sentenceDepMap = new HashMap<String, DepInstance>();
		
		for(DepInstance inst: sdpCorpusReader.m_vecInstances)
		{
			String theSentence = "";
			String theNewSentence = "";
			for(int idx = 0; idx < inst.forms.size(); idx++)
			{
				theSentence = theSentence + " " + inst.forms.get(idx);
				String thenewform = inst.forms.get(idx);
				thenewform = thenewform.replace("/", "\\/");
				thenewform = thenewform.replace("*", "\\*");
				thenewform = thenewform.replace("(", "-LRB-");
				thenewform = thenewform.replace(")", "-RRB-");
				thenewform = thenewform.replace("{", "-LRB-");
				thenewform = thenewform.replace("}", "-RRB-");
				theNewSentence = theNewSentence + " " + thenewform;
			}
			theSentence = theSentence.trim();
			theNewSentence = theNewSentence.trim();
			sentenceDepMap.put(theSentence, inst);
			sentenceDepMap.put(theNewSentence, inst);
		}
		
		SDPCorpusReader sdpCorpusReader_target = new SDPCorpusReader(true);
		sdpCorpusReader_target.Init(args[1]);
		
		PrintWriter writer1 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[2]), "UTF-8"));
		PrintWriter writer2 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[3]), "UTF-8"));
		PrintWriter writer12 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[4]), "UTF-8"));
		
		int count = 0;
		int lost = 0;
		for(DepInstance inst : sdpCorpusReader_target.m_vecInstances)
		{
			String theSentence = "";
			
			for(int idx = 0; idx < inst.forms.size(); idx++)
			{
				theSentence = theSentence + " " + inst.forms.get(idx);
			}
			
			theSentence = theSentence.trim();
			
			if(sentenceDepMap.containsKey(theSentence))
			{
				int length = inst.forms.size();
				List<String> output1 = new ArrayList<String>();
				output1.clear();
				
				for (int i = 0; i < length; i++) {
					String tmpOut = String.format(
							"%s\t%s\t%d\t%s",inst.forms.get(i),
							inst.cpostags.get(i), 
							inst.heads.get(i)-1, inst.deprels.get(i));

					output1.add(tmpOut);
				}
				
				List<String> output2 = new ArrayList<String>();
				//sentenceDepMap.get(theSentence).toZhangYueGoldListString(ourput2);
				output2.clear();
				
				for (int i = 0; i < length; i++) {
					String tmpOut = String.format(
							"%s\t%s\t%d\t%s",inst.forms.get(i),
							inst.cpostags.get(i), 
							sentenceDepMap.get(theSentence).heads.get(i)-1, sentenceDepMap.get(theSentence).deprels.get(i).toLowerCase());

					output2.add(tmpOut);
				}
				

				
				for(String theStr : output1)
				{
					writer1.println(theStr);
					writer12.println(theStr);
				}
				
				writer1.println();
				writer12.println();
		
				for(String theStr : output2)
				{
					writer2.println(theStr);
					writer12.println(theStr);
				}
				
				writer2.println();
				writer12.println();
				
				count++;
			}
			else
			{
				System.out.println(theSentence);
				lost++;
			}
			
			
		}

		System.out.println(count);
		System.out.println(lost);
		writer1.close();
		writer2.close();
		writer12.close();
	}

}
