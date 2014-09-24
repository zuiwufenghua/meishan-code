#ifndef _PARSER_
#define _PARSER_

#pragma once
#include <vector>
#include <iostream>
#include <fstream>
#include <iomanip>
#include <set>
using namespace std;

#include "IOPipe.h"
#include "FGen.h"
#include "Decoder_1o.h"
#include "Decoder_2o_carreras.h"
#include "Decoder_3o_koo.h"
#include "Parameters.h"
#include "common.h"
#include "GzFile.h"

/*******************
There seems some conflicts between "ChartUtils.h" and "spthread.h".
The order of their #include can not be reversed!
I do not know why!
 *******************/
#include "CharUtils.h"
#include "StringMap.h"
using namespace egstra;

#include "spthread.h"
#include "threadpool.h"
/*******************/

extern const string LEFT_DEP;
extern const string RGHT_DEP;

void usage(const char* mesg = "");

namespace dparser {

	/*
	this class controls the parsing process.
	*/
	class Parser
	{
	public:
		IOPipe m_pipe_train;
		IOPipe m_pipe_test;
		IOPipe m_pipe_dev;
		FGen m_fgen;
		parameters m_param;
		Decoder *m_decoder;

/* options */
		bool _labeled;
		bool _use_filtered_heads;
		//bool _use_cpostags_to_filter_dependencies;
		//int _use_cpostags_to_filter_dependencies_pattern_cutoff;
		//bool _use_cpostags_to_filter_labels;

		// options
		int _display_interval;
		int _k_best_pos;

		string _dictionary_path;
		string _parameter_path;
		int _inst_max_len_to_throw;
		StringMap<int> _punctuation_tags;

		bool _train;
		int _iter_num;
		bool _train_conservative_update;
		bool _train_use_pa;
		bool _train_use_seperate_pa;
		string _filename_train;
		string _filename_dev;
		int _inst_max_num_train;
		bool _dictionary_exist;
		bool _pamameter_exist;
		int _param_tmp_num;
		bool _param_all_exist;
		bool _dealloc_fvec_prob;
		bool _eval_on_voted_parameters;
		bool _train_add_gold_pos;
		bool _train_add_gold_head;

		int _thread_num;
		bool _train_parallel_creating_features_and_decoding;
		int _train_creating_features_ahead; // if the creating-features-thread is ahead of the decoding thread by a few instances, then wait.

		bool _test;
		string _filename_test;
		string _filename_output;
		int _param_num_for_eval;
		int _inst_max_num_eval;
		int _test_batch_size;

		bool _use_negative_features;
		int _k_duplicate_positive_features;
		bool _verify_decoding_algorithm;
		bool _use_unlabeled_syn_features;
		bool _evaluate_model_score;

/* structures for filtering dependencies and labels */

		//StringMap< StringMap<int> > _cpos_dependency_2_labels;  // [int] for frequency
		//StringMap< StringMap<int> > _cpos_as_modifier_direction_2_labels;
		//StringMap< StringMap<int> > _cpos_as_modifier_nodirection_2_labels;

		//StringMap< set<int> > _cpos_dependency_2_labels_id;
		//StringMap< set<int> > _cpos_as_modifier_direction_2_labels_id; // [int] for label id
		//StringMap< set<int> > _cpos_as_modifier_nodirection_2_labels_id;

		//StringMap< StringMap<int> > _cpos_as_leftside_modifier_2_head_cposs;
		//StringMap< StringMap<int> > _cpos_as_rightside_modifier_2_head_cposs;

/* variables used in train */
		int _number_processed;

		int ctr_update_step_nega_pos;
		int ctr_update_step_nega_syn;
		int ctr_update_step_zero_pos;
		int ctr_update_step_zero_syn;
		double update_step_accumulate_pos;
		double update_step_accumulate_syn;

/* variables used in evaluate */
		int inst_num_processed_total;
		int word_num_total;
		int word_num_dep_correct;
		int word_num_rel_correct;
		int sent_num_root_corect;
		int sent_num_CM; // complete match
		int sent_num_LCM;

		int oov_num;
		int oov_pos_correct_num;
		int word_punc_num_pos_correct;
		int word_punc_num_total;
		vector<int> pos_correct_k_best;

		double tot_score_pos_gold;
		double tot_score_syn_gold;
		double tot_score_pos_sys;
		double tot_score_syn_sys;
		double tot_pos_feature_num;
		double tot_syn_feature_num;

/* thread control */
		threadpool _tp;
		static sp_thread_mutex_t _mutex;
		static sp_thread_cond_t _cond_waiting_create; // waiting for the decoding-thread
		static sp_thread_cond_t _cond_waiting_decode; // waiting for the creating-features-thread to finish the current instance.
		static sp_thread_cond_t _cond_done_decode; // complete all the instances

		static vector<bool> _train_features_created;
		static int _train_creating_features_i;
		static int _train_decoding_i;

	public:
		Parser() : m_decoder(0), _tp(0) {
			process_options();
			if (_thread_num > 1) {
				_tp = create_threadpool(_thread_num);
			}
		}

		~Parser(void) {
			delete_decoder(m_decoder);
			if (_thread_num > 1) {
				destroy_threadpool(_tp);
			}
		}

		void process_options();

		void run()
		{
			if (_train) train();
			if (_test) test(_param_num_for_eval);
		}

		static Decoder *new_decoder() {
			Decoder *decoder = 0;
			string decoder_name;

			if (!options::get("decoder", decoder_name)) {
				usage("please provide the decoder type: 1o/2o"); exit(1);
			}
			if (decoder_name == "1o") {
				decoder = new Decoder_1o();
			} else if (decoder_name == "2o-carreras") {
				decoder = new Decoder_2o_carreras();
			} else if (decoder_name == "3o-koo") {
				decoder = new Decoder_3o_koo();
			} else {
				usage("other decoder than 1o is not implemented."); exit(1);
			}
			assert(decoder);

			decoder->process_options();	// get _L, _T etc.
			return decoder;
		}

		static void delete_decoder(Decoder *&decoder) {
			if (decoder) {
				delete decoder;
				decoder = 0;
			}
		}

	public:
		typedef struct train_creating_features_for_one_instance_thread_arg {
			train_creating_features_for_one_instance_thread_arg(Parser * const parser, const int inst_idx) 
				: _parser(parser), _inst_idx(inst_idx) {}
			Parser * const _parser;
			const int _inst_idx;
		} ;

		typedef struct train_creating_features_thread_arg {
			train_creating_features_thread_arg(Parser * const parser) 
				: _parser(parser){}
			Parser * const _parser;
		} ;

		typedef struct train_decoding_thread_arg {
			train_decoding_thread_arg(Parser * const parser) 
				: _parser(parser) {}
			Parser * const _parser;
		} ;

		typedef struct parse_thread_arg {
			parse_thread_arg(Parser * const parser, Instance * const inst, const bool use_aver) 
				: _parser(parser), _inst(inst), _use_aver(use_aver) {}

			Parser * const _parser;
			Instance * const _inst;
			const bool _use_aver;
		} ;

		static void parse_thread(void *arg);
		static void train_creating_features_thread(void *arg);
		static void train_creating_features_for_one_instance_thread(void *arg);
		static void train_decoding_thread(void *arg);

		void parse(Decoder *decoder, Instance *inst, const bool use_average=false) {
			if (_labeled) m_fgen.assign_deprels_int(inst);

			m_fgen.alloc_fvec_prob(inst);
			m_fgen.create_all_feature_vectors(inst);
			compute_all_probs(inst, use_average);

			decoder->decodeProjectiveInterface(inst);
			if (_labeled) m_fgen.convert_predicted_deprels_int(inst);
			if(_verify_decoding_algorithm) verify_decoding_algorithm(inst);
			if (_evaluate_model_score) evaluate_model_score(inst, use_average);

			inst->predicted_fv.clear();
			m_fgen.dealloc_fvec_prob(inst);
		}
		
		void filter_dependencies(IOPipe &pipe, const bool add_gold_head = false);
		void filter_labels(IOPipe &pipe);

		void train();
		void test(const int iter);

		void train_one_iteration(const int iter_num);
		void train_tmp_param_all_exist();
		void evaluate(IOPipe &pipe, const bool test=false, const bool use_average=true);
		void reset_evaluate_metrics();
		void output_evaluate_metrics();
		//static void save_dictionary_for_filtering_dep_labels(StringMap< StringMap<int> > &dict, const string &fileName);
		//static void load_dictionary_for_filtering_dep_labels(StringMap< StringMap<int> > &dict, const string &fileName, const int cutoff=0);
		//static void add_dictionary_for_filtering_dep_labels(StringMap< StringMap<int> > &dict, const string &key, const string &val);
		//void process_dictionary_for_filtering_labels(const StringMap< StringMap<int> > &dict, StringMap< set<int> > &dict_int);
		//void create_dictionaries_for_filtering_dep_labels();

		void create_dictionaries();

		void load_dictionaries() {
			m_fgen.load_dictionaries(_dictionary_path);
			//if (_use_cpostags_to_filter_labels) {
			//	load_dictionary_for_filtering_dep_labels(_cpos_dependency_2_labels, _dictionary_path + "/cpostags-dependency-2-labels.gz");
			//	load_dictionary_for_filtering_dep_labels(_cpos_as_modifier_direction_2_labels, _dictionary_path + "/cpostags-as-modifier-direction-2-labels.gz");
			//	load_dictionary_for_filtering_dep_labels(_cpos_as_modifier_nodirection_2_labels, _dictionary_path + "/cpostags-as-modifier-nodirection-2-labels.gz");
			//	process_dictionary_for_filtering_labels(_cpos_dependency_2_labels, _cpos_dependency_2_labels_id);
			//	process_dictionary_for_filtering_labels(_cpos_as_modifier_direction_2_labels, _cpos_as_modifier_direction_2_labels_id);
			//	process_dictionary_for_filtering_labels(_cpos_as_modifier_nodirection_2_labels, _cpos_as_modifier_nodirection_2_labels_id);
			//}
			//if (_use_cpostags_to_filter_dependencies) {
			//	load_dictionary_for_filtering_dep_labels(_cpos_as_leftside_modifier_2_head_cposs, _dictionary_path + "/cpostags-as-leftside-modifier-2-head-cpostags.gz", _use_cpostags_to_filter_dependencies_pattern_cutoff);
			//	load_dictionary_for_filtering_dep_labels(_cpos_as_rightside_modifier_2_head_cposs, _dictionary_path + "/cpostags-as-rightside-modifier-2-head-cpostags.gz", _use_cpostags_to_filter_dependencies_pattern_cutoff);
			//}
		}

		void save_dictionaries() {
			m_fgen.save_dictionaries(_dictionary_path);
			//if (_use_cpostags_to_filter_labels) {
			//	save_dictionary_for_filtering_dep_labels(_cpos_dependency_2_labels, _dictionary_path + "/cpostags-dependency-2-labels.gz");
			//	save_dictionary_for_filtering_dep_labels(_cpos_as_modifier_direction_2_labels, _dictionary_path + "/cpostags-as-modifier-direction-2-labels.gz");
			//	save_dictionary_for_filtering_dep_labels(_cpos_as_modifier_nodirection_2_labels, _dictionary_path + "/cpostags-as-modifier-nodirection-2-labels.gz");
			//}
			//if (_use_cpostags_to_filter_dependencies) {
			//	save_dictionary_for_filtering_dep_labels(_cpos_as_leftside_modifier_2_head_cposs, _dictionary_path + "/cpostags-as-leftside-modifier-2-head-cpostags.gz");
			//	save_dictionary_for_filtering_dep_labels(_cpos_as_rightside_modifier_2_head_cposs, _dictionary_path + "/cpostags-as-rightside-modifier-2-head-cpostags.gz");
			//}
		}

		void save_parameters(const int iter) {
			m_param.flush_avg(_number_processed);
			m_param.save(_parameter_path, iter, false);
			m_param.save(_parameter_path, iter, true);
		}

		void load_parameters(const int iter) {
			m_param.load(_parameter_path, iter, true);
			m_param.load(_parameter_path, iter, false);
			//m_param.average();
		}

		void dot_all(const fvec * const fs, double * const probs, const int sz, const bool use_aver=false) const;

		void compute_all_probs(Instance *inst, const bool use_aver = false) const;

		void verify_decoding_algorithm( Instance * const inst);
		void evaluate_model_score( Instance * const inst, const bool use_aver = true)
		{
			sparsevec fv_pos_gold;
			sparsevec fv_syn_gold;
			sparsevec fv_pos_sys;
			sparsevec fv_syn_sys;

			m_fgen.create_all_pos_features_according_to_tree(inst, fv_pos_gold, 1.0);
			m_fgen.create_all_pos_features_according_to_tree(inst, fv_pos_sys, 1.0, inst->predicted_postags_idx);
			m_fgen.create_all_syn_features_according_to_tree(inst, inst->heads, inst->deprels_int, fv_syn_gold, 1.0);
			m_fgen.create_all_syn_features_according_to_tree(inst, inst->predicted_heads, inst->predicted_deprels_int, fv_syn_sys, 1.0, inst->predicted_postags_idx);

			inst->sys_pos_feature_num = fv_pos_sys.size();
			inst->sys_syn_feature_num = fv_syn_sys.size();

			inst->score_pos_gold = m_param.dot(fv_pos_gold, use_aver);
			inst->score_syn_gold = m_param.dot(fv_syn_gold, use_aver);
			inst->score_pos_sys = m_param.dot(fv_pos_sys, use_aver);
			inst->score_syn_sys = m_param.dot(fv_syn_sys, use_aver);
		}


		void evaluate_one_instance(const Instance * const inst);
		void get_update_step( double &update_step_pos, double &update_step_syn,
			const sparsevec &fv_dist_pos, const double pos_error, const sparsevec &fv_dist_syn, const double syn_error);
		void get_fvdist_error(Instance * const inst, sparsevec &fv_dist_pos, double &pos_error, sparsevec &fv_dist_syn, double &syn_error);


		void eval_oov_pos(const Instance *inst, int &oov_num, int &oov_pos_correct_num);
		int error_num_pos(const Instance *inst) const;
		void error_num_dp(const Instance *inst, const bool bIncludePunc, int &nDepError, int &nLabelError, int &nUnscoredToken, bool &bRootCorrect) const;
		void eval_k_best_pos_oracle(const Instance *pInst, vector<int> &pos_correct_k_best) const;

	};
}


#endif

