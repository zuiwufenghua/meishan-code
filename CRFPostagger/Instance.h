#ifndef _JST_INSTANCE_
#define _JST_INSTANCE_

#pragma once

#include <string>
#include <vector>
#include <fstream>
#include <algorithm>

using namespace std;

#include "MyLib.h"
/*
	this class implements the representation of parsing result of one sentence.

*/



class Instance
{
public:
	Instance() {}
	~Instance() {}

	int size() { return labels.size(); }

	void clear()
	{
			labels.clear();
			for(int i = 0; i < size(); i++)
			{
				features[i].clear();
			}
			features.clear();
	}

	void allocate(int length)
	{
			clear();
		  labels.resize(length);
			features.resize(length);
	}

	void copyValuesFrom(Instance anInstance)
	{
		allocate(anInstance.size());
		for(int i = 0; i < anInstance.size(); i++)
		{
				labels[i] = anInstance.labels[i];
				for(int j = 0; j < anInstance.features[i].size(); j++)
				{
						features[i].push_back(anInstance.features[i][j]);
				}
		}

	}

public:
	vector<string> labels;
	vector<vector<string> > features;
};

#endif

