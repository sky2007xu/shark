/*
 * Copyright (C) 2012 The Regents of The University California.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package shark.memstore2.column

import javaewah.EWAHCompressedBitmap
import javaewah.EWAHCompressedBitmapSerializer
import javaewah.IntIterator

import shark.memstore2.buffer.ByteBufferReader


/**
 * A wrapper around non-null ColumnIterator so it can handle null values.
 */
class EWAHNullableColumnIterator[T <: ColumnIterator](baseIter: T) extends ColumnIterator {

  var _nullBitmap: EWAHCompressedBitmap = null
  var _pos = -1
  var _nextNullPosition = -1
  var _nullsIter: IntIterator = null

  override def initialize(bytes: ByteBufferReader) {
    _nullBitmap = EWAHCompressedBitmapSerializer.readFromBuffer(bytes)
    _nullsIter = _nullBitmap.intIterator
    baseIter.initialize(bytes)
    _pos = -1
  }

  override def next() {
    _pos += 1
    baseIter.next()
  }

  override def current: Object = {
    while (_nextNullPosition < _pos && _nullsIter.hasNext) _nextNullPosition = _nullsIter.next
    if (_nextNullPosition == _pos) {
      _nextNullPosition = if (_nullsIter.hasNext) _nullsIter.next else Int.MaxValue
      null
    } else {
      baseIter.current
    }
  }
}
