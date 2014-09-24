/**
* Implements the feature dictionary
* @file FeatureDictionary.cc
* @author Mihai Surdeanu
*/

#include <iostream>
#include <iomanip>
#include <fstream>

#include <string.h>
#include <stdio.h>

#include "FeatureDictionary.h"
#include "CharUtils.h"
#include "Constants.h"
#include "CppAssert.h"
#include "GzFile.h"

using namespace std;

namespace egstra {

	int FeatureDictionary::getFeature(const std::string & name, 
		bool create)
	{
		IndexAndFrequency* iaf;

		// this feature already exists
		if((iaf = mMap.get(name.c_str())) != NULL) {
			// increment the feature frequency
			if(create) iaf->mFreq ++;
			return iaf->mIndex;
		}

		// the feature does not exist
		if(create == false) return -1;

		// must create a new feature
		// int index = mMap.size();
		++mMaxDim;
		int index = mMaxDim;
		mMap.set(name.c_str(), IndexAndFrequency(index));
		//cerr << "Created feature \"" + name + "\" with index: " << index << "\n";
		return index;
	}

	void FeatureDictionary::load(const std::string & fileName,
		const int cutoff) {
			// reset the previous content
			mMap.clear();

			FILE* const f = gzfile::gzopen(fileName.c_str(), "r");
			char* const buf = new char[16384];
			int idx = 0;
			int cnt = 0;
			int line = 0;

			cerr << "FeatureDictionary : loading from \"" << fileName
				<< "\" " << fixed << setprecision(1) << endl;
			cerr << "FeatureDictionary : " << flush;
			while(1) {
				const int nread = fscanf(f, "%16383s %d", buf, &cnt);
				if(nread <= 0 && feof(f)) { break; }
				else                      { assert(nread == 2); }

				if(cnt >= cutoff) {
					mMap.set(buf, IndexAndFrequency(idx, cnt));
					++idx;
				}
				++line;

				if((line & 0x1fffff) == 0) {
					cerr << "(" << (double)idx/1000000.0 << "m)" << flush;
				} else if((line & 0x3ffff) == 0) {
					cerr << "." << flush;
				}
			}
			delete [] buf;
			gzfile::gzclose(fileName.c_str(), f);
			cerr << " " << idx << " features";
			if(cutoff > 1) { cerr << " (" << line << " total)"; }
			cerr << endl;

			mMaxDim = idx - 1;
	}

	void FeatureDictionary::save(const std::string & fileName) /*const*/
	{
		FILE* const f = gzfile::gzopen(fileName.c_str(), "w");
		int count = 0;

		cerr << "FeatureDictionary : saving to \"" << fileName
			<< "\" " << fixed << setprecision(1) << endl;
		cerr << "FeatureDictionary : " << flush;
		for(StringMap<IndexAndFrequency>::const_iterator it = mMap.begin();
			it != mMap.end();
			++it) {
				const string& key = it->first;
				IndexAndFrequency iaf;
				mMap.get(key.c_str(), iaf);
				fprintf(f, "%s %d\n", key.c_str(), iaf.mFreq);
				count ++;

				if((count & 0x1fffff) == 0) {
					cerr << "(" << (double)count/1000000.0 << "m)" << flush;
				} else if((count & 0x3ffff) == 0) {
					cerr << "." << flush;
				}
		}

		gzfile::gzclose(fileName.c_str(), f);
		cerr << " " << count << " features" << endl;

		clear();
	}

	void FeatureDictionary::map_all(vector<int>& fidx,
		const list<string>& fstr,
		const bool create) {
			fidx.reserve(fidx.size() + fstr.size());
			list<string>::const_iterator it = fstr.begin();
			const list<string>::const_iterator it_end = fstr.end();
			for(; it != it_end; ++it) {
				const int idx = getFeature(*it, create);
				assert(idx >= -1);
				if(idx >= 0) { fidx.push_back(idx); }
			}
	}

	int FeatureDictionary::map_all(int* const fidx,
		const list<string>& fstr,
		const bool create) {
			list<string>::const_iterator it = fstr.begin();
			const list<string>::const_iterator it_end = fstr.end();
			int n = 0;
			for(; it != it_end; ++it) {
				const int idx = getFeature(*it, create);
				assert(idx >= -1);
				if(idx >= 0) { fidx[n] = idx; ++n; }
			}
			return n;
	}

	void FeatureDictionary::map_all( fvec * const fv, const int offset, const std::list<std::string>& fstr, const bool create )
	{
		assert(offset >= 0);
		assert(fv->idx == 0);

		vector<int> fidx;
		map_all(fidx, fstr, create);

		fv->idx = 0;
		fv->val = 0;
		fv->offset = offset;
		fv->n = fidx.size();

		if (!fidx.empty()) {
			int * const f = new int[fv->n];
			for (int i = 0; i < fv->n; ++i) f[i] = fidx[i];
			fv->idx = f;
		}
	}

	void FeatureDictionary::collect_keys( const char ** const keys, const int sz ) const
	{
		assert(sz == dimensionality());
		for( StringMap<IndexAndFrequency>::const_iterator it = mMap.begin(); it != mMap.end(); ++it) {
			const char *key = it->first;
			const int idx = it->second.mIndex;
			assert(idx >= 0 && idx < sz);
			assert(NULL == keys[idx]);
			keys[idx] = key;
		}
	}
}

