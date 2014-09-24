package preparation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class RemoveDuplicatedLines {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		String sLine = null;
		Set<String> savedSentence = new HashSet<String>();
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"));
		while ((sLine = in.readLine()) != null) {
			String newLine = sLine.trim();
			if(newLine.length() < 2)continue;
			newLine = newLine.replace("\\s+", " ");
			if(!savedSentence.contains(newLine))
			{
				output.println(newLine);
				savedSentence.add(newLine);
			}
		}
		
		in.close();
		output.close();

	}

}
