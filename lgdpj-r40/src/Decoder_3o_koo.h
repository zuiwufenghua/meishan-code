#ifndef _DPARSER_DECODER_3O_KOO_H_
#define _DPARSER_DECODER_3O_KOO_H_

#include "Decoder.h"
#include "NRMat.h"

using namespace nr;

#include "Parameters.h"
using namespace egstra;

namespace dparser {

	class Decoder_3o_koo : public Decoder
	{
	public:
		Decoder_3o_koo() : Decoder() {}
		~Decoder_3o_koo() {
			dealloc();
		}

	protected: 
		void decode_projective(const Instance *inst);

		void reset_chart(const Instance *inst) {
			const int length = inst->size();
			_chart_cmp.resize(length+1, length, length);
			_chart_incmp.resize(length+1, length, length);
			_chart_sibling.resize(length, length, length);
		}

		void init_chart(const Instance *inst);

		void get_result(Instance *inst) {
			get_best_parse(inst);
		}

		void get_best_parse(Instance *inst) const;

		void get_best_parse_recursively(Instance *inst, const ChartItem * const item) const;

	protected:
		void dealloc() {
			dealloc_m3m3(_chart_cmp);
			dealloc_m3m3(_chart_incmp);
			dealloc_m3m3(_chart_sibling);
		}

	protected:
		NRMat3d< NRMat3d<const ChartItem *> > _chart_cmp;		// N+1 * N * N; Tg * Th * Tm
		NRMat3d< NRMat3d<const ChartItem *> > _chart_incmp;	// N+1 * N * N; Tg * Th * Te
		NRMat3d< NRMat3d<const ChartItem *> > _chart_sibling;	// N * N * N; Tg * Th * Ts
	};

} // namespace dparser

#endif


