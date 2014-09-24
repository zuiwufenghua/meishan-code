package WordStructure;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class ReadWebData {

	//第一层 ： http://www.chazidian.com/ci_b_32/
	public void  readerPageByUrl(String pageUrl, String key, Map<String, String> results){
		try
		{
			Document doc = Jsoup.connect(pageUrl).timeout(30000).get(); 
			Elements links = doc.getElementsByClass(key);			
			for (Element link : links) {
				Elements urllinks = link.getElementsByTag("a");
				for (Element urllink : urllinks)
				{
					String linkHref = urllink.attr("href").trim();
					String linkText = urllink.text().trim();
					if(results.containsKey(linkText))
					{
						System.out.println(linkText + "\t" + linkHref);						
					}
					//else
					//{
					if(linkText.equals("a") && linkHref.equals("http://www.chazidian.com/ci_a_28/"))
					{
						results.put("a", "http://www.chazidian.com/ci_a_27/");
					}
					else
					{
						results.put(linkText, linkHref);
					}
					//}
					//results.add(linkHref);
				}
			}
			System.out.println(pageUrl);
		}
		catch (Exception e)
		{
			System.out.println(e.toString());
		}
		
		
	}
	
	public String  getWordStructure(String pageUrl){
		try
		{
			Document doc = Jsoup.connect(pageUrl).timeout(30000).get(); 
			Elements links = doc.getElementsByClass("text");			
			for (Element link : links) {
				String curContent = link.toString();
				curContent = curContent.replace("\\s+", "");
				int index = curContent.indexOf("<b>[构成]</b>");
				if(index != -1)
				{
					int startIndex = curContent.indexOf(">", index+ "<b>[构成]</b>".length());
					int endIndex = curContent.indexOf("<", startIndex+1);
					return curContent.substring(startIndex+1, endIndex);
				}

			}
		}
		catch (Exception e)
		{
			System.out.println(e.toString());
			return "null";
		}
		
		return "";
	}

	/**
	 * @param args
	 */
	public static void main1(String[] args) throws Exception{
		// TODO Auto-generated method stub

		ReadWebData rd = new ReadWebData();
			
		Map<String, String> pinyinURL = new HashMap<String, String>();
		rd.readerPageByUrl("http://www.chazidian.com/ci_pinyin/#a", "pyjs_c", pinyinURL);
		
		//PrintWriter outer_prime = new PrintWriter(new OutputStreamWriter(
		//		new FileOutputStream(args[0]), "UTF-8"), false);
		
		PrintWriter outer_word = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[0]+".wordurl"), "UTF-8"), false);

		for(String url : pinyinURL.values())
		{
			{
				Map<String, String> wordURL = new HashMap<String, String>();
				rd.readerPageByUrl(url, "common6", wordURL);
				for(String curWord : wordURL.keySet())
				{
					outer_word.println(curWord.length() + "\t" + curWord + "\t" + wordURL.get(curWord));
					outer_word.flush();
					/*
					if(curWord.length() == 2)
					{
						String temp = rd.getWordStructure(wordURL.get(curWord));
						if(!temp.equals(""))
						{
							outer_prime.println(curWord + "\t" + temp);
							outer_prime.flush();
						}
					}*/
				}
			}
			
			Map<String, String> pagesURL = new HashMap<String, String>();
			String parentURL = url;
			if(!parentURL.endsWith("/"))
			{
				parentURL = parentURL + "/";
			}
			rd.readerPageByUrl(url, "a1", pagesURL);
			int maxPageNum = 1; 
			for(String mark : pagesURL.keySet())
			{
				int currentNum = 0;
				try
				{
					currentNum = Integer.parseInt(mark);
				}
				catch (Exception e)
				{
				}
				if(currentNum > maxPageNum)
				{
					maxPageNum = currentNum;
				}
			}
			for(int currentNum = 2; currentNum <= maxPageNum; currentNum++)
			{
				Map<String, String> wordURL = new HashMap<String, String>();
				String subURL = String.format("?page=%d/", currentNum);
				String totalURL = parentURL.trim() + subURL;
				rd.readerPageByUrl(totalURL, "common6", wordURL);
				for(String curWord : wordURL.keySet())
				{
					outer_word.println(curWord.length() + "\t" + curWord + "\t" + wordURL.get(curWord));
					outer_word.flush();
					/*
					if(curWord.length() == 2)
					{
						String temp = rd.getWordStructure(wordURL.get(curWord));
						if(!temp.equals(""))
						{
							outer_prime.println(curWord + "\t" + temp);
							outer_prime.flush();
						}
					}*/
				}
			}
			/*
			for(String curWord : wordURL.keySet())
			{
				outer_word.println(curWord.length() + "\t" + curWord + "\t" + wordURL.get(curWord));
				outer_word.flush();
				
				if(curWord.length() == 2)
				{
					String temp = rd.getWordStructure(wordURL.get(curWord));
					if(!temp.equals(""))
					{
						outer_prime.println(curWord + "\t" + temp);
						outer_prime.flush();
					}
				}
			}*/
			
		}
		
		//outer_prime.close();
		outer_word.close();
		
	}
	
	public static void main2(String[] args) throws Exception{
		// TODO Auto-generated method stub

		ReadWebData rd = new ReadWebData();
			
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));	
		PrintWriter outer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		String sLine = null;
		
		int nCount = 0;
		int iStart = Integer.parseInt(args[2]) * 10000;
		int iEnd = iStart + 10000;
		while (nCount < iEnd && (sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] words = sLine.trim().split("\\s+");
			if(words.length != 3)continue;
			if(nCount < iStart) 
			{
				nCount++;
				continue;
			}
			nCount++;
			String temp = rd.getWordStructure(words[2]);
			if(temp.equals("null"))
			{
				outer.println(words[1] + "\t" + "error");
			}
			else if(temp.equals(""))
			{
				outer.println(words[1] + "\t" + "nostructure");
			}
			else
			{
				outer.println(words[1] + "\t" + temp);
			}
			outer.flush();
			if(nCount % 50 == 0) System.out.print(nCount + " ");
			if(nCount % 1000 == 0) System.out.println();
		}
		outer.close();
		
	}
	
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub

		ReadWebData rd = new ReadWebData();
		Map<String, String> wordURL = new HashMap<String, String>();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		String sLine = null;
		while ( (sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] words = sLine.trim().split("\\s+");
			if(words.length != 3)continue;
			wordURL.put(words[1], words[2]);
		}
		
		in.close();
		
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[1]), "UTF8"));	
		PrintWriter outer = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);
		
		
		int nCount = 0;
		while ( (sLine = in.readLine()) != null) {
			if(sLine.trim().equals(""))continue;
			String[] words = sLine.trim().split("\\s+");
			if(words.length == 2 && words[1].equals("nostructure"))continue;
			if(words.length == 2 && words[1].equals("error"))
			{
				nCount++;
				if(!wordURL.containsKey(words[0]))
				{
					System.out.println(words[0] + " no url availabel!");
					continue;
				}
				String temp = rd.getWordStructure(wordURL.get(words[0]));
				if(temp.equals("null"))
				{
					outer.println(words[0] + "\t" + "error");
				}
				else if(temp.equals(""))
				{
					//outer.println(words[1] + "\t" + "nostructure");
				}
				else
				{
					outer.println(words[0] + "\t" + temp);
				}
				outer.flush();
				if(nCount % 50 == 0) System.out.print(nCount + " ");
				if(nCount % 1000 == 0) System.out.println();
			}
			else if(words.length == 2)
			{
				outer.println(sLine.trim());
			}
			else
			{
				System.out.println(words.length + " " + sLine.trim());
			}
		}
		outer.close();
		
	}

}
