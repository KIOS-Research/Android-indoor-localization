package com.kios.airplace

import java.io.{BufferedReader, File, FileReader}
import java.util

import scala.beans.BeanProperty
import scala.collection.JavaConversions._
import scala.util.control.Breaks._

class rssRadioMapMean(private val isIndoor: Boolean, @BeanProperty val defaultNaNValue: Int) {
  private var RadioMapMean_File: File = _

  private val MacAddressList: util.ArrayList[String] = new util.ArrayList[String]()

  private var LocationRSS_HashMap: util.HashMap[String, util.ArrayList[String]] = _

  private val GroupLocationRSS_HashMap: util.HashMap[Integer, util.HashMap[String, util.ArrayList[String]]] = new util.HashMap[Integer, util.HashMap[String, util.ArrayList[String]]]()

  private val OrderList: util.ArrayList[String] = new util.ArrayList[String]()

  def getMacAddressList: util.ArrayList[String] = MacAddressList

  def getLocationRSS_HashMap(group: Int): util.HashMap[String, util.ArrayList[String]] = GroupLocationRSS_HashMap.get(group)

  def getOrderList: util.ArrayList[String] = OrderList

  def getRadioMapMean_File: File = this.RadioMapMean_File

  def ConstructRadioMap(inFile: File): Boolean = {
    if (!inFile.exists() || !inFile.canRead) {
      return false
    }
    this.RadioMapMean_File = inFile
    this.OrderList.clear()
    this.MacAddressList.clear()
    var RSS_Values: util.ArrayList[String] = null
    var reader: BufferedReader = null
    var line: String = null
    var temp: Array[String] = null
    var group = -1
    var key: String = null
    var lastKey: String = null
    try {
      reader = new BufferedReader(new FileReader(inFile))
      var c = 0
      while ( {
        line = reader.readLine
        line != null
      }) {
        breakable {
          c += 1
          if (c == 33) {
            System.out.print("tet")
          }
          if (line.trim() == "") {
            //continue (break from breakable inside loop)
            break
          }
          line = line.replace(", ", " ")
          temp = line.split(" ")
          if (temp(0).trim() == "#") {
            if (temp(1).trim() == "NaN") {
              //continue (break from breakable inside loop)
              break
            }
            if (temp.length < 5) {
              return false
            } else if (this.isIndoor &&
              (!temp(1).trim().equalsIgnoreCase("X") || !temp(2).trim().equalsIgnoreCase("Y"))) {
              return false
            }
            for (i <- 4 until temp.length) {
              if (!temp(i).matches("[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}")) {
                return false
              }
              this.MacAddressList.add(temp(i))
            }
            //continue (break from breakable inside loop)
            break
          }
          key = temp(0) + " " + temp(1)
          group = java.lang.Integer.parseInt(temp(2))
          RSS_Values = new util.ArrayList[String]()
          for (i <- 3 until temp.length) {
            RSS_Values.add(temp(i))
          }
          if (this.MacAddressList.size != RSS_Values.size) {
            return false
          }
          if (key != lastKey) {
            this.OrderList.add(key)
            lastKey = key
          }
          this.LocationRSS_HashMap = this.GroupLocationRSS_HashMap.get(group)
          if (this.LocationRSS_HashMap == null) {
            this.LocationRSS_HashMap = new util.HashMap[String, util.ArrayList[String]]()
            this.LocationRSS_HashMap.put(key, RSS_Values)
            this.GroupLocationRSS_HashMap.put(group, LocationRSS_HashMap)
            //continue (break from breakable inside loop)
            break
          }
          this.LocationRSS_HashMap.put(key, RSS_Values)
        }
      }
      reader.close()
    } catch {
      case e: Exception =>
        System.err.println("Error while constructing Location.RssRadioMap: " + "")
        e.printStackTrace()
        return false
    }
    true
  }

  def getGroupLocationRSS_HashMap: util.HashMap[Integer, util.HashMap[String, util.ArrayList[String]]] = {
    GroupLocationRSS_HashMap
  }

  override def toString: String = {
    var str = "MAC Addresses: "
    var temp: util.ArrayList[String] = null
    for (i <- 0 until MacAddressList.size) {
      str += MacAddressList.get(i) + " "
    }
    str += "\nLocations\n"
    for (location <- LocationRSS_HashMap.keySet) {
      str += location + " "
      temp = LocationRSS_HashMap.get(location)
      for (i <- 0 until temp.size) {
        str += temp.get(i) + " "
      }
      str += "\n"
    }
    str
  }
}