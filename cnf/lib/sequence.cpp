/**
 * Copyright (C) Kei Uchiumi
 */
# include <myutil.h>
# include <sequence.h>
# include <cstdio>
# include <cstring>


Sequence::Sequence ()
{
   this->colsize = 1;
   delim = " ";
   this->ngram = 1;
   this->tokens.resize(MAX_SENT_SIZE, MAX_FEAT_NUM_EACHPOS);
}

Sequence::~Sequence ()
{
	this->tokens.dealloc();
}

int Sequence::dump ()
{
   for (unsigned int i = 0; i < this->arraysize; i++)
   {
      for (unsigned int j = 0; j < this->colsize; j++)
      {
         fprintf (stdout, "%s\t",this->tokens[i][j].c_str());
      }
      fprintf (stdout, "\n");
   }
   fprintf (stdout, "\n");
   return 0;
}

int Sequence::init ()
{
	this->arraysize = 0;
   return 0;
}


int Sequence::setColSize (unsigned int size)
{
   this->colsize = size;
   return 0;
}

int Sequence::push (const char *str)
{

	std::string orgin = str;
	std::vector<std::string> splits;
	MyUtil::split_bystr(str, splits, delim);
	int col = splits.size();

   if (col != this->colsize)
   {
      fprintf (stderr, "pushed tokens num != colsize[%u]\n",this->colsize);
      //exit (1);
      return -1;
   }
   else
   {
	   for(int idx = 0; idx < this->colsize; idx++)
	   {
		   this->tokens[this->arraysize][idx] = splits[idx];
	   }
	   this->arraysize++;
   }
   return col;
}

// the item number of a sequence
unsigned int Sequence::getRowSize ()
{
   return this->arraysize;
}


int Sequence::clear ()
{
	this->arraysize = 0;
   return 0;
}

std::string Sequence::getToken (int row, int col)
{
   if (col >= (int)this->colsize || col < 0)
   {
      return "";
   }
   // BOS
   if (row < 0)
   {
      const char *head = "_B";
      char tail[64] = "\0";
      MyUtil::itoa(row,tail);
      return  std::string("_B") + std::string(tail);
   }
   // EOS
   else if (row >= (int)this->arraysize)
   {
      const char *head = "_E+";
      char tail[64] = "\0";
      MyUtil::itoa(row+(int)(1-this->arraysize),tail);

      return std::string("_E+") + std::string(tail);
   }
   // default
   else //if (col < (int)this->colsize)
   {
      return this->tokens[row][col];
   }
   return "";
}
