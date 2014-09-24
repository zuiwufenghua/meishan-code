/**
* EG for Structured Algorithms
* @file egstra.cc
* @author Xavier Carreras, Terry Koo
*/

#include <iostream>
#include <string>

#include <stdlib.h>

#include "Constants.h"
#include "Options.h"

#include "Parser.h"

using namespace std;
using namespace egstra;
using namespace dparser;

void test_nr() {
	NRMat<int> m(11, 13);
	for (int i = 0; i < m.nrows()*m.ncols(); i++) {
		m.c_buf()[i] = i;
	}
	for (int i = 0; i < m.nrows(); i++) 
		for (int j = 0; j < m.ncols(); j++)
			cerr << m[i][j] << "\t";
	cerr << endl; 
	cerr << endl; 

	NRMat3d<int> m3(11, 13, 3);
	for (int i = 0; i < m3.dim1()*m3.dim2()*m3.dim3(); i++) {
		m3.c_buf()[i] = i;
	}
	for (int i = 0; i < m3.dim1(); i++) 
		for (int j = 0; j < m3.dim2(); j++)
			for (int k = 0; k < m3.dim3(); k++)
			cerr << m3[i][j][k] << "\t";
	cerr << endl; 
	cerr << endl; 

	NRMat4d<int> m4(11, 7, 3, 4);
	for (int i = 0; i < m4.dim1()*m4.dim2()*m4.dim3()*m4.dim4(); i++) {
		m4.c_buf()[i] = i;
	}
	for (int i = 0; i < m4.dim1(); i++) 
		for (int j = 0; j < m4.dim2(); j++)
			for (int k = 0; k < m4.dim3(); k++)
				for (int l = 0; l < m4.dim4(); l++)
					cerr << m4[i][j][k][l] << "\t";
	cerr << endl; 
	cerr << endl; 
}


void usage(const char* mesg/* = ""*/) {
	cerr << "lgdpj " << VERSION << " --- {lzh}@ir.hit.edu.cn" << endl;
	cerr << endl;
	cerr << mesg << endl << endl;

	cerr << "lgdpj options:" << endl;
	cerr << " --dir=<pathname>     : output directory for various files" << endl;
	cerr << " --T=<int>            : number of training rounds" << endl;
	cerr << " --trdata=<pathname>  : training data file" << endl;
	cerr << " --valdata=<pathname> : held-out data file, to be classified" << endl;
	cerr << "                        on each training iteration" << endl;
}

int main(int argc, char **argv)
{
	//vector<const char *> vec;
	//vec.push_back("abcxyz");
	//vec.push_back("Abcxyz");
	//vec.push_back("abcxyZ");
	//vec.push_back("abCxyz");
	//vec.push_back("abC-xyz");
	//vec.push_back("abc0#xyz");
	//vec.push_back("abc9$xyZ");	
	//for (int i = 0; i < vec.size(); ++i) {
	//	string str = vec[i];
	//	cerr << str << " : " << contain_uppercase_character(str) 
	//		<< " " << contain_number(str)
	//		<< " " << contain_hyphen(str)
	//		<< endl;
	//}
	//return 0;

	//string form_lc = "hello world!";
	//for (int i = 1; i <= 10 && i <= form_lc.size(); ++i) {
	//	cerr << "prefix:" << form_lc.substr(0, i) << endl;
	//	cerr << "suffix:" << form_lc.substr(form_lc.size()-i, i) << endl;
	//}
	//return 0;

	srand(0);
/*	// Initialize Huang Yun@Singapore 's Hasher. Use a lot of rand()
	// Do this to make sure that the experiments can be easily reproduced.
	StringMap<int> m; m.set("hello world!", 1);
*/
	//for (int i = 0; i < 100; ++i) {
	//	cerr << rand() << endl;
	//}
	//return 0;
	//test_nr(); return 0;

	if(argc == 1) { usage(); exit(1); }

	const char* const arg = argv[1];
	if(arg[0] != '-') {
		options::read(string(argv[1]));
	} else {
		 usage("argv[1] must be the config file"); exit(1);		 
	}

	/* now parse the command-line arguments (potentially overwriting
	anything set in a config file) */
	options::read(argc, argv);
	options::display(cerr, "");

	Parser dparser;
	dparser.run();

	return 0;
}

