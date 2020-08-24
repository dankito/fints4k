package net.dankito.banking.util

import ch.tutteli.atrium.api.fluent.en_GB.notToBeNull
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import org.junit.Test


class InputValidatorTest {

    companion object {

        const val ValidRemitteeName = "Marieke Musterfrau"

        const val ValidIban = "DE11123456780987654321"

        const val ValidBic = "ABCDDEBBXXX"

        const val ValidUsage = "Usage"

        const val InvalidSepaCharacter = "!"

        const val InvalidUmlaut = "ö"
        const val ConvertedInvalidUmlaut = "o"

    }

    private val underTest = InputValidator()


    @Test
    fun getInvalidIbanCharacters() {

        // given
        val invalidIbanCharacters = "ajvz!@#$%^&*()-_=+[]{}'\"\\|/?.,;:<>"

        // when
        val result = underTest.getInvalidIbanCharacters("EN${invalidIbanCharacters}1234")

        // then
        expect(result).toBe(invalidIbanCharacters)
    }

    @Test
    fun getInvalidSepaCharacters() {

        // given
        val invalidSepaCharacters = "!€@#$%^*=[]\\|<>"

        // when
        val result = underTest.getInvalidSepaCharacters("abcd${invalidSepaCharacters}1234")

        // then
        expect(result).toBe(invalidSepaCharacters)
    }


    @Test
    fun validateRemitteeName_EmptyStringEntered() {

        // given
        val enteredName = ""

        // when
        val result = underTest.validateRemitteeName(enteredName)

        // then
        expect(result.validationSuccessful).toBe(false)
        expect(result.didCorrectString).toBe(false)
        expect(result.inputString).toBe(enteredName)
        expect(result.correctedInputString).toBe(enteredName)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).notToBeNull()
    }

    @Test
    fun validateRemitteeName_ValidNameEntered() {

        // given
        val enteredName = ValidRemitteeName

        // when
        val result = underTest.validateRemitteeName(enteredName)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(false)
        expect(result.inputString).toBe(enteredName)
        expect(result.correctedInputString).toBe(enteredName)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateRemitteeName_UmlautGetsConverted() {

        // given
        val enteredName = ValidRemitteeName + InvalidUmlaut

        // when
        val result = underTest.validateRemitteeName(enteredName)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(true)
        expect(result.inputString).toBe(enteredName)
        expect(result.correctedInputString).toBe(ValidRemitteeName + ConvertedInvalidUmlaut)
        expect(result.validationHint?.contains(InvalidUmlaut)).toBe(true)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateRemitteeName_InvalidCharacterGetsRemoved() {

        // given
        val enteredName = ValidRemitteeName + InvalidSepaCharacter

        // when
        val result = underTest.validateRemitteeName(enteredName)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(true)
        expect(result.inputString).toBe(enteredName)
        expect(result.correctedInputString).toBe(ValidRemitteeName)
        expect(result.validationHint?.contains(InvalidSepaCharacter)).toBe(true)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateRemitteeName_TooLong() {

        // given
        val nameWithMaxLength = IntRange(0, InputValidator.RemitteNameMaxLength - 1).map { "a" }.joinToString("")
        val enteredName = nameWithMaxLength + "a"

        // when
        val result = underTest.validateRemitteeName(enteredName)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(true)
        expect(result.inputString).toBe(enteredName)
        expect(result.correctedInputString).toBe(nameWithMaxLength)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).notToBeNull()
    }


    @Test
    fun validateIban_EmptyStringEntered() {

        // given
        val enteredIban = ""

        // when
        val result = underTest.validateIban(enteredIban)

        // then
        expect(result.validationSuccessful).toBe(false)
        expect(result.didCorrectString).toBe(false)
        expect(result.inputString).toBe(enteredIban)
        expect(result.correctedInputString).toBe(enteredIban)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).notToBeNull()
    }

    @Test
    fun validateIban_ValidIbanEntered() {

        // given
        val enteredIban = ValidIban

        // when
        val result = underTest.validateIban(enteredIban)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(false)
        expect(result.inputString).toBe(enteredIban)
        expect(result.correctedInputString).toBe(enteredIban)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateIban_IbanTooShort() {

        // given
        val enteredIban = "DE11"

        // when
        val result = underTest.validateIban(enteredIban)

        // then
        expect(result.validationSuccessful).toBe(false)
        expect(result.didCorrectString).toBe(false)
        expect(result.inputString).toBe(enteredIban)
        expect(result.correctedInputString).toBe(enteredIban)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).notToBeNull()
    }

    @Test
    fun validateIban_UmlautGetsRemoved() {

        // given
        val enteredIban = ValidIban + InvalidUmlaut

        // when
        val result = underTest.validateIban(enteredIban)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(true)
        expect(result.inputString).toBe(enteredIban)
        expect(result.correctedInputString).toBe(ValidIban)
        expect(result.validationHint?.contains(InvalidUmlaut)).toBe(true)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateIban_InvalidCharacterGetsRemoved() {

        // given
        val enteredIban = ValidIban + InvalidSepaCharacter

        // when
        val result = underTest.validateIban(enteredIban)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(true)
        expect(result.inputString).toBe(enteredIban)
        expect(result.correctedInputString).toBe(ValidIban)
        expect(result.validationHint?.contains(InvalidSepaCharacter)).toBe(true)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateIban_TooLong() {

        // given
        val ibanWithMaxLength = IntRange(0, InputValidator.IbanMaxLength - 1).map { "1" }.joinToString("")
        val enteredIban = ibanWithMaxLength + "1"

        // when
        val result = underTest.validateIban(enteredIban)

        // then
        expect(result.validationSuccessful).toBe(false)
        expect(result.didCorrectString).toBe(true)
        expect(result.inputString).toBe(enteredIban)
        expect(result.correctedInputString).toBe(ibanWithMaxLength)
        expect(result.validationHint).notToBeNull()
        expect(result.validationError).toBe(null)
    }


    @Test
    fun validateIbanWhileTyping_EmptyStringEntered() {

        // given
        val enteredIban = ""

        // when
        val result = underTest.validateIbanWhileTyping(enteredIban)

        // then
        expect(result.validationSuccessful).toBe(true) // while user is typing an empty string is ok
        expect(result.didCorrectString).toBe(false)
        expect(result.inputString).toBe(enteredIban)
        expect(result.correctedInputString).toBe(enteredIban)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateIbanWhileTyping_ValidIbanEntered() {

        // given
        val enteredIban = ValidIban

        // when
        val result = underTest.validateIbanWhileTyping(enteredIban)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(false)
        expect(result.inputString).toBe(enteredIban)
        expect(result.correctedInputString).toBe(enteredIban)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateIbanWhileTyping_IbanTooShort() {

        // given
        val enteredIban = "DE11"

        // when
        val result = underTest.validateIbanWhileTyping(enteredIban)

        // then
        expect(result.validationSuccessful).toBe(true) // while user is typing an incomplete IBAN is ok
        expect(result.didCorrectString).toBe(false)
        expect(result.inputString).toBe(enteredIban)
        expect(result.correctedInputString).toBe(enteredIban)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateIbanWhileTyping_UmlautGetsRemoved() {

        // given
        val enteredIban = ValidIban + InvalidUmlaut

        // when
        val result = underTest.validateIbanWhileTyping(enteredIban)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(true)
        expect(result.inputString).toBe(enteredIban)
        expect(result.correctedInputString).toBe(ValidIban)
        expect(result.validationHint?.contains(InvalidUmlaut)).toBe(true)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateIbanWhileTyping_InvalidCharacterGetsRemoved() {

        // given
        val enteredIban = ValidIban + InvalidSepaCharacter

        // when
        val result = underTest.validateIbanWhileTyping(enteredIban)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(true)
        expect(result.inputString).toBe(enteredIban)
        expect(result.correctedInputString).toBe(ValidIban)
        expect(result.validationHint?.contains(InvalidSepaCharacter)).toBe(true)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateIbanWhileTyping_TooLong() {

        // given
        val ibanWithMaxLength = IntRange(0, InputValidator.IbanMaxLength - 1).map { "1" }.joinToString("")
        val enteredIban = ibanWithMaxLength + "1"

        // when
        val result = underTest.validateIbanWhileTyping(enteredIban)

        // then
        expect(result.validationSuccessful).toBe(false)
        expect(result.didCorrectString).toBe(true)
        expect(result.inputString).toBe(enteredIban)
        expect(result.correctedInputString).toBe(ibanWithMaxLength)
        expect(result.validationHint).notToBeNull()
        expect(result.validationError).toBe(null)
    }


    @Test
    fun validateBic_EmptyStringEntered() {

        // given
        val enteredBic = ""

        // when
        val result = underTest.validateBic(enteredBic)

        // then
        expect(result.validationSuccessful).toBe(false)
        expect(result.didCorrectString).toBe(false)
        expect(result.inputString).toBe(enteredBic)
        expect(result.correctedInputString).toBe(enteredBic)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).notToBeNull()
    }

    @Test
    fun validateBic_ValidBicEntered() {

        // given
        val enteredBic = ValidBic

        // when
        val result = underTest.validateBic(enteredBic)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(false)
        expect(result.inputString).toBe(enteredBic)
        expect(result.correctedInputString).toBe(enteredBic)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateBic_BicTooShort() {

        // given
        val enteredBic = "ABCD"

        // when
        val result = underTest.validateBic(enteredBic)

        // then
        expect(result.validationSuccessful).toBe(false)
        expect(result.didCorrectString).toBe(false)
        expect(result.inputString).toBe(enteredBic)
        expect(result.correctedInputString).toBe(enteredBic)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).notToBeNull()
    }

    @Test
    fun validateBic_UmlautGetsRemoved() {

        // given
        val bicWithoutLastPlace = ValidBic.substring(0, ValidBic.length - 1)
        val enteredBic = bicWithoutLastPlace + InvalidUmlaut

        // when
        val result = underTest.validateBic(enteredBic)

        // then
        expect(result.validationSuccessful).toBe(false)
        expect(result.didCorrectString).toBe(true)
        expect(result.inputString).toBe(enteredBic)
        expect(result.correctedInputString).toBe(bicWithoutLastPlace)
        expect(result.validationHint?.contains(InvalidUmlaut)).toBe(true)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateBic_InvalidCharacterGetsRemoved() {

        // given
        val bicWithoutLastPlace = ValidBic.substring(0, ValidBic.length - 1)
        val enteredBic = bicWithoutLastPlace + InvalidSepaCharacter

        // when
        val result = underTest.validateBic(enteredBic)

        // then
        expect(result.validationSuccessful).toBe(false)
        expect(result.didCorrectString).toBe(true)
        expect(result.inputString).toBe(enteredBic)
        expect(result.correctedInputString).toBe(bicWithoutLastPlace)
        expect(result.validationHint?.contains(InvalidSepaCharacter)).toBe(true)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateBic_TooLong() {

        // given
        val bicWithMaxLength = IntRange(0, InputValidator.BicMaxLength - 1).map { "A" }.joinToString("")
        val enteredBic = bicWithMaxLength + "A"

        // when
        val result = underTest.validateBic(enteredBic)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(true)
        expect(result.inputString).toBe(enteredBic)
        expect(result.correctedInputString).toBe(bicWithMaxLength)
        expect(result.validationHint).notToBeNull()
        expect(result.validationError).toBe(null)
    }


    @Test
    fun validateUsage_EmptyStringEntered() {

        // given
        val enteredUsage = ""

        // when
        val result = underTest.validateUsage(enteredUsage)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(false)
        expect(result.inputString).toBe(enteredUsage)
        expect(result.correctedInputString).toBe(enteredUsage)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateUsage_ValidUsageEntered() {

        // given
        val enteredUsage = ValidUsage

        // when
        val result = underTest.validateUsage(enteredUsage)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(false)
        expect(result.inputString).toBe(enteredUsage)
        expect(result.correctedInputString).toBe(enteredUsage)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateUsage_UmlautGetsConverted() {

        // given
        val enteredUsage = ValidUsage + InvalidUmlaut

        // when
        val result = underTest.validateUsage(enteredUsage)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(true)
        expect(result.inputString).toBe(enteredUsage)
        expect(result.correctedInputString).toBe(ValidUsage + ConvertedInvalidUmlaut)
        expect(result.validationHint?.contains(InvalidUmlaut)).toBe(true)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateUsage_InvalidCharacterGetsRemoved() {

        // given
        val enteredUsage = ValidUsage + InvalidSepaCharacter

        // when
        val result = underTest.validateUsage(enteredUsage)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(true)
        expect(result.inputString).toBe(enteredUsage)
        expect(result.correctedInputString).toBe(ValidUsage)
        expect(result.validationHint?.contains(InvalidSepaCharacter)).toBe(true)
        expect(result.validationError).toBe(null)
    }

    // TODO: does not work yet
    @Test
    fun validateUsage_AmpersandGetsRemoved() {

        // given
        val invalidSepaCharacter = "&"
        val enteredUsage = ValidUsage + invalidSepaCharacter

        // when
        val result = underTest.validateUsage(enteredUsage)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(true)
        expect(result.inputString).toBe(enteredUsage)
        expect(result.correctedInputString).toBe(ValidUsage)
        expect(result.validationHint?.contains(invalidSepaCharacter)).toBe(true)
        expect(result.validationError).toBe(null)
    }

    // TODO: does not work yet
    @Test
    fun validateUsage_EnteringACharacterAfterConvertingAXmlEntityDoesNotFail() {

        // given
        val convertedXmlEntity = "&amp;"
        val validSepaCharacter = "h"
        val enteredUsage = ValidUsage + convertedXmlEntity + validSepaCharacter

        // when
        val result = underTest.validateUsage(enteredUsage)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(true)
        expect(result.inputString).toBe(enteredUsage)
        expect(result.correctedInputString).toBe(ValidUsage + convertedXmlEntity + validSepaCharacter)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateUsage_TooLong() {

        // given
        val usageWithMaxLength = IntRange(0, InputValidator.UsageMaxLength - 1).map { "a" }.joinToString("")
        val enteredUsage = usageWithMaxLength + "a"

        // when
        val result = underTest.validateUsage(enteredUsage)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(true)
        expect(result.inputString).toBe(enteredUsage)
        expect(result.correctedInputString).toBe(usageWithMaxLength)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).notToBeNull()
    }


    @Test
    fun validateAmount_EmptyStringEntered() {

        // given
        val enteredAmount = ""

        // when
        val result = underTest.validateAmount(enteredAmount)

        // then
        expect(result.validationSuccessful).toBe(false)
        expect(result.didCorrectString).toBe(false)
        expect(result.inputString).toBe(enteredAmount)
        expect(result.correctedInputString).toBe(enteredAmount)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).notToBeNull()
    }

    @Test
    fun validateAmount_ValidAmountEntered() {

        // given
        val enteredAmount = "84,25"

        // when
        val result = underTest.validateAmount(enteredAmount)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(false)
        expect(result.inputString).toBe(enteredAmount)
        expect(result.correctedInputString).toBe(enteredAmount)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateAmount_ZeroEntered() {

        // given
        val enteredAmount = "0"

        // when
        val result = underTest.validateAmount(enteredAmount)

        // then
        expect(result.validationSuccessful).toBe(false)
        expect(result.didCorrectString).toBe(false)
        expect(result.inputString).toBe(enteredAmount)
        expect(result.correctedInputString).toBe(enteredAmount)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).notToBeNull()
    }

    @Test
    fun validateAmount_NegativeAmountEntered() {

        // given
        val enteredAmount = "-84,25"

        // when
        val result = underTest.validateAmount(enteredAmount)

        // then
        expect(result.validationSuccessful).toBe(false)
        expect(result.didCorrectString).toBe(false)
        expect(result.inputString).toBe(enteredAmount)
        expect(result.correctedInputString).toBe(enteredAmount)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).notToBeNull()
    }

}