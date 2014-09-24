package preparation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class AddModelCopyLine2Sh {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		BufferedReader in = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(args[0])));
		String sLine = null;
		
		List<String> allLines = new ArrayList<String>();
		String addedLine = "";
		boolean bContainCP = false;
		while ((sLine = in.readLine()) != null) {
			String newLine = sLine.trim();
			if(newLine.equals(""))
			{
				continue;
			}
			
			if(newLine.startsWith("./train"))
			{
				String[] substrparas = newLine.split("\\s+");
				if(substrparas[2].endsWith(".model"))
				{
					addedLine = "\tcp " + substrparas[2] + " " + substrparas[2] + ".$i";
				}
			}
			if(newLine.startsWith("cp"))
			{
				bContainCP = true;
			}
			
			if(newLine.equals("done"))
			{
				if(!bContainCP && addedLine.length() > 1)
				{
					allLines.add(addedLine);
				}
			}
			
			allLines.add(sLine);
		}
		
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1])));
		output.println();
		
		for(String oneLine : allLines)
		{
			output.println(oneLine);
		}
		
		output.close();

	}

}
