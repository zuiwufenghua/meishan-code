#include "Alphabet.h"


const vector<string> & Alphabet::getAllKeys() const
{
	if (!m_hasCollectedKeys) {
		throw string("Alphabet::getAllKeys() err: have not collected keys");
	}
	return m_vecKeys;
}

void Alphabet::getKeys(vector<string> &vecKeys) const
{
	vecKeys.clear();
	//map<string, int, string_less>::const_iterator it = m_map.begin();
	hash_map<string, int>::const_iterator it = m_map.begin();
	while (it != m_map.end()) {
		vecKeys.push_back(it->first);
		++it;
	}
}


void Alphabet::readObject(ifstream &inf)
{
	int tmp;
	::readObject(inf, tmp);
	m_isGrowthStopped = tmp == 0 ? false : true;
	m_hasCollectedKeys = false;
	::readObject(inf, m_numEntries);
	int i = 0;
	for (; i < m_numEntries; ++i) {
		string strFeat;
		my_getline(inf, strFeat);
		string strIdx;
		my_getline(inf, strIdx);
		m_map[strFeat] = atoi(strIdx.c_str());
	}
}

void Alphabet::writeObject(ofstream &outf) const
{
	::writeObject(outf, (m_isGrowthStopped ? int(1) : int(0)));
	::writeObject(outf, m_numEntries);

	/*
	hash_map<string, int>::const_iterator it = m_map.begin();
	while (it != m_map.end()) {
		outf << it->first << endl
			<< it->second << endl;
		++it;
	}
	*/

	for (int i=0; i<m_numEntries; i++)
	{
		outf << m_dict._vec_key[i] << endl
			 << m_dict._vec_value[i] << endl;
	}
}

int Alphabet::lookupIndex(const string &str)
{
	// after add all feats, call trie.find()
	if (m_hasCollectedKeys) // m_isGrowthStopped also be true
	{
		return m_dict.find(str.c_str());
	}

	// when not finished add all feats
	hash_map<string, int>::const_iterator it = m_map.find(str);
	if (it != m_map.end()) return it->second;
	m_map[str] = m_numEntries;
	++m_numEntries;
	return (m_numEntries-1);
}

int Alphabet::lookupIndex(const string &str, bool& bAppend)
{
  bAppend = false;
	// after add all feats, call trie.find()
	if (m_hasCollectedKeys) // m_isGrowthStopped also be true
	{
		return m_dict.find(str.c_str());
	}

	// when not finished add all feats
	hash_map<string, int>::const_iterator it = m_map.find(str);
	if (it != m_map.end()) return it->second;
  if(!m_isGrowthStopped)
  {
	  m_map[str] = m_numEntries;
	  ++m_numEntries;
    bAppend = true;
	  return (m_numEntries-1);
  }
  else
  {
    return -1;
  }
}

void Alphabet::collectKeys()
{
	if (!m_isGrowthStopped) {
		cerr << "alphabet is still growing, fail to collect keys" << endl;;
		return;
	}
	if (m_hasCollectedKeys) return;
	m_vecKeys.resize(m_numEntries);

	hash_map<string, int>::const_iterator it = m_map.begin();
	while (it != m_map.end()) {
		if (it->second < 0 || it->second >= m_vecKeys.size()) {
			cerr << "alphabet includes wrong index: " << it->second << endl
				<< "numEntries: " << m_numEntries << endl;
			return;
		}
		m_vecKeys[it->second] = it->first;
		++it;
	}

	m_hasCollectedKeys = true;
}

/*
int Alphabet::add(const string &str)
{
	int idx = lookupIndex(str);
	if (idx == -1) {
		++m_numEntries;
		string str2 = str;
		m_map[str] = m_numEntries;
		return m_numEntries;
	} else {
		return idx;
	}
}
*/


