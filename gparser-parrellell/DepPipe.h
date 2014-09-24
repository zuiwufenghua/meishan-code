#ifndef _DEP_PIPE_
#define _DEP_PIPE_

#pragma once

#include <fstream>
#include <iostream>
#include <sstream>
#include <vector>
#include <string>
#include <map>
using namespace std;

#include "DepInstance.h"
#include "FeatureVec.h"
#include "Alphabet.h"
#include "CONLLReader.h"
#include "CONLLWriter.h"
#include "Parameter.h"
#include "ParserOptions.h"
#include "MultiArray.h"
#include "spthread.h"
#include "threadpool.h"


extern const string NO_FORM;
extern const string NO_LEMMA;
extern const string NO_CPOSTAG;
extern const string NO_POSTAG;
extern const string NO_FEAT;
extern const string FEAT_SEP;




struct ARC_INFO {
	bool is_root;
	bool is_right_arc;
	string dir;
	string dist;
	string dir_dist;
};

class DepPipe
{
public:
	DepPipe(const ParserOptions &_options);
	~DepPipe(void);

  void initSimpRule();

	int initInputFile(const char *filename);
	void uninitInputFile();
	int initOutputFile(const char *filename);
	void uninitOutputFile();

	int outputInstance(const DepInstance *pInstance);

	const char *getType(int typeIndex);

	DepInstance *nextInstance();

	int createAlphabet(vector<int> &vecLength);
	void closeAlphabet();


	virtual int writeInstance(FILE *featFile, DepInstance *pInstance);

public:



//	static void writeObject(ofstream &outf, const DepInstance &instance);
//	static void readObject(ifstream &inf, DepInstance &instance);

  struct ParseArg
	{
    ParseArg(DepPipe* _dp, DepInstance *_pInst, FVS_PROBS* _pFvsProbs, const Parameter* _pparams, int _start_pos, int _end_pos) 
		{
			dp = _dp;
      pInst = _pInst;
      pFvsProbs = _pFvsProbs;
      pparams = _pparams;
      start_pos = _start_pos;
      end_pos = _end_pos;
		}

    DepPipe* dp;
		DepInstance *pInst;
    FVS_PROBS* pFvsProbs;
    const Parameter* pparams;
    int start_pos;
    int end_pos;
	};

	void fillFeatureVectors(DepInstance *instance, FVS_PROBS* pFvsProbs,	const Parameter &params, 	int start_pos,	int end_pos);

	void fillFeatureVectors(DepInstance *instance, FVS_PROBS* pFvsProbs,	const Parameter &params);

  static void fillFeatures(void* _arg);



	void createSpan(DepInstance *pInstance);

	int createFeatureVector(DepInstance *pInstance);
  int createFeatureVector(DepInstance *pInstance, FeatureVec &fv);

protected:
	int addExtendedFeature(DepInstance *pInstance, FeatureVec &fv);

	// virtual void writeExtendedFeatures(DepInstance *pInstance, ofstream &featFile) {}
	void writeExtendedFeatures(DepInstance *pInstance, FILE *featFile);

protected:
	void mapTypes();

	int add(const string &feat, FeatureVec &fv);
	//void add(const string &feat, double val, FeatureVec &fv);

	int addArcFeature(DepInstance *pInstance, int smaller, int larger, bool attR, FeatureVec &fv);

	int addLabelFeature(DepInstance *pInstance, int nodeIdx, const string &deprel, bool is_child, bool attR, FeatureVec &fv);
	int addLabelFeature_surrounding(const string &prefix, const vector<string> &vecInfo, int nodeIdx, const string &deprel, const string &strIsChild, const string &dir, FeatureVec &fv);


public:
	Alphabet m_featAlphabet;
	Alphabet m_labelAlphabet;

	vector<string> m_vecTypes;
	vector<int> m_vecTypesInt;
	vector<DepInstance> m_vecInstances;
  vector< vector<SimpRuleUnit> > rules;
  bool m_bSimpFeature;

protected:
	const ParserOptions &options;
	DepReader *m_depReader;
	DepWriter *m_depWriter;

public:
 	threadpool tp;
	sp_thread_mutex_t * mut_iter;
	sp_thread_mutex_t * mut_counter;
	sp_thread_cond_t * cond_iter_over;
	int counter_; // for one iter's actual handle num
	int t_;       // for one iter's should handle num

public:
  //ostringstream out;
  void ReInit(set<int> invalidFeatures, vector<string>& invalidFeatures_str);

  inline void getDistance(int idx1, int idx2, string &dist);

  int addFeature( DepInstance *pInstance, const string &deprel, const int head_id, const int child_id, FeatureVec &fv );
  int addFeature_bi_bet_sur( DepInstance *pInstance, const string &deprel, const int head_id, const int child_id, FeatureVec &fv );
  int addFeature_unigram( DepInstance *pInstance, const string &deprel, const int node_id, const bool is_child, const bool is_right_arc, FeatureVec &fv );
  int addFeature_unigram_mcdonald06(DepInstance *pInstance, const string &deprel, const int node_id, const bool is_child, const bool is_right_arc, FeatureVec &fv);
	int addFeature_bi_bet_sur_mcdonald06(DepInstance *pInstance, const string &deprel, const int head_id, const int child_id, FeatureVec &fv);
	int addFeature_bi_mcdonald06(DepInstance *pInstance, const string &deprel, const int head_id, const int child_id, FeatureVec &fv);
	int addFeature_bet_mcdonald06(DepInstance *pInstance, const string &deprel, const int head_id, const int child_id, FeatureVec &fv);
	int addFeature_sur_mcdonald06(DepInstance *pInstance, const string &deprel, const int head_id, const int child_id, FeatureVec &fv);

	int addFeature_grand_sib( DepInstance *pInstance, const int par, const int mod, const int gch1, const int gch2, FeatureVec &fv);
	int addFeature_sib(DepInstance *pInstance, const int par, const int ch1, const int ch2, FeatureVec &fv);
	int addFeature_grand( DepInstance *pInstance, const int par, const int mod, const int gchild, FeatureVec &fv);

	void sentenceSimplification();

  void rerankWithGlobalFeature(DepInstance &inst, vector<FeatureVec>& d0, vector<string>& d1, const Parameter &params);

  //void createGlobalFeature(DepInstance &inst);
	

protected:
	void get_arc_info(const DepInstance *pInstance, const int head_id, const int child_id, ARC_INFO &arc_info ) {
		arc_info.is_root = (0 == head_id);
		arc_info.is_right_arc = (head_id < child_id ? true : false);
		arc_info.dir = arc_info.is_right_arc ? "-R" : "-L";
		if (arc_info.is_root) arc_info.dir = "-L#R";
		getDistance(head_id, child_id, arc_info.dist);	// 距离采用orig句子中的距离
		if (arc_info.is_root) 
		{	// 如果child是root，则使用左右两边的句子长度作为距离。
			string dist_child_to_end;
			getDistance(child_id, pInstance->orig_size(), dist_child_to_end);
			arc_info.dist += "#" + dist_child_to_end;
		}
		arc_info.dir_dist = arc_info.dir + FEAT_SEP + arc_info.dist;
	}

};

void DepPipe::getDistance(int idx1, int idx2, string &strDist)
{
	ostringstream out;
	int dist = abs(idx1 - idx2);
	if (dist > 10) {
		strDist = "10";
	} else if (dist > 5) {
		strDist = "5";
	} else {
		out.str("");
		out << dist-1;
		strDist = out.str();
	}
}


#endif
