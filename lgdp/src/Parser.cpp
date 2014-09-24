#include "Parser.h"
#include <cstdio>
#include <iomanip>
#include <ctime>
using namespace std;

const string LEFT_DEP = "<-";
const string RGHT_DEP = "->";

namespace dparser {



	void Parser::process_options()
	{
		m_pipe_train.process_options();
		m_pipe_test.process_options();
		m_pipe_dev.process_options();
		m_fgen.process_options();

		_train = false;
		_test = false;
		_inst_max_len_to_throw = 40;
		_inst_max_num_eval = -1;
		_inst_max_num_train = -1;
		_test_batch_size = 10000;

		_display_interval = 100;
		_verify_decoding_algorithm = true;
		_evaluate_model_score = false;

    _kbest = 1;
    _output_confidence = false;
    _prune_times = 0.005;

		_dictionary_path = ".";
		_parameter_path = ".";		
		_punctuation_tags.clear();

		_filename_train = "";
		_filename_dev = "";
		_iter_num = 20;
		_train_use_pa = true;

		_dictionary_exist = false;
		_pamameter_exist = false;
		_param_tmp_num = -1;
		_param_all_exist = false;


		_filename_test = "";
		_filename_output = "";
		_param_num_for_eval = -1;

		int tmp;
		string strtmp;
    double ftmp;


		if (options::get("train", tmp)) {
			_train = tmp;
		}

		if (options::get("test", tmp)) {
			_test = tmp;
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

		if(options::get("dictionary-path", strtmp)) {
			_dictionary_path = strtmp;
		}
		if(options::get("parameter-path", strtmp)) {
			_parameter_path = strtmp;
		}

    const string default_punc_tags = "PU|,|.|:|;|\"|``|''|'|-LRB-|-RRB-";
		_punctuation_tags.clear();
		if(options::get("punctuation-tags", strtmp)) {
		} 
    else {
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

		if(options::get("train-use-pa", tmp)) {
			_train_use_pa = tmp;
		}


		if(options::get("test-file", strtmp)) {
			_filename_test = strtmp;
		}

    if(options::get("kbest", tmp)) {
			_kbest = tmp;
		}

    if (options::get("confidence", tmp)) {
			_output_confidence = tmp;
		}

    if (options::get("prune-times", ftmp)) {
			_prune_times = ftmp;
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
		m_pipe_train.getInstancesFromInputFile(_inst_max_num_train, _inst_max_len_to_throw);
		m_pipe_train.closeInputFile();

		m_pipe_dev.openInputFile( _filename_dev.c_str() );
		m_pipe_dev.getInstancesFromInputFile(_inst_max_num_eval, _inst_max_len_to_throw);
		m_pipe_dev.closeInputFile();

    m_pipe_test.openInputFile( _filename_test.c_str() );
		m_pipe_test.getInstancesFromInputFile(_inst_max_num_eval, _inst_max_len_to_throw);
		m_pipe_test.closeInputFile();

		if (!_dictionary_exist) {
			create_dictionaries();
			save_dictionaries();
		}

		load_dictionaries();

		assert(!m_decoder);

    m_decoder = new_decoder(m_fgen.get_label_num(), _kbest);

    //only for evaluate and find the best iter
		if (_param_all_exist) {
			train_tmp_param_all_exist();
			return;
		}

		_number_processed = 0;
		int iter = 1;

    // continuing train		
    if (_pamameter_exist) {
			assert(_param_tmp_num > 0);

			load_parameters(_param_tmp_num);

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

			cerr << "\n\n +++++ eval develop using averaged parameters: " << iter; print_time();
			evaluate(m_pipe_dev, false, _train_use_pa);
			cerr << "\n\n +++++ done "; print_time();

      cerr << "\n\n +++++ eval test using averaged parameters: " << iter; print_time();
			evaluate(m_pipe_test, false, _train_use_pa);
			cerr << "\n\n +++++ done "; print_time();
		}
	}


	void Parser::train_one_iteration( const int iter_num )
	{
		m_pipe_train.shuffleTrainInstances();



		update_step_accumulate_syn = 0.0;

		const int inst_num = m_pipe_train.getInstanceNum();

		for (int i = 0; i < inst_num; ++i) {

			Instance *inst = m_pipe_train.getInstance(i);
      if(_train_use_pa)
      {
			  parse(m_decoder, inst, false);
      }
      else
      {
        parse_with_confidence(m_decoder, inst, false);
      }

			++_number_processed;
			sparsevec  fv_dist_syn;
			double syn_error;
			get_fvdist_error(inst, fv_dist_syn, syn_error);

			double update_step_pos, update_step_syn;

      if(_train_use_pa)
      {
			  get_update_step(update_step_syn, fv_dist_syn, syn_error);
      }
      else
      {
        get_update_step(update_step_syn, fv_dist_syn, 0);
        //update_step_syn = 1- exp(inst->predicted_probs[0]);
        //cerr << "\nupdate_step_syn " << update_step_syn;
        update_step_syn = 1000/(1+i+(iter_num-1)*inst_num);

        
      }

			update_step_accumulate_syn += update_step_syn;

      
			m_param.add(fv_dist_syn, _number_processed, update_step_syn);

			if(i % _display_interval == 0) cerr << i << " ";
			if (i % (_display_interval * 10) == 0) print_time();
		}


		cerr.precision(8);
		cerr << "\ninstance num: " << inst_num;
		cerr << "\nEvarage update step syn: " << update_step_accumulate_syn / inst_num;
		cerr << "\n[0] update step syn num: " << ctr_update_step_zero_syn;
		cerr << "\n[-] update step syn num: " << ctr_update_step_nega_syn;
	}

	void Parser::train_tmp_param_all_exist()
	{
		for(int iter = _param_tmp_num; iter <= _iter_num; iter++) {
			cerr << "Iteration " << iter; print_time();

			load_parameters(iter);

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

		for (int i = 0; i < inst_num; ++i) {
			Instance *inst = pipe.getInstance(i);
      if(_output_confidence)
      {
        parse_with_confidence(m_decoder, inst, use_average);
      }
      else
      {
        if(_kbest == 1)
        {
			    parse(m_decoder, inst, use_average);
        }
        else
        {
          kbestparse(m_decoder, inst, use_average);
        }
      }

			evaluate_one_instance(inst);

			if (test) {
				m_fgen.assign_predicted_deprels_str(inst);
        if(!_output_confidence)
        {
				  pipe.writeInstance(inst);
        }
        else
        {
          pipe.writeInstance(inst, m_fgen.get_labels(), _prune_times);
        }
				//inst->clear();
			}

			if(i % _display_interval == 0) cerr << i << " ";
			if (i % (_display_interval * 10) == 0) print_time();
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

	}

	void Parser::output_evaluate_metrics() {

		// ================ evaluate res ================

		cerr.precision(5);
		cerr << "CM (excluding punc): \t\t"  << sent_num_CM << "/" << inst_num_processed_total << " = " << sent_num_CM*100.0/inst_num_processed_total << endl;
		cerr << "UAS (excluding punc):  \t\t" << word_num_dep_correct << "/" << word_num_total << " = " << word_num_dep_correct*100.0/word_num_total << endl;
		cerr << "LCM (excluding punc): \t\t"  << sent_num_LCM << "/" << inst_num_processed_total << " = " << sent_num_LCM*100.0/inst_num_processed_total << endl;
		cerr << "LAS (excluding punc):  \t\t" << word_num_rel_correct << "/" << word_num_total << " = " << word_num_rel_correct*100.0/word_num_total << endl;
		cerr << "ROOT (excluding punc):  \t\t" << sent_num_root_corect << "/" << inst_num_processed_total << " = " << sent_num_root_corect*100.0/inst_num_processed_total << endl;
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
			m_decoder = new_decoder(m_fgen.get_label_num(), _kbest);
		}


		load_parameters(iter);

		reset_evaluate_metrics();
		while (1) {
			const int inst_num_left = _inst_max_num_eval < 0 ? _test_batch_size : (_inst_max_num_eval - inst_num_processed_total);
			if (inst_num_left <= 0) break;

			m_pipe_test.getInstancesFromInputFile(		
				_test_batch_size < inst_num_left ? _test_batch_size : inst_num_left,
				_inst_max_len_to_throw);
			if (m_pipe_test.getInstanceNum() <= 0) break;

      //if()
			//evaluate(m_pipe_test, true);
      evaluate(m_pipe_test, true, true);
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


	void Parser::create_dictionaries()
	{
		cerr << "\ncreating dictionaries..."; print_time();
		m_fgen.start_generation_mode();


		for (int i = 0; i < m_pipe_train.getInstanceNum(); ++i) {
			Instance *inst = m_pipe_train.getInstance(i);

			m_fgen.collect_word_postag_label( inst, true );

  		//sparsevec fv;
	    //m_fgen.create_all_syn_features_according_to_tree(inst, inst->heads, inst->deprels_int, fv);

			m_fgen.alloc_fvec_prob(inst);
			m_fgen.create_all_feature_vectors(inst);
			m_fgen.dealloc_fvec_prob(inst);



			if (i % _display_interval == 0) cerr << i << " ";
			if (i % (_display_interval * 10) == 0) print_time();
		}

		m_fgen.stop_generation_mode();
		cerr << "\ninstance num: " << m_pipe_train.getInstanceNum() << endl;
		cerr << "create dictionaries done"; print_time();

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
				if ( _punctuation_tags.get(inst->cpostags[i].c_str(), tmp) ) {
					++nUnscoredToken;
					continue;
				}
			}

			if (0 == inst->predicted_heads[0][i] && 0 == inst->heads[i]) 
				bRootCorrect = true;

			if (inst->predicted_heads[0][i] != inst->heads[i])
				++nDepError;

			if (inst->predicted_heads[0][i] == inst->heads[i] && inst->predicted_deprels_int[0][i] != inst->deprels_int[i])
				++nLabelError;
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

    if(m_fgen._use_dependency)
    {
			NRVec<fvec> *mf = inst->fvec_depl.c_buf();
			NRVec<double> *mp = inst->prob_depl.c_buf();
			for (int i = 0; i < inst->fvec_depl.total_size(); ++i,++mf,++mp) { 
				if (mf->size() > 0)
					dot_all(mf->c_buf(), mp->c_buf(), mf->size(), use_aver);
			}
    }
		

    if(m_fgen._use_sibling)
    {
			NRVec<fvec> *mf = inst->fvec_sibl.c_buf();
			NRVec<double> *mp = inst->prob_sibl.c_buf();
			for (int i = 0; i < inst->fvec_sibl.total_size(); ++i,++mf,++mp) { 
				if (mf->size() > 0)
					dot_all(mf->c_buf(), mp->c_buf(), mf->size(), use_aver);
			}
    }


    if(m_fgen._use_grand)
    {
			NRVec<fvec> *mf = inst->fvec_grdl.c_buf();
			NRVec<double> *mp = inst->prob_grdl.c_buf();
			for (int i = 0; i < inst->fvec_grdl.total_size(); ++i,++mf,++mp) { 
				if (mf->size() > 0)
					dot_all(mf->c_buf(), mp->c_buf(), mf->size(), use_aver);
			}
    }
	}

	void Parser::verify_decoding_algorithm( Instance * const inst )
	{
		sparsevec fv_dist = inst->predicted_fvs[0];
    vector<int> predicted_heads;
    vector<int> predicted_deprels_int;
    for(int idx = 0; idx < inst->size(); idx++)
    {
      predicted_heads.push_back(inst->predicted_heads[0][idx]);
      predicted_deprels_int.push_back(inst->predicted_deprels_int[0][idx]);
    }
		m_fgen.create_all_syn_features_according_to_tree(inst, predicted_heads, predicted_deprels_int, fv_dist, -1.0);

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

		bool bRootCorrect;
		int nDepError, nLabelError, nUnscoredToken;
		error_num_dp(inst, false, nDepError, nLabelError, nUnscoredToken, bRootCorrect);
		word_num_total += length - 1 - nUnscoredToken;
		word_num_dep_correct += length - 1 - nUnscoredToken - nDepError;
		if (bRootCorrect) ++sent_num_root_corect;
		if (0 == nDepError) ++sent_num_CM;
		word_num_rel_correct += length - 1 - nUnscoredToken - nDepError - nLabelError;
		if (0 == nDepError && 0 == nLabelError) ++sent_num_LCM;


	}

	void Parser::get_update_step( double &update_step_syn, const sparsevec &fv_dist_syn, const double syn_error )
	{
		update_step_syn = 1.0;
    if (_train_use_pa) 
    {
			const double fScoreDistSyn = m_param.dot(fv_dist_syn, false);
			const double fNormSyn = parameters::sparse_sqL2(fv_dist_syn);

			if (fNormSyn <= EPS) {
				update_step_syn = 0;
			} else {
				update_step_syn = (syn_error- fScoreDistSyn) * 1.0 / fNormSyn;
			}
	
			if ( update_step_syn > 1.0) {
				cerr << "\n update step (pos vs. syn): "  << update_step_syn << endl;
        cerr <<  syn_error << endl;
        cerr <<  fScoreDistSyn << endl;
        cerr <<  fNormSyn << endl;
			}
    }
    else
    {
      /*
      const double fScoreDistSyn = m_param.dot(fv_dist_syn, false);
			const double fNormSyn = parameters::sparse_sqL2(fv_dist_syn);

			if (fNormSyn <= EPS) {
				update_step_syn = 0;
			} else {
				update_step_syn =  1.0 / fNormSyn;
			}
	    
			if ( update_step_syn > 1.0) {
				cerr << "\n update step (pos vs. syn): "  << update_step_syn << endl;
        cerr <<  syn_error << endl;
        cerr <<  fScoreDistSyn << endl;
        cerr <<  fNormSyn << endl;
			}*/
      update_step_syn = 1.0;
    }
	}

	void Parser::get_fvdist_error( Instance * const inst, sparsevec &fv_dist_syn, double &syn_error )
	{
		fv_dist_syn.clear();



		m_fgen.create_all_syn_features_according_to_tree(inst, inst->heads, inst->deprels_int, fv_dist_syn, 1.0);
    if(_train_use_pa)
    {
      vector<int> predicted_heads;
      vector<int> predicted_deprels_int;
      for(int idx = 0; idx < inst->size(); idx++)
      {
        predicted_heads.push_back(inst->predicted_heads[0][idx]);
        predicted_deprels_int.push_back(inst->predicted_deprels_int[0][idx]);
      }
		  m_fgen.create_all_syn_features_according_to_tree(inst, predicted_heads, predicted_deprels_int, fv_dist_syn, -1.0);
    }
    else
    {
      m_fgen.create_all_feature_vectors_margin(inst, fv_dist_syn, -1.0);
    }

		int nUnscoredToken, nDepError, nLabelError; 
		bool isRootCorrect;
		error_num_dp(inst, true, nDepError, nLabelError, nUnscoredToken, isRootCorrect);
		syn_error = nDepError + 0.5*nLabelError;
	}



} // namespace dparser

