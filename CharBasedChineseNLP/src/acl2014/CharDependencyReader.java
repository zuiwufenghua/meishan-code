package acl2014;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CharDependencyReader {

	public List<CharDependency> m_vecInstances;
	public BufferedReader m_fileReader;
	public CharDependency m_nextInstance; 

	public CharDependencyReader() {
		m_vecInstances = new ArrayList<CharDependency>();	
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
				
				CharDependency tmpInstance = new CharDependency();
				
				int length = oneSentence.size();
				
				boolean bCorrectParsed = true;

				
				for (int i = 0; i < oneSentence.size(); i++) {
					String[] unit_labels = oneSentence.get(i).trim()
							.split("\t");

					if ( unit_labels.length != 10 )
					{
						bCorrectParsed = false;
						System.out.println("Error read inputfile!");
						break;
					}
					tmpInstance.forms.add(unit_labels[1]);
					tmpInstance.lemmas.add(unit_labels[2]);

					String cpostag = unit_labels[3];
					String postag = unit_labels[4];
					
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
					
					tmpInstance.feats.add(unit_labels[5]);
					
					
					if(unit_labels[6].equals("_"))
					{
						unit_labels[6] = "-1";
					}
					
					tmpInstance.heads.add(Integer.parseInt(unit_labels[6]));
					tmpInstance.deprels.add(unit_labels[7]);

					if(unit_labels[8].equals("_"))
					{
						unit_labels[8] = "-1";
					}
					
					tmpInstance.p1heads.add(Integer.parseInt(unit_labels[8]));
					tmpInstance.p1deprels.add(unit_labels[9]);					

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




}
