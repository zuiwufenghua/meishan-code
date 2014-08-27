package mason.utils;

import java.util.HashMap;
import java.util.Map;

public class UniversPostager {
	
	public static String GetEnglishUniverPOSTag(String inPos)
	{
		Map<String, String> tagmap = new HashMap<String, String>();
		tagmap.put("!", ".");
		tagmap.put("#", ".");
		tagmap.put("$", ".");
		tagmap.put("''", ".");
		tagmap.put("(", ".");
		tagmap.put(");", ".");
		tagmap.put(",", ".");
		tagmap.put("-LRB-", ".");
		tagmap.put("-RRB-", ".");
		tagmap.put(".", ".");
		tagmap.put(":", ".");
		tagmap.put("?", ".");
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
		tagmap.put("``", ".");
		if(tagmap.containsKey(inPos))
		{
			return tagmap.get(inPos);
		}
		else
		{
			return inPos;
		}
	}

}
