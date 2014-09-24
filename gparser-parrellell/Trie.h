//=========================================================
// @Author: Chen Xin (xchen@ir.hit.edu.cn)
// @Date:   2011/03/09
// @Brief:  Interface of Doule Array Trie Implementation.
//			darts_clone saves 50% memory, retrieves 50% fast
//==========================================================

#ifndef _TRIE_H
#define _TRIE_H

#include <vector>
#include <algorithm>
#include <iostream>
using namespace std;

#include "darts_clone.h"

#define key_type const char*
//#define result_type int
#define value_type int


class StrCmp
{
public:
	bool operator() (key_type k1, key_type k2) const
	{
		return strcmp(k1, k2) < 0 ? true : false;
	}
};


class Node
{
public:
	key_type k;
	value_type v;
	Node() {};
	Node(key_type _k, value_type _v) : k(_k), v(_v) {};
};

class NodeCmp
{
public:
	bool operator() (const Node& n1, const Node& n2) const
	{
		return strcmp(n1.k, n2.k) < 0 ? true : false;
	}
};


// Interface of Doule Array Trie Implementation
class Trie
{
public:
   Trie(bool use_value = false)
   {
       _da = new Darts::DoubleArray();
	   _use_value = use_value;
   }

   ~Trie()
   {
      _da->clear();
       delete _da;
   }

   void use_value(bool flag)
   {
		_use_value = flag;
   }

   void add(key_type key)
   {
	   _vec_key.push_back(key);
   }

   void add(key_type key, value_type value)
   {
	   _vec_node.push_back(Node(key, value));
   }

   void build()
   {
	   cerr << "building da_trie...\r";
	   if (_use_value)
	   {
		   sort(_vec_node.begin(), _vec_node.end(), NodeCmp());
		   _vec_key.resize(_vec_node.size());
		   _vec_value.resize(_vec_node.size());
		   for (size_t i=0; i<_vec_node.size(); i++)
		   {
				_vec_key[i] = _vec_node[i].k;
				_vec_value[i] = _vec_node[i].v;
		   }
	       _da->build(_vec_key.size(), &_vec_key[0], 0, &_vec_value[0]);
	   }
	   else
	   {
		   sort(_vec_key.begin(), _vec_key.end(), StrCmp());
	       _da->build(_vec_key.size(), &_vec_key[0]);
	   }
	   cerr << "building da_trie... done!\n";
   }

   value_type find(key_type key)
   {
       return _da->exactMatchSearch<value_type>(key);
   }

   int open(const char *file, const char *mode = "rb", size_t offset = 0, size_t size = 0)
   {
	   return _da->open(file, mode, offset, size);
   }

   int save(const char *file, const char *mode = "wb", size_t offset = 0)
   {
	   return _da->save(file, mode, offset);
   }

   size_t size()
   {
	   return this->_vec_key.size();
   }

   void report()
   {
	   cout << "DATrie unit_size: " << _da->unit_size() << " bytes" << endl;
	   cout << "DATrie units number: " << _da->size() << endl;
	   cout << "DATrie used units number: " << _da->nonzero_size() << endl;
	   cout << "DATrie used memory: " << _da->size() * _da->unit_size() << " bytes" << endl;
   }

   void reinit()
   {
     _vec_key.clear();
     _vec_value.clear();
     _vec_node.clear();
     _da->clear();
   }


	vector<key_type> _vec_key;
 vector<value_type> _vec_value;
	vector<Node> _vec_node;

private:
	Darts::DoubleArray * _da;
	bool _use_value;


/*
	// quick_sort implementation
	int _partition(vector<key_type>& key, vector<value_type>& value, int p, int r)
	{
		key_type x = key[r];
		int i = p - 1, tmp = 0;
		for (int j = p; j < r; ++j)
		{
			//if (key[j] <= x)
			if (strcmp(key[j], x) < 0)
			{
				++i;
				swap(key[i], key[j]);
				swap(value[i], value[j]);
			}
		}
		swap(key[r], key[i+1]);
		swap(value[r], value[i+1]);
		return i+1;
	}

	int _partition(vector<key_type>& key, int p, int r)
	{
		key_type x = key[r];
		int i = p - 1, tmp = 0;
		for (int j = p; j < r; ++j)
		{
			//if (key[j] <= x)
			if (strcmp(key[j], x) < 0)
			{
				++i;
				swap(key[i], key[j]);
			}
		}
		swap(key[r], key[i+1]);
		return i+1;
	}

	int _rand(int a, int b)  //[a, b]
	{
		return a + (b - a + 1) * rand() / RAND_MAX;
	}

	int _random_partition(vector<key_type>& key, vector<value_type>& value, int p, int r)
	{
		int i = _rand(p, r);
		swap(key[r], key[i]);
		swap(value[r], value[i]);
		_partition(key, value, p, r);
	}

	int _random_partition(vector<key_type>& key, int p, int r)
	{
		int i = _rand(p, r);
		swap(key[r], key[i]);
		_partition(key, p, r);
	}

	void _quick_sort(vector<key_type>& key, vector<value_type>& value, int low, int high)
	{
		if (low < high)
		{
			int q = _random_partition(key, value, low, high);
			_quick_sort(key, value, low, q-1);
			_quick_sort(key, value, q+1, high);
		}
	}

	void _quick_sort(vector<key_type>& key, int low, int high)
	{
		cout << low << " " << high << endl;
		if (low < high)
		{
			int q = _random_partition(key, low, high);
			_quick_sort(key, low, q-1);
			_quick_sort(key, q+1, high);
		}
	}
*/

};




#endif

