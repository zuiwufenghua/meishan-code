package process;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.text.DecimalFormat;

public class AnalyzeANSResult {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Map<String, String> errorsRes = new HashMap<String, String>();
		
		for(int fileNum = 0; fileNum < args.length - 1; fileNum++)
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
					args[fileNum]), "UTF-8"));
			
			String sLine = null;
			
			while((sLine = reader.readLine()) != null) {
				String[] curUnits = sLine.trim().split("\\s+");
				if(curUnits.length < 4)
				{
					continue;
				}
				String keyStr = curUnits[0];
				for(int idx = 1; idx < curUnits.length-2; idx++)
				{
					keyStr = keyStr + "\t" + curUnits[idx];
				}
				String valueStr = curUnits[curUnits.length-2] + "\t" + curUnits[curUnits.length-1];
				//errorsRes.put(keyStr, valueStr);
				if(errorsRes.containsKey(keyStr))
				{
					String oldValueStr = errorsRes.get(keyStr);
					String[] oldVUnits = oldValueStr.split("\\s+");
					int midEmpNums = 2 * fileNum - oldVUnits.length;
					String compStr = "";
					for(int idx = 0; idx < midEmpNums; idx++)
					{
						compStr = compStr + "\t" + "0";
					}
					if(midEmpNums > 0)
					{
						valueStr = errorsRes.get(keyStr) + "\t" + compStr.trim() + "\t" + valueStr;
					}
					else
					{
						valueStr = errorsRes.get(keyStr) + "\t" + valueStr;
					}
					valueStr = valueStr.trim();
				}
				else
				{
					int midEmpNums = 2 * fileNum;
					String compStr = "";
					for(int idx = 0; idx < midEmpNums; idx++)
					{
						compStr = compStr + "\t" + "0" ;
					}
					
					valueStr = compStr.trim() + "\t" + valueStr;
					valueStr = valueStr.trim();
				}
				
				errorsRes.put(keyStr, valueStr);
				
			}
			reader.close();
		}
		
		
		
		List<String> resultKeySet = new ArrayList<String>();
		for(String theKey : errorsRes.keySet())
		{
			resultKeySet.add(theKey);
		}
		Collections.sort(resultKeySet);
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[args.length - 1]), "UTF-8"));
		
		for(String theKey : resultKeySet)
		{
			int fileNum = args.length - 1;
			String oldValueStr = errorsRes.get(theKey);
			String[] oldVUnits = oldValueStr.split("\\s+");
			int midEmpNums = 2 * fileNum - oldVUnits.length;
			String compStr = "";
			for(int idx = 0; idx < midEmpNums; idx++)
			{
				compStr = compStr + "\t" + "0";
			}
			String valueStr = errorsRes.get(theKey);
			if(midEmpNums > 0)
			{
				valueStr = errorsRes.get(theKey) + "\t" + compStr.trim();
			}
			errorsRes.put(theKey, valueStr);
			output.println(String.format("%s\t%s", theKey, errorsRes.get(theKey)));
		}
		
		for(String theKey : resultKeySet)
		{
			String oldValueStr = errorsRes.get(theKey);
			String[] oldVUnits = oldValueStr.split("\\s+");
			int fileNum = args.length - 1;
			if(oldVUnits.length != fileNum * 2)
			{
				System.out.println("error!");
				break;
			}
			String valueStr = "";
			for(int idx = 0; idx < fileNum; idx++)
			{
				DecimalFormat df=new DecimalFormat("#.00"); 
				int number1 = Integer.parseInt(oldVUnits[2*idx]);
				int number2 = Integer.parseInt(oldVUnits[2*idx+1]);
				if(number2 == 0)
				{
					valueStr = valueStr + "\tnull";
				}
				else
				{
					valueStr = valueStr + "\t" + df.format(number1*100.0/number2);
				}
				
			}
			output.println(String.format("%s\t%s", theKey, valueStr.trim()));
		}
				
		output.close();
	}

}
