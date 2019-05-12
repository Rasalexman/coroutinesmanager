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
 * Simple CoroutinesManager to use with your regular classes
 */
open class CoroutinesManager : ICoroutinesManager, CoroutineScope {

    /**
     * Working job
     */
    private val job = SupervisorJob()

    /**
     * CoroutineContext UI thread
     */
    override val coroutineContext: CoroutineContext
        get() = CoroutinesProvider.UI + job

    /**
     * launch single coroutine job on main thread
     */
    @Synchronized
    override fun launchOnUI(block: suspend CoroutineScope.() -> Unit) {
        launch(coroutineContext) { block() }.also { job -> job.invokeOnCompletion { job.cancel() } }
    }

    /**
     * launch single coroutine job on main thread with try catch block
     */
    @Synchronized
    override fun launchOnUITryCatch(
            tryBlock: suspend CoroutineScope.() -> Unit,
            catchBlock: suspend CoroutineScope.(Throwable) -> Unit,
            handleCancellationExceptionManually: Boolean) {
        launchOnUI { tryCatch(tryBlock, catchBlock, handleCancellationExceptionManually) }
    }

    /**
     * launch single coroutine job on main thread with try catch and finally block
     */
    @Synchronized
    override fun launchOnUITryCatchFinally(
            tryBlock: suspend CoroutineScope.() -> Unit,
            catchBlock: suspend CoroutineScope.(Throwable) -> Unit,
            finallyBlock: suspend CoroutineScope.() -> Unit,
            handleCancellationExceptionManually: Boolean) {
        launchOnUI { tryCatchFinally(tryBlock, catchBlock, finallyBlock, handleCancellationExceptionManually) }
    }

    /**
     * launch single coroutine job on main thread with try and finally block
     */
    @Synchronized
    override fun launchOnUITryFinally(
            tryBlock: suspend CoroutineScope.() -> Unit,
            finallyBlock: suspend CoroutineScope.() -> Unit,
            suppressCancellationException: Boolean) {
        launchOnUI { tryFinally(tryBlock, finallyBlock, suppressCancellationException) }
    }

    /**
     * Cancel all working coroutines
     */
    @Synchronized
    override fun cancelAllCoroutines() {
        coroutineContext.cancelChildren()
    }

    /**
     * Ensure that all children's is active
     */
    @Synchronized
    override fun isCompleted() = !this.job.children.any()


}