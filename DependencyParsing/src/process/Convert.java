package process;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeRenderer;
import edu.berkeley.*;
import edu.berkeley.nlp.*;
import edu.berkeley.nlp.syntax.*;


public class Convert {
	/*����������ÿһ���ڵ���Ϣ����*/
	private  static class bracket {
		String word;//����
		String label;//���Ա�ע
		int head;//���ڵ���
		String pos;
		
	}
	/*�ж��������б��Ϊt�Ľڵ��Ƿ��к�̽��,���к�̽�㷵��true�����򷵻�false*/
	private static boolean haveNext(List<bracket> data,Integer t){
		for(int i = 0; i < data.size(); i++){
			if(data.get(i).head==t){
				return true;
			}
		}
		return false;
	}
	/*������ṹ�洢��������ת����Flat Constituency Tree*/
	private static void toFctree(Tree<String> tree, List<bracket> data){
		List<Tree<String>> children = new ArrayList<Tree<String>>();
		for(int i=0; i<data.size(); i++){
			
			if(i+1==tree.root){
			//	Tree<String> tempTree = new Tree<String>(data.get(i).label+"#H");
				Tree<String> tempTree = new Tree<String>(data.get(i).label);
				List<Tree<String>> tChildren = new ArrayList<Tree<String>>();
				tChildren.add(new Tree(data.get(i).word));
				tempTree.setChildren(tChildren);
				tempTree.root=-2;
				tree.smaller=children.size();
				children.add(tempTree);
				continue;
			}
			if(data.get(i).head==tree.root && !(haveNext(data,i+1))){
				Tree<String> tempTree = new Tree<String>(data.get(i).label);
				List<Tree<String>> tChildren = new ArrayList<Tree<String>>();
				tChildren.add(new Tree(data.get(i).word));
				tempTree.setChildren(tChildren);
				children.add(tempTree);
				continue;
			}
			if(data.get(i).head==tree.root && haveNext(data,i+1)){
				Tree<String> tempTree =new Tree<String>(data.get(i).label);
				tempTree.root=i+1;
				children.add(tempTree);
				continue;
			}
		} 
		
		tree.setChildren(children);
		
		for (Tree<String> child : tree.getChildren()) {
				if(child.root!=-1 && child.root !=-2){
					toFctree(child,data);
				}
		}
		
	}
	/*��Flat Constituency Tree ת���� Binary Constituency Tree*/
	public static void toBctree(Tree<String> tree){
		if (tree.isPreTerminal()) {
			return;
		}
		if(tree.root>0 && tree.getChildren().size()==2){
			if(tree.smaller==0){
				tree.setLabel(tree.getLabel()+"#L");
				tree.bigger=0;
			}
			if(tree.smaller==1){
				tree.setLabel(tree.getLabel()+"#R");
				tree.bigger=1;
			}
		}
		
		if(tree.root>0 && tree.getChildren().size()>2){
			
			if(tree.smaller==0){
				tree.setLabel(tree.getLabel()+"#L");
				tree.bigger=0;
			}
			if(tree.smaller>=1){
				tree.setLabel(tree.getLabel()+"#R");
				tree.bigger=1;
			}
			if(tree.smaller==0){
				Tree<String> tempTree = null;
				tempTree = tree.getChild(tree.smaller);
				int i;
				for(i=1; i<tree.getChildren().size()-1;i++){
					List<Tree<String>> children = new ArrayList<Tree<String>>();
					children.add(tempTree);
					children.add(tree.getChild(i));
					tempTree =new Tree<String>(tree.getChild(tree.smaller).getLabel()+"#L");
					tempTree.setChildren(children);
					tempTree.bigger=0;
				}
				List<Tree<String>> children = new ArrayList<Tree<String>>();
				children.add(tempTree);
				children.add(tree.getChild(i));
				tree.setChildren(children);
			}
			if(tree.smaller>=1){
				Tree<String> tempTree = null;
				tempTree = tree.getChild(tree.smaller);
				int i;
				for(i=tree.smaller+1; i<tree.getChildren().size();i++){
					List<Tree<String>> children = new ArrayList<Tree<String>>();
					children.add(tempTree);
					children.add(tree.getChild(i));
					tempTree =new Tree<String>(tree.getChild(tree.smaller).getLabel()+"#L");
					tempTree.setChildren(children);
					tempTree.bigger=0;
				}
				
				for(i=tree.smaller-1; i>0; i--){
					List<Tree<String>> children = new ArrayList<Tree<String>>();
					children.add(tree.getChild(i));
					children.add(tempTree);
					tempTree = new Tree<String>(tree.getChild(tree.smaller).getLabel()+"#R");
					tempTree.setChildren(children);
					tempTree.bigger=1;
				}
				List<Tree<String>> children = new ArrayList<Tree<String>>();
				children.add(tree.getChild(0));
				children.add(tempTree);
				tree.setChildren(children);
			}
			
		}
		
		for (Tree<String> child : tree.getChildren()) {
				toBctree(child);
			}
	}
	
	public static void main(String[] args){
		try{
			File file = new File("C:/Documents and Settings/Administrator.CHINA-1559A22F7/����/ctb5.1/train.conll06");//ԭʼ�ļ��洢·��
			File file2 = new File("C:/Documents and Settings/Administrator.CHINA-1559A22F7/����/ctb5.1/output.txt");//����ļ��洢·��
	//		File file = new File("C:/Users/RB/Desktop/IR/ctb5.1/1.txt");
	//		File file2 = new File("C:/Users/RB/Desktop/IR/ctb5.1/output2.txt");
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
					
			BufferedReader bf = null;
			bf=new BufferedReader(isr);
			
			PrintWriter output = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file2), "UTF-8"),false);
			String parse=null;
			Tree<String> tree=null;
			
			while((parse=bf.readLine())!=null){
				List<bracket> data=new ArrayList<bracket>();	
				do{
					bracket entry=new bracket();

					/*����word*/
					int startIndex = parse.indexOf('\t');
					parse = parse.substring(startIndex+1);
					StringReader in = new StringReader(parse);
					StringBuilder sb = new StringBuilder();
					int ch=in.read();
					while (ch!='\t') {
						sb.append((char) ch);
						ch = in.read();
						
					}
					entry.word=sb.toString();

					/*����label*/
					startIndex = parse.indexOf('\t');
					parse = parse.substring(startIndex+1);
					startIndex = parse.indexOf('\t');
					parse = parse.substring(startIndex+1);
					in = new StringReader(parse);
					sb= new StringBuilder();
					ch=in.read();
					while (ch!='\t') {
						sb.append((char) ch);
						ch = in.read();
						
					}
					entry.label=sb.toString();

					/*����head*/
					startIndex = parse.indexOf('\t');
					parse = parse.substring(startIndex+1);
					startIndex = parse.indexOf('\t');
					parse = parse.substring(startIndex+1);
					startIndex = parse.indexOf('\t');
					parse = parse.substring(startIndex+1);
					in = new StringReader(parse);
					sb= new StringBuilder();
					ch=in.read();
					while (ch!='\t') {
						sb.append((char) ch);
						ch = in.read();
						
					}
					entry.head=Integer.parseInt( sb.toString());

					/*����pos*/
					startIndex = parse.indexOf('\t');
					parse = parse.substring(startIndex+1);
					in = new StringReader(parse);
					sb= new StringBuilder();
					ch=in.read();
					while (ch!='\t') {
						sb.append((char) ch);
						ch = in.read();
						
					}
					entry.pos= sb.toString();
					data.add(entry);
					
				}while(((parse=bf.readLine()).length())!=0);
				
				/*��ʼ��Tree�ṹ*/
				for(int i=0;i<data.size(); i++){
					if(data.get(i).head==0){
						tree = new Tree<String>(data.get(i).label);
						tree.root=i+1;
						break;
					}
					
				}
				
				toFctree(tree,data);//������ṹ�洢��������ת����Flat Constituency Tree
	//			System.out.println(tree);
	//			System.out.println(PennTreeRenderer.render(tree));
				
				toBctree(tree);//��Flat Constituency Tree ת���� Binary Constituency Tree
	//			System.out.println(tree);
				System.out.println(PennTreeRenderer.render(tree));
				
				output.write(tree+"\r\n");//�����д������ļ�
				
			}
			output.close();
			fis.close();
			isr.close();
		
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}
}
