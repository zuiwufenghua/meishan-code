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

public class OOVLabel {

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
		
		
		int exampleSize = sentenceGold.size();
		Map<String, Integer> IVHighFreq = new HashMap<String, Integer>();
		for(int iy = 0; iy < exampleSize; iy++)
		{
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
		
		
		in = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(predictFile), "UTF-8"));
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"));
		while ((sLine = in.readLine()) != null) {
			String newLine = sLine.trim();
			String[] wordposs = newLine.split("\\s+");
			String outline = "";
			int ivNumber = 0;
			int totalWordNum = 0;
			for(String curWordPOS : wordposs)
			{
				String tag = "OOV";
				if(IVHighFreq.containsKey(curWordPOS))
				{
					tag = "IV";
					if(!curWordPOS.substring(1,2).equals("_") || curWordPOS.endsWith("PU"))
					{
						ivNumber++;
					}
				}
				totalWordNum++;
				if(curWordPOS.indexOf("_") > 4 && tag.equals("OOV"))
				{
					ivNumber = - wordposs.length;
				}
				
				//outline = outline + " "+ curWordPOS + "_" + tag;
				outline = outline + " "+ curWordPOS;
				if(curWordPOS.equals("。_PU")
					|| curWordPOS.equals("?_PU")
					|| curWordPOS.equals("!_PU"))
				{
					if(ivNumber > totalWordNum/2)
					{
						output.println(outline.trim());
						outline = "";
					}
					totalWordNum = 0;
					ivNumber = 0;
				}
			}
			
			if(!outline.equals("") && ivNumber > totalWordNum/2)
			{
				output.println(outline.trim());
			}
			
		}
		
		in.close();
		output.close();

	}

}
