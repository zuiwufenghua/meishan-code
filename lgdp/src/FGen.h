#ifndef _FEATURE_EXTRACTER_
#define _FEATURE_EXTRACTER_

#pragma once

#include <fstream>
#include <iostream>
#include <sstream>
#include <vector>
#include <string>
#include <list>
#include <map>
using namespace std;

#include "Instance.h"
#include "common.h"

#include "NRMat.h"
using namespace nr;

#include "FVec.h"
#include "FeatureDictionary.h"
using namespace egstra;

namespace dparser {
	struct ARC_INFO {
		bool is_root;
		bool is_right_arc;
		string dir;
		string dist;
		string dir_dist;
	};

	class FGen
	{
	private:
		int _total_feature_dim;

		NRVec<const char *> _labels;
		FeatureDictionary _label_dict;

		FeatureDictionary _dependency_feat_dict;
		FeatureDictionary _sibling_feat_dict;
		FeatureDictionary _grand_feat_dict;
		FeatureDictionary _grandsibling_feat_dict;

		int _dependency_feat_offset;
		int _dependency_feat_dim;
		int _sibling_feat_offset;
		int _sibling_feat_dim;
		int _grand_feat_offset;
		int _grand_feat_dim;
		int _grandsibling_feat_offset;
		int _grandsibling_feat_dim;

	private:
		string _name;
		bool _generation_mode;
		int _L; // label number


	private: // options
		

		bool _use_lemma;
		bool _english;
		bool _use_coarse_postag;



		bool _use_dependency_unigram;
		bool _use_dependency_bigram;
		bool _use_dependency_surrounding;
		bool _use_dependency_between;


		bool _use_sibling_basic;
		bool _use_sibling_linear;

		
		bool _use_grand_basic;
		bool _use_grand_linear;

    // default it as true, h->m, if m -s a leaf node, then regard a fake h->m->m node.
		//bool _use_no_grand_features;

		bool _use_unlabeled_syn_features;
		int _fcutoff;
		bool _use_bohnet_syn_features;
		bool _use_distance_in_dependency_features;

	public:

		bool _use_dependency;
    bool _use_sibling;
    bool _use_grand;

		FGen() {
			_name = "FGen";
			_generation_mode = false;
			_L = 1;

			_dependency_feat_offset = 0;
			_dependency_feat_dim = 0;
			_sibling_feat_offset = 0;
			_sibling_feat_dim = 0;
			_grand_feat_offset = 0;
			_grand_feat_dim = 0;
			_grandsibling_feat_offset = 0;
			_grandsibling_feat_dim = 0;
		}

		~FGen() {}

		void process_options();
		void start_generation_mode() { _generation_mode = true; }
		void stop_generation_mode() { _generation_mode = false; }

		void alloc_fvec_prob(Instance * const inst) const;
		void dealloc_fvec_prob(Instance * const inst) const;
		void create_all_feature_vectors(Instance * const inst);
    void create_all_feature_vectors_margin( Instance * const inst, sparsevec &sp_fv, const double scale = 1.0 );

		void create_all_syn_features_according_to_tree( Instance * const inst, const vector<int> &heads, const vector<int> &deprels, sparsevec &sp_fv, const double scale = 1.0);

		void save_dictionaries(const string &dictdir) /*const*/;
		void load_dictionaries(const string &dictdir);

		int feature_dimentionality() const {
			return _total_feature_dim;
		}

		void collect_word_postag_label( Instance * const inst, const bool collect_word=false); // when creating dictionaries,  collect word/postag/label
		
		int get_label_id( const string &deprel )
		{
			assert(!_generation_mode);
			const int label_id = _label_dict.getFeature(deprel, false);
			if (label_id < 0) {
				return 0;
				//cerr << "unknown label: " << deprel << endl;
				//exit(-1);
			}
			return label_id;
		}

    NRVec<const char *> get_labels()
    {
      return  _labels;;
    }

		int get_label_num() {
			return _label_dict.dimensionality();
		}

		void assign_deprels_int(Instance * const inst);
		void assign_predicted_deprels_str(Instance * const inst);

		bool no_modifier_bet(const int beg, const int end, const int head_id, const vector<int> &_heads_ref ) const {
			if (beg >= end) {
				cerr << "FGen::no_modifier_bet(): beg >= end!" << beg << " " << end << "; head_id: " << head_id << "; length: " << _heads_ref.size() << endl;
				exit(-1);
			}
			for (int i = beg+1; i < end; ++i) { // (beg, end), NOT [beg, end]
				if (_heads_ref[i] == head_id) return false;
			}
			return true;
		}

	private:

		void usage(const char * const mesg) const;

		void addDependencyFeature(const Instance *inst, const int head_id, const int child_id, list<string> &feats_str) const {
			addDependencyFeature_bohnet(inst, head_id, child_id, feats_str);
		}

		void addGrandSiblingFeature( const Instance *inst, const int grand_id, const int head_id, const int child_id, const int sibling_id, list<string> &feats_str);
		void addSiblingFeature_bohnet(const Instance *inst, const int head_id, const int child_id, const int sibling_id, list<string> &feats_str);
		void addGrandFeature_bohnet(const Instance *inst, const int head_id, const int child_id, const int gchild_id, list<string> &feats_str);

		void addDependencyFeature_bohnet(const Instance *inst, const int head_id, const int child_id, list<string> &feats_str) const;

		void addPOSFeature_unigram(const Instance *inst, const int node_id, list<string> &feats_str ) const;
		void addPOSFeature_bigram( const Instance *inst, const int node_id, list<string> &feats_str ) const;

	public:
		static void getDirection(const int head_id, const int child_id, string &strDir) {
			if (head_id == 0) {
				strDir = "L#R";
			} else {
				strDir = head_id > child_id ? "L" : "R";
			}
		}

		static void getDistance_1_2_36_7( const int idx1, const int idx2, string &strDist ) {
			const int dist = abs(idx1 - idx2);
			//if (idx1 == 0 || idx2 == 0) {
			//	strDist = "0"; // ROOT
			//} else 
			if (dist < 3) { // 1, 2
				ostringstream out;
				out << dist;
				strDist = out.str();
			} else if (dist < 7) {
				strDist = "<7";
			} else {
				strDist = ">6";
			}
		}

	};
} // namespace gparser_space

#endif


