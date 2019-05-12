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
 * AsyncTasksManager for doing some async work
 */
open class AsyncTasksManager : IAsyncTasksManager, CoroutineScope {

    /**
     * Cancelation handlers local store
     */
    private val cancelationHandlers = mutableSetOf<CancelationHandler>()

    /**
     * Async Job
     */
    private val job = SupervisorJob()

    /**
     * Was Canceled already
     */
    private var wasCanceled: Boolean = false

    /**
     * CoroutineContext to use in this manager. It's async
     */
    override val coroutineContext: CoroutineContext
        get() = CoroutinesProvider.COMMON + job

    /**
     * launch coroutine on common pool job
     *
     * @param block
     * The worker block to invoke
     */
    @Synchronized
    override suspend fun <T> async(block: suspend CoroutineScope.() -> T): Deferred<T> {
        wasCanceled = false
        return async(coroutineContext) { block() }.also { job -> job.invokeOnCompletion { job.cancel() } }
    }

    /**
     * launch coroutine on common pool job
     *
     * @param block
     * The worker block to invoke
     */
    @Synchronized
    override suspend fun <T> asyncAwait(block: suspend CoroutineScope.() -> T): T {
        wasCanceled = false
        return async(block).await()
    }

    /**
     * cancel all working coroutines
     */
    @Synchronized
    override fun cancelAllAsync() {
        wasCanceled = true
        coroutineContext.cancelChildren()
        cancelationHandlers.forEach { it() }
    }

    /**
     * Clear all working coroutines and cancelationHandlers
     */
    @Synchronized
    override fun cleanup() {
        coroutineContext.cancelChildren()
        cancelationHandlers.clear()
    }

    /**
     * Add cancel all jobs handler
     *
     * @param handler - handler function () -> Unit
     */
    @Synchronized
    override fun addCancelationHandler(handler: CancelationHandler) {
        cancelationHandlers.add(handler)
    }

    /**
     * Remove cancel job handler
     *
     * @param handler - the reference to already added function
     */
    @Synchronized
    override fun removeCancelationHandler(handler: CancelationHandler) {
        cancelationHandlers.remove(handler)
    }

    /**
     * Count of cancelations function on this manager
     */
    override fun cancelationsCount(): Int {
        return cancelationHandlers.size
    }

    /**
     * Was this manager canceled already
     */
    @Synchronized
    override fun isCanceled(): Boolean = wasCanceled
}