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
#include "Decoder_1okbest.h"
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

		// options
		int _display_interval;

		string _dictionary_path;
		string _parameter_path;
		int _inst_max_len_to_throw;
		StringMap<int> _punctuation_tags;

		bool _train;
		int _iter_num;
		bool _train_use_pa;  // if true, use max-marginal, else use max likelihood

		string _filename_train;
		string _filename_dev;
		int _inst_max_num_train;
		bool _dictionary_exist;
		bool _pamameter_exist;
		int _param_tmp_num;
		bool _param_all_exist;

		bool _test;
		string _filename_test;
		string _filename_output;
		int _param_num_for_eval;
		int _inst_max_num_eval;
		int _test_batch_size;

		bool _verify_decoding_algorithm;

		bool _evaluate_model_score;

    int _kbest;
    bool _output_confidence;
    double _prune_times; // if current arc confidence is lower than _prune_times of the best arc, then current arc will be pruned


/* variables used in train */
		int _number_processed;

		int ctr_update_step_nega_syn;
		int ctr_update_step_zero_syn;
		double update_step_accumulate_syn;

/* variables used in evaluate */
		int inst_num_processed_total;
		int word_num_total;
		int word_num_dep_correct;
		int word_num_rel_correct;
		int sent_num_root_corect;
		int sent_num_CM; // complete match
		int sent_num_LCM;


	public:
		Parser() : m_decoder(0) {
			process_options();
		}

		~Parser(void) {
			delete_decoder(m_decoder);
		}

		void process_options();

		void run()
		{
			if (_train) train();
			if (_test) test(_param_num_for_eval);
		}

		static Decoder *new_decoder( int L, int K) {
			Decoder *decoder = 0;
			string decoder_name;

			if (!options::get("decoder", decoder_name)) {
				usage("please provide the decoder type: 1o/2o"); exit(1);
			}
			if (decoder_name == "1o") {
				decoder = new Decoder_1o();
			}
      else if (decoder_name == "1okbest") 
      {
        decoder = new Decoder_1okbest();
      }
      else {
				usage("other decoder than 1o is not implemented."); exit(1);
			}
			assert(decoder);

      decoder->set_labelnum(L);
      decoder->set_kbestnum(K);

			return decoder;
		}

		static void delete_decoder(Decoder *&decoder) {
			if (decoder) {
				delete decoder;
				decoder = 0;
			}
		}

	public:


		void parse(Decoder *decoder, Instance *inst, const bool use_average=false) {
			
      m_fgen.assign_deprels_int(inst);
    	m_fgen.alloc_fvec_prob(inst);
			m_fgen.create_all_feature_vectors(inst);
			compute_all_probs(inst, use_average);

      decoder->decodeProjectiveInterface(inst);
			if(_verify_decoding_algorithm) verify_decoding_algorithm(inst);
			if (_evaluate_model_score) evaluate_model_score(inst, use_average);

			inst->predicted_fvs.dealloc();
			m_fgen.dealloc_fvec_prob(inst);
		}

    void kbestparse(Decoder *decoder, Instance *inst, const bool use_average=false) {
			
      m_fgen.assign_deprels_int(inst);
    	m_fgen.alloc_fvec_prob(inst);
			m_fgen.create_all_feature_vectors(inst);
			compute_all_probs(inst, use_average);

			decoder->decodeProjectiveInterfaceKbest(inst, _kbest);
			if(_verify_decoding_algorithm) verify_decoding_algorithm(inst);
			if (_evaluate_model_score) evaluate_model_score(inst, use_average);

			inst->predicted_fvs.dealloc();
			m_fgen.dealloc_fvec_prob(inst);
		}

     void parse_with_confidence(Decoder *decoder, Instance *inst, const bool use_average=false) {
			
      m_fgen.assign_deprels_int(inst);
    	m_fgen.alloc_fvec_prob(inst);
			m_fgen.create_all_feature_vectors(inst);
			compute_all_probs(inst, use_average);

			decoder->decodeProjectiveInterfaceConfidence(inst);
			if(_verify_decoding_algorithm) verify_decoding_algorithm(inst);
			if (_evaluate_model_score) evaluate_model_score(inst, use_average);

			inst->predicted_fvs.dealloc();
			m_fgen.dealloc_fvec_prob(inst);
		}
		


		void train();
		void test(const int iter);

		void train_one_iteration(const int iter_num);
		void train_tmp_param_all_exist();
		void evaluate(IOPipe &pipe, const bool test=false, const bool use_average=true);
		void reset_evaluate_metrics();
		void output_evaluate_metrics();


		void create_dictionaries();

		void load_dictionaries() {
			m_fgen.load_dictionaries(_dictionary_path);
		}

		void save_dictionaries() {
			m_fgen.save_dictionaries(_dictionary_path);
		}

		void save_parameters(const int iter) {
			m_param.flush_avg(_number_processed);
			m_param.save(_parameter_path, iter, false);
			m_param.save(_parameter_path, iter, true);
		}

		void load_parameters(const int iter) {
			m_param.load(_parameter_path, iter, true);
			m_param.load(_parameter_path, iter, false);

		}

		void dot_all(const fvec * const fs, double * const probs, const int sz, const bool use_aver=false) const;

		void compute_all_probs(Instance *inst, const bool use_aver = false) const;

		void verify_decoding_algorithm( Instance * const inst);
		void evaluate_model_score( Instance * const inst, const bool use_aver = true)
		{
			sparsevec fv_syn_gold;
			sparsevec fv_syn_sys;

      vector<int> predicted_heads;
      vector<int> predicted_deprels_int;
      for(int idx = 0; idx < inst->size(); idx++)
      {
        predicted_heads.push_back(inst->predicted_heads[0][idx]);
        predicted_deprels_int.push_back(inst->predicted_deprels_int[0][idx]);
      }
			m_fgen.create_all_syn_features_according_to_tree(inst, inst->heads, inst->deprels_int, fv_syn_gold, 1.0);
			m_fgen.create_all_syn_features_according_to_tree(inst, predicted_heads, predicted_deprels_int, fv_syn_sys, 1.0);

			inst->sys_syn_feature_num = fv_syn_sys.size();

			inst->score_syn_gold = m_param.dot(fv_syn_gold, use_aver);
			inst->score_syn_sys = m_param.dot(fv_syn_sys, use_aver);
		}


		void evaluate_one_instance(const Instance * const inst);
		void get_update_step( double &update_step_syn, const sparsevec &fv_dist_syn, const double syn_error);
		void get_fvdist_error(Instance * const inst, sparsevec &fv_dist_syn, double &syn_error);


		void error_num_dp(const Instance *inst, const bool bIncludePunc, int &nDepError, int &nLabelError, int &nUnscoredToken, bool &bRootCorrect) const;


	};
}


#endif

