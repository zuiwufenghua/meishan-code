#ifndef _CONLL_WRITER_
#define _CONLL_WRITER_

#pragma once
#include "Writer.h"

/*
	this class writes conll-format result (no srl-info).
*/
class InstanceWriter : public Writer
{
public:
	InstanceWriter();
	~InstanceWriter();
	int write(const Instance *pInstance);
};

#endif

