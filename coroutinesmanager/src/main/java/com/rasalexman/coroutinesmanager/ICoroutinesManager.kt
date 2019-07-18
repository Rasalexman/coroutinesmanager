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
 * ICoroutinesManager
 */
interface ICoroutinesManager : CoroutineScope {
    /**
     * Working job
     *
     * You better to override this val in implementing classes,
     * because when you create a new coroutine you will always
     * work with providing single job that store all of your coroutines
     * no matter if you work on UI or COMMON
     */
    val job: Job
        get() = CoroutinesProvider.supervisorJob

    /**
     * Coroutine Context
     * launch single coroutine job on main thread
     */
    override val coroutineContext: CoroutineContext
        get() = CoroutinesProvider.UI + job
}

/**
 * Launch some suspend function on UI thread
 *
 * @param start         - starting coroutine strategy [CoroutineStart.DEFAULT] by default
 * @param block         - main block for execute on UI Thread
 */
fun ICoroutinesManager.launchOnUI(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: SuspendTry<Unit>
) = launch(coroutineContext, start, block).also { job -> job.invokeOnCompletion { job.cancel() } }


/**
 * Launch some suspend function on UI thread with try catch block
 *
 * @param tryBlock      - main working block
 * @param catchBlock    - block where throwable will be handled.
 *
 * @param handleCancellationExceptionManually - does we need to handle cancelation manually
 * @param start         - starting coroutine strategy [CoroutineStart.DEFAULT] by default
 */
fun ICoroutinesManager.launchOnUITryCatch(
    tryBlock: SuspendTry<Unit>,
    catchBlock: SuspendCatch<Unit>,
    handleCancellationExceptionManually: Boolean = false,
    start: CoroutineStart = CoroutineStart.DEFAULT
) = launchOnUI(start) {
    tryCatch(
        tryBlock = tryBlock,
        catchBlock = catchBlock,
        handleCancellationExceptionManually = handleCancellationExceptionManually
    )
}

/**
 * Launch some suspend function on UI thread with try catch finally block
 *
 * @param tryBlock      - main working block
 * @param catchBlock    - block where throwable will be handled
 * @param finallyBlock  - there is a block where exception can passed as param `it:Throwable?`
 *
 * @param handleCancellationExceptionManually - does we need to handle cancelation manually
 * @param start         - starting coroutine strategy [CoroutineStart.DEFAULT] by default
 */
fun ICoroutinesManager.launchOnUITryCatchFinally(
    tryBlock: SuspendTry<Unit>,
    catchBlock: SuspendCatch<Unit>,
    finallyBlock: SuspendFinal<Unit>,
    handleCancellationExceptionManually: Boolean = false,
    start: CoroutineStart = CoroutineStart.DEFAULT
) = launchOnUI(start) {
    tryCatchFinally(
        tryBlock = tryBlock,
        catchBlock = catchBlock,
        finallyBlock = finallyBlock,
        handleCancellationExceptionManually = handleCancellationExceptionManually
    )
}

/**
 * Launch some suspend function on UI thread with try finally block. If there is an error occures
 * It will be throwed and passed into [finallyBlock] as parameter
 *
 * @param tryBlock      - main working block
 * @param finallyBlock  - there is a block where exception can exist as param `it:Throwable?`
 * @param start         - starting coroutine strategy [CoroutineStart.DEFAULT] by default
 */
fun ICoroutinesManager.launchOnUITryFinally(
    tryBlock: SuspendTry<Unit>,
    finallyBlock: SuspendFinal<Unit>,
    start: CoroutineStart = CoroutineStart.DEFAULT
) = launchOnUI(start) {
    tryFinally(
        tryBlock = tryBlock,
        finallyBlock = finallyBlock
    )
}

/**
 * Cancel all working coroutines
 */
fun ICoroutinesManager.cancelUICoroutines() {
    coroutineContext.cancelChildren()
}

/**
 * Is All coroutines on UI complete
 */
fun ICoroutinesManager.isCompleted(): Boolean {
    return !this.job.children.any()
}