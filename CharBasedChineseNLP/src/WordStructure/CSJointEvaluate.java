package WordStructure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

//待完成
public class CSJointEvaluate {

	static int tempNum=0,bn=0;
	static int r_wn1;
	static int match=0,match_tag=0,crossing,correct_tag;
	static int start,end;
	static int Status,Error_count,Line;
	static String tempLabel;
	static PrintWriter output;
	
	static int TOTAL_bn1;     /* total number of brackets */
	static int TOTAL_bn2;
	static int TOTAL_match;
	static int TOTAL_sent;                            /* No. of sentence */
	static int TOTAL_error_sent;                      /* No. of error sentence */
	static int TOTAL_skip_sent;                       /* No. of skip sentence */
	static int TOTAL_comp_sent;                       /* No. of complete match sent */
	static int TOTAL_word;                            /* total number of word */
	static int TOTAL_crossing;                        /* total crossing */
	static int TOTAL_no_crossing;                     /* no crossing sentence */
	static int TOTAL_2L_crossing;                     /* 2 or less crossing sentence */
	static int TOTAL_correct_tag;                     /* total correct tagging */
	
	static int TOT_cut_len = 40;         /* Cut-off length in statistics */

    /* data for sentences with len <= CUT_LEN */
    /* Historically it was 40.                */
	static int TOT40_bn1, TOT40_bn2, TOT40_match;     /* total number of brackets */
	static int TOT40_sent;                            /* No. of sentence */
	static int TOT40_error_sent;                      /* No. of error sentence */
	static int TOT40_skip_sent;                       /* No. of skip sentence */
	static int TOT40_comp_sent;                       /* No. of complete match sent */
	static int TOT40_word;                            /* total number of word */
	static int TOT40_crossing;                        /* total crossing */
	static int TOT40_no_crossing;                     /* no crossing sentence */
	static int TOT40_2L_crossing;                     /* 2 or less crossing sentence */
	static int TOT40_correct_tag;                     /* total correct tagging */
	
	private static String modify(String label) {
		int i;
		char cr;
		int strLen=label.length();

		for (i=0;i<strLen;i++)
		{
			cr=label.charAt(i);
			if(cr=='-'||cr=='='){
				return label.substring(0, i);
			}
		}
		return label;
	}
	private static void init_index(Tree<String> tree){
	
		if(tree.isPreTerminal()){
			tree.smaller=tempNum;
			tempNum++;
			tree.bigger=tempNum;
			return;
			
			}
		bn++;
		for (Tree<String> child : tree.getChildren()) {
			init_index(child);
		}
	
		
	}
	private static void all_index(Tree<String> tree){
		if(tree.isPreTerminal()){
			
			return;
		}
		for (Tree<String> child : tree.getChildren()) {
				all_index(child);
		}
		
		tree.smaller=tree.getChild(0).smaller;
		for (Tree<String> child : tree.getChildren()) {
			tree.bigger=child.bigger;
		}
	}
	static void toBrackets(Tree<String> tree,List<Tree<String>> yieldX){
		if(tree.isPreTerminal()){
			return;
		}
		yieldX.add(tree);
		for (Tree<String> child : tree.getChildren()) {
			toBrackets(child,yieldX);
		}
	}
	static void print_head()
	{
	    output.format("  Sent.                        Matched  Bracket   Cross        Correct Tag\r\n");
	    output.format(" ID  Len.  Stat. Recal  Prec.  Bracket gold test Bracket Words  Tags Accracy\r\n");
	    output.format("============================================================================\r\n");
	    output.flush();
	}
	
	static void individual_result(int wn1,int bn1,int bn2,int match,int crossing,int correct_tag){
		  	TOTAL_sent++;
		    if(Status==1){
		    	TOTAL_error_sent++;
		    }else if(Status==2){
		    	TOTAL_skip_sent++;
		    }else{
		    	TOTAL_bn1 += bn1;
		    	TOTAL_bn2 += bn2;
		    	TOTAL_match += match;
			if(bn1==bn2 && bn2==match){
			    TOTAL_comp_sent++;
			}
			TOTAL_word += wn1;
			TOTAL_crossing += crossing;
			if(crossing==0){
			    TOTAL_no_crossing++;
			}
			if(crossing <= 2){
			    TOTAL_2L_crossing++;
			}
			TOTAL_correct_tag += correct_tag;
		    }
		    
		    
		    /* Statistics for sent length <= TOT_cut_len */
		    /*-------------------------------------------*/
		    if(r_wn1<=TOT_cut_len){
		    	TOT40_sent++;
		    	if(Status==1){
		    	    TOT40_error_sent++;
		    	}else if(Status==2){
		    	    TOT40_skip_sent++;
		    	}else{
		    	    TOT40_bn1 += bn1;
		    	    TOT40_bn2 += bn2;
		    	    TOT40_match += match;
		    	    if(bn1==bn2 && bn2==match){
		    		TOT40_comp_sent++;
		    	    }
		    	    TOT40_word += wn1;
		    	    TOT40_crossing += crossing;
		    	    if(crossing==0){
		    		TOT40_no_crossing++;
		    	    }
		    	    if(crossing <= 2){
		    		TOT40_2L_crossing++;
		    	    }
		    	    TOT40_correct_tag += correct_tag;
		    	}
		        }
		    /* Print individual result */
		    /*-------------------------*/
		    output.format("%4d  %3d    %d  ",Line,r_wn1,Status);
		    output.flush();
		    output.format("%6.2f %6.2f   %3d    %3d  %3d    %3d",
			   (bn1==0?0.0:100.0*match/bn1), 
			   (bn2==0?0.0:100.0*match/bn2),
			   match, bn1, bn2, crossing);
		    output.flush();
		    output.format("   %4d  %4d   %6.2f\r\n",wn1,correct_tag,
			   (wn1==0?0.0:100.0*correct_tag/wn1));
		    output.flush();
	}
	static void print_total(){
		int sentn;
	    double r,p,f;

	    output.format("============================================================================\r\n");

	    if(TOTAL_bn1>0 && TOTAL_bn2>0){
		output.format("                %6.2f %6.2f %6d %5d %5d  %5d",
		       (TOTAL_bn1>0?100.0*TOTAL_match/TOTAL_bn1:0.0),
		       (TOTAL_bn2>0?100.0*TOTAL_match/TOTAL_bn2:0.0),
		       TOTAL_match, 
		       TOTAL_bn1, 
		       TOTAL_bn2,
		       TOTAL_crossing);
	    }

	    output.format("  %5d %5d   %6.2f",
		   TOTAL_word,
		   TOTAL_correct_tag,
		   (TOTAL_word>0?100.0*TOTAL_correct_tag/TOTAL_word:0.0));

	    output.format("\r\n");
	    output.format("=== Summary ===\r\n");

	    sentn = TOTAL_sent - TOTAL_error_sent - TOTAL_skip_sent;

	    output.format("\r\n-- All --\r\n");
	    output.format("Number of sentence        = %6d\r\n",TOTAL_sent);
	    output.format("Number of Error sentence  = %6d\r\n",TOTAL_error_sent);
	    output.format("Number of Skip  sentence  = %6d\r\n",TOTAL_skip_sent);
	    output.format("Number of Valid sentence  = %6d\r\n",sentn);
	    
	    r = TOTAL_bn1>0 ? 100.0*TOTAL_match/TOTAL_bn1 : 0.0;
	    output.format("Bracketing Recall         = %6.2f\r\n",r);

	    p = TOTAL_bn2>0 ? 100.0*TOTAL_match/TOTAL_bn2 : 0.0;
	    output.format("Bracketing Precision      = %6.2f\r\n",p);

	    f = 2*p*r/(p+r);
	    output.format("Bracketing FMeasure       = %6.2f\r\n",f);
				    
	    output.format("Complete match            = %6.2f\r\n",
		   (sentn>0?100.0*TOTAL_comp_sent/sentn:0.0));
	    output.format("Average crossing          = %6.2f\r\n",
		   (sentn>0?1.0*TOTAL_crossing/sentn:0.0));
	    output.format("No crossing               = %6.2f\r\n",
		   (sentn>0?100.0*TOTAL_no_crossing/sentn:0.0));
	    output.format("2 or less crossing        = %6.2f\r\n",
		   (sentn>0?100.0*TOTAL_2L_crossing/sentn:0.0));
	    output.format("Tagging accuracy          = %6.2f\r\n",
		   (TOTAL_word>0?100.0*TOTAL_correct_tag/TOTAL_word:0.0));
	    output.flush();
	    
	    
	    sentn = TOT40_sent - TOT40_error_sent - TOT40_skip_sent;

	    output.format("\r\n-- len<=%d --\r\n",TOT_cut_len);
	    output.format("Number of sentence        = %6d\r\n",TOT40_sent);
	    output.format("Number of Error sentence  = %6d\r\n",TOT40_error_sent);
	    output.format("Number of Skip  sentence  = %6d\r\n",TOT40_skip_sent);
	    output.format("Number of Valid sentence  = %6d\r\n",sentn);


	    r = TOT40_bn1>0 ? 100.0*TOT40_match/TOT40_bn1 : 0.0;
	    output.format("Bracketing Recall         = %6.2f\r\n",r);

	    p = TOT40_bn2>0 ? 100.0*TOT40_match/TOT40_bn2 : 0.0;
	    output.format("Bracketing Precision      = %6.2f\r\n",p);

	    f = 2*p*r/(p+r);
	    output.format("Bracketing FMeasure       = %6.2f\r\n",f);

	    output.format("Complete match            = %6.2f\r\n",
		   (sentn>0?100.0*TOT40_comp_sent/sentn:0.0));
	    output.format("Average crossing          = %6.2f\r\n",
		   (sentn>0?1.0*TOT40_crossing/sentn:0.0));
	    output.format("No crossing               = %6.2f\r\n",
		   (sentn>0?100.0*TOT40_no_crossing/sentn:0.0));
	    output.format("2 or less crossing        = %6.2f\r\n",
		   (sentn>0?100.0*TOT40_2L_crossing/sentn:0.0));
	    output.format("Tagging accuracy          = %6.2f\r\n",
		   (TOT40_word>0?100.0*TOT40_correct_tag/TOT40_word:0.0));
	    output.flush();
	}
	/**
	 * @param args

	 */
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		
		try{
			
				File file = new File(args[0]);
				File file2 = new File(args[1]);
				File file3 = new File(args[2]);
				boolean bSke = false;
				if(args.length > 3 && args[3].equals("-ske"))
				{
					bSke = true;
				}
		

				BufferedReader bf1=new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
				BufferedReader bf2=new BufferedReader(new InputStreamReader(new FileInputStream(file2),"UTF-8"));
				output = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file3), "UTF-8"),false);
				
				String parse=null;
				TreeMap<String, Integer> treemap=new TreeMap<String, Integer>();
				int wn1,wn2,bn1,bn2;
				int i,j;
				
				print_head();
				
				outer:
				while((parse=bf1.readLine())!=null)
				{	
					if(parse.trim().equals("(())"))continue;
					int startIndex = parse.indexOf("("); //ȥ���������Ϣ
					parse = parse.substring(startIndex);
					parse = parse.trim();
					PennTreeReader reader = new PennTreeReader(new StringReader(parse));
					Tree<String> tree = reader.next();
					Tree<String> tree1 = tree.getChildren().get(0);
					if(!tree1.getLabel().equalsIgnoreCase("root") 
						&& !tree1.getLabel().equalsIgnoreCase("top")
							)
					{
						tree1 = tree;
					}
					
				
					parse=bf2.readLine();
					if(parse.trim().equals("(())"))continue;
					startIndex=parse.indexOf("("); //ȥ���������Ϣ
					parse = parse.substring(startIndex);
					parse = parse.trim();
					Status=0;//��������					
					if(parse.equals("(())")){
							Status = 2;
							individual_result(0,0,0,0,0,0);
							continue;
					    }					

					reader = new PennTreeReader(new StringReader(parse));
					tree = reader.next();
					Tree<String> tree2 = tree.getChildren().get(0);
					if(!tree2.getLabel().equalsIgnoreCase("root") 
						&& !tree2.getLabel().equalsIgnoreCase("top")
							)
					{
						tree2 = tree;
					}
					
					if(bSke)
					{
						skeProcess(tree1);
						skeProcess(tree2);
					}
					
					Line++;
					
					tempNum=0;
					bn=0;
					tree1.removeEmptyNodes();
					tree2.removeEmptyNodes();
					
					wn1=tree1.getTerminalYield().size();
					r_wn1=wn1;
					wn2=tree2.getTerminalYield().size();
					
					init_index(tree1);
					all_index(tree1);
					bn1=bn;
					
					tempNum=0;
					bn=0;
					init_index(tree2);
					all_index(tree2);
					bn2=bn;
				
					//wn1=tree1.bigger;
					//wn2=tree2.bigger;
					
					//Status=0;
					//if(wn2==0){
					//		Status = 2;
					//		individual_result(0,0,0,0,0,0);
					//		continue;
					 //   }
				
					if(wn1 != wn2){
				//		Error("Length unmatch (%d|%d)\n",wn1,wn2);
						Status=1;
						individual_result(0,0,0,0,0,0);
						continue;
					}
				
					List<String> watches1 = tree1.getTerminalYield();
					List<String> watches2 = tree2.getTerminalYield();
					for(i=0;i<wn1;i++){
						char ab=watches2.get(i).charAt(0);
						if(Line==1&&i==0)
						{
							if(watches1.get(i).equals(watches2.get(i).substring(1)))
							{
								continue;
							}
						}
						if(!watches1.get(i).equals(watches2.get(i))){
				//			Error("Words unmatch (%s|%s)\n",watches1.get(i),watches2.get(i));
							Status=1;
							individual_result(0,0,0,0,0,0);
							continue outer;
						}

					}
					
					List<Tree<String>> yieldX1=new ArrayList<Tree<String>>();
					List<Tree<String>> yieldX2=new ArrayList<Tree<String>>();
					toBrackets(tree1,yieldX1);
					toBrackets(tree2,yieldX2);
					
					match=0;
				//	matchBrackets(tree1,tree2);
					for(i=0;i<bn1;i++){
						for(j=0;j<bn2;j++){
							if(yieldX2.get(j).root==-1 && yieldX1.get(i).smaller==yieldX2.get(j).smaller && yieldX1.get(i).bigger==yieldX2.get(j).bigger){
								if(modify(yieldX1.get(i).getLabel()).equals(modify(yieldX2.get(j).getLabel()))){
									yieldX2.get(j).root=0;
									match++;
									break;
								}
							}
						}
					}
				
					crossing=0;
				//	countCrossings(tree1,tree2);
					for(j=0;j<bn2;j++){
						for(i=0;i<bn1;i++){
							if((yieldX1.get(i).smaller<yieldX2.get(j).smaller && yieldX1.get(i).bigger>yieldX2.get(j).smaller && yieldX1.get(i).bigger<yieldX2.get(j).bigger)||(yieldX1.get(i).smaller>yieldX2.get(j).smaller && yieldX1.get(i).smaller<yieldX2.get(j).bigger && yieldX1.get(i).bigger>yieldX2.get(j).bigger)){
								crossing++;
								break;
							}
						}
					}
					correct_tag=0;
					watches1=tree1.getPreTerminalYield();
					watches2=tree2.getPreTerminalYield();
					for(i=0;i<wn1;i++){
						if(watches1.get(i).equals(watches2.get(i))){
							correct_tag++;
						}
					}
				
					individual_result(wn1,bn1,bn2,match,crossing,correct_tag);
				}
				bf1.close();
				bf2.close();
				
				print_total();
				
		
				output.close();
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		
	}
	
	public static void skeProcess(Tree<String> root)
	{
		if(root.isLeaf()) return;
		
		for(Tree<String> curChild : root.getChildren())
		{
			skeProcess(curChild);
		}
		
		//String[] miniLabels = root.getLabel().split("-");
		root.setLabel("##");
	}

}
