package com.kios.airplace

class MyVector(val x: Float, val y: Float, val z: Float) {
  def getMyVector: String = x + ", " + y + ", " + z

  def getMyVectorInt: String = x + ", " + y + ", " + z.toInt
}