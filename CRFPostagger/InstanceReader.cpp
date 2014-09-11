#include "InstanceReader.h"
#include "MyLib.h"
#include "utf.h"

#include <sstream>
using namespace std;

InstanceReader::InstanceReader(void)
{
}

InstanceReader::~InstanceReader(void)
{
}

Instance *InstanceReader::getNext()
{
  m_instance.labels.clear();
  m_instance.features.clear();

  string strLine;

	vector<string> vecLine;
	while (1) 
	{
			string strLine;
			if (!my_getline(m_inf, strLine)) {
						break;
			}
			if (strLine.empty()) break;
			vecLine.push_back(strLine);
	}

  int length = vecLine.size();

  m_instance.allocate(length);
  
  for (int i = 0; i < length; ++i) {
		vector<string> vecInfo;
		split_bychar(vecLine[i], vecInfo, ' ');
    m_instance.labels[i] = vecInfo[0];
		for(int j = 1; j < vecInfo.size(); j++)
		{
				m_instance.features[i].push_back(vecInfo[j]);
		} 
      
  }

	return &m_instance;
}


