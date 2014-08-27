package mason.dep;

public class MainFrame {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		int mainType = Integer.parseInt(args[0]);
		if(mainType < 10)
		{
			Process mainP = new Process();
			mainP.init(args[1]);
			switch (mainType) {
			case -1: {
				String outputfile = args[1] + "." + args[2];
				int outType = Integer.parseInt(args[2]);
				mainP.Output(outputfile, outType);
				break;
			}
			case 0: {
				String evalResFile0 = args[1] + ".eval0";
				mainP.evaluate(evalResFile0, 0, 0);
				break;
			}
			case 1: {
				String evalResFile1 = args[1] + ".eval1";
				double marginal = Double.parseDouble(args[2]);
				mainP.evaluate(evalResFile1, 1, marginal);
				break;
			}
			case 2: {
				String outputfile = args[1] + ".ans";
				mainP.errorStatistic(outputfile);
				break;
			}
			case 3: {
				String clause = args[2];
				String suffixfilename = clause.replaceAll("#=#", ".");
				suffixfilename = suffixfilename.replaceAll(":", ".");
				String resultAnalysisFile = args[1] + "." + suffixfilename;
				mainP.errorAnalysis(resultAnalysisFile, clause);
				break;
			}
			case 4: {
				String outputfile = args[1] + ".dep.subtree";
				mainP.deptree_erroranalysis(outputfile);
				break;
			}
			case 5: {
				String outputfile = args[1] + ".sib.subtree";
				mainP.sibtree_erroranalysis(outputfile);
				break;
			}
			case 6: {
				String outputfile = args[1] + ".gch.subtree";
				mainP.gchtree_erroranalysis(outputfile);
				break;
			}
			case 7: {
				String outputfile = args[1] + ".wordstat";
				mainP.word_stat(outputfile);
				break;
			}
			
			case 8: {
				String outputfile = args[1] + ".rand";
				int nfold = Integer.parseInt(args[2]);
				mainP.RandSplit(outputfile, nfold);
				break;
			}	
			
			case 9: {
				String outputfile = args[1] + ".errors";
				int option = Integer.parseInt(args[2]);
				mainP.errorLocation(outputfile, option);
				break;
			}
			}
		}
		else
		{
			SubTreeProcess mainP = new SubTreeProcess();
			mainP.init(args[1]);
			
			switch (mainType) {
			case 10: {
				String outputfile = args[1] + ".3tree";
				mainP.subtreeExtract(outputfile);
				break;
			}
			
			case 11: {
				String outputfile = args[1] + ".simp";
				mainP.convert(outputfile);
				break;
			}
			}
			
		}

	}

}
