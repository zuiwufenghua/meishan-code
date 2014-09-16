#include "Pipe.h"
#include "DataInterface.h"
#include <iostream>
#include <string>
#include "Argument_helper.h"

using namespace std;

/*
enum  optionIndex { UNKNOWN, TRAIN, ALGO, OPTIMUM, DECODER };
const option::Descriptor usage[] =
{
		{UNKNOWN, 0, "", "",option::Arg::None, "USAGE: example [options]\n\n"
                                        "Options:" },
		{TRAIN, 0,"l", "learn",option::Arg::None, "  -l, --learn  \tTrain or Test." },
		{ALGO, 0,"a","algo",option::Arg::Optional, " -a, --algo,  \tCRF1d default." },
		{OPTIMUM, 0, "o", "optimum",option::Arg::Optional, " -a, --algo,  \tlbfgs default." },
		{DECODER, 0, "n", "nbest",option::Arg::Optional, " -n, --nbest,  \ttest k best (k=0 for marginal probablity)." },
		{0,0,0,0,0,0}
};
*/
int main(int argc, char* argv[])
{
  std::string input_filename, output_filename, model_filename;
	string learn_algorithm = "crf1d";
	string optimum_algorithm = "lbfgs";
	int iv, oiv, niv;
	bool bTrain  = false;
    bool bProb = false;
	int nbest = 1;
	string temp = optimum_algorithm.substr(2);
	double CUTOFF_PROB = 0.0001;

	dsr::Argument_helper ah;
  ah.new_string("input_filename.type", "train instances or test instances",
		input_filename);
  ah.new_string("output_filename.type", "output file for test, hold-out file for train",
		output_filename);
	ah.new_string("model_filename.type", "The name of the model file",
		model_filename);
	ah.new_flag('l', "learn", "train or test", bTrain);
	//ah.new_named_string('a', "algo", "CRF learning algorithm", "The name of learning method", learn_algorithm);
	//ah.new_named_string('o', "opti", "CRF optimum algorithm", "The name of optimum method", optimum_algorithm);
  ah.new_named_int('n', "nbest", "named_int", "test k best (k=0 for marginal probablity).", nbest);
	ah.new_named_double('m', "marginal", "named_double", "nbest == 0, the marginal cutoff.", CUTOFF_PROB);
  ah.set_description("CRFSuite redefined");
  ah.set_author("Meishan Zhang, mszhang@ir.hit.edu.cn");
  ah.new_flag('p', "prob", "whether output probs, only valid when nbest bigger than zero and in test process", bProb);

  ah.process(argc, argv);

  Pipe pipe;
  DataInterface crftagger;


  //test(argv);
  //ah.write_values(std::cout);



}
