#ifndef _COMMON_H_
#define _COMMON_H_

#include "FeatureVec.h"
#include "MultiArray.h"

extern const double EPS;
extern const double ZERO;
extern const double DOUBLE_NEGATIVE_INFINITY;
extern const double DOUBLE_POSITIVE_INFINITY;
//extern static const int MAX_THREAD_NUM;

struct SimpRuleUnit
{
	int position;
	int type; //0:word; 1:cpos; 2:lemma; or will be more
	bool arcdep; // true, position means the count by arc relation; false, by general sequence
	string value;
  bool bUnitT;
};

class FVS_PROBS {
public:

	void allocMultiArr( int length, int typesize);

	MultiArray<FeatureVec> fvs;
	MultiArray<double> probs;
	MultiArray<FeatureVec> fvs_trips;
	MultiArray<double> probs_trips;
	MultiArray<FeatureVec> nt_fvs;
	MultiArray<double> nt_probs;
};



class ValueIndexPair {
public:
	double val;
	int i1, i2;
public:
	ValueIndexPair(double _val=0, int _i1=0, int _i2=0) : val(_val), i1(_i1), i2(_i2) {}

	int compareTo(const ValueIndexPair &other) const {
		if(val < other.val - EPS)
			return -1;
		if(val > other.val + EPS)
			return 1;
		return 0;
	}

	ValueIndexPair &operator=(const ValueIndexPair &other) {
		val = other.val; i1 = other.i1; i2 = other.i2; return *this;
	}
};

// Max Heap
// We know that never more than K elements on Heap
class BinaryHeap { 
private:
	int DEFAULT_CAPACITY; 
	int currentSize; 
	vector<ValueIndexPair> theArray;
public:
	bool empty() {
		return currentSize == 0;
	}
	BinaryHeap(int def_cap) {
		DEFAULT_CAPACITY = def_cap;
		theArray.resize(DEFAULT_CAPACITY+1); 
		// theArray[0] serves as dummy parent for root (who is at 1) 
		// "largest" is guaranteed to be larger than all keys in heap
		theArray[0] = ValueIndexPair(DOUBLE_POSITIVE_INFINITY,-1,-1);      
		currentSize = 0; 
	} 

	ValueIndexPair getMax() {
		return theArray[1]; 
	}

	int parent(int i) { return i / 2; } 
	int getLeftChild(int i) { return 2 * i; } 
	int getRightChild(int i) { return 2 * i + 1; } 

	void add(const ValueIndexPair &e) { 
		// bubble up: 
		int where = currentSize + 1; // new last place 
		while ( e.compareTo(theArray[parent(where)]) > 0 ){ 
			theArray[where] = theArray[parent(where)]; 
			where = parent(where); 
		} 
		theArray[where] = e; currentSize++;
	}

	void removeMax(ValueIndexPair &max);
};
#endif

