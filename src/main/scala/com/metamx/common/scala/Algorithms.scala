package com.metamx.common.scala

object Algorithms {

  // From com.metamx.druid.utils.BufferUtils.binarySearch translated to Scala
  def binarySearch[A](seq: IndexedSeq[A], minIndex: Int, maxIndex: Int, value: A)(implicit ordering: Ordering[A]): Int = {
    var _minIndex = minIndex
    var _maxIndex = maxIndex
    while (_minIndex < _maxIndex) {
      val currIndex = (_minIndex + _maxIndex - 1) >>> 1
      val currValue = seq(currIndex)
      val comparison = ordering.compare(currValue, value)
      if (comparison == 0) {
        return currIndex
      }
      if (comparison < 0) {
        _minIndex = currIndex + 1
      } else {
        _maxIndex = currIndex
      }
    }
    return -(_minIndex + 1)
  }
}