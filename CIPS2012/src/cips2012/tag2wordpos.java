package cips2012;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

//�� c1_B#NN c2_M#NN .....ת����c1c2.._NN���ַ�ʽ
public class tag2wordpos {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		FileInputStream fis = new FileInputStream(args[0]);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");

		BufferedReader bf = null;
		bf = new BufferedReader(isr);
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"));
		
		String sLine = null;
		while ((sLine = bf.readLine()) != null) {
			List<String> curWords = new ArrayList<String>();
			List<String> curPoss = new ArrayList<String>();
			String[] wordposs = sLine.trim().split("\\s+");
			boolean bvalidInput = true;
			for (int idx = 0; bvalidInput && idx < wordposs.length; idx++) {
				int lastsplitIndex = wordposs[idx].lastIndexOf("_");
				if(lastsplitIndex == -1)
				{
					bvalidInput = false; break;
				}
				String curTag = wordposs[idx].substring(lastsplitIndex + 1);
				String curChar = wordposs[idx].substring(0, lastsplitIndex);
				if(idx == 0)
				{
					curWords.add(curChar);
					if(curTag.length() > 2 && curTag.substring(1,2).equals("#"))
					{
						curPoss.add(curTag.substring(2));
					}
					else
					{
						curPoss.add(curTag);
					}
					continue;
				}
				
				if(curTag.startsWith("M#") || curTag.startsWith("E#"))
				{
					curWords.set(curWords.size()-1, curWords.get(curWords.size()-1) + curChar);
				}
				else
				{
					curWords.add(curChar);
					if(curTag.length() > 2 && curTag.substring(1,2).equals("#"))
					{
						curPoss.add(curTag.substring(2));
					}
					else
					{
						curPoss.add(curTag);
					}
				}
			}
			
			if(!bvalidInput || curWords.size() != curPoss.size() || curWords.size() < 1)
			{
				System.out.println("error: " + sLine);
				continue;
			}
			
			String curLine = curWords.get(0) + "_" + curPoss.get(0);
			
			for(int idTmp = 1; idTmp < curWords.size(); idTmp++)
			{
				curLine = curLine + " " + curWords.get(idTmp) + "_" + curPoss.get(idTmp);
			}
			
			writer.println(curLine.trim());		
		}
		
		writer.close();
		bf.close();

	}

}
