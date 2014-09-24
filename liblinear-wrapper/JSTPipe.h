#ifndef _JST_PIPE_
#define _JST_PIPE_

#pragma once

#include <fstream>
#include <iostream>
#include <sstream>
#include <vector>
#include <string>
#include <map>
using namespace std;

#include "JSTInstance.h"
#include "FeatureVec.h"
#include "Alphabet.h"
#include "CONLLReader.h"
#include "CONLLWriter.h"
#include "JSTOptions.h"
#include "MultiArray.h"

#include "common.h"
#include "utf.h"


#define MAX_BUFFER_SIZE 256


class JSTPipe
{
public:
	JSTPipe(const JSTOptions &_options);
	~JSTPipe(void);

  void initLexicon();

  void readTrainInstances();

	int initInputFile(const char *filename);
	void uninitInputFile();
	int initOutputFile(const char *filename);
	void uninitOutputFile();

	int outputInstance(const JSTInstance *pInstance);

	const char *getType(int typeIndex);

	JSTInstance *nextInstance();

  bool getNextRawSentence(vector<string>& sentence);

	int createAlphabet();
	void closeAlphabet();


public:

  void reco(const JSTInstance& inst, const vector<string>& predWords, const vector<string>& predLabels, const vector<string>& predSentenceTags, vector<int>& predRes);

  double computeLoss(const JSTInstance& inst, const vector<string>& predWords, const vector<string>& predLabels, const vector<string>& predSentenceTags);

  int initSentenceFeatures(vector<FeatureVec> &fvs, const vector<string>& sentence, const vector<string>& sentenceTags);

  int initSentenceFeatures(vector<FeatureVec> &fvs, const vector<string>& sentence, const vector<string>& sentenceTags, int i);

  void featureStatistic(map<string,int>& fvstat, const vector<string>& sentence, const vector<string>& sentenceTags);



protected:
	void mapTypes();

	int add(const string &feat, FeatureVec &fv);
  int add(const string &feat, double value, FeatureVec &fv);




public:
	Alphabet m_featAlphabet;
	Alphabet m_labelAlphabet;
  //成为模型的一部分
  map<string, set<string> > m_chseglabels;

	vector<string> m_vecTypes;
	vector<JSTInstance> m_vecInstances;
  int totalChars;

  int m_maxWordLength;

  set<string> m_dict;
  set<string> m_bidict;

  map<string, set<string> > m_bilexicon;


  map<string, set<string> > m_lexicon;
  map<string, set<string> > m_chlexicon;

  
    
  //for evaluate: iv and oov 
  map<string, set<string> > m_goldlexicon;

protected:
	const JSTOptions &options;
	JSTReader *m_jstReader;
	JSTWriter *m_jstWriter;

public:
 
  
//public:
// 	threadpool tp;
//	sp_thread_mutex_t * mut_iter;
//	sp_thread_mutex_t * mut_counter;
//	sp_thread_cond_t * cond_iter_over;
//	int counter_; // for one iter's actual handle num
//	int t_;       // for one iter's should handle num
//  static void fillFeatures(void* _arg);


};


#endif
