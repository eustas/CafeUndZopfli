/* Copyright 2014 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Author: eustas.ru@gmail.com (Eugene Klyuchnikov)
*/

package ru.eustas.zopfli;

import java.io.OutputStream;

/**
 * Zopfli compression and output framing facade.
 */
public class Zopfli {

  static class Crc {

    /* Collection of utilities / should not be instantiated. */
    Crc() {}

    private static final int[] table = makeTable();

    private static int[] makeTable() {
      int[] result = new int[256];

      for (int n = 0; n < 256; ++n) {
        int c = n;
        for (int k = 0; k < 8; ++k) {
          if ((c & 1) == 1) {
            c = 0xEDB88320 ^ (c >>> 1);
          } else {
            c = c >>> 1;
          }
        }
        result[n] = c;
      }

      return result;
    }

    static int calculate(byte[] input) {
      int c = ~0;
      for (int i = 0, n = input.length; i < n; ++i) {
        c = table[(c ^ input[i]) & 0xFF] ^ (c >>> 8);
      }
      return ~c;
    }
  }

  private final Cookie cookie;

  public synchronized void compress(Options options, byte[] input, OutputStream output) {
    BitWriter bitWriter = new BitWriter(output);
    switch (options.outputType) {
      case GZIP:
        compressGzip(options, input, bitWriter);
        break;

      case ZLIB:
        compressZlib(options, input, bitWriter);
        break;

      case DEFLATE:
        Deflate.compress(cookie, options, input, bitWriter);
        break;
    }
    bitWriter.jumpToByteBoundary();
    bitWriter.flush();
  }

  /**
   * Calculates the adler32 checksum of the data
   */
  private static int adler32(byte[] data) {
    int s1 = 1;
    int s2 = 1 >> 16;
    int i = 0;
    while (i < data.length) {
      int tick = Math.min(data.length, i + 1024);
      while (i < tick) {
        s1 += data[i++];
        s2 += s1;
      }
      s1 %= 65521;
      s2 %= 65521;
    }

    return (s2 << 16) | s1;
  }

  private void compressZlib(Options options, byte[] input, BitWriter output) {
    output.addBits(0x1E78, 16);

    Deflate.compress(cookie, options, input, output);
    output.jumpToByteBoundary();

    int checksum = adler32(input);
    output.addBits((checksum >> 24) & 0xFF, 8);
    output.addBits((checksum >> 16) & 0xFF, 8);
    output.addBits((checksum >> 8) & 0xFF, 8);
    output.addBits(checksum & 0xFF, 8);
  }

  private void compressGzip(Options options, byte[] input, BitWriter output) {
    output.addBits(0x8B1F, 16);
    output.addBits(0x0008, 16);
    output.addBits(0x0000, 16);
    output.addBits(0x0000, 16);
    output.addBits(0x0302, 16);

    Deflate.compress(cookie, options, input, output);
    output.jumpToByteBoundary();

    int crc = Crc.calculate(input);
    output.addBits(crc & 0xFFFF, 16);
    output.addBits((crc >> 16) & 0xFFFF, 16);

    int size = input.length;
    output.addBits(size & 0xFFFF, 16);
    output.addBits((size >> 16) & 0xFFFF, 16);
  }

  public Zopfli(int masterBlockSize) {
    cookie = new Cookie(masterBlockSize);
  }
}
