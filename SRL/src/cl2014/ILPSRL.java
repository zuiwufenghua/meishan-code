package cl2014;

import mason.srl.DepInstanceSRL;
import mason.srl.SDPSRLCorpusReader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;

import lpsolve.LpSolve;

public class ILPSRL {

	/**
	 * @param args
	 */
	
	static final String[] closedSRLLabels = 
    { "B-ARG0", "I-ARG0", "C-ARG0", "B-ARG1", "I-ARG1", "C-ARG1", 
	  "B-ARG2", "I-ARG2", "C-ARG2", "B-ARG3", "I-ARG3", "C-ARG3",
	  "B-ARG4", "I-ARG4", "C-ARG4", "B-ARG5", "I-ARG5", "C-ARG5",
	  "B-ARGM-TMP", "I-ARGM-TMP", "C-ARGM-TMP",
	  "B-ARGM-LOC", "I-ARGM-LOC", "C-ARGM-LOC",
	  "B-ARGM-ADV", "I-ARGM-ADV", "C-ARGM-ADV",
	  "B-ARGM-BNF", "I-ARGM-BNF", "C-ARGM-BNF",
	  "B-ARGM-CND", "I-ARGM-CND", "C-ARGM-CND",
	  "B-ARGM-DIR", "I-ARGM-DIR", "C-ARGM-DIR",
	  "B-ARGM-DIS", "I-ARGM-DIS", "C-ARGM-DIS",
	  "B-ARGM-DGR", "I-ARGM-DGR", "C-ARGM-DGR",
	  "B-ARGM-EXT", "I-ARGM-EXT", "C-ARGM-EXT",
	  "B-ARGM-FRQ", "I-ARGM-FRQ", "C-ARGM-FRQ",
	  "B-ARGM-MNR", "I-ARGM-MNR", "C-ARGM-MNR",
	  "B-ARGM-NEG", "I-ARGM-NEG", "C-ARGM-NEG",
	  "B-ARGM-PRP", "I-ARGM-PRP", "C-ARGM-PRP",
	  "B-ARGM-TPC", "I-ARGM-TPC", "C-ARGM-TPC",
	  "rel", "O"
    };
	

	
	static final int[] atmostonelabels = 
	{
		0, 3, 6, 9, 12, 15
	};
	
	static final int[] mustbeonelabels = 
	{
		60
	};
	
	
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		SDPSRLCorpusReader sdpCorpusReader = new SDPSRLCorpusReader();
		sdpCorpusReader.Init(args[0]);
		
		List<DepInstanceSRL> vecInstances = sdpCorpusReader.m_vecInstances;
		
		BufferedReader in = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(args[1]), "UTF-8"));
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"));	
		
		double min_lable_prob = 0.3;
		if(args.length > 3)
		{
			min_lable_prob = Double.parseDouble(args[3]);
		}
			
		int srlinstcount = 0;
		for(DepInstanceSRL depInst :  vecInstances)
		{
			
			if(depInst.predicateslabels.size()==0)continue;
			
			depInst.removeOneSRLType(true);
			
			if(depInst.predicateslabels.size()==0)continue;
			
			int length = depInst.forms.size();
			
			int srlCount = 0;
			for(int i = 0; i < depInst.predicates_type.size(); i++)
			{
				if(depInst.predicates_type.get(i).equals("N"))
				{
					srlCount++;
				}
				else if(depInst.predicates_type.get(i).equals("V"))
				{
					List<String> oneblocks = getOneBlock(in);
					if(oneblocks.size() != length)
					{
						System.out.println("sentence word number is not match");
						continue;
					}
					
					boolean bValidBlock = true;
					List<Map<String, Double>> probs = new ArrayList<Map<String, Double>> ();
					try
					{
						for(String oneLine : oneblocks)
						{
							String[] smallunits = oneLine.split("\\s+");
							int wordId = Integer.parseInt(smallunits[0]);
							if(wordId != probs.size() + 1)
							{
								bValidBlock = false;
								break;
							}
							
							probs.add(new HashMap<String, Double>());
							double total_prob = 0.0;
							boolean bOLableFind = false;
							for(int idx = 1; idx < smallunits.length; idx++)
							{
								int theColonIndex = smallunits[idx].indexOf(":");
								String label = smallunits[idx].substring(0, theColonIndex);
								double labelprob = Double.parseDouble(smallunits[idx].substring(theColonIndex+1));
								if(label.equalsIgnoreCase("o") || labelprob >= min_lable_prob)
								{
									probs.get(wordId-1).put(label, labelprob);
									if(label.equalsIgnoreCase("o"))
									{
										bOLableFind = true;
									}
								}
								
								total_prob = total_prob + labelprob;
								
								if(total_prob > 1- min_lable_prob && bOLableFind)
								{
									break;
								}
								
							}
						}
					}
					catch (Exception ex)
					{
						bValidBlock = false;
					}
					
					if(!bValidBlock)
					{
						System.out.println("invalid bolck");
						continue;
					}
					
					
					//if(srlinstcount == 702)
					//{
					//	System.out.println("start debug");
					//}
					
					List<String> results = ILPDecode(probs);
					
					for(int idx = 0; idx < results.size(); idx++)
					{
						depInst.predicateslabels.get(srlCount).set(idx, results.get(idx));
					}															
					srlCount++;
					srlinstcount++;
				}
				else {
					
				}
			}
			
			List<String> outputs = new ArrayList<String>();
			depInst.toString(outputs);
			
			for(String oneout : outputs)
			{
				writer.println(oneout);
			}
			writer.println();
			
			
		}
		
		
		in.close();
		writer.close();
		System.out.println(srlinstcount);

	}
	
	
	public static List<String> getOneBlock(BufferedReader in) throws Exception
	{
		List<String> newblocks = new ArrayList<String>();
		
		String sLine = "";
		while ((sLine = in.readLine()) != null) {
			if(!sLine.trim().isEmpty())
			{
				newblocks.add(sLine.trim());
				break;
			}
		}
		
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().isEmpty()) break;
			newblocks.add(sLine.trim());
		}
		
		return newblocks;
	}
	
	
	public static List<String> ILPDecode(List<Map<String, Double>> probs) throws Exception
	{
		
		int length = probs.size();
		int[][] seqmap = new int[length][closedSRLLabels.length];
		List<String> variables2string = new ArrayList<String>();
		
		int valsize = 0;
		for(Map<String, Double> oneItem : probs)
		{
			valsize = valsize + oneItem.size();
		}
		
		double[] alpha = new double[valsize+1];
		int curseq = 1;
		for(int idx = 0; idx < length; idx++)
		{
			for(int labelId = 0; labelId < closedSRLLabels.length; labelId++)
			{
				if(probs.get(idx).containsKey(closedSRLLabels[labelId]))
				{
					alpha[curseq] = Math.log(probs.get(idx).get(closedSRLLabels[labelId]));
					seqmap[idx][labelId] = curseq;
					curseq++;
					variables2string.add(String.format("%d %s", idx, closedSRLLabels[labelId]));
				}
				else
				{
					seqmap[idx][labelId] = -1;
				}
			}
		}
		
		LpSolve solver = LpSolve.makeLp(0, valsize);
		solver.setVerbose(1);
		solver.setMaxim();
		
		for(int idx = 1; idx <= valsize; idx++)
		{
			solver.setBinary(idx, true);
		}
		
		
		solver.setObjFn(alpha);
		
		//one word must have one label
		for(int idx = 0; idx < length; idx++)
		{
			double[] constraint_beltas = new double[valsize+1];
			for(int idy = 0; idy < valsize+1; idy++ )
			{
				constraint_beltas[idy] = 0.0;
			}
			
			int nonzerovarnum = 0;
			for(int labelId = 0; labelId < closedSRLLabels.length; labelId++)
			{
				if(seqmap[idx][labelId] >= 0)
				{
					constraint_beltas[seqmap[idx][labelId]] = 1.0;
					nonzerovarnum++;
				}
			}
			
			if(nonzerovarnum > 0)
			{
				solver.addConstraint(constraint_beltas, LpSolve.EQ, 1.0);
			}
			else
			{
				System.out.println("lpsolver error");
			}
			
		}
		
		
		//at most one constraints  
		for(int splabelId = 0; splabelId < atmostonelabels.length; splabelId++)
		{
			int labelId = atmostonelabels[splabelId];
			double[] constraint_beltas = new double[valsize+1];
			for(int idy = 0; idy < valsize+1; idy++ )
			{
				constraint_beltas[idy] = 0.0;
			}
			int nonzerovarnum = 0;
			for(int idx = 0; idx < length; idx++)
			{
				if(seqmap[idx][labelId] >= 0)
				{
					constraint_beltas[seqmap[idx][labelId]] = 1.0;
					nonzerovarnum++;
				}
			}
			
			if(nonzerovarnum > 0)
			{
				solver.addConstraint(constraint_beltas, LpSolve.LE, 1.0);
			}
		}
		
		
		
		//must be one constraints
		
		for(int splabelId = 0; splabelId < mustbeonelabels.length; splabelId++)
		{
			int labelId = mustbeonelabels[splabelId];
			double[] constraint_beltas = new double[valsize+1];
			for(int idy = 0; idy < valsize+1; idy++ )
			{
				constraint_beltas[idy] = 0.0;
			}
			int nonzerovarnum = 0;
			for(int idx = 0; idx < length; idx++)
			{
				if(seqmap[idx][labelId] >= 0)
				{
					constraint_beltas[seqmap[idx][labelId]] = 1.0;
					nonzerovarnum++;
				}
			}
			
			if(nonzerovarnum > 0)
			{
				solver.addConstraint(constraint_beltas, LpSolve.EQ, 1.0);
			}
		}
		
		
		// C-xx related constraints
		
		for(int splabelId = 0; 3 * splabelId + 2 < closedSRLLabels.length; splabelId++)
		{
			int clabelId = 3 * splabelId + 2;
			int blabelId = 3 * splabelId;
			int ilabelId = 3 * splabelId + 1;
			for(int idend = 0; idend < length; idend++)
			{
				
				if(seqmap[idend][clabelId] >= 0)
				{
					double[] constraint_beltas = new double[valsize+1];
					for(int idy = 0; idy < valsize+1; idy++ )
					{
						constraint_beltas[idy] = 0.0;
					}
					
					constraint_beltas[seqmap[idend][clabelId]] = -1.0;
					for(int idx = 0; idx < idend; idx++)
					{
						if(seqmap[idx][blabelId]>=0)constraint_beltas[seqmap[idx][blabelId]] = 1.0;
					}
					
					solver.addConstraint(constraint_beltas, LpSolve.GE, 0.0);					
				}
				
				
				if(seqmap[idend][clabelId] >= 0 && idend > 0)
				{
					double[] constraint_beltas = new double[valsize+1];
					for(int idy = 0; idy < valsize+1; idy++ )
					{
						constraint_beltas[idy] = 0.0;
					}
					
					constraint_beltas[seqmap[idend][clabelId]] = 1.0;
					if(seqmap[idend-1][blabelId] >= 0)constraint_beltas[seqmap[idend-1][blabelId]] = 1.0;
					if(seqmap[idend-1][ilabelId] >= 0)constraint_beltas[seqmap[idend-1][ilabelId]] = 1.0;
					if(seqmap[idend-1][clabelId] >= 0)constraint_beltas[seqmap[idend-1][clabelId]] = 1.0;
					
					solver.addConstraint(constraint_beltas, LpSolve.LE, 1.0);					
				}
								
			}
		}
		
		
		// I-xx related constraints
		for(int splabelId = 0; 3 * splabelId + 2 < closedSRLLabels.length; splabelId++)
		{
			int clabelId = 3 * splabelId + 2;
			int blabelId = 3 * splabelId;
			int ilabelId = 3 * splabelId + 1;
			
			// the label before i-xx must be bic-xx
			for(int idend = 1; idend < length; idend++)
			{
				if(seqmap[idend][ilabelId] >= 0 && idend > 0)
				{
					double[] constraint_beltas = new double[valsize+1];
					for(int idy = 0; idy < valsize+1; idy++ )
					{
						constraint_beltas[idy] = 0.0;
					}
					
					constraint_beltas[seqmap[idend][ilabelId]] = -1.0;
					if(seqmap[idend-1][blabelId] >= 0)constraint_beltas[seqmap[idend-1][blabelId]] = 1.0;
					if(seqmap[idend-1][ilabelId] >= 0)constraint_beltas[seqmap[idend-1][ilabelId]] = 1.0;
					if(seqmap[idend-1][clabelId] >= 0)constraint_beltas[seqmap[idend-1][clabelId]] = 1.0;
					
					solver.addConstraint(constraint_beltas, LpSolve.GE, 0.0);	
				}
			}
			
			// first word label is not c-xx or i-xx
			{
				double[] constraint_beltas = new double[valsize+1];
				for(int idy = 0; idy < valsize+1; idy++ )
				{
					constraint_beltas[idy] = 0.0;
				}
				
				int nonzerovarnum = 0;
				if(seqmap[0][ilabelId] >= 0)
				{
					constraint_beltas[seqmap[0][ilabelId]] = 1.0;
					nonzerovarnum++;
				}
				if(seqmap[0][clabelId] >= 0)
				{
					constraint_beltas[seqmap[0][clabelId]] = 1.0;
					nonzerovarnum++;
				}
				
				if(nonzerovarnum > 0)
				{
					solver.addConstraint(constraint_beltas, LpSolve.EQ, 0.0);
				}
				
			}
		}
		
		try
		{
			solver.solve();
		}
		catch (Exception ex)
		{
			System.out.println("lpsolver error");
		}
		double[] sol = solver.getPtrVariables();
		
		List<String> results = new ArrayList<String>();
		int relnum = 0;
		for(int idx = 0; idx < valsize; idx++)
		{
			if(sol[idx] > 0.5)
			{
				//results.add(String.format("%d_%d", lp2org.get(idx+1)/length, lp2org.get(idx+1)%length));
				String[] answers = variables2string.get(idx).split("\\s+");
				int answerid = Integer.parseInt(answers[0]);
				int wordseq = results.size();
				if(answerid != wordseq)
				{
					System.out.println("an error answer!");
				}
				
				results.add(answers[1]);
				if(answers[1].equalsIgnoreCase("rel"))
				{
					relnum++;
				}
			}			
		}
		
		
		int resultsize = results.size();
		if(resultsize != length || relnum != 1)
		{
			System.out.println("an error answer!");
		}
		
		return results;
	}

}
