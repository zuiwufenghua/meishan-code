#include "FeatureVec.h"
#include <cstdlib>

double FeatureVec::getScore(const vector<double> &parameters, bool negate) const
{
	double score = 0.0;
	int i = 0;
	for (; i < subfv.size(); ++i) {
		bool neg = negate ? !subfv[i].negate : subfv[i].negate;
		score += subfv[i].pFv->getScore(parameters, neg);
	}

	vector<Feature>::const_iterator it = m_fv.begin();
	int neg = negate ? -1 : 1;
	while (it != m_fv.end()) {
		if (it->index < 0 || it->index >= parameters.size()) {
			cout << "index err: " << it->index << endl;
			cout << "1" << endl;
			exit(0);
			continue;
		}
		score += neg * parameters[it->index] * it->value;
		++it;
	}
	return score;
}

void FeatureVec::addKeys2List(vector<int> &vecKeys) const
{
	int i = 0;
	for (; i < subfv.size(); ++i) {
		subfv[i].pFv->addKeys2List(vecKeys);
	}
	vector<Feature>::const_iterator it = m_fv.begin();
	while (it != m_fv.end()) {
		vecKeys.push_back(it->index);
		++it;
	}
}

void FeatureVec::addKeys2Set(set<int> &setKeys) const
{
	int i = 0;
	for (; i < subfv.size(); ++i) {
		subfv[i].pFv->addKeys2Set(setKeys);
	}
	vector<Feature>::const_iterator it = m_fv.begin();
	while (it != m_fv.end()) {
		setKeys.insert(it->index);
		++it;
	}
}

void FeatureVec::update(vector<double> &parameters, vector<double> &total,
			double alpha_k, double upd, bool negate) const
{
	int i = 0;
	for (; i < subfv.size(); ++i) {
		bool neg = negate ? !subfv[i].negate : subfv[i].negate;
		subfv[i].pFv->update(parameters, total, alpha_k, upd, neg);
	}

	vector<Feature>::const_iterator it = m_fv.begin();
	int neg = negate ? -1 : 1;
	int cnt = 0;
//	cout << "\n[" << neg << "]" << endl;
	while (it != m_fv.end()) {
		if (it->index < 0 || it->index >= parameters.size()) {
			cout << "index err: " << it->index << endl;
			cout << "2" << endl;
			exit(0);
			continue;
		}
/*		if (++cnt % 15 == 0) {
			cout << endl;
			//break;
		}
		cout << "(" << it->index << " " << parameters[it->index] << " | " << alpha_k * it->value << ")\t";
*/		parameters[it->index] += neg * alpha_k * it->value;
		total[it->index] += neg * upd * alpha_k * it->value;
		++it;
	}
//	cout << "\n***\n" << endl;;
}

double FeatureVec::dotProduct(const FeatureVec &fv1,const FeatureVec &fv2)
{
	map<int, double> map1;
	fv1.addFeaturesToMap(map1, false);
	map<int, double> map2;
	fv2.addFeaturesToMap(map2, false);

	double result = 0.0;
	map<int, double>::const_iterator it = map1.begin();
	while (it != map1.end()) {
		map<int, double>::const_iterator it2 = map2.find(it->first);
		if (it2 != map2.end()) {
			result += it->second * it2->second;
		}
		++it;
	}
	return result;
}

void FeatureVec::addFeaturesToMap(map<int, double> &mapFv, bool negate) const {
	int i = 0;
	for (; i < subfv.size(); ++i) {
		bool neg = negate ? !subfv[i].negate : subfv[i].negate;
		subfv[i].pFv->addFeaturesToMap(mapFv, neg);
	}

	vector<Feature>::const_iterator it = m_fv.begin();
	int neg = negate ? -1 : 1;
	while (it != m_fv.end()) {
		map<int, double>::iterator it_map = mapFv.find(it->index);
		if (it_map == mapFv.end()) {
			mapFv[it->index] = neg * it->value;
		} else {
			it_map->second += neg * it->value;
		}
		++it;
	}
}

//void FeatureVec::addFeatureCount(vector<double> &posCount, vector<double> &negCount, set<int> invalidFeatures, double alpha, bool negate)const
//{
//	int i = 0;
//	for (; i < subfv.size(); ++i) {
//		bool neg = negate ? !subfv[i].negate : subfv[i].negate;
//		subfv[i].pFv->update(posCount, negCount, alpha, negate);
//	}
//  //cout << "7" << endl;
//	vector<Feature>::const_iterator it = m_fv.begin();
//	int neg = negate ? -1 : 1;
//	int cnt = 0;
////	cout << "\n[" << neg << "]" << endl;
//	while (it != m_fv.end()) {
//		if (it->index < 0 || it->index >= posCount.size() || it->index >= negCount.size()) {
//			cout << "index err: " << it->index << endl;
//			continue;
//		}
//		if(invalidFeatures.find(it->index) != invalidFeatures.end())
//		{
//		  continue;
//		}
//
//    //cout << "8" << endl;
//    double curValue = neg * alpha * it->value;
//    if(curValue > 0)
//    {
//      posCount[it->index] += curValue;
//    }
//    else
//    {
//      negCount[it->index] += -curValue;
//    }
//
//		++it;
//		//cout << "9" << endl;
//	}
////	cout << "\n***\n" << endl;;
//}
//

void FeatureVec::update(vector<double> &parameters, vector<double> &total, vector<double> &variance,
                        double alpha_k, vector<double>& upd, double curUpdSeq, bool negate) const
{
	int i = 0;
	for (; i < subfv.size(); ++i) {
		bool neg = negate ? !subfv[i].negate : subfv[i].negate;
		subfv[i].pFv->update(parameters, total, variance, alpha_k, upd, curUpdSeq, neg);
	}
  //cout << "7" << endl;
	vector<Feature>::const_iterator it = m_fv.begin();
	int neg = negate ? -1 : 1;
	int cnt = 0;
//	cout << "\n[" << neg << "]" << endl;
	while (it != m_fv.end()) {
		if (it->index < 0 || it->index >= parameters.size()) {
			cout << "index err: " << it->index << endl;
			cout << "3" << endl;
			exit(0);
			continue;
		}

/*		if (++cnt % 15 == 0) {
			cout << endl;
			//break;
		}
		cout << "(" << it->index << " " << parameters[it->index] << " | " << alpha_k * it->value << ")\t";
*/
    //cout << "8" << endl;
    //cout << it->index << "\t" << upd[it->index] << endl;
		total[it->index] += (curUpdSeq - upd[it->index]-1) * parameters[it->index];
		variance[it->index] += (curUpdSeq - upd[it->index]-1) * parameters[it->index] * parameters[it->index];
		parameters[it->index] += neg * alpha_k * it->value;
		total[it->index] += parameters[it->index];
		variance[it->index] += parameters[it->index] * parameters[it->index];
		upd[it->index] = curUpdSeq;
		++it;
		//cout << "9" << endl;
	}
//	cout << "\n***\n" << endl;;
}


