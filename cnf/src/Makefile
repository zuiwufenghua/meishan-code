CXX=g++
DEF= -W -Wall -g -O0
INC= -I/opt/local/include -I. -I../lib
LIB= -L/opt/local/lib -L../lib

bin= tagger learner

all: $(bin)

tagger: tagger_main.o cnftagger.o  sparsevect.o sequence.o dic.o 
	$(CXX) $(DEF) tagger_main.o cnftagger.o  sequence.o sparsevect.o dic.o  -o tagger $(INC) $(LIB)

learner: learn_main.o cnflearn.o  sparsevect.o sequence.o dic.o 
	$(CXX) $(DEF) learn_main.o cnflearn.o  sequence.o sparsevect.o dic.o  -o learner $(INC) $(LIB)

tagger_main.o: tagger_main.cpp
	$(CXX) $(DEF) -c tagger_main.cpp $(INC) $(LIB)

cnftagger.o: cnftagger.cpp
	$(CXX) $(DEF) -fPIC -c cnftagger.cpp $(INC) $(LIB)

learn_main.o: learn_main.cpp
	$(CXX) $(DEF) -c learn_main.cpp $(INC) $(LIB)

cnflearn.o: cnflearn.cpp
	$(CXX) $(DEF) -fPIC -c cnflearn.cpp $(INC) $(LIB)

sparsevect.o: ../lib/sparsevect.cpp
	$(CXX) $(DEF) -fPIC -c ../lib/sparsevect.cpp $(INC) $(LIB)

dictest.o: ../lib/dictest.cpp
	$(CXX) $(DEF) -fPIC -c ../lib/dictest.cpp $(INC) $(LIB)

dic.o: ../lib/dic.cpp
	$(CXX) $(DEF) -fPIC -c ../lib/dic.cpp $(INC) $(LIB)

sequence.o: ../lib/sequence.cpp
	$(CXX) $(DEF) -fPIC -c ../lib/sequence.cpp $(INC) $(LIB)



clean:
	@rm -rf libcnf.* *.o ../lib/*.o sequencetest  *.core $(bin)
