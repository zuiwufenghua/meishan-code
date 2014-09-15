/**
 * Copyright (C) 2010- Kei Uchiumi
 */
# include <dic.h>
# include <cstring>

struct dicnode nil = { NULL, NULL, std::string("\0"), 0 };

Dic::Dic(DicMode mode) {
	this->mode = mode;
	nodeptr p;
	this->wordnum = 0;
	this->table = new nodeptr[HASHSIZE];
	this->init();
}
/*
 Dic::Dic ()
 {
 }
 */
Dic::~Dic() {
	if (this->table != NULL) {
		for (int idx = 0; idx < HASHSIZE; idx++) {
			nodeptr *p = (this->table + idx);
			if (*p == &nil) {
				continue;
			}
			delete this->table[idx];
		}
		delete[] this->table;
	}
}

nodeptr Dic::getnil() {
	return &nil;
}

int Dic::init() {
	for (int i = 0; i < HASHSIZE; i++) {
		*(this->table + i) = &nil;
	}
	return 0;
}

int Dic::getsize() {
	return this->wordnum;
}

nodeptr Dic::insert(const char *word) {
	int dist = 0;
	nodeptr *p = (this->table + Dic::hash(word));
	while (*p != &nil && (dist = (*p)->key.compare(word)) != 0) {
		if (dist < 0) {
			p = &((*p)->left);
		} else {
			p = &((*p)->right);
		}
	}

	if (*p != &nil) { // stored
		if (this->mode == CountUp) {
			(*p)->val++;
		}
		return NULL;
	}

	nodeptr n = new dicnode;
	n->key = std::string(word);
	if (this->mode == CountUp) {
		n->val = 1;
		this->wordnum++;
	} else if (this->mode == Index) {
		n->val = this->wordnum++;
	}
	n->left = &nil;
	n->right = *p;
	*p = n;

	return *p;
}

nodeptr* Dic::get(const char *word) {
	int dist = 0;
	nodeptr *p = (this->table + Dic::hash(word));
	while ((dist = (*p)->key.compare(word)) != 0 && *p != &nil) {
		if (dist < 0) {
			p = &((*p)->left);
		} else {
			p = &((*p)->right);
		}
	}
	return p;
}

float Dic::getfillingrate() {
	int count = 0;
	for (int i = 0; i < HASHSIZE; i++) {
		nodeptr *p = (this->table + i);
		if (*p == &nil) {
			continue;
		}
		count++;
	}

	return (float) count / HASHSIZE;
}
