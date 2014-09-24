#include "Decoder_1okbest.h"

namespace dparser {

	void Decoder_1okbest::init_chart(const Instance *inst)
	{
		const int length = inst->size();
	
		for(int i = 0; i < length; i++) {
      bool b_inserted = false;
			_chart_cmp[i][i].add_elem(new ChartItem(i), b_inserted);
		}
	}

	void Decoder_1okbest::decode_projective(const Instance *inst)
	{

		const int length = inst->size();
		for(int width = 1; width < length; width++) 
		{
			for(int s = 0; s+width < length; s++)
      {
				const int t = s + width;

        for (int l = 0; l < _L; ++l) {
				  for(int r = s; r < t; r++) { // C(s,r) + C(t,r+1)
            for(int k1 = 0; k1 < _chart_cmp[s][r].elemsize(); k1++)
            {
              bool b_stl_full = false;
              bool b_tsl_full = false;
              bool b_zero_k2_allfull = false;
					    const ChartItem * const left = _chart_cmp[s][r][k1];
					    if (!left) continue;
              for(int k2 = 0; k2 < _chart_cmp[t][r+1].elemsize(); k2++)
              {
                b_stl_full = false;
                b_tsl_full = false;
					      const ChartItem * const right = _chart_cmp[t][r+1][k2];
					      if (!right) continue;

                bool b_inserted = false;

						    if(!b_stl_full){ // I(s,t)
							    list<const fvec *> fvs;
							    double prob = left->_prob + right->_prob;
							    fvs.push_back(&inst->fvec_depl[s][t][l]);
							    prob += inst->prob_depl[s][t][l];
							    // add the new item
							    const ChartItem * const item = new ChartItem(INCMP, s, t, prob, fvs, left, right, l);
							    //add_item(_chart_incmp[s][t][l], item);
                  
                  const ChartItem * new_item = _chart_incmp[s][t][l].add_elem(item, b_inserted);
                  if(new_item != NULL)
                  {
                    delete new_item;
                  }

                  if(!b_inserted && _chart_incmp[s][t][l].elemsize() == _K)
                  {
                    b_stl_full = true;
                  }
						    }

						    if (s != 0 && !b_tsl_full) { // I(t,s)
							    list<const fvec *> fvs;
							    double prob = left->_prob + right->_prob;
							    fvs.push_back(&inst->fvec_depl[t][s][l]);
							    prob += inst->prob_depl[t][s][l];
							    // add the new item
							    const ChartItem * const item = new ChartItem(INCMP, t, s, prob, fvs, left, right, l);
							    //add_item(_chart_incmp[t][s][l], item);
                  const ChartItem * new_item = _chart_incmp[t][s][l].add_elem(item, b_inserted);
                  
                  if(new_item != NULL)
                  {
                    delete new_item;
                  }

                  if(!b_inserted && _chart_incmp[t][s][l].elemsize() == _K)
                  {
                    b_tsl_full = true;
                  }
                }

                // if C(s,r)[k1] && C(t,r+1)[k2] is not able to be inserted in, then break k2;
                if(b_stl_full && b_tsl_full)
                {
                  if(k2 == 0) b_zero_k2_allfull = true;
                  break;
                }
              }
              // if C(s,r)[k1] && C(t,r+1)[0] is not able to be inserted in, then break k1;
              if(b_zero_k2_allfull)
              {
                break;
              }

            }
          }


          _chart_incmp[s][t][l].sort_elem();
          for(int k = 0; k <  _chart_incmp[s][t][l].elemsize(); k++)
          {           
            if(_chart_incmp[s][t][l][k]->_s != s || _chart_incmp[s][t][l][k]->_t != t 
              || _chart_incmp[s][t][l][k]->_label_s_t != l || abs(_chart_incmp[s][t][l][k]->_prob) > DOUBLE_POSITIVE_INFINITY)
            {
              cerr << "\n" << *(_chart_incmp[s][t][l][k]);
            }
          }

           _chart_incmp[t][s][l].sort_elem();
          for(int k = 0; k <  _chart_incmp[t][s][l].elemsize(); k++)
          {
            if(_chart_incmp[t][s][l][k]->_s != t || _chart_incmp[t][s][l][k]->_t != s 
              || _chart_incmp[t][s][l][k]->_label_s_t != l || abs(_chart_incmp[t][s][l][k]->_prob) > DOUBLE_POSITIVE_INFINITY)
            {
              cerr << "\n" << *(_chart_incmp[t][s][l][k]);
            }
          }
        }


        for(int r = s; r <= t; r++) {
          if (r != s) { // C(s,t) = I(s,r) + C(r,t)
            for (int l = 0; l < _L; ++l) {
              for(int k1 = 0; k1 < _chart_cmp[r][t].elemsize(); k1++) {
                bool b_zero_k2_allfull = false;
						    const ChartItem * const right = _chart_cmp[r][t][k1];
						    if (!right) continue;
                for(int k2 = 0; k2 < _chart_incmp[s][r][l].elemsize(); k2++)
                {
                  bool b_full = false;
                  bool b_inserted = false;
							    const ChartItem * const left = _chart_incmp[s][r][l][k2];
							    if (!left) continue;

							    list<const fvec *> fvs;
							    const double prob = left->_prob + right->_prob;
							    // add the new item
							    const ChartItem * const item = new ChartItem(CMP, s, t, prob, fvs, left, right);
							    //add_item(_chart_cmp[s][t], item);
                  const ChartItem * new_item =_chart_cmp[s][t].add_elem(item, b_inserted);
                  if(new_item != NULL)
                  {
                    delete new_item;
                  }

                  if(!b_inserted &&_chart_cmp[s][t].elemsize() == _K)
                  {
                    b_full = true;
                  }

                  if(b_full)
                  {
                    if(k2 == 0) b_zero_k2_allfull = true;
                    break;
                  }
                }
                if(b_zero_k2_allfull)
                {
                  break;
                }
              }
            }
          }

					if (r != t && s != 0) { // C(t,s) = C(r,s) + I(t,r)
            for (int l = 0; l < _L; ++l) {
              for(int k1 = 0; k1 < _chart_cmp[r][s].elemsize(); k1++) {
                bool b_zero_k2_allfull = false;
						    const ChartItem * const left = _chart_cmp[r][s][k1];
						    if (!left) continue;
                for(int k2 = 0; k2 < _chart_incmp[t][r][l].elemsize(); k2++)
                {
                  bool b_full = false;
                  bool b_inserted = false;
						
							    const ChartItem * const right = _chart_incmp[t][r][l][k2];
							    if (!right) continue;

							    list<const fvec *> fvs;
							    double prob = left->_prob + right->_prob;
							    // add the new item
							    const ChartItem * const item = new ChartItem(CMP, t, s, prob, fvs, left, right);
							    //add_item(_chart_cmp[t][s], item);
                  const ChartItem * new_item =_chart_cmp[t][s].add_elem(item, b_inserted);
                  if(new_item != NULL)
                  {
                    delete new_item;
                  }

                  if(!b_inserted &&_chart_cmp[t][s].elemsize() == _K)
                  {
                    b_full = true;
                  }

                  if(b_full)
                  {
                    if(k2 == 0) b_zero_k2_allfull = true;
                    break;
                  }
                }
                if(b_zero_k2_allfull)
                {
                  break;
                }
              }
            }
          }
        }

        _chart_cmp[s][t].sort_elem();
        for(int k = 0; k <  _chart_cmp[s][t].elemsize(); k++)
        {
          if( _chart_cmp[s][t][k]->_s != s ||  _chart_cmp[s][t][k]->_t != t 
            || abs( _chart_cmp[s][t][k]->_prob) > DOUBLE_POSITIVE_INFINITY)
          {
            cerr << "\n" << *(_chart_cmp[s][t][k]);
          }
        }

        _chart_cmp[t][s].sort_elem();
        for(int k = 0; k <  _chart_cmp[t][s].elemsize(); k++)
        {
          if(_chart_cmp[t][s][k]->_s != t || _chart_cmp[t][s][k]->_t != s 
            || abs(_chart_cmp[t][s][k]->_prob) > DOUBLE_POSITIVE_INFINITY)
          {
            cerr << "\n" << *(_chart_cmp[t][s][k]);
          }
        }
      }
		}
	}

	void Decoder_1okbest::get_best_parse( Instance *inst ) const
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

		const ChartItem * best_item = _chart_cmp[0][length-1][0];

    //for(int k = 0; k <  _chart_cmp[0][length-1].elemsize(); k++)
    //{
    //    cerr << "\n" << *(_chart_cmp[0][length-1][k]);
    //}


		inst->predicted_probs[0] = best_item->_prob;
		get_best_parse_recursively(inst, best_item);

	}

  
  void Decoder_1okbest::get_kbest_parse( Instance *inst) const
	{
		const int length = inst->size();
    int kbest = _chart_cmp[0][length-1].elemsize();
		inst->predicted_heads.resize(kbest,inst->size());
		inst->predicted_heads=-1;
		inst->predicted_fvs.resize(kbest);
    inst->predicted_fvs = sparsevec();
    inst->predicted_probs.resize(kbest);
		inst->predicted_probs = 0;
		inst->predicted_deprels_int.resize(kbest,inst->size());
		inst->predicted_deprels_int=-1;


    for(int k = 0; k <  _chart_cmp[0][length-1].elemsize(); k++)
    {
      const ChartItem * cur_item = _chart_cmp[0][length-1][k];
      inst->predicted_probs[k] = cur_item->_prob;
		  get_best_parse_recursively(inst, cur_item, k);
    }

	}

	void Decoder_1okbest::get_best_parse_recursively( Instance *inst, const ChartItem * const item ) const
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

  
  void Decoder_1okbest::get_best_parse_recursively( Instance *inst, const ChartItem * const item, int k) const
	{
		if (!item) return;
		get_best_parse_recursively(inst, item->_left, k);

		if (INCMP == item->_comp) {
			// set heads and deprels
			assert(0 > inst->predicted_heads[k][item->_t]);
			inst->predicted_heads[k][item->_t] = item->_s;
			assert(0 > inst->predicted_deprels_int[k][item->_t]);
			inst->predicted_deprels_int[k][item->_t] = item->_label_s_t;
		} else if (CMP == item->_comp) {
			// do nothing
		} else {
			cerr << "unknown item->_comp: " << item->_comp << endl;
			exit(0);
		}

    		// collect features
		for (list<const fvec *>::const_iterator it = item->_fvs.begin(); it != item->_fvs.end(); ++it) {
			parameters::sparse_add(inst->predicted_fvs[k], *it);
		}
		get_best_parse_recursively(inst, item->_right, k);
	}


} // namespace dparser

