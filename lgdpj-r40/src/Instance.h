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

		double sys_pos_feature_num;
		double sys_syn_feature_num;

		double score_pos_gold;
		double score_syn_gold;
		double score_pos_sys;
		double score_syn_sys;

		bool _has_create_all_feature_vectors;
		bool _has_alloc_fvec_probs;
		NRMat< NRMat<fvec> > fvec_dep;	// [ N*N; Q1*Q2 ]
		NRMat< NRMat<double> > prob_dep;

		NRMat< NRMat3d<fvec> > fvec_depl;	// labeled: [ N*N; L*Q1*Q2 ]
		NRMat< NRMat3d<double> > prob_depl;

		NRMat3d< NRMat3d<fvec> > fvec_sib;	// [N*N*N; Qh*Qs*Qc]
		NRMat3d< NRMat3d<double> > prob_sib;

		NRMat3d< NRMat4d<fvec> > fvec_sibl;	// [N*N*N; L*Qh*Qs*Qc]
		NRMat3d< NRMat4d<double> > prob_sibl;

		NRMat3d< NRMat3d<fvec> > fvec_grd;	// [N*N*N; Qh*Qc*Qg]
		NRMat3d< NRMat3d<double> > prob_grd;

		NRMat3d< NRMat4d<fvec> > fvec_grdl;	// [N*N*N; L*Qh*Qc*Qg]
		NRMat3d< NRMat4d<double> > prob_grdl;

		NRMat4d< NRMat4d<fvec> > fvec_grdsib;	// [N*N*N*N; Qg*Qh*Qs*Qc]
		NRMat4d< NRMat4d<double> > prob_grdsib;

		NRVec< NRVec<fvec> > fvec_pos1; // POS unigram features, [N; Q]. [i][0]: alloc fvec.idx
		NRVec< NRVec<double> > prob_pos1;

		NRVec< NRMat<fvec> > fvec_pos2; // POS bigram features, [N; Q1*Q2]. [i][?][0]: alloc fvec.idx
		NRVec< NRMat<double> > prob_pos2;

	public:

		NRMat<bool> candidate_heads;
		NRMat< vector<string> > candidate_labels;

		vector<int> heads;
		vector<string> deprels;
		vector<int> deprels_int;

		vector<string> forms;
		vector<string> lemmas;
		vector<string> orig_lemmas;
		vector<string> orig_cpostags;
		vector<string> cpostags;
		vector<string> postags;

		vector<string> contain_hyphen;
		vector<string> contain_number;
		vector<string> contain_uppercase_char;

		/* Joint models always use pseudo between features. 
		Therefore, postags_for_bet_feat contains the 1-best candidate POSs (p_postags[i][0]) in the joint cases.

		NOTE: during training, the reference (gold) features extracted from the correct tree should also use "postags_for_bet_feat", rather than cpostags.
		In this way, the learning process is consistent in the sense that when POS[h] and POS[m] are correctly found, 
		then the corresponding in between features are also identical to the reference features.

		For DP cases, postags_for_bet_feat = cpostags.
		*/
		vector<string> postags_for_bet_feat;
		vector<int> verb_cnt;
		vector<int> conj_cnt;
		vector<int> punc_cnt;
		vector< vector<string> > p_postags;
		vector<int> p_postags_num;

		vector<int> predicted_heads;
		vector<string> predicted_deprels;
		vector<int> predicted_deprels_int;
		vector<int> predicted_postags_idx;
		sparsevec predicted_fv;
		double predicted_prob;

		vector< vector<string> > chars;

		vector<string> orig_feats;

		vector<string> pheads;
		vector<string> pdeprels;

/*		void clear() {
			candidate_heads.dealloc();
			candidate_labels.dealloc();

			heads.clear();
			deprels.clear();
			deprels_int.clear();

			forms.clear();
			lemmas.clear();
			orig_lemmas.clear();
			orig_cpostags.clear();
			cpostags.clear();
			postags.clear();

			vector<string> contain_hyphen;
			vector<string> contain_number;
			vector<string> contain_uppercase_char;

			vector<string> postags_for_bet_feat;
			vector<int> verb_cnt;
			vector<int> conj_cnt;
			vector<int> punc_cnt;
			vector< vector<string> > p_postags;
			vector<int> p_postags_num;

			vector<int> predicted_heads;
			vector<string> predicted_deprels;
			vector<int> predicted_deprels_int;
			vector<int> predicted_postags_idx;
			sparsevec predicted_fv;
			double predicted_prob;

			vector< vector<string> > chars;

			vector<string> orig_feats;

			vector<string> pheads;
			vector<string> pdeprels;
		}
*/
/*	    void write( ostream &outf ) const {
		    for (int i = 1; i < size(); ++i) {
		    	outf << i << "\t"
			    	<< forms[i] << "\t"
			    	<< orig_lemmas[i] << "\t"
			    	<< orig_cpostags[i] << "\t"
			    	<< postags[i] << "\t"
			    	<< orig_feats[i] << "\t"				
			    	<< heads[i] << "\t"
			    	<< deprels[i]	<< "\t" 
			    	<< pheads[i] << "\t"
			    	<< pdeprels[i] << endl;
		        }
	        outf << endl;
        }
*/	};
} // namespace dparser


#endif



