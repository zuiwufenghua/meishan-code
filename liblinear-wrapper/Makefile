cc=g++
cflags = -O2



obj = FeatureVec.o dnrm2.o daxpy.o main.o tron.o Alphabet.o common.o linear.o ddot.o JSTReader.o MyLib.o CONLLReader.o JSTPipe.o CONLLWriter.o dscal.o JSTOptions.o JSTParser.o SVMInterFace.o JSTWriter.o 

liblinear_wrapper: $(obj)
	$(cc) -o liblinear_wrapper $(obj) $(cflags)

FeatureVec.o: FeatureVec.cpp FeatureVec.h MyVector.h common.h
	$(cc) -c FeatureVec.cpp $(cflags)

dnrm2.o: dnrm2.cpp blas.h blasp.h
	$(cc) -c dnrm2.cpp $(cflags)

daxpy.o: daxpy.cpp blas.h blasp.h
	$(cc) -c daxpy.cpp $(cflags)

main.o: main.cpp JSTPipe.h JSTInstance.h FeatureVec.h MyVector.h common.h MyLib.h Alphabet.h hash_map.hpp Trie.h darts_clone.h CONLLReader.h JSTReader.h CONLLWriter.h JSTWriter.h JSTOptions.h MultiArray.h utf.h JSTParser.h SVMInterFace.h linear.h
	$(cc) -c main.cpp $(cflags)

tron.o: tron.cpp tron.h
	$(cc) -c tron.cpp $(cflags)

Alphabet.o: Alphabet.cpp Alphabet.h MyLib.h MyVector.h hash_map.hpp Trie.h darts_clone.h
	$(cc) -c Alphabet.cpp $(cflags)

common.o: common.cpp common.h
	$(cc) -c common.cpp $(cflags)

linear.o: linear.cpp linear.h tron.h
	$(cc) -c linear.cpp $(cflags)

ddot.o: ddot.cpp blas.h blasp.h
	$(cc) -c ddot.cpp $(cflags)

JSTReader.o: JSTReader.cpp JSTReader.h JSTInstance.h FeatureVec.h MyVector.h common.h MyLib.h
	$(cc) -c JSTReader.cpp $(cflags)

MyLib.o: MyLib.cpp MyLib.h MyVector.h
	$(cc) -c MyLib.cpp $(cflags)

CONLLReader.o: CONLLReader.cpp CONLLReader.h JSTReader.h JSTInstance.h FeatureVec.h MyVector.h common.h MyLib.h utf.h
	$(cc) -c CONLLReader.cpp $(cflags)

JSTPipe.o: JSTPipe.cpp JSTPipe.h JSTInstance.h FeatureVec.h MyVector.h common.h MyLib.h Alphabet.h hash_map.hpp Trie.h darts_clone.h CONLLReader.h JSTReader.h CONLLWriter.h JSTWriter.h JSTOptions.h MultiArray.h utf.h
	$(cc) -c JSTPipe.cpp $(cflags)

CONLLWriter.o: CONLLWriter.cpp CONLLWriter.h JSTWriter.h JSTInstance.h FeatureVec.h MyVector.h common.h MyLib.h
	$(cc) -c CONLLWriter.cpp $(cflags)

dscal.o: dscal.cpp blas.h blasp.h
	$(cc) -c dscal.cpp $(cflags)

JSTOptions.o: JSTOptions.cpp JSTOptions.h MyLib.h MyVector.h
	$(cc) -c JSTOptions.cpp $(cflags)

JSTParser.o: JSTParser.cpp JSTParser.h JSTOptions.h JSTPipe.h JSTInstance.h FeatureVec.h MyVector.h common.h MyLib.h Alphabet.h hash_map.hpp Trie.h darts_clone.h CONLLReader.h JSTReader.h CONLLWriter.h JSTWriter.h MultiArray.h utf.h SVMInterFace.h linear.h
	$(cc) -c JSTParser.cpp $(cflags)

SVMInterFace.o: SVMInterFace.cpp SVMInterFace.h linear.h JSTInstance.h FeatureVec.h MyVector.h common.h MyLib.h JSTOptions.h
	$(cc) -c SVMInterFace.cpp $(cflags)

JSTWriter.o: JSTWriter.cpp JSTWriter.h JSTInstance.h FeatureVec.h MyVector.h common.h MyLib.h
	$(cc) -c JSTWriter.cpp $(cflags)


clean:
	rm -rf *.o
	rm -rf 
	rm -rf liblinear_wrapper liblinear_wrapper.exe

