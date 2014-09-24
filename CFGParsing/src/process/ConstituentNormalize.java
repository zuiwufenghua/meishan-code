package process;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.*;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class ConstituentNormalize {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub

		List<Tree<String>> consTrees = new ArrayList<Tree<String>>();
		BufferedReader cfgreader = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0])));
		
		int nFold = Integer.parseInt(args[2]);
		boolean bRandom = false;
		if(nFold < 0)
		{
			bRandom = true;
			nFold = -nFold;
		}
		
		

		//String sLine = null;
		//while ((sLine = cfgreader.readLine()) != null) {
		PennTreeReader reader = new PennTreeReader(cfgreader);

		while (reader.hasNext()) {
			Tree<String> tree = reader.next();
			List<String> forms = tree.getYield();
			String sentence = "";
			for (String theWord : forms) {
				sentence = sentence + " " + theWord;
			}
			sentence = sentence.trim();
			
			tree.removeEmptyNodes();
			//check 
			List<String> postags = tree.getPreTerminalYield();
			boolean bContainEmptyNode = false;
			for(String curPos : postags)
			{
				if(curPos.equals("-NONE-"))
				{
					bContainEmptyNode = true;
				}
			}
			if(bContainEmptyNode)
			{
				System.out.println("Error, contain empty nodes.");
			}
			
			tree.removeUnaryChains();
			consTrees.add(tree);
			/*
			if(tree.getChildren().size() == 1 && tree.getLabel().equals("ROOT"))
			{
				consTrees.add(tree.getChild(0));
			}
			else
			{
				consTrees.add(tree);
			}*/
		}
		//}
		cfgreader.close();
		
		
				
		if(bRandom)
		{
			Collections.shuffle(consTrees,  new Random(0));
		}
			
		int totalInstancesNum = consTrees.size();		
		int intervalNum = (totalInstancesNum + nFold - 1)/nFold;
		
		for(int curFold = 0; curFold < nFold; curFold++)
		{
			boolean[] bTrain = new boolean[totalInstancesNum];
			for(int idx = 0; idx < totalInstancesNum; idx++)
			{
				bTrain[idx] = true;
			}
			
			for(int idx = curFold * intervalNum; idx < (curFold+1) * intervalNum && idx < totalInstancesNum; idx++)
			{
				bTrain[idx] = false;
			}
			
			String outputFile1 = args[1] + String.format(".%d.%d", curFold+1, nFold-1);
			String outputFile2 = args[1] + String.format(".%d.%d", curFold+1, 1);
			
			PrintWriter writer1 = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile1), "UTF-8"));
			PrintWriter writer2 = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile2), "UTF-8"));
			
			for(int idx = 0; idx < totalInstancesNum; idx++)
			{
				if(bTrain[idx])
				{
					writer1.println(consTrees.get(idx));
				}
				else
				{
					writer2.println(consTrees.get(idx));
				}
			}
			writer1.close();
			writer2.close();
		}
	}

}
