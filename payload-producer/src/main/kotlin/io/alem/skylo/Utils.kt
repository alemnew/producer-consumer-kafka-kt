package io.alem.skylo

import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
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
        } catch (e: Exception) {
            println(e.stackTrace)
        }
        return payloadArray
    }

}