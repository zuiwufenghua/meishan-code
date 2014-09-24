package process;

import java.util.ArrayList;
import java.util.List;

import mason.dep.DepInstance;
import mason.dep.SDPCorpusReader;


public class RuleModifyDP {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		SDPCorpusReader sdpCorpusReader = new SDPCorpusReader();
		sdpCorpusReader.Init(args[0]);
		
		SDPCorpusReader sdpCorpusReader_compare = new SDPCorpusReader();
		sdpCorpusReader_compare.Init(args[1]);
		
		
		
		List<DepInstance> vecInstances = sdpCorpusReader.m_vecInstances;
		List<DepInstance> vecInstances_compare = sdpCorpusReader_compare.m_vecInstances;
		int totalInstances = vecInstances.size();
		if(totalInstances != vecInstances_compare.size())
		{
			System.out.println("Sentence not match");
			return;
		}
		
		for (int senId = 0; senId < totalInstances; senId++) {
			if ((senId + 1) % 500 == 0) {
				System.out.println(String.format(
						"process instance %d", senId + 1));
			}
			
			DepInstance tmpInstance = vecInstances.get(senId);
			
			DepInstance tmpInstance_compare = vecInstances_compare.get(senId);
			
			
			Integer[] heads1 = new Integer[tmpInstance.heads.size()];
			tmpInstance.heads.toArray(heads1);
			
			Integer[] heads2 = new Integer[tmpInstance_compare.heads.size()];
			tmpInstance_compare.heads.toArray(heads2);
			
			String[] postags = new String[tmpInstance.postags.size()];
			tmpInstance.postags.toArray(postags);
			
			
			
			for(int idx = 0; idx < postags.length; idx++)
			{
				//���ֽṹ���ѵ��ָ�Ϊ����
				if(postags[idx].equals("DEC"))
				{
					//Ѱ�ҵĵ�Ҷ�ӽڵ�
					List<Integer> de_deps = new ArrayList<Integer>();
					for(int jdx = 0; jdx < postags.length; jdx++)
					{
						if(heads1[jdx] == idx+1)
						{
							de_deps.add(jdx);
						}
					}
					
					if(de_deps.size() == 0)
					{
						int de_head = heads1[idx]-1;
						int de_head_head = heads1[de_head]-1;
						heads1[idx] = de_head_head+1;
						heads1[de_head]= idx+1;
					}					
				}
				
				//�������ĳ����Ϊ����
				if(idx < postags.length-1 
				&& postags[idx].equals("VV")
				&& postags[idx].equals("VV")){
					if(heads1[idx] == idx+2)
					{
						List<Integer> vvr_deps = new ArrayList<Integer>();
						for(int jdx = 0; jdx < postags.length; jdx++)
						{
							if(jdx == idx)continue;
							if(heads1[jdx] == idx+2)
							{
								vvr_deps.add(jdx);
							}
						}
						
						for(Integer cur_dep : vvr_deps)
						{
							heads1[cur_dep] = idx+1;
						}
						int vvr_head_head = heads1[idx+1]-1;
						heads1[idx] = vvr_head_head+1;
						heads1[idx+1] = idx+1;
					}
				}
					
			}
			
			for(int idx = 0; idx < postags.length; idx++)
			{
				//���ֽṹ���ѵ��ָ�Ϊ����
				if(postags[idx].equals("DEC"))
				{
					//Ѱ�ҵĵ�Ҷ�ӽڵ�
					List<Integer> de_deps = new ArrayList<Integer>();
					for(int jdx = 0; jdx < postags.length; jdx++)
					{
						if(heads2[jdx] == idx+1)
						{
							de_deps.add(jdx);
						}
					}
					
					if(de_deps.size() == 0)
					{
						int de_head = heads2[idx]-1;
						int de_head_head = heads2[de_head]-1;
						heads2[idx] = de_head_head+1;
						heads2[de_head]= idx+1;
					}					
				}
				
				//�������ĳ����Ϊ����
				if(idx < postags.length-1 
				&& postags[idx].equals("VV")
				&& postags[idx].equals("VV")){
					if(heads2[idx] == idx+2)
					{
						List<Integer> vvr_deps = new ArrayList<Integer>();
						for(int jdx = 0; jdx < postags.length; jdx++)
						{
							if(jdx == idx)continue;
							if(heads2[jdx] == idx+2)
							{
								vvr_deps.add(jdx);
							}
						}
						
						for(Integer cur_dep : vvr_deps)
						{
							heads2[cur_dep] = idx+1;
						}
						int vvr_head_head = heads2[idx+1]-1;
						heads2[idx] = vvr_head_head+1;
						heads2[idx+1] = idx+1;
					}
				}
					
			}
			
			boolean bMatch = true;
			
			for(int idx = 0; idx < postags.length; idx++)
			{
				int h1 = heads1[idx], h2 = heads2[idx];
				if(h1 != h2)
				{
					bMatch = false;
				}
			}
			
			if(!bMatch)
			{
				System.out.println(String.format("Sentence %d not match.", senId));
			}			
		}

	}

}
