#include "common.h"
#include <sstream>
using std::ostringstream;

namespace dparser {

	const double EPS = 1e-10;
	const double ZERO = 1e-20;
	const double DOUBLE_NEGATIVE_INFINITY = - 10e20;
	const double DOUBLE_POSITIVE_INFINITY = 10e20;

	const size_t RIGHT = 0;
	const size_t LEFT = 1;
	const size_t CMP = 0;
	const size_t INCMP = 1;
	const size_t SIB_SP = 2;
	const size_t HEAD = 0;
	const size_t CHILD = 1;

	const string OOV_STR = "-OOV-";

	const string NO_FORM = "##";
	const string NO_LEMMA = "##";
	const string NO_CPOSTAG = "##";
	const string NO_POSTAG = "##";
	const string NO_FEAT = "##";
	const string FEAT_SEP = "-";

	const string BET_NO_POS = "#0#";
	const string BET_ONE_POS = "#1";

	const string ROOT_FORM = "RT#";
	const string ROOT_LEMMA = "RT#";
	const string ROOT_CPOSTAG = "RT#";
	const string ROOT_POSTAG = "RT#";
	const string ROOT_FEAT = "RT#";
	const string ROOT_DEPREL = "RT_TP";
	const int ROOT_HEAD = -1;

	// both left/right children (rank from inside to outside)
	void get_children( const vector<int> &heads, vector< list<int> > &children_l, vector< list<int> > &children_r )
	{
		const int length = heads.size();

		children_l.clear();
		children_l.resize(length);
		children_r.clear();
		children_r.resize(length);

		for (int i = 1; i < length; ++i) {
			if (heads[i] < 0 || heads[i] >= heads.size()) {
				ostringstream out;
				out << "heads[i] range err: " << heads[i];
				throw out.str();
			}
			if (i < heads[i]) {
				children_l[ heads[i] ].push_front( i );
			} else {
				children_r[ heads[i] ].push_back( i );
			}
		}
	}
	void BinaryHeap::removeMax(ValueIndexPair &max)
	{
		if (empty()) {
			max.val = DOUBLE_NEGATIVE_INFINITY;
			return;
		}
		max = theArray[1];
		theArray[1] = theArray[currentSize];
		currentSize--;
		bool switched = true;
		// bubble down
		int parent = 1;
		while(switched && parent < currentSize) 
		{
			switched = false;
			int leftChild = getLeftChild(parent);
			int rightChild = getRightChild(parent);

			if(leftChild <= currentSize) 
			{
				// if there is a right child, see if we should bubble down there
				int largerChild = leftChild;
				if ((rightChild <= currentSize) && 
					(theArray[rightChild].compareTo(theArray[leftChild])) > 0) {
						largerChild = rightChild; 
				}
				if (theArray[largerChild].compareTo(theArray[parent]) > 0) {      
					ValueIndexPair temp = theArray[largerChild];
					theArray[largerChild] = theArray[parent];
					theArray[parent] = temp;
					parent = largerChild;
					switched = true;
				}
			}
		}
	}
}



