#ifndef _DEP_INSTANCE_
#define _DEP_INSTANCE_

#pragma once

#include <string>
#include <vector>
#include <fstream>
#include <algorithm>

using namespace std;

#include "FeatureVec.h"
#include "MyLib.h"
#include "common.h"


/*
	this class implements the representation of parsing result of one sentence.

*/


class DepInstance
{
public:
	DepInstance() {}
	~DepInstance() {}
	int size() { return forms.size(); }
	void resize(int _size) { forms.resize(_size); }

	int orig_size() const { return forms.size(); }
	/*
	int size_to_parse() const { return orig_id_to_parse.size(); }
	int orig_id(const int id) const {
		if (id >= orig_size()) {
			ostringstream out;
			out << "id:" << id << " >= orig_size():" << orig_size() << endl;
			throw out.str();
		}
		return orig_id_to_parse[id];
	}
*/

/*	void writeObject(ofstream &outf) const;
	void readObject(ifstream &inf);
	void setInstance(const vector<string> &_forms,
					 const vector<string> &_lemmas,
					 const vector<string> &_cpostags,
					 const vector<string> &_postags,
					 const vector< vector<string )
*/
	void copyValuesFrom(const DepInstance &depInstance)
	{
		actParseTree = depInstance.actParseTree;
		
		//cout << "1";
		forms.resize(depInstance.forms.size());
		copy(depInstance.forms.begin(), depInstance.forms.end(), forms.begin());
		//cout << "2";
		lemmas.resize(depInstance.lemmas.size());
		copy(depInstance.lemmas.begin(), depInstance.lemmas.end(), lemmas.begin());
		//cout << "3";
		cpostags.resize(depInstance.cpostags.size());
		copy(depInstance.cpostags.begin(), depInstance.cpostags.end(), cpostags.begin());
		//cout << "4";
		postags.resize(depInstance.postags.size());
		copy(depInstance.postags.begin(), depInstance.postags.end(), postags.begin());
		//cout << "5";
		heads.resize(depInstance.heads.size());
		copy(depInstance.heads.begin(), depInstance.heads.end(), heads.begin());
		//cout << "6";
		deprels.resize(depInstance.deprels.size());
		copy(depInstance.deprels.begin(), depInstance.deprels.end(), deprels.begin());

		postags_for_bet_feat.resize(depInstance.postags_for_bet_feat.size());
		copy(depInstance.postags_for_bet_feat.begin(), depInstance.postags_for_bet_feat.end(), postags_for_bet_feat.begin());
		//cout << "7";
		verb_cnt.resize(depInstance.verb_cnt.size());
		copy(depInstance.verb_cnt.begin(), depInstance.verb_cnt.end(), verb_cnt.begin());
		//cout << "8";
		conj_cnt.resize(depInstance.conj_cnt.size());
		copy(depInstance.conj_cnt.begin(), depInstance.conj_cnt.end(), conj_cnt.begin());
		//cout << "9\n";
		punc_cnt.resize(depInstance.punc_cnt.size());
		copy(depInstance.punc_cnt.begin(), depInstance.punc_cnt.end(), punc_cnt.begin());

    feats.resize(depInstance.feats.size());
    for(int i = 0; i < feats.size(); i++)
    {
      feats[i].resize(depInstance.feats[i].size());
		  copy(depInstance.feats[i].begin(), depInstance.feats[i].end(), feats[i].begin());
    }
	}




	void copyValuesTo(DepInstance &depInstance)
	{
		depInstance.actParseTree = actParseTree;
		
		//cout << "1";
		depInstance.forms.resize(forms.size());
		copy(forms.begin(), forms.end(), depInstance.forms.begin());
		//cout << "2";
		depInstance.lemmas.resize(lemmas.size());
		copy(lemmas.begin(), lemmas.end(), depInstance.lemmas.begin());
		//cout << "3";
		depInstance.cpostags.resize(cpostags.size());
		copy(cpostags.begin(),cpostags.end(), depInstance.cpostags.begin());
		//cout << "4";
		depInstance.postags.resize(postags.size());
		copy(postags.begin(), postags.end(), depInstance.postags.begin());
		//cout << "5";
		depInstance.heads.resize(heads.size());
		copy(heads.begin(), heads.end(), depInstance.heads.begin());
		//cout << "6";
		depInstance.deprels.resize(deprels.size());
		copy(deprels.begin(), deprels.end(), depInstance.deprels.begin());

		depInstance.postags_for_bet_feat.resize(postags_for_bet_feat.size());
		copy(postags_for_bet_feat.begin(), postags_for_bet_feat.end(), depInstance.postags_for_bet_feat.begin());
		//cout << "7";
		depInstance.verb_cnt.resize(verb_cnt.size());
		copy(verb_cnt.begin(), verb_cnt.end(), depInstance.verb_cnt.begin());
		//cout << "8";
		depInstance.conj_cnt.resize(conj_cnt.size());
		copy(conj_cnt.begin(), conj_cnt.end(), depInstance.conj_cnt.begin());
		//cout << "9\n";
		depInstance.punc_cnt.resize(punc_cnt.size());
		copy(punc_cnt.begin(), punc_cnt.end(), depInstance.punc_cnt.begin());


    depInstance.feats.resize(feats.size());
    for(int i = 0; i < feats.size(); i++)
    {
      depInstance.feats[i].resize(feats[i].size());
		  copy(feats[i].begin(), feats[i].end(), depInstance.feats[i].begin());
    }
	}



	 void deleteNode(vector<int> nodeIds, DepInstance &depInstance);
   void findDeleteNodes(vector< vector<SimpRuleUnit> > rules, vector<int>& deleteNodes );
   void findChild(int id, vector<int>& childs)
   {
     childs.clear();
     for(int i = 0; i < forms.size(); i++)
     {
       if(heads[i] == id)
       {
         childs.push_back(i);
       }
     }
   }

  static void fillInstance_k(DepInstance &inst, const vector<string> d1, const vector<double> &parse_probs, const vector<string>& vecTypes);
	static void fillParseResult(const string &tree_span, vector<int> &heads, vector<string> &deprels, const vector<string>& vecTypes);
	static void fillInstance(DepInstance &inst, const string &tree_span, const vector<string>& vecTypes);

	static int fillInstance(DepInstance &inst,
		const vector<string> &vecWord,
		const vector<string> &vecCPOS);

	static int getParseResult(const DepInstance &inst,
		vector<int> &vecHead,
		vector<string> &vecRel);

public:
	FeatureVec fv;
	string actParseTree;
	vector<string> forms;
	vector<string> lemmas;
	vector<string> cpostags;
	vector<string> postags;
	vector< vector<string> > feats;
	vector<int> heads;
	vector<string> deprels;

	vector< vector<string> > k_deprels;
	vector< vector<int> > k_heads;
	vector<double> k_probs;

  vector<string> postags_for_bet_feat;
	vector<int> verb_cnt;
	vector<int> conj_cnt;
	vector<int> punc_cnt;
};

#endif

