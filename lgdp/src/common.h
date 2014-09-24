#ifndef _COMMON_H_
#define _COMMON_H_

#include <vector>
#include <string>
#include <map>
#include <iostream>
#include <ctime>
#include <list>
using namespace std;


#define FEAT_ID (unsigned long)
#define POSI	(unsigned int)

namespace dparser {

	extern const double EPS;
	extern const double ZERO;
	extern const double DOUBLE_NEGATIVE_INFINITY;
	extern const double DOUBLE_POSITIVE_INFINITY;

	extern const size_t RIGHT;
	extern const size_t LEFT;
	extern const size_t CMP;
	extern const size_t INCMP;
	extern const size_t SIB_SP;
	extern const size_t HEAD;
	extern const size_t CHILD;

	extern const string OOV_STR;

	extern const string NO_FORM;
	extern const string NO_LEMMA;
	extern const string NO_CPOSTAG;
	extern const string NO_POSTAG;
	extern const string NO_FEAT;
	extern const string FEAT_SEP;

	extern const string BET_NO_POS;
	extern const string BET_ONE_POS;

	extern const string ROOT_FORM;
	extern const string ROOT_LEMMA;
	extern const string ROOT_CPOSTAG;
	extern const string ROOT_POSTAG;
	extern const string ROOT_FEAT;
	extern const string ROOT_DEPREL;
	extern const int	ROOT_HEAD;

	// remove the blanks at the begin and end of string
	inline void clean_str(string &str) 
	{
		string blank = " \t\r\n";
		string::size_type pos1 = str.find_first_not_of(blank);
		string::size_type pos2 = str.find_last_not_of(blank);
		if (pos1 == string::npos) {
			str = "";
		} else {
			str = str.substr(pos1, pos2-pos1+1);
		}
	}

	inline void print_time() {
#ifdef SHOW_TIME
		time_t lt=time(NULL);
		string strTime = ctime(&lt);
		clean_str(strTime);
		std::cerr << "\t[" << strTime << "]" << endl;
#endif
	}

    inline double abs(const double val) { return (val > 0 ? val : -val); }

	void get_children( const vector<int> &heads, vector< list<int> > &children_l, vector< list<int> > &children_r );

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

		BinaryHeap() {} 

		BinaryHeap &resize(int new_size) {
			DEFAULT_CAPACITY = new_size;
			theArray.resize(DEFAULT_CAPACITY+1); 
			theArray[0] = ValueIndexPair(DOUBLE_POSITIVE_INFINITY,-1,-1);      
			currentSize = 0;
			return *this;
		}

		//ValueIndexPair getMax() {
		//	return theArray[1]; 
		//}


		void add(const ValueIndexPair &e) { 
			//if (currentSize == DEFAULT_CAPACITY) {	// reach the max size
			//
			//}
			// bubble up: 
			int where = currentSize + 1; // new last place 
			while ( e.compareTo(theArray[parent(where)]) > 0 ){ 
				theArray[where] = theArray[parent(where)]; 
				where = parent(where); 
			} 
			theArray[where] = e; currentSize++;
		}

		void removeMax(ValueIndexPair &max);

	private:
		int parent(int i) { return i / 2; } 
		int getLeftChild(int i) { return 2 * i; } 
		int getRightChild(int i) { return 2 * i + 1; } 

	private:
		int DEFAULT_CAPACITY; 
		int currentSize; 
		vector<ValueIndexPair> theArray;
	};

}

#endif



