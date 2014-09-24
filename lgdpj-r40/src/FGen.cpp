#include "FGen.h"
#include <iterator>
using namespace std;

#include "StringMap.h"
#include "Options.h"
#include "CharUtils.h"
#include "CppAssert.h"
using namespace egstra;

#define CONF_DEFAULT "d|du|dbi|ds|dbe|p|pu|pb|pch|s|sb|sl|g|gb|gl|gsib"

const string PRP = "PRP";
const string PRP2 = "PRP$";

namespace dparser {


	void FGen::addGrandSiblingFeature( const Instance *inst, const int grand_id, const int head_id, const int child_id, const int sibling_id, list<string> &feats_str, const bool isUseMultiPOS/*=false*/, const int grand_pos_idx/*=-1*/, const int head_pos_idx/*=-1*/, const int child_pos_idx/*=-1*/, const int sibling_pos_idx/*=-1*/ )
	{
		assert(_use_grandsibling);
		assert(head_id != 0);  // [n+1]->[0->]

		assert(grand_id != head_id && head_id != child_id);
		assert(head_id != sibling_id || child_id != sibling_id);
		const bool first_gchild = (head_id == sibling_id);
		const bool last_gchild = (child_id == sibling_id);

		string dir, gdir, feat;
		getDirection(grand_id, head_id, dir);
		getDirection(head_id, child_id, gdir);			

		const string &form_g = inst->forms[grand_id];
		const string &form_h = inst->forms[head_id];
		const string &form_s = (first_gchild || last_gchild) ? NO_FORM: inst->forms[sibling_id];
		const string &form_c = inst->forms[child_id];

		const string &lemm_g = _use_lemma ? inst->lemmas[grand_id] : NO_LEMMA;
		const string &lemm_h = _use_lemma ? inst->lemmas[head_id] : NO_LEMMA;
		const string &lemm_c = _use_lemma ? inst->lemmas[child_id] : NO_LEMMA;
		const string &lemm_s = (_use_lemma && !first_gchild && !last_gchild) ? inst->lemmas[sibling_id] : NO_LEMMA;

		const string &fpos_g = isUseMultiPOS ? inst->p_postags[grand_id][grand_pos_idx] : inst->cpostags[grand_id];
		const string &fpos_h = isUseMultiPOS ? inst->p_postags[head_id][head_pos_idx] : inst->cpostags[head_id];
		const string &fpos_s = (first_gchild || last_gchild) ? NO_CPOSTAG : 
			(isUseMultiPOS ? inst->p_postags[sibling_id][sibling_pos_idx] : inst->cpostags[sibling_id]);
		const string &fpos_c = isUseMultiPOS ? inst->p_postags[child_id][child_pos_idx] : inst->cpostags[child_id];

		string cpos_g, cpos_h, cpos_s, cpos_c;
		if (_use_coarse_postag) {
			cpos_g = (fpos_g != PRP && fpos_g != PRP2 && fpos_g.length() > 2) ? fpos_g.substr(0, 2) : fpos_g;
			cpos_h = (fpos_h != PRP && fpos_h != PRP2 && fpos_h.length() > 2) ? fpos_h.substr(0, 2) : fpos_h;
			cpos_s = (fpos_s != PRP && fpos_s != PRP2 && fpos_s.length() > 2) ? fpos_s.substr(0, 2) : fpos_s;
			cpos_c = (fpos_c != PRP && fpos_c != PRP2 && fpos_c.length() > 2) ? fpos_c.substr(0, 2) : fpos_c;
		}

		feat = "GS0=" + fpos_g + FEAT_SEP + fpos_h + FEAT_SEP + fpos_s + FEAT_SEP + fpos_c;	feats_str.push_back(feat); feats_str.push_back(feat + dir + gdir);
		feat = "GS1=" + form_g + FEAT_SEP + fpos_h + FEAT_SEP + fpos_s + FEAT_SEP + fpos_c;	feats_str.push_back(feat); feats_str.push_back(feat + dir + gdir);
		feat = "GS2=" + fpos_g + FEAT_SEP + form_h + FEAT_SEP + fpos_s + FEAT_SEP + fpos_c;	feats_str.push_back(feat); feats_str.push_back(feat + dir + gdir);
		feat = "GS3=" + fpos_g + FEAT_SEP + fpos_h + FEAT_SEP + form_s + FEAT_SEP + fpos_c;	feats_str.push_back(feat); feats_str.push_back(feat + dir + gdir);
		feat = "GS4=" + fpos_g + FEAT_SEP + fpos_h + FEAT_SEP + fpos_s + FEAT_SEP + form_c;	feats_str.push_back(feat); feats_str.push_back(feat + dir + gdir);
		feat = "GS5=" + form_g + FEAT_SEP + form_h + FEAT_SEP + fpos_s + FEAT_SEP + fpos_c;	feats_str.push_back(feat); feats_str.push_back(feat + dir + gdir);

		feat = "GS8=" + fpos_g + FEAT_SEP + fpos_s + FEAT_SEP + fpos_c;	feats_str.push_back(feat); feats_str.push_back(feat + dir + gdir);
		feat = "GS9=" + form_g + FEAT_SEP + fpos_s + FEAT_SEP + fpos_c;	feats_str.push_back(feat); feats_str.push_back(feat + dir + gdir);

		if (_use_lemma) {
			feat = "GS1L=" + lemm_g + FEAT_SEP + fpos_h + FEAT_SEP + fpos_s + FEAT_SEP + fpos_c;	feats_str.push_back(feat); feats_str.push_back(feat + dir + gdir);
			feat = "GS2L=" + fpos_g + FEAT_SEP + lemm_h + FEAT_SEP + fpos_s + FEAT_SEP + fpos_c;	feats_str.push_back(feat); feats_str.push_back(feat + dir + gdir);
			feat = "GS3L=" + fpos_g + FEAT_SEP + fpos_h + FEAT_SEP + lemm_s + FEAT_SEP + fpos_c;	feats_str.push_back(feat); feats_str.push_back(feat + dir + gdir);
			feat = "GS4L=" + fpos_g + FEAT_SEP + fpos_h + FEAT_SEP + fpos_s + FEAT_SEP + lemm_c;	feats_str.push_back(feat); feats_str.push_back(feat + dir + gdir);
			feat = "GS5L=" + lemm_g + FEAT_SEP + lemm_h + FEAT_SEP + fpos_s + FEAT_SEP + fpos_c;	feats_str.push_back(feat); feats_str.push_back(feat + dir + gdir);

			feat = "GS9L=" + lemm_g + FEAT_SEP + fpos_s + FEAT_SEP + fpos_c;	feats_str.push_back(feat); feats_str.push_back(feat + dir + gdir);
		}

		if (_use_coarse_postag) {
			feat = "GS0C=" + cpos_g + FEAT_SEP + cpos_h + FEAT_SEP + cpos_s + FEAT_SEP + cpos_c;	feats_str.push_back(feat); feats_str.push_back(feat + dir + gdir);
			feat = "GS1C=" + form_g + FEAT_SEP + cpos_h + FEAT_SEP + cpos_s + FEAT_SEP + cpos_c;	feats_str.push_back(feat); feats_str.push_back(feat + dir + gdir);
			feat = "GS2C=" + cpos_g + FEAT_SEP + form_h + FEAT_SEP + cpos_s + FEAT_SEP + cpos_c;	feats_str.push_back(feat); feats_str.push_back(feat + dir + gdir);
			feat = "GS3C=" + cpos_g + FEAT_SEP + cpos_h + FEAT_SEP + form_s + FEAT_SEP + cpos_c;	feats_str.push_back(feat); feats_str.push_back(feat + dir + gdir);
			feat = "GS4C=" + cpos_g + FEAT_SEP + cpos_h + FEAT_SEP + cpos_s + FEAT_SEP + form_c;	feats_str.push_back(feat); feats_str.push_back(feat + dir + gdir);
			feat = "GS5C=" + form_g + FEAT_SEP + form_h + FEAT_SEP + cpos_s + FEAT_SEP + cpos_c;	feats_str.push_back(feat); feats_str.push_back(feat + dir + gdir);

			feat = "GS8C=" + cpos_g + FEAT_SEP + cpos_s + FEAT_SEP + cpos_c;	feats_str.push_back(feat); feats_str.push_back(feat + dir + gdir);
			feat = "GS9C=" + form_g + FEAT_SEP + cpos_s + FEAT_SEP + cpos_c;	feats_str.push_back(feat); feats_str.push_back(feat + dir + gdir);
		}
	}

	void FGen::addSiblingFeature_bohnet( const Instance *inst, const int head_id, const int child_id, const int sibling_id, list<string> &feats_str, const bool isUseMultiPOS/*=false*/, const int head_pos_idx/*=-1*/, const int child_pos_idx/*=-1*/, const int sibling_pos_idx/*=-1*/ )
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

		const string &cpos_h = isUseMultiPOS ? inst->p_postags[head_id][head_pos_idx] : inst->cpostags[head_id];
		const string &cpos_s = (first_child || last_child) ? NO_CPOSTAG : 
			(isUseMultiPOS ? inst->p_postags[sibling_id][sibling_pos_idx] : inst->cpostags[sibling_id]);
		const string &cpos_c = isUseMultiPOS ? inst->p_postags[child_id][child_pos_idx] : inst->cpostags[child_id];

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

			feat = "31=" + cpos_h + FEAT_SEP + cpos_s + FEAT_SEP + dir;	if (_labeled) {feats_str.push_back(feat);} feats_str.push_back(feat + dist);
			feat = "33=" + form_h + FEAT_SEP + form_s + FEAT_SEP + dir;	if (_labeled) {feats_str.push_back(feat);} feats_str.push_back(feat + dist);
			feat = "34=" + cpos_h + FEAT_SEP + form_s + FEAT_SEP + dir;	if (_labeled) {feats_str.push_back(feat);} feats_str.push_back(feat + dist);
			feat = "35=" + form_h + FEAT_SEP + cpos_s + FEAT_SEP + dir;	if (_labeled) {feats_str.push_back(feat);} feats_str.push_back(feat + dist);

			feat = "32=" + cpos_c + FEAT_SEP + cpos_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
			feat = "36=" + form_c + FEAT_SEP + form_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
			feat = "37=" + form_c + FEAT_SEP + cpos_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
			feat = "38=" + cpos_c + FEAT_SEP + form_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);

			if (_use_lemma) {
				feat = "97=" + lemm_h + FEAT_SEP + lemm_s + FEAT_SEP + dir;	if (_labeled) {feats_str.push_back(feat);} feats_str.push_back(feat + dist);
				feat = "99=" + cpos_h + FEAT_SEP + lemm_s + FEAT_SEP + dir;	if (_labeled) {feats_str.push_back(feat);} feats_str.push_back(feat + dist);
				feat ="101=" + lemm_h + FEAT_SEP + cpos_s + FEAT_SEP + dir;	if (_labeled) {feats_str.push_back(feat);} feats_str.push_back(feat + dist);

				feat = "98=" + lemm_c + FEAT_SEP + lemm_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
				feat ="100=" + cpos_c + FEAT_SEP + lemm_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
				feat ="102=" + lemm_c + FEAT_SEP + cpos_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
			}

			if (_use_coarse_postag) {
				feat = "30C=" + coarse_pos_h + FEAT_SEP + coarse_pos_s + FEAT_SEP + coarse_pos_c + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
				feat ="31?C=" + form_h + FEAT_SEP + coarse_pos_s + FEAT_SEP + coarse_pos_c + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
				feat ="32?C=" + coarse_pos_h + FEAT_SEP + form_s + FEAT_SEP + coarse_pos_c + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
				feat ="33?C=" + coarse_pos_h + FEAT_SEP + coarse_pos_s + FEAT_SEP + form_c + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);

				feat = "31C=" + coarse_pos_h + FEAT_SEP + coarse_pos_s + FEAT_SEP + dir;	if (_labeled) {feats_str.push_back(feat);} feats_str.push_back(feat + dist);
				feat = "34C=" + coarse_pos_h + FEAT_SEP + form_s + FEAT_SEP + dir;	if (_labeled) {feats_str.push_back(feat);} feats_str.push_back(feat + dist);
				feat = "35C=" + form_h + FEAT_SEP + coarse_pos_s + FEAT_SEP + dir;	if (_labeled) {feats_str.push_back(feat);} feats_str.push_back(feat + dist);

				feat = "32C=" + coarse_pos_c + FEAT_SEP + coarse_pos_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
				feat = "37C=" + form_c + FEAT_SEP + coarse_pos_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
				feat = "38C=" + coarse_pos_c + FEAT_SEP + form_s + FEAT_SEP + dir;	feats_str.push_back(feat); feats_str.push_back(feat + dist);
			}
		}

		if (_use_sibling_linear) {
			const string &cpos_h_L1 = (head_id <= 1) ? NO_CPOSTAG : inst->p_postags[head_id-1][0];
			const string &cpos_h_R1 = (head_id == 0 || head_id+1 >= inst->size()) ? NO_CPOSTAG : inst->p_postags[head_id+1][0];
			const string &cpos_s_L1 = (first_child || last_child || sibling_id <= 1) ? NO_CPOSTAG : inst->p_postags[sibling_id-1][0];
			const string &cpos_s_R1 = (first_child || last_child || sibling_id+1 >= inst->size()) ? NO_CPOSTAG : inst->p_postags[sibling_id+1][0];
			const string &cpos_c_L1 = (child_id <= 1) ? NO_CPOSTAG : inst->p_postags[child_id-1][0];
			const string &cpos_c_R1 = (child_id+1 >= inst->size()) ? NO_CPOSTAG : inst->p_postags[child_id+1][0];

			if (_labeled) { // some features make sense only when labeled.
				feat = "58=" + cpos_h + FEAT_SEP + cpos_s + FEAT_SEP + cpos_s_R1 + FEAT_SEP + dir;	feats_str.push_back(feat);
				feat = "59=" + cpos_h + FEAT_SEP + cpos_s_L1 + FEAT_SEP + cpos_s + FEAT_SEP + dir;	feats_str.push_back(feat);
				feat = "60=" + cpos_h + FEAT_SEP + cpos_h_R1 + FEAT_SEP + cpos_s + FEAT_SEP + dir;	feats_str.push_back(feat);
				feat = "61=" + cpos_h_L1 + FEAT_SEP + cpos_h + FEAT_SEP + cpos_s + FEAT_SEP + dir;	feats_str.push_back(feat);

				feat = "62=" + cpos_h_L1 + FEAT_SEP + cpos_h + FEAT_SEP + cpos_s + FEAT_SEP + cpos_s_R1 + FEAT_SEP + dir;	feats_str.push_back(feat);
				feat = "63=" + cpos_h_L1 + FEAT_SEP + cpos_h + FEAT_SEP + cpos_s_L1 + FEAT_SEP + cpos_s + FEAT_SEP + dir;	feats_str.push_back(feat);
				feat = "64=" + cpos_h + FEAT_SEP + cpos_h_R1 + FEAT_SEP + cpos_s + FEAT_SEP + cpos_s_R1 + FEAT_SEP + dir;	feats_str.push_back(feat);
				feat = "65=" + cpos_h + FEAT_SEP + cpos_h_R1 + FEAT_SEP + cpos_s_L1 + FEAT_SEP + cpos_s + FEAT_SEP + dir;	feats_str.push_back(feat);
			}

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

				if (_labeled) { // some features make sense only when labeled.
					feat = "58C=" + coarse_pos_h + FEAT_SEP + coarse_pos_s + FEAT_SEP + coarse_pos_s_R1 + FEAT_SEP + dir;	feats_str.push_back(feat);
					feat = "59C=" + coarse_pos_h + FEAT_SEP + coarse_pos_s_L1 + FEAT_SEP + coarse_pos_s + FEAT_SEP + dir;	feats_str.push_back(feat);
					feat = "60C=" + coarse_pos_h + FEAT_SEP + coarse_pos_h_R1 + FEAT_SEP + coarse_pos_s + FEAT_SEP + dir;	feats_str.push_back(feat);
					feat = "61C=" + coarse_pos_h_L1 + FEAT_SEP + coarse_pos_h + FEAT_SEP + coarse_pos_s + FEAT_SEP + dir;	feats_str.push_back(feat);

					feat = "62C=" + coarse_pos_h_L1 + FEAT_SEP + coarse_pos_h + FEAT_SEP + coarse_pos_s + FEAT_SEP + coarse_pos_s_R1 + FEAT_SEP + dir;	feats_str.push_back(feat);
					feat = "63C=" + coarse_pos_h_L1 + FEAT_SEP + coarse_pos_h + FEAT_SEP + coarse_pos_s_L1 + FEAT_SEP + coarse_pos_s + FEAT_SEP + dir;	feats_str.push_back(feat);
					feat = "64C=" + coarse_pos_h + FEAT_SEP + coarse_pos_h_R1 + FEAT_SEP + coarse_pos_s + FEAT_SEP + coarse_pos_s_R1 + FEAT_SEP + dir;	feats_str.push_back(feat);
					feat = "65C=" + coarse_pos_h + FEAT_SEP + coarse_pos_h_R1 + FEAT_SEP + coarse_pos_s_L1 + FEAT_SEP + coarse_pos_s + FEAT_SEP + dir;	feats_str.push_back(feat);
				}

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
	void FGen::addGrandFeature_bohnet( const Instance *inst, const int head_id, const int child_id, const int gchild_id, list<string> &feats_str, const bool isUseMultiPOS/*=false*/, const int head_pos_idx/*=-1*/, const int child_pos_idx/*=-1*/, const int gchild_pos_idx/*=-1*/ )
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

		const string &cpos_h = isUseMultiPOS ? inst->p_postags[head_id][head_pos_idx] : inst->cpostags[head_id];
		const string &cpos_c = isUseMultiPOS ? inst->p_postags[child_id][child_pos_idx] : inst->cpostags[child_id];
		const string &cpos_g = no_gchild ? NO_CPOSTAG : 
			(isUseMultiPOS ? inst->p_postags[gchild_id][gchild_pos_idx] : inst->cpostags[gchild_id]);

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
			const string &cpos_h_L1 = (head_id <= 1) ? NO_CPOSTAG : inst->p_postags[head_id-1][0];
			const string &cpos_h_R1 = (head_id == 0 || head_id+1 >= inst->size()) ? NO_CPOSTAG : inst->p_postags[head_id+1][0];
			const string &cpos_c_L1 = (child_id <= 1) ? NO_CPOSTAG : inst->p_postags[child_id-1][0];
			const string &cpos_c_R1 = (child_id+1 >= inst->size()) ? NO_CPOSTAG : inst->p_postags[child_id+1][0];	
			const string &cpos_g_L1 = (no_gchild || gchild_id <= 1) ? NO_CPOSTAG : inst->p_postags[gchild_id-1][0];
			const string &cpos_g_R1 = (no_gchild || gchild_id+1 >= inst->size()) ? NO_CPOSTAG : inst->p_postags[gchild_id+1][0];
			
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

	void FGen::addDependencyFeature_bohnet( const Instance *inst, const int head_id, const int child_id, list<string> &feats_str, const bool isUseMultiPOS/*=false*/, const int head_pos_idx/*=-1*/, const int child_pos_idx/*=-1*/  ) const
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
		const string &cpos_h = isUseMultiPOS ? inst->p_postags[head_id][head_pos_idx] : inst->cpostags[head_id];
		const string &cpos_c = isUseMultiPOS ? inst->p_postags[child_id][child_pos_idx] : inst->cpostags[child_id];

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

			const string &cpos_h_L1 = (head_id <= 1) ? NO_CPOSTAG : inst->p_postags[head_id-1][0];
			const string &cpos_h_R1 = (is_root || head_id+1 >= inst->size()) ? NO_CPOSTAG : inst->p_postags[head_id+1][0];
			const string &cpos_c_L1 = (child_id <= 1) ? NO_CPOSTAG : inst->p_postags[child_id-1][0];
			const string &cpos_c_R1 = (child_id+1 >= inst->size()) ? NO_CPOSTAG : inst->p_postags[child_id+1][0];

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

	void FGen::addPOSFeature_unigram( const Instance *inst, const int node_id, list<string> &feats_str ) const
	{
		const string prefix = "PU-";
		string feat, c_beg, c_end;

		if (_english)
		{
			const string &form = inst->forms[node_id];

			const string &w_L1 = node_id > 1 ? inst->forms[node_id-1] : NO_FORM;
			const string &w_R1 = node_id < inst->size() - 1 ? inst->forms[node_id+1] : NO_FORM;
			const string &w_L2 = node_id > 2 ? inst->forms[node_id-2] : NO_FORM;
			const string &w_R2 = node_id < inst->size() - 2 ? inst->forms[node_id+2] : NO_FORM;
			
			feat = prefix + "w=" + form;		feats_str.push_back(feat);
			feat = prefix + inst->contain_hyphen[node_id];		feats_str.push_back(feat);
			feat = prefix + inst->contain_number[node_id];		feats_str.push_back(feat);
			feat = prefix + inst->contain_uppercase_char[node_id];		feats_str.push_back(feat);

			feat = prefix + "w-1=" + w_L1;		feats_str.push_back(feat);
			feat = prefix + "w+1=" + w_R1;		feats_str.push_back(feat);
			feat = prefix + "w-2=" + w_L2;		feats_str.push_back(feat);
			feat = prefix + "w+1=" + w_R2;		feats_str.push_back(feat);

			string form_lc = toLower(form);
			feat = prefix + "wlc=" + form_lc;		feats_str.push_back(feat);

			const int len = form_lc.size();
			for (int i = 1; i <= 4 && i <= form_lc.size(); ++i) {
				feat = prefix + "prefix=" + form_lc.substr(0, i);		feats_str.push_back(feat);
				feat = prefix + "suffix=" + form_lc.substr(len-i, i);		feats_str.push_back(feat);
			}
		}
		else // Chinese: same as ZhangYue08
		{			
			const string &form = inst->forms[node_id];

			const string &w_L1 = node_id > 1 ? inst->forms[node_id-1] : NO_FORM;
			const string &w_R1 = node_id < inst->size() - 1 ? inst->forms[node_id+1] : NO_FORM;

			feat = prefix + "0=" + form;		feats_str.push_back(feat);
			feat = prefix + "W+1=" + w_R1;		feats_str.push_back(feat);
			feat = prefix + "W-1=" + w_L1;		feats_str.push_back(feat);

			if (_use_pos_chars) {
				const vector<string> &chars = inst->chars[node_id];
				const string &c_L1 = node_id > 1 ? inst->chars[node_id-1][ inst->chars[node_id-1].size()-1 ] : NO_FORM;
				const string &c_R1 = node_id < inst->size() - 1 ? inst->chars[node_id+1][ inst->chars[node_id+1].size()-1 ] : NO_FORM;

				c_beg = chars[0];
				c_end = chars[ chars.size()-1 ];
				if (chars.size() == 1) {
					c_beg += "#1";
					c_end = c_beg;
				}

				feat = prefix + "1=" + form + FEAT_SEP + c_L1;			feats_str.push_back(feat);
				feat = prefix + "2=" + form + FEAT_SEP + c_R1;			feats_str.push_back(feat);

				if (chars.size() == 1) {
					feat = prefix + "3=" + c_L1 + FEAT_SEP + form + FEAT_SEP + c_R1;				feats_str.push_back(feat);
				}

				feat = prefix + "4=" + c_beg;			feats_str.push_back(feat);
				feat = prefix + "5=" + c_end;			feats_str.push_back(feat);
				if (chars.size() > 2) {						
					for (int i = 2; i < chars.size()-1; ++i) {
						feat = prefix + "6=" + chars[i];									feats_str.push_back(feat);
						feat = prefix + "7=" + c_beg + FEAT_SEP + chars[i];					feats_str.push_back(feat);
						feat = prefix + "8=" + chars[i] + FEAT_SEP + c_end;					feats_str.push_back(feat);
					}
				}

				for (int i = 1; i < chars.size(); ++i) {
					if (chars[i] == chars[i-1]) {
						feat = prefix + "cc=" + chars[i];					feats_str.push_back(feat);
					}
				}

				// 2012.09.03, those features are suggested by Meishan Zhang 
				string curprefix = "";
				string cursuffix = "";
				const int chars_num = chars.size();
				for(int i = 0; i <= 3 && i < chars_num; ++i)
				{
					curprefix = curprefix + chars[i];
					cursuffix = chars[chars_num-i-1] + cursuffix;
					feat = prefix + "prefix=" + curprefix;		feats_str.push_back(feat);
					feat = prefix + "suffix=" + cursuffix;		feats_str.push_back(feat);			   
				}

				string length = "5";
				if (chars.size() < 5) { 
					ostringstream out;
					out << chars.size();
					length = out.str();
				}

				feat = prefix + "length=" + length;		feats_str.push_back(feat);
			}
		}
	}

	void FGen::addPOSFeature_bigram( const Instance *inst, const int node_id, list<string> &feats_str, const bool isUseMultiPOS/*=false*/, const int pos_i_L1/*=-1 */ ) const
	{
		const string &cpos_L1 = node_id <= 1 ? NO_CPOSTAG :
			(isUseMultiPOS ? inst->p_postags[node_id-1][pos_i_L1] : inst->cpostags[node_id-1]);
		const string &cpos_L2 = node_id <= 2 ? NO_CPOSTAG : inst->p_postags[node_id-2][0];

		string feat;
		feat = "PB-" + cpos_L1; 						feats_str.push_back(feat);
		feat = "PT-" + cpos_L2 + FEAT_SEP + cpos_L1;	feats_str.push_back(feat);
	}


	void FGen::process_options()
	{
		int tmp;

		_simulate_pipeline = false;
		if(options::get("simulate-pipeline", tmp)) {
			_simulate_pipeline = tmp;
		}

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


		_use_filtered_heads = false;
		if(options::get("use-filtered-heads", tmp)) {
			_use_filtered_heads = (1 == tmp);
		}

		_labeled = false;
		if(options::get("labeled", tmp)) {
			_labeled = (1 == tmp);
		}

		_use_filtered_labels = false;
		if (_labeled && _use_filtered_heads) {
			if(options::get("use-filtered-labels", tmp)) {
				_use_filtered_labels = (1 == tmp);
			}
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

		_use_unlabeled_syn_features = false;
		if (_labeled) {
			if(options::get("use-unlabeled-syn-features", tmp)) {
				_use_unlabeled_syn_features = tmp;
			}
		} else {
			_use_unlabeled_syn_features = true;
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
		
		_use_grandsibling = false;

		_use_pos_feat = false;
		_use_pos_unigram = false;
		_use_pos_bigram = false;
		_use_pos_chars = false;

		string conf;
		if(!options::get("fconf", conf)) {
			cerr << _name << " : using default feature configuration \""
				<< CONF_DEFAULT << "\"" << endl;
			conf = CONF_DEFAULT;
		}

		cerr << _name << " : feature conf \"" << conf << "\"" << endl;
		vector<string> flags;
		simpleTokenize(conf, flags, "|");

		for (vector<string>::const_iterator i = flags.begin(); i != flags.end(); ++i) {
				if (*i == "d") {
					cerr << _name << " : activating dependency features\n";
					_use_dependency = true;
				} else if (*i == "du") {
					cerr << _name << " : activating dependency unigram features\n";
					_use_dependency_unigram = true;
				} else if (*i == "dbi") {
					cerr << _name << " : activating dependency bigram features\n";
					_use_dependency_bigram = true;
				} else if (*i == "ds") {
					cerr << _name << " : activating dependency surrounding features\n";
					_use_dependency_surrounding = true;
				} else if (*i == "dbe") {
					cerr << _name << " : activating dependency between features\n";
					_use_dependency_between = true;

				} else if (*i == "s") {
					cerr << _name << " : activating sibling features\n";
					_use_sibling = true;
				} else if (*i == "sb") {
					cerr << _name << " : activating sibling basic features\n";
					_use_sibling_basic = true;
				} else if (*i == "sl") {
					cerr << _name << " : activating sibling linear features\n";
					_use_sibling_linear = true;

				} else if (*i == "g") {
					cerr << _name << " : activating grand features\n";
					_use_grand = true;
				} else if (*i == "gb") {
					cerr << _name << " : activating grand basic features\n";
					_use_grand_basic = true;
				} else if (*i == "gl") {
					cerr << _name << " : activating grand linear features\n";
					_use_grand_linear = true;
				} else if (*i == "gsib") {
					cerr << _name << " : activating grandsibling features\n";
					_use_grandsibling = true;

				} else if (*i == "p") {
					cerr << _name << " : activating pos features\n";
					_use_pos_feat = true;
				} else if (*i == "pu") {
					cerr << _name << " : activating pos unigram features\n";
					_use_pos_unigram = true;
				} else if (*i == "pb") {
					cerr << _name << " : activating pos bigram features\n";
					_use_pos_bigram = true;
				} else if (*i == "pch") {
					cerr << _name << " : activating pos char-based features\n";
					_use_pos_chars = true;
				} else {
					string str = "unknown feature flag \"" + (*i) + "\"";
					usage(str.c_str()); exit(1);
				}
		}

		_use_last_sibling_features = false;
		_use_no_grand_features = false;

		if(options::get("use-last-sibling-features", tmp)) {
			_use_last_sibling_features = tmp;
		}
		if(options::get("use-no-grand-features", tmp)) {
			_use_no_grand_features = tmp;
		}

		string decoder_name;
		assert( options::get("decoder", decoder_name) );
		if (decoder_name == "1o") {
			_use_sibling = false;
			_use_grand = false;
			_use_grandsibling = false;
		} else if (decoder_name == "2o-carreras") {
			_use_outermost_grand_features = true;
			assert(!_use_last_sibling_features);
			_use_grandsibling = false;
		} else if (decoder_name == "3o-koo") {
			_use_outermost_grand_features = false;
			assert(!_labeled);
		} 

		if (_use_no_grand_features) cerr << "\t\t*** " << _name << " : " << decoder_name << " use no-grand features\n";
		if (_use_last_sibling_features) cerr << "\t\t*** " << _name << " : " << decoder_name << " use last-sibling features\n";

		if (_simulate_pipeline) {
			_use_pos_feat = false;
			_use_pos_unigram = false;
			_use_pos_bigram = false;
			_use_pos_chars = false;
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
		cerr << "     p    : pos features" << endl;
		cerr << "     pu   : pos unigram features" << endl;
		cerr << "     pb   : pos bigram features" << endl;
		cerr << "     pc   : pos char-based features" << endl;

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
			NRMat3d<fvec> *m3 = inst->fvec_sib.c_buf();
			for (int i = 0; i < inst->fvec_sib.total_size(); ++i, ++m3) {
				fvec * pfv = m3->c_buf();
				for (int j = 0; j < m3->total_size(); ++j, ++pfv) {
					pfv->dealloc();
				}
			}
			inst->fvec_sib.dealloc();
			inst->prob_sib.dealloc();
			if (_labeled) {
				inst->fvec_sibl.dealloc();
				inst->prob_sibl.dealloc();
			}
		}

		if (_use_grand) {
			NRMat3d<fvec> *m3 = inst->fvec_grd.c_buf();
			for (int i = 0; i < inst->fvec_grd.total_size(); ++i, ++m3) {
				fvec * pfv = m3->c_buf();
				for (int j = 0; j < m3->total_size(); ++j, ++pfv) {
					pfv->dealloc();
				}
			}
			inst->fvec_grd.dealloc();
			inst->prob_grd.dealloc();
			if (_labeled) {
				inst->fvec_grdl.dealloc();
				inst->prob_grdl.dealloc();
			}
		}

		if (_use_grandsibling) {
			NRMat4d<fvec> *m4 = inst->fvec_grdsib.c_buf();
			for (int i = 0; i < inst->fvec_grdsib.total_size(); ++i, ++m4) {
				fvec * pfv = m4->c_buf();
				for (int j = 0; j < m4->total_size(); ++j, ++pfv) {
					pfv->dealloc();
				}
			}
			inst->fvec_grdsib.dealloc();
			inst->prob_grdsib.dealloc();
		}

		if (_use_dependency) {
			NRMat<fvec> *m2 = inst->fvec_dep.c_buf();
			for (int i = 0; i < inst->fvec_dep.total_size(); ++i, ++m2) {
				fvec * pfv = m2->c_buf();
				for (int j = 0; j < m2->total_size(); ++j, ++pfv) {
					pfv->dealloc();
				}
			}
			inst->fvec_dep.dealloc();
			inst->prob_dep.dealloc();
			if (_labeled) {
				inst->fvec_depl.dealloc();
				inst->prob_depl.dealloc();
			}
		}

		if (_use_pos_feat) {
			if(_use_pos_unigram) {
				for (int i = 1; i < len; ++i) {
					inst->fvec_pos1[i][0].dealloc(); // delete *fvec::idx
				}
				inst->fvec_pos1.dealloc();
				inst->prob_pos1.dealloc();
			}
			if (_use_pos_bigram) {
				for (int i = 1; i < len; ++i) {
					NRMat<fvec> &fv_mat = inst->fvec_pos2[i];
					for (int piL1 = 0; piL1 < fv_mat.nrows(); ++piL1) {
						(fv_mat[piL1][0]).dealloc(); // delete *fvec::idx
					}
				}
				inst->fvec_pos2.dealloc();
				inst->prob_pos2.dealloc();
			}
		}
	}

	void FGen::create_all_feature_vectors( Instance * const inst )
	{
		const int len = inst->size();
		list<string> feats_str;

		if (_use_sibling) {
			inst->fvec_sib.resize(len, len, len);
			inst->prob_sib.resize(len, len, len);
			if (_labeled && !_generation_mode) {
				inst->fvec_sibl.resize(len, len, len);
				inst->prob_sibl.resize(len, len, len);
			}
			for (int h = 0; h < len; ++h) {
				const int pos_h_num = inst->p_postags_num[h];
				for (int m = 1; m < len; ++m) {
					if (h == m) continue;
					if (!inst->candidate_heads[m][h]) continue;
					const int pos_m_num = inst->p_postags_num[m];
					const int step = h < m ? 1 : -1;
					const int end = h < m ? m+1 : m-1;
					for (int s = h; s != end; s += step) { // when s == h, first child; when s == m, last child
						if (s != h && !inst->candidate_heads[s][h]) continue;
						if (s == m && !_use_last_sibling_features) continue;
						const int pos_s_num = inst->p_postags_num[s];
						inst->fvec_sib[h][m][s].resize(pos_h_num, pos_m_num, pos_s_num);
						inst->prob_sib[h][m][s].resize(pos_h_num, pos_m_num, pos_s_num);
						inst->fvec_sib[h][m][s] = fvec();
						inst->prob_sib[h][m][s] = DOUBLE_NEGATIVE_INFINITY;
						if (_labeled && !_generation_mode) {
							const int label_num = _use_filtered_labels ? inst->candidate_labels[m][h].size() : _L;
							assert(label_num > 0);
							(inst->fvec_sibl[h][m][s]).resize(label_num, pos_h_num, pos_m_num, pos_s_num);
							(inst->prob_sibl[h][m][s]).resize(label_num, pos_h_num, pos_m_num, pos_s_num);
							inst->fvec_sibl[h][m][s] = fvec();
							inst->prob_sibl[h][m][s] = DOUBLE_NEGATIVE_INFINITY;	
						}
						for (int ph = 0; ph < pos_h_num; ++ph) {
							for (int pm = 0; pm < pos_m_num; ++pm) {
								for (int ps = 0; ps < pos_s_num; ++ps) {
									if (s == h && ps != ph) continue;
									if (s == m && ps != pm) continue;
									fvec * const fv = &inst->fvec_sib[h][m][s][ph][pm][ps];
									feats_str.clear();
									addSiblingFeature_bohnet(inst, h, m, s, feats_str, true, ph, pm, ps);
									_sibling_feat_dict.map_all(fv, _sibling_feat_offset, feats_str, _generation_mode);
									if (_labeled && !_generation_mode) {
										for (int il = 0; il < inst->fvec_sibl[h][m][s].dim1(); ++il) {
											const int label_id = _use_filtered_labels ? get_label_id(inst->candidate_labels[m][h][il]) : il;
											fvec *fv_l = &inst->fvec_sibl[h][m][s][il][ph][pm][ps];
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
				}
			}
		}

		if (_use_grandsibling) {
			assert(!_labeled);
			inst->fvec_grdsib.resize(len, len, len, len);
			inst->prob_grdsib.resize(len, len, len, len);
			for (int h = 1; h < len; ++h) {
				const int pos_h_num = inst->p_postags_num[h];
				for (int m = 1; m < len; ++m) {
					if (h == m) continue;
					if (!inst->candidate_heads[m][h]) continue;
					const int pos_m_num = inst->p_postags_num[m];
					const int step = h < m ? 1 : -1;
					const int end = h < m ? m+1 : m-1;
					for (int s = h; s != end; s += step) { // when s == h, first grandchild; when s == m, last grandchild
						if (s != h && !inst->candidate_heads[s][h]) continue;
						const int pos_s_num = inst->p_postags_num[s];
						for (int g = 0; g < len; ++g) { // grandparent id
							if (g == min(h,m)) {
								g = max(h,m);
								continue;
							}
							if (!inst->candidate_heads[h][g]) continue;
							const int pos_g_num = inst->p_postags_num[g];
							inst->fvec_grdsib[g][h][m][s].resize(pos_g_num, pos_h_num, pos_m_num, pos_s_num);
							inst->prob_grdsib[g][h][m][s].resize(pos_g_num, pos_h_num, pos_m_num, pos_s_num);
							inst->fvec_grdsib[g][h][m][s] = fvec();
							inst->prob_grdsib[g][h][m][s] = DOUBLE_NEGATIVE_INFINITY;
							for (int pg = 0; pg < pos_g_num; ++pg) {
								for (int ph = 0; ph < pos_h_num; ++ph) {
									for (int pm = 0; pm < pos_m_num; ++pm) {
										for (int ps = 0; ps < pos_s_num; ++ps) {
											if (s == h && ps != ph) continue;
											if (s == m && ps != pm) continue;
											fvec * const fv = &inst->fvec_grdsib[g][h][m][s][pg][ph][pm][ps];
											feats_str.clear();
											addGrandSiblingFeature(inst, g, h, m, s, feats_str, true, pg, ph, pm, ps);
											_grandsibling_feat_dict.map_all(fv, _grandsibling_feat_offset, feats_str, _generation_mode);
										}
									}
								}
							}
						}
					}
				}
			}
		}

		if (_use_grand) {
			inst->fvec_grd.resize(len, len, len);
			inst->prob_grd.resize(len, len, len);
			if (_labeled && !_generation_mode) {
				inst->fvec_grdl.resize(len, len, len);
				inst->prob_grdl.resize(len, len, len);
			}
			for (int h = 0; h < len; ++h) {
				const int pos_h_num = inst->p_postags_num[h];
				for (int m = 1; m < len; ++m) {
					if (h == m) continue;
					if (!inst->candidate_heads[m][h]) continue;
					const int pos_m_num = inst->p_postags_num[m];
					const int step = h < m ? 1 : -1;
					const int end = h < m ? len : 0;
					// when gch == h, [h->m] has no left-side grandchild; 
					// when gch == m, [h->m] has no right-side grandchild.
					for (int gch = h; gch != end; gch += step) {
						if (gch != h && gch!= m && !inst->candidate_heads[gch][m]) continue;
						if ( (gch == h || gch == m) && !_use_no_grand_features ) continue;
						const int pos_g_num = inst->p_postags_num[gch];
						inst->fvec_grd[h][m][gch].resize(pos_h_num, pos_m_num, pos_g_num);
						inst->prob_grd[h][m][gch].resize(pos_h_num, pos_m_num, pos_g_num);
						inst->fvec_grd[h][m][gch] = fvec();
						inst->prob_grd[h][m][gch] = DOUBLE_NEGATIVE_INFINITY;
						if (_labeled && !_generation_mode) {
							const int label_num = _use_filtered_labels ? inst->candidate_labels[m][h].size() : _L;
							assert(label_num > 0);
							(inst->fvec_grdl[h][m][gch]).resize(label_num, pos_h_num, pos_m_num, pos_g_num);
							(inst->prob_grdl[h][m][gch]).resize(label_num, pos_h_num, pos_m_num, pos_g_num);
							inst->fvec_grdl[h][m][gch] = fvec();
							inst->prob_grdl[h][m][gch] = DOUBLE_NEGATIVE_INFINITY;
						}
						for (int ph = 0; ph < pos_h_num; ++ph) {
							for (int pm = 0; pm < pos_m_num; ++pm) {
								for (int pg = 0; pg < pos_g_num; ++pg) {
									if (gch == h && pg != ph
										||
										gch == m && pg != pm) continue;

									fvec * const fv = &inst->fvec_grd[h][m][gch][ph][pm][pg];
									feats_str.clear();
									addGrandFeature_bohnet(inst, h, m, gch, feats_str, true, ph, pm, pg);
									_grand_feat_dict.map_all(fv, _grand_feat_offset, feats_str, _generation_mode);
									if (_labeled && !_generation_mode) {
										for (int il = 0; il < inst->fvec_grdl[h][m][gch].dim1(); ++il) {
											const int label_id = _use_filtered_labels ? get_label_id(inst->candidate_labels[m][h][il]) : il;
											fvec *fv_l = &inst->fvec_grdl[h][m][gch][il][ph][pm][pg];
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
				}
			}
		}
		
		if (_use_dependency) {
			inst->fvec_dep.resize(len, len);
			inst->prob_dep.resize(len, len);
			if (_labeled && !_generation_mode) {
				inst->fvec_depl.resize(len, len);
				inst->prob_depl.resize(len, len);
			}

			for (int h = 0; h < len; ++h) {
				for (int m = 1; m < len; ++m) {
					if (h == m) continue;
					if (!inst->candidate_heads[m][h]) continue;
					const int pos_num_h = inst->p_postags_num[h];
					const int pos_num_m = inst->p_postags_num[m];
					(inst->fvec_dep[h][m]).resize(pos_num_h, pos_num_m);
					(inst->prob_dep[h][m]).resize(pos_num_h, pos_num_m);
					inst->fvec_dep[h][m] = fvec();
					inst->prob_dep[h][m] = DOUBLE_NEGATIVE_INFINITY;
					if (_labeled && !_generation_mode) {
						const int label_num = _use_filtered_labels ? inst->candidate_labels[m][h].size() : _L;
						assert(label_num > 0);
						(inst->fvec_depl[h][m]).resize(label_num, pos_num_h, pos_num_m);
						(inst->prob_depl[h][m]).resize(label_num, pos_num_h, pos_num_m);
						inst->fvec_depl[h][m] = fvec();
						inst->prob_depl[h][m] = DOUBLE_NEGATIVE_INFINITY;
					}

					NRMat<fvec> &fv_mat = inst->fvec_dep[h][m];
					for (int ph = 0; ph < fv_mat.nrows(); ++ph) {
						for (int pm = 0; pm < fv_mat.ncols(); ++pm) {
							fvec * const fv = &(fv_mat[ph][pm]);
							feats_str.clear();
							addDependencyFeature(inst, h, m, feats_str, true, ph, pm);
							_dependency_feat_dict.map_all(fv, _dependency_feat_offset, feats_str, _generation_mode);
							if (_labeled && !_generation_mode) {
								for (int il = 0; il < inst->fvec_depl[h][m].dim1(); ++il) {
									const int label_id = _use_filtered_labels ? get_label_id(inst->candidate_labels[m][h][il]) : il;
									fvec *fv_l = &inst->fvec_depl[h][m][il][ph][pm];
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
		}

		if (_use_pos_feat) {
			if (_use_pos_unigram) {
				inst->fvec_pos1.resize(len);
				inst->prob_pos1.resize(len);
				for (int i = 1; i < len; ++i) {
					const int pos_num_i = inst->p_postags_num[i];
					(inst->fvec_pos1[i]).resize(pos_num_i);
					(inst->prob_pos1[i]).resize(pos_num_i);
					inst->fvec_pos1[i] = fvec();
					inst->prob_pos1[i] = DOUBLE_NEGATIVE_INFINITY;

					const fvec * const fv0 = &inst->fvec_pos1[i][0];
					for (int pi = 0; pi < pos_num_i; ++pi) {
						const int tag_id = _postag_dict.getFeature(inst->p_postags[i][pi], _generation_mode);
						assert(tag_id >= 0);
						const int offset = _pos_feat_offset + _pos_feat_dim * tag_id;
						fvec * const fv = &inst->fvec_pos1[i][pi];
			
						if (pi == 0) {
							feats_str.clear();
							addPOSFeature_unigram(inst, i, feats_str);
							_pos_feat_dict.map_all(fv, offset, feats_str, _generation_mode);
						} else {
							fv->idx = fv0->idx;
							fv->n = fv0->n;
							fv->val = fv0->val;
							fv->offset = offset;
						}
					}
				}
			}

			if (_use_pos_bigram) {
				inst->fvec_pos2.resize(len);
				inst->prob_pos2.resize(len);
				for (int i = 1; i < len; ++i) {
					const int pos_num_i = inst->p_postags_num[i];
					const int pos_num_iL1 = inst->p_postags_num[i-1];
					(inst->fvec_pos2[i]).resize(pos_num_iL1, pos_num_i);
					(inst->prob_pos2[i]).resize(pos_num_iL1, pos_num_i);
					inst->fvec_pos2[i] = fvec();
					inst->prob_pos2[i] = DOUBLE_NEGATIVE_INFINITY;

					for (int piL1 = 0; piL1 < pos_num_iL1; ++piL1) {
						const fvec * const fv0 = &inst->fvec_pos2[i][piL1][0];
						for (int pi = 0; pi < pos_num_i; ++pi) {
							const int tag_id = _postag_dict.getFeature(inst->p_postags[i][pi], _generation_mode);
							assert(tag_id >= 0);
							const int offset = _pos_feat_offset + _pos_feat_dim * tag_id;
							fvec * const fv = &inst->fvec_pos2[i][piL1][pi];

							if (pi == 0) {
								feats_str.clear();
								addPOSFeature_bigram(inst, i, feats_str, true, piL1);
								_pos_feat_dict.map_all(fv, offset, feats_str, _generation_mode);
							} else {
								fv->idx = fv0->idx;
								fv->n = fv0->n;
								fv->val = fv0->val;
								fv->offset = offset;
							}
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

		if (_labeled) _label_dict.save(dictdir + "/label.dict.gz");

		_postag_dict.save(dictdir + "/postag.dict.gz");

		_word_dict.save(dictdir + "/word.dict.gz");

		if (_use_pos_feat) _pos_feat_dict.save(dictdir + "/pos.features.gz");
		if (_use_dependency) _dependency_feat_dict.save(dictdir + "/dependency.features.gz");
		if (_use_sibling) _sibling_feat_dict.save(dictdir + "/sibling.features.gz");
		if (_use_grand) _grand_feat_dict.save(dictdir + "/grand.features.gz");
		if (_use_grandsibling) _grandsibling_feat_dict.save(dictdir + "/grandsibling.features.gz");
	}

	void FGen::load_dictionaries( const string &dictdir )
	{
		assert(!_generation_mode);
		cerr << _name << " : loading feature dictionaries from \""
			<< dictdir << "\""; print_time();

		if (_labeled) {
			_label_dict.load(dictdir + "/label.dict.gz", 0);
			_L = _label_dict.dimensionality();
		}

		_postag_dict.load(dictdir + "/postag.dict.gz", 0);
		_T = _postag_dict.dimensionality();

		_word_dict.load(dictdir + "/word.dict.gz", 0);

		if (_use_pos_feat) _pos_feat_dict.load(dictdir + "/pos.features.gz", _fcutoff);
		if (_use_dependency) _dependency_feat_dict.load(dictdir + "/dependency.features.gz", _fcutoff);
		if (_use_sibling) _sibling_feat_dict.load(dictdir + "/sibling.features.gz", _fcutoff);
		if (_use_grand) _grand_feat_dict.load(dictdir + "/grand.features.gz", _fcutoff);
		if (_use_grandsibling) _grandsibling_feat_dict.load(dictdir + "/grandsibling.features.gz", _fcutoff);

		_pos_feat_offset = 0;
		_pos_feat_dim = _pos_feat_dict.dimensionality();

		_dependency_feat_offset = _pos_feat_offset + _pos_feat_dim * _T;
		_dependency_feat_dim = _dependency_feat_dict.dimensionality();

		_sibling_feat_offset = _dependency_feat_offset + _dependency_feat_dim * (_labeled ? _L+1 : 1);
		_sibling_feat_dim = _sibling_feat_dict.dimensionality();

		_grand_feat_offset = _sibling_feat_offset + _sibling_feat_dim * (_labeled ? _L+1 : 1);
		_grand_feat_dim = _grand_feat_dict.dimensionality();

		_grandsibling_feat_offset = _grand_feat_offset + _grand_feat_dim * (_labeled ? _L+1 : 1);
		_grandsibling_feat_dim = _grandsibling_feat_dict.dimensionality();

		_total_feature_dim = _grandsibling_feat_offset + _grandsibling_feat_dim * (_labeled ? _L+1 : 1);

		if (_labeled) {
			_labels.resize(_L);
			_labels = NULL;
			_label_dict.collect_keys(_labels.c_buf(), _L);
		}

		// set T and L in options, needed by decoder
		int tmp;
		if (options::get("T", tmp)) {
			cerr << "\"T\" already exists in options: " << tmp << endl;
			exit(0);
		}
		ostringstream o;
		o << _T;
		options::set(string("T"), o.str(), true);
		if (_labeled) {
			if (options::get("L", tmp)) {
				cerr << "\"L\" already exists in options: " << tmp << endl;
				exit(0);
			}
			o.str("");
			o << _L;
			options::set("L", o.str(), true);
		}


		cerr << "word  number: " << _word_dict.dimensionality() << endl;
		cerr << "pos   number: " << _T << endl;
		cerr << "label number: " << _L << endl;
		cerr << "pos        feature dimensionality: " << _pos_feat_dim << endl;
		cerr << "dependency feature dimensionality: " << _dependency_feat_dim << endl;
		cerr << "sibling    feature dimensionality: " << _sibling_feat_dim << endl;
		cerr << "grand      feature dimensionality: " << _grand_feat_dim << endl;
		cerr << "grandsib   feature dimensionality: " << _grandsibling_feat_dim << endl;
		cerr << "total      feature dimensionality: " << _total_feature_dim << endl;
		cerr << "pos        feature start   offset: " << _pos_feat_offset << endl;
		cerr << "dependency feature start   offset: " << _dependency_feat_offset << endl;
		cerr << "sibling    feature start   offset: " << _sibling_feat_offset << endl;
		cerr << "grand      feature start   offset: " << _grand_feat_offset << endl;
		cerr << "grandsib   feature start   offset: " << _grandsibling_feat_offset << endl;

		cerr << "\n done!"; print_time();
	}

	void FGen::create_all_pos_features_according_to_tree( Instance * const inst, sparsevec &sp_fv, const double scale /*= 1.0*/, const vector<int> &pos_idx/*=vector<int>()*/ )
	{
		if (!_use_pos_feat) return;
		fvec fv;

		vector<string> bak_cpostags;
		if (pos_idx.size() == inst->size()) {
			bak_cpostags = inst->cpostags;
			for (int i = 1; i < inst->size(); ++i) {
				inst->cpostags[i] = inst->p_postags[i][ pos_idx[i] ];
			}
		}

		list<string> feats_str;
		for (int i = 1; i < inst->size(); ++i) {
			feats_str.clear();
			if (_use_pos_unigram) {
				addPOSFeature_unigram(inst, i, feats_str);
			}
			if (_use_pos_bigram) {
				addPOSFeature_bigram(inst, i, feats_str);
			}
			const int tag_id = _postag_dict.getFeature(inst->cpostags[i], _generation_mode);
			//cerr << inst->cpostags[i] << " " << _generation_mode << endl;
			//assert(tag_id >= 0);
			if (tag_id >= 0) {
				const int offset = _pos_feat_offset + _pos_feat_dim * tag_id;
				_pos_feat_dict.map_all(&fv, offset, feats_str, _generation_mode);
				parameters::sparse_add(sp_fv, &fv, scale);
				fv.dealloc();
			}
		}

		if (pos_idx.size() == inst->size()) {
			inst->cpostags = bak_cpostags;
		}
	}

	void FGen::create_all_syn_features_according_to_tree( Instance * const inst, const vector<int> &heads, const vector<int> &deprels, sparsevec &sp_fv, const double scale /*= 1.0*/, const vector<int> &pos_idx/*=vector<int>()*/, const vector<int> &heads_ref/*=vector<int>()*/, const vector<int> &deprels_ref/*=vector<int>()*/ )
	{
		fvec fv;
		const int length = inst->size();
		assert(heads.size() == length);
		if (_labeled && !_generation_mode) assert(deprels.size() == length);
		if (!pos_idx.empty()) assert(pos_idx.size() == length);

		const bool conservatively_add_partwise_features = (heads.size() == heads_ref.size());
		if (conservatively_add_partwise_features && _labeled) assert(deprels.size() == deprels_ref.size());

		vector<string> bak_cpostags;
		if (!pos_idx.empty()) {
			bak_cpostags = inst->cpostags;
			for (int i = 1; i < length; ++i) {
				inst->cpostags[i] = inst->p_postags[i][ pos_idx[i] ];
			}
		}

		list<string> feats_str;
		if (_use_dependency) {
			for (int i = 1; i < length; ++i) {
				const int head_id = heads[i];
				feats_str.clear();
				addDependencyFeature(inst, head_id, i, feats_str);
				_dependency_feat_dict.map_all(&fv, _dependency_feat_offset, feats_str, _generation_mode);

				if (!conservatively_add_partwise_features || head_id != heads_ref[i]) {
					if(_use_unlabeled_syn_features) parameters::sparse_add(sp_fv, &fv, scale);
				}

				if (_labeled && !_generation_mode) {
					const int deprel_id = deprels[i];
					assert(deprel_id >= 0 && deprel_id < _L);
					if (!conservatively_add_partwise_features || head_id != heads_ref[i] || deprel_id != deprels_ref[i]) {
						fv.offset += _dependency_feat_dim * (deprel_id + 1);
						parameters::sparse_add(sp_fv, &fv, scale);
					}
				}
				fv.dealloc();
			}
		}

		vector< list<int> > children_l;
		vector< list<int> > children_r;
		vector< list<int> > children_l_ref;
		vector< list<int> > children_r_ref;
		if (_use_sibling || _use_grand ||_use_grandsibling) {
			get_children(heads, children_l, children_r);
			if (conservatively_add_partwise_features) 
				get_children(heads_ref, children_l_ref, children_r_ref);
		}

		if (_use_sibling) {
			for (int head_id = 0; head_id < length; ++head_id) { // allow multiple roots
				for (int dir = 0; dir <= 1; ++dir) {
					const list<int> &children = dir ? children_l[head_id] : children_r[head_id];
					const list<int> &children_ref = conservatively_add_partwise_features ? (dir ? children_l_ref[head_id] : children_r_ref[head_id]) : list<int>();
					
					if (!children.empty()) { 
						{ // first child
							const int first_child_id = children.front();
							feats_str.clear();
							addSiblingFeature_bohnet(inst, head_id, first_child_id, head_id, feats_str);
							_sibling_feat_dict.map_all(&fv, _sibling_feat_offset, feats_str, _generation_mode);

							const bool pairwise_correct = conservatively_add_partwise_features 
								&& !children_ref.empty() && children.front() == first_child_id;

							if (!conservatively_add_partwise_features || !pairwise_correct) {
								if(_use_unlabeled_syn_features) parameters::sparse_add(sp_fv, &fv, scale);
							}

							if (_labeled && !_generation_mode) {
								const int deprel_id = deprels[first_child_id];
								assert(deprel_id >= 0 && deprel_id < _L);
								if (!conservatively_add_partwise_features || !pairwise_correct || deprel_id != deprels_ref[first_child_id]) {
									fv.offset += _sibling_feat_dim * (deprel_id + 1);
									parameters::sparse_add(sp_fv, &fv, scale);
								}
							}
							fv.dealloc();
						}

						if (_use_last_sibling_features) {
							const int last_child_id = children.back();
							feats_str.clear();
							addSiblingFeature_bohnet(inst, head_id, last_child_id, last_child_id, feats_str);
							_sibling_feat_dict.map_all(&fv, _sibling_feat_offset, feats_str, _generation_mode);

							const bool pairwise_correct = conservatively_add_partwise_features 
								&& !children_ref.empty() && children.back() == last_child_id;

							if (!conservatively_add_partwise_features || !pairwise_correct) {
								if(_use_unlabeled_syn_features) parameters::sparse_add(sp_fv, &fv, scale);
							}

							if (_labeled && !_generation_mode) {
								const int deprel_id = deprels[last_child_id];
								assert(deprel_id >= 0 && deprel_id < _L);
								if (!conservatively_add_partwise_features || !pairwise_correct || deprel_id != deprels_ref[last_child_id]) {
									fv.offset += _sibling_feat_dim * (deprel_id + 1);
									parameters::sparse_add(sp_fv, &fv, scale);
								}
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

								const bool pairwise_correct = conservatively_add_partwise_features && 
									heads_ref[sibling_id] == head_id && heads_ref[child_id] == head_id &&
									no_modifier_bet(dir ? child_id : sibling_id, dir ? sibling_id : child_id, head_id, heads_ref);

								if (!conservatively_add_partwise_features || !pairwise_correct ) {
									if(_use_unlabeled_syn_features) parameters::sparse_add(sp_fv, &fv, scale);
								}

								if (_labeled && !_generation_mode) {
									const int deprel_id = deprels[child_id];
									assert(deprel_id >= 0 && deprel_id < _L);
									if (!conservatively_add_partwise_features || !pairwise_correct || deprel_id != deprels_ref[child_id]) {
										fv.offset += _sibling_feat_dim * (deprel_id + 1);
										parameters::sparse_add(sp_fv, &fv, scale);
									}
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
					const list<int> &gchildren_ref = conservatively_add_partwise_features ? (dir ? children_l_ref[child_id] : children_r_ref[child_id]) : list<int>();

					if (gchildren.empty() && _use_no_grand_features) { // no grandchild

						feats_str.clear();
						addGrandFeature_bohnet(inst, head_id, child_id, dir ? head_id : child_id, feats_str);
						_grand_feat_dict.map_all(&fv, _grand_feat_offset, feats_str, _generation_mode);

						const bool pairwise_correct = conservatively_add_partwise_features && 
							head_id == heads_ref[child_id] && gchildren_ref.empty();

						if (!conservatively_add_partwise_features || !pairwise_correct) {
							if(_use_unlabeled_syn_features) parameters::sparse_add(sp_fv, &fv, scale);
						}

						if (_labeled && !_generation_mode) {
							const int deprel_id = deprels[child_id];
							assert(deprel_id >= 0 && deprel_id < _L);
							if (!conservatively_add_partwise_features || !pairwise_correct || deprel_id != deprels_ref[child_id]) {
								fv.offset += _grand_feat_dim * (deprel_id + 1);
								parameters::sparse_add(sp_fv, &fv, scale);
							}
						}

						fv.dealloc();
					} 

					
					for (list<int>::const_reverse_iterator rit = gchildren.rbegin(); rit != gchildren.rend(); ++rit) {
						const int gchild_id = *rit;
						feats_str.clear();
						addGrandFeature_bohnet(inst, head_id, child_id, gchild_id, feats_str);
						_grand_feat_dict.map_all(&fv, _grand_feat_offset, feats_str, _generation_mode);

						const bool pairwise_correct = conservatively_add_partwise_features && head_id == heads_ref[child_id] 
						&& 
							(_use_outermost_grand_features && !gchildren_ref.empty() && gchildren_ref.back() == gchild_id
							||
							!_use_outermost_grand_features && heads_ref[gchild_id] == child_id);

						if (!conservatively_add_partwise_features || !pairwise_correct) {
							if(_use_unlabeled_syn_features) parameters::sparse_add(sp_fv, &fv, scale);
						}

						if (_labeled && !_generation_mode) {
							const int deprel_id = deprels[child_id];
							assert(deprel_id >= 0 && deprel_id < _L);
							if (!conservatively_add_partwise_features || !pairwise_correct || deprel_id != deprels_ref[child_id]) {
								fv.offset += _grand_feat_dim * (deprel_id + 1);
								parameters::sparse_add(sp_fv, &fv, scale);
							}
						}

						fv.dealloc();
						if (_use_outermost_grand_features) break;
					}
				}
			}
		}

		if (_use_grandsibling) {
			assert(!_labeled);
			assert(!conservatively_add_partwise_features); // not support this.
			for (int head_id = 1; head_id < length; ++head_id) { 
				const int grand_id = heads[head_id];
				for (int dir = 0; dir <= 1; ++dir) {
					const list<int> &children = dir ? children_l[head_id] : children_r[head_id];

					if (!children.empty()) { 
						{ // first grandchild
							const int first_child_id = children.front();
							feats_str.clear();
							addGrandSiblingFeature(inst, grand_id, head_id, first_child_id, head_id, feats_str);
							_grandsibling_feat_dict.map_all(&fv, _grandsibling_feat_offset, feats_str, _generation_mode);
							if(_use_unlabeled_syn_features) parameters::sparse_add(sp_fv, &fv, scale);
							fv.dealloc();
						}

						{ // last grandchild
							const int last_child_id = children.back();
							feats_str.clear();
							addGrandSiblingFeature(inst, grand_id, head_id, last_child_id, last_child_id, feats_str);
							_grandsibling_feat_dict.map_all(&fv, _grandsibling_feat_offset, feats_str, _generation_mode);
							if(_use_unlabeled_syn_features) parameters::sparse_add(sp_fv, &fv, scale);
							fv.dealloc();
						}

						{
							list<int>::const_iterator i = children.begin();
							while (true) {
								const int sibling_id = *i++;
								if (i == children.end()) break;
								const int child_id = *i;
								feats_str.clear();
								addGrandSiblingFeature(inst, grand_id, head_id, child_id, sibling_id, feats_str);
								_grandsibling_feat_dict.map_all(&fv, _grandsibling_feat_offset, feats_str, _generation_mode);
								if(_use_unlabeled_syn_features) parameters::sparse_add(sp_fv, &fv, scale);
								fv.dealloc();
							}
						}
					}
				}
			}
		}

		if (!pos_idx.empty()) {
			inst->cpostags = bak_cpostags;
		}
	}

	void FGen::collect_word_postag_label( Instance * const inst, const bool collect_word/*=false*/ )
	{
		assert(_generation_mode);
		const int length = inst->size();
		if (_labeled) {
			assign_deprels_int(inst);
			if (_use_filtered_labels) {
				for (int i = 1; i < length; ++i) {
					for (int h = 0; h < length; ++h) {
						if (h == i) continue;
						const vector<string> &cand_labels = inst->candidate_labels[i][h];
						for (int k = 0; k < cand_labels.size(); ++k) {
							_label_dict.getFeature(cand_labels[k], _generation_mode);
						}
					}
				}
			}
		}

		for (int i = 1; i < length; ++i) {
			if (collect_word) _word_dict.getFeature(inst->forms[i], true);
			
			_postag_dict.getFeature(inst->cpostags[i], true);
			for (int pi = 0; pi < inst->p_postags_num[i]; ++pi) {
				_postag_dict.getFeature(inst->p_postags[i][pi], true);
			}
		}
	}

	void FGen::assign_deprels_int( Instance * const inst )
	{
		assert(_labeled);
		//if (inst->deprels_int.size() > 0) return;

		const int length = inst->size();
		inst->deprels_int.resize(length);
		for (int i = 1; i < length; ++i) {
			const int label_id = _label_dict.getFeature(inst->deprels[i], _generation_mode);
			if (label_id < 0) {
				//cerr << "unknown label: " << inst->deprels[i] << endl;
				//exit(-1);
			}
			inst->deprels_int[i] = label_id >= 0 ? label_id : 0;
		}
	}

	void FGen::convert_predicted_deprels_int( Instance * const inst )
	{
		assert(_labeled);
		if (!_use_filtered_labels) return;
		const int length = inst->size();
		for (int i = 1; i < length; ++i) {
			const int predict_head_id = inst->predicted_heads[i];
			const int predict_deprel_idx = inst->predicted_deprels_int[i];
			const string &predict_deprel = inst->candidate_labels[i][predict_head_id][predict_deprel_idx];
			const int label_id = get_label_id(predict_deprel);
			inst->predicted_deprels_int[i] = label_id;
		}
	}

	void FGen::assign_predicted_deprels_str( Instance * const inst )
	{
		assert(_labeled);
		const int length = inst->size();
		assert(length == inst->predicted_deprels_int.size());
		inst->predicted_deprels.resize(length);
		for (int i = 1; i < length; ++i) {
			const int label_id = inst->predicted_deprels_int[i];
			assert(label_id >= 0 && label_id < _labels.size());
			inst->predicted_deprels[i] = _labels[label_id];
		}
	}

	// consider deleting those

	void FGen::addDependencyFeature_unigram(const Instance *inst, const int node_id, const bool child, const bool right_arc, list<string> &feats_str, const bool isUseMultiPOS/*=false*/, const int pos_i/*=-1*/ ) const
	{
		// same as McDonald-phd-thesis-06
		string str_child = child ? "-C" : "-H";
		string dir = (right_arc ? "-R" : "-L");

		const string &form = inst->forms[node_id];
		const string &cpostag = isUseMultiPOS ? inst->p_postags[node_id][pos_i] : inst->cpostags[node_id];
		const string prefix = "U-";

		string feat;
		feat = prefix + "0=" + form + FEAT_SEP + cpostag + str_child;
		feats_str.push_back(feat); 
		feats_str.push_back(feat + dir);

		feat = prefix + "1=" + form + str_child;
		feats_str.push_back(feat); 
		feats_str.push_back(feat + dir);

		feat = prefix + "2=" + cpostag + str_child;
		feats_str.push_back(feat); 
		feats_str.push_back(feat + dir);
	}

	void FGen::addDependencyFeature_bigram( const Instance *inst, const int head_id, const int child_id, list<string> &feats_str, const bool isUseMultiPOS/*=false*/, const int head_pos_idx/*=-1*/, const int child_pos_idx/*=-1*/  ) const
	{
		ARC_INFO arc_info;
		get_arc_info(inst, head_id, child_id, arc_info);
		const string &dir_dist = arc_info.dir_dist;

		const vector<string> &forms = inst->forms;
		const string &cpos_h = isUseMultiPOS ? inst->p_postags[head_id][head_pos_idx] : inst->cpostags[head_id];
		const string &cpos_c = isUseMultiPOS ? inst->p_postags[child_id][child_pos_idx] : inst->cpostags[child_id];

		string feat;
		const string prefix = "BB-";

		if (arc_info.is_root)
		{
			feat = prefix + "01=" + forms[head_id] + FEAT_SEP + forms[child_id] + FEAT_SEP + cpos_c; // word-0 word-j pos-j
			feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);

			feat = prefix + "02=" + forms[head_id]+ FEAT_SEP + forms[child_id]; // word-0 word-j
			feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);

			feat = prefix + "03=" + forms[head_id]+ FEAT_SEP + cpos_c; // word-0 pos-j
			feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);
		} // end if (is_root)
		else 
		{
			feat = prefix + "0=" + forms[head_id] + FEAT_SEP + cpos_h + FEAT_SEP + forms[child_id] + FEAT_SEP + cpos_c; // word-i pos-i word-j pos-j
			feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);

			feat = prefix + "1=" + forms[head_id] + FEAT_SEP + forms[child_id] + FEAT_SEP + cpos_c; // word-i word-j pos-j
			feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);

			feat = prefix + "2=" + cpos_h + FEAT_SEP + forms[child_id] + FEAT_SEP + cpos_c; // pos-i word-j pos-j
			feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);

			feat = prefix + "3=" + forms[head_id] + FEAT_SEP + cpos_h + FEAT_SEP + forms[child_id]; // word-i pos-i word-j
			feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);

			feat = prefix + "4=" + forms[head_id] + FEAT_SEP + cpos_h + FEAT_SEP + cpos_c; // word-i pos-i pos-j
			feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);

			// not in mcdonald-phd-thesis06
			feat = prefix + "5=" + forms[head_id] + FEAT_SEP + cpos_c; // word-i pos-j
			feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);

			feat = prefix + "6=" + cpos_h + FEAT_SEP + forms[child_id]; // pos-i word-j
			feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);

			feat = prefix + "7=" + forms[head_id] + FEAT_SEP + forms[child_id]; // word-i word-j
			feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);

			feat = prefix + "8=" + cpos_h + FEAT_SEP + cpos_c; // pos-i pos-j
			feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);
		} // end else if (is_root)
	}


	/* 
	It is kind of complex here.
	NOTE, *always* use postags_for_bet_feat for in between POS tags.

	There are two cases when isUseMultiPOS==false:
	1. DP
	2. creating reference (gold) features (joint case)

	--------
	2011.11.03 16:30 by Li Zhenghua
	Add option: use-bet-feat-wrong-way [as in gdp-r168], 
	use pos-bet[small] instead of p_postags[small][small_pos_idx]
	Previous experiments on dev seem that this leads to higher parsing accuracy. [not seriously verified. why?]

	*/
	void FGen::addDependencyFeature_between( const Instance *inst, const int head_id, const int child_id, list<string> &feats_str/*, const bool isUseMultiPOS/*=false/, const int head_pos_idx/*=-1/, const int child_pos_idx/*=-1*/  ) const
	{
		ARC_INFO arc_info;
		get_arc_info(inst, head_id, child_id, arc_info);
		const string &dir_dist = arc_info.dir_dist;

		const vector<string> &pos_bet = inst->postags_for_bet_feat;
		const vector<int> &verb_cnt = inst->verb_cnt;
		const vector<int> &conj_cnt = inst->conj_cnt;
		const vector<int> &punc_cnt = inst->punc_cnt;

		StringMap<int> pos_seen;

		string feat;
		const string prefix = "BBE-";

		if (arc_info.is_root)
		{
			const string &pos_root = inst->forms[0];
			const string &p_large = pos_bet[child_id];

			// add left poss
			if (1 >= child_id) { // beginning
				feat = prefix + "00L="+ pos_root + "##" + p_large;
				feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);
			} else {
				pos_seen.clear();
				int i = 1;
				for (; i < child_id; ++i) {
					// pos-i pos-b pos-j
					if ( pos_seen.get(pos_bet[i].c_str()) == NULL ) {
						feat = prefix + "0L=" + pos_root + FEAT_SEP + pos_bet[i] + FEAT_SEP + p_large;
						feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);
						pos_seen.set(pos_bet[i].c_str(), 1);
					}
				}

				// left-part: verb, punc, conj +cnt
				ostringstream out;
				out.str(""); out << verb_cnt[child_id-1] - verb_cnt[0];
				feat = prefix + "0L=" + pos_root + FEAT_SEP + "verb=" + out.str() + FEAT_SEP + p_large;
				feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);

				out.str(""); out << conj_cnt[child_id-1] - conj_cnt[0];
				feat = prefix + "0L=" + pos_root + FEAT_SEP + "conj=" + out.str() + FEAT_SEP + p_large;
				feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);

				out.str(""); out << punc_cnt[child_id-1] - punc_cnt[0];
				feat = prefix + "0L=" + pos_root + FEAT_SEP + "punc=" + out.str() + FEAT_SEP + p_large;
				feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);
			}
			// add right poss
			if (inst->size()-1 == child_id) { // ending
				//if (use grandparental parts) {
				//	cerr << "FeatureExtracter::addFeature_bi_bet_sur: may be wrong to judge the ending in this way. [n+1] is used?" << endl;
				//	exit(0);
				//}
				feat = prefix + "00R="+ pos_root + "##" + p_large;
				feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);
			} else {
				pos_seen.clear();
				int i = child_id + 1;
				for (; i < inst->size(); ++i) {
					// pos-i pos-b pos-j
					if ( pos_seen.get(pos_bet[i].c_str()) == NULL ) {
						string feat = prefix + "0R=" + pos_root + FEAT_SEP + pos_bet[i] + FEAT_SEP + p_large;
						feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);
						pos_seen.set(pos_bet[i].c_str(), 1);
					}
				}

				// right-part: verb, punc, conj +cnt
				ostringstream out;
				const int N = inst->size() - 1;
				out.str(""); out << verb_cnt[N] - verb_cnt[child_id];
				feat = prefix + "0R=" + pos_root + FEAT_SEP + "verb=" + out.str() + FEAT_SEP + p_large;
				feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);

				out.str(""); out << conj_cnt[N] - conj_cnt[child_id];
				feat = prefix + "0R=" + pos_root + FEAT_SEP + "conj=" + out.str() + FEAT_SEP + p_large;
				feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);

				out.str(""); out << punc_cnt[N] - punc_cnt[child_id];
				feat = prefix + "0R=" + pos_root + FEAT_SEP + "punc=" + out.str() + FEAT_SEP + p_large;
				feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);
			}
		} // end if (is_root)
		else 
		{
			const int small = (head_id < child_id ? head_id : child_id);
			const int large = (head_id > child_id ? head_id : child_id);

			const string &p_small = pos_bet[small];
			const string &p_large = pos_bet[large];

			if (1 == (large-small)) {
				feat = prefix + "00="+ p_small + "##" + p_large;
				feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);
			} else {
				pos_seen.clear();
				int i = small+1;
				for (; i < large; ++i) {
					// pos-i pos-b pos-j
					if ( pos_seen.get(pos_bet[i].c_str()) == NULL ) {
						string feat = prefix + "0=" + p_small + FEAT_SEP + pos_bet[i] + FEAT_SEP + p_large;
						feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);
						pos_seen.set(pos_bet[i].c_str(), 1);
					}
				}

				// verb, punc, conj +cnt
				ostringstream out;
				out.str(""); out << verb_cnt[large-1] - verb_cnt[small];
				feat = prefix + "0=" + p_small + FEAT_SEP + "verb=" + out.str() + FEAT_SEP + p_large;
				feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);

				out.str(""); out << conj_cnt[large-1] - conj_cnt[small];
				feat = prefix + "0=" + p_small + FEAT_SEP + "conj=" + out.str() + FEAT_SEP + p_large;
				feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);

				out.str(""); out << punc_cnt[large-1] - punc_cnt[small];
				feat = prefix + "0=" + p_small + FEAT_SEP + "punc=" + out.str() + FEAT_SEP + p_large;
				feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);
			}
		} // end else if (is_root)
	}

	void FGen::addDependencyFeature_surrounding( const Instance *inst, const int head_id, const int child_id, list<string> &feats_str, const bool isUseMultiPOS/*=false*/, const int head_pos_idx/*=-1*/, const int child_pos_idx/*=-1*/  ) const
	{
		ARC_INFO arc_info;
		get_arc_info(inst, head_id, child_id, arc_info);
		const string &dir_dist = arc_info.dir_dist;

		const vector<string> &forms = inst->forms;

		const string prefix = "BS-";

		string feat;
		if (arc_info.is_root)
		{
			const string &pi = forms[head_id];
			const string &pj = isUseMultiPOS ? inst->p_postags[child_id][child_pos_idx] : inst->cpostags[child_id];

			const string &pj_L1 = child_id <= 1 ? NO_CPOSTAG : inst->p_postags[child_id-1][0];
			const string &pj_R1 = child_id == inst->size()-1 ? NO_CPOSTAG : inst->p_postags[child_id+1][0];

			feat = prefix + "0=" + pi + FEAT_SEP + pj_L1 + FEAT_SEP + pj + FEAT_SEP + pj_R1; // pos-0 pos-j-1 pos-j pos-j+1
			feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);

			feat = prefix + "1=" + pi + FEAT_SEP + pj_L1 + FEAT_SEP + pj; // pos-0 pos-j-1 pos-j
			feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);

			feat = prefix + "2=" + pi + FEAT_SEP + pj + FEAT_SEP + pj_R1; // pos-0 pos-j pos-j+1
			feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);
		} // end if (is_root)
		else 
		{
			const int small = (arc_info.is_right_arc ? head_id : child_id);
			const int large = (arc_info.is_right_arc ? child_id : head_id);
			const int pos_i_small = (arc_info.is_right_arc ? head_pos_idx : child_pos_idx);
			const int pos_i_large = (arc_info.is_right_arc ? child_pos_idx : head_pos_idx);

			const string &pi = isUseMultiPOS ? inst->p_postags[small][pos_i_small] : inst->cpostags[small];
			const string &pj = isUseMultiPOS ? inst->p_postags[large][pos_i_large] : inst->cpostags[large];
			const string &pi_L1 = (small <= 1) ? NO_CPOSTAG : inst->p_postags[small-1][0]; // pos-i-1
			const string &pj_R1 = (large == inst->size()-1) ? NO_CPOSTAG : inst->p_postags[large+1][0]; // pos-j+1
			string pi_R1 = inst->p_postags[small+1][0]; // pos-i+1
			string pj_L1 = inst->p_postags[large-1][0]; // pos-j-1


			if (1 == (large - small)) { // adjacent
				pi_R1 = BET_NO_POS;
				pj_L1 = pi_R1;
			} else if (2 == (large - small)) {
				pi_R1 += BET_ONE_POS;
				pj_L1 = pi_R1;
			}

			feat = prefix + "0=" + pi + FEAT_SEP + pi_R1 + FEAT_SEP + pj_L1 + FEAT_SEP + pj; // pos-i pos-i+1 pos-j-1 pos-j
			feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);

			feat = prefix + "1=" + pi_L1 + FEAT_SEP + pi + FEAT_SEP + pj_L1 + FEAT_SEP + pj; // pos-i-1 pos-i pos-j-1 pos-j
			feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);

			feat = prefix + "2=" + pi + FEAT_SEP + pi_R1 + FEAT_SEP + pj + FEAT_SEP + pj_R1; // pos-i pos-i+1 pos-j pos-j+1
			feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);

			feat = prefix + "3=" + pi_L1 + FEAT_SEP + pi + FEAT_SEP + pj + FEAT_SEP + pj_R1; // pos-i-1 pos-i pos-j pos-j+1
			feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);

			// not in mcdonald-phd-thesis06
			feat = prefix + "4=" + pi + FEAT_SEP + pj_L1 + FEAT_SEP + pj + FEAT_SEP + pj_R1; // pos-i pos-j-1 pos-j pos-j+1
			feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);

			feat = prefix + "5=" + pi_L1 + FEAT_SEP + pi + FEAT_SEP + pi_R1 + FEAT_SEP + pj; // pos-i-1 pos-i pos-i+1 pos-j
			feats_str.push_back(feat); feats_str.push_back(feat + dir_dist);

		} // end else if (is_root)
	}


	void FGen::get_arc_info( const Instance *inst, const int head_id, const int child_id, ARC_INFO &arc_info )
	{
		arc_info.is_root = (0 == head_id);
		arc_info.is_right_arc = (head_id < child_id ? true : false);
		arc_info.dir = arc_info.is_right_arc ? "-R" : "-L";
		if (arc_info.is_root) arc_info.dir = "-L#R";
		getDistance_1_2_36_7(head_id, child_id, arc_info.dist);	
		if (arc_info.is_root) {	// use the length of both sides
			string dist_child_to_end;
			getDistance_1_2_36_7(child_id, inst->size(), dist_child_to_end);
			arc_info.dist += "#" + dist_child_to_end;
		}
		arc_info.dir_dist = arc_info.dir + FEAT_SEP + arc_info.dist;
	}




} // namespace gparser_space













