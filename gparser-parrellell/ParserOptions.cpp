#include "ParserOptions.h"
#include "MyLib.h"

ParserOptions::ParserOptions()
{
  m_traingAlgorithm = 0;
  m_fsMethod = 0;
  //m_threshold = 1e-6;
	m_isTrain = false;
	m_strTrainFile = "";
	m_numIter = 10;
	m_trainK = 1;
	m_strTrain_IterNums_to_SaveParamModel = "";
	m_setTrain_IterNums_to_SaveParamModel.clear();
	m_bSaveMemory = true;

	m_strTestFile = "";
	m_strOutFile = "";

	m_isEvaluate = false;
	m_strEvalFile = "";
	m_strEvalOutFile = "";
	m_strEvalErrGoldFile = "";
	m_evalK = 0;
	m_strEval_IterNum_of_ParamModel = "";

	m_strDevFile = "";
	m_devK = 0;

	m_isUseSib = true;
	m_isLabeled = false;
	m_strModelName = "default.model";
	m_numMaxInstance = -1;
	m_isCONLLFormat = true;

	m_display_interval = 1;
//	m_start_iter = 1;
	m_reduction_iter = 5;
	m_mute_info_k = 1;

	m_reduction_ratio = 0.1;

	m_isUseForm = true;
	m_isUseGuideArc = false;
	m_isUseGuideLab = false;
	m_isUseCPostag = true;
	m_isUseFeats = true;

	m_isUseForm_label = false;
	m_isUseLemma_label = false;

	m_isUse_label_feats_t_child = false;
	m_isUse_label_feats_t = false;


  m_isUse_uni = true;
	m_isUse_bi = true;
	m_isUse_bet = true;
	m_isUse_sur = true;
  m_isUseSimp = false;

//	m_isEval = false;
//	m_strGoldFile = "";
  m_is_english = false;

	m_bSentenceSimplification = false;
	m_simpRuleFile = "";
	m_threadnum = 1;
}

ParserOptions::~ParserOptions()
{
}

int ParserOptions::setOptions(const char *option_file)
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

void ParserOptions::setOptions(const vector<string> &vecOption)
{
	int i = 0;
	for (; i < vecOption.size(); ++i) {
		pair<string, string> pr;
		string2pair_first(vecOption[i], pr, ':');
    if (pr.first == "train-algo")
    {
      if(pr.second == "mira")
      {
        m_traingAlgorithm = 1;
      }
      else if(pr.second == "Perceptron")
      {
        m_traingAlgorithm = 0;
      }
      else if(pr.second == "KCRF")
      {
        m_traingAlgorithm = 2;
      }
      else
      {
        cout << "Input training algorithm error, using Perceptron as default." << endl;
        m_traingAlgorithm = 0;
      }
    }

    if (pr.first == "fs-method")
    {
      if(pr.second == "weight-abs")
      {
        m_fsMethod = 2;
      }
      else if(pr.second == "likelihood-est")
      {
        m_fsMethod = 1;
      }
      else if(pr.second == "likelihood-ratio")
      {
        m_fsMethod = 0;
      }
      else
      {
        cout << "Input feature selection algorithm error, using likelihood-ratio as default." << endl;
        m_fsMethod = 0;
      }
    }

//    if (pr.first == "threshold") m_threshold = atof(pr.second.c_str());
		if (pr.first == "is-english") m_is_english = atoi(pr.second.c_str());
		if (pr.first == "sentence-siplification") m_bSentenceSimplification = true;
		if (pr.first == "siplification-rule-file") m_simpRuleFile = pr.second;

		if (pr.first == "train") m_isTrain = true;
		if (pr.first == "train-file") m_strTrainFile = pr.second;
		if (pr.first == "iters") m_numIter = atoi(pr.second.c_str());
		if (pr.first == "train-k") m_trainK = atoi(pr.second.c_str());
		if (pr.first == "train-iter-nums-to-save-param-model") {
			m_strTrain_IterNums_to_SaveParamModel = pr.second;
			vector<string> vec;
			split_bychar(m_strTrain_IterNums_to_SaveParamModel, vec, '_');
			int j = 0;
			for (; j < vec.size(); ++j) {
				m_setTrain_IterNums_to_SaveParamModel.insert( atoi( vec[j].c_str() ) );
			}
		}
		if (pr.first == "save-memory") m_bSaveMemory = atoi(pr.second.c_str()) == 0 ? false : true;

		if (pr.first == "test-file") m_strTestFile = pr.second;
		if (pr.first == "output-file") m_strOutFile = pr.second;

		if (pr.first == "evaluate") m_isEvaluate = true;
		if (pr.first == "eval-file") m_strEvalFile = pr.second;
		if (pr.first == "eval-output-file") m_strEvalOutFile = pr.second;
		if (pr.first == "eval-errorgold-file") m_strEvalErrGoldFile = pr.second;
		if (pr.first == "eval-k") m_evalK = atoi(pr.second.c_str());
		if (pr.first == "eval-param-model-iter-num") m_strEval_IterNum_of_ParamModel = pr.second;

		if (pr.first == "dev-file") m_strDevFile = pr.second;
		if (pr.first == "dev-k") m_devK = atoi(pr.second.c_str());

		if (pr.first == "use-sib") m_isUseSib = true;
		if (pr.first == "labeled") m_isLabeled = true;
		if (pr.first == "model-name") m_strModelName = pr.second;
		if (pr.first == "max-instance") m_numMaxInstance = atoi(pr.second.c_str());
		if (pr.first == "data-format") {
			if (pr.second == "conll") {
				m_isCONLLFormat = true;
			} else {
				m_isCONLLFormat = false;
			}
		}
		if (pr.first == "display-interval") m_display_interval = atoi(pr.second.c_str());
//		if (pr.first == "start-iter") m_start_iter = atoi(pr.second.c_str());
//		if(m_start_iter < 1 )m_start_iter = 1;
		if (pr.first == "reduction-iter") m_reduction_iter = atoi(pr.second.c_str());
		if (pr.first == "mute-info-k") m_mute_info_k = atoi(pr.second.c_str());
		if (pr.first == "reduction-radio") m_reduction_ratio = atof(pr.second.c_str());

		if (pr.first == "use-form") m_isUseForm = true;
		if (pr.first == "use-guide-arc") m_isUseGuideArc = true;
		if (pr.first == "use-guide-lab") m_isUseGuideLab = true;
		if (pr.first == "use-cpostag") m_isUseCPostag = true;
		if (pr.first == "use-feats") m_isUseFeats = true;

		if (pr.first == "use-form-label") m_isUseForm_label = true;
		if (pr.first == "use-guide-arc-label") m_isUseLemma_label = true;

		if (pr.first == "use-label-feats_t_child") m_isUse_label_feats_t_child = true;
		if (pr.first == "use-label-feats_t") m_isUse_label_feats_t = true;


		if (pr.first == "use-uni") m_isUse_uni = true;
		if (pr.first == "use-bi") m_isUse_bi = true;
		if (pr.first == "use-bet") m_isUse_bet = true;
		if (pr.first == "use-sur") m_isUse_sur = true;
    if (pr.first == "use-simp") m_isUseSimp = true;

		if (pr.first == "threadnum") m_threadnum = atoi(pr.second.c_str());

//		if (pr.first == "eval") m_isEval = true;
//		if (pr.first == "gold-file") m_strGoldFile = pr.second;
	}
}


void ParserOptions::showOptions()
{
	cout << "\n/*******configuration-beg*******/" << endl;

  string tmpStr = "Perceptron";
  if(m_traingAlgorithm == 1)
  {
    tmpStr = "mira";
  }
  else if(m_traingAlgorithm == 2)
  {
    tmpStr = "KCRF";
  }
	cout << "train-algo: " << tmpStr << endl;

	tmpStr = "likelihood-ratio";
	if(m_fsMethod == 1)
  {
    tmpStr = "likelihood-est";
  }
  else if(m_fsMethod == 2)
  {
    tmpStr = "weight-abs";
  }
  cout << "fs-method: " << tmpStr << endl;

	if(m_bSentenceSimplification)
	{
		cout << "sentence-siplification" << endl;
		cout << "siplification-rule-file:" << m_simpRuleFile << endl;
	}

//  cout << "\t" << "threshold: " << m_threshold << endl;

	if (m_isTrain) {
		cout << ">train: " << endl;
		cout << "\t" << "train-file: " << m_strTrainFile << endl;
		cout << "\t" << "iteration-num: " << m_numIter << endl;
		cout << "\t" << "train-k: " << m_trainK << endl;
		cout << "\t" << "train-iter-nums-to-save-param-model: " << (m_strTrain_IterNums_to_SaveParamModel.empty() ? "not defined" : m_strTrain_IterNums_to_SaveParamModel) << endl;
		cout << "\t" << "save-memory" << (m_bSaveMemory ? "yes" : "no") << endl;
	}

	if (m_isEvaluate) {
		cout << ">evaluate: " << endl;
		cout << "\t" << "eval-file: " << m_strEvalFile << endl;
		cout << "\t" << "eval-output-file: " << m_strEvalOutFile << endl;
		cout << "\t" << "eval-errorgold-file: " << m_strEvalErrGoldFile << endl;
		cout << "\t" << "eval-k: " << m_evalK << endl;
		cout << "\t" << "eval-param-model-iter-num: " << (m_strEval_IterNum_of_ParamModel.empty() ? "not defined" : m_strEval_IterNum_of_ParamModel) << endl;
	}


	cout << "\t" << "test-file: " << m_strTestFile << endl;
	cout << "\t" << "output-file: " << m_strOutFile << endl;
	cout << "\t" << "dev-file: " << m_strDevFile << endl;
	cout << "\t" << "dev-k: " << m_devK << endl;


	cerr << "\n\t" << "is-english: " << (m_is_english ? "yes" : "no") << endl;


	cout << ">other: " << endl;
	cout << "\t" << "use-sib: " << (m_isUseSib ? "yes" : "no") << endl;
	cout << "\t" << "labeled: " << (m_isLabeled ? "yes" : "no") << endl;
	cout << "\t" << "model-name: " << m_strModelName << endl;
	cout << endl;

	cout << "\t" << "instance-limit: " << m_numMaxInstance << endl;
	cout << "\t" << "data-format: " << (m_isCONLLFormat ? "conll" : "not-conll") << endl;
	cout << "\t" << "display-interval: " << m_display_interval << endl;
//	cout << "\t" << "start-iter: " << m_start_iter << endl;
	cout << "\t" << "reduction-iter: " << m_reduction_iter << endl;
	cout << "\t" << "mute-info-k: " << m_mute_info_k << endl;
	cout << "\t" << "reduction-radio: " << m_reduction_ratio << endl;


	cout << ">features: " << endl;
	cout << "\t" << "use-form: " << (m_isUseForm ? "yes" : "no") << endl;
	cout << "\t" << "use-guide-arc: " << (m_isUseGuideArc ? "yes" : "no") << endl;
	cout << "\t" << "use-guide-lab: " << (m_isUseGuideLab ? "yes" : "no") << endl;
	cout << "\t" << "use-cpostag: " << (m_isUseCPostag ? "yes" : "no") << endl;
	cout << endl;
	cout << "\t" << "use-form-label: " << (m_isUseForm_label ? "yes" : "no") << endl;
	cout << "\t" << "use-guide-arc-label: " << (m_isUseLemma_label ? "yes" : "no") << endl;
	cout << "\t" << "use-label-feats_t_child [such as form_cpos_type_is-child]: " << (m_isUse_label_feats_t_child ? "yes" : "no") << endl;
	cout << "\t" << "use-label-feats_t [such as form_cpos_type]: " << (m_isUse_label_feats_t ? "yes" : "no") << endl;



	cout << endl;

	cout << "\t" << "use-uni: " << (m_isUse_uni ? "yes" : "no") << endl;
	cout << "\t" << "use-bi: " << (m_isUse_bi ? "yes" : "no") << endl;
	cout << "\t" << "use-bet: " << (m_isUse_bet ? "yes" : "no") << endl;
	cout << "\t" << "use-sur: " << (m_isUse_sur ? "yes" : "no") << endl;
  cout << "\t" << "use-simp: " << (m_isUseSimp ? "yes" : "no") << endl;
  cout << "\t" << "use-feats: " << (m_isUseFeats ? "yes" : "no") << endl;
	cout << "\t" << "threadnum: " << m_threadnum << endl;

	cout << "/*******configuration-end*******/" << endl;
}


