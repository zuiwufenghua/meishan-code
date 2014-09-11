#ifndef _CONLL_READER_
#define _CONLL_READER_

#pragma once
#include "Reader.h"


/*
	this class reads conll-format data (10 columns, no srl-info)
*/
class InstanceReader : public Reader
{
public:
	InstanceReader();
	~InstanceReader();

	Instance *getNext();
};

#endif

