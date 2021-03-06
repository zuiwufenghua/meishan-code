#include "IOPipe.h"
#include <iterator>

#include "CharUtils.h"

using namespace std;
using namespace egstra;


namespace dparser {

	void IOPipe::preprocessInstance( Instance *inst)
	{
		const int length = inst->size();


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

		
		for (int i = 1; i < length; ++i) {

			// set characters
			vector<string> vec;
			if (_use_chars) {
				getCharactersFromUTF8String(inst->forms[i], inst->chars[i]);
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
    }
		
		// set postags for in between features
		inst->postags_for_bet_feat.resize(length);
		inst->verb_cnt.resize(length);
		inst->conj_cnt.resize(length);
		inst->punc_cnt.resize(length);
		inst->postags_for_bet_feat[0] = inst->cpostags[0];
		inst->verb_cnt[0] = 0;
		inst->conj_cnt[0] = 0;
		inst->punc_cnt[0] = 0;

		for (int i = 1; i < length; ++i) {  // for joint models: use the 1-best pos candidate (always pseudo-bet-feat)
			inst->postags_for_bet_feat[i] = inst->cpostags[i];
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


	void IOPipe::getInstancesFromInputFile( const int maxInstNum/*=-1*/, const int instMaxLen/*=-1*/)
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
				preprocessInstance(inst);
			}

			if (maxInstNum > 0 && m_instances.size() == maxInstNum) break;
		}
		
		fillVecInstIdxToRead();

		cerr << "\ninstance num: " << getInstanceNum() << endl;
		cerr << "Done!"; print_time();
	}
}


