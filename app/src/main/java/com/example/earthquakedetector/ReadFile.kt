package com.example.earthquakedetector

import java.io.File

val magnitude:Double?=null
val data = listOf<String>()
val lastline: String?=null
val lines:Any?=null
val listofparams:Any?=null
val recent:List<String>?=null
class ReadFile(s: String) {


    fun readFile(filename: String){
        val listofparams = mutableListOf<String>()
        val lines: List<String> = File(filename).readLines()
        lines.forEach { line -> listofparams.addAll(lines) }
        val recent = lines.last().split(" ")
    }

    fun getMagnitude(): Int? {
        val magnitude = recent?.get(10)?.toInt()
        return magnitude
    }
    fun getRange(): Int? {
        val range = recent?.get(9)?.toInt()
        return range
    }

    fun getHour(): Int? {
        val hour = recent?.get(4)?.toInt()
        return hour
    }
    fun getMinutes(): Int? {
        val minutes = recent?.get(5)?.toInt()
        return minutes
    }
}