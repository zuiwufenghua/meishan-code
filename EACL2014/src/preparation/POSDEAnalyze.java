package preparation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class POSDEAnalyze {

	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception{
		// TODO Auto-generated method stub
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] wordposs = sLine.trim().split("\\s+");
			List<String> words = new ArrayList<String>();
			List<String> poss = new ArrayList<String>();
			int lastIndex = poss.size()-1;
			for(String wordpos : wordposs)
			{
				int splitIndex = wordpos.lastIndexOf("_");
				if(splitIndex == -1 || splitIndex == 0 || splitIndex == wordpos.length()-1)
				{
					System.out.println(wordpos + "[in]" + sLine);
					continue;
				}
				String theWord = wordpos.substring(0, splitIndex);
				String thePOS = wordpos.substring(splitIndex+1);
				
				lastIndex = poss.size()-1;
				if(lastIndex > 0 && thePOS.equals("PU"))
				{		
					String lastPOS = poss.get(lastIndex);
					if(lastPOS.startsWith("DE") || lastPOS.startsWith("AS") || lastPOS.startsWith("SP"))
					{
						poss.set(lastIndex, "PED");
					}
				}
				
				words.add(theWord);
				poss.add(thePOS);

				
				//if(thePOS.startsWith("DE"))
				//{
				//	out.println(words.get(words.size()-1) + "_" + poss.get(poss.size()-1));
				//}
				
				//if(theWord.length() == 4 && thePOS.startsWith("VA"))
				//{
				//	poss.add("VV");
				//}
				//else
				//{
				//	poss.add(thePOS);
				//}
				//if(thePOS.startsWith("VA") || thePOS.startsWith("JJ") || thePOS.startsWith("DE"))
				//{
				//	poss.add("PED");
				//}
				//else
				//{
				//	poss.add(thePOS);
				//}
				/*
				if(thePOS.startsWith("DE"))
				{
					poss.add("PED");
					for(int iEnd = poss.size() -2 ; iEnd >= 0; iEnd--)
					{
						if(poss.get(iEnd).startsWith("VA") || poss.get(iEnd).startsWith("JJ")
								|| poss.get(iEnd).startsWith("AD") || poss.get(iEnd).startsWith("PED"))
						{
							poss.set(iEnd, "PED");
						}
					}
				}
				else
				{
					poss.add(thePOS);
				}*/
			}
			{
				lastIndex = poss.size()-1;
				String lastPOS = poss.get(lastIndex);
				if(lastPOS.startsWith("DE") || lastPOS.startsWith("AS") || lastPOS.startsWith("SP"))
				{
					poss.set(lastIndex, "PED");
				}
			}
			
			String newLine = "";
			for(int iStart = 0; iStart < words.size(); iStart++)
			{
				newLine = newLine + words.get(iStart) + "_" + poss.get(iStart) + " ";
			}
			
			out.println(newLine.trim());
		}
		
		in.close();
		out.close();

	}

}
