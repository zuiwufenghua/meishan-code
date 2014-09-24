package process;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.*;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;


public class JointDPSigEvaluate {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();
		sdpCorpusReader.Init(args[0]);

		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		int totalInstances = vecInstances.size();
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"));
		
		output.println("     Sent.          Attachment      Correct        Scoring          ");
		output.println("    ID Tokens  -   Unlab. Lab.   HEAD HEAD+DEPREL   tokens   - - - -");
		output.println("  ============================================================================");
		
		List<Integer> overAllPerformance = new ArrayList<Integer>();
		overAllPerformance.add(0);
		overAllPerformance.add(0);
		overAllPerformance.add(0);
		overAllPerformance.add(0);
		DecimalFormat df=new DecimalFormat("#.00"); 
		for (int i = 0; i < totalInstances; i++) {
			DepInstance tmpInstance = vecInstances.get(i);
			List<Integer> evalMetrics = new ArrayList<Integer>();
			tmpInstance.SigEvaluate(evalMetrics);
			int length = evalMetrics.get(0);
			int scoreLength = evalMetrics.get(1);			
			int  posCorrect = evalMetrics.get(2); 
			int headCorrect = evalMetrics.get(3);
			String curOutput = String.format("     %d    %d    0   %s  %s     %d       %d           %d    0 0 0 0",
					i+1, length,df.format(headCorrect*100.0/scoreLength),df.format(posCorrect*100.0/length),headCorrect,posCorrect,scoreLength);
			output.println(curOutput);
			
			overAllPerformance.set(0, overAllPerformance.get(0)+length);
			overAllPerformance.set(1, overAllPerformance.get(1)+scoreLength);
			overAllPerformance.set(2, overAllPerformance.get(2)+posCorrect);
			overAllPerformance.set(3, overAllPerformance.get(3)+headCorrect);
		}
		output.println();
		output.println();
		output.println();
		
		output.println(String.format("  Unlabeled   attachment score: %d / %d * 100 = %s %%", overAllPerformance.get(3), overAllPerformance.get(1),
				df.format(overAllPerformance.get(3)*100.0/overAllPerformance.get(1))));
		output.println(String.format("  POS tagging accuracy:         %d / %d * 100 = %s %%", overAllPerformance.get(2), overAllPerformance.get(0),
				df.format(overAllPerformance.get(2)*100.0/overAllPerformance.get(0))));
		output.println();
		output.println("  ============================================================================");
		output.close();

	}

}
