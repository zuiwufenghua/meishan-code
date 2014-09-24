package preparation;

import mason.utils.PinyinComparator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class BaiduBaiKeReading {
	/*
	public static void getWebCon(String urlname, PrintWriter output) {
		output.println("###########" + urlname + "###########");
		try {
			java.net.URL url = new java.net.URL(urlname);
			BufferedReader in = new BufferedReader(new InputStreamReader(url
					.openStream()));
			String line;
			while ((line = in.readLine()) != null) {
				if(line.trim().length() > 3)
				{
					output.println(line.trim());
				}
			}
			in.close();
		} catch (Exception e) { // Report any errors that arise
			System.err.println(e);
			System.err
					.println("Usage:   java   HttpClient   <URL>   [<filename>]");
		}
	}
	*/
	public static int  readerPageByUrl(String pageUrl, Map<String, String> results, Set<String> seedWords, Set<String> currentWebWords, Set<String> subLists, List<String> contents){
		int seedWordNum = 0;	
		try
		{
			System.setProperty("http.proxyHost", "127.0.0.1");
			System.setProperty("http.proxyPort", "8087");
			Document doc = Jsoup.connect(pageUrl).timeout(30000).get();
			//String format = doc.html();
			//System.out.println(format);
			Elements links = null;
			if(pageUrl.endsWith(".htm"))
			{
				links = doc.getAllElements();	
			}
			else
			{
				int subIndex = pageUrl.lastIndexOf("#sub");
				if(subIndex != -1)
				{
					String value = pageUrl.substring(subIndex + 4);
					links = doc.getElementsByAttributeValue("sublemmaid", value);
				}
			}
			
			if(links == null)
			{
				System.out.println("error url:\t" + pageUrl);
				return 0;
			}
			
			String aContent = links.html();
			if(aContent.indexOf("访问出错") != -1)
			{
				System.out.println(pageUrl + " 访问出错!");
				return -1;
			}
			contents.add(aContent);
			for (Element link : links) {
				
				Elements urllinks = link.getElementsByTag("a");
				for (Element urllink : urllinks)
				{
					Elements hreflinks = urllink.getElementsByAttribute("href");
					for (Element hreflink : hreflinks)
					{
						String linkHref = hreflink.attr("href").trim();
						while(linkHref.endsWith("/"))
						{
							linkHref = linkHref.substring(0, linkHref.length()-1);
						}
						if(pageUrl.indexOf("#sub") == -1 && linkHref.startsWith("#sub"))
						{
							subLists.add(pageUrl+linkHref);
						}
						if(pageUrl.indexOf("#sub") == -1 && linkHref.startsWith(pageUrl+"#sub"))
						{
							subLists.add(linkHref);
						}
						
						if(linkHref.startsWith("/view/") || linkHref.startsWith("http://baike.baidu.com/view/")
						|| linkHref.startsWith("baike.baidu.com/view/"))
						{
							
						}
						else{
							continue;
						}
						if(linkHref.startsWith("/"))
						{
							linkHref = "http://baike.baidu.com" + linkHref;
						}
						if(linkHref.startsWith("baike.baidu.com/view/"))
						{
							linkHref = "http://" + linkHref;
						}
						if(!linkHref.endsWith(".htm"))
						{
							int lastSplit = linkHref.lastIndexOf("/");
							int htmIndex = linkHref.indexOf(".htm", lastSplit+1);
							linkHref = linkHref.substring(0, htmIndex+4);							
						}
						String linkText = hreflink.text().trim();
						if(linkText.length() > 6 ||linkText.length() < 2 || !PinyinComparator.bAllChineseCharacter(linkText))
						{
							continue;
						}
	
						results.put(linkHref, linkText);
						
						if(seedWords.contains(linkText) && !currentWebWords.contains(linkText))
						{
							seedWordNum++;
						}
						currentWebWords.add(linkText);
					}

				}
			}
			//System.out.println(pageUrl);
		}
		catch (Exception e)
		{
			System.out.println(e.toString());
			return -1;
		}
		return seedWordNum;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception{
		// TODO Auto-generated method stub
		Map<String, String> urlWithSeeds = new TreeMap<String, String>();
		urlWithSeeds.put("http://baike.baidu.com/view/6915.htm", "张小凡");
		urlWithSeeds.put("http://baike.baidu.com/view/504267.htm",  "陆雪琪");
		urlWithSeeds.put("http://baike.baidu.com/view/1039316.htm",  "青云门");
		urlWithSeeds.put("http://baike.baidu.com/view/1120372.htm",  "林惊羽");
		urlWithSeeds.put("http://baike.baidu.com/view/1348071.htm",  "周一仙");
		urlWithSeeds.put("http://baike.baidu.com/view/86901.htm",  "碧瑶");
		urlWithSeeds.put("http://baike.baidu.com/view/1601695.htm",  "苏茹");
		urlWithSeeds.put("http://baike.baidu.com/view/2183536.htm",  "宋大仁");
		urlWithSeeds.put("http://baike.baidu.com/view/217458.htm",  "李洵");
		urlWithSeeds.put("http://baike.baidu.com/view/984653.htm",  "鬼王宗");
		urlWithSeeds.put("http://baike.baidu.com/view/2749831.htm",  "齐昊");
		urlWithSeeds.put("http://baike.baidu.com/view/42860.htm",  "青云山");
		urlWithSeeds.put("http://baike.baidu.com/view/3446537.htm",  "云易岚");
		urlWithSeeds.put("http://baike.baidu.com/view/2550297.htm",  "大竹峰");   
		
		Set<String> currentSeed = new TreeSet<String>();
		
		currentSeed.add("张小凡");
		currentSeed.add("陆雪琪");
		currentSeed.add("青云门");
		currentSeed.add("林惊羽");
		currentSeed.add("周一仙");
		currentSeed.add("鬼厉");
		currentSeed.add("碧瑶");
		currentSeed.add("苏茹");
		currentSeed.add("宋大仁");
		currentSeed.add("李洵");
		currentSeed.add("鬼王宗");
		currentSeed.add("齐昊");
		currentSeed.add("青云山");
		currentSeed.add("云易岚");
		currentSeed.add("大竹峰"); 
		
		Map<String, Integer> processedURLs = new TreeMap<String, Integer>();
		
		
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[0]), "UTF-8"));
		PrintWriter output_seed = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"));
		boolean nextIterationEnd = false;
		for(int iteration  = 0; iteration < 1000; iteration++)
		{
			int threshold = Math.min(4, currentSeed.size()/15);
			System.out.println(String.format("Iteration %d, threshold %d", iteration,  threshold));
			Map<String, String> newUrlWithSeeds = new TreeMap<String, String>();
			Set<String> newSeeds = new TreeSet<String>();
			for(String url : urlWithSeeds.keySet())
			{
				if(processedURLs.containsKey(url) && processedURLs.get(url)> 5)continue;
				if(!processedURLs.containsKey(url))
				{
					processedURLs.put(url, 0);
				}
				processedURLs.put(url, processedURLs.get(url) + 1);
				Map<String, String> results = new TreeMap<String, String>();
				Set<String> currentWebWords = new TreeSet<String>();
				Set<String> subList = new TreeSet<String>();
				List<String> contents = new ArrayList<String>();
				Random tmp = new Random();
				Thread.sleep(5000 + tmp.nextInt()%5 * 1000);
				int seedNum  = -1;
				while (seedNum == -1)
				{
					seedNum  = readerPageByUrl(url, results, currentSeed, currentWebWords, subList, contents);					
					System.out.println(String.format("%s seed:%d\tURL:%s", urlWithSeeds.get(url), seedNum, url));
					if(seedNum == -1)
					{ 
						//wait 150 seconds
						Thread.sleep(50000 + tmp.nextInt()%50 * 1000);
					}
				}
				if(subList.size() > 0)
				{
					System.out.println("多义词.....,推迟处理!");
					for(String curUrl :subList)
					{
						if(!urlWithSeeds.containsKey(curUrl))
						{
							newUrlWithSeeds.put(curUrl, urlWithSeeds.get(url));
						}
					}
					processedURLs.put(url, 10);
				}
				else if(seedNum > threshold)
				{
					System.out.println("saving word :\t" + urlWithSeeds.get(url) + "\tin\t" + url);
					newSeeds.add(urlWithSeeds.get(url));
					output_seed.println(urlWithSeeds.get(url));
					output_seed.flush();
					output.println("###########" + url + "###########");
					for(String theStr : contents)
					{
						output.println(theStr);
						output.flush();
					}
					for(String curUrl : results.keySet())
					{
						if(!urlWithSeeds.containsKey(curUrl))
						{
							newUrlWithSeeds.put(curUrl, results.get(curUrl));
						}
					}
					processedURLs.put(url, 10);
				}
				else if(seedNum > 0)
				{
					System.out.println("Will be later processed :\t" + urlWithSeeds.get(url) + "\tin\t" + url);
				}
				else if(seedNum == 0)
				{
					processedURLs.put(url, 10);
				}
				else
				{
					System.out.println("error seed return");
				}
				
			}
			
			if(nextIterationEnd)
			{
				break;
			}
			if(newUrlWithSeeds.size() < 1)
			{
				nextIterationEnd = true;
			}
			System.out.println(String.format("%d new urls generated!", newUrlWithSeeds.size()));
			for(String theKey : newUrlWithSeeds.keySet())
			{
				urlWithSeeds.put(theKey, newUrlWithSeeds.get(theKey));
			}
			
			for(String theSeed : newSeeds)
			{
				currentSeed.add(theSeed);
			}
			
		}
		
		output.close();
						
		output_seed.close();
		

		

	}

}
