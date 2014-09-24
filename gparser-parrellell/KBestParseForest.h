#ifndef _K_BEST_PARSE_FOREST_
#define _K_BEST_PARSE_FOREST_
#pragma once
#include "ParseForestItem.h"
#include "DepInstance.h"
#include "MyLib.h"
#include "MultiArray.h"
#include "common.h"


#include <vector>
#include <string>
using namespace std;



class KBestParseForest
{
public:
	//static int rootType;
	MultiArray<ParseForestItem> chart;

protected:
	vector<string> *sent;
	vector<string> *pos;
	int start;
	int end;
	int K;
	int tmp;

public:
	KBestParseForest() {}

	virtual KBestParseForest &reset(int _start, int _end, DepInstance *pInstance, int _K) {
		K = _K;
		start = _start;
		end = _end;
		sent = &(pInstance->forms);
		pos = &(pInstance->cpostags); 
		vector<unsigned int> chart_dim;
		chart.setDemisionVal(chart_dim, end+1, end+1, 2, 3, K);
		chart.resize(chart_dim);
		return *this;
	}

	virtual ~KBestParseForest(void);

	bool add(int s, int type, int dir, double score, const FeatureVec &fv);

	bool add(int s, int r, int t, int type,
		int dir, int comp, double score,
		const FeatureVec &fv,
		ParseForestItem *p1, ParseForestItem *p2);

	double getProb(int s, int t, int dir, int comp) {
		return getProb(s,t,dir,comp,0);
	}

	double getProb(int s, int t, int dir, int comp, int i);

	void getProbs(int s, int t, int dir, int comp, vector<double> &vecProb); 

	void getBestParse(FeatureVec &d0, string &d1, double &parse_prob);

	void getBestParses(vector<FeatureVec> &d0, vector<string> &d1, vector<double> &parse_probs);

	void getFeatureVec(ParseForestItem &pfi, FeatureVec &fv);

	void getFeatureVec(ParseForestItem &pfi, vector<FeatureVec *> &pvfv);

	virtual void getDepString(const ParseForestItem &pfi, string &strDep);

	string &trim(string &str) {
		remove_beg_end_spaces(str);
		return str;
	}

	void viterbi( DepInstance *inst, FVS_PROBS* pFvsProbs, const MultiArray<int> &static_types, bool isLabeled);

	// returns pairs of indices and -1,-1 if < K pairs
	void getKBestPairs(unsigned int chart_pos, unsigned int chart_pos2, vector< pair<int, int> > &pairs);

};


#endif

