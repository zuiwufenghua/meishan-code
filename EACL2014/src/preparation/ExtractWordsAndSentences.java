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
import java.util.Set;
import java.util.TreeSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;



public class ExtractWordsAndSentences {
	
	// now only dictionaries.
	public static void parseHTMLStr(String input, Set<String> newWords, List<String> newSentences)
	{
		Set<String> punctions = new TreeSet<String>();
		punctions.add("。");
		punctions.add("？");
		punctions.add("！");
		
		
		
		if(input.length() < 5) return;
		try{
			Document doc = Jsoup.parse(input);
			
			Elements links = doc.getAllElements();	
			
			for (Element link : links) {
				
				Elements urllinks = link.getElementsByClass("para");
				
				for (Element urllink : urllinks)
				{		
					Elements hreflinks = urllink.getElementsByAttribute("href");
					int hrefcount = 0;
					for (Element hreflink : hreflinks)
					{
						String linkHref = hreflink.attr("href").trim();
						
						if(linkHref.startsWith("/view/") || linkHref.startsWith("http://baike.baidu.com/view/")
						|| linkHref.startsWith("baike.baidu.com/view/"))
						{
							
						}
						else{
							continue;
						}
						String linkText = hreflink.text().trim();
						if(linkText.length() > 6 ||linkText.length() < 2 || !PinyinComparator.bAllChineseCharacter(linkText))
						{
							continue;
						}
	
						newWords.add(linkText);
						hrefcount++;
					}
					
					if(hrefcount > 0)
					{
						String theSent = getElementNodeStr(urllink);

						//String subSentence = "";
						//int splitCount = 0;
						//for(int idx = 0; idx < theSent.length(); idx++)
						//{
						//	String curChar = theSent.substring(idx, idx+1);
						//	if(curChar.equals(" "))splitCount++;
						//	subSentence = subSentence + curChar;
						//}
						newSentences.add(theSent);
					}
					
					
					
	
				}
			}
		//System.out.println(pageUrl);
	}
	catch (Exception e)
	{
		System.out.println(e.toString());

	}
		
	}
	
	public static String getElementNodeStr(Node link)
	{
		String theSent = "";
		List<Node> links = link.childNodes();
		
		if(links.size() == 0)
		{
			if(link instanceof TextNode)
			{
				theSent = link.toString();
			}
			
			if(link instanceof Element)
			{
				Element tlink = (Element)link;
				theSent = tlink.text();
			}
			
			
		}
		else
		{
			for(Node theLink : links)
			{
				theSent = theSent + " " + getElementNodeStr(theLink);
			}
		}
		theSent = theSent.replace("\\s+", " ").trim();
		theSent = theSent.replace("\r", " ").trim();
		theSent = theSent.replace("\n", " ").trim();
		
		return theSent;
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception{
		// TODO Auto-generated method stub
		Set<String> keyWords = new TreeSet<String>();
		List<String> sentences = new ArrayList<String>();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF8"));
		String sLine = null;
		//while ((sLine = in.readLine()) != null) {
		//	if(sLine.trim().length() < 1)continue;
		//	keyWords.add(sLine.trim());
		//}
		
		//in.close();
		
		//in = new BufferedReader(new InputStreamReader(
		//		new FileInputStream(args[1]), "UTF8"));
		String inputHtml = "";
		while ((sLine = in.readLine()) != null) {
			if(sLine.trim().length() < 1)continue;
			if(sLine.endsWith("###########"))
			{
				parseHTMLStr(inputHtml.trim(), keyWords, sentences);
				inputHtml = "";
			}
			else
			{
				inputHtml = inputHtml + "\r\n" + sLine;
			}
		}
		
		in.close();
		
		
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]), "UTF-8"), false);
		
		for(String theWord : keyWords)
		{
			out.println(String.format("[%s] : %d", theWord, 2));
		}
		
		out.close();
		
		out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(args[2]), "UTF-8"), false);
		
		for(String theSentence : sentences)
		{
			out.println(theSentence);
		}
			
		out.close();
		

	}

}
