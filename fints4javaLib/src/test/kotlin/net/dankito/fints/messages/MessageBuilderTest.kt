package net.dankito.fints.messages

import net.dankito.fints.FinTsTestBase
import net.dankito.fints.util.FinTsUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*


class MessageBuilderTest : FinTsTestBase() {

    private val underTest = MessageBuilder(utils = object : FinTsUtils() {
        override fun formatDate(date: Date): String {
            return Date.toString()
        }

        override fun formatTime(time: Date): String {
            return Time.toString()
        }
    })


    @Test
    fun createAnonymousDialogInitMessage() {

        // given
        val underTest = underTest.createAnonymousDialogInitMessage(Bank, Product)

        // when
        val result = underTest.format()

        // then
        assertThat(result).isEqualTo(
            "HNHBK:1:3+000000000125+300+0+1'" +
            "HKIDN:2:2+280:12345678+9999999999+0+0'" +
            "HKVVB:3:3+0+0+0+$ProductName+$ProductVersion'" +
            "HNHBS:4:1+1'"
        )
    }

    @Test
    fun createDialogInitMessage() {

        // given
        val underTest = underTest.createDialogInitMessage(Bank, Customer, Product)

        // when
        val result = underTest.format()

        // then
        assertThat(normalizeBinaryData(result)).isEqualTo(normalizeBinaryData(
            "HNHBK:1:3+000000000386+300+0+1'" +
            "HNVSK:998:3+PIN:2+998+1+1::0+1:$Date:$Time+2:16:14:@8@        :5:1+280:$BankCode:$CustomerId:V:0:0+0+'" +
            "HNVSD:999:1+@221@" + "HNSHK:2:4+PIN:2+${SecurityFunction.code}+$ControlReference+1+1+1::0+1+1:$Date:$Time+1:999:1+6:10:16+280:$BankCode:$CustomerId:S:0:0'" +
            "HKIDN:3:2+280:$BankCode+$CustomerId+0+1'" +
            "HKVVB:4:3+0+0+${Language.code}+$ProductName+$ProductVersion'" +
            "HKTAN:5:6+4+HKIDN++++N'" +
            "HNSHA:6:2+$ControlReference++$Pin''" +
            "HNHBS:7:1+1'"
        ))
    }

}