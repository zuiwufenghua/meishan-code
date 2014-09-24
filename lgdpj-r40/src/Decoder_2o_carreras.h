#ifndef _DPARSER_DECODER_2O_CARRERAS_H_
#define _DPARSER_DECODER_2O_CARRERAS_H_

#include "Decoder.h"
#include "NRMat.h"

using namespace nr;

#include "Parameters.h"
using namespace egstra;

namespace dparser {

	class Decoder_2o_carreras : public Decoder
	{
	public:
		Decoder_2o_carreras() : Decoder() {}
		~Decoder_2o_carreras() {
			dealloc();
		}

	protected: 
		void decode_projective(const Instance *inst);

		void reset_chart(const Instance *inst) {
			const int length = inst->size();
			_chart_cmp.resize(length, length, length);
			_chart_incmp.resize(length, length);
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
			dealloc_m2m3(_chart_incmp);
		}

	protected:
		NRMat3d< NRMat3d<const ChartItem *> > _chart_cmp;		// N * N * N; Th * Tm * Te
		NRMat< NRMat3d<const ChartItem *> > _chart_incmp;	// N * N; L * Ts * Tt
	};

} // namespace dparser

#endif


