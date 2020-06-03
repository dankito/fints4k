package net.dankito.banking.fints.extensions

import ch.tutteli.atrium.creating.Expect
import ch.tutteli.atrium.domain.builders.ExpectImpl


fun <T : Boolean> Expect<T>.isTrue(): Expect<T> = addAssertion(ExpectImpl.any.toBe(this, true))

fun <T : Boolean> Expect<T>.isFalse(): Expect<T> = addAssertion(ExpectImpl.any.toBe(this, false))