#ifndef _JST_READER_
#define _JST_READER_

#pragma once

#include <fstream>
#include <iostream>
using namespace std;

#include "JSTInstance.h"

class JSTReader
{
public:
	JSTReader();
	virtual ~JSTReader();
	int startReading(const char *filename) {
		if (m_inf.is_open()) {
			m_inf.close();
			m_inf.clear();
		}
		m_inf.open(filename);

    if (!m_inf.is_open()) {
			cout << "JSTReader::startReading() open file err: " << filename << endl;
			return -1;
		}

		return 0;
	}

	void finishReading() {
		if (m_inf.is_open()) {
			m_inf.close();
			m_inf.clear();
		}
	}

	string normalize(const string &str);
	virtual JSTInstance *getNext() = 0;
  virtual bool getNextRawSentence(vector<string>& sentence) = 0;
  virtual bool getNextRawSentence(string& strLine) = 0;
protected:
	ifstream m_inf;

	int m_numInstance;

	JSTInstance m_instance;
};

#endif

