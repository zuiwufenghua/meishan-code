package mason.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipFileTrans {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException{
		// TODO Auto-generated method stub

		if(args.length > 2 && args[2].equals("zip"))
		{
			txtModelToBin(args[0], args[1]);
		}
		else
		{
			binModelToTxt(args[0], args[1]);
		}
	}
	
	
	public static void binModelToTxt(String sFile, String sOutFile) throws IOException
	{
		InputStream is = new GZIPInputStream(new FileInputStream(sFile));
		BufferedReader sr = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		PrintWriter sw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(sOutFile), "UTF-8"));
		String sLine = null;
		while((sLine = sr.readLine()) != null)
		{
			sw.println(sLine);
			sw.flush();
		}
		
		sr.close();
		sw.close();
		
	}
	
	
	public static void txtModelToBin(String sFile, String sOutFile) throws IOException
	{
		BufferedReader sr = new BufferedReader(new InputStreamReader(new FileInputStream(sFile), "UTF-8"));
		
		OutputStream os = new GZIPOutputStream(new FileOutputStream(sOutFile));
		PrintWriter sw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
		String sLine = null;
		while((sLine = sr.readLine()) != null)
		{
			sw.println(sLine);
			sw.flush();
		}
		
		sr.close();
		sw.close();
		
	}

}
