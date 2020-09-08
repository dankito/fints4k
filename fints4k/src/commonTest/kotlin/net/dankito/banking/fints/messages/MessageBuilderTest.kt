package net.dankito.banking.fints.messages

import ch.tutteli.atrium.api.fluent.en_GB.notToBeNull
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import net.dankito.banking.fints.FinTsTestBase
import net.dankito.banking.fints.model.*
import net.dankito.banking.fints.response.segments.AccountType
import net.dankito.banking.fints.response.segments.JobParameters
import net.dankito.banking.fints.util.FinTsUtils
import net.dankito.utils.multiplatform.Date
import net.dankito.utils.multiplatform.Month
import kotlin.test.AfterTest
import kotlin.test.Test


class MessageBuilderTest : FinTsTestBase() {

    private val underTest = object : MessageBuilder(utils = object : FinTsUtils() {
        override fun formatDate(date: Date): String {
            return Date.toString()
        }

        override fun formatTime(time: Date): String {
            return Time.toString()
        }
    }) {

        override fun createControlReference(): String {
            return ControlReference
        }

    }


    @AfterTest
    fun tearDown() {
        Bank.supportedJobs = listOf()
    }


    @Test
    fun createAnonymousDialogInitMessage() {

        // given
        val dialogContext = DialogContext(Bank, Product)

        // when
        val result = underTest.createAnonymousDialogInitMessage(dialogContext).createdMessage

        // then
        expect(result).toBe(
            "HNHBK:1:3+000000000125+300+0+1'" +
            "HKIDN:2:2+280:12345678+9999999999+0+0'" +
            "HKVVB:3:3+0+0+0+$ProductName+$ProductVersion'" +
            "HNHBS:4:1+1'"
        )
    }

    @Test
    fun createAnonymousDialogEndMessage() {

        // given
        val dialogId = createDialogId()
        val dialogContext = DialogContext(Bank, Product, false, null, dialogId)

        // when
        val result = underTest.createAnonymousDialogEndMessage(dialogContext).createdMessage ?: ""

        // then
        expect(normalizeBinaryData(result)).toBe(normalizeBinaryData(
            "HNHBK:1:3+000000000086+300+$dialogId+1'" +
            "HKEND:2:1+$dialogId'" +
            "HNHBS:3:1+1'"
        ))
    }


    @Test
    fun createDialogInitMessage() {

        // given
        val dialogContext = DialogContext(Bank, Product)

        // when
        val result = underTest.createSynchronizeCustomerSystemIdMessage(dialogContext).createdMessage ?: ""

        // then
        expect(normalizeBinaryData(result)).toBe(normalizeBinaryData(
            "HNHBK:1:3+000000000398+300+0+1'" +
            "HNVSK:998:3+PIN:2+998+1+1::0+1:$Date:$Time+2:16:14:@8@        :5:1+280:$BankCode:$CustomerId:V:0:0+0'" +
            "HNVSD:999:1+@234@" + "HNSHK:2:4+PIN:2+${SecurityFunction.code}+$ControlReference+1+1+1::0+1+1:$Date:$Time+1:999:1+6:10:16+280:$BankCode:$CustomerId:S:0:0'" +
            "HKIDN:3:2+280:$BankCode+$CustomerId+0+0'" +
            "HKVVB:4:3+0+0+${Language.code}+$ProductName+$ProductVersion'" +
            "HKTAN:5:6+4+HKIDN'" +
            "HKSYN:6:3+0'" +
            "HNSHA:7:2+$ControlReference++$Pin''" +
            "HNHBS:8:1+1'"
        ))
    }

    @Test
    fun createDialogEndMessage() {

        // given
        val dialogId = createDialogId()
        val dialogContext = DialogContext(Bank, Product, false, null, dialogId)

        // when
        val result = underTest.createDialogEndMessage(dialogContext).createdMessage ?: ""

        // then
        expect(normalizeBinaryData(result)).toBe(normalizeBinaryData(
            "HNHBK:1:3+000000000329+300+$dialogId+1'" +
            "HNVSK:998:3+PIN:2+998+1+1::0+1:$Date:$Time+2:16:14:@8@        :5:1+280:$BankCode:$CustomerId:V:0:0+0'" +
            "HNVSD:999:1+@165@" + "HNSHK:2:4+PIN:2+${SecurityFunction.code}+$ControlReference+1+1+1::0+1+1:$Date:$Time+1:999:1+6:10:16+280:$BankCode:$CustomerId:S:0:0'" +
            "HKEND:3:1+$dialogId'" +
            "HNSHA:4:2+$ControlReference++$Pin''" +
            "HNHBS:5:1+1'"
        ))
    }


    @Test
    fun createGetTransactionsMessage_JobIsNotAllowed() {

        // given
        val dialogContext = DialogContext(Bank, Product)

        // when
        val result = underTest.createGetTransactionsMessage(GetTransactionsParameter(), Account, dialogContext)

        // then
        expect(result.isJobAllowed).toBe(false)
    }

    @Test
    fun createGetTransactionsMessage_JobVersionIsNotSupported() {

        // given
        val getTransactionsJob = JobParameters("HKKAZ", 1, 1, null, "HKKAZ:73:5")
        val getTransactionsJobWithPreviousVersion = JobParameters("HKKAZ", 1, 1, null, "HKKAZ:72:4")
        Bank.supportedJobs = listOf(getTransactionsJob)
        val account = AccountData(CustomerId, null, BankCountryCode, BankCode, null, CustomerId, AccountType.Girokonto, "EUR", "", null, null, listOf(getTransactionsJob.jobName), listOf(getTransactionsJobWithPreviousVersion))
        Bank.addAccount(account)
        val dialogContext = DialogContext(Bank, Product)

        // when
        val result = underTest.createGetTransactionsMessage(GetTransactionsParameter(), account, dialogContext)

        // then
        expect(result.isJobAllowed).toBe(true)
        expect(result.isJobVersionSupported).toBe(false)
    }

    @Test
    fun createGetTransactionsMessage() {

        // given
        val getTransactionsJob = JobParameters("HKKAZ", 1, 1, null, "HKKAZ:73:5")
        Bank.supportedJobs = listOf(getTransactionsJob)
        val account = AccountData(CustomerId, null, BankCountryCode, BankCode, null, CustomerId, AccountType.Girokonto, "EUR", "", null, null, listOf(getTransactionsJob.jobName), listOf(getTransactionsJob))
        Bank.addAccount(account)
        val dialogContext = DialogContext(Bank, Product)

        val fromDate = Date(2019, Month.August, 6)
        val toDate = Date(2019, Month.October, 21)
        val maxCountEntries = 99

        // when
        val result = underTest.createGetTransactionsMessage(GetTransactionsParameter(false, fromDate, toDate, maxCountEntries), account, dialogContext)

        // then
        expect(result.createdMessage).notToBeNull()

        expect(normalizeBinaryData(result.createdMessage!!)).toBe(normalizeBinaryData(
            "HNHBK:1:3+000000000362+300+0+1'" +
            "HNVSK:998:3+PIN:2+998+1+1::0+1:$Date:$Time+2:16:14:@8@        :5:1+280:$BankCode:$CustomerId:V:0:0+0'" +
            "HNVSD:999:1+@198@" + "HNSHK:2:4+PIN:2+${SecurityFunction.code}+$ControlReference+1+1+1::0+1+1:$Date:$Time+1:999:1+6:10:16+280:$BankCode:$CustomerId:S:0:0'" +
            "HKKAZ:3:${getTransactionsJob.segmentVersion}+$CustomerId::280:$BankCode+N+${convertDate(fromDate)}+${convertDate(toDate)}+$maxCountEntries'" +
            "HKTAN:4:6+4+HKKAZ'" +
            "HNSHA:5:2+$ControlReference++$Pin''" +
            "HNHBS:6:1+1'"
        ))
    }

    @Test
    fun createGetTransactionsMessage_WithContinuationIdSet() {

        // given
        val getTransactionsJob = JobParameters("HKKAZ", 1, 1, null, "HKKAZ:73:5")
        Bank.supportedJobs = listOf(getTransactionsJob)
        val account = AccountData(CustomerId, null, BankCountryCode, BankCode, null, CustomerId, AccountType.Girokonto, "EUR", "", null, null, listOf(getTransactionsJob.jobName), listOf(getTransactionsJob))
        Bank.addAccount(account)
        val dialogContext = DialogContext(Bank, Product)

        val fromDate = Date(2019, Month.August, 6)
        val toDate = Date(2019, Month.October, 21)
        val maxCountEntries = 99
        val continuationId = "9345-10-26-11.52.15.693455"

        // when
        val result = underTest.createGetTransactionsMessage(GetTransactionsParameter(false, fromDate, toDate, maxCountEntries, false), account, dialogContext) // TODO: test Aufsetzpunkt / continuationId

        // then
        expect(result.createdMessage).notToBeNull()

        expect(normalizeBinaryData(result.createdMessage!!)).toBe(normalizeBinaryData(
            "HNHBK:1:3+000000000389+300+0+1'" +
            "HNVSK:998:3+PIN:2+998+1+1::0+1:$Date:$Time+2:16:14:@8@        :5:1+280:$BankCode:$CustomerId:V:0:0+0'" +
            "HNVSD:999:1+@225@" + "HNSHK:2:4+PIN:2+${SecurityFunction.code}+$ControlReference+1+1+1::0+1+1:$Date:$Time+1:999:1+6:10:16+280:$BankCode:$CustomerId:S:0:0'" +
            "HKKAZ:3:${getTransactionsJob.segmentVersion}+$CustomerId::280:$BankCode+N+${convertDate(fromDate)}+${convertDate(toDate)}+$maxCountEntries+$continuationId'" +
            "HKTAN:4:6+4+HKKAZ'" +
            "HNSHA:5:2+$ControlReference++$Pin''" +
            "HNHBS:6:1+1'"
        ))
    }

}