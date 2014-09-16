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

#include "Instance.h"
#include "InstanceReader.h"
#include "InstanceWriter.h"

#include "common.h"

#define MAX_BUFFER_SIZE 256

class Pipe {
public:
	Pipe();
	~Pipe(void);

	void readTrainInstances(const string& m_strTrainFile);
	void readHoldInstances(const string& m_strEvalFile);

	int initInputFile(const char *filename);
	void uninitInputFile();
	int initOutputFile(const char *filename);
	void uninitOutputFile();

	int outputInstance(const Instance *pInstance);

	Instance *nextInstance();

public:

	vector<Instance> m_vecInstances;
	vector<Instance> m_vecDevInstances;

protected:
	Reader *m_jstReader;
	Writer *m_jstWriter;

};

#endif
