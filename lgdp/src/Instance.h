#ifndef _INSTANCE_
#define _INSTANCE_

#pragma once

#include <string>
#include <vector>
#include <fstream>
#include <iostream>
#include <algorithm>

#include "Parameters.h"
#include "FVec.h"
#include "NRMat.h"
using namespace nr;
using namespace std;
using namespace egstra;

namespace dparser {

	class Instance
	{
	public:
		Instance(const int _id) : id(_id), _has_alloc_fvec_probs(false), _has_create_all_feature_vectors(false) {}
		~Instance() {}
		int size() const { return forms.size(); }

	public:
		const int id;
		//sparsevec gold_fv;

		double sys_syn_feature_num;

		double score_syn_gold;
		double score_syn_sys;

		bool _has_create_all_feature_vectors;
		bool _has_alloc_fvec_probs;
		NRMat< fvec > fvec_dep;	// [N*N]
		NRMat< double > prob_dep;

		NRMat< NRVec<fvec> >  fvec_depl;	// labeled: [N*N, L]
		NRMat< NRVec<double> > prob_depl;

		NRMat3d<fvec>  fvec_sib;	// [N*N*N]
	  NRMat3d<double> prob_sib;

		NRMat3d< NRVec<fvec> > fvec_sibl;	// [N*N*N,L]
		NRMat3d< NRVec<double> > prob_sibl;

		NRMat3d<fvec> fvec_grd;	// [N*N*N]
		NRMat3d<double> prob_grd;

		NRMat3d< NRVec<fvec> > fvec_grdl;	// [N*N*N, L]
		NRMat3d< NRVec<double> > prob_grdl;



	public:


		vector<int> heads;
		vector<string> deprels;
		vector<int> deprels_int;

		vector<string> forms;
		vector<string> lemmas;
		vector<string> cpostags;
		vector<string> postags;

		vector<string> contain_hyphen;
		vector<string> contain_number;
		vector<string> contain_uppercase_char;


		vector<string> postags_for_bet_feat;
		vector<int> verb_cnt;
		vector<int> conj_cnt;
		vector<int> punc_cnt;


		NRMat<int>  predicted_heads;   // K*N
		NRMat<string> predicted_deprels; // K*N
		NRMat<int> predicted_deprels_int; // K*N
		NRVec<sparsevec> predicted_fvs; // K
		NRVec<double> predicted_probs;  //K

		vector< vector<string> > chars;

    NRMat3d<double> marginal_scores;



	};
} // namespace dparser


#endif



