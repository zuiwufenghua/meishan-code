#include "JSTWriter.h"

JSTWriter::JSTWriter()
{
}

JSTWriter::~JSTWriter()
{
	if (m_outf.is_open()) m_outf.close();
}
