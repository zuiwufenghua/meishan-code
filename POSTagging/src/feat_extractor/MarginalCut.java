package feat_extractor;

import mason.utils.MapSort;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MarginalCut {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		String sLine = null;
		String sep = "\t";
		if(args.length > 3 && args[3].equals("li"))
		{
			sep = "_";
		}
		String thePostag = "``";
		if(thePostag.equals("``"))
		{
			System.out.println(thePostag);
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		int maxPOSCandidates = Integer.parseInt(args[2]);
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))
			{
				out.println();
				continue;				
			}
			String[] poscandidates = sLine.trim().split("\\s+");
			
			if(poscandidates.length <= 1 )
			{
				System.out.println("error....." + sLine);
				return;
			}
			Map<String, Double> posprobs = new HashMap<String, Double>();
			for(int idx = 1; idx < poscandidates.length; idx++ )
			{
				int lastIndesSep = poscandidates[idx].lastIndexOf(":");
				if(lastIndesSep == -1 )
				{
					System.out.println("error....." + sLine);
					return;
				}
				posprobs.put(poscandidates[idx].substring(0, lastIndesSep), Double.parseDouble(poscandidates[idx].substring(lastIndesSep+1)));				
			}
			
			List<Entry<String, Double>> sortedposprobs = MapSort.MapDoubleSort(posprobs);
			String curOut = sortedposprobs.get(0).getKey();
			for(int idx = 1; idx < maxPOSCandidates && idx < sortedposprobs.size(); idx++)
			{
				curOut = curOut + sep + sortedposprobs.get(idx).getKey();
			}
			
			out.println(curOut);
		}
		
		out.close();
		in.close();

	}

}
