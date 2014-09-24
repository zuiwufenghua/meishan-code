package cips2012;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class ExtractSentenceTWAW {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		List<String> allFiles = new ArrayList<String>();
		getAllFilesInFolder(args[0], allFiles);
		int processedFileNum = 0;
		PrintWriter writer = null;
		writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
				args[1]), "UTF-8"), false);
		for (String oneFile : allFiles) {
			processOneFile(oneFile, writer);
			processedFileNum++;
			if (processedFileNum % 2000 == 0) {
				System.out.println(processedFileNum);
			}
		}

		writer.close();

	}

	public static void getAllFilesInFolder(String inputFolder,
			List<String> allFiles) {
		File file = new File(inputFolder);
		if (file.exists()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					allFiles.add(files[i].getAbsolutePath());
				} else if (files[i].isDirectory()) {
					getAllFilesInFolder(files[i].getAbsolutePath(), allFiles);
				}
			}
		}
	}

	public static void processOneFile(String inputFile, PrintWriter writer)
			throws Exception {
		File file = new File(inputFile);
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader bf = null;
		bf = new BufferedReader(isr);

		String sLine = null;
		while ((sLine = bf.readLine()) != null) {
			if (sLine.trim().equals("")) {
				continue;
			}

			String[] words = sLine.trim().split("\\s+");

			String outSentence = "";
			boolean bStart = false;
			for (String curWord : words) {
				if (bStart) {
					outSentence = outSentence + " " + curWord;
				} else if (containHanzi(curWord)) {
					bStart = true;
					outSentence = curWord;
				}
			}
			if(bStart)
			{
				writer.println(outSentence.trim());
				writer.flush();
			}
		}
		bf.close();
	}

	public static boolean containHanzi(String input) {
		boolean bContainH = false;
		char[] inputChars = input.toCharArray();
		for (int index = 0; index < input.length(); index++) {
			byte[] bytes = ("" + inputChars[index]).getBytes();
			if (bytes.length == 2) {
				int[] ints = new int[2];
				ints[0] = bytes[0] & 0xff;
				ints[1] = bytes[1] & 0xff;
				if (ints[0] >= 0x81 && ints[0] <= 0xFE && ints[1] >= 0x40
						&& ints[1] <= 0xFE) {
					bContainH = true;
					break;
				}
			}

		}

		return bContainH;
	}

}
