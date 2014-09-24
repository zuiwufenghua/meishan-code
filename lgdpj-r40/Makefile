# -*-makefile-*-

INCLUDES = $(patsubst %,-I%,$(filter-out %svn,$(shell find src/ -type d))) -Isrc
LIBS = -lm
CCFLAGS = -Wall -pedantic -Wno-deprecated -lpthread -D _LINUX -D SHOW_TIME 
CC = g++ -O3 $(CCFLAGS) 
CCDEP = $(CC) $(INCLUDES) -MM
CCOBJ = $(CC) $(INCLUDES) -c
CCLINK = $(CC) $(LIBS)
CCGDB = g++ -O3 -g -pg  $(CCFLAGS)
CCGDBOBJ = $(CCGDB) $(INCLUDES) -ggdb -c
CCGDBLINK = $(CCGDB) $(LINK) -ggdb

#VERSION = $(shell svn update | egrep '[0-9]+' -o)
SRCS = $(shell find src -name '[a-zA-Z0-9]*'.cpp)
OBJS = $(patsubst %.cpp,%.o,$(SRCS))
DEPS = $(patsubst %.cpp,%.dep,$(SRCS))
GDBOBJS = $(patsubst %.cpp,%-gdb.o,$(SRCS))

LIBDIR = ./lib-$(ARCH)
CLAPACK_LIBS = $(LIBDIR)/lapack_$(ARCH).a \
               $(LIBDIR)/libcblaswr.a $(LIBDIR)/lib*cblas.a \
	       $(LIBDIR)/libatlas.a $(PTHREAD) -lg2c -lm

GPARSER = lgdpj-r
GPARSER-GDB = lgdpj-r-gdb 

EXEC = $(GPARSER) $(GPARSER-GDB)

all : $(GPARSER)

gdb : $(GPARSER-GDB)

$(GPARSER) : obj
	$(CCLINK) $(OBJS) -o $@

$(GPARSER-GDB) : obj-gdb
	$(CCGDBLINK) $(GDBOBJS) -o $@

dep : $(DEPS)

obj : dep
	@$(MAKE) NESTED=true -s obj-internal

obj-gdb : dep
	@$(MAKE) NESTED=true -s obj-gdb-internal

%.dep : %.cpp
	@echo DEP $<
	-@rm -f $@
	@echo -n $(<D)/ > $@
	@$(CCDEP) $< >> $@
	@echo '	@echo CC $$@' >> $@
	@echo '	@$$(CCOBJ) $$< -o $$@' >> $@
	@echo "" >> $@
	@$(CCDEP) -MT $*-gdb.o $< >> $@
	@echo '	@echo CC-gdb $$@' >> $@
	@echo '	@$$(CCGDBOBJ) $$< -o $$@' >> $@

clean :
	-find src -name \*.gch -exec echo rm {} \; -exec rm {} \;
	-find src -name \*.dep -exec echo rm {} \; -exec rm {} \;
	-find src -name \*.o -exec echo rm {} \; -exec rm {} \;
	-find src -name \*~ -exec echo rm {} \; -exec rm {} \;
	-rm -f $(EXEC)


# these rules are only active from a nested invocation of make (see
# above)
ifdef NESTED
include $(DEPS)

obj-internal : $(OBJS)

obj-gdb-internal : $(GDBOBJS)
endif


.PHONY : clean all gdb dep obj obj-gdb obj-internal obj-gdb-internal
