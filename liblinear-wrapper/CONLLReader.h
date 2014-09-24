#ifndef _CONLL_READER_
#define _CONLL_READER_

#pragma once
#include "JSTReader.h"


/*
	this class reads conll-format data (10 columns, no srl-info)
*/
class CONLLReader : public JSTReader
{
public:
	CONLLReader();
	~CONLLReader();

	JSTInstance *getNext();

  bool getNextRawSentence(vector<string>& sentence);

  bool getNextRawSentence(string& strLine);
};

#endif

