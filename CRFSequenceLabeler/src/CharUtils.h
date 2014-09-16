/**
 * Contains some string manipulation functions
 * @file CharUtils.h
 * @author Mihai Surdeanu
 */

#include <string>
#include <vector>
#include <sstream>
#include <cstring>
#include <exception>
#include <iostream>
#include <fstream>

#ifndef EGSTRA_CHAR_UTILS_H
#define EGSTRA_CHAR_UTILS_H

using namespace std;

template <typename Ty>
class MyVector {
public:
	Ty *m_data; // The last one is not used!
public:
	int m_capacity;
	int m_size;
public:
	MyVector() : m_data(0), m_capacity(0), m_size(0) {

	}

	~MyVector() {
		if (m_data) {
			delete [] m_data;
		}
	}

	bool empty() {
		return m_size == 0;
	}
	int capacity() const {
		return m_capacity;
	}

	int size() const {
		return m_size;
	}

	void clear() {
		m_size = 0;
	}
	int resize(int _size) {
		if (_size < 0) {
			cout << "MyVector::resize() err: new size is: " << _size << endl;
			return -1;
		}

		if (_size <= m_capacity) {
			m_size = _size;
		}
		else { // _size > m_capacity
			int new_capacity = 2 * _size;
			try {
				if (m_data) delete [] m_data;
				m_capacity = 0;
				m_size = 0;
				m_data = 0;
				m_data = new Ty[new_capacity + 1]; // The last one is not used!
			} catch (const exception &e) {
				cout << "MyVector::resize( " << new_capacity + 1 << " ) exception" << endl;
				cout << "element size: " << sizeof(Ty) << endl;
				cout << e.what() << endl;
				return -1;
			}
			m_capacity = new_capacity;
			m_size = _size;
		}

		return 0;
	}

	Ty *begin() {
		return m_data;
	}

	const Ty *begin() const {
		return m_data;
	}

	Ty &operator[](int pos) {
		if (pos < 0 || pos >= size()) return m_data[size()];
		return m_data[pos];
	}

	const Ty &operator[](int pos) const {
		if (pos < 0 || pos >= size()) return m_data[size()];
		return m_data[pos];
	}
};


class string_less
{
public:
	bool operator()(const string &str1, const string &str2) const {
		int ret = strcmp(str1.c_str(), str2.c_str());
		if (ret < 0) return true;
		else return false;
	}
};

inline void print_time() {

	time_t lt=time(NULL);
	cout << ctime(&lt) << endl;

}

inline void readObject(FILE *inf, int &obj) {
	fread(&obj, sizeof(int), 1, inf);
}

template <typename Ty>
void readObject(FILE *inf, MyVector<Ty> &obj) {
  obj.clear();
	int size = 0;
	fread(&size, sizeof(int), 1, inf);
	if (size <= 0) {
		return;
	}
	obj.resize(size);
	fread(obj.begin(), sizeof(Ty), size, inf);
}


inline void writeObject(FILE *outf, int obj) {
	fwrite(&obj, sizeof(int), 1, outf);
}

template <typename Ty>
inline void printObject(ofstream &fout, Ty obj) {
	fout << obj ;
}


inline void writeObject(FILE *outf, const string &obj) {
	int size = obj.size() + 1;
	fwrite(&size, sizeof(int), 1, outf);
//	cout << "write size: " << size << endl;
	if (!obj.empty()) {
		fwrite(obj.c_str(), obj.size()*sizeof(char), 1, outf);
	}
	char end = '\0';
	fwrite(&end, sizeof(char), 1, outf);
}

inline void writeObject(FILE *outf, const vector<string> &obj) {
	int size = obj.size();
	fwrite(&size, sizeof(int), 1, outf);
//	cout << "write size: " << size << endl;
  for(int i = 0; i < size; i++)
  {
    writeObject(outf, obj[i]);
  }
}

inline void readObject(FILE *inf, string &obj)
{
  MyVector<char> my_str;
	readObject(inf, my_str);
	obj = my_str.begin();
}


inline void readObject(FILE *inf, vector<string> &obj)
{
  obj.clear();
  int size = 0;
	fread(&size, sizeof(int), 1, inf);
	if (size <= 0) {
		return;
	}
	obj.resize(size);
	for(int i = 0; i < size; i++)
	{
	  readObject(inf, obj[i]);
	}
}



template <typename Ty>
void writeObject(FILE *outf, const vector<Ty> &obj) {
	int size = obj.size();
	fwrite(&size, sizeof(size), 1, outf);
	if (0 == size) return;
	fwrite(&(*obj.begin()), sizeof(Ty), size, outf);
}

template <typename Ty>
void printObject(ofstream &fout, const vector<Ty> &obj) {
	int size = obj.size();
	fout << "\\[" << size ;
	if (0 == size) return;
	for(int i = 0; i < obj.size(); i++)
	{
	  fout << " " << obj[i] ;
	}
	fout << "\\]";
}




inline void readObject(ifstream &inf, int &obj) {
	inf >> obj;
	string str;
	getline(inf, str);
}

inline void readObject(ifstream &inf, vector<int> &obj) {
	int size = 0;
	inf >> size;
	obj.resize(size);
	int i = 0;
	for (; i < size; ++i) {
		inf >> obj[i];
	}
	string str;
	getline(inf, str);
}

inline void readObject(ifstream &inf, vector<double> &obj) {
	int size = 0;
	inf >> size;
	obj.resize(size);
	int i = 0;
	for (; i < size; ++i) {
		inf >> obj[i];
	}
	string str;
	getline(inf, str);
}



inline void writeObject(ofstream &outf, int obj) {
	outf << obj << endl;
}
inline void writeObject(ofstream &outf, const vector<int> &obj) {
	outf << obj.size();
	int i = 0;
	for (; i < obj.size(); ++i) {
		outf << " " << obj[i];
	}
	outf << endl;
}

inline void writeObject(ofstream &outf, const vector<double> &obj) {
	outf << obj.size();
	int i = 0;
	for (; i < obj.size(); ++i) {
		outf << " " << obj[i];
	}
	outf << endl;
}

inline char* mystrcat(char *dst, const char *src)
{
    int n = (dst != 0 ? strlen(dst) : 0);
    dst = (char*)realloc(dst, n + strlen(src) + 1);
    strcat(dst, src);
    return dst;
}


inline char* mystrdup(const char *src)
{
    char *dst = (char*)malloc(strlen(src)+1);
    if (dst != NULL) {
        strcpy(dst, src);
    }
    return dst;
}

inline int message_callback(void *instance, const char *format, va_list args)
{
    vfprintf(stdout, format, args);
    fflush(stdout);
    return 0;
}

// split by each of the chars
void split_bychars(const string& str, vector<string> & vec, const char *sep = " ");

void replace_char_by_char(string &str, char c1, char c2);

// remove the blanks at the begin and end of string
void clean_str(string &str);
inline void remove_beg_end_spaces(string &str) { clean_str(str); }

bool my_getline(ifstream &inf, string &line);

void int2str_vec(const vector<int> &vecInt, vector<string> &vecStr);

void str2uint_vec(const vector<string> &vecStr, vector<unsigned int> &vecInt);

void str2int_vec(const vector<string> &vecStr, vector<int> &vecInt);

void join_bystr(const vector<string> &vec, string &str, const string &sep);

void split_bystr(const string &str, vector<string> &vec, const string &sep);
inline void split_bystr(const string &str, vector<string> &vec, const char *sep) { split_bystr(str, vec, string(sep));}

//split a sentence into a vector by separator which is a char
void split_bychar(const string& str, vector<string> & vec, const char separator = ' ');

//convert a string to a pair splited by separator which is '/' by default
void string2pair(const string& str, pair<string, string>& pairStr, const char separator = '/');

//convert every item separated by '/' in a vector to a pair
void convert_to_pair(vector<string>& vecString, vector< pair<string, string> >& vecPair);

//the combination of the two functions above
void split_to_pair(const string& str, vector< pair<string, string> >& vecPair);

void split_pair_vector(const vector< pair<int, string> > &vecPair, vector<int> &vecInt, vector<string> &vecStr);



//delete the white(space, Tab or a new line) on the two sides of a string
void chomp(string& str);

//get the length of the longest common string of two strings
int common_substr_len(string str1, string str2);

//compute the index of a Chinese character, the input
//can be any string whose length is larger than 2
int get_char_index(string& str);

string word(string& word_pos);



void simpleTokenize(const string & input,
		vector<string> & output, const string & separators);

bool emptyLine(const string & input);

int toInteger(const string & s);

void toString(const int i, string& s);

string toUpper(const string & s);

string toLower(const string & s);

bool startsWith(const string & big, const string & small);

bool endsWith(const string & big, const string & small);

string stripString(const string & s, int left, int right);


bool contain_uppercase_character(const string & s);
bool contain_hyphen(const string & s);
bool contain_number(const string & s);

#endif /* EGSTRA_CHAR_UTILS_H */
