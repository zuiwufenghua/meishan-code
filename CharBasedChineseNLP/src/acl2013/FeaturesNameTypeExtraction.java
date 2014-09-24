package acl2013;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class FeaturesNameTypeExtraction {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Map<String, String> variableandtypes = new HashMap<String, String>();
		Map<String, String> featuresandtypes = new HashMap<String, String>();
		
		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		String sLine = null;
		while ((sLine = in.readLine()) != null) {
			sLine = sLine.trim(); 
			if(sLine.equals(""))continue;
			String[] framments = sLine.split("\\s+");
			String theContent = "";
			for(int idx = 0; idx < framments.length; idx++)
			{
				String theKey = framments[idx].trim();
				
				//if(theKey.startsWith("&st0_word"))
				//{
				//	System.out.println("strange");
				//}
				
				if(theKey.startsWith("&"))
				{
					theKey = theKey.substring(1);
				}
				
				if(idx > 0)
				{
					String theType = "";
					if(theContent.endsWith("Word"))
					{
						theType = "Word";
					}
					else if(theContent.endsWith("Tag"))
					{
						theType = "Tag";
					}
					else if(theContent.endsWith("long"))
					{
						theType = "Ulong";
					}
					else
					{
						theType = "";
					}
					
					if(theType.length() > 1 && !variableandtypes.containsKey(theKey))
					{
						variableandtypes.put(theKey, theType);
					}
						
				}
				
				theContent = theContent + " " + theKey;
				
			}
			
			int featureIndex = sLine.indexOf(".getOrUpdateScore(");
			if(featureIndex != -1)
			{
				String firstPart = sLine.substring(0, featureIndex);
				int featureStartIndex = firstPart.lastIndexOf("->");
				String featurename = firstPart.substring(featureStartIndex + 2);
				String secondPart = sLine.substring(featureIndex + ".getOrUpdateScore(".length());
				String[] variables = secondPart.split(",");
				
				if(variables[2].trim().equals("action"))
				{
					String theType = variables[1].replace("\\s+", "").trim();
					if(variableandtypes.containsKey(theType))
					{
						String featureType = "C" + variableandtypes.get(theType) + "Map";
						if(!featuresandtypes.containsKey(featurename))
						{
							featuresandtypes.put(featurename, featureType);
						}
						else
						{
							System.out.println("Error:\t" + featurename + "\t" +theType);
						}
					}
					else
					{
						String featureType = "C";
						String[] smallfrags = theType.split("_");
						assert(smallfrags.length > 1);
						boolean validtype = true;
						for(int idx = 0; idx < smallfrags.length; idx++)
						{
							if(smallfrags[idx].equals("word"))
							{
								featureType = featureType + "Word";
							}
							else if(smallfrags[idx].equals("tag"))
							{
								featureType = featureType + "Tag";
							}
							else if(smallfrags[idx].equals("ulong"))
							{
								featureType = featureType + "Ulong";
							}
							else
							{
								System.out.println("Unssported type: " + smallfrags[idx]);
								validtype = false;
							}
						}	
						
						featureType = featureType + "Map";
						
						if(validtype && !featuresandtypes.containsKey(featurename))
						{
							featuresandtypes.put(featurename, featureType);
						}
						else
						{
							System.out.println("Error:\t" + featurename + "\t" +theType);
						}
					}
				}
				else if(variables[3].trim().equals("action"))
				{
					assert(variables[1].trim().startsWith("std::make_pair"));
					String variablepart1 = variables[1].replace("\\s+", "").trim();
					int bracket1Index = variablepart1.lastIndexOf("(");
					assert(bracket1Index != -1);
					variablepart1 = variablepart1.substring(bracket1Index+1);
					String variablepart2 = variables[2].replace("\\s+", "").trim();
					assert(variablepart2.endsWith(")"));
					variablepart2 = variablepart2.substring(0, variablepart2.length()-1);
					if(variableandtypes.containsKey(variablepart1) && variableandtypes.containsKey(variablepart2) && !featuresandtypes.containsKey(featurename))
					{
						String featureType = "C" + variableandtypes.get(variablepart1) + variableandtypes.get(variablepart2) + "Map";
						featuresandtypes.put(featurename, featureType);
					}
					else
					{
						System.out.println("Error:\t" + featurename + "\t" + variablepart1 + "\t" + variablepart2);
					}
				}
			}


		}
		
		in.close();
		
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		for(String theKey : featuresandtypes.keySet())
		{
			out.println("left(" + theKey + ")right\\");
		}
		
		out.println();
		out.println();
		
		for(String theKey : featuresandtypes.keySet())
		{
			out.println("left " + theKey + " middle " + theKey + " right\\");
		}
		
		out.println();
		out.println();
		
		for(String theKey : featuresandtypes.keySet())
		{
			out.println(featuresandtypes.get(theKey) + "    " + theKey + ";");
		}
		
		out.println();
		out.println();
		
		for(String theKey : featuresandtypes.keySet())
		{
			out.println(theKey + "(\"" +theKey.substring(5) + "\", dep_table_size),");
		}
			
		out.close();
		

	}

}
