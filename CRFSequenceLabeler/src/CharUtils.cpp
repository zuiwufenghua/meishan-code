/**
 * Contains some string manipulation functions
 * @file CharUtils.cc
 * @author Mihai Surdeanu
 */

#include <list>
#include <stdlib.h>
#include <ctype.h>
#include <iostream>
#include <fstream>
#include <string>

#include "CharUtils.h"
#include "CppAssert.h"

using namespace std;

bool startsWith(const std::string & big, const std::string & small) {
	if (small.size() > big.size())
		return false;

	for (size_t i = 0; i < small.size(); i++) {
		if (small[i] != big[i]) {
			return false;
		}
	}

	return true;
}

bool endsWith(const std::string & big, const std::string & small) {
	size_t bigSize = big.size();
	size_t smallSize = small.size();

	if (smallSize > bigSize)
		return false;

	for (size_t i = 0; i < smallSize; i++) {
		if (small[i] != big[bigSize - smallSize + i]) {
			return false;
		}
	}

	return true;
}

int toInteger(const std::string & s) {
	return strtol(s.c_str(), NULL, 10);
}

void toString(const int i, std::string& s) {
	std::ostringstream os;
	os << i;
	s = os.str();
}

void split_bystr(const std::string &str, std::vector<std::string> &vec,
		const std::string &sep) {
	vec.clear();
	std::string::size_type pos1 = 0, pos2 = 0;
	std::string word;
	while ((pos2 = str.find(sep, pos1)) != string::npos) {
		word = str.substr(pos1, pos2 - pos1);
		pos1 = pos2 + sep.size();
		if (!word.empty())
			vec.push_back(word);
	}
	word = str.substr(pos1);
	if (!word.empty())
		vec.push_back(word);
}

void simpleTokenize(const std::string & input,
		std::vector<std::string> & output, const std::string & separators) {
	output.clear();
	for (int start = input.find_first_not_of(separators);
			start < (int) input.size() && start >= 0;
			start = input.find_first_not_of(separators, start)) {
		int end = input.find_first_of(separators, start);
		if (end < 0)
			end = input.size();
		output.push_back(input.substr(start, end - start));
		start = end;
	}
}

std::string toUpper(const std::string & s) {
	std::string o = s;

	for (int i = 0; i < (int) o.size(); i++) {
		o[i] = toupper(o[i]);
	}

	return o;
}

std::string toLower(const std::string & s) {
	std::string o = s;

	for (int i = 0; i < (int) o.size(); i++) {
		o[i] = tolower(o[i]);
	}

	return o;
}

std::string stripString(const std::string & s, int left, int right) {
	return s.substr(left, s.size() - left - right);
}

//bool emptyLine(const std::string & input)
//{
//	for(size_t i = 0; i < input.size(); i ++)
//		if(! isblank(input[i])) return false;
//	return true;
//}

bool my_getline(std::ifstream &inf, std::string &line) {
	if (!std::getline(inf, line))
		return false;
	int end = line.size() - 1;
	while (end >= 0 && (line[end] == '\r' || line[end] == '\n')) {
		line.erase(end--);
	}

	return true;
}

bool contain_uppercase_character(const std::string & s) {
	for (int i = 0; i < (int) s.size(); i++) {
		if (s[i] >= 'A' && s[i] <= 'Z')
			return true;
	}
	return false;
}

bool contain_hyphen(const std::string & s) {
	for (int i = 0; i < (int) s.size(); i++) {
		if (s[i] == '-')
			return true;
	}
	return false;
}

bool contain_number(const std::string & s) {
	for (int i = 0; i < (int) s.size(); i++) {
		if (s[i] >= '0' && s[i] <= '9')
			return true;
	}
	return false;
}


void replace_char_by_char(string &str, char c1, char c2)
{
	string::size_type pos = 0;
	for (; pos < str.size(); ++pos) {
		if (str[pos] == c1) {
			str[pos] = c2;
		}
	}
}

void split_bychars(const string& str, vector<string> & vec, const char *sep)
{	//assert(vec.empty());
	vec.clear();
	string::size_type pos1 = 0, pos2 = 0;
	string word;
	while((pos2 = str.find_first_of(sep, pos1)) != string::npos)
	{
		word = str.substr(pos1, pos2-pos1);
		pos1 = pos2 + 1;
		if(!word.empty())
			vec.push_back(word);
	}
	word = str.substr(pos1);
	if(!word.empty())
		vec.push_back(word);
}

// remove the blanks at the begin and end of string
void clean_str(string &str)
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




void str2uint_vec(const vector<string> &vecStr, vector<unsigned int> &vecInt)
{
	vecInt.resize(vecStr.size());
	int i = 0;
	for (; i < vecStr.size(); ++i)
	{
		vecInt[i] = atoi(vecStr[i].c_str());
	}
}

void str2int_vec(const vector<string> &vecStr, vector<int> &vecInt)
{
	vecInt.resize(vecStr.size());
	int i = 0;
	for (; i < vecStr.size(); ++i)
	{
		vecInt[i] = atoi(vecStr[i].c_str());
	}
}

void int2str_vec(const vector<int> &vecInt, vector<string> &vecStr)
{
	vecStr.resize(vecInt.size());
	int i = 0;
	for (; i < vecInt.size(); ++i) {
		ostringstream out;
		out << vecInt[i];
		vecStr[i] = out.str();
	}
}

void join_bystr(const vector<string> &vec, string &str, const string &sep)
{
	str = "";
	if (vec.empty()) return;
	str = vec[0];
	int i = 1;
	for(; i < vec.size(); ++i)
	{
		str += sep + vec[i];
	}
}


void split_pair_vector(const vector< pair<int, string> > &vecPair, vector<int> &vecInt, vector<string> &vecStr)
{
	int i = 0;
	vecInt.resize(vecPair.size());
	vecStr.resize(vecPair.size());
	for (; i < vecPair.size(); ++i) {
		vecInt[i] = vecPair[i].first;
		vecStr[i] = vecPair[i].second;
	}
}

void split_bychar(const string& str, vector<string>& vec,
				 const char separator)
{
	//assert(vec.empty());
	vec.clear();
	string::size_type pos1 = 0, pos2 = 0;
	string word;
	while((pos2 = str.find_first_of(separator, pos1)) != string::npos)
	{
		word = str.substr(pos1, pos2-pos1);
		pos1 = pos2 + 1;
		if(!word.empty())
			vec.push_back(word);
	}
	word = str.substr(pos1);
	if(!word.empty())
		vec.push_back(word);
}

void string2pair(const string& str, pair<string, string>& pairStr, const char separator)
{
	string::size_type pos = str.find_last_of(separator);
	if (pos == string::npos) {
    string tmp = str + "";
    clean_str(tmp);
		pairStr.first = tmp;
		pairStr.second = "";
	} else {
    string tmp = str.substr(0, pos);
    clean_str(tmp);
    pairStr.first =  tmp;
    tmp = str.substr(pos+1);
    clean_str(tmp);
		pairStr.second = tmp;
	}
}

void convert_to_pair(vector<string>& vecString,
					 vector< pair<string, string> >& vecPair)
{
	assert(vecPair.empty());
	int size = vecString.size();
	string::size_type cur;
	string strWord, strPos;
	for(int i = 0; i < size; ++i)
	{
		cur = vecString[i].find('/');

		if (cur == string::npos)
		{
			strWord = vecString[i].substr(0);
			strPos = "";
		}
		else if (cur == vecString[i].size()-1)
		{
			strWord = vecString[i].substr(0, cur);
			strPos = "";
		}
		else
		{
			strWord = vecString[i].substr(0, cur);
			strPos = vecString[i].substr(cur+1);
		}

		vecPair.push_back(pair<string, string>(strWord, strPos));
	}
}

void split_to_pair(const string& str, vector< pair<string, string> >& vecPair)
{
	assert(vecPair.empty());
	vector<string> vec;
	split_bychar(str, vec);
	convert_to_pair(vec, vecPair);
}


void chomp(string& str)
{
	string white = " \t\n";
	string::size_type pos1 = str.find_first_not_of(white);
	string::size_type pos2 = str.find_last_not_of(white);
	if (pos1 == string::npos || pos2 == string::npos)
	{
		str = "";
	}
	else
	{
		str = str.substr(pos1, pos2-pos1+1);
	}
}

int common_substr_len(string str1, string str2)
{
	string::size_type minLen;
	if (str1.length() < str2.length())
	{
		minLen = str1.length();
	}
	else
	{
		minLen = str2.length();
		str1.swap(str2); //make str1 the shorter string
	}

	string::size_type maxSubstrLen = 0;
	string::size_type posBeg;
	string::size_type substrLen;
	string sub;
	for (posBeg = 0; posBeg < minLen; posBeg++)
	{
		for (substrLen = minLen-posBeg; substrLen > 0; substrLen--)
		{
			sub = str1.substr(posBeg, substrLen);
			if (str2.find(sub) != string::npos)
			{
				if (maxSubstrLen < substrLen)
				{
					maxSubstrLen = substrLen;
				}

				if (maxSubstrLen >= minLen-posBeg-1)
				{
					return maxSubstrLen;
				}
			}
		}
	}
	return 0;
}

int get_char_index(string& str)
{
	assert(str.size() == 2);
	return ((unsigned char)str[0]-176)*94 + (unsigned char)str[1] - 161;
}


string word(string& word_pos)
{
	return word_pos.substr(0, word_pos.find("/"));
}
