package feat_extractor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExtractPOSTagFeat {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub

		Map<String, Map<String, Integer>> dict = null;
		if(args.length > 3)
		{
			dict = new HashMap<String, Map<String, Integer>>();
			BufferedReader in_dict = new BufferedReader(new InputStreamReader(
					new FileInputStream(args[3]), "UTF8"));
			String sLineD = null;
			while ((sLineD = in_dict.readLine()) != null) {
				if(sLineD.trim().equals("") || sLineD.trim().length() < 2)continue;
				String[] wordposs = sLineD.trim().split("\\s+");
				if(!dict.containsKey(wordposs[0]))
				{
					dict.put(wordposs[0], new HashMap<String, Integer>());
				}
				for(int idx = 1; idx < wordposs.length; idx++)
				{
					int splitIndex = wordposs[idx].lastIndexOf(":");
					if(splitIndex <= 0 || splitIndex >= wordposs[idx].length()-1)
					{
						System.out.println("Invalid dict file: " + sLineD);
						continue;
					}
					String thePOS = wordposs[idx].substring(0, splitIndex);
					int theFreq = Integer.parseInt(wordposs[idx].substring(splitIndex+1));
					if(!dict.get(wordposs[0]).containsKey(thePOS))
					{
						dict.get(wordposs[0]).put(thePOS, 0);
					}
					dict.get(wordposs[0]).put(thePOS, dict.get(wordposs[0]).get(thePOS) + theFreq);
				}
			}
			in_dict.close();
		}
		String language = args[2];
		String sLine = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] wordposs = sLine.trim().split("\\s+");
			String[] words = new String[wordposs.length];
			String[] poss = new String[wordposs.length];

			for(int idx  = 0; idx < wordposs.length; idx++)
			{
				String wordpos = wordposs[idx];
				int splitIndex = wordpos.lastIndexOf("_");
				if(splitIndex == 0)
				{
					System.out.println("error: " + sLine);
					return;
				}
				if(splitIndex == -1 ||  splitIndex == wordpos.length()-1)
				{
					words[idx] = wordposs[idx];
					poss[idx] = "_";
				}
				else
				{
					words[idx] = wordposs[idx].substring(0, splitIndex);
					poss[idx] =  wordposs[idx].substring(splitIndex+1);
				}
			}
			
			
			for(int idx  = 0; idx < wordposs.length; idx++)
			{
				String prev2word = idx >= 2? words[idx-2] : "#START#";
				String prev1word = idx >= 1? words[idx-1] : "#START#";
				String curword = words[idx];
				String next1word = idx+1 < wordposs.length ? words[idx+1] : "#END#";
				String next2word = idx+2 < wordposs.length ? words[idx+2] : "#END#";
				
			   List<String> curfeatures = new ArrayList<String>();
			   curfeatures.add("P2@"+prev2word);
			   curfeatures.add("P1@"+prev1word);
			   curfeatures.add("U@"+curword);
			   curfeatures.add("N1@"+next1word);
			   curfeatures.add("N2@"+next2word);
			   curfeatures.add("BiP1U@" + prev1word + "#" + curword);
			   curfeatures.add("BiN1U@" + next1word + "#" + curword);
			   curfeatures.add("BiP1N1@" + prev1word + "#" + next1word);
			   int fixlength = 5;
			   if(language.equalsIgnoreCase("chinese"))
			   {
				   fixlength = 3;
			   }
			   for(int idy = 1; idy <= fixlength && idy < curword.length(); idy++)
			   {
				   String premark = String.format("PRE%d@", idy);
				   String sufmark = String.format("SUF%d@", idy);
				   curfeatures.add(premark + curword.substring(0, idy));
				   curfeatures.add(sufmark + curword.substring(curword.length()-idy));
			   }
			   
			   if(language.equalsIgnoreCase("chinese"))
			   {
				   for(int idy = 0; idy < curword.length(); idy++)
				   {
					   curfeatures.add("curChar@" + curword.substring(idy, idy+1));
				   }
				   
				   curfeatures.add(String.format("length@%d", curword.length() > 5 ? 5:curword.length() ));
				   
				   curfeatures.add("firstlastchars@" + curword.substring(0, 1) + curword.substring(curword.length()-1));
				   curfeatures.add("firstbichars@" + curword.substring(0, 1) + (idx >= 1? words[idx-1].substring(0,1) : "#START#"));
			   }
			   
			   if(dict != null)
			   {
				   if(dict.containsKey(curword))
				   {
					   for(String candidatePOS : dict.get(curword).keySet())
					   {
						   if(dict.get(curword).get(candidatePOS) >= 5)
						   {
							   curfeatures.add("dictpos@"+candidatePOS);
						   }
					   }
				   }
			   }
			   String output = poss[idx];
			   for(String thefeat : curfeatures)
			   {
				   output = output + " " + thefeat;
			   }
			   out.println(output.trim());
			}
			
			out.println();
		}
		
		out.close();
		in.close();

	}

}
