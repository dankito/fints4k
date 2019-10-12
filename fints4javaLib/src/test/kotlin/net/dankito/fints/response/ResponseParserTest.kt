package net.dankito.fints.response

import net.dankito.fints.FinTsTestBase
import net.dankito.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.fints.messages.datenelemente.implementierte.HbciVersion
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Sicherheitsverfahren
import net.dankito.fints.messages.datenelemente.implementierte.signatur.VersionDesSicherheitsverfahrens
import net.dankito.fints.messages.datenelementgruppen.implementierte.signatur.Sicherheitsprofil
import net.dankito.fints.messages.segmente.id.ISegmentId
import net.dankito.fints.messages.segmente.id.MessageSegmentId
import net.dankito.fints.response.segments.*
import net.dankito.utils.datetime.asUtilDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate


class ResponseParserTest : FinTsTestBase() {

    private val underTest = ResponseParser()


    @Test
    fun doNotSplitMaskedSegmentSeparator() {

        // when
        val result = underTest.parse(
            "HNHBK:1:3+000000000596+300+abcd?'efg+2'" +
                    "HKIDN:2:2+280:12345678+9999999999+0+0'"
        )

        // then
        assertThat(result.receivedSegments).hasSize(2)

        assertThat(result.messageHeader?.dialogId).isEqualTo("abcd'efg")
    }

    @Test
    fun doNotSplitMaskedDataElementGroupsSeparator() {

        // when
        val result = underTest.parse(
            "HNHBK:1:3+000000000596+300+abcd?+efg+2'" +
                    "HKIDN:2:2+280:12345678+9999999999+0+0'"
        )

        // then
        assertThat(result.receivedSegments).hasSize(2)

        assertThat(result.messageHeader?.dialogId).isEqualTo("abcd+efg")
    }

    @Test
    fun doNotSplitMaskedDataElementsSeparator() {

        // when
        val result = underTest.parse(
            "HNHBK:1:3+000000000596+300+https?://www.example.org+2'" +
                    "HKIDN:2:2+280:12345678+9999999999+0+0'"
        )

        // then
        assertThat(result.receivedSegments).hasSize(2)

        assertThat(result.messageHeader?.dialogId).isEqualTo("https://www.example.org")
    }

    @Test
    fun unmaskMaskingCharacter() {

        // when
        val result = underTest.parse(
            "HNHBK:1:3+000000000596+300+abcd??efg+2'" +
                    "HKIDN:2:2+280:12345678+9999999999+0+0'"
        )

        // then
        assertThat(result.receivedSegments).hasSize(2)

        assertThat(result.messageHeader?.dialogId).isEqualTo("abcd?efg")
    }


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


    @Test
    fun parseUserParameters() {

        // when
        val result = underTest.parse("HIUPA:6:4:4+3498443795+34+0++PERSNR0010789316542'")

        // then
        assertSuccessfullyParsedSegment(result, InstituteSegmentId.UserParameters, 6, 4, 4)

        result.getFirstSegmentById<UserParameters>(InstituteSegmentId.UserParameters)?.let { segment ->
            assertThat(segment.userIdentifier).isEqualTo("3498443795")
            assertThat(segment.updVersion).isEqualTo(34)
            assertThat(segment.areListedJobsBlocked).isTrue()
            assertThat(segment.username).isNull()
            assertThat(segment.extension).isEqualTo("PERSNR0010789316542")
        }
        ?: run { Assert.fail("No segment of type UserParameters found in ${result.receivedSegments}") }
    }

    @Test
    fun parseAccountInfo() {

        // when
        val result = underTest.parse("HIUPD:7:6:4+0987654321::280:12345678+DE11123456780987654321+2197654321+1+EUR+Hans Dampf++Sichteinlagen++HKSAK:1+HKISA:1+HKSSP:1+HKPAE:1+HKTSY:1+HKTAB:1+HKTAU:1+HKTAZ:1+HKSPA:1+HKPKA:1+HKPKB:1+HKPWE:1+HKPWA:1+HKPWB:1+HKPWL:1+HKCAZ:1+HKCCM:1+HKCCS:1+HKCDB:1+HKCDE:1+HKCDL:1+HKCDN:1+HKCDU:1+HKCMB:1+HKCME:1+HKCML:1+HKCSA:1+HKCSB:1+HKCSE:1+HKCSL:1+HKCUB:1+HKCUM:1+HKDSB:1+HKDSW:1+HKIPS:1+HKIPZ:1+HKPCR:1+HKPPD:1+DKPSA:1+DKPSP:1+HKTAN:1+DKANA:1+DKANL:1+DKKBA:1+DKDKL:1+DKBDK:1+DKBAZ:1+DKTCK:1+DKZDF:1+DKZDL:1+HKFRD:1+HKKDM:1+HKKAZ:1+HKKIF:1+HKSAL:1+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++{\n" +
                "umsltzt\n" +
                "?:'")

        // then
        assertSuccessfullyParsedSegment(result, InstituteSegmentId.AccountInfo, 7, 6, 4)

        result.getFirstSegmentById<AccountInfo>(InstituteSegmentId.AccountInfo)?.let { segment ->
            assertThat(segment.accountNumber).isEqualTo("0987654321")
            assertThat(segment.subAccountAttribute).isNull()
            assertThat(segment.bankCountryCode).isEqualTo(280)
            assertThat(segment.bankCode).isEqualTo("12345678")
            assertThat(segment.iban).isEqualTo("DE11123456780987654321")
            assertThat(segment.customerId).isEqualTo("2197654321")
            assertThat(segment.accountType).isEqualTo(AccountType.Girokonto)
            assertThat(segment.currency).isEqualTo("EUR")
            assertThat(segment.accountHolderName1).isEqualTo("Hans Dampf")
            assertThat(segment.accountHolderName2).isNull()
            assertThat(segment.productName).isEqualTo("Sichteinlagen")
        }
        ?: run { Assert.fail("No segment of type AccountInfo found in ${result.receivedSegments}") }
    }


    @Test
    fun parseBalance() {

        // given
        val balance = 1234.56.toBigDecimal()
        val date = LocalDate.of(1988, 3, 27).asUtilDate()
        val bankCode = "12345678"
        val accountId = "0987654321"
        val accountProductName = "Sichteinlagen"

        // when
        val result = underTest.parse("HISAL:8:5:3+$accountId::280:$bankCode+$accountProductName+EUR+" +
                "C:${convertAmount(balance)}:EUR:${convertDate(date)}+C:0,:EUR:20191006++${convertAmount(balance)}:EUR")

        // then
        assertSuccessfullyParsedSegment(result, InstituteSegmentId.Balance, 8, 5, 3)

        result.getFirstSegmentById<BalanceSegment>(InstituteSegmentId.Balance)?.let { segment ->
            assertThat(segment.balance).isEqualTo(balance)
            assertThat(segment.currency).isEqualTo("EUR")
            assertThat(segment.date).isEqualTo(date)
            assertThat(segment.accountProductName).isEqualTo(accountProductName)
            assertThat(segment.balanceOfPreBookedTransactions).isNull()
        }
        ?: run { Assert.fail("No segment of type Balance found in ${result.receivedSegments}") }
    }


    @Test
    fun parseTanInfo() {

        // when
        val result = underTest.parse("HITANS:171:6:4+1+1+1+J:N:0:910:2:HHD1.3.0:::chipTAN manuell:6:1:TAN-Nummer:3:J:2:N:0:0:N:N:00:0:N:1:911:2:HHD1.3.2OPT:HHDOPT1:1.3.2:chipTAN optisch:6:1:TAN-Nummer:3:J:2:N:0:0:N:N:00:0:N:1:912:2:HHD1.3.2USB:HHDUSB1:1.3.2:chipTAN-USB:6:1:TAN-Nummer:3:J:2:N:0:0:N:N:00:0:N:1:913:2:Q1S:Secoder_UC:1.2.0:chipTAN-QR:6:1:TAN-Nummer:3:J:2:N:0:0:N:N:00:0:N:1:920:2:smsTAN:::smsTAN:6:1:TAN-Nummer:3:J:2:N:0:0:N:N:00:2:N:5:921:2:pushTAN:::pushTAN:6:1:TAN-Nummer:3:J:2:N:0:0:N:N:00:2:N:2:900:2:iTAN:::iTAN:6:1:TAN-Nummer:3:J:2:N:0:0:N:N:00:0:N:0'")

        // then
        assertSuccessfullyParsedSegment(result, InstituteSegmentId.TanInfo, 171, 6, 4)

        result.getFirstSegmentById<TanInfo>(InstituteSegmentId.TanInfo)?.let { segment ->
            assertThat(segment.maxCountJobs).isEqualTo(1)
            assertThat(segment.minimumCountSignatures).isEqualTo(1)
            assertThat(segment.securityClass).isEqualTo("1")
            assertThat(segment.tanProcedureParameters.oneStepProcedureAllowed).isTrue()
            assertThat(segment.tanProcedureParameters.moreThanOneTanDependentJobPerMessageAllowed).isFalse()
            assertThat(segment.tanProcedureParameters.jobHashValue).isEqualTo("0")

            assertThat(segment.tanProcedureParameters.procedureParameters).hasSize(7)
            assertThat(segment.tanProcedureParameters.procedureParameters).extracting("procedureName")
                .containsExactlyInAnyOrder("chipTAN manuell", "chipTAN optisch", "chipTAN-USB", "chipTAN-QR",
                    "smsTAN", "pushTAN", "iTAN")
        }
        ?: run { Assert.fail("No segment of type TanInfo found in ${result.receivedSegments}") }
    }


    private fun assertSuccessfullyParsedSegment(result: Response, segmentId: ISegmentId, segmentNumber: Int,
                                                segmentVersion: Int, referenceSegmentNumber: Int? = null) {

        assertThat(result.successful).isTrue()
        assertThat(result.error).isNull()
        assertThat(result.receivedResponse).isNotNull()

        val segment = result.getFirstSegmentById<ReceivedSegment>(segmentId)

        assertThat(segment).isNotNull()

        segment?.let {
            assertThat(segment.segmentId).isEqualTo(segmentId.id)
            assertThat(segment.segmentNumber).isEqualTo(segmentNumber)
            assertThat(segment.segmentVersion).isEqualTo(segmentVersion)
            assertThat(segment.referenceSegmentNumber).isEqualTo(referenceSegmentNumber)
        }
    }

}