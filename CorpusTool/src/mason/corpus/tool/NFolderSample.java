package mason.corpus.tool;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class NFolderSample {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		boolean bLine = true;
		if(args.length > 2 && args[2].equals("line")) bLine = false;
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));		
		String sLine = null;
		List<String> vecInstances = new ArrayList<String>();
		String oneSen = "";
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))
			{
				if(!oneSen.trim().equals(""))
				{
					vecInstances.add(oneSen.trim());
				}
				oneSen = "";
			}
			else
			{
				if(bLine)
				{
					vecInstances.add(sLine);
				}
				else
				{
					
					oneSen = oneSen + System.getProperty("line.separator") + sLine;
				}
			}
		}
		
		in.close();
		
		int nFold = Integer.parseInt(args[1]);
		boolean bRandom = false;
		if (nFold < 0) {
			bRandom = true;
			nFold = -nFold;
		}

		if (bRandom) {
			Collections.shuffle(vecInstances, new Random(0));
		}

		int totalInstancesNum = vecInstances.size();
		int intervalNum = (totalInstancesNum + nFold - 1) / nFold;

		for (int curFold = 0; curFold < nFold; curFold++) {
			boolean[] bTrain = new boolean[totalInstancesNum];
			for (int idx = 0; idx < totalInstancesNum; idx++) {
				bTrain[idx] = true;
			}

			for (int idx = curFold * intervalNum; idx < (curFold + 1)
					* intervalNum
					&& idx < totalInstancesNum; idx++) {
				bTrain[idx] = false;
			}

			String outputFile1 = args[0]
					+ String.format(".%d.%d", curFold + 1, nFold - 1);
			String outputFile2 = args[0]
					+ String.format(".%d.%d", curFold + 1, 1);

			PrintWriter writer1 = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile1), "UTF-8"));
			PrintWriter writer2 = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile2), "UTF-8"));
			int count1 = 0, count2 = 0;
			for (int idx = 0; idx < totalInstancesNum; idx++) {
				String curInstance = vecInstances.get(idx);
				if (bTrain[idx]) {	
					count1++;
					writer1.println(curInstance);
					if(!bLine) writer1.println();
				} else {	
					count2++;
					writer2.println(curInstance);
					if(!bLine) writer2.println();
				}
				
			}
			System.out.println(String.format("%s:%d, %s:%d", String.format("%d.%d", curFold + 1, nFold - 1), count1, 
					String.format("%d.%d", curFold + 1, 1), count2 ));
			writer1.close();
			writer2.close();
		}

	}

}
