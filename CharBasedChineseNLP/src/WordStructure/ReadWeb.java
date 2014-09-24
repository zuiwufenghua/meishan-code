package WordStructure;

import mason.utils.PinyinComparator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Map.Entry;

public class ReadWeb {

	/**
	 * 根据网址读取网页HTML内容
	 * 
	 * @param pageUrl
	 *            网页地址
	 */
	public void readerPageByUrl(String pageUrl, Map<String, Set<String>> tempResult, PrintWriter outer_prime) {
		URL url;
		
		try {
			url = new URL(pageUrl);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			InputStream is = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is,  "GBK") );
			StringBuffer sb = new StringBuffer();
			String line = null;
			String theWord = "";
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if(line.startsWith("<title>") && line.endsWith("</title>"))
				{
					int startIndex = "<title>".length();
					int endIndex = line.indexOf("---");
					if(endIndex != -1 && endIndex == startIndex + 5 && line.substring(startIndex+1, endIndex).equals("字的解释"))
					{
						theWord = line.substring(startIndex, startIndex+1);
						if(tempResult.containsKey(theWord))
						{
							System.out.println("Duplicate key : " + theWord);
							return;
						}
						tempResult.put(theWord, new HashSet<String>());
						System.out.println(pageUrl);
						outer_prime.print(theWord);
					}
					else
					{
						return ;
					}
					
				}
				
				if(!theWord.equals("") && tempResult.containsKey(theWord))
				{
					int posMark = line.indexOf("【"); 
					while(posMark != -1)
					{
						int endMark = line.indexOf("】", posMark);
						String curMark = line.substring(posMark+1, endMark);
						if(tempResult.get(theWord).add(curMark))
						{
							outer_prime.print(" " + curMark);
						}
						posMark = line.indexOf("【", endMark+1); 
					}
				}				
				
			}
			
			if(!theWord.equals(""))
			{
				outer_prime.println();
			}
		} catch (Exception e) {
			//e.printStackTrace();
		} finally {

		}
		
	}

	public void readerPageByUrlOther(String pageUrl,  PrintWriter outer_prime) {
		URL url;
		try {
			url = new URL(pageUrl);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setConnectTimeout(300000);
			InputStream is = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is,  "GBK") );
			StringBuffer sb = new StringBuffer();
			String line = null;
			String theWord = "";
			Set<String> curWordPos = null;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if(line.startsWith("<title>") && line.endsWith("</title>"))
				{
					int startIndex = "<title>".length();
					int endIndex = line.indexOf("---");
					if(endIndex != -1 && endIndex == startIndex + 5 && line.substring(startIndex+1, endIndex).equals("字的解释"))
					{
						theWord = line.substring(startIndex, startIndex+1);
						//if(tempResult.containsKey(theWord))
						//{
						//	System.out.println("Duplicate key : " + theWord);
						//	return;
						//}
						curWordPos =  new HashSet<String>();
						System.out.println(pageUrl);
						outer_prime.print(theWord);
					}
					else
					{
						return ;
					}
					
				}
				
				if(curWordPos != null)
				{
					int posMark = line.indexOf("【"); 
					while(posMark != -1)
					{
						int endMark = line.indexOf("】", posMark);
						String curMark = line.substring(posMark+1, endMark);
						if(curWordPos.add(curMark))
						{
							outer_prime.print(" " + curMark);
						}
						posMark = line.indexOf("【", endMark+1); 
					}
				}				
				
			}
			
			if(!theWord.equals(""))
			{
				outer_prime.println();
			}
		} catch (Exception e) {
			//e.printStackTrace();
			System.out.println(e.toString());
		} finally {

		}
		
	}

	// 测试
	public static void main(String[] args) throws Exception{
		ReadWeb rb = new ReadWeb();
		
		String filePath = args[0];
		int	idxStart = Integer.parseInt(args[1]);
		int	idxEnd = Integer.parseInt(args[2]);
		
		//Map<String, Set<String>> tempResult = new HashMap<String, Set<String>>();
		PrintWriter outer_prime = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(filePath), "UTF-8"), false);		
		for(int seq = idxStart; seq < idxEnd; seq++)
		{
			String pageUrl = String.format("http://xh.5156edu.com/html3/%d.html", seq);
			try {				
				rb.readerPageByUrlOther(pageUrl, outer_prime);
				outer_prime.flush();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//System.out.println(e.getMessage());
			}
		}
		outer_prime.close();
		
		/*
		List<Entry<String, Set<String>>> chapossortlist = new ArrayList<Entry<String, Set<String>>>(tempResult.entrySet());
		
		Collections.sort(chapossortlist, new Comparator(){   
			public int compare(Object o1, Object o2) {    
				Map.Entry obj1 = (Map.Entry) o1;
				Map.Entry obj2 = (Map.Entry) o2;
				
				return PinyinComparator.Compare((String) obj1.getKey(), (String) obj2.getKey());				
            }   
		});
		
		PrintWriter outer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(filePath + ".sort"), "UTF-8"), false);
		for(Entry<String, Set<String>> curCharPoslist: chapossortlist)
		{
			String outline = curCharPoslist.getKey();
			for (String curPosInfo : curCharPoslist.getValue())
			{
				outline = outline + " " + curPosInfo;
			}
			outer.println(outline.trim());
		}
		
		outer.close();
		*/
	}

}
