package net.dankito.fints.messages

import net.dankito.fints.FinTsTestBase
import net.dankito.fints.model.AccountData
import net.dankito.fints.model.DialogData
import net.dankito.fints.model.GetTransactionsParameter
import net.dankito.fints.response.segments.AccountType
import net.dankito.fints.response.segments.JobParameters
import net.dankito.fints.util.FinTsUtils
import net.dankito.utils.datetime.asUtilDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import java.time.LocalDate
import java.time.Month
import java.util.*


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


    @After
    fun tearDown() {
        Bank.supportedJobs = listOf()
        Customer.accounts = listOf()
    }


    @Test
    fun createAnonymousDialogInitMessage() {

        // when
        val result = underTest.createAnonymousDialogInitMessage(Bank, Product, DialogData.DialogInitDialogData)

        // then
        assertThat(result).isEqualTo(
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
        val dialogData = DialogData(dialogId)

        // when
        val result = underTest.createAnonymousDialogEndMessage(Bank, dialogData)

        // then
        assertThat(normalizeBinaryData(result)).isEqualTo(normalizeBinaryData(
            "HNHBK:1:3+000000000086+300+$dialogId+1'" +
            "HKEND:2:1+$dialogId'" +
            "HNHBS:3:1+1'"
        ))
    }


    @Test
    fun createDialogInitMessage() {

        // when
        val result = underTest.createSynchronizeCustomerSystemIdMessage(Bank, Customer, Product, DialogData.DialogInitDialogData)

        // then
        assertThat(normalizeBinaryData(result)).isEqualTo(normalizeBinaryData(
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
        val dialogData = DialogData(dialogId)

        // when
        val result = underTest.createDialogEndMessage(Bank, Customer, dialogData)

        // then
        assertThat(normalizeBinaryData(result)).isEqualTo(normalizeBinaryData(
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

        // when
        val result = underTest.createGetTransactionsMessage(GetTransactionsParameter(), Bank, Customer, Product, DialogData.DialogInitDialogData)

        // then
        assertThat(result.isJobAllowed).isFalse()
    }

    @Test
    fun createGetTransactionsMessage_JobVersionIsNotSupported() {

        // given
        val getTransactionsJob = JobParameters("HKKAZ", 1, 1, null, "HKKAZ:73:5")
        val getTransactionsJobWithPreviousVersion = JobParameters("HKKAZ", 1, 1, null, "HKKAZ:72:4")
        Bank.supportedJobs = listOf(getTransactionsJob)
        val account = AccountData(CustomerId, null, BankCountryCode, BankCode, null, CustomerId, AccountType.Girokonto, "EUR", "", null, null, listOf(getTransactionsJob.jobName), listOf(getTransactionsJobWithPreviousVersion))
        Customer.accounts = listOf(account)

        // when
        val result = underTest.createGetTransactionsMessage(GetTransactionsParameter(), Bank, Customer, Product, DialogData.DialogInitDialogData)

        // then
        assertThat(result.isJobAllowed).isTrue()
        assertThat(result.isJobVersionSupported).isFalse()
    }

    @Test
    fun createGetTransactionsMessage() {

        // given
        val getTransactionsJob = JobParameters("HKKAZ", 1, 1, null, "HKKAZ:73:5")
        Bank.supportedJobs = listOf(getTransactionsJob)
        val account = AccountData(CustomerId, null, BankCountryCode, BankCode, null, CustomerId, AccountType.Girokonto, "EUR", "", null, null, listOf(getTransactionsJob.jobName), listOf(getTransactionsJob))
        Customer.accounts = listOf(account)

        val fromDate = LocalDate.of(2019, Month.AUGUST, 6).asUtilDate()
        val toDate = LocalDate.of(2019, Month.OCTOBER, 21).asUtilDate()
        val maxCountEntries = 99

        // when
        val result = underTest.createGetTransactionsMessage(GetTransactionsParameter(false, fromDate, toDate, maxCountEntries), Bank, Customer, Product, DialogData.DialogInitDialogData)

        // then
        assertThat(result.createdMessage).isNotNull()

        assertThat(normalizeBinaryData(result.createdMessage!!)).isEqualTo(normalizeBinaryData(
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
        Customer.accounts = listOf(account)

        val fromDate = LocalDate.of(2019, Month.AUGUST, 6).asUtilDate()
        val toDate = LocalDate.of(2019, Month.OCTOBER, 21).asUtilDate()
        val maxCountEntries = 99
        val continuationId = "9345-10-26-11.52.15.693455"

        // when
        val result = underTest.createGetTransactionsMessage(GetTransactionsParameter(false, fromDate, toDate, maxCountEntries, false, continuationId), Bank, Customer, Product, DialogData.DialogInitDialogData)

        // then
        assertThat(result.createdMessage).isNotNull()

        assertThat(normalizeBinaryData(result.createdMessage!!)).isEqualTo(normalizeBinaryData(
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