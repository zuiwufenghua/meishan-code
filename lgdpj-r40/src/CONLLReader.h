#ifndef _CONLL_READER_
#define _CONLL_READER_

#pragma once

#include <fstream>
#include <iostream>
using namespace std;

#include "Instance.h"
#include "common.h"


namespace dparser {

	/*
	this class reads conll-format data (10 columns, no srl-info)
	*/

	class CONLLReader
	{
	public:
		CONLLReader() {}
		~CONLLReader() {}

		// return the number of words in the sentence, excluding W0
		Instance *getNext(const int id) {
			const int word_num = read_lines();
			if (word_num > 0) {
				Instance *inst = new Instance(id);
				decompose_sent(inst);
				return inst;
			} else {
				return 0;
			}
		}

	protected:
		void reset_sent(Instance * const inst, const int length);
		void decompose_sent(Instance * const inst);

		// return the number of words in the sentence, excluding W0
		int read_lines();

	public:

		int openFile(const char *filename) {
			if (m_inf.is_open()) {
				m_inf.close();
			}
			m_inf.open(filename);

			if (!m_inf.is_open()) {
				cerr << "CoNLLReader::openFile() err: " << filename << endl;
				return -1;
			}

			return 0;
		}

		void closeFile() {
			if (m_inf.is_open()) {
				m_inf.close();
			}
		}

	protected:
		ifstream m_inf;
		vector<string> m_vecLine;
	};

} // namespace dparser


#endif

