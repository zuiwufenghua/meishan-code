#ifndef _ALPHABET_
#define _ALPHABET_
#pragma once

#include "MyLib.h"
#include "hash_map.hpp"
#include "Trie.h"

/*
	This class serializes feature from string to int.
	Index starts from 0.
*/

class Alphabet
{
public:
	Alphabet() : m_numEntries(0), m_isGrowthStopped(false), m_hasCollectedKeys(false)
	{
		m_dict.use_value(true);
	}

	~Alphabet(void) {}
	int lookupIndex(const string &key);
  int lookupIndex(const string &key, bool& bAppend);
	void lookupIndex_vec(const vector<string> &vecKey, vector<int> &vecIdx) {
		for (int i = 0; i < vecKey.size(); ++i) {
			int idx = lookupIndex(vecKey[i]);
			if (idx >= 0) vecIdx.push_back(idx);
		}
	}

	int size() {
		return m_map.size();
	}
//	int add(const string &key);
/*	int add(const string &key, int idx) {
		m_map[key] = idx;
	}
*/	void allowGrowth() {
		m_isGrowthStopped = false;
	}
	void stopGrowth() {
		m_isGrowthStopped = true;
	}

	void show() {
		cerr << "total num: " << m_map.size() << endl;
		hash_map<string, int>::const_iterator it = m_map.begin();
		while (it != m_map.end()) {
			cerr << "(" << it->first << " " << it->second << ")" << endl;
			++it;
		}
	}

	void getKey_vec(const vector<int> &vecIdx, vector<string> &vecKey) const {
		vecKey.resize(vecIdx.size());
		for (int i = 0; i < vecIdx.size(); ++i) vecKey[i] = string(getKey(vecIdx[i]));
	}

	const char *getKey(int idx) const
	{
		if (idx >= 0 && idx < m_vecKeys.size()) {
			return m_vecKeys[idx].c_str();
		} else {
			cerr << "Alphabet::getKey idx range err: " << idx << endl
				<< "Alphabet::m_vecKeys.size: " << m_vecKeys.size() << endl;
			return 0;
		}
	}

	const vector<string> & getAllKeys() const;

	void getKeys(vector<string> &vecKeys) const;

	void collectKeys();

	void readObject(ifstream &inf);

	void writeObject(ofstream &outf) const;

	// added by xchen
	void fillTrie() {
		if (!m_isGrowthStopped || !m_hasCollectedKeys) {
			cerr << "alphabet still growing or hasn't collected keys. fail to fill trie dict!" << endl;;
			return;
		}
		print_time();
		for (int i=0; i<m_vecKeys.size(); i++)
		{
			m_dict.add(m_vecKeys[i].c_str(), i);
		}
		m_dict.build();
		print_time();
	}

	void saveTrie(const char* file) {
		m_dict.save(file);
	}

	void loadTrie(const char* file) {
		m_dict.open(file);
	}

	void getFeatures(set<int> &featureIds, vector<string>& featureStrings_reverse, vector<string>& featureStrings)
	{
	  featureStrings_reverse.clear();
	  featureStrings.clear();
	  //map<string, int, string_less>::const_iterator it = m_map.begin();
	  hash_map<string, int>::const_iterator it = m_map.begin();
		while (it != m_map.end()) {
		  if(featureIds.find(it->second) == featureIds.end())
		  {
		    featureStrings_reverse.push_back(it->first);
		  }
		  else
		  {
		    featureStrings.push_back(it->first);
		  }
		  ++it;
		}
	}

	void reinit()
	{
	  m_map.clear();
	  m_dict.reinit();
	  m_dict.use_value(true);
	  m_vecKeys.clear();
		m_numEntries = 0;
		m_isGrowthStopped = false;
		m_hasCollectedKeys = false;
	}

	void reportTrie() {
		m_dict.report();
	}

private:
	hash_map<string, int> m_map;
	Trie m_dict;
	vector<string> m_vecKeys;
	int m_numEntries;
	bool m_isGrowthStopped;
	bool m_hasCollectedKeys;
};

#endif

