package process;

import mason.dep.*;

import java.io.*;
import java.util.*;

import mason.dep.MergeResult;

public class DepOralResult {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		int length = args.length;
		List<String> inputFiles = new ArrayList<String>();
		for(int i = 0; i < args.length-1; i++)
		{
			inputFiles.add(args[i]);
		}
		MergeResult mergefile = new MergeResult();
		mergefile.Init(inputFiles);
		String outputFile = args[length-1];
		
		mergefile.evaluateOral(outputFile);
		
	}

}
