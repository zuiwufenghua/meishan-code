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
			_chart_cmp.resize(length, length);
			_chart_incmp.resize(length, length, _L);
		}

		void init_chart(const Instance *inst);

		void get_result(Instance *inst) {
			get_best_parse(inst);
		}

		void get_best_parse(Instance *inst) const;

		void get_best_parse_recursively(Instance *inst, const ChartItem * const item) const;

	protected:
		void dealloc() {
			assert(_chart_cmp.nrows() == _chart_incmp.dim1());
			assert(_chart_cmp.ncols() == _chart_incmp.dim2());
			dealloc_m2m2(_chart_cmp);
			dealloc_m3m2(_chart_incmp);
		}

	protected:
		NRMat< NRMat<const ChartItem *> > _chart_cmp;		// N * N; T1 * T2
		NRMat3d< NRMat<const ChartItem *> > _chart_incmp;	// N * N * L; T1 * T2
	};

} // namespace dparser

#endif


