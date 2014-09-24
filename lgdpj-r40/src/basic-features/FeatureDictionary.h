/**
* Implements the feature dictionary
* @file FeatureDictionary.h
* @author Mihai Surdeanu
*/

#ifndef EGSTRA_FEATURE_DICTIONARY_H
#define EGSTRA_FEATURE_DICTIONARY_H

#include <string>
#include "FVec.h"
#include "StringMap.h"

namespace egstra {

	class IndexAndFrequency {
	public:
		/** The index of a given feature */
		int mIndex;

		/** The frequency (counts in training) of a given feature */
		int mFreq;

		IndexAndFrequency() {
			mIndex = -1;
			mFreq = 0;
		}

		IndexAndFrequency(int i) {
			mIndex = i;
			mFreq = 1;
		}

		IndexAndFrequency(int i, int f) {
			mIndex = i;
			mFreq = f;
		}
	};

	class FeatureDictionary {
	private:
		/** The actual hash map from feature strings to indices */
		StringMap<IndexAndFrequency> mMap;

		/** The maximum dimension in the mapped space */
		int mMaxDim;

	public:
		FeatureDictionary()
			: mMaxDim(-1)
		{}

		void clear() {
			mMap.clear();
			mMaxDim = -1;
		}

		/**
		* Fetches the index of the feature with the given name.
		* If create is true:
		*   a new feature is created if it does not exist, and
		*   the frequency count is incremented for every call
		* @return The index of this feature in the dictionary, 
		*         or -1 if it does not exist
		*/
		int getFeature(const std::string & name, bool create);

		/** Loads a feature dictionary from a file, using the given count
		cutoff */
		void load(const std::string & fileName, const int cutoff = 0);

		/** Saves a feature dictionary in a file */
		void save(const std::string & fileName) /*const*/; 

		int size() const { return mMap.size(); } 

		int dimensionality() const { return mMaxDim+1; }

		void map_all(fvec * const fv,
			const int offset, 
			const std::list<std::string>& fstr,
			const bool create);

		void map_all(std::vector<int>& fidx,
			const std::list<std::string>& fstr,
			const bool create);

		int map_all(int* const fidx,
			const std::list<std::string>& fstr,
			const bool create);

		void collect_keys(const char ** const keys, const int sz) const;
	};

}

#endif /* EGSTRA_FEATURE_DICTIONARY_H */

