#ifndef _DEP_PARSER_
#define _DEP_PARSER_

#pragma once
#include <vector>
#include <iostream>
#include <fstream>
using namespace std;

#include "Parameter.h"
#include "DepDecoder.h"
#include "ParserOptions.h"
#include "DepPipe.h"
#include "MyLib.h"



/*
	this class controls the parsing process.
*/

class DepParser
{

private:
	const ParserOptions &options;
	DepPipe &pipe;
	DepDecoder &decoder;
	Parameter params;  




public:
  DepParser(const ParserOptions &_options, DepPipe &_pipe, DepDecoder &_decoder) : options(_options), pipe(_pipe), decoder(_decoder), params(_pipe.m_featAlphabet.size(), _options)
	{ 

	}
	~DepParser(void) 
	{

	}


	void train(const vector<int> &instanceLengths ,int round = -1);

	void trainingIter(const vector<int> &instanceLengths, int iter);

	//////////////////////////////////////////////////////
	// Get Best Parses ///////////////////////////////////
	//////////////////////////////////////////////////////
	void evaluate();
	int parseSent(const vector<string> &vecWord,
				  const vector<string> &vecCPOS,
				  vector<int> &vecHead,
				  vector<string> &vecRel);


	int saveParamModel(const char *modelName, const char *paramModelIterNum);
	int saveAlphabetModel(const char *modelName);

	int loadParamModel(const char *modelPath, const char *modelName, const char *paramModelIterNum);
	int loadAlphabetModel(const char *modelPath, const char *modelName);



public:
  void featureReduction(const vector<int> &instanceLengths, int reduCount);
  void computeFeatureMutalInfo(const vector<int> &instanceLengths);
  void computeFeatureWeightInfo();
  void featureReductionTrain(const vector<int> &instanceLengths);
  double devTest(string devFile);	

};




#endif


