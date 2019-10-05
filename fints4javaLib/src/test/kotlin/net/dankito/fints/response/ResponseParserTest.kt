package net.dankito.fints.response

import net.dankito.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.fints.messages.datenelemente.implementierte.HbciVersion
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Sicherheitsverfahren
import net.dankito.fints.messages.datenelemente.implementierte.signatur.VersionDesSicherheitsverfahrens
import net.dankito.fints.messages.datenelementgruppen.implementierte.signatur.Sicherheitsprofil
import net.dankito.fints.messages.segmente.id.ISegmentId
import net.dankito.fints.messages.segmente.id.MessageSegmentId
import net.dankito.fints.response.segments.BankParameters
import net.dankito.fints.response.segments.ReceivedMessageHeader
import net.dankito.fints.response.segments.ReceivedSynchronization
import net.dankito.fints.response.segments.SecurityMethods
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert
import org.junit.Test


class ResponseParserTest {

    private val underTest = ResponseParser()


    @Test
    fun parseMessageHeader() {

        // when
        val result = underTest.parse("HNHBK:1:3+000000000596+300+817407729605=887211382312BLB4=+2+817407729605=887211382312BLB4=:2")

        // then
        assertSuccessfullyParsedSegment(result, MessageSegmentId.MessageHeader, 1, 3)

        assertThat(result.messageHeader).isNotNull
        val header = result.receivedSegments.first() as ReceivedMessageHeader

        assertThat(header.messageSize).isEqualTo(596)
        assertThat(header.finTsVersion).isEqualTo(300)
        assertThat(header.dialogId).isEqualTo("817407729605=887211382312BLB4=")
        assertThat(header.messageNumber).isEqualTo(2)
    }


    @Test
    fun parseSynchronization() {

        // when
        val result = underTest.parse("HISYN:173:4:6+WL/2/Trhmm0BAAAjIADlyFkXrAQA")

        // then
        assertSuccessfullyParsedSegment(result, InstituteSegmentId.Synchronization, 173, 4, 6)

        result.getFirstSegmentById<ReceivedSynchronization>(InstituteSegmentId.Synchronization)?.let { segment ->
            assertThat(segment.customerSystemId).isEqualTo("WL/2/Trhmm0BAAAjIADlyFkXrAQA")
        }
        ?: run { Assert.fail("No segment of type ReceivedSynchronization found in ${result.receivedSegments}") }
    }


    @Test
    fun parseBankParameters() {

        // when
        val result = underTest.parse("HIBPA:5:3:3+34+280:10070000+Deutsche Bank+0+1+300+0'")

        // then
        assertSuccessfullyParsedSegment(result, InstituteSegmentId.BankParameters, 5, 3, 3)

        result.getFirstSegmentById<BankParameters>(InstituteSegmentId.BankParameters)?.let { segment ->
            assertThat(segment.bpdVersion).isEqualTo(34)
            assertThat(segment.bankCountryCode).isEqualTo(280)
            assertThat(segment.bankCode).isEqualTo("10070000")
            assertThat(segment.bankName).isEqualTo("Deutsche Bank")

            assertThat(segment.countMaxJobsPerMessage).isEqualTo(0)
            assertThat(segment.supportedLanguages).containsExactly(Dialogsprache.German)
            assertThat(segment.supportedHbciVersions).containsExactly(HbciVersion.FinTs_3_0_0)

            assertThat(segment.maxMessageSize).isEqualTo(0)
            assertThat(segment.minTimeout).isNull()
            assertThat(segment.maxTimeout).isNull()
        }
        ?: run { Assert.fail("No segment of type BankParameters found in ${result.receivedSegments}") }
    }


    @Test
    fun parseSecurityMethods() {

        // when
        val result = underTest.parse("HISHV:7:3:3+N+RDH:1:9:10+DDV:1+PIN:1'")

        // then
        assertSuccessfullyParsedSegment(result, InstituteSegmentId.SecurityMethods, 7, 3, 3)

        result.getFirstSegmentById<SecurityMethods>(InstituteSegmentId.SecurityMethods)?.let { segment ->
            assertThat(segment.mixingAllowed).isFalse()

            assertThat(segment.securityProfiles).contains(
                Sicherheitsprofil(Sicherheitsverfahren.RDH, VersionDesSicherheitsverfahrens.PIN_Ein_Schritt),
                Sicherheitsprofil(Sicherheitsverfahren.RDH, VersionDesSicherheitsverfahrens.RAH_9),
                Sicherheitsprofil(Sicherheitsverfahren.RDH, VersionDesSicherheitsverfahrens.RAH_10),
                Sicherheitsprofil(Sicherheitsverfahren.DDV, VersionDesSicherheitsverfahrens.PIN_Ein_Schritt),
                Sicherheitsprofil(Sicherheitsverfahren.PIN_TAN_Verfahren, VersionDesSicherheitsverfahrens.PIN_Ein_Schritt)
            )
        }
        ?: run { Assert.fail("No segment of type SecurityMethods found in ${result.receivedSegments}") }
    }


    private fun assertSuccessfullyParsedSegment(result: Response, segmentId: ISegmentId, segmentNumber: Int,
                                                segmentVersion: Int, referenceSegmentNumber: Int? = null) {

        assertThat(result.successful).isTrue()
        assertThat(result.receivedResponse).isNotNull()
        assertThat(result.receivedSegments).hasSize(1)

        val segment = result.receivedSegments.first()

        assertThat(segment.segmentId).isEqualTo(segmentId.id)
        assertThat(segment.segmentNumber).isEqualTo(segmentNumber)
        assertThat(segment.segmentVersion).isEqualTo(segmentVersion)
        assertThat(segment.referenceSegmentNumber).isEqualTo(referenceSegmentNumber)
    }

}