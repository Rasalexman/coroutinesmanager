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

package com.rasalexman.coroutinesmanager

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * IAsyncTasksManager
 */
interface IAsyncTasksManager {
    /**
     * Async Job
     * You better to override this val in implementing classes,
     * because when you create a new coroutine you will always
     * work with providing single job that store all of your coroutines
     * no matter if you work on [CoroutinesProvider.UI] or [CoroutinesProvider.COMMON]
     */
    val asyncJob: Job
        get() = CoroutinesProvider.supervisorJob

    /**
     * Cancelation handlers local store
     */
    val cancelationHandlers: MutableSet<CancelationHandler>?
            get() = CoroutinesProvider.cancelationHandlersSet

    /**
     * CoroutineContext to use in this manager. It's async
     */
    val asyncCoroutineContext: CoroutineContext
        get() = CoroutinesProvider.COMMON + asyncJob
}

/**
 * launch coroutine on common pool job
 *
 * @param start - starting coroutine strategy [CoroutineStart]
 * @param block - The worker block to invoke
 */
suspend fun <T> IAsyncTasksManager.doAsync(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: SuspendTry<T>
): Deferred<T> {
    return coroutineScope { async(asyncCoroutineContext, start, block).also { job -> job.invokeOnCompletion { job.cancel() } } }
}

/**
 * launch coroutine on common pool job and await a result
 *
 * @param start
 * Start strategy
 *
 * @param block
 * The worker block to invoke
 */
suspend fun <T> IAsyncTasksManager.doAsyncAwait(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: SuspendTry<T>
): T {
    return doAsync(start, block).await()
}

/**
 * Doing some async work with tryCatch block
 *
 * @param tryBlock      - main working block
 * @param catchBlock    - block where throwable will be handled.
 *
 * @param handleCancellationExceptionManually - does we need to handle cancelation manually
 * @param start         - starting coroutine strategy [CoroutineStart]
 */
suspend fun <T> IAsyncTasksManager.doTryCatchAsync(
    tryBlock: SuspendTry<T>,
    catchBlock: SuspendCatch<T>,
    handleCancellationExceptionManually: Boolean = false,
    start: CoroutineStart = CoroutineStart.DEFAULT
): Deferred<T> = doAsync(start) {
    tryCatch(
        tryBlock = tryBlock,
        catchBlock = catchBlock,
        handleCancellationExceptionManually = handleCancellationExceptionManually
    )
}

/**
 * Doing some async work with tryCatchFinally block
 *
 * @param tryBlock      - main working block
 * @param catchBlock    - block where throwable will be handled
 * @param finallyBlock  - there is a block where exception can passed as param `it:Throwable?`
 *
 * @param handleCancellationExceptionManually - does we need to handle cancelation manually
 * @param start         - starting coroutine strategy [CoroutineStart.DEFAULT] by default
 */
suspend fun <T> IAsyncTasksManager.doTryCatchFinallyAsync(
    tryBlock: SuspendTry<T>,
    catchBlock: SuspendCatch<T>,
    finallyBlock: SuspendFinal<T>,
    handleCancellationExceptionManually: Boolean = false,
    start: CoroutineStart = CoroutineStart.DEFAULT
): Deferred<T> = doAsync(start) {
    tryCatchFinally(
        tryBlock = tryBlock,
        catchBlock = catchBlock,
        finallyBlock = finallyBlock,
        handleCancellationExceptionManually = handleCancellationExceptionManually
    )
}

/**
 * Doing some async work with tryFinally block. If there is an error occures
 * It will be throwed and passed into [finallyBlock] as parameter
 *
 * @param tryBlock      - main working block
 * @param finallyBlock  - there is a block where exception can exist as param `it:Throwable?`
 *
 * @param start         - starting coroutine strategy [CoroutineStart]
 */
suspend fun <T> IAsyncTasksManager.doTryFinallyAsync(
    tryBlock: SuspendTry<T>,
    finallyBlock: SuspendFinal<T>,
    start: CoroutineStart = CoroutineStart.DEFAULT
): Deferred<T> = doAsync(start) {
    tryFinally(
        tryBlock = tryBlock,
        finallyBlock = finallyBlock
    )
}

/**
 * Doing some async work with tryCatch block and wait for result
 * This function does not block a MAIN thread
 *
 * @param tryBlock      - main working block
 * @param catchBlock    - block where throwable will be handled.
 *
 * @param handleCancellationExceptionManually - does we need to handle cancelation manually
 * @param start         - starting coroutine strategy [CoroutineStart]
 */
suspend fun <T> IAsyncTasksManager.doTryCatchAsyncAwait(
    tryBlock: SuspendTry<T>,
    catchBlock: SuspendCatch<T>,
    handleCancellationExceptionManually: Boolean = false,
    start: CoroutineStart = CoroutineStart.DEFAULT
): T = doTryCatchAsync(
    tryBlock = tryBlock,
    catchBlock = catchBlock,
    handleCancellationExceptionManually = handleCancellationExceptionManually,
    start = start
).await()

/**
 * Doing some async work with tryCatchFinally block and waiting for [tryBlock] result
 * or some errors in [catchBlock]
 *
 * @param tryBlock      - main working block
 * @param catchBlock    - block where throwable will be handled
 * @param finallyBlock  - there is a block where exception can exist as param `it:Throwable?`
 *
 * @param handleCancellationExceptionManually - does we need to handle cancelation manually
 * @param start         - starting coroutine strategy [CoroutineStart]
 */
suspend fun <T> IAsyncTasksManager.doTryCatchFinallyAsyncAwait(
    tryBlock: SuspendTry<T>,
    catchBlock: SuspendCatch<T>,
    finallyBlock: SuspendFinal<T>,
    handleCancellationExceptionManually: Boolean = false,
    start: CoroutineStart = CoroutineStart.DEFAULT
): T = doTryCatchFinallyAsync(
    tryBlock = tryBlock,
    catchBlock = catchBlock,
    finallyBlock = finallyBlock,
    handleCancellationExceptionManually = handleCancellationExceptionManually,
    start = start
).await()

/**
 * Doing some async work with tryFinally block and waiting for [tryBlock] result.
 * If there is an error occures, It will be throwed and passed into [finallyBlock] as parameter
 *
 * @param tryBlock      - main working block
 * @param finallyBlock  - there is a block where exception can exist as param `it:Throwable?`
 *
 * @param start         - starting coroutine strategy [CoroutineStart]
 */
suspend fun <T> IAsyncTasksManager.doTryFinallyAsyncAwait(
    tryBlock: SuspendTry<T>,
    finallyBlock: SuspendFinal<T>,
    start: CoroutineStart = CoroutineStart.DEFAULT
): T = doTryFinallyAsync(
    tryBlock = tryBlock,
    finallyBlock = finallyBlock,
    start = start
).await()


/**
 * cancel all working coroutines
 */
@Synchronized
fun IAsyncTasksManager.cancelAllAsync() {
    asyncCoroutineContext.cancelChildren()
    cancelationHandlers?.forEach { it() }
}

/**
 * Clear all working coroutines and cancelationHandlers
 */
@Synchronized
fun IAsyncTasksManager.cleanup() {
    asyncCoroutineContext.cancelChildren()
    cancelationHandlers?.clear()
}

/**
 * Add cancel all jobs handler
 *
 * @param handler - handler for cancel coroutine job manually [CancelationHandler]
 */
@Synchronized
fun IAsyncTasksManager.addCancelationHandler(handler: CancelationHandler) {
    cancelationHandlers?.add(handler)
}

/**
 * Remove cancel job handler
 *
 * @param handler - the reference to already added function
 */
@Synchronized
fun IAsyncTasksManager.removeCancelationHandler(handler: CancelationHandler) {
    cancelationHandlers?.remove(handler)
}

/**
 * Count of cancelations function on this manager
 */
@Synchronized
fun IAsyncTasksManager.cancelationsHandlersCount(): Int {
    return cancelationHandlers?.size ?: 0
}

/**
 * Was this manager canceled already
 */
val IAsyncTasksManager.isCancelled: Boolean
    get() = asyncJob.children.all { it.isCancelled }
