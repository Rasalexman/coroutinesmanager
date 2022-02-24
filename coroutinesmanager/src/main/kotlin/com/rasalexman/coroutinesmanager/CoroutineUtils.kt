/*
MIT License

Copyright (c) 2019 Alexandr Minkin (sphc@yandex.ru)
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial
portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

@file:Suppress("SuspendFunctionOnCoroutineScope", "unused")

package com.rasalexman.coroutinesmanager

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Handler for cancelation
 */
typealias CancelationHandler = () -> Unit

/**
 * Typealias for tryBlock
 */
typealias SuspendTry<T> = suspend CoroutineScope.() -> T

/**
 * Typealias for catchBlock
 */
typealias SuspendCatch<T> = suspend CoroutineScope.(Throwable) -> T

/**
 * Typealias for finallyBlock, it can handle an exception if it's exist
 */
typealias SuspendFinal<T> = suspend CoroutineScope.(Throwable?) -> T

/**
 * tryCatch coroutines function
 *
 * @param tryBlock      - main working block
 * @param catchBlock    - block where throwable will be handled.
 * @param handleCancellationExceptionManually - does we need to handle cancelation manually
 */
suspend fun <T> CoroutineScope.tryCatch(
    tryBlock: SuspendTry<T>,
    catchBlock: SuspendCatch<T>,
    handleCancellationExceptionManually: Boolean = false
): T {
    return try {
        tryBlock()
    } catch (e: Throwable) {
        if (e !is CancellationException || handleCancellationExceptionManually) {
            catchBlock(e)
        } else {
            throw e
        }
    }
}

/**
 * tryCatchFinally coroutines function
 *
 * @param tryBlock
 * @param catchBlock
 * @param finallyBlock
 * @param handleCancellationExceptionManually - can we handle cancellation manually
 */
suspend fun <T> CoroutineScope.tryCatchFinally(
    tryBlock: SuspendTry<T>,
    catchBlock: SuspendCatch<T>,
    finallyBlock: SuspendFinal<T>,
    handleCancellationExceptionManually: Boolean = false
): T {
    var caughtThrowable: Throwable? = null
    return try {
        tryBlock()
    } catch (e: Throwable) {
        caughtThrowable = e
        if (e !is CancellationException || handleCancellationExceptionManually) {
            catchBlock(e)
        } else {
            throw e
        }
    } finally {
        finallyBlock(caughtThrowable)
    }
}

/**
 * Try finally coroutines function
 *
 * @param tryBlock
 * @param finallyBlock
 */
suspend fun <T> CoroutineScope.tryFinally(
    tryBlock: SuspendTry<T>,
    finallyBlock: SuspendFinal<T>
): T {
    var caughtThrowable: Throwable? = null
    return try {
        tryBlock()
    } catch (e: Throwable) {
        caughtThrowable = e
        throw e
    } finally {
        finallyBlock(caughtThrowable)
    }

}

/**
 * tryCatch with switching coroutine context function
 *
 * @param tryBlock
 * @param catchBlock
 * @param tryContext - context for invoke [tryBlock]. Current Scope Context by default
 * @param catchContext - context for invoke [catchBlock]. Current Scope Context by default
 * @param handleCancellationExceptionManually - does we need to handle cancelation manually
 */
suspend fun <T> CoroutineScope.tryCatchWithContext(
    tryBlock: SuspendTry<T>,
    catchBlock: SuspendCatch<T>,
    tryContext: CoroutineContext = Dispatchers.Main,
    catchContext: CoroutineContext = Dispatchers.Main,
    handleCancellationExceptionManually: Boolean = false
): T {
    return try {
        withContext(tryContext, tryBlock)
    } catch (e: Throwable) {
        if (e !is CancellationException || handleCancellationExceptionManually) {
            withContext(catchContext) {
                catchBlock(e)
            }
        } else {
            throw e
        }
    }
}