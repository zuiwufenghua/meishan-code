lgdpj r40
2013.6.1 Li Zhenghua
http://ir.hit.edu.cn/~lzh


CoNLL format in 10 Columns

Column 0 (ID)

Column 1 (WORD)

Column 2 (LEMMA)
	- For Chinese, put here the characters seperated with ``##'', e.g., ``ґч##Па##Бъ''.
	- For English, empty
	
Column 3 (CPOSTAGS)
	- The gold-standard POS tags
	
Column 4 (POSTAG)
	- Empty
	
Column 5 (FEATS)
	- The pruned dependency and label candidates, e.g.,
		- Only dependency candidates: ``22_19_18_21_14_17''
		- Both dependency and label candidates: ``22[cc|num|nn|dep|amod|det|conj]_19[cc]_18[cc]_21[cc]_14[cc|dobj|dep]_17[cc]''
		
Column 6 (HEAD)
	- The gold-standard head ID
	
Column 7 (LABEL)
	- The gold-standard label
	
Column 8 (PHEAD)
	- Empty
	
Column 9 (PLABEL)
	- The POS tag candidates, e.g., ``NN_VBP_VB''
	
	
