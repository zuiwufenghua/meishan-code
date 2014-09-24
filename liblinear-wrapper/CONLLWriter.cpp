#include "CONLLWriter.h"

#include <sstream>
using namespace std;

CONLLWriter::CONLLWriter()
{
}

CONLLWriter::~CONLLWriter()
{
}

int CONLLWriter::write(const JSTInstance *pInstance)
{
	if (!m_outf.is_open()) return -1;

	const vector<string> &words = pInstance->words;
	const vector<string> &labels = pInstance->labels;

	int i = 0;
	for (; i < words.size(); ++i) {
    m_outf << words[i] << "_" << labels[i] << " ";
	}
	m_outf << endl;

  const vector<string> &sentenceTags = pInstance->sentenceTags;
  const vector<string> &sentence = pInstance->sentence;
  
  string start_pos = sentenceTags[0].substr(2);
  int startIndex = 0;
	for (i = 1; i < sentence.size(); i++) {
    if(sentenceTags[i].substr(0,2) == "I-" && sentenceTags[i].substr(2) != start_pos)
    {
      m_outf << i << " not consistent with begin " << startIndex << "\tstart char:" << sentence[startIndex] << "\tstart_pos:" << start_pos << "\tcur char:" << sentence[i] << "\tcur pos:" << sentenceTags[i].substr(2) << endl;
    }
    if(sentenceTags[i].substr(0,2) == "B-")
    {
      start_pos = sentenceTags[i].substr(2);
      startIndex = i;
    }
	}


	return 0;
}

