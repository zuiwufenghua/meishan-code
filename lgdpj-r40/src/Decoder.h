#ifndef _DECODER_
#define _DECODER_
#pragma once

#include "ChartItem.h"
#include "Instance.h"
#include "common.h"
#include "Options.h"
using namespace egstra;

namespace dparser {

	class Decoder
	{
	public:
		Decoder() {}

		virtual ~Decoder(void) {}

		void decodeProjectiveInterface( Instance *inst ) {
			reset_chart(inst);
			init_chart(inst);
			decode_projective(inst);
			get_result(inst);
			dealloc();
		}

		bool add_item(const ChartItem * &add_place, const ChartItem * const new_item) {
			if (add_place == NULL) {
				add_place = new_item;
				return true;
			}
			if (add_place->_prob < new_item->_prob - EPS) { // absolutely less than the new item
				delete add_place;
				add_place = new_item;
				return true;
			} else {
				delete new_item;
				return false;
			}
		}

		void process_options() {
			int tmp;

			_use_filtered_heads = false;
			if(options::get("use-filtered-heads", tmp)) {
				_use_filtered_heads = (1 == tmp);
			}

			_labeled = false;
			if(options::get("labeled", tmp)) {
				_labeled = (1 == tmp);
			}

			_use_filtered_labels = false;
			if (_labeled && _use_filtered_heads) {
				if(options::get("use-filtered-labels", tmp)) {
					_use_filtered_labels = (1 == tmp);
				}
			}
			
			_L = 1;
			if (_labeled) {
				assert(options::get("L", tmp));
				_L = tmp;
			}

			_T = 1;
			assert(options::get("T", tmp));
			_T = tmp;

			_use_unlabeled_syn_features = false;
			if (_labeled) {
				if(options::get("use-unlabeled-syn-features", tmp)) {
					_use_unlabeled_syn_features = tmp;
				}
			} else {
				_use_unlabeled_syn_features = true;
			}

			_use_last_sibling_features = false;
			_use_no_grand_features = false;
			if(options::get("use-last-sibling-features", tmp)) {
				_use_last_sibling_features = tmp;
			}
			if(options::get("use-no-grand-features", tmp)) {
				_use_no_grand_features = tmp;
			}
		}

	protected:
		bool _labeled;
		bool _use_filtered_heads;
		bool _use_filtered_labels;

		bool _use_unlabeled_syn_features;

		bool _use_last_sibling_features;
		bool _use_no_grand_features;

		int _T;
		int _L;

	protected:
		virtual void reset_chart(const Instance *inst) = 0;
		virtual void init_chart(const Instance *inst) = 0;
		virtual void decode_projective(const Instance *inst) = 0;
		virtual void get_result(Instance *inst)= 0;
		virtual	void dealloc() = 0;

		static void dealloc_m3m3(NRMat3d< NRMat3d<const ChartItem *> > &_chart);
		static void dealloc_m3m2(NRMat3d< NRMat<const ChartItem *> > &_chart);
		static void dealloc_m2m2(NRMat< NRMat<const ChartItem *> > &_chart);
		static void dealloc_m2m3(NRMat< NRMat3d<const ChartItem *> > &_chart);
	};

	inline void Decoder::dealloc_m3m3(NRMat3d< NRMat3d<const ChartItem *> > &_chart) {
		NRMat3d<const ChartItem *> *m3 = _chart.c_buf();
		for (int i = 0; i < _chart.total_size(); ++i, ++m3) {
			const ChartItem * * pitem = m3->c_buf();
			for (int j = 0; j < m3->total_size(); ++j, ++pitem)
				if (*pitem) delete *pitem;
		}
		_chart.dealloc();
	}

	inline void Decoder::dealloc_m3m2(NRMat3d< NRMat<const ChartItem *> > &_chart) {
		NRMat<const ChartItem *> *m2 = _chart.c_buf();
		for (int i = 0; i < _chart.total_size(); ++i, ++m2) {
			const ChartItem * * pitem = m2->c_buf();
			for (int j = 0; j < m2->total_size(); ++j, ++pitem)
				if (*pitem) delete *pitem;
		}
		_chart.dealloc();
	}

	inline void Decoder::dealloc_m2m3(NRMat< NRMat3d<const ChartItem *> > &_chart) {
		NRMat3d<const ChartItem *> *m3 = _chart.c_buf();
		for (int i = 0; i < _chart.total_size(); ++i, ++m3) {
			const ChartItem * * pitem = m3->c_buf();
			for (int j = 0; j < m3->total_size(); ++j, ++pitem)
				if (*pitem) delete *pitem;
		}
		_chart.dealloc();
	}

	inline void Decoder::dealloc_m2m2(NRMat< NRMat<const ChartItem *> > &_chart) {
		//for (int i = 0; i < _chart.nrows(); i++) {
		//	for (int j = 0; j < _chart.ncols(); j++) {
		//		for (int pi = 0; pi < _chart[i][j].nrows(); pi++)
		//			for (int pj = 0; pj < _chart[i][j].ncols(); pj++) 
		//				if (_chart[i][j][pi][pj]) 
		//					delete _chart[i][j][pi][pj];
		//	}
		//}
		//_chart.dealloc();

		NRMat<const ChartItem *> *m2 = _chart.c_buf();
		for (int i = 0; i < _chart.total_size(); ++i, ++m2) {
			const ChartItem * * pitem = m2->c_buf();
			for (int j = 0; j < m2->total_size(); ++j, ++pitem)
				if (*pitem) delete *pitem;
		}
		_chart.dealloc();
	}


} // namespace dparser

#endif

