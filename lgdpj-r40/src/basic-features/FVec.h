/* FVec : structure representing a sparse primal feature vector

Author : Terry Koo
maestro@csail.mit.edu */

#include "CppAssert.h"

#ifndef EGSTRA_FVEC_H
#define EGSTRA_FVEC_H


namespace egstra {
	class fvec {
	public:
		fvec() : idx(0), val(0), n(-1), offset(0) {}
		fvec(const fvec &rhs) : idx(rhs.idx), val(rhs.val), n(rhs.n), offset(rhs.offset) {}
		fvec & operator=(const fvec &rhs) {
			idx = rhs.idx;
			val = rhs.val;
			n = rhs.n;
			offset = rhs.offset;
			return *this;
		}

		void dealloc() {
			if (idx) {
				assert(n > 0);
				delete [] idx;
				idx = 0;
			}
			if (val) {
				assert(n > 0);
				delete [] val;
				val = 0;
			}
			n = -1;
		}

		~fvec() {} // do NOT delete *idx and *val! The user need to do it explicitly!

	public:
		const int* idx;    /* list of feature indices */
		const double* val; /* list of feature values.  if this is NULL,
						   then indicator features are assumed */
		int n;             /* number of features */
		int offset;        /* a base offset in the feature space.  each
						   feature index in idx[] is interpreted as
						   (idx[k] + offset). */
	};
}

#endif /* EGSTRA_FVEC_H */


