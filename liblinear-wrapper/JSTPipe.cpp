#include "JSTPipe.h"
#include "MyVector.h"
#include <iterator>

using namespace std;


JSTPipe::JSTPipe(const JSTOptions &_options) : options(_options)
{
	m_jstReader = 0;
	m_jstReader = new CONLLReader();
	m_jstWriter = new CONLLWriter();

}

JSTPipe::~JSTPipe(void)
{
	if (m_jstReader) delete m_jstReader;
	if (m_jstWriter) delete m_jstWriter;

}

int JSTPipe::initInputFile(const char *filename) {
	if (0 != m_jstReader->startReading(filename)) return -1;
	return 0;
}

void JSTPipe::uninitInputFile() {
	if (m_jstWriter) m_jstReader->finishReading();
}

int JSTPipe::initOutputFile(const char *filename) {
	if (0 != m_jstWriter->startWriting(filename)) return -1;
	return 0;
}

void JSTPipe::uninitOutputFile() {
	if (m_jstWriter) m_jstWriter->finishWriting();
}

int JSTPipe::outputInstance(const JSTInstance *pInstance) {
	if (0 != m_jstWriter->write(pInstance)) return -1;
	return 0;
}


const char *JSTPipe::getType(int typeIndex) {
	if (typeIndex >= 0 && typeIndex < m_vecTypes.size()) {
		return m_vecTypes[typeIndex].c_str();
	} else {
		return "";
	}
}


int JSTPipe::createAlphabet()
{
	cout << "Creating Alphabet..." << endl;

	m_featAlphabet.allowGrowth();
	m_labelAlphabet.allowGrowth();
  int numInstance;
  map<string, int> feature_stat;

  for(numInstance = 0; numInstance < m_vecInstances.size(); numInstance++)
  {
		JSTInstance *pInstance = &m_vecInstances[numInstance];
		featureStatistic(feature_stat, pInstance->sentence, pInstance->sentenceTags);

		const vector<string> &sentenceTags = pInstance->sentenceTags;
    pInstance->sentenceTagIds.resize(sentenceTags.size());
		int i = 0;
		for (; i < sentenceTags.size(); ++i) {
			int id = m_labelAlphabet.lookupIndex(sentenceTags[i]);
      pInstance->sentenceTagIds[i] = id;
		}
    
    if ( (numInstance+1) % options.m_display_interval == 0) 
    {
      cout << numInstance  + 1<< " ";
      cout.flush();
    }
		if ( options.m_numMaxInstance > 0 && numInstance == options.m_numMaxInstance) break;
	}

	cout << numInstance << " " << endl;
  cout << "label num: " <<  m_labelAlphabet.size() << endl;
  cout << "Total Features num: " <<  feature_stat.size() << endl;
  
  map<string, int>::iterator feat_iter;
  for(feat_iter = feature_stat.begin(); feat_iter != feature_stat.end(); feat_iter++)
  {
    if(feat_iter->second > options.m_featFreqCutNum)
    {
      FeatureVec fv;
      add(feat_iter->first,1.0, fv);
    }
  }

  cout << "Remain Features num: " <<  m_featAlphabet.size() << endl;

  closeAlphabet();

  for(numInstance = 0; numInstance < m_vecInstances.size(); numInstance++)
  {
		JSTInstance *pInstance = &m_vecInstances[numInstance];
		initSentenceFeatures(pInstance->fvs, pInstance->sentence, pInstance->sentenceTags);
    
    if ( (numInstance+1) % options.m_display_interval == 0) 
    {
      cout << numInstance  + 1<< " ";
      cout.flush();
    }
		if ( options.m_numMaxInstance > 0 && numInstance == options.m_numMaxInstance) break;
	}

	cout << numInstance << " " << endl;

	
	
	return 0;
}


void JSTPipe::closeAlphabet() {
	m_featAlphabet.stopGrowth();	
	m_featAlphabet.collectKeys();	
	m_featAlphabet.fillTrie();

  m_labelAlphabet.stopGrowth();
  m_labelAlphabet.collectKeys();
  m_labelAlphabet.fillTrie();
	mapTypes();
}

void JSTPipe::mapTypes() {
	m_vecTypes.resize(m_labelAlphabet.size());
	vector<string> vecKeys;
	m_labelAlphabet.getKeys(vecKeys);
	int i = 0;
	for(; i < vecKeys.size(); ++i) {
		int idx = m_labelAlphabet.lookupIndex(vecKeys[i]);
		if (idx < 0 || idx >= m_labelAlphabet.size()) 
    {
			cout << "m_labelAlphabet err: " << vecKeys[i] << " : " << idx << endl;
			continue;
		}
		m_vecTypes[idx] = vecKeys[i];
	}
}

JSTInstance *JSTPipe::nextInstance()
{
	JSTInstance *pInstance = m_jstReader->getNext();
	if (!pInstance || pInstance->words.empty()) return 0;

	return pInstance;
}

bool JSTPipe::getNextRawSentence(vector<string>& sentence)
{
	return m_jstReader->getNextRawSentence(sentence);
}

void JSTPipe::readTrainInstances()
{
  initInputFile(options.m_strTrainFile.c_str());

  JSTInstance *pInstance = nextInstance();
	int numInstance = 0;
  totalChars = 0;
	while (pInstance) {
		if (++numInstance % options.m_display_interval == 0) 
    {
      cout << numInstance << " ";
      cout.flush();
    }
    JSTInstance trainInstance;	
		trainInstance.copyValuesFrom(*pInstance);
    m_vecInstances.push_back(trainInstance);
    
    for(int i = 0; i < trainInstance.words.size(); i++)
    {
      if(m_goldlexicon.find (pInstance->words[i]) != m_goldlexicon.end())
		  {
		      m_goldlexicon[pInstance->words[i]].insert(pInstance->labels[i]);
		  }
		  else
		  {
	      set<string> theWordLabels;
        theWordLabels.insert(pInstance->labels[i]);
	      m_goldlexicon.insert(pair<string, set<string> >(pInstance->words[i], theWordLabels));
		  }
    }
    totalChars += pInstance->sentence.size();

    if ( options.m_numMaxInstance > 0 && numInstance == options.m_numMaxInstance) break;

    pInstance = nextInstance();

  }
  
  uninitInputFile();

	cout << numInstance << endl;
}



void JSTPipe::initLexicon()
{
  m_labelAlphabet.allowGrowth();
  m_dict.clear();
  m_bidict.clear();
  m_bilexicon.clear();
  m_lexicon.clear();
  m_chlexicon.clear();

  initInputFile(options.m_strDictFile.c_str());
  string strLine;
  while( (m_jstReader->getNextRawSentence(strLine)) == true)
  {
    vector<string> word_labels;
    split_bychar(strLine, word_labels, ' ');
    if(word_labels.size() <= 1)
    {
      continue;
    }
    if(word_labels[0] == "word")
    {
      if(word_labels.size() == 2)
      {
        m_dict.insert(word_labels[1]);
      }
      else
      {
        m_dict.insert(word_labels[1]);
        if(m_lexicon.find(word_labels[1]) != m_lexicon.end())
        {
          for(int i = 2; i < word_labels.size(); i++)
          {
            m_lexicon[word_labels[1]].insert(word_labels[i]);
           // m_labelAlphabet.lookupIndex(word_labels[i]);
          }
        }
        else
        {
          set<string> theWordLabels;
          for(int i = 2; i < word_labels.size(); i++)
          {
            theWordLabels.insert(word_labels[i]);
            //m_labelAlphabet.lookupIndex(word_labels[i]);
          }
          m_lexicon.insert(pair<string, set<string> >(word_labels[1], theWordLabels));
        }
      }
    }

    if(word_labels[0] == "biword")
    {
      if(word_labels.size() == 2)
      {
        m_bidict.insert(word_labels[1]);
      }
    }

    if(word_labels[0] == "biwordlabel")
    {
      if(word_labels.size() <= 2)
      {
        continue;
      }
      
      if(m_bilexicon.find(word_labels[1]) != m_bilexicon.end())
      {
        for(int i = 2; i < word_labels.size(); i++)
        {
          m_bilexicon[word_labels[1]].insert(word_labels[i]);
          //m_labelAlphabet.lookupIndex(word_labels[i]);
        }
      }
      else
      {
        set<string> theWordLabels;
        for(int i = 2; i < word_labels.size(); i++)
        {
          theWordLabels.insert(word_labels[i]);
          //m_labelAlphabet.lookupIndex(word_labels[i]);
        }
        m_bilexicon.insert(pair<string, set<string> >(word_labels[1], theWordLabels));
      }
    }


    if(word_labels[0] == "char")
    {
      if(word_labels.size() <= 2)
      {
        continue;
      }
      
      if(m_chlexicon.find(word_labels[1]) != m_chlexicon.end())
      {
        for(int i = 2; i < word_labels.size(); i++)
        {
          m_chlexicon[word_labels[1]].insert(word_labels[i]);
          //m_labelAlphabet.lookupIndex(word_labels[i]);
        }
      }
      else
      {
        set<string> theWordLabels;
        for(int i = 2; i < word_labels.size(); i++)
        {
          theWordLabels.insert(word_labels[i]);
          //m_labelAlphabet.lookupIndex(word_labels[i]);
        }
        m_chlexicon.insert(pair<string, set<string> >(word_labels[1], theWordLabels));
      }
    }
    
  }

  uninitInputFile();
  
}


int JSTPipe::add(const string &feat, FeatureVec &fv) 
{
  string truefeat = "" + feat;
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




int JSTPipe::add(const string &feat, double value, FeatureVec &fv) 
{
  string truefeat = "" + feat;
  bool bAppend = false;
	int num = m_featAlphabet.lookupIndex(truefeat, bAppend);
	if (num >= 0) 
  {
		fv.add(num, value);
    if(bAppend) return 1;
    return 0;
	}
  else
  {
    return 1;
  }
}

void JSTPipe::featureStatistic(map<string,int>& fvstat, const vector<string>& sentence, const vector<string>& sentenceTags)
{
  int newFeatureCount = 0;
  int sentLength = sentence.size();
  for(int i = 0; i < sentLength; i++)  
  {
    string prevChar = i-1 >= 0 ? sentence[i-1] : startChar;
    string prev2Char = i-2 >= 0 ? sentence[i-2] : startChar;
    string nextChar = i+1 < sentence.size() ? sentence[i+1] : endChar;
    string next2Char = i+2 < sentence.size() ? sentence[i+2] : endChar;
    string curChar = sentence[i];

    string curCharType = getUTF8CharType(curChar);
    string prevCharType = i-1 >= 0 ? getUTF8CharType(prevChar) : startChar;
    string nextCharType = i+1 < sentence.size() ? getUTF8CharType(nextChar) : startChar;
        
    //segment basic

    string feat = "F0="+prev2Char;
    fvstat[feat]++;
    feat = "F1="+prevChar;
    fvstat[feat]++;
    feat = "F2="+curChar;
    fvstat[feat]++;
    feat = "F3="+nextChar;
    fvstat[feat]++;
    feat = "F4="+next2Char;
    fvstat[feat]++;


	  feat = "F5="+prev2Char+seperateCH+prevChar;
    fvstat[feat]++;
    feat = "F6="+prevChar+seperateCH+curChar;
    fvstat[feat]++;
    feat = "F7="+curChar+seperateCH+nextChar;
    fvstat[feat]++;
    feat = "F8="+nextChar+seperateCH+next2Char;
    fvstat[feat]++;


	  feat = "F9="+prev2Char+seperateCH+curChar;
    fvstat[feat]++;
    feat = "F10="+prevChar+seperateCH+nextChar;
    fvstat[feat]++;
    feat = "F11="+curChar+seperateCH+next2Char;
    fvstat[feat]++;

    
    feat = "LF0="+prev2Char+seperateCH+curChar;
    fvstat[feat]++;
    feat = "LF1="+prevChar+seperateCH+curChar;
    fvstat[feat]++;
    feat = "LF2="+curChar+seperateCH+curChar;
    fvstat[feat]++;
    feat = "LF3="+nextChar+seperateCH+curChar;
    fvstat[feat]++;
    feat = "LF4="+next2Char+seperateCH+curChar;
    fvstat[feat]++;


	  feat = "LF5="+prev2Char+seperateCH+prevChar+seperateCH+curChar;
    fvstat[feat]++;
    feat = "LF6="+prevChar+seperateCH+curChar+seperateCH+curChar;
    fvstat[feat]++;
    feat = "LF7="+curChar+seperateCH+nextChar+seperateCH+curChar;
    fvstat[feat]++;
    feat = "LF8="+nextChar+seperateCH+next2Char+seperateCH+curChar;
    fvstat[feat]++;


	  feat = "LF9="+prev2Char+seperateCH+curChar+seperateCH+curChar;
    fvstat[feat]++;
    feat = "LF10="+prevChar+seperateCH+nextChar+seperateCH+curChar;
    fvstat[feat]++;
    feat = "LF11="+curChar+seperateCH+next2Char+seperateCH+curChar;
    fvstat[feat]++;


    feat = "F12="+prevCharType;
    fvstat[feat]++;
    feat = "F13="+curCharType;
    fvstat[feat]++;
    feat = "F14="+nextCharType;
    fvstat[feat]++;

    feat = "F15="+prevCharType+seperateCH+curCharType;
    fvstat[feat]++;
    feat = "F16="+curCharType+seperateCH+nextCharType;
    fvstat[feat]++;

    feat = "F17="+prevCharType+seperateCH+curCharType+seperateCH+nextCharType;
    fvstat[feat]++;


    if(curChar == prev2Char)
    {
  	  feat = "F18";
      fvstat[feat]++;
      feat = "LF18="+curChar;
      fvstat[feat]++;
    }
    if(prevChar == nextChar)
    {
  	  feat = "F19";  
      fvstat[feat]++;
      feat = "LF19="+curChar; 
      fvstat[feat]++;
    }
    if(curChar == next2Char)
    {
  	  feat = "F20";
      fvstat[feat]++;
      feat = "LF20="+curChar;
      fvstat[feat]++;
    }

    if(curChar == prevChar)
    {
  	  feat = "F21";
      fvstat[feat]++;
      feat = "LF21="+curChar;
      fvstat[feat]++;
    }
    if(curChar == nextChar)
    {
  	  feat = "F22"; 
      fvstat[feat]++;
      feat = "LF22="+curChar;
      fvstat[feat]++;
    }

    
    {
      string prevTag = i-1 >= 0 ? sentenceTags[i-1] : startTag;

      //feat = "F100="+prevTag;
      //fvstat[feat]++;
      feat = "F101="+prevTag+seperateCH+curChar;
      fvstat[feat]++;
      feat = "F102="+prevTag+seperateCH+curChar+seperateCH+prevChar;
      fvstat[feat]++;

    }

    
    {     
      int preSep = -1;
      for(int j =i-1; j >= 0; j--)
      {
        if(sentenceTags[j].substr(0,2) == "B-")
        {
          preSep = j;
          break;
        }
      }
      if(preSep < 0 && i > 0)preSep = 0;

      int pre2Sep = -1;
      for(int j =preSep-1; j >= 0; j--)
      {
        if(sentenceTags[j].substr(0,2) == "B-")
        {
          preSep = j;
          break;
        }
      }
      if(pre2Sep < 0 && preSep > 0)pre2Sep = 0;

      ostringstream out;
      int prevParitalWordLength = 0;
      if(preSep>= 0) prevParitalWordLength = i - preSep;
      if(prevParitalWordLength > 5) prevParitalWordLength = 5;
      out << prevParitalWordLength;

      feat = "PrevParitalWordLength="+out.str();
      fvstat[feat]++;
      feat = "PrevParitalWordLength&Char="+out.str() +seperateCH+curChar;
      fvstat[feat]++;
     
    }

    
  }

}


int JSTPipe::initSentenceFeatures(vector<FeatureVec> &fvs, const vector<string>& sentence, const vector<string>& sentenceTags)
{
  int newFeatureCount = 0;
  int sentLength = sentence.size();
  fvs.resize(sentLength);
  for(int i = 0; i < sentLength; i++)
  {
    fvs[i].clear();
    string prevChar = i-1 >= 0 ? sentence[i-1] : startChar;
    string prev2Char = i-2 >= 0 ? sentence[i-2] : startChar;
    string nextChar = i+1 < sentence.size() ? sentence[i+1] : endChar;
    string next2Char = i+2 < sentence.size() ? sentence[i+2] : endChar;
    string curChar = sentence[i];

    string curCharType = getUTF8CharType(curChar);
    string prevCharType = i-1 >= 0 ? getUTF8CharType(prevChar) : startChar;
    string nextCharType = i+1 < sentence.size() ? getUTF8CharType(nextChar) : startChar;
        
    //segment basic

    string feat = "F0="+prev2Char;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "F1="+prevChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "F2="+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "F3="+nextChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "F4="+next2Char;
    newFeatureCount += add(feat,1.0,fvs[i]);


	  feat = "F5="+prev2Char+seperateCH+prevChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "F6="+prevChar+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "F7="+curChar+seperateCH+nextChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "F8="+nextChar+seperateCH+next2Char;
    newFeatureCount += add(feat,1.0,fvs[i]);


	  feat = "F9="+prev2Char+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "F10="+prevChar+seperateCH+nextChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "F11="+curChar+seperateCH+next2Char;
    newFeatureCount += add(feat,1.0,fvs[i]);

    feat = "LF0="+prev2Char+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "LF1="+prevChar+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "LF2="+curChar+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "LF3="+nextChar+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "LF4="+next2Char+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);


	  feat = "LF5="+prev2Char+seperateCH+prevChar+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "LF6="+prevChar+seperateCH+curChar+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "LF7="+curChar+seperateCH+nextChar+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "LF8="+nextChar+seperateCH+next2Char+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);


	  feat = "LF9="+prev2Char+seperateCH+curChar+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "LF10="+prevChar+seperateCH+nextChar+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "LF11="+curChar+seperateCH+next2Char+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);


    feat = "F12="+prevCharType;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "F13="+curCharType;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "F14="+nextCharType;
    newFeatureCount += add(feat,1.0,fvs[i]);

    feat = "F15="+prevCharType+seperateCH+curCharType;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "F16="+curCharType+seperateCH+nextCharType;
    newFeatureCount += add(feat,1.0,fvs[i]);

    feat = "F17="+prevCharType+seperateCH+curCharType+seperateCH+nextCharType;
    newFeatureCount += add(feat,1.0,fvs[i]);


    if(curChar == prev2Char)
    {
  	  feat = "F18";
      newFeatureCount += add(feat,1.0,fvs[i]);
      feat = "LF18="+curChar;
      newFeatureCount += add(feat,1.0,fvs[i]);
    }
    if(prevChar == nextChar)
    {
  	  feat = "F19";  
      newFeatureCount += add(feat,1.0,fvs[i]);
      feat = "LF19="+curChar; 
      newFeatureCount += add(feat,1.0,fvs[i]);
    }
    if(curChar == next2Char)
    {
  	  feat = "F20";
      newFeatureCount += add(feat,1.0,fvs[i]);
      feat = "LF20="+curChar;
      newFeatureCount += add(feat,1.0,fvs[i]);
    }

    if(curChar == prevChar)
    {
  	  feat = "F21";
      newFeatureCount += add(feat,1.0,fvs[i]);
      feat = "LF21="+curChar;
      newFeatureCount += add(feat,1.0,fvs[i]);
    }
    if(curChar == nextChar)
    {
  	  feat = "F22"; 
      newFeatureCount += add(feat,1.0,fvs[i]);
      feat = "LF22="+curChar;
      newFeatureCount += add(feat,1.0,fvs[i]);
    }

    
    {
      string prevTag = i-1 >= 0 ? sentenceTags[i-1] : startTag;    

      feat = "F100="+prevTag;
      newFeatureCount += add(feat,1.0,fvs[i]);
      feat = "F101="+prevTag+seperateCH+curChar;
      newFeatureCount += add(feat,1.0,fvs[i]);
      feat = "F102="+prevTag+seperateCH+curChar+seperateCH+prevChar;
      newFeatureCount += add(feat,1.0,fvs[i]);

    }

    
    {     
      int preSep = -1;
      for(int j =i-1; j >= 0; j--)
      {
        if(sentenceTags[j].substr(0,2) == "B-")
        {
          preSep = j;
          break;
        }
      }
      if(preSep < 0 && i > 0)preSep = 0;

      int pre2Sep = -1;
      for(int j =preSep-1; j >= 0; j--)
      {
        if(sentenceTags[j].substr(0,2) == "B-")
        {
          preSep = j;
          break;
        }
      }
      if(pre2Sep < 0 && preSep > 0)pre2Sep = 0;

      ostringstream out;
      int prevParitalWordLength = 0;
      if(preSep>= 0) prevParitalWordLength = i - preSep;
      if(prevParitalWordLength > 5) prevParitalWordLength = 5;
      out << prevParitalWordLength;

      feat = "PrevParitalWordLength="+out.str();
      newFeatureCount += add(feat,1.0,fvs[i]);
      feat = "PrevParitalWordLength&Char="+out.str() +seperateCH+curChar;
      newFeatureCount += add(feat,1.0,fvs[i]);
    }
    
  }

  return newFeatureCount;
}


int JSTPipe::initSentenceFeatures(vector<FeatureVec> &fvs, const vector<string>& sentence, const vector<string>& sentenceTags, int i)
{
  int newFeatureCount = 0;
  int sentLength = sentence.size();
  //fvs.resize(sentLength);
  //for(int i = 0; i < sentLength; i++)
  {
    fvs[i].clear();
    string prevChar = i-1 >= 0 ? sentence[i-1] : startChar;
    string prev2Char = i-2 >= 0 ? sentence[i-2] : startChar;
    string nextChar = i+1 < sentence.size() ? sentence[i+1] : endChar;
    string next2Char = i+2 < sentence.size() ? sentence[i+2] : endChar;
    string curChar = sentence[i];

    string curCharType = getUTF8CharType(curChar);
    string prevCharType = i-1 >= 0 ? getUTF8CharType(prevChar) : startChar;
    string nextCharType = i+1 < sentence.size() ? getUTF8CharType(nextChar) : startChar;
        
    //segment basic

    string feat = "F0="+prev2Char;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "F1="+prevChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "F2="+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "F3="+nextChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "F4="+next2Char;
    newFeatureCount += add(feat,1.0,fvs[i]);


	  feat = "F5="+prev2Char+seperateCH+prevChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "F6="+prevChar+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "F7="+curChar+seperateCH+nextChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "F8="+nextChar+seperateCH+next2Char;
    newFeatureCount += add(feat,1.0,fvs[i]);


	  feat = "F9="+prev2Char+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "F10="+prevChar+seperateCH+nextChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "F11="+curChar+seperateCH+next2Char;
    newFeatureCount += add(feat,1.0,fvs[i]);

    
    feat = "LF0="+prev2Char+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "LF1="+prevChar+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "LF2="+curChar+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "LF3="+nextChar+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "LF4="+next2Char+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);


	  feat = "LF5="+prev2Char+seperateCH+prevChar+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "LF6="+prevChar+seperateCH+curChar+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "LF7="+curChar+seperateCH+nextChar+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "LF8="+nextChar+seperateCH+next2Char+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);


	  feat = "LF9="+prev2Char+seperateCH+curChar+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "LF10="+prevChar+seperateCH+nextChar+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "LF11="+curChar+seperateCH+next2Char+seperateCH+curChar;
    newFeatureCount += add(feat,1.0,fvs[i]);


    feat = "F12="+prevCharType;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "F13="+curCharType;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "F14="+nextCharType;
    newFeatureCount += add(feat,1.0,fvs[i]);

    feat = "F15="+prevCharType+seperateCH+curCharType;
    newFeatureCount += add(feat,1.0,fvs[i]);
    feat = "F16="+curCharType+seperateCH+nextCharType;
    newFeatureCount += add(feat,1.0,fvs[i]);

    feat = "F17="+prevCharType+seperateCH+curCharType+seperateCH+nextCharType;
    newFeatureCount += add(feat,1.0,fvs[i]);


    if(curChar == prev2Char)
    {
  	  feat = "F18";
      newFeatureCount += add(feat,1.0,fvs[i]);
      feat = "LF18="+curChar;
      newFeatureCount += add(feat,1.0,fvs[i]);
    }
    if(prevChar == nextChar)
    {
  	  feat = "F19";  
      newFeatureCount += add(feat,1.0,fvs[i]);
      feat = "LF19="+curChar; 
      newFeatureCount += add(feat,1.0,fvs[i]);
    }
    if(curChar == next2Char)
    {
  	  feat = "F20";
      newFeatureCount += add(feat,1.0,fvs[i]);
      feat = "LF20="+curChar;
      newFeatureCount += add(feat,1.0,fvs[i]);
    }

    if(curChar == prevChar)
    {
  	  feat = "F21";
      newFeatureCount += add(feat,1.0,fvs[i]);
      feat = "LF21="+curChar;
      newFeatureCount += add(feat,1.0,fvs[i]);
    }
    if(curChar == nextChar)
    {
  	  feat = "F22"; 
      newFeatureCount += add(feat,1.0,fvs[i]);
      feat = "LF22="+curChar;
      newFeatureCount += add(feat,1.0,fvs[i]);
    }

    
    {
      string prevTag = i-1 >= 0 ? sentenceTags[i-1] : startTag;    

      feat = "F100="+prevTag;
      newFeatureCount += add(feat,1.0,fvs[i]);
      feat = "F101="+prevTag+seperateCH+curChar;
      newFeatureCount += add(feat,1.0,fvs[i]);
      feat = "F102="+prevTag+seperateCH+curChar+seperateCH+prevChar;
      newFeatureCount += add(feat,1.0,fvs[i]);

    }

    
    {     
      int preSep = -1;
      for(int j =i-1; j >= 0; j--)
      {
        if(sentenceTags[j].substr(0,2) == "B-")
        {
          preSep = j;
          break;
        }
      }
      if(preSep < 0 && i > 0)preSep = 0;

      int pre2Sep = -1;
      for(int j =preSep-1; j >= 0; j--)
      {
        if(sentenceTags[j].substr(0,2) == "B-")
        {
          preSep = j;
          break;
        }
      }
      if(pre2Sep < 0 && preSep > 0)pre2Sep = 0;

      ostringstream out;
      int prevParitalWordLength = 0;
      if(preSep>= 0) prevParitalWordLength = i - preSep;
      if(prevParitalWordLength > 5) prevParitalWordLength = 5;
      out << prevParitalWordLength;

      feat = "PrevParitalWordLength="+out.str();
      newFeatureCount += add(feat,1.0,fvs[i]);
      feat = "PrevParitalWordLength&Char="+out.str() +seperateCH+curChar;
      newFeatureCount += add(feat,1.0,fvs[i]);
    }
    
  }

  return newFeatureCount;
}

void JSTPipe::reco(const JSTInstance& inst, const vector<string>& predWords, const vector<string>& predLabels, const vector<string>& predSentenceTags, vector<int>& predRes)
{
  //seg: 0 goldWords 1 predWords
  //seg: 2 goldIVWords 3 predIVWords
  //seg: 4 goldOOVWords 5 predOOVWords
  //seg: 6 recoWords 7 recoIVWords 8 recoOOVWords
  //tag: 9 recoPos 10 recoIVPos 11 recoOOVPos
  //char: 12 charTotal 13 charCorrect
  predRes.clear();
  predRes.resize(14);

  for(int i = 0; i < 14; i++)
  {
    predRes[i] = 0;
  }

  vector<string> goldWords = inst.words;
  vector<string> goldLabels = inst.labels;
  //vector<string> predWords = predElem.words;
  //vector<string> predLabels = predElem.labels;


  int m = 0, n = 0;
  for(int i = 0; i < goldWords.size(); i++)
  {
    predRes[0]++;
    if(m_goldlexicon.find(goldWords[i]) != m_goldlexicon.end())
    {
      predRes[2]++;
    }
    else
    {
      predRes[4]++;
    }
  }

  for(int i = 0; i < predWords.size(); i++)
  {
    predRes[1]++;
    if(m_goldlexicon.find(predWords[i]) != m_goldlexicon.end())
    {
      predRes[3]++;
    }
    else
    {
      predRes[5]++;
    }
  }

  while (m < predWords.size() && n < goldWords.size())
  {
    if (predWords[m] == goldWords[n] )
    {
      predRes[6]++;
      bool bTagMatch = false;

      if(predLabels[m] == goldLabels[n])
      {
        bTagMatch = true;
        predRes[9]++;
      }
      if(m_goldlexicon.find(predWords[m]) != m_goldlexicon.end())
      {
          predRes[7]++;
          if(bTagMatch)
          {
            if(m_goldlexicon[predWords[m]].find(predLabels[m]) != m_goldlexicon[predWords[m]].end())
            {
	            predRes[10]++;
            }
            else
            {
	            predRes[11]++;
            }
          }
      }
      else
      {
        predRes[8]++;
        if(bTagMatch)
        {
          predRes[11]++;
        }
      }
      m++;
      n++;
    } 
    else
    {
      int lgold = goldWords[n].length();
      int lpred = predWords[m].length();
      int lm = m + 1;
      int ln = n + 1;
      int sm = m;
      int sn = n;

      while (lm < predWords.size() || ln < goldWords.size())
      {
        if (lgold > lpred && lm < predWords.size())
        {
            lpred = lpred + predWords[lm].length();
            sm = lm;
            lm++;
        }
        else if (lgold < lpred && ln < goldWords.size())
        {
            lgold = lgold + goldWords[ln].length();
            sn = ln;
            ln++;
        }
        else
        {
            break;
        }
      }

      m = sm + 1;
      n = sn + 1;
    }
  }
  
  predRes[12] = predSentenceTags.size();
  for(int i = 0; i < predSentenceTags.size(); i++)
  {
    if(predSentenceTags[i] == inst.sentenceTags[i])
    {
      predRes[13]++;
    }
  }

}


double JSTPipe::computeLoss(const JSTInstance& inst, const vector<string>& predWords, const vector<string>& predLabels, const vector<string>& predSentenceTags)
{
  vector<int> predRes;
  reco(inst, predWords, predLabels, predSentenceTags, predRes);
  //double errorValue = (predRes[0] + predRes[1] - 2.0 * predRes[6]) * 0.5;
  //errorValue = (predRes[0] + predRes[1] - 2.0 * predRes[9]) * 0.5;
  double errorValue = predRes[12] - predRes[13];
  return errorValue;
}