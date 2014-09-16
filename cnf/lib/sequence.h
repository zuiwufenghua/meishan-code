/**
 * written by kei.uchiumi
 * sequence class
 * Copyright (C) 2010- Kei Uchiumi
 */

# ifndef __SEQUENCE_H__
# define __SEQUENCE_H__

# include <cstdio>
# include <string>
# include <nrmat.h>

# define MAX_SENT_SIZE 1024
# define MAX_FEAT_NUM_EACHPOS 1024

class Sequence
{
   public:
      Sequence();
      ~Sequence();
      /**
       *
       */
      int dump();
      /**
       * @param[in] str
       */
      int push(const char *str);
      /**
       *
       */
      int clear();
      /**
       * @param[out] rowsize
       */
      unsigned int getRowSize();
      /**
       * @param[in] row
       * @param[in] col
       * @param[out] char*
       */
      std::string getToken(int row, int col);
      /**
       * @param[in] delimit
       */
      int setDelimit(const char *delimit);
      /**
       *
       */
      int init();

      /**
       * @param[in] colsize
       */
      int setColSize(unsigned int colsize);
      /**
       * @param[in] char
       */

   private:
      Sequence(const Sequence&);
      Sequence& operator=(const Sequence&);

      NRMat<std::string> tokens;
      std::string delim;

      unsigned int colsize;
      unsigned int arraysize;

      unsigned int ngram;
};

# endif /* __SEQUENCE_H__ */
