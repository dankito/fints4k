package net.dankito.banking.util

import ch.tutteli.atrium.api.fluent.en_GB.notToBeNull
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import org.junit.Test


class InputValidatorTest {

    companion object {

        const val ValidRecipientName = "Marieke Musterfrau"

        const val ValidIban = "DE11123456780987654321"

        const val ValidBic = "ABCDDEBBXXX"

        const val ValidReference = "Reference"

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
    fun validateRecipientName_EmptyStringEntered() {

        // given
        val enteredName = ""

        // when
        val result = underTest.validateRecipientName(enteredName)

        // then
        expect(result.validationSuccessful).toBe(false)
        expect(result.didCorrectString).toBe(false)
        expect(result.inputString).toBe(enteredName)
        expect(result.correctedInputString).toBe(enteredName)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).notToBeNull()
    }

    @Test
    fun validateRecipientName_ValidNameEntered() {

        // given
        val enteredName = ValidRecipientName

        // when
        val result = underTest.validateRecipientName(enteredName)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(false)
        expect(result.inputString).toBe(enteredName)
        expect(result.correctedInputString).toBe(enteredName)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateRecipientName_UmlautGetsConverted() {

        // given
        val enteredName = ValidRecipientName + InvalidUmlaut

        // when
        val result = underTest.validateRecipientName(enteredName)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(true)
        expect(result.inputString).toBe(enteredName)
        expect(result.correctedInputString).toBe(ValidRecipientName + ConvertedInvalidUmlaut)
        expect(result.validationHint?.contains(InvalidUmlaut)).toBe(true)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateRecipientName_InvalidCharacterGetsRemoved() {

        // given
        val enteredName = ValidRecipientName + InvalidSepaCharacter

        // when
        val result = underTest.validateRecipientName(enteredName)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(true)
        expect(result.inputString).toBe(enteredName)
        expect(result.correctedInputString).toBe(ValidRecipientName)
        expect(result.validationHint?.contains(InvalidSepaCharacter)).toBe(true)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateRecipientName_TooLong() {

        // given
        val nameWithMaxLength = IntRange(0, InputValidator.RemitteNameMaxLength - 1).map { "a" }.joinToString("")
        val enteredName = nameWithMaxLength + "a"

        // when
        val result = underTest.validateRecipientName(enteredName)

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
    fun validateReference_EmptyStringEntered() {

        // given
        val enteredReference = ""

        // when
        val result = underTest.validateReference(enteredReference)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(false)
        expect(result.inputString).toBe(enteredReference)
        expect(result.correctedInputString).toBe(enteredReference)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateReference_ValidReferenceEntered() {

        // given
        val enteredReference = ValidReference

        // when
        val result = underTest.validateReference(enteredReference)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(false)
        expect(result.inputString).toBe(enteredReference)
        expect(result.correctedInputString).toBe(enteredReference)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateReference_UmlautGetsConverted() {

        // given
        val enteredReference = ValidReference + InvalidUmlaut

        // when
        val result = underTest.validateReference(enteredReference)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(true)
        expect(result.inputString).toBe(enteredReference)
        expect(result.correctedInputString).toBe(ValidReference + ConvertedInvalidUmlaut)
        expect(result.validationHint?.contains(InvalidUmlaut)).toBe(true)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateReference_InvalidCharacterGetsRemoved() {

        // given
        val enteredReference = ValidReference + InvalidSepaCharacter

        // when
        val result = underTest.validateReference(enteredReference)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(true)
        expect(result.inputString).toBe(enteredReference)
        expect(result.correctedInputString).toBe(ValidReference)
        expect(result.validationHint?.contains(InvalidSepaCharacter)).toBe(true)
        expect(result.validationError).toBe(null)
    }

    // TODO: does not work yet
    @Test
    fun validateReference_AmpersandGetsRemoved() {

        // given
        val invalidSepaCharacter = "&"
        val enteredReference = ValidReference + invalidSepaCharacter

        // when
        val result = underTest.validateReference(enteredReference)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(true)
        expect(result.inputString).toBe(enteredReference)
        expect(result.correctedInputString).toBe(ValidReference)
        expect(result.validationHint?.contains(invalidSepaCharacter)).toBe(true)
        expect(result.validationError).toBe(null)
    }

    // TODO: does not work yet
    @Test
    fun validateReference_EnteringACharacterAfterConvertingAXmlEntityDoesNotFail() {

        // given
        val convertedXmlEntity = "&amp;"
        val validSepaCharacter = "h"
        val enteredReference = ValidReference + convertedXmlEntity + validSepaCharacter

        // when
        val result = underTest.validateReference(enteredReference)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(true)
        expect(result.inputString).toBe(enteredReference)
        expect(result.correctedInputString).toBe(ValidReference + convertedXmlEntity + validSepaCharacter)
        expect(result.validationHint).toBe(null)
        expect(result.validationError).toBe(null)
    }

    @Test
    fun validateReference_TooLong() {

        // given
        val referenceWithMaxLength = IntRange(0, InputValidator.ReferenceMaxLength - 1).map { "a" }.joinToString("")
        val enteredReference = referenceWithMaxLength + "a"

        // when
        val result = underTest.validateReference(enteredReference)

        // then
        expect(result.validationSuccessful).toBe(true)
        expect(result.didCorrectString).toBe(true)
        expect(result.inputString).toBe(enteredReference)
        expect(result.correctedInputString).toBe(referenceWithMaxLength)
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