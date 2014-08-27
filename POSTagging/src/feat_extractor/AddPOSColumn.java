package feat_extractor;

import mason.utils.MapSort;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class AddPOSColumn {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		String sLine = null;
		String sLine2 = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		BufferedReader in_2 = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[1]), "UTF8"));
		
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);
		while ((sLine = in.readLine()) != null && (sLine2 = in_2.readLine()) != null) {
			if(sLine.trim().equals("") && sLine2.trim().equals(""))
			{
				out.println();
				continue;				
			}
			else if(sLine.trim().equals("") || sLine2.trim().equals(""))
			{
				System.out.println("error....." + sLine);
			}
			String[] poscandidates1 = sLine.trim().split("\\s+");
			String[] poscandidates2 = sLine2.trim().split("\\s+");
			
			if(poscandidates1.length < 1  || poscandidates2.length < 1)
			{
				System.out.println("error....." + sLine);
				return;
			}
			
			Set<String> posprobs = new HashSet<String>();
			for(int idx = 0; idx < poscandidates1.length; idx++ )
			{
				posprobs.add(poscandidates1[idx]);				
			}
			
			String curOut = sLine.trim();
			for(int idx = 0; idx < poscandidates2.length; idx++)
			{
				if(!posprobs.contains(poscandidates2[idx]))
				{
					curOut = curOut + "\t" + poscandidates2[idx];
				}
			}
			
			out.println(curOut);
		}
		
		out.close();
		in.close();
		in_2.close();

	}

}
