Shanga is a compression library programmed in Java.

Shanga is based on the Zopfli Compression Algorithm, see:
https://code.google.com/p/zopfli/

To compress data create a Shanga class instance and invoke Shanga.compress.
Shanga.compress is synchronized. Shanga extensively preallocates memory to avoid
in-flight GC. The amount of memory is about 48 times the master block size.

Shanga.compress supports deflate, gzip and zlib output format with a parameter.

This library can only compress, not decompress data. Use an existing zlib or deflate
librariy to decompress the data.

The JAR contains the main class, and can be used as a standalone application.

To build the binary, use "ant".

Shanga was created by Eugene Klyuchnikov, based on the Zopfli Compression Algorithm
by Lode Vandevenne and Jyrki Alakuijala.
