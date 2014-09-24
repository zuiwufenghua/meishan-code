#ifndef _DEP_PIPE_
#define _DEP_PIPE_

#pragma once

#include <fstream>
#include <iostream>
#include <sstream>
#include <vector>
#include <string>
#include <map>
#include <algorithm>

using namespace std;

#include "Instance.h"
#include "CONLLReader.h"
#include "CONLLWriter.h"
#include "common.h"

#include "Options.h"
using namespace egstra;

namespace dparser {

	class IOPipe
	{
	private:
		vector<int> m_vecInstIdxToRead;
		vector<Instance *> m_instances;

		string m_inf_name;
		string m_outf_name;
		CONLLReader *m_reader;
		CONLLWriter *m_writer;


		bool _english;
		bool _use_lemma;
		bool _use_chars;

	public:
		IOPipe() : m_reader(0), m_writer(0) {}

		~IOPipe()
		{
			dealloc_instance();
			closeInputFile();
			closeOutputFile();
		}

		void dealloc_instance() {
			for (int i = 0; i < m_instances.size(); ++i) {
				assert(m_instances[i]);
				delete m_instances[i];
				m_instances[i] = 0;
			}
			m_instances.clear();
			m_vecInstIdxToRead.clear();
		}

		const string &in_file_name() const {
			return m_inf_name;
		}

		void process_options() {
			int tmp;
			

			_english = false;
			_use_lemma = false;
			_use_chars = true;
			if(options::get("english", tmp)) {
				_english = tmp;
			}
			if(options::get("use-lemma", tmp)) {
				_use_lemma = tmp;
			}
			if(options::get("use-chars", tmp)) {
				_use_chars = tmp;
			}

			if (_english) {
				assert(!_use_chars); // Both tagging and parsing models do not need "chars" for English
			} else {
				if (_use_lemma) {
					assert(_use_chars); // use the last character as lemma for Chinese
				}
			}

		}

		int openInputFile(const char *filename) {
			m_inf_name = filename;
			m_reader = new CONLLReader();
			if (!m_reader) {
				string str = "IOPipe::IOPipe() create reader error";
				cerr << str << endl;
				throw(str);
			}
			return m_reader->openFile(filename); 
		}

		void closeInputFile() {	
			if (m_reader) {
				m_reader->closeFile();
				delete m_reader;
				m_reader = 0;
			}
		}

		int openOutputFile(const char *filename) { 
			m_writer = new CONLLWriter();
			if (!m_writer) {
				string str = "IOPipe::IOPipe() create writer error";
				cerr << str << endl;
				throw(str);
			}
			return m_writer->openFile(filename);
		}

		void closeOutputFile() { 
			if (m_writer) {
				m_writer->closeFile(); 
				delete m_writer;
				m_writer = 0;
			}
		}

		void getInstancesFromInputFile(const int maxInstNum=-1, const int instMaxLen=-1);

		void shuffleTrainInstances() {
			random_shuffle(m_vecInstIdxToRead.begin(), m_vecInstIdxToRead.end());
		}

		void preprocessInstance( Instance *inst );

		int getInstanceNum() const {
			return m_instances.size();
		}

		Instance *getInstance(const int instIdx) {
			if (instIdx < 0 || instIdx >= m_instances.size()) {
				cerr << "\nIOPipe::getInstance instIdx range err: " << instIdx << endl;
				return 0;
			}
			return m_instances[ m_vecInstIdxToRead[instIdx] ];
		}

		void fillVecInstIdxToRead() {
			m_vecInstIdxToRead.clear();
			m_vecInstIdxToRead.resize(m_instances.size());
			for (int i = 0; i < m_instances.size(); ++i) m_vecInstIdxToRead[i] = i;
		}

		int writeInstance(const Instance *inst) {
			return m_writer->write(inst);
		}

    int writeInstance(const Instance *inst, const NRVec<const char *> &labels,  const double &thres) {
			return m_writer->write(inst, labels, thres);
		}
	};
}

#endif
