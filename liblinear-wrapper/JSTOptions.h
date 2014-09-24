#ifndef _PARSER_OPTIONS_
#define _PARSER_OPTIONS_

#pragma once

#include <string>
#include <fstream>
#include <vector>
#include <iostream>
#include <set>
using namespace std;


class JSTOptions
{
public:
	JSTOptions();
	int setOptions(const char *option_file);
	void setOptions(const vector<string> &vecOption);
	void showOptions();
	~JSTOptions();

public:
  int m_traingAlgorithm;

	bool m_isTrain;
	string m_strTrainFile;
  string m_strEvalFile;
	int m_numIter;
	int m_trainK;

	bool m_isTest;
  string m_strTestFile;
	string m_strOutFile; // test & evaluate share a common output result file.
  string m_strEval_IterNum_of_ParamModel;
  int m_nFold;


	string m_strModelName;

	int m_numMaxInstance;
	int m_display_interval;
  int m_featFreqCutNum;
  string m_strDictFile;

  bool m_bEvaluateOut;
  bool m_bPostager;


};

#endif

