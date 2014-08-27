package edu.berkeley.nlp.syntax;

import edu.berkeley.nlp.util.CollectionUtils;
import edu.berkeley.nlp.util.MapFactory;
import edu.berkeley.nlp.util.MyMethod;
import edu.berkeley.nlp.util.Pair;

import java.io.Serializable;
import java.util.*;

/**
 * Represent linguistic trees, with each node consisting of a label and a list
 * of children.
 * 
 * @author Dan Klein
 * 
 * Added function to get a map of subtrees to constituents.
 */
public class Tree<L> implements Serializable, Comparable<Tree<L>>, Iterable<Tree<L>> {

	private static final long serialVersionUID = 1L;

	L label;

	List<Tree<L>> children;
	public Tree<L> parent;
	public int childSeq;
	public boolean binitparent;
	
	public int smaller;
	public int bigger;
	public int root;
	

	private  static <L> void initParentElem(Tree<L> curTree)
	{
		if(curTree.isLeaf())
		{
			return;
		}
		
		List<Tree<L>> childrenlist = curTree.getChildren();
		
		int theChildSeq = 0;
		for(Tree<L> curChild : childrenlist)
		{
			curChild.parent = curTree;
			curChild.childSeq = theChildSeq;
			theChildSeq++;
			initParentElem(curChild);
		}
	}
	
	public void  initParent()
	{
		parent = null;
		childSeq = 0;
		initParentElem(this);
	}

  public void setChild(int i, Tree<L> child) {
    children.set(i,child);
  }

	public void setChildren(List<Tree<L>> c) {
		this.children = c;
	}

	public List<Tree<L>> getChildren() {
		return children;
	}

  public Tree<L> getChild(int i) {
    return children.get(i);
  }

	public L getLabel() {
		return label;
	}
	
	public L getFirstLabel() {
		//�������-,@
		String[] firstLayer = label.toString().split("-");
		String[] secondLayer = firstLayer[0].split("@");
		return (L)(secondLayer[0]);
	}
	
	public L getLastLabel() {
		//�������-,@
		String[] firstLayer = label.toString().split("-");
		String[] secondLayer = firstLayer[firstLayer.length-1].split("@");
		return (L)(secondLayer[secondLayer.length-1]);
	}
	
	public void convert2WordSEG()
	{
		List<Tree<L>> prevYields = this.getPreTerminals();
		for(Tree<L> curPrevYield : prevYields)
		{
			String curWord = curPrevYield.getChild(0).getLabel().toString();
			String curLabel =  curPrevYield.getLabel().toString();
			int curWordLength = curWord.length();
			if(curWordLength == 1)continue;
			
			Tree<L> rightTree = new Tree<L>((L)("E#"+curLabel));
			List<Tree<L>> curTreeChilds = new ArrayList<Tree<L>>();
			curTreeChilds.add(new Tree<L>((L)(curWord.substring(curWordLength-1))));
			rightTree.setChildren(curTreeChilds);
			Tree<L> leftTree = null;
			List<Tree<L>> tmpConstructChilds = null;
			for(int iChild = curWordLength - 2; iChild > 0; iChild--)
			{
				leftTree = new Tree<L>((L)("M#"+curLabel));
				curTreeChilds = new ArrayList<Tree<L>>();
				curTreeChilds.add(new Tree<L>((L)(curWord.subSequence(iChild, iChild+1))));
				leftTree.setChildren(curTreeChilds);
				tmpConstructChilds = new ArrayList<Tree<L>>();
				tmpConstructChilds.add(leftTree);
				tmpConstructChilds.add(rightTree);
				Tree<L> tempTree = new Tree<L>((L)("ME"+curLabel));
				tempTree.setChildren(tmpConstructChilds);
				rightTree = tempTree;
			}
			
			leftTree = new Tree<L>((L)("B#"+curLabel));
			curTreeChilds = new ArrayList<Tree<L>>();
			curTreeChilds.add(new Tree<L>((L)(curWord.subSequence(0, 1))));
			leftTree.setChildren(curTreeChilds);
			tmpConstructChilds = new ArrayList<Tree<L>>();
			tmpConstructChilds.add(leftTree);
			tmpConstructChilds.add(rightTree);
			curPrevYield.setChildren(tmpConstructChilds);
		}
	}

	public boolean convertBack2Phrase(Tree<L> curTree)
	{
		if(curTree.isLeaf())
		{
			System.out.println("Impossible!");
			return false;
		}
		
		if(curTree.isPreTerminal())
		{
			return true;
		}
		
		List<Tree<L>> curChildren = curTree.getChildren();
		
		if(curChildren.size() > 1 && curChildren.get(0).isPreTerminal() 
				&& curChildren.get(0).getLabel().toString().length() > 1
				&& curChildren.get(0).getLabel().toString().substring(1,2).equals("#"))
		{
			List<L> yields = curTree.getTerminalYield();
			String curWord = yields.get(0).toString();
			for(int i = 1; i < yields.size(); i++)
			{
				curWord = curWord + yields.get(i).toString();
			}
			
			List<Tree<L>> curPosChildrens = new ArrayList<Tree<L>>();
			curPosChildrens.add(new Tree<L>((L)(curWord)));
			curTree.setChildren(curPosChildrens);
		}
		else
		{
			boolean finalResult = true;
			for(Tree<L> tempTree: curChildren)
			{
				if(tempTree.isLeaf())
				{
					System.out.println();
				}
				 if(!convertBack2Phrase(tempTree))
				 {
					 finalResult = false;
				 }
			}
			return finalResult;
		}
		
		return true;
	}
	public boolean isLeaf() {
		return getChildren().isEmpty();
	}

	public boolean isPreTerminal() {
		//return getChildren().size() == 1 && getChildren().get(0).isLeaf();
		return getChildren().size() >= 1 && getChildren().get(0).isLeaf();
	}

	public List<L> getYield() {
		List<L> yield = new ArrayList<L>();
		appendYield(this, yield);
		return yield;
	}

	public Collection<Constituent<L>> getConstituentCollection() {
		Collection<Constituent<L>> constituents = new ArrayList<Constituent<L>>();
		appendConstituent(this, constituents, 0);
		return constituents;
	}

	/**
	 * John: I changed this from a hash map because it was broken as a HashMap.
	 */
	public Map<Tree<L>, Constituent<L>> getConstituents() {
		Map<Tree<L>, Constituent<L>> constituents = new IdentityHashMap<Tree<L>, Constituent<L>>();
		appendConstituent(this, constituents, 0);
		return constituents;
	}

  public Map<Pair<Integer,Integer>, List<Tree<L>>> getSpanMap() {
    Map<Tree<L>, Constituent<L>> cMap = getConstituents();
    Map<Pair<Integer,Integer>, List<Tree<L>>> spanMap = new HashMap();
    for (Map.Entry<Tree<L>, Constituent<L>> entry : cMap.entrySet()) {
      Tree<L> t = entry.getKey();
      Constituent<L> c = entry.getValue();
      Pair<Integer,Integer> span = Pair.newPair(c.getStart(),c.getEnd()+1);
      CollectionUtils.addToValueList(spanMap,span,t);
    }
    for (List<Tree<L>> trees : spanMap.values()) {
      Collections.sort(trees,new Comparator<Tree<L>>() {
        public int compare(Tree<L> t1, Tree<L> t2) {
          return t2.getDepth()-t1.getDepth();
      }});          
    }
    return spanMap;
  }

	public Map<Tree<L>, Constituent<L>> getConstituents(MapFactory mf) {
		Map<Tree<L>, Constituent<L>> constituents = mf.buildMap();
		appendConstituent(this, constituents, 0);
		return constituents;
	}

	private static <L> int appendConstituent(Tree<L> tree,
			Map<Tree<L>, Constituent<L>> constituents, int index) {
		if (tree.isLeaf()) {
			Constituent<L> c = new Constituent<L>(tree.getLabel(), index, index);
			constituents.put(tree, c);
			return 1; // Length of a leaf constituent
		} else {
			int nextIndex = index;
			for (Tree<L> kid : tree.getChildren()) {
				nextIndex += appendConstituent(kid, constituents, nextIndex);
			}
			Constituent<L> c = new Constituent<L>(tree.getLabel(), index, nextIndex - 1);
			constituents.put(tree, c);
			return nextIndex - index; // Length of a leaf constituent
		}
	}

	private static <L> int appendConstituent(Tree<L> tree,
			Collection<Constituent<L>> constituents, int index) {
		if (tree.isLeaf() || tree.isPreTerminal()) {
			Constituent<L> c = new Constituent<L>(tree.getLabel(), index, index);
			constituents.add(c);
			return 1; // Length of a leaf constituent
		} else {
			int nextIndex = index;
			for (Tree<L> kid : tree.getChildren()) {
				nextIndex += appendConstituent(kid, constituents, nextIndex);
			}
			Constituent<L> c = new Constituent<L>(tree.getLabel(), index, nextIndex - 1);
			constituents.add(c);
			return nextIndex - index; // Length of a leaf constituent
		}
	}

	private static <L> void appendNonTerminals(Tree<L> tree, List<Tree<L>> yield) {
	  	if (tree.isLeaf()) {
	  	
	  		return;
	  	}
	  	yield.add(tree);
	    for (Tree<L> child : tree.getChildren()) {
	      appendNonTerminals(child, yield);
	    }
	}
	  
	public List<Tree<L>> getTerminals() {
		List<Tree<L>> yield = new ArrayList<Tree<L>>();
		appendTerminals(this, yield);
		return yield;
	}

    public List<Tree<L>> getNonTerminals(){
	  	List<Tree<L>> yield = new ArrayList<Tree<L>>();
	  	appendNonTerminals(this, yield);
	  	return yield;
    }
    
    //public void 
    
    public void annotateSubTrees() {
    	final List<L> yield = getYield();
    	this.bigger = yield.size()-1;
    	this.smaller = 0;
		annotateSubTreesHelper(this, 0, yield.size()-1);   	
	}
 
    private static <L> void annotateSubTreesHelper(Tree<L> tree, int start, int end){
    	if (tree.isPreTerminal()){
    		if (start == end) 
    		{
    			tree.root = start;
    			tree.getChildren().get(0).bigger = tree.getChildren().get(0).smaller = start;
    			tree.getChildren().get(0).root = start;
    		}
    		else
    		{
    			System.out.println("Error in annotateSubTreesHelper.");
    		}
		}
    	
    	int startId = start;
    	for (Tree<L> child : tree.getChildren()) {
    		List<L> currYield = child.getYield();
    		int curEndId = startId + currYield.size()-1;
    		child.smaller = startId; child.bigger = curEndId;    		
    		annotateSubTreesHelper(child, startId, curEndId);
    		String curLable = child.label.toString();
    		if(child.label.toString().endsWith("-HEAD"))
    		{
    			tree.root = child.root;
    		}
    		startId = curEndId + 1;
		}	  	
    }
    
    public void annotateSubTreesByChar() {
    	String  charsequence = getTerminalStr();
    	this.bigger = charsequence.length()-1;
    	this.smaller = 0;
    	annotateSubTreesByCharHelper(this, 0, charsequence.length()-1);   	
	}
 
    private static <L> void annotateSubTreesByCharHelper(Tree<L> tree, int start, int end){
    	if (tree.isPreTerminal()){
    		String curWord = tree.getTerminalStr();
    		if (start + curWord.length() -1 == end) 
    		{
    			tree.getChildren().get(0).bigger = end;
    			tree.getChildren().get(0).smaller = start;
    		}
    		else
    		{
    			System.out.println("Error in annotateSubTreesHelper.");
    		}
		}
    	
    	int startId = start;
    	for (Tree<L> child : tree.getChildren()) {
    		String phrase = child.getTerminalStr();
    		int curEndId = startId + phrase.length()-1;
    		child.smaller = startId; child.bigger = curEndId;    		
    		annotateSubTreesByCharHelper(child, startId, curEndId);
    		startId = curEndId + 1;
		}	  	
    }


    
    public Tree<L> getSubTrees(List<Integer> finalIds, int index) {
    	List<Integer> changeIds = new ArrayList<Integer>();
    	for(int theId : finalIds)
    	{
    		changeIds.add(theId + index);
    	}
		return getSubTreesHelper(this, changeIds);
	}
 
    private static <L> Tree<L>  getSubTreesHelper(Tree<L> tree, List<Integer> finalIds){
    	if(finalIds.size() == 0)return null;
    	Tree<L> yield = new Tree<L>(tree.label);
    	if (tree.isLeaf()){
    		if (finalIds.size() == 1 && finalIds.get(0) == 1) 
    		{
    			return yield;
    		}
    		else
    		{
    			return null;
    		}
		}
    	
    	int startId = 0;
    	int curPos = 0;
    	yield.children = new ArrayList<Tree<L>>();
    	for (Tree<L> child : tree.getChildren()) {
    		List<L> currYield = child.getYield();
    		int curEndId = startId + currYield.size();
    		List<Integer> childIds = new ArrayList<Integer>();
    		for(int j = curPos; j < finalIds.size(); j++)
    		{
    			if(finalIds.get(j) <= curEndId)
    			{
    				if(finalIds.get(j) >= startId)
    				{
    					childIds.add(finalIds.get(j) - startId);
    				}
    			}
    			else
    			{
    				break;
    			}
    		}
    		
    		final Tree<L> curSubTree = getSubTreesHelper(child, childIds);
    		if(curSubTree != null)
    		{    				
    			yield.children.add(curSubTree);
    		}
    		curPos = curPos + childIds.size();
    		startId = curEndId;
		}
	  	
	  	
	  	return yield;
    }

	
    
    public Tree<String> getSubTreeStructure(List<Integer> finalIds, int index) {
    	if(finalIds.size() == 0)return null;
    	List<Integer> changeIds = new ArrayList<Integer>();
    	for(int theId : finalIds)
    	{
    		changeIds.add(theId + index);
    	}
    	Tree<String> theYield = new Tree<String>(label.toString());
    	List<Tree<String>> childTrees = getSubTreeStructureHelper(this, changeIds);
    	theYield.children = new ArrayList<Tree<String>>();
    	for(Tree<String> childtree : childTrees)
    	{
    		theYield.children.add(childtree);
    	}
		return theYield;
	}
 
    private static <L> List<Tree<String>>  getSubTreeStructureHelper(Tree<L> tree, List<Integer> finalIds){
    	if(finalIds.size() == 0)return null;
    	//Tree<String> yield = new Tree<String>(tree.label);
    	List<Tree<String>> childrens = new ArrayList<Tree<String>>();
    	if (tree.isPreTerminal()){
    		if (finalIds.size() == 1 && finalIds.get(0) == 1) 
    		{
    			childrens.add(new Tree<String>("S"));
    			return childrens;
    		}
    		else
    		{
    			return null;
    		}
		}
    	
    	int startId = 0;
    	int curPos = 0;
    	//List<Tree<String>> childrens = new ArrayList<Tree<String>>();
    	int childSize = tree.getChildren().size();
    	if(childSize == 1)
    	{
    		//for(int i = 0; i < childSize; i++)
    		{
    			Tree<L> child = tree.getChildren().get(0);
    			List<L> currYield = child.getYield();
        		int curEndId = startId + currYield.size();
        		List<Integer> childIds = new ArrayList<Integer>();
        		for(int j = curPos; j < finalIds.size(); j++)
        		{
        			if(finalIds.get(j) <= curEndId)
        			{
        				if(finalIds.get(j) >= startId)
        				{
        					childIds.add(finalIds.get(j) - startId);
        				}
        			}
        			else
        			{
        				break;
        			}
        		}
        		
        		List<Tree<String>> curSubTree = getSubTreeStructureHelper(child, childIds);
        		if(curSubTree != null)
        		{
        			Tree<String> theChild = new Tree<String>("S");
	        		theChild.children = new ArrayList<Tree<String>>();
	        		for(Tree<String> gcChild : curSubTree)
	        		{
	        			theChild.children.add(gcChild);
	        		}
	        		childrens.add(theChild);
        		}
        		
        		curPos = curPos + childIds.size();
        		startId = curEndId;
    		}
    	}
    	else
    	{
    		for(int i = 0; i < childSize; i++)
    		{
    			Tree<L> child = tree.getChildren().get(i);
    			List<L> currYield = child.getYield();
        		int curEndId = startId + currYield.size();
        		List<Integer> childIds = new ArrayList<Integer>();
        		for(int j = curPos; j < finalIds.size(); j++)
        		{
        			if(finalIds.get(j) <= curEndId)
        			{
        				if(finalIds.get(j) >= startId)
        				{
        					childIds.add(finalIds.get(j) - startId);
        				}
        			}
        			else
        			{
        				break;
        			}
        		}
        		
        		List<Tree<String>> curSubTree = getSubTreeStructureHelper(child, childIds);
        		if(curSubTree != null)
        		{
        			String curPosition = "M";
            		if(i == 0 )curPosition = "B";
            		if(i == childSize-1 )curPosition = "E";
            		Tree<String> theChild = new Tree<String>(curPosition);
	        		theChild.children = new ArrayList<Tree<String>>();
	        		for(Tree<String> gcChild : curSubTree)
	        		{
	        			theChild.children.add(gcChild);
	        		}
	        		childrens.add(theChild);
        		}
        		
        		curPos = curPos + childIds.size();
        		startId = curEndId;
    		}
    	}
    	
	  	
	  	return childrens;
    }

    

    
	private static <L> void appendTerminals(Tree<L> tree, List<Tree<L>> yield) {
		if (tree.isLeaf()) {
			yield.add(tree);
			return;
		}
		for (Tree<L> child : tree.getChildren()) {
			appendTerminals(child, yield);
		}
	}

	/**
	 * Clone the structure of the tree. Unfortunately, the new labels are copied
	 * by reference from the current tree.
	 * 
	 * @return
	 */
	public Tree<L> shallowClone() {
		ArrayList<Tree<L>> newChildren = new ArrayList<Tree<L>>(children.size());
		for (Tree<L> child : children) {
			newChildren.add(child.shallowClone());
		}
		return new Tree<L>(label, newChildren);
	}

	/**
	 * Return a clone of just the root node of this tree (with no children)
	 * 
	 * @return
	 */
	public Tree<L> shallowCloneJustRoot() {

		return new Tree<L>(label);
	}

	private static <L> void appendYield(Tree<L> tree, List<L> yield) {
		if (tree.isLeaf()) {
			yield.add(tree.getLabel());
			return;
		}
		for (Tree<L> child : tree.getChildren()) {
			appendYield(child, yield);
		}
	}

	public List<L> getPreTerminalYield() {
		List<L> yield = new ArrayList<L>();
		appendPreTerminalYield(this, yield);
		return yield;
	}

	public List<L> getTerminalYield() {
		List<Tree<L>> terms = getTerminals();
		List<L> yield = new ArrayList<L>();
		for (Tree<L> term : terms) {
			yield.add(term.getLabel());
		}
		return yield;
	}

	public List<Tree<L>> getPreTerminals() {
		List<Tree<L>> preterms = new ArrayList<Tree<L>>();
		appendPreTerminals(this, preterms);
		return preterms;
	}

	public List<Tree<L>> getTreesOfDepth(int depth) {
		List<Tree<L>> trees = new ArrayList<Tree<L>>();
		appendTreesOfDepth(this, trees, depth);
		return trees;
	}

	private static <L> void appendPreTerminalYield(Tree<L> tree, List<L> yield) {
		if (tree.isPreTerminal()) {
			yield.add(tree.getLabel());
			return;
		}
		for (Tree<L> child : tree.getChildren()) {
			appendPreTerminalYield(child, yield);
		}
	}

	private static <L> void appendPreTerminals(Tree<L> tree, List<Tree<L>> yield) {
		if (tree.isPreTerminal()) {
			yield.add(tree);
			return;
		}
		for (Tree<L> child : tree.getChildren()) {
			appendPreTerminals(child, yield);
		}
	}

	private static <L> void appendTreesOfDepth(Tree<L> tree, List<Tree<L>> yield, int depth) {
		if (tree.getDepth() == depth) {
			yield.add(tree);
			return;
		}
		for (Tree<L> child : tree.getChildren()) {
			appendTreesOfDepth(child, yield, depth);
		}
	}

	public List<Tree<L>> getPreOrderTraversal() {
		ArrayList<Tree<L>> traversal = new ArrayList<Tree<L>>();
		traversalHelper(this, traversal, true);
		return traversal;
	}

	public List<Tree<L>> getPostOrderTraversal() {
		ArrayList<Tree<L>> traversal = new ArrayList<Tree<L>>();
		traversalHelper(this, traversal, false);
		return traversal;
	}

	private static <L> void traversalHelper(Tree<L> tree, List<Tree<L>> traversal,
			boolean preOrder) {
		if (preOrder) traversal.add(tree);
		for (Tree<L> child : tree.getChildren()) {
			traversalHelper(child, traversal, preOrder);
		}
		if (!preOrder) traversal.add(tree);
	}

	public int getDepth() {
		int maxDepth = 0;
		for (Tree<L> child : children) {
			int depth = child.getDepth();
			if (depth > maxDepth) maxDepth = depth;
		}
		return maxDepth + 1;
	}

  public int size() {
    int sum = 0;
    for (Tree<L> child : children) {
      sum += child.size();
    }
    return sum + 1;
  }

	public List<Tree<L>> getAtDepth(int depth) {
		List<Tree<L>> yield = new ArrayList<Tree<L>>();
		appendAtDepth(depth, this, yield);
		return yield;
	}

	private static <L> void appendAtDepth(int depth, Tree<L> tree, List<Tree<L>> yield) {
		if (depth < 0) return;
		if (depth == 0) {
			yield.add(tree);
			return;
		}
		for (Tree<L> child : tree.getChildren()) {
			appendAtDepth(depth - 1, child, yield);
		}
	}

	public void setLabel(L label) {
		this.label = label;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toStringBuilder(sb);
		return sb.toString();
	}
	
	public String toString(int depth) {
		StringBuilder sb = new StringBuilder();
		toStringBuilder(sb, depth);
		return sb.toString();
	}
	

	public void toStringBuilder(StringBuilder sb) {
		if (!isLeaf()) sb.append('(');
		if (getLabel() != null) {
			sb.append(getLabel());
		}
		if (!isLeaf()) {
			for (Tree<L> child : getChildren()) {
				sb.append(' ');
				child.toStringBuilder(sb);
			}
			sb.append(')');
		}
	}
	
	public String toZparString() {
		StringBuilder sb = new StringBuilder();
		toZparStringBuilder(sb);
		return sb.toString();
	}
	
	//only for word structure 
	public void toZparStringBuilder(StringBuilder sb) {
		if (!isLeaf()) sb.append("( ");
		if (getLabel() != null) {
			//sb.append(getLabel());
			String curLabel = getLabel().toString();			
			if(curLabel.endsWith("#r"))
			{
				String firstPart = curLabel.substring(0, curLabel.length()-2);
				sb.append(firstPart + " y");
			}
			else if(curLabel.endsWith("#l"))
			{
				String firstPart = curLabel.substring(0, curLabel.length()-2);
				sb.append(firstPart + " z");
			}
			else if(curLabel.endsWith("#c"))
			{
				String firstPart = curLabel.substring(0, curLabel.length()-2);
				sb.append(firstPart + " x");
			}
			else if(curLabel.endsWith("#b"))
			{
				String firstPart = curLabel.substring(0, curLabel.length()-2);
				sb.append(firstPart + " b");
			}
			else if(curLabel.endsWith("#i"))
			{
				String firstPart = curLabel.substring(0, curLabel.length()-2);
				sb.append(firstPart + " i");
			}
			else
			{
				sb.append(curLabel);
			}
		}
		if (!isLeaf()) {
			for (Tree<L> child : getChildren()) {
				sb.append(" ");
				child.toZparStringBuilder(sb);
			}
			sb.append(" ) ");
		}
	}

	// leaves are characters only
	public String toCLTString() {
		StringBuilder sb = new StringBuilder();
		toCLTStringBuilder(sb);
		return sb.toString();
	}
	
	// leaves are characters only
	public void toCLTStringBuilder(StringBuilder sb) {
		if (!isLeaf()) sb.append("( ");
		if (getLabel() != null) {
			//sb.append(getLabel());
			String curLabel = getLabel().toString();			
			if(curLabel.endsWith("#r"))
			{
				String firstPart = curLabel.substring(0, curLabel.length()-2);
				sb.append(firstPart + " r");
			}
			else if(curLabel.endsWith("#l"))
			{
				String firstPart = curLabel.substring(0, curLabel.length()-2);
				sb.append(firstPart + " l");
			}
			else if(curLabel.endsWith("#s") || (curLabel.indexOf("#") == -1 && children.size() == 1))
			{
				String firstPart = curLabel;
				if(curLabel.endsWith("#s")) firstPart = curLabel.substring(0, curLabel.length()-2);
				sb.append(firstPart + " s");
			}
			else if(curLabel.endsWith("#r*"))
			{
				String firstPart = curLabel.substring(0, curLabel.length()-3);
				sb.append(firstPart + " r*");
			}
			else if(curLabel.endsWith("#l*"))
			{
				String firstPart = curLabel.substring(0, curLabel.length()-3);
				sb.append(firstPart + " l*");
			}
			else if(curLabel.endsWith("#s*"))
			{
				String firstPart = curLabel.substring(0, curLabel.length()-3);
				sb.append(firstPart + " s*");
			}
			else if(curLabel.endsWith("#t"))
			{
				String firstPart = curLabel.substring(0, curLabel.length()-2);
				sb.append(firstPart + " t");
			}
			else if(curLabel.endsWith("#z"))
			{
				String firstPart = curLabel.substring(0, curLabel.length()-2);
				sb.append(firstPart + " z");
			}
			else if(curLabel.endsWith("#y"))
			{
				String firstPart = curLabel.substring(0, curLabel.length()-2);
				sb.append(firstPart + " y");
			}
			else if(curLabel.endsWith("#x"))
			{
				String firstPart = curLabel.substring(0, curLabel.length()-2);
				sb.append(firstPart + " x");
			}
			else if(curLabel.endsWith("#b"))
			{
				String firstPart = curLabel.substring(0, curLabel.length()-2);
				sb.append(firstPart + " b");
			}
			else if(curLabel.endsWith("#i"))
			{
				String firstPart = curLabel.substring(0, curLabel.length()-2);
				sb.append(firstPart + " i");
			}
			else if(curLabel.length() == 1)
			{
				sb.append(curLabel);
			}
			else
			{
				System.out.println("error label :" + curLabel);
			}
		}
		if (!isLeaf()) {
			for (Tree<L> child : getChildren()) {
				sb.append(" ");
				child.toCLTStringBuilder(sb);
			}
			sb.append(" ) ");
		}
	}


	
	public void toStringBuilder(StringBuilder sb, int depth) {
		if(depth == 0) return;
		if (!isLeaf()) sb.append('(');
		if (getLabel() != null) {
			sb.append(getLabel());
		}
		if (!isLeaf()) {
			for (Tree<L> child : getChildren()) {
				sb.append(' ');
				child.toStringBuilder(sb, depth-1);
			}
			sb.append(')');
		}
	}

	/**
	 * Same as toString(), but escapes terminals like so:
	 * ( becomes -LRB-
	 * ) becomes -RRB-
	 * \ becomes -BACKSLASH- ("\" does not occur in PTB; this is our own convention)
	 * This is useful because otherwise it's hard to tell a "(" terminal from the tree's bracket
	 * structure, or tell an escaping \ from a literal.
	 */
	public String toEscapedString() {
		StringBuilder sb = new StringBuilder();
		toStringBuilderEscaped(sb);
		return sb.toString();
	}

	public void toStringBuilderEscaped(StringBuilder sb) {
		if (!isLeaf()) sb.append('(');
		if (getLabel() != null) {
			if (isLeaf()) {
				String escapedLabel = getLabel().toString();
				escapedLabel = escapedLabel.replaceAll("\\(", "-LRB-");
				escapedLabel = escapedLabel.replaceAll("\\)", "-RRB-");
				escapedLabel = escapedLabel.replaceAll("\\\\", "-BACKSLASH-");
				sb.append(escapedLabel);
			} else {
				sb.append(getLabel());
			}
		}
		if (!isLeaf()) {
			for (Tree<L> child : getChildren()) {
				sb.append(' ');
				child.toStringBuilderEscaped(sb);
			}
			sb.append(')');
		}
	}

	public Tree(L label, List<Tree<L>> children) {
		this.label = label;
		this.children = children;
		this.smaller = this.bigger = -1;
		this.root = -1;
		binitparent = false;
	}

	public Tree(L label) {
		this.label = label;
		this.children = Collections.emptyList();
		this.smaller = this.bigger = -1;
		this.root = -1;
		this.binitparent = false;
	}

	/**
	 * Get the set of all subtrees inside the tree by returning a tree rooted at
	 * each node. These are <i>not</i> copies, but all share structure. The
	 * tree is regarded as a subtree of itself.
	 * 
	 * @return the <code>Set</code> of all subtrees in the tree.
	 */
	public Set<Tree<L>> subTrees() {
		return (Set<Tree<L>>) subTrees(new HashSet<Tree<L>>());
	}

	/**
	 * Get the list of all subtrees inside the tree by returning a tree rooted
	 * at each node. These are <i>not</i> copies, but all share structure. The
	 * tree is regarded as a subtree of itself.
	 * 
	 * @return the <code>List</code> of all subtrees in the tree.
	 */
	public List<Tree<L>> subTreeList() {
		return (List<Tree<L>>) subTrees(new ArrayList<Tree<L>>());
	}

	/**
	 * Add the set of all subtrees inside a tree (including the tree itself) to
	 * the given <code>Collection</code>.
	 * 
	 * @param n
	 *            A collection of nodes to which the subtrees will be added
	 * @return The collection parameter with the subtrees added
	 */
	public Collection<Tree<L>> subTrees(Collection<Tree<L>> n) {
		n.add(this);
		List<Tree<L>> kids = getChildren();
		for (Tree<L> kid : kids) {
			kid.subTrees(n);
		}
		return n;
	}

	/**
	 * Returns an iterator over the nodes of the tree. This method implements
	 * the <code>iterator()</code> method required by the
	 * <code>Collections</code> interface. It does a preorder (children after
	 * node) traversal of the tree. (A possible extension to the class at some
	 * point would be to allow different traversal orderings via variant
	 * iterators.)
	 * 
	 * @return An iterator over the nodes of the tree
	 */
	public Iterator<Tree<L>> iterator() {
		return new TreeIterator();
	}

	private class TreeIterator implements Iterator<Tree<L>> {

		private List<Tree<L>> treeStack;

		private TreeIterator() {
			treeStack = new ArrayList<Tree<L>>();
			treeStack.add(Tree.this);
		}

		public boolean hasNext() {
			return (!treeStack.isEmpty());
		}

		public Tree<L> next() {
			int lastIndex = treeStack.size() - 1;
			Tree<L> tr = treeStack.remove(lastIndex);
			List<Tree<L>> kids = tr.getChildren();
			// so that we can efficiently use one List, we reverse them
			for (int i = kids.size() - 1; i >= 0; i--) {
				treeStack.add(kids.get(i));
			}
			return tr;
		}

		/**
		 * Not supported
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * Applies a transformation to all labels in the tree and returns the
	 * resulting tree.
	 * 
	 * @param <O>
	 *            Output type of the transformation
	 * @param trans
	 *            The transformation to apply
	 * @return Transformed tree
	 */
	public <O> Tree<O> transformNodes(MyMethod<L, O> trans) {
		ArrayList<Tree<O>> newChildren = new ArrayList<Tree<O>>(children.size());
		for (Tree<L> child : children) {
			newChildren.add(child.transformNodes(trans));
		}
		return new Tree<O>(trans.call(label), newChildren);
	}

	/**
	 * Applies a transformation to all nodes in the tree and returns the
	 * resulting tree. Different from <code>transformNodes</code> in that you
	 * get the full node and not just the label
	 * 
	 * @param <O>
	 * @param trans
	 * @return
	 */
	public <O> Tree<O> transformNodesUsingNode(MyMethod<Tree<L>, O> trans) {
		ArrayList<Tree<O>> newChildren = new ArrayList<Tree<O>>(children.size());
		O newLabel = trans.call(this);
		for (Tree<L> child : children) {
			newChildren.add(child.transformNodesUsingNode(trans));
		}
		return new Tree<O>(newLabel, newChildren);
	}

	public <O> Tree<O> transformNodesUsingNodePostOrder(MyMethod<Tree<L>, O> trans) {
		ArrayList<Tree<O>> newChildren = new ArrayList<Tree<O>>(children.size());
		for (Tree<L> child : children) {
			newChildren.add(child.transformNodesUsingNode(trans));
		}
		O newLabel = trans.call(this);
		return new Tree<O>(newLabel, newChildren);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		for (Tree<L> child : children) {
			result = prime * result + ((child == null) ? 0 : child.hashCode());
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		if (!(obj instanceof Tree)) return false;
		final Tree<L> other = (Tree<L>) obj;
		if (!this.label.equals(other.label)) return false;
		if (this.getChildren().size() != other.getChildren().size()) return false;
		for (int i = 0; i < getChildren().size(); ++i) {

			if (!getChildren().get(i).equals(other.getChildren().get(i))) return false;
		}
		return true;

	}

	public int compareTo(Tree<L> o) {
		if (!(o.getLabel() instanceof Comparable && getLabel() instanceof Comparable))
			throw new IllegalArgumentException("Tree labels are not comparable");
		int cmp = ((Comparable) o.getLabel()).compareTo(getLabel());
		if (cmp != 0) return cmp;
		int cmp2 = Double.compare(this.getChildren().size(), o.getChildren().size());
		if (cmp2 != 0) return cmp2;
		for (int i = 0; i < getChildren().size(); ++i) {

			int cmp3 = getChildren().get(i).compareTo(o.getChildren().get(i));
			if (cmp3 != 0) return cmp3;
		}
		return 0;

	}

	public boolean isPhrasal() {
		return getYield().size() > 1;
	}

	public Constituent<L> getLeastCommonAncestorConstituent(int i, int j) {
		final List<L> yield = getYield();
		final Constituent<L> leastCommonAncestorConstituentHelper = getLeastCommonAncestorConstituentHelper(
				this, 0, yield.size(), i, j);

		return leastCommonAncestorConstituentHelper;
	}
	
	public String getContext(List<Integer> finalIds)
	{
		String[] kernelLabel = label.toString().split("-");
		if(kernelLabel[0].equals("FRAG"))
		{
			return "FRAG";
		}
		
		String contextResult = String.format("%d%s", finalIds.size(), kernelLabel[0]) + "(";

		int id_pos = 0;	
		//String inContent = "";
		String lastLabel = "####";
		
		
		boolean bLastPU = false;
    	for (Tree<L> child : children) {
    		String[] kernelchildLabel = child.label.toString().split("-");
    		
    		if(kernelchildLabel[0].equals("PU"))
    		{
    			bLastPU = true; 
    			continue;
    		}
    		String curContext = kernelchildLabel[0] + "[";

    		if(kernelchildLabel[0].equals(lastLabel) && bLastPU)
    		{
    			return "reduplicate";
    		}
    		else
    		{
    			lastLabel = kernelchildLabel[0];
    		}
    		
    		bLastPU = false;

    		int startId = child.smaller;
    		int curEndId = child.bigger;
    		int cur_pos = id_pos;
    		for(; cur_pos < finalIds.size(); cur_pos++)
    		{
    			int theId = finalIds.get(cur_pos);
    			if(theId < startId)continue;
    			if(theId > curEndId)break;
    			//if(child.root != theId)return "invalid";
    			String curMark = "n";//general node
    			//if(this.root == theId)curMark = "h"; //phrase head node
    			//if(this.root != theId && child.root == theId)curMark = "n";//general head node
    			
    			
    			curContext = curContext + curMark;
    		}
    		id_pos = cur_pos;
    		curContext = curContext + "]";
    		contextResult = contextResult + curContext;
		}
    	contextResult = contextResult + ")";
    	
		
		return contextResult;
	}

	public Tree<L> getTopTreeForSpan(int i, int j) {
		final List<L> yield = getYield();
		return getTopTreeForSpanHelper(this, 0, yield.size(), i, j);
	}

	private static <L> Tree<L> getTopTreeForSpanHelper(Tree<L> tree, int start, int end,
			int i, int j) {

		assert i <= j;
		if (start == i && end == j) {
			assert tree.getLabel().toString().matches("\\w+");
			return tree;
		}

		Queue<Tree<L>> queue = new LinkedList<Tree<L>>();
		queue.addAll(tree.getChildren());
		int currStart = start;
		while (!queue.isEmpty()) {
			Tree<L> remove = queue.remove();
			List<L> currYield = remove.getYield();
			final int currEnd = currStart + currYield.size();
			if (currStart <= i && currEnd >= j)
				return getTopTreeForSpanHelper(remove, currStart, currEnd, i, j);
			currStart += currYield.size();
		}
		return null;
	}

	private static <L> Constituent<L> getLeastCommonAncestorConstituentHelper(Tree<L> tree,
			int start, int end, int i, int j) {

		if (start == i && end == j) return new Constituent<L>(tree.getLabel(), start, end);

		Queue<Tree<L>> queue = new LinkedList<Tree<L>>();
		queue.addAll(tree.getChildren());
		int currStart = start;
		while (!queue.isEmpty()) {
			Tree<L> remove = queue.remove();
			List<L> currYield = remove.getYield();
			final int currEnd = currStart + currYield.size();
			if (currStart <= i && currEnd >= j) {
				final Constituent<L> leastCommonAncestorConstituentHelper = getLeastCommonAncestorConstituentHelper(
						remove, currStart, currEnd, i, j);
				if (leastCommonAncestorConstituentHelper != null) return leastCommonAncestorConstituentHelper;
				else break;
			}
			currStart += currYield.size();
		}
		return new Constituent<L>(tree.getLabel(), start, end);
	}

	  public boolean hasUnariesOtherThanRoot()
	  {
	  	assert children.size() == 1;
	  	return hasUnariesHelper(children.get(0));
	  	
	  }
	  
	  private boolean hasUnariesHelper(Tree<L> tree)
	  {
	  	if (tree.isPreTerminal())
	  		return false;
	  	if (tree.getChildren().size() == 1)
	  		return true;
	  	for (Tree<L> child : tree.getChildren())
	  	{
	  		if (hasUnariesHelper(child))
	  			return true;
	  	}
	  	return false;
	  }
	  
	  public boolean hasUnaryChain(){
	  	return hasUnaryChainHelper(this, false);
	  }
	  	
	  private boolean hasUnaryChainHelper(Tree<L> tree, boolean unaryAbove){
	  	boolean result = false;
			if (tree.getChildren().size()==1){
				if (unaryAbove) return true;
				else if (tree.getChildren().get(0).isPreTerminal()) return false;
				else return hasUnaryChainHelper(tree.getChildren().get(0), true);
	  	}
	  	else {
	  		for (Tree<L> child : tree.getChildren()){
	  			if (!child.isPreTerminal()) 
	  				result = result || hasUnaryChainHelper(child,false);
	  		}
	  	}
	  	return result;
	  }
	  
	  public void removeUnaryChains(){
	  	removeUnaryChainHelper(this, null);
	  }
	  	
	  private void removeUnaryChainHelper(Tree<L> tree, Tree<L> parent){
	  	if (tree.isLeaf()) return;
	  	if (tree.getChildren().size()==1&&!tree.isPreTerminal()){
				if (parent!=null) {
					tree = tree.getChildren().get(0);
					parent.getChildren().set(0, tree);
					removeUnaryChainHelper(tree, parent);
				}
				else 
					removeUnaryChainHelper(tree.getChildren().get(0), tree);
	  	}
	  	else {
	  		for (Tree<L> child : tree.getChildren()){
	  			if (!child.isPreTerminal()) 
	  				removeUnaryChainHelper(child,null);
	  		}
	  	}
	  }
	  
	  private void removeEmptyNodeHelper(int childId, Tree<L> parent){
		  Tree<L> tree = parent.getChild(childId);
		  List<L> postags = tree.getPreTerminalYield();
		  
		  boolean bAllEmptyNodes = true;
		  boolean bAllNotEmpty = true;
		  for(L curPos : postags)
		  {
			  if(!curPos.toString().equals("-NONE-"))
			  {
				  bAllEmptyNodes = false;
			  }
			  else
			  {
				  bAllNotEmpty = false;
			  }
		  }
		  
		  if(bAllEmptyNodes)
		  {
			  parent.getChildren().remove(childId);
			  return;
		  }
		  else if(bAllNotEmpty)
		  {
			  return;
		  }
		  else
		  {
			  int childSize = tree.getChildren().size();
			  for(int idx = childSize - 1; idx >= 0 ; idx--)
			  {
				  removeEmptyNodeHelper(idx, tree);
			  }
		  }
		  
	  }
	  
	  public void removeEmptyNodes(){
		  if(isPreTerminal() && !label.toString().equals("-NONE-")) return;
		  int childSize = getChildren().size();
		  for(int idx = childSize - 1; idx >= 0 ; idx--)
		  {
			  removeEmptyNodeHelper(idx, this);
		  }
	}
	  
	 public static <L> void  removeFunction(Tree<L> tree){		
			if (tree.isLeaf()) {
				return;
			}
			String curLabel = tree.getLabel().toString();
			String[] subLabels = curLabel.split("@");
			String finalLabel = "";
			for(String tempLabel : subLabels)
			{
				finalLabel = finalLabel + "@" + tempLabel.split("-")[0];
			}
			tree.setLabel((L)(finalLabel.substring(1)));
			List<Tree<L>> childrenlist=tree.getChildren();
			for (Tree<L> child : childrenlist) {
				removeFunction(child);
			}	
		}
	  
	  
	 public String getTerminalStr(){
		  List<L> terminals = getTerminalYield();
		  int childSize = terminals.size();
		  String result = "";
		  for(int idx = 0; idx < childSize ; idx++)
		  {
			  result = result + terminals.get(idx).toString();
		  }
		  return result;
	}
	 
	 public String getPreTerminalStr(){
		  List<L> preterminals = getPreTerminalYield();
		  int childSize = preterminals.size();
		  String result = "";
		  for(int idx = 0; idx < childSize ; idx++)
		  {
			  result = result + preterminals.get(idx).toString();
		  }
		  return result;
	}
	 
	 public  void removeDuplicate()
	 {
		 removeDuplicateHelper(this);
	 }
	 
	 private void removeDuplicateHelper(Tree<L> tree){
		  	if (tree.isLeaf() || tree.isPreTerminal()) return;
		  	if (tree.getChildren().size()==1){
		  		Tree<L> child = tree.getChild(0);
				if(tree.getLabel().equals(child.getLabel()))
				{
					tree.setChildren(child.getChildren());
					removeDuplicateHelper(tree);
				}
				else
				{
					removeDuplicateHelper(child);
				}
		  	}
		  	else {
		  		for (Tree<L> child : tree.getChildren()){
		  			removeDuplicateHelper(child);
		  		}
		  	}
		  }
		

}
