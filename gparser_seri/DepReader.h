#ifndef _DEP_READER_
#define _DEP_READER_

#pragma once

#include <fstream>
#include <iostream>
using namespace std;

#include "DepInstance.h"

class DepReader
{
public:
	DepReader();
	virtual ~DepReader();
	int startReading(const char *filename) {
		if (m_inf.is_open()) {
/*			cout << endl;
			cout << ( m_inf.rdstate( ) & ios::badbit ) << endl;
			cout << ( m_inf.rdstate( ) & ios::failbit ) << endl;
			cout << ( m_inf.rdstate( ) & ios::eofbit ) << endl;
			cout << m_inf.good() << endl;
			cout << m_inf.bad() << endl;
			cout << m_inf.fail() << endl;
			cout << m_inf.eof() << endl;
			cout << endl;
*/			m_inf.close();
			m_inf.clear();
		}
		m_inf.open(filename);
/*		cout << endl;
		cout << ( m_inf.rdstate( ) & ios::badbit ) << endl;
		cout << ( m_inf.rdstate( ) & ios::failbit ) << endl;
		cout << ( m_inf.rdstate( ) & ios::eofbit ) << endl;
		cout << m_inf.good() << endl;
		cout << m_inf.bad() << endl;
		cout << m_inf.fail() << endl;
		cout << m_inf.eof() << endl;
		cout << endl;
*/		if (!m_inf.is_open()) {
			cout << "DepReader::startReading() open file err: " << filename << endl;
			return -1;
		}
//		m_inf.seekg(0, ios_base::beg);
		return 0;
	}

	void finishReading() {
		if (m_inf.is_open()) {
			m_inf.close();
			m_inf.clear();
		}
	}

	string normalize(const string &str);
	virtual DepInstance *getNext() = 0;
protected:
	ifstream m_inf;
//	bool m_isLabeled;
	int m_numInstance;

	DepInstance m_instance;
};

#endif

