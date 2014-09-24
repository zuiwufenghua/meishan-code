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
			RVASSERT(tokens.size() ==4, "Invalid corpus line: " << m_vecLine[i]);

			inst->forms[i+1] = tokens[0];
			inst->lemmas[i+1] = tokens[0];
			inst->cpostags[i+1] = tokens[1];
			inst->postags[i+1] = tokens[1];
			inst->heads[i+1] = egstra::toInteger(tokens[2]) + 1;
			inst->deprels[i+1] = tokens[3];

		}
	}

	void CONLLReader::reset_sent( Instance * const inst, const int length )
	{
		inst->forms.resize(length);
    inst->lemmas.resize(length); 
    inst->cpostags.resize(length);
		inst->postags.resize(length);
		inst->heads.resize(length);
		inst->deprels.resize(length);


		inst->forms[0] = ROOT_FORM;
		inst->lemmas[0] = ROOT_FORM;
		inst->cpostags[0] = ROOT_CPOSTAG;
		inst->postags[0] = ROOT_POSTAG;
		inst->heads[0] = ROOT_HEAD;
		inst->deprels[0] = ROOT_DEPREL;
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



