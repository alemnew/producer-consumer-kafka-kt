package io.alem.skylo

data class PayloadData(
    val hubId: String = "",
    val hubName: String = "",
    val baseStation: String = "",
    val profile: String = "",
    val hubGroup: String = "",
    val imsi: String = "",
    val customerId: String = "",
    val group: String = "",
    val type: String = "",
    val seqNo: String = "",
    val eventTime: String = "",
    val hubSystemId: String = ""
    )
data class Payload (val payload: String)
