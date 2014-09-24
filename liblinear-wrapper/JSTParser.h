#ifndef _JST_PARSER_
#define _JST_PARSER_

#pragma once
#include <vector>
#include <iostream>
#include <fstream>
using namespace std;

#include "JSTOptions.h"
#include "JSTPipe.h"
#include "MyLib.h"
#include "SVMInterFace.h"



/*
	this class controls the parsing process.
*/

class JSTParser
{

private:
	const JSTOptions &options;
	JSTPipe &pipe;
  SVMInterFace svm;


public:
  JSTParser(const JSTOptions &_options, JSTPipe &_pipe) : options(_options), pipe(_pipe)
	{ 
    svm.initTrainParam(_options);
	}
	~JSTParser(void) 
	{
    svm.release();
	}

  void release_model()
  {
    svm.free_model();
  }


	void train();

  void train_and_evalout(double c);

	void trainingIter(int iter);

	//////////////////////////////////////////////////////
	// Get Best Parses ///////////////////////////////////
	//////////////////////////////////////////////////////
	double evaluate(string eval_file, int iter = 0);

  double evaluate(vector<SVMInterFace> curSvm, vector<set<int> > exampleIds, string outputFile);

  double evaluate(string inputFile, string outputFile);

  void decode(string decodeFile, string outputFile);

	int saveParamModel(const char *modelName, const char *paramModelIterNum);
	int saveAlphabetModel(const char *modelName);

	int loadParamModel(const char *modelPath, const char *modelName, const char *paramModelIterNum);
	int loadAlphabetModel(const char *modelPath, const char *modelName);

	
};


#endif


