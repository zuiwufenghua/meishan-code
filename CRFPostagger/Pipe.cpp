#include "Pipe.h"
#include <iterator>

using namespace std;


Pipe::Pipe() 
{
	m_jstReader = 0;
	m_jstReader = new InstanceReader();
	m_jstWriter = new InstanceWriter();
}

Pipe::~Pipe(void)
{
	if (m_jstReader) delete m_jstReader;
	if (m_jstWriter) delete m_jstWriter;
}

int Pipe::initInputFile(const char *filename) {
	if (0 != m_jstReader->startReading(filename)) return -1;
	return 0;
}

void Pipe::uninitInputFile() {
	if (m_jstWriter) m_jstReader->finishReading();
}

int Pipe::initOutputFile(const char *filename) {
	if (0 != m_jstWriter->startWriting(filename)) return -1;
	return 0;
}

void Pipe::uninitOutputFile() {
	if (m_jstWriter) m_jstWriter->finishWriting();
}

int Pipe::outputInstance(const Instance *pInstance) {
	if (0 != m_jstWriter->write(pInstance)) return -1;
	return 0;
}

Instance *Pipe::nextInstance()
{
	Instance *pInstance = m_jstReader->getNext();
	if (!pInstance || pInstance->labels.empty()) return 0;

	return pInstance;
}


void Pipe::readTrainInstances(const string& m_strTrainFile)
{
  initInputFile(m_strTrainFile.c_str());

  Instance *pInstance = nextInstance();
	int numInstance = 0;

	while (pInstance) {
		
    Instance trainInstance;	
		trainInstance.copyValuesFrom(*pInstance);
    m_vecInstances.push_back(trainInstance);
    numInstance++;
  
    pInstance = nextInstance();

  }
  
  uninitInputFile();

	cout << endl;
	cout << "instance num: " << numInstance << endl;
}

void Pipe::readHoldInstances(const string& m_strEvalFile)
{
  initInputFile(m_strEvalFile.c_str());

  Instance *pInstance = nextInstance();
	int numInstance = 0;

	while (pInstance) {
		
    Instance trainInstance;	
		trainInstance.copyValuesFrom(*pInstance);
    m_vecDevInstances.push_back(trainInstance);
		numInstance++;
  
    pInstance = nextInstance();

  }
  
  uninitInputFile();

	cout << endl;
	cout << "instance num: " << numInstance << endl;
}

