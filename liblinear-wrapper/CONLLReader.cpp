#include "CONLLReader.h"
#include "MyLib.h"
#include "utf.h"

#include <sstream>
using namespace std;

CONLLReader::CONLLReader(void)
{
}

CONLLReader::~CONLLReader(void)
{
}

JSTInstance *CONLLReader::getNext()
{
  m_instance.sentence.clear();
	m_instance.fvs.clear();
	m_instance.words.clear();
  m_instance.labels.clear();
  /*for(int i = 0; i < m_instance.features.size(); i++)
  {
    m_instance.features[i].clear();
    m_instance.features.clear();
  }*/
  m_instance.sentenceTags.clear();

  string strLine;

	while (my_getline(m_inf, strLine)) {
		if (strLine.empty())
    {
      continue;
    }
		break;
	}
  vector<string> vecInfo;
	split_bychar(strLine, vecInfo, ' ');
  int length = vecInfo.size();

  m_instance.words.resize(length);
  m_instance.labels.resize(length);
  

  for (int i = 0; i < length; ++i) {
    int lastUnderLineIndex = vecInfo[i].find_last_of('_');
    m_instance.words[i] = vecInfo[i].substr(0, lastUnderLineIndex);
    m_instance.labels[i] = vecInfo[i].substr(lastUnderLineIndex+1);
    vector<string> sentence;
    getCharactersFromUTF8String(m_instance.words[i], &sentence);
    
    for(int j = 0; j < sentence.size();j++)
    {
      m_instance.sentence.push_back(sentence[j]);
      
      
      if(j == 0)
      {
        m_instance.sentenceTags.push_back("B-" + m_instance.labels[i]);
      }
      else
      {
        m_instance.sentenceTags.push_back("I-" + m_instance.labels[i]);
      }
      
      /*if(j == 0 && sentence.size() == 1)
      {
        m_instance.sentenceTags.push_back("S-" + m_instance.labels[i]);
      }
      else if( j == 0 && sentence.size() > 1)
      {
        m_instance.sentenceTags.push_back("B-" + m_instance.labels[i]);
      }
      else if(j > 0 && j < sentence.size()-1)
      {
        m_instance.sentenceTags.push_back("M-" + m_instance.labels[i]);
      }
      else
      {
        m_instance.sentenceTags.push_back("E-" + m_instance.labels[i]);
      }*/
    }


  
  }

	return &m_instance;
}

bool CONLLReader::getNextRawSentence(vector<string>& sentence)
{
  string strLine;
  sentence.clear();
	while (my_getline(m_inf, strLine)) {
		if (strLine.empty() ) continue;
    string rawSentence = "";
    vector<string> vecInfo;
	  split_bychar(strLine, vecInfo, ' ');
    int length = vecInfo.size();
    for (int i = 0; i < length; ++i) {
      int lastUnderLineIndex = vecInfo[i].find_last_of('_');
      if(lastUnderLineIndex >= 0 && lastUnderLineIndex < vecInfo[i].length())
      {
        rawSentence = rawSentence + vecInfo[i].substr(0, lastUnderLineIndex);
      }
      else
      {
         rawSentence = rawSentence + vecInfo[i];
      }
    }

    getCharactersFromUTF8String(rawSentence, &sentence);
		return true;
	}

  return false;
}

bool CONLLReader::getNextRawSentence(string& strLine)
{
	while (my_getline(m_inf, strLine)) {
		if (strLine.empty()) continue;
		return true;
	}
  return false;
}

