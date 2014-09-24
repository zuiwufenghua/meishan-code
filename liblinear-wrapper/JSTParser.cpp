#include "JSTParser.h"
#include <cstdio>
#include <algorithm>
#include <functional>
#include <cstdlib>


using namespace std;


void JSTParser::train()
{
  svm.init_problems(pipe.m_vecInstances, pipe.m_featAlphabet.size());
  //for(double c = 0.125; c <= 128 + EPS; c = c*2.0)
  double bestModel = -1;
  double bestAccuracy = 0;
  for(double c = 0.01; c <= 0.2 + EPS; c = c + 0.01)
  {
    //double c = 0.125;
    svm.resetC(c);
		svm.svm_train();
    //evaluate(options.m_strTrainFile,0);
    double curAccuracy = evaluate(options.m_strEvalFile,0);
    evaluate(options.m_strTestFile,0); 

    if(bestAccuracy < curAccuracy)
    {
      bestAccuracy = curAccuracy;
      bestModel = c;
    }

    ostringstream tout;
		tout << c ;
		saveParamModel(options.m_strModelName.c_str(), tout.str().c_str());
    svm.free_model();
    cout << "model " << c << "finished" << endl;
	}
	cout << "The best model is " << bestModel << endl;

}

void JSTParser::decode(string decodeFile, string outputFile)
{
  if (0 > pipe.initInputFile(decodeFile.c_str())) return;
  if (0 > pipe.initOutputFile(outputFile.c_str())) return;

  cout << "Start decoding on file " << options.m_strTestFile << ":" << endl;
  
  vector<string> sentence;
  svm.initPredictParam(options);
  int numInstance = 0;

  while(pipe.getNextRawSentence(sentence) == true)
  {
    vector<FeatureVec> fvs;
    
    int sentenceLength = sentence.size();
    fvs.resize(sentenceLength);
    JSTInstance jstOutInstance;
    jstOutInstance.words.clear();
    jstOutInstance.labels.clear();
    jstOutInstance.sentence.clear();
    jstOutInstance.sentenceTags.clear();

    vector<string> sentenceTags;
    jstOutInstance.sentenceTags.resize(sentenceLength);
    for(int curl = 0; curl < sentenceLength; curl++)
    {
      sentenceTags.push_back("_");
      jstOutInstance.sentence.push_back(sentence[curl]);
    }

    for(int curl = 0; curl < sentenceLength; curl++)
    {
      pipe.initSentenceFeatures(fvs, sentence, sentenceTags, curl);
		  int curSegLabelTagId = svm.svm_predict(fvs[curl]);
      string curSegLabelTag = pipe.m_vecTypes[curSegLabelTagId];
      sentenceTags[curl] = curSegLabelTag;
      jstOutInstance.sentenceTags[curl] = curSegLabelTag;
      if(curl == 0 || curSegLabelTag.substr(0,2) == "B-")
      {
        jstOutInstance.words.push_back(sentence[curl]);
        jstOutInstance.labels.push_back(curSegLabelTag.substr(2));
      }
      else
      {
        int curWordLength = jstOutInstance.words.size();
        jstOutInstance.words[curWordLength-1] = jstOutInstance.words[curWordLength-1] + sentence[curl];
      }     
    }

    pipe.outputInstance(&jstOutInstance);

    if (++numInstance % options.m_display_interval == 0) 
    {
      cout << numInstance << " ";
      cout.flush();
    }

  }

  cout << numInstance << endl;

  pipe.uninitInputFile();
  pipe.uninitOutputFile();
}


int JSTParser::saveParamModel(const char *modelName, const char *paramModelIterNum)
{
	string strFileName = "parameter.";
	strFileName += paramModelIterNum;
	strFileName += ".";
	strFileName += modelName;

	cout << "save parameter model: " << strFileName << endl;
	print_time();
  svm.save_svm_model(strFileName.c_str());
	cout << "done!" << endl;
	print_time();

	return 0;
}

int JSTParser::loadParamModel(const char *modelPath, const char *modelName, const char *paramModelIterNum)
{
	string strFileName = modelPath;
	strFileName += "parameter.";
	strFileName += paramModelIterNum;
	strFileName += ".";
	strFileName += modelName;

	cout << "load parameter model: " << strFileName << endl;
	print_time();
  svm.load_svm_model(strFileName.c_str());
	cout << "done!" << endl;
	print_time();
	return 0;
}


int JSTParser::saveAlphabetModel(const char *modelName)
{
	string strFileName = "alphabet.";
	strFileName += modelName;
	cout << "save alphabet model: " << strFileName << endl;
	print_time();

	ofstream alphabetModel(strFileName.c_str());
	if (!alphabetModel) {
		cout << "JSTParser::saveAlphabetModel() open file err: " << strFileName << endl;
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


int JSTParser::loadAlphabetModel(const char *modelPath, const char *modelName)
{
	string strFileName = modelPath;
	strFileName += "alphabet.";
	strFileName += modelName;
	cout << "load alphabet model: " << strFileName << endl;
	print_time();

	ifstream alphabetModel(strFileName.c_str());
	if (!alphabetModel) {
		cout << "JSTParser::loadAlphabetModel() open file err: " << strFileName << endl;
		return -1;
	}
	int tag;
	pipe.m_featAlphabet.readObject(alphabetModel);
	readObject(alphabetModel, tag);
	if (tag != -2) {
		cout << "JSTParser::loadAlphabetModel() err, not see -2" << endl;
		return -1;
	}

	pipe.m_featAlphabet.collectKeys();

	pipe.m_labelAlphabet.readObject(alphabetModel);
	readObject(alphabetModel, tag);
	if (tag != -3) {
		cout << "JSTParser::loadAlphabetModel() err, not see -3" << endl;
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

double JSTParser::evaluate(string devFile ,int iter)
{
  if (0 > pipe.initInputFile(devFile.c_str())) return 0;
  print_time();

	cout << "Start evaluating on file " << devFile << ":" << endl;
	JSTInstance *instance = pipe.nextInstance();

	int totalPredWords = 0; int totalRecoWords = 0; int totalGoldWords = 0;
	int totalPredIVWords = 0; int totalRecoIVWords = 0; int totalGoldIVWords = 0;
	int totalPredOOVWords = 0; int totalRecoOOVWords = 0; int totalGoldOOVWords = 0;
	int numsent = 0; int corrsent = 0; int corrsentL = 0;
	int correctLabels = 0; int correctLabelsIV = 0; int correctLabelsOOV = 0;
  int correctTags = 0; int totalTags = 0;
  int feature_extract_time = 0;
  int decode_time = 0;
  //int tag2st_time = 0;
  int evaluate_time = 0;

  int cnt = 0;
  ofstream eval_outf;
  ostringstream out;   
  out << iter;
  //string outfilename = devFile + ".out." + out.str();
  //eval_outf.open(outfilename.c_str());
	if (!eval_outf) {
		cout << "JSTParser::startWriting evaluating result result open file err:" << endl;
	}

	while(instance) {

    vector<FeatureVec> fvs;   
    int sentenceLength = instance->sentence.size();
    fvs.resize(sentenceLength);

    vector<string> words;
    vector<string> labels;

    vector<string> sentenceTags;
    for(int curl = 0; curl < sentenceLength; curl++)
    {
      sentenceTags.push_back("_");
    }

    for(int curl = 0; curl < sentenceLength; curl++)
    {
      clock_t lt_start=clock();
      pipe.initSentenceFeatures(fvs, instance->sentence, sentenceTags, curl);
      clock_t lt_feature=clock();
      feature_extract_time += lt_feature - lt_start;

		  int curSegLabelTagId = svm.svm_predict(fvs[curl]);
      string curSegLabelTag = pipe.m_vecTypes[curSegLabelTagId];
      sentenceTags[curl] = curSegLabelTag;

      if(options.m_bPostager)
      {
        if(curl == 0 || instance->sentenceTags[curl].substr(0,2) == "B-")
        {
          words.push_back(instance->sentence[curl]);
          labels.push_back(curSegLabelTag.substr(2));
        }
        else
        {
          int curWordLength = words.size();
          words[curWordLength-1] = words[curWordLength-1] + instance->sentence[curl];
        }
      }
      else
      {
        if(curl == 0 || curSegLabelTag.substr(0,2) == "B-")
        {
          words.push_back(instance->sentence[curl]);
          labels.push_back(curSegLabelTag.substr(2));
        }
        else
        {
          int curWordLength = words.size();
          words[curWordLength-1] = words[curWordLength-1] + instance->sentence[curl];
        }
      }
      clock_t lt_decode=clock();
      decode_time += lt_decode - lt_feature;     
    }

    
    clock_t lt_eval_start=clock();
    vector<int>  evalRes;
    pipe.reco(*instance, words, labels, sentenceTags, evalRes); 
		totalGoldWords += evalRes[0]; totalPredWords += evalRes[1];
		totalGoldIVWords += evalRes[2]; totalPredIVWords += evalRes[3];
		totalGoldOOVWords += evalRes[4]; totalPredOOVWords += evalRes[5];
		
		totalRecoWords += evalRes[6]; totalRecoIVWords += evalRes[7];totalRecoOOVWords += evalRes[8];
		correctLabels += evalRes[9]; correctLabelsIV += evalRes[10]; correctLabelsOOV += evalRes[11];

    correctTags += evalRes[13];  totalTags += evalRes[12]; 
		
		if(evalRes[9] == instance->words.size())
		{
		    corrsentL++;
		}
		
		if(evalRes[6] == instance->words.size())
		{
		    corrsent++;
		}
		
		numsent++;

    


    if(++cnt % options.m_display_interval == 0)
    {
      cout << cnt << endl;
      cout << "SEG_AC: R=" << totalRecoWords << "/" << totalGoldWords << "=" << totalRecoWords*1.0/totalGoldWords
           << " P=" << totalRecoWords << "/" << totalPredWords << "=" << totalRecoWords*1.0/totalPredWords
           << " F=" << totalRecoWords*2.0/(totalGoldWords +totalPredWords) << endl;

      cout << "SEG_IV_AC: R=" << totalRecoIVWords << "/" << totalGoldIVWords << "=" << totalRecoIVWords*1.0/totalGoldIVWords
           << " P=" << totalRecoIVWords << "/" << totalPredIVWords << "=" << totalRecoIVWords*1.0/totalPredIVWords
           << " F=" << totalRecoIVWords*2.0/(totalGoldIVWords +totalPredIVWords) << endl;
    
      cout << "SEG_OOV_AC: R=" << totalRecoOOVWords << "/" << totalGoldOOVWords << "=" << totalRecoOOVWords*1.0/totalGoldOOVWords
           << " P=" << totalRecoOOVWords << "/" << totalPredOOVWords << "=" << totalRecoOOVWords*1.0/totalPredOOVWords
           << " F=" << totalRecoOOVWords*2.0/(totalGoldOOVWords +totalPredOOVWords) << endl;

      cout << "LABEL_AC: R=" << correctLabels << "/" << totalGoldWords << "=" << correctLabels*1.0/totalGoldOOVWords
           << " P=" << correctLabels << "/" << totalPredWords << "=" << correctLabels*1.0/totalPredOOVWords
           << " F=" << correctLabels*2.0/(totalGoldWords +totalPredWords) << endl;

      cout << "LABEL_IV_AC: R=" << correctLabelsIV << "/" << totalGoldIVWords << "=" << correctLabelsIV*1.0/totalGoldIVWords
           << " P=" << correctLabelsIV << "/" << totalPredIVWords << "=" << correctLabelsIV*1.0/totalPredIVWords
           << " F=" << correctLabelsIV*2.0/(totalGoldIVWords +totalPredIVWords) << endl;

      cout << "LABEL_OOV_AC: R=" << correctLabelsOOV << "/" << totalGoldOOVWords << "=" << correctLabelsOOV*1.0/totalGoldOOVWords
           << " P=" << correctLabelsOOV << "/" << totalPredOOVWords << "=" << correctLabelsOOV*1.0/totalPredOOVWords
           << " F=" << correctLabelsOOV*2.0/(totalGoldOOVWords +totalPredOOVWords) << endl;

      cout << "SENT_SEG_AC: " << corrsent << "/" << numsent << "=" << corrsent*1.0/numsent
           << " SENT_SEG&Label_AC: " << corrsentL << "/" << numsent << "=" << corrsentL*1.0/numsent << endl;

      cout << "TAG_AC: " << correctTags << "/" << totalTags << "=" << correctTags*1.0/totalTags << endl;
    }
    clock_t lt_evaluate=clock();
    evaluate_time += lt_evaluate - lt_eval_start;

		if ( options.m_numMaxInstance > 0 && cnt == options.m_numMaxInstance) break;
		instance = pipe.nextInstance();
	}

	{
    cout << cnt << endl;
    cout << "SEG_AC: R=" << totalRecoWords << "/" << totalGoldWords << "=" << totalRecoWords*1.0/totalGoldWords
         << " P=" << totalRecoWords << "/" << totalPredWords << "=" << totalRecoWords*1.0/totalPredWords
         << " F=" << totalRecoWords*2.0/(totalGoldWords +totalPredWords) << endl;

    cout << "SEG_IV_AC: R=" << totalRecoIVWords << "/" << totalGoldIVWords << "=" << totalRecoIVWords*1.0/totalGoldIVWords
         << " P=" << totalRecoIVWords << "/" << totalPredIVWords << "=" << totalRecoIVWords*1.0/totalPredIVWords
         << " F=" << totalRecoIVWords*2.0/(totalGoldIVWords +totalPredIVWords) << endl;
  
    cout << "SEG_OOV_AC: R=" << totalRecoOOVWords << "/" << totalGoldOOVWords << "=" << totalRecoOOVWords*1.0/totalGoldOOVWords
         << " P=" << totalRecoOOVWords << "/" << totalPredOOVWords << "=" << totalRecoOOVWords*1.0/totalPredOOVWords
         << " F=" << totalRecoOOVWords*2.0/(totalGoldOOVWords +totalPredOOVWords) << endl;

    cout << "LABEL_AC: R=" << correctLabels << "/" << totalGoldWords << "=" << correctLabels*1.0/totalGoldWords
         << " P=" << correctLabels << "/" << totalPredWords << "=" << correctLabels*1.0/totalPredWords
         << " F=" << correctLabels*2.0/(totalGoldWords +totalPredWords) << endl;

    cout << "LABEL_IV_AC: R=" << correctLabelsIV << "/" << totalGoldIVWords << "=" << correctLabelsIV*1.0/totalGoldIVWords
         << " P=" << correctLabelsIV << "/" << totalPredIVWords << "=" << correctLabelsIV*1.0/totalPredIVWords
         << " F=" << correctLabelsIV*2.0/(totalGoldIVWords +totalPredIVWords) << endl;

    cout << "LABEL_OOV_AC: R=" << correctLabelsOOV << "/" << totalGoldOOVWords << "=" << correctLabelsOOV*1.0/totalGoldOOVWords
         << " P=" << correctLabelsOOV << "/" << totalPredOOVWords << "=" << correctLabelsOOV*1.0/totalPredOOVWords
         << " F=" << correctLabelsOOV*2.0/(totalGoldOOVWords +totalPredOOVWords) << endl;

    cout << "SENT_SEG_AC: " << corrsent << "/" << numsent << "=" << corrsent*1.0/numsent
         << " SENT_SEG&Label_AC: " << corrsentL << "/" << numsent << "=" << corrsentL*1.0/numsent << endl;

    cout << "TAG_AC: " << correctTags << "/" << totalTags << "=" << correctTags*1.0/totalTags << endl;

   }

	pipe.uninitInputFile();
  print_time();
  cout << "feature extract time:" << feature_extract_time << " decode time:" << decode_time << "evaluate time" << evaluate_time << endl;

	cout << "\ninstance num: " << cnt << endl;
	cout << "Finish testing on file " << devFile << "." << endl;
	cout << endl;
	return correctLabels*2.0/(totalGoldWords +totalPredWords);
}



double JSTParser::evaluate(vector<SVMInterFace> curSvm, vector<set<int> > exampleIds, string outputFile)
{
  print_time();

	cout << "Start nfold evaluating on file " << options.m_strTrainFile << ":" << endl;
  vector<int> exampleSvmSeq;
  exampleSvmSeq.resize(pipe.m_vecInstances.size());
  for(int curIndex=0; curIndex < exampleIds.size();curIndex++)
  {
    for(set<int>::iterator cur_iter = exampleIds[curIndex].begin(); cur_iter != exampleIds[curIndex].end(); cur_iter++)
    {
      exampleSvmSeq[*cur_iter] = curIndex;
    }
  }

	int totalPredWords = 0; int totalRecoWords = 0; int totalGoldWords = 0;
	int totalPredIVWords = 0; int totalRecoIVWords = 0; int totalGoldIVWords = 0;
	int totalPredOOVWords = 0; int totalRecoOOVWords = 0; int totalGoldOOVWords = 0;
	int numsent = 0; int corrsent = 0; int corrsentL = 0;
	int correctLabels = 0; int correctLabelsIV = 0; int correctLabelsOOV = 0;
  int correctTags = 0; int totalTags = 0;
  int feature_extract_time = 0;
  int decode_time = 0;
  //int tag2st_time = 0;
  int evaluate_time = 0;

  int cnt = 0;
  ofstream eval_outf;
  ostringstream out;   
  eval_outf.open(outputFile.c_str());
	if (!eval_outf) {
		cout << "JSTParser::startWriting evaluating result result open file err:" << endl;
	}

	for(int examId=0;examId<pipe.m_vecInstances.size();examId++)
  {
    JSTInstance* instance = &(pipe.m_vecInstances[examId]);

    vector<FeatureVec> fvs;   
    int sentenceLength = instance->sentence.size();
    fvs.resize(sentenceLength);

    vector<string> words;
    vector<string> labels;

    vector<string> sentenceTags;
    for(int curl = 0; curl < sentenceLength; curl++)
    {
      sentenceTags.push_back("_");
    }
    
    for(int curl = 0; curl < sentenceLength; curl++)
    {
      clock_t lt_start=clock();
      pipe.initSentenceFeatures(fvs, instance->sentence, sentenceTags, curl);
      clock_t lt_feature=clock();
      feature_extract_time += lt_feature - lt_start;

		  int curSegLabelTagId = curSvm[exampleSvmSeq[examId]].svm_predict(fvs[curl]);
      string curSegLabelTag = pipe.m_vecTypes[curSegLabelTagId];
      sentenceTags[curl] = curSegLabelTag;

      if(options.m_bPostager)
      {
        if(curl == 0 || instance->sentenceTags[curl].substr(0,2) == "B-")
        {
          words.push_back(instance->sentence[curl]);
          labels.push_back(curSegLabelTag.substr(2));
        }
        else
        {
          int curWordLength = words.size();
          words[curWordLength-1] = words[curWordLength-1] + instance->sentence[curl];
        }
      }
      else
      {
        if(curl == 0 || curSegLabelTag.substr(0,2) == "B-")
        {
          words.push_back(instance->sentence[curl]);
          labels.push_back(curSegLabelTag.substr(2));
        }
        else
        {
          int curWordLength = words.size();
          words[curWordLength-1] = words[curWordLength-1] + instance->sentence[curl];
        }
      }
      clock_t lt_decode=clock();
      decode_time += lt_decode - lt_feature;     
    }

    for (int curl = 0; curl < instance->words.size(); ++curl) {
      eval_outf << instance->words[curl] << "_" << instance->labels[curl] << " ";
	  }
	  eval_outf << endl;

    for (int curl = 0; curl < sentenceLength; ++curl) {
      eval_outf << instance->sentence[curl] << "_" << sentenceTags[curl] << " ";
	  }
	  eval_outf << endl;
    eval_outf.flush();
  
    clock_t lt_eval_start=clock();
    vector<int>  evalRes;
    pipe.reco(*instance, words, labels, sentenceTags, evalRes); 
		totalGoldWords += evalRes[0]; totalPredWords += evalRes[1];
		totalGoldIVWords += evalRes[2]; totalPredIVWords += evalRes[3];
		totalGoldOOVWords += evalRes[4]; totalPredOOVWords += evalRes[5];
		
		totalRecoWords += evalRes[6]; totalRecoIVWords += evalRes[7];totalRecoOOVWords += evalRes[8];
		correctLabels += evalRes[9]; correctLabelsIV += evalRes[10]; correctLabelsOOV += evalRes[11];

    correctTags += evalRes[13];  totalTags += evalRes[12]; 
		
		if(evalRes[9] == instance->words.size())
		{
		    corrsentL++;
		}
		
		if(evalRes[6] == instance->words.size())
		{
		    corrsent++;
		}
		
		numsent++;


    if(++cnt % options.m_display_interval == 0)
    {
      cout << cnt << endl;
      cout << "SEG_AC: R=" << totalRecoWords << "/" << totalGoldWords << "=" << totalRecoWords*1.0/totalGoldWords
           << " P=" << totalRecoWords << "/" << totalPredWords << "=" << totalRecoWords*1.0/totalPredWords
           << " F=" << totalRecoWords*2.0/(totalGoldWords +totalPredWords) << endl;

      cout << "SEG_IV_AC: R=" << totalRecoIVWords << "/" << totalGoldIVWords << "=" << totalRecoIVWords*1.0/totalGoldIVWords
           << " P=" << totalRecoIVWords << "/" << totalPredIVWords << "=" << totalRecoIVWords*1.0/totalPredIVWords
           << " F=" << totalRecoIVWords*2.0/(totalGoldIVWords +totalPredIVWords) << endl;
    
      cout << "SEG_OOV_AC: R=" << totalRecoOOVWords << "/" << totalGoldOOVWords << "=" << totalRecoOOVWords*1.0/totalGoldOOVWords
           << " P=" << totalRecoOOVWords << "/" << totalPredOOVWords << "=" << totalRecoOOVWords*1.0/totalPredOOVWords
           << " F=" << totalRecoOOVWords*2.0/(totalGoldOOVWords +totalPredOOVWords) << endl;

      cout << "LABEL_AC: R=" << correctLabels << "/" << totalGoldWords << "=" << correctLabels*1.0/totalGoldOOVWords
           << " P=" << correctLabels << "/" << totalPredWords << "=" << correctLabels*1.0/totalPredOOVWords
           << " F=" << correctLabels*2.0/(totalGoldWords +totalPredWords) << endl;

      cout << "LABEL_IV_AC: R=" << correctLabelsIV << "/" << totalGoldIVWords << "=" << correctLabelsIV*1.0/totalGoldIVWords
           << " P=" << correctLabelsIV << "/" << totalPredIVWords << "=" << correctLabelsIV*1.0/totalPredIVWords
           << " F=" << correctLabelsIV*2.0/(totalGoldIVWords +totalPredIVWords) << endl;

      cout << "LABEL_OOV_AC: R=" << correctLabelsOOV << "/" << totalGoldOOVWords << "=" << correctLabelsOOV*1.0/totalGoldOOVWords
           << " P=" << correctLabelsOOV << "/" << totalPredOOVWords << "=" << correctLabelsOOV*1.0/totalPredOOVWords
           << " F=" << correctLabelsOOV*2.0/(totalGoldOOVWords +totalPredOOVWords) << endl;

      cout << "SENT_SEG_AC: " << corrsent << "/" << numsent << "=" << corrsent*1.0/numsent
           << " SENT_SEG&Label_AC: " << corrsentL << "/" << numsent << "=" << corrsentL*1.0/numsent << endl;

      cout << "TAG_AC: " << correctTags << "/" << totalTags << "=" << correctTags*1.0/totalTags << endl;
    }
    clock_t lt_evaluate=clock();
    evaluate_time += lt_evaluate - lt_eval_start;

		if ( options.m_numMaxInstance > 0 && cnt == options.m_numMaxInstance) break;
	}

	{
    cout << cnt << endl;
    cout << "SEG_AC: R=" << totalRecoWords << "/" << totalGoldWords << "=" << totalRecoWords*1.0/totalGoldWords
         << " P=" << totalRecoWords << "/" << totalPredWords << "=" << totalRecoWords*1.0/totalPredWords
         << " F=" << totalRecoWords*2.0/(totalGoldWords +totalPredWords) << endl;

    cout << "SEG_IV_AC: R=" << totalRecoIVWords << "/" << totalGoldIVWords << "=" << totalRecoIVWords*1.0/totalGoldIVWords
         << " P=" << totalRecoIVWords << "/" << totalPredIVWords << "=" << totalRecoIVWords*1.0/totalPredIVWords
         << " F=" << totalRecoIVWords*2.0/(totalGoldIVWords +totalPredIVWords) << endl;
  
    cout << "SEG_OOV_AC: R=" << totalRecoOOVWords << "/" << totalGoldOOVWords << "=" << totalRecoOOVWords*1.0/totalGoldOOVWords
         << " P=" << totalRecoOOVWords << "/" << totalPredOOVWords << "=" << totalRecoOOVWords*1.0/totalPredOOVWords
         << " F=" << totalRecoOOVWords*2.0/(totalGoldOOVWords +totalPredOOVWords) << endl;

    cout << "LABEL_AC: R=" << correctLabels << "/" << totalGoldWords << "=" << correctLabels*1.0/totalGoldWords
         << " P=" << correctLabels << "/" << totalPredWords << "=" << correctLabels*1.0/totalPredWords
         << " F=" << correctLabels*2.0/(totalGoldWords +totalPredWords) << endl;

    cout << "LABEL_IV_AC: R=" << correctLabelsIV << "/" << totalGoldIVWords << "=" << correctLabelsIV*1.0/totalGoldIVWords
         << " P=" << correctLabelsIV << "/" << totalPredIVWords << "=" << correctLabelsIV*1.0/totalPredIVWords
         << " F=" << correctLabelsIV*2.0/(totalGoldIVWords +totalPredIVWords) << endl;

    cout << "LABEL_OOV_AC: R=" << correctLabelsOOV << "/" << totalGoldOOVWords << "=" << correctLabelsOOV*1.0/totalGoldOOVWords
         << " P=" << correctLabelsOOV << "/" << totalPredOOVWords << "=" << correctLabelsOOV*1.0/totalPredOOVWords
         << " F=" << correctLabelsOOV*2.0/(totalGoldOOVWords +totalPredOOVWords) << endl;

    cout << "SENT_SEG_AC: " << corrsent << "/" << numsent << "=" << corrsent*1.0/numsent
         << " SENT_SEG&Label_AC: " << corrsentL << "/" << numsent << "=" << corrsentL*1.0/numsent << endl;

    cout << "TAG_AC: " << correctTags << "/" << totalTags << "=" << correctTags*1.0/totalTags << endl;

   }

  print_time();
  cout << "feature extract time:" << feature_extract_time << " decode time:" << decode_time << "evaluate time" << evaluate_time << endl;

	cout << "\ninstance num: " << cnt << endl;

	cout << endl;
  eval_outf.close();
	return correctLabels*2.0/(totalGoldWords +totalPredWords);
}

void JSTParser::train_and_evalout(double c)
{
  //for(double c = 0.125; c <= 128 + EPS; c = c*2.0)
  //for(double c = 0.01; c <= 0.4 + EPS; c = c + 0.01)
  vector<SVMInterFace> curSvms;
  curSvms.resize(options.m_nFold);
  vector<set<int> > exampleIds;
  exampleIds.resize(options.m_nFold);
  int perDecodeNum  = (pipe.m_vecInstances.size() + options.m_nFold - 1)/options.m_nFold;

  for(int i = 0; i < pipe.m_vecInstances.size(); i++)
  {
    exampleIds[i/perDecodeNum].insert(i);
  }

  for(int iter = 0; iter < options.m_nFold; iter++)
  {
    curSvms[iter].initTrainParam(options);
    curSvms[iter].init_problems(pipe.m_vecInstances, pipe.m_featAlphabet.size(), exampleIds[iter]);  
    curSvms[iter].resetC(c);
		curSvms[iter].svm_train();
    //evaluate(options.m_strTrainFile,0);
    

    /*ostringstream tout;
		tout << c ;
		saveParamModel(options.m_strModelName.c_str(), tout.str().c_str());*/

	}
  cout << "model " << c << "finished" << endl;
  evaluate(curSvms, exampleIds, options.m_strOutFile);

  for(int iter = 0; iter < options.m_nFold; iter++)
  {
    curSvms[iter].free_model();
  }

	//cout << "The best model is " << bestModel << endl;

}


double JSTParser::evaluate(string inputFile, string outputFile)
{
  if (0 > pipe.initInputFile(inputFile.c_str())) return 0;
  print_time();

	cout << "Start evaluating on file " << inputFile << ":" << endl;
	JSTInstance *instance = pipe.nextInstance();


	int totalPredWords = 0; int totalRecoWords = 0; int totalGoldWords = 0;
	int totalPredIVWords = 0; int totalRecoIVWords = 0; int totalGoldIVWords = 0;
	int totalPredOOVWords = 0; int totalRecoOOVWords = 0; int totalGoldOOVWords = 0;
	int numsent = 0; int corrsent = 0; int corrsentL = 0;
	int correctLabels = 0; int correctLabelsIV = 0; int correctLabelsOOV = 0;
  int correctTags = 0; int totalTags = 0;
  int feature_extract_time = 0;
  int decode_time = 0;
  //int tag2st_time = 0;
  int evaluate_time = 0;

  int cnt = 0;
  ofstream eval_outf;
  ostringstream out;   
  eval_outf.open(outputFile.c_str());
	if (!eval_outf) {
		cout << "JSTParser::startWriting evaluating result result open file err:" << endl;
	}

	while(instance) {
  {
    vector<FeatureVec> fvs;   
    int sentenceLength = instance->sentence.size();
    fvs.resize(sentenceLength);

    vector<string> words;
    vector<string> labels;

    vector<string> sentenceTags;
    for(int curl = 0; curl < sentenceLength; curl++)
    {
      sentenceTags.push_back("_");
    }
    
    for(int curl = 0; curl < sentenceLength; curl++)
    {
      clock_t lt_start=clock();
      pipe.initSentenceFeatures(fvs, instance->sentence, sentenceTags, curl);
      clock_t lt_feature=clock();
      feature_extract_time += lt_feature - lt_start;

		  int curSegLabelTagId = svm.svm_predict(fvs[curl]);
      string curSegLabelTag = pipe.m_vecTypes[curSegLabelTagId];
      sentenceTags[curl] = curSegLabelTag;

      if(options.m_bPostager)
      {
        if(curl == 0 || instance->sentenceTags[curl].substr(0,2) == "B-")
        {
          words.push_back(instance->sentence[curl]);
          labels.push_back(curSegLabelTag.substr(2));
        }
        else
        {
          int curWordLength = words.size();
          words[curWordLength-1] = words[curWordLength-1] + instance->sentence[curl];
        }
      }
      else
      {
        if(curl == 0 || curSegLabelTag.substr(0,2) == "B-")
        {
          words.push_back(instance->sentence[curl]);
          labels.push_back(curSegLabelTag.substr(2));
        }
        else
        {
          int curWordLength = words.size();
          words[curWordLength-1] = words[curWordLength-1] + instance->sentence[curl];
        }
      }
      clock_t lt_decode=clock();
      decode_time += lt_decode - lt_feature;     
    }

    for (int curl = 0; curl < instance->words.size(); ++curl) {
      eval_outf << instance->words[curl] << "_" << instance->labels[curl] << " ";
	  }
	  eval_outf << endl;

    for (int curl = 0; curl < sentenceLength; ++curl) {
      eval_outf << instance->sentence[curl] << "_" << sentenceTags[curl] << " ";
	  }
	  eval_outf << endl;
    eval_outf.flush();
  
    clock_t lt_eval_start=clock();
    vector<int>  evalRes;
    pipe.reco(*instance, words, labels, sentenceTags, evalRes); 
		totalGoldWords += evalRes[0]; totalPredWords += evalRes[1];
		totalGoldIVWords += evalRes[2]; totalPredIVWords += evalRes[3];
		totalGoldOOVWords += evalRes[4]; totalPredOOVWords += evalRes[5];
		
		totalRecoWords += evalRes[6]; totalRecoIVWords += evalRes[7];totalRecoOOVWords += evalRes[8];
		correctLabels += evalRes[9]; correctLabelsIV += evalRes[10]; correctLabelsOOV += evalRes[11];

    correctTags += evalRes[13];  totalTags += evalRes[12]; 
		
		if(evalRes[9] == instance->words.size())
		{
		    corrsentL++;
		}
		
		if(evalRes[6] == instance->words.size())
		{
		    corrsent++;
		}
		
		numsent++;


    if(++cnt % options.m_display_interval == 0)
    {
      cout << cnt << endl;
      cout << "SEG_AC: R=" << totalRecoWords << "/" << totalGoldWords << "=" << totalRecoWords*1.0/totalGoldWords
           << " P=" << totalRecoWords << "/" << totalPredWords << "=" << totalRecoWords*1.0/totalPredWords
           << " F=" << totalRecoWords*2.0/(totalGoldWords +totalPredWords) << endl;

      cout << "SEG_IV_AC: R=" << totalRecoIVWords << "/" << totalGoldIVWords << "=" << totalRecoIVWords*1.0/totalGoldIVWords
           << " P=" << totalRecoIVWords << "/" << totalPredIVWords << "=" << totalRecoIVWords*1.0/totalPredIVWords
           << " F=" << totalRecoIVWords*2.0/(totalGoldIVWords +totalPredIVWords) << endl;
    
      cout << "SEG_OOV_AC: R=" << totalRecoOOVWords << "/" << totalGoldOOVWords << "=" << totalRecoOOVWords*1.0/totalGoldOOVWords
           << " P=" << totalRecoOOVWords << "/" << totalPredOOVWords << "=" << totalRecoOOVWords*1.0/totalPredOOVWords
           << " F=" << totalRecoOOVWords*2.0/(totalGoldOOVWords +totalPredOOVWords) << endl;

      cout << "LABEL_AC: R=" << correctLabels << "/" << totalGoldWords << "=" << correctLabels*1.0/totalGoldOOVWords
           << " P=" << correctLabels << "/" << totalPredWords << "=" << correctLabels*1.0/totalPredOOVWords
           << " F=" << correctLabels*2.0/(totalGoldWords +totalPredWords) << endl;

      cout << "LABEL_IV_AC: R=" << correctLabelsIV << "/" << totalGoldIVWords << "=" << correctLabelsIV*1.0/totalGoldIVWords
           << " P=" << correctLabelsIV << "/" << totalPredIVWords << "=" << correctLabelsIV*1.0/totalPredIVWords
           << " F=" << correctLabelsIV*2.0/(totalGoldIVWords +totalPredIVWords) << endl;

      cout << "LABEL_OOV_AC: R=" << correctLabelsOOV << "/" << totalGoldOOVWords << "=" << correctLabelsOOV*1.0/totalGoldOOVWords
           << " P=" << correctLabelsOOV << "/" << totalPredOOVWords << "=" << correctLabelsOOV*1.0/totalPredOOVWords
           << " F=" << correctLabelsOOV*2.0/(totalGoldOOVWords +totalPredOOVWords) << endl;

      cout << "SENT_SEG_AC: " << corrsent << "/" << numsent << "=" << corrsent*1.0/numsent
           << " SENT_SEG&Label_AC: " << corrsentL << "/" << numsent << "=" << corrsentL*1.0/numsent << endl;

      cout << "TAG_AC: " << correctTags << "/" << totalTags << "=" << correctTags*1.0/totalTags << endl;
    }
    clock_t lt_evaluate=clock();
    evaluate_time += lt_evaluate - lt_eval_start;

		if ( options.m_numMaxInstance > 0 && cnt == options.m_numMaxInstance) break;
    instance = pipe.nextInstance();
	}
	}

	{
    cout << cnt << endl;
    cout << "SEG_AC: R=" << totalRecoWords << "/" << totalGoldWords << "=" << totalRecoWords*1.0/totalGoldWords
         << " P=" << totalRecoWords << "/" << totalPredWords << "=" << totalRecoWords*1.0/totalPredWords
         << " F=" << totalRecoWords*2.0/(totalGoldWords +totalPredWords) << endl;

    cout << "SEG_IV_AC: R=" << totalRecoIVWords << "/" << totalGoldIVWords << "=" << totalRecoIVWords*1.0/totalGoldIVWords
         << " P=" << totalRecoIVWords << "/" << totalPredIVWords << "=" << totalRecoIVWords*1.0/totalPredIVWords
         << " F=" << totalRecoIVWords*2.0/(totalGoldIVWords +totalPredIVWords) << endl;
  
    cout << "SEG_OOV_AC: R=" << totalRecoOOVWords << "/" << totalGoldOOVWords << "=" << totalRecoOOVWords*1.0/totalGoldOOVWords
         << " P=" << totalRecoOOVWords << "/" << totalPredOOVWords << "=" << totalRecoOOVWords*1.0/totalPredOOVWords
         << " F=" << totalRecoOOVWords*2.0/(totalGoldOOVWords +totalPredOOVWords) << endl;

    cout << "LABEL_AC: R=" << correctLabels << "/" << totalGoldWords << "=" << correctLabels*1.0/totalGoldWords
         << " P=" << correctLabels << "/" << totalPredWords << "=" << correctLabels*1.0/totalPredWords
         << " F=" << correctLabels*2.0/(totalGoldWords +totalPredWords) << endl;

    cout << "LABEL_IV_AC: R=" << correctLabelsIV << "/" << totalGoldIVWords << "=" << correctLabelsIV*1.0/totalGoldIVWords
         << " P=" << correctLabelsIV << "/" << totalPredIVWords << "=" << correctLabelsIV*1.0/totalPredIVWords
         << " F=" << correctLabelsIV*2.0/(totalGoldIVWords +totalPredIVWords) << endl;

    cout << "LABEL_OOV_AC: R=" << correctLabelsOOV << "/" << totalGoldOOVWords << "=" << correctLabelsOOV*1.0/totalGoldOOVWords
         << " P=" << correctLabelsOOV << "/" << totalPredOOVWords << "=" << correctLabelsOOV*1.0/totalPredOOVWords
         << " F=" << correctLabelsOOV*2.0/(totalGoldOOVWords +totalPredOOVWords) << endl;

    cout << "SENT_SEG_AC: " << corrsent << "/" << numsent << "=" << corrsent*1.0/numsent
         << " SENT_SEG&Label_AC: " << corrsentL << "/" << numsent << "=" << corrsentL*1.0/numsent << endl;

    cout << "TAG_AC: " << correctTags << "/" << totalTags << "=" << correctTags*1.0/totalTags << endl;

   }

  print_time();
  cout << "feature extract time:" << feature_extract_time << " decode time:" << decode_time << "evaluate time" << evaluate_time << endl;

	cout << "\ninstance num: " << cnt << endl;

	cout << endl;
  eval_outf.close();
	return correctLabels*2.0/(totalGoldWords +totalPredWords);
}


