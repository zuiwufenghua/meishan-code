#ifndef _CONLL_WRITER_
#define _CONLL_WRITER_

#pragma once

#include "NRMat.h"
#include "FGen.h"

namespace dparser {

	/*
	this class writes conll-format result.
	*/

	class CONLLWriter
	{
	public:
		CONLLWriter() {}
		~CONLLWriter() {}

		int write(const Instance *inst);
    int write( const Instance *inst, const NRVec<const char *> &labels, const double &thres);

		int openFile(const char *filename) {
			if (m_outf.is_open()) {
				m_outf.close();
			}
			m_outf.open(filename);

			if (!m_outf.is_open()) {
				cerr << "CoNLLWriter::openFile() err: " << filename << endl;
				return -1;
			}

			return 0;
		}

		void closeFile() {
			if (m_outf.is_open()) {
				m_outf.close();
			}
		}

	protected:
		ofstream m_outf;
	};

}

#endif

