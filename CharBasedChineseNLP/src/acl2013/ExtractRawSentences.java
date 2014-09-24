package acl2013;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class ExtractRawSentences {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub

			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(args[0]), "UTF8"));
			
			PrintWriter out = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(args[1]), "UTF-8"), false);
			String sLine = null;
			while ((sLine = in.readLine()) != null) {
				if(sLine.trim().equals(""))continue;
				String[] wordposs = sLine.trim().split("\\s+");
				String rawline = "";
				for(String wordpos : wordposs)
				{
					int splitIndex = wordpos.lastIndexOf("_");
					if(splitIndex == -1)
					{
						System.out.println(wordpos + "[in]" + sLine);
						continue;
					}
					String theWord = wordpos.substring(0, splitIndex);
					rawline = rawline + theWord;
					
				}
				out.println(rawline);
			}
			
			in.close();
			out.close();

	}

}
