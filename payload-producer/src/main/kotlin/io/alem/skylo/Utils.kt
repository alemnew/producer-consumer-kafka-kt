package io.alem.skylo

import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException
import java.io.File
import java.io.StringReader

class Utils {
    fun getPayloads(pathname: String): List<Payload> {
        val payloadArray = arrayListOf<Payload>()
        try {
            val jsonString: String = File(pathname).readText(Charsets.UTF_8)
            JsonReader(StringReader(jsonString)).use { reader ->
                reader.beginArray {
                    while (reader.hasNext()) {
                        val payload = Klaxon().parse<Payload>(reader)
                        payloadArray.add(payload!!)
                    }
                }
            }
        } catch (e: KlaxonException) {
            println("Klaxon parsing ERROR: ${e.stackTrace.toString()}")
        } catch (e: Exception) {
            println("ERROR: ${e.stackTrace.toString()}")
        }
        return payloadArray
    }

}