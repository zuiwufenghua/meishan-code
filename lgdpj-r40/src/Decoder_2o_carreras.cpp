#include "Decoder_2o_carreras.h" 

namespace dparser {

	void Decoder_2o_carreras::init_chart(const Instance *inst)
	{
		const int length = inst->size();
	
		for(int i = 0; i < length; i++) {
			const int pos_i_num = inst->p_postags_num[i];
			_chart_cmp[i][i][i].resize(pos_i_num, pos_i_num, pos_i_num);
			_chart_cmp[i][i][i] = NULL;
			for (int pi = 0; pi < pos_i_num; ++pi) {
				_chart_cmp[i][i][i][pi][pi][pi] = new ChartItem(i, pi);
			}
		}
	}

	void Decoder_2o_carreras::decode_projective(const Instance *inst)
	{
		const bool use_unlabeled_dependency_features = (_use_unlabeled_syn_features && inst->fvec_dep.nrows() > 0);
		const bool use_labeled_dependency_features = (_labeled && inst->fvec_depl.nrows() > 0);
		const bool use_pos_unigram_features = inst->fvec_pos1.size() > 0;
		const bool use_pos_bigram_features = inst->fvec_pos2.size() > 0;

		const bool use_unlabeled_sibling_features = (_use_unlabeled_syn_features && inst->fvec_sib.dim1() > 0);
		const bool use_labeled_sibling_features = (_labeled && inst->fvec_sibl.dim1() > 0);
		const bool use_unlabeled_grand_features = (_use_unlabeled_syn_features && inst->fvec_grd.dim1() > 0);
		const bool use_labeled_grand_features = (_labeled && inst->fvec_grdl.dim1() > 0);

		const int length = inst->size();
		const NRMat<bool> &candidate_heads = inst->candidate_heads;

		for(int width = 1; width < length; width++) {
			for(int s = 0; s+width < length; s++) {
				const int t = s + width;
				const int pos_s_num = inst->p_postags_num[s];
				const int pos_t_num = inst->p_postags_num[t];
				if (inst->candidate_heads[t][s]) {
					const int label_num = _use_filtered_labels ? inst->candidate_labels[t][s].size() : _L;
					assert(label_num > 0);
					_chart_incmp[s][t].resize(label_num, pos_s_num, pos_t_num); _chart_incmp[s][t] = NULL;
				}
				if (s != 0 && inst->candidate_heads[s][t]) {
					const int label_num = _use_filtered_labels ? inst->candidate_labels[s][t].size() : _L;
					assert(label_num > 0);
					_chart_incmp[t][s].resize(label_num, pos_t_num, pos_s_num); _chart_incmp[t][s] = NULL;
				}

				for (int m = s; m <= t; ++m) {
					const int pos_m_num = inst->p_postags_num[m];
					if (m != s)
						_chart_cmp[s][t][m].resize(pos_s_num, pos_t_num, pos_m_num); _chart_cmp[s][t][m] = NULL;
					if (m != t && s != 0)
						_chart_cmp[t][s][m].resize(pos_t_num, pos_s_num, pos_m_num); _chart_cmp[t][s][m] = NULL;
				}

				for (int ps = 0; ps < pos_s_num; ++ps) {
					for (int pt = 0; pt < pos_t_num; ++pt) {
						if (inst->candidate_heads[t][s]) { // I(s, t) = C(s,r) + C(t,r+1)
							for (int l = 0; l < _chart_incmp[s][t].dim1(); ++l) {
								for(int r = s; r < t; r++) { 
									const int pos_r_num = inst->p_postags_num[r];
									const int pos_r1_num = inst->p_postags_num[r+1];

									for (int pr = 0; pr < pos_r_num; ++pr) {
										if (r == s && pr != ps) continue;
										for (int pr1 = 0; pr1 < pos_r1_num; ++pr1) {
											if (r+1 == t && pr1 != pt) continue;

											const ChartItem * best_left_item_st = 0;
											double best_left_prob_st = DOUBLE_NEGATIVE_INFINITY;
											list<const fvec *> best_left_fvs_st;

											for (int cs = s; cs <= r; ++cs) {
												if (cs == s && s != r) continue;
												const int pos_cs_num = inst->p_postags_num[cs];
												for (int pcs = 0; pcs < pos_cs_num; ++pcs) {
													if (cs == s && pcs != ps
														||
														cs == r && pcs != pr) continue;

													const ChartItem *item = _chart_cmp[s][r][cs][ps][pr][pcs];
													//assert(item);
													if (!item) continue;
													double prob_st = item->_prob;
													list<const fvec *> fvs_st;
													if (use_unlabeled_sibling_features) {
														fvs_st.push_back(&inst->fvec_sib[s][t][cs][ps][pt][pcs]); // when cs == s, [s->t] first child 
														prob_st += inst->prob_sib[s][t][cs][ps][pt][pcs];
													}
													if (use_labeled_sibling_features) {
														fvs_st.push_back(&inst->fvec_sibl[s][t][cs][l][ps][pt][pcs]);
														prob_st += inst->prob_sibl[s][t][cs][l][ps][pt][pcs];
													}
													if (prob_st > best_left_prob_st + EPS) {
														best_left_item_st = item;
														best_left_prob_st = prob_st;
														best_left_fvs_st = fvs_st;
													}
												}
											}

											const ChartItem * best_right_item_st = 0;
											double best_right_prob_st = DOUBLE_NEGATIVE_INFINITY;
											list<const fvec *> best_right_fvs_st;

											for (int ct = r + 1; ct <= t; ++ct) {
												if (ct == t && r+1 != t) continue;
												const int pos_ct_num = inst->p_postags_num[ct];
												for (int pct = 0; pct < pos_ct_num; ++pct) {
													if (ct == t && pct != pt
														||
														ct == r+1 && pct != pr1) continue;

													const ChartItem *item = _chart_cmp[t][r+1][ct][pt][pr1][pct];
													//assert(item);
													if (!item) continue;
													double prob_st = item->_prob;
													list<const fvec *> fvs_st;
													if ( use_unlabeled_grand_features && (_use_no_grand_features || ct != t) ) {
														fvs_st.push_back(&inst->fvec_grd[s][t][ct==t ? s : ct][ps][pt][ct==t ? ps : pct]); // when ct == t: [s->t] no left-side grandchild
														prob_st += inst->prob_grd[s][t][ct==t ? s : ct][ps][pt][ct==t ? ps : pct];
													}
													if ( use_labeled_grand_features && (_use_no_grand_features || ct != t) ) {
														fvs_st.push_back(&inst->fvec_grdl[s][t][ct==t ? s : ct][l][ps][pt][ct==t ? ps : pct]);
														prob_st += inst->prob_grdl[s][t][ct==t ? s : ct][l][ps][pt][ct==t ? ps : pct];
													}
													if (prob_st > best_right_prob_st + EPS) {
														best_right_item_st = item;
														best_right_prob_st = prob_st;
														best_right_fvs_st = fvs_st;
													}
												}
											}

											if (best_left_item_st && best_right_item_st) { // I(s,t)
												double prob = best_left_prob_st + best_right_prob_st;
												list<const fvec *> fvs = best_left_fvs_st;
												fvs.insert(fvs.end(), best_right_fvs_st.begin(), best_right_fvs_st.end());

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
												const ChartItem * const item = new ChartItem(INCMP, s, t, ps, pt, prob, fvs, best_left_item_st, best_right_item_st, l);
												add_item(_chart_incmp[s][t][l][ps][pt], item);
											}
										}
									}
								}
							}
						}
						
						if (s != 0 && inst->candidate_heads[s][t]) { // I(t,s) = C(s,r) + C(t,r+1)
							for (int l = 0; l < _chart_incmp[t][s].dim1(); ++l) {
								for(int r = s; r < t; r++) { 
									const int pos_r_num = inst->p_postags_num[r];
									const int pos_r1_num = inst->p_postags_num[r+1];

									for (int pr = 0; pr < pos_r_num; ++pr) {
										if (r == s && pr != ps) continue;
										for (int pr1 = 0; pr1 < pos_r1_num; ++pr1) {
											if (r+1 == t && pr1 != pt) continue;

											const ChartItem * best_left_item_ts = 0;
											double best_left_prob_ts = DOUBLE_NEGATIVE_INFINITY;
											list<const fvec *> best_left_fvs_ts;

											for (int cs = s; cs <= r; ++cs) {
												if (cs == s && s != r) continue;
												const int pos_cs_num = inst->p_postags_num[cs];
												for (int pcs = 0; pcs < pos_cs_num; ++pcs) {
													if (cs == s && pcs != ps
														||
														cs == r && pcs != pr) continue;

													const ChartItem *item = _chart_cmp[s][r][cs][ps][pr][pcs];
													//assert(item);
													if (!item) continue;
													double prob_ts = item->_prob;
													list<const fvec *> fvs_ts;
													if ( use_unlabeled_grand_features && (_use_no_grand_features || cs != s) ) {
														fvs_ts.push_back(&inst->fvec_grd[t][s][cs][pt][ps][pcs]); // when cs == s, [t->s] no right-side grandchild
														prob_ts += inst->prob_grd[t][s][cs][pt][ps][pcs];
													}
													if ( use_labeled_grand_features && (_use_no_grand_features || cs != s) ) {
														fvs_ts.push_back(&inst->fvec_grdl[t][s][cs][l][pt][ps][pcs]);
														prob_ts += inst->prob_grdl[t][s][cs][l][pt][ps][pcs];
													}
													if (prob_ts > best_left_prob_ts + EPS) {
														best_left_item_ts = item;
														best_left_prob_ts = prob_ts;
														best_left_fvs_ts = fvs_ts;
													}
												}
											}

											const ChartItem * best_right_item_ts = 0;
											double best_right_prob_ts = DOUBLE_NEGATIVE_INFINITY;
											list<const fvec *> best_right_fvs_ts;

											for (int ct = r + 1; ct <= t; ++ct) {
												if (ct == t && r+1 != t) continue;
												const int pos_ct_num = inst->p_postags_num[ct];
												for (int pct = 0; pct < pos_ct_num; ++pct) {
													if (ct == t && pct != pt
														||
														ct == r+1 && pct != pr1) continue;

													const ChartItem *item = _chart_cmp[t][r+1][ct][pt][pr1][pct];
													//assert(item);
													if (!item) continue;
													double prob_ts = item->_prob;
													list<const fvec *> fvs_ts;
													if (use_unlabeled_sibling_features) {
														fvs_ts.push_back(&inst->fvec_sib[t][s][ct][pt][ps][pct]); // when ct == t, [t->s] first child
														prob_ts += inst->prob_sib[t][s][ct][pt][ps][pct];
													}
													if (use_labeled_sibling_features) {
														fvs_ts.push_back(&inst->fvec_sibl[t][s][ct][l][pt][ps][pct]);
														prob_ts += inst->prob_sibl[t][s][ct][l][pt][ps][pct];
													}
													if (prob_ts > best_right_prob_ts + EPS) {
														best_right_item_ts = item;
														best_right_prob_ts = prob_ts;
														best_right_fvs_ts = fvs_ts;
													}
												}
											}

											if (best_left_item_ts && best_right_item_ts) { // I(t,s)											
												double prob = best_left_prob_ts + best_right_prob_ts;
												list<const fvec *> fvs = best_left_fvs_ts;
												fvs.insert(fvs.end(), best_right_fvs_ts.begin(), best_right_fvs_ts.end());

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
												const ChartItem * const item = new ChartItem(INCMP, t, s, pt, ps, prob, fvs, best_left_item_ts, best_right_item_ts, l);
												add_item(_chart_incmp[t][s][l][pt][ps], item);
											}
										}
									}
								}
							}
						}

						for (int m = s; m <= t; ++m) {
							const int pos_m_num = inst->p_postags_num[m];
							if (m != s) { // C(s,t,m) = I(s,m,l) + C(m,t,cm)
								for (int l = 0; l < _chart_incmp[s][m].dim1(); ++l) {
									for (int pm = 0; pm < pos_m_num; ++pm) {
										if (m == t && pm != pt) continue;
										const ChartItem * const left = _chart_incmp[s][m][l][ps][pm];
										//assert(left);
										if (!left) continue;

										for (int cm = m; cm <= t; ++cm) {
											if (cm == m && cm != t) continue;
											const int pos_cm_num = inst->p_postags_num[cm];
											for (int pcm = 0; pcm < pos_cm_num; ++pcm) {
												if (cm == m && pcm != pm
													||
													cm == t && pcm != pt) continue;

												const ChartItem * const right = _chart_cmp[m][t][cm][pm][pt][pcm];
												//assert(right);
												if (!right) continue;

												list<const fvec *> fvs;
												double prob = left->_prob + right->_prob;
												if ( use_unlabeled_grand_features && (_use_no_grand_features || cm != m) ) {
													fvs.push_back(&inst->fvec_grd[s][m][cm][ps][pm][pcm]); // when cm == m == t: [s->t] no right-side grandchild
													prob += inst->prob_grd[s][m][cm][ps][pm][pcm];
												}
												if ( use_labeled_grand_features && (_use_no_grand_features || cm != m) ) { 
													fvs.push_back(&inst->fvec_grdl[s][m][cm][l][ps][pm][pcm]);
													prob += inst->prob_grdl[s][m][cm][l][ps][pm][pcm];
												}
												// add the new item
												const ChartItem * const item = new ChartItem(CMP, s, t, ps, pt, prob, fvs, left, right);
												add_item(_chart_cmp[s][t][m][ps][pt][pm], item);
											}
										}
									}
								}
							}
							if (m != t && s != 0) { // C(t,s,m) = C(m,s,cm) + I(t,m,l)
								for (int l = 0; l < _chart_incmp[t][m].dim1(); ++l) {
									for (int pm = 0; pm < pos_m_num; ++pm) {
										if (m == s && pm != ps) continue;

										const ChartItem * const right = _chart_incmp[t][m][l][pt][pm];
										//assert(right);
										if (!right) continue;

										for (int cm = s; cm <= m; ++cm) {
											if (cm == m && cm != s) continue;

											const int pos_cm_num = inst->p_postags_num[cm];
											for (int pcm = 0; pcm < pos_cm_num; ++pcm) {
												if (cm == m && pcm != pm
													||
													cm == s && pcm != ps) continue;

												const ChartItem * const left = _chart_cmp[m][s][cm][pm][ps][pcm];
												//assert(left);
												if (!left) continue;

												list<const fvec *> fvs;
												double prob = left->_prob + right->_prob;
												if ( use_unlabeled_grand_features && (_use_no_grand_features || cm != m) ) {
													fvs.push_back(&inst->fvec_grd[t][m][cm==m ? t : cm][pt][pm][cm==m ? pt : pcm]); // cm == m == s: [t->s] no left-side grandchild
													prob += inst->prob_grd[t][m][cm==m ? t : cm][pt][pm][cm==m ? pt : pcm];
												}
												if ( use_labeled_grand_features && (_use_no_grand_features || cm != m) ) { 
													fvs.push_back(&inst->fvec_grdl[t][m][cm==m ? t : cm][l][pt][pm][cm==m ? pt : pcm]);
													prob += inst->prob_grdl[t][m][cm==m ? t : cm][l][pt][pm][cm==m ? pt : pcm];
												}
												// add the new item
												const ChartItem * const item = new ChartItem(CMP, t, s, pt, ps, prob, fvs, left, right);
												add_item(_chart_cmp[t][s][m][pt][ps][pm], item);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}


	void Decoder_2o_carreras::get_best_parse( Instance *inst ) const
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

		const ChartItem * best_item = 0;
		const int n = length-1;
		for (int pn = 0; pn < inst->p_postags_num[n]; ++pn) {
			for (int c0 = 1; c0 < length; ++c0) {
				//const ChartItem * const item1 = _chart_cmp[0][length-1][1][0][0][0];
				//const ChartItem * const item2 = _chart_cmp[0][length-1][2][0][0][0];
				//const ChartItem * const item3 = _chart_cmp[0][length-1][3][0][0][0];
				//const ChartItem * const item4 = _chart_cmp[0][length-1][4][0][0][0];
				//const ChartItem * const item5 = _chart_cmp[0][length-1][5][0][0][0];

				const int pos_c0_num = inst->p_postags_num[c0];
				for (int pc0 = 0; pc0 < pos_c0_num; ++pc0) {
					if (c0 == n && pc0 != pn) continue;
					const ChartItem * const item = _chart_cmp[0][length-1][c0][0][pn][pc0];
					if(!item) continue;
					if (!best_item || best_item->_prob < item->_prob - EPS) { // absolutely less than the new item
						best_item = item;
					}
				}
			}
		}

		inst->predicted_prob = best_item->_prob;
		get_best_parse_recursively(inst, best_item);
	}

	void Decoder_2o_carreras::get_best_parse_recursively( Instance *inst, const ChartItem * const item ) const
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

