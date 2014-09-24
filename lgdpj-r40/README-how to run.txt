lgdpj r40
Zhenghua Li
http://ir.hit.edu.cn/~lzh
2013.6.1


Package overview:
.
	Makefile
	win-proj		-- Can also use this package on Visual Studio (2008 or newer) on Windows
	win-bin
	win-build
	src				-- source files
	data			-- some sample data
	scripts			-- some useful scripts in perl 
	run-examples	-- some examples for runing (configuration files and .sh)

-------------------

Compilation:

The command
$ make
should suffice, and the exectable file is named as ``lgdpj-r'' where the version number (r40) in the svn repository is missed.


-------------------

Command line:

$ ./lgdpj-r config.txt 

``config.txt'' is the configuration file containing all the options for the parser.

Extra options can also be provided from the command line in the form "--name=value", which will overwrite the options from the configuration file. E.g.

$ ./lgdpj-r config.txt --train=0 --test=1 --param-num-for-eval=5 --thread-num=10


-------------------

Please look at ``run-examples''





