'''
Created on Sep 2, 2014

@author: masonzms
'''
from logger import LOG, INFO
from scipy import optimize
import os
import sys

from crfmodel import gradient_test, likelihood, dlikelihood

def lbfgs(model, instances):
    '''
    Invoke the scipy.optimize.fmin_l_bfgs_b to perform optimization
    '''
    def callback(xk):
        LOG(INFO, "L-BFGS finish one iteration")
        LOG(INFO, "Gradient test starts: ")
        gradient_test(model.w, instances, model)

    model.w, f, d = optimize.fmin_l_bfgs_b(likelihood,
                                           model.w,
                                           fprime = dlikelihood,
                                           args = (instances, model),
                                           iprint = 1,
                                           factr = 1e12,
                                           callback = callback
                                           )