#include "DepParser.h"
#include <cstdio>
#include <algorithm>
#include <functional>
#include <cstdlib>


using namespace std;


void DepParser::train(const vector<int> &instanceLengths ,int round)
{
	const set<int> &setIterNums = options.m_setTrain_IterNums_to_SaveParamModel;
	double maxDevAccuracy = 0.0;
	int bestModel = 0;
	int i = 1;
	for(; i <= options.m_numIter; i++) {
		cout << "Iteration " << i << "training started." << endl;
    print_time();
		if(i == 1)
		{
		  params.startAdding(0);
		}

    trainingIter(instanceLengths, i);

   
		params.storeParamsToTmp();
		params.finishAdding(1.0 *i*instanceLengths.size());
		params.averageParams(i * instanceLengths.size());
    cout << "Iteration " << i << "training finished." << endl;
    print_time();
    cout << "Iteration " << i << "testing started." << endl;
		double devAccuracy = devTest(options.m_strDevFile);
		if(devAccuracy > maxDevAccuracy)
		{
			maxDevAccuracy = devAccuracy;
			bestModel = i;
		}
		devTest(options.m_strTestFile);
    cout << "Iteration " << i << "testing finished." << endl;
    print_time();
		ostringstream tout;
		tout << i;
		saveParamModel(options.m_strModelName.c_str(), tout.str().c_str());
		//computeFeatureMutalInfo(instanceLengths);
		params.restoreParamsFromTmp();
	}
	cout << "The best model is " << bestModel << endl;
	if(round != -1)
	{
		// do other things
	}
}

void DepParser::trainingIter(const vector<int> &instanceLengths, int iter)
{
	 //cout << "1"<< endl;
	int numUpd = 0;
	FILE *trainForest = 0;
	vector<int> train_sequence;
	int numInstances = instanceLengths.size();

  //cout << "2"<< endl;
	if(options.m_bSaveMemory)
	{
		for(int i = 0; i < numInstances; i++)
		{
			train_sequence.push_back(i);
		}

		srand( iter+10 );
		// using built-in random generator:
		random_shuffle ( train_sequence.begin(), train_sequence.end() );
	} 
	else 
	{
		if (0 > pipe.initInputFile(options.m_strTrainFile.c_str())) return;
	}
  //cout << "3"<< endl;
	
	//cout << "numInstances\t" << numInstances << endl;
	DepInstance local_inst;
	DepInstance *pInst = 0;



	int i = 0;
	for(; i < numInstances; i++) {

		if((i) % options.m_display_interval == 0) 
    {
      cout<< i << " ";
      cout.flush();
    }

		int length = instanceLengths[i];
		if(options.m_bSaveMemory)
		{
			length = instanceLengths[train_sequence[i]];
		}

		FVS_PROBS fvs_probs_;
		fvs_probs_.allocMultiArr(length, pipe.m_vecTypes.size());
		if(options.m_bSaveMemory)
		{
			pInst = &pipe.m_vecInstances[train_sequence[i]];
			if (!pInst) break;
			pipe.createFeatureVector(pInst);
			pipe.fillFeatureVectors(pInst,&fvs_probs_,params);
		}
		else 
		{
			pInst = pipe.nextInstance();
			if (!pInst) break;
			pipe.createFeatureVector(pInst);
			pipe.createSpan(pInst);
			pipe.fillFeatureVectors(pInst,&fvs_probs_,params);
		}

		//double upd = (double)(options.m_numIter*numInstances - (numInstances*(iter-1)+(i+1)) + 1);
		double curUpdSeq = numInstances * (iter - 1) + i;
		int K = options.m_trainK;
		vector<FeatureVec> d0;
		vector<string> d1;
		vector<double> parse_probs;

		decoder.decodeProjective(pInst,&fvs_probs_, K, d0, d1, parse_probs);
    if(options.m_isUseSimp)
    {
      pipe.rerankWithGlobalFeature(*pInst, d0, d1, params);
    }

    if(options.m_traingAlgorithm == 1)
    {
		  params.updateParamsMIRA(pInst, d0, d1, curUpdSeq);
    }
    else if(options.m_traingAlgorithm == 0)
    {
      params.updateParamsPerceptron(pInst, d0, d1, curUpdSeq);
    }
    else if(options.m_traingAlgorithm == 2)
    {
      params.updateParamsKCRF(pInst, d0, d1, curUpdSeq);
    }
		//params.show();
	}

	cout << "\ninstance num: " << numInstances << endl;

	if(!options.m_bSaveMemory)
	{
		pipe.uninitInputFile();
	}

}

int DepParser::parseSent(const vector<string> &vecWord,
			  const vector<string> &vecPOS,
			  vector<int> &vecHead,
			  vector<string> &vecRel)
{
	DepInstance inst;
  if (0 != DepInstance::fillInstance(inst, vecWord, vecPOS)) return -1;
	DepInstance *pInst = &inst;

	int length = pInst->forms.size();

	FVS_PROBS fvs_probs_;
	fvs_probs_.allocMultiArr(length, pipe.m_vecTypes.size());

	pipe.fillFeatureVectors(pInst,&fvs_probs_,params);

	int K = 1; //default

	vector<FeatureVec> d0;
	vector<string> d1;
	vector<double> parse_probs;
	decoder.decodeProjective(pInst,&fvs_probs_,K, d0, d1, parse_probs);

  if(options.m_isUseSimp)
  {
    pipe.rerankWithGlobalFeature(*pInst, d0, d1, params);
  }

	if (parse_probs.empty() || parse_probs[0] < DOUBLE_NEGATIVE_INFINITY + EPS) {
		cout << " parse err: returned 0 result" << endl;
		return -1;
	}

	DepInstance::fillInstance(*pInst, d1[0], pipe.m_vecTypes);

	if (0 != DepInstance::getParseResult(inst, vecHead, vecRel)) return -1;
	if (vecHead.size() != vecWord.size()) {
		cout << "gparser parse err: word and head num not equal." << endl;
		return -1;
	}

	return 0;
}


int DepParser::saveParamModel(const char *modelName, const char *paramModelIterNum)
{
	string strFileName = "parameter.";
	strFileName += paramModelIterNum;
	strFileName += ".";
	strFileName += modelName;

	cout << "save parameter model: " << strFileName << endl;
	print_time();

	//FILE *paramModel = fopen(strFileName.c_str(), "wb");
	ofstream  paramModelTxt((strFileName + ".txt").c_str());
	FILE *paramModel = fopen(strFileName.c_str(), "wb");
	if (!paramModel) {
		cout << "DepParser::saveModel() open file err: " << strFileName << endl;
		return -1;
	}
	writeObject(paramModel, params.m_parameters);
	printObject(paramModelTxt, params.m_parameters);
	fclose(paramModel);
	paramModelTxt.close();
	cout << "done!" << endl;
	print_time();

	return 0;
}

int DepParser::loadParamModel(const char *modelPath, const char *modelName, const char *paramModelIterNum)
{
	string strFileName = modelPath;
	strFileName += "parameter.";
	strFileName += paramModelIterNum;
	strFileName += ".";
	strFileName += modelName;

	cout << "load parameter model: " << strFileName << endl;
	print_time();

	FILE *paramModel = fopen(strFileName.c_str(), "rb");
	if (!paramModel) {
		cout << "DepParser::loadParamModel() open file err: " << strFileName << endl;
		return -1;
	}

	MyVector<double> parameters;
	readObject(paramModel, parameters);
	params.setParams(parameters);

	fclose(paramModel);
	cout << "done!" << endl;
	print_time();
	return 0;
}


int DepParser::saveAlphabetModel(const char *modelName)
{
	string strFileName = "alphabet.";
	strFileName += modelName;
	cout << "save alphabet model: " << strFileName << endl;
	print_time();

	ofstream alphabetModel(strFileName.c_str());
	if (!alphabetModel) {
		cout << "DepParser::saveAlphabetModel() open file err: " << strFileName << endl;
		return -1;
	}
	pipe.m_featAlphabet.writeObject(alphabetModel);
	writeObject(alphabetModel, int(-2));
	pipe.m_labelAlphabet.writeObject(alphabetModel);
	writeObject(alphabetModel, int(-3));
	alphabetModel.close();

	string str1 = strFileName + ".feat.trie";
	pipe.m_featAlphabet.saveTrie(str1.c_str());
	string str2 = strFileName + ".label.trie";
	pipe.m_labelAlphabet.saveTrie(str2.c_str());


	cout << "done!" << endl;
	print_time();
	return 0;
}


int DepParser::loadAlphabetModel(const char *modelPath, const char *modelName)
{
	string strFileName = modelPath;
	strFileName += "alphabet.";
	strFileName += modelName;
	cout << "load alphabet model: " << strFileName << endl;
	print_time();

	ifstream alphabetModel(strFileName.c_str());
	if (!alphabetModel) {
		cout << "DepParser::loadAlphabetModel() open file err: " << strFileName << endl;
		return -1;
	}
	int tag;
	pipe.m_featAlphabet.readObject(alphabetModel);
	readObject(alphabetModel, tag);
	if (tag != -2) {
		cout << "DepParser::loadAlphabetModel() err, not see -2" << endl;
		return -1;
	}

	pipe.m_featAlphabet.collectKeys();

	pipe.m_labelAlphabet.readObject(alphabetModel);
	readObject(alphabetModel, tag);
	if (tag != -3) {
		cout << "DepParser::loadAlphabetModel() err, not see -3" << endl;
		return -1;
	}

	pipe.m_labelAlphabet.collectKeys();

	alphabetModel.close();

	string str1 = strFileName + ".feat.trie";
	pipe.m_featAlphabet.loadTrie(str1.c_str());
	string str2 = strFileName + ".label.trie";
	pipe.m_labelAlphabet.loadTrie(str2.c_str());

	cout << "done!" << endl;
	print_time();
	return 0;
}

void DepParser::computeFeatureMutalInfo(const vector<int> &instanceLengths)
{
  //cout << "1"<< endl;
	FILE *trainForest = 0;
  //cout << "2"<< endl;
	if(options.m_bSaveMemory)
	{
		// do nothing
	}
	else 
	{
		if (0 > pipe.initInputFile(options.m_strTrainFile.c_str())) return;
	}
  //cout << "3"<< endl;
	int numInstances = instanceLengths.size();
	//cout << "numInstances\t" << numInstances << endl;
	DepInstance local_inst;
	DepInstance *pInst = 0;

  int cnt = 0;
	int sent_num_word_all_nopunc_dep_correct_total = 0;
	int sent_num_root_correct_total = 0;
	int word_num_nopunc_total = 0;
	int word_num_nopunc_dep_correct_total = 0;

	int sent_num_word_all_nopunc_dep_correct_cur = 0;
	int sent_num_root_correct_cur = 0;
	int word_num_nopunc_cur = 0;
	int word_num_nopunc_dep_correct_cur = 0;

	int i = 0;
	for(; i < numInstances; i++) {

		//if((i) % options.m_display_interval == 0) cout<< i << " ";
    cnt++;
		int length = instanceLengths[i];


		FVS_PROBS fvs_probs_;
		fvs_probs_.allocMultiArr(length, pipe.m_vecTypes.size());

		if(options.m_bSaveMemory)
		{
			pInst = &pipe.m_vecInstances[i];
			if (!pInst) break;
			pipe.createFeatureVector(pInst);
			pipe.fillFeatureVectors(pInst,&fvs_probs_,params);
		} 
		else 
		{
			pInst = pipe.nextInstance();
			if (!pInst) break;
			pipe.createFeatureVector(pInst);
			pipe.createSpan(pInst);
			pipe.fillFeatureVectors(pInst,&fvs_probs_,params);
		}

		int K = options.m_mute_info_k;
		vector<FeatureVec> d0;
		vector<string> d1;
		vector<double> parse_probs;

		decoder.decodeProjective(pInst,&fvs_probs_,K, d0, d1, parse_probs);

    if(options.m_isUseSimp)
    {
      pipe.rerankWithGlobalFeature(*pInst, d0, d1, params);
    }

		const string &actParseTree = pInst->actParseTree;

		sent_num_word_all_nopunc_dep_correct_cur = 0;
    sent_num_root_correct_cur = 0;
    word_num_nopunc_cur = 0;
    word_num_nopunc_dep_correct_cur = 0;


		params.evaluateArcNoPunc(pInst, d1[0], actParseTree, word_num_nopunc_dep_correct_cur, word_num_nopunc_cur);
		if(word_num_nopunc_dep_correct_cur == word_num_nopunc_cur)
		{
		  sent_num_word_all_nopunc_dep_correct_cur = 1;
		}
		params.isRootCorrect(pInst, d1[0], actParseTree, sent_num_root_correct_cur);

		word_num_nopunc_total += word_num_nopunc_cur;
		word_num_nopunc_dep_correct_total += word_num_nopunc_dep_correct_cur;
		sent_num_word_all_nopunc_dep_correct_total += sent_num_word_all_nopunc_dep_correct_cur;
		sent_num_root_correct_total += sent_num_root_correct_cur;


		if((i+1) % options.m_display_interval == 0)
		{
		  cout << "CM (excluding punc): \t\t"  << sent_num_word_all_nopunc_dep_correct_total << "/" << cnt << " = " << sent_num_word_all_nopunc_dep_correct_total*100.0/cnt << endl;
      cout << "UAS (excluding punc):  \t\t" << word_num_nopunc_dep_correct_total << "/" << word_num_nopunc_total << " = " << word_num_nopunc_dep_correct_total*100.0/word_num_nopunc_total << endl;
      cout << "ROOT (excluding punc):  \t\t" << sent_num_root_correct_total << "/" << cnt << " = " << sent_num_root_correct_total*100.0/cnt << endl;
		}

    //params.computeFeatureCount(pInst, d0, d1);
		//cout << "\nupd: " << upd << endl;

		//cout << "6" << endl;
		//params.show();
	}

	cout << "CM (excluding punc): \t\t"  << sent_num_word_all_nopunc_dep_correct_total << "/" << cnt << " = " << sent_num_word_all_nopunc_dep_correct_total*100.0/cnt << endl;
  cout << "UAS (excluding punc):  \t\t" << word_num_nopunc_dep_correct_total << "/" << word_num_nopunc_total << " = " << word_num_nopunc_dep_correct_total*100.0/word_num_nopunc_total << endl;
  cout << "ROOT (excluding punc):  \t\t" << sent_num_root_correct_total << "/" << cnt << " = " << sent_num_root_correct_total*100.0/cnt << endl;


	cout << "\ninstance num: " << numInstances << endl;

	if(!options.m_bSaveMemory)
	{
		pipe.uninitInputFile();
	}
}

void DepParser::computeFeatureWeightInfo()
{
  //params.
}

void DepParser::featureReduction(const vector<int> &instanceLengths, int reduCount)
{
  computeFeatureMutalInfo(instanceLengths);
  computeFeatureWeightInfo();

  ostringstream tout;
  tout << reduCount;

  string strFileName = "parameter.";
	strFileName += tout.str();
	strFileName += ".";
	strFileName += "reduction.temp";

	cout << "save parameter information: " << strFileName << endl;
	print_time();

	//FILE *paramModel = fopen(strFileName.c_str(), "wb");
	ofstream  paramInfoTxt((strFileName + ".txt").c_str());

  for(int i = 0; i < params.m_parameters.size(); i++)
  {
    printObject(paramInfoTxt, params.m_parameters[i]);
    printObject(paramInfoTxt, "\t");
    printObject(paramInfoTxt, params.m_posCount[i]);
    printObject(paramInfoTxt, "\t");
    printObject(paramInfoTxt, params.m_negCount[i]);
    printObject(paramInfoTxt, "\t");
    printObject(paramInfoTxt, params.m_variance[i]);
    printObject(paramInfoTxt, "\n");
  }

	paramInfoTxt.close();
	cout << "done!" << endl;
	print_time();

	params.selectInvalidFeatures(reduCount, options.m_reduction_ratio, options.m_fsMethod);


}

void DepParser::featureReductionTrain(const vector<int> &instanceLengths)
{
  //params.m_totalLearningCount = options.m_numIter * instanceLengths.size();
  //params.m_addingStart = options.m_start_iter * instanceLengths.size();
  for(int i = 0; i < options.m_reduction_iter; i++)
  {
    train(instanceLengths, i);
    vector<string> invalidFeatures_str;
    pipe.ReInit(params.m_invalidFeatures, invalidFeatures_str);
    ostringstream tout;
    tout << i;
    string strFileName = "parameter.";
    strFileName += tout.str();
    strFileName += ".";
    strFileName += "invalidfeatures";

    cout << "save invalid parameter model: " << strFileName << endl;
    print_time();

    //FILE *paramModel = fopen(strFileName.c_str(), "wb");
    ofstream  paramInfoTxt((strFileName + ".txt").c_str());

    for(int j = 0; j < invalidFeatures_str.size(); j++)
    {
      printObject(paramInfoTxt, invalidFeatures_str[j]);
      printObject(paramInfoTxt, "\n");
    }

    paramInfoTxt.close();

    cout << "Features Number:" << pipe.m_featAlphabet.size() << endl;
    params.ReInit(pipe.m_featAlphabet.size());
    ostringstream out;
    out << i;
    saveAlphabetModel((options.m_strModelName + out.str()).c_str());
  }
}

double DepParser::devTest(string devFile)
{
  if (0 > pipe.initInputFile(devFile.c_str())) return 0;

	cout << "Start testing on file " << devFile << ":" << endl;
	DepInstance *instance = pipe.nextInstance();

	int cnt = 0;
	int sent_num_word_all_nopunc_dep_correct_total = 0;
	int sent_num_root_correct_total = 0;
	int word_num_nopunc_total = 0;
	int word_num_nopunc_dep_correct_total = 0;

  int sent_num_word_all_nopunc_deplabel_correct_total = 0;
  int word_num_nopunc_deplabel_correct_total = 0;

	int sent_num_word_all_nopunc_dep_correct_cur = 0;
	int sent_num_root_correct_cur = 0;
	int word_num_nopunc_cur = 0;
	int word_num_nopunc_dep_correct_cur = 0;

  int sent_num_word_all_nopunc_deplabel_correct_cur = 0;
  int word_num_nopunc_deplabel_correct_cur = 0;

	while(instance) {
		//if(++cnt % options.m_display_interval == 0) cout<< cnt << " ";

		int length = instance->forms.size();
		FVS_PROBS fvs_probs_;
		fvs_probs_.allocMultiArr(length, pipe.m_vecTypes.size());
    pipe.createFeatureVector(instance);
    pipe.createSpan(instance);
		pipe.fillFeatureVectors(instance,&fvs_probs_,params);

		int K = 1; //default
		if (options.m_devK > 0) {
			K = options.m_devK;
		}

		vector<FeatureVec> d0;
		vector<string> d1;
		vector<double> parse_probs;
		decoder.decodeProjective(instance,&fvs_probs_,K, d0, d1, parse_probs);

    if(options.m_isUseSimp)
    {
      pipe.rerankWithGlobalFeature(*instance, d0, d1, params);
    }
		
		const string &actParseTree = instance->actParseTree;

		sent_num_word_all_nopunc_dep_correct_cur = 0;
    sent_num_root_correct_cur = 0;
    word_num_nopunc_cur = 0;
    word_num_nopunc_dep_correct_cur = 0;

    sent_num_word_all_nopunc_deplabel_correct_cur = 0;
    word_num_nopunc_deplabel_correct_cur = 0;

		params.evaluateArcNoPunc(instance, d1[0], actParseTree, word_num_nopunc_dep_correct_cur, word_num_nopunc_cur);
    //cout << word_num_nopunc_cur << endl;
    params.evaluateLabelNoPunc(instance, d1[0], actParseTree, word_num_nopunc_deplabel_correct_cur, word_num_nopunc_cur);

		if(word_num_nopunc_dep_correct_cur == word_num_nopunc_cur)
		{
		  sent_num_word_all_nopunc_dep_correct_cur = 1;
		}

    if(word_num_nopunc_deplabel_correct_cur == word_num_nopunc_cur)
		{
		  sent_num_word_all_nopunc_deplabel_correct_cur = 1;
		}

		params.isRootCorrect(instance, d1[0], actParseTree, sent_num_root_correct_cur);

		word_num_nopunc_total += word_num_nopunc_cur;
		word_num_nopunc_dep_correct_total += word_num_nopunc_dep_correct_cur;
    word_num_nopunc_deplabel_correct_total += word_num_nopunc_deplabel_correct_cur;
		sent_num_word_all_nopunc_dep_correct_total += sent_num_word_all_nopunc_dep_correct_cur;
    sent_num_word_all_nopunc_deplabel_correct_total += sent_num_word_all_nopunc_deplabel_correct_cur;
		sent_num_root_correct_total += sent_num_root_correct_cur;


    if(++cnt % options.m_display_interval == 0)
    {
      cout << "CM (excluding punc): \t\t"  << sent_num_word_all_nopunc_dep_correct_total << "/" << cnt << " = " << sent_num_word_all_nopunc_dep_correct_total*100.0/cnt << endl;
      cout << "CM_L (excluding punc): \t\t"  << sent_num_word_all_nopunc_deplabel_correct_total << "/" << cnt << " = " << sent_num_word_all_nopunc_deplabel_correct_total*100.0/cnt << endl;
      cout << "UAS (excluding punc):  \t\t" << word_num_nopunc_dep_correct_total << "/" << word_num_nopunc_total << " = " << word_num_nopunc_dep_correct_total*100.0/word_num_nopunc_total << endl;
      cout << "LAS (excluding punc):  \t\t" << word_num_nopunc_deplabel_correct_total << "/" << word_num_nopunc_total << " = " << word_num_nopunc_deplabel_correct_total*100.0/word_num_nopunc_total << endl;
      cout << "ROOT (excluding punc):  \t\t" << sent_num_root_correct_total << "/" << cnt << " = " << sent_num_root_correct_total*100.0/cnt << endl;
    }

		if ( options.m_numMaxInstance > 0 && cnt == options.m_numMaxInstance) break;
		instance = pipe.nextInstance();
	}

	cout << "CM (excluding punc): \t\t"  << sent_num_word_all_nopunc_dep_correct_total << "/" << cnt << " = " << sent_num_word_all_nopunc_dep_correct_total*100.0/cnt << endl;
  cout << "CM_L (excluding punc): \t\t"  << sent_num_word_all_nopunc_deplabel_correct_total << "/" << cnt << " = " << sent_num_word_all_nopunc_deplabel_correct_total*100.0/cnt << endl;
  cout << "UAS (excluding punc):  \t\t" << word_num_nopunc_dep_correct_total << "/" << word_num_nopunc_total << " = " << word_num_nopunc_dep_correct_total*100.0/word_num_nopunc_total << endl;
  cout << "LAS (excluding punc):  \t\t" << word_num_nopunc_deplabel_correct_total << "/" << word_num_nopunc_total << " = " << word_num_nopunc_deplabel_correct_total*100.0/word_num_nopunc_total << endl;
  cout << "ROOT (excluding punc):  \t\t" << sent_num_root_correct_total << "/" << cnt << " = " << sent_num_root_correct_total*100.0/cnt << endl;


	pipe.uninitInputFile();

	cout << "\ninstance num: " << cnt << endl;
	cout << "Finish testing on file " << devFile << "." << endl;
	cout << endl;
	return word_num_nopunc_deplabel_correct_total*100.0/word_num_nopunc_total;
}


//////////////////////////////////////////////////////
// Evaluate ///////////////////////////////////
//////////////////////////////////////////////////////
void DepParser::evaluate()
{
	cout << "start\n";
	if (0 > pipe.initInputFile(options.m_strEvalFile.c_str())) return;
	if (0 > pipe.initOutputFile(options.m_strOutFile.c_str())) return;

	cout << "Processing Sentence: ";
	DepInstance *instance = pipe.nextInstance();
	int cnt = 0;


	while(instance) {
		if(++cnt % options.m_display_interval == 0) 
    {
      cout<< cnt << " ";
      cout.flush();
    }
		pipe.createFeatureVector(instance);
    pipe.createSpan(instance);

		int length = instance->forms.size();
		FVS_PROBS fvs_probs_;
		fvs_probs_.allocMultiArr(length, pipe.m_vecTypes.size());
		pipe.fillFeatureVectors(instance,&fvs_probs_,params);

		int K = 1; //default
		if (options.m_evalK > 0) 
		{
			K = options.m_evalK;
		}

		vector<FeatureVec> d0;
		vector<string> d1;
		vector<double> parse_probs;

		decoder.decodeProjective(instance,&fvs_probs_,K, d0, d1, parse_probs);

    if(options.m_isUseSimp)
    {
      pipe.rerankWithGlobalFeature(*instance, d0, d1, params);
    }

		instance->k_heads.clear();
		instance->k_deprels.clear();
		instance->k_probs.clear();

		//if (options.m_evalK > 1) 
		//{
      DepInstance::fillInstance_k(*instance, d1, parse_probs, pipe.m_vecTypes);
		//} 
		//else 
		//{
		//	DepInstance::fillInstance(*instance, d1[0], pipe);
		//}
		pipe.outputInstance(instance);

		


		if ( options.m_numMaxInstance > 0 && cnt == options.m_numMaxInstance) break;
		instance = pipe.nextInstance();
	}
	pipe.uninitInputFile();
	pipe.uninitOutputFile();

	cout << "\ninstance num: " << cnt << endl;

}

