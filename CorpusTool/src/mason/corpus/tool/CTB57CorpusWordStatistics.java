package mason.corpus.tool;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class CTB57CorpusWordStatistics {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		BufferedReader cfgreader = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "UTF-8"));
		
		
		Set<String> ctb5words = new TreeSet<String>();
		Set<String> ctb7words = new TreeSet<String>();
		PrintWriter outout = null;
		String sLine = null;
		boolean bctb5 = true;
		while ((sLine = cfgreader.readLine()) != null) {
			sLine =sLine.trim();
			int sentenceIdEnd = sLine.indexOf("\t");
			if(sentenceIdEnd == -1)
			{
				continue;				
			}
			String firstPart = sLine.substring(0, sentenceIdEnd);
			int sentenceId = Integer.parseInt(firstPart);
			
			if(sentenceId < 2000)
			{
				bctb5 = true;
			}
			else
			{
				bctb5 = false;
			}

			String secondPart = sLine.substring(sentenceIdEnd +1).trim();
			PennTreeReader reader = new PennTreeReader(new StringReader(
					secondPart));

			
			while (reader.hasNext()) {
				Tree<String> tree = reader.next();
				tree.removeEmptyNodes();
				List<Tree<String>> wordtreelists = tree.getTerminals();
				for(Tree<String> oneTree : wordtreelists)
				{
					String theWord = oneTree.toString();
					if(bctb5)
					{
						ctb5words.add(theWord);
					}
					ctb7words.add(theWord);
				}
			}
		}
		cfgreader.close();
		System.out.println(ctb5words.size());
		System.out.println(ctb7words.size());

	}

}
