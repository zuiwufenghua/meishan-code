#include "Decoder_1o.h"

namespace dparser {

	void Decoder_1o::init_chart(const Instance *inst)
	{
		const int length = inst->size();
	
		for(int i = 0; i < length; i++) {
			const int pos_i_num = inst->p_postags_num[i];
			_chart_cmp[i][i].resize(pos_i_num, pos_i_num);
			_chart_cmp[i][i] = NULL;
			for (int pi = 0; pi < pos_i_num; ++pi) {
				_chart_cmp[i][i][pi][pi] = new ChartItem(i, pi);
			}
		}
	}

	void Decoder_1o::decode_projective(const Instance *inst)
	{
		const bool use_unlabeled_dependency_features = (_use_unlabeled_syn_features && inst->fvec_dep.nrows() > 0);
		const bool use_labeled_dependency_features = (_labeled && inst->fvec_depl.nrows() > 0);
		const bool use_pos_unigram_features = inst->fvec_pos1.size() > 0;
		const bool use_pos_bigram_features = inst->fvec_pos2.size() > 0;

		const int length = inst->size();
		const NRMat<bool> &candidate_heads = inst->candidate_heads;
		for(int width = 1; width < length; width++) 
		{
			for(int s = 0; s+width < length; s++)
			{
				const int t = s + width;
				const int pos_s_num = inst->p_postags_num[s];
				const int pos_t_num = inst->p_postags_num[t];
				
				_chart_cmp[s][t].resize(pos_s_num, pos_t_num); _chart_cmp[s][t] = NULL;
				_chart_cmp[t][s].resize(pos_t_num, pos_s_num); _chart_cmp[t][s] = NULL;
				for (int l = 0; l < _L; ++l) {
					_chart_incmp[s][t][l].resize(pos_s_num, pos_t_num); _chart_incmp[s][t][l] = NULL;
					_chart_incmp[t][s][l].resize(pos_t_num, pos_s_num); _chart_incmp[t][s][l] = NULL;
				}

				for(int r = s; r < t; r++) { // C(s,r) + C(t,r+1)
					//if (_chart_cmp[s][r].nrows() == 0 || _chart_cmp[t][r+1].nrows() == 0) continue;
					const int pos_r_num = inst->p_postags_num[r];
					const int pos_r1_num = inst->p_postags_num[r+1];
					assert(pos_r_num == _chart_cmp[s][r].ncols() && pos_r1_num == _chart_cmp[t][r+1].ncols());

					for (int ps = 0; ps < pos_s_num; ++ps) {
						for (int pt = 0; pt < pos_t_num; ++pt) {

							for (int pr = 0; pr < pos_r_num; ++pr) {
								const ChartItem * const left = _chart_cmp[s][r][ps][pr];
								if (!left) continue;

								for (int pr1 = 0; pr1 < pos_r1_num; ++pr1) {
									const ChartItem * const right = _chart_cmp[t][r+1][pt][pr1];
									if (!right) continue;

									for (int l = 0; l < _L; ++l) {
										if (inst->candidate_heads[t][s]) { // I(s,t)
											list<const fvec *> fvs;
											double prob = left->_prob + right->_prob;
											if (use_unlabeled_dependency_features) {  // dependency: (s, t)
												fvs.push_back(&inst->fvec_dep[s][t][ps][pt]);
												prob += inst->prob_dep[s][t][ps][pt];
											}
											if (use_labeled_dependency_features) { // labeled dependency: (s, t, l)
												fvs.push_back(&inst->fvec_depl[s][t][l][ps][pt]);
												prob += inst->prob_depl[s][t][l][ps][pt];
											}
											if (use_pos_unigram_features) { // pos unigram: <t>
												fvs.push_back(&inst->fvec_pos1[t][pt]);
												prob += inst->prob_pos1[t][pt];
											}
											if (use_pos_bigram_features) { // pos bigram: <r, r+1>
												fvs.push_back(&inst->fvec_pos2[r+1][pr][pr1]);
												prob += inst->prob_pos2[r+1][pr][pr1];
											}
											// add the new item
											const ChartItem * const item = new ChartItem(INCMP, s, t, ps, pt, prob, fvs, left, right, l);
											add_item(_chart_incmp[s][t][l][ps][pt], item);
										}
										if (s != 0 && inst->candidate_heads[s][t]) { // I(t,s)
											list<const fvec *> fvs;
											double prob = left->_prob + right->_prob;
											if (use_unlabeled_dependency_features) {  // dependency: (t, s)
												fvs.push_back(&inst->fvec_dep[t][s][pt][ps]);
												prob += inst->prob_dep[t][s][pt][ps];
											}
											if (use_labeled_dependency_features) { // labeled dependency: (t, s, l)
												fvs.push_back(&inst->fvec_depl[t][s][l][pt][ps]);
												prob += inst->prob_depl[t][s][l][pt][ps];
											}
											if (use_pos_unigram_features) { // pos unigram: <s>
												fvs.push_back(&inst->fvec_pos1[s][ps]);
												prob += inst->prob_pos1[s][ps];
											}
											if (use_pos_bigram_features) { // pos bigram: <r, r+1>
												fvs.push_back(&inst->fvec_pos2[r+1][pr][pr1]);
												prob += inst->prob_pos2[r+1][pr][pr1];
											}
											// add the new item
											const ChartItem * const item = new ChartItem(INCMP, t, s, pt, ps, prob, fvs, left, right, l);
											add_item(_chart_incmp[t][s][l][pt][ps], item);
										}
									}
								}
							}
						}
					}
				}

				for(int r = s; r <= t; r++) {
					const int pos_r_num = inst->p_postags_num[r];

					for (int ps = 0; ps < pos_s_num; ++ps) {
						for (int pt = 0; pt < pos_t_num; ++pt) {
							for (int pr = 0; pr < pos_r_num; ++pr) {
								if (r != s) { // C(s,t) = I(s,r) + C(r,t)
									//if (s == 0 && t != length-1) continue; // multi-root NOT allowed

									//if (_chart_cmp[r][t].nrows() == 0) continue;
									const ChartItem * const right = _chart_cmp[r][t][pr][pt];
									if (!right) continue;
									for (int l = 0; l < _L; ++l) {
										//if (_chart_incmp[s][r][l].nrows() == 0) continue;
										const ChartItem * const left = _chart_incmp[s][r][l][ps][pr];
										if (!left) continue;

										list<const fvec *> fvs;
										const double prob = left->_prob + right->_prob;
										// add the new item
										const ChartItem * const item = new ChartItem(CMP, s, t, ps, pt, prob, fvs, left, right);
										add_item(_chart_cmp[s][t][ps][pt], item);
									}
								}

								if (r != t && s != 0) { // C(t,s) = C(r,s) + I(t,r)
									//if (_chart_cmp[r][s].nrows() == 0) continue;
									const ChartItem * const left = _chart_cmp[r][s][pr][ps];
									if (!left) continue;

									for (int l = 0; l < _L; ++l) {
										//if (_chart_incmp[t][r][l].nrows() == 0) continue;
										const ChartItem * const right = _chart_incmp[t][r][l][pt][pr];
										if (!right) continue;

										list<const fvec *> fvs;
										double prob = left->_prob + right->_prob;
										// add the new item
										const ChartItem * const item = new ChartItem(CMP, t, s, pt, ps, prob, fvs, left, right);
										add_item(_chart_cmp[t][s][pt][ps], item);
									}
								}
							}
						}
					}
				}	
			}
		}
	}

	void Decoder_1o::get_best_parse( Instance *inst ) const
	{
		const int length = inst->size();
		inst->predicted_heads.clear();
		inst->predicted_heads.resize(length, -1);
		inst->predicted_postags_idx.clear();
		inst->predicted_postags_idx.resize(length, -1);
		inst->predicted_fv.clear();
		inst->predicted_prob = 0;

		if (_labeled) {
			inst->predicted_deprels_int.clear();
			inst->predicted_deprels_int.resize(length, -1);
		}

		const ChartItem * best_item = _chart_cmp[0][length-1][0][0];
		for (int pn = 1; pn < inst->p_postags_num[length-1]; ++pn) {
			const ChartItem * const item = _chart_cmp[0][length-1][0][pn];
			if (best_item->_prob < item->_prob - EPS) { // absolutely less than the new item
				best_item = item;
			}
		}

		inst->predicted_prob = best_item->_prob;
		get_best_parse_recursively(inst, best_item);
	}

	void Decoder_1o::get_best_parse_recursively( Instance *inst, const ChartItem * const item ) const
	{
		if (!item) return;
		get_best_parse_recursively(inst, item->_left);

		if (INCMP == item->_comp) {
			// set heads and deprels
			assert(0 > inst->predicted_heads[item->_t]);
			inst->predicted_heads[item->_t] = item->_s;
			if (_labeled) {
				assert(0 > inst->predicted_deprels_int[item->_t]);
				inst->predicted_deprels_int[item->_t] = item->_label_s_t;
			}
		} else if (CMP == item->_comp) {
			// do nothing
		} else {
			cerr << "unknown item->_comp: " << item->_comp << endl;
			exit(0);
		}

		// collect features
		for (list<const fvec *>::const_iterator it = item->_fvs.begin(); it != item->_fvs.end(); ++it) {
			parameters::sparse_add(inst->predicted_fv, *it);
		}
		// set pos-idx
		assert (0 > inst->predicted_postags_idx[item->_s] || item->_s_pos_i == inst->predicted_postags_idx[item->_s]);
		assert (0 > inst->predicted_postags_idx[item->_t] || item->_t_pos_i == inst->predicted_postags_idx[item->_t]);
		inst->predicted_postags_idx[item->_s] = item->_s_pos_i;
		inst->predicted_postags_idx[item->_t] = item->_t_pos_i;

		get_best_parse_recursively(inst, item->_right);
	}


} // namespace dparser

