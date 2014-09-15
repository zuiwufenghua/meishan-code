# ifndef CNF_TAGGER
# define CNF_TAGGER

# include <cnftagger.h>

template<class T>
class Tagger
{
   public:
      /** Construct Tagger
       * @param tmpl template
       * @param poolsize poolsize for allocator
       */
      Tagger(const char *tmpl);
      ~Tagger();
      /** Read Modelfile
       * @param model modelfile
       */
      void read(const char *model);
      /** Tagging to corpus
       * @param corpus test-corpus
       */
      void tagging(const char *corpus);
      /** Predict Tags and set to labels
       * @param s target sequence
       * @param cache cache
       * @param labels vector to store predicted labels
       */
      void viterbi(Sequence *s,
            std::vector<int>& labels);
      /** Output labeled-sequence
       * @param s sequence
       * @param labels vector stored labels
       */
      void output(Sequence *s, std::vector<int>& labels);
      /** Set colsize of sequence
       * @param colsize colsize of sequence
       */
      void setsqcol(unsigned int sqcolsize);
      /** Set arraysize of sequence
       * @param arraysize number of tokens in sequence
       */
      /** Clear model parameter */
      void clear();
   private:
      Tagger();
      Tagger(const Tagger&);
      Tagger& operator=(const Tagger&);

      T *impl;
};

   template<class T>
Tagger<T>::Tagger(const char *tmpl)
{
   this->impl = new T(tmpl);
}

   template<class T>
Tagger<T>::~Tagger()
{
   delete this->impl;
}

   template<class T>
void Tagger<T>::read(const char *model)
{
   this->impl->read(model);
}

   template<class T>
void Tagger<T>::tagging(const char *corpus)
{
   this->impl->tagging(corpus);
}

   template<class T>
void Tagger<T>::viterbi(Sequence *s,
      std::vector<int>& labels)
{
   this->impl->viterbi(s,labels);
}

   template<class T>
void Tagger<T>::output(Sequence *s,
      std::vector<int>& labels)
{
   this->impl->output(s,labels);
}

   template <class T>
void Tagger<T>::setsqcol(unsigned int sqcolsize)
{
   this->impl->setsqcol(sqcolsize);
}

   template<>
void Tagger<Cnf::Cnftagger>::clear()
{
   this->impl->clear();
}
# endif /* CNF_TAGGER */
