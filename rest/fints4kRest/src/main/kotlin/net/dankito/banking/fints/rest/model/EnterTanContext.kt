package net.dankito.banking.fints.rest.model

import net.dankito.banking.fints.model.EnterTanResult
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference


open class EnterTanContext(
    open val enterTanResult: AtomicReference<EnterTanResult>,
    open val countDownLatch: CountDownLatch,
    open val tanRequestedTimeStamp: Date = Date()
)