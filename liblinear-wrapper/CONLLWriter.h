#ifndef _CONLL_WRITER_
#define _CONLL_WRITER_

#pragma once
#include "JSTWriter.h"

/*
	this class writes conll-format result (no srl-info).
*/
class CONLLWriter : public JSTWriter
{
public:
	CONLLWriter();
	~CONLLWriter();
	int write(const JSTInstance *pInstance);
};

#endif

