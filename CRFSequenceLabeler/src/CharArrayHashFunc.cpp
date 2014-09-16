/**
 * Implements the hash function for the string hash map
 * @file CharArrayHashFunc.cc
 * @author Mihai Surdeanu
 */

#include "CharArrayHashFunc.h"
#include <iostream>

size_t CharArrayHashFunc::operator()(const char * s) const {
	int hashTemp = 0;

	for (unsigned int i = 0; s[i] != 0; i++) {
		if (0 > hashTemp)
			hashTemp = (hashTemp << 1) + 1;
		else
			hashTemp = hashTemp << 1;
		hashTemp ^= s[i];
	}

	return (size_t(hashTemp));
}

