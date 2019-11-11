package com.kios.airplace

import java.io.File

class CreateRadioMaps(path: String) {
  //RSS

  val defaultNaNValue: Int = -110
  val rssFileName = "rssRadioMap.txt" //Output file name
  val rssPathFolder: String = path + "\\logs\\rss" //Input folder
  val rssRadioMapFolder: String = path + "\\radioMaps\\rss" //Output folder
  val rssRadioMapFile = new rssRadioMap(new File(rssPathFolder), rssFileName, rssRadioMapFolder, defaultNaNValue)

  rssRadioMapFile.createRadioMap()

  //MAGNETIC
  val magneticFileName = "magneticRadioMap.txt" //Output file name
  val magneticPathFolder: String = path + "\\logs\\magnetic" //Input folder
  val magneticRadioMapFolder: String = path + "\\radioMaps\\magnetic" //Output folder
  val magneticRadioMapFile = new magneticRadioMap(new File(magneticPathFolder), magneticFileName, magneticRadioMapFolder, rssRadioMapFile.getCoordinates)

  magneticRadioMapFile.createRadioMap()
}