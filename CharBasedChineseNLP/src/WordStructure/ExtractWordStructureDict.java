package WordStructure;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

import mason.utils.PinyinComparator;

public class ExtractWordStructureDict {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		 Map<String, Map<String, Integer>> wordstructure = new HashMap<String, Map<String, Integer>>();
		 
		 for(int idx = 0; idx < args.length-1; idx++)
		 {
			 extractDict(args[idx], wordstructure);
		 }
		 
		 
		 List<Entry<String, Map<String, Integer>>> chapossortlist = new ArrayList<Entry<String, Map<String, Integer>>>(wordstructure.entrySet());
			
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.CompareModify((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		}); 
			PrintWriter out = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(args[args.length-1]), "UTF-8"), false);
			for(Entry<String, Map<String, Integer>> curCharPoslist: chapossortlist)
			{
				List<Entry<String, Integer>> synsortlist = new ArrayList<Entry<String, Integer>>(curCharPoslist.getValue().entrySet());
				
				Collections.sort(synsortlist, new Comparator(){   
					public int compare(Object o1, Object o2) {    
						Map.Entry obj1 = (Map.Entry) o1;
						Map.Entry obj2 = (Map.Entry) o2;
						Integer s1 = (Integer) obj1.getValue();
						Integer s2 = (Integer) obj1.getValue();
						return s1.compareTo(s2)	;
		            }   
				});
				
				for(Entry<String, Integer> curSyn: synsortlist)
				{
					final PennTreeReader reader = new PennTreeReader(
							new StringReader(curSyn.getKey().trim()));
					Tree<String> tree = reader.next();
					Tree<String> subTree1 = tree;

					while (subTree1.getLabel().equalsIgnoreCase("root")
							|| subTree1.getLabel().equalsIgnoreCase("top")) {
						tree = tree.getChild(0);
						subTree1 = subTree1.getChild(0);
					}
					String newLabel = String.format("%s#%d", subTree1.getLabel(), curSyn.getValue());
					subTree1.setLabel(newLabel);
					out.println(subTree1.toString());
				}
			}
			
			out.close();

	}
	
	public static void extractDict(String inputFile, Map<String, Map<String, Integer>> wordstructure) throws Exception
	{
		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();
		sdpCorpusReader.Init(inputFile);
		
		List<DepInstance> depInstances = sdpCorpusReader.m_vecInstances;
		
		for(DepInstance inst : depInstances)
		{
			for(int i = 0; i < inst.forms.size(); i++)
			{
				String word = inst.forms.get(i);
				String pos = inst.postags.get(i);
				String structer = inst.feats1.get(i);
				
				final PennTreeReader reader = new PennTreeReader(
						new StringReader(structer.trim()));
				Tree<String> tree = reader.next();
				Tree<String> subTree1 = tree;

				while (subTree1.getLabel().equalsIgnoreCase("root")
						|| subTree1.getLabel().equalsIgnoreCase("top")) {
					tree = tree.getChild(0);
					subTree1 = subTree1.getChild(0);
				}
				
				String key = String.format("[%s][%s]", word, pos);
				if(!wordstructure.containsKey(key))
				{
					wordstructure.put(key, new HashMap<String, Integer>());
				}
				Tree<String> newRoot = new Tree<String>(key);
				List<Tree<String>> children = new ArrayList<Tree<String>>();
				children.add(subTree1);
				newRoot.setChildren(children);
				String secondKey = newRoot.toString().trim();
				
				if(!wordstructure.get(key).containsKey(secondKey))
				{
					wordstructure.get(key).put(secondKey, 0);
				}
				
				wordstructure.get(key).put(secondKey, wordstructure.get(key).get(secondKey)+1);
			}
		}
		
	}

}
