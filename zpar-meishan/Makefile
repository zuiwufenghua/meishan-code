 #****************************************************************
#
# Makefile
#
# Yue Zhang
# Computing lab, Oxford. 2006.10 - 2008.2
#
#****************************************************************

# Makable targe systems include:
#
# === Chinese ===
# segmentor - Chinese word segmentor
# chinese.postagger - Chinese POS tagger (joint / single)
# chinese.depparser - Chinese dependency parser
#
# === English ===
# english.postagger - English POS tagger
# english.depparser - English dependency parser

#================================================================
#
# Configurations
#
#================================================================

# choose between agenda, agendachart etc ## NO SPACE AFTER NAME ###
#
# agenda: the single agenda method - reproduce paper
# agendaplus: try to improve upon the decoding algorithm of agenda
# viterbi: dynamic programming
SEGMENTOR_IMPL = agenda

# Chinese postagger implementations
#
# joint taggers include the following implementations
# agendachart: combining agenda and chart, this is the best system - reproduce paper
# 
# taggers on segmented sentences include the following implementations
# segmented: the unidirectional trigram tagger
CHINESE_TAGGER_IMPL = agendalexicon

CHINESE_STTAGGER_IMPL = stagenda

# Chinese dependency parser
#
# currently support eisner, covington, nivre, combined and joint implementations 
CHINESE_DEPPARSER_IMPL = arcstandard
CHINESE_DEPPARSER_LABELED = true
CHINESE_DEPLABELER_IMPL = naive

# currently support eisner, covington, nivre, combined and joint implementations 
CHINESE_POSDEPPARSER_IMPL = arcstandard
CHINESE_POSDEPPARSER_LABELED = true
CHINESE_POSDEPLABELER_IMPL = naive
CHINESE_PROCESS_MANNER = wordbase

# currently support sr implementations 
CHINESE_CONPARSER_IMPL = cad

# currently support only agenda
ENGLISH_TAGGER_IMPL = agenda

# currently support eisner, covington, nivre, combined implementations 
ENGLISH_DEPPARSER_IMPL =arcstandard
ENGLISH_DEPPARSER_LABELED = true
ENGLISH_DEPLABELER_IMPL = naive

# currently support eisner, covington, nivre, combined implementations 
ENGLISH_POSDEPPARSER_IMPL = arcstandard
ENGLISH_POSDEPPARSER_LABELED = true
ENGLISH_POSDEPLABELER_IMPL = naive
# currently support sr implementations 
ENGLISH_CONPARSER_IMPL = cad

#================================================================
#
# Debug mode or the run mode (empty)
#
#================================================================

DEBUG = -DDEBUG -g -O0
#DEBUG = -DNDEBUG -O3

#================================================================
#
# directory configurations
#
#================================================================

BASE_DIR = .
include Makefile.common

#================================================================
#
# compiler commands
# 
#================================================================

INCLUDES = -I$(SRC_INCLUDES)

CXX = g++
CXXFLAGS = -w -W  $(INCLUDES) $(DEBUG)

LD=$(CXX)
LDFLAGS =

#================================================================
#
# Shared objects
#
#================================================================

# the objects
LINGUISTICS_OBJECTS = $(OBJECT_DIR)/linguistics/lemma.o $(OBJECT_DIR)/linguistics/conll.o
OBJECTS = $(OBJECT_DIR)/reader.o $(OBJECT_DIR)/writer.o $(OBJECT_DIR)/options.o $(LINGUISTICS_OBJECTS)

$(OBJECT_DIR)/%.o: $(SRC_LIBS)/%.cpp $(SRC_INCLUDES)/%.h
	mkdir -p $(OBJECT_DIR)
	mkdir -p $(OBJECT_DIR)/linguistics
	$(CXX) $(CXXFLAGS) -c $< -o $@

all: zpar

# the directories
$(OBJECT_DIR):
	mkdir -p $(OBJECT_DIR)
$(DIST_DIR):
	mkdir -p $(DIST_DIR)

#  tagger
SRC_TAGGER = $(SRC_CHINESE)/tagger
DIST_TAGGER = $(DIST_DIR)/chinese.postagger
OBJECT_TAGGER = $(OBJECT_DIR)/chinese.postagger
$(DIST_TAGGER):
	mkdir $(DIST_TAGGER)
$(OBJECT_TAGGER):
	mkdir $(OBJECT_TAGGER)
	

#  tagger
SRC_STTAGGER = $(SRC_CHINESE)/sttagger
DIST_STTAGGER = $(DIST_DIR)/chinese.stpostagger
OBJECT_STTAGGER = $(OBJECT_DIR)/chinese.stpostagger
$(DIST_STTAGGER):
	mkdir $(DIST_STTAGGER)
$(OBJECT_STTAGGER):
	mkdir $(OBJECT_STTAGGER)

SRC_ENGLISH_TAGGER = $(SRC_COMMON)/tagger
DIST_ENGLISH_TAGGER = $(DIST_DIR)/english.postagger
OBJECT_ENGLISH_TAGGER = $(OBJECT_DIR)/english.postagger
$(DIST_ENGLISH_TAGGER):
	mkdir $(DIST_ENGLISH_TAGGER)
$(OBJECT_ENGLISH_TAGGER):
	mkdir $(OBJECT_ENGLISH_TAGGER)

#  depparser
SRC_COMMON_DEPPARSER = $(SRC_COMMON)/depparser
#ifeq ($(CHINESE_DEPPARSER_IMPL), joint)
#	SRC_CHINESE_DEPPARSER = $(SRC_CHINESE)/depparser
#else
#	SRC_CHINESE_DEPPARSER = $(SRC_COMMON_DEPPARSER)
#endif
SRC_CHINESE_DEPPARSER = $(SRC_COMMON_DEPPARSER)
DIST_DEPPARSER = $(DIST_DIR)/chinese.depparser
OBJECT_DEPPARSER = $(OBJECT_DIR)/chinese.depparser
DIST_ENGLISH_DEPPARSER = $(DIST_DIR)/english.depparser
OBJECT_ENGLISH_DEPPARSER = $(OBJECT_DIR)/english.depparser


#  posdepparser
SRC_COMMON_POSDEPPARSER = $(SRC_COMMON)/posdepparser
ifeq ($(CHINESE_PROCESS_MANNER), charbase)
	SRC_CHINESE_POSDEPPARSER = $(SRC_CHINESE)/posdepparser
else	
	SRC_CHINESE_POSDEPPARSER = $(SRC_COMMON_POSDEPPARSER)
endif


#SRC_CHINESE_POSDEPPARSER = $(SRC_COMMON_POSDEPPARSER)
DIST_POSDEPPARSER = $(DIST_DIR)/chinese.posdepparser
OBJECT_POSDEPPARSER = $(OBJECT_DIR)/chinese.posdepparser
DIST_ENGLISH_POSDEPPARSER = $(DIST_DIR)/english.posdepparser
OBJECT_ENGLISH_POSDEPPARSER = $(OBJECT_DIR)/english.posdepparser

# deplabeler
SRC_COMMON_DEPLABELER = $(SRC_COMMON)/deplabeler
SRC_CHINESE_DEPLABELER = $(SRC_COMMON_DEPLABELER)
DIST_DEPLABELER = $(DIST_DIR)/chinese.deplabeler
OBJECT_DEPLABELER = $(OBJECT_DIR)/chinese.deplabeler
SRC_ENGLISH_DEPLABELER = $(SRC_COMMON_DEPLABELER)
DIST_ENGLISH_DEPLABELER = $(DIST_DIR)/english.deplabeler
OBJECT_ENGLISH_DEPLABELER = $(OBJECT_DIR)/english.deplabeler

#  conparser
SRC_COMMON_CONPARSER = $(SRC_COMMON)/conparser
SRC_CHINESE_CONPARSER = $(SRC_COMMON_CONPARSER)
ifeq ($(CHINESE_PROCESS_MANNER), charbase)
	SRC_CHINESE_CONPARSER = $(SRC_CHINESE)/conparser
else 
	SRC_CHINESE_CONPARSER = $(SRC_COMMON_CONPARSER)
endif

SRC_ENGLISH_CONPARSER = $(SRC_COMMON_CONPARSER)
DIST_CONPARSER = $(DIST_DIR)/chinese.conparser
OBJECT_CONPARSER = $(OBJECT_DIR)/chinese.conparser
DIST_ENGLISH_CONPARSER = $(DIST_DIR)/english.conparser
OBJECT_ENGLISH_CONPARSER = $(OBJECT_DIR)/english.conparser

#----------------------------------------------------------------
#
# zpar general
#
#----------------------------------------------------------------

include Makefile.zpar
include Makefile.zpar.en
include Makefile.zpar.ge

#----------------------------------------------------------------
#
# The sentence boundary detector
#
#----------------------------------------------------------------

include Makefile.doc2snt

#----------------------------------------------------------------
#
# The segmentor
#
#----------------------------------------------------------------

include Makefile.segmentor

#----------------------------------------------------------------
#
# The pos taggers (Chinese and English)
#
#----------------------------------------------------------------

include Makefile.postagger
include Makefile.stpostagger

#----------------------------------------------------------------
#
# The depparsers (Chinese and English)
#
#----------------------------------------------------------------

include Makefile.depparser
include Makefile.deplabeler

include Makefile.posdepparser

#----------------------------------------------------------------
#
# The conparser
#
#----------------------------------------------------------------

include Makefile.conparser

#----------------------------------------------------------------
#
# The ccgparser
#
#----------------------------------------------------------------

include Makefile.ccg

#----------------------------------------------------------------
#
# Miscelaneous
#
#----------------------------------------------------------------

include Makefile.misc
#include Makefile.rr

