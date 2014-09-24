#include "common.h"

const double DOUBLE_POSITIVE_INFINITY = 10e20;
//const int MAX_THREAD_NUM = 256;
const double EPS = 1e-10;
const double ZERO = 1e-20;
const double DOUBLE_NEGATIVE_INFINITY = - 10e20;

void FVS_PROBS::allocMultiArr(int length, int typesize )
{
	vector<unsigned int> fvs_dim;
	fvs.setDemisionVal(fvs_dim, length, length, 2);
	probs.resize(fvs_dim, 0.0);
	if (0 > fvs.resize(fvs_dim, FeatureVec())) return;
	//if (options.m_isLabeled) 
	{
		vector<unsigned int> nt_dim(4);
		nt_fvs.setDemisionVal(nt_dim, length, typesize, 2, 2);
		nt_fvs.resize(nt_dim, FeatureVec());
		nt_probs.resize(nt_dim, 0.0);
	}

	//if (options.m_isUseSib) 
	{
		fvs_trips.setDemisionVal(fvs_dim, length, length, length);
		fvs_trips.resize(fvs_dim, FeatureVec());
		probs_trips.resize(fvs_dim, 0.0);
	}
}

void BinaryHeap::removeMax(ValueIndexPair &max)
{
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
