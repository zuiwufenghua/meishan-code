/**
 * Copyright (C) 2010- Kei Uchiumi
 */
# ifndef __S_VEC_H__
# define __S_VEC_H__
# include <list>
# define HASHSIZE 2597

typedef enum {
	SpvFailed,
	SpvInvalid,
	SpvOk
} SpvState;

typedef struct fact {
	struct fact *left, *right;
	int key;
	double val;
} *factptr;

class SparseVector
{
	public:
		/**
		 * @param	ac	allocator
		 */
		SparseVector();
		~SparseVector();
		/**
		 * @param	key	key
		 * @param	val	value
		 */
		SpvState add(int key, double val);
		/**
		 * @param	key	key
		 * @return	val	value
		 */
		double get(int key);
		/// key list
		std::list<int> keys;
	private:
		//SparseVector();
		//SparseVector(const SparseVector&);
		//SparseVector& operator=(const SparseVector&);

		/**
		 * hash table
		 */
		factptr *table;
		SpvState reset();
		static inline int hash(int key)
		{
			return key%HASHSIZE;
		}
};
# endif /* __S_VEC_H__ */
