/*
 * Copyright (c) 2021 Alemnew Asrese
 * <p>
 * A test program to produce Histogram and timeline of  transmission from a data.
 *
 * @author alemnewsh@gmail.com Alemnew Asrese
 * @version 1.0
 * Created on 2021/05/17
 */

package io.alem.skylo

import com.beust.klaxon.Klaxon
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

internal class PayloadConsumerProcessorTest {
    private val payloadStringOK = arrayListOf<String>(
        "{\"hubId\":\"ASSIGN210\",\"hubName\":\"SKY-B8-S2\",\"baseStation\":\"ASSIGN210\",\"profile\":\"normal\"," +
                "\"hubGroup\":\"003--1143978760-SubNetwork=Fin,MeContext=ASSIGN210,ManagedElement=ASSIGN210,Equipment=1," +
                "FieldReplaceableUnit=SKY-B8-S2,RfPort=A-1460123\",\"imsi\":\"1460123\",\"customerId\":\"0\",\"group\":\"SubNetwork=Fin,MeContext=ASSIGN210,ManagedElement=ASSIGN210,Equipment=1,FieldReplaceableUnit=SKY-B8-S2,RfPort=A\",\"type\":\"Minor\",\"seqNo\":\"1460123\"," +
                "\"eventTime\":\"2020-01-22T02:36:31+02:00\",\"hubSystemId\":\"HUBFIN021\"}",
        "{\"hubId\":\"ASSIGN141\",\"baseStation\":\"ASSIGN141\",\"profile\":\"normal\",\"hubGroup\":\"003--936265541-SubNetwork=US,MeContext=ASSIGN141,ManagedElement=ASSIGN141,ENodeBFunction=1,NbIotCell=vodice_ht-1469856\",\"imsi\":\"1469856\",\"customerId\":\"9175114\",\"group\":\"SubNetwork=US,MeContext=ASSIGN141,ManagedElement=ASSIGN141,ENodeBFunction=1,NbIotCell=vodice_ht\",\"type\":\"Major\",\"seqNo\":\"1469856\"," +
                "\"eventTime\":\"2020-01-22T02:36:31+02:00\",\"hubSystemId\":\"HUBFIN015\"}",
        "{\"hubId\":\"ASSIGN18\",\"hubName\":\"SKY-B8-S2\",\"baseStation\":\"ASSIGN18\",\"profile\":\"UNKNOWN\"," +
                "\"hubGroup\":\"003--1143978760-SubNetwork=Fin,MeContext=ASSIGN18,ManagedElement=ASSIGN18,Equipment=1," +
                "FieldReplaceableUnit=SKY-B8-S2,RfPort=A-1460123\",\"imsi\":\"1460123\",\"customerId\":\"0\"," +
                "\"group\":\"SubNetwork=Fin,MeContext=ASSIGN18,ManagedElement=ASSIGN18,Equipment=1,FieldReplaceableUnit=SKY-B8-S2,RfPort=A\",\"type\":\"Minor\",\"seqNo\":\"1460123\"," +
                "\"eventTime\":\"2020-01-24T09:09:25+02:00\",\"hubSystemId\":\"HUBFIN021\"}",
        "{\"hubId\":\"ASSIGN10\",\"baseStation\":\"ASSIGN10\",\"profile\":\"UNKNOWN\"," +
                "\"hubGroup\":\"003--936265541-SubNetwork=US,MeContext=ASSIGN10,ManagedElement=ASSIGN10,ENodeBFunction=1,NbIotCell=predavac-1469856\",\"imsi\":\"1469856\",\"customerId\":\"9175114\",\"group\":\"SubNetwork=US,MeContext=ASSIGN10,ManagedElement=ASSIGN10,ENodeBFunction=1,NbIotCell=predavac\",\"type\":\"Major\",\"seqNo\":\"1469856\"," +
                "\"eventTime\":\"2020-01-23T11:08:57+02:00\",\"hubSystemId\":\"HUBFIN015\"}",
        "{\"hubId\":\"ASSIGN5\",\"baseStation\":\"ASSIGN5\",\"profile\":\"UNKNOWN\"," +
                "\"hubGroup\":\"003--936265541-SubNetwork=US,MeContext=ASSIGN5,ManagedElement=ASSIGN5,ENodeBFunction=1,NbIotCell=bj_tkc-1469856\",\"imsi\":\"1469856\",\"customerId\":\"9175114\",\"group\":\"SubNetwork=US,MeContext=ASSIGN5,ManagedElement=ASSIGN5,ENodeBFunction=1,NbIotCell=bj_tkc\",\"type\":\"Major\",\"seqNo\":\"1469856\"," +
                "\"eventTime\":\"2020-01-23T11:08:45+02:00\",\"hubSystemId\":\"HUBFIN015\"}",
        "{\"hubId\":\"ASSIGN17\",\"baseStation\":\"ASSIGN17\",\"profile\":\"UNKNOWN\"," +
                "\"hubGroup\":\"003--936265541-SubNetwork=US,MeContext=ASSIGN17,ManagedElement=ASSIGN17,ENodeBFunction=1,NbIotCell=sandrovac-1469856\",\"imsi\":\"1469856\",\"customerId\":\"9175114\",\"group\":\"SubNetwork=US,MeContext=ASSIGN17,ManagedElement=ASSIGN17,ENodeBFunction=1,NbIotCell=sandrovac\",\"type\":\"Major\",\"seqNo\":\"1469856\"," +
                "\"eventTime\":\"2020-01-23T11:09:09+02:00\",\"hubSystemId\":\"HUBFIN015\"}",
        "{\"hubId\":\"ASSIGN138\",\"baseStation\":\"ASSIGN138\",\"profile\":\"normal\"," +
                "\"hubGroup\":\"003--936265541-SubNetwork=US,MeContext=ASSIGN138,ManagedElement=ASSIGN138,ENodeBFunction=1,NbIotCell=tunel_dumbocica-1469856\",\"imsi\":\"1469856\",\"customerId\":\"9175114\",\"group\":\"SubNetwork=US,MeContext=ASSIGN138,ManagedElement=ASSIGN138,ENodeBFunction=1,NbIotCell=tunel_dumbocica\",\"type\":\"Major\",\"seqNo\":\"1469856\"," +
                "\"eventTime\":\"2020-01-22T06:25:35+02:00\",\"hubSystemId\":\"HUBFIN015\"}",
        "{\"hubId\":\"ASSIGN245\",\"hubName\":\"SKY-B8-S2\",\"baseStation\":\"ASSIGN245\",\"profile\":\"normal\"," +
                "\"hubGroup\":\"003--1143978760-SubNetwork=Fin,MeContext=ASSIGN245,ManagedElement=ASSIGN245,Equipment=1,FieldReplaceableUnit=SKY-B8-S2,RfPort=A-1460123\",\"imsi\":\"1460123\",\"customerId\":\"0\",\"group\":\"SubNetwork=Fin,MeContext=ASSIGN245,ManagedElement=ASSIGN245,Equipment=1,FieldReplaceableUnit=SKY-B8-S2,RfPort=A\",\"type\":\"Minor\",\"seqNo\":\"1460123\"," +
                "\"eventTime\":\"2020-01-22T06:25:35+02:00\",\"hubSystemId\":\"HUBFIN021\"}",
        "{\"hubId\":\"ASSIGN54\",\"hubName\":\"SKY-B8-S2\",\"baseStation\":\"ASSIGN54\",\"profile\":\"normal\"," +
                "\"hubGroup\":\"003--1143978760-SubNetwork=Fin,MeContext=ASSIGN54,ManagedElement=ASSIGN54,Equipment=1,FieldReplaceableUnit=SKY-B8-S2,RfPort=A-1460123\",\"imsi\":\"1460123\",\"customerId\":\"0\",\"group\":\"SubNetwork=Fin,MeContext=ASSIGN54,ManagedElement=ASSIGN54,Equipment=1,FieldReplaceableUnit=SKY-B8-S2,RfPort=A\",\"type\":\"Minor\",\"seqNo\":\"1460123\"," +
                "\"eventTime\":\"2020-01-21T22:47:28+02:00\",\"hubSystemId\":\"HUBFIN021\"}",
        "{\"hubId\":\"ASSIGN228\",\"baseStation\":\"ASSIGN228\",\"profile\":\"normal\",\"hubGroup\":\"003--936265541-SubNetwork=US,MeContext=ASSIGN228,ManagedElement=ASSIGN228,ENodeBFunction=1,NbIotCell=gracani-1469856\",\"imsi\":\"1469856\",\"customerId\":\"9175114\",\"group\":\"SubNetwork=US,MeContext=ASSIGN228,ManagedElement=ASSIGN228,ENodeBFunction=1,NbIotCell=gracani\",\"type\":\"Major\",\"seqNo\":\"1469856\"," +
                "\"eventTime\":\"2020-01-21T22:47:28+02:00\",\"hubSystemId\":\"HUBFIN015\"}"
    )

    private val payloadDataOK = arrayListOf<PayloadData?>()
    private val payloadC: PayloadConsumerProcessor = PayloadConsumerProcessor("localhost:9093", "payload")

    @BeforeTest
    fun setup() {
        for (payload in payloadStringOK) {
            payloadDataOK.add(Klaxon().parse<PayloadData>(payload))
        }
    }


    @Test
    fun testCalculateHistogram() {
        //{HUBFIN021=4, HUBFIN015=6}
        val result = payloadC.calculateHistogram(payloadDataOK)
        assertTrue(result.size == 2, "Length is not 2. It is ${result.size}")
        assertTrue(result["HUBFIN021"] == 4 && result["HUBFIN015"] == 6)
    }

    @Test
    fun testCalculateTransmissionTimelinePerHour() {
        val expectedList = arrayListOf<String>("2020-01-21 22", "2020-01-22 02", "2020-01-22 06", "2020-01-23 11")
        val et = payloadC.getEventTimeOfSensors(payloadDataOK)
        val result = payloadC.calculateTransmissionTimelinePerHour(et, "HUBFIN015")

        assertContentEquals(expectedList, result, "Timeline is not the same -- $result")

    }
}