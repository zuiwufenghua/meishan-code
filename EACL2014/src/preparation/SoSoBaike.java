package preparation;

import mason.utils.PinyinComparator;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SoSoBaike {
	
	public static int  readerPageByUrl(String pageUrl, Map<String, String> results, Set<String> seedWords, Set<String> currentWebWords, Set<String> subLists, List<String> contents){
		int seedWordNum = 0;	
		try
		{
			///System.setProperty("http.proxyHost", "127.0.0.1");
			//System.setProperty("http.proxyPort", "8087");
			Document doc = Jsoup.connect(pageUrl).timeout(30000).get();
			//String format = doc.html();
			//System.out.println(format);
			Elements links = null;

			links = doc.getAllElements();	
			
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
						if(linkHref.startsWith("/v") || linkHref.startsWith("/ShowTitle"))
						{
							linkHref = "http://baike.soso.com" + linkHref;
						}
						if(linkHref.startsWith("baike.soso.com/v") || linkHref.startsWith("baike.soso.com/ShowTitle"))
						{
							linkHref = "http://" + linkHref;
						}
						if(!linkHref.startsWith("http://baike.soso.com/v") && !linkHref.startsWith("http://baike.soso.com/ShowTitle"))
						{
							continue;
						}
						
						if(hreflink.parent().parent().className().trim().equalsIgnoreCase("semanticItemList") 
								|| hreflink.parent().parent().className().trim().equalsIgnoreCase("semanticItemIntro") 
								|| hreflink.parent().className().trim().equalsIgnoreCase("semanticItemIntro")
								|| hreflink.parent().className().trim().equalsIgnoreCase("semanticItemList"))
						{
							subLists.add(linkHref);
							continue;
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
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Map<String, String> urlWithSeeds = new TreeMap<String, String>();
		urlWithSeeds.put("http://baike.soso.com/v118058.htm?sp=SST%E5%BC%A0%E5%B0%8F%E5%87%A1", "张小凡");
		urlWithSeeds.put("http://baike.soso.com/v422087.htm?sp=SST%E9%99%86%E9%9B%AA%E7%90%AA",  "陆雪琪");
		urlWithSeeds.put("http://baike.soso.com/v787104.htm?sp=SST%E9%9D%92%E4%BA%91%E9%97%A8",  "青云门");
		urlWithSeeds.put("http://baike.soso.com/v805982.htm?sp=SST%E6%9E%97%E6%83%8A%E7%BE%BD",  "林惊羽");
		urlWithSeeds.put("http://baike.soso.com/v891731.htm?sp=SST%E5%91%A8%E4%B8%80%E4%BB%99",  "周一仙");
		urlWithSeeds.put("http://baike.soso.com/v422091.htm?sp=SST%E7%A2%A7%E7%91%B6",  "碧瑶");
		urlWithSeeds.put("http://baike.soso.com/v6112047.htm?sp=SST%E8%8B%8F%E8%8C%B9",  "苏茹");
		urlWithSeeds.put("http://baike.soso.com/v51530714.htm?sp=SST%E5%AE%8B%E5%A4%A7%E4%BB%81",  "宋大仁");
		urlWithSeeds.put("http://baike.soso.com/v7813541.htm?sp=SST%E6%9D%8E%E6%B4%B5",  "李洵");
		urlWithSeeds.put("http://baike.soso.com/v793631.htm?sp=SST%E9%AC%BC%E7%8E%8B%E5%AE%97",  "鬼王宗");
		urlWithSeeds.put("http://baike.soso.com/v9962206.htm?sp=SST%E9%BD%90%E6%98%8A",  "齐昊");
		urlWithSeeds.put("http://baike.soso.com/v580769.htm?sp=SST%E9%9D%92%E4%BA%91%E5%B1%B1",  "青云山");
		urlWithSeeds.put("http://baike.soso.com/v52368689.htm?sp=SST%E4%BA%91%E6%98%93%E5%B2%9A",  "云易岚");
		urlWithSeeds.put("http://baike.soso.com/v10868297.htm?sp=SST%E5%A4%A7%E7%AB%B9%E5%B3%B0",  "大竹峰");   
		
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
			int threshold = 2;
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
				int waitTime = 2000 + tmp.nextInt()%2 * 1000;
				System.out.println(String.format("Waiting time %d seconds", waitTime/1000));
				Thread.sleep(waitTime);
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
					System.out.println("多义词.....");
					for(String curUrl :subList)
					{
						if(!urlWithSeeds.containsKey(curUrl))
						{
							newUrlWithSeeds.put(curUrl, urlWithSeeds.get(url));
						}
					}
				}
				
				if(seedNum >= threshold)
				{
					System.out.println("saving word :\t" + urlWithSeeds.get(url) + "\tin\t" + url);
					if( (!newSeeds.contains(urlWithSeeds.get(url)) && !currentSeed.contains(urlWithSeeds.get(url))) || iteration == 0)
					{
						newSeeds.add(urlWithSeeds.get(url));
						output_seed.println(urlWithSeeds.get(url));
						output_seed.flush();
						output.println("########### " + url + " ########### " + urlWithSeeds.get(url) + " ###########");
						for(String theStr : contents)
						{
							output.println(theStr);
							output.flush();
						}
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
