#ifndef _DPARSER_DECODER_1O_
#define _DPARSER_DECODER_1O_

#include "Decoder.h"
#include "NRMat.h"
using namespace nr;

#include "Parameters.h"
using namespace egstra;

namespace dparser {

	class Decoder_1o : public Decoder
  {
	public:
		Decoder_1o() : Decoder() {}
		~Decoder_1o() {
			dealloc();
		}

	protected: 
		void decode_projective(const Instance *inst);

		void reset_chart(const Instance *inst) {
			const int length = inst->size();
       //cerr << "\n\n +++++ reset_chart start: " << length;
			_chart_cmp.resize(length, length);
			_chart_incmp.resize(length, length);
     
    
      for (int idx = 0; idx < length; idx++)
      {
        for(int idy = 0; idy < length; idy++)
        {
          _chart_cmp[idx][idy] = NULL;
           _chart_incmp[idx][idy] = NULL;
        }
      }
      //cerr << "\n\n +++++ reset_chart end: " << length;
    
		}

		void init_chart(const Instance *inst);

		void get_result(Instance *inst) {
			get_best_parse(inst);
		}

    void get_result_kbest(Instance *inst)
    {
      cerr << "not implemented....";
    }

    void get_result_confidence(Instance *inst)
    {
      const int length = inst->size();
      Marginal marg(length, _L);
      inside(inst, marg);
      outside(inst, marg);
      inst->marginal_scores.resize(length, length, _L);
      inst->marginal_scores = 0;
      //cerr << "\n" << marg;
      for(int i = 1; i < length; i++)
      {
        for(int j = 0; j < length; j++)
        {
          if(j == i) continue;
          for(int l = 0; l < _L; l++)
          {
            inst->marginal_scores[i][j][l] = exp(marg.log_marginal(j, i, l));
            //cerr << "\nmarginal_scores[" << i << "][" << j << "][" << l << "]=" << inst->marginal_scores[i][j][l];
          }
        }
      }
      for (int k = 0; k < inst->predicted_probs.size(); k++)
      {
        inst->predicted_probs[k] = inst->predicted_probs[k] - marg.log_Z();
      }
    }

    void outside(Instance *inst, Marginal &marg);

    void inside(Instance *inst, Marginal &marg);

		void get_best_parse(Instance *inst) const;

		void get_best_parse_recursively(Instance *inst, const ChartItem * const item) const;

    void chart_dump(int length) const
    {
		  for(int width = 1; width < length; width++) 
		  {
			  for(int s = 0; s+width < length; s++)
			  {
				  const int t = s + width;
          cerr << "\n" << *(_chart_cmp[s][t]);
          cerr << "\n" << *(_chart_cmp[t][s]);
          cerr << "\n" << *(_chart_incmp[s][t]);
          cerr << "\n" << *(_chart_incmp[t][s]);
        }
      }
    }

	protected:
		void dealloc() {
			assert(_chart_cmp.nrows() == _chart_incmp.nrows());
			assert(_chart_cmp.ncols() == _chart_incmp.ncols());
			dealloc_m2(_chart_cmp);
			dealloc_m2(_chart_incmp);
		}

	protected:
		NRMat< const ChartItem * > _chart_cmp;		// N * N; 
		NRMat< const ChartItem * > _chart_incmp;	// N * N ; 
	};

} // namespace dparser

#endif


