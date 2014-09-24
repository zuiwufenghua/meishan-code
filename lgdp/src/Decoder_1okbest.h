#ifndef _DPARSER_DECODER_1O_KBEST_
#define _DPARSER_DECODER_1O_KBEST_

#include "Decoder.h"
#include "NRMat.h"
using namespace nr;

#include "Parameters.h"
using namespace egstra;

namespace dparser {

	class Decoder_1okbest : public Decoder
  {
	public:
		Decoder_1okbest() : Decoder() {}
		~Decoder_1okbest() {
			dealloc();
		}

	protected: 
		void decode_projective(const Instance *inst);

		void reset_chart(const Instance *inst) {
			const int length = inst->size();
       //cerr << "\n\n +++++ reset_chart start: " << length;
			_chart_cmp.resize(length, length);
			_chart_incmp.resize(length, length, _L);
     
    
      for (int idx = 0; idx < length; idx++)
      {
        for(int idy = 0; idy < length; idy++)
        {
          _chart_cmp[idx][idy].resize(_K);
          for(int l = 0; l < _L; l++)
          {
            _chart_incmp[idx][idy][l].resize(_K);
          }
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
      get_kbest_parse(inst);
    }

    void get_result_confidence(Instance *inst)
    {
      cerr << "not implemented....";
    }

		void get_best_parse(Instance *inst) const;

    void get_kbest_parse(Instance *inst) const;

		void get_best_parse_recursively(Instance *inst, const ChartItem * const item) const;
    void get_best_parse_recursively(Instance *inst, const ChartItem * const item, int k) const;

    void chart_dump(int length) const
    {
		  for(int width = 1; width < length; width++) 
		  {
			  for(int s = 0; s+width < length; s++)
			  {
				  const int t = s + width;
          for(int k = 0; k <  _chart_cmp[s][t].elemsize(); k++)
          {
            cerr << "\n" << *(_chart_cmp[s][t][k]);
          }

          for(int k = 0; k <  _chart_cmp[t][s].elemsize(); k++)
          {
            cerr << "\n" << *(_chart_cmp[t][s][k]);
          }

          for(int l = 0; l < _L; l++)
          {
            for(int k = 0; k <  _chart_incmp[s][t][l].elemsize(); k++)
            {
              cerr << "\n" << *(_chart_incmp[s][t][l][k]);
            }

            for(int k = 0; k <  _chart_incmp[t][s][l].elemsize(); k++)
            {
              cerr << "\n" << *(_chart_incmp[t][s][l][k]);
            }
          }
        }
      }
    }

	protected:
		void dealloc() {
			assert(_chart_cmp.nrows() == _chart_incmp.dim1());
			assert(_chart_cmp.ncols() == _chart_incmp.dim2());
			dealloc_m2h(_chart_cmp);
			dealloc_m3h(_chart_incmp);
		}

	protected:
		NRMat< NRHeap<const ChartItem *, pChartItemCompare > > _chart_cmp;		// N * N; K
		NRMat3d< NRHeap<const ChartItem *, pChartItemCompare > > _chart_incmp;	// N * N * L; K
	};

} // namespace dparser

#endif


