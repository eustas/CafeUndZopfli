/* Copyright 2018 Google Inc. All Rights Reserved.

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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Wrapper/buffer that accumulates bits before sending them to destination.
 */
public class BitWriter {

  private static final int PAGE_SIZE = 4096;

  final byte[] data = new byte[PAGE_SIZE + 1];
  int offset;
  private int bitOffset;
  private int accumulator;

  OutputStream output;

  BitWriter(OutputStream output) {
    this.output = output;
  }

  /**
   * Appends 0-7 bits to output, to align output to byte boundary.
   */
  void jumpToByteBoundary() {
    int paddingBits = 8 - (bitOffset & 7);
    if (paddingBits != 8) {
      addBits(0, paddingBits);
    }
  }

  /**
   * Appends bits to output.
   *
   * Requirement: value & ~((1 << length) - 1) == 0
   * Requirement: 1 <= length <= 16
   *
   * Invariant: bitOffset < 8
   * Invariant: offset < PAGE_SIZE
   *
   * Up to 2 bytes are added to data, but offset invariant guarantees
   * that there are enough slots in data.
   *
   * @param value bits to append
   * @param length number of bits to append
   */
  void addBits(int value, int length) {
    accumulator = accumulator | (value << bitOffset);
    bitOffset += length;
    while (bitOffset >= 8) {
      data[offset++] = (byte) accumulator;
      bitOffset -= 8;
      accumulator >>= 8;
    }
    if (offset >= PAGE_SIZE) {
      flush();
    }
  }

  /**
   * Writes whole number of accumulated bytes to output.
   *
   * Note: up to 7 bits might remain in accumulator.
   */
  void flush() {
    int slice = offset > PAGE_SIZE ? PAGE_SIZE : offset;
    try {
      output.write(data, 0, slice);
    } catch (IOException ex) {
      throw new ZopfliRuntimeException("Failed to push output", ex);
    }
    offset -= slice;
    data[0] = data[PAGE_SIZE];
  }
}
