#! /usr/bin/perl


if (@ARGV != 1) {
	print "Usage: Makefile.pl  EXE-File \n";
	exit(0);
}

open OUT, ">Makefile" or die;

#=========================
$cc =<<CMD;
cc=g++
cflags = -O2

CMD
#=========================
$clean=<<"CMD";

clean:
	rm -rf *.o
	rm -rf $AGRV[0]
	rm -rf $ARGV[0].exe

CMD
#=========================

$mm = `g++ -MM *.cpp`;
@mm = split/\n/, $mm;

$line = shift @mm;
for (@mm) {
	if ($_ =~ m/^\s/) {
		$line .= $_;
	} else {
		push(@oo, $line);
		$line = $_;
	}
}
push(@oo, $line);

for (@oo) {
	s/[:|\\]//g;
}

%oo = map { 
	@tmp = split; 
	$k = shift @tmp;
	$k => [@tmp];
	} @oo;

$obj = "obj = ". join(" ", keys %oo) . " \n\n";

$body = "$ARGV[0]: ". '$(obj)'. "\n\t". '$(cc) -o '.
		"$ARGV[0] ". '$(obj) $(cflags)'."\n\n"; 

for (keys %oo) {
	$_ =~ m/(.*?)o$/;
	die "$_\n !" unless defined $1;
	$cpp = $1 . "cpp";
	$body .= $_ . ": " . join(" ", @{$oo{$_}});
	$body .= "\n\t". '$(cc) -c '. $cpp . ' $(cflags)';
	$body .= "\n\n";
}

print OUT $cc, "\n"x2, $obj, $body, $clean;
print "Makefile generated successfully!\n";








__END__

cc=g++

cflags = -O2 -Wall 

obj = Main.o KMeans.o Cluster.o Dist.o MyLib.o

KMean: $(obj)
	$(cc) -o kmean $(obj) $(cflags)

Cluster.o: Cluster.cpp Cluster.h Dist.h MyLib.h
	$(cc) -c Cluster.cpp $(cflags)

KMeans.o: KMeans.cpp KMeans.h Cluster.h Dist.h MyLib.h
	$(cc) -c KMeans.cpp $(cflags)

MyLib.o: MyLib.cpp MyLib.h
	$(cc) -c MyLib.cpp $(cflags)

Dist.o : Dist.cpp Dist.h
	$(cc) -c Dist.cpp $(cflags)

clean:
	rm -rf *.o
	rm -rf kmean.exe
	rm -rf kmean
