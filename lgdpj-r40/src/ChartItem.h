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
		const int _g_pos_i, _s_pos_i, _t_pos_i;
		const int _label_s_t; // label type
		const ChartItem * const _left;
		const ChartItem * const _right;

		const double _prob;
		const list<const fvec *> _fvs;

	public:
		ChartItem(const int comp, const int g, const int s, const int t, const int g_pos_i, const int s_pos_i, const int t_pos_i,
			const double prob=0.0, const list<const fvec *> &fvs=list<const fvec *>(),
			const ChartItem * const left=0, const ChartItem * const right=0) :
		_comp(comp),
			_g(g), _s(s), _t(t), _g_pos_i(g_pos_i), _s_pos_i(s_pos_i), _t_pos_i(t_pos_i),
			_prob(prob), _fvs(fvs),
			_left(left), _right(right),
			_label_s_t(-1)
		{}


		ChartItem(const int comp, const int s, const int t, const int s_pos_i, const int t_pos_i,
			const double prob=0.0, const list<const fvec *> &fvs=list<const fvec *>(),
			const ChartItem * const left=0, const ChartItem * const right=0,
			const int label_s_t=-1) :
		_comp(comp),
			_s(s), _t(t), _s_pos_i(s_pos_i), _t_pos_i(t_pos_i),
			_prob(prob), _fvs(fvs),
			_left(left), _right(right),
			_label_s_t(label_s_t),
			_g(-1), _g_pos_i(-1)
		{}

		ChartItem(const int g, const int s, const int g_pos_i, const int s_pos_i) : // for spans like C^g(s,s)
		_comp(CMP),
			_g(g), _s(s), _t(s), _g_pos_i(g_pos_i), _s_pos_i(s_pos_i), _t_pos_i(s_pos_i),
			_prob(0.0), _fvs(),
			_left(0), _right(0),
			_label_s_t(-1)
		{}


		ChartItem(const int s, const int s_pos_i) : // for spans like C(s,s)
		_comp(CMP),
			_s(s), _t(s), _s_pos_i(s_pos_i), _t_pos_i(s_pos_i),
			_prob(0.0), _fvs(),
			_left(0), _right(0),
			_label_s_t(-1),
			_g(-1), _g_pos_i(-1)
		{}

		~ChartItem(void) {}

	private:
		// forbid
		ChartItem(const ChartItem &rhs): 
		   _comp(-1),
			   _s(0), _t(0), _s_pos_i(0), _t_pos_i(0),
			   _prob(0.0), _fvs(list<const fvec *>()),
			   _left(0), _right(0),
			   _label_s_t(-1),
			   _g(-1), _g_pos_i(-1)

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


}

#endif


