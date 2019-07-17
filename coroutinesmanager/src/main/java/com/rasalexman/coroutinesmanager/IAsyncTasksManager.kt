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
interface IAsyncTasksManager : CoroutineScope {
    /**
     * Async Job
     * You better to override this val in implementing classes,
     * because when you create a new coroutine you will always
     * work with providing single job that store all of your coroutines
     * no matter if you work on UI or COMMON
     */
    val job: Job
        get() = CoroutinesProvider.supervisorJob

    /**
     * Cancelation handlers local store
     */
    val cancelationHandlers: MutableSet<CancelationHandler>

    /**
     * CoroutineContext to use in this manager. It's async
     */
    override val coroutineContext: CoroutineContext
        get() = CoroutinesProvider.COMMON + job

    /**
     * Was Canceled already
     */
    var wasCanceled: Boolean

}

/**
 * launch coroutine on common pool job
 *
 * @param block
 * The worker block to invoke
 */
suspend fun <T> IAsyncTasksManager.doAsync(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: SuspendTry<T>
): Deferred<T> {
    wasCanceled = false
    return async(coroutineContext, start, block).also { job -> job.invokeOnCompletion { job.cancel() } }
}

/**
 * launch coroutine on common pool job
 *
 * @param start
 * Start strategy
 *
 * @param block
 * The worker block to invoke
 */
suspend fun <T> IAsyncTasksManager.doAsyncAwait(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
): T {
    wasCanceled = false
    return doAsync(start, block).await()
}

/**
 * Do some work on coroutine context
 * @param block
 * The worker block to invoke
 */
suspend fun <T> IAsyncTasksManager.doWithContext(
    block: suspend CoroutineScope.() -> T
): T = withContext(coroutineContext, block)

/**
 *
 */
suspend fun <T> IAsyncTasksManager.doAsyncTryCatch(
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
 *
 */
suspend fun <T> IAsyncTasksManager.doAsyncTryCatchFinally(
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
 *
 */
suspend fun <T> IAsyncTasksManager.doAsyncTryFinally(
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
 *
 */
suspend fun <T> IAsyncTasksManager.doAsyncAwaitTryCatch(
    tryBlock: SuspendTry<T>,
    catchBlock: SuspendCatch<T>,
    handleCancellationExceptionManually: Boolean = false,
    start: CoroutineStart = CoroutineStart.DEFAULT
): T = doAsyncTryCatch(
    tryBlock = tryBlock,
    catchBlock = catchBlock,
    handleCancellationExceptionManually = handleCancellationExceptionManually,
    start = start
).await()

/**
 *
 */
suspend fun <T> IAsyncTasksManager.doAsyncAwaitTryCatchFinally(
    tryBlock: SuspendTry<T>,
    catchBlock: SuspendCatch<T>,
    finallyBlock: SuspendFinal<T>,
    handleCancellationExceptionManually: Boolean = false,
    start: CoroutineStart = CoroutineStart.DEFAULT
): T = doAsyncTryCatchFinally(
    tryBlock = tryBlock,
    catchBlock = catchBlock,
    finallyBlock = finallyBlock,
    handleCancellationExceptionManually = handleCancellationExceptionManually,
    start = start
).await()

/**
 *
 */
suspend fun <T> IAsyncTasksManager.doAsyncAwaitTryFinally(
    tryBlock: SuspendTry<T>,
    finallyBlock: SuspendFinal<T>,
    start: CoroutineStart = CoroutineStart.DEFAULT
): T = doAsyncTryFinally(
    tryBlock = tryBlock,
    finallyBlock = finallyBlock,
    start = start
).await()


/**
 * cancel all working coroutines
 */
@Synchronized
fun IAsyncTasksManager.cancelAllAsync() {
    wasCanceled = true
    coroutineContext.cancelChildren()
    cancelationHandlers.forEach { it() }
}

/**
 * Clear all working coroutines and cancelationHandlers
 */
@Synchronized
fun IAsyncTasksManager.cleanup() {
    coroutineContext.cancelChildren()
    cancelationHandlers.clear()
}

/**
 * Add cancel all jobs handler
 *
 * @param handler - handler function () -> Unit
 */
@Synchronized
fun IAsyncTasksManager.addCancelationHandler(handler: CancelationHandler) {
    cancelationHandlers.add(handler)
}

/**
 * Remove cancel job handler
 *
 * @param handler - the reference to already added function
 */
@Synchronized
fun IAsyncTasksManager.removeCancelationHandler(handler: CancelationHandler) {
    cancelationHandlers.remove(handler)
}

/**
 * Count of cancelations function on this manager
 */
fun IAsyncTasksManager.cancelationsCount(): Int {
    return cancelationHandlers.size
}

/**
 * Was this manager canceled already
 */
@Synchronized
fun IAsyncTasksManager.isCanceled(): Boolean = wasCanceled
