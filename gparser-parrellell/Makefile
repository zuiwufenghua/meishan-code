cc=g++
cflags = -O2



obj = ParserOptions.o DepDecoder.o Parameter.o DepPipe.o FeatureVec.o main.o threadpool.o Alphabet.o common.o KBestParseForest.o DepInstance.o CONLLReader.o MyLib.o CONLLWriter.o DepParser.o DepWriter.o DepReader.o ParseForestItem.o 

gparser: $(obj)
	$(cc) -o gparser $(obj) $(cflags)  -lpthread

ParserOptions.o: ParserOptions.cpp ParserOptions.h MyLib.h MyVector.h
	$(cc) -c ParserOptions.cpp $(cflags)

DepDecoder.o: DepDecoder.cpp DepDecoder.h DepPipe.h DepInstance.h FeatureVec.h MyVector.h MyLib.h Alphabet.h hash_map.hpp Trie.h darts_clone.h CONLLReader.h DepReader.h CONLLWriter.h DepWriter.h Parameter.h ParserOptions.h common.h MultiArray.h spthread.h threadpool.h ParseForestItem.h KBestParseForest.h
	$(cc) -c DepDecoder.cpp $(cflags)

Parameter.o: Parameter.cpp Parameter.h DepInstance.h FeatureVec.h MyVector.h MyLib.h ParserOptions.h common.h MultiArray.h
	$(cc) -c Parameter.cpp $(cflags)

DepPipe.o: DepPipe.cpp DepPipe.h DepInstance.h FeatureVec.h MyVector.h MyLib.h Alphabet.h hash_map.hpp Trie.h darts_clone.h CONLLReader.h DepReader.h CONLLWriter.h DepWriter.h Parameter.h ParserOptions.h common.h MultiArray.h spthread.h threadpool.h
	$(cc) -c DepPipe.cpp $(cflags)

FeatureVec.o: FeatureVec.cpp FeatureVec.h MyVector.h
	$(cc) -c FeatureVec.cpp $(cflags)

main.o: main.cpp DepPipe.h DepInstance.h FeatureVec.h MyVector.h MyLib.h Alphabet.h hash_map.hpp Trie.h darts_clone.h CONLLReader.h DepReader.h CONLLWriter.h DepWriter.h Parameter.h ParserOptions.h common.h MultiArray.h spthread.h threadpool.h DepParser.h DepDecoder.h ParseForestItem.h KBestParseForest.h
	$(cc) -c main.cpp $(cflags)

threadpool.o: threadpool.cpp threadpool.h spthread.h
	$(cc) -c threadpool.cpp $(cflags)

Alphabet.o: Alphabet.cpp Alphabet.h MyLib.h MyVector.h hash_map.hpp Trie.h darts_clone.h
	$(cc) -c Alphabet.cpp $(cflags)

common.o: common.cpp common.h FeatureVec.h MyVector.h MultiArray.h
	$(cc) -c common.cpp $(cflags)

KBestParseForest.o: KBestParseForest.cpp KBestParseForest.h ParseForestItem.h FeatureVec.h MyVector.h DepInstance.h MyLib.h MultiArray.h common.h
	$(cc) -c KBestParseForest.cpp $(cflags)

DepInstance.o: DepInstance.cpp DepInstance.h FeatureVec.h MyVector.h MyLib.h
	$(cc) -c DepInstance.cpp $(cflags)

CONLLReader.o: CONLLReader.cpp CONLLReader.h DepReader.h DepInstance.h FeatureVec.h MyVector.h MyLib.h
	$(cc) -c CONLLReader.cpp $(cflags)

MyLib.o: MyLib.cpp MyLib.h MyVector.h
	$(cc) -c MyLib.cpp $(cflags)

CONLLWriter.o: CONLLWriter.cpp CONLLWriter.h DepWriter.h DepInstance.h FeatureVec.h MyVector.h MyLib.h
	$(cc) -c CONLLWriter.cpp $(cflags)

DepParser.o: DepParser.cpp DepParser.h Parameter.h DepInstance.h FeatureVec.h MyVector.h MyLib.h ParserOptions.h common.h MultiArray.h DepDecoder.h DepPipe.h Alphabet.h hash_map.hpp Trie.h darts_clone.h CONLLReader.h DepReader.h CONLLWriter.h DepWriter.h spthread.h threadpool.h ParseForestItem.h KBestParseForest.h
	$(cc) -c DepParser.cpp $(cflags)

DepWriter.o: DepWriter.cpp DepWriter.h DepInstance.h FeatureVec.h MyVector.h MyLib.h
	$(cc) -c DepWriter.cpp $(cflags)

DepReader.o: DepReader.cpp DepReader.h DepInstance.h FeatureVec.h MyVector.h MyLib.h
	$(cc) -c DepReader.cpp $(cflags)

ParseForestItem.o: ParseForestItem.cpp
	$(cc) -c ParseForestItem.cpp $(cflags)


clean:
	rm -rf *.o
	rm -rf 
	rm -rf gparser gparser.exr

