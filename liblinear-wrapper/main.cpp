#include "JSTPipe.h"
#include "JSTOptions.h"
#include "JSTParser.h"
#include "MyLib.h"
using namespace std;

int main(int argc, const char *argv[])
{
	string strConfigFileName;
	if (argc < 2) {
		cout << "format: gparser option-config-file" << endl;
		strConfigFileName = "default_config.txt";
//		return -1;
	} else {
		strConfigFileName = argv[1];
	}

	JSTOptions options;
	options.setOptions(strConfigFileName.c_str());
	options.showOptions();


	if (options.m_isTrain) {
		cout << "start training..." << endl;
		print_time();

		JSTPipe *pipe = new JSTPipe(options);
		
		if (NULL == pipe) {
			cout << "new JSTPipe failed" << endl;
			exit(0);
		}

    pipe->readTrainInstances();
		pipe->createAlphabet();
		//pipe->closeAlphabet();

    

//		pipe.m_featAlphabet.show();
//		pipe.m_labelAlphabet.show();
//		exit(1);



		JSTParser dp(options, *pipe);
		dp.saveAlphabetModel(options.m_strModelName.c_str());
//zms
		//dp.featureReductionTrain(instanceLengths);
		//if(options.m_threadnum == 1)
		{
			dp.train();
		}
		//else
		//{
		//	dp.train_threads(instanceLengths);
		//}
		cout << "training over" << endl;
		print_time();

		if (pipe) delete pipe;
	}

	
	if (options.m_isTest) 
	{
		JSTPipe *pipe = new JSTPipe(options);
		
		if (NULL == pipe) {
			cout << "new JSTPipe failed" << endl;
			exit(0);
		}

		JSTParser dp(options, *pipe);

		cout << "Loading model..." << endl;
		print_time();
		if ( 0 != dp.loadAlphabetModel("./", options.m_strModelName.c_str()) ) {
			exit(0);
		}

		if ( 0 != dp.loadParamModel("./", options.m_strModelName.c_str(), options.m_strEval_IterNum_of_ParamModel.c_str()) ) {
			exit(0);
		}
		pipe->closeAlphabet();
		cout << "done." << endl;
		print_time();

		dp.decode(options.m_strTestFile, options.m_strOutFile);
		print_time();
    dp.release_model();
		if (pipe) delete pipe;
    
	}


  if (options.m_bEvaluateOut && options.m_nFold > 0) {
		cout << "start training..." << endl;
		print_time();

		JSTPipe *pipe = new JSTPipe(options);
		
		if (NULL == pipe) {
			cout << "new JSTPipe failed" << endl;
			exit(0);
		}

    //pipe->initLexicon();
    pipe->readTrainInstances();
		pipe->createAlphabet();
		//pipe->closeAlphabet();

    

//		pipe.m_featAlphabet.show();
//		pipe.m_labelAlphabet.show();
//		exit(1);



		JSTParser dp(options, *pipe);
		//dp.saveAlphabetModel(options.m_strModelName.c_str());
//zms
		//dp.featureReductionTrain(instanceLengths);
		//if(options.m_threadnum == 1)
		{
      double c = atof(options.m_strEval_IterNum_of_ParamModel.c_str());
			dp.train_and_evalout(c);
		}
		//else
		//{
		//	dp.train_threads(instanceLengths);
		//}
		cout << "training over" << endl;
		print_time();

		if (pipe) delete pipe;
	}



  if (options.m_bEvaluateOut && options.m_nFold <= 0) 
	{
		JSTPipe *pipe = new JSTPipe(options);
		
		if (NULL == pipe) {
			cout << "new JSTPipe failed" << endl;
			exit(0);
		}

		JSTParser dp(options, *pipe);

		cout << "Loading model..." << endl;
		print_time();
		if ( 0 != dp.loadAlphabetModel("./", options.m_strModelName.c_str()) ) {
			exit(0);
		}

		if ( 0 != dp.loadParamModel("./", options.m_strModelName.c_str(), options.m_strEval_IterNum_of_ParamModel.c_str()) ) {
			exit(0);
		}
		pipe->closeAlphabet();
		cout << "done." << endl;
		print_time();

		dp.evaluate(options.m_strTestFile, options.m_strOutFile);
		print_time();
    dp.release_model();
		if (pipe) delete pipe;
    
	}


	cout << "\n-----\n" << endl;


	return 0;
}
