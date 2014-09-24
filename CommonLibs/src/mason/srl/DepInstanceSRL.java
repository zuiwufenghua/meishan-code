package mason.srl;


import java.util.*;


public class DepInstanceSRL {
	public List<String> forms;
	public List<String> lemmas;
	public List<String> lemmas_app;
	public List<String> cpostags;
	public List<String> postags;
	public List<Integer> heads;
	public List<String> deprels;
	public List<String> predicates;
	public List<String> predicates_type;
	public List<List<String>> predicateslabels;
	public int predicatessize;



	

	public DepInstanceSRL() {
		forms = new ArrayList<String>();
		lemmas = new ArrayList<String>();
		lemmas_app = new ArrayList<String>();
		cpostags = new ArrayList<String>();
		postags = new ArrayList<String>();
		heads = new ArrayList<Integer>();
		deprels = new ArrayList<String>();
		predicates = new ArrayList<String>();
		predicates_type = new ArrayList<String>();
		predicateslabels = new ArrayList<List<String>>();
		predicatessize = 0;

	}

	public void reset() {
		forms = new ArrayList<String>();
		lemmas = new ArrayList<String>();
		lemmas_app = new ArrayList<String>();
		cpostags = new ArrayList<String>();
		postags = new ArrayList<String>();
		heads = new ArrayList<Integer>();
		deprels = new ArrayList<String>();
		predicates = new ArrayList<String>();
		predicates_type = new ArrayList<String>();
		predicateslabels = new ArrayList<List<String>>();
		predicatessize = 0;
	}
	
	public int size() {
		return forms.size();
	}
	


	public void toString(List<String> outputs) {
		outputs.clear();
		// outputs.add(String.format("kprob\t1\t%f", k_probs[0]));
		int length = forms.size();
		for (int i = 0; i < length; i++) {
			String tmpOut = String.format(
					"%d\t%s\t%s\t%s\t%s\t%s\t_\t_\t%d\t%d\t%s\t%s\t%s\t%s", i + 1, forms.get(i),
					lemmas.get(i), lemmas_app.get(i), cpostags.get(i), postags.get(i),
					heads.get(i), heads.get(i), deprels.get(i), deprels.get(i), predicates.get(i), predicates_type.get(i));
			
			for (List<String> cursrls : predicateslabels)
			{
				tmpOut = tmpOut + "\t" + cursrls.get(i);
			}
			

			outputs.add(tmpOut);
		}
	}
	
	
	public void removeOneSRLType(boolean bNoun)
	{
		List<List<String>> remainSRLs = new ArrayList<List<String>>();

		int srlCount = 0;
		if(predicateslabels.size()==0)return;
		for(int i = 0; i < predicates_type.size(); i++)
		{
			if(predicates_type.get(i).equals("N"))
			{
				if(!bNoun)
				{
					remainSRLs.add(predicateslabels.get(srlCount));
				}
				else
				{
					predicates_type.set(i, "_");
					predicates.set(i, "_");
				}
				srlCount++;
			}
			else if(predicates_type.get(i).equals("V"))
			{
				if(bNoun)
				{
					remainSRLs.add(predicateslabels.get(srlCount));
				}
				else
				{
					predicates_type.set(i, "_");
					predicates.set(i, "_");
				}
				srlCount++;
			}
			else
			{
				
			}

		}
		
		predicateslabels.clear();
		for(List<String> oneSRLs : remainSRLs)
		{
			predicateslabels.add(oneSRLs);
		}
		
	}
	
	
	public void classiffypredicateslabels()
	{
		List<List<String>> newPredicateslabels = new ArrayList<List<String>>();
		int srlCount = 0;
		//if(predicateslabels.size()==0)return;
		for(int i = 0; i < predicates_type.size(); i++)
		{
			if(predicates_type.get(i).equals("N") || predicates_type.get(i).equals("V"))
			{
				newPredicateslabels.add(SRLaddDependency(predicateslabels.get(srlCount), i));
				srlCount++;
			}
		}
		
		predicateslabels.clear();
		for(List<String> oneSRLs : newPredicateslabels)
		{
			predicateslabels.add(oneSRLs);
		}
		
	}
	
	
	public  List<String> SRLaddDependency(List<String> BIOSequences, int predicateid)
	{
		List<String> result = new ArrayList<String>();
		int sequenceSize = BIOSequences.size();
		
		//Map<String, Set<Integer>> SRLChunks = new HashMap<String, Set<Integer>>();
		//String lastLabel = "";
		//int lastLabelBegin = -1;
		//boolean lastLabelModified = false;
		for(int i = 0; i < sequenceSize; i++ )
		{
			//result.add("_");
			if(BIOSequences.get(i).equals("O")  )
			{
				/*
				if(!lastLabel.equals(""))
				{
					int cutHead =  GetHead(lastChunk, lastLabel.equalsIgnoreCase("rel") || lastLabel.equalsIgnoreCase("O"));
					for(int idx = 0; idx < lastChunk.size(); idx++)
					{
						assert(lastChunk.get(idx) == result.size());
						if(cutHead == lastChunk.get(idx))
						{
							result.add(lastLabel);
						}
						else
						{
							result.add("O");
						}
					}
				}
				*/
				//if(!SRLChunks.containsKey("O"))
				//{
				//	SRLChunks.put("O", new TreeSet<Integer>());
				//}
				//SRLChunks.get("O").add(i);
				result.add(BIOSequences.get(i));
			}
			else if(BIOSequences.get(i).equalsIgnoreCase("rel"))
			{
				result.add(BIOSequences.get(i));
			}
			else if(BIOSequences.get(i).startsWith("B-") || (i == 0 && BIOSequences.get(i).startsWith("I-")))
			{
				String attachedLabel = "";
				boolean bModified = false;
				int curChunkLenght =0;
				for(int j = 0; i+j < sequenceSize; j++)
				{
					if( BIOSequences.get(i+j).length() < 4 ||  !BIOSequences.get(i).substring(2).equals(BIOSequences.get(i+j).substring(2)))
					{
						break;
					}
					else
					{
						curChunkLenght++;
						if(heads.get(i+j) - 1 == predicateid)
						{
							bModified = true;
							//attachedLabel = deprels.get(i+j);
							attachedLabel = "1";
						}
						
						if(heads.get(predicateid) - 1 == i+j)
						{
							bModified = true;
							//attachedLabel = deprels.get(predicateid);
							attachedLabel = "1";
						}
					}
				}
				
				if(bModified)
				{
					for(int j = 0; j < curChunkLenght; j++)
					{
						result.add(BIOSequences.get(i+j) + "-" + attachedLabel );
					}
				}
				else
				{
					for(int j = 0; j < curChunkLenght; j++)
					{
						result.add(BIOSequences.get(i+j) );
					}
				}
				
				i = i + curChunkLenght -1;
			}
			else if(BIOSequences.get(i).startsWith("C-"))
			{				
				String attachedLabel = "";
				boolean bModified = false;
				int curChunkLenght =0;
				for(int j = 0; i+j < sequenceSize; j++)
				{
					if(BIOSequences.get(i+j).length() < 4 ||  !BIOSequences.get(i).substring(2).equals(BIOSequences.get(i+j).substring(2)))
					{
						break;
					}
					else
					{
						curChunkLenght++;
						if(heads.get(i+j) - 1 == predicateid)
						{
							bModified = true;
							//attachedLabel = deprels.get(i+j);
							attachedLabel = "1";
						}
						
						if(heads.get(predicateid) - 1 == i+j)
						{
							bModified = true;
							//attachedLabel = deprels.get(predicateid);
							attachedLabel = "1";
						}
					}
				}
				
				if(bModified)
				{
					for(int j = 0; j < curChunkLenght; j++)
					{
						result.add(BIOSequences.get(i+j) + "-" + attachedLabel );
					}
				}
				else
				{
					for(int j = 0; j < curChunkLenght; j++)
					{
						result.add(BIOSequences.get(i+j));
					}
				}
				
				i = i + curChunkLenght -1;
			}
			else
			{
				assert(BIOSequences.get(i).startsWith("I-"));
			}
		}
		
		//clean C-xx
		Map<String, Integer> cchunksmodified = new HashMap<String, Integer>();
		for(int idx = result.size()-1; idx >=0; idx--)
		{
			if(result.get(idx).length() > 4)
			{
				if(result.get(idx).endsWith("-1"))
				{
					String theLabel = result.get(idx).substring(2, result.get(idx).length()-2);
					if(!cchunksmodified.containsKey(theLabel))
					{
						cchunksmodified.put(theLabel, 1);
					}
				}
				else
				{
					String theLabel = result.get(idx).substring(2);
					cchunksmodified.put(theLabel, 0);
				}
			}
		}
		
		for(int idx = result.size()-1; idx >=0; idx--)
		{
			if(result.get(idx).endsWith("-1"))
			{
				String theLabel = result.get(idx).substring(2, result.get(idx).length()-2);
				if(cchunksmodified.containsKey(theLabel) && cchunksmodified.get(theLabel) == 0)
				{
					result.set(idx, result.get(idx).substring(0, 2) + theLabel);
				}
			}
			else if(result.get(idx).length() > 4)
			{
				String theLabel = result.get(idx).substring(2);
				if(cchunksmodified.containsKey(theLabel) && cchunksmodified.get(theLabel) == 1)
				{
					result.set(idx, result.get(idx).substring(0, 2) + theLabel + "-1");
					System.out.println("error transformation");
				}
			}
		}

		if(result.size() != BIOSequences.size())
		{
			System.out.println("error transformation");
		}
		
		return result;
	}
	
	

	
	

	
	public  void correctErrors()
	{
		int predictnum = 0;
		for(int i = 0; i < predicates.size(); i++)
		{
			if(!predicates.get(i).equals("_"))
			{
				if(predicates_type.get(i).equals("_"))
				{
					if(predicates.get(i).equals("ny"))
					{
						predicates.set(i, "Y");
						predicates_type.set(i, "N");
					}
					else if(predicates.get(i).equals("vy"))
					{
						predicates.set(i, "Y");
						predicates_type.set(i, "V");
					}
					else
					{
						System.out.println("Bad predicates : " + predicates.get(i));
					}
				}
				predictnum++;
			}
			
		}
		
		if(predictnum == 0)
		{
			predicateslabels = new ArrayList<List<String>>();
		}
		else if(predictnum != predicatessize)
		{
			System.out.println("Bad predicatelabel num !");
		}
		
	}
	
	

	
	public void toEvaluateFormat(List<String> outputsVerb, List<String> outputsNoun)
	{
		// assume the input is correct;
		List<List<String>> verbSRLs = new ArrayList<List<String>>();
		List<String> verbPreds = new ArrayList<String>();
		List<List<String>> nounSRLs = new ArrayList<List<String>>();
		List<String> nounPreds = new ArrayList<String>();
		int srlCount = 0;
		if(predicateslabels.size()==0)return;
		for(int i = 0; i < predicates_type.size(); i++)
		{
			if(predicates_type.get(i).equals("N"))
			{
				nounSRLs.add(transfer(predicateslabels.get(srlCount)));
				nounPreds.add(forms.get(i));
				verbPreds.add("-");
				srlCount++;
			}
			else if(predicates_type.get(i).equals("V"))
			{
				verbSRLs.add(transfer(predicateslabels.get(srlCount)));
				verbPreds.add(forms.get(i));
				nounPreds.add("-");
				srlCount++;
			}
			else
			{
				verbPreds.add("-");
				nounPreds.add("-");
			}
		}
		
		
		int length = forms.size();
		if(nounSRLs.size() != 0)
		{
			for (int i = 0; i < length; i++) {
				String tmpOut = nounPreds.get(i);
				
				for (List<String> cursrls : nounSRLs)
				{
					tmpOut = tmpOut + "\t" + cursrls.get(i);
				}
				
	
				outputsNoun.add(tmpOut);
			}
		}
		
		if(verbSRLs.size() != 0)
		{
			for (int i = 0; i < length; i++) {
				String tmpOut = verbPreds.get(i);
				
				for (List<String> cursrls : verbSRLs)
				{
					tmpOut = tmpOut + "\t" + cursrls.get(i);
				}
				
	
				outputsVerb.add(tmpOut);
			}
		}
		
			
	}
	
	
	public void toEvaluateFormatVDEP(List<String> outputsVerb, List<String> outputsNoun)
	{
		// assume the input is correct;
		List<List<String>> verbSRLs = new ArrayList<List<String>>();
		List<String> verbPreds = new ArrayList<String>();
		List<List<String>> nounSRLs = new ArrayList<List<String>>();
		List<String> nounPreds = new ArrayList<String>();
		int srlCount = 0;
		if(predicateslabels.size()==0)return;
		for(int i = 0; i < predicates_type.size(); i++)
		{
			if(predicates_type.get(i).equals("N"))
			{
				nounSRLs.add(transferfromdep(predicateslabels.get(srlCount)));
				nounPreds.add(forms.get(i));
				verbPreds.add("-");
				srlCount++;
			}
			else if(predicates_type.get(i).equals("V"))
			{
				verbSRLs.add(transferfromdep(predicateslabels.get(srlCount)));
				verbPreds.add(forms.get(i));
				nounPreds.add("-");
				srlCount++;
			}
			else
			{
				verbPreds.add("-");
				nounPreds.add("-");
			}
		}
		
		
		int length = forms.size();
		if(nounSRLs.size() != 0)
		{
			for (int i = 0; i < length; i++) {
				String tmpOut = nounPreds.get(i);
				
				for (List<String> cursrls : nounSRLs)
				{
					tmpOut = tmpOut + "\t" + cursrls.get(i);
				}
				
	
				outputsNoun.add(tmpOut);
			}
		}
		
		if(verbSRLs.size() != 0)
		{
			for (int i = 0; i < length; i++) {
				String tmpOut = verbPreds.get(i);
				
				for (List<String> cursrls : verbSRLs)
				{
					tmpOut = tmpOut + "\t" + cursrls.get(i);
				}
				
	
				outputsVerb.add(tmpOut);
			}
		}
		
			
	}
	
	
	public  List<String> transfer(List<String> BIOSequences)
	{
		List<String> result = new ArrayList<String>();
		int sequenceSize = BIOSequences.size();
		for(int i = 0; i < sequenceSize; i++ )
		{
			if(BIOSequences.get(i).equals("O"))
			{
				result.add("*");
			}
			else if(BIOSequences.get(i).equalsIgnoreCase("rel"))
			{
				result.add("(V*)");
			}
			else if(BIOSequences.get(i).startsWith("B-"))
			{
				if(i+1 == sequenceSize || !BIOSequences.get(i+1).equalsIgnoreCase("I-" + BIOSequences.get(i).substring(2)))
				{
					result.add("(" + BIOSequences.get(i).substring(2) + "*)");
				}
				else
				{
					result.add("(" + BIOSequences.get(i).substring(2) + "*");
				}
			}
			else if(BIOSequences.get(i).startsWith("C-"))
			{
				if(i+1 == sequenceSize || !BIOSequences.get(i+1).equalsIgnoreCase("I-" + BIOSequences.get(i).substring(2)))
				{
					result.add("(" + BIOSequences.get(i) + "*)");
				}
				else
				{
					result.add("(" + BIOSequences.get(i) + "*");
				}
			}
			else
			{
				assert(BIOSequences.get(i).startsWith("I-"));
				String theTag = "";
				if(i+1 == sequenceSize || !BIOSequences.get(i+1).equalsIgnoreCase("I-" + BIOSequences.get(i).substring(2)))
				{
					theTag = "*)";
				}
				else
				{
					theTag = "*";
				}
				if( i == 0 || !BIOSequences.get(i-1).endsWith(BIOSequences.get(i).substring(2)) )
				{
					theTag = "(" + BIOSequences.get(i).substring(2) + theTag;
				}
				result.add(theTag);
			}
		}
		
		return result;
	}
	
	
	public  List<String> transferfromdep(List<String> depSequences)
	{
		List<String> result = new ArrayList<String>();
		int sequenceSize = depSequences.size();
		
		
		List<String> BIOSequences = new ArrayList<String>();
		for(int i = 0; i < sequenceSize; i++ )
		{
			BIOSequences.add("O");
		}
		
		for(int i = 0; i < sequenceSize; i++ )
		{
			if(!depSequences.get(i).equalsIgnoreCase("_"))
			{
				if(depSequences.get(i).equalsIgnoreCase("rel"))
				{
					BIOSequences.set(i, "rel");
				}
				else
				{
					Set<Integer> childs = new HashSet<Integer>();
					getChildren(i, childs);
					childs.add(i);
					int maxNum = -1;
					int minNum = sequenceSize;
					for(int curId : childs)
					{
						if(curId < minNum)
						{
							minNum = curId;
						}
						
						if(curId > maxNum)
						{
							maxNum = curId;
						}
					}
					
					boolean bValid = true;
					
					for(int curId = minNum; curId <= maxNum; curId++)
					{
						if(!childs.contains(curId))
						{
							bValid = false;
						}
					}
					
					if(!bValid)
					{
						System.out.println("Invalid getChindren function.....");
					}
					
					String startlabel = depSequences.get(i);
					String newLabel = startlabel;
					if(startlabel.startsWith("C-"))
					{
						newLabel = startlabel.substring(2);
					}
					else
					{
						startlabel = "B-" + startlabel;
					}
					
					BIOSequences.set(minNum, startlabel);
					for(int curId = minNum+1; curId <= maxNum; curId++)
					{
						BIOSequences.set(curId, "I-" + newLabel);
					}
				}
			}
		}
		
		for(int i = 0; i < sequenceSize; i++ )
		{
			if(BIOSequences.get(i).equals("O"))
			{
				result.add("*");
			}
			else if(BIOSequences.get(i).equalsIgnoreCase("rel"))
			{
				result.add("(V*)");
			}
			else if(BIOSequences.get(i).startsWith("B-"))
			{
				if(i+1 == sequenceSize || !BIOSequences.get(i+1).equalsIgnoreCase("I-" + BIOSequences.get(i).substring(2)))
				{
					result.add("(" + BIOSequences.get(i).substring(2) + "*)");
				}
				else
				{
					result.add("(" + BIOSequences.get(i).substring(2) + "*");
				}
			}
			else if(BIOSequences.get(i).startsWith("C-"))
			{
				if(i+1 == sequenceSize || !BIOSequences.get(i+1).equalsIgnoreCase("I-" + BIOSequences.get(i).substring(2)))
				{
					result.add("(" + BIOSequences.get(i) + "*)");
				}
				else
				{
					result.add("(" + BIOSequences.get(i) + "*");
				}
			}
			else
			{
				assert(BIOSequences.get(i).startsWith("I-"));
				String theTag = "";
				if(i+1 == sequenceSize || !BIOSequences.get(i+1).equalsIgnoreCase("I-" + BIOSequences.get(i).substring(2)))
				{
					theTag = "*)";
				}
				else
				{
					theTag = "*";
				}
				if( i == 0 || !BIOSequences.get(i-1).endsWith(BIOSequences.get(i).substring(2)) )
				{
					theTag = "(" + BIOSequences.get(i).substring(2) + theTag;
				}
				result.add(theTag);
			}
		}
		
		return result;
	}
	
	
	public void toDepFormat(List<String> outputs)
	{
		// assume the input is correct;
		List<List<String>> newPredicateslabels = new ArrayList<List<String>>();
		int srlCount = 0;
		//if(predicateslabels.size()==0)return;
		for(int i = 0; i < predicates_type.size(); i++)
		{
			if(predicates_type.get(i).equals("N") || predicates_type.get(i).equals("V"))
			{
				newPredicateslabels.add(SRLToDependency(predicateslabels.get(srlCount)));
				srlCount++;
			}
		}
		
		
		int length = forms.size();
		for (int i = 0; i < length; i++) {
			String  predicatesvalue = predicates.get(i);
			String  predicates_typevalue = predicates_type.get(i);
			if(predicates_type.get(i).equals("N"))
			{
				predicatesvalue = "_";
				predicates_typevalue = "_";
			}
			String tmpOut = String.format(
					"%d\t%s\t%s\t%s\t%s\t%s\t_\t_\t%d\t%d\t%s\t%s\t%s\t%s", i + 1, forms.get(i),
					lemmas.get(i), lemmas_app.get(i), cpostags.get(i), postags.get(i),
					heads.get(i), heads.get(i), deprels.get(i), deprels.get(i), predicatesvalue, predicates_typevalue);
			
			srlCount = 0;
			for(int idx = 0; idx < predicates_type.size(); idx++)
			{
				List<String> cursrls = null;
				if(predicates_type.get(idx).equals("N") || predicates_type.get(idx).equals("V"))
				{
					cursrls = newPredicateslabels.get(srlCount);
					srlCount++;
				}
				if(predicates_type.get(idx).equals("V"))
				{
					tmpOut = tmpOut + "\t" + cursrls.get(i);
				}
			}

			outputs.add(tmpOut);
		}
		
			
	}
	


	public  List<String> SRLToDependency(List<String> BIOSequences)
	{
		List<String> result = new ArrayList<String>();
		int sequenceSize = BIOSequences.size();
		
		Map<String, Set<Integer>> SRLChunks = new HashMap<String, Set<Integer>>();
		String lastLabel = "";
		for(int i = 0; i < sequenceSize; i++ )
		{
			result.add("_");
			if(BIOSequences.get(i).equals("O")  )
			{
				/*
				if(!lastLabel.equals(""))
				{
					int cutHead =  GetHead(lastChunk, lastLabel.equalsIgnoreCase("rel") || lastLabel.equalsIgnoreCase("O"));
					for(int idx = 0; idx < lastChunk.size(); idx++)
					{
						assert(lastChunk.get(idx) == result.size());
						if(cutHead == lastChunk.get(idx))
						{
							result.add(lastLabel);
						}
						else
						{
							result.add("O");
						}
					}
				}
				*/
				//if(!SRLChunks.containsKey("O"))
				//{
				//	SRLChunks.put("O", new TreeSet<Integer>());
				//}
				//SRLChunks.get("O").add(i);
			}
			else if(BIOSequences.get(i).equalsIgnoreCase("rel"))
			{
				lastLabel = BIOSequences.get(i);
				if(!SRLChunks.containsKey(lastLabel))
				{
					SRLChunks.put(lastLabel, new TreeSet<Integer>());
				}
				SRLChunks.get(lastLabel).add(i);
			}
			else if(BIOSequences.get(i).startsWith("B-"))
			{
				lastLabel = BIOSequences.get(i).substring(2);
				if(!SRLChunks.containsKey(lastLabel))
				{
					SRLChunks.put(lastLabel, new TreeSet<Integer>());
				}
				SRLChunks.get(lastLabel).add(i);
			}
			else if(BIOSequences.get(i).startsWith("C-"))
			{				
				lastLabel = BIOSequences.get(i).substring(2);
				if(!SRLChunks.containsKey(lastLabel))
				{
					SRLChunks.put(lastLabel, new TreeSet<Integer>());
				}
				SRLChunks.get(lastLabel).add(i);
			}
			else
			{
				assert(BIOSequences.get(i).startsWith("I-"));
				lastLabel = BIOSequences.get(i).substring(2);
				if(!SRLChunks.containsKey(lastLabel))
				{
					SRLChunks.put(lastLabel, new TreeSet<Integer>());
				}
				SRLChunks.get(lastLabel).add(i);
			}
		}
		
		for(String curLabel : SRLChunks.keySet())
		{
			Set<Integer> newheads = new TreeSet<Integer>();
			GetHead(SRLChunks.get(curLabel), newheads);
			boolean bFirst = true;
			for(int i = 0; i < sequenceSize; i++)
			{
				if(newheads.contains(i))
				{
					if(bFirst)
					{
						bFirst = false;
						result.set(i, curLabel);
					}
					else
					{
						result.set(i, "C-"+curLabel);
					}
				}
			}
		}
		
		return result;
	}
	
	public boolean GetHead(Set<Integer> chunk, Set<Integer> newheads )
	{
		for(Integer i : chunk)
		{
			int curNodeHead = heads.get(i) -1;
			if( chunk.contains(curNodeHead))
			{
				continue;
			}
			else
			{
				newheads.add(i);
			}
		}
		
		return true;
		/*
		if(!bRel)
		{
			Set<Integer> thechildren = new TreeSet<Integer>();
			getChildren(head, thechildren);
			
			for(Integer theChild : thechildren)
			{
				if(theChild > end || theChild < start)
				{
					System.out.println("Please check corpus......");
				}
			}
		}
		
		return head;*/
	}
	
	public void getDescendants(int head, Set<Integer> childs )
	{
		childs.add(head);
		for(int idx = 0; idx < heads.size(); idx++)
		{
			if(heads.get(idx) == head+1)
			{
				childs.add(idx);
				getDescendants(idx, childs);
			}
		}
	}
	
	
	public void getChildren(int head, Set<Integer> childs )
	{
		for(int idx = 0; idx < heads.size(); idx++)
		{
			if(heads.get(idx) == head+1)
			{
				childs.add(idx);
				//getChildren(idx, childs);
			}
		}
	}

	public static boolean isPunc(String thePostag)
	{
		if(thePostag.equals("PU") || thePostag.equals("``")
		 || thePostag.equals("''") || thePostag.equals(",")
		 || thePostag.equals(".") || thePostag.equals(":"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean checkInst()
	{
		int predictnum = 0;
		for(int i = 0; i < predicates.size(); i++)
		{
			if(!predicates.get(i).equals("_"))
			{
				if(predicates_type.get(i).equals("_"))
				{
					return false;
				}
				predictnum++;
			}
			
		}
		
		if(predictnum != predicatessize)
		{
			return false;
		}
		
		return true;
	}
	
	
	public List<Integer> path2Root(int idx)
	{
		List<Integer> paths = new ArrayList<Integer>();
		paths.add(idx);
		
		int headid = heads.get(idx);
		while(headid > 0)
		{
			paths.add(0, headid-1);
			headid = heads.get(headid-1);
		}
				
		return paths;
	}
	
	
}
