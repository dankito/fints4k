package net.dankito.fints.response

import net.dankito.fints.FinTsTestBase
import net.dankito.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.fints.messages.datenelemente.implementierte.HbciVersion
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Sicherheitsverfahren
import net.dankito.fints.messages.datenelemente.implementierte.signatur.VersionDesSicherheitsverfahrens
import net.dankito.fints.messages.datenelemente.implementierte.tan.*
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
    fun parseMessageFeedback_Warning() {

        // when
        val result = underTest.parse("HIRMG:3:2+3060::Bitte beachten Sie die enthaltenen Warnungen/Hinweise.")

        // then
        assertSuccessfullyParsedSegment(result, InstituteSegmentId.MessageFeedback, 3, 2)

        assertThat(result.messageFeedback).isNotNull()

        assertThat(result.messageFeedback?.feedbacks).hasSize(1)

        val firstFeedback = result.messageFeedback?.feedbacks?.get(0)!!
        assertThat(firstFeedback.responseCode).isEqualTo(3060)
        assertThat(firstFeedback.isSuccess).isFalse()
        assertThat(firstFeedback.isWarning).isTrue()
        assertThat(firstFeedback.isError).isFalse()
        assertThat(firstFeedback.message).isEqualTo("Bitte beachten Sie die enthaltenen Warnungen/Hinweise.")
        assertThat(firstFeedback.parameter).isNull()
    }

    @Test
    fun parseMessageFeedback_Error() {

        // when
        val result = underTest.parse("HIRMG:3:2+9050::Die Nachricht enthält Fehler.")

        // then
        assertCouldParseSegment(result, InstituteSegmentId.MessageFeedback, 3, 2)

        assertThat(result.messageFeedback).isNotNull()

        assertThat(result.messageFeedback?.feedbacks).hasSize(1)

        val firstFeedback = result.messageFeedback?.feedbacks?.get(0)!!
        assertThat(firstFeedback.responseCode).isEqualTo(9050)
        assertThat(firstFeedback.isSuccess).isFalse()
        assertThat(firstFeedback.isWarning).isFalse()
        assertThat(firstFeedback.isError).isTrue()
        assertThat(firstFeedback.message).isEqualTo("Die Nachricht enthält Fehler.")
        assertThat(firstFeedback.parameter).isNull()
    }

    @Test
    fun parseMessageFeedback_MultipleFeedback() {

        // when
        val result = underTest.parse("HIRMG:3:2+9050::Die Nachricht enthält Fehler.+3905::Es wurde keine Challenge erzeugt.")

        // then
        assertCouldParseSegment(result, InstituteSegmentId.MessageFeedback, 3, 2)

        assertThat(result.messageFeedback).isNotNull()

        assertThat(result.messageFeedback?.feedbacks).hasSize(2)

        val firstFeedback = result.messageFeedback?.feedbacks?.get(0)!!
        assertThat(firstFeedback.responseCode).isEqualTo(9050)
        assertThat(firstFeedback.isSuccess).isFalse()
        assertThat(firstFeedback.isWarning).isFalse()
        assertThat(firstFeedback.isError).isTrue()
        assertThat(firstFeedback.message).isEqualTo("Die Nachricht enthält Fehler.")
        assertThat(firstFeedback.parameter).isNull()

        val secondFeedback = result.messageFeedback?.feedbacks?.get(1)!!
        assertThat(secondFeedback.responseCode).isEqualTo(3905)
        assertThat(secondFeedback.isSuccess).isFalse()
        assertThat(secondFeedback.isWarning).isTrue()
        assertThat(secondFeedback.isError).isFalse()
        assertThat(secondFeedback.message).isEqualTo("Es wurde keine Challenge erzeugt.")
        assertThat(secondFeedback.parameter).isNull()
    }

    @Test
    fun parseSegmentFeedback_MultipleFeedback() {

        // when
        val result = underTest.parse("HIRMG:3:2+9050::Die Nachricht enthält Fehler.+3905::Es wurde keine Challenge erzeugt.")

        // then
        assertCouldParseSegment(result, InstituteSegmentId.MessageFeedback, 3, 2)

        assertThat(result.messageFeedback).isNotNull()

        assertThat(result.messageFeedback?.feedbacks).hasSize(2)

        val firstFeedback = result.messageFeedback?.feedbacks?.get(0)!!
        assertThat(firstFeedback.responseCode).isEqualTo(9050)
        assertThat(firstFeedback.isSuccess).isFalse()
        assertThat(firstFeedback.isWarning).isFalse()
        assertThat(firstFeedback.isError).isTrue()
        assertThat(firstFeedback.message).isEqualTo("Die Nachricht enthält Fehler.")
        assertThat(firstFeedback.parameter).isNull()

        val secondFeedback = result.messageFeedback?.feedbacks?.get(1)!!
        assertThat(secondFeedback.responseCode).isEqualTo(3905)
        assertThat(secondFeedback.isSuccess).isFalse()
        assertThat(secondFeedback.isWarning).isTrue()
        assertThat(secondFeedback.isError).isFalse()
        assertThat(secondFeedback.message).isEqualTo("Es wurde keine Challenge erzeugt.")
        assertThat(secondFeedback.parameter).isNull()
    }


    @Test
    fun parseSegmentFeedback_Aufsetzpunkt() {

        // when
        val result = underTest.parse("HIRMS:4:2:3+0020::Der Auftrag wurde ausgeführt.+0020::Die gebuchten Umsätze wurden übermittelt.+3040::Es liegen weitere Informationen vor.:9345-10-26-11.52.15.693455")


        // then
        assertCouldParseSegment(result, InstituteSegmentId.SegmentFeedback, 4, 2, 3)

        assertThat(result.aufsetzpunkt).isEqualTo("9345-10-26-11.52.15.693455")
    }

    @Test
    fun parseSegmentFeedback_AllowedUserTanProcedures() {

        // when
        val result = underTest.parse("HIRMS:4:2:4+3050::BPD nicht mehr aktuell, aktuelle Version enthalten.+3920::Zugelassene Zwei-Schritt-Verfahren für den Benutzer.:910:911:912:913+0020::Der Auftrag wurde ausgeführt.")


        // then
        assertCouldParseSegment(result, InstituteSegmentId.SegmentFeedback, 4, 2, 4)

        assertThat(result.segmentFeedbacks).hasSize(1)

        assertThat(result.supportedTanProceduresForUser).containsExactlyInAnyOrder(Sicherheitsfunktion.PIN_TAN_910,
            Sicherheitsfunktion.PIN_TAN_911, Sicherheitsfunktion.PIN_TAN_912, Sicherheitsfunktion.PIN_TAN_913)

        val segmentFeedback = result.segmentFeedbacks.first()

        assertThat(segmentFeedback.feedbacks).hasSize(3)

        val firstFeedback = segmentFeedback.feedbacks[0]
        assertThat(firstFeedback.responseCode).isEqualTo(3050)
        assertThat(firstFeedback.isSuccess).isFalse()
        assertThat(firstFeedback.isWarning).isTrue()
        assertThat(firstFeedback.isError).isFalse()
        assertThat(firstFeedback.message).isEqualTo("BPD nicht mehr aktuell, aktuelle Version enthalten.")
        assertThat(firstFeedback.parameter).isNull()

        val secondFeedback = segmentFeedback.feedbacks[1]
        assertThat(secondFeedback is SupportedTanProceduresForUserFeedback).isTrue()
        assertThat((secondFeedback as SupportedTanProceduresForUserFeedback).supportedTanProcedures)
            .containsExactlyInAnyOrder(Sicherheitsfunktion.PIN_TAN_910,Sicherheitsfunktion.PIN_TAN_911,
                Sicherheitsfunktion.PIN_TAN_912, Sicherheitsfunktion.PIN_TAN_913)
        assertThat(secondFeedback.responseCode).isEqualTo(3920)
        assertThat(secondFeedback.isSuccess).isFalse()
        assertThat(secondFeedback.isWarning).isTrue()
        assertThat(secondFeedback.isError).isFalse()
        assertThat(secondFeedback.message).isEqualTo("Zugelassene Zwei-Schritt-Verfahren für den Benutzer.")
        assertThat(secondFeedback.parameter).isNull()

        val thirdFeedback = segmentFeedback.feedbacks[2]
        assertThat(thirdFeedback.responseCode).isEqualTo(20)
        assertThat(thirdFeedback.isSuccess).isTrue()
        assertThat(thirdFeedback.isWarning).isFalse()
        assertThat(thirdFeedback.isError).isFalse()
        assertThat(thirdFeedback.message).isEqualTo("Der Auftrag wurde ausgeführt.")
        assertThat(thirdFeedback.parameter).isNull()
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
                Sicherheitsprofil(Sicherheitsverfahren.RDH, VersionDesSicherheitsverfahrens.Version_1),
                Sicherheitsprofil(Sicherheitsverfahren.RDH, VersionDesSicherheitsverfahrens.Version_9),
                Sicherheitsprofil(Sicherheitsverfahren.RDH, VersionDesSicherheitsverfahrens.Version_10),
                Sicherheitsprofil(Sicherheitsverfahren.DDV, VersionDesSicherheitsverfahrens.Version_1),
                Sicherheitsprofil(Sicherheitsverfahren.PIN_TAN_Verfahren, VersionDesSicherheitsverfahrens.Version_1)
            )
        }
        ?: run { Assert.fail("No segment of type SecurityMethods found in ${result.receivedSegments}") }
    }

    @Test
    fun parseSecurityMethods_AllVersions() {

        // when
        val result = underTest.parse("HISHV:7:3:3+N+RDH:1:3:5:7:9+RAH:2:4:6:8:10+PIN:1'")

        // then
        assertSuccessfullyParsedSegment(result, InstituteSegmentId.SecurityMethods, 7, 3, 3)

        result.getFirstSegmentById<SecurityMethods>(InstituteSegmentId.SecurityMethods)?.let { segment ->
            assertThat(segment.mixingAllowed).isFalse()

            assertThat(segment.securityProfiles).contains(
                Sicherheitsprofil(Sicherheitsverfahren.RDH, VersionDesSicherheitsverfahrens.Version_1),
                Sicherheitsprofil(Sicherheitsverfahren.RDH, VersionDesSicherheitsverfahrens.Version_3),
                Sicherheitsprofil(Sicherheitsverfahren.RDH, VersionDesSicherheitsverfahrens.Version_5),
                Sicherheitsprofil(Sicherheitsverfahren.RDH, VersionDesSicherheitsverfahrens.Version_7),
                Sicherheitsprofil(Sicherheitsverfahren.RDH, VersionDesSicherheitsverfahrens.Version_9),
                Sicherheitsprofil(Sicherheitsverfahren.RSA_AES_Hybridverfahren, VersionDesSicherheitsverfahrens.Version_2),
                Sicherheitsprofil(Sicherheitsverfahren.RSA_AES_Hybridverfahren, VersionDesSicherheitsverfahrens.Version_4),
                Sicherheitsprofil(Sicherheitsverfahren.RSA_AES_Hybridverfahren, VersionDesSicherheitsverfahrens.Version_6),
                Sicherheitsprofil(Sicherheitsverfahren.RSA_AES_Hybridverfahren, VersionDesSicherheitsverfahrens.Version_8),
                Sicherheitsprofil(Sicherheitsverfahren.PIN_TAN_Verfahren, VersionDesSicherheitsverfahrens.Version_1)
            )
        }
        ?: run { Assert.fail("No segment of type SecurityMethods found in ${result.receivedSegments}") }
    }

    @Test
    fun parseCommunicationInfo() {

        // given
        val language = Dialogsprache.German

        // when
        val result = underTest.parse("HIKOM:5:4:3+$BankCountryCode:$BankCode+1+3:$BankFinTsServerAddress+2:$BankFinTsServerAddress::MIM:1'")

        // then
        assertSuccessfullyParsedSegment(result, InstituteSegmentId.CommunicationInfo, 5, 4, 3)

        result.getFirstSegmentById<CommunicationInfo>(InstituteSegmentId.CommunicationInfo)?.let { segment ->
            assertThat(segment.bankInfo.bankCountryCode).isEqualTo(BankCountryCode)
            assertThat(segment.bankInfo.bankCode).isEqualTo(BankCode)

            assertThat(segment.defaultLanguage).isEqualTo(language)

            assertThat(segment.parameters).containsExactlyInAnyOrder(
                CommunicationParameter(Kommunikationsdienst.Https, BankFinTsServerAddress),
                CommunicationParameter(Kommunikationsdienst.TCP_IP, BankFinTsServerAddress)
            )
        }
        ?: run { Assert.fail("No segment of type CommunicationInfo found in ${result.receivedSegments}") }
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
            assertThat(segment.accountIdentifier).isEqualTo("0987654321")
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
            assertThat(segment.accountLimit).isNull()
            assertThat(segment.allowedJobNames).hasSize(44)
            assertThat(segment.extension).isNotNull()
        }
        ?: run { Assert.fail("No segment of type AccountInfo found in ${result.receivedSegments}") }
    }

    @Test
    fun parseAccountInfo_OptionalFieldsNotSet() {

        // when
        val result = underTest.parse("HIUPD:74:6:3+9999999999::280:10070000++9999999999+++anonym")

        // then
        assertSuccessfullyParsedSegment(result, InstituteSegmentId.AccountInfo, 74, 6, 3)

        result.getFirstSegmentById<AccountInfo>(InstituteSegmentId.AccountInfo)?.let { segment ->
            assertThat(segment.accountIdentifier).isEqualTo("9999999999")
            assertThat(segment.subAccountAttribute).isNull()
            assertThat(segment.bankCountryCode).isEqualTo(280)
            assertThat(segment.bankCode).isEqualTo("10070000")
            assertThat(segment.iban).isNull()
            assertThat(segment.customerId).isEqualTo("9999999999")
            assertThat(segment.accountType).isNull()
            assertThat(segment.currency).isNull()
            assertThat(segment.accountHolderName1).isEqualTo("anonym")
            assertThat(segment.accountHolderName2).isNull()
            assertThat(segment.productName).isNull()
            assertThat(segment.accountLimit).isNull()
            assertThat(segment.allowedJobNames).isEmpty()
            assertThat(segment.extension).isNull()
        }
        ?: run { Assert.fail("No segment of type AccountInfo found in ${result.receivedSegments}") }
    }

    @Test
    fun parseSepaAccountInfo() {

        // when
        val result = underTest.parse("HISPA:5:1:3+J:$Iban:$Bic:$CustomerId::280:$BankCode")

        // then
        assertSuccessfullyParsedSegment(result, InstituteSegmentId.SepaAccountInfo, 5, 1, 3)

        result.getFirstSegmentById<SepaAccountInfo>(InstituteSegmentId.SepaAccountInfo)?.let { segment ->
            assertThat(segment.account.isSepaAccount).isTrue()
            assertThat(segment.account.iban).isEqualTo(Iban)
            assertThat(segment.account.bic).isEqualTo(Bic)
            assertThat(segment.account.accountIdentifier).isEqualTo(CustomerId)
            assertThat(segment.account.subAccountAttribute).isNull()
            assertThat(segment.account.bankInfo.bankCountryCode).isEqualTo(BankCountryCode)
            assertThat(segment.account.bankInfo.bankCode).isEqualTo(BankCode)
        }
        ?: run { Assert.fail("No segment of type SepaAccountInfo found in ${result.receivedSegments}") }
    }

    @Test
    fun parseSepaAccountInfoParameters() {

        // when
        val result = underTest.parse("HISPAS:147:1:3+1+1+1+J:N:N:sepade.pain.001.001.02.xsd:sepade.pain.001.002.02.xsd:sepade.pain.001.002.03.xsd:sepade.pain.008.002.02.xsd:urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.001.003.03:urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.008.003.02:urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.001.001.03:urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.008.001.02'" +
                "HISPAS:148:2:3+1+1+1+J:N:N:N:sepade.pain.001.001.02.xsd:sepade.pain.001.002.02.xsd:sepade.pain.001.002.03.xsd:sepade.pain.008.002.02.xsd:urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.001.003.03:urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.008.003.02:urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.001.001.03:urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.008.001.02'")

        // then
        assertCouldParseResponse(result)

        val sepaAccountInfoParameters = result.getSegmentsById<SepaAccountInfoParameters>(InstituteSegmentId.SepaAccountInfoParameters)
        assertThat(sepaAccountInfoParameters).hasSize(2)

        assertCouldParseJobParametersSegment(sepaAccountInfoParameters[0], InstituteSegmentId.SepaAccountInfoParameters, 147, 1, 3, "HKSPA", 1, 1, 1)
        assertCouldParseJobParametersSegment(sepaAccountInfoParameters[1], InstituteSegmentId.SepaAccountInfoParameters, 148, 2, 3, "HKSPA", 1, 1, 1)

        for (segment in sepaAccountInfoParameters) {
            assertThat(segment.retrieveSingleAccountAllowed).isTrue()
            assertThat(segment.nationalAccountRelationshipAllowed).isFalse()
            assertThat(segment.structuredUsageAllowed).isFalse()
            assertThat(segment.settingMaxAllowedEntriesAllowed).isFalse()
            assertThat(segment.countReservedUsageLength).isEqualTo(SepaAccountInfoParameters.CountReservedUsageLengthNotSet)
            assertThat(segment.supportedSepaFormats).containsExactlyInAnyOrder(
                "sepade.pain.001.001.02.xsd",
                "sepade.pain.001.002.02.xsd",
                "sepade.pain.001.002.03.xsd",
                "sepade.pain.008.002.02.xsd",
                "urn:iso:std:iso:20022:tech:xsd:pain.001.003.03",
                "urn:iso:std:iso:20022:tech:xsd:pain.008.003.02",
                "urn:iso:std:iso:20022:tech:xsd:pain.001.001.03",
                "urn:iso:std:iso:20022:tech:xsd:pain.008.001.02"
            )
        }
    }

    @Test
    fun parseSupportedJobs() {

        // when
        val result = underTest.parse(
                "HICSUS:9:1:4+1+1+1+INTC;CORT:urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.001.003.03:urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.001.001.03'" +
                "HIPKBS:10:1:4+1+1+1+N'" +
                "HIPKAS:11:1:4+1+1+1+N:N:N:N:N:N:N:N:N:N:N'" +
                "HIPCRS:12:1:4+1+1+1'" +
                "HIPWES:13:1:4+1+1+1'" +
                "HIPWLS:14:1:4+1+1+1+N:J:J'" +
                "HIPWBS:15:1:4+1+1+1+N:N'" +
                "HIPWAS:16:1:4+1+1+1'" +
                "HIIPZS:17:1:4+1+1+1+;:urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.001.001.03'" +
                "HIIPSS:18:1:4+1+1+1+10:urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.001.001.03'" +
                "HIAUBS:90:5:4+1+1+1'" +
                "HIBMES:91:1:4+1+1+1+2:28:2:28:1000:J:N'" +
                "HIBSES:92:1:4+1+1+1+2:28:2:28'" +
                "HICAZS:93:1:4+1+1+1+450:N:N:urn?:iso?:std?:iso?:20022?:tech?:xsd?:camt.052.001.02'" +
                "HICCMS:94:1:4+1+1+1+1000:J:N'" +
                "HICCSS:95:1:4+1+1+1'" +
                "HICDBS:96:1:4+3+1+1+N'" +
                "HICDES:97:1:4+3+1+1+4:0:9999:0102030612:01020304050607080910111213141516171819202122232425262728293099'" +
                "HICDLS:98:1:4+3+1+1+0:9999:J:J'" +
                "HICDNS:99:1:4+3+1+1+0:0:9999:J:J:J:J:J:N:J:J:J:0102030612:01020304050607080910111213141516171819202122232425262728293099'" +
                "HICDUS:100:1:4+3+1+1+1:0:9999:1:N:N'" +
                "HICMBS:101:1:4+1+1+1+N:J'" +
                "HICMES:102:1:4+1+1+1+1:360:1000:J:N'" +
                "HICMLS:103:1:4+1+1+1'" +
                "HICSAS:104:1:4+1+1+1+1:360'" +
                "HICSBS:105:1:4+1+1+1+N:J'" +
                "HICSES:106:1:4+1+1+1+1:360'" +
                "HICSLS:107:1:4+1+1+1+J'" +
                "HICUBS:108:1:4+3+1+1+J'" +
                "HICUMS:109:1:4+3+1+1+;:sepade.pain.001.001.02.xsd:sepade.pain.001.002.02.xsd:sepade.pain.001.002.03.xsd:urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.001.003.03:urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.001.001.03'" +
                "HIDMCS:110:1:4+1+1+1+1000:J:N:2:28:2:28::urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.008.003.02'" +
                "HIDMES:111:1:4+1+1+1+2:28:2:28:1000:J:N'" +
                "HIDSBS:112:1:4+3+1+1+J:J:56'" +
                "HIDSCS:113:1:4+1+1+1+2:28:2:28::urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.008.003.02'" +
                "HIDSES:114:1:4+1+1+1+2:28:2:28'" +
                "HIDSWS:115:1:4+1+1+1+N'" +
                "HIEKAS:116:2:4+1+1+1+J:J:N:1'" +
                "HIEKAS:117:3:4+1+1+1+J:J:N:1'" +
                "HIEKPS:118:1:4+1+1+1+J:J:N'" +
                "HIFGBS:119:2:4+3+1'" +
                "HIFGBS:120:3:4+3+1'" +
                "HIFRDS:121:1:4+1+1'" +
                "HIFRDS:122:4:4+1+1+1+N:J:N:0:Kreditinstitut:1:DekaBank'" +
                "HIKAZS:123:4:4+1+1+360:J'" +
                "HIKAZS:124:5:4+1+1+360:J:N'" +
                "HIKDMS:125:2:4+3+0+2048'" +
                "HIKDMS:126:3:4+3+0+2048'" +
                "HIKDMS:127:4:4+3+0+2048'" +
                "HIKIFS:128:1:4+1+1'" +
                "HIKIFS:129:4:4+1+1+1+J:J'" +
                "HIKIFS:130:5:4+1+1+1+J:J'" +
                "HIKIFS:131:6:4+1+1+1+J:J'" +
                "HIMTAS:132:1:4+1+1+1+N'" +
                "HIMTAS:133:2:4+1+1+1+N:J'" +
                "HIMTFS:134:1:4+1+1+1'" +
                "HIMTRS:135:1:4+1+1+1+N'" +
                "HIMTRS:136:2:4+1+1+1+N:J'" +
                "HINEAS:137:1:4+1+1+1:2:3:4'" +
                "HINEZS:138:3:4+1+1+1+N:N:4:N:N:::N:J'" +
                "HIWFOS:139:3:4+1+1+1+N:4:N:N:N::::MAKT:N:J'" +
                "HIWPOS:140:5:4+1+1+1+0:N:4:N:N::::9999999,99:EUR:STOP;STLI;LMTO;MAKT;OCOO;TRST:BUYI;SELL;AUCT;CONT;ALNO;DIHA:GDAY;GTMO;GTHD;GTCA;IOCA;OPEN;CLOS;FIKI:N:J'" +
                "HIWSDS:141:5:4+3+1+1+J:A;Inland DAX:B;Inland Sonstige:C;Ausland Europa:D;Ausland Sonstige'" +
                "HIFPOS:142:3:4+1+1+1+N:4:N:N:::N:J'" +
                "HIPAES:143:1:4+1+1+1'" +
                "HIPPDS:144:1:4+1+1+1+1:Telekom:Xtra-Card:N:::15;30;50:2:Vodafone:CallYa:N:::15;25;50:3:E-Plus:Free and easy:N:::15;20;30:4:O2:Loop:N:::15;20;30:5:congstar:congstar:N:::15;30;50:6:blau:blau:N:::15;20;30:8:o.tel.o:o.tel.o:N:::9;19;29:9:SIM Guthaben:SIM Guthaben:N:::15;30;50'" +
                "HIQTGS:145:1:4+1+1+1'" +
                "HISALS:146:3:4+1+1'" +
                "HISALS:147:4:4+1+1'" +
                "HISALS:148:5:4+1+1'" +
                "HISPAS:149:1:4+1+1+1+J:N:N:sepade.pain.001.001.02.xsd:sepade.pain.001.002.02.xsd:sepade.pain.001.002.03.xsd:sepade.pain.008.002.02.xsd:urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.001.003.03:urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.008.003.02:urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.001.001.03:urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.008.001.02'" +
                "HISPAS:150:2:4+1+1+1+J:N:N:N:sepade.pain.001.001.02.xsd:sepade.pain.001.002.02.xsd:sepade.pain.001.002.03.xsd:sepade.pain.008.002.02.xsd:urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.001.003.03:urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.008.003.02:urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.001.001.03:urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.008.001.02'" +
                "HITABS:151:2:4+1+1+1'" +
                "HITABS:152:3:4+1+1+1'" +
                "HITABS:153:4:4+1+1+1'" +
                "HITAUS:154:1:4+1+1+1+N:N:J'" +
                "HITAZS:155:1:4+1+1+1'" +
                "HITAZS:156:2:4+1+1+1'" +
                "HITMLS:157:1:4+1+1+1'" +
                "HITSYS:158:1:4+1+1+1+N:N'" +
                "HIWDUS:159:4:4+3+1+999'" +
                "HIWFPS:160:2:4+3+1+RENTEN:INVESTMENTFONDS:GENUSSSCHEINE:SPARBRIEFE:UNTERNEHMENSANLEIHEN:EMERGING MARKET ANLEIHEN:STRUKTURIERTE ANLEIHEN:ZERTIFIKATE:AKTIEN:OPTIONSSCHEINE:ALLE ANGEBOTE EIGENES INSTITUT:ALLE ANGEBOTE UEBERGEORD. INSTITUTE'" +
                "HIWOAS:161:2:4+1+1+J:STOP;SLOS;LMTO;MAKT:J:J:GDAY;GTMO;GTHD:J:1:N:N:N:9999999,99:EUR'" +
                "HIWOAS:162:4:4+1+1+1+J:STOP;STLI;LMTO;MAKT;OCOO;TRST:J:J:J:J:J:GDAY;GTMO;GTHD:J:1:N:N:N:9999999,99:EUR'" +
                "HIWPDS:163:3:4+3+1+J'" +
                "HIWPDS:164:5:4+1+1+J:N:N'" +
                "HIWPKS:165:1:4+3+0'" +
                "HIWPRS:166:1:4+3+1+J:J:N:N::Aktien:Festverzinsliche Wertpapiere:Fonds:Fremdw�hrungsanleihen:Genussscheine:Indexzertifikate:Optionsscheine:Wandel- und Optionsanleihen cum'" +
                "HIWPSS:167:1:4+3+1+J'" +
                "HIWSOS:168:4:4+3+1+1+J:J:90:1:2:3:4:5:6:7:8:9:10:11'" +
                "HIWSOS:169:5:4+3+1+1+J:J:90:1:2:3:4:5:6:7:8:9:10:11:12:13:14:15:16:17'" +
                "HITANS:170:6:4+1+1+1+J:N:0:910:2:HHD1.3.0:::chipTAN manuell:6:1:TAN-Nummer:3:J:2:N:0:0:N:N:00:0:N:1:911:2:HHD1.3.2OPT:HHDOPT1:1.3.2:chipTAN optisch:6:1:TAN-Nummer:3:J:2:N:0:0:N:N:00:0:N:1:912:2:HHD1.3.2USB:HHDUSB1:1.3.2:chipTAN-USB:6:1:TAN-Nummer:3:J:2:N:0:0:N:N:00:0:N:1:913:2:Q1S:Secoder_UC:1.2.0:chipTAN-QR:6:1:TAN-Nummer:3:J:2:N:0:0:N:N:00:0:N:1:920:2:smsTAN:::smsTAN:6:1:TAN-Nummer:3:J:2:N:0:0:N:N:00:2:N:5:921:2:pushTAN:::pushTAN:6:1:TAN-Nummer:3:J:2:N:0:0:N:N:00:2:N:2:900:2:iTAN:::iTAN:6:1:TAN-Nummer:3:J:2:N:0:0:N:N:00:0:N:0'" +
                "HIPINS:171:1:4+1+1+0+5:5:6:USERID:CUSTID:HKAUB:J:HKBME:J:HKBSE:J:HKCAZ:J:HKCCM:J:HKCCS:J:HKCDB:N:HKCDE:J:HKCDL:J:HKCDN:J:HKCDU:J:HKCMB:N:HKCME:J:HKCML:J:HKCSA:J:HKCSB:N:HKCSE:J:HKCSL:J:HKCSU:J:HKIPZ:J:HKIPS:N:HKPKB:N:HKPKA:J:HKPWE:J:HKPWL:N:HKPWB:N:HKPWA:J:HKCUB:N:HKCUM:J:HKDMC:J:HKDME:J:HKDSB:N:HKDSC:J:HKDSE:J:HKDSW:J:HKEKA:N:HKEKP:N:HKFGB:N:HKFRD:N:HKKAZ:J:HKKDM:J:HKKIF:J:HKMTA:J:HKMTF:N:HKMTR:J:HKNEA:N:HKNEZ:J:HKWFO:J:HKWPO:J:HKFPO:J:HKWSD:N:HKPAE:J:HKPPD:J:HKQTG:N:HKSAL:J:HKSPA:N:HKTAB:N:HKTAU:N:HKTAZ:N:HKTML:N:HKTSY:N:HKUTA:N:HKWDU:N:HKWFP:N:HKWOA:J:HKWPD:N:HKWPK:N:HKWPR:N:HKWPS:J:HKWSO:N:HKTAN:N:DKBKD:N:DKBKU:N:DKBUM:N:DKFDA:N:DKPAE:N:DKPSA:J:DKPSP:N:DKTLA:N:DKTLF:J:DKTSP:N:DKWAP:N:DKALE:J:DKALL:J:DKALN:J:DKANA:J:DKANL:J:DKBAZ:N:DKBVA:J:DKBVB:J:DKBVD:N:DKBVK:N:DKBVP:J:DKBVR:J:DKBVS:N:DKDFA:N:DKDFB:N:DKDFC:J:DKDFD:N:DKDFL:J:DKDFU:N:DKDIH:J:DKDFS:N:DKDDI:N:DKDFO:J:DKDFP:J:DKDPF:N:DKDFE:J:DKDEF:N:DKDOF:N:DKFAF:N:DKGBA:J:DKGBS:J:DKKAU:N:DKKKA:N:DKKKS:N:DKKKU:N:DKKSB:N:DKKSP:J:DKQUO:N:DKQUT:N:DKVVD:N:DKVVU:N:DKWDG:N:DKWGV:N:DKWLV:N:DKNZP:N:DKFOP:N:DKFPO:N:DKWOP:N:DKWVB:N:DKZDF:J:DKZDL:J:DKWOK:N:DKWDH:N:DKBVE:J:DKPTZ:N:DKEEA:N'")


        // then

        assertThat(result.successful).isTrue()
        assertThat(result.receivedSegments).hasSize(92)

        for (segment in result.receivedSegments) {
            assertThat(segment is JobParameters).describedAs("$segment should be of type JobParameters").isTrue()
        }
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
            assertThat(segment.securityClass).isEqualTo(1)
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

    @Test
    fun parseTanInfo_CountSupportedActiveTanMediaIsBlank() {

        // when
        val result = underTest.parse("HITANS:27:6:3+1+1+1+N:N:0:901:2:TechnicalId901:::mobileTAN-Verfahren:6:1:Freigabe durch mobileTAN:1:N:4:N:0:0:N:N:00:0:N::902:2:MS1.0.0:::photoTAN-Verfahren:6:1:Freigabe durch photoTAN:1:N:4:N:0:0:N:N:00:0:N:'")

        // then
        assertSuccessfullyParsedSegment(result, InstituteSegmentId.TanInfo, 27, 6, 3)

        result.getFirstSegmentById<TanInfo>(InstituteSegmentId.TanInfo)?.let { segment ->
            assertThat(segment.maxCountJobs).isEqualTo(1)
            assertThat(segment.minimumCountSignatures).isEqualTo(1)
            assertThat(segment.securityClass).isEqualTo(1)
            assertThat(segment.tanProcedureParameters.oneStepProcedureAllowed).isFalse()
            assertThat(segment.tanProcedureParameters.moreThanOneTanDependentJobPerMessageAllowed).isFalse()
            assertThat(segment.tanProcedureParameters.jobHashValue).isEqualTo("0")

            assertThat(segment.tanProcedureParameters.procedureParameters).hasSize(2)
            assertThat(segment.tanProcedureParameters.procedureParameters).extracting("procedureName")
                .containsExactlyInAnyOrder("mobileTAN-Verfahren", "photoTAN-Verfahren")
        }
        ?: run { Assert.fail("No segment of type TanInfo found in ${result.receivedSegments}") }
    }

    @Test
    fun parseTanInfo_SmsAbbuchungskontoErforderlichAnduftraggeberkontoErforderlichAreEncodedAsBoolean() {

        // when
        val result = underTest.parse("HITANS:56:6:3+1+1+1+N:N:0:901:2:CR#1:::SMS-TAN:6:1:SMS-TAN:256:J:2:J:J:N:N:N:01:1:N:1:904:2:CR#5 - 1.4:HHDOPT1:1.4:chipTAN comfort:6:1:chipTAN comfort:2048:N:1:N:0:0:N:N:01:0:N:1:905:2:CR#6 - 1.4:HHD:1.4:chipTAN comfort manuell:6:1:chipTAN comfort manuell:2048:N:1:N:0:0:N:N:01:0:N:0:906:2:CR#8 - MS1.0:::BV AppTAN:6:1:BV AppTAN:2048:N:1:N:0:0:N:N:01:0:N:0:907:2:CR#7:::PhotoTAN:7:1:PhotoTAN:2048:N:1:N:N:N:N:J:01:1:N:0'")

        // then
        assertSuccessfullyParsedSegment(result, InstituteSegmentId.TanInfo, 56, 6, 3)

        result.getFirstSegmentById<TanInfo>(InstituteSegmentId.TanInfo)?.let { segment ->
            assertThat(segment.maxCountJobs).isEqualTo(1)
            assertThat(segment.minimumCountSignatures).isEqualTo(1)
            assertThat(segment.securityClass).isEqualTo(1)
            assertThat(segment.tanProcedureParameters.oneStepProcedureAllowed).isFalse()
            assertThat(segment.tanProcedureParameters.moreThanOneTanDependentJobPerMessageAllowed).isFalse()
            assertThat(segment.tanProcedureParameters.jobHashValue).isEqualTo("0")

            assertThat(segment.tanProcedureParameters.procedureParameters).hasSize(5)
            assertThat(segment.tanProcedureParameters.procedureParameters).extracting("procedureName")
                .containsExactlyInAnyOrder("SMS-TAN", "chipTAN comfort", "chipTAN comfort manuell", "BV AppTAN", "PhotoTAN")
        }
        ?: run { Assert.fail("No segment of type TanInfo found in ${result.receivedSegments}") }
    }

    @Test
    fun parseTanInfo7_NewAppBasedProcedure() {

        // when
        val result = underTest.parse("HITANS:33:7:3+1+1+0+N:N:1:900:2:iTAN:::iTAN:6:1:Challenge:999:J:1:N:0:0:N:N:00:0:N:1:901:2:mTAN:mobileTAN::mobile TAN:6:1:SMS:999:J:1:N:0:0:N:N:00:1:N:1:906:S:appTAN:appTAN::App-basiertes Verfahren:0:2:Hinweis:120:J:2:N:0:0:N:N:00:1:N:1:907:2:HHD1.4OPT:HHDOPT1:1.4:chipTAN 1.4:6:1:Challenge:999:J:1:N:0:0:N:J:00:1:N:1:908:2:HHD1.4:HHD:1.4:chipTAN 1.4 manuell:6:1:Challenge:999:J:1:N:0:0:N:J:00:1:N:1:909:2:noTAN:::Vorlagen und Informationen:6:1:Challenge:999:J:1:N:0:0:N:J:00:0:N:1'")

        // then
        assertSuccessfullyParsedSegment(result, InstituteSegmentId.TanInfo, 33, 7, 3)

        result.getFirstSegmentById<TanInfo>(InstituteSegmentId.TanInfo)?.let { segment ->
            assertThat(segment.maxCountJobs).isEqualTo(1)
            assertThat(segment.minimumCountSignatures).isEqualTo(1)
            assertThat(segment.securityClass).isEqualTo(0)
            assertThat(segment.tanProcedureParameters.oneStepProcedureAllowed).isFalse()
            assertThat(segment.tanProcedureParameters.moreThanOneTanDependentJobPerMessageAllowed).isFalse()
            assertThat(segment.tanProcedureParameters.jobHashValue).isEqualTo("1")

            assertThat(segment.tanProcedureParameters.procedureParameters).hasSize(6)
            assertThat(segment.tanProcedureParameters.procedureParameters).extracting("procedureName")
                .containsExactlyInAnyOrder("iTAN", "mobile TAN", "App-basiertes Verfahren", "chipTAN 1.4",
                    "chipTAN 1.4 manuell", "Vorlagen und Informationen")
        }
        ?: run { Assert.fail("No segment of type TanInfo found in ${result.receivedSegments}") }
    }

    @Test
    fun parseTanResponse_NoStrongAuthenticationRequired() {

        // when
        val result = underTest.parse("HITAN:6:6:5+4++noref+nochallenge")

        // then
        assertSuccessfullyParsedSegment(result, InstituteSegmentId.Tan, 6, 6, 5)

        assertThat(result.isStrongAuthenticationRequired).isFalse()

        result.getFirstSegmentById<TanResponse>(InstituteSegmentId.Tan)?.let { segment ->
            assertThat(segment.tanProcess).isEqualTo(TanProcess.TanProcess4)
            assertThat(segment.jobHashValue).isNull()
            assertThat(segment.jobReference).isEqualTo(TanResponse.NoJobReferenceResponse)
            assertThat(segment.challenge).isEqualTo(TanResponse.NoChallengeResponse)
            assertThat(segment.challengeHHD_UC).isNull()
            assertThat(segment.validityDateTimeForChallenge).isNull()
            assertThat(segment.tanMediaIdentifier).isNull()
        }
        ?: run { Assert.fail("No segment of type TanResponse found in ${result.receivedSegments}") }
    }

    @Test
    fun parseTanResponse_StrongAuthenticationRequired() {

        // given
        val jobReference = "4937-10-13-02.30.03.700259"
        val challenge = "Sie möchten eine \"Umsatzabfrage\" freigeben?: Bitte bestätigen Sie den \"Startcode 80085335\" mit der Taste \"OK\"."
        val challengeHHD_UC = "100880085335"
        val tanMediaIdentifier = "Kartennummer ******0892"

        // when
        val result = underTest.parse("'HITAN:5:6:4+4++$jobReference+$challenge+@12@$challengeHHD_UC++$tanMediaIdentifier'")

        // then
        assertSuccessfullyParsedSegment(result, InstituteSegmentId.Tan, 5, 6, 4)

        assertThat(result.isStrongAuthenticationRequired).isTrue()

        result.getFirstSegmentById<TanResponse>(InstituteSegmentId.Tan)?.let { segment ->
            assertThat(segment.tanProcess).isEqualTo(TanProcess.TanProcess4)
            assertThat(segment.jobHashValue).isNull()
            assertThat(segment.jobReference).isEqualTo(jobReference)
            assertThat(segment.challenge).isEqualTo(unmaskString(challenge))
            assertThat(segment.challengeHHD_UC).isEqualTo(challengeHHD_UC)
            assertThat(segment.validityDateTimeForChallenge).isNull()
            assertThat(segment.tanMediaIdentifier).isEqualTo(tanMediaIdentifier)
        }
        ?: run { Assert.fail("No segment of type TanResponse found in ${result.receivedSegments}") }
    }


    @Test
    fun parseTanMediaListResponse() {

        // given
        val oldCardNumber = "5109972878"
        val cardSequenceNumber = "5200310149"
        val mediaName = "EC-Card (Debitkarte)"

        // when
        val result = underTest.parse("HITAB:5:4:3+1+G:3:$oldCardNumber:$cardSequenceNumber:::::::::$mediaName::::::::+G:2:$cardSequenceNumber::::::::::$mediaName::::::::'")

        // then
        assertSuccessfullyParsedSegment(result, InstituteSegmentId.TanMediaList, 5, 4, 3)

        assertThat(result.isStrongAuthenticationRequired).isFalse()

        result.getFirstSegmentById<TanMediaList>(InstituteSegmentId.TanMediaList)?.let { segment ->
            assertThat(segment.usageOption).isEqualTo(TanEinsatzOption.KundeKannGenauEinMediumZuEinerZeitNutzen)
            assertThat(segment.tanMedia).containsOnly(
                TanGeneratorTanMedium(TanMediumKlasse.TanGenerator, TanMediumStatus.AktivFolgekarte, oldCardNumber, cardSequenceNumber, null, null, null, mediaName),
                TanGeneratorTanMedium(TanMediumKlasse.TanGenerator, TanMediumStatus.Verfuegbar, cardSequenceNumber, null, null, null, null, mediaName)
            )
        }
        ?: run { Assert.fail("No segment of type TanMediaList found in ${result.receivedSegments}") }
    }



    @Test
    fun parseChangeTanMediaParametersVersion1() {

        // when
        val result = underTest.parse("HITAUS:64:1:3+1+1+1+J:N:J:'")

        // then
        assertSuccessfullyParsedSegment(result, InstituteSegmentId.ChangeTanMediaParameters, 64, 1, 3)

        result.getFirstSegmentById<ChangeTanMediaParameters>(InstituteSegmentId.ChangeTanMediaParameters)?.let { segment ->
            assertThat(segment.maxCountJobs).isEqualTo(1)
            assertThat(segment.minimumCountSignatures).isEqualTo(1)
            assertThat(segment.securityClass).isEqualTo(1)

            assertThat(segment.enteringTanListNumberRequired).isTrue()
            assertThat(segment.enteringCardSequenceNumberRequired).isFalse()
            assertThat(segment.enteringAtcAndTanRequired).isTrue()
            assertThat(segment.enteringCardTypeAllowed).isFalse()
            assertThat(segment.accountInfoRequired).isFalse()
            assertThat(segment.allowedCardTypes).isEmpty()
        }
        ?: run { Assert.fail("No segment of type ChangeTanMediaParameters found in ${result.receivedSegments}") }
    }

    @Test
    fun parseChangeTanMediaParametersVersion2() {

        // when
        val result = underTest.parse("HITAUS:64:2:3+1+1+1+N:J:N:J:11:13:15:17'")

        // then
        assertSuccessfullyParsedSegment(result, InstituteSegmentId.ChangeTanMediaParameters, 64, 2, 3)

        result.getFirstSegmentById<ChangeTanMediaParameters>(InstituteSegmentId.ChangeTanMediaParameters)?.let { segment ->
            assertThat(segment.maxCountJobs).isEqualTo(1)
            assertThat(segment.minimumCountSignatures).isEqualTo(1)
            assertThat(segment.securityClass).isEqualTo(1)

            assertThat(segment.enteringTanListNumberRequired).isFalse()
            assertThat(segment.enteringCardSequenceNumberRequired).isTrue()
            assertThat(segment.enteringAtcAndTanRequired).isFalse()
            assertThat(segment.enteringCardTypeAllowed).isTrue()
            assertThat(segment.accountInfoRequired).isFalse()
            assertThat(segment.allowedCardTypes).containsExactlyInAnyOrder(11, 13, 15, 17)
        }
        ?: run { Assert.fail("No segment of type ChangeTanMediaParameters found in ${result.receivedSegments}") }
    }

    @Test
    fun parseChangeTanMediaParametersVersion3() {

        // when
        val result = underTest.parse("HITAUS:64:3:3+1+1+1+N:J:N:J:J:11:13:15:17'")

        // then
        assertSuccessfullyParsedSegment(result, InstituteSegmentId.ChangeTanMediaParameters, 64, 3, 3)

        result.getFirstSegmentById<ChangeTanMediaParameters>(InstituteSegmentId.ChangeTanMediaParameters)?.let { segment ->
            assertThat(segment.maxCountJobs).isEqualTo(1)
            assertThat(segment.minimumCountSignatures).isEqualTo(1)
            assertThat(segment.securityClass).isEqualTo(1)

            assertThat(segment.enteringTanListNumberRequired).isFalse()
            assertThat(segment.enteringCardSequenceNumberRequired).isTrue()
            assertThat(segment.enteringAtcAndTanRequired).isFalse()
            assertThat(segment.enteringCardTypeAllowed).isTrue()
            assertThat(segment.accountInfoRequired).isTrue()
            assertThat(segment.allowedCardTypes).containsExactlyInAnyOrder(11, 13, 15, 17)
        }
        ?: run { Assert.fail("No segment of type ChangeTanMediaParameters found in ${result.receivedSegments}") }
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


    private fun assertSuccessfullyParsedSegment(result: Response, segmentId: ISegmentId, segmentNumber: Int,
                                                segmentVersion: Int, referenceSegmentNumber: Int? = null) {

        assertCouldParseResponse(result)

        assertCouldParseSegment(result, segmentId, segmentNumber, segmentVersion, referenceSegmentNumber)
    }

    private fun assertCouldParseResponse(result: Response) {
        assertThat(result.successful).isTrue()
        assertThat(result.responseContainsErrors).isFalse()
        assertThat(result.exception).isNull()
        assertThat(result.errorsToShowToUser).isEmpty()
        assertThat(result.receivedResponse).isNotNull()
    }

    private fun assertCouldParseSegment(result: Response, segmentId: ISegmentId, segmentNumber: Int,
                                        segmentVersion: Int, referenceSegmentNumber: Int? = null) {

        val segment = result.getFirstSegmentById<ReceivedSegment>(segmentId)

        assertCouldParseSegment(segment, segmentId, segmentNumber, segmentVersion, referenceSegmentNumber)
    }

    private fun assertCouldParseSegment(segment: ReceivedSegment?, segmentId: ISegmentId, segmentNumber: Int, 
                                        segmentVersion: Int, referenceSegmentNumber: Int?) {
        
        assertThat(segment).isNotNull()

        segment?.let {
            assertThat(segment.segmentId).isEqualTo(segmentId.id)
            assertThat(segment.segmentNumber).isEqualTo(segmentNumber)
            assertThat(segment.segmentVersion).isEqualTo(segmentVersion)
            assertThat(segment.referenceSegmentNumber).isEqualTo(referenceSegmentNumber)
        }
    }

    private fun assertCouldParseJobParametersSegment(segment: JobParameters?, segmentId: ISegmentId, segmentNumber: Int, 
                                                     segmentVersion: Int, referenceSegmentNumber: Int?, jobName: String,
                                                     maxCountJobs: Int, minimumCountSignatures: Int, securityClass: Int?) {
        
        assertCouldParseSegment(segment, segmentId, segmentNumber, segmentVersion, referenceSegmentNumber)

        segment?.let {
            assertThat(segment.jobName).isEqualTo(jobName)
            assertThat(segment.maxCountJobs).isEqualTo(maxCountJobs)
            assertThat(segment.minimumCountSignatures).isEqualTo(minimumCountSignatures)
            assertThat(segment.securityClass).isEqualTo(securityClass)
        }
    }

}