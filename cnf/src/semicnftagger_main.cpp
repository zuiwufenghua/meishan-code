# include <semicnftagger.h>
# include <cstdio>

using namespace SemiCnf;
int main(int argc, char **argv)
{
   SemiCnftagger tagger(*(argv+1), 10000000);
   tagger.read(*(argv+2));
   tagger.tagging(*(argv+3));
   return 0;
}
