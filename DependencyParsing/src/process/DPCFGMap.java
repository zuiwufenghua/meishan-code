package process;


import java.io.*;
import java.util.*;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

import edu.berkeley.nlp.syntax.*;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class DPCFGMap {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Tree<String>> senCFGMap = new HashMap<String, Tree<String>>();
		BufferedReader cfgreader = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8"));

		String sLine = null;
		while ((sLine = cfgreader.readLine()) != null) {
			PennTreeReader reader = new PennTreeReader(new StringReader(
					sLine.trim()));

			while (reader.hasNext()) {
				Tree<String> tree = reader.next();
				List<String> forms = tree.getYield();
				String sentence = "";
				for (String theWord : forms) {
					sentence = sentence + " " + theWord;
				}
				sentence = sentence.trim();
				senCFGMap.put(sentence, tree);
			}
		}
		cfgreader.close();

		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();
		sdpCorpusReader.Init(args[1]);
		

		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		int totalInstances = vecInstances.size();

		Map<String, Integer> dp2cfg_freq = new HashMap<String, Integer>();

		for (int i = 0; i < totalInstances; i++) {
			DepInstance tmpInstance = vecInstances.get(i);

			Integer[] predheads = new Integer[tmpInstance.heads.size()];
			tmpInstance.heads.toArray(predheads);
			String[] predlabels = new String[tmpInstance.deprels.size()];
			tmpInstance.deprels.toArray(predlabels);
			String[] predForms = new String[tmpInstance.forms.size()];
			tmpInstance.forms.toArray(predForms);

			String sentence = "";
			for (String theWord : predForms) {
				sentence = sentence + " " + theWord;
			}
			sentence = sentence.trim();
			Tree<String> tree = senCFGMap.get(sentence);

			if (tree == null) {
				System.out.println(sentence);
				continue;
			}
			List<String> words = tree.getYield();
			int n = tree.getYield().size() + 1;
			if (n != predForms.length + 1) {
				System.out.println(sentence + "\t" + tree.toString());
				continue;
			}
			
			//writer.println(tree.toString());
			

			List<List<Integer>> childs = new ArrayList<List<Integer>>();

			for (int j1 = 0; j1 < tmpInstance.size(); j1++) {
				childs.add(new ArrayList<Integer>());
			}

			for (int j1 = 0; j1 < tmpInstance.size(); j1++) {
				if (predheads[j1] == 0)
					continue;
				childs.get(predheads[j1]-1).add(j1);
				String dppattern = String.format("h->m");

				int smaller = j1 < predheads[j1]-1 ? j1 : predheads[j1]-1;
				int bigger = j1 < predheads[j1]-1 ? predheads[j1]-1 : j1;

				Constituent<String> contsituent = tree.getLeastCommonAncestorConstituent(smaller, bigger + 1);
				Tree<String> spantree = tree.getTopTreeForSpan(contsituent.getStart(), contsituent.getEnd());
				List<Integer> tmpIds = new ArrayList<Integer>();
				tmpIds.add(smaller);
				tmpIds.add(bigger);
				Tree<String> subtree = spantree.getSubTrees(tmpIds, - contsituent.getStart() + 1);

				String cfgpattern = simplify(subtree.toString());					
				cfgpattern = cfgpattern.replaceAll(" ", "");
				Tree<String> gentree = PennTreeReader.parseEasy(cfgpattern, false);
				
				
				String transformpattern = dppattern + "|" + cfgpattern;
				if(dp2cfg_freq.containsKey(transformpattern))
				{
					dp2cfg_freq.put(transformpattern, dp2cfg_freq.get(transformpattern)+1);
				}
				else
				{
					dp2cfg_freq.put(transformpattern, 1);
				}
				
			}

			for (int j1 = 0; j1 < tmpInstance.size(); j1++) {
				for (int j2 = 0; j2 < childs.get(j1).size() - 1; j2++) {
					int s1 = childs.get(j1).get(j2);
					int s2 = childs.get(j1).get(j2 + 1);

					int smaller = s1 < s2 ? s1 : s2;
					int bigger = s1 < s2 ? s2 : s1;
					int midder = -1;

					if (j1 < smaller) {
						midder = smaller;
						smaller = j1;
					} else if (j1 > smaller && j1 < bigger) {
						midder = j1;
					} else {
						midder = bigger;
						bigger = j1;
					}

					String dppattern = String.format("h->m1&h->m2");
					Constituent<String> contsituent = tree.getLeastCommonAncestorConstituent(smaller, bigger + 1);
					Tree<String> spantree = tree.getTopTreeForSpan(contsituent.getStart(), contsituent.getEnd());
					List<Integer> tmpIds = new ArrayList<Integer>();
					tmpIds.add(smaller);
					tmpIds.add(midder);
					tmpIds.add(bigger);
					Tree<String> subtree = spantree.getSubTrees(tmpIds, - contsituent.getStart() + 1);

					String cfgpattern = simplify(subtree.toString());					
					cfgpattern = cfgpattern.replaceAll(" ", "");
					
					String transformpattern = dppattern + "|" + cfgpattern;
					
					if(dp2cfg_freq.containsKey(transformpattern))
					{
						dp2cfg_freq.put(transformpattern, dp2cfg_freq.get(transformpattern)+1);
					}
					else
					{
						dp2cfg_freq.put(transformpattern, 1);
					}
				}
			}

			for (int j1 = 1; j1 < tmpInstance.size(); j1++) {
				int s1 = predheads[j1]-1;
				if (s1 == -1)
					continue;
				int s2 = predheads[s1]-1;
				if (s2 == -1)
					continue;

				int smaller = s1 < s2 ? s1 : s2;
				int bigger = s1 < s2 ? s2 : s1;
				int midder = -1;

				if (j1 < smaller) {
					midder = smaller;
					smaller = j1;
				} else if (j1 > smaller && j1 < bigger) {
					midder = j1;
				} else {
					midder = bigger;
					bigger = j1;
				}

				String dppattern = String.format("g->h->m");

				Constituent<String> contsituent = tree.getLeastCommonAncestorConstituent(smaller, bigger + 1);
				Tree<String> spantree = tree.getTopTreeForSpan(contsituent.getStart(), contsituent.getEnd());
				List<Integer> tmpIds = new ArrayList<Integer>();
				tmpIds.add(smaller);
				tmpIds.add(midder);
				tmpIds.add(bigger);
				Tree<String> subtree = spantree.getSubTrees(tmpIds, - contsituent.getStart() + 1);

				String cfgpattern = simplify(subtree.toString());					
				cfgpattern = cfgpattern.replaceAll(" ", "");
				String transformpattern = dppattern + "|" + cfgpattern;
				
				if(dp2cfg_freq.containsKey(transformpattern))
				{
					dp2cfg_freq.put(transformpattern, dp2cfg_freq.get(transformpattern)+1);
				}
				else
				{
					dp2cfg_freq.put(transformpattern, 1);
				}
			}

		}
		
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[2]), "UTF-8"));		
		for(String type : dp2cfg_freq.keySet())
		{
			writer.println(String.format("%s\t%d", type, dp2cfg_freq.get(type)));
		}
		writer.close();

	}
	
	public static String simplify(String input)
	{
		int lastIndex = 0;
		String output = "";
		while(lastIndex < input.length())
		{
			int bracketfirstIndex = input.indexOf(")", lastIndex);
			if(bracketfirstIndex < 0)break;
			int i = bracketfirstIndex-1;
			for(; i > lastIndex; i--)
			{
				if(input.substring(i, i+1).equals(" "))
				{
					break;
				}
			}
			if(i == lastIndex)break;
			
			output = output + input.substring(lastIndex, i);
			i = bracketfirstIndex + 1;
			
			for(; i < input.length(); i++)
			{
				if(!input.substring(i, i+1).equals(")"))
				{
					break;
				}
			}
			output = output + input.substring(bracketfirstIndex, i);
			lastIndex = i;			
		}
		
		int slashIndex = output.indexOf("-");
		while(slashIndex != -1)
		{
			int endIndex = slashIndex+1;
			for(;endIndex < output.length(); endIndex++)
			{
				if(output.substring(endIndex,endIndex+1).equals(")") || output.substring(endIndex,endIndex+1).equals("("))
				{
					break;
				}
			}
			output = output.substring(0, slashIndex)+output.substring(endIndex);
			slashIndex = output.indexOf("-");
		}
		
		return output;
	}

}
