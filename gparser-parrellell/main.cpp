#include "DepPipe.h"
#include "ParserOptions.h"
#include "DepParser.h"
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

	ParserOptions options;
	options.setOptions(strConfigFileName.c_str());
	options.showOptions();

	if(options.m_bSentenceSimplification)
	{
		cout << "start sentence simplication..." << endl;
		print_time();

		DepPipe *pipe = new DepPipe(options);
		pipe->sentenceSimplification();

		if (pipe) delete pipe;

	}

	if (options.m_isTrain) {
		cout << "start training..." << endl;
		print_time();

		DepPipe *pipe = new DepPipe(options);
		DepDecoder *decoder = new DepDecoder(options, *pipe);
		
		if (NULL == pipe) {
			cout << "new DepPipe failed" << endl;
			exit(0);
		}
		if (NULL == decoder) {
			cout << "new DepDecoder failed" << endl;
			exit(0);
		}
    
    if(options.m_isUseSimp)
    {
      pipe->initSimpRule();
    }
		vector<int> instanceLengths;
		pipe->createAlphabet(instanceLengths);
		pipe->closeAlphabet();

//		pipe.m_featAlphabet.show();
//		pipe.m_labelAlphabet.show();
//		exit(1);



		DepParser dp(options, *pipe, *decoder);
		dp.saveAlphabetModel(options.m_strModelName.c_str());
//zms
		//dp.featureReductionTrain(instanceLengths);
		//if(options.m_threadnum == 1)
		{
			dp.train(instanceLengths);
		}
		//else
		//{
		//	dp.train_threads(instanceLengths);
		//}
		cout << "training over" << endl;
		print_time();
		if (options.m_setTrain_IterNums_to_SaveParamModel.empty()) {
			dp.saveParamModel(options.m_strModelName.c_str(), "");
		}

		if (pipe) delete pipe;
		if (decoder) delete decoder;
	}

	
	if (options.m_isEvaluate) 
	{
		DepPipe *pipe = new DepPipe(options);
		DepDecoder *decoder = new DepDecoder(options, *pipe);
		
		if (NULL == pipe) {
			cout << "new DepPipe failed" << endl;
			exit(0);
		}
		if (NULL == decoder) {
			cout << "new DepDecoder failed" << endl;
			exit(0);
		}

		DepParser dp(options, *pipe, *decoder);

    if(options.m_isUseSimp)
    {
      pipe->initSimpRule();
    }

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

		dp.evaluate();
		print_time();
		if (pipe) delete pipe;
		if (decoder) delete decoder;
	}

	cout << "\n-----\n" << endl;


	return 0;
}
