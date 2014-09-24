#ifndef _DECODER_
#define _DECODER_
#pragma once

#include "ChartItem.h"
#include "Instance.h"
#include "common.h"
#include "Options.h"
using namespace egstra;

namespace dparser {

	class Marginal {
	private:
		NRMat<double> _inside_cmp;
		NRMat3d<double> _inside_ucmp;
		NRMat<double> _outside_cmp;
		NRMat<double> _outside_ucmp;
    int _N;
    int _L;

	public:
    Marginal(int N, int L):_N(N),_L(L)
    {
      _inside_cmp.resize(N, N);
      _inside_ucmp.resize(N, N, L);
      _outside_cmp.resize(N,N);
      _outside_ucmp.resize(N,N);
      _inside_cmp = DOUBLE_NEGATIVE_INFINITY;
      _inside_ucmp = DOUBLE_NEGATIVE_INFINITY;
      _outside_cmp = DOUBLE_NEGATIVE_INFINITY;
      _outside_ucmp = DOUBLE_NEGATIVE_INFINITY;
    }

		~Marginal() {
      _inside_cmp.dealloc();
      _inside_ucmp.dealloc();
      _outside_cmp.dealloc();
      _outside_ucmp.dealloc();
    }


		static double log_add(double a, double b)
		{
			if (a>b)
				return a+log(1+exp(b-a));
			else
				return b+log(1+exp(a-b));
		}


		void reset_all(int N, int L) {
			_N = N;
      _L = L;
      _inside_cmp.resize(N, N);
      _inside_ucmp.resize(N, N, L);
      _outside_cmp.resize(N,N);
      _outside_ucmp.resize(N,N);
      _inside_cmp = DOUBLE_NEGATIVE_INFINITY;
      _inside_ucmp = DOUBLE_NEGATIVE_INFINITY;
      _outside_cmp = DOUBLE_NEGATIVE_INFINITY;
      _outside_ucmp = DOUBLE_NEGATIVE_INFINITY;
		}

		void inside_C_add(int h, int m, double w) { 
			_inside_cmp[h][m] = log_add(_inside_cmp[h][m], w); 
		}

		void outside_C_add(int h, int m, double w) { 
			_outside_cmp[h][m] = log_add(_outside_cmp[h][m], w); 
		}

		void inside_U_add(int h, int m, int l, double w) { 
			_inside_ucmp[h][m][l] = log_add(_inside_ucmp[h][m][l], w); 
		}
		void outside_U_add(int h, int m, double w) { 
			_outside_ucmp[h][m] = log_add(_outside_ucmp[h][m], w); 
		}

		void inside_C_set(int h, int m, double w) { 
			_inside_cmp[h][m] = w; 
		}
		void outside_C_set(int h, int m, double w) { 
			_outside_cmp[h][m] = w; 
		}

		void inside_U_set(int h, int m, int l, double w) { 
			_inside_ucmp[h][m][l] = w; 
		}
		void outside_U_set(int h, int m, double w) { 
			_outside_ucmp[h][m] = w; 
		}

		double inside_C(int h, int m) const { 
			const double tmp = _inside_cmp[h][m];
			if (tmp <= DOUBLE_NEGATIVE_INFINITY + EPS) {
				cerr << "DOUBLE_NEGATIVE_INFINITY is returned!" << endl;
				exit(-1);
			}
			return tmp; 
		}
		double outside_C(int h, int m) const { 
			const double tmp = _outside_cmp[h][m];
			if (tmp <= DOUBLE_NEGATIVE_INFINITY + EPS) {
				cerr << "DOUBLE_NEGATIVE_INFINITY is returned!" << endl;
				exit(-1);
			}
			return tmp; 
		}
		double inside_U(int h, int m, int l) const { 
			const double tmp = _inside_ucmp[h][m][l];
			if (tmp <= DOUBLE_NEGATIVE_INFINITY + EPS) {
				cerr << "DOUBLE_NEGATIVE_INFINITY is returned!" << endl;
				exit(-1);
			}
			return tmp; 
		}
		double outside_U(int h, int m) const { 
			const double tmp = _outside_ucmp[h][m];
			if (tmp <= DOUBLE_NEGATIVE_INFINITY + EPS) {
				cerr << "DOUBLE_NEGATIVE_INFINITY is returned!" << endl;
				exit(-1);
			}
			return tmp; 
		}

		double log_Z() const { 
			return _inside_cmp[0][_N-1];
		}

		double log_marginal(int h, int m, int l) const {
      return inside_U(h,m, l) + outside_U(h,m) - log_Z();
		}

   friend ostream& operator << (ostream& os, const Marginal& item)
   {
     //os << "cmp:" << item._comp << ", g:" << item._g << ", s:" << item._s << ", t:" << item._t <<", label(st):" << item._label_s_t << ", score:" << item._prob;
      for(int i = 0; i < item._N; i++)
      {
        for(int j = 1; j < item._N; j++)
        {
          if(j == i) continue;
          if(i != 0 || j == item._N -1)os << "inside_cmp[" << i << "][" << j << "]=" << item._inside_cmp[i][j] << "\t";
          if(i != 0 || j == item._N -1)os << "outside_cmp[" << i << "][" << j << "]=" << item._outside_cmp[i][j] << "\t";
          if(i != 0)os << "outside_ucmp[" << i << "][" << j << "]=" << item._outside_ucmp[i][j] << "\n";
          if(i == 0)continue;
          for(int l = 0; l < item._L -1; l++)
          {
            os << "inside_ucmp[" << i << "][" << j << "][" << l << "]=" << item._inside_ucmp[i][j][l] << "\t";
          }
          os << "inside_ucmp[" << i << "][" << j << "][" << item._L-1 << "]=" << item._inside_ucmp[i][j][item._L-1] << "\n";
        }
        os << "\n";
      }
     return os;
   }
	};


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


 
    void decodeProjectiveInterfaceKbest( Instance *inst, int K) {
      _K = K;
			reset_chart(inst);
			init_chart(inst);
			decode_projective(inst);
			get_result_kbest(inst);
			dealloc();
    }

    void decodeProjectiveInterfaceConfidence( Instance *inst) {
      _K = 1;
			reset_chart(inst);
			init_chart(inst);
			decode_projective(inst);
      get_result(inst);
			get_result_confidence(inst);
			dealloc();
    }


    void set_labelnum(int L)
    {
      _L = L;
    }

    void set_kbestnum(int K)
    {
      _K = K;
    }


	protected:


    // default it as true, h->m, if m -s a leaf node, then regard a fake h->m->m node.
		//bool _use_no_grand_features;

		int _L;
    int _K;

	protected:
		virtual void reset_chart(const Instance *inst) = 0;
		virtual void init_chart(const Instance *inst) = 0;
		virtual void decode_projective(const Instance *inst) = 0;
		virtual void get_result(Instance *inst)= 0;
    virtual void get_result_kbest(Instance *inst)= 0;
    virtual void get_result_confidence(Instance *inst)= 0;
		virtual	void dealloc() = 0;

		static void dealloc_m3m3(NRMat3d< NRMat3d<const ChartItem *> > &_chart);
		static void dealloc_m3m2(NRMat3d< NRMat<const ChartItem *> > &_chart);
		static void dealloc_m2m2(NRMat< NRMat<const ChartItem *> > &_chart);
		static void dealloc_m2m3(NRMat< NRMat3d<const ChartItem *> > &_chart);
    static void dealloc_m3(NRMat3d< const ChartItem * > &_chart);
    static void dealloc_m2(NRMat< const ChartItem * > &_chart);

    static void dealloc_m3h(NRMat3d< NRHeap<const ChartItem *, pChartItemCompare > > &_chart);
		static void dealloc_m2h(NRMat< NRHeap<const ChartItem *, pChartItemCompare > > &_chart);
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

  
	inline void Decoder::dealloc_m3(NRMat3d< const ChartItem * > &_chart) {
    const ChartItem * * pitem= _chart.c_buf();
		for (int j = 0; j < _chart.total_size(); ++j, ++pitem) {
			if (*pitem) delete *pitem;
		}
		_chart.dealloc();
	}

	inline void Decoder::dealloc_m2(NRMat< const ChartItem * > &_chart) {

    const ChartItem * * pitem= _chart.c_buf();
		for (int j = 0; j < _chart.total_size(); ++j, ++pitem) {
			if (*pitem) delete *pitem;
		}
		_chart.dealloc();
	}


	inline void Decoder::dealloc_m3h(NRMat3d< NRHeap<const ChartItem *, pChartItemCompare > > &_chart) {
		NRHeap<const ChartItem *, pChartItemCompare > *h = _chart.c_buf();
		for (int i = 0; i < _chart.total_size(); ++i, ++h) {
			const ChartItem * * pitem = h->c_buf();
			for (int j = 0; j < h->elemsize(); ++j, ++pitem)
				if (*pitem) delete *pitem;
		}
		_chart.dealloc();
	}

  inline void Decoder::dealloc_m2h(NRMat< NRHeap<const ChartItem *, pChartItemCompare > > &_chart) {
		NRHeap<const ChartItem *, pChartItemCompare > *h = _chart.c_buf();
		for (int i = 0; i < _chart.total_size(); ++i, ++h) {
			const ChartItem * * pitem = h->c_buf();
			for (int j = 0; j < h->elemsize(); ++j, ++pitem)
				if (*pitem) delete *pitem;
		}
		_chart.dealloc();
	}


  

} // namespace dparser

#endif

