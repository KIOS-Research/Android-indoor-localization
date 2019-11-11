package com.kios.airplace

import java.io._
import java.util

import scala.collection.JavaConversions._

class magneticRadioMap(private val magnetic_folder: File, private val radioMap_filename: String, private val radioMapFolder: String, private val sortedRSSCoordinates: String) {

  private val coordinates = new util.ArrayList[MyVector]()
  private val magneticField = new util.ArrayList[MyVector]()
  private val sortedCoordinates = new util.ArrayList[MyVector]()
  private val radioMap_mean_filename = radioMap_filename.replace(".", "-mean.")

  private val coordinatesMap = new util.ArrayList[MyVector]()
  private val coordinatesMean = new util.ArrayList[MyVector]()
  private val magneticMap = new util.ArrayList[MyVector]()
  private val magneticMean = new util.ArrayList[MyVector]()

  def createRadioMap(): Boolean = {
    if (!magnetic_folder.exists() || !magnetic_folder.isDirectory) {
      return false
    }
    createRadioMapFromPath(magnetic_folder)
    if (!writeRadioMap()) {
      return false
    }
    true
  }

  private def parseLogFileToRadioMap(inFile: File): Unit = {
    val f = inFile
    if (!authenticateMagneticLogFile(f)) {
      return
    }
    var line: String = null
    val fr = new FileReader(f)
    val reader = new BufferedReader(fr)

    while ( {
      line = reader.readLine
      line != null
    }) {
      if (!(line.startsWith("#") || line.trim().isEmpty)) {

        line = line.replace(", ", " ")
        val temp = line.split(" ")

        val xPoint = java.lang.Float.parseFloat(temp(1))
        val yPoint = java.lang.Float.parseFloat(temp(2))
        var heading = java.lang.Float.parseFloat(temp(3))
        val xMagnetic = java.lang.Float.parseFloat(temp(4))
        val yMagnetic = java.lang.Float.parseFloat(temp(5))
        val zMagnetic = java.lang.Float.parseFloat(temp(6))

        heading = (((Math.round(heading + 45) % 360) / 90) % 4) * 90

        coordinates.add(new MyVector(xPoint, yPoint, heading))
        magneticField.add(new MyVector(xMagnetic, yMagnetic, zMagnetic))
      }
    }
  }

  private def authenticateMagneticLogFile(inFile: File): Boolean = {
    var line_num = 0
    var reader: BufferedReader = null
    try {
      var line: String = null
      val fr = new FileReader(inFile)
      reader = new BufferedReader(fr)
      while ( {
        line = reader.readLine
        line != null
      }) {

        line_num += 1
        // Check X, Y or Latitude, Longitude
        if (!(line.startsWith("#") || line.trim().isEmpty)) {
          // Remove commas
          line = line.replace(", ", " ")
          val temp = line.split(" ")
          java.lang.Float.parseFloat(temp(1))
          java.lang.Float.parseFloat(temp(2))
          java.lang.Float.parseFloat(temp(3))
          java.lang.Float.parseFloat(temp(4))
          java.lang.Float.parseFloat(temp(5))
          java.lang.Float.parseFloat(temp(6))
        }
      }
      fr.close()
      reader.close()
    } catch {
      case nfe: NumberFormatException =>
        System.err.println("Error while authenticating Magnetic log file " + inFile.getAbsolutePath + ": Line " + line_num + " " + nfe.getMessage)
        return false
      case e: Exception =>
        System.err.println("Error while authenticating Magnetic log file " + inFile.getAbsolutePath + ": " + e.getMessage)
        return false
    }
    true
  }

  private def createRadioMapFromPath(inFile: File) {
    if (inFile.exists()) {
      if (inFile.canExecute && inFile.isDirectory) {
        val list = inFile.list()
        if (list != null) {
          for (i <- 0 until list.length) {
            createRadioMapFromPath(new File(inFile, list(i)))
          }
        }
      } else if (inFile.canRead && inFile.isFile) {
        parseLogFileToRadioMap(inFile)
      }
    }
  }

  private def writeRadioMap(): Boolean = {
    for (i <- sortedRSSCoordinates.split("\n")) {
      val temp = i.split(", ")
      val x = java.lang.Float.parseFloat(temp(0))
      val y = java.lang.Float.parseFloat(temp(1))
      val z = java.lang.Float.parseFloat(temp(2))
      sortedCoordinates.add(new MyVector(x, y, z))
    }

    println("Writing Magnetic radio map to files")
    var fos: FileOutputStream = null
    var fos_mean: FileOutputStream = null
    val radioMap_file = new File(radioMap_filename)
    val radioMap_mean_file = new File(radioMap_mean_filename)

    try {
      new File(radioMapFolder).mkdirs()
      fos = new FileOutputStream(radioMapFolder + "\\" + radioMap_file, false)
      fos_mean = new FileOutputStream(radioMapFolder + "\\" + radioMap_mean_file, false)

      fos.write("X, Y, Orientation, Magnetic Field X, Magnetic Field Y, Magnetic Field Z\n".getBytes())
      fos_mean.write("X, Y, Orientation, Magnetic Field X, Magnetic Field Y, Magnetic Field Z\n".getBytes())

      bubbleShort()

      var count = 0
      for (_ <- coordinates) {
        coordinatesMap.add(new MyVector(coordinates.get(count).x, coordinates.get(count).y, coordinates.get(count).z))
        magneticMap.add(new MyVector(magneticField.get(count).x, magneticField.get(count).y, magneticField.get(count).z))

        count += 1
      }

      count = 0
      //Averaging
      while (count < coordinates.size()) {
        val range = coordinates.count(_.getMyVector.equals(coordinates.get(count).getMyVector))

        if (range == 1) {
          coordinatesMean.add(new MyVector(coordinates.get(count).x, coordinates.get(count).y, coordinates.get(count).z))
          magneticMean.add(new MyVector(magneticField.get(count).x, magneticField.get(count).y, magneticField.get(count).z))
        } else {

          var avgX, avgY, avgZ = 0.0f
          for (i <- 0 until range) {
            avgX += magneticField.get(count + i).x
            avgY += magneticField.get(count + i).y
            avgZ += magneticField.get(count + i).z
          }
          coordinatesMean.add(new MyVector(coordinates.get(count).x, coordinates.get(count).y, coordinates.get(count).z))
          magneticMean.add(new MyVector(avgX / range, avgY / range, avgZ / range))
        }

        count += range
      }

      //Write radioMap
      for (a <- sortedCoordinates) {
        for (b <- coordinatesMap) {
          if (a.getMyVector == b.getMyVector) {
            val index = coordinatesMap.indexOf(b)
            fos.write((coordinatesMap.get(index).getMyVectorInt + ", " + magneticMap.get(index).getMyVector + "\n").getBytes())
          }
        }
      }

      //Write radioMap-mean
      for (a <- sortedCoordinates) {
        for (b <- coordinatesMean) {
          if (a.getMyVector == b.getMyVector) {
            val index = coordinatesMean.indexOf(b)
            fos_mean.write((coordinatesMean.get(index).getMyVectorInt + ", " + magneticMean.get(index).getMyVector + "\n").getBytes())
          }
        }
      }

      println("Finished writing Magnetic radio map to files!")
      true
    } catch {
      case e: FileNotFoundException =>
        System.err.println("Error while writing Magnetic radio map: " + e.getMessage)
        radioMap_file.delete()
        radioMap_mean_file.delete()
        false
    }
  }

  private def bubbleShort() {
    for (i <- 0 until coordinates.size()) {
      for (j <- 0 until coordinates.size()) {
        val a = coordinates.get(i)
        val b = coordinates.get(j)
        val c = magneticField.get(i)
        val d = magneticField.get(j)

        if (a.x < b.x) {
          coordinates.set(i, b)
          coordinates.set(j, a)
          magneticField.set(i, d)
          magneticField.set(j, c)
        } else if (a.x == b.x) {
          if (a.z < b.z) {
            coordinates.set(i, b)
            coordinates.set(j, a)
            magneticField.set(i, d)
            magneticField.set(j, c)
          }
        }
      }
    }
  }
}