/* Copyright 2018 Google Inc. All Rights Reserved.

   Distributed under MIT license.
   See file LICENSE for detail or copy at https://opensource.org/licenses/MIT
*/

package ru.eustas.zopfli;

/**
 * Unchecked exception used internally.
 */
class ZopfliRuntimeException extends RuntimeException {

  ZopfliRuntimeException(String message) {
    super(message);
  }

  ZopfliRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }
}
