import sys

def conll(pos):
   index = 1
   for word in pos.split():
      word = word.split('_')
      print '\t'.join([str(index), word[0], '_', word[1], word[1], '_', '0', 'ROOT', '_', '_'])
      index += 1
   print

if __name__ == "__main__":
   file = open(sys.argv[1])
   for line in file:
      conll(line)
