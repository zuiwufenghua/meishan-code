#include "Decoder_3o_koo.h"

namespace dparser {

	void Decoder_3o_koo::init_chart(const Instance *inst)
	{
		const int length = inst->size();
	
		for(int g = 0; g < length+1; g++) {
			const int pos_g_num = (g == length ? 1 : inst->p_postags_num[g]);

			for (int s = 0; s < length; s++) {
				if (s == g) continue;
				if (g == length && s != 0) continue;
				// if (!inst->candidate_heads[s][g]) continue;

				const int pos_s_num = inst->p_postags_num[s];
				_chart_cmp[g][s][s].resize(pos_g_num, pos_s_num, pos_s_num);
				_chart_cmp[g][s][s] = NULL;

				for (int pg = 0; pg < pos_g_num; ++pg)
					for (int ps = 0; ps < pos_s_num; ++ps)
						_chart_cmp[g][s][s][pg][ps][ps] = new ChartItem(g, s, pg, ps);
			}
		}
	}


	void Decoder_3o_koo::decode_projective(const Instance *inst)
	{
		const bool use_dependency_features = inst->fvec_dep.nrows() > 0;
		const bool use_sibling_features = inst->fvec_sib.dim1() > 0;
		const bool use_grand_features = inst->fvec_grd.dim1() > 0;
		const bool use_grandsib_features = inst->fvec_grdsib.dim1() > 0;

		const bool use_pos_unigram_features = inst->fvec_pos1.size() > 0;
		const bool use_pos_bigram_features = inst->fvec_pos2.size() > 0;

		const int length = inst->size();
		const NRMat<bool> &candidate_heads = inst->candidate_heads;

		for(int width = 1; width < length; width++) {
			for(int s = 0; s+width < length; s++) {
				const int t = s + width;
				const int pos_s_num = inst->p_postags_num[s];
				const int pos_t_num = inst->p_postags_num[t];

				for (int g = 0; g < length+1; ++g) {
					if (g == s) g = t+1;
					if (g == length && s != 0) continue;
					const int pos_g_num = (g == length ? 1 : inst->p_postags_num[g]);

					_chart_incmp[g][s][t].resize(pos_g_num, pos_s_num, pos_t_num); _chart_incmp[g][s][t] = NULL;
					_chart_cmp[g][s][t].resize(pos_g_num, pos_s_num, pos_t_num); _chart_cmp[g][s][t] = NULL;

					if (g != length) {
						_chart_incmp[g][t][s].resize(pos_g_num, pos_t_num, pos_s_num); _chart_incmp[g][t][s] = NULL;
						_chart_cmp[g][t][s].resize(pos_g_num, pos_t_num, pos_s_num); _chart_cmp[g][t][s] = NULL;
						_chart_sibling[g][s][t].resize(pos_g_num, pos_s_num, pos_t_num); _chart_sibling[g][s][t] = NULL;
					}
					
					for (int pg = 0; pg < pos_g_num; ++pg) {
						for (int ps = 0; ps < pos_s_num; ++ps) {
							for (int pt = 0; pt < pos_t_num; ++pt) {

								// incomplete spans I[g](s -> t)
								if (candidate_heads[t][s] && (g == length || (s != 0 && candidate_heads[s][g])) ) {
									double shared_prob = 0;
									list<const fvec *> shared_fvs;
									if (use_dependency_features) {  // dependency: (s -> t)
										shared_fvs.push_back(&inst->fvec_dep[s][t][ps][pt]);
										shared_prob += inst->prob_dep[s][t][ps][pt];
									}
									if (g != length && use_grand_features) {
										shared_fvs.push_back(&inst->fvec_grd[g][s][t][pg][ps][pt]);
										shared_prob += inst->prob_grd[g][s][t][pg][ps][pt];
									}
									if (use_pos_unigram_features) { // pos unigram: <t>
										shared_fvs.push_back(&inst->fvec_pos1[t][pt]);
										shared_prob += inst->prob_pos1[t][pt];
									}

									{ // t is the first child of s: C[g](s->s) + C[s](s+1 <- t)
										const int pos_s1_num = inst->p_postags_num[s+1];
										for (int ps1 = 0; ps1 < pos_s1_num; ++ps1) {
											if (s+1 == t && ps1 != pt) continue;

											const ChartItem * const left = _chart_cmp[g][s][s][pg][ps][ps];
											const ChartItem * const right = _chart_cmp[s][t][s+1][ps][pt][ps1];
											if (!left || !right) continue;

											list<const fvec *> fvs = shared_fvs;
											double prob = left->_prob + right->_prob + shared_prob;
											if (use_sibling_features) {
												fvs.push_back(&inst->fvec_sib[s][t][s][ps][pt][ps]); // first child 
												prob += inst->prob_sib[s][t][s][ps][pt][ps];
											}
											if (use_grand_features && _use_no_grand_features && s+1 == t) { // [s->t] has no left-side grandchild
												fvs.push_back(&inst->fvec_grd[s][t][s][ps][pt][ps]); 
												prob += inst->prob_grd[s][t][s][ps][pt][ps];
											}
											if (use_grandsib_features && g != length) {
												fvs.push_back(&inst->fvec_grdsib[g][s][t][s][pg][ps][pt][ps]); // first grandchild 
												prob += inst->prob_grdsib[g][s][t][s][pg][ps][pt][ps];
											}
											if (use_pos_bigram_features) { // pos bigram: <s, s+1>
												fvs.push_back(&inst->fvec_pos2[s+1][ps][ps1]);
												prob += inst->prob_pos2[s+1][ps][ps1];
											}

											const ChartItem * const item = new ChartItem(INCMP, g, s, t, pg, ps, pt, prob, fvs, left, right);
											add_item(_chart_incmp[g][s][t][pg][ps][pt], item);
										}
									} 
									
									if (s != 0) { // NOT allow multi-root, I[g](s->r) + S[s](r, t)
										for (int r = s+1; r < t; ++r) {
											const int pos_r_num = inst->p_postags_num[r];
											for (int pr = 0; pr < pos_r_num; ++pr) {
												const ChartItem * const left = _chart_incmp[g][s][r][pg][ps][pr];
												const ChartItem * const right = _chart_sibling[s][r][t][ps][pr][pt];
												if (!left || !right) continue;

												list<const fvec *> fvs = shared_fvs;
												double prob = left->_prob + right->_prob + shared_prob;
												if (use_grandsib_features) {
													fvs.push_back(&inst->fvec_grdsib[g][s][t][r][pg][ps][pt][pr]); 
													prob += inst->prob_grdsib[g][s][t][r][pg][ps][pt][pr];
												}

												const ChartItem * const item = new ChartItem(INCMP, g, s, t, pg, ps, pt, prob, fvs, left, right);
												add_item(_chart_incmp[g][s][t][pg][ps][pt], item);
											}
										}
									}
								} // build incomplete spans I[g](s -> t)


								// incomplete spans I[g](s <- t)
								if (g != length && s != 0 && candidate_heads[s][t] && candidate_heads[t][g]) {

									double shared_prob = 0;
									list<const fvec *> shared_fvs;
									if (use_dependency_features) {  // dependency: (s <- t)
										shared_fvs.push_back(&inst->fvec_dep[t][s][pt][ps]);
										shared_prob += inst->prob_dep[t][s][pt][ps];
									}
									if (use_grand_features) {
										shared_fvs.push_back(&inst->fvec_grd[g][t][s][pg][pt][ps]);
										shared_prob += inst->prob_grd[g][t][s][pg][pt][ps];
									}
									if (use_pos_unigram_features) { // pos unigram: <s>
										shared_fvs.push_back(&inst->fvec_pos1[s][ps]);
										shared_prob += inst->prob_pos1[s][ps];
									}

									{ // s is the first child of t: C[t](s -> t-1) + C[g](t <- t)
										const int pos_1t_num = inst->p_postags_num[t-1];
										for (int p1t = 0; p1t < pos_1t_num; ++p1t) {
											if (t-1 == s && p1t != ps) continue;

											const ChartItem * const left = _chart_cmp[t][s][t-1][pt][ps][p1t];
											const ChartItem * const right = _chart_cmp[g][t][t][pg][pt][pt];
											if (!left || !right) continue;

											list<const fvec *> fvs = shared_fvs;
											double prob = left->_prob + right->_prob + shared_prob;
											if (use_sibling_features) {
												fvs.push_back(&inst->fvec_sib[t][s][t][pt][ps][pt]); // first child 
												prob += inst->prob_sib[t][s][t][pt][ps][pt];
											}
											if (use_grand_features && _use_no_grand_features && s == t-1) { // [s<-t] has no right-side grandchild
												fvs.push_back(&inst->fvec_grd[t][s][s][pt][ps][ps]); 
												prob += inst->prob_grd[t][s][s][pt][ps][ps];
											}
											if (use_grandsib_features) {
												fvs.push_back(&inst->fvec_grdsib[g][t][s][t][pg][pt][ps][pt]); // first grandchild 
												prob += inst->prob_grdsib[g][t][s][t][pg][pt][ps][pt];
											}
											if (use_pos_bigram_features) { // pos bigram: <t-1, t>
												fvs.push_back(&inst->fvec_pos2[t][p1t][pt]);
												prob += inst->prob_pos2[t][p1t][pt];
											}

											const ChartItem * const item = new ChartItem(INCMP, g, t, s, pg, pt, ps, prob, fvs, left, right);
											add_item(_chart_incmp[g][t][s][pg][pt][ps], item);
										}
									} 

									{ // S[t](s,r) + I[g](r <- t)
										for (int r = s+1; r < t; ++r) {
											const int pos_r_num = inst->p_postags_num[r];
											for (int pr = 0; pr < pos_r_num; ++pr) {
												const ChartItem * const left = _chart_sibling[t][s][r][pt][ps][pr];
												const ChartItem * const right = _chart_incmp[g][t][r][pg][pt][pr];
												if (!left || !right) continue;

												list<const fvec *> fvs = shared_fvs;
												double prob = left->_prob + right->_prob + shared_prob;
												if (use_grandsib_features) {
													fvs.push_back(&inst->fvec_grdsib[g][t][s][r][pg][pt][ps][pr]); 
													prob += inst->prob_grdsib[g][t][s][r][pg][pt][ps][pr];
												}
												const ChartItem * const item = new ChartItem(INCMP, g, t, s, pg, pt, ps, prob, fvs, left, right);
												add_item(_chart_incmp[g][t][s][pg][pt][ps], item);
											}
										}
									}
								} // build incomplete spans I[g](s <- t)


								// sibling spans S[g](s,t)
								if (g != length && s != 0 && g != 0 /*NOT allow multi-root*/ && candidate_heads[s][g] && candidate_heads[t][g]) { 
									if (g == 0) break; 

									double shared_prob = 0;
									list<const fvec *> shared_fvs;
									if (use_sibling_features) {
										const int c1 = g < s ? s : t;
										const int c2 = g < s ? t : s;
										const int pc1 = g < s ? ps : pt;
										const int pc2 = g < s ? pt : ps;
										shared_fvs.push_back(&inst->fvec_sib[g][c2][c1][pg][pc2][pc1]);
										shared_prob += inst->prob_sib[g][c2][c1][pg][pc2][pc1];
									}

									for(int r = s; r < t; r++) { // C[g](s -> r) + C[g](r+1 <- t)
										const int pos_r_num = inst->p_postags_num[r];
										const int pos_r1_num = inst->p_postags_num[r+1];
										for (int pr = 0; pr < pos_r_num; ++pr) {
											if (r == s && pr != ps) continue;
											for (int pr1 = 0; pr1 < pos_r1_num; ++pr1) {
												if (r+1 == t && pr1 != pt) continue;
												
												const ChartItem * const left = _chart_cmp[g][s][r][pg][ps][pr];
												const ChartItem * const right = _chart_cmp[g][t][r+1][pg][pt][pr1];
												if (!left || !right) continue;

												list<const fvec *> fvs = shared_fvs;
												double prob = left->_prob + right->_prob + shared_prob;

												if (use_pos_bigram_features) { // pos bigram: <r, r+1>
													fvs.push_back(&inst->fvec_pos2[r+1][pr][pr1]);
													prob += inst->prob_pos2[r+1][pr][pr1];
												}
												if (use_grand_features && _use_no_grand_features) {
													if (s == r) { // [g->s] has no right-side grandchild
														fvs.push_back(&inst->fvec_grd[g][s][s][pg][ps][ps]); 
														prob += inst->prob_grd[g][s][s][pg][ps][ps];
													}
													if (r+1 == t) { // [g->t] has no left-side grandchild
														fvs.push_back(&inst->fvec_grd[g][t][g][pg][pt][pg]); 
														prob += inst->prob_grd[g][t][g][pg][pt][pg];
													}
												}
												const ChartItem * const item = new ChartItem(SIB_SP, g, s, t, pg, ps, pt, prob, fvs, left, right);
												add_item(_chart_sibling[g][s][t][pg][ps][pt], item);
											}
										}
									}
								} // build sibling spans S[g](s,t)

								// complete spans C[g](s -> t)
								if (s == 0 && g == length && t == length-1 
									|| 
									s != 0 && g != length && candidate_heads[s][g] ) 
								{
									for(int r = s+1; r <= t; ++r) { // I[g](s -> r) + C[s](r -> t)
										const int pos_r_num = inst->p_postags_num[r];
										for (int pr = 0; pr < pos_r_num; ++pr) {
											if (r == t && pr != pt) continue;
											const ChartItem * const left = _chart_incmp[g][s][r][pg][ps][pr];
											const ChartItem * const right = _chart_cmp[s][r][t][ps][pr][pt];
											if (!left || !right) continue;

											list<const fvec *> fvs;
											double prob = left->_prob + right->_prob;
											if (use_sibling_features && _use_last_sibling_features) {
												fvs.push_back(&inst->fvec_sib[s][r][r][ps][pr][pr]); // last child 
												prob += inst->prob_sib[s][r][r][ps][pr][pr];
											}
											if (use_grand_features && _use_no_grand_features && r == t) { // [s->r] has no right-side grandchild
												fvs.push_back(&inst->fvec_grd[s][r][r][ps][pr][pr]); 
												prob += inst->prob_grd[s][r][r][ps][pr][pr];
											}
											if (use_grandsib_features && g != length) {
												fvs.push_back(&inst->fvec_grdsib[g][s][r][r][pg][ps][pr][pr]); // last grandchild 
												prob += inst->prob_grdsib[g][s][r][r][pg][ps][pr][pr];
											}
											const ChartItem * const item = new ChartItem(CMP, g, s, t, pg, ps, pt, prob, fvs, left, right);
											add_item(_chart_cmp[g][s][t][pg][ps][pt], item);
										}
									}
								} // build complete spans C[g](s -> t)

								// complete spans C[g](s <- t)
								if (s != 0 && g != length && candidate_heads[t][g]) {
									for(int r = s; r < t; ++r) { // C[t](s <- r) + I[g](r <- t)
										const int pos_r_num = inst->p_postags_num[r];
										for (int pr = 0; pr < pos_r_num; ++pr) {
											if (r == s && pr != ps) continue;

											const ChartItem * const left = _chart_cmp[t][r][s][pt][pr][ps];
											const ChartItem * const right = _chart_incmp[g][t][r][pg][pt][pr];
											if (!left || !right) continue;

											list<const fvec *> fvs;
											double prob = left->_prob + right->_prob;
											if (use_sibling_features && _use_last_sibling_features) {
												fvs.push_back(&inst->fvec_sib[t][r][r][pt][pr][pr]); // last child 
												prob += inst->prob_sib[t][r][r][pt][pr][pr];
											}
											if (use_grand_features && _use_no_grand_features && r == s) { // [r<-t] has no left-side grandchild
												fvs.push_back(&inst->fvec_grd[t][r][t][pt][pr][pt]); 
												prob += inst->prob_grd[t][r][t][pt][pr][pt];
											}
											if (use_grandsib_features) {
												fvs.push_back(&inst->fvec_grdsib[g][t][r][r][pg][pt][pr][pr]); // last grandchild 
												prob += inst->prob_grdsib[g][t][r][r][pg][pt][pr][pr];
											}
											const ChartItem * const item = new ChartItem(CMP, g, t, s, pg, pt, ps, prob, fvs, left, right);
											add_item(_chart_cmp[g][t][s][pg][pt][ps], item);
										}
									}
								} // build complete spans C[g](s <- t)
							} // loop on pt
						} // loop on ps
					} // loop on pg
				} // loop on g
			} // loop on s
		} // loop on width
	}



	void Decoder_3o_koo::get_best_parse( Instance *inst ) const
	{
		const int length = inst->size();
		inst->predicted_heads.clear();
		inst->predicted_heads.resize(length, -1);
		inst->predicted_postags_idx.clear();
		inst->predicted_postags_idx.resize(length, -1);
		inst->predicted_fv.clear();
		inst->predicted_prob = 0;

		const ChartItem * best_item = 0;
		const int n = length-1;
		for (int pn = 0; pn < inst->p_postags_num[n]; ++pn) {
			const ChartItem * const item = _chart_cmp[length][0][n][0][0][pn];
			if(!item) continue;
			if (!best_item || best_item->_prob < item->_prob - EPS) { // absolutely less than the new item
				best_item = item;
			}
		}

		inst->predicted_prob = best_item->_prob;
		get_best_parse_recursively(inst, best_item);
	}

	void Decoder_3o_koo::get_best_parse_recursively( Instance *inst, const ChartItem * const item ) const
	{
		if (!item) return;
		get_best_parse_recursively(inst, item->_left);

		if (INCMP == item->_comp) {
			assert(0 > inst->predicted_heads[item->_t]);
			inst->predicted_heads[item->_t] = item->_s;
		} else if (CMP == item->_comp) {
			// do nothing
		} else if (SIB_SP == item->_comp) {
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





