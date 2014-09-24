lgdpj r40
2013.6.1 Li Zhenghua
http://ir.hit.edu.cn/~lzh

english=1
	- language: English (1) or Chinese (0)

labeled=1
	- labeled dependency parsing

decoder=2o-carreras
	- decoder type: 1o; 2o-carreras, 3o-koo
	
fconf="d|du|dbi|ds|dbe|p|pu|pb|pch|s|sl|sb|g|gl|gb"
	- feature sets, for example:
		- d -> dependency features
		- du -> dependency unigram features ...
		
use-lemma=1
	- use lemma-related features
		- For Chinese, use the last character of each word as its lemma.
		- For English, use the 5-letter prefix.
		
use-chars=1
	- use Chinese characters in POS tagging or parsing features, only for Chinese

use-coarse-postag=1
	- use coarse-grained POS tags (NNP=>NN), only for English


k-best-pos=3
	- use the top-k best POS tag candidates for each word during joint decoding.

use-filtered-heads=1
	- use the pruned dependency candidates in column 5 (FEATS)
	
use-filtered-labels=1
	- use the pruned label candidates in column 5 (FEATS). This option is effective only when labeled=1
	- format: 22[cc|num|nn|dep|amod|det|conj]_19[cc]_18[cc]_21[cc]_14[cc|dobj|dep]_17[cc]
	
thread-num=10
	- thread number for parralleled training or test
	
train-parallel-creating-features-and-decoding=1
	- during training, use one thread to decode and other threads to create features

train-creating-features-ahead=15
	- during training, the maximum number of instances for which features are created ahead. The larger this number, the more memory will be used.

train-use-pa=1
	- choice for training algorithm
		- 1: PA or SPA
		- 0: AP
		
train-use-seperate-pa=1
	- choose between SPA (1) and PA (0)

train=1
	- train phase

iter-num=20
	- maximum iteration number

train-file:/users/nash/gdp-svn/cdt/pku-n-v/train.lem.no-neg-all-tag.conll06
dev-file:/users/nash/gdp-svn/cdt/pku-n-v/dev.lem.no-neg-all-tag.conll06

inst-max-len-to-throw=100
	- throw long instances
	
inst-max-num-train=-1
	- the instance number used for training (for debugging); negative number means using all

dictionary-path="."
dictionary-exist=0
	- whether the dictionaries are previously built
		- feature map: string => id
		- label map
		- POS tag map ...	

parameter-path="."
	- the path for model parameters (feature weights)

test=0

param-num-for-eval=10
	- during test, use the model parameters (feature weights) trained by the corresponding iteration number

test-file=/users/nash/gdp-svn/cdt/pku-n-v/test.lem.no-neg-all-tag.conll06

output-file=test.out.conll06

inst-max-num-eval=-10
	- the instance number used for test (for debugging); negative number means using all


display-interval=100
	- logging




