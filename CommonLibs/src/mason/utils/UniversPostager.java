package mason.utils;

import java.util.HashMap;
import java.util.Map;

public class UniversPostager {
	
	public static String GetEnglishUniverPOSTag(String inPos)
	{
		Map<String, String> tagmap = new HashMap<String, String>();
		tagmap.put("!", "PU");
		tagmap.put("#", "PU");
		tagmap.put("$", "PU");
		tagmap.put("''", "PU");
		tagmap.put("(", "PU");
		tagmap.put(");", "PU");
		tagmap.put(",", "PU");
		tagmap.put("-LRB-", "PU");
		tagmap.put("-RRB-", "PU");
		tagmap.put(".", "PU");
		tagmap.put(":", "PU");
		tagmap.put("?", "PU");
		tagmap.put("CC", "CONJ");
		tagmap.put("CD", "NUM");
		tagmap.put("CD|RB", "X");
		tagmap.put("DT", "DET");
		tagmap.put("EX", "DET");
		tagmap.put("FW", "X");
		tagmap.put("IN", "ADP");
		tagmap.put("IN|RP", "ADP");
		tagmap.put("JJ", "ADJ");
		tagmap.put("JJR", "ADJ");
		tagmap.put("JJRJR", "ADJ");
		tagmap.put("JJS", "ADJ");
		tagmap.put("JJ|RB", "ADJ");
		tagmap.put("JJ|VBG", "ADJ");
		tagmap.put("LS", "X");
		tagmap.put("MD", "VERB");
		tagmap.put("NN", "NOUN");
		tagmap.put("NNP", "NOUN");
		tagmap.put("NNPS", "NOUN");
		tagmap.put("NNS", "NOUN");
		tagmap.put("NN|NNS", "NOUN");
		tagmap.put("NN|SYM", "NOUN");
		tagmap.put("NN|VBG", "NOUN");
		tagmap.put("NP", "NOUN");
		tagmap.put("PDT", "DET");
		tagmap.put("POS", "PRT");
		tagmap.put("PRP", "PRON");
		tagmap.put("PRP$", "PRON");
		tagmap.put("PRP|VBP", "PRON");
		tagmap.put("PRT", "PRT");
		tagmap.put("RB", "ADV");
		tagmap.put("RBR", "ADV");
		tagmap.put("RBS", "ADV");
		tagmap.put("RB|RP", "ADV");
		tagmap.put("RB|VBG", "ADV");
		tagmap.put("RN", "X");
		tagmap.put("RP", "PRT");
		tagmap.put("SYM", "X");
		tagmap.put("TO", "PRT");
		tagmap.put("UH", "X");
		tagmap.put("VB", "VERB");
		tagmap.put("VBD", "VERB");
		tagmap.put("VBD|VBN", "VERB");
		tagmap.put("VBG", "VERB");
		tagmap.put("VBG|NN", "VERB");
		tagmap.put("VBN", "VERB");
		tagmap.put("VBP", "VERB");
		tagmap.put("VBP|TO", "VERB");
		tagmap.put("VBZ", "VERB");
		tagmap.put("VP", "VERB");
		tagmap.put("WDT", "DET");
		tagmap.put("WH", "X");
		tagmap.put("WP", "PRON");
		tagmap.put("WP$", "PRON");
		tagmap.put("WRB", "ADV");
		tagmap.put("``", "PU");
		if(tagmap.containsKey(inPos))
		{
			return tagmap.get(inPos);
		}
		else
		{
			System.out.println("error pos: " + inPos);
			
			return inPos;
		}
	}
	
	
	public static String GetChineseUniverPOSTag(String inPos)
	{
		Map<String, String> tagmap = new HashMap<String, String>();
		tagmap.put("AD", "ADV");
		tagmap.put("AS", "PRT");
		tagmap.put("BA", "X");
		tagmap.put("CC", "CONJ");
		tagmap.put("CD", "NUM");
		tagmap.put("CS", "CONJ");
		tagmap.put("DEC", "PRT");
		tagmap.put("DEG", "PRT");
		tagmap.put("DER", "PRT");
		tagmap.put("DEV", "PRT");
		tagmap.put("DT", "DET");
		tagmap.put("ETC", "PRT");
		tagmap.put("FW", "X");
		tagmap.put("IJ", "X");
		tagmap.put("JJ", "ADJ");
		tagmap.put("LB", "X");
		tagmap.put("LC", "PRT");
		tagmap.put("M", "NUM");
		tagmap.put("MSP", "PRT");
		tagmap.put("NN", "NOUN");
		tagmap.put("NR", "NOUN");
		tagmap.put("NT", "NOUN");
		tagmap.put("OD", "NUM");
		tagmap.put("ON", "X");
		tagmap.put("P", "ADP");
		tagmap.put("PN", "PRON");
		tagmap.put("PU", "PU");
		tagmap.put("SB", "X");
		tagmap.put("SP", "PRT");
		tagmap.put("VA", "VERB");
		tagmap.put("VC", "VERB");
		tagmap.put("VE", "VERB");
		tagmap.put("VV", "VERB");
		tagmap.put("X", "X");
		
		if(tagmap.containsKey(inPos))
		{
			return tagmap.get(inPos);
		}
		else
		{
			System.out.println("error pos: " + inPos);
			return inPos;
		}
	}

}
