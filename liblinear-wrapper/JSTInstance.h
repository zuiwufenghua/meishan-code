#ifndef _JST_INSTANCE_
#define _JST_INSTANCE_

#pragma once

#include <string>
#include <vector>
#include <fstream>
#include <algorithm>

using namespace std;

#include "FeatureVec.h"
#include "MyLib.h"
/*
	this class implements the representation of parsing result of one sentence.

*/



class JSTInstance
{
public:
	JSTInstance() {}
	~JSTInstance() {}

	int wordsize() { return words.size(); }
  int charLength(int _size) { sentence.size(); }


	void copyValuesFrom(const JSTInstance &jstInstance)
	{
		sentenceTags.resize(jstInstance.sentenceTags.size());
		copy(jstInstance.sentenceTags.begin(), jstInstance.sentenceTags.end(), sentenceTags.begin());

    sentence.resize(jstInstance.sentence.size());
		copy(jstInstance.sentence.begin(), jstInstance.sentence.end(), sentence.begin());
		
		words.resize(jstInstance.words.size());
		copy(jstInstance.words.begin(), jstInstance.words.end(), words.begin());

		labels.resize(jstInstance.labels.size());
		copy(jstInstance.labels.begin(), jstInstance.labels.end(), labels.begin());
		
	}




	void copyValuesTo(JSTInstance &jstInstance)
	{
		jstInstance.sentenceTags.resize(sentenceTags.size());
		copy(sentenceTags.begin(), sentenceTags.end(), jstInstance.sentenceTags.begin());

    jstInstance.sentence.resize(sentence.size());
		copy(sentence.begin(), sentence.end(), jstInstance.sentence.begin());
		
		jstInstance.words.resize(words.size());
		copy(words.begin(), words.end(), jstInstance.words.begin());

		jstInstance.labels.resize(labels.size());
		copy(labels.begin(), labels.end(), jstInstance.labels.begin());
		
	}



public:
	vector<FeatureVec> fvs;
  vector<string> sentence;
	vector<string> words;
	vector<string> labels;
  vector<string> sentenceTags;
  vector<int> sentenceTagIds;
  //vector<vector<string> > features;
	
};

#endif

