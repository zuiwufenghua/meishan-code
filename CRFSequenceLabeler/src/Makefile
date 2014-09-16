cc=g++
cflags = -O2



obj = InstanceReader.o StringMap.o Pipe.o Parameters.o GzFile.o CharArrayHashFunc.o main.o InstanceWriter.o CharUtils.o FeatureDictionary.o CharArrayEqualFunc.o common.o Timer.o 

CRFSequenceLaberler: $(obj)
	$(cc) -o CRFSequenceLaberler $(obj) $(cflags)

InstanceReader.o: InstanceReader.cpp InstanceReader.h Reader.h Instance.h CharUtils.h
	$(cc) -c InstanceReader.cpp $(cflags)

StringMap.o: StringMap.cpp StringMap.h HashMap.h CharArrayEqualFunc.h CharArrayHashFunc.h CharUtils.h CppAssert.h common.h
	$(cc) -c StringMap.cpp $(cflags)

Pipe.o: Pipe.cpp Pipe.h Instance.h CharUtils.h InstanceReader.h Reader.h InstanceWriter.h Writer.h common.h
	$(cc) -c Pipe.cpp $(cflags)

Parameters.o: Parameters.cpp Parameters.h FVec.h CppAssert.h common.h
	$(cc) -c Parameters.cpp $(cflags)

GzFile.o: GzFile.cpp GzFile.h
	$(cc) -c GzFile.cpp $(cflags)

CharArrayHashFunc.o: CharArrayHashFunc.cpp CharArrayHashFunc.h
	$(cc) -c CharArrayHashFunc.cpp $(cflags)

main.o: main.cpp Pipe.h Instance.h CharUtils.h InstanceReader.h Reader.h InstanceWriter.h Writer.h common.h DataInterface.h NRMat.h FeatureDictionary.h FVec.h CppAssert.h StringMap.h HashMap.h CharArrayEqualFunc.h CharArrayHashFunc.h Argument_helper.h
	$(cc) -c main.cpp $(cflags)

InstanceWriter.o: InstanceWriter.cpp InstanceWriter.h Writer.h Instance.h CharUtils.h
	$(cc) -c InstanceWriter.cpp $(cflags)

CharUtils.o: CharUtils.cpp CharUtils.h CppAssert.h
	$(cc) -c CharUtils.cpp $(cflags)

FeatureDictionary.o: FeatureDictionary.cpp FeatureDictionary.h FVec.h CppAssert.h StringMap.h HashMap.h CharArrayEqualFunc.h CharArrayHashFunc.h CharUtils.h GzFile.h common.h
	$(cc) -c FeatureDictionary.cpp $(cflags)

CharArrayEqualFunc.o: CharArrayEqualFunc.cpp CharArrayEqualFunc.h
	$(cc) -c CharArrayEqualFunc.cpp $(cflags)

common.o: common.cpp common.h
	$(cc) -c common.cpp $(cflags)

Timer.o: Timer.cpp Timer.h
	$(cc) -c Timer.cpp $(cflags)


clean:
	rm -rf *.o
	rm -rf 
	rm -rf CRFSequenceLaberler CRFSequenceLaberler.exe

