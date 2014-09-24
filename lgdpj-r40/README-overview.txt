lgdpj r40
Zhenghua Li
http://ir.hit.edu.cn/~lzh
2013.6.1

This package implments a Labeled Graph-based Dependency Parsing Joint (lgdpj) with POS tagging for both Chinese and English.


Three versions of joint models are implemented according to the complexity of the graph-based parsing component:
-- Labeled first-order model (McDonald, 2005)
-- Labeled second-order model (Carreras, 2007)
-- Unlabeled third-order model (Koo et al., 2010)


-------------------

Efficiency Issue:

Since the decoding speed is quite slow for second/third-order joint models, we have tried a few optimization strategies.

-- Parrellel training: 
	Use several threads to build the features ahead of time.
	Use one thread to decode the current instance and update the feature weights accordingly.
	
-- Parrellel decoding:
	Parse several instances simultaneously using multi-thread techniques. The results of the instances will be output according to their positions in the input file.

-- POS tag pruning
	One can prune the POS tag candidates for each word using the marginal probabilities based on probabilistic CRF models (Li et al., 2012); Another choice is to use the candidates contained in the n-best POS sequences from an arbitrary POS tagger.
	The POS tag candidates should be stored in the 9th (last, PLABEL) column in the input CoNLL file.

-- Coarse-to-fine parsing (Koo et al., 2010)
	The candidate heads and labels for each word can be pruned using the coarse-to-fine strategy.
	The pruned candidates are stored in the 4th (POSTAG) column in the input CoNLL file.

-------------------	

Training algorithms

We implement three training algorithms:
-- Averaged Perceptron (AP)
-- Passive Aggressive (PA)
-- Separately Passive Aggressive (SPA)
As we showed in Li et al., (2012), SPA can better train the joint models.


-------------------	

Features:

-- The syntactic features
	We mainly borrow the feature sets listed in Bohnet (2010). For Chinese, we also use the last character of one word to compose some lemma-related features.
-- The POS tagging features
	For English, we adopt the feature set of Ratnaparkhi (1996)
	For Chinese, we adopt those of Zhang and Clark (2008)

-------------------

Borrowed Codes

-- The implementation of multi-array is borrowed from "Numerical Recipes in C++" by Willam H. Press et al.
-- Many codes are from the EGSTRA package by Xavier Carreras and Terry Koo and Mihai Surdeanu.
-- The codes for multi-trhead control is from ThreadPool by Stephen Liu <stephen.nil@gmail.com> 


-------------------	

References

Zhenghua Li, Min Zhang, Wanxiang Che, Ting Liu. 2012. A Separately Passive-Aggressive Training Algorithm for Joint POS Tagging and Dependency Parsing. In Proceedings of COLING 2012. 2012.12, pp. 1681-1698
Zhenghua Li, Min Zhang, Wanxiang Che, Ting Liu, Wenliang Chen, Haizhou Li. 2011. Joint Models for Chinese POS Tagging and Dependency Parsing. In Proceedings of EMNLP 2011. 2011.07, pp. 1180-1191


Bernd Bohnet. 2010. Top accuracy and fast dependency parsing is not a contradiction. In Proceedings of COLING 2010, pages 89每97
Xavier Carreras. Experiments with a higher-order projective dependency parser. In Proceedings of EMNLP/CoNLL 2007, pages 141每150.
Ryan McDonald, Koby Crammer, and Fernando Pereira. 2005. Online large-margin training of dependency parsers. In Proceedings of ACL 2005, pages 91每98.
Adwait Ratnaparkhi. 1996. A maximum entropy model for part-of-speech tagging. In Proceedings of EMNLP 1996.
Yue Zhang and Stephen Clark. 2008. Joint word segmentation and POS tagging using a single perceptron. In Proceedings of ACL-08: HLT, pages 888每896.




