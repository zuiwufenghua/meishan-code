package preparation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class HMMModelProcess {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8"));
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"));
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			if(sLine.equals("transitions"))
			{
				output.println(sLine);
				sLine = in.readLine();
				sLine = sLine.trim();
				List<String> processedLine = new ArrayList<String>();
				processScalaOptionsLine(sLine, processedLine);
				for(String theline : processedLine)
				{
					output.println(theline);
				}
				output.println();
			}
			else if(sLine.equals("emissions"))
			{
				output.println(sLine);
				sLine = in.readLine();
				sLine = sLine.trim();
				List<String> processedLine = new ArrayList<String>();
				processScalaOptionsLine(sLine, processedLine);
				for(String theline : processedLine)
				{
					output.println(theline);
				}
				output.println();
			}
			else
			{
				continue;
			}
			
		}
		
		in.close();
		output.close();
	}
	
	public static  void processScalaOptionsLine(String line, List<String> processedLine)
	{
		line = line.trim();
		String remainline = "";
		String curline = "";
		while(line.length() > 1)
		{
			if(line.startsWith("CondFreqDist("))
			{
				remainline = line.substring("CondFreqDist(".length());
			}
			else if(line.startsWith("Map(Some(") || line.startsWith(", "))
			{
				int stardpos = "Map(Some(".length();
				if(line.startsWith(", "))
				{
					stardpos = ", ".length();
				}

				int index1 =  line.indexOf(", ", stardpos);
				int index2 =  line.indexOf("Map(Some(", stardpos);
				int minIndex = -1;
				if(index1 >= 0 && index2 >= 0)
				{
					if(index2 > index1)
					{
						minIndex = index1;
						remainline = line.substring(minIndex);
						curline = line.substring(stardpos, minIndex);	
					}
					else
					{
						minIndex = index2;
						remainline = line.substring(minIndex);
						curline = line.substring(stardpos, minIndex);	
					}
				}
				else if(index1 >= 0)
				{
					minIndex = index1;
					remainline = line.substring(minIndex);
					curline = line.substring(stardpos, minIndex);
				}
				else if(index2 >= 0)
				{
					minIndex = index2;
					remainline = line.substring(minIndex);
					curline = line.substring(stardpos, minIndex);	
				}
				else
				{
					curline = line.substring(stardpos).trim();
					remainline = "";
				}
				
				curline = replace(curline, "Some(", "");
				curline = replace(curline, "Map(", "");
				curline = replace(curline, "(", "");
				curline = replace(curline, ")", "");
				processedLine.add(curline.trim());
			}
			else
			{
				System.out.println(line);
			}
			
			line = remainline;
		}
	}
	
	public static String replace(String inputstr, String pattern, String replacepattern)
	{
		String result = inputstr;
		
		int patternIndex = indexOf(result, pattern);
		while(patternIndex > -1)
		{
			String tempresult1 = patternIndex == 0 ? "" : result.substring(0, patternIndex);
			String tempresult2 = patternIndex + pattern.length() == result.length() ? "" :result.substring(patternIndex + pattern.length());
			result = tempresult1;
			if(!replacepattern.equals(""))
			{
				result = result + replacepattern;
			}
			if(!tempresult2.equals(""))
			{
				result = result + tempresult2;
			}
			patternIndex = indexOf(result, pattern);;
		}
		
		
		return result;
	}
	
	public static int indexOf(String stringinput, String parttern)
	{
		for(int idx = 0; idx <= stringinput.length() - parttern.length(); idx++)
		{
			String tempstr = stringinput.substring(idx, idx + parttern.length());
			if(tempstr.equals(parttern))
			{
				return idx;
			}
		}
		
		return -1;
	}

}
