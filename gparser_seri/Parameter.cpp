#include "Parameter.h"
#include "MyLib.h"
#include "MultiArray.h"


void Parameter::updateParamsMIRA(DepInstance *pInstance, vector<FeatureVec> &d0, vector<string> &d1, double curUpdSeq)
{
	const string &actParseTree = pInstance->actParseTree;
	FeatureVec &actFV = pInstance->fv;

	int K = 0;
	int i = 0;
	for(; i < d0.size() && i < d1.size() && !d1[i].empty(); i++) {
		K = i+1;
	}

	vector<double> b(K, 0.0);
	vector<double> lam_dist(K, 0.0);
	vector<FeatureVec> dist(K);
	int k = 0;
	int _K = K;
	for(; k < K; k++) {
		if (d1[k].empty()) {
			_K = k;
			break;
		}
		lam_dist[k] = getScore(actFV) - getScore(d0[k]);
		b[k] = (double)numErrors(pInstance, d1[k], actParseTree);

/*		cout << "score dist: " << lam_dist[k] << endl;
		cout << "err num: " << b[k] << endl;
		cout << "sys parse: " << d1[k] << endl;
		cout << "gold parse: " << actParseTree << endl;
		vector<int> vecKeys;
		actFV.getKeys(vecKeys);
		cout << "gold parse feat num: " << vecKeys.size() << endl;
		d0[k].getKeys(vecKeys);
		cout << "sys parse feat num: " << vecKeys.size() << endl;
*/
		b[k] -= lam_dist[k];
		dist[k].add(&actFV);
		dist[k].remove(&d0[k]);
    dist[k].collectFeatures();
//		dist[k].collectFeatures();

/*		vector<int> vecKeys;
		dist[k].getKeys(vecKeys);
		cout << "dist vec key num: " << vecKeys.size() << endl;
		d0[k].getKeys(vecKeys);
		cout << "d0[k] vec key num: " << vecKeys.size() << endl;
		actFV.getKeys(vecKeys);
		cout << "gold tree vec key num: " << vecKeys.size() << endl;
*/	}

	vector<double> alpha;
//	cout << "(";
	hildreth(dist, b, alpha, _K);
//	cout << ")";

	int res = 0;
	for(k = 0; k < _K; k++) {
		//		for(k = 0; k < K; k++) {
//		cout << "[" << alpha[k] << "]" << endl;
		//dist[k].update(m_parameters, m_total, alpha[k], upd);
		dist[k].update(m_parameters, m_total, m_variance, alpha[k], m_upd, curUpdSeq);
//		cout << "\n-----\n" << endl;
	}
}

void Parameter::hildreth(const vector<FeatureVec> &a, const vector<double> &b, vector<double> &alpha, int K)
{
	
	int max_iter = 10000;
	alpha.resize(K);
	vector<double> F(K, 0.0);
	vector<double> kkt(K, 0.0);
	double max_kkt = DOUBLE_NEGATIVE_INFINITY;

	vector<unsigned int> A_dim;
	unsigned int A_pos;
	MultiArray<double> A;
	A.setDemisionVal(A_dim, K, K);
	A.resize(A_dim, 0.0);

	vector<bool> is_computed(K, false);

	int i;
	for(i = 0; i < K; i++) {
		A.setDemisionVal(A_dim, i, i);
		A.getElement(A_dim, A_pos) = FeatureVec::dotProduct(a[i], a[i]);
	}

	int max_kkt_i = -1;

	for(i = 0; i < F.size(); i++) {
		F[i] = b[i];
		kkt[i] = b[i];
		if(kkt[i] > max_kkt) { max_kkt = kkt[i]; max_kkt_i = i; }
	}

	int iter = 0;
	double diff_alpha;
	double try_alpha;
	double add_alpha;

	while(max_kkt >= EPS && iter < max_iter) {
//		cout << ".";

		A.setDemisionVal(A_dim, max_kkt_i, max_kkt_i);
		A.getElement(A_dim, A_pos);

		diff_alpha = A.getElement(A_pos) <= ZERO ? 0.0 : F[max_kkt_i]/A.getElement(A_pos);
		try_alpha = alpha[max_kkt_i] + diff_alpha;
		add_alpha = 0.0;

		if(try_alpha < 0.0)
			add_alpha = -1.0 * alpha[max_kkt_i];
		else
			add_alpha = diff_alpha;

		alpha[max_kkt_i] = alpha[max_kkt_i] + add_alpha;

		if (!is_computed[max_kkt_i]) {
			for(i = 0; i < K; i++) {
				A.setDemisionVal(A_dim, i, max_kkt_i);
				A.getElement(A_dim, A_pos) = FeatureVec::dotProduct(a[max_kkt_i], a[i]); // for version 1
				is_computed[max_kkt_i] = true;
			}
		}

		for(i = 0; i < F.size(); i++) {
			A.setDemisionVal(A_dim, i, max_kkt_i);
			F[i] -= add_alpha * A.getElement(A_dim, A_pos);
			kkt[i] = F[i];
			if(alpha[i] > ZERO)
				kkt[i] = abs(F[i]);
		}

		max_kkt = DOUBLE_NEGATIVE_INFINITY;
		max_kkt_i = -1;
		for(i = 0; i < F.size(); i++) {
			if(kkt[i] > max_kkt) { max_kkt = kkt[i]; max_kkt_i = i; }
		}
		iter++;
	}
}

void Parameter::evaluateArc(DepInstance *pInstance, const string &pred, const string &act, int& correct, int& total)
{
	vector<string> act_spans;
	split_bychar(act, act_spans, ' ');
	vector<string> pred_spans;
	split_bychar(pred, pred_spans, ' ');
	correct = 0;
	total = 0;
	int i = 0;
	for(; i < pred_spans.size() && i < act_spans.size(); i++) {
		vector<string> vec;
		vector<string> vec2;
		split_bychar(pred_spans[i], vec, ':');
		split_bychar(act_spans[i], vec2, ':');

		if (vec.empty() || vec2.empty()) {
			cout << "span format err: " << pred_spans[i] << " : " << act_spans[i] << endl;
			continue;
		}

		if(vec[0] == vec2[0]) {
			correct++;
		}
		total++;
	}

}

void Parameter::evaluateLabel(DepInstance *pInstance, const string &pred, const string &act, int& correct, int& total)
{
	vector<string> act_spans;
	split_bychar(act, act_spans, ' ');
	vector<string> pred_spans;
	split_bychar(pred, pred_spans, ' ');
/*	cout << endl;
	copy(pred_spans.begin(), pred_spans.end(), ostream_iterator<string>(cout, "#"));
	cout << endl;
	copy(act_spans.begin(), act_spans.end(), ostream_iterator<string>(cout, "#"));
	cout << endl;
*/
	correct = 0;
  total = 0;
	int i = 0;
	for(; i < pred_spans.size() && i < act_spans.size(); i++) {
		vector<string> vec;
		vector<string> vec2;
		split_bychar(pred_spans[i], vec, ':');
		split_bychar(act_spans[i], vec2, ':');

		if (vec.size() < 2 || vec2.size() < 2) {
			cout << "span format err: " << pred_spans[i] << " : " << act_spans[i] << endl;
			continue;
		}

    if (vec[0] == vec2[0]) {
      ++correct;
      if(vec[1] == vec2[1]) {
        ++correct;
      }
		}
		++total;
		++total;
	}
}

void Parameter::evaluateArcNoPunc(DepInstance *pInstance, const string &pred, const string &act, int& correct, int& total)
{
	vector<string> act_spans;
	split_bychar(act, act_spans, ' ');
	vector<string> pred_spans;
	split_bychar(pred, pred_spans, ' ');

	const vector<string> &cpostags = pInstance->cpostags;

	correct = 0;
  total = 0;
	for(int i = 0; i < pred_spans.size(); i++) {
		vector<string> vec;
		vector<string> vec2;
		split_bychar(pred_spans[i], vec, ':');
		split_bychar(act_spans[i], vec2, ':');

		if (vec.empty() || vec2.empty()) {
			cout << "span format err: " << pred_spans[i] << " : " << act_spans[i] << endl;
			continue;
		}

		if(cpostags[i+1] == "PU") {
			continue;
		}

		if(vec[0] == vec2[0]) {
			correct++;
		}
		total++;
	}

}

void Parameter::evaluateLabelNoPunc(DepInstance *pInstance, const string &pred, const string &act, int& correct, int& total)
{
	vector<string> act_spans;
	split_bychar(act, act_spans, ' ');
	vector<string> pred_spans;
	split_bychar(pred, pred_spans, ' ');

	const vector<string> &cpostags = pInstance->cpostags;

  correct = 0;
  total = 0;
	for(int i = 0; i < pred_spans.size(); i++) {
		vector<string> vec;
		vector<string> vec2;
		split_bychar(pred_spans[i], vec, ':');
		split_bychar(act_spans[i], vec2, ':');

		if (vec.size() < 2 || vec2.size() < 2) {
			cout << "span format err: " << pred_spans[i] << " : " << act_spans[i] << endl;
			continue;
		}

		if(cpostags[i+1] == "PU") {
			continue;
		}

		if (vec[0] == vec2[0] && vec[1] == vec2[1]) {
			++correct;
		}
		++total;
	}
}

void Parameter::isRootCorrect(DepInstance *pInstance, const string &pred, const string &act, int& correct)
{
  vector<string> act_spans;
	split_bychar(act, act_spans, ' ');
	vector<string> pred_spans;
	split_bychar(pred, pred_spans, ' ');
	correct = 0;
	int i = 0;
	for(; i < pred_spans.size() && i < act_spans.size(); i++) {
		vector<string> vec;
		vector<string> vec2;
		split_bychar(pred_spans[i], vec, ':');
		split_bychar(act_spans[i], vec2, ':');

		if (vec.empty() || vec2.empty()) {
			cout << "span format err: " << pred_spans[i] << " : " << act_spans[i] << endl;
			continue;
		}

    vector<string> vecsp;
		vector<string> vecsp2;
    split_bychar(vec[0], vecsp, '|');
    split_bychar(vec2[0], vecsp2, '|');
		if(vec[0] == vec2[0] && vecsp[0] == "0") {
			correct=1;
			break;
		}
	}
}


void Parameter::updateParamsPerceptron(DepInstance *pInstance, vector<FeatureVec> &d0, vector<string> &d1, double curUpdSeq)
{
  const string &actParseTree = pInstance->actParseTree;
	FeatureVec &actFV = pInstance->fv;

 // cout << "7" << endl;
	int K = 0;
	int i = 0;
	for(; i < d0.size() && i < d1.size() && !d1[i].empty(); i++) {
		K = i+1;
	}
	//cout << "8" << endl;
  if(K==0){cout << "No result has getten here" << endl; return;}
  K=1;
	vector<double> b(K, 0.0);
	vector<double> lam_dist(K, 0.0);
	vector<FeatureVec> dist(K);
	int k = 0;
	int _K = K;
	for(; k < K; k++) {
		if (d1[k].empty()) {
			_K = k;
			break;
		}
		lam_dist[k] = getScore(actFV) - getScore(d0[k]);

		b[k] = (double)numErrors(pInstance, d1[k], actParseTree);

/*		cout << "score dist: " << lam_dist[k] << endl;
		cout << "err num: " << b[k] << endl;
		cout << "sys parse: " << d1[k] << endl;
		cout << "gold parse: " << actParseTree << endl;
		vector<int> vecKeys;
		actFV.getKeys(vecKeys);
		cout << "gold parse feat num: " << vecKeys.size() << endl;
		d0[k].getKeys(vecKeys);
		cout << "sys parse feat num: " << vecKeys.size() << endl;
*/
		b[k] -= lam_dist[k];
		dist[k].add(&actFV);
		dist[k].remove(&d0[k]);
		dist[k].collectFeatures();

/*		vector<int> vecKeys;
		dist[k].getKeys(vecKeys);
		cout << "dist vec key num: " << vecKeys.size() << endl;
		d0[k].getKeys(vecKeys);
		cout << "d0[k] vec key num: " << vecKeys.size() << endl;
		actFV.getKeys(vecKeys);
		cout << "gold tree vec key num: " << vecKeys.size() << endl;
*/	}

//	vector<double> alpha;
//	cout << "(";
//	hildreth(dist, b, alpha, _K);
//	cout << ")";
//cout << "9" << endl;
	int res = 0;
	//cout << "_K: " << _K << endl;
	for(k = 0; k < _K; k++) {
		//		for(k = 0; k < K; k++) {
//		cout << "[" << alpha[k] << "]" << endl;
    //cout << "k: " << k << endl;
    //cout << "m_parameters.size: " << m_parameters.size() << endl;
    //cout << "m_total.size: " << m_total.size() << endl;
    //cout << "m_variance.size: " << m_variance.size() << endl;
    //cout << "m_upd.size: " << m_upd.size() << endl;
    //cout << "curUpdSeq: " << curUpdSeq << endl;
		dist[k].update(m_parameters, m_total, m_variance, 1.0, m_upd, curUpdSeq);
		//cout << "updated" << endl;
//		cout << "\n-----\n" << endl;
	}


	//cout << "12" << endl;
}

void Parameter::startAdding(double ival)
{
  for(int i = 0; i < m_parameters.size(); i++)
  {
    m_total[i] = m_parameters[i];
    m_variance[i] = m_parameters[i] * m_parameters[i];
    m_upd[i] = ival;
  }
}

void Parameter::finishAdding(double ival)
{
  for(int i = 0; i < m_parameters.size(); i++)
  {
    m_total[i] += m_parameters[i] * (ival -  m_upd[i] -1);
    m_variance[i] += m_parameters[i] * m_parameters[i] * (ival -  m_upd[i] -1);
    m_upd[i] = ival;
  }
}


//void Parameter::computeFeatureCount(DepInstance *pInstance, vector<FeatureVec> &d0, vector<string> &d1)
//{
//  const string &actParseTree = pInstance->actParseTree;
//	FeatureVec &actFV = pInstance->fv;
//  vector<double> decodeWeights;
//
// // cout << "7" << endl;
//	int K = 0;
//	int i = 0;
//	double normalizeValue = 0.0;
//	for(; i < d0.size() && i < d1.size() && !d1[i].empty(); i++) {
//	  double curweight  = getScore(d0[i]);
//	  decodeWeights.push_back(curweight);
//	  normalizeValue += curweight * curweight;
//
//		K = i+1;
//	}
//	//cout << "8" << endl;
//  if(K==0){cout << "No result has getten here" << endl; return;}
//
//  normalizeValue = sqrt(normalizeValue);
//  for(int j = 0; j < K; j++)
//  {
//     decodeWeights[j] /= normalizeValue;
//  }
//  normalizeValue = 0.0;
//  for(int j = 0; j < K; j++)
//  {
//    double curweight = decodeWeights[j];
//    decodeWeights[j] = exp(curweight);
// 	  normalizeValue += decodeWeights[j];
//
//	  cout << curweight << "\t" << decodeWeights[j] << "\t" << normalizeValue << endl;
//  }
//
//
//	vector<FeatureVec> dist(K);
//	int k = 0;
//	int _K = K;
//	for(; k < K; k++) {
//		if (d1[k].empty()) {
//			_K = k;
//			break;
//		}
//
//		dist[k].add(&actFV);
//		dist[k].remove(&d0[k]);
//    dist[k].collectFeatures();
//   // cout << decodeWeights[k]/normalizeValue << endl;
////    dist[k].addFeatureCount(m_posCount, m_negCount, m_invalidFeatures, decodeWeights[k]/normalizeValue);
//
// /*		vector<int> vecKeys;
//		dist[k].getKeys(vecKeys);
//		cout << "dist vec key num: " << vecKeys.size() << endl;
//		d0[k].getKeys(vecKeys);
//		cout << "d0[k] vec key num: " << vecKeys.size() << endl;
//		actFV.getKeys(vecKeys);
//		cout << "gold tree vec key num: " << vecKeys.size() << endl;
// */	}
//
//
//	//cout << "12" << endl;
//}

//void Parameter::normalizePosNeg()
//{
//  double posSum = 0.0;
//  for(int i = 0; i < m_posCount.size(); i++)
//  {
//    if(m_invalidFeatures.find(i) != m_invalidFeatures.end())
//    {
//      continue;
//    }
//    posSum += m_posCount[i];
//  }
//
//  for(int i = 0; i < m_posCount.size(); i++)
//  {
//    if(m_invalidFeatures.find(i) != m_invalidFeatures.end())
//    {
//      continue;
//    }
//    m_posCount[i] = m_posCount[i] ;
//  }
//
//  double negSum = 0.0;
//  for(int i = 0; i < m_negCount.size(); i++)
//  {
//    if(m_invalidFeatures.find(i) != m_invalidFeatures.end())
//    {
//      continue;
//    }
//    negSum += m_negCount[i];
//  }
//
//  for(int i = 0; i < m_negCount.size(); i++)
//  {
//    if(m_invalidFeatures.find(i) != m_invalidFeatures.end())
//    {
//      continue;
//    }
//    m_negCount[i] = m_negCount[i] ;
//  }
//
//}

void Parameter::selectInvalidFeatures(int reduCount, double ratio, int fsmethod)
{
  m_invalidFeatures.clear();
  //set<FeatureWeight, FeatureWeightComp> featureSet;

  for(int i = 0; i < m_parameters.size(); i++)
  {
    double svariance = m_variance[i] - m_parameters[i] * m_total[i];
    if(svariance < -ZERO)
    {
      cout << "Error occured in computation of featureScore" << endl;
      continue;
    }

    svariance = svariance + 1.0;


    double featureScore = (m_parameters[i] *  m_parameters[i]) / svariance;

    if(fsmethod == 1)
    {
      featureScore = m_variance[i];
    }

    if(fsmethod == 2)
    {
      featureScore = abs(m_parameters[i]);
    }


    FeatureWeight tmp;
    tmp.feature = i;
    tmp.feature_score =  featureScore;
    if(tmp.feature_score < ratio + ZERO)
    {
      m_invalidFeatures.insert(tmp.feature);
    }
    else
    {
      //featureSet.insert(tmp);
    }

  }

  //int reductsize = (int) featureSet.size() * ratio;

/*
  for(int i = 0; i < reductsize ; i++)
  {
    FeatureWeight tmp = *(featureSet.begin());
    if(i == 0)
    {
      cout << "Smallest excluded feature score in round " << reduCount << ":" << tmp.feature_score << endl;
    }

    if(i == reductsize-1)
    {
      cout << "Largest excluded feature score in round " << reduCount << ":" << tmp.feature_score << endl;
    }

    m_invalidFeatures.insert(tmp.feature);
    featureSet.erase(featureSet.begin());
  }
  */
}


void Parameter::updateParamsKCRF(DepInstance *pInstance, vector<FeatureVec> &d0, vector<string> &d1, double curUpdSeq)
{
	const string &actParseTree = pInstance->actParseTree;
	FeatureVec &actFV = pInstance->fv;

	int K = 0;
	int i = 0;
	for(; i < d0.size() && i < d1.size() && !d1[i].empty(); i++) {
		K = i+1;
	}

	vector<double> b(K, 0.0);
	vector<double> lam_dist(K, 0.0);
	vector<FeatureVec> dist(K);
	int k = 0;
	int _K = K;
  double z_norm = 0.0;
	for(; k < K; k++) {
		if (d1[k].empty()) {
			_K = k;
			break;
		}
		lam_dist[k] = exp(getScore(d0[k]) - getScore(d0[0]));
    z_norm = z_norm + lam_dist[k];

		//b[k] = (double)numErrors(pInstance, d1[k], actParseTree);

/*		cout << "score dist: " << lam_dist[k] << endl;
		cout << "err num: " << b[k] << endl;
		cout << "sys parse: " << d1[k] << endl;
		cout << "gold parse: " << actParseTree << endl;
		vector<int> vecKeys;
		actFV.getKeys(vecKeys);
		cout << "gold parse feat num: " << vecKeys.size() << endl;
		d0[k].getKeys(vecKeys);
		cout << "sys parse feat num: " << vecKeys.size() << endl;
*/
		//b[k] -= lam_dist[k];
		dist[k].add(&actFV);
		dist[k].remove(&d0[k]);
    dist[k].collectFeatures();
//		dist[k].collectFeatures();

/*		vector<int> vecKeys;
		dist[k].getKeys(vecKeys);
		cout << "dist vec key num: " << vecKeys.size() << endl;
		d0[k].getKeys(vecKeys);
		cout << "d0[k] vec key num: " << vecKeys.size() << endl;
		actFV.getKeys(vecKeys);
		cout << "gold tree vec key num: " << vecKeys.size() << endl;
*/	}

	vector<double> alpha(K, 0.0);
  for(k = 0; k < _K; k++)
  {
    alpha[k] = lam_dist[k] / z_norm;
  }

//	cout << "(";
	//hildreth(dist, b, alpha, _K);
//	cout << ")";

	int res = 0;
	for(k = 0; k < _K; k++) {
		//		for(k = 0; k < K; k++) {
//		cout << "[" << alpha[k] << "]" << endl;
		//dist[k].update(m_parameters, m_total, alpha[k], upd);
		dist[k].update(m_parameters, m_total, m_variance, alpha[k], m_upd, curUpdSeq);
//		cout << "\n-----\n" << endl;
	}
}


