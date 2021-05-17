/*
 * Copyright (c) 2021 Alemnew Asrese
 * <p>
 * A message consumer from Kafka broker and process it. The program subscribes to a Kafka broker and reads the
 * messages. It process the messages it receive to  produce Histogram and timeline of transmission (eventTime) for a
 * given sender (hub system in this case).
 *
 * @author alemnewsh@gmail.com Alemnew Asrese
 * @version 1.0
 * Created on 2021/05/17
 */

package io.alem.skylo

import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.KafkaException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Broker address and port are missing.")
        println("Please enter broker address. NAME:PORT or IP:PORT")
        exitProcess(1)
    } else if(args.size < 2) {
        println("Hub System ID is missing! Please enter Hub System ID.")
        exitProcess(1)
    } else {
        val brokers = args[0]
        val hubSystemId = args[1]
        // Check brokers address is well formed.
        val ipAndPort = brokers.split(":")
        if (ipAndPort.size == 2 && ipAndPort[1].toIntOrNull().let { true }) {
            val payloadConsumer = PayloadConsumerProcessor(brokers, "payload")
            payloadConsumer.start(hubSystemId)
        } else {
            println("Broker address not correct.")
        }
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
    fun start(hubSystemId: String) {
        payloadConsumer.subscribe(listOf(inputTopic))
        readFromInputTopic()
        val hubSystemIdFrequencyMap = calculateHistogram(payloadData)
        val et = getEventTimeOfSensors(payloadData)
        val timelinePerHr = calculateTransmissionTimelinePerHour(et, hubSystemId)

        /* Display result */
        println("--- Histogram of Frequently transmitting Hubs ---")
        println(hubSystemIdFrequencyMap)

        println("------ Event Transmission Timeline for Hub $hubSystemId -------")
        println(timelinePerHr)
    }

    /**
     *  Read payload from kafka stream server and return hubSystemId.
     */
    private fun readFromInputTopic() {
        try {
            while (true) {
                val payloads = payloadConsumer.poll(5000)
                if (!payloads.isEmpty) {
                    for (payload in payloads) {
                        payloadData.add(klaxon.parse<PayloadData>(payload.value()))
                    }
                } else {
                    break
                }
            }
        } catch (e: KafkaException) {
            println("Kafka Exception ERROR: ${e.stackTrace}")
        } catch (e: KlaxonException) {
            println("Klaxon parsing ERROR: ${e.stackTrace}")
        } catch (e: Exception) {
            println("ERROR. ${e.stackTrace}")
        }

    }

    /**
     * Calculate the most frequently transmitting hubs.
     * @param payloadData list of received payload data.
     */
    fun calculateHistogram(payloadData: List<PayloadData?>): MutableMap<String?, Int> {
        val hubSystemIdFrequencyMap: MutableMap<String?, Int> = HashMap()
        val hubSystemIdArray = arrayListOf<String?>()
        for (payload in payloadData) {
            hubSystemIdArray.add(payload?.hubSystemId)
        }
        for (hubSystemId in hubSystemIdArray.distinct()) {
            hubSystemIdFrequencyMap[hubSystemId] = Collections.frequency(hubSystemIdArray, hubSystemId)
        }
        return hubSystemIdFrequencyMap
    }

    /**
     * Get event time for all for bub.
     * @param payloadData list of payload data
     * @return  Map with hubSystemId and list of event times.
     */
    fun getEventTimeOfSensors(payloadData: List<PayloadData?>): MutableMap<String?, MutableList<String?>?> {
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
    fun calculateTransmissionTimelinePerHour(
        et: MutableMap<String?, MutableList<String?>?>,
        hubSystemId: String
    ): List<String> {
        var sortedEventTimesZdt = emptyList<String>()
        val eventTimes = et[hubSystemId]
        try {
            val eventTimesSet = mutableSetOf<String>()
            if (eventTimes != null) for (et in eventTimes) {
                val eventTimeZdt = ZonedDateTime.parse(et).withZoneSameInstant(ZoneId.systemDefault())
                eventTimesSet.add(eventTimeZdt.format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH")))
            }

            /* sort event times in ascending order.*/
            sortedEventTimesZdt = eventTimesSet.sortedBy { it }
        } catch (e: DateTimeParseException) {
            println("Date parsing ERROR. ${e.stackTrace}")
        }
        return sortedEventTimesZdt
    }

}