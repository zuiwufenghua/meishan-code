'''
Created on Sep 2, 2014

@author: masonzms
'''
import os
import sys
import random
from collections import  defaultdict
from numpy import array, zeros, exp, add,log
from numpy.linalg import norm
# from scipy.misc import logsumexp 'for a higher version'
from scipy.misc import logsumexp
from logger import INFO, WARN, ERROR, LOG, trace

DELTA = 1.

class crfmodel(object):
    DUMMY = '_X_'
    
    def __init__(self):
        self.tag_dict = {}
        self.feat_dict = {}
        
        self.tag_dict[self.DUMMY] = 0
        self.w = None
        self._g0 = None
        self.g = None
        
    def preprocess(self, insts):
        for inst in insts:
            for item in inst.elems:
                tag, features = item
                if tag not in self.tag_dict:
                    self.tag_dict[tag] = len(self.tag_dict)
                for feature in features:
                    if feature not in self.feat_dict:
                        self.feat_dict[feature] = len(self.feat_dict)
                        
        self.nr_tags = len(self.tag_dict)
        self.nr_feats = len(self.feat_dict)
        self.nr_dim = (self.nr_feats + self.nr_tags) * self.nr_tags
        self.w = zeros(self.nr_dim, dtype = float)
        

from viterbi import forward, backward

def _likelihood(w, inst, model):
    L = len(inst)
    T = model.nr_tags
    A = model.nr_feats
    
    inst.build_instance(model.feat_dict, model.tag_dict, True)
    g0, g = inst.build_score_cache(w, L, T, A)
    F = inst.correct_feats
    
    ret = array([w[k] * v for k, v in F.iteritems()]).sum()   
    a = forward(g0, g, L, T)
    
    return ret - logsumexp(a[L-1, :])

def likelihood(w, insts, model):
    ret = 0
    for inst in insts:
        ret += _likelihood(w, inst, model)
    
    return -ret + ((w **2).sum() / (2 * DELTA **2))
 
 
def _dlikelihood(w, inst, model):
    
    grad = zeros(w.shape[0], dtype=float)
    L = len(inst)
    T = model.nr_tags
    A = model.nr_feats   
    
    inst.build_instance(model.feat_dict, model.tag_dict, True)
    g0, g = inst.build_score_cache(w, L, T, A)
    F = inst.correct_feats
    
    for k, v in F.iteritems():
        grad[k] += v
        
    a = forward(g0, g, L, T) 
    b = backward(g, L, T)
    
    logZ = logsumexp(a[L-1, :])
    
    U = inst.uni_feats
    B = inst.bi_feats
    
    c = exp(g0 + b[0, :] - logZ).clip(0., 1.)
    
    for i in xrange(1, L):
        c = exp(add.outer(a[i-1,:], b[i,:]) + g[i,:,:] - logZ).clip(0.,1.)
        # The following code is an equilism of this
        #for j in range(T):
        #    for k in range(T):
        #        grad[U[i,k]] -= c[j,k]
        #        grad[B[j,k]] -= c[j,k]
        for k in range(T):
            grad[U[i,k]] -= c[:,k].sum()
        grad[range(A*T, (A+T)*T)] -= c.flatten()

    #destroy_inst(inst)
    return grad


def dlikelihood(w, insts, model):
    '''
    Calculate the gradient on the overall data set.

    - param[w]  the
    '''
    N = len(insts)

    grad = zeros(w.shape[0], dtype=float)
    for index, inst in enumerate(insts):
        trace("Calculate gradient", index, N)
        grad += _dlikelihood(w, inst, model)

    return -(grad - w / DELTA)


def _gradient_test(w, inst, model, choosen_dims = None):
    '''
    The gradient test. Used to test if the gradient is correctly calculated.
    Detail of this method is described in Stochastic Gradient Descent Tricks
    by Bottou, L

    1. Pick an example z
    2. Compute the loss Q(z,w)
    3. Compute the gradient g = D_w Q(z,w)
    4. Apply a slightly pertubation to w'=w+\Delta.
    5. Compute the new loss Q(z,w') and verify Q(z,w')=Q(z,w)+g\Delta
    '''

    lossQ = _likelihood(w, inst, model)
    DlossQ = _dlikelihood(w, inst, model)

    inst.build_instance(model.feat_dict, model.tag_dict, True)

    L = len(inst)
    T = len(model.tag_dict)

    if not choosen_dims:
        U = inst.uni_feats
        B = inst.bi_feats
        features = []
        for i in range(L):
            for j in range(T):
                if i == 0:
                    features.extend(U[i,j])
                else:
                    for k in range(T):
                        features.extend(U[i,j].tolist())
                        features.extend(B[k,j].tolist())
        choosen_dims = random.sample(features, 5)

    epsilon = 1e-4
    grad_diff = epsilon * DlossQ[choosen_dims].sum()
    w[choosen_dims] += epsilon

    lossQ2 = _likelihood(w, inst, model)

    if abs(lossQ2 - (lossQ + grad_diff)) > 1e-7:
        LOG(WARN, "Failed gradient test.")
        LOG(WARN, "Pertubation on %s dims." % str(choosen_dims))
        LOG(WARN, "Loss before pertubation: %f" % lossQ)
        LOG(WARN, "Loss after pertubation: %f" % lossQ2)
        LOG(WARN, "Gradient difference: %f" % grad_diff)
    else:
        LOG(INFO, "Success gradient test.")

    w[choosen_dims] -= epsilon


def gradient_test(w, insts, model, choosen_dims = None):
    '''
    Sample some examples to perform gradient test
    '''
    for inst in random.sample(insts, min(5, len(insts))):
        _gradient_test(w, inst, model, choosen_dims)
      
        