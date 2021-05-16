package io.alem.skylo

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.*
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.isEmpty()){
        println("Broker address and port is missing.")
        println("Please enter broker address in the format name:port or ip:port")
        exitProcess(1)
    }else {
        val brokers = args[0]
        PayloadReaderSender(brokers).produce(2)
    }
}

class PayloadReaderSender(brokers: String) {

    private val producer = createProducer(brokers)

    private fun createProducer(brokers: String): Producer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["key.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
        props["value.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
        return KafkaProducer<String, String>(props)
    }

    fun produce(ratePerSecond: Int) {
        val waitTimeBetweenIterationsMs = 1000L / ratePerSecond
        val payloadArray = Utils().getPayloads(PATHNAME)
        for (payload in payloadArray) {
            val futureResult = producer.send(ProducerRecord(PAYLOADS_TOPIC, payload.payload))
            println("${payload.payload} Sent a payload")
            Thread.sleep(waitTimeBetweenIterationsMs)
            // wait for the write acknowledgment
            futureResult.get()
        }
    }
}