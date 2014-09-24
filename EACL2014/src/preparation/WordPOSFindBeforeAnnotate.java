package preparation;

import mason.utils.PinyinComparator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

public class WordPOSFindBeforeAnnotate {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		
		
		Map<String, Map<String, Integer>> currentwordposs = new TreeMap<String, Map<String, Integer>>();
		Map<String, Integer> currentwordfreq = new TreeMap<String, Integer>();
		// TODO Auto-generated method stub
		int allWordsNum = 0;
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			String newLine = sLine.trim();
			if(newLine.equals(""))
			{
				continue;
			}
			String[] wordposivtags = newLine.split("\\s+");
			allWordsNum += wordposivtags.length;
			for(int idx = 0; idx < wordposivtags.length; idx++)
			{
				String[] curUnits = wordposivtags[idx].split("_");
				
				String left1WordPOSTAG = "[START]_[START]";
				String left2WordPOSTAG = "[START]_[START]";
				
				String right1WordPOSTAG = "[END]_[END]";
				String right2WordPOSTAG = "[END]_[END]";
				if(idx > 0)left1WordPOSTAG = wordposivtags[idx-1];
				if(idx > 1)left2WordPOSTAG = wordposivtags[idx-2];
				if(idx < wordposivtags.length -1)right1WordPOSTAG = wordposivtags[idx+1];
				if(idx < wordposivtags.length -2)right1WordPOSTAG = wordposivtags[idx+2];
				String[] left1Units = left1WordPOSTAG.split("_");
				String[] left2Units = left2WordPOSTAG.split("_");
				String[] right1Units = right1WordPOSTAG.split("_");
				String[] right2Units = right2WordPOSTAG.split("_");
				
				if(left1Units.length != 2 || left2Units.length != 2
				|| right1Units.length != 2 || right2Units.length != 2
				|| curUnits.length != 2)
				{
					System.out.println("error _ num:\t" + left2WordPOSTAG + " " + left1WordPOSTAG + " "
							+ wordposivtags[idx] + " " + right1WordPOSTAG + " " + right2WordPOSTAG);
					continue;
				}

				if(!PinyinComparator.bAllChineseCharacter(curUnits[0]))
				{
					continue;
				}


				String wordKey ="[" + curUnits[0] + "]";
				String wordposKey = "[" + curUnits[0] + "] , " + curUnits[1];

				if(!currentwordfreq.containsKey(wordKey))
				{
					currentwordfreq.put(wordKey, 0);
					currentwordposs.put(wordKey, new TreeMap<String, Integer>());
				}
				
				if(!currentwordposs.get(wordKey).containsKey(wordposKey))
				{
					currentwordposs.get(wordKey).put(wordposKey, 0);
				}
				currentwordfreq.put(wordKey, currentwordfreq.get(wordKey)+1);
				currentwordposs.get(wordKey).put(wordposKey, currentwordposs.get(wordKey).get(wordposKey)+1);

			}
			
		}
		
		in.close();
		
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[1]), "UTF8"));
		
		PrintWriter output_gold = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]+".gold"), "UTF-8"));
		PrintWriter output_remain = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]+".remain"), "UTF-8"));
		while ((sLine = in.readLine()) != null) {
			String newLine = sLine.trim();
			if(newLine.length() < 3 )
			{
				continue;
			}
			String[] wordposivtags = newLine.split("\\s+");
			if(wordposivtags.length == 2)
			{
				boolean bConvienced = false;
				if(currentwordfreq.containsKey(wordposivtags[0]))
				{
					bConvienced = true;
					output_gold.println(newLine);
					List<Entry<String, Integer>> wordpossort = KnowledgeExtractFromAutoSentences.MapIntSort(currentwordposs.get(wordposivtags[0]));
					for(int idy = 0; idy < wordpossort.size(); idy++)
					{
						output_gold.println(String.format("%s\t%d", wordpossort.get(idy).getKey(), wordpossort.get(idy).getValue()));
					}
					output_gold.println();
					
				}
				if(!bConvienced)
				{
					output_remain.println(newLine);
				}
				while ((sLine = in.readLine()) != null)
				{
					String snewLine = sLine.trim();
					if(snewLine.equals(""))
					{
						if(!bConvienced)output_remain.println();
						break;
					}
					else
					{
						if(!bConvienced)output_remain.println(snewLine);
					}
				}
			}
			else
			{
				System.out.println("error");
			}
		}
		
		in.close();
		output_gold.close();
		output_remain.close();

	}

}
