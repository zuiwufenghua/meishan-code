#include "KBestParseForest.h"


KBestParseForest::~KBestParseForest(void)
{
}

bool KBestParseForest::add(int s, int type, int dir, double score, const FeatureVec &fv)
{
	bool added = false;
	vector<unsigned int> chart_dim;
	unsigned int chart_pos;
	chart.setDemisionVal(chart_dim, s, s, dir, 0, 0);
	chart.getElement(chart_dim, chart_pos);
	if( !chart.getElement(chart_pos).m_isInit ) {
		for(int i = 0; i < K; i++) {
			ParseForestItem item(s, type, dir, DOUBLE_NEGATIVE_INFINITY, FeatureVec());
			item.copyValuesTo(chart.getElement(chart_pos++));
		}
	}
	chart.setDemisionVal(chart_dim, s, s, dir, 0, K-1);
	if(chart.getElement(chart_dim, chart_pos).prob > score - EPS)
		return false;

	// find the first item which is smaller than score
	chart.setDemisionVal(chart_dim, s, s, dir, 0, 0);
	chart.getElement(chart_dim, chart_pos);
	for(int i = 0; i < K; i++) {
		if(chart.getElement(chart_pos + i).prob < score - EPS) {
			ParseForestItem tmp;
			chart.getElement(chart_pos + i).copyValuesTo(tmp); // store [i] to tmp
			ParseForestItem tmp2(s, type, dir, score, fv);
			tmp2.copyValuesTo(chart.getElement(chart_pos + i)); // add to [i]

			// push [i] into the remaining ones, until encountering INFINITY
			// the score should be in descending order
			for(int j = i+1; j < K && tmp.prob > DOUBLE_NEGATIVE_INFINITY+EPS; j++) {
				ParseForestItem tmp3;
				chart.getElement(chart_pos + j).copyValuesTo(tmp3);		// [j] -> tmp3
				tmp.copyValuesTo(chart.getElement(chart_pos + j));		// [j-1](tmp) -> [j]
				tmp3.copyValuesTo(tmp);									// tmp3 -> tmp
			}
			added = true;
			break;
		}
	}
	return added;
}


bool KBestParseForest::add(int s, int r, int t, int type,
		 int dir, int comp, double score,
		 const FeatureVec &fv,
		 ParseForestItem *p1, ParseForestItem *p2)
{
	bool added = false;
	// if (s == 1 && r == 1  && t == 2 && dir == 1 && comp == 0) {
	// 		cout << "bad" << endl;
	// }
//	cout << s << " " << r << " " << t << " " << dir << " " << comp << " " << score << endl;
	vector<unsigned int> chart_dim;
	unsigned int chart_pos;
	chart.setDemisionVal(chart_dim, s, t, dir, comp, 0);
	if( !chart.getElement(chart_dim, chart_pos).m_isInit ) {
		int i = 0;
		for(; i < K; i++) {
			ParseForestItem item(s,r,t,type,dir,comp,DOUBLE_NEGATIVE_INFINITY,FeatureVec(),0,0);
			item.copyValuesTo(chart.getElement(chart_pos++));
		}
	}
	chart.setDemisionVal(chart_dim, s, t, dir, comp, K-1);
	if(chart.getElement(chart_dim, chart_pos).prob > score - EPS)
		return false;

	chart.setDemisionVal(chart_dim, s, t, dir, comp, 0);
	chart.getElement(chart_dim, chart_pos);
	// find the first item which is smaller than score
	int i = 0;
	for(; i < K; i++) {
		if(chart.getElement(chart_pos + i).prob < score - EPS) {
			ParseForestItem tmp;
			chart.getElement(chart_pos + i).copyValuesTo(tmp); // store [i] to tmp
			ParseForestItem tmp2(s,r,t,type,dir,comp,score,fv,p1,p2);
			tmp2.copyValuesTo(chart.getElement(chart_pos + i)); // add to [i]

			// push [i] into the remaining ones, until encountering INFINITY
			// the score should be in descending order
			int j = i+1;
			for(; j < K && tmp.prob > DOUBLE_NEGATIVE_INFINITY+EPS; j++) {
				ParseForestItem tmp3;
				chart.getElement(chart_pos + j).copyValuesTo(tmp3);	// [j] -> tmp3
				tmp.copyValuesTo(chart.getElement(chart_pos + j));	// [j-1](tmp) -> j
				tmp3.copyValuesTo(tmp);								// tmp3 -> tmp
			}
			added = true;
			break;
		}
	}

	return added;
}

double KBestParseForest::getProb(int s, int t, int dir, int comp, int i) {
	vector<unsigned int> chart_dim;
	unsigned int chart_pos;
	chart.setDemisionVal(chart_dim, s, t, dir, comp, i);
	if(chart.getElement(chart_dim, chart_pos).m_isInit)
		return chart.getElement(chart_pos).prob;
	return DOUBLE_NEGATIVE_INFINITY;
}


void KBestParseForest::getProbs(int s, int t, int dir, int comp, vector<double> &vecProb)
{
	vector<unsigned int> chart_dim;
	unsigned int chart_pos;
	vecProb.resize(K, DOUBLE_NEGATIVE_INFINITY);
	int i = 0;
	chart.setDemisionVal(chart_dim, s, t, dir, comp, 0);
	chart.getElement(chart_dim, chart_pos);
	for(; i < K; i++) {
		if (chart.getElement(chart_pos).m_isInit) {
			vecProb[i] = chart.getElement(chart_pos).prob;
			++chart_pos;
		}
	}
}

void KBestParseForest::getBestParse(FeatureVec &d0, string &d1, double &parse_prob) {
	vector<unsigned int> chart_dim;
	unsigned int chart_pos;
	chart.setDemisionVal(chart_dim, 0, end, 0, 0, 0);
	getFeatureVec(chart.getElement(chart_dim, chart_pos), d0);
	getDepString(chart.getElement(chart_pos), d1);
	parse_prob = chart.getElement(chart_pos).prob;
}

void KBestParseForest::getBestParses(vector<FeatureVec> &d0, vector<string> &d1, vector<double> &parse_probs)
{
	tmp = 0;
	vector<unsigned int> chart_dim;
	unsigned int chart_pos;
	d0.resize(K, FeatureVec());
	d1.resize(K, "");
	parse_probs.clear();
//	parse_probs.resize(K, DOUBLE_NEGATIVE_INFINITY);
	int k = 0;
	chart.setDemisionVal(chart_dim, 0, end, 0, 0, 0);
	chart.getElement(chart_dim, chart_pos);
	for(; k < K; k++) {
		if(chart.getElement(chart_pos).prob > DOUBLE_NEGATIVE_INFINITY+EPS) {
//			parse_probs[k] = chart.getElement(chart_pos).prob;
			parse_probs.push_back( chart.getElement(chart_pos).prob );
			getDepString(chart.getElement(chart_pos), d1[k]);
			getFeatureVec(chart.getElement(chart_pos), d0[k]);
			++chart_pos;
		}
		else {
			break;
		}
	}
	if (parse_probs.empty()) {
		cout << "KBestParseForest::getBestParses() err: no parse result exists" << endl;
	}
//	vector<int> vecKeys;
//	d0[0].getKeys(vecKeys);
//	cout << "best parse key num: " << vecKeys.size() << endl;
//	cout << "tmp: " << tmp << endl;
//	exit(0);
}

void KBestParseForest::getFeatureVec(ParseForestItem &pfi, FeatureVec &fv) {
	vector<FeatureVec *> pvfv;
	getFeatureVec(pfi, pvfv);
	int i = 0;
	for (; i < pvfv.size(); ++i) {
		fv.add(pvfv[i]);
	}
}


void KBestParseForest::getFeatureVec(ParseForestItem &pfi, vector<FeatureVec *> &pvfv)
{
//	cout << pfi.s << " " << pfi.r << " " << pfi.t << " " << pfi.dir << " " << pfi.comp << " " << pfi.type << " " << pfi.prob << endl;
//	vector<int> vecKeys;
//	pfi.fv.getKeys(vecKeys);
//	cout << vecKeys.size() << endl;
//	tmp += vecKeys.size();

	if (pfi.m_isInit) pvfv.push_back( &(pfi.fv) );
	else {
		cout << "KBestParseForest::getFeatureVec() : pfi is not initialized" << endl;
		return;
	}

	if(!pfi.left) {
		return;
	}

	getFeatureVec((*(pfi.left)), pvfv);
	getFeatureVec(*(pfi.right), pvfv);
}

void KBestParseForest::getDepString(const ParseForestItem &pfi, string &strDep)
{
	if (!pfi.m_isInit) 
	{
		cout << "KBestParseForest::getDepString() : pfi is not initialized" << endl;
		strDep = "";
		return;
	}

	if(!pfi.left) 
	{
		strDep = "";
		return;
	}

	string left;
	getDepString(*(pfi.left), left);
	string right;
	getDepString(*(pfi.right), right);
	string left_right = left + " " + right;
	trim(left_right);

	if(pfi.dir == 0 && pfi.comp == 1) 
	{
		ostringstream out;
		out << left_right << " " << pfi.s << "|" << pfi.t << ":" << pfi.type;
		strDep = out.str();
		trim(strDep);
//		return ((getDepString(pfi.left)+" "+getDepString(pfi.right)).trim()+" "+pfi.s+"|"+pfi.t+":"+pfi.type).trim();
	}
	else if(pfi.dir == 1 && pfi.comp == 1) 
	{
		ostringstream out;
		out << pfi.t << "|" << pfi.s << ":" << pfi.type << " " << left_right;
		strDep = out.str();
		trim(strDep);
//		return (pfi.t+"|"+pfi.s+":"+pfi.type+" "+(getDepString(pfi.left)+" "+getDepString(pfi.right)).trim()).trim();
	}
	else 
	{
		strDep = left_right;
	}
}

void KBestParseForest::viterbi( DepInstance *inst, FVS_PROBS* pFvsProbs, const MultiArray<int> &static_types, bool isLabeled)
{
	int s = 0;
	for(; s < inst->size(); s++) {
		add(s, -1, 0, 0.0, FeatureVec());	// -1:type; 0:dir;
		add(s, -1, 1, 0.0, FeatureVec());
 	}

  MultiArray<FeatureVec>& fvs = pFvsProbs->fvs;
	MultiArray<double>& probs = pFvsProbs->probs;
	MultiArray<FeatureVec>& fvs_trips = pFvsProbs->fvs_trips;
	MultiArray<double>& probs_trips = pFvsProbs->probs_trips;
	MultiArray<FeatureVec>& nt_fvs = pFvsProbs->nt_fvs;
	MultiArray<double>& nt_probs = pFvsProbs->nt_probs;

	vector<unsigned int> fvs_dim;
	unsigned int fvs_pos;
	vector<unsigned int> trip_dim;
	unsigned int trip_pos;
	vector<unsigned int> sib_dim;
	unsigned int sib_pos;
	vector<unsigned int> type_dim;
	unsigned int type_pos;
	vector<unsigned int> nt_dim;
	unsigned int nt_pos;

	vector<unsigned int> chart_dim;
	unsigned int chart_pos;
	vector<unsigned int> chart_dim2;
	unsigned int chart_pos2;
//	cout << "\n2-order viterbi start" << endl;
	int j = 1;
	for(; j < inst->size(); j++)
	{
//		cout << "j : " << j << endl;
		int s = 0;
		for(; s < inst->size() && s+j < inst->size(); s++)
		{
			int t = s + j;
//			cout << "(s, t) : " << s << " " << t << endl;
			fvs.setDemisionVal(fvs_dim, s, t, 0);
			FeatureVec &prodFV_st = fvs.getElement(fvs_dim, fvs_pos);
			double prodProb_st = probs.getElement(fvs_pos);
			++fvs_pos;
			FeatureVec &prodFV_ts = fvs.getElement(fvs_pos);
			double prodProb_ts = probs.getElement(fvs_pos);

			static_types.setDemisionVal(type_dim, s, t);
			int type1 = isLabeled ? static_types.getElement(type_dim, type_pos) : -1;
			static_types.setDemisionVal(type_dim, t, s);
			int type2 = isLabeled ? static_types.getElement(type_dim, type_pos) : -1;

			FeatureVec tmp_feature_vec;

			nt_fvs.setDemisionVal(nt_dim, s, type1, 0, 1);
			FeatureVec &nt_fv_s_01 = isLabeled ? nt_fvs.getElement(nt_dim, nt_pos) : tmp_feature_vec;
			double nt_prob_s_01 = isLabeled ? nt_probs.getElement(nt_pos) : 0.0;

			nt_fvs.setDemisionVal(nt_dim, s, type2, 1, 0);
			FeatureVec &nt_fv_s_10 = isLabeled ? nt_fvs.getElement(nt_dim, nt_pos) : tmp_feature_vec;
			double nt_prob_s_10 = isLabeled ? nt_probs.getElement(nt_pos) : 0.0;

			nt_fvs.setDemisionVal(nt_dim, t, type1, 0, 0);
			FeatureVec &nt_fv_t_00 = isLabeled ? nt_fvs.getElement(nt_dim, nt_pos) : tmp_feature_vec;
			double nt_prob_t_00 = isLabeled ? nt_probs.getElement(nt_pos) : 0.0;

			nt_fvs.setDemisionVal(nt_dim, t, type2, 1, 1);
			FeatureVec &nt_fv_t_11 = isLabeled ? nt_fvs.getElement(nt_dim, nt_pos) : tmp_feature_vec;
			double nt_prob_t_11 = isLabeled ? nt_probs.getElement(nt_pos) : 0.0;

			double prodProb = 0.0;
			if(true) // case when t == s+1
			{
				{	
					chart.setDemisionVal(chart_dim, s, s, 0, 0, 0);		// s->s comp
					chart.setDemisionVal(chart_dim2, s+1, t, 1, 0, 0);	// s+1<-t comp
					chart.getElement(chart_dim, chart_pos);
					chart.getElement(chart_dim2, chart_pos2);

					FeatureVec prodFV_sst;
					fvs_trips.setDemisionVal(trip_dim, s, s, t);
					fvs_trips.getElement(trip_dim, trip_pos);
					prodFV_sst.add( &fvs_trips.getElement(trip_pos) );
					double prodProb_sst = probs_trips.getElement(trip_pos);

					vector< pair<int, int> > pairs;
					getKBestPairs(chart_pos, chart_pos2, pairs);
					for(int k = 0; k < pairs.size(); k++)
					{
						if(pairs[k].first == -1 || pairs[k].second == -1) break;
						int comp1 = pairs[k].first;
						int comp2 = pairs[k].second;
						double bc = chart.getElement(chart_pos+comp1).prob + chart.getElement(chart_pos2+comp2).prob;
						// create sibling pair
						// create parent pair: s->t and s->(start,t)
						bc += prodProb_st + prodProb_sst;

						FeatureVec fv_fin = prodFV_sst;
						fv_fin.add(&prodFV_st);

						if(isLabeled) {
							bc += nt_prob_s_01 + nt_prob_t_00;
							fv_fin.add(&nt_fv_s_01).add(&nt_fv_t_00);
						}
						add(s,s,t,type1,0,1,bc,fv_fin, &chart.getElement(chart_pos+comp1),  &chart.getElement(chart_pos2+comp2));
					}
				}
				{	
					chart.setDemisionVal(chart_dim, s, t-1, 0, 0, 0);
					chart.setDemisionVal(chart_dim2, t, t, 1, 0, 0);
					chart.getElement(chart_dim, chart_pos);
					chart.getElement(chart_dim2, chart_pos2);

					FeatureVec prodFV_tts;
					fvs_trips.setDemisionVal(trip_dim, t, t, s);
					fvs_trips.getElement(trip_dim, trip_pos);
					prodFV_tts.add( &fvs_trips.getElement(trip_pos) );
					double prodProb_tts = probs_trips.getElement(trip_pos);

					vector< pair<int, int> > pairs;
					getKBestPairs(chart_pos, chart_pos2, pairs);
					int k = 0;
					for(; k < pairs.size(); k++)
					{
						if(pairs[k].first == -1 || pairs[k].second == -1) break;
						int comp1 = pairs[k].first;
						int comp2 = pairs[k].second;
						double bc = chart.getElement(chart_pos+comp1).prob + chart.getElement(chart_pos2+comp2).prob;
						// create sibling pair
						// create parent pair: t->s and t->(start,s)
						bc += prodProb_ts + prodProb_tts;

						FeatureVec fv_fin = prodFV_tts;
						fv_fin.add(&prodFV_ts);

						if(isLabeled) {
							bc += nt_prob_t_11 + nt_prob_s_10;
							fv_fin.add(&nt_fv_t_11).add(&nt_fv_s_10);
						}
						add(s,t,t,type2,1,1,bc,fv_fin,&chart.getElement(chart_pos+comp1),  &chart.getElement(chart_pos2+comp2));
					}
				} // end case when r == t
			} // end if(true)

			
			int r;
			for(r = s; r < t; r++) 	// First case - create sibling
			{
				chart.setDemisionVal(chart_dim, s, r, 0, 0, 0);		// -> complete
				chart.setDemisionVal(chart_dim2, r+1, t, 1, 0, 0);	// <- complete
				chart.getElement(chart_dim, chart_pos);
				chart.getElement(chart_dim2, chart_pos2);

				vector< pair<int, int> > pairs;
				getKBestPairs(chart_pos, chart_pos2, pairs);
				int k = 0;
				for(; k < pairs.size(); k++)
				{
					if(pairs[k].first == -1 || pairs[k].second == -1) break;
					int comp1 = pairs[k].first;
					int comp2 = pairs[k].second;
					double bc = chart.getElement(chart_pos+comp1).prob + chart.getElement(chart_pos2+comp2).prob;
					// create sibling pair
					// create parent pair: t->s and t->(start,s)
					add(s,r,t,-1,0,2,bc, FeatureVec(), &chart.getElement(chart_pos+comp1),  &chart.getElement(chart_pos2+comp2));
					add(s,r,t,-1,1,2,bc, FeatureVec(), &chart.getElement(chart_pos+comp1),  &chart.getElement(chart_pos2+comp2));
				}
			} // end for(r = s; r < t; r++)

			for(r = s+1; r < t; r++) {
				if (s != 0) // special case: NO multi-ROOT in one sent.
				{ // s -> (r,t)
					chart.setDemisionVal(chart_dim, s, r, 0, 1, 0);		// s->r incomplete
					chart.setDemisionVal(chart_dim2, r, t, 0, 2, 0);	// r-t-sib
					chart.getElement(chart_dim, chart_pos);
					chart.getElement(chart_dim2, chart_pos2);
					vector< pair<int, int> > pairs;
					getKBestPairs(chart_pos, chart_pos2, pairs);
					int k = 0;
					for(; k < pairs.size(); k++)
					{
						if(pairs[k].first == -1 || pairs[k].second == -1) break;
						int comp1 = pairs[k].first;
						int comp2 = pairs[k].second;
						double bc = chart.getElement(chart_pos+comp1).prob + chart.getElement(chart_pos2+comp2).prob;

						fvs_trips.setDemisionVal(trip_dim, s, r, t);
						fvs_trips.getElement(trip_dim, trip_pos);
						bc += prodProb_st + probs_trips.getElement(trip_pos);

						FeatureVec fv_fin;
						fv_fin.add(&prodFV_st);
						fv_fin.add( &fvs_trips.getElement(trip_pos) );
						if(isLabeled) 
						{
							bc += nt_prob_s_01 + nt_prob_t_00;
							fv_fin.add(&nt_fv_s_01).add(&nt_fv_t_00);
						}

						add(s,r,t,type1,0,1,bc,fv_fin, &chart.getElement(chart_pos+comp1),  &chart.getElement(chart_pos2+comp2));
					}
				} // end // s -> (r,t)
				{ // t -> (r,s)
					chart.setDemisionVal(chart_dim, s, r, 1, 2, 0);		// s-r-sib
					chart.setDemisionVal(chart_dim2, r, t, 1, 1, 0);	// r<-t incomplete
					chart.getElement(chart_dim, chart_pos);
					chart.getElement(chart_dim2, chart_pos2);
					vector< pair<int, int> > pairs;
					getKBestPairs(chart_pos, chart_pos2, pairs);
					int k = 0;
					for(; k < pairs.size(); k++)
					{
						if(pairs[k].first == -1 || pairs[k].second == -1) break;
						int comp1 = pairs[k].first;
						int comp2 = pairs[k].second;
						double bc = chart.getElement(chart_pos+comp1).prob + chart.getElement(chart_pos2+comp2).prob;

						fvs_trips.setDemisionVal(trip_dim, t, r, s);
						fvs_trips.getElement(trip_dim, trip_pos);
						bc += prodProb_ts + probs_trips.getElement(trip_pos);

						FeatureVec fv_fin;
						fv_fin.add(&prodFV_ts);
						fv_fin.add( &fvs_trips.getElement(trip_pos) );

						if(isLabeled)
						{
							bc += nt_prob_t_11+nt_prob_s_10;
							fv_fin.add(&nt_fv_t_11).add(&nt_fv_s_10);
						}
						add(s,r,t,type2,1,1,bc,fv_fin, &chart.getElement(chart_pos+comp1),  &chart.getElement(chart_pos2+comp2));
					}
				} // end // t -> (r,s)
			} // end for(r = s+1; r < t; r++)

			// Finish off pieces incom + comp -> comp
			for(r = s; r <= t; r++)
			{
				if(r != s)
				{
					chart.setDemisionVal(chart_dim, s, r, 0, 1, 0);		// s->r incomplete
					chart.setDemisionVal(chart_dim2, r, t, 0, 0, 0);	// r->t complete
					chart.getElement(chart_dim, chart_pos);
					chart.getElement(chart_dim2, chart_pos2);
					vector< pair<int, int> > pairs;
					getKBestPairs(chart_pos, chart_pos2, pairs);

					FeatureVec fv_fin;
					fv_fin.add(&prodFV_st);
					fv_fin.add( &fvs_trips.getElement(trip_pos) );

					int k = 0;
					for(; k < pairs.size(); k++)
					{
						if(pairs[k].first == -1 || pairs[k].second == -1) break;
						int comp1 = pairs[k].first;
						int comp2 = pairs[k].second;
						double bc = chart.getElement(chart_pos+comp1).prob + chart.getElement(chart_pos2+comp2).prob;

						// search the parent of t
						int par_t = -1;
						if(r == t)
						{
							par_t = s;
						}
						else
						{
							ParseForestItem* right_child = &chart.getElement(chart_pos2+comp2);
							while(right_child)
							{
								if(right_child->r != right_child->t)
								{
									right_child = right_child->right;
								}
								else
								{
									par_t = right_child->s;
									break;
								}
							}
						}

						if(par_t == -1)
						{
							cout << "error occurs, the root of t for right-arc complete is not found!\n";
							return;
						}

						fvs_trips.setDemisionVal(trip_dim, par_t, t, t);
						fvs_trips.getElement(trip_dim, trip_pos);
						bc += probs_trips.getElement(trip_pos);
						FeatureVec fv_fin;
						fv_fin.add( &fvs_trips.getElement(trip_pos) );

						add(s,r,t,-1,0,0,bc, fv_fin, &chart.getElement(chart_pos+comp1), &chart.getElement(chart_pos2+comp2));
					}
				} // end if(r != s)

				if(r != t)
				{
					chart.setDemisionVal(chart_dim, s, r, 1, 0, 0);		// s<-r complete
					chart.setDemisionVal(chart_dim2, r, t, 1, 1, 0);	// r<-t incomplete
					chart.getElement(chart_dim, chart_pos);
					chart.getElement(chart_dim2, chart_pos2);
					vector< pair<int, int> > pairs;
					getKBestPairs(chart_pos, chart_pos2, pairs);
					int k = 0;
					for(; k < pairs.size(); k++)
					{
						if(pairs[k].first == -1 || pairs[k].second == -1) break;
						int comp1 = pairs[k].first;
						int comp2 = pairs[k].second;
						double bc = chart.getElement(chart_pos+comp1).prob + chart.getElement(chart_pos2+comp2).prob;
						
						// search the parent of t
						int par_s = -1;
						if(r == s)
						{
							par_s = t;
						}
						else
						{
							ParseForestItem* left_child = &chart.getElement(chart_pos+comp1);
							while(left_child)
							{
								if(left_child->r != left_child->s)
								{
									left_child = left_child->left;
								}
								else
								{
									par_s = left_child->t;
									break;
								}
							}
						}

						if(par_s == -1)
						{
							cout << "error occurs, the root of s for left-arc complete is not found!\n";
							return;
						}

						fvs_trips.setDemisionVal(trip_dim, par_s, s, s);
						fvs_trips.getElement(trip_dim, trip_pos);
						bc += probs_trips.getElement(trip_pos);
						FeatureVec fv_fin;
						fv_fin.add( &fvs_trips.getElement(trip_pos) );

						add(s,r,t,-1,1,0,bc, fv_fin, &chart.getElement(chart_pos+comp1),  &chart.getElement(chart_pos2+comp2));
					}
				} // end if(r != t)
			} // end for(r = s; r <= t; r++) {
		} // end for(; s < inst->size() && s+j < inst->size(); s++)
	} // end for(; j < inst->size(); j++)

}




// returns pairs of indices and -1,-1 if < K pairs
void KBestParseForest::getKBestPairs(unsigned int chart_pos, unsigned int chart_pos2, vector< pair<int, int> > &pairs)
{
	// in this case K = items1.length
	vector< vector<bool> > beenPushed(K);
	pairs.resize(K);
	int i = 0;
	for(; i < K; i++) {
		beenPushed[i].resize(K, false);
		pairs[i].first = -1;
		pairs[i].second = -1;
	}
	BinaryHeap heap(K+1);

	if (!chart.getElement(chart_pos).m_isInit || chart.getElement(chart_pos).prob < DOUBLE_NEGATIVE_INFINITY + EPS) return;
	if (!chart.getElement(chart_pos2).m_isInit || chart.getElement(chart_pos2).prob < DOUBLE_NEGATIVE_INFINITY + EPS) return;
	ValueIndexPair vip(chart.getElement(chart_pos).prob + chart.getElement(chart_pos2).prob, 0, 0);

	heap.add(vip);
	beenPushed[0][0] = true;

	int n = 0;
	while(!heap.empty()) {
		heap.removeMax(vip);

//		if(vip.val < DOUBLE_NEGATIVE_INFINITY + EPS) break;

		pairs[n].first = vip.i1;
		pairs[n].second = vip.i2;

		n++;
		if(n >= K) break;

		if(vip.i1+1 < K && !beenPushed[vip.i1 + 1][vip.i2]) {
			if (chart.getElement(chart_pos + vip.i1 + 1).m_isInit && chart.getElement(chart_pos + vip.i1 + 1).prob > DOUBLE_NEGATIVE_INFINITY+EPS) {
				heap.add( ValueIndexPair( chart.getElement(chart_pos + vip.i1 + 1).prob + chart.getElement(chart_pos2 + vip.i2).prob, vip.i1 + 1, vip.i2 ) );
				beenPushed[vip.i1 + 1][vip.i2] = true;
			}
		}
		if(vip.i2+1 < K && !beenPushed[vip.i1][vip.i2+1]) {
			if (chart.getElement(chart_pos2 + vip.i2 + 1).m_isInit && chart.getElement(chart_pos2 + vip.i2 + 1).prob > DOUBLE_NEGATIVE_INFINITY+EPS) {
				heap.add( ValueIndexPair( chart.getElement(chart_pos + vip.i1).prob + chart.getElement(chart_pos2 + vip.i2 + 1).prob, vip.i1, vip.i2 + 1 ) );
				beenPushed[vip.i1][vip.i2 + 1] = true;
			}
		}
	}

}

