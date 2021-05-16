/* Copyright 2021
* @author Alemnew Asrese
*/

package io.alem.skylo

import com.beust.klaxon.Klaxon
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.isEmpty()){
        println("Broker address and port is missing.")
        println("Please enter broker address in the format NAME:PORT or IP:PORT")
        exitProcess(1)
    }else {
        val brokers = args[0]
        val payloadConsumer = PayloadConsumerProcessor(brokers, "payload")
        payloadConsumer.start()
    }
}

class PayloadConsumerProcessor(brokers: String, private val inputTopic: String) {
    private val payloadConsumer = createPayloadConsumer(brokers)
    private val payloadData = arrayListOf<PayloadData?>()
    private val klaxon = Klaxon()
    private fun createPayloadConsumer(brokers: String): Consumer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["group.id"] = "stream-payload"
        props["key.deserializer"] = "org.apache.kafka.common.serialization.StringDeserializer"
        props["value.deserializer"] = "org.apache.kafka.common.serialization.StringDeserializer"
        props["fetch.min.bytes"] = 10000
        return KafkaConsumer<String, String>(props)
    }

    /**
     * Start Payload consumer and process the received data.
     */
    fun start() {
        payloadConsumer.subscribe(listOf(inputTopic))
        readFromInputTopic()
        calculateHistogram(payloadData)
        val et = getEventTimeOfSensors(payloadData)
        calculateTransmissionTimelinePerHour(et, "HUBFIN015")
    }

    /**
     *  Read payload from kafka stream server and return hubSystemId.
     */
    private fun readFromInputTopic() {
        while (true) {
            val payloads = payloadConsumer.poll(5000)
            if (!payloads.isEmpty) {
                val payloadArray = arrayListOf<String>()
                for (payload in payloads) {
                    payloadArray.add(payload.value())
                    payloadData.add(klaxon.parse<PayloadData>(payload.value()))
                }
            } else {
                // println("No messages to read and poll timeout reached ... exit")
                break
            }
        }
    }

    /**
     * Calculate the most frequently transmitting hubs.
     * @param payloadData list of received payload data.
     */
    private fun calculateHistogram(payloadData: List<PayloadData?>) {
        //val hubSystemIdFrequencyMap: MutableMap<String?, Int> = HashMap()
        val hubSystemIdArray = arrayListOf<String?>()
        for (payload in payloadData) {
            hubSystemIdArray.add(payload?.hubSystemId)
        }

        for (hubSystemId in hubSystemIdArray.distinct()) {
            println(hubSystemId + ": " + Collections.frequency(hubSystemIdArray, hubSystemId))
        }
    }

    /**
     * Get event time for all for bub.
     * @param payloadData list of payload data
     * @return  Map with hubSystemId and list of event times.
     */
    private fun getEventTimeOfSensors(payloadData: List<PayloadData?>): MutableMap<String?, MutableList<String?>?> {
        val timeEventOfHubs: MutableMap<String?, MutableList<String?>?> = HashMap()
        var currentEvent: MutableList<String?>?
        for (payload in payloadData) {
            if (!timeEventOfHubs.containsKey(payload?.hubSystemId)) {
                currentEvent = timeEventOfHubs[payload?.hubSystemId] ?: run {
                    mutableListOf(payload?.eventTime)
                }
            } else {
                currentEvent = timeEventOfHubs[payload?.hubSystemId]
                currentEvent?.add(payload?.eventTime)
            }
            timeEventOfHubs[payload?.hubSystemId] = currentEvent
        }
        return timeEventOfHubs
    }

    /**
     * Timeline of the transmission timeline for a hub per hour
     * @param et a map of event time (hubSystemId as a key and list of event times as a value).
     * @param hubSystemId an identified for the hub.
     */
    private fun calculateTransmissionTimelinePerHour(
        et: MutableMap<String?, MutableList<String?>?>,
        hubSystemId: String
    ) {
        val eventTimes = et[hubSystemId]
        println("=============== Event Transmission Timeline for Hub $hubSystemId ===============")
        try {
            val eventTimesSet = mutableSetOf<String>()
            if (eventTimes != null) for (et in eventTimes) {
                val eventTimeZdt = ZonedDateTime.parse(et).withZoneSameInstant(ZoneId.systemDefault())
                eventTimesSet.add(eventTimeZdt.format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH")))
            }

            /* sort event times in ascending order.*/
            val sortedEventTimesZdt = eventTimesSet.sortedBy { it }
            println(sortedEventTimesZdt)

        } catch (e: DateTimeParseException) {
            println("Date parsing error.")
        }
    }

}