# optional commands to create class file and jar file
$ libs=`find libs/ -name "*jar" | sed ':a;N;$!ba;s/\n/:/g'`
$ javac -cp $libs:. com/almworks/sqlite4java_checker/Activator.java 
$ jar -cvfm check-sqlite4java.jar manifest.mf com/almworks/sqlite4java_checker/Activator.class

# command to check com.almworks.sqlite4java osgi-bundle
$ cd path/to/felix # for example
$ java -jar bin/felix.jar pathToBundles
$$ install file:/path/to/com.almworks.sqlite4java-1.0.version.jar
Bundle ID: 5 # for example
$$ install file:/path/to/check-sqlite4java.jar
Bundle ID: 6 # for example
$$ start 6 #
# should be displayed something like this:
Starting to listen for service events.
im here!
Oct 15, 2014 5:30:43 PM com.almworks.sqlite4java.Internal log
INFO: [sqlite] DB[1]: instantiated [/tmp/prefix2487987471091151557extension]
Oct 15, 2014 5:30:43 PM com.almworks.sqlite4java.Internal log
INFO: [sqlite] Internal: loaded sqlite4java-linux-i386 from system path
Oct 15, 2014 5:30:43 PM com.almworks.sqlite4java.Internal log
INFO: [sqlite] Internal: loaded sqlite 3.8.6, wrapper 1.3
Oct 15, 2014 5:30:43 PM com.almworks.sqlite4java.Internal log
INFO: [sqlite] DB[1]: opened
vaka vaka vaka


