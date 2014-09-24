#include "CONLLWriter.h"

#include <vector>
#include <sstream>
using namespace std;

namespace dparser {

	int CONLLWriter::write( const Instance *inst )
  {
		if (!m_outf.is_open()) return -1;

		for (int i = 1; i < inst->size(); ++i) {
			m_outf << inst->forms[i] << "\t" << inst->cpostags[i];
      for (int j = 0; j < inst->predicted_heads.nrows(); j++)
      {
				m_outf << "\t" << inst->predicted_heads[j][i]-1 << "\t" << inst->predicted_deprels[j][i];
      }
			m_outf << endl;
		}
		m_outf << endl;

		return 0;
	}

  int CONLLWriter::write( const Instance *inst, const NRVec<const char *> &labels,  const double &thres)
  {
		if (!m_outf.is_open()) return -1;

		for (int i = 1; i < inst->size(); ++i) {
			m_outf << inst->forms[i] << "\t" << inst->cpostags[i];
      assert(inst->marginal_scores.dim1() == inst->size() && inst->marginal_scores.dim2() == inst->size()
        && inst->marginal_scores.dim3() == labels.size());
      for (int j = 0; j < inst->predicted_heads.nrows(); j++)
      {
				m_outf << "\t" << inst->predicted_heads[j][i]-1 << "\t" << inst->predicted_deprels[j][i] << "\t" << inst->marginal_scores[i][inst->predicted_heads[j][i]][inst->predicted_deprels_int[j][i]] ;
      }

      double best_confidence = 0;
      for (int j = 0; j < inst->size(); j++)
      {
        for(int l = 0; l < labels.size(); l++)
        {
          if(inst->marginal_scores[i][j][l] > best_confidence)
          {
				    best_confidence = inst->marginal_scores[i][j][l];
          }
        }
      }
      double minum_confidence = thres * best_confidence;
      for (int j = 0; j < inst->size(); j++)
      {
        //if(j == i) continue;
        for(int l = 0; l < labels.size(); l++)
        {
          if(inst->marginal_scores[i][j][l] > minum_confidence)
          {
				    m_outf << "\t" << j-1 << "\t" << labels[l]  << "\t"  << inst->marginal_scores[i][j][l];
          }
        }
      }
			m_outf << endl;
		}
		m_outf << endl;

		return 0;
	}

}


