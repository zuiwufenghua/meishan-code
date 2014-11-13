cc=g++
cflags = -g -O0



obj = cqdb.o crf1d_context.o dictionary.o train_l2sgd.o params.o rumavl.o Pipe.o dataset.o crfsuite_train.o crf1d_model.o train_averaged_perceptron.o main.o holdout.o common.o crf1d_feature.o InstanceReader.o crf1d_tag.o train_lbfgs.o logging.o crfsuite.o option.o lookup3.o MyLib.o InstanceWriter.o quark.o lbfgs.o train_passive_aggressive.o train_arow.o crf1d_encode.o 

PartialCRF: $(obj)
	$(cc) -o PartialCRF $(obj) $(cflags)

cqdb.o: cqdb.cpp cqdb.h
	$(cc) -c cqdb.cpp $(cflags)

crf1d_context.o: crf1d_context.cpp os.h crfsuite.h crf1d.h crfsuite_internal.h logging.h vecmath.h
	$(cc) -c crf1d_context.cpp $(cflags)

dictionary.o: dictionary.cpp os.h crfsuite.h quark.h
	$(cc) -c dictionary.cpp $(cflags)

train_l2sgd.o: train_l2sgd.cpp os.h crfsuite.h crfsuite_internal.h logging.h params.h crf1d.h vecmath.h
	$(cc) -c train_l2sgd.cpp $(cflags)

params.o: params.cpp os.h crfsuite.h quark.h
	$(cc) -c params.cpp $(cflags)

rumavl.o: rumavl.cpp rumavl.h
	$(cc) -c rumavl.cpp $(cflags)

Pipe.o: Pipe.cpp Pipe.h Instance.h MyLib.h MyVector.h InstanceReader.h Reader.h InstanceWriter.h Writer.h MultiArray.h common.h utf.h
	$(cc) -c Pipe.cpp $(cflags)

dataset.o: dataset.cpp os.h crfsuite.h crfsuite_internal.h logging.h
	$(cc) -c dataset.cpp $(cflags)

crfsuite_train.o: crfsuite_train.cpp os.h crfsuite.h crfsuite_internal.h logging.h params.h crf1d.h
	$(cc) -c crfsuite_train.cpp $(cflags)

crf1d_model.o: crf1d_model.cpp os.h cqdb.h crfsuite.h crf1d.h crfsuite_internal.h logging.h
	$(cc) -c crf1d_model.cpp $(cflags)

train_averaged_perceptron.o: train_averaged_perceptron.cpp os.h crfsuite.h crfsuite_internal.h logging.h params.h vecmath.h
	$(cc) -c train_averaged_perceptron.cpp $(cflags)

main.o: main.cpp Pipe.h Instance.h MyLib.h MyVector.h InstanceReader.h Reader.h InstanceWriter.h Writer.h MultiArray.h common.h utf.h crfsuite_interface.h os.h option.h crfsuite.h Argument_helper.h
	$(cc) -c main.cpp $(cflags)

holdout.o: holdout.cpp os.h crfsuite.h crfsuite_internal.h logging.h
	$(cc) -c holdout.cpp $(cflags)

common.o: common.cpp common.h
	$(cc) -c common.cpp $(cflags)

crf1d_feature.o: crf1d_feature.cpp os.h crfsuite.h logging.h crf1d.h crfsuite_internal.h rumavl.h
	$(cc) -c crf1d_feature.cpp $(cflags)

InstanceReader.o: InstanceReader.cpp InstanceReader.h Reader.h Instance.h MyLib.h MyVector.h utf.h
	$(cc) -c InstanceReader.cpp $(cflags)

crf1d_tag.o: crf1d_tag.cpp os.h crfsuite.h crf1d.h crfsuite_internal.h logging.h
	$(cc) -c crf1d_tag.cpp $(cflags)

train_lbfgs.o: train_lbfgs.cpp os.h crfsuite.h crfsuite_internal.h logging.h params.h vecmath.h lbfgs.h
	$(cc) -c train_lbfgs.cpp $(cflags)

logging.o: logging.cpp os.h crfsuite.h logging.h
	$(cc) -c logging.cpp $(cflags)

crfsuite.o: crfsuite.cpp os.h crfsuite.h logging.h
	$(cc) -c crfsuite.cpp $(cflags)

option.o: option.cpp os.h option.h
	$(cc) -c option.cpp $(cflags)

lookup3.o: lookup3.cpp
	$(cc) -c lookup3.cpp $(cflags)

MyLib.o: MyLib.cpp MyLib.h MyVector.h
	$(cc) -c MyLib.cpp $(cflags)

InstanceWriter.o: InstanceWriter.cpp InstanceWriter.h Writer.h Instance.h MyLib.h MyVector.h
	$(cc) -c InstanceWriter.cpp $(cflags)

quark.o: quark.cpp os.h rumavl.h quark.h
	$(cc) -c quark.cpp $(cflags)

lbfgs.o: lbfgs.cpp lbfgs.h arithmetic_ansi.h
	$(cc) -c lbfgs.cpp $(cflags)

train_passive_aggressive.o: train_passive_aggressive.cpp os.h crfsuite.h crfsuite_internal.h logging.h params.h vecmath.h
	$(cc) -c train_passive_aggressive.cpp $(cflags)

train_arow.o: train_arow.cpp os.h crfsuite.h crfsuite_internal.h logging.h params.h vecmath.h
	$(cc) -c train_arow.cpp $(cflags)

crf1d_encode.o: crf1d_encode.cpp os.h crfsuite.h crfsuite_internal.h logging.h crf1d.h params.h vecmath.h
	$(cc) -c crf1d_encode.cpp $(cflags)


clean:
	rm -rf *.o
	rm -rf 
	rm -rf PartialCRF.exe

