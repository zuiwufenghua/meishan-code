#include "Decoder_1o.h"

namespace dparser {

	void Decoder_1o::init_chart(const Instance *inst)
	{
		const int length = inst->size();
	
		for(int i = 0; i < length; i++) {
      _chart_cmp[i][i] = new ChartItem(i);
		}
	}

	void Decoder_1o::decode_projective(const Instance *inst)
  {

		const int length = inst->size();
		for(int width = 1; width < length; width++) 
		{
			for(int s = 0; s+width < length; s++)
      {
				const int t = s + width;
        // incompeleted
        {
          double max_score = _chart_cmp[s][s]->_prob +  _chart_cmp[t][s+1]->_prob;
          int max_r = s;
          
          if(s!=0)
          {
				    for(int r = s+1; r < t; r++) { // C(s,r) + C(t,r+1)
              const ChartItem * const left = _chart_cmp[s][r];
              if (!left) continue;
              const ChartItem * const right = _chart_cmp[t][r+1];
              if (!right) continue;
              if(max_score < left->_prob + right->_prob)
              {
                max_score = left->_prob + right->_prob;
                max_r = r;        
              }
            }
          }

          {
            double max_label_score = inst->prob_depl[s][t][0];
            int max_label = 0;
            for(int l = 0; l < _L; l++)
            {
              if(max_label_score < inst->prob_depl[s][t][l])
              {
                max_label_score = inst->prob_depl[s][t][l];
                max_label = l;
              }
            }
            list<const fvec *> fvs;
            fvs.push_back(&inst->fvec_depl[s][t][max_label]);
            const ChartItem * const item = new ChartItem(INCMP, s, t, max_score + max_label_score, fvs, _chart_cmp[s][max_r], _chart_cmp[t][max_r+1], max_label);
            add_item(_chart_incmp[s][t], item);

            //cerr << "\n" << *(_chart_incmp[s][t]) << ", r" <<max_r;;
          }

          if(s!= 0){
            double max_label_score = inst->prob_depl[t][s][0];
            int max_label = 0;
            for(int l = 0; l < _L; l++)
            {
              if(max_label_score < inst->prob_depl[t][s][l])
              {
                max_label_score = inst->prob_depl[t][s][l];
                max_label = l;
              }
            }
            list<const fvec *> fvs;
            fvs.push_back(&inst->fvec_depl[t][s][max_label]);
            const ChartItem * const item = new ChartItem(INCMP, t, s, max_score + max_label_score, fvs, _chart_cmp[s][max_r], _chart_cmp[t][max_r+1], max_label);
            add_item(_chart_incmp[t][s], item);
            //cerr << "\n" << *(_chart_incmp[t][s]) << ", r" <<max_r;
          }
        }

        //right compeleted
        if(s!= 0 || t== length-1){
          double max_score = _chart_incmp[s][t]->_prob +  _chart_cmp[t][t]->_prob;
          int max_r = t;
          for(int r = s+1; r <t; r++)
          {
            const ChartItem * const left = _chart_incmp[s][r];
            if (!left) continue;
            const ChartItem * const right = _chart_cmp[r][t];
            if (!right) continue;
            if(max_score < left->_prob + right->_prob)
            {
              max_score = left->_prob + right->_prob;
              max_r = r;        
            }           
          }
          list<const fvec *> fvs;
          const ChartItem * const item = new ChartItem(CMP, s, t, max_score, fvs, _chart_incmp[s][max_r], _chart_cmp[max_r][t]);
				  add_item(_chart_cmp[s][t], item);
          //cerr << "\n" << *(_chart_cmp[s][t]) << ", r" <<max_r;;
        }

        //left compeleted
        if(s!= 0){
          double max_score = _chart_incmp[t][s]->_prob +  _chart_cmp[s][s]->_prob;
          int max_r = s;
          for(int r = s+1; r <t; r++)
          {
            const ChartItem * const left = _chart_cmp[r][s];
            if (!left) continue;
            const ChartItem * const right = _chart_incmp[t][r];
            if (!right) continue;
            if(max_score < left->_prob + right->_prob)
            {
              max_score = left->_prob + right->_prob;
              max_r = r;        
            }           
          }
          list<const fvec *> fvs;
          const ChartItem * const item = new ChartItem(CMP, t, s, max_score, fvs, _chart_cmp[max_r][s], _chart_incmp[t][max_r]);
				  add_item(_chart_cmp[t][s], item);
          //cerr << "\n" << *(_chart_cmp[t][s]) << ", r" <<max_r;;
        }
      }
		}
	}

	void Decoder_1o::get_best_parse( Instance *inst ) const
	{
    //chart_dump(inst->size());
		const int length = inst->size();
		inst->predicted_heads.resize(1,inst->size());
		inst->predicted_heads=-1;
		inst->predicted_fvs.resize(1);
    inst->predicted_fvs = sparsevec();
    inst->predicted_probs.resize(1);
		inst->predicted_probs = 0;
		inst->predicted_deprels_int.resize(1,inst->size());
		inst->predicted_deprels_int=-1;

		const ChartItem * best_item = _chart_cmp[0][length-1];

    //for(int k = 0; k <  _chart_cmp[0][length-1].elemsize(); k++)
    //{
    //    cerr << "\n" << *(_chart_cmp[0][length-1][k]);
    //}


		inst->predicted_probs[0] = best_item->_prob;
		get_best_parse_recursively(inst, best_item);

	}

	void Decoder_1o::get_best_parse_recursively( Instance *inst, const ChartItem * const item ) const
  {
		if (!item) return;
		get_best_parse_recursively(inst, item->_left);

		if (INCMP == item->_comp) {
			// set heads and deprels
			assert(0 > inst->predicted_heads[0][item->_t]);
			inst->predicted_heads[0][item->_t] = item->_s;
			assert(0 > inst->predicted_deprels_int[0][item->_t]);
			inst->predicted_deprels_int[0][item->_t] = item->_label_s_t;
		} else if (CMP == item->_comp) {
			// do nothing
		} else {
			cerr << "unknown item->_comp: " << item->_comp << endl;
			exit(0);
		}

		// collect features
		for (list<const fvec *>::const_iterator it = item->_fvs.begin(); it != item->_fvs.end(); ++it) {
			parameters::sparse_add(inst->predicted_fvs[0], *it);
		}
		get_best_parse_recursively(inst, item->_right);
	}


	void Decoder_1o::inside( Instance *inst, Marginal &marg) { 
		
		// initialize all _I_cmp[s,s] with log(1.0) 
		// = log ( exp(0.0) )
    const int length = inst->size();
		const double log_exp_zero = log(1.0);
		for (int s = 0; s < length; ++s) {
			marg.inside_C_set(s, s, log_exp_zero);
		}

		for (int width = 1; width < length; ++width) {
			for (int s = 0; s + width < length; ++s) {
				const int t = s + width;

				double sum_product_s_t = marg.inside_C(s,s) + marg.inside_C(t,s+1);
				if (s != 0){
					for (int r = s+1; r+1 <= t; ++r) {
						sum_product_s_t = Marginal::log_add(sum_product_s_t, marg.inside_C(s,r) + marg.inside_C(t,r+1));
					}
				}

        for(int l = 0; l < _L; l++) {
				  // I(s->t), if s==0, only allow C(s->s) + C(s+1 <- t)
				  marg.inside_U_set(s, t, l, sum_product_s_t + inst->prob_depl[s][t][l]);

				  // I(s<-t)
				  if (s != 0) {
					  marg.inside_U_set(t, s, l, sum_product_s_t + inst->prob_depl[t][s][l]);
				  } 
        }

				// C(s->t)
				if (s != 0 || t == length-1) { 
					int c = 0;        
					for(int r = s+1; r <= t; ++r) {
            for(int l = 0; l < _L; l++) {
              if(c == 0)
              {
                marg.inside_C_set(s, t, marg.inside_U(s, r, l) + marg.inside_C(r, t));
              }
              else
              {
						    marg.inside_C_add(s, t, marg.inside_U(s, r, l) + marg.inside_C(r, t));
              }
              c++;
            }
          }
        }

				// C(s<-t)
				if (s != 0) {
					int c = 0;        
					for(int r = s; r < t; ++r) {
            for(int l = 0; l < _L; l++) {
              if(c == 0)
              {
                marg.inside_C_set(t, s, marg.inside_U(t, r, l) + marg.inside_C(r, s));
              }
              else
              {
						    marg.inside_C_add(t, s, marg.inside_U(t, r, l) + marg.inside_C(r, s));
              }
              c++;
            }
          }
        }
			}
		}
	}


	void Decoder_1o::outside(Instance *inst, Marginal &marg) {
		//MultiArray<double> marg_probs;
		//marg_probs.setDimensionVal(dim, length, length);
		//marg_probs.resize(dim, -1.0);

		// initialize _O_cmp[0,length-1] and _O_UnCmp[0,length-1] with log(1.0) 
		// = log ( exp(0.0) )

    const int length = inst->size();
		const double log_exp_zero = log(1.0);
		marg.outside_C_set(0, length-1, log_exp_zero);
		marg.outside_U_set(0, length-1, marg.inside_C(length-1, length-1) + marg.outside_C(0, length-1)); // log(1.0)

		for (int width = length-2; width > 0; --width) {
			for (int s = 0; s + width < length; ++s) {
				const int t = s + width;

				// ----- C(s -> t) -----				

        if (s != 0) { // actually should be also s!=0||t==length-1, just because outside_cmp[0][length-1] has been cacluated 

					int c = 0;

					// I(r->s) + C(s->t) = C(r->t)
					for (int r = 0; r < s; ++r) {

						if (r == 0 && t != length -1) { // only C(0 -> length-1) is allowed
							continue;
						}
            for(int l = 0; l < _L; l++) {
              const double accumu_score = marg.inside_U(r,s, l) + marg.outside_C(r,t);
						  if (!c++)
							  marg.outside_C_set(s, t, accumu_score);
						  else
							  marg.outside_C_add(s, t, accumu_score);
            }
					}

					for (int r = t+1; r < length; ++r) {	// s != 0

						// C(s->t) + C(t+1 <- r) = I(s->r)
						const double accumu_score_sr = marg.inside_C(r, t+1) + marg.outside_U(s, r);
            for(int l = 0; l < _L; l++)
            {
						  if (!c++)
							  marg.outside_C_set(s, t, accumu_score_sr + inst->prob_depl[s][r][l]);
						  else
							  marg.outside_C_add(s, t, accumu_score_sr + inst->prob_depl[s][r][l]);
            }

						// C(s->t) + C(t+1 <- r) = I(s<-r)
						const double accumu_score_rs = marg.inside_C(r, t+1) + marg.outside_U(r, s);
            for(int l = 0; l < _L; l++)
            {
						  if (!c++)
							  marg.outside_C_set(s, t, accumu_score_rs + inst->prob_depl[r][s][l]);
						  else
							  marg.outside_C_add(s, t, accumu_score_rs + inst->prob_depl[r][s][l]);
            }
					}
				} // if (s != 0) { // single root is allowed


				// ----- C(s <- t) -----

				if (s != 0) { // w0 is never a modifier

					int c = 0;

					// C(s <- t) + I(t <- r) = C(s <- r)
					for (int r = t+1; r < length; ++r) {
            for(int l = 0; l < _L; l++) {
              const double accumu_score = marg.inside_U(r,t, l) + marg.outside_C(r,s);
						  if (!c++)
							  marg.outside_C_set(t, s, accumu_score);
						  else
							  marg.outside_C_add(t, s, accumu_score);
            }
					}

					for (int r = 0; r < s; ++r) {	

						// C(r -> s-1) + C(s <- t) = I(r -> t)
						if (r == 0 && s-1 != 0) continue;
						const double accumu_score_rt = marg.inside_C(r, s-1) + marg.outside_U(r, t);
            for(int l = 0; l < _L; l++) {
						  if (!c++)
							  marg.outside_C_set(t, s, accumu_score_rt + inst->prob_depl[r][t][l]);
						  else
							  marg.outside_C_add(t, s, accumu_score_rt + inst->prob_depl[r][t][l]);
            }

						// C(r -> s-1) + C(s <- t) = I(r <- t)
						if (r == 0) continue;
						const double accumu_score_tr = marg.inside_C(r, s-1) + marg.outside_U(t, r);
            for(int l = 0; l < _L; l++) {
						if (!c++)
							marg.outside_C_set(t, s, accumu_score_tr + inst->prob_depl[t][s][l]);
						else
							marg.outside_C_add(t, s, accumu_score_tr + inst->prob_depl[t][r][l]);
            }
          }
        } 
				
				// ----- I(s -> t) -----
        {
          int c = 0;
				  for (int r = t; r < length; ++r) {	
					  if (s == 0 && r != length-1) continue;

					  // I(s -> t) + C(t -> r) = C(s -> r)
					  const double accumu_score = marg.inside_C(t, r) + marg.outside_C(s, r);
					  if (!c++)
						  marg.outside_U_set(s, t, accumu_score);
					  else
						  marg.outside_U_add(s, t, accumu_score);
				  }
        }


				// ----- I(s <- t) -----
        {
				  int c = 0;
				  if (s != 0) {
					  for (int r = 1; r <= s; ++r) {	
						  // C(r <- s) + I(s <- t) = C(r <- t)
						  const double accumu_score = marg.inside_C(s, r) + marg.outside_C(t, r);
						  if (!c++)
							  marg.outside_U_set(t, s, accumu_score);
						  else
							  marg.outside_U_add(t, s, accumu_score);
					  }
          }
        }
			}
		}
	}



} // namespace dparser

