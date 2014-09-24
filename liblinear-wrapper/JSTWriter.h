#ifndef _JST_WRITER_
#define _JST_WRITER_

#pragma once

#include <fstream>
#include <iostream>
using namespace std;

#include "JSTInstance.h"

class JSTWriter
{
public:
	JSTWriter();
	virtual ~JSTWriter();
	int startWriting(const char *filename) {
		m_outf.open(filename);
		if (!m_outf) {
			cout << "JSTWriterr::startWriting() open file err: " << filename << endl;
			return -1;
		}
		return 0;
	}

	void finishWriting() {
		m_outf.close();
	}

	virtual int write(const JSTInstance *pInstance) = 0;
protected:
	ofstream m_outf;
};

#endif

