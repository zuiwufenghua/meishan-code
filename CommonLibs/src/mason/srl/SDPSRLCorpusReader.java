package mason.srl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;

public class SDPSRLCorpusReader {
	public List<DepInstanceSRL> m_vecInstances;
	public BufferedReader m_fileReader;
	public DepInstanceSRL m_nextInstance; 

	public SDPSRLCorpusReader() {
		m_vecInstances = new ArrayList<DepInstanceSRL>();	
	}

	public void Init(String inputFile) throws Exception {
		if (inputFile == null)
			return;
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(inputFile), "UTF8"));
		String sLine = null;
		List<String> oneSentence = new ArrayList<String>();

		while ((sLine = in.readLine()) != null) {
			if (sLine.trim().equals("")) {
				if (oneSentence.isEmpty()) {
					continue;
				}
				
				DepInstanceSRL tmpInstance = new DepInstanceSRL();
				
				int length = oneSentence.size();
				
				boolean bCorrectParsed = true;

				
				for (int i = 0; i < oneSentence.size(); i++) {
					String[] unit_labels = oneSentence.get(i).trim()
							.split("\t");
					tmpInstance.predicatessize = unit_labels.length - 14;

					if ( unit_labels.length < 11 )
					{
						bCorrectParsed = false;
						System.out.println("Error read inputfile!");
						break;
					}
					tmpInstance.forms.add(unit_labels[1]);
					tmpInstance.lemmas.add(unit_labels[2]);
					tmpInstance.lemmas_app.add(unit_labels[3]);

					String cpostag = unit_labels[4];
					String postag = unit_labels[5];
					
					if (cpostag.equals("_") && !postag.equals("_"))
					{
						cpostag = postag;
					}					
					else if (postag.equals("_") && !cpostag.equals("_"))
					{

						postag = cpostag;
					}
					else if (!postag.equals("_") && !cpostag.equals("_"))
					{

					}
															
					tmpInstance.cpostags.add(cpostag);
					tmpInstance.postags.add(postag);

					if(unit_labels[8].equals("_"))
					{
						unit_labels[8] = "-1";
					}
					
					tmpInstance.heads.add(Integer.parseInt(unit_labels[8]));
					tmpInstance.deprels.add(unit_labels[10]);
					
					tmpInstance.predicates.add(unit_labels[12]);
					tmpInstance.predicates_type.add(unit_labels[13]);
					
					if(i == 0)
					{
						for(int j = 14; j < unit_labels.length; j++)
						{
							tmpInstance.predicateslabels.add(new ArrayList<String>());
						}
					}
					
					for(int j = 14; j < unit_labels.length; j++)
					{
						tmpInstance.predicateslabels.get(j - 14).add(unit_labels[j]);
					}					

				}

				if (bCorrectParsed) {
					m_vecInstances.add(tmpInstance);
				} else {
					System.out.println();
				}
				oneSentence.clear();
			} else {
				oneSentence.add(sLine);
			}

		}
		in.close();
	}

	/*
	
	public void InitNextInstance(String inputFile) throws Exception {
		if (inputFile == null)
			return;
		m_fileReader = new BufferedReader(new InputStreamReader(
				new FileInputStream(inputFile), "UTF8"));
		m_nextInstance = null;
		String sLine = null;
		List<String> oneSentence = new ArrayList<String>();

		while ((sLine = m_fileReader.readLine()) != null) {
			if (sLine.trim().equals("")) {
				if (oneSentence.isEmpty()) {
					continue;
				}
				m_nextInstance = new DepInstanceSRL();
				int startId = 0;
				int length = oneSentence.size();
				String[] head_titles = oneSentence.get(0).trim().split("\t");
				int K = 0;
				if (head_titles[0].equals("kprob")) {
					K = Integer.parseInt(head_titles[1]);
					length--;
					startId++;
					m_nextInstance.k_scores = new double[K];
					m_nextInstance.k_heads = new int[K][length];
					m_nextInstance.k_deprels = new String[K][length];
					for (int k = 0; k < K; k++) {
						m_nextInstance.k_scores[k] = Double
								.parseDouble(head_titles[2 + k]);
					}
				}
				
				int columSize = 14 + K * 2;
				boolean bCorrectParsed = true;
				
				for (int i = startId; i < oneSentence.size(); i++) {
					String[] unit_labels = oneSentence.get(i).trim()
							.split("\t");
					if(m_nextInstance.maxColumn < unit_labels.length)
					{
						m_nextInstance.maxColumn = unit_labels.length;
					}
					if ( (K > 0 && unit_labels.length != columSize)
							|| columSize < 10 )
					{
						bCorrectParsed = false;
						System.out.println("Error read inputfile!");
						break;
					}
					m_nextInstance.forms.add(unit_labels[1]);
					m_nextInstance.loadParams.add(1);
					m_nextInstance.lemmas.add(unit_labels[2]);
					m_nextInstance.loadParams.add(2);
					String cpostag = unit_labels[3];
					String postag = unit_labels[4];
					
					if (cpostag.equals("_") && !postag.equals("_"))
					{
						m_nextInstance.loadParams.add(4);
						cpostag = postag;
					}					
					else if (postag.equals("_") && !cpostag.equals("_"))
					{
						m_nextInstance.loadParams.add(3);
						postag = cpostag;
					}
					else if (!postag.equals("_") && !cpostag.equals("_"))
					{
						m_nextInstance.loadParams.add(3);
						m_nextInstance.loadParams.add(4);
					}
					
										
					m_nextInstance.cpostags.add(cpostag);
					m_nextInstance.postags.add(postag);
					m_nextInstance.feats1.add(unit_labels[5]);
					m_nextInstance.loadParams.add(5);
					if(unit_labels[6].equals("_"))
					{
						unit_labels[6] = "-1";
					}
					m_nextInstance.heads.add(Integer.parseInt(unit_labels[6]));
					m_nextInstance.loadParams.add(6);
					m_nextInstance.deprels.add(unit_labels[7]);
					m_nextInstance.loadParams.add(7);
					m_nextInstance.feats2.add(unit_labels[8]);
					m_nextInstance.loadParams.add(8);
					m_nextInstance.feats3.add(unit_labels[9]);
					m_nextInstance.loadParams.add(9);
					
					int p1Head = -1;
					if(unit_labels.length > 10 && !unit_labels[10].equals("_"))
					{
						p1Head = Integer.parseInt(unit_labels[10]);
					}
					if(unit_labels.length > 10)
					{
						m_nextInstance.loadParams.add(10);
					}					
					String p1deprel = "_";
					if(unit_labels.length > 11 && !unit_labels[11].equals("_"))
					{
						p1deprel = unit_labels[11];
					}
					if(unit_labels.length > 11)
					{
						m_nextInstance.loadParams.add(11);
					}
					
					int p2Head = -1;
					if(unit_labels.length > 12 && !unit_labels[12].equals("_"))
					{
						p2Head = Integer.parseInt(unit_labels[12]);
					}
					if(unit_labels.length > 12)
					{
						m_nextInstance.loadParams.add(12);
					}
					String p2deprel = "_";
					if(unit_labels.length > 13 && !unit_labels[13].equals("_"))
					{
						p2deprel = unit_labels[13];
					}
					if(unit_labels.length > 13)
					{
						m_nextInstance.loadParams.add(13);
					}
					
					m_nextInstance.p1heads.add(p1Head);
					m_nextInstance.p2heads.add(p2Head);
					m_nextInstance.p1deprels.add(p1deprel);
					m_nextInstance.p2deprels.add(p2deprel);
					
					
					for (int k = 0; k < K; k++) {
						m_nextInstance.k_heads[k][i - startId] = Integer
								.parseInt(unit_labels[14 + 2 * k]);
						m_nextInstance.k_deprels[k][i - startId] = unit_labels[15 + 2 * k];
						m_nextInstance.loadParams.add(14 + 2 * k);
						m_nextInstance.loadParams.add(14 + 2 * k);
					}
				}

				if (bCorrectParsed) {
					String rawSentence = "";
					for (int i = 0; i < m_nextInstance.forms.size(); i++) {
						rawSentence = rawSentence + m_nextInstance.forms.get(i);
					}
					return;
				} else {
					System.out.println();
				}
				oneSentence.clear();
			} else {
				oneSentence.add(sLine);
			}
				
		}
		
	}
	
	public void NextInstance() throws Exception {
				
		String sLine = null;
		List<String> oneSentence = new ArrayList<String>();
		m_nextInstance = null;
		while ((sLine = m_fileReader.readLine()) != null) {
			if (sLine.trim().equals("")) {
				if (oneSentence.isEmpty()) {
					continue;
				}
				m_nextInstance = new DepInstanceSRL();
				int startId = 0;
				int length = oneSentence.size();
				String[] head_titles = oneSentence.get(0).trim().split("\t");
				int K = 0;
				if (head_titles[0].equals("kprob")) {
					K = Integer.parseInt(head_titles[1]);
					length--;
					startId++;
					m_nextInstance.k_scores = new double[K];
					m_nextInstance.k_heads = new int[K][length];
					m_nextInstance.k_deprels = new String[K][length];
					for (int k = 0; k < K; k++) {
						m_nextInstance.k_scores[k] = Double
								.parseDouble(head_titles[2 + k]);
					}
				}
				
				int columSize = 14 + K * 2;
				boolean bCorrectParsed = true;
				
				for (int i = startId; i < oneSentence.size(); i++) {
					String[] unit_labels = oneSentence.get(i).trim()
							.split("\t");
					if(m_nextInstance.maxColumn < unit_labels.length)
					{
						m_nextInstance.maxColumn = unit_labels.length;
					}
					if ( (K > 0 && unit_labels.length != columSize)
							|| columSize < 10 )
					{
						bCorrectParsed = false;
						System.out.println("Error read inputfile!");
						break;
					}
					m_nextInstance.forms.add(unit_labels[1]);
					m_nextInstance.loadParams.add(1);
					m_nextInstance.lemmas.add(unit_labels[2]);
					m_nextInstance.loadParams.add(2);
					String cpostag = unit_labels[3];
					String postag = unit_labels[4];
					
					if (cpostag.equals("_") && !postag.equals("_"))
					{
						m_nextInstance.loadParams.add(4);
						cpostag = postag;
					}					
					else if (postag.equals("_") && !cpostag.equals("_"))
					{
						m_nextInstance.loadParams.add(3);
						postag = cpostag;
					}
					else if (!postag.equals("_") && !cpostag.equals("_"))
					{
						m_nextInstance.loadParams.add(3);
						m_nextInstance.loadParams.add(4);
					}
					
										
					m_nextInstance.cpostags.add(cpostag);
					m_nextInstance.postags.add(postag);
					m_nextInstance.feats1.add(unit_labels[5]);
					m_nextInstance.loadParams.add(5);
					if(unit_labels[6].equals("_"))
					{
						unit_labels[6] = "-1";
					}
					m_nextInstance.heads.add(Integer.parseInt(unit_labels[6]));
					m_nextInstance.loadParams.add(6);
					m_nextInstance.deprels.add(unit_labels[7]);
					m_nextInstance.loadParams.add(7);
					m_nextInstance.feats2.add(unit_labels[8]);
					m_nextInstance.loadParams.add(8);
					m_nextInstance.feats3.add(unit_labels[9]);
					m_nextInstance.loadParams.add(9);
					
					int p1Head = -1;
					if(unit_labels.length > 10 && !unit_labels[10].equals("_"))
					{
						p1Head = Integer.parseInt(unit_labels[10]);
					}
					if(unit_labels.length > 10)
					{
						m_nextInstance.loadParams.add(10);
					}					
					String p1deprel = "_";
					if(unit_labels.length > 11 && !unit_labels[11].equals("_"))
					{
						p1deprel = unit_labels[11];
					}
					if(unit_labels.length > 11)
					{
						m_nextInstance.loadParams.add(11);
					}
					
					int p2Head = -1;
					if(unit_labels.length > 12 && !unit_labels[12].equals("_"))
					{
						p2Head = Integer.parseInt(unit_labels[12]);
					}
					if(unit_labels.length > 12)
					{
						m_nextInstance.loadParams.add(12);
					}
					String p2deprel = "_";
					if(unit_labels.length > 13 && !unit_labels[13].equals("_"))
					{
						p2deprel = unit_labels[13];
					}
					if(unit_labels.length > 13)
					{
						m_nextInstance.loadParams.add(13);
					}
					
					m_nextInstance.p1heads.add(p1Head);
					m_nextInstance.p2heads.add(p2Head);
					m_nextInstance.p1deprels.add(p1deprel);
					m_nextInstance.p2deprels.add(p2deprel);
					
					
					for (int k = 0; k < K; k++) {
						m_nextInstance.k_heads[k][i - startId] = Integer
								.parseInt(unit_labels[14 + 2 * k]);
						m_nextInstance.k_deprels[k][i - startId] = unit_labels[15 + 2 * k];
						m_nextInstance.loadParams.add(14 + 2 * k);
						m_nextInstance.loadParams.add(14 + 2 * k);
					}
				}

				if (bCorrectParsed) {
					String rawSentence = "";
					for (int i = 0; i < m_nextInstance.forms.size(); i++) {
						rawSentence = rawSentence + m_nextInstance.forms.get(i);
					}
					return;
				} else {
					System.out.println();
					m_nextInstance = null;
				}
				oneSentence.clear();
			} else {
				oneSentence.add(sLine);
			}
				
		}
		
		if(m_nextInstance == null)
		{
			m_fileReader.close();
		}
		
	}
*/
}
