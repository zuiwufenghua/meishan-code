#ifndef _PARSER_OPTIONS_
#define _PARSER_OPTIONS_

#pragma once

#include <string>
#include <fstream>
#include <vector>
#include <iostream>
#include <set>
using namespace std;

/*
	this class implements global options for parser. include:
		2-order or 1-order
		prof or non-proj

		parameters:
			iter-num
			k-best
			feature-set
			...
*/

class ParserOptions
{
public:
	ParserOptions();
	int setOptions(const char *option_file);
	void setOptions(const vector<string> &vecOption);
	void showOptions();
	~ParserOptions();

public:
  int m_traingAlgorithm;
  int m_fsMethod;
//  double m_threshold;
	bool m_isTrain;
	string m_strTrainFile;
	int m_numIter;
	int m_trainK;
	string m_strTrain_IterNums_to_SaveParamModel;
	set<int> m_setTrain_IterNums_to_SaveParamModel;
	bool m_bSaveMemory;

	string m_strDevFile;
	int m_devK;


	string m_strTestFile;
	string m_strOutFile; // test & evaluate share a common output result file.

	bool m_isEvaluate;
	string m_strEvalFile;
	string m_strEvalOutFile;
	string m_strEvalErrGoldFile;
	int m_evalK;
	string m_strEval_IterNum_of_ParamModel;

	bool m_isUseSib;
	bool m_isLabeled;
	string m_strModelName;
	int m_numMaxInstance;
	bool m_isCONLLFormat;


	int m_display_interval;
//	int m_start_iter;
	int m_reduction_iter;
	int m_mute_info_k;
	double m_reduction_ratio;

	bool m_isUseForm;
	bool m_isUseLemma;
	bool m_isUsePostag;
	bool m_isUseCPostag;
	bool m_isUseFeats;

	bool m_isUseForm_label;
	bool m_isUseLemma_label;
	bool m_isUse_label_feats_t_child;
	bool m_isUse_label_feats_t;


	bool m_isUse_uni;
	bool m_isUse_bi;
	bool m_isUse_bet;
	bool m_isUse_sur;
  bool m_isUseSimp;

	bool m_bSentenceSimplification;
	string m_simpRuleFile;

  bool m_is_english;

	int m_threadnum;
//	bool m_isEval;
//	string m_strGoldFile;
};

#endif

