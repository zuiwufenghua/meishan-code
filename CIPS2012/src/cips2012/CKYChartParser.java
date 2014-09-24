package cips2012;

import edu.berkeley.nlp.syntax.*;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.HashMap;

import edu.berkeley.nlp.syntax.Tree;

public class CKYChartParser {
	
	public List<String> allNonTerminals = null;
	public Map<String, Integer> allNonTerminalsDigitMap = null;
	public int allNonTerminalsNum = 0;
	
	// parent-right-left
	public Map<Integer,Map<Integer,Map<Integer, Double>>> grammarWeights = null;
	

	public Map<Integer,Map<Integer,Map<Integer,Map<String, Double>>>> fineGrammarWeights = null;
	
	
	public Map<Integer, Map<Integer, Map<Integer,List<Entry<String, Double>>>>> fineGrammarWeightsIndex = null;
	
	public Map<Integer,Map<Integer,Map<Integer,Double>>> fineSkeWeights = null;
	
	public Map<String, TreeMap<String, Double>> treemapRule = null;
	
	
	
	public void setNonTerminals(Set<String> nonTerminals)
	{
		allNonTerminals = new ArrayList<String>();
		allNonTerminalsDigitMap = new HashMap<String, Integer>();
		for(String curNonTer : nonTerminals)
		{
			allNonTerminals.add(curNonTer);
			allNonTerminalsDigitMap.put(curNonTer, allNonTerminals.size()-1);
		}
		allNonTerminalsNum = allNonTerminals.size();
	}
	
	public void setGrammars(Map<String, Double> curGrammarWeights)
	{
		if(allNonTerminals == null) 
		{
			System.out.println("Please init non-terminals first!");
			return;
		}
		grammarWeights = new HashMap<Integer,Map<Integer,Map<Integer, Double>>>();
		for(String curRule : curGrammarWeights.keySet())
		{
			String[] curRuleUnits = curRule.split("\t");
			if(curRuleUnits.length != 3)
			{
				System.out.println(curRule);
				continue;
			}
			//二叉化专用start
			if(curRuleUnits[1].equals("PU") || curRuleUnits[2].equals("PU")) continue;
			if(curRuleUnits[1].equals("SP") || curRuleUnits[2].equals("SP")) continue;
			//二叉化专用end
			int parentKey = allNonTerminalsDigitMap.get(curRuleUnits[0]);
			int leftChildKey = allNonTerminalsDigitMap.get(curRuleUnits[1]);
			int rightChildKey = allNonTerminalsDigitMap.get(curRuleUnits[2]);
			double curRuleScore = curGrammarWeights.get(curRule);
			
			
			if(!grammarWeights.containsKey(parentKey))
			{
				grammarWeights.put(parentKey, new HashMap<Integer,Map<Integer, Double>>());
			}

			Map<Integer,Map<Integer, Double>> parentWeight = grammarWeights.get(parentKey);
			
			if(!parentWeight.containsKey(rightChildKey))
			{
				parentWeight.put(rightChildKey, new HashMap<Integer, Double>());
			}
			
			Map<Integer, Double> parentRightChildWeight = parentWeight.get(rightChildKey);
			
			parentRightChildWeight.put(leftChildKey, curRuleScore);
			//if(!parentWeight.containsKey(leftChildKey))
			//{
			//	parentWeight.put(leftChildKey, new HashMap<Integer, Double>());
			//}
			
			//grammarWeights.put(curRule, curGrammarWeights.get(curRule));
		}
		
		//punc
		//二叉化时专用
		int puncKey = allNonTerminalsDigitMap.get("PU");
		int spKey = allNonTerminalsDigitMap.get("SP");
		for(int parentKey : grammarWeights.keySet())
		{
			Map<Integer, Double> parentPuncChildWeight = new HashMap<Integer, Double>();
			for(int puncRightId = 0; puncRightId < allNonTerminalsNum; puncRightId++)
			{
				if(puncRightId == spKey)continue;
				if(grammarWeights.containsKey(puncRightId))
				{
					parentPuncChildWeight.put(puncRightId, 1.0);
				}
				else
				{
					parentPuncChildWeight.put(puncRightId, 0.5);
				}
				
			}
			grammarWeights.get(parentKey).put(puncKey, parentPuncChildWeight);
		}
		
		//punc
		
		for(int parentKey : grammarWeights.keySet())
		{
			Map<Integer, Double> parentPuncChildWeight = new HashMap<Integer, Double>();
			for(int spRightId = 0; spRightId < allNonTerminalsNum; spRightId++)
			{
				if(spRightId == puncKey)continue;
				if(grammarWeights.containsKey(spRightId))
				{
					parentPuncChildWeight.put(spRightId, 2.0);
				}
				else
				{
					parentPuncChildWeight.put(spRightId, 1.0);
				}
				
			}
			grammarWeights.get(parentKey).put(spKey, parentPuncChildWeight);
		}
	}
	
	public void setFineGrammars(Map<String, Double> curGrammarWeights)
	{
		if(allNonTerminals == null) 
		{
			System.out.println("Please init non-terminals first!");
			return;
		}
		fineGrammarWeights = new HashMap<Integer, Map<Integer,Map<Integer,Map<String, Double>>>>();
		fineGrammarWeightsIndex = new HashMap<Integer, Map<Integer, Map<Integer,List<Entry<String, Double>>>>>();
		fineSkeWeights = new HashMap<Integer, Map<Integer,Map<Integer, Double>>>();
		for(String curRule : curGrammarWeights.keySet())
		{
			String[] curRuleUnits = curRule.split("\t");
			if(curRuleUnits.length != 4)
			{
				System.out.println(curRule);
				continue;
			}
			
			int parentKey = allNonTerminalsDigitMap.get(curRuleUnits[1]);
			int leftChildKey = allNonTerminalsDigitMap.get(curRuleUnits[2]);
			int rightChildKey = allNonTerminalsDigitMap.get(curRuleUnits[3]);
			double curRuleScore = curGrammarWeights.get(curRule);
			
			
			if(!fineGrammarWeights.containsKey(parentKey))
			{
				fineGrammarWeights.put(parentKey, new HashMap<Integer,Map<Integer,Map<String, Double>>>());
			}

			Map<Integer,Map<Integer,Map<String, Double>>> parentWeight = fineGrammarWeights.get(parentKey);
			
			if(!parentWeight.containsKey(rightChildKey))
			{
				parentWeight.put(rightChildKey, new HashMap<Integer, Map<String, Double>>());
			}
			
			Map<Integer, Map<String, Double>> parentRightChildWeight = parentWeight.get(rightChildKey);
			
			if(!parentRightChildWeight.containsKey(leftChildKey))
			{
				parentRightChildWeight.put(leftChildKey, new HashMap<String, Double>());
			}
			Map<String, Double> parentRightLeftChildWeight = parentRightChildWeight.get(leftChildKey);
			parentRightLeftChildWeight.put(curRuleUnits[0], curRuleScore);			
			
			String[] posUnits = curRuleUnits[0].split("#");
			if(posUnits.length != 3)
			{
				System.out.println("error fineGrammarWeights.");
			}
			
			
			int s = Integer.parseInt(posUnits[1]);
			int start = Integer.parseInt(posUnits[0]);
			int end = Integer.parseInt(posUnits[2]);
			
			if(!fineSkeWeights.containsKey(start))
			{
				fineSkeWeights.put(start, new HashMap<Integer,Map<Integer,Double>>());
			}

			Map<Integer,Map<Integer,Double>> leftWeight = fineSkeWeights.get(start);
			
			if(!leftWeight.containsKey(end))
			{
				leftWeight.put(end, new HashMap<Integer, Double>());
			}
			
			Map<Integer, Double> leftRightWeight = leftWeight.get(end);
			
			if(!leftRightWeight.containsKey(s))
			{
				leftRightWeight.put(s, 0.0);
			}
			leftRightWeight.put(s, leftRightWeight.get(s)+curRuleScore);			
		}
		
		for(int parentKey :  fineGrammarWeights.keySet())
		{
			Map<Integer,Map<Integer,Map<String, Double>>> parentWeight = fineGrammarWeights.get(parentKey);
			for(int rightChildKey : parentWeight.keySet())
			{
				Map<Integer, Map<String, Double>> parentRightChildWeight = parentWeight.get(rightChildKey);
				for(int leftChildKey : parentRightChildWeight.keySet())
				{
					Set<Entry<String, Double>> entries = parentRightChildWeight.get(leftChildKey).entrySet();
					for(Entry<String, Double> curEntry : entries)
					{
						String posInfo = curEntry.getKey();
						String[] posUnits = posInfo.split("#");
						if(posUnits.length != 3)
						{
							System.out.println("error fineGrammarWeights.");
						}
						
						
						int s = Integer.parseInt(posUnits[1]);
						int start = Integer.parseInt(posUnits[0]);
						int end = Integer.parseInt(posUnits[2]);
						
						if(!fineGrammarWeightsIndex.containsKey(start))
						{
							fineGrammarWeightsIndex.put(start, new HashMap<Integer, Map<Integer,List<Entry<String, Double>>>>());
						}
						Map<Integer, Map<Integer,List<Entry<String, Double>>>> startMap = fineGrammarWeightsIndex.get(start);
						if(!startMap.containsKey(end))
						{
							startMap.put(end, new HashMap<Integer,List<Entry<String, Double>>>());
						}						
						Map<Integer,List<Entry<String, Double>>> startEndMap = startMap.get(end);
						if(!startEndMap.containsKey(s))
						{
							startEndMap.put(s, new ArrayList<Entry<String, Double>>());
						}
						List<Entry<String, Double>> startEndMidEntries = startEndMap.get(s);
						startEndMidEntries.add(curEntry);
					}
				}
			}
		}
			
	}
	
	
	public void setGuideModelParameters(Map<String, TreeMap<String, Double>> curTreemapRule)
	{
		treemapRule = curTreemapRule;
	}
	public  Tree<String> CKYVersion1(String root, List<Tree<String>> bottleChilds, List<Double> bottleChildWeights, String mark)
	{
		if(allNonTerminals == null || grammarWeights == null) return null;
		int length = bottleChilds.size();
		if(length == 1) return bottleChilds.get(0);
		
		int NonTerminalNum = allNonTerminalsNum;
		double[][][] chartScores = new double[length][length][NonTerminalNum];
		int[][][] chartSplit = new int[length][length][NonTerminalNum];
		String[][][] chartRuleLeft = new String[length][length][NonTerminalNum];
		String[][][] chartRuleRight = new String[length][length][NonTerminalNum];
		for(int i = 0; i < length; i++)
			for(int j = 0; j < length; j++)
				for(int k = 0; k < NonTerminalNum; k++)
				{
					chartScores[i][j][k] = 0;
					//left phrase end index
					chartSplit[i][j][k] = -1;
					//left phrase label
					chartRuleLeft[i][j][k]= null;
					//right phrase label
					chartRuleRight[i][j][k]= null;
				}
		for(int i = 0; i < length; i++)
		{
			String curLabel = bottleChilds.get(i).getFirstLabel();
			int labelIndex = allNonTerminalsDigitMap.get(curLabel);
			chartScores[i][i][labelIndex] = bottleChildWeights.get(i);		
		}
		
		for(int l = 1; l < length; l++)
			for(int i = 0; i < length-l; i++)
			{
				int j = i+l;
				//for(int k = 0; k < NonTerminalNum; k++)
				for(int k : grammarWeights.keySet())
				{
					Map<Integer, Map<Integer, Double>> grammarParentWeights = grammarWeights.get(k);
					double maxScore = 0;
					int maxScoreSplitIndex = -1;
					String maxScoreRuleLeft = null;
					String maxScoreRuleRight = null;
					for(int k1 : grammarParentWeights.keySet())
					{
						Map<Integer, Double> grammarParentRightWeights = grammarParentWeights.get(k1);
						for(int k2 : grammarParentRightWeights.keySet())
						{
							for(int s = i; s < j; s++)
							{
								if(chartScores[i][s][k2] == 0 || chartScores[s+1][j][k1] == 0) continue;
								double curScore = grammarParentRightWeights.get(k2) + chartScores[i][s][k2] + chartScores[s+1][j][k1];
								if(curScore > maxScore)
								{
									maxScore = curScore;
									maxScoreSplitIndex = s;
									maxScoreRuleLeft = allNonTerminals.get(k2);
									maxScoreRuleRight = allNonTerminals.get(k1);
								}
							}							
						}
					}
					chartScores[i][j][k] = maxScore;
					chartSplit[i][j][k] = maxScoreSplitIndex;
					chartRuleLeft[i][j][k]= maxScoreRuleLeft;
					chartRuleRight[i][j][k]= maxScoreRuleRight;
				}
			}
		
		
		
		String[] firstLayer = root.toString().split("-");
		String[] secondLayer = firstLayer[0].split("@");
		
		int rootLabelIndex = allNonTerminalsDigitMap.get(secondLayer[0]);
		Tree<String> resultTree = null;
		if(chartScores[0][length-1][rootLabelIndex] == 0) 
		{			
			//二叉化专用
			double[][] splitScore = new double[length][length];
			int[][] splitScoreLabel = new int[length][length];
			
			for(int i = 0; i < length; i++)
				for(int j = 0; j < length; j++)
				{
					double curSpanMaxScore = 0;
					int curSpanBestLabel = -1;
					for(int k = 0; k < NonTerminalNum; k++)
					{
						if(chartScores[i][j][k] > curSpanMaxScore)
						{
							curSpanMaxScore = chartScores[i][j][k];
							curSpanBestLabel = k;
						}
					}
					splitScore[i][j] = curSpanMaxScore;
					splitScoreLabel[i][j] = curSpanBestLabel;
				}
			
			//能由两个phrase拼接
			double maxScore = 0;
			int maxScoreSplitIndex = -1;
			int maxScoreRuleLeftId = -1;
			int maxScoreRuleRightId = -1; 
			for(int s = 0; s < length-1; s++)
			{
				if(splitScore[0][s] != 0 && splitScore[s+1][length-1] != 0 && splitScore[0][s] + splitScore[s+1][length-1] > maxScore)
				{
					maxScore = splitScore[0][s] + splitScore[s+1][length-1];
					maxScoreSplitIndex = s;
					maxScoreRuleLeftId = splitScoreLabel[0][s];
					maxScoreRuleRightId = splitScoreLabel[s+1][length-1];
				}
			}
			if(maxScoreSplitIndex != -1)
			{
				Tree<String> resultTreeLeft = constructBestTree(chartScores,chartSplit,bottleChilds,chartRuleLeft,chartRuleRight,
						0, maxScoreSplitIndex, maxScoreRuleLeftId, mark);
				Tree<String> resultTreeRight = constructBestTree(chartScores,chartSplit,bottleChilds,chartRuleLeft,chartRuleRight,
						maxScoreSplitIndex+1, length-1, maxScoreRuleRightId, mark);
				List<Tree<String>> tmpConstructChilds = new ArrayList<Tree<String>>();		
				tmpConstructChilds.add(resultTreeLeft);
				tmpConstructChilds.add(resultTreeRight);
				resultTree = new Tree<String>(root);
				resultTree.setChildren(tmpConstructChilds);
				System.out.print("#");
			}
			else
			{
				//能由三个phrase拼接
				int maxScoreSplitIndex1 = -1;
				int maxScoreRuleRightId1 = -1; 
				
				for(int s1 = 0; s1 < length-2; s1++)
					for(int s2 = s1+1; s2 < length-1; s2++)
				{
					if(splitScore[0][s1] != 0 && splitScore[s1+1][s2] != 0 && splitScore[s2+1][length-1] != 0
						&& splitScore[0][s1] + splitScore[s1+1][s2] + splitScore[s2+1][length-1] > maxScore)
					{
						maxScore = splitScore[0][s1] + splitScore[s1+1][s2] + splitScore[s2+1][length-1];
						maxScoreSplitIndex = s1; maxScoreSplitIndex1 = s2;
						maxScoreRuleLeftId = splitScoreLabel[0][s1];
						maxScoreRuleRightId = splitScoreLabel[s1+1][s2];
						maxScoreRuleRightId1 =  splitScoreLabel[s2+1][length-1];
					}
				}
				
				if(maxScoreSplitIndex != -1 && maxScoreSplitIndex1 != -1)
				{
					Tree<String> resultTreeLeft = constructBestTree(chartScores,chartSplit,bottleChilds,chartRuleLeft,chartRuleRight,
							0, maxScoreSplitIndex, maxScoreRuleLeftId, mark);
					Tree<String> resultTreeRight = constructBestTree(chartScores,chartSplit,bottleChilds,chartRuleLeft,chartRuleRight,
							maxScoreSplitIndex+1, maxScoreSplitIndex1, maxScoreRuleRightId, mark);
					Tree<String> resultTreeRightRight = constructBestTree(chartScores,chartSplit,bottleChilds,chartRuleLeft,chartRuleRight,
							maxScoreSplitIndex1+1, length-1, maxScoreRuleRightId1, mark);
					
					List<Tree<String>> tmpConstructChildsMid = new ArrayList<Tree<String>>();		
					tmpConstructChildsMid.add(resultTreeLeft);
					tmpConstructChildsMid.add(resultTreeRight);
					Tree<String> resultTreeMid = new Tree<String>(secondLayer[0]+mark);
					resultTreeMid.setChildren(tmpConstructChildsMid);
					
					List<Tree<String>> tmpConstructChilds = new ArrayList<Tree<String>>();		
					tmpConstructChilds.add(resultTreeMid);
					tmpConstructChilds.add(resultTreeRightRight);
					resultTree = new Tree<String>(root);
					resultTree.setChildren(tmpConstructChilds);	
					System.out.print("$");
				}
				
			}
			
			
			
		}
		else
		{
			resultTree = constructBestTree(chartScores,chartSplit,bottleChilds,chartRuleLeft,chartRuleRight,		
				0, length-1, rootLabelIndex, mark);
			resultTree.setLabel(root);
		}
		
		return resultTree;
	}
	
	
	
	public Tree<String> constructBestTree(double[][][] chartScores, int[][][] chartSplit, List<Tree<String>> bottleChilds,
			String[][][] chartRuleLeft, String[][][] chartRuleRight, int startIndex, int endIndex, int rootIndex, String mark)
	{
		if(startIndex == endIndex) return bottleChilds.get(endIndex);
			
		Tree<String> root = null;
		if(startIndex == 0 && endIndex == bottleChilds.size()-1)
		{
			root = new Tree<String>(allNonTerminals.get(rootIndex));
		}
		else
		{
			root = new Tree<String>(allNonTerminals.get(rootIndex)+mark);
		}
		
		int leftEndIndex = chartSplit[startIndex][endIndex][rootIndex];
		int rightStartIndex = leftEndIndex+1;
		int leftRootIndex = allNonTerminalsDigitMap.get(chartRuleLeft[startIndex][endIndex][rootIndex]);
		int rightRootIndex = allNonTerminalsDigitMap.get(chartRuleRight[startIndex][endIndex][rootIndex]);
		
		
		Tree<String> resultTreeLeft = constructBestTree(chartScores,chartSplit,bottleChilds,chartRuleLeft,chartRuleRight,
				startIndex, leftEndIndex, leftRootIndex, mark);
		Tree<String> resultTreeRight = constructBestTree(chartScores,chartSplit,bottleChilds,chartRuleLeft,chartRuleRight,
				rightStartIndex, endIndex, rightRootIndex, mark);
		List<Tree<String>> tmpConstructChilds = new ArrayList<Tree<String>>();		
		tmpConstructChilds.add(resultTreeLeft);
		tmpConstructChilds.add(resultTreeRight);
		root.setChildren(tmpConstructChilds);
		
		return root;
	}
	
	public Tree<String> constructBestTreeVersion2(double[][][] chartScores, int[][][] chartSplit, List<Map<Tree<String>,Double>> bottleChilds, 
			String[][][] chartRuleLeft, String[][][] chartRuleRight, int startIndex, int endIndex, int rootIndex, String mark)
	{
		if(startIndex == endIndex)
		{
			//return bottleChilds.get(endIndex);
			Tree<String> resultTree = null;
			Map<Tree<String>,Double> curTrees = bottleChilds.get(startIndex);
			for(Tree<String> curTree : curTrees.keySet())
			{
				String curLabel = curTree.getLabel();
				int labelIndex = allNonTerminalsDigitMap.get(curLabel);
				if(rootIndex == labelIndex)
				{
					resultTree = curTree;
					break;
				}
			}
			if(resultTree == null)
			{
				System.out.println("constructBestTreeVersion2 error");
			}
			
			return resultTree;
				
		}
			
		Tree<String> root = null;
		if(startIndex == 0 && endIndex == bottleChilds.size()-1)
		{
			root = new Tree<String>(allNonTerminals.get(rootIndex));
		}
		else
		{
			root = new Tree<String>(allNonTerminals.get(rootIndex)+mark);
		}
		
		int leftEndIndex = chartSplit[startIndex][endIndex][rootIndex];
		int rightStartIndex = leftEndIndex+1;
		int leftRootIndex = allNonTerminalsDigitMap.get(chartRuleLeft[startIndex][endIndex][rootIndex]);
		int rightRootIndex = allNonTerminalsDigitMap.get(chartRuleRight[startIndex][endIndex][rootIndex]);
		
		
		Tree<String> resultTreeLeft = constructBestTreeVersion2(chartScores,chartSplit,bottleChilds,chartRuleLeft,chartRuleRight,
				startIndex, leftEndIndex, leftRootIndex, mark);
		Tree<String> resultTreeRight = constructBestTreeVersion2(chartScores,chartSplit,bottleChilds,chartRuleLeft,chartRuleRight,
				rightStartIndex, endIndex, rightRootIndex, mark);
		List<Tree<String>> tmpConstructChilds = new ArrayList<Tree<String>>();		
		tmpConstructChilds.add(resultTreeLeft);
		tmpConstructChilds.add(resultTreeRight);
		root.setChildren(tmpConstructChilds);
		
		return root;
	}
	
	
	public Tree<String> constructBestSkeTree(double[][] chartScores, int[][] chartSplit, List<Tree<String>> bottleChilds,
			 int startIndex, int endIndex, String mark)
	{
		if(startIndex == endIndex) return bottleChilds.get(endIndex);
			
		Tree<String> root = null;
		if(startIndex == 0 && endIndex == bottleChilds.size()-1)
		{
			root = new Tree<String>("##");
		}
		else
		{
			root = new Tree<String>("##"+mark);
		}
		
		int leftEndIndex = chartSplit[startIndex][endIndex];
		int rightStartIndex = leftEndIndex+1;
		
		
		Tree<String> resultTreeLeft = constructBestSkeTree(chartScores,chartSplit,bottleChilds,
				startIndex, leftEndIndex, mark);
		Tree<String> resultTreeRight = constructBestSkeTree(chartScores,chartSplit,bottleChilds,
				rightStartIndex, endIndex, mark);
		List<Tree<String>> tmpConstructChilds = new ArrayList<Tree<String>>();		
		tmpConstructChilds.add(resultTreeLeft);
		tmpConstructChilds.add(resultTreeRight);
		root.setChildren(tmpConstructChilds);
		
		return root;
	}
	
	public  Tree<String> CKYVersion2(String root, List<Tree<String>> bottleChilds, List<Double> bottleChildWeights, String mark)
	{
		if(allNonTerminals == null || fineGrammarWeights == null) return null;
		int length = bottleChilds.size();
		if(length == 1) return bottleChilds.get(0);
		
		int NonTerminalNum = allNonTerminalsNum;
		double[][][] chartScores = new double[length][length][NonTerminalNum];
		int[][][] chartSplit = new int[length][length][NonTerminalNum];
		String[][][] chartRuleLeft = new String[length][length][NonTerminalNum];
		String[][][] chartRuleRight = new String[length][length][NonTerminalNum];
		for(int i = 0; i < length; i++)
			for(int j = 0; j < length; j++)
				for(int k = 0; k < NonTerminalNum; k++)
				{
					chartScores[i][j][k] = 0;
					//left phrase end index
					chartSplit[i][j][k] = -1;
					//left phrase label
					chartRuleLeft[i][j][k]= null;
					//right phrase label
					chartRuleRight[i][j][k]= null;
				}
		for(int i = 0; i < length; i++)
		{
			String curLabel = bottleChilds.get(i).getLabel();
			int labelIndex = allNonTerminalsDigitMap.get(curLabel);
			chartScores[i][i][labelIndex] = bottleChildWeights.get(i);		
		}
		
		for(int l = 1; l < length; l++)
			for(int i = 0; i < length-l; i++)
			{
				int j = i+l;
				//for(int k = 0; k < NonTerminalNum; k++)
				for(int k : fineGrammarWeights.keySet())
				{
					Map<Integer, Map<Integer, Map<String, Double>>> grammarParentWeights = fineGrammarWeights.get(k);
					double maxScore = 0;
					int maxScoreSplitIndex = -1;
					String maxScoreRuleLeft = null;
					String maxScoreRuleRight = null;
					for(int k1 : grammarParentWeights.keySet())
					{
						Map<Integer, Map<String, Double>> grammarParentRightWeights = grammarParentWeights.get(k1);
						for(int k2 : grammarParentRightWeights.keySet())
						{
							Map<String, Double> grammarParentRightLeftWeights = grammarParentRightWeights.get(k2);
							//for(int s = i; s < j; s++)
							for(String posInfo : grammarParentRightLeftWeights.keySet())
							{
								String[] posUnits = posInfo.split("#");
								if(posUnits.length != 3)
								{
									System.out.println("error fineGrammarWeights.");
								}
								int s = Integer.parseInt(posUnits[1]);
								int start = Integer.parseInt(posUnits[0]);
								int end = Integer.parseInt(posUnits[2]);
								if(start != i || end != j)continue;
								if(chartScores[i][s][k2] == 0 || chartScores[s+1][j][k1] == 0) continue;
								double curScore = grammarParentRightLeftWeights.get(posInfo) + chartScores[i][s][k2] + chartScores[s+1][j][k1];
								if(curScore > maxScore)
								{
									maxScore = curScore;
									maxScoreSplitIndex = s;
									maxScoreRuleLeft = allNonTerminals.get(k2);
									maxScoreRuleRight = allNonTerminals.get(k1);
								}
							}							
						}
					}
					chartScores[i][j][k] = maxScore;
					chartSplit[i][j][k] = maxScoreSplitIndex;
					chartRuleLeft[i][j][k]= maxScoreRuleLeft;
					chartRuleRight[i][j][k]= maxScoreRuleRight;
				}
			}
		
		
		
		
		int rootLabelIndex = allNonTerminalsDigitMap.get(root);
		
		Tree<String> resultTree = null;
		
		if(chartScores[0][length-1][rootLabelIndex] != 0)
		{
			resultTree = constructBestTree(chartScores,chartSplit,bottleChilds,chartRuleLeft,chartRuleRight,		
				0, length-1, rootLabelIndex, mark);
		}
		return resultTree;
	}
	
	public void reinitCKYByMultiResults(List<Tree<String>> targetResults, List<Tree<String>> guideResults)
	{
		Map<String, Double> rulesExtract = new HashMap<String, Double>();
		Set<String> nonTerminals = new HashSet<String>();
		//target
		for(Tree<String> curTree : targetResults)
		{
			extracRules(curTree,rulesExtract,nonTerminals,0);
		}
		
		setNonTerminals(nonTerminals);
		setFineGrammars(rulesExtract);
		
		
		if(guideResults != null)
		{
			for(Tree<String> curTree : guideResults)
			{
				reWeightByGuideTree(curTree,0);
			}
		}
		//guide
	}
	
	public static void extracRules(Tree<String> tree, Map<String, Double> rulesExtract, Set<String> nonTerminals, int start)
	{
		if (tree.isLeaf() || tree.isPreTerminal()) {
			return;
		}
		String parentLabel = tree.getLabel();		
		List<Tree<String>> childrenlist = tree.getChildren();
		
		if(childrenlist.size() != 2)
		{
			System.out.println("error");
			return;
		}
		String leftChildLabel = childrenlist.get(0).getLabel();
		String rightChildLabel = childrenlist.get(1).getLabel();
		nonTerminals.add(parentLabel);
		nonTerminals.add(leftChildLabel);
		nonTerminals.add(rightChildLabel);
		
		List<String> leftWords = childrenlist.get(0).getTerminalYield();
		List<String> rightWords = childrenlist.get(1).getTerminalYield();
		
		int mid = start + leftWords.size()-1;
		int end = start + leftWords.size() + rightWords.size()-1;
		
		String binaryRule = String.format("%d#%d#%d", start, mid, end) + "\t" + parentLabel + "\t" 
		+ leftChildLabel + "\t" + rightChildLabel;

		Double iValue = rulesExtract.get(binaryRule);
		if (iValue == null) {
			rulesExtract.put(binaryRule, 1.0);
		} else {
			iValue = iValue + 1;
			rulesExtract.put(binaryRule, iValue);
		}

		extracRules(childrenlist.get(0),rulesExtract, nonTerminals, start);
		extracRules(childrenlist.get(1),rulesExtract, nonTerminals, start + leftWords.size());
	}
	
	
	public void reWeightByGuideTree(Tree<String> tree, int start)
	{
		if (tree.isLeaf() || tree.isPreTerminal()) {
			return;
		}
		String parentLabel = tree.getLabel();		
		List<Tree<String>> childrenlist = tree.getChildren();
		
		if(childrenlist.size() != 2)
		{
			System.out.println("error");
			return;
		}
		String leftChildLabel = childrenlist.get(0).getLabel();
		String rightChildLabel = childrenlist.get(1).getLabel();
		
		List<String> leftWords = childrenlist.get(0).getTerminalYield();
		List<String> rightWords = childrenlist.get(1).getTerminalYield();
		
		int mid = start + leftWords.size()-1;
		int end = start + leftWords.size() + rightWords.size()-1;
		String keyGuide = parentLabel + "\t" + leftChildLabel + "\t" + rightChildLabel;

		if(treemapRule.containsKey(keyGuide))
		{
			for(String key2 : treemapRule.get(keyGuide).keySet())
			{
				//暂时不考虑这么复杂的问题
				if(key2.equals("#null#"))
				{
					if(fineGrammarWeightsIndex.containsKey(start) && 
							fineGrammarWeightsIndex.get(start).containsKey(end))
					{
						Map<Integer,List<Entry<String, Double>>> startEndMap = fineGrammarWeightsIndex.get(start).get(end);
						for(int curMiddle : startEndMap.keySet())
						{
							for(int curMiddleIndex = 0; curMiddleIndex < startEndMap.get(curMiddle).size(); curMiddleIndex++)
							{
								double primeValue = startEndMap.get(curMiddle).get(curMiddleIndex).getValue();
								//startEndMap.get(curMiddle).get(curMiddleIndex).setValue(primeValue - treemapRule.get(keyGuide).get(key2));
								//startEndMap.get(curMiddle).get(curMiddleIndex).setValue(primeValue - 1);
							}
						}
					}
				}
				else
				{
					String[] key2Units = key2.split("\t");
					
					
					if(fineGrammarWeightsIndex.containsKey(start) && 
							fineGrammarWeightsIndex.get(start).containsKey(end) && treemapRule.get(keyGuide).get(key2) > 0.1 )
					{
						Map<Integer,List<Entry<String, Double>>> startEndMap = fineGrammarWeightsIndex.get(start).get(end);
						for(int curMiddle : startEndMap.keySet())
						{
							for(int curMiddleIndex = 0; curMiddleIndex < startEndMap.get(curMiddle).size(); curMiddleIndex++)
							{
								double primeValue = startEndMap.get(curMiddle).get(curMiddleIndex).getValue();
								//startEndMap.get(curMiddle).get(curMiddleIndex).setValue(primeValue - treemapRule.get(keyGuide).get(key2));
								startEndMap.get(curMiddle).get(curMiddleIndex).setValue(primeValue + 0.1);
							}
						}
					}
					
					if(!allNonTerminalsDigitMap.containsKey(key2Units[0])
							|| !allNonTerminalsDigitMap.containsKey(key2Units[1])
							|| !allNonTerminalsDigitMap.containsKey(key2Units[2]))
					{
						continue;
					}
					
					//if(treemapRule.get(keyGuide).get(key2) < 0.05)
					//{
					//	continue;
					//}
					
					if(key2Units.length == 4)
					{						
						int parentKey = allNonTerminalsDigitMap.get(key2Units[0]);
						int leftChildKey = allNonTerminalsDigitMap.get(key2Units[1]);
						int rightChildKey = allNonTerminalsDigitMap.get(key2Units[2]);
						if(fineGrammarWeights.containsKey(parentKey)
						&& fineGrammarWeights.get(parentKey).containsKey(rightChildKey)
						&& fineGrammarWeights.get(parentKey).get(rightChildKey).containsKey(leftChildKey))
						{
							Map<String, Double> grammarParentRightLeftWeights = fineGrammarWeights.get(parentKey).get(rightChildKey).get(leftChildKey);
							List<String> allCandKeys = new ArrayList<String>();
							for(String curCandKey : grammarParentRightLeftWeights.keySet())
							{
								allCandKeys.add(curCandKey);
							}
							
							for(String curCandKey : allCandKeys)
							{
								String posInfo = curCandKey;
								String[] posUnits = posInfo.split("#");
								if(posUnits.length != 3)
								{
									System.out.println("error fineGrammarWeights.");
								}
																
								int s = Integer.parseInt(posUnits[1]);
								if (start == Integer.parseInt(posUnits[0])
									&& end == Integer.parseInt(posUnits[2]))
								{
									double primeWeight = grammarParentRightLeftWeights.get(curCandKey);
									//grammarParentRightLeftWeights.put(curCandKey, primeWeight + treemapRule.get(keyGuide).get(key2)*1.0);
									grammarParentRightLeftWeights.put(curCandKey, primeWeight + 0.4);
								}
								
							}
						}
					}
					else
					{
						int parentKey = allNonTerminalsDigitMap.get(key2Units[0]);
						int leftChildKey = allNonTerminalsDigitMap.get(key2Units[1]);
						int rightChildKey = allNonTerminalsDigitMap.get(key2Units[2]);
						if(fineGrammarWeights.containsKey(parentKey)
						&& fineGrammarWeights.get(parentKey).containsKey(rightChildKey)
						&& fineGrammarWeights.get(parentKey).get(rightChildKey).containsKey(leftChildKey))
						{
							Map<String, Double> grammarParentRightLeftWeights = fineGrammarWeights.get(parentKey).get(rightChildKey).get(leftChildKey);
							List<String> allCandKeys = new ArrayList<String>();
							for(String curCandKey : grammarParentRightLeftWeights.keySet())
							{
								allCandKeys.add(curCandKey);
							}
							
							for(String curCandKey : allCandKeys)
							{
								String posInfo = curCandKey;
								String[] posUnits = posInfo.split("#");
								if(posUnits.length != 3)
								{
									System.out.println("error fineGrammarWeights.");
								}
																
								if (start == Integer.parseInt(posUnits[0])
									&& end == Integer.parseInt(posUnits[2])
									&& mid == Integer.parseInt(posUnits[1]))
								{
									double primeWeight = grammarParentRightLeftWeights.get(curCandKey);
									//grammarParentRightLeftWeights.put(curCandKey, primeWeight + treemapRule.get(keyGuide).get(key2));
									grammarParentRightLeftWeights.put(curCandKey, primeWeight + 0.9);
								}
								
							}
						}
					}
				}				
			}
		}

		reWeightByGuideTree(childrenlist.get(0), start);
		reWeightByGuideTree(childrenlist.get(1), start + leftWords.size());
	}

	public  Tree<String> CKYVersion4(List<Tree<String>> bottleChilds, List<Double> bottleChildWeights, String mark)
	{
		if(allNonTerminals == null || fineGrammarWeights == null) return null;
		int length = bottleChilds.size();
		if(length == 1) return bottleChilds.get(0);
		
		int NonTerminalNum = allNonTerminalsNum;
		double[][][] chartScores = new double[length][length][NonTerminalNum];
		int[][][] chartSplit = new int[length][length][NonTerminalNum];
		String[][][] chartRuleLeft = new String[length][length][NonTerminalNum];
		String[][][] chartRuleRight = new String[length][length][NonTerminalNum];
		for(int i = 0; i < length; i++)
			for(int j = 0; j < length; j++)
				for(int k = 0; k < NonTerminalNum; k++)
				{
					chartScores[i][j][k] = 0;
					//left phrase end index
					chartSplit[i][j][k] = -1;
					//left phrase label
					chartRuleLeft[i][j][k]= null;
					//right phrase label
					chartRuleRight[i][j][k]= null;
				}
		for(int i = 0; i < length; i++)
		{
			String curLabel = bottleChilds.get(i).getLabel();
			int labelIndex = allNonTerminalsDigitMap.get(curLabel);
			chartScores[i][i][labelIndex] = bottleChildWeights.get(i);		
		}
		
		for(int l = 1; l < length; l++)
			for(int i = 0; i < length-l; i++)
			{
				int j = i+l;
				//for(int k = 0; k < NonTerminalNum; k++)
				for(int k : fineGrammarWeights.keySet())
				{
					Map<Integer, Map<Integer, Map<String, Double>>> grammarParentWeights = fineGrammarWeights.get(k);
					double maxScore = 0;
					int maxScoreSplitIndex = -1;
					String maxScoreRuleLeft = null;
					String maxScoreRuleRight = null;
					for(int k1 : grammarParentWeights.keySet())
					{
						Map<Integer, Map<String, Double>> grammarParentRightWeights = grammarParentWeights.get(k1);
						for(int k2 : grammarParentRightWeights.keySet())
						{
							Map<String, Double> grammarParentRightLeftWeights = grammarParentRightWeights.get(k2);
							//for(int s = i; s < j; s++)
							for(String posInfo : grammarParentRightLeftWeights.keySet())
							{
								String[] posUnits = posInfo.split("#");
								if(posUnits.length != 3)
								{
									System.out.println("error fineGrammarWeights.");
								}
								int s = Integer.parseInt(posUnits[1]);
								if(chartScores[i][s][k2] == 0 || chartScores[s+1][j][k1] == 0) continue;
								double curScore = grammarParentRightLeftWeights.get(posInfo) + chartScores[i][s][k2] + chartScores[s+1][j][k1];
								if(curScore > maxScore)
								{
									maxScore = curScore;
									maxScoreSplitIndex = s;
									maxScoreRuleLeft = allNonTerminals.get(k2);
									maxScoreRuleRight = allNonTerminals.get(k1);
								}
							}							
						}
					}
					chartScores[i][j][k] = maxScore;
					chartSplit[i][j][k] = maxScoreSplitIndex;
					chartRuleLeft[i][j][k]= maxScoreRuleLeft;
					chartRuleRight[i][j][k]= maxScoreRuleRight;
				}
			}
		
		
		
		
		//int rootLabelIndex = allNonTerminalsDigitMap.get(root);
		double maxScoreRoot = 0;
		int rootLabelIndex = -1;
		for(int rootId = 0; rootId < NonTerminalNum; rootId++)
		{
			if(chartScores[0][length-1][rootId] > maxScoreRoot)
			{
				maxScoreRoot = chartScores[0][length-1][rootId];
				rootLabelIndex = rootId;
			}
		}
		
		Tree<String> resultTree = null;
		
		if(maxScoreRoot != 0)
		{
			resultTree = constructBestTree(chartScores,chartSplit,bottleChilds,chartRuleLeft,chartRuleRight,		
				0, length-1, rootLabelIndex, mark);
		}
		else
		{
			//fineGrammarWeights
			//print model
			//System.out.println();
			//resultTree = new Tree<String>(""); 
		}
		return resultTree;
	}

	public  Tree<String> CKYVersion5(List<Map<Tree<String>,Double>> bottleChilds, String mark)
	{
		if(allNonTerminals == null || fineGrammarWeights == null) return null;
		int length = bottleChilds.size();
		if(length == 1)
		{
			Map<Tree<String>,Double> curTrees = bottleChilds.get(0);
			double maxScore = 0;
			Tree<String> bestTree = null;
			for(Tree<String> curTree : curTrees.keySet())
			{
				if(curTrees.get(curTree) > maxScore)
				{
					maxScore = curTrees.get(curTree);
					bestTree = curTree;
				}
			}
			return bestTree;
		}
		
		int NonTerminalNum = allNonTerminalsNum;
		double[][][] chartScores = new double[length][length][NonTerminalNum];
		int[][][] chartSplit = new int[length][length][NonTerminalNum];
		String[][][] chartRuleLeft = new String[length][length][NonTerminalNum];
		String[][][] chartRuleRight = new String[length][length][NonTerminalNum];
		for(int i = 0; i < length; i++)
			for(int j = 0; j < length; j++)
				for(int k = 0; k < NonTerminalNum; k++)
				{
					chartScores[i][j][k] = 0;
					//left phrase end index
					chartSplit[i][j][k] = -1;
					//left phrase label
					chartRuleLeft[i][j][k]= null;
					//right phrase label
					chartRuleRight[i][j][k]= null;
				}
		for(int i = 0; i < length; i++)
		{
			//String curLabel = bottleChilds.get(i).getLabel();
			//int labelIndex = allNonTerminalsDigitMap.get(curLabel);
			//for(int k = 0; k < NonTerminalNum; k++)
			//{
			//chartScores[i][i][k] = bottleChildWeights.get(i);	
			Map<Tree<String>,Double> curTrees = bottleChilds.get(i);
			for(Tree<String> curTree : curTrees.keySet())
			{
				String curLabel = curTree.getLabel();
				int labelIndex = allNonTerminalsDigitMap.get(curLabel);
				chartScores[i][i][labelIndex] = curTrees.get(curTree);	
			}
		}
		
		for(int l = 1; l < length; l++)
			for(int i = 0; i < length-l; i++)
			{
				int j = i+l;
				//for(int k = 0; k < NonTerminalNum; k++)
				for(int k : fineGrammarWeights.keySet())
				{
					Map<Integer, Map<Integer, Map<String, Double>>> grammarParentWeights = fineGrammarWeights.get(k);
					double maxScore = 0;
					int maxScoreSplitIndex = -1;
					String maxScoreRuleLeft = null;
					String maxScoreRuleRight = null;
					for(int k1 : grammarParentWeights.keySet())
					{
						Map<Integer, Map<String, Double>> grammarParentRightWeights = grammarParentWeights.get(k1);
						for(int k2 : grammarParentRightWeights.keySet())
						{
							Map<String, Double> grammarParentRightLeftWeights = grammarParentRightWeights.get(k2);
							//for(int s = i; s < j; s++)
							for(String posInfo : grammarParentRightLeftWeights.keySet())
							{
								String[] posUnits = posInfo.split("#");
								if(posUnits.length != 3)
								{
									System.out.println("error fineGrammarWeights.");
								}
								int s = Integer.parseInt(posUnits[1]);
								int start = Integer.parseInt(posUnits[0]);
								int end = Integer.parseInt(posUnits[2]);
								if(start != i || end != j) continue;
								if(chartScores[i][s][k2] == 0 || chartScores[s+1][j][k1] == 0) continue;
								double curScore = grammarParentRightLeftWeights.get(posInfo) + chartScores[i][s][k2] + chartScores[s+1][j][k1];
								if(curScore > maxScore)
								{
									maxScore = curScore;
									maxScoreSplitIndex = s;
									maxScoreRuleLeft = allNonTerminals.get(k2);
									maxScoreRuleRight = allNonTerminals.get(k1);
								}
							}							
						}
					}
					chartScores[i][j][k] = maxScore;
					chartSplit[i][j][k] = maxScoreSplitIndex;
					chartRuleLeft[i][j][k]= maxScoreRuleLeft;
					chartRuleRight[i][j][k]= maxScoreRuleRight;
				}
			}
		
		
		
		//printDetailedInfo(logWriter, chartScores, chartSplit, chartRuleLeft, chartRuleRight, bottleChilds);
		
		//int rootLabelIndex = allNonTerminalsDigitMap.get(root);
		double maxScoreRoot = 0;
		int rootLabelIndex = -1;
		for(int rootId = 0; rootId < NonTerminalNum; rootId++)
		{
			if(chartScores[0][length-1][rootId] > maxScoreRoot)
			{
				maxScoreRoot = chartScores[0][length-1][rootId];
				rootLabelIndex = rootId;
			}
		}
		
		Tree<String> resultTree = null;
		
		if(maxScoreRoot != 0)
		{
			resultTree = constructBestTreeVersion2(chartScores,chartSplit,bottleChilds,chartRuleLeft,chartRuleRight,		
				0, length-1, rootLabelIndex, mark);
		}
		else
		{
			//fineGrammarWeights
			//print model
			//System.out.println();
			//resultTree = new Tree<String>(""); 
		}
		return resultTree;
	}

	
	public  Tree<String> CKYVersion6(List<Tree<String>> bottleChilds, List<Double> bottleChildWeights, String mark, PrintWriter logWriter)
	{
		if(fineSkeWeights == null) return null;
		int length = bottleChilds.size();
		if(length == 1) return bottleChilds.get(0);
		
		double[][] chartScores = new double[length][length];
		int[][] chartSplit = new int[length][length];
		
		for(int i = 0; i < length; i++)
			for(int j = 0; j < length; j++)
				{
					chartScores[i][j] = 0;
					//left phrase end index
					chartSplit[i][j] = -1;
				}
		for(int i = 0; i < length; i++)
		{
			chartScores[i][i] = bottleChildWeights.get(i);		
		}
		
		for(int l = 1; l < length; l++)
			for(int i = 0; i < length-l; i++)
			{
				int j = i+l;					
				double maxScore = 0;
				int maxScoreSplitIndex = -1;
				for(int s = i; s < j; s++)
				{
					double curScore = 0.0;
					if(fineSkeWeights.containsKey(i)
						&& fineSkeWeights.get(i).containsKey(j)
						&& fineSkeWeights.get(i).get(j).containsKey(s))
					{
						curScore = fineSkeWeights.get(i).get(j).get(s);
					}
					curScore = curScore + chartScores[i][s] + chartScores[s+1][j];
					if(curScore > maxScore)
					{
						maxScore = curScore;
						maxScoreSplitIndex = s;
					}					
				}
				chartScores[i][j] = maxScore;
				chartSplit[i][j] = maxScoreSplitIndex;				
			}
		
		
		
		
		
		//int rootLabelIndex = allNonTerminalsDigitMap.get(root);
		double maxScoreRoot = chartScores[0][length-1];
		
		Tree<String> resultTree = null;
		
		if(maxScoreRoot != 0)
		{
			resultTree = constructBestSkeTree(chartScores,chartSplit,bottleChilds,
					0, length-1, mark);
		}
		else
		{
			//fineGrammarWeights
			//print model
			//System.out.println();
			//resultTree = new Tree<String>(""); 
		}
		return resultTree;
	}

	public void restoreTree(Tree<String> tree)
	{
		if(tree.isLeaf())
		{
			return;
		}
		List<Tree<String>> childrens = tree.getChildren();
		for(Tree<String> curChild : childrens)restoreTree(curChild);		
		String curLabel = tree.getLabel();
		String[] miniLabels = curLabel.split("@");
		
		if(miniLabels.length == 2)
		{
			Tree<String> tempTree = new Tree<String>(miniLabels[1]);
			tempTree.setChildren(tree.getChildren());
			
			List<Tree<String>> tmpConstructChilds = new ArrayList<Tree<String>>();
			tmpConstructChilds.add(tempTree);
			tree.setChildren(tmpConstructChilds);
			tree.setLabel(miniLabels[0]);
		}
		else if (miniLabels.length == 1)
		{
			
		}
		else
		{
			System.out.println("Fuck the data!!!");
		}
		
	}

	
	public void convertTree(Tree<String> parent, int index)
	{
		List<Tree<String>> childrenlist = parent.getChildren();

		Tree<String> child = childrenlist.get(index);
		if (child.isLeaf() || child.isPreTerminal()) {
			return;
		}

		List<Tree<String>> grandchildlist = child.getChildren();
		if (grandchildlist.size() == 1) {
			parent.setChild(index, grandchildlist.get(0));
			grandchildlist.get(0).setLabel(child.getLabel() + "@" + grandchildlist.get(0).getLabel());
			convertTree(parent, index);
		} else {
			for (int i = 0; i < grandchildlist.size(); i++) {
				convertTree(child, i);
			}
		}	
	}
	
	public void convert1Tree(Tree<String> rootTree)
	{
		List<Tree<String>> childlist = rootTree.getChildren();
		for (int index = 0; index < childlist.size(); index++) {
			convertTree(rootTree, index);
		}
		if(childlist.size()==1)
		{
			String newLabel = rootTree.getLabel()+"@" + childlist.get(0).getLabel();
			rootTree.setLabel(newLabel);
			rootTree.setChildren(childlist.get(0).getChildren());
		}
	}
	
	public  boolean Binarize(Tree<String> parent, int index) {
		Tree<String> tree = parent.getChild(index);
		if (tree.isLeaf() || tree.isPreTerminal()) {
			return true;
		}

		List<Tree<String>> childrenlist = tree.getChildren();

		if (childrenlist.size() == 2) {
			if(!Binarize(tree, 0)) return false;
			if(!Binarize(tree, 1))return false;
		} else if (childrenlist.size() > 2) {
			// 构造两个map，测试一下怎么转换的。
			String rootLabel = tree.getLabel();
			List<Double> childWeights = new ArrayList<Double>();
			int curIndex = 0;
			List<Tree<String>> tmpConstructChilds = new ArrayList<Tree<String>>();
						
			for (Tree<String> curChild : childrenlist) {
				if(!Binarize(tree, curIndex))return false;
				tmpConstructChilds.add(new Tree<String>(curChild.getLabel()));
				childWeights.add(1.0);
				curIndex++;
			}

			Tree<String> newTree = CKYVersion1(rootLabel, childrenlist,
					childWeights, "*");
			
			
			if(newTree != null)
			{
				parent.setChild(index, newTree);
			}
			else
			{
				int childsize = childrenlist.size();
				Tree<String> rightTree = childrenlist.get(childsize-1);
				Tree<String> leftTree = null;
				for(int iChild = childsize - 2; iChild >= 0; iChild--)
				{
					leftTree = childrenlist.get(iChild);
					tmpConstructChilds = new ArrayList<Tree<String>>();
					tmpConstructChilds.add(leftTree);
					tmpConstructChilds.add(rightTree);
					Tree<String> tempTree = null;
					if(iChild == 0)
					{
						tempTree = new Tree<String>(rootLabel);
					}
					else
					{
						tempTree = new Tree<String>(rootLabel + "*");
					}
					tempTree.setChildren(tmpConstructChilds);
					rightTree = tempTree;
				}
				
				parent.setChild(index, rightTree);
				//leftTree = childrenlist.get(0);
				//tmpConstructChilds = new ArrayList<Tree<String>>();
				//tmpConstructChilds.add(leftTree);
				//tmpConstructChilds.add(rightTree);
				//parent.setChildren(tmpConstructChilds);
				
				/*
				String rule = orgTree.toString() + "====> error!";
				
				Integer value = midStatics.get(rule);
				if (value == null) {
					midStatics.put(rule, 1);
				} else {
					value = value + 1;
					midStatics.put(rule, value);
				}*/
				//return false;
			}

		} 
		else {
			System.out.println("Still exist rule A->B.");
			return false;
		}
		
		return true;
	}
	
	public void convert2Tree(Tree<String> curTree)
	{
		Tree<String> newTree = new Tree<String>("ROOT");
		List<Tree<String>> tmpConstructChilds = new ArrayList<Tree<String>>();
		tmpConstructChilds.add(curTree);
		newTree.setChildren(tmpConstructChilds);
		if(Binarize(newTree, 0))
		{
			curTree = newTree.getChild(0);
		}
		else
		{
			curTree = null;
		}
	}
	
	//只有当目前节点 label是间接节点。
	public void getActualChilds(Tree<String> parent, List<Tree<String>> childrens)
	{
		if(!parent.getLabel().endsWith("*"))return;
		for(Tree<String> curChild: parent.getChildren())
		{
			if(!curChild.getLabel().endsWith("*"))childrens.add(curChild);
			else
			{
				getActualChilds(curChild, childrens);
			}
		}
	}
	
	public void restore2TreeInterface(Tree<String> parent)
	{	
		if(parent.isLeaf() || parent.isPreTerminal())
		{
			return;
		}
		List<Tree<String>> tmpConstructChilds = new ArrayList<Tree<String>>();
		for(int i = 0; i < parent.getChildren().size(); i++)
		{
			Tree<String> curTree = parent.getChild(i);
			//叶子节点和词性节点都不可能为中间节点
			if(curTree.isLeaf() || curTree.isPreTerminal())
			{
				tmpConstructChilds.add(curTree);
				continue;
			}	
			String curLabel = curTree.getLabel();
			if(curLabel.endsWith("*"))
			{
				getActualChilds(curTree,tmpConstructChilds);
			}
			else
			{
				tmpConstructChilds.add(curTree);
			}
		}
		parent.setChildren(tmpConstructChilds);
		
		for(Tree<String>  curTree : tmpConstructChilds)
		{
			restore2TreeInterface(curTree);
		}			
			
	}
	
	public void restore2Tree(Tree<String> curTree)
	{
		restore2TreeInterface(curTree);
	}

	
	
	
	
	public void printDetailedInfo(PrintWriter logWriter, double[][][] chartScores, int [][][] chartSplit, String[][][] chartRuleLeft, String[][][] chartRuleRight, List<Tree<String>> bottleChilds)
	{
		int length = chartScores.length;
		logWriter.println("Save non-terminals.......");
		for(int i = 0; i < allNonTerminalsNum; i++)
		{
			String theNonTerminal = allNonTerminals.get(i);
			//for(String theNonTerminal: allNonTerminals)
			//{
			logWriter.println(String.format("%s\t%d", theNonTerminal, allNonTerminalsDigitMap.get(theNonTerminal)));
			//}
		}
			
		logWriter.println("Save grammars.......");
		//Map<Integer,Map<Integer,Map<Integer,Map<String, Double>>>> fineGrammarWeights
		for(int parent: fineGrammarWeights.keySet())
		{
			Map<Integer, Map<Integer, Map<String, Double>>> grammarParentWeights = fineGrammarWeights.get(parent);
			for(int right : grammarParentWeights.keySet())
			{
				Map<Integer, Map<String, Double>> grammarParentRightWeights = grammarParentWeights.get(right);
				for(int left : grammarParentRightWeights.keySet())
				{
					Map<String, Double> grammarParentRightLeftWeights = grammarParentRightWeights.get(left);
					for(String posInfo : grammarParentRightLeftWeights.keySet())
					{
						String parentLabel = allNonTerminals.get(parent);
						String leftLabel = allNonTerminals.get(left);
						String rightLabel = allNonTerminals.get(right);
						
						logWriter.println(String.format("%s\t%s\t%s\t%s,%f", posInfo, parentLabel, leftLabel, rightLabel, grammarParentRightLeftWeights.get(posInfo)));							
					}
				}
			}
		}
		
		logWriter.println("Write sentences......");
		String sentence = "";
		for(int i = 0; i < length; i++)
		{
			sentence = sentence + bottleChilds.get(i).toString() + "\t";
		}
		
		logWriter.println(sentence.trim());
		
		logWriter.println("Save charts .......");
		
		for(int i = 0; i < length; i++)
			for(int j = length-1; j >= i; j--)
				for(int k = 0; k < allNonTerminalsNum; k++)
				{
					if(chartScores[i][j][k] == 0) continue;
					String curScoreInfo = null;
					if(j != i)
					{
						curScoreInfo = String.format("(%d,%d,%f), %s->[%s,%s](%d)", i, j, chartScores[i][j][k],
								allNonTerminals.get(k), chartRuleLeft[i][j][k], chartRuleRight[i][j][k], chartSplit[i][j][k]);
					}
					else
					{
						curScoreInfo = String.format("(%d, %s, %f)", i, allNonTerminals.get(k), chartScores[i][j][k]);
					}
					logWriter.println(curScoreInfo);
				}					
	}

}
