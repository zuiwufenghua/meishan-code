#include "JSTOptions.h"
#include "MyLib.h"

JSTOptions::JSTOptions()
{
  m_traingAlgorithm = 0;

  //m_threshold = 1e-6;
	m_isTrain = false;
	m_strTrainFile = "";
  m_strEvalFile = "";
	m_numIter = 10;
	m_trainK = 1;
	

  m_isTest = false;
	m_strTestFile = "";
	m_strOutFile = "";
  m_strEval_IterNum_of_ParamModel = "";

	
	m_nFold = 0;


	m_strModelName = "default.model";
	m_numMaxInstance = -1;

	m_display_interval = 1;

  m_strDictFile = "";

  m_featFreqCutNum = 1;

  m_bEvaluateOut = false;
  m_bPostager = false;

//	m_start_iter = 1;

	

	//m_threadnum = 1;
}

JSTOptions::~JSTOptions()
{
}

int JSTOptions::setOptions(const char *option_file)
{
	cout << "\ngparser-option-config file: " << option_file << endl;

	ifstream conf(option_file);
	if (!conf) return -1;

	vector<string> vecOpt;
	string strLine;
	while (my_getline(conf, strLine)) vecOpt.push_back(strLine);
	conf.close();

	setOptions(vecOpt);
	return 0;
}

void JSTOptions::setOptions(const vector<string> &vecOption)
{
	int i = 0;
	for (; i < vecOption.size(); ++i) {
		pair<string, string> pr;
		string2pair(vecOption[i], pr, '=');
    /*
    if (pr.first == "train-algo")
    {
      if(pr.second == "mira")
      {
        m_traingAlgorithm = 1;
      }
      else if(pr.second == "pa")
      {
        m_traingAlgorithm = 2;
      }
      else if(pr.second == "Perceptron")
      {
        m_traingAlgorithm = 0;
      }
      else
      {
        cout << "Input training algorithm error, using Perceptron as default." << endl;
        m_traingAlgorithm = 0;
      }
    }*/

		if (pr.first == "train") m_isTrain = true;
		if (pr.first == "train-file") m_strTrainFile = pr.second;
		if (pr.first == "iters") m_numIter = atoi(pr.second.c_str());
		if (pr.first == "train-k") m_trainK = atoi(pr.second.c_str());


    if (pr.first == "dict-file") m_strDictFile = pr.second;

    if (pr.first == "test") m_isTest = true;
		if (pr.first == "test-file") m_strTestFile = pr.second;
    if (pr.first == "eval-file") m_strEvalFile = pr.second;
		if (pr.first == "output-file") m_strOutFile = pr.second;
    if (pr.first == "eval-param-model-iter-num") m_strEval_IterNum_of_ParamModel = pr.second;


		if (pr.first == "model-name") m_strModelName = pr.second;
		if (pr.first == "max-instance") m_numMaxInstance = atoi(pr.second.c_str());

		if (pr.first == "display-interval") m_display_interval = atoi(pr.second.c_str());

    if (pr.first == "feat-threshold") 
    {
      m_featFreqCutNum = atoi(pr.second.c_str());
      if(m_featFreqCutNum < 0) m_featFreqCutNum = 0;
    }

    if (pr.first == "eval-out") m_bEvaluateOut = true;

    if (pr.first == "postagger") m_bPostager = true;

    if (pr.first == "nfold") m_nFold = atoi(pr.second.c_str());


		//if (pr.first == "threadnum") m_threadnum = atoi(pr.second.c_str());

	}
}


void JSTOptions::showOptions()
{
	cout << "\n/*******configuration-beg*******/" << endl;

  string tmpStr = "Perceptron";
  if(m_traingAlgorithm == 1)
  {
    tmpStr = "mira";
  }

  if(m_traingAlgorithm == 2)
  {
    tmpStr = "pa";
  }
	cout << "train-algo: " << tmpStr << endl;


	if (m_isTrain) {
		cout << ">train: " << endl;
		cout << "\t" << "train-file: " << m_strTrainFile << endl;
    cout << "\t" << "eval-file: " << m_strEvalFile << endl;
		cout << "\t" << "iteration-num: " << m_numIter << endl;
		cout << "\t" << "train-k: " << m_trainK << endl;
	}


  cout << "\t" << "dict-file: " << m_strDictFile << endl;
	cout << "\t" << "test-file: " << m_strTestFile << endl;
	cout << "\t" << "output-file: " << m_strOutFile << endl;
  cout << "\t" << "eval-param-model-iter-num: " << (m_strEval_IterNum_of_ParamModel.empty() ? "not defined" : m_strEval_IterNum_of_ParamModel) << endl;

	cout << "\t" << "model-name: " << m_strModelName << endl;
	cout << endl;

	cout << "\t" << "instance-limit: " << m_numMaxInstance << endl;
	cout << "\t" << "display-interval: " << m_display_interval << endl;

  cout << "\t" << "feat-threshold: " << m_featFreqCutNum << endl;
  cout << "\t" << "nfold: " << m_nFold << endl;

	cout << endl;

  if (m_bEvaluateOut) cout << "surround-wordpos" << endl;
  if (m_bPostager) cout << "postagger" << endl;


	//cout << "\t" << "threadnum: " << m_threadnum << endl;

	cout << "/*******configuration-end*******/" << endl;
}


