package com.rasalexman.coroutinesmanager

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope

typealias CancelationHandler = () -> Unit

/**
 * tryCatch coroutines function
 *
 * @param tryBlock
 * @param catchBlock
 * @param handleCancellationExceptionManually
 */
suspend fun CoroutineScope.tryCatch(
        tryBlock: suspend CoroutineScope.() -> Unit,
        catchBlock: suspend CoroutineScope.(Throwable) -> Unit,
        handleCancellationExceptionManually: Boolean = false) {
    try {
        tryBlock()
    } catch (e: Throwable) {
        if (e !is CancellationException ||
                handleCancellationExceptionManually) {
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
suspend fun CoroutineScope.tryCatchFinally(
        tryBlock: suspend CoroutineScope.() -> Unit,
        catchBlock: suspend CoroutineScope.(Throwable) -> Unit,
        finallyBlock: suspend CoroutineScope.() -> Unit,
        handleCancellationExceptionManually: Boolean = false) {

    var caughtThrowable: Throwable? = null

    try {
        tryBlock()
    } catch (e: Throwable) {
        if (e !is CancellationException ||
                handleCancellationExceptionManually) {
            catchBlock(e)
        } else {
            caughtThrowable = e
        }
    } finally {
        if (caughtThrowable is CancellationException &&
                !handleCancellationExceptionManually) {
            throw caughtThrowable
        } else {
            finallyBlock()
        }
    }
}

/**
 * Try finally coroutines function
 *
 * @param tryBlock
 * @param finallyBlock
 * @param suppressCancellationException
 */
suspend fun CoroutineScope.tryFinally(
        tryBlock: suspend CoroutineScope.() -> Unit,
        finallyBlock: suspend CoroutineScope.() -> Unit,
        suppressCancellationException: Boolean = false) {

    var caughtThrowable: Throwable? = null

    try {
        tryBlock()
    } catch (e: CancellationException) {
        if (!suppressCancellationException) {
            caughtThrowable = e
        }
    } finally {
        if (caughtThrowable is CancellationException &&
                !suppressCancellationException) {
            throw caughtThrowable
        } else {
            finallyBlock()
        }
    }
}