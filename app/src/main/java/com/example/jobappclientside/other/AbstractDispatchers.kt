package com.example.jobappclientside.other

import kotlinx.coroutines.CoroutineDispatcher

interface AbstractDispatchers {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}