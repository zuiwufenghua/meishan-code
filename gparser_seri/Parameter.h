#ifndef _PARAMETER_
#define _PARAMETER_

#pragma once
#include <vector>
#include <string>
#include <set>
#include <math.h>
using namespace std;

#include "DepInstance.h"
#include "FeatureVec.h"
#include "MyVector.h"
#include "ParserOptions.h"
#include "common.h"


class Parameter
{
public:
	Parameter(int size, const ParserOptions &_options) : options(_options) {
		m_parameters.resize(0);
		m_parameters.resize(size, 0.0);
		m_total.resize(0);
		m_total.resize(size, 0.0);
		m_lossType = "punc";
		m_SCORE = 0.0;

		m_variance.resize(0);
		m_variance.resize(size, 0.0);
		m_upd.resize(0);
		m_upd.resize(size, 0.0);
//		m_means.resize(0);
//		m_means.resize(size, 0.0);
		m_invalidFeatures.clear();

		m_posCount.resize(0);
		m_posCount.resize(size, 0.0);
		m_negCount.resize(0);
		m_negCount.resize(size, 0.0);

//		m_isCacluateVariance = false;
	}

	~Parameter(void) {}

	void ReInit(int size)
	{
	  m_parameters.resize(0);
		m_parameters.resize(size, 0.0);
		m_total.resize(0);
		m_total.resize(size, 0.0);
		m_lossType = "punc";
		m_SCORE = 0.0;
		m_variance.resize(0);
		m_variance.resize(size, 0.0);
		m_upd.resize(0);
		m_upd.resize(size, 0.0);
//		m_means.resize(0);
//		m_means.resize(size, 0.0);
		m_invalidFeatures.clear();

		m_posCount.resize(0);
		m_posCount.resize(size, 0.0);
		m_negCount.resize(0);
		m_negCount.resize(size, 0.0);

//		m_isCacluateVariance = false;
	}

	void setLoss(const string &it) {
		m_lossType = it;
	}

	void setParams(const MyVector<double> &parameters) {
		m_parameters.resize(parameters.size(), 0.0);
		copy(parameters.begin(), parameters.begin()+parameters.size(), m_parameters.begin());
		m_total.resize(0);
		m_total.resize(parameters.size(), 0.0);
		m_lossType = "punc";
		m_SCORE = 0.0;
		m_variance.resize(0);
		m_variance.resize(parameters.size(), 0.0);
		m_upd.resize(0);
		m_upd.resize(parameters.size(), 0.0);
//		m_means.resize(0);
//		m_means.resize(parameters.size(), 0.0);
		m_invalidFeatures.clear();

		m_posCount.resize(0);
		m_posCount.resize(parameters.size(), 0.0);
		m_negCount.resize(0);
		m_negCount.resize(parameters.size(), 0.0);

//		m_isCacluateVariance = false;
	}

	void averageParams(double avVal) {
		int j = 0;
		for (; j < m_total.size(); ++j) {
//			m_total[j] *= 1.0/avVal;
			m_parameters[j] = m_total[j] / avVal;
		}
	}


	void storeParamsToTmp() {
		m_vecTmpParameters.resize(m_parameters.size());
		copy(m_parameters.begin(), m_parameters.end(), m_vecTmpParameters.begin());
	}

	void restoreParamsFromTmp() {
		m_parameters.resize(m_vecTmpParameters.size());
		copy(m_vecTmpParameters.begin(), m_vecTmpParameters.end(), m_parameters.begin());
	}

	void updateParamsMIRA(DepInstance *pInstance, vector<FeatureVec> &d0, vector<string> &d1, double curUpdSeq);
  void updateParamsKCRF(DepInstance *pInstance, vector<FeatureVec> &d0, vector<string> &d1, double curUpdSeq);

	double getScore(const FeatureVec &fv) const{
		return fv.getScore(m_parameters);
	}

	void hildreth(const vector<FeatureVec> &a, const vector<double> &b, vector<double> &alpha, int K);

	int numErrors(DepInstance *pInstance, const string &pred, const string &act)
	{
	  int correct = 0;
	  int total = 0;
		if(m_lossType == "nopunc") {
			if (options.m_isLabeled) {
				//return numErrorsLabelNoPunc(pInstance, pred, act);
				evaluateLabelNoPunc(pInstance, pred, act, correct, total);
			} else {
				//return numErrorsArcNoPunc(pInstance, pred, act);
				evaluateArcNoPunc(pInstance, pred, act, correct, total);
			}
		} else {
			if (options.m_isLabeled) {
				evaluateLabel(pInstance, pred, act, correct, total);
			} else {
				evaluateArc(pInstance, pred, act, correct, total);
			}
		}

		return total-correct;
	}

  void isRootCorrect(DepInstance *pInstance, const string &pred, const string &act, int& correct);
  void evaluateLabelNoPunc(DepInstance *pInstance, const string &pred, const string &act, int& correct, int& total);
  void evaluateArcNoPunc(DepInstance *pInstance, const string &pred, const string &act, int& correct, int& total);
  void evaluateLabel(DepInstance *pInstance, const string &pred, const string &act, int& correct, int& total);
  void evaluateArc(DepInstance *pInstance, const string &pred, const string &act, int& correct, int& total);

	void show() {
		int interval = m_parameters.size() / 50;
		cout << endl;
		int i = 0;
		for (; i < m_parameters.size(); i += interval) {
			cout << "(" << i << " " << m_parameters[i] << " " << m_total[i] << ")\t";
			if (i % 10 >= 0) cout << endl;
		}
/*		cout << endl;
		int i = 1;
		for (i; i < m_parameters.size() && i <= 10; ++i) {
			cout << "(" << i << " " << m_parameters[i] << " " << m_total[i] << ")\t";
			if (i % 10 >= 0) cout << endl;
		}
		cout << endl << endl;
		int len = m_parameters.size();
		for (i=1; len - i >= 0 && i <= 10; ++i) {
			cout << "(" << len-i << " " << m_parameters[len-i] << " " << m_total[len-i] << ")\t";
			if (i % 10 >= 0) cout << endl;
		}
		cout << endl;
*/	}
public:
	vector<double> m_parameters;
	vector<double> m_total;
	string m_lossType;
	double m_SCORE;


	vector<double> m_vecTmpParameters;
//	vector<double> m_vecBackParameters;

	const ParserOptions &options;

public:
	vector<double> m_variance;
	vector<double> m_posCount;
	vector<double> m_negCount;
	vector<double> m_upd;
//	bool m_isCacluateVariance;
//	vector<double> m_means;

	void updateParamsPerceptron(DepInstance *pInstance, vector<FeatureVec> &d0, vector<string> &d1, double curUpdSeq);
	void finishAdding(double ival);
	void startAdding(double ival);
	set<int> m_invalidFeatures;

//	void computeFeatureCount(DepInstance *pInstance, vector<FeatureVec> &d0, vector<string> &d1);
//	void normalizePosNeg();
	void selectInvalidFeatures(int reduCount, double ratio, int fsmethod);

/*
	void storeParamsToTmp2() {
		m_vecBackParameters.resize(m_parameters.size());
		copy(m_parameters.begin(), m_parameters.end(), m_vecBackParameters.begin());
	}

	void restoreParamsFromTmp2() {
		m_parameters.resize(m_vecBackParameters.size());
		copy(m_vecBackParameters.begin(), m_vecBackParameters.end(), m_parameters.begin());
	}


	void copyParamsMeans() {
		m_means.resize(m_parameters.size());
		copy(m_parameters.begin(), m_parameters.end(), m_means.begin());
	}
*/
};

typedef struct {
  int feature;
  double feature_score;
} FeatureWeight;

struct FeatureWeightComp {
  bool operator() (const FeatureWeight& lfw, const FeatureWeight& rfw)
  {
      return (lfw.feature_score <= rfw.feature_score);
  }
};

#endif

