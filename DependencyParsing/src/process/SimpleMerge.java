package process;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class SimpleMerge {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		File file = new File(args[0]);
		String[] subFilenames = file.list();
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"));
		
		for (String subFilename : subFilenames) {
			String inputFile = args[0] + File.separator + subFilename;
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
					inputFile), "UTF-8"));
			
			String sLine = null;
			
			while((sLine = reader.readLine()) != null) {
				writer.println(sLine);
			}
			reader.close();
		}
		
		writer.close();
	}

}
