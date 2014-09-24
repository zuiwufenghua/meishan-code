#include "Parser.h"
#include <cstdio>
#include <iomanip>
#include <ctime>
using namespace std;

const string LEFT_DEP = "<-";
const string RGHT_DEP = "->";

namespace dparser {

	sp_thread_mutex_t Parser::_mutex;
	sp_thread_cond_t Parser::_cond_waiting_create;
	sp_thread_cond_t Parser::_cond_waiting_decode;
	sp_thread_cond_t Parser::_cond_done_decode;

	vector<bool> Parser::_train_features_created;
	int Parser::_train_creating_features_i;
	int Parser::_train_decoding_i;

	void Parser::process_options()
	{
		m_pipe_train.process_options();
		m_pipe_test.process_options();
		m_pipe_dev.process_options();
		m_fgen.process_options();

		_thread_num = 3;
		_train_parallel_creating_features_and_decoding = true;
		_train_creating_features_ahead = 3;

		_train = false;
		_test = false;
		_k_best_pos = 3;
		_inst_max_len_to_throw = 150;
		_inst_max_num_eval = -1;
		_inst_max_num_train = -1;
		_test_batch_size = 10000;

		_display_interval = 100;
		_verify_decoding_algorithm = true;
		_evaluate_model_score = false;

		_dictionary_path = ".";
		_parameter_path = ".";		
		_punctuation_tags.clear();

		_filename_train = "";
		_filename_dev = "";
		_iter_num = 20;
		_train_conservative_update = false;
		_train_use_pa = true;
		_train_use_seperate_pa = true;

		_dictionary_exist = false;
		_pamameter_exist = false;
		_param_tmp_num = -1;
		_param_all_exist = false;
		_train_add_gold_pos = true;
		_train_add_gold_head = true;
		_use_negative_features = false;
		_k_duplicate_positive_features = 1;
		_dealloc_fvec_prob = true;
		_eval_on_voted_parameters = false;

		_filename_test = "";
		_filename_output = "";
		_param_num_for_eval = -1;

		int tmp;
		string strtmp;

		_labeled = false;
		//_use_cpostags_to_filter_labels = false;
		_use_unlabeled_syn_features = false;
		if(options::get("labeled", tmp)) {
			_labeled = tmp;

			if (_labeled) {
				//if(options::get("use-cpostags-to-filter-labels", tmp)) {
				//	_use_cpostags_to_filter_labels = tmp;
				//	if (_use_cpostags_to_filter_labels)	cerr << "\n\tuse-cpostags-to-filter-labels\n" << endl;
				//}
				if(options::get("use-unlabeled-syn-features", tmp)) {
					_use_unlabeled_syn_features = tmp;
				}
			} else {
				_use_unlabeled_syn_features = true;
			}
		}

		_use_filtered_heads = false;
		//_use_cpostags_to_filter_dependencies = false;
  //      _use_cpostags_to_filter_dependencies_pattern_cutoff = 1;
		//if(options::get("use-filtered-heads", tmp)) {
		//	_use_filtered_heads = tmp;
		//	if (!_use_filtered_heads) {
		//		if(options::get("use-cpostags-to-filter-dependencies", tmp)) {
		//			_use_cpostags_to_filter_dependencies = tmp;
		//			if (_use_cpostags_to_filter_dependencies) {
		//		        if(options::get("use-cpostags-to-filter-dependencies-pattern-cutoff", tmp)) {
		//			        _use_cpostags_to_filter_dependencies_pattern_cutoff = tmp;
  //                      }

  //                      cerr << "\n\tuse-cpostags-to-filter-dependencies, cutoff: " << _use_cpostags_to_filter_dependencies_pattern_cutoff << endl << endl;
  //                  }
		//		}
		//	}
		//}

		if (options::get("train", tmp)) {
			_train = tmp;
		}
		if (options::get("thread-num", tmp)) {
			assert(tmp > 0);
			_thread_num = tmp;
		}
		if (options::get("train-parallel-creating-features-and-decoding", tmp)) {
			_train_parallel_creating_features_and_decoding = tmp;
		}
		if (options::get("train-creating-features-ahead", tmp)) {
			assert(tmp >= 0);
			_train_creating_features_ahead = tmp;
		}

		if (options::get("test", tmp)) {
			_test = tmp;
		}
		if(options::get("k-best-pos", tmp)) {
			_k_best_pos = tmp;
		}
		if(options::get("train-add-gold-pos", tmp)) {
			_train_add_gold_pos = tmp;
		}
		if(options::get("train-add-gold-head", tmp)) {
			_train_add_gold_head = tmp;
		}
		if(options::get("inst-max-len-to-throw", tmp)) {
			_inst_max_len_to_throw = tmp;
		}
		if(options::get("inst-max-num-train", tmp)) {
			_inst_max_num_train = tmp;
		}
		if(options::get("inst-max-num-eval", tmp)) {
			_inst_max_num_eval = tmp;
		}
		if(options::get("test-batch-size", tmp)) {
			if (tmp > 0) _test_batch_size = tmp;
		}
		if(options::get("display-interval", tmp)) {
			_display_interval = tmp;
		}
		if(options::get("use-negative-features", tmp)) {
			_use_negative_features = tmp;
		}
        if (_use_negative_features) {
		    if(options::get("k-duplicate-positive-features", tmp)) {
		        _k_duplicate_positive_features = tmp;
		        cerr << "k-dup-posfeat: " << _k_duplicate_positive_features << endl;
                assert(_k_duplicate_positive_features > 0);
		    }
        }
		if(options::get("dealloc-fvec-prob", tmp)) {
			_dealloc_fvec_prob = tmp;
		}

		if(options::get("dictionary-path", strtmp)) {
			_dictionary_path = strtmp;
		}
		if(options::get("parameter-path", strtmp)) {
			_parameter_path = strtmp;
		}

        const string default_punc_tags = "PU|,|.|:|;|\"|``|''|'|-LRB-|-RRB-";
		_punctuation_tags.clear();
		if(options::get("punctuation-tags", strtmp)) {
		} else {
            strtmp = default_punc_tags;
        }
        {
			vector<string> vec;
			simpleTokenize(strtmp, vec, "|");
			for (vector<string>::const_iterator it = vec.begin(); it != vec.end(); ++it) {
				_punctuation_tags.set(it->c_str(), 1);
			}
            cerr << "\n punctuation tags: ";
            StringMap<int>::const_iterator it = _punctuation_tags.begin();
            for (; it != _punctuation_tags.end(); ++it) cerr << it->first << " ";
            cerr << endl << endl;
        }

		if(options::get("dictionary-exist", tmp)) {
			_dictionary_exist = tmp;
		}
		if(options::get("param-all-exist", tmp)) {
			_param_all_exist = tmp;
			if (_param_all_exist) _dictionary_exist = 1;
		}

		if(options::get("parameter-exist", tmp)) {
			_pamameter_exist = tmp;
		}
		if(options::get("param-tmp-num", tmp)) {
			_param_tmp_num = tmp;
            if (_param_tmp_num <= 0) _param_tmp_num = 1;
		}

		if(options::get("train-file", strtmp)) {
			_filename_train = strtmp;
		}
		if(options::get("dev-file", strtmp)) {
			_filename_dev = strtmp;
		}
		if(options::get("iter-num", tmp)) {
			_iter_num = tmp;
		}

		if(options::get("evaluate-model-score", tmp)) {
			_evaluate_model_score = tmp;
		}
		if(options::get("eval-on-voted-parameters", tmp)) {
			_eval_on_voted_parameters = tmp;
		}

		if(options::get("train-conservative-update", tmp)) {
			_train_conservative_update = tmp;
		}
		if(options::get("train-use-pa", tmp)) {
			_train_use_pa = tmp;
		}
		if(options::get("train-use-seperate-pa", tmp)) {
			_train_use_seperate_pa = tmp;
		}


		if(options::get("test-file", strtmp)) {
			_filename_test = strtmp;
		}
		if(options::get("output-file", strtmp)) {
			_filename_output = strtmp;
		}
		if(options::get("param-num-for-eval", tmp)) {
			_param_num_for_eval = tmp;
		}
	}

	void Parser::train()
	{
		m_pipe_train.openInputFile( _filename_train.c_str() );
		m_pipe_train.getInstancesFromInputFile(_inst_max_num_train, _inst_max_len_to_throw, _train_add_gold_pos, _train_add_gold_head);
		m_pipe_train.closeInputFile();

		m_pipe_dev.openInputFile( _filename_dev.c_str() );
		m_pipe_dev.getInstancesFromInputFile(_inst_max_num_eval, _inst_max_len_to_throw);
		m_pipe_dev.closeInputFile();

		if (!_dictionary_exist) {
			create_dictionaries();
			save_dictionaries();
		}

		load_dictionaries();

		//if (_use_cpostags_to_filter_dependencies) {
		//	filter_dependencies(m_pipe_train, _train_add_gold_head);
		//	filter_dependencies(m_pipe_dev);
		//}

		//if (_labeled) {
		//	filter_labels(m_pipe_train);
		//	filter_labels(m_pipe_dev);
		//}

		assert(!m_decoder);
		m_decoder = new_decoder();

		if (_param_all_exist) {
			train_tmp_param_all_exist();
			return;
		}

		_number_processed = 0;
		int iter = 1;

		if (_pamameter_exist) {
			assert(_param_tmp_num > 0);

			load_parameters(_param_tmp_num);

			if (_eval_on_voted_parameters) {
				cerr << "\n\n +++++ eval using voted parameters: " << _param_tmp_num; print_time();
				evaluate(m_pipe_dev, false, false);
				cerr << "\n\n +++++ done "; print_time();
			}
			cerr << "\n\n +++++ eval using averaged parameters: " << _param_tmp_num; print_time();
			evaluate(m_pipe_dev, false, true);
			cerr << "\n\n +++++ done "; print_time();

			for (int i = 1; i < _param_tmp_num; ++i) {
				m_pipe_train.shuffleTrainInstances();
			}

			iter = _param_tmp_num + 1;
			_number_processed = m_param.get_time();

		} else {
			m_param.realloc(m_fgen.feature_dimentionality());
		}

		for(; iter <= _iter_num; iter++) {
			cerr << "\nIteration " << iter; print_time();
			train_one_iteration(iter);
			save_parameters(iter); // flush-average

			if (_eval_on_voted_parameters) {
				cerr << "\n\n +++++ eval using voted parameters: " << iter; print_time();
				evaluate(m_pipe_dev, false, false);
				cerr << "\n\n +++++ done "; print_time();
			}
			cerr << "\n\n +++++ eval using averaged parameters: " << iter; print_time();
			evaluate(m_pipe_dev, false, true);
			cerr << "\n\n +++++ done "; print_time();
		}
	}


	void Parser::train_one_iteration( const int iter_num )
	{
		m_pipe_train.shuffleTrainInstances();

		ctr_update_step_nega_pos = 0;
		ctr_update_step_nega_syn = 0;
		ctr_update_step_zero_pos = 0;
		ctr_update_step_zero_syn = 0;
		update_step_accumulate_pos = 0.0;
		update_step_accumulate_syn = 0.0;

		const int inst_num = m_pipe_train.getInstanceNum();
		if (_train_parallel_creating_features_and_decoding) {
			sp_thread_mutex_init( &_mutex, NULL );
			sp_thread_cond_init( &_cond_waiting_create, NULL );
			sp_thread_cond_init( &_cond_waiting_decode, NULL );
			sp_thread_cond_init( &_cond_done_decode, NULL );

			_train_creating_features_i = 0;
			_train_decoding_i = 0;
			_train_features_created.clear();
			_train_features_created.resize(inst_num, false);

			{
				sp_thread_t tid_create;
				train_creating_features_thread_arg * arg_create = new train_creating_features_thread_arg(this);

				if( 0 != sp_thread_create(&tid_create, 0, (sp_thread_func_t)train_creating_features_thread, (void *)arg_create) ) { // create a new thread
					printf( "\n\n-------- cannot create creating-features-thread -------- \n" );
					exit(-1);
				}
			}
			{
				sp_thread_t tid_decode;
				train_decoding_thread_arg * arg_decode = new train_decoding_thread_arg(this);
				if( 0 != sp_thread_create(&tid_decode, 0, (sp_thread_func_t)train_decoding_thread, (void *)arg_decode) ) { // create a new thread
					printf( "\n\n-------- cannot create decoding-thread -------- \n" );
					exit(-1);
				}
			}

			sp_thread_mutex_lock( &_mutex );
			while(_train_decoding_i < inst_num) {
				sp_thread_cond_wait(&_cond_done_decode, &_mutex);
				sp_thread_mutex_unlock( &_mutex );
			}

			sp_thread_mutex_destroy( &_mutex );
			sp_thread_cond_destroy( &_cond_waiting_create );
			sp_thread_cond_destroy( &_cond_waiting_decode );
			sp_thread_cond_destroy( &_cond_done_decode );

		} else {
			for (int i = 0; i < inst_num; ++i) {

				Instance *inst = m_pipe_train.getInstance(i);
				parse(m_decoder, inst, false);
				++_number_processed;

				sparsevec fv_dist_pos, fv_dist_syn;
				double pos_error, syn_error;
				get_fvdist_error(inst, fv_dist_pos, pos_error, fv_dist_syn, syn_error);

				double update_step_pos, update_step_syn;
				get_update_step(update_step_pos, update_step_syn, fv_dist_pos, pos_error, fv_dist_syn, syn_error);
				update_step_accumulate_pos += update_step_pos;
				update_step_accumulate_syn += update_step_syn;
				if (update_step_pos <= EPS) ++ctr_update_step_zero_pos;
				if (update_step_syn <= EPS) ++ctr_update_step_zero_syn;
				m_param.add(fv_dist_pos, _number_processed, update_step_pos);
				m_param.add(fv_dist_syn, _number_processed, update_step_syn);

				if(i % _display_interval == 0) cerr << i << " ";
				if (i % (_display_interval * 10) == 0) print_time();
			}
		}

		cerr.precision(8);
		cerr << "\ninstance num: " << inst_num;
		cerr << "\nEvarage update step pos: " << update_step_accumulate_pos / inst_num ;
		cerr << "\nEvarage update step syn: " << update_step_accumulate_syn / inst_num;
		cerr << "\n[0] update step pos num: " << ctr_update_step_zero_pos;
		cerr << "\n[0] update step syn num: " << ctr_update_step_zero_syn;
		cerr << "\n[-] update step pos num: " << ctr_update_step_nega_pos;
		cerr << "\n[-] update step syn num: " << ctr_update_step_nega_syn;
	}

	void Parser::train_tmp_param_all_exist()
	{
		for(int iter = _param_tmp_num; iter <= _iter_num; iter++) {
			cerr << "Iteration " << iter; print_time();

			load_parameters(iter);

			if (_eval_on_voted_parameters) {
				cerr << "\n\n +++++ eval using voted parameters...\n";
				evaluate(m_pipe_dev, false, false);
			}
			cerr << "\n\n +++++ eval using averaged parameters...\n";
			evaluate(m_pipe_dev, false, true);

			print_time();
		}
	}


	void Parser::evaluate( IOPipe &pipe, const bool test/*=false*/, const bool use_average/*=true*/ )
	{
		if (!test) reset_evaluate_metrics();

		const int inst_num = pipe.getInstanceNum();
		inst_num_processed_total += inst_num;

		if (_thread_num > 1) {
			for (int i = 0; i < inst_num ; ++i) {
				Instance *inst = pipe.getInstance(i);
				parse_thread_arg * arg = new parse_thread_arg(this, inst, use_average);
				dispatch_threadpool(_tp, parse_thread, (void *)arg);

				if(i % _display_interval == 0) cerr << i << " ";
				if (i % (_display_interval * 10) == 0) print_time();
			}

			wait_all_jobs_done(_tp);

			for (int i = 0; i < inst_num ; ++i) {
				Instance *inst = pipe.getInstance(i);
				evaluate_one_instance(inst);
				if (test) {
					if (_labeled) m_fgen.assign_predicted_deprels_str(inst);
					pipe.writeInstance(inst);
					//inst->clear();
				}
			}
		} else {
			for (int i = 0; i < inst_num; ++i) {
				Instance *inst = pipe.getInstance(i);
				parse(m_decoder, inst, use_average);

				evaluate_one_instance(inst);

				if (test) {
					if (_labeled) m_fgen.assign_predicted_deprels_str(inst);
					pipe.writeInstance(inst);
					//inst->clear();
				}

				if(i % _display_interval == 0) cerr << i << " ";
				if (i % (_display_interval * 10) == 0) print_time();
			}
		}

		cerr << "\ninstance num: " << inst_num; print_time();
		if (!test) output_evaluate_metrics();
	}

	void Parser::reset_evaluate_metrics() {
		inst_num_processed_total = 0;
		word_num_total = 0;
		word_num_dep_correct = 0;
		word_num_rel_correct = 0;
		sent_num_root_corect = 0;
		sent_num_CM = 0;
		sent_num_LCM = 0;

		oov_num = 0;
		oov_pos_correct_num = 0;
		word_punc_num_pos_correct = 0;
		word_punc_num_total = 0;
		pos_correct_k_best.clear();
		pos_correct_k_best.resize(_k_best_pos, 0);

		tot_score_pos_gold = 0;
		tot_score_syn_gold = 0;
		tot_score_pos_sys = 0;
		tot_score_syn_sys = 0;
		tot_pos_feature_num = 0;
		tot_syn_feature_num = 0;
	}

	void Parser::output_evaluate_metrics() {
		if (_evaluate_model_score) {
			cerr.precision(5);
			cerr << "average score pos gold : \t"  << tot_score_pos_gold/inst_num_processed_total << endl;
			cerr << "average score syn gold : \t"  << tot_score_syn_gold/inst_num_processed_total << endl;
			cerr << "average score pos sys  : \t"  << tot_score_pos_sys /inst_num_processed_total << endl;
			cerr << "average score syn sys  : \t"  << tot_score_syn_sys /inst_num_processed_total << endl;
			cerr << "average pos feat  num  : \t"  << tot_pos_feature_num /inst_num_processed_total << endl;
			cerr << "average syn feat  num  : \t"  << tot_syn_feature_num /inst_num_processed_total << endl;
		}

		// ================ evaluate res ================

		cerr.precision(5);
		cerr << "CM (excluding punc): \t\t"  << sent_num_CM << "/" << inst_num_processed_total << " = " << sent_num_CM*100.0/inst_num_processed_total << endl;
		cerr << "UAS (excluding punc):  \t\t" << word_num_dep_correct << "/" << word_num_total << " = " << word_num_dep_correct*100.0/word_num_total << endl;
		if (_labeled) {
			cerr << "LCM (excluding punc): \t\t"  << sent_num_LCM << "/" << inst_num_processed_total << " = " << sent_num_LCM*100.0/inst_num_processed_total << endl;
			cerr << "LAS (excluding punc):  \t\t" << word_num_rel_correct << "/" << word_num_total << " = " << word_num_rel_correct*100.0/word_num_total << endl;
		}
		cerr << "ROOT (excluding punc):  \t\t" << sent_num_root_corect << "/" << inst_num_processed_total << " = " << sent_num_root_corect*100.0/inst_num_processed_total << endl;
		cerr << "POS Precision:  \t\t" << word_punc_num_pos_correct << "/" << word_punc_num_total << " = " << word_punc_num_pos_correct*100.0/word_punc_num_total << endl;
		cerr << "POS Prec(OOV):  \t\t" << oov_pos_correct_num << "/" << oov_num << " = " << (oov_num > 0 ? oov_pos_correct_num*100.0/oov_num : 0.0) << endl;
		for (int k = 0; k < _k_best_pos; ++k) {
			cerr << k+1 << "-best POS Precision:  \t\t" << pos_correct_k_best[k] << "/" << word_punc_num_total << " = " << pos_correct_k_best[k]*100.0/word_punc_num_total << endl;
		}
	}

	void Parser::test(const int iter)
	{
		assert(iter >= 1);
		cerr << "\n\n eval using averaged parameters: " << iter; print_time();

		m_pipe_test.openInputFile( _filename_test.c_str() );
		m_pipe_test.openOutputFile( _filename_output.c_str() );

		if (!_train) {
			load_dictionaries();
			assert(!m_decoder);
			m_decoder = new_decoder();
		}

		//if (_use_cpostags_to_filter_dependencies) {
		//	filter_dependencies(m_pipe_test);
		//}
		//if (_labeled) {
		//	filter_labels(m_pipe_test);
		//}

		load_parameters(iter);

		reset_evaluate_metrics();
		while (1) {
			const int inst_num_left = _inst_max_num_eval < 0 ? _test_batch_size : (_inst_max_num_eval - inst_num_processed_total);
			if (inst_num_left <= 0) break;

			m_pipe_test.getInstancesFromInputFile(		
				_test_batch_size < inst_num_left ? _test_batch_size : inst_num_left,
				_inst_max_len_to_throw);
			if (m_pipe_test.getInstanceNum() <= 0) break;

			evaluate(m_pipe_test, true);
		}
		cerr << "done";  print_time();
		output_evaluate_metrics();

		m_pipe_test.closeInputFile();
		m_pipe_test.closeOutputFile();
	}

	void set_all_labels( set<int> &labels, const int label_num)
	{
		labels.clear();
		for (int i = 0; i < label_num; ++i)
			labels.insert(i);
	}

	//void set_filtered_labels(const set<int> &labels, vector<int> &candidate_labels) {
	//	candidate_labels.clear();
	//	candidate_labels.resize(labels.size());
	//	candidate_labels.insert(candidate_labels.begin(), labels.begin(), labels.end());
	//}

	//void Parser::filter_dependencies(IOPipe &pipe, const bool add_gold_head/* = false*/) {
	//	cerr << "\n filtering dependencies for " << pipe.in_file_name(); print_time();
	//	int number_total_dependencies = 0;
	//	int number_survived_dependencies = 0;
	//	int number_words = 0;
	//	int number_words_with_right_head = 0;

	//	for (int i = 0; i < pipe.getInstanceNum(); ++i) {

	//		Instance *inst = pipe.getInstance(i);
	//		const int length = inst->size();
	//		number_words += length - 1;
	//		number_total_dependencies += (length-1) * (length-1);

	//		inst->candidate_heads = false;

	//		for (int node_i = 1; node_i < length; ++node_i) {

	//			int number_heads = 0;

	//			// modify 0, left-side adjacent, right-side adjacent
	//			inst->candidate_heads[node_i][0] = true; ++number_heads;
	//			if (node_i > 1) {
	//				inst->candidate_heads[node_i][node_i-1] = true; ++number_heads;
	//			}
	//			if (node_i < length-1) {
	//				inst->candidate_heads[node_i][node_i+1] = true; ++number_heads;
	//			}

	//			for (int head_i = 0; head_i < length; ++head_i) {
	//				if (head_i == node_i) continue;
	//				if (inst->candidate_heads[node_i][head_i]) continue;

	//				StringMap< StringMap<int> > &dict = node_i < head_i ? _cpos_as_leftside_modifier_2_head_cposs : _cpos_as_rightside_modifier_2_head_cposs;
	//				for (int node_pi = 0; node_pi < inst->p_postags_num[node_i]; ++node_pi) {
	//					if (inst->candidate_heads[node_i][head_i]) break;

	//					StringMap<int> * dict_pi = dict.get( (inst->p_postags[node_i][node_pi]).c_str() );
	//					if (0 == dict_pi) continue;

	//					for (int head_pi = 0; head_pi < inst->p_postags_num[head_i]; ++head_pi) {
	//						if(0 != dict_pi->get( inst->p_postags[head_i][head_pi].c_str() ) ) {
	//							inst->candidate_heads[node_i][head_i] = true;
	//							++number_heads;
	//							break;
	//						}
 //                           //break; // only use the 1-best cpos
	//					}
 //                       //break; // only use the 1-best cpos
	//				}
	//			}

	//			//if (0 == number_heads) {
	//			//	for (int head_i = 0; head_i < length; ++head_i) {
	//			//		if (head_i == node_i) continue;
	//			//		inst->candidate_heads[node_i][head_i] = true;
	//			//	}
	//			//	number_heads = length;
	//			//}

	//			number_survived_dependencies += number_heads;

	//			if (inst->candidate_heads[node_i][ inst->heads[node_i] ])
	//				++number_words_with_right_head;
	//			else
	//				if (add_gold_head) inst->candidate_heads[node_i][ inst->heads[node_i] ] = true;
	//		}
	//	}

	//	cerr << "\n filter statistics:\n" << endl;
	//	cerr << setprecision(3);
	//	cerr << "survived / total: (" << number_survived_dependencies << "/" << number_total_dependencies << ") "  << 1.0*number_survived_dependencies/number_total_dependencies << endl;
	//	cerr << "dep       oracle: (" << number_words_with_right_head << "/" << number_words << ") " << 1.0*number_words_with_right_head/number_words << endl;
	//	cerr << "done"; print_time();
	//}


	//void Parser::filter_labels(IOPipe &pipe) {
	//	assert(_labeled);
	//	cerr << "\n filtering labels for " << pipe.in_file_name(); print_time();

	//	int number_dependencies = 0;
	//	int number_labels = 0;
	//	int number_words = 0;
	//	int number_words_with_right_head = 0;
	//	int number_words_with_right_label = 0;

	//	for (int i = 0; i < pipe.getInstanceNum(); ++i) {

	//		Instance *inst = pipe.getInstance(i);
	//		const int length = inst->size();
	//		number_words += length - 1;

	//		inst->candidate_labels.resize(length, length);
	//		inst->candidate_labels = vector<int>();

	//		for (int node_i = 1; node_i < length; ++node_i) {
	//			for (int head_i = 0; head_i < length; ++head_i) {
	//				if (head_i == node_i || !inst->candidate_heads[node_i][head_i]) continue;

	//				vector<int> &candidate_labels = inst->candidate_labels[node_i][head_i];
	//				set<int> labels_accumulated;

	//				if (_use_cpostags_to_filter_labels) {
	//					const string &dir = node_i < head_i ? LEFT_DEP : RGHT_DEP;

	//					for (int node_pi = 0; node_pi < inst->p_postags_num[node_i]; ++node_pi) {
	//						for (int head_pi = 0; head_pi < inst->p_postags_num[head_i]; ++head_pi) {
	//							const string &cpos_c = inst->p_postags[node_i][node_pi];
	//							const string &cpos_h = inst->p_postags[head_i][head_pi];

	//							// search the filtering rules from fine-grained to coarse-grained
	//							string pattern = cpos_c + "_" + cpos_h + "_" + dir;
	//							const set<int> *labels_of_one_rule = _cpos_dependency_2_labels_id.get( pattern.c_str() );
	//							if (0 == labels_of_one_rule) {
	//								pattern = cpos_c + "_" + dir;
	//								labels_of_one_rule = _cpos_as_modifier_direction_2_labels_id.get( pattern.c_str() );
	//								if (0 == labels_of_one_rule) {
	//									pattern = cpos_c;
	//									labels_of_one_rule = _cpos_as_modifier_nodirection_2_labels_id.get( pattern.c_str() );
	//									if (0 == labels_of_one_rule) {
	//										set_all_labels(labels_accumulated, m_fgen.get_label_num());
	//										break;
	//									}
	//								}
	//							}
	//							assert (labels_of_one_rule);
	//							labels_accumulated.insert(labels_of_one_rule->begin(), labels_of_one_rule->end());
	//						}
	//					}
	//				} else {
	//					set_all_labels(labels_accumulated, m_fgen.get_label_num());
	//				}

	//				set_filtered_labels(labels_accumulated, candidate_labels);

	//				++number_dependencies;
	//				number_labels += candidate_labels.size();
	//				if (head_i == inst->heads[node_i]) {
	//					++number_words_with_right_head;
	//					if ( labels_accumulated.end() != labels_accumulated.find( m_fgen.get_label_id(inst->deprels[node_i]) ) ) {
	//						++number_words_with_right_label;
	//					}
	//				}
	//			}				
	//		}
	//	}

	//	cerr << "\n filter statistics:\n" << endl;
	//	cerr << setprecision(3);
	//	cerr << "#label / #dependency: (" << number_labels << "/" << number_dependencies << ") "  << 1.0*number_labels/number_dependencies << endl;
	//	cerr << "dependency    oracle: (" << number_words_with_right_head << "/" << number_words << ") " << 1.0*number_words_with_right_head/number_words << endl;
	//	cerr << "label         oracle: (" << number_words_with_right_head << "/" << number_words << ") " << 1.0*number_words_with_right_head/number_words << endl;
	//	cerr << "done"; print_time();
	//}

	void Parser::create_dictionaries()
	{
		cerr << "\ncreating dictionaries..."; print_time();
		m_fgen.start_generation_mode();

		cerr << "\ncollect postag/label from: " << _filename_dev; print_time();
		for (int i = 0; i < m_pipe_dev.getInstanceNum(); ++i) {
			m_fgen.collect_word_postag_label( m_pipe_dev.getInstance(i), false );
		}
		cerr << "\ndone! instance num: " << m_pipe_dev.getInstanceNum(); print_time();

		for (int i = 0; i < m_pipe_train.getInstanceNum(); ++i) {
			Instance *inst = m_pipe_train.getInstance(i);

			m_fgen.collect_word_postag_label( inst, true );

            for (int k = 0; k < _k_duplicate_positive_features; ++k) {
				sparsevec fv;
			    m_fgen.create_all_pos_features_according_to_tree(inst, fv);
			    m_fgen.create_all_syn_features_according_to_tree(inst, inst->heads, inst->deprels_int, fv);
            }

			if (_use_negative_features) {
				m_fgen.alloc_fvec_prob(inst);
				m_fgen.create_all_feature_vectors(inst);
				m_fgen.dealloc_fvec_prob(inst);
			}

			if (i % _display_interval == 0) cerr << i << " ";
			if (i % (_display_interval * 10) == 0) print_time();
		}

		m_fgen.stop_generation_mode();
		cerr << "\ninstance num: " << m_pipe_train.getInstanceNum() << endl;
		cerr << "create dictionaries done"; print_time();

		//if (_use_cpostags_to_filter_labels || _use_cpostags_to_filter_dependencies) {
		//	create_dictionaries_for_filtering_dep_labels();
		//}
	}

	void Parser::eval_oov_pos( const Instance *inst, int &oov_num, int &oov_pos_correct_num )
	{
		for (int i = 1; i < inst->size(); ++i)
			if ( m_fgen.get_word_id(inst->forms[i]) < 0 ) {
				++oov_num;
				if (inst->orig_cpostags[i] == inst->p_postags[i][ inst->predicted_postags_idx[i] ])
					++oov_pos_correct_num;
			}
	}

	int Parser::error_num_pos( const Instance *inst ) const
	{
		int error_num = 0;			
		for (int i = 1; i < inst->size(); ++i)
			if (inst->orig_cpostags[i] != inst->p_postags[i][ inst->predicted_postags_idx[i] ])
				++error_num;
		return error_num;
	}

	void Parser::error_num_dp( const Instance *inst, const bool bIncludePunc, int &nDepError, int &nLabelError, int &nUnscoredToken, bool &bRootCorrect ) const
	{
		nDepError = 0;
		nLabelError = 0;
		bRootCorrect = false;
		nUnscoredToken = 0;
		for (int i = 1; i < inst->size(); ++i) {
			if (!bIncludePunc) {
				int tmp;
				if ( _punctuation_tags.get(inst->orig_cpostags[i].c_str(), tmp) ) {
					++nUnscoredToken;
					continue;
				}
			}

			if (0 == inst->predicted_heads[i] && 0 == inst->heads[i]) 
				bRootCorrect = true;

			if (inst->predicted_heads[i] != inst->heads[i])
				++nDepError;

			if (_labeled)
				if (inst->predicted_heads[i] == inst->heads[i] && inst->predicted_deprels_int[i] != inst->deprels_int[i])
					++nLabelError;
		}
	}

	void Parser::eval_k_best_pos_oracle( const Instance *pInst, vector<int> &pos_correct_k_best ) const
	{
		for (int i = 1; i < pInst->size(); ++i) {
			// k-best-each-word
			for (int k = 0; k < _k_best_pos; ++k) {
				bool is_correct = false;
				if (k >= pInst->p_postags_num[i]) break;
				if (pInst->orig_cpostags[i] == pInst->p_postags[i][ k ]) {
					is_correct = true;						
					for (int k2 = k; k2 < _k_best_pos; ++k2) { // considered as *correct* for all following k
						++pos_correct_k_best[k2];							
					}
					break;
				}					
			}
		}
	}

	void Parser::dot_all( const fvec * const fs, double * const probs, const int sz, const bool use_aver/*=false*/ ) const
	{
		for (int i = 0; i < sz; ++i) {
			if (fs[i].n >= 0)
				probs[i] = m_param.dot(fs+i, use_aver);
		}
	}

	void Parser::compute_all_probs( Instance *inst, const bool use_aver/*=false*/ ) const
	{
		if (_use_unlabeled_syn_features) { // dependency features
			NRMat<fvec> *mf2 = inst->fvec_dep.c_buf();
			NRMat<double> *mp2 = inst->prob_dep.c_buf();
			for (int i = 0; i < inst->fvec_dep.total_size(); ++i,++mf2,++mp2) { 
				if (mf2->total_size() > 0)
					dot_all(mf2->c_buf(), mp2->c_buf(), mf2->total_size(), use_aver);
			}
		}
		{ 
			NRMat3d<fvec> *mf3 = inst->fvec_depl.c_buf();
			NRMat3d<double> *mp3 = inst->prob_depl.c_buf();
			for (int i = 0; i < inst->fvec_depl.total_size(); ++i,++mf3,++mp3) { 
				if (mf3->total_size() > 0)
					dot_all(mf3->c_buf(), mp3->c_buf(), mf3->total_size(), use_aver);
			}
		}
		if (_use_unlabeled_syn_features) {
			NRMat3d<fvec> *mf3 = inst->fvec_sib.c_buf();
			NRMat3d<double> *mp3 = inst->prob_sib.c_buf();
			for (int i = 0; i < inst->fvec_sib.total_size(); ++i,++mf3,++mp3) { 
				if (mf3->total_size() > 0)
					dot_all(mf3->c_buf(), mp3->c_buf(), mf3->total_size(), use_aver);
			}
		}
		{
			NRMat4d<fvec> *mf4 = inst->fvec_sibl.c_buf();
			NRMat4d<double> *mp4 = inst->prob_sibl.c_buf();
			for (int i = 0; i < inst->fvec_sibl.total_size(); ++i,++mf4,++mp4) { 
				if (mf4->total_size() > 0)
					dot_all(mf4->c_buf(), mp4->c_buf(), mf4->total_size(), use_aver);
			}
		}
		if (_use_unlabeled_syn_features) {
			NRMat3d<fvec> *mf3 = inst->fvec_grd.c_buf();
			NRMat3d<double> *mp3 = inst->prob_grd.c_buf();
			for (int i = 0; i < inst->fvec_grd.total_size(); ++i,++mf3,++mp3) { 
				if (mf3->total_size() > 0)
					dot_all(mf3->c_buf(), mp3->c_buf(), mf3->total_size(), use_aver);
			}
		}
		{
			NRMat4d<fvec> *mf4 = inst->fvec_grdl.c_buf();
			NRMat4d<double> *mp4 = inst->prob_grdl.c_buf();
			for (int i = 0; i < inst->fvec_grdl.total_size(); ++i,++mf4,++mp4) { 
				if (mf4->total_size() > 0)
					dot_all(mf4->c_buf(), mp4->c_buf(), mf4->total_size(), use_aver);
			}
		}
		if (_use_unlabeled_syn_features) {
			NRMat4d<fvec> *mf4 = inst->fvec_grdsib.c_buf();
			NRMat4d<double> *mp4 = inst->prob_grdsib.c_buf();
			for (int i = 0; i < inst->fvec_grdsib.total_size(); ++i,++mf4,++mp4) { 
				if (mf4->total_size() > 0)
					dot_all(mf4->c_buf(), mp4->c_buf(), mf4->total_size(), use_aver);
			}
		}
		{
			for (int i = 1; i < inst->fvec_pos1.size(); ++i)
				if (inst->fvec_pos1[i].size() > 0)
					dot_all(inst->fvec_pos1[i].c_buf(), inst->prob_pos1[i].c_buf(), inst->fvec_pos1[i].size(), use_aver);
		}
		{
			for (int i = 1; i < inst->fvec_pos2.size(); ++i)
				if (inst->fvec_pos2[i].total_size() > 0)
					dot_all(inst->fvec_pos2[i].c_buf(), inst->prob_pos2[i].c_buf(), inst->fvec_pos2[i].total_size(), use_aver);
		}
	}

	void Parser::verify_decoding_algorithm( Instance * const inst )
	{
		sparsevec fv_dist = inst->predicted_fv;
		m_fgen.create_all_pos_features_according_to_tree(inst, fv_dist, -1.0, inst->predicted_postags_idx);
		m_fgen.create_all_syn_features_according_to_tree(inst, inst->predicted_heads, inst->predicted_deprels_int, fv_dist, -1.0, inst->predicted_postags_idx);

		bool not_matched = false;
		for (sparsevec::const_iterator it = fv_dist.begin(); it != fv_dist.end(); ++it) {
			if (it->second > ZERO || it->second < -ZERO) {
				not_matched = true;
				cerr << "[inst: " << inst->id << "] viterbi - according-to-tree: not matched feature: " << it->first << "\t value: " << it->second << endl;
			}
		}
		if (not_matched) {
			//freopen("log.txt", "w", stderr);
			//output_fv(inst, fv_according_to_tree);
			cerr << "[inst: " << inst->id << "] verify_decoding_algorithm error: two FeatureVec (by two ways: viterbi vs. according-to-tree) not matched" << endl;
			throw(1);
		}
	}

	void Parser::evaluate_one_instance( const Instance * const inst )
	{
		// ================ evaluate ================
		// evaluate the 1-best
		const int length = inst->size();
		word_punc_num_total += length-1;
		word_punc_num_pos_correct += length - 1 - error_num_pos(inst);
		eval_oov_pos(inst, oov_num, oov_pos_correct_num);

		bool bRootCorrect;
		int nDepError, nLabelError, nUnscoredToken;
		error_num_dp(inst, false, nDepError, nLabelError, nUnscoredToken, bRootCorrect);
		word_num_total += length - 1 - nUnscoredToken;
		word_num_dep_correct += length - 1 - nUnscoredToken - nDepError;
		if (bRootCorrect) ++sent_num_root_corect;
		if (0 == nDepError) ++sent_num_CM;
		if (_labeled) {
			word_num_rel_correct += length - 1 - nUnscoredToken - nDepError - nLabelError;
			if (0 == nDepError && 0 == nLabelError) ++sent_num_LCM;
		}

		eval_k_best_pos_oracle(inst, pos_correct_k_best);

		if (_evaluate_model_score) {
			tot_score_pos_gold += inst->score_pos_gold;
			tot_score_syn_gold += inst->score_syn_gold;
			tot_score_pos_sys += inst->score_pos_sys;
			tot_score_syn_sys += inst->score_syn_sys;
			tot_pos_feature_num += inst->sys_pos_feature_num;
			tot_syn_feature_num += inst->sys_syn_feature_num;
		}
	}

	void Parser::get_update_step( double &update_step_pos, double &update_step_syn, const sparsevec &fv_dist_pos, const double pos_error, const sparsevec &fv_dist_syn, const double syn_error )
	{
		update_step_pos = 1.0;
		update_step_syn = 1.0;
		if (_train_use_pa) {
			const double fScoreDistPOS = m_param.dot(fv_dist_pos, false);
			const double fScoreDistSyn = m_param.dot(fv_dist_syn, false);

			const double fNormPOS = parameters::sparse_sqL2(fv_dist_pos);
			const double fNormSyn = parameters::sparse_sqL2(fv_dist_syn);

			if (_train_use_seperate_pa) {
				if (fNormPOS <= EPS) {
					update_step_pos = 0;
				} else {
					update_step_pos = (pos_error - fScoreDistPOS) * 1.0 / (fNormPOS);
				}
				if (fNormSyn <= EPS) {
					update_step_syn = 0;
				} else {
					update_step_syn = (syn_error - fScoreDistSyn) * 1.0 / (fNormSyn);
				}
			} else {
				if (fNormPOS + fNormSyn <= EPS) {
					update_step_syn = 0;
				} else {
					update_step_syn = (pos_error + syn_error - fScoreDistPOS - fScoreDistSyn) * 1.0 / (fNormPOS + fNormSyn);
				}
				update_step_pos = update_step_syn;
			}
			if (update_step_pos > 1.0 || update_step_syn > 1.0) {
				cerr << "\n update step (pos vs. syn): " << update_step_pos << ":" << update_step_syn << endl;
                cerr << pos_error << " : " << syn_error << endl;
                cerr << fScoreDistPOS << " : " << fScoreDistSyn << endl;
                cerr << fNormPOS << " : " << fNormSyn << endl;
			}

			// it is possible that update_step_pos < 0 (maybe also for update_step_syn)
			// because: 1. the decoding algorithm find the joint optimal results, maybe dominated by the syntactic part.
			// 2. the gold POS tags may not contained in the search space.
			if (update_step_pos < -ZERO) {
				update_step_pos = 0.0;
				ctr_update_step_nega_pos++;
			}
			if (update_step_syn < -ZERO) {
				update_step_syn = 0.0;
				ctr_update_step_nega_syn++;
			}

			/* if ( !(update_step_pos >= -ZERO && update_step_syn >= -ZERO) ) {
			cerr.precision(8);
			cerr << update_step_pos << "\t" << update_step_syn << endl;
			cerr << nPOSError << "\t" << fScoreDistPOS << endl;
			cerr << nDepError << "\t" << fScoreDistSyn << endl;
			inst->write(cerr);
			}
			*/
		}
	}

	void Parser::get_fvdist_error( Instance * const inst, sparsevec &fv_dist_pos, double &pos_error, sparsevec &fv_dist_syn, double &syn_error )
	{
		fv_dist_pos.clear(); 
		fv_dist_syn.clear();

		m_fgen.create_all_pos_features_according_to_tree(inst, fv_dist_pos, 1.0);
		m_fgen.create_all_pos_features_according_to_tree(inst, fv_dist_pos, -1.0, inst->predicted_postags_idx);
		if (_train_conservative_update) {
			m_fgen.create_all_syn_features_according_to_tree(inst, inst->heads, inst->deprels_int, fv_dist_syn, 1.0, vector<int>(), inst->predicted_heads, inst->predicted_deprels_int);
			m_fgen.create_all_syn_features_according_to_tree(inst, inst->predicted_heads, inst->predicted_deprels_int, fv_dist_syn, -1.0, inst->predicted_postags_idx, inst->heads, inst->deprels_int);
		} else {
			m_fgen.create_all_syn_features_according_to_tree(inst, inst->heads, inst->deprels_int, fv_dist_syn, 1.0);
			m_fgen.create_all_syn_features_according_to_tree(inst, inst->predicted_heads, inst->predicted_deprels_int, fv_dist_syn, -1.0, inst->predicted_postags_idx);
		}

		int nUnscoredToken, nDepError, nLabelError; 
		bool isRootCorrect;
		const int nPOSError = error_num_pos(inst);
		error_num_dp(inst, true, nDepError, nLabelError, nUnscoredToken, isRootCorrect);

		pos_error = nPOSError;
		syn_error = nDepError + 0.5*nLabelError;
	}

	void Parser::parse_thread( void *arg )
	{
		parse_thread_arg * _arg = (parse_thread_arg *) arg;
		//cerr << "\n\tstart processing inst " << _arg->_inst->id << endl;
		Decoder *decoder = new_decoder();
		_arg->_parser->parse(decoder, _arg->_inst, _arg->_use_aver);
		delete_decoder(decoder);
		//cerr << "\n\tend   processing inst " << _arg->_inst->id << endl;
		delete _arg;
	}

	void Parser::train_creating_features_for_one_instance_thread( void *arg )
	{
		Parser *par = ((train_creating_features_for_one_instance_thread_arg *) arg)->_parser;
		const int inst_idx = ((train_creating_features_for_one_instance_thread_arg *) arg)->_inst_idx;
		delete ((train_creating_features_for_one_instance_thread_arg *) arg);
		Instance *inst = par->m_pipe_train.getInstance(inst_idx);

		if (par->_labeled) par->m_fgen.assign_deprels_int(inst);

		par->m_fgen.alloc_fvec_prob(inst);
		par->m_fgen.create_all_feature_vectors(inst);

		sp_thread_mutex_lock(&_mutex);
		_train_features_created[inst_idx] = true;
		sp_thread_cond_signal(&_cond_waiting_decode);
		//cerr << "C" << inst_idx << " ";
		sp_thread_mutex_unlock(&_mutex);
	}

	void Parser::train_creating_features_thread( void *arg )
	{
		Parser *par = ((train_creating_features_thread_arg *)arg)->_parser;
		const int inst_num = par->m_pipe_train.getInstanceNum();
		delete ((train_creating_features_thread_arg *)arg);

		for(; _train_creating_features_i < inst_num; ++_train_creating_features_i) {
			sp_thread_mutex_lock(&_mutex);
			while(_train_creating_features_i - _train_decoding_i > par->_train_creating_features_ahead ) {
				sp_thread_cond_wait(&_cond_waiting_create, &_mutex);
			}
			sp_thread_mutex_unlock(&_mutex);

			train_creating_features_for_one_instance_thread_arg * arg_one_instance= new train_creating_features_for_one_instance_thread_arg(par, _train_creating_features_i);
			dispatch_threadpool(par->_tp, train_creating_features_for_one_instance_thread, (void *)arg_one_instance);
		}
		wait_all_jobs_done(par->_tp);
	}

	void Parser::train_decoding_thread( void *arg )
	{
		Parser *par = ((train_decoding_thread_arg *)arg)->_parser;
		delete ((train_decoding_thread_arg *)arg);

		const int inst_num = par->m_pipe_train.getInstanceNum();
		
		for(; _train_decoding_i < inst_num; ++_train_decoding_i) {
			sp_thread_mutex_lock(&_mutex);
			while(!_train_features_created[_train_decoding_i]) {
				sp_thread_cond_wait(&_cond_waiting_decode, &_mutex);
			}
			sp_thread_mutex_unlock(&_mutex);
		
			Instance *inst = par->m_pipe_train.getInstance(_train_decoding_i);
			par->compute_all_probs(inst, false);
			par->m_decoder->decodeProjectiveInterface(inst);
			if (par->_labeled) par->m_fgen.convert_predicted_deprels_int(inst);
			if(par->_verify_decoding_algorithm) par->verify_decoding_algorithm(inst);
			
			inst->predicted_fv.clear();
			par->m_fgen.dealloc_fvec_prob(inst);

			++par->_number_processed;

			sparsevec fv_dist_pos, fv_dist_syn;
			double pos_error, syn_error;
			par->get_fvdist_error(inst, fv_dist_pos, pos_error, fv_dist_syn, syn_error);

			double update_step_pos, update_step_syn;
			par->get_update_step(update_step_pos, update_step_syn, fv_dist_pos, pos_error, fv_dist_syn, syn_error);
			par->update_step_accumulate_pos += update_step_pos;
			par->update_step_accumulate_syn += update_step_syn;
			if (update_step_pos <= EPS) ++par->ctr_update_step_zero_pos;
			if (update_step_syn <= EPS) ++par->ctr_update_step_zero_syn;
			par->m_param.add(fv_dist_pos, par->_number_processed, update_step_pos);
			par->m_param.add(fv_dist_syn, par->_number_processed, update_step_syn);

			if(_train_decoding_i % par->_display_interval == 0) cerr<< _train_decoding_i << " ";
			if (_train_decoding_i % (par->_display_interval * 10) == 0) print_time();

			//cerr << "D" << _train_decoding_i << " ";
			sp_thread_cond_signal(&_cond_waiting_create);
		}
		
		sp_thread_cond_signal(&_cond_done_decode);

	}



	//void Parser::save_dictionary_for_filtering_dep_labels( StringMap< StringMap<int> > &dict, const string &fileName )
	//{
	//	FILE* const f = gzfile::gzopen(fileName.c_str(), "w");
	//	int count = 0;

	//	cerr << "Dictionary for filtering dep and labels : saving to \"" << fileName
	//		<< "\" " << fixed << setprecision(1) << endl;
	//	cerr << "Dictionary : " << flush;
	//	for(StringMap< StringMap<int> >::const_iterator it = dict.begin(); it != dict.end(); ++it) {
	//		const StringMap<int> &val_freq = it->second;

	//		fprintf(f, "%s %d", it->first, val_freq.size());

	//		for(StringMap<int>::const_iterator j = val_freq.begin(); j != val_freq.end(); ++j) {
	//			fprintf(f, " %s %d", j->first, j->second);
	//			count ++;
	//		}
	//		fprintf(f, "\n");
	//	}

	//	gzfile::gzclose(fileName.c_str(), f);
	//	cerr << " " << count << " patterns" << endl;
	//	cerr << endl;
	//	dict.clear();
	//}

	//void Parser::load_dictionary_for_filtering_dep_labels( StringMap< StringMap<int> > &dict, const string &fileName, const int cutoff/* = 0*/ )
	//{
	//	dict.clear();

	//	FILE* const f = gzfile::gzopen(fileName.c_str(), "r");
	//	char* const buf = new char[16384];
	//	int cnt = 0;
	//	cerr << "Dictionary for filtering dep and labels loading from \"" << fileName
	//		<< "\" " << fixed << setprecision(1) << endl;
	//	cerr << "Dictionary : " << flush;

	//	while(1) {
	//		int n;
	//		const int nread = fscanf(f, "%16383s %d", buf, &n);
	//		if(nread <= 0 && feof(f)) { break; }
	//		else  { assert(nread == 2); }

	//		//cnt += n;
	//		assert( !dict.contains(buf) );
	//		dict.set(buf, StringMap<int>());

	//		StringMap<int> *val_freq = dict.get(buf);
	//		assert( val_freq );

	//		for (int i = 0; i < n; ++i) {
	//			int freq;
	//			const int nread = fscanf(f, "%16383s %d", buf, &freq);
	//			assert(nread == 2);
	//			assert( !val_freq->contains(buf) );
	//			if (freq > cutoff) {
 //                   val_freq->set(buf, freq);
 //                   ++cnt;
 //               }
	//		}
	//	}
	//	delete [] buf;

	//	gzfile::gzclose(fileName.c_str(), f);
	//	cerr << " " << cnt << " patterns" << endl;
	//	cerr << endl;
	//}


	//void Parser::add_dictionary_for_filtering_dep_labels( StringMap< StringMap<int> > &dict, const string &key, const string &val )
	//{
	//	if ( !dict.contains(key.c_str()) ){
	//		dict.set(key.c_str(), StringMap<int>());
	//	}
	//	StringMap<int> *val_freq = dict.get(key.c_str());
	//	assert( val_freq );
	//	if ( !val_freq->contains(val.c_str()) ) {
	//		val_freq->set(val.c_str(), 0);
	//	}
	//	int *freq = val_freq->get(val.c_str());
	//	assert ( freq );
	//	++(*freq);
	//}

	//void Parser::process_dictionary_for_filtering_labels( const StringMap< StringMap<int> > &dict, StringMap< set<int> > &dict_int )
	//{
	//	dict_int.clear();
	//	for(StringMap< StringMap<int> >::const_iterator it = dict.begin(); it != dict.end(); ++it) {
	//		const char *key = it->first;
	//		assert( !dict_int.contains(key) );

	//		const StringMap<int> &val_freq = it->second;
	//		int tot_cnt = 0;	// the total number of the pattern (e.g. NN_VV_<-)
	//		set<int> tmp_label_ids;
	//		for(StringMap<int>::const_iterator j = val_freq.begin(); j != val_freq.end(); ++j) {
	//			const char *label = j->first;
	//			tot_cnt += j->second;
	//			tmp_label_ids.insert(m_fgen.get_label_id( string(label) ));
	//		}
	//		//if (tot_cnt > 3) {
	//			dict_int.set(key, tmp_label_ids);
	//		//}
	//	}
	//}

	//void Parser::create_dictionaries_for_filtering_dep_labels()
	//{
	//	cerr << "\ncreating dictionaries for filtering dependencies and labels..."; print_time();

	//	//if (_use_cpostags_to_filter_labels) {
	//	//	_cpos_dependency_2_labels.clear();
	//	//	_cpos_as_modifier_direction_2_labels.clear();
	//	//	_cpos_as_modifier_nodirection_2_labels.clear();
	//	//}

	//	//if (_use_cpostags_to_filter_dependencies) {
	//	//	_cpos_as_leftside_modifier_2_head_cposs.clear();
	//	//	_cpos_as_rightside_modifier_2_head_cposs.clear();
	//	//}

	//	for (int inst_i = 0; inst_i < m_pipe_train.getInstanceNum(); ++inst_i) {
	//		Instance *inst = m_pipe_train.getInstance(inst_i);
	//		for (int node_i = 1; node_i < inst->size(); ++node_i) {
	//			const int head_i = inst->heads[node_i];
	//			assert(head_i >= 0 && head_i < inst->size() && head_i != node_i);

	//			const string &cpos_c = inst->cpostags[node_i];
	//			const string &cpos_h = inst->cpostags[head_i];
	//			const string &dir = node_i < head_i ? LEFT_DEP : RGHT_DEP;

	//			//if (_use_cpostags_to_filter_labels) {
	//			//	const string &deprel = inst->deprels[node_i];
	//			//	string cpos_dependency = cpos_c + "_" + cpos_h + "_" + dir;
	//			//	string cpos_as_modifer_direction = cpos_c + "_" + dir;
	//			//	string cpos_as_modifer_nodirection = cpos_c;
	//			//	add_dictionary_for_filtering_dep_labels(_cpos_dependency_2_labels, cpos_dependency, deprel);
	//			//	add_dictionary_for_filtering_dep_labels(_cpos_as_modifier_direction_2_labels, cpos_as_modifer_direction, deprel);
	//			//	add_dictionary_for_filtering_dep_labels(_cpos_as_modifier_nodirection_2_labels, cpos_as_modifer_nodirection, deprel);
	//			//}

	//			//if (_use_cpostags_to_filter_dependencies) {
	//			//	StringMap< StringMap<int> > &dict = node_i < head_i ? _cpos_as_leftside_modifier_2_head_cposs : _cpos_as_rightside_modifier_2_head_cposs;
	//			//	add_dictionary_for_filtering_dep_labels(dict, cpos_c, cpos_h);
	//			//}
	//		}

	//		if (inst_i % _display_interval == 0) cerr << inst_i << " ";
	//		if (inst_i % (_display_interval * 10) == 0) print_time();
	//	}

	//	cerr << "\ndone creating dictionaries for filtering dependencies and labels..."; print_time();
	//}


} // namespace dparser

