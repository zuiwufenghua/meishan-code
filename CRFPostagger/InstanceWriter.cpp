#include "InstanceWriter.h"

#include <sstream>
using namespace std;

InstanceWriter::InstanceWriter()
{
}

InstanceWriter::~InstanceWriter()
{
}

int InstanceWriter::write(const Instance *pInstance)
{
	if (!m_outf.is_open()) return -1;

	const vector<string> &labels = pInstance->labels;

	int i = 1;
	for (; i < labels.size(); ++i) {
    m_outf << labels[i] << endl;
	}
	m_outf << endl;
	return 0;
}

