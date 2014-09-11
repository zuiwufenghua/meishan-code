'''
Created on Sep 2, 2014

@author: masonzms
'''
from collections import defaultdict
from itertools import chain
from numpy import array, zeros

class Instance(object):
    def __init__(self, context):
        self.elems = []
        for line in context.split('\n'):
            tokens = line.split()
            tag = tokens[0]
            features = tokens[1:]

            self.elems.append((tag, features))
        
        self.uni_feats = None
        self.bi_feats = None
        self.correct_feats = None

    def __len__(self):
        return len(self.elems)
    
    def _destroy_(self):
        self.uni_feats = None
        self.bi_feats = None
        self.correct_feats = None
        self.elems = None
                              
    def build_instance(self, _feat_dict, _tag_dict,  train = True):   
        if(self.uni_feats is not None and self.bi_feats is not None):
            return
        
        self.uni_feats = U = {}    
        self.bi_feats = B = {}
        self.correct_feats = F = defaultdict(int)
        
        T = len(_tag_dict)
        A = len(_feat_dict)
        
        for i, item in enumerate(self.elems):
            tag, features = item
            features = [_feat_dict[feature] for feature in features if feature in _feat_dict]
            #newfeatures = []
            #for feature in features:
            #    if feature in _feat_dict:
            #        newfeatures.append(_feat_dict[feature])
            for k in xrange(T):
                U[i,k] = array([feature*T +k for feature in features])
                
        for j in xrange(T):
            for k in xrange(T):
                B[j,k] = array([A*T+j*T+k])
        
        if train:
            j, k = None, _tag_dict[self.elems[0][0]]
            for e in U[0,k]:
                F[e] += 1
            for i, item in enumerate(self.elems[1:]):
                j, k = k, _tag_dict[item[0]]
                for e in chain(U[i+1, k].tolist(), B[j,k].tolist()):
                    F[e] += 1
 
    def build_score_cache(self, w, L, T, A):
        g0 = zeros(T, dtype=float)
        g = zeros((L, T, T), dtype=float)
        
        U = self.uni_feats
        B = self.bi_feats
        
        for j in xrange(T):
            g0[j] = w.take(U[0,j], axis=0).sum()
            
        for i in xrange(1,L):
            for k in xrange(T):
                pv = w.take(U[i,k], axis=0).sum()
                for j in xrange(T):
                    g[i,j,k] = pv + w[(A+j)*T+k]
                    
        return (g0, g)
