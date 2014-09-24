#include "FGen.h"
#include <iterator>
using namespace std;

#include "StringMap.h"
#include "Options.h"
#include "CharUtils.h"
#include "CppAssert.h"
using namespace egstra;

#define CONF_DEFAULT "d|du|dbi|ds|dbe|s|sb|sl|g|gb|gl"

const string PRP = "PRP";
const string PRP2 = "PRP$";

namespace dparser {

	void FGen::addSiblingFeature_bohnet( const Instance *inst, const int head_id, const int child_id, const int sibling_id, list<string> &feats_str)
	{
		assert (_use_sibling);
		assert(head_id != child_id);
		assert(head_id != sibling_id || child_id != sibling_id);
		const bool first_child = (head_id == sibling_id);
		const bool last_child = (child_id == sibling_id);

		string dir, dist, feat;
		getDirection(head_id, child_id, dir);
		if (first_child) dir = "#" + dir;
		else if (last_child) dir = dir + "#";

		getDistance_1_2_36_7(head_id, child_id, dist);

		const string &form_h = inst->forms[head_id];
		const string &form_s = (first_child || last_child) ? NO_FORM: inst->forms[sibling_id];
		const string &form_c = inst->forms[child_id];

		const string &lemm_h = _use_lemma ? inst->lemmas[head_id] : NO_LEMMA;
		const string &lemm_c = _use_lemma ? inst->lemmas[child_id] : NO_LEMMA;
		const string &lemm_s = (_use_lemma && !first_child && !last_child) ? inst->lemmas[sibling_id] : NO_LEMMA;

		const string &cpos_h = inst->cpostags[head_id];
		const string &cpos_s = (first_child || last_child) ? NO_CPOSTAG : inst->cpostags[sibling_id];
		const string &cpos_c = inst->cpostags[child_id];

		string coarse_pos_h, coarse_pos_s, coarse_pos_c;
		if (_use_coarse_postag) {
			coarse_pos_h = (cpos_h != PRP && cpos_h != PRP2 && cpos_h.length() > 2) ? cpos_h.substr(0, 2) : cpos_h;
			coarse_pos_s = (cpos_s != PRP && cpos_s != PRP2 && cpos_s.length() > 2) ? cpos_s.substr(0, 2) : cpos_s;
			coarse_pos_c = (cpos_c != PRP && cpos_c != PRP2 && cpos_c.length() > 2) ? cpos_c.substr(0, 2) : cpos_c;
		}

		if (_use_sibling_basic) {
			feat = "30=" + cpos_h + FEAT_SEP + cpos_s + FEAT_SEP + cpos_c + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
			feat ="31?=" + form_h + FEAT_SEP + cpos_s + FEAT_SEP + cpos_c + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
			feat ="32?=" + cpos_h + FEAT_SEP + form_s + FEAT_SEP + cpos_c + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
			feat ="33?=" + cpos_h + FEAT_SEP + cpos_s + FEAT_SEP + form_c + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);

			feat = "31=" + cpos_h + FEAT_SEP + cpos_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
			feat = "33=" + form_h + FEAT_SEP + form_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
			feat = "34=" + cpos_h + FEAT_SEP + form_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
			feat = "35=" + form_h + FEAT_SEP + cpos_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);

			feat = "32=" + cpos_c + FEAT_SEP + cpos_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
			feat = "36=" + form_c + FEAT_SEP + form_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
			feat = "37=" + form_c + FEAT_SEP + cpos_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
			feat = "38=" + cpos_c + FEAT_SEP + form_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);

			if (_use_lemma) {
				feat = "97=" + lemm_h + FEAT_SEP + lemm_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
				feat = "99=" + cpos_h + FEAT_SEP + lemm_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
				feat ="101=" + lemm_h + FEAT_SEP + cpos_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);

				feat = "98=" + lemm_c + FEAT_SEP + lemm_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
				feat ="100=" + cpos_c + FEAT_SEP + lemm_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
				feat ="102=" + lemm_c + FEAT_SEP + cpos_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
			}

			if (_use_coarse_postag) {
				feat = "30C=" + coarse_pos_h + FEAT_SEP + coarse_pos_s + FEAT_SEP + coarse_pos_c + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
				feat ="31?C=" + form_h + FEAT_SEP + coarse_pos_s + FEAT_SEP + coarse_pos_c + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
				feat ="32?C=" + coarse_pos_h + FEAT_SEP + form_s + FEAT_SEP + coarse_pos_c + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
				feat ="33?C=" + coarse_pos_h + FEAT_SEP + coarse_pos_s + FEAT_SEP + form_c + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);

				feat = "31C=" + coarse_pos_h + FEAT_SEP + coarse_pos_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
				feat = "34C=" + coarse_pos_h + FEAT_SEP + form_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
				feat = "35C=" + form_h + FEAT_SEP + coarse_pos_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);

				feat = "32C=" + coarse_pos_c + FEAT_SEP + coarse_pos_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
				feat = "37C=" + form_c + FEAT_SEP + coarse_pos_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
				feat = "38C=" + coarse_pos_c + FEAT_SEP + form_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
			}
		}

		if (_use_sibling_linear) {
			const string &cpos_h_L1 = (head_id <= 1) ? NO_CPOSTAG : inst->cpostags[head_id-1];
			const string &cpos_h_R1 = (head_id == 0 || head_id+1 >= inst->size()) ? NO_CPOSTAG : inst->cpostags[head_id+1];
			const string &cpos_s_L1 = (first_child || last_child || sibling_id <= 1) ? NO_CPOSTAG : inst->cpostags[sibling_id-1];
			const string &cpos_s_R1 = (first_child || last_child || sibling_id+1 >= inst->size()) ? NO_CPOSTAG : inst->cpostags[sibling_id+1];
			const string &cpos_c_L1 = (child_id <= 1) ? NO_CPOSTAG : inst->cpostags[child_id-1];
			const string &cpos_c_R1 = (child_id+1 >= inst->size()) ? NO_CPOSTAG : inst->cpostags[child_id+1];

			feat = "58=" + cpos_h + FEAT_SEP + cpos_s + FEAT_SEP + cpos_s_R1 + FEAT_SEP + dir;	feats_str.push_back(feat);
			feat = "59=" + cpos_h + FEAT_SEP + cpos_s_L1 + FEAT_SEP + cpos_s + FEAT_SEP + dir;	feats_str.push_back(feat);
			feat = "60=" + cpos_h + FEAT_SEP + cpos_h_R1 + FEAT_SEP + cpos_s + FEAT_SEP + dir;	feats_str.push_back(feat);
			feat = "61=" + cpos_h_L1 + FEAT_SEP + cpos_h + FEAT_SEP + cpos_s + FEAT_SEP + dir;	feats_str.push_back(feat);

			feat = "62=" + cpos_h_L1 + FEAT_SEP + cpos_h + FEAT_SEP + cpos_s + FEAT_SEP + cpos_s_R1 + FEAT_SEP + dir;	feats_str.push_back(feat);
			feat = "63=" + cpos_h_L1 + FEAT_SEP + cpos_h + FEAT_SEP + cpos_s_L1 + FEAT_SEP + cpos_s + FEAT_SEP + dir;	feats_str.push_back(feat);
			feat = "64=" + cpos_h + FEAT_SEP + cpos_h_R1 + FEAT_SEP + cpos_s + FEAT_SEP + cpos_s_R1 + FEAT_SEP + dir;	feats_str.push_back(feat);
			feat = "65=" + cpos_h + FEAT_SEP + cpos_h_R1 + FEAT_SEP + cpos_s_L1 + FEAT_SEP + cpos_s + FEAT_SEP + dir;	feats_str.push_back(feat);

			feat = "66=" + cpos_c + FEAT_SEP + cpos_s + FEAT_SEP + cpos_s_R1 + FEAT_SEP + dir;	feats_str.push_back(feat);
			feat = "67=" + cpos_c + FEAT_SEP + cpos_s_L1 + FEAT_SEP + cpos_s + FEAT_SEP + dir;	feats_str.push_back(feat);
			feat = "68=" + cpos_c + FEAT_SEP + cpos_c_R1 + FEAT_SEP + cpos_s + FEAT_SEP + dir;	feats_str.push_back(feat);
			feat = "69=" + cpos_c_L1 + FEAT_SEP + cpos_c + FEAT_SEP + cpos_s + FEAT_SEP + dir;	feats_str.push_back(feat);

			feat = "70=" + cpos_c_L1 + FEAT_SEP + cpos_c + FEAT_SEP + cpos_s + FEAT_SEP + cpos_s_R1 + FEAT_SEP + dir;	feats_str.push_back(feat);
			feat = "71=" + cpos_c_L1 + FEAT_SEP + cpos_c + FEAT_SEP + cpos_s_L1 + FEAT_SEP + cpos_s + FEAT_SEP + dir;	feats_str.push_back(feat);
			feat = "72=" + cpos_c + FEAT_SEP + cpos_c_R1 + FEAT_SEP + cpos_s + FEAT_SEP + cpos_s_R1 + FEAT_SEP + dir;	feats_str.push_back(feat);
			feat = "73=" + cpos_c + FEAT_SEP + cpos_c_R1 + FEAT_SEP + cpos_s_L1 + FEAT_SEP + cpos_s + FEAT_SEP + dir;	feats_str.push_back(feat);
			
			if (_use_coarse_postag) {
				string coarse_pos_h_L1 = (cpos_h_L1 != PRP && cpos_h_L1 != PRP2 && cpos_h_L1.length() > 2) ? cpos_h_L1.substr(0, 2) : cpos_h_L1;
				string coarse_pos_h_R1 = (cpos_h_R1 != PRP && cpos_h_R1 != PRP2 && cpos_h_R1.length() > 2) ? cpos_h_R1.substr(0, 2) : cpos_h_R1;
				string coarse_pos_s_L1 = (cpos_s_L1 != PRP && cpos_s_L1 != PRP2 && cpos_s_L1.length() > 2) ? cpos_s_L1.substr(0, 2) : cpos_s_L1;
				string coarse_pos_s_R1 = (cpos_s_R1 != PRP && cpos_s_R1 != PRP2 && cpos_s_R1.length() > 2) ? cpos_s_R1.substr(0, 2) : cpos_s_R1;
				string coarse_pos_c_L1 = (cpos_c_L1 != PRP && cpos_c_L1 != PRP2 && cpos_c_L1.length() > 2) ? cpos_c_L1.substr(0, 2) : cpos_c_L1;
				string coarse_pos_c_R1 = (cpos_c_R1 != PRP && cpos_c_R1 != PRP2 && cpos_c_R1.length() > 2) ? cpos_c_R1.substr(0, 2) : cpos_c_R1;


				feat = "58C=" + coarse_pos_h + FEAT_SEP + coarse_pos_s + FEAT_SEP + coarse_pos_s_R1 + FEAT_SEP + dir;	feats_str.push_back(feat);
				feat = "59C=" + coarse_pos_h + FEAT_SEP + coarse_pos_s_L1 + FEAT_SEP + coarse_pos_s + FEAT_SEP + dir;	feats_str.push_back(feat);
				feat = "60C=" + coarse_pos_h + FEAT_SEP + coarse_pos_h_R1 + FEAT_SEP + coarse_pos_s + FEAT_SEP + dir;	feats_str.push_back(feat);
				feat = "61C=" + coarse_pos_h_L1 + FEAT_SEP + coarse_pos_h + FEAT_SEP + coarse_pos_s + FEAT_SEP + dir;	feats_str.push_back(feat);

				feat = "62C=" + coarse_pos_h_L1 + FEAT_SEP + coarse_pos_h + FEAT_SEP + coarse_pos_s + FEAT_SEP + coarse_pos_s_R1 + FEAT_SEP + dir;	feats_str.push_back(feat);
				feat = "63C=" + coarse_pos_h_L1 + FEAT_SEP + coarse_pos_h + FEAT_SEP + coarse_pos_s_L1 + FEAT_SEP + coarse_pos_s + FEAT_SEP + dir;	feats_str.push_back(feat);
				feat = "64C=" + coarse_pos_h + FEAT_SEP + coarse_pos_h_R1 + FEAT_SEP + coarse_pos_s + FEAT_SEP + coarse_pos_s_R1 + FEAT_SEP + dir;	feats_str.push_back(feat);
				feat = "65C=" + coarse_pos_h + FEAT_SEP + coarse_pos_h_R1 + FEAT_SEP + coarse_pos_s_L1 + FEAT_SEP + coarse_pos_s + FEAT_SEP + dir;	feats_str.push_back(feat);


				feat = "66C=" + coarse_pos_c + FEAT_SEP + coarse_pos_s + FEAT_SEP + coarse_pos_s_R1 + FEAT_SEP + dir;	feats_str.push_back(feat);
				feat = "67C=" + coarse_pos_c + FEAT_SEP + coarse_pos_s_L1 + FEAT_SEP + coarse_pos_s + FEAT_SEP + dir;	feats_str.push_back(feat);
				feat = "68C=" + coarse_pos_c + FEAT_SEP + coarse_pos_c_R1 + FEAT_SEP + coarse_pos_s + FEAT_SEP + dir;	feats_str.push_back(feat);
				feat = "69C=" + coarse_pos_c_L1 + FEAT_SEP + coarse_pos_c + FEAT_SEP + coarse_pos_s + FEAT_SEP + dir;	feats_str.push_back(feat);

				feat = "70C=" + coarse_pos_c_L1 + FEAT_SEP + coarse_pos_c + FEAT_SEP + coarse_pos_s + FEAT_SEP + coarse_pos_s_R1 + FEAT_SEP + dir;	feats_str.push_back(feat);
				feat = "71C=" + coarse_pos_c_L1 + FEAT_SEP + coarse_pos_c + FEAT_SEP + coarse_pos_s_L1 + FEAT_SEP + coarse_pos_s + FEAT_SEP + dir;	feats_str.push_back(feat);
				feat = "72C=" + coarse_pos_c + FEAT_SEP + coarse_pos_c_R1 + FEAT_SEP + coarse_pos_s + FEAT_SEP + coarse_pos_s_R1 + FEAT_SEP + dir;	feats_str.push_back(feat);
				feat = "73C=" + coarse_pos_c + FEAT_SEP + coarse_pos_c_R1 + FEAT_SEP + coarse_pos_s_L1 + FEAT_SEP + coarse_pos_s + FEAT_SEP + dir;	feats_str.push_back(feat);
			}
		}
	}

	// head_id == gchild_id: no left-side grandchild; child_id == gchild_id: no right-side grandchild
	void FGen::addGrandFeature_bohnet( const Instance *inst, const int head_id, const int child_id, const int gchild_id, list<string> &feats_str)
	{
		assert (_use_grand);
		assert(head_id != child_id);
		assert(head_id != gchild_id || child_id != gchild_id);
		const bool no_gchild = (head_id == gchild_id || child_id == gchild_id);
		const bool no_gchild_r = (child_id == gchild_id);
		
		string dir, gdir, feat;
		getDirection(head_id, child_id, dir);
		if (no_gchild)
			gdir = no_gchild_r ? "?R" : "?L";
		else
			getDirection(child_id, gchild_id, gdir);
		
		const string &form_h = inst->forms[head_id];
		const string &form_c = inst->forms[child_id];
		const string &form_g = no_gchild ? NO_FORM: inst->forms[gchild_id];

		const string &lemm_h = _use_lemma ? inst->lemmas[head_id] : NO_LEMMA;
		const string &lemm_c = _use_lemma ? inst->lemmas[child_id] : NO_LEMMA;
		const string &lemm_g = (_use_lemma && !no_gchild) ? inst->lemmas[gchild_id] : NO_LEMMA;

		const string &cpos_h = inst->cpostags[head_id];
		const string &cpos_c = inst->cpostags[child_id];
		const string &cpos_g = no_gchild ? NO_CPOSTAG :inst->cpostags[gchild_id];

		string coarse_pos_h, coarse_pos_g, coarse_pos_c;
		if (_use_coarse_postag) {
			coarse_pos_h = (cpos_h != PRP && cpos_h != PRP2 && cpos_h.length() > 2) ? cpos_h.substr(0, 2) : cpos_h;
			coarse_pos_c = (cpos_c != PRP && cpos_c != PRP2 && cpos_c.length() > 2) ? cpos_c.substr(0, 2) : cpos_c;
			coarse_pos_g = (cpos_g != PRP && cpos_g != PRP2 && cpos_g.length() > 2) ? cpos_g.substr(0, 2) : cpos_g;
		}

		if (_use_grand_basic) {
			feat = "21=" + cpos_h + FEAT_SEP + cpos_c + FEAT_SEP + cpos_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);
			feat ="21?=" + form_h + FEAT_SEP + cpos_c + FEAT_SEP + cpos_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);
			feat ="22?=" + cpos_h + FEAT_SEP + form_c + FEAT_SEP + cpos_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);
			feat ="23?=" + cpos_h + FEAT_SEP + cpos_c + FEAT_SEP + form_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);

			feat = "22=" + cpos_h + FEAT_SEP + cpos_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);
			feat = "23=" + cpos_c + FEAT_SEP + cpos_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);
			feat = "24=" + form_h + FEAT_SEP + form_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);
			feat = "25=" + form_c + FEAT_SEP + form_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);
			feat = "26=" + cpos_h + FEAT_SEP + form_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);
			feat = "27=" + cpos_c + FEAT_SEP + form_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);
			feat = "28=" + form_h + FEAT_SEP + cpos_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);
			feat = "29=" + form_c + FEAT_SEP + cpos_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);

			if (_use_lemma) {
				feat = "91=" + lemm_h + FEAT_SEP + lemm_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);
				feat = "93=" + cpos_h + FEAT_SEP + lemm_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);
				feat = "94=" + cpos_c + FEAT_SEP + lemm_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);
				feat = "95=" + lemm_h + FEAT_SEP + cpos_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);
				feat = "96=" + lemm_c + FEAT_SEP + cpos_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);
			}

			if (_use_coarse_postag) {
				feat = "21C=" + coarse_pos_h + FEAT_SEP + coarse_pos_c + FEAT_SEP + coarse_pos_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);
				feat ="21?C=" + form_h + FEAT_SEP + coarse_pos_c + FEAT_SEP + coarse_pos_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);
				feat ="22?C=" + coarse_pos_h + FEAT_SEP + form_c + FEAT_SEP + coarse_pos_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);
				feat ="23?C=" + coarse_pos_h + FEAT_SEP + coarse_pos_c + FEAT_SEP + form_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);

				feat = "22C=" + coarse_pos_h + FEAT_SEP + coarse_pos_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);
				feat = "23C=" + coarse_pos_c + FEAT_SEP + coarse_pos_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);
				feat = "26C=" + coarse_pos_h + FEAT_SEP + form_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);
				feat = "27C=" + coarse_pos_c + FEAT_SEP + form_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);
				feat = "28C=" + form_h + FEAT_SEP + coarse_pos_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);
				feat = "29C=" + form_c + FEAT_SEP + coarse_pos_g + FEAT_SEP + dir + gdir;	feats_str.push_back(feat);
			}
		}
		
		if (_use_grand_linear) {
			const string &cpos_h_L1 = (head_id <= 1) ? NO_CPOSTAG : inst->cpostags[head_id-1];
			const string &cpos_h_R1 = (head_id == 0 || head_id+1 >= inst->size()) ? NO_CPOSTAG : inst->cpostags[head_id+1];
			const string &cpos_c_L1 = (child_id <= 1) ? NO_CPOSTAG : inst->cpostags[child_id-1];
			const string &cpos_c_R1 = (child_id+1 >= inst->size()) ? NO_CPOSTAG : inst->cpostags[child_id+1];	
			const string &cpos_g_L1 = (no_gchild || gchild_id <= 1) ? NO_CPOSTAG : inst->cpostags[gchild_id-1];
			const string &cpos_g_R1 = (no_gchild || gchild_id+1 >= inst->size()) ? NO_CPOSTAG : inst->cpostags[gchild_id+1];
			
			feat = "42=" + cpos_g + FEAT_SEP + cpos_g_R1 + FEAT_SEP + cpos_c + dir + gdir;	feats_str.push_back(feat);
			feat = "43=" + cpos_g_L1 + FEAT_SEP + cpos_g + FEAT_SEP + cpos_c + dir + gdir;	feats_str.push_back(feat);
			feat = "44=" + cpos_g + FEAT_SEP + cpos_c + FEAT_SEP + cpos_c_R1 + dir + gdir;	feats_str.push_back(feat);
			feat = "45=" + cpos_g + FEAT_SEP + cpos_c_L1 + FEAT_SEP + cpos_c + dir + gdir;	feats_str.push_back(feat);
			feat = "46=" + cpos_g + FEAT_SEP + cpos_g_R1 + FEAT_SEP + cpos_c_L1 + FEAT_SEP + cpos_c + dir + gdir;	feats_str.push_back(feat);
			feat = "47=" + cpos_g_L1 + FEAT_SEP + cpos_g + FEAT_SEP + cpos_c_L1 + FEAT_SEP + cpos_c + dir + gdir;	feats_str.push_back(feat);
			feat = "48=" + cpos_g + FEAT_SEP + cpos_g_R1 + FEAT_SEP + cpos_c + FEAT_SEP + cpos_c_R1 + dir + gdir;	feats_str.push_back(feat);
			feat = "49=" + cpos_g_L1 + FEAT_SEP + cpos_g + FEAT_SEP + cpos_c + FEAT_SEP + cpos_c_R1 + dir + gdir;	feats_str.push_back(feat);

			feat = "50=" + cpos_g + FEAT_SEP + cpos_g_R1 + FEAT_SEP + cpos_h + dir + gdir;	feats_str.push_back(feat);
			feat = "51=" + cpos_g_L1 + FEAT_SEP + cpos_g + FEAT_SEP + cpos_h + dir + gdir;	feats_str.push_back(feat);
			feat = "52=" + cpos_g + FEAT_SEP + cpos_h + FEAT_SEP + cpos_h_R1 + dir + gdir;	feats_str.push_back(feat);
			feat = "53=" + cpos_g + FEAT_SEP + cpos_h_L1 + FEAT_SEP + cpos_h + dir + gdir;	feats_str.push_back(feat);
			feat = "54=" + cpos_g + FEAT_SEP + cpos_g_R1 + FEAT_SEP + cpos_h_L1 + FEAT_SEP + cpos_h + dir + gdir;	feats_str.push_back(feat);
			feat = "55=" + cpos_g_L1 + FEAT_SEP + cpos_g + FEAT_SEP + cpos_h_L1 + FEAT_SEP + cpos_h + dir + gdir;	feats_str.push_back(feat);
			feat = "56=" + cpos_g + FEAT_SEP + cpos_g_R1 + FEAT_SEP + cpos_h + FEAT_SEP + cpos_h_R1 + dir + gdir;	feats_str.push_back(feat);
			feat = "57=" + cpos_g_L1 + FEAT_SEP + cpos_g + FEAT_SEP + cpos_h + FEAT_SEP + cpos_h_R1 + dir + gdir;	feats_str.push_back(feat);

			if (_use_coarse_postag) {
				string coarse_pos_h_L1 = (cpos_h_L1 != PRP && cpos_h_L1 != PRP2 && cpos_h_L1.length() > 2) ? cpos_h_L1.substr(0, 2) : cpos_h_L1;
				string coarse_pos_h_R1 = (cpos_h_R1 != PRP && cpos_h_R1 != PRP2 && cpos_h_R1.length() > 2) ? cpos_h_R1.substr(0, 2) : cpos_h_R1;
				string coarse_pos_c_L1 = (cpos_c_L1 != PRP && cpos_c_L1 != PRP2 && cpos_c_L1.length() > 2) ? cpos_c_L1.substr(0, 2) : cpos_c_L1;
				string coarse_pos_c_R1 = (cpos_c_R1 != PRP && cpos_c_R1 != PRP2 && cpos_c_R1.length() > 2) ? cpos_c_R1.substr(0, 2) : cpos_c_R1;
				string coarse_pos_g_L1 = (cpos_g_L1 != PRP && cpos_g_L1 != PRP2 && cpos_g_L1.length() > 2) ? cpos_g_L1.substr(0, 2) : cpos_g_L1;
				string coarse_pos_g_R1 = (cpos_g_R1 != PRP && cpos_g_R1 != PRP2 && cpos_g_R1.length() > 2) ? cpos_g_R1.substr(0, 2) : cpos_g_R1;

				feat = "42C=" + coarse_pos_g + FEAT_SEP + coarse_pos_g_R1 + FEAT_SEP + coarse_pos_c + dir + gdir;	feats_str.push_back(feat);
				feat = "43C=" + coarse_pos_g_L1 + FEAT_SEP + coarse_pos_g + FEAT_SEP + coarse_pos_c + dir + gdir;	feats_str.push_back(feat);
				feat = "44C=" + coarse_pos_g + FEAT_SEP + coarse_pos_c + FEAT_SEP + coarse_pos_c_R1 + dir + gdir;	feats_str.push_back(feat);
				feat = "45C=" + coarse_pos_g + FEAT_SEP + coarse_pos_c_L1 + FEAT_SEP + coarse_pos_c + dir + gdir;	feats_str.push_back(feat);
				feat = "46C=" + coarse_pos_g + FEAT_SEP + coarse_pos_g_R1 + FEAT_SEP + coarse_pos_c_L1 + FEAT_SEP + coarse_pos_c + dir + gdir;	feats_str.push_back(feat);
				feat = "47C=" + coarse_pos_g_L1 + FEAT_SEP + coarse_pos_g + FEAT_SEP + coarse_pos_c_L1 + FEAT_SEP + coarse_pos_c + dir + gdir;	feats_str.push_back(feat);
				feat = "48C=" + coarse_pos_g + FEAT_SEP + coarse_pos_g_R1 + FEAT_SEP + coarse_pos_c + FEAT_SEP + coarse_pos_c_R1 + dir + gdir;	feats_str.push_back(feat);
				feat = "49C=" + coarse_pos_g_L1 + FEAT_SEP + coarse_pos_g + FEAT_SEP + coarse_pos_c + FEAT_SEP + coarse_pos_c_R1 + dir + gdir;	feats_str.push_back(feat);

				feat = "50C=" + coarse_pos_g + FEAT_SEP + coarse_pos_g_R1 + FEAT_SEP + coarse_pos_h + dir + gdir;	feats_str.push_back(feat);
				feat = "51C=" + coarse_pos_g_L1 + FEAT_SEP + coarse_pos_g + FEAT_SEP + coarse_pos_h + dir + gdir;	feats_str.push_back(feat);
				feat = "52C=" + coarse_pos_g + FEAT_SEP + coarse_pos_h + FEAT_SEP + coarse_pos_h_R1 + dir + gdir;	feats_str.push_back(feat);
				feat = "53C=" + coarse_pos_g + FEAT_SEP + coarse_pos_h_L1 + FEAT_SEP + coarse_pos_h + dir + gdir;	feats_str.push_back(feat);
				feat = "54C=" + coarse_pos_g + FEAT_SEP + coarse_pos_g_R1 + FEAT_SEP + coarse_pos_h_L1 + FEAT_SEP + coarse_pos_h + dir + gdir;	feats_str.push_back(feat);
				feat = "55C=" + coarse_pos_g_L1 + FEAT_SEP + coarse_pos_g + FEAT_SEP + coarse_pos_h_L1 + FEAT_SEP + coarse_pos_h + dir + gdir;	feats_str.push_back(feat);
				feat = "56C=" + coarse_pos_g + FEAT_SEP + coarse_pos_g_R1 + FEAT_SEP + coarse_pos_h + FEAT_SEP + coarse_pos_h_R1 + dir + gdir;	feats_str.push_back(feat);
				feat = "57C=" + coarse_pos_g_L1 + FEAT_SEP + coarse_pos_g + FEAT_SEP + coarse_pos_h + FEAT_SEP + coarse_pos_h_R1 + dir + gdir;	feats_str.push_back(feat);
			}
		}
	}

	void FGen::addDependencyFeature_bohnet( const Instance *inst, const int head_id, const int child_id, list<string> &feats_str) const
	{
		assert(_use_dependency);
		assert(head_id != child_id && child_id != 0);

		string dir, dist, feat;
		getDirection(head_id, child_id, dir);
		getDistance_1_2_36_7(head_id, child_id, dist);
		const bool is_root = (head_id == 0);

		const string &form_h = inst->forms[head_id];
		const string &form_c = inst->forms[child_id];
		const string &lemm_h = _use_lemma ? inst->lemmas[head_id] : NO_LEMMA;
		const string &lemm_c = _use_lemma ? inst->lemmas[child_id] : NO_LEMMA;
		const string &cpos_h = inst->cpostags[head_id];
		const string &cpos_c = inst->cpostags[child_id];

		string coarse_pos_h, coarse_pos_c;
		if (_use_coarse_postag) {
			coarse_pos_h = (cpos_h != PRP && cpos_h != PRP2 && cpos_h.length() > 2) ? cpos_h.substr(0, 2) : cpos_h;
			coarse_pos_c = (cpos_c != PRP && cpos_c != PRP2 && cpos_c.length() > 2) ? cpos_c.substr(0, 2) : cpos_c;
		}

		if (_use_dependency_unigram) {
			if (!is_root) { // head
				feat = "1=" + form_h + FEAT_SEP + cpos_h + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
				feat = "2=" + form_h + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
				feat = "3=" + cpos_h + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist); 
				if (_use_lemma) {
					feat = "77=" + lemm_h + FEAT_SEP + cpos_h + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
					feat = "78=" + lemm_h + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
				}
				if (_use_coarse_postag) {
					feat = "1C=" + form_h + FEAT_SEP + coarse_pos_h + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
					feat = "3C=" + coarse_pos_h + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist); 
				}
			}
			{ // child
				feat = "4=" + form_c + FEAT_SEP + cpos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist); 
				feat = "5=" + form_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist); 
				feat = "6=" + cpos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist); 
				if (_use_lemma) {
					feat = "80=" + lemm_c + FEAT_SEP + cpos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
					feat = "81=" + lemm_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
				}
				if (_use_coarse_postag) {
					feat = "4C=" + form_c + FEAT_SEP + coarse_pos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist); 
					feat = "6C=" + coarse_pos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist); 
				}
			}
		}

		if (_use_dependency_bigram) {
			feat = "7=" + form_h + FEAT_SEP + cpos_h + FEAT_SEP + form_c + FEAT_SEP + cpos_c + FEAT_SEP + dir;	feats_str.push_back(feat);

			feat = "8=" + cpos_h + FEAT_SEP + form_c + FEAT_SEP + cpos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
			feat = "9=" + form_h + FEAT_SEP + form_c + FEAT_SEP + cpos_c + FEAT_SEP + dir;	feats_str.push_back(feat);
			feat= "10=" + form_h + FEAT_SEP + cpos_h + FEAT_SEP + cpos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
			feat= "11=" + form_h + FEAT_SEP + cpos_h + FEAT_SEP + form_c + FEAT_SEP + dir;	feats_str.push_back(feat);

			feat= "12=" + form_h + FEAT_SEP + form_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
			feat= "13=" + cpos_h + FEAT_SEP + cpos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
			feat="12?=" + form_h + FEAT_SEP + cpos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
			feat="13?=" + cpos_h + FEAT_SEP + form_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
			if (_use_lemma) {
				feat = "83=" + lemm_h + FEAT_SEP + cpos_h + FEAT_SEP + lemm_c + FEAT_SEP + cpos_c + FEAT_SEP + dir;	feats_str.push_back(feat);

				feat = "84=" + cpos_h + FEAT_SEP + lemm_c + FEAT_SEP + cpos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
				feat = "85=" + lemm_h + FEAT_SEP + lemm_c + FEAT_SEP + cpos_c + FEAT_SEP + dir;	feats_str.push_back(feat);
				feat = "86=" + lemm_h + FEAT_SEP + cpos_h + FEAT_SEP + cpos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
				feat = "87=" + lemm_h + FEAT_SEP + cpos_h + FEAT_SEP + lemm_c + FEAT_SEP + dir;	feats_str.push_back(feat);
			}
			if (_use_coarse_postag) {
				feat = "7C=" + form_h + FEAT_SEP + coarse_pos_h + FEAT_SEP + form_c + FEAT_SEP + coarse_pos_c + FEAT_SEP + dir;	feats_str.push_back(feat);

				feat = "8C=" + coarse_pos_h + FEAT_SEP + form_c + FEAT_SEP + coarse_pos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
				feat = "9C=" + form_h + FEAT_SEP + form_c + FEAT_SEP + coarse_pos_c + FEAT_SEP + dir;	feats_str.push_back(feat);
				feat= "10C=" + form_h + FEAT_SEP + coarse_pos_h + FEAT_SEP + coarse_pos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
				feat= "11C=" + form_h + FEAT_SEP + coarse_pos_h + FEAT_SEP + form_c + FEAT_SEP + dir;	feats_str.push_back(feat);

				feat= "13C=" + coarse_pos_h + FEAT_SEP + coarse_pos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
				feat="12?C=" + form_h + FEAT_SEP + coarse_pos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
				feat="13?C=" + coarse_pos_h + FEAT_SEP + form_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
			}
		}

		if (_use_dependency_surrounding) {

			const string &cpos_h_L1 = (head_id <= 1) ? NO_CPOSTAG : inst->cpostags[head_id-1];
			const string &cpos_h_R1 = (is_root || head_id+1 >= inst->size()) ? NO_CPOSTAG : inst->cpostags[head_id+1];
			const string &cpos_c_L1 = (child_id <= 1) ? NO_CPOSTAG : inst->cpostags[child_id-1];
			const string &cpos_c_R1 = (child_id+1 >= inst->size()) ? NO_CPOSTAG : inst->cpostags[child_id+1];

			feat = "14=" + cpos_h + FEAT_SEP + cpos_h_R1 + FEAT_SEP + cpos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
			feat = "15=" + cpos_h + FEAT_SEP + cpos_c_L1 + FEAT_SEP + cpos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
			feat = "16=" + cpos_h + FEAT_SEP + cpos_c + FEAT_SEP + cpos_c_R1 + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
			feat = "17=" + cpos_h + FEAT_SEP + cpos_h_R1 + FEAT_SEP + cpos_c_L1 + FEAT_SEP + cpos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
			feat = "18=" + cpos_h_L1 + FEAT_SEP + cpos_h_R1 + FEAT_SEP + cpos_c_L1 + FEAT_SEP + cpos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
			feat = "19=" + cpos_h + FEAT_SEP + cpos_h_R1 + FEAT_SEP + cpos_c + FEAT_SEP + cpos_c_R1 + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
			feat = "20=" + cpos_h_L1 + FEAT_SEP + cpos_h + FEAT_SEP + cpos_c_L1 + FEAT_SEP + cpos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
			feat= "?20=" + cpos_h_L1 + FEAT_SEP + cpos_h + FEAT_SEP + cpos_h_R1 + FEAT_SEP + cpos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
			feat= "?19=" + cpos_h + FEAT_SEP + cpos_c_L1 + FEAT_SEP + cpos_c + FEAT_SEP + cpos_c_R1 + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);

			if (_use_coarse_postag) {
				string coarse_pos_h_L1 = (cpos_h_L1 != PRP && cpos_h_L1 != PRP2 && cpos_h_L1.length() > 2) ? cpos_h_L1.substr(0, 2) : cpos_h_L1;
				string coarse_pos_h_R1 = (cpos_h_R1 != PRP && cpos_h_R1 != PRP2 && cpos_h_R1.length() > 2) ? cpos_h_R1.substr(0, 2) : cpos_h_R1;
				string coarse_pos_c_L1 = (cpos_c_L1 != PRP && cpos_c_L1 != PRP2 && cpos_c_L1.length() > 2) ? cpos_c_L1.substr(0, 2) : cpos_c_L1;
				string coarse_pos_c_R1 = (cpos_c_R1 != PRP && cpos_c_R1 != PRP2 && cpos_c_R1.length() > 2) ? cpos_c_R1.substr(0, 2) : cpos_c_R1;

				feat = "14=C" + coarse_pos_h + FEAT_SEP + coarse_pos_h_R1 + FEAT_SEP + coarse_pos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
				feat = "15=C" + coarse_pos_h + FEAT_SEP + coarse_pos_c_L1 + FEAT_SEP + coarse_pos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
				feat = "16=C" + coarse_pos_h + FEAT_SEP + coarse_pos_c + FEAT_SEP + coarse_pos_c_R1 + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
				feat = "17=C" + coarse_pos_h + FEAT_SEP + coarse_pos_h_R1 + FEAT_SEP + coarse_pos_c_L1 + FEAT_SEP + coarse_pos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
				feat = "18=C" + coarse_pos_h_L1 + FEAT_SEP + coarse_pos_h_R1 + FEAT_SEP + coarse_pos_c_L1 + FEAT_SEP + coarse_pos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
				feat = "19=C" + coarse_pos_h + FEAT_SEP + coarse_pos_h_R1 + FEAT_SEP + coarse_pos_c + FEAT_SEP + coarse_pos_c_R1 + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
				feat = "20=C" + coarse_pos_h_L1 + FEAT_SEP + coarse_pos_h + FEAT_SEP + coarse_pos_c_L1 + FEAT_SEP + coarse_pos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
				feat= "?20=C" + coarse_pos_h_L1 + FEAT_SEP + coarse_pos_h + FEAT_SEP + coarse_pos_h_R1 + FEAT_SEP + coarse_pos_c + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
				feat= "?19=C" + coarse_pos_h + FEAT_SEP + coarse_pos_c_L1 + FEAT_SEP + coarse_pos_c + FEAT_SEP + coarse_pos_c_R1 + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
			}
		}

		if (_use_dependency_between) {
			const vector<string> &pos_bet = inst->postags_for_bet_feat;
			const vector<int> &verb_cnt = inst->verb_cnt;
			const vector<int> &conj_cnt = inst->conj_cnt;
			const vector<int> &punc_cnt = inst->punc_cnt;


			const int small = (head_id < child_id ? head_id : child_id);
			const int large = (head_id > child_id ? head_id : child_id);
			const string &p_small = pos_bet[small];
			const string &p_large = pos_bet[large];

			StringMap<int> pos_seen;
			for (int i = small+1; i < large; ++i) {
				// pos-i pos-b pos-j
				if ( pos_seen.get(pos_bet[i].c_str()) == NULL ) {
					feat = "39=" + p_small + FEAT_SEP + pos_bet[i] + FEAT_SEP + p_large + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
					pos_seen.set(pos_bet[i].c_str(), 1);
				}
			}

			// verb, punc, conj +cnt
			ostringstream out;
			int cnt = verb_cnt[large-1] - verb_cnt[small];
			if (cnt > 2) cnt = 3;
			out.str(""); out << cnt;
			feat = "39=" + p_small + FEAT_SEP + "verb=" + out.str() + FEAT_SEP + p_large + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);

			cnt = conj_cnt[large-1] - conj_cnt[small];
			if (cnt > 2) cnt = 3;
			out.str(""); out << cnt;
			feat = "39=" + p_small + FEAT_SEP + "conj=" + out.str() + FEAT_SEP + p_large + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);

			cnt = punc_cnt[large-1] - punc_cnt[small];
			if (cnt > 2) cnt = 3;
			out.str(""); out << cnt;
			feat = "39=" + p_small + FEAT_SEP + "punc=" + out.str() + FEAT_SEP + p_large + FEAT_SEP + dir;	feats_str.push_back(feat);	if (_use_distance_in_dependency_features) feats_str.push_back(feat+dist);
		}
	}


	void FGen::process_options()
	{
		int tmp;


		_english = false;
		_use_coarse_postag = false;
		if(options::get("english", tmp)) {
			_english = tmp;
			if (_english) {
				_use_coarse_postag = true;
				if(options::get("use-coarse-postag", tmp)) {
					_use_coarse_postag = tmp;
				}
			}
		}

		_use_lemma = false;
		if(options::get("use-lemma", tmp)) {
			_use_lemma = tmp;
		}


		_use_bohnet_syn_features = true;
		if(options::get("use-bohnet-syn-features", tmp)) {
			if (!tmp) {
				cerr << "Note: must use bohnet-syn-features, do not support other options" << endl;
			}
		}

		_use_distance_in_dependency_features = true;
		if (_use_bohnet_syn_features) {
			if(options::get("use-distance-in-dependency-features", tmp)) {
				_use_distance_in_dependency_features = tmp;
			}
		}


		_fcutoff = 1;
		if(options::get("fcutoff", tmp)) {
			_fcutoff = tmp;
		}

		/************* feature configuration *************/
		_use_dependency = false;
		_use_dependency_unigram = false;
		_use_dependency_bigram = false;
		_use_dependency_surrounding = false;
		_use_dependency_between = false;

		_use_sibling = false;
		_use_sibling_basic = false;
		_use_sibling_linear = false;

		_use_grand = false;
		_use_grand_basic = false;
		_use_grand_linear = false;



		string decoder_name;
		assert( options::get("decoder", decoder_name) );
		if (decoder_name == "1o") {
		  _use_dependency = true;
		  _use_dependency_unigram = true;
		  _use_dependency_bigram = true;
		  _use_dependency_surrounding = true;
		  _use_dependency_between = true;
    } else if (decoder_name == "1okbest") {
		  _use_dependency = true;
		  _use_dependency_unigram = true;
		  _use_dependency_bigram = true;
		  _use_dependency_surrounding = true;
		  _use_dependency_between = true;
		} else if (decoder_name == "2o-carreras") {
      _use_dependency = true;
		  _use_dependency_unigram = true;
		  _use_dependency_bigram = true;
		  _use_dependency_surrounding = true;
		  _use_dependency_between = true;

      _use_sibling = true;
		  _use_sibling_basic = true;
		  _use_sibling_linear = true;

		  _use_grand = true;
		  _use_grand_basic = true;
		  _use_grand_linear = true;

		}



		//if(_word_limit == -1) { _word_limit = INT_MAX; }
	}

	void FGen::usage(const char* const mesg) const {
		cerr << _name << " options:" << endl;
		cerr << " --fdictdir=<str> : pathname to feature-dictionary directory" << endl;
		cerr << " --fcutoff=<int>  : minimum feature count (default 1)" << endl;
		cerr << " --fconf=\"<flag>|...\" : feature-configuration flags" << endl;
		cerr << "     d    : dependency features" << endl;
		cerr << "     du   : dependency unigram features" << endl;
		cerr << "     dbi  : dependency bigram features" << endl;		
		cerr << "     ds   : dependency surrounding features" << endl;
		cerr << "     dbe  : depenedency between features" << endl;
		cerr << "     s    : sibling features" << endl;
		cerr << "     sb   : sibling basic features" << endl;
		cerr << "     sl   : sibling linear features" << endl;
		cerr << "     g    : grand features" << endl;
		cerr << "     gb   : grand basic features" << endl;
		cerr << "     gl   : grand linear features" << endl;

		cerr << "   (default \"" << CONF_DEFAULT << "\")" << endl;
		cerr << endl;
		cerr << mesg << endl;
	}

	void FGen::alloc_fvec_prob( Instance * const inst ) const
	{
	}

	void FGen::dealloc_fvec_prob( Instance * const inst ) const
	{
		const int len = inst->size();

		if (_use_sibling) {
      {
				fvec * pfv = inst->fvec_sib.c_buf();
				for (int j = 0; j < inst->fvec_sib.total_size(); ++j, ++pfv) {
					pfv->dealloc();
				}
			}
			inst->fvec_sib.dealloc();
			inst->prob_sib.dealloc();

			inst->fvec_sibl.dealloc();
			inst->prob_sibl.dealloc();
		}

		if (_use_grand) {
      {
				fvec * pfv = inst->fvec_grd.c_buf();
				for (int j = 0; j < inst->fvec_grd.total_size(); ++j, ++pfv) {
					pfv->dealloc();
				}
			}
			inst->fvec_grd.dealloc();
			inst->prob_grd.dealloc();

			inst->fvec_grdl.dealloc();
			inst->prob_grdl.dealloc();

		}


		if (_use_dependency) {
			{
				fvec * pfv= inst->fvec_dep.c_buf();
				for (int j = 0; j < inst->fvec_dep.total_size(); ++j, ++pfv) {
					pfv->dealloc();
				}
			}
			inst->fvec_dep.dealloc();
			inst->prob_dep.dealloc();
 
			inst->fvec_depl.dealloc();
			inst->prob_depl.dealloc();
		}
	}

	void FGen::create_all_feature_vectors( Instance * const inst )
	{
		const int len = inst->size();
		list<string> feats_str;

		if (_use_sibling) {
			inst->fvec_sib.resize(len, len, len);
			inst->prob_sib.resize(len, len, len);
			if (!_generation_mode) {
				inst->fvec_sibl.resize(len, len, len);
				inst->prob_sibl.resize(len, len, len);
			}
			for (int h = 0; h < len; ++h) {
				for (int m = 1; m < len; ++m) {
					if (h == m) continue;
					const int step = h < m ? 1 : -1;
					const int end = h < m ? m+1 : m-1;
					for (int s = h; s != end; s += step) { // when s == h, first child; when s == m, last child						
						inst->prob_sib[h][m][s] = DOUBLE_NEGATIVE_INFINITY;

						if (!_generation_mode) {
							const int label_num = _L;
							assert(label_num > 0);
							(inst->fvec_sibl[h][m][s]).resize(label_num);
							(inst->prob_sibl[h][m][s]).resize(label_num);
							inst->fvec_sibl[h][m][s] = fvec();
							inst->prob_sibl[h][m][s] = DOUBLE_NEGATIVE_INFINITY;	
						}

						fvec * const fv = &inst->fvec_sib[h][m][s];
						feats_str.clear();
						addSiblingFeature_bohnet(inst, h, m, s, feats_str);
						_sibling_feat_dict.map_all(fv, _sibling_feat_offset, feats_str, _generation_mode);
						if (!_generation_mode) {
							for (int il = 0; il < inst->fvec_sibl[h][m][s].size(); ++il) {
								const int label_id = il;
								fvec *fv_l = &inst->fvec_sibl[h][m][s][il];
								fv_l->idx = fv->idx;
								fv_l->n = fv->n;
								fv_l->val = fv->val;
								fv_l->offset = fv->offset + _sibling_feat_dim * (label_id + 1);
              }
            }
          }
        }
      }
		}


		if (_use_grand) {
			inst->fvec_grd.resize(len, len, len);
			inst->prob_grd.resize(len, len, len);
			if (!_generation_mode) {
				inst->fvec_grdl.resize(len, len, len);
				inst->prob_grdl.resize(len, len, len);
			}
			for (int h = 0; h < len; ++h) {
				for (int m = 1; m < len; ++m) {
					if (h == m) continue;
					const int step = h < m ? 1 : -1;
					const int end = h < m ? len : 0;
					// when gch == h, [h->m] has no left-side grandchild; 
					// when gch == m, [h->m] has no right-side grandchild.
					for (int gch = h; gch != end; gch += step) {
						inst->fvec_grd[h][m][gch] = fvec();
						inst->prob_grd[h][m][gch] = DOUBLE_NEGATIVE_INFINITY;
						if (!_generation_mode) {
							const int label_num =_L;
							assert(label_num > 0);
							(inst->fvec_grdl[h][m][gch]).resize(label_num);
							(inst->prob_grdl[h][m][gch]).resize(label_num);
							inst->fvec_grdl[h][m][gch] = fvec();
							inst->prob_grdl[h][m][gch] = DOUBLE_NEGATIVE_INFINITY;
						}

						fvec * const fv = &inst->fvec_grd[h][m][gch];
						feats_str.clear();
						addGrandFeature_bohnet(inst, h, m, gch, feats_str);
						_grand_feat_dict.map_all(fv, _grand_feat_offset, feats_str, _generation_mode);
						if (!_generation_mode) {
							for (int il = 0; il < inst->fvec_grdl[h][m][gch].size(); ++il) {
								const int label_id = il;
								fvec *fv_l = &inst->fvec_grdl[h][m][gch][il];
								fv_l->idx = fv->idx;
								fv_l->n = fv->n;
								fv_l->val = fv->val;
								fv_l->offset = fv->offset + _grand_feat_dim * (label_id + 1);
              }
						}
					}
				}
			}
		}
		
		if (_use_dependency) {
			inst->fvec_dep.resize(len, len);
			inst->prob_dep.resize(len, len);
			if (!_generation_mode) {
				inst->fvec_depl.resize(len, len);
				inst->prob_depl.resize(len, len);
			}

			for (int h = 0; h < len; ++h) {
				for (int m = 1; m < len; ++m) {
					if (h == m) continue;
					inst->fvec_dep[h][m] = fvec();
					inst->prob_dep[h][m] = DOUBLE_NEGATIVE_INFINITY;
					if (!_generation_mode) {
						const int label_num =  _L;
						assert(label_num > 0);
						(inst->fvec_depl[h][m]).resize(label_num);
						(inst->prob_depl[h][m]).resize(label_num);
						inst->fvec_depl[h][m] = fvec();
						inst->prob_depl[h][m] = DOUBLE_NEGATIVE_INFINITY;
					}

          fvec * const fv = &inst->fvec_dep[h][m];
					feats_str.clear();
					addDependencyFeature(inst, h, m, feats_str);
					_dependency_feat_dict.map_all(fv, _dependency_feat_offset, feats_str, _generation_mode);
					if (!_generation_mode) {
						for (int il = 0; il < inst->fvec_depl[h][m].size(); ++il) {
							const int label_id = il;
							fvec *fv_l = &inst->fvec_depl[h][m][il];
							fv_l->idx = fv->idx;
							fv_l->n = fv->n;
							fv_l->val = fv->val;
							fv_l->offset = fv->offset + _dependency_feat_dim * (label_id + 1);
						}
					}
				}
			}
		}


	}

	void FGen::save_dictionaries( const string &dictdir ) /*const*/
	{
		assert(!_generation_mode);
		cerr << _name << " : saving feature dictionaries to \""
			<< dictdir << "\"" << endl;

		_label_dict.save(dictdir + "/label.dict.gz");



		if (_use_dependency) _dependency_feat_dict.save(dictdir + "/dependency.features.gz");
		if (_use_sibling) _sibling_feat_dict.save(dictdir + "/sibling.features.gz");
		if (_use_grand) _grand_feat_dict.save(dictdir + "/grand.features.gz");
	}

	void FGen::load_dictionaries( const string &dictdir )
	{
		assert(!_generation_mode);
		cerr << _name << " : loading feature dictionaries from \""
			<< dictdir << "\""; print_time();

		_label_dict.load(dictdir + "/label.dict.gz", 0);
		_L = _label_dict.dimensionality();



		if (_use_dependency) _dependency_feat_dict.load(dictdir + "/dependency.features.gz", _fcutoff);
		if (_use_sibling) _sibling_feat_dict.load(dictdir + "/sibling.features.gz", _fcutoff);
		if (_use_grand) _grand_feat_dict.load(dictdir + "/grand.features.gz", _fcutoff);

		_dependency_feat_offset = 0;
		_dependency_feat_dim = _dependency_feat_dict.dimensionality();

		_sibling_feat_offset = _dependency_feat_offset + _dependency_feat_dim * (_L+1);
		_sibling_feat_dim = _sibling_feat_dict.dimensionality();

		_grand_feat_offset = _sibling_feat_offset + _sibling_feat_dim * (_L+1);
		_grand_feat_dim = _grand_feat_dict.dimensionality();

		_grandsibling_feat_offset = _grand_feat_offset + _grand_feat_dim * (_L+1);
		_grandsibling_feat_dim = _grandsibling_feat_dict.dimensionality();

		_total_feature_dim = _grandsibling_feat_offset + _grandsibling_feat_dim * (_L+1);

		_labels.resize(_L);
		_labels = NULL;
		_label_dict.collect_keys(_labels.c_buf(), _L);




		cerr << "label number: " << _L << endl;
		cerr << "dependency feature dimensionality: " << _dependency_feat_dim << endl;
		cerr << "sibling    feature dimensionality: " << _sibling_feat_dim << endl;
		cerr << "grand      feature dimensionality: " << _grand_feat_dim << endl;
		cerr << "grandsib   feature dimensionality: " << _grandsibling_feat_dim << endl;
		cerr << "total      feature dimensionality: " << _total_feature_dim << endl;
		cerr << "dependency feature start   offset: " << _dependency_feat_offset << endl;
		cerr << "sibling    feature start   offset: " << _sibling_feat_offset << endl;
		cerr << "grand      feature start   offset: " << _grand_feat_offset << endl;

		cerr << "\n done!"; print_time();
	}


	void FGen::create_all_syn_features_according_to_tree( Instance * const inst, const vector<int> &heads, const vector<int> &deprels, sparsevec &sp_fv, const double scale /*= 1.0*/ )
	{
		fvec fv;
		const int length = inst->size();
		assert(heads.size() == length);
		if (!_generation_mode) assert(deprels.size() == length);




		list<string> feats_str;
		if (_use_dependency) {
			for (int i = 1; i < length; ++i) {
				const int head_id = heads[i];
				feats_str.clear();
				addDependencyFeature(inst, head_id, i, feats_str);
				_dependency_feat_dict.map_all(&fv, _dependency_feat_offset, feats_str, _generation_mode);

				if (!_generation_mode) {
					const int deprel_id = deprels[i];
					assert(deprel_id >= 0 && deprel_id < _L);
					fv.offset += _dependency_feat_dim * (deprel_id + 1);
					parameters::sparse_add(sp_fv, &fv, scale);
				}
				fv.dealloc();
			}
		}

		vector< list<int> > children_l;
		vector< list<int> > children_r;

		if (_use_sibling || _use_grand) {
			get_children(heads, children_l, children_r);
		}

		if (_use_sibling) {
			for (int head_id = 0; head_id < length; ++head_id) { // allow multiple roots
				for (int dir = 0; dir <= 1; ++dir) {
					const list<int> &children = dir ? children_l[head_id] : children_r[head_id];

          if (!children.empty()) { 
						
            // first child
            { 
							const int first_child_id = children.front();
							feats_str.clear();
							addSiblingFeature_bohnet(inst, head_id, first_child_id, head_id, feats_str);
							_sibling_feat_dict.map_all(&fv, _sibling_feat_offset, feats_str, _generation_mode);

							if (!_generation_mode) {
								const int deprel_id = deprels[first_child_id];
								assert(deprel_id >= 0 && deprel_id < _L);								
								fv.offset += _sibling_feat_dim * (deprel_id + 1);
								parameters::sparse_add(sp_fv, &fv, scale);
							}
							fv.dealloc();
            }

            {
							list<int>::const_iterator i = children.begin();
							while (true) {
								const int sibling_id = *i++;
								if (i == children.end()) break;
								const int child_id = *i;
								feats_str.clear();
								addSiblingFeature_bohnet(inst, head_id, child_id, sibling_id, feats_str);
								_sibling_feat_dict.map_all(&fv, _sibling_feat_offset, feats_str, _generation_mode);

								if (!_generation_mode) {
									const int deprel_id = deprels[child_id];
									assert(deprel_id >= 0 && deprel_id < _L);
									fv.offset += _sibling_feat_dim * (deprel_id + 1);
									parameters::sparse_add(sp_fv, &fv, scale);
								}
								fv.dealloc();
							}
						}
					}
				}
			}
		}

		if (_use_grand) {
			for (int child_id = 1; child_id < length; ++child_id) {
				const int head_id = heads[child_id];
        for (int dir = 0; dir <= 1; ++dir) {
					const list<int> &gchildren = dir ? children_l[child_id] : children_r[child_id];

					if (gchildren.empty()) { // no grandchild

						feats_str.clear();
						addGrandFeature_bohnet(inst, head_id, child_id, dir ? head_id : child_id, feats_str);
						_grand_feat_dict.map_all(&fv, _grand_feat_offset, feats_str, _generation_mode);

						if (!_generation_mode) {
							const int deprel_id = deprels[child_id];
							assert(deprel_id >= 0 && deprel_id < _L);
							fv.offset += _grand_feat_dim * (deprel_id + 1);
							parameters::sparse_add(sp_fv, &fv, scale);
						}
						fv.dealloc();
          } 

					
          for (list<int>::const_reverse_iterator rit = gchildren.rbegin(); rit != gchildren.rend(); ++rit) {
						const int gchild_id = *rit;
						feats_str.clear();
						addGrandFeature_bohnet(inst, head_id, child_id, gchild_id, feats_str);
						_grand_feat_dict.map_all(&fv, _grand_feat_offset, feats_str, _generation_mode);

						if (!_generation_mode) {
							const int deprel_id = deprels[child_id];
							assert(deprel_id >= 0 && deprel_id < _L);						
							fv.offset += _grand_feat_dim * (deprel_id + 1);
							parameters::sparse_add(sp_fv, &fv, scale);
						}

						fv.dealloc();
						break;
					}
				}
			}
		}

	}

	void FGen::collect_word_postag_label( Instance * const inst, const bool collect_word/*=false*/ )
	{
		assert(_generation_mode);
		const int length = inst->size();
		assign_deprels_int(inst);
	}

	void FGen::assign_deprels_int( Instance * const inst )
	{

		const int length = inst->size();
		inst->deprels_int.resize(length);
		for (int i = 1; i < length; ++i) {
			const int label_id = _label_dict.getFeature(inst->deprels[i], _generation_mode);
			inst->deprels_int[i] = label_id >= 0 ? label_id : 0;
		}
	}



	void FGen::assign_predicted_deprels_str( Instance * const inst )
  {
		const int length = inst->size();
    int K = inst->predicted_heads.nrows();
    inst->predicted_deprels.resize(K,length);
    assert(length == inst->predicted_deprels_int.ncols());
    for(int k = 0; k < K; k++)
    {	  
		  for (int i = 1; i < length; ++i) {
			  const int label_id = inst->predicted_deprels_int[k][i];
			  assert(label_id >= 0 && label_id < _labels.size());
			  inst->predicted_deprels[k][i] = _labels[label_id];
		  }
    }
	}


  void FGen::create_all_feature_vectors_margin( Instance * const inst, sparsevec &sp_fv, const double scale /*= 1.0*/ )
	{
    fvec fv;
		const int len = inst->size();

    list<string> feats_str;		
		if (_use_dependency) {

			for (int h = 0; h < len; ++h) {
				for (int m = 1; m < len; ++m) {
					if (h == m) continue;
          feats_str.clear();
					addDependencyFeature(inst, h, m, feats_str);
					_dependency_feat_dict.map_all(&fv, _dependency_feat_offset, feats_str, _generation_mode);
          if (!_generation_mode){
						for (int l = 0; l < _L; ++l) {
							const int deprel_id = l;
					    fv.offset += _dependency_feat_dim * (deprel_id + 1);
					    parameters::sparse_add(sp_fv, &fv, scale*inst->marginal_scores[m][h][deprel_id]);
						}
					}
          fv.dealloc();
        }
      }
    }


  }


} // namespace gparser_space
