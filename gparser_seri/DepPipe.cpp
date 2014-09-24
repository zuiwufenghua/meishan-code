#include "DepPipe.h"
#include "MyVector.h"
#include <iterator>
using namespace std;

#define MAX_BUFFER_SIZE 256

const string NO_FORM = "##";
const string NO_LEMMA = "##";
const string NO_CPOSTAG = "##";
const string NO_POSTAG = "##";
const string NO_FEAT = "##";
const string FEAT_SEP = "-";

const string LEFT_RIGHT_END_POS = "##";
const string BET_NO_POS = "#0#";
const string BET_ONE_POS = "#1";

DepPipe::DepPipe(const ParserOptions &_options) : options(_options)
{
	m_depReader = 0;
	m_depReader = new CONLLReader();
	m_depWriter = new CONLLWriter();

  m_bSimpFeature = false;  // to control sentense simplication features

}

DepPipe::~DepPipe(void)
{
	if (m_depReader) delete m_depReader;
	if (m_depWriter) delete m_depWriter;

}
int DepPipe::initInputFile(const char *filename) {
	if (0 != m_depReader->startReading(filename)) return -1;
	return 0;
}

void DepPipe::uninitInputFile() {
	if (m_depWriter) m_depReader->finishReading();
}

int DepPipe::initOutputFile(const char *filename) {
	if (0 != m_depWriter->startWriting(filename)) return -1;
	return 0;
}

void DepPipe::uninitOutputFile() {
	if (m_depWriter) m_depWriter->finishWriting();
}

int DepPipe::outputInstance(const DepInstance *pInstance) {
	if (0 != m_depWriter->write(pInstance)) return -1;
	return 0;
}


const char *DepPipe::getType(int typeIndex) {
	if (typeIndex >= 0 && typeIndex < m_vecTypes.size()) {
		return m_vecTypes[typeIndex].c_str();
	} else {
		return "";
	}
}

void DepPipe::initSimpRule()
{

	cout << "Reading Rule File..." << endl;
	ifstream conf((options.m_simpRuleFile).c_str());
	if (!conf)
  {
    return;
  }

	string strLine;
	while (my_getline(conf, strLine)) 
	{
		if (strLine.empty() || strLine[0] == '*') continue;
		vector<SimpRuleUnit> oneRule;
    vector<string> vecInfo;
		split_bychar(strLine, vecInfo, ' ');
    vector<string> unitatoms;
    for(int i = 0; i < vecInfo.size(); i++)
    {
      split_bychar(vecInfo[i], unitatoms, ':');
      SimpRuleUnit ruleUnit;
      //if(unitatoms[0].
      int current_pos = 0;
      ruleUnit.bUnitT = true;
      if(unitatoms[0][current_pos] == '!')
      {
        ruleUnit.bUnitT = false;
        current_pos++;
      }
      ruleUnit.arcdep = false;
      if(unitatoms[0][current_pos] == 'd')
      {
        ruleUnit.arcdep = true;
        current_pos++;
      }
      if(unitatoms[0][current_pos] == 't')
      {
        ruleUnit.type = 1;
        current_pos++;
      }
      else if(unitatoms[0][current_pos] == 'w')
      {
        ruleUnit.type = 0;
        current_pos++;
      }
      else if(unitatoms[0][current_pos] == 'l')
      {
        ruleUnit.type = 2;
        current_pos++;
      }
      else
      {
        cout << "error rule type" << endl;
        conf.close();
        return;
      }

      string strposition = unitatoms[0].substr(current_pos);
      ruleUnit.position = atoi(strposition.c_str());
      ruleUnit.value = unitatoms[1];   
      oneRule.push_back(ruleUnit);
    }
    rules.push_back(oneRule);
	}

  conf.close();

	cout << "Reading Rule File Finished..." << endl;
}

int DepPipe::createAlphabet(vector<int> &instanceLength)
{


	cout << "Creating Alphabet..." << endl;

	initInputFile(options.m_strTrainFile.c_str());
	m_featAlphabet.allowGrowth();
	m_labelAlphabet.allowGrowth();

	instanceLength.clear();

	DepInstance *pInstance = nextInstance();
	int numInstance = 0;

	while (pInstance) {
		if (++numInstance % options.m_display_interval == 0) cout << numInstance << " ";

		createSpan(pInstance);
		createFeatureVector(pInstance);

		instanceLength.push_back(pInstance->size());

		if(options.m_bSaveMemory)
		{
			DepInstance goldInstance;	
			goldInstance.copyValuesFrom(*pInstance);
			m_vecInstances.push_back(goldInstance);
			//m_vecInstances.push_back(pInstance);
		}

//		const vector<string> &deprels = pInstance->deprels;
//		int i = 0;
//		for (; i < deprels.size(); ++i) {
//			int id = m_labelAlphabet.lookupIndex(deprels[i]);
//			cout << deprels[i] << " " << id << endl;
//			exit(0);
//		}
//		m_labelAlphabet.show();
//		exit(0);

		if ( options.m_numMaxInstance > 0 && numInstance == options.m_numMaxInstance) break;
		pInstance = nextInstance();
	}

	uninitInputFile();

	cout << endl;
	cout << "instance num: " << numInstance << endl;
	cout << "Features num: " <<  m_featAlphabet.size() << endl;
//	m_featAlphabet.show();
//	m_labelAlphabet.show();
	cout << "label num: " <<  m_labelAlphabet.size() << endl;
	cout << "Create Alphabet Done" << endl;
	return 0;
}


void DepPipe::closeAlphabet() {
	m_featAlphabet.stopGrowth();
	m_labelAlphabet.stopGrowth();
	m_featAlphabet.collectKeys();
	m_labelAlphabet.collectKeys();
	m_featAlphabet.fillTrie();
	m_labelAlphabet.fillTrie();
	mapTypes();
}

void DepPipe::mapTypes() {
	m_vecTypes.resize(m_labelAlphabet.size());
	vector<string> vecKeys;
	m_labelAlphabet.getKeys(vecKeys);
	int i = 0;
	for(; i < vecKeys.size(); ++i) {
		int idx = m_labelAlphabet.lookupIndex(vecKeys[i]);
		if (idx < 0 || idx >= m_labelAlphabet.size()) {
			cout << "m_labelAlphabet err: " << vecKeys[i] << " : " << idx << endl;
			continue;
		}
		m_vecTypes[idx] = vecKeys[i];
	}
}

DepInstance *DepPipe::nextInstance()
{
	DepInstance *pInstance = m_depReader->getNext();
	if (!pInstance || pInstance->forms.empty()) return 0;

  const int length = pInstance->orig_size();

	pInstance->postags_for_bet_feat.clear();
  pInstance->verb_cnt.clear();
  pInstance->conj_cnt.clear();
  pInstance->punc_cnt.clear();
  pInstance->postags_for_bet_feat.resize(length, "");
  pInstance->verb_cnt.resize(length, 0);
  pInstance->conj_cnt.resize(length, 0);
  pInstance->punc_cnt.resize(length, 0);
  for (int i = 1; i < length; ++i) 
  {  
    pInstance->postags_for_bet_feat[i] =  pInstance->cpostags[i];
    const string &tag = pInstance->postags_for_bet_feat[i];
    pInstance->verb_cnt[i] = pInstance->verb_cnt[i-1];
    pInstance->conj_cnt[i] = pInstance->conj_cnt[i-1];
    pInstance->punc_cnt[i] = pInstance->punc_cnt[i-1];
    if(tag[0] == 'v' || tag[0] == 'V') 
    {
      ++pInstance->verb_cnt[i];
    } 
    else if( tag == "Punc" ||	tag == "PU" || tag == "," || tag == ":") 
    {
      ++pInstance->punc_cnt[i];
    } 
    else if( tag == "Conj" ||	tag == "CC" || tag == "cc") 
    {
      ++pInstance->conj_cnt[i];
    }
  }

	return pInstance;
}

void DepPipe::createSpan(DepInstance *pInstance) {
	const vector<string> &deprels = pInstance->deprels;
	const vector<int> &heads = pInstance->heads;
	string &spans = pInstance->actParseTree;
	spans = "";
	m_labelAlphabet.lookupIndex(deprels[0]);
	int i = 1;
	for (; i < deprels.size(); ++i) {
		ostringstream tout;
		tout << heads[i] << "|" << i << ":" << m_labelAlphabet.lookupIndex(deprels[i]);
		spans += tout.str();
		if (i < deprels.size()-1) spans += " ";
	}
//	cout << spans << endl;
}

int DepPipe::add(const string &feat, FeatureVec &fv) 
{
  //if(m_bSimpFeature) feat = "SIMP-" + feat;
  string truefeat = "" + feat;
  if(m_bSimpFeature) truefeat = "SIMP-" + feat;
  bool bAppend = false;
	int num = m_featAlphabet.lookupIndex(truefeat, bAppend);
	if (num >= 0) 
  {
		fv.add(num, 1.0);
    if(bAppend) return 1;
    return 0;
	}
  else
  {
    return 1;
  }
}
/*
void DepPipe::add(const string &feat, double val, FeatureVec &fv) {
	int num = m_featAlphabet.lookupIndex(feat);
	if (num >= 0) {
		fv.add(num, val);
	}
}
*/
int DepPipe::addArcFeature(DepInstance *pInstance, int smaller, int larger, bool attR, FeatureVec &fv)
{
  int newFeatureCount = 0;
	if(attR)
  {
    newFeatureCount += addFeature(pInstance, "", smaller, larger, fv );
  }
  else
  {
    newFeatureCount += addFeature(pInstance, "", larger, smaller, fv );
  }

  return newFeatureCount;
}

int DepPipe::addLabelFeature(DepInstance *pInstance, int nodeIdx, const string &deprel, bool is_child, bool attR, FeatureVec &fv)
{
  int newFeatureCount = 0;
	string dir = attR ? "_R" : "_L";
	string strIsChild = is_child ? "_1" : "_0";
	string dir_child = dir + strIsChild;

	const string &form = pInstance->forms[nodeIdx];
	const string &lemma = pInstance->lemmas[nodeIdx];
	const string &postag = pInstance->postags[nodeIdx];
	const string &cpostag = pInstance->cpostags[nodeIdx];

/*
	string att = dir_child;
	string feattmp = string("lbl") + "1" + form + "_" + deprel;
	add(feattmp , fv);
	add(feattmp + att, fv);
	feattmp = string("lbl") + "2" + cpostag + "_" + deprel;
	add(feattmp , fv);
	add(feattmp + att, fv);
	return;
//*/

	string feat = "lbl";
	feat += deprel + dir;
	newFeatureCount += add(feat, fv);

	string prefix = "lbl2"; prefix += deprel;

	//if (options.m_isUseForm && options.m_isUsePostag && options.m_isUseForm_label) 
  {
		feat = prefix + "1" + form + " " + postag;
		if (options.m_isUse_label_feats_t) newFeatureCount += add(feat, fv);
		if (options.m_isUse_label_feats_t_child) newFeatureCount += add(feat + strIsChild, fv);
		newFeatureCount += add(feat + dir_child, fv);
	}
	//if (options.m_isUseLemma && options.m_isUsePostag && options.m_isUseLemma_label)
  {
		feat = prefix + "2" + lemma + " " + postag;
		if (options.m_isUse_label_feats_t) newFeatureCount += add(feat, fv);
		if (options.m_isUse_label_feats_t_child) newFeatureCount += add(feat + strIsChild, fv);
		newFeatureCount += add(feat + dir_child, fv);
	}
	//if (options.m_isUseForm && options.m_isUseCPostag && options.m_isUseForm_label) 
  {
		feat = prefix + "3" + form + " " + cpostag;
		if (options.m_isUse_label_feats_t) newFeatureCount += add(feat, fv);
		if (options.m_isUse_label_feats_t_child) newFeatureCount += add(feat + strIsChild, fv);
		newFeatureCount += add(feat + dir_child, fv);
	}
	//if (options.m_isUseLemma && options.m_isUseCPostag && options.m_isUseLemma_label) 
  {
		feat = prefix + "4" + lemma + " " + cpostag;
		if (options.m_isUse_label_feats_t) newFeatureCount += add(feat, fv);
		if (options.m_isUse_label_feats_t_child) newFeatureCount += add(feat + strIsChild, fv);
		newFeatureCount += add(feat + dir_child, fv);
	}

	prefix = "lbl1"; prefix += deprel;

	//if (options.m_isUseForm && options.m_isUseForm_label) 
  {
		feat = prefix + "1" + form;
		if (options.m_isUse_label_feats_t) newFeatureCount += add(feat, fv);
		if (options.m_isUse_label_feats_t_child) newFeatureCount += add(feat + strIsChild, fv);
		newFeatureCount += add(feat + dir_child, fv);
	}

	//if (options.m_isUseLemma && options.m_isUseLemma_label) 
  {
		feat = prefix + "2" + lemma;
		if (options.m_isUse_label_feats_t) newFeatureCount += add(feat, fv);
		if (options.m_isUse_label_feats_t_child) newFeatureCount += add(feat + strIsChild, fv);
		newFeatureCount += add(feat + dir_child, fv);
	}
	//if (options.m_isUsePostag) 
  {
		feat = prefix + "3" + postag;
		if (options.m_isUse_label_feats_t) newFeatureCount += add(feat, fv);
		if (options.m_isUse_label_feats_t_child) newFeatureCount += add(feat + strIsChild, fv);
		newFeatureCount += add(feat + dir_child, fv);
	}
	//if (options.m_isUseCPostag) 
  {
		feat = prefix + "4" + cpostag;
		if (options.m_isUse_label_feats_t) newFeatureCount += add(feat, fv);
		if (options.m_isUse_label_feats_t_child) newFeatureCount += add(feat + strIsChild, fv);
		newFeatureCount += add(feat + dir_child, fv);
	}

	// surrounding features
	//if (options.m_isUsePostag) 
  {
		prefix = "lbl-sur-pos";
		newFeatureCount += addLabelFeature_surrounding(prefix, pInstance->postags, nodeIdx, deprel, strIsChild, dir, fv);
	}
	//if (options.m_isUseCPostag) 
  {
		prefix = "lbl-sur-cpos";
		newFeatureCount += addLabelFeature_surrounding(prefix, pInstance->cpostags, nodeIdx, deprel, strIsChild, dir, fv);
	}

  return newFeatureCount;
}

int DepPipe::addLabelFeature_surrounding(const string &prefix, const vector<string> &vecInfo, int nodeIdx, const string &deprel, const string &strIsChild, const string &dir, FeatureVec &fv)
{
  int newFeatureCount = 0;
	string left = nodeIdx > 0 ? vecInfo[nodeIdx-1] : "BEG"; // i-1
	string right = nodeIdx < vecInfo.size()-1 ? vecInfo[nodeIdx+1] : "END"; //i+1
	string dir_child = dir + strIsChild;
	string feat;
	// i-1 i i+1  # i-1 i  # i i+1
	feat = prefix + "_"  + deprel +  "1" + left + " " + vecInfo[nodeIdx] + " " + right;
	if (options.m_isUse_label_feats_t) newFeatureCount += add(feat, fv);
	if (options.m_isUse_label_feats_t_child) newFeatureCount += add(feat + strIsChild, fv);
	newFeatureCount += add(feat + dir_child, fv);

	feat = prefix + "_"  + deprel + "2" + left + " " + vecInfo[nodeIdx];
	if (options.m_isUse_label_feats_t) newFeatureCount += add(feat, fv);
	if (options.m_isUse_label_feats_t_child) newFeatureCount += add(feat + strIsChild, fv);
	newFeatureCount += add(feat + dir_child, fv);

	feat = prefix + "_"  + deprel +  "3" + vecInfo[nodeIdx] + " " + right;
	if (options.m_isUse_label_feats_t) newFeatureCount += add(feat, fv);
	if (options.m_isUse_label_feats_t_child) newFeatureCount += add(feat + strIsChild, fv);
	newFeatureCount += add(feat + dir_child, fv);

  return newFeatureCount;
}

int DepPipe::createFeatureVector(DepInstance *pInstance)
{
  int newFeatureCount = 0;
	pInstance->fv.clear();
	const vector<string> &deprels = pInstance->deprels;
	const vector<int> &heads = pInstance->heads;
	FeatureVec &fv = pInstance->fv;
	int i = 0;
	int length = pInstance->forms.size();
	for (; i < length; ++i) {
		if (heads[i] == -1) continue;
		int smaller = i < heads[i] ? i : heads[i];
		int larger = i > heads[i] ? i : heads[i];
		bool attR = i < heads[i] ? false : true;
    newFeatureCount += addArcFeature(pInstance, smaller, larger, attR, fv);
		if (options.m_isLabeled) {
			newFeatureCount += addLabelFeature(pInstance, i, deprels[i], true, attR, fv);
			newFeatureCount += addLabelFeature(pInstance, heads[i], deprels[i], false, attR, fv);
		}
	}

	if(options.m_isUseSib)
	{
		newFeatureCount += addExtendedFeature(pInstance, fv);
	}
  
  if(options.m_isUseSimp)
	{
    vector<int> deleteNodes;
    pInstance->findDeleteNodes(rules, deleteNodes);

    if(deleteNodes.size() > 0)
    {
      DepInstance curSimpInstance;
      pInstance->deleteNode(deleteNodes, curSimpInstance);
      if(curSimpInstance.orig_size() > 1)
      {
        m_bSimpFeature = true;
        newFeatureCount += createFeatureVector(&curSimpInstance, pInstance->fv);
        m_bSimpFeature = false;
      }
    }
  }

  return newFeatureCount;
}

int DepPipe::createFeatureVector(DepInstance *pInstance, FeatureVec &fv)
{
  int newFeatureCount = 0;
	const vector<string> &deprels = pInstance->deprels;
	const vector<int> &heads = pInstance->heads;
	int i = 0;
	int length = pInstance->forms.size();
	for (; i < length; ++i) {
		if (heads[i] == -1) continue;
		int smaller = i < heads[i] ? i : heads[i];
		int larger = i > heads[i] ? i : heads[i];
		bool attR = i < heads[i] ? false : true;
    newFeatureCount += addArcFeature(pInstance, smaller, larger, attR, fv);
		if (options.m_isLabeled) {
			newFeatureCount += addLabelFeature(pInstance, i, deprels[i], true, attR, fv);
			newFeatureCount += addLabelFeature(pInstance, heads[i], deprels[i], false, attR, fv);
		}
	}

	if(options.m_isUseSib)
	{
		newFeatureCount += addExtendedFeature(pInstance, fv);
	}

  return newFeatureCount;
}

void DepPipe::fillFeatureVectors(DepInstance *instance, FVS_PROBS* pFvsProbs, const Parameter &params, int start_pos, int end_pos)
{

	int instanceLength = instance->size();
	vector<unsigned int> fvs_dim(3);
	unsigned int fvs_pos;
	if(end_pos > instanceLength)
	{
		end_pos = instanceLength;
	}
	if(start_pos < 0)
	{
		start_pos = 0;
	}

  MultiArray<FeatureVec>& fvs = pFvsProbs->fvs;
	MultiArray<double>& probs = pFvsProbs->probs;
	MultiArray<FeatureVec>& fvs_trips = pFvsProbs->fvs_trips;
	MultiArray<double>& probs_trips = pFvsProbs->probs_trips;
	MultiArray<FeatureVec>& nt_fvs = pFvsProbs->nt_fvs;
	MultiArray<double>& nt_probs = pFvsProbs->nt_probs;

	// Get production crap.
	
	{
		int w1 = start_pos;
		for(; w1 < end_pos; w1++) 
		{
			int w2 = w1+1;
			if (w2 >= instanceLength) continue;
			fvs.setDemisionVal(fvs_dim, w1, w2, 0);
			fvs.getElement(fvs_dim, fvs_pos);
			for(; w2 < instanceLength; w2++) {
				int ph = 0;
				for(; ph < 2; ph++) {
					bool attR = ph == 0 ? true : false;
					addArcFeature(instance,w1,w2,attR, fvs.getElement(fvs_pos));
					probs.getElement(fvs_pos) = params.getScore(fvs.getElement(fvs_pos));
					vector<int> vecKeys;
					fvs.getElement(fvs_pos).getKeys(vecKeys);
					// cout << vecKeys.size() << "\t";
					++fvs_pos;
				}
			}
		}
	}
	

	if(options.m_isLabeled) 
  {
		vector<unsigned int> nt_dim(4);
		unsigned int nt_pos;
		nt_fvs.setDemisionVal(nt_dim, 0, 0, 0, 0);
		nt_fvs.getElement(nt_dim, nt_pos);
		int w1 = start_pos;
		for(; w1 < end_pos; w1++) {
			int t = 0;
			for(; t < m_vecTypes.size(); t++) {
				const string &type = m_vecTypes[t];
				int ph = 0;
				for(; ph < 2; ph++) {
					bool attR = ph == 0 ? true : false;
					int ch = 0;
					for(; ch < 2; ch++) {
						bool child = ch == 0 ? true : false;
						addLabelFeature(instance, w1, type, child, attR, nt_fvs.getElement(nt_pos));
						nt_probs.getElement(nt_pos) = params.getScore(nt_fvs.getElement(nt_pos));
						vector<int> vecKeys;
						nt_fvs.getElement(nt_pos).getKeys(vecKeys);
						// cout << vecKeys.size() << "\t";
						++nt_pos;
					}
				}
			}
			//			cout << endl;
		}
	}


	if(options.m_isUseSib)
	{
		int w1, w2, w3, wh;
		for(w1 = start_pos; w1 < end_pos; w1++) {
			for(w2 = w1; w2 < instanceLength; w2++) {
				for(w3 = w2+1; w3 < instanceLength; w3++) {
					fvs_trips.setDemisionVal(fvs_dim, w1, w2, w3);
					fvs_trips.getElement(fvs_dim, fvs_pos);
					addFeature_sib(instance,w1,w2,w3, fvs_trips.getElement(fvs_pos));
					probs_trips.getElement(fvs_pos) = params.getScore( fvs_trips.getElement(fvs_pos) );
				}
			}
			for(w2 = w1; w2 >= 0; w2--) {
				for(w3 = w2-1; w3 >= 0; w3--) {
					fvs_trips.setDemisionVal(fvs_dim, w1, w2, w3);
					fvs_trips.getElement(fvs_dim, fvs_pos);

					addFeature_sib(instance,w1,w2,w3, fvs_trips.getElement(fvs_pos));
					probs_trips.getElement(fvs_pos) = params.getScore( fvs_trips.getElement(fvs_pos) );
				}
			}
		}
	}

	return;

}


int DepPipe::writeInstance(FILE *featFile, DepInstance *pInstance)
{
	//	cout << endl;
	int instanceLength = pInstance->size();
	for(int w1 = 0; w1 < instanceLength; w1++) {
		for(int w2 = w1+1; w2 < instanceLength; w2++) {
			for(int ph = 0; ph < 2; ph++) {
				bool attR = ph == 0 ? true : false;
				FeatureVec prodFV;
				addArcFeature(pInstance,w1,w2,attR,prodFV);
				vector<int> vecKeys;
				prodFV.getKeys(vecKeys);
				// cout << vecKeys.size() << " ";
				::writeObject(featFile, vecKeys);
			}
		}
		// cout << endl;
	}
	::writeObject(featFile, (int)-1);

	if(options.m_isLabeled) {
		for(int w1 = 0; w1 < instanceLength; w1++) {
			for(int t = 0; t < m_vecTypes.size(); t++) {
				const string &type = m_vecTypes[t];
				for(int ph = 0; ph < 2; ph++) {
					bool attR = ph == 0 ? true : false;
					for(int ch = 0; ch < 2; ch++) {
						bool child = ch == 0 ? true : false;
						FeatureVec prodFV;
						addLabelFeature(pInstance, w1, type, child, attR, prodFV);
						vector<int> vecKeys;
						prodFV.getKeys(vecKeys);
						// cout << vecKeys.size() << " ";
						// copy(vecKeys.begin(), vecKeys.end(), ostream_iterator<int>(cout, " "));
						// cout << endl;
						::writeObject(featFile, vecKeys);
					}
				}
			}
			// cout << endl;
		}
		::writeObject(featFile, int(-2));

	}
	//	exit(0);
	if(options.m_isUseSib)
	{
		writeExtendedFeatures(pInstance,featFile);
	}

	vector<int> vecKeys;
	pInstance->fv.getKeys(vecKeys);
	::writeObject(featFile, vecKeys);
	::writeObject(featFile, int(-3));

	//	cout << pInstance->actParseTree.size() << endl;
	writeObject(featFile, pInstance->actParseTree);


	writeObject(featFile, int(-4));

	//
	writeObject(featFile, pInstance->cpostags);


	writeObject(featFile, int(-5));
	return 0;
}

int DepPipe::addExtendedFeature(DepInstance *pInstance, FeatureVec &fv)
{
  int newFeatureCount = 0;
	const int instanceLength = pInstance->size();
	const vector<int> &heads = pInstance->heads;

	// find all trip features
	for(int i = 0; i < instanceLength; i++) {
		if(heads[i] == -1 && i != 0) continue;
		// right children
		int prev = i;
		for(int j = i+1; j < instanceLength; j++) {
			if(heads[j] == i) {		  
			    newFeatureCount += addFeature_sib(pInstance, i, prev, j, fv);
					prev = j;
			}
		}

		prev = i;
		for(int j = i-1; j >= 0; j--) 
		{
			if(heads[j] == i) 
			{
			  newFeatureCount += addFeature_sib(pInstance,i,prev,j,fv);
				prev = j;
			}
		}
	}

  return newFeatureCount;
}
void DepPipe::writeExtendedFeatures(DepInstance *pInstance, FILE *featFile)
{
	const int pInstanceLength = pInstance->size();
	int w1, w2, w3, wh;
	for(w1 = 0; w1 < pInstanceLength; w1++) {
		for(w2 = w1; w2 < pInstanceLength; w2++) {
			for(w3 = w2+1; w3 < pInstanceLength; w3++) {
				FeatureVec prodFV;
				addFeature_sib(pInstance,w1,w2,w3,prodFV);
				vector<int> vecKeys;
				prodFV.getKeys(vecKeys);
				// cout << vecKeys.size() << " ";
				::writeObject(featFile, vecKeys);
			}
		}
		for(w2 = w1; w2 >= 0; w2--) {
			for(w3 = w2-1; w3 >= 0; w3--) {
				FeatureVec prodFV;
				addFeature_sib(pInstance,w1,w2,w3,prodFV);
				vector<int> vecKeys;
				prodFV.getKeys(vecKeys);
				// cout << vecKeys.size() << " ";
				::writeObject(featFile, vecKeys);
			}
		}
	}

	::writeObject(featFile, (int)-13);

}


void DepPipe::ReInit(set<int> invalidFeatures, vector<string>& invalidFeatures_str)
{
  vector<string> usefulFeatures;
  m_featAlphabet.getFeatures(invalidFeatures, usefulFeatures, invalidFeatures_str);
  m_featAlphabet.reinit();
  m_featAlphabet.allowGrowth();
  for(int i = 0; i < usefulFeatures.size(); i++)
  {
    m_featAlphabet.lookupIndex(usefulFeatures[i]);
  }
  m_featAlphabet.stopGrowth();
  m_featAlphabet.collectKeys();
  m_featAlphabet.fillTrie();

}


int DepPipe::addFeature( DepInstance *pInstance, const string &deprel, const int head_id, const int child_id, FeatureVec &fv )
{
  int newFeatureCount = 0;
	newFeatureCount += addFeature_bi_bet_sur(pInstance, deprel, head_id, child_id, fv);

	if (head_id != 0) {
		bool isRightArc = (head_id < child_id ? true : false);
		newFeatureCount += addFeature_unigram(pInstance, deprel, head_id, false, isRightArc, fv);
		newFeatureCount += addFeature_unigram(pInstance, deprel, child_id, true, isRightArc, fv);
	}

  return newFeatureCount;
}


int DepPipe::addFeature_bi_bet_sur( DepInstance *pInstance, const string &deprel, const int head_id, const int child_id, FeatureVec &fv )
{
		return addFeature_bi_bet_sur_mcdonald06(pInstance, deprel, head_id, child_id, fv);
}

int DepPipe::addFeature_unigram( DepInstance *pInstance, const string &deprel, const int node_id, const bool is_child, const bool is_right_arc, FeatureVec &fv )
{
		return addFeature_unigram_mcdonald06(pInstance, deprel, node_id, is_child, is_right_arc, fv);
}

int DepPipe::addFeature_bi_bet_sur_mcdonald06( DepInstance *pInstance, const string &deprel, const int head_id, const int child_id, FeatureVec &fv )
{
  int newFeatureCount = 0;
	newFeatureCount += addFeature_bi_mcdonald06(pInstance, deprel, head_id, child_id, fv);
	newFeatureCount += addFeature_bet_mcdonald06(pInstance, deprel, head_id, child_id, fv);
	newFeatureCount += addFeature_sur_mcdonald06(pInstance, deprel, head_id, child_id, fv);
  return newFeatureCount;
}

int DepPipe::addFeature_bi_mcdonald06( DepInstance *pInstance, const string &deprel, const int head_id, const int child_id, FeatureVec &fv)
{
	if (!options.m_isUse_bi) return 0;

  int newFeatureCount = 0;

	ARC_INFO arc_info;
	get_arc_info(pInstance, head_id, child_id, arc_info);
	const string &dir_dist = arc_info.dir_dist;


	vector<string> &forms = pInstance->forms;

	const string &cpos_h = pInstance->cpostags[head_id];
	const string &cpos_c = pInstance->cpostags[child_id];

	string feat;

	{
		if (arc_info.is_root)
		{
			const string prefix = "BB-";
			if (options.m_isUseForm && options.m_isUseCPostag) {
				feat = prefix + "01=" + forms[head_id] + FEAT_SEP + forms[child_id] + FEAT_SEP + cpos_c; // word-0 word-j pos-j
				newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
			}
			if (options.m_isUseForm) {
				feat = prefix + "02=" + forms[head_id]+ FEAT_SEP + forms[child_id]; // word-0 word-j
				newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
			}
			if (options.m_isUseCPostag) {
				feat = prefix + "03=" + forms[head_id]+ FEAT_SEP + cpos_c; // word-0 pos-j
				newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
			}
		} // end if (is_root)
		else
		{
			const string prefix = "BB-";
			if (options.m_isUseForm && options.m_isUseCPostag) {
				feat = prefix + "0=" + forms[head_id] + FEAT_SEP + cpos_h + FEAT_SEP + forms[child_id] + FEAT_SEP + cpos_c; // word-i pos-i word-j pos-j
				newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
				feat = prefix + "1=" + forms[head_id] + FEAT_SEP + forms[child_id] + FEAT_SEP + cpos_c; // word-i word-j pos-j
				newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
				feat = prefix + "2=" + cpos_h + FEAT_SEP + forms[child_id] + FEAT_SEP + cpos_c; // pos-i word-j pos-j
				newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
				feat = prefix + "3=" + forms[head_id] + FEAT_SEP + cpos_h + FEAT_SEP + forms[child_id]; // word-i pos-i word-j
				newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
				feat = prefix + "4=" + forms[head_id] + FEAT_SEP + cpos_h + FEAT_SEP + cpos_c; // word-i pos-i pos-j
				newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);

				// not in mcdonald-phd-thesis06
				feat = prefix + "5=" + forms[head_id] + FEAT_SEP + cpos_c; // word-i pos-j
				newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
				feat = prefix + "6=" + cpos_h + FEAT_SEP + forms[child_id]; // pos-i word-j
				newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
			}
			if (options.m_isUseForm) 
			{
				feat = prefix + "7=" + forms[head_id] + FEAT_SEP + forms[child_id]; // word-i word-j
				newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
			}
			if (options.m_isUseCPostag) {
				feat = prefix + "8=" + cpos_h + FEAT_SEP + cpos_c; // pos-i pos-j
				newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
			}
		} // end else if (is_root)
	}
  
  if(pInstance->lemmas[child_id] == pInstance->forms[head_id] || (pInstance->lemmas[child_id] == "<EOS>" && arc_info.is_root))
  {
    string prefix = "SYNHEAD-EQUAL-";
		feat = prefix + "00";
    newFeatureCount += add(feat, fv); 
    feat = prefix + "01" + FEAT_SEP+ pInstance->forms[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "02" + FEAT_SEP+ pInstance->forms[head_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "03" + FEAT_SEP+ pInstance->forms[head_id]+ FEAT_SEP+ pInstance->forms[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "04" + FEAT_SEP+ pInstance->cpostags[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "05" + FEAT_SEP+ pInstance->cpostags[head_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "06" + FEAT_SEP+ pInstance->cpostags[head_id]+ FEAT_SEP+ pInstance->cpostags[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "07" + FEAT_SEP+ pInstance->forms[child_id] + FEAT_SEP+ pInstance->cpostags[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "08" + FEAT_SEP+ pInstance->forms[head_id] + FEAT_SEP+ pInstance->cpostags[head_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "09" + FEAT_SEP+ pInstance->forms[head_id] + FEAT_SEP+ pInstance->cpostags[head_id] + FEAT_SEP+ pInstance->forms[child_id]+ FEAT_SEP+ pInstance->cpostags[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "10" + FEAT_SEP+ pInstance->forms[child_id] + FEAT_SEP+ pInstance->cpostags[head_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "11" + FEAT_SEP+ pInstance->forms[head_id] + FEAT_SEP+ pInstance->cpostags[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "12" + FEAT_SEP+ pInstance->forms[head_id]+ FEAT_SEP+ pInstance->forms[child_id] + FEAT_SEP+ pInstance->cpostags[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "13" + FEAT_SEP+ pInstance->forms[head_id]+ FEAT_SEP+ pInstance->forms[child_id] + FEAT_SEP+ pInstance->cpostags[head_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "14" + FEAT_SEP+ pInstance->cpostags[head_id]+ FEAT_SEP+ pInstance->cpostags[child_id] + FEAT_SEP+ pInstance->forms[head_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "15" + FEAT_SEP+ pInstance->cpostags[head_id]+ FEAT_SEP+ pInstance->cpostags[child_id] + FEAT_SEP+ pInstance->forms[child_id];
    newFeatureCount += add(feat, fv); 

    prefix = "SYNHEAD-LABEL-EQUAL-" + pInstance->postags[child_id]+ FEAT_SEP;
		feat = prefix + "00";
    newFeatureCount += add(feat, fv); 
    feat = prefix + "01" + FEAT_SEP+ pInstance->forms[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "02" + FEAT_SEP+ pInstance->forms[head_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "03" + FEAT_SEP+ pInstance->forms[head_id]+ FEAT_SEP+ pInstance->forms[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "04" + FEAT_SEP+ pInstance->cpostags[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "05" + FEAT_SEP+ pInstance->cpostags[head_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "06" + FEAT_SEP+ pInstance->cpostags[head_id]+ FEAT_SEP+ pInstance->cpostags[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "07" + FEAT_SEP+ pInstance->forms[child_id] + FEAT_SEP+ pInstance->cpostags[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "08" + FEAT_SEP+ pInstance->forms[head_id] + FEAT_SEP+ pInstance->cpostags[head_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "09" + FEAT_SEP+ pInstance->forms[head_id] + FEAT_SEP+ pInstance->cpostags[head_id] + FEAT_SEP+ pInstance->forms[child_id]+ FEAT_SEP+ pInstance->cpostags[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "10" + FEAT_SEP+ pInstance->forms[child_id] + FEAT_SEP+ pInstance->cpostags[head_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "11" + FEAT_SEP+ pInstance->forms[head_id] + FEAT_SEP+ pInstance->cpostags[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "12" + FEAT_SEP+ pInstance->forms[head_id]+ FEAT_SEP+ pInstance->forms[child_id] + FEAT_SEP+ pInstance->cpostags[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "13" + FEAT_SEP+ pInstance->forms[head_id]+ FEAT_SEP+ pInstance->forms[child_id] + FEAT_SEP+ pInstance->cpostags[head_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "14" + FEAT_SEP+ pInstance->cpostags[head_id]+ FEAT_SEP+ pInstance->cpostags[child_id] + FEAT_SEP+ pInstance->forms[head_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "15" + FEAT_SEP+ pInstance->cpostags[head_id]+ FEAT_SEP+ pInstance->cpostags[child_id] + FEAT_SEP+ pInstance->forms[child_id];
    newFeatureCount += add(feat, fv); 
	}
  else
  {
    string prefix = "SYNHEAD-NOT-EQUAL-";
		feat = prefix + "00";
    newFeatureCount += add(feat, fv); 
    feat = prefix + "01" + FEAT_SEP+ pInstance->forms[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "02" + FEAT_SEP+ pInstance->forms[head_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "03" + FEAT_SEP+ pInstance->forms[head_id]+ FEAT_SEP+ pInstance->forms[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "04" + FEAT_SEP+ pInstance->cpostags[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "05" + FEAT_SEP+ pInstance->cpostags[head_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "06" + FEAT_SEP+ pInstance->cpostags[head_id]+ FEAT_SEP+ pInstance->cpostags[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "07" + FEAT_SEP+ pInstance->forms[child_id] + FEAT_SEP+ pInstance->cpostags[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "08" + FEAT_SEP+ pInstance->forms[head_id] + FEAT_SEP+ pInstance->cpostags[head_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "09" + FEAT_SEP+ pInstance->forms[head_id] + FEAT_SEP+ pInstance->cpostags[head_id] + FEAT_SEP+ pInstance->forms[child_id]+ FEAT_SEP+ pInstance->cpostags[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "10" + FEAT_SEP+ pInstance->forms[child_id] + FEAT_SEP+ pInstance->cpostags[head_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "11" + FEAT_SEP+ pInstance->forms[head_id] + FEAT_SEP+ pInstance->cpostags[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "12" + FEAT_SEP+ pInstance->forms[head_id]+ FEAT_SEP+ pInstance->forms[child_id] + FEAT_SEP+ pInstance->cpostags[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "13" + FEAT_SEP+ pInstance->forms[head_id]+ FEAT_SEP+ pInstance->forms[child_id] + FEAT_SEP+ pInstance->cpostags[head_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "14" + FEAT_SEP+ pInstance->cpostags[head_id]+ FEAT_SEP+ pInstance->cpostags[child_id] + FEAT_SEP+ pInstance->forms[head_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "15" + FEAT_SEP+ pInstance->cpostags[head_id]+ FEAT_SEP+ pInstance->cpostags[child_id] + FEAT_SEP+ pInstance->forms[child_id];
    newFeatureCount += add(feat, fv); 

    prefix = "SYNHEAD-LABEL-NOT-EQUAL-" + pInstance->postags[child_id]+ FEAT_SEP;
		feat = prefix + "00";
    newFeatureCount += add(feat, fv); 
    feat = prefix + "01" + FEAT_SEP+ pInstance->forms[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "02" + FEAT_SEP+ pInstance->forms[head_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "03" + FEAT_SEP+ pInstance->forms[head_id]+ FEAT_SEP+ pInstance->forms[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "04" + FEAT_SEP+ pInstance->cpostags[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "05" + FEAT_SEP+ pInstance->cpostags[head_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "06" + FEAT_SEP+ pInstance->cpostags[head_id]+ FEAT_SEP+ pInstance->cpostags[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "07" + FEAT_SEP+ pInstance->forms[child_id] + FEAT_SEP+ pInstance->cpostags[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "08" + FEAT_SEP+ pInstance->forms[head_id] + FEAT_SEP+ pInstance->cpostags[head_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "09" + FEAT_SEP+ pInstance->forms[head_id] + FEAT_SEP+ pInstance->cpostags[head_id] + FEAT_SEP+ pInstance->forms[child_id]+ FEAT_SEP+ pInstance->cpostags[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "10" + FEAT_SEP+ pInstance->forms[child_id] + FEAT_SEP+ pInstance->cpostags[head_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "11" + FEAT_SEP+ pInstance->forms[head_id] + FEAT_SEP+ pInstance->cpostags[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "12" + FEAT_SEP+ pInstance->forms[head_id]+ FEAT_SEP+ pInstance->forms[child_id] + FEAT_SEP+ pInstance->cpostags[child_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "13" + FEAT_SEP+ pInstance->forms[head_id]+ FEAT_SEP+ pInstance->forms[child_id] + FEAT_SEP+ pInstance->cpostags[head_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "14" + FEAT_SEP+ pInstance->cpostags[head_id]+ FEAT_SEP+ pInstance->cpostags[child_id] + FEAT_SEP+ pInstance->forms[head_id];
    newFeatureCount += add(feat, fv); 
    feat = prefix + "15" + FEAT_SEP+ pInstance->cpostags[head_id]+ FEAT_SEP+ pInstance->cpostags[child_id] + FEAT_SEP+ pInstance->forms[child_id];
    newFeatureCount += add(feat, fv); 
  }
	
  return newFeatureCount;
}

int DepPipe::addFeature_bet_mcdonald06( DepInstance *pInstance, const string &deprel, const int head_id, const int child_id, FeatureVec &fv)
{
	if (!options.m_isUse_bet) return 0;
	ostringstream out;
	ARC_INFO arc_info;
	get_arc_info(pInstance, head_id, child_id, arc_info);
	const string &dir_dist = arc_info.dir_dist;

	const vector<string> &pos_bet = pInstance->postags_for_bet_feat;
	const vector<int> &verb_cnt = pInstance->verb_cnt;
	const vector<int> &conj_cnt = pInstance->conj_cnt;
	const vector<int> &punc_cnt = pInstance->punc_cnt;

	map<string, int> pos_seen;

  int newFeatureCount = 0;

	string feat;
	if (arc_info.is_root)
	{
		const string &pos_root = pInstance->postags[0];
		if (options.m_isUseCPostag) 
    {
			const string prefix = "BBE-";
			// add left poss
			if (1 >= child_id) 
      { // beginning
				feat = prefix + "00L="+ pos_root + "##" + pos_bet[child_id];
				newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
			} 
      else 
      {
				pos_seen.clear();
				int i = 1;
				for (; i < child_id; ++i) 
        {
					// pos-i pos-b pos-j
					if (pos_seen.find(pos_bet[i]) == pos_seen.end()) 
          {
						feat = prefix + "0L=" + pos_root + FEAT_SEP + pos_bet[i] + FEAT_SEP + pos_bet[child_id];
						newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
						pos_seen[ pos_bet[i] ] = 1;
					}
				}

				// left-part: verb, punc, conj +cnt
				out.str(""); out << verb_cnt[child_id-1] - verb_cnt[0];
				feat = prefix + "0L=" + pos_root + FEAT_SEP + "verb=" + out.str() + FEAT_SEP + pos_bet[child_id];
				newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
				out.str(""); out << conj_cnt[child_id-1] - conj_cnt[0];
				feat = prefix + "0L=" + pos_root + FEAT_SEP + "conj=" + out.str() + FEAT_SEP + pos_bet[child_id];
				newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
				out.str(""); out << punc_cnt[child_id-1] - punc_cnt[0];
				feat = prefix + "0L=" + pos_root + FEAT_SEP + "punc=" + out.str() + FEAT_SEP + pos_bet[child_id];
				newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
			}
			// add right poss
			if (pInstance->orig_size()-1 == child_id) 
      { // ending
				/*
				if (options.m_order.sib_order || options.m_order.grand_order || options.m_order.all_grand_order) {
					cout << "DepPipe::addFeature_bi_bet_sur: may be wrong to judge the ending in this way. [n+1] is used?" << endl;
					exit(0);
				}
				*/
				feat = prefix + "00R="+ pos_root + "##" + pos_bet[child_id];
				newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
			} 
      else 
      {
				pos_seen.clear();
				int i = child_id + 1;
				for (; i < pInstance->orig_size(); ++i) 
        {
					// pos-i pos-b pos-j
					if (pos_seen.find(pos_bet[i]) == pos_seen.end()) 
          {
						string feat = prefix + "0R=" + pos_root + FEAT_SEP + pos_bet[i] + FEAT_SEP + pos_bet[child_id];
						newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
						pos_seen[ pos_bet[i] ] = 1;
					}
				}

				// right-part: verb, punc, conj +cnt
				const int N = pInstance->orig_size() - 1;
				out.str(""); out << verb_cnt[N] - verb_cnt[child_id];
				feat = prefix + "0R=" + pos_root + FEAT_SEP + "verb=" + out.str() + FEAT_SEP + pos_bet[child_id];
				newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
				out.str(""); out << conj_cnt[N] - conj_cnt[child_id];
				feat = prefix + "0R=" + pos_root + FEAT_SEP + "conj=" + out.str() + FEAT_SEP + pos_bet[child_id];
				newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
				out.str(""); out << punc_cnt[N] - punc_cnt[child_id];
				feat = prefix + "0R=" + pos_root + FEAT_SEP + "punc=" + out.str() + FEAT_SEP + pos_bet[child_id];
				newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
			}
		}
	} // end if (is_root)
	else
	{
		const int smaller = (head_id < child_id ? head_id : child_id);
		const int larger = (head_id > child_id ? head_id : child_id);
		if (options.m_isUseCPostag) {
			const string prefix = "BBE-";
			if (1 == (larger - smaller)) {
				feat = prefix + "00="+ pos_bet[smaller] + "##" + pos_bet[larger];
				newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
			} else {
				pos_seen.clear();
				int i = smaller+1;
				for (; i < larger; ++i) {
					// pos-i pos-b pos-j
					if (pos_seen.find(pos_bet[i]) == pos_seen.end()) {
						string feat = prefix + "0=" + pos_bet[smaller] + FEAT_SEP + pos_bet[i] + FEAT_SEP + pos_bet[larger];
						newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
						pos_seen[ pos_bet[i] ] = 1;
					}
				}

				// verb, punc, conj +cnt
				out.str(""); out << verb_cnt[larger-1] - verb_cnt[smaller];
				feat = prefix + "0=" + pos_bet[smaller] + FEAT_SEP + "verb=" + out.str() + FEAT_SEP + pos_bet[larger];
				newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
				out.str(""); out << conj_cnt[larger-1] - conj_cnt[smaller];
				feat = prefix + "0=" + pos_bet[smaller] + FEAT_SEP + "conj=" + out.str() + FEAT_SEP + pos_bet[larger];
				newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
				out.str(""); out << punc_cnt[larger-1] - punc_cnt[smaller];
				feat = prefix + "0=" + pos_bet[smaller] + FEAT_SEP + "punc=" + out.str() + FEAT_SEP + pos_bet[larger];
				newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
			}
		}
	} // end else if (is_root)

  return newFeatureCount;
}

int DepPipe::addFeature_sur_mcdonald06( DepInstance *pInstance, const string &deprel, const int head_id, const int child_id, FeatureVec &fv)
{
	if (!options.m_isUse_sur) return 0;
	if (child_id == 0) return 0;

  int newFeatureCount = 0;

	ARC_INFO arc_info;
	get_arc_info(pInstance, head_id, child_id, arc_info);
	const string &dir_dist = arc_info.dir_dist;


	string feat;
	if (arc_info.is_root)
	{
		if (options.m_isUseCPostag) {
			const string prefix = "BS-";
			const string &pi =  pInstance->cpostags[head_id];

			const string &pj = pInstance->cpostags[child_id];

			const string &pj_L1 = child_id <= 1 ? LEFT_RIGHT_END_POS :pInstance->cpostags[child_id-1]; // pos-j-1

			const string &pj_R1 = child_id == pInstance->orig_size()-1 ? LEFT_RIGHT_END_POS : pInstance->cpostags[child_id+1]; // pos-j+1

			feat = prefix + "4=" + pi + FEAT_SEP + pj_L1 + FEAT_SEP + pj + FEAT_SEP + pj_R1; // pos-0 pos-j-1 pos-j pos-j+1
			newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
			feat = prefix + "1=" + pi + FEAT_SEP + pj_L1 + FEAT_SEP + pj; // pos-0 pos-j-1 pos-j
			newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
			feat = prefix + "3=" + pi + FEAT_SEP + pj + FEAT_SEP + pj_R1; // pos-0 pos-j pos-j+1
			newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
		}
	} // end if (is_root)
	else
	{
		if (options.m_isUseCPostag) {
			const int smaller = (arc_info.is_right_arc ? head_id : child_id);
			const int larger = (!arc_info.is_right_arc ? head_id : child_id);


			const string prefix = "BS-";
			const string &pi = pInstance->cpostags[smaller];
			const string &pj = pInstance->cpostags[larger];

			const string &pi_L1 = (smaller <= 1) ? LEFT_RIGHT_END_POS : pInstance->cpostags[smaller-1]; // pos-i-1
			const string &pj_R1 = (larger == pInstance->orig_size()-1) ? LEFT_RIGHT_END_POS : pInstance->cpostags[larger+1]; // pos-j+1
			string pi_R1 = pInstance->cpostags[smaller+1]; // pos-i+1
			string pj_L1 =  pInstance->cpostags[larger-1]; // pos-j-1
			if (1 == (larger - smaller)) { // adjacent
				pi_R1 = BET_NO_POS;
				pj_L1 = pi_R1;
			} else if (2 == (larger - smaller)) {
				pi_R1 += BET_ONE_POS;
				pj_L1 = pi_R1;
			}

			feat = prefix + "0=" + pi + FEAT_SEP + pi_R1 + FEAT_SEP + pj_L1 + FEAT_SEP + pj; // pos-i pos-i+1 pos-j-1 pos-j
			newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
			feat = prefix + "1=" + pi_L1 + FEAT_SEP + pi + FEAT_SEP + pj_L1 + FEAT_SEP + pj; // pos-i-1 pos-i pos-j-1 pos-j
			newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
			feat = prefix + "2=" + pi + FEAT_SEP + pi_R1 + FEAT_SEP + pj + FEAT_SEP + pj_R1; // pos-i pos-i+1 pos-j pos-j+1
			newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
			feat = prefix + "3=" + pi_L1 + FEAT_SEP + pi + FEAT_SEP + pj + FEAT_SEP + pj_R1; // pos-i-1 pos-i pos-j pos-j+1
			newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);

			// not in mcdonald-phd-thesis06
			feat = prefix + "4=" + pi + FEAT_SEP + pj_L1 + FEAT_SEP + pj + FEAT_SEP + pj_R1; // pos-i pos-j-1 pos-j pos-j+1
			newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
			feat = prefix + "5=" + pi_L1 + FEAT_SEP + pi + FEAT_SEP + pi_R1 + FEAT_SEP + pj; // pos-i-1 pos-i pos-i+1 pos-j
			newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir_dist, fv);
		}
	} // end else if (is_root)

  return newFeatureCount;
}


int DepPipe::addFeature_unigram_mcdonald06( DepInstance *pInstance, const string &deprel, const int node_id, const bool is_child, const bool is_right_arc, FeatureVec &fv)
{
	// same as McDonald-phd-thesis-06
	if (!options.m_isUse_uni) return 0;

  int newFeatureCount = 0;

	string str_is_child = is_child ? "-C" : "-H";
	string dir = (is_right_arc ? "-R" : "-L");

	string &form = pInstance->forms[node_id];
  
	string &cpostag = pInstance->cpostags[node_id];

	string feat;
	
	{
		const string prefix = "U-";
		if (options.m_isUseForm && options.m_isUseCPostag) 
		{
			feat = prefix + "0=" + form + FEAT_SEP + cpostag + str_is_child;
			newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir, fv);	
		}

		if (options.m_isUseForm) {
			feat = prefix + "1=" + form + str_is_child;
			newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir, fv);
		}

		if (options.m_isUseCPostag) {
			feat = prefix + "2=" + cpostag + str_is_child;
			newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir, fv);
		}
	}

  /*
  form = pInstance->lemmas[node_id];
  {
		const string prefix = "SYNHEAD-U-";
		if (options.m_isUseForm && options.m_isUseCPostag) 
		{
			feat = prefix + "0=" + form + FEAT_SEP + cpostag + str_is_child;
			newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir, fv);	
		}

		if (options.m_isUseForm) {
			feat = prefix + "1=" + form + str_is_child;
			newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir, fv);
		}

		if (options.m_isUseCPostag) {
			feat = prefix + "2=" + cpostag + str_is_child;
			newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir, fv);
		}
	}


  form = pInstance->postags[node_id];
  {
		const string prefix = "SYNLABEL-U-";
		if (options.m_isUseForm && options.m_isUseCPostag) 
		{
			feat = prefix + "0=" + form + FEAT_SEP + cpostag + str_is_child;
			newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir, fv);	
		}

		if (options.m_isUseForm) {
			feat = prefix + "1=" + form + str_is_child;
			newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir, fv);
		}

		if (options.m_isUseCPostag) {
			feat = prefix + "2=" + cpostag + str_is_child;
			newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir, fv);
		}
	}
*/
  return newFeatureCount;
}


int DepPipe::addFeature_grand_sib( DepInstance *pInstance, const int par, const int mod, const int gch1, const int gch2, FeatureVec &fv)
{
	if (mod == 0) return 0; // [n+1]->[0->]

  int newFeatureCount = 0;

	const bool isFirstGrandChild = (mod == gch1);
	const bool isLastGrandChild = (gch1 == gch2);
	ARC_INFO arc_info;
	get_arc_info(pInstance, par, mod, arc_info);
	ARC_INFO arc_info_g;
	get_arc_info(pInstance, mod, (isFirstGrandChild ? gch2 : gch1), arc_info_g);
	string dir_gdir = arc_info.dir + arc_info_g.dir;

	vector<string> &forms = pInstance->forms;
  
	const vector<string> &cpostags = pInstance->cpostags;

	{
		const string &wrd_f = forms[par];
		const string &cpos_f = cpostags[par];
		const string &wrd_c = forms[mod];
		const string &cpos_c = cpostags[mod];
		const string &cpos_g1 = isFirstGrandChild ? NO_CPOSTAG : cpostags[gch1];
		const string &cpos_g2 = isLastGrandChild ? NO_CPOSTAG : cpostags[gch2];
	
		string prefix = "GS-";
		string feat;
		feat = prefix + "0=" + cpos_f + FEAT_SEP + cpos_c + FEAT_SEP + cpos_g1 + FEAT_SEP + cpos_g2;	// pos-f pos-c pos-g1 pos-g2
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat+dir_gdir, fv);
		feat = prefix + "1=" + wrd_f + FEAT_SEP + wrd_c + FEAT_SEP + cpos_g1 + FEAT_SEP + cpos_g2;		// wrd-f wrd-c pos-g1 pos-g2
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat+dir_gdir, fv);
		feat = prefix + "2=" + wrd_f + FEAT_SEP + cpos_c + FEAT_SEP + cpos_g1 + FEAT_SEP + cpos_g2;		// wrd-f pos-c pos-g1 pos-g2
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat+dir_gdir, fv);
		feat = prefix + "3=" + cpos_f + FEAT_SEP + wrd_c + FEAT_SEP + cpos_g1 + FEAT_SEP + cpos_g2;		// pos-f wrd-c pos-g1 pos-g2
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat+dir_gdir, fv);

		feat = prefix + "5=" + cpos_f + FEAT_SEP + cpos_g1 + FEAT_SEP + cpos_g2;	// pos-f pos-g1 pos-g2
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat+dir_gdir, fv);
		feat = prefix + "6=" + wrd_f + FEAT_SEP + cpos_g1 + FEAT_SEP + cpos_g2;		// wrd-f pos-g1 pos-g2
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat+dir_gdir, fv);
	}


  return newFeatureCount;
}


// ����ǵ�һ�����ӣ���ôhead_id == child1_id
// ��������һ�����ӣ���ôchild1_id == child2_id
int DepPipe::addFeature_sib( DepInstance *pInstance, const int par, const int ch1, const int ch2, FeatureVec &fv)
{
	if (par == 0) return 0; // [n+1]->[0->]

  int newFeatureCount = 0;

	vector<string> &forms = pInstance->forms;
  
	const vector<string> &cpostags = pInstance->cpostags;

	const bool isFirstChild = (par == ch1);
	const bool isLastChild = (ch1 == ch2);
	// ch1 is always the closes to par
	ARC_INFO arc_info;
	get_arc_info(pInstance, par, (isFirstChild ? ch2 : ch1), arc_info);
	//const string &dir_dist = arc_info.dir_dist;
	const string &dir = arc_info.dir;

	{
		const string &wrd_f = forms[par];
		const string &cpos_f = cpostags[par];
		const string &wrd1 = isFirstChild ? NO_FORM : forms[ch1];
		const string &cpos1 = isFirstChild ? NO_CPOSTAG : cpostags[ch1];
		const string &wrd2 = isLastChild ? NO_FORM : forms[ch2];
		const string &cpos2 = isLastChild ? NO_CPOSTAG : cpostags[ch2];


		string prefix = "S-";
		string feat = prefix + "1=" + cpos_f + FEAT_SEP + cpos1 + FEAT_SEP + cpos2;	// cpos_f cpos1 cpos2
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir, fv);
		// feats added 2009-01-08
		feat = prefix + "2=" + cpos_f + FEAT_SEP + wrd1 + FEAT_SEP + cpos2;		// cpos_f wrd1 cpos2
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir, fv);
		feat = prefix + "3=" + cpos_f + FEAT_SEP + cpos1 + FEAT_SEP + wrd2;		// cpos_f cpos1 wrd2
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir, fv);
		feat = prefix + "4=" + cpos_f + FEAT_SEP + wrd1 + FEAT_SEP + wrd2;		// cpos_f wrd1 wrd2
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir, fv);
		feat = prefix + "5=" + wrd_f + FEAT_SEP + cpos1 + FEAT_SEP + cpos2;		// wrd_f cpos1 cpos2
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir, fv);
		feat = prefix + "6=" + wrd_f + FEAT_SEP + wrd1 + FEAT_SEP + cpos2;		// wrd_f wrd1 cpos2
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir, fv);
		feat = prefix + "7=" + wrd_f + FEAT_SEP + cpos1 + FEAT_SEP + wrd2;		// wrd_f cpos1 wrd2
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir, fv);
		feat = prefix + "8=" + wrd_f + FEAT_SEP + wrd1 + FEAT_SEP + wrd2;		// wrd_f wrd1 wrd2
		newFeatureCount += add(feat, fv);
		feat += dir; newFeatureCount += add(feat, fv);


		prefix = "S2-"; // pairs
		feat = prefix + "1=" + cpos1 + FEAT_SEP + cpos2;	// cpos1 cpos2
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir, fv);
		feat = prefix + "2=" + wrd1 + FEAT_SEP + cpos2;		// wrd1 cpos2
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir, fv);
		feat = prefix + "3=" + cpos1 + FEAT_SEP + wrd2;		// cpos1 wrd2
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir, fv);
		feat = prefix + "4=" + wrd1 + FEAT_SEP + wrd2;		// wrd1 wrd2
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat + dir, fv);
	}

  return newFeatureCount;
}



int DepPipe::addFeature_grand( DepInstance *pInstance, const int par, const int mod, const int gchild, FeatureVec &fv)
{
	if (mod == 0) return 0; // [n+1]->[0->]

  int newFeatureCount = 0;

	vector<string> &forms = pInstance->forms;
	const vector<string> &cpostags = pInstance->cpostags;

	ARC_INFO arc_info;
	get_arc_info(pInstance, par, mod, arc_info);
	ARC_INFO arc_info_g;
	get_arc_info(pInstance, mod, gchild, arc_info_g);

	string dir_gdir = arc_info.dir + arc_info_g.dir;

	{
		const string &wrd_f = forms[par];
		const string &wrd_c = forms[mod];
		const string &wrd_g = forms[gchild];
		const string &cpos_f = cpostags[par];
		const string &cpos_c = cpostags[mod];
		const string &cpos_g = cpostags[gchild];

		string prefix = "G-";
		string feat;

		feat = prefix + "0=" + cpos_f + FEAT_SEP + cpos_c + FEAT_SEP + cpos_g;	// pos-f pos-c pos-g
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat+dir_gdir, fv);
		feat = prefix + "1=" + wrd_f + FEAT_SEP + cpos_c + FEAT_SEP + cpos_g;	// wrd-f pos-c pos-g
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat+dir_gdir, fv);
		feat = prefix + "2=" + cpos_f + FEAT_SEP + wrd_c + FEAT_SEP + cpos_g;	// pos-f wrd-c pos-g
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat+dir_gdir, fv);
		feat = prefix + "3=" + cpos_f + FEAT_SEP + cpos_c + FEAT_SEP + wrd_g;	// pos-f pos-c wrd-g
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat+dir_gdir, fv);
		feat = prefix + "4=" + wrd_f + FEAT_SEP + cpos_c + FEAT_SEP + wrd_g;	// wrd-f pos-c wrd-g
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat+dir_gdir, fv);
		feat = prefix + "5=" + cpos_f + FEAT_SEP + wrd_c + FEAT_SEP + wrd_g;	// pos-f wrd-c wrd-g
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat+dir_gdir, fv);


		prefix = "G2-"; // pair
		feat = prefix + "0=" + wrd_f + FEAT_SEP + wrd_g;	// wrd-f wrd-g
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat+dir_gdir, fv);
		feat = prefix + "1=" + cpos_f + FEAT_SEP + cpos_g;	// pos-f pos-g
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat+dir_gdir, fv);
		feat = prefix + "2=" + wrd_f + FEAT_SEP + cpos_g;	// wrd-f pos-g
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat+dir_gdir, fv);
		feat = prefix + "3=" + cpos_f + FEAT_SEP + wrd_g;	// pos-f wrd-g
		newFeatureCount += add(feat, fv); newFeatureCount += add(feat+dir_gdir, fv);
	}

  return newFeatureCount;
}

void DepPipe::sentenceSimplification()
{
	
  vector<int> instanceLength;
  createAlphabet(instanceLength);

  //m_featAlphabet.allowGrowth();
	//m_labelAlphabet.allowGrowth();

	initInputFile(options.m_strTrainFile.c_str());
	initOutputFile(options.m_strOutFile.c_str());

	DepInstance *pInstance = nextInstance();
	int numInstance = 0;
	int newInstanceCount = 0;
	while (pInstance) 
	{
		if (++numInstance % options.m_display_interval == 0) cout << numInstance << " ";

		//createSpan(pInstance);

		/*
		DepInstance goldInstance;	
		goldInstance.copyValuesFrom(*pInstance);
		m_vecInstances.push_back(goldInstance);
		*/
		
		//outputInstance(pInstance);
		//newInstanceCount++;
    vector<int> deleteNodes;
    pInstance->findDeleteNodes(rules, deleteNodes);
    
    if(deleteNodes.size() > 0)
    {
      DepInstance curSimpInstance;
			pInstance->deleteNode(deleteNodes, curSimpInstance);
			if(curSimpInstance.orig_size() > 1)
			{
        m_featAlphabet.stopGrowth();
	      m_labelAlphabet.stopGrowth();
        int newFeaturesNum = createFeatureVector(&curSimpInstance);
        m_featAlphabet.allowGrowth();
        m_labelAlphabet.allowGrowth();
        if(newFeaturesNum >= options.m_reduction_iter)
        {
          newFeaturesNum = createFeatureVector(&curSimpInstance);
          cout << "current feature num : " <<  m_featAlphabet.size() <<"\tnew feature number added for instance  " << newInstanceCount <<" : " << newFeaturesNum << endl;
				  outputInstance(&curSimpInstance);
				  newInstanceCount++;
        }
        else
        {
          cout << "no new features be generated by the simp!" << endl;
        }
			}
    }

    if(deleteNodes.size() > 1)
    {
      for(int i = 0; i < deleteNodes.size(); i++ )
		  {
        vector<int> curDeleteNodes;
        curDeleteNodes.push_back(deleteNodes[i]);
			  DepInstance curSimpInstance;
			  pInstance->deleteNode(curDeleteNodes, curSimpInstance);

        if(curSimpInstance.orig_size() > 1)
			  {
          m_featAlphabet.stopGrowth();
	        m_labelAlphabet.stopGrowth();
          int newFeaturesNum = createFeatureVector(&curSimpInstance);
          m_featAlphabet.allowGrowth();
          m_labelAlphabet.allowGrowth();
          if(newFeaturesNum >= options.m_reduction_iter)
          {
            newFeaturesNum = createFeatureVector(&curSimpInstance);
            cout << "current feature num : " <<  m_featAlphabet.size() <<"\tnew feature number added for instance  " << newInstanceCount <<" : " << newFeaturesNum << endl;
				    outputInstance(&curSimpInstance);
				    newInstanceCount++;
          }
          else
          {
            cout << "no new features be generated by the simp!" << endl;
          }
			  }
		  }
    }



		if ( options.m_numMaxInstance > 0 && numInstance == options.m_numMaxInstance) break;
		pInstance = nextInstance();
	}

	uninitInputFile();
	uninitOutputFile();

	cout << endl;
	cout << "instance num: " << numInstance << endl;
	cout << "new instance num: " << newInstanceCount << endl;

}


void DepPipe::fillFeatureVectors(DepInstance *instance, FVS_PROBS* pFvsProbs,	const Parameter &params)
{

    fillFeatureVectors(instance, pFvsProbs, params, 0, instance->orig_size());

}


void DepPipe::rerankWithGlobalFeature(DepInstance &inst, vector<FeatureVec>& d0, vector<string>& d1, const Parameter &params)
{
  DepInstance tempInst;
  DepInstance* pInstance = &tempInst;
  inst.copyValuesTo(tempInst);
  
  int validResults = d1.size();

  for(int k = 0; k < d1.size(); k++) 
  {
		if (d1[k].empty()) 
    {
			validResults = k;
			break;
		}
  }
  vector<double> globalScores;
  globalScores.resize(validResults);
  for(int i = 0; i < validResults; i++)
  {
    DepInstance::fillInstance(*pInstance, d1[i], m_vecTypes);
    vector<int> deleteNodes;
    pInstance->findDeleteNodes(rules, deleteNodes);
    
    if(deleteNodes.size() > 0)
    {
      DepInstance curSimpInstance;
		  pInstance->deleteNode(deleteNodes, curSimpInstance);
		  if(curSimpInstance.orig_size() > 1)
		  {
        m_bSimpFeature = true;
        createFeatureVector(&curSimpInstance, d0[i]);
        m_bSimpFeature = false;
      }
    }
    
    globalScores[i] = params.getScore(d0[i]);    
  }

  for(int i = 0; i < validResults; i++)
  {
    int largestScore = i;
    for(int j = i+1; j < validResults; j++)
    {
      if(globalScores[j] > globalScores[largestScore])
      {
        largestScore = j;
      }
    }
    if(largestScore != i)
    {
      double tempScore = globalScores[i];
      string tempstring = d1[i];
      FeatureVec tempFV = d0[i];
      globalScores[i] = globalScores[largestScore];
      d1[i] = d1[largestScore];
      d0[i] =  d0[largestScore];
      globalScores[largestScore] = tempScore;
      d1[largestScore] = tempstring;
      d0[largestScore] = tempFV;
    }

  }
  
}
