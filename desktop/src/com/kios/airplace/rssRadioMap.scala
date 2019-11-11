package com.kios.airplace

import java.io._
import java.text.{DecimalFormat, DecimalFormatSymbols}
import java.util

import scala.collection.JavaConversions._

class rssRadioMap(private val rss_folder: File, private val radioMap_filename: String, private val radioMapFolder: String, private val defaultNaNValue: Int) {

  private val RadioMap = new util.HashMap[String, util.HashMap[String, util.ArrayList[Integer]]]()

  private val NewRadioMap: util.HashMap[String, util.HashMap[Integer, util.ArrayList[Any]]] = new util.HashMap[String, util.HashMap[Integer, util.ArrayList[Any]]]()
  private val radioMap_mean_filename = radioMap_filename.replace(".", "-mean.")
  private var orientationLists: util.HashMap[Integer, util.ArrayList[Any]] = _

  def createRadioMap(): Boolean = {
    if (!rss_folder.exists() || !rss_folder.isDirectory) {
      return false
    }
    RadioMap.clear()
    createRadioMapFromPath(rss_folder)
    if (!writeRadioMap()) {
      return false
    }
    true
  }

  def parseLogFileToRadioMap(inFile: File) {
    var MACAddressMap: util.HashMap[String, util.ArrayList[Integer]] = null
    var RSS_Values: util.ArrayList[Integer] = null
    var orientationList: util.ArrayList[Any] = null
    val f = inFile
    var reader: BufferedReader = null
    if (!authenticateRSSlogFile(f)) {
      return
    }
    var group = 0
    var line_num = 0
    try {
      var line: String = null
      var key = ""
      val degrees = 360
      val num_orientations = 4
      val range = degrees / num_orientations
      val deviation = range / 2
      var RSS_Value = 0
      val fr = new FileReader(f)
      reader = new BufferedReader(fr)
      while ( {
        line = reader.readLine
        line != null
      }) {
        line_num += 1
        if (!(line.startsWith("#") || line.trim().isEmpty)) {

          line = line.replace(", ", " ")
          val temp = line.split(" ")
          RSS_Value = java.lang.Integer.parseInt(temp(5))
          key = temp(1) + ", " + temp(2)
          group = ((Math.round(java.lang.Float.parseFloat(temp(3)) + deviation) % degrees) / range) % num_orientations
          orientationLists = NewRadioMap.get(key)
          if (orientationLists == null) {
            orientationLists = new util.HashMap[Integer, util.ArrayList[Any]](Math.round(num_orientations))
            orientationList = new util.ArrayList[Any](2)
            orientationLists.put(group, orientationList)
            MACAddressMap = new util.HashMap[String, util.ArrayList[Integer]]()
            RSS_Values = new util.ArrayList[Integer]()
            RSS_Values.add(RSS_Value)
            MACAddressMap.put(temp(4).toLowerCase(), RSS_Values)
            orientationList.add(MACAddressMap)
            orientationList.add(0)
            NewRadioMap.put(key, orientationLists)
          } else if (orientationLists.get(group) == null) {
            orientationList = new util.ArrayList[Any](2)
            orientationLists.put(group, orientationList)
            MACAddressMap = new util.HashMap[String, util.ArrayList[Integer]]()
            RSS_Values = new util.ArrayList[Integer]()
            RSS_Values.add(RSS_Value)
            MACAddressMap.put(temp(4).toLowerCase(), RSS_Values)
            orientationList.add(MACAddressMap)
            orientationList.add(0)
            NewRadioMap.put(key, orientationLists)
          } else {
            MACAddressMap = orientationLists.get(group).get(0).asInstanceOf[util.HashMap[String, util.ArrayList[Integer]]]
            RSS_Values = MACAddressMap.get(temp(4).toLowerCase())
            var position = orientationLists.get(group).get(1).asInstanceOf[java.lang.Integer]
            if (RSS_Values == null) {
              RSS_Values = new util.ArrayList[Integer]()
            }
            if (position == RSS_Values.size) {
              position = position + 1
              orientationLists.get(group).set(1, position)
              RSS_Values.add(RSS_Value)
              MACAddressMap.put(temp(4).toLowerCase(), RSS_Values)
            } else {
              for (_ <- RSS_Values.size until position - 1) {
                RSS_Values.add(this.defaultNaNValue)
              }
              RSS_Values.add(RSS_Value)
              MACAddressMap.put(temp(4).toLowerCase(), RSS_Values)
            }
          }
        }
      }
      fr.close()
      reader.close()
    } catch {
      case e: Exception => System.err.println("Error while parsing RSS log file " + f.getAbsolutePath + ": " + e.getMessage)
    }
  }

  def authenticateRSSlogFile(inFile: File): Boolean = {
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
          if (!temp(4).matches("[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}")) {
            throw new Exception("Line " + line_num + " MAC Address is not valid.")
          }
          java.lang.Integer.parseInt(temp(5))
        }
      }
      fr.close()
      reader.close()
    } catch {
      case nfe: NumberFormatException =>
        System.err.println("Error while authenticating RSS log file " + inFile.getAbsolutePath + ": Line " + line_num + " " + nfe.getMessage)
        return false
      case e: Exception =>
        System.err.println("Error while authenticating RSS log file " + inFile.getAbsolutePath + ": " + e.getMessage)
        return false
    }
    true
  }

  def getCoordinates: String = {
    var radioMap = new String
    for ((x_y, value) <- NewRadioMap) {
      for ((heading, _) <- value) {
        radioMap += x_y + ", " + (heading * 90) + "\n"
      }
    }
    radioMap
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
    println("Writing RSS radio map to files")
    val dec = new DecimalFormat("###.#")

    val decimalFormatSymbols = new DecimalFormatSymbols()
    decimalFormatSymbols.setDecimalSeparator('.')
    dec.setDecimalFormatSymbols(decimalFormatSymbols)

    var MACAddressMap: util.HashMap[String, util.ArrayList[Integer]] = null
    val AP = new util.ArrayList[String]()
    var fos: FileOutputStream = null
    var fos_mean: FileOutputStream = null
    val orientations = 4
    val radioMap_file = new File(radioMap_filename)
    val radioMap_mean_file = new File(radioMap_mean_filename)
    if (NewRadioMap.isEmpty) {
      return false
    }
    try {
      new File(radioMapFolder).mkdirs()
      fos = new FileOutputStream(radioMapFolder + "\\" + radioMap_file, false)
      fos_mean = new FileOutputStream(radioMapFolder + "\\" + radioMap_mean_file, false)
    } catch {
      case e: FileNotFoundException =>
        System.err.println("Error while writing RSS radio map: " + e.getMessage)
        radioMap_file.delete()
        radioMap_mean_file.delete()
        return false
    }
    try {
      var count = 0
      var max_values = 0
      var first = 0
      val NaNValue = "# NaN " + this.defaultNaNValue + "\n"
      val header = "# X, Y, Orientation, "
      fos.write(NaNValue.getBytes)
      fos_mean.write(NaNValue.getBytes)
      fos.write(header.getBytes)
      fos_mean.write(header.getBytes)
      val MACKeys = new util.ArrayList[String]()
      var MRSS_Values: Array[Int] = null
      var heading = 0
      var x_y = ""
      var group = 0
      for ((_, value) <- NewRadioMap) {
        for ((_, value) <- value) {
          MACAddressMap = value.get(0).asInstanceOf[util.HashMap[String, util.ArrayList[Integer]]]
          for ((key, _) <- MACAddressMap) {
            val MACAddress = key
            if (!MACKeys.contains(MACAddress.toLowerCase())) {
              if (AP.size == 0 || AP.contains(MACAddress.toLowerCase())) {
                MACKeys.add(MACAddress.toLowerCase())
                if (first == 0) {
                  fos.write(MACAddress.toLowerCase().getBytes)
                  fos_mean.write(MACAddress.toLowerCase().getBytes)
                } else {
                  fos.write((", " + MACAddress.toLowerCase()).getBytes)
                  fos_mean.write((", " + MACAddress.toLowerCase()).getBytes)
                }
                first += 1
              }
            }
          }
        }
      }
      for ((key, value) <- NewRadioMap) {
        val degrees = 360
        group = degrees / orientations
        x_y = key
        for ((key, value) <- value) {
          max_values = 0
          heading = key * group
          MACAddressMap = value.get(0).asInstanceOf[util.HashMap[String, util.ArrayList[Integer]]]
          for ((_, value) <- MACAddressMap) {
            val wifi_rss_values = value
            if (wifi_rss_values.size > max_values) {
              max_values = wifi_rss_values.size
            }
          }
          if (count == 0) {
            fos.write("\n".getBytes)
            fos_mean.write("\n".getBytes)
          }
          MRSS_Values = new Array[Int](MACKeys.size)
          for (v <- 0 until max_values) {
            fos.write((x_y + ", " + heading).getBytes)
            for (i <- 0 until MACKeys.size) {
              var rss_value = 0
              if (MACAddressMap.containsKey(MACKeys.get(i).toLowerCase())) {
                if (v >=
                  MACAddressMap.get(MACKeys.get(i).toLowerCase()).size &&
                  MACAddressMap.get(MACKeys.get(i).toLowerCase()).size <
                    max_values) {
                  MRSS_Values(i) += this.defaultNaNValue
                  rss_value = this.defaultNaNValue
                } else {
                  rss_value = MACAddressMap.get(MACKeys.get(i).toLowerCase()).get(v)
                  MRSS_Values(i) += rss_value
                }
              } else {
                rss_value = this.defaultNaNValue
                MRSS_Values(i) += this.defaultNaNValue
              }
              fos.write((", " + dec.format(rss_value)).getBytes)
            }
            fos.write("\n".getBytes)
          }
          fos_mean.write((x_y + ", " + heading).getBytes)
          for (i <- MRSS_Values.indices) {
            fos_mean.write((", " + dec.format(MRSS_Values(i).toFloat / max_values)).getBytes)
          }
          fos_mean.write("\n".getBytes)
          count += 1
        }
      }
      fos.close()
    } catch {
      case cce: ClassCastException =>
        System.err.println("Error1: " + cce.getMessage)
        return false
      case nfe: NumberFormatException =>
        System.err.println("Error2: " + nfe.getMessage)
        return false
      case fnfe: FileNotFoundException =>
        System.err.println("Error3: " + fnfe.getMessage)
        return false
      case ioe: IOException =>
        System.err.println("Error4: " + ioe.getMessage)
        return false
      case e: Exception =>
        System.err.println("Error5: " + e.getMessage)
        return false
    }
    println("Finished writing RSS radio map to files!")
    getCoordinates
    true
  }
}