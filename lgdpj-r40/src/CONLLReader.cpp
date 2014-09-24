#include "CONLLReader.h"
#include "CharUtils.h"
#include "CppAssert.h"

#include <sstream>
using namespace std;

namespace dparser {


	void CONLLReader::decompose_sent( Instance * const inst )
	{
		reset_sent(inst, m_vecLine.size()+1);

		for (int i = 0; i < m_vecLine.size(); ++i) {
			vector<string> tokens;
			egstra::simpleTokenize(m_vecLine[i], tokens, " \t");
			RVASSERT(tokens.size() >= 10, "Invalid corpus line: " << m_vecLine[i]);

			inst->forms[i+1] = tokens[1];
			inst->orig_lemmas[i+1] = tokens[2];
			inst->orig_cpostags[i+1] = tokens[3];
			inst->postags[i+1] = tokens[4];

			inst->orig_feats[i+1] = tokens[5];

			inst->heads[i+1] = egstra::toInteger(tokens[6]);
			inst->deprels[i+1] = tokens[7];
			inst->pheads[i+1] = tokens[8];
			inst->pdeprels[i+1] = tokens[9];
		}
		inst->cpostags = inst->orig_cpostags;
	}

	void CONLLReader::reset_sent( Instance * const inst, const int length )
	{
		inst->forms.resize(length); 
		inst->orig_lemmas.resize(length);
		inst->orig_cpostags.resize(length);
		inst->postags.resize(length);
		inst->orig_feats.resize(length);
		inst->heads.resize(length);
		inst->deprels.resize(length);
		inst->pheads.resize(length);
		inst->pdeprels.resize(length);

		inst->forms[0] = ROOT_FORM;
		inst->forms[1] = ROOT_FORM;
		inst->orig_lemmas[0] = ROOT_LEMMA;
		inst->orig_cpostags[0] = ROOT_CPOSTAG;
		inst->postags[0] = ROOT_POSTAG;
		inst->orig_feats[0] = ROOT_FEAT;
		inst->heads[0] = ROOT_HEAD;
		inst->deprels[0] = ROOT_DEPREL;
		inst->pheads[0] = ROOT_HEAD;
		inst->pdeprels[0] = ROOT_DEPREL;
	}

	int CONLLReader::read_lines()
	{
		m_vecLine.clear();
		while (1) {
			string strLine;
			if (!egstra::my_getline(m_inf, strLine)) {
				break;
			}
			if (strLine.empty()) break;
			m_vecLine.push_back(strLine);
		}

		return m_vecLine.size();
	}

} // namespace dparser



