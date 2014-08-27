package feat_extractor;



import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class UniversalPOSTaggerMap {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(args[0]), "UTF-8"));
		Map<String, String> posmapping = new HashMap<String, String>();
		String sLine = null;
		while ((sLine = reader.readLine()) != null) {
			if (sLine.trim().equals(""))
				continue;
			String[] wordElems = sLine.trim().split("\\s+");
			if(wordElems.length != 2) continue;
			//if(wordElems[1].equals("."))
			//{
			//	posmapping.put(wordElems[0], "PU");
			//}
			//else
			//{
				posmapping.put(wordElems[0], wordElems[1]);
			//}
		}
		
		reader.close();
		
		reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[1]), "UTF-8"));
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);
		while ((sLine = reader.readLine()) != null) {
			if (sLine.trim().equals(""))
				continue;
			String[] wordElems = sLine.trim().split("\\s+");

			int wordNum = wordElems.length;
			String[] theWords = new String[wordNum];
			String[] thePoss = new String[wordNum];
			String[] theNewPoss = new String[wordNum];
			String outline = "";
			for (int idx = 0;  idx < wordNum; idx++) {
				String curWord = wordElems[idx].trim();
				String curPOS = "FAKEPOS";
				int lastSplitIndex = wordElems[idx].lastIndexOf("_");
				if (lastSplitIndex == -1) {
					lastSplitIndex = wordElems[idx].lastIndexOf("_");
				}
				if (lastSplitIndex == -1) {
					
					System.out.println("Error: [ " + sLine + "]\t" + wordElems[idx]);
					break;
				}
				curWord = wordElems[idx].substring(0, lastSplitIndex).trim();
				curPOS = wordElems[idx].substring(lastSplitIndex + 1).trim();
				theWords[idx] = curWord;
				thePoss[idx] = curPOS;
				
				if(posmapping.containsKey(curPOS))
				{
					theNewPoss[idx] = posmapping.get(curPOS);
					outline = outline + " " + theWords[idx] + "_" + theNewPoss[idx];
				}
				else
				{
					theNewPoss[idx] = "X";
					outline = outline + " " + theWords[idx] + "_" + theNewPoss[idx];
					//System.out.println("ErrorPOS: [ " + sLine + "]\t" + curPOS);
					//break;
				}
			}
			output.println(outline.trim());

		}
		
		reader.close();
		output.close();

	}

}
