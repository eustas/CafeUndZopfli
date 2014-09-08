Shanga is a compression library programmed in Java.

Shanga is based on Zopfli Compression Algorithm, see:
https://code.google.com/p/zopfli/

To compress data create instance of Shanga class and invoke Shanga.compress.
Shanga.compress is synchronized. Shanga extensively preallocate memory to avoid
in-flight GC. The amount of memory is about 48 x master block size.

Shanga.compress supports deflate, gzip and zlib output format with a parameter.

This library can only compress, not decompress. Existing zlib or deflate
libraries can decompress the data.

JAR contains main class, and could be used as a standalone application.

To build the binary, use "ant".

Shanga was created by Eugene Klyuchnikov, based on Zopfli Compression Algorithm
by Lode Vandevenne and Jyrki Alakuijala.
