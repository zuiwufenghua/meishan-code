package mason.utils;

import java.util.Comparator;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;


public class PinyinComparator implements Comparator<Object> {
	public int compare(Object o1, Object o2) {
		char c1 = ((String) o1).charAt(0);
		char c2 = ((String) o2).charAt(0);
		return concatPinyinStringArray(
				PinyinHelper.toHanyuPinyinStringArray(c1)).compareTo(
				concatPinyinStringArray(PinyinHelper
						.toHanyuPinyinStringArray(c2)));
	}

	private String concatPinyinStringArray(String[] pinyinArray) {
		StringBuffer pinyinSbf = new StringBuffer();
		if ((pinyinArray != null) && (pinyinArray.length > 0)) {
			for (int i = 0; i < pinyinArray.length; i++) {
				pinyinSbf.append(pinyinArray[i]);
			}
		}
		return pinyinSbf.toString();
	}
	private String concatPinyinStringArray(String[] pinyinArray, char orginal) {
		StringBuffer pinyinSbf = new StringBuffer();
		if ((pinyinArray != null) && (pinyinArray.length > 0)) {
			for (int i = 0; i < pinyinArray.length; i++) {
				pinyinSbf.append(pinyinArray[i]);
			}
		}
		return pinyinSbf.toString()+orginal;
	}

	public static String concatPinyinStringArrayS(String[] pinyinArray) {
		StringBuffer pinyinSbf = new StringBuffer();
		if ((pinyinArray != null) && (pinyinArray.length > 0)) {
			for (int i = 0; i < pinyinArray.length; i++) {
				pinyinSbf.append(pinyinArray[i]);
			}
		}
		return pinyinSbf.toString();
	}
	
	public static String concatPinyinStringArrayS(String[] pinyinArray,char c) {
		StringBuffer pinyinSbf = new StringBuffer();
		if ((pinyinArray != null) && (pinyinArray.length > 0)) {
			for (int i = 0; i < pinyinArray.length; i++) {
				pinyinSbf.append(pinyinArray[i]);
			}
		}
		return pinyinSbf.toString()+c;
	}

	public static int Compare(String o1, String o2) {
		char c1 = ((String) o1).charAt(0);
		char c2 = ((String) o2).charAt(0);
		return concatPinyinStringArrayS(
				PinyinHelper.toHanyuPinyinStringArray(c1)).compareTo(
				concatPinyinStringArrayS(PinyinHelper
						.toHanyuPinyinStringArray(c2)));
	}
	
	public static int CompareModify(String o1, String o2) {
		char[] o1chars = o1.toCharArray();
		char[] o2chars = o2.toCharArray();
		int idx = 0;
		for(; idx < o1chars.length && idx < o2chars.length; idx++)
		{
			if(o1chars[idx] != o2chars[idx])
			{
				break;
			}
		}
		
		if(idx == o1chars.length && idx < o2chars.length)
		{
			return -1;
		}
		else if(idx < o1chars.length && idx == o2chars.length)
		{
			return 1;
		}
		else if(idx == o1chars.length && idx == o2chars.length)
		{
			return 0;
		}
		else
		{
			char c1 = ((String) o1).charAt(idx);
			char c2 = ((String) o2).charAt(idx);
			if(bChineseCharacter(c1) && !bChineseCharacter(c2))
			{
				return 1;
			}
			else if(bChineseCharacter(c2) && !bChineseCharacter(c1))
			{
				return -1;
			}
			else if(!bChineseCharacter(c1) && !bChineseCharacter(c2))
			{
				if(c1 < c2) return -1;
				return 1;
			}
			else
			{
				return concatPinyinStringArrayS(
						PinyinHelper.toHanyuPinyinStringArray(c1), c1).compareTo(
								concatPinyinStringArrayS(PinyinHelper
								.toHanyuPinyinStringArray(c2), c2));
			}
		}
	}

	public static boolean bChineseCharacter(char c)
	{
		String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(c);
		if(pinyin == null)return false;		
		return true;
	}
	
	public static String ToPinyin(String input)
	{
		String output = "";
		char[] theChars = input.toCharArray();
		for(char c : theChars)
		{
			String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(c);
			if(pinyin == null)
			{
				output = output + c;
			}
			else
			{
				output = output + "#" + pinyin[0];
			}
		}
		
		return output;
	}
	
	public static boolean bContainChineseCharacter(String str)
	{
		char[] chars = str.toCharArray();
		for(int i = 0; i < chars.length; i++)
		{
			if(bChineseCharacter(chars[i]))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean bAllChineseCharacter(String str)
	{
		char[] chars = str.toCharArray();
		for(int i = 0; i < chars.length; i++)
		{
			if(!bChineseCharacter(chars[i]))
			{
				return false;
			}
		}
		
		return true;
	}
}