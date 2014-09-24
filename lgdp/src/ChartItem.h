#ifndef _DPARSER_CHART_ITEM_
#define _DPARSER_CHART_ITEM_

#pragma once

#include <stdlib.h>
#include "common.h"
#include "FVec.h"
#include <list>
using namespace std;
using namespace egstra;

namespace dparser {

	class ChartItem
	{
	public:
		const int _g, _s, _t;
		const int _comp;
		const int _label_s_t; // label type
		const ChartItem * const _left;
		const ChartItem * const _right;

		const double _prob;
		const list<const fvec *> _fvs;

	public:
		ChartItem(const int comp, const int g, const int s, const int t, 
			const double prob=0.0, const list<const fvec *> &fvs=list<const fvec *>(),
			const ChartItem * const left=0, const ChartItem * const right=0) :
		_comp(comp),
			_g(g), _s(s), _t(t), 
			_prob(prob), _fvs(fvs),
			_left(left), _right(right),
			_label_s_t(-1)
		{}


		ChartItem(const int comp, const int s, const int t, 
			const double prob=0.0, const list<const fvec *> &fvs=list<const fvec *>(),
			const ChartItem * const left=0, const ChartItem * const right=0,
			const int label_s_t=-1) :
		_comp(comp),
			_s(s), _t(t), 
			_prob(prob), _fvs(fvs),
			_left(left), _right(right),
			_label_s_t(label_s_t),
			_g(-1)
		{}

		ChartItem(const int g, const int s) : // for spans like C^g(s,s)
		_comp(CMP),
			_g(g), _s(s), _t(s),
			_prob(0.0), _fvs(),
			_left(0), _right(0),
			_label_s_t(-1)
		{}


		ChartItem(const int s) : // for spans like C(s,s)
		_comp(CMP),
			_s(s), _t(s),
			_prob(0.0), _fvs(),
			_left(0), _right(0),
			_label_s_t(-1),
			_g(-1)
		{}

		~ChartItem(void) {}

   bool operator < (const ChartItem&  other)
   {
     if(_prob < other._prob + EPS)
     {
       return true;
     }
     else
     {
       return false;
     }
   }

   friend ostream& operator << (ostream& os, const ChartItem& item)
   {
     os << "cmp:" << item._comp << ", g:" << item._g << ", s:" << item._s << ", t:" << item._t <<", label(st):" << item._label_s_t << ", score:" << item._prob;
     return os;
   }



	private:
		// forbid
		ChartItem(const ChartItem &rhs): 
		   _comp(-1),
			   _s(0), _t(0),
			   _prob(0.0), _fvs(list<const fvec *>()),
			   _left(0), _right(0),
			   _label_s_t(-1),
			   _g(-1)
		   {
			   cerr << "not allow ChartItem::ChartItem(const ChartItem &rhs)" << endl;
			   exit(-1);
		   }

		ChartItem &operator =(const ChartItem &rhs) {
			cerr << "not allow ChartItem::operator =(const ChartItem &rhs)" << endl;
			exit(-1);
			return *this;
		}

	};


  struct pChartItemCompare
  {
    int operator () (const ChartItem * x, const ChartItem * y) const {
      if(x->_prob < y->_prob - EPS)
      {
        return -1;
      }
      else if(x->_prob > y->_prob + EPS)
      {
        return 1;
      }
      else
      {
        return 0;
      }
    }

  };



}

#endif


