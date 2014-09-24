#include "DepInstance.h"

void DepInstance::findDeleteNodes(vector< vector<SimpRuleUnit> > rules, vector<int>& deleteNodes )
{
  deleteNodes.clear();
  int sentenceLength = forms.size();
  for(int i = 0; i < sentenceLength; i++)
  {
    
    int rulesNum = rules.size();
    for(int j = 0; j < rulesNum; j++)
    {
      int atomNum = rules[j].size();
      bool bSatisfied = true;

      for(int k = 0; bSatisfied && k < atomNum; k++)
      {
        int position = i;
        if(rules[j][k].arcdep && rules[j][k].position > 0)
        {
          for(int l = 0; l < rules[j][k].position; l++)
          {
            position = heads[position];
          }
        }
        else if(rules[j][k].arcdep && rules[j][k].position < 0)
        {
          //node i's child node, do not consider it currently
        }
        else
        {
          position += rules[j][k].position;
        }

    
        if(position < 0 || position >= sentenceLength)
        {
          bSatisfied = false;
          break;
        }

        string curValue = "";
        if(rules[j][k].type == 0)
        {
          curValue = forms[position];
          if(curValue != rules[j][k].value)
          {
            bSatisfied = false;
          }
        }
        else if (rules[j][k].type == 1)
        {
          curValue = cpostags[position];
          if(curValue.length() < rules[j][k].value.length() || curValue.substr(0, rules[j][k].value.length()) != rules[j][k].value)
          {
            bSatisfied = false;
          }
        }
        else if (rules[j][k].type == 2)
        {
          curValue = lemmas[position];
          if(curValue != rules[j][k].value)
          {
            bSatisfied = false;
          }
        }
        else
        {
          bSatisfied = false;   
          break;
        }

        if( !bSatisfied && !rules[j][k].bUnitT)
        {
           bSatisfied = true; 
        }

      }

      if(bSatisfied)
      {
        deleteNodes.push_back(i);
        break;
      }
    }
  }
}


void DepInstance::deleteNode(vector<int> nodeIds,DepInstance &depInstance)
{
		//copyValuesTo(depInstance);

		int sentenceLength = forms.size();
		set<int> childNodes;
    for(int i = 0; i < nodeIds.size(); i++)
    {
      childNodes.insert(nodeIds[i]);
    }
		
		for(int i = 0; i < sentenceLength; i++)
		{
      if(childNodes.find(i) != childNodes.end())continue;
			int par = heads[i];
			while(par >= 0 && par < sentenceLength)
			{
				if(childNodes.find(par) != childNodes.end())
				{
					childNodes.insert(i);
					break;
				}
				else
				{
					par = heads[par];
				}
			}
		}
    
    int lastPos = -1;
    int lastlastPos = -2;
    for(int i = 0; i < sentenceLength; i++)
    {
      if(childNodes.find(i) == childNodes.end())
      {
        if(lastPos == -1 && (forms[i] == "," || forms[i] == ";"))
        {
          childNodes.insert(i);
          continue;
        }
        if(lastPos >= 0 && (forms[lastPos] == "," || forms[lastPos] == ";") && (forms[i] == "," || forms[i] == ";"))
        {
          childNodes.insert(i);
          childNodes.insert(lastPos);
          lastPos = lastlastPos;
          lastlastPos = -1;
          continue;
        }
        lastlastPos = lastlastPos;
        lastlastPos = i;
      }
    }
		
		vector<int> id_changes;
		id_changes.resize(sentenceLength);
		for(int i = 0; i < sentenceLength; i++)
		{
			if(childNodes.find(i) != childNodes.end())
			{
				id_changes[i] = -1;
			}
			else
			{
				int count_deleted = 0;
				for(int j = 0; j < i; j++)
				{
					if(id_changes[j] == -1)
					{
						count_deleted++;
					}
				}
				id_changes[i] = i-count_deleted;
			}
		}

		depInstance.forms.resize(sentenceLength - childNodes.size());
		depInstance.lemmas.resize(sentenceLength - childNodes.size());
		depInstance.cpostags.resize(sentenceLength - childNodes.size());
		depInstance.postags.resize(sentenceLength - childNodes.size());
		depInstance.heads.resize(sentenceLength - childNodes.size());
		depInstance.deprels.resize(sentenceLength - childNodes.size());
		depInstance.feats.resize(sentenceLength - childNodes.size());

		for(int i = 0, j = 0; i < sentenceLength && j < sentenceLength - childNodes.size(); i++)
		{
			if(id_changes[i] == -1) continue;
			depInstance.forms[j] = forms[i];
			depInstance.lemmas[j] = lemmas[i];
			depInstance.cpostags[j] = cpostags[i];
			depInstance.postags[j] = postags[i];
			if(heads[i] == -1)
			{
				depInstance.heads[j] = -1;
			}
			else
			{
				depInstance.heads[j] = id_changes[ heads[i] ];
			}
			depInstance.deprels[j] = deprels[i];
			depInstance.feats[j].resize(feats[i].size());
			copy(feats[i].begin(), feats[i].end(), depInstance.feats[j].begin());
			j++;
		}


  const int length = depInstance.orig_size();

	depInstance.postags_for_bet_feat.clear();
  depInstance.verb_cnt.clear();
  depInstance.conj_cnt.clear();
  depInstance.punc_cnt.clear();
  depInstance.postags_for_bet_feat.resize(length, "");
  depInstance.verb_cnt.resize(length, 0);
  depInstance.conj_cnt.resize(length, 0);
  depInstance.punc_cnt.resize(length, 0);
  for (int i = 1; i < length; ++i) {  // for joint models: use the 1-best pos candidate
    depInstance.postags_for_bet_feat[i] =  depInstance.cpostags[i];
    const string &tag = depInstance.postags_for_bet_feat[i];
    depInstance.verb_cnt[i] = depInstance.verb_cnt[i-1];
    depInstance.conj_cnt[i] = depInstance.conj_cnt[i-1];
    depInstance.punc_cnt[i] = depInstance.punc_cnt[i-1];
    if(tag[0] == 'v' || tag[0] == 'V') {
      ++depInstance.verb_cnt[i];
    } else if( tag == "Punc" ||	tag == "PU" || tag == "," || tag == ":") {
      ++depInstance.punc_cnt[i];
    } else if( tag == "Conj" ||	tag == "CC" || tag == "cc") {
      ++depInstance.conj_cnt[i];
    }
  }

}

/*
void DepInstance::writeObject(ofstream &outf) const
{
	outf << "[inst]" << endl;
	copy(forms.begin(), forms.end(), ostream_iterator<string>(outf, "\t"));
	outf << endl;
	copy(lemmas.begin(), lemmas.end(), ostream_iterator<string>(outf, "\t"));
	outf << endl;
	copy(cpostags.begin(), cpostags.end(), ostream_iterator<string>(outf, "\t"));
	outf << endl;
	copy(postags.begin(), postags.end(), ostream_iterator<string>(outf, "\t"));
	outf << endl;
	int i = 0;
	for (; i < feats.size(); ++i) {
		copy(feats[i].begin(), feats[i].end(), ostream_iterator<string>(outf, "|"));
		outf << "\t";
	}
	outf << endl;
	copy(heads.begin(), heads.end(), ostream_iterator<int>(outf, "\t"));
	outf << endl;
	copy(deprels.begin(), deprels.end(), ostream_iterator<string>(outf, "\t"));
	outf << endl;
	outf << actParseTree << endl;
	outf << endl;
}

void DepInstance::readObject(ifstream &inf)
{
	string strLine;
	my_getline(inf, strLine);
	if (strLine != "[inst]") {
		cout << "DepInstance::readObject() err: " << strLine << endl;
		return;
	}
	my_getline(inf, strLine);
	split_bychar(strLine, forms, '\t');
	my_getline(inf, strLine);
	split_bychar(strLine, lemmas, '\t');
	my_getline(inf, strLine);
	split_bychar(strLine, cpostags, '\t');
	my_getline(inf, strLine);
	split_bychar(strLine, postags, '\t');

	vector<string> vec;
	my_getline(inf, strLine);
	split_bychar(strLine, vec, '\t');
	feats.resize(vec.size());
	int i = 0;
	for (; i < vec.size(); ++i) {
		split_bychar(vec[i], feats[i], '|');
	}

	my_getline(inf, strLine);
	split_bychar(strLine, vec, '\t');
	str2int_vec(vec, heads);

	my_getline(inf, strLine);
	split_bychar(strLine, deprels, '\t');

	my_getline(inf, actParseTree);
	my_getline(inf, strLine);
}
*/


void DepInstance::fillParseResult(const string &tree_span, vector<int> &heads, vector<string> &deprels, const vector<string>& vecTypes)
{
	vector<string> triples;
	split_bychar(tree_span, triples, ' ');

	int node_num = triples.size();

//	if (node_num + 1 !=
	heads.resize(node_num+1);		// heads[0] is not used.
	deprels.resize(node_num+1);

	int j = 1;
	for(; j < heads.size(); ++j) {
		int triple_idx = j - 1;
		vector<string> head_child_rel;
		split_bychars(triples[triple_idx], head_child_rel, "|:");

		if (head_child_rel.size() != 3) {
			cout << "tree span format err: " << triples[triple_idx] << endl;
			cout << "whole span: [" << tree_span << "]" << endl;
			deprels[j] = "ERR";
			heads[j] = -1;
			continue;
		}

		const string &strDepRelIdx = head_child_rel[2];
		const string &strHead = head_child_rel[0];

    

		int typeIdx = atoi(strDepRelIdx.c_str());
    if (typeIdx >= 0 && typeIdx < vecTypes.size()) {
		  deprels[j] = vecTypes[typeIdx];
    }
    else if(typeIdx == -1)
    {
      deprels[j] = "";
    }
    else
    {
			cout << "deprel err: idx = " << strDepRelIdx << endl;
		}

    
		heads[j] =atoi(strHead.c_str());
	}
}

void DepInstance::fillInstance_k(DepInstance &inst, const vector<string> d1, const vector<double> &parse_probs, const vector<string>& vecTypes)
{
	int i = 0;
	for (; i < parse_probs.size(); ++i) {
		if (parse_probs[i] < DOUBLE_NEGATIVE_INFINITY + EPS) {
			cout << "parse err: return only " << i << " results." << endl;
			break;
		}

		inst.k_probs.push_back(parse_probs[i]);
		inst.k_heads.push_back(vector<int>());
		inst.k_deprels.push_back(vector<string>());
		fillParseResult(d1[i], inst.k_heads.back(), inst.k_deprels.back(), vecTypes);
	}
}

void DepInstance::fillInstance(DepInstance &inst, const string &tree_span, const vector<string>& vecTypes)
{
	fillParseResult(tree_span, inst.heads, inst.deprels, vecTypes);
}



int DepInstance::fillInstance(DepInstance &inst,
				 const vector<string> &vecWord,
				 const vector<string> &vecCPOS)
{
	if (vecWord.empty() || vecWord.size() != vecCPOS.size()) {
		cout << "gparser param error: word, CPOS num not equal!" << endl;
		return -1;
	}

	inst.forms.resize(vecWord.size()+1);
	inst.lemmas.resize(vecWord.size()+1);
	inst.cpostags.resize(vecWord.size()+1);
	inst.postags.resize(vecWord.size()+1);
	inst.feats.resize(vecWord.size()+1);
	inst.heads.resize(vecWord.size()+1);
	inst.deprels.resize(vecWord.size()+1);

	inst.forms[0] = "<root>";
	inst.lemmas[0] = "<root-LEMMA>";
	inst.cpostags[0] = "<root-CPOS>";
	inst.deprels[0] = "<no-type>";
	inst.heads[0] = -1;

	copy(vecWord.begin(), vecWord.end(), inst.forms.begin()+1);
	copy(vecCPOS.begin(), vecCPOS.end(), inst.cpostags.begin()+1);
	return 0;
}

int DepInstance::getParseResult(const DepInstance &inst,
				   vector<int> &vecHead,
				   vector<string> &vecRel)
{
	if (inst.heads.size() != inst.deprels.size()) {
		cout << "gparser parse err: heads and deprels num not equal." << endl;
		return -1;
	}
	vecHead.resize(inst.heads.size()-1);
	vecRel.resize(inst.deprels.size()-1);
	copy(inst.heads.begin()+1, inst.heads.end(), vecHead.begin());
	copy(inst.deprels.begin()+1, inst.deprels.end(), vecRel.begin());
	return 0;
}

