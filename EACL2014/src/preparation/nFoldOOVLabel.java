package preparation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class nFoldOOVLabel {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		String predictFile = args[1];
		String goldFile = args[0];
		
		List<String> sentenceGold = new ArrayList<String>();
		BufferedReader in = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(goldFile), "UTF-8"));
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			String newLine = sLine.trim();
			if(!newLine.equals(""))
			{
				sentenceGold.add(newLine);
			}
		}
		in.close();
		
		List<String> sentencePred = new ArrayList<String>();
		in = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(predictFile), "UTF-8"));
		while ((sLine = in.readLine()) != null) {
			String newLine = sLine.trim();
			if(!newLine.equals(""))
			{
				sentencePred.add(newLine);
			}
		}
		in.close();
		
		
		int exampleSize = sentenceGold.size();
		int foldNum = Integer.parseInt(args[3]);
		int oneFoldSentNum = exampleSize/foldNum;
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"));
		for(int idx = 0; idx < foldNum; idx++)
		{
			int startidx = idx * oneFoldSentNum;
			int endidx = idx * oneFoldSentNum + oneFoldSentNum -1;
			if(idx == foldNum - 1) endidx = exampleSize-1;
			Map<String, Integer> IVHighFreq = new HashMap<String, Integer>();
			for(int iy = 0; iy < exampleSize; iy++)
			{
				if(iy >= startidx && iy <= endidx)
				{
					continue;
				}
				String[] wordposs = sentenceGold.get(iy).split("\\s+");
				for(String curWordPOS : wordposs)
				{
					if(!IVHighFreq.containsKey(curWordPOS))
					{
						IVHighFreq.put(curWordPOS, 0);
					}
					IVHighFreq.put(curWordPOS, IVHighFreq.get(curWordPOS)+1);
				}
			}
			for(int iy = startidx; iy <= endidx; iy++)
			{
				String[] wordposs = sentencePred.get(iy).split("\\s+");
				String outline = "";
				for(String curWordPOS : wordposs)
				{
					String tag = "OOV";
					if(IVHighFreq.containsKey(curWordPOS))
					{
						tag = "IV";
					}
					
					outline = outline + " "+ curWordPOS + "_" + tag;
				}
				output.println(outline.trim());
			}
		}
		
		
		output.close();

	}

}
