#include "IOPipe.h"
#include <iterator>

#include "CharUtils.h"

using namespace std;
using namespace egstra;


namespace dparser {

	void IOPipe::preprocessInstance( Instance *inst, const bool add_gold_pos, const bool add_gold_head )
	{
		const int length = inst->size();

		inst->p_postags.resize(length);
		inst->p_postags_num.resize(length);
		inst->p_postags[0].resize(1);
		inst->p_postags[0][0] = inst->cpostags[0];
		inst->p_postags_num[0] = 1;

		if (_use_chars) {
			inst->chars.resize(length);
		}

		if (_use_lemma) {
			inst->lemmas.resize(length);
			inst->lemmas[0] = ROOT_LEMMA;
		}

		if (_english) {
			inst->contain_hyphen.resize(length);
			inst->contain_number.resize(length);
			inst->contain_uppercase_char.resize(length);
		}

		inst->candidate_heads.resize(length, length);
		if (_use_filtered_heads) 
			inst->candidate_heads = false;
		else 
			inst->candidate_heads = true;

		if (_use_filtered_labels) {
			inst->candidate_labels.resize(length, length);
			inst->candidate_labels = vector<string>();
		}
		
		for (int i = 1; i < length; ++i) {

			if (_copy_cpostag_from_postag) {
                inst->orig_cpostags[i] = inst->postags[i];
				inst->cpostags[i] = inst->postags[i];
			}

			// set characters
			vector<string> vec;
			if (_use_chars) {
				split_bystr(inst->orig_lemmas[i], vec, "##");
				inst->chars[i] = vec;
			}

			if (_use_lemma) {
				if (_english) {
					string form_lc = toLower(inst->forms[i]);
					inst->lemmas[i] = ( form_lc.length()<=5 ? form_lc : form_lc.substr(0,5) );
				} else {
					assert(!inst->chars[i].empty());
					inst->lemmas[i] = inst->chars[i].back();
				}
			}

			if (_english) {
				const string &form = inst->forms[i];
				inst->contain_hyphen[i] = contain_hyphen(form) ? "hyp=y" : "hyp=n";
				inst->contain_number[i] = contain_number(form) ? "num=y" : "num=n";
				inst->contain_uppercase_char[i] = contain_uppercase_character(form) ? "upc=y" : "upc=n";
			}

			if (_use_filtered_heads) {
				simpleTokenize(inst->orig_feats[i], vec, "_");
				if (vec.empty()) {
					cerr << "IOPipe::preprocessInstance: empty filtered heads!" << endl;
					exit(-1);
				}
				const bool contain_fltd_labels = (']' == vec[0][ vec[0].size()-1 ]);
				if (_use_filtered_labels) assert(contain_fltd_labels);

				for (int j = 0; j < vec.size(); ++j) {
					if (contain_fltd_labels) {
						const string head_deprel = vec[j].substr(0, vec[j].size()-1);
						vector<string> pair_head_deprel;
						simpleTokenize(head_deprel, pair_head_deprel, "[");
						assert(pair_head_deprel.size() == 2);
						const int head_id = toInteger(pair_head_deprel[0]);
						assert(head_id >= 0 && head_id < length);
						inst->candidate_heads[i][head_id] = true;
						if (_use_filtered_labels) {
							vector<string> cand_deprels;
							simpleTokenize(pair_head_deprel[1], cand_deprels, "|");
							assert(!cand_deprels.empty());
							inst->candidate_labels[i][head_id] = cand_deprels;
						}
					} else {
						const int head_id = toInteger(vec[j]);
						assert(head_id >= 0 && head_id < length);
						inst->candidate_heads[i][head_id] = true;
					}
				}
				const int gold_head_id = inst->heads[i];
				if (add_gold_head && !inst->candidate_heads[i][ gold_head_id ]) {
					inst->candidate_heads[i][ gold_head_id ] = true;
					if (_use_filtered_labels) {
						assert(inst->candidate_labels[i][ gold_head_id ].empty());
						inst->candidate_labels[i][ gold_head_id ].push_back( inst->deprels[i] );
					}
				}
			}
			
			{ /* set candidate postag lists */
				if (_simulate_pipeline) {
					if (_get_cpostag_from_pdeprel) {
						//assert(!_copy_cpostag_from_postag);
						simpleTokenize(inst->pdeprels[i], vec, "_");
						if (vec.empty()) {
							cerr << "candidate pos list [pdeprel] empty(): node_i = " << i << endl;
							exit(-1);
						}
						vec.erase(vec.begin() + 1, vec.end());
						inst->cpostags[i] = vec[0]; // NOTE: also change the reference postag.

					} else {
						vec.clear();
						vec.push_back(inst->cpostags[i]); // NOTE: use the reference postag.
					}

				} else {
					simpleTokenize(inst->pdeprels[i], vec, "_");
					if (vec.empty()) {
						cerr << "candidate pos list empty(): node_i = " << i << endl;
						exit(-1);
					}
					if (vec.size() > _k_best_pos) {
						vec.erase(vec.begin() + _k_best_pos, vec.end());
					}
					if (add_gold_pos) {
						if ( vec.end() == find(vec.begin(), vec.end(), inst->cpostags[i]) ) {
							vec.push_back(inst->cpostags[i]);
						}
					}
				}
				inst->p_postags[i] = vec;
				inst->p_postags_num[i] = inst->p_postags[i].size();
			}
		}
		
		// set postags for in between features
		inst->postags_for_bet_feat.resize(length);
		inst->verb_cnt.resize(length);
		inst->conj_cnt.resize(length);
		inst->punc_cnt.resize(length);
		inst->postags_for_bet_feat[0] = inst->p_postags[0][0];
		inst->verb_cnt[0] = 0;
		inst->conj_cnt[0] = 0;
		inst->punc_cnt[0] = 0;

		for (int i = 1; i < length; ++i) {  // for joint models: use the 1-best pos candidate (always pseudo-bet-feat)
			inst->postags_for_bet_feat[i] = inst->p_postags[i][0];
			const string &tag = inst->postags_for_bet_feat[i];
			inst->verb_cnt[i] = inst->verb_cnt[i-1];
			inst->conj_cnt[i] = inst->conj_cnt[i-1];
			inst->punc_cnt[i] = inst->punc_cnt[i-1];
			if(tag[0] == 'v' || tag[0] == 'V') {
				++inst->verb_cnt[i];
			} else if(tag == "wp" || tag == "WP" || tag == "Punc" ||	tag == "PU" || tag == "," || tag == ":") {
				++inst->punc_cnt[i];
			} else if( tag == "Conj" ||	tag == "CC" || tag == "cc" || tag == "c") {
				++inst->conj_cnt[i];
			}
		}
	}

	void IOPipe::getInstancesFromInputFile( const int maxInstNum/*=-1*/, const int instMaxLen/*=-1*/, const bool add_gold_pos/*=false*/, const bool add_gold_head/*=false*/ )
	{
		cerr << "Get all instances from " << m_inf_name; print_time();
		dealloc_instance();
		int inst_thrown_ctr = 0;

		while (1) {
			Instance * const inst = m_reader->getNext(m_instances.size());
			if (!inst) break;

			if (instMaxLen > 0 && inst->size() > instMaxLen) { // to be consistent with the old version.
				cerr << " [" << inst_thrown_ctr++ << ":" << inst->size() << "] ";
				delete inst;
			} else {
				m_instances.push_back(inst);
				preprocessInstance(inst, add_gold_pos, add_gold_head);
			}

			if (maxInstNum > 0 && m_instances.size() == maxInstNum) break;
		}
		
		fillVecInstIdxToRead();

		cerr << "\ninstance num: " << getInstanceNum() << endl;
		cerr << "Done!"; print_time();
	}
}


