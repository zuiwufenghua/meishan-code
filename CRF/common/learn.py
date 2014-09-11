'''
Created on Sep 2, 2014

@author: masonzms
'''
import sys
import os
from logger import INFO, ERROR, LOG, LOG2
from viterbi import viterbi
from Instance import Instance
from crfmodel import crfmodel
from lbfgs import lbfgs

def evaluate(model, eval_file):
    fp = open(eval_file, "r")
    instances = [Instance(c) for c in fp.read().strip().split("\n\n")]

    nr_correct = 0
    nr_tags = 0
    for instance in instances:
        predict = viterbi(model, instance)
        for index, word in enumerate(instance.raw):
            if predict[index] == model.tags.get(word[0], 0):
                nr_correct += 1

        nr_tags += len(instance)

    acc = float(nr_correct) / nr_tags
    LOG2(INFO, "accuracy = %f (%d/%d)" % (acc, nr_correct, nr_tags))

def learn(opts):
    try:
        fp = open(opts.train_file, "r")
    except IOError:
        LOG(ERROR, "Failed to open train file %s:" % opts.train_file)
        return
    except:
        return

    m = crfmodel()
    instances = [Instance(c) for c in fp.read().strip().split("\n\n")]
    fp.close()

    m.preprocess(instances)

    LOG(INFO, "number of tags %d" % m.nr_tags)
    LOG(INFO, "number of attributes %d" % m.nr_feats)
    LOG(INFO, "number of instances %d" % len(instances))
    LOG(INFO, "paramter dimision is %d" % m.nr_dim)



    lbfgs(model = m, instances = instances)

    if opts.dev_file:
        evaluate(m, opts.dev_file)
            
            
if __name__ == '__main__':
    from optparse import OptionParser

    usage = "This is a toy program of linear-chained CRF"
    optparser = OptionParser(usage)
    optparser.add_option("-t", "--train", dest="train_file",
                         help="specify training file")
    optparser.add_option("-d", "--dev",  dest="dev_file",
                         help="specify development file")
    optparser.add_option("-a", "--algorithm", dest="algorithm",
                         help="specify training algorithm",
                         default="l2sgd")
    optparser.add_option("-e", "--eta", dest="eta",
                         help="initial learning rate",
                         type = float, default = 1.)
    optparser.add_option("-i", "--epoth", dest="epoth",
                         help="specify training epoth",
                         type = int, default = 3)
    optparser.add_option("-c", "--cepoth", dest="cepoth",
                         help="specify training interval length",
                         type = int, default = 3)
    optparser.add_option("-v", "--verbose", dest="verbose",
                         action="store_true", default=False,
                         help="verbose output")

    opts, args = optparser.parse_args()

    if opts.verbose:
        import logger
        logger.__VERBOSE__ = True

    if len(args) == 0 or args[0] not in ["learn", "tag"]:
        print >> sys.stderr, "ERROR: command [learn/tag] must be specified.\n"
        optparser.print_help()
        sys.exit(1)

    if args[0] == "learn":
        learn(opts)
        
#    if args[0] == "tag":
#        learn(opts)