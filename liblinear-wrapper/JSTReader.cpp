#include "JSTReader.h"

JSTReader::JSTReader(void)
{
}

JSTReader::~JSTReader(void)
{
	if (m_inf.is_open()) m_inf.close();
}

string JSTReader::normalize(const string &str)
{
	return str;
}

