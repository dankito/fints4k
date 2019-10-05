package net.dankito.fints.response

import net.dankito.fints.messages.segmente.id.ISegmentId
import net.dankito.fints.messages.segmente.id.MessageSegmentId
import net.dankito.fints.response.segments.ReceivedMessageHeader
import net.dankito.fints.response.segments.ReceivedSynchronization
import org.assertj.core.api.Assertions.assertThat
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

        val segment = result.receivedSegments.first() as ReceivedSynchronization

        assertThat(segment.customerSystemId).isEqualTo("WL/2/Trhmm0BAAAjIADlyFkXrAQA")
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