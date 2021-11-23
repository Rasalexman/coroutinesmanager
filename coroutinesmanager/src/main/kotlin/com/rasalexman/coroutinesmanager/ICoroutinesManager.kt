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
interface ICoroutinesManager : IAsyncTasksManager {
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
     * Cancelation handlers local store
     */
    override val cancelationHandlers: MutableSet<CancelationHandler>?
        get() = CoroutinesProvider.cancelationHandlersSet

    /**
     * Coroutine Context
     * launch single coroutine job on main thread
     */
    override val coroutineContext: CoroutineContext
        get() = CoroutinesProvider.UI
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
): Job {
    return launch(start = start, block = block)//.also { job -> job.invokeOnCompletion { job.cancel() } }
}

/**
 * Launch some suspend function on UI thread with optional catch and finally blocks
 *
 * @param tryBlock      - main working block
 * @param catchBlock    - block where throwable will be handled (Optional)
 * @param finallyBlock  - there is a block where exception can passed as param `it:Throwable?` (Optional)
 *
 * @param handleCancellationExceptionManually - does we need to handle cancelation manually
 * @param start         - starting coroutine strategy [CoroutineStart.DEFAULT] by default
 */
fun ICoroutinesManager.launchOnUiBy(
    tryBlock: SuspendTry<Unit>,
    catchBlock: SuspendCatch<Unit>? = null,
    finallyBlock: SuspendFinal<Unit>? = null,
    handleCancellationExceptionManually: Boolean = false,
    start: CoroutineStart = CoroutineStart.DEFAULT
): Job {
    return when {
        catchBlock == null && finallyBlock == null -> launchOnUI(start, tryBlock)
        finallyBlock == null && catchBlock != null -> launchOnUITryCatch(tryBlock, catchBlock, handleCancellationExceptionManually, start)
        finallyBlock != null && catchBlock != null -> launchOnUITryCatchFinally(tryBlock, catchBlock, finallyBlock, handleCancellationExceptionManually, start)
        finallyBlock != null && catchBlock == null -> launchOnUITryFinally(tryBlock, finallyBlock, start)
        else -> launchOnUI(start, tryBlock)
    }
}

/**
 * Launch some suspend function on AsyncCoroutineContext with optional catch and finally blocks
 *
 * @param tryBlock      - main working block
 * @param catchBlock    - block where throwable will be handled (Optional)
 * @param finallyBlock  - there is a block where exception can passed as param `it:Throwable?` (Optional)
 *
 * @param start         - starting coroutine strategy [CoroutineStart.DEFAULT] by default
 */
fun ICoroutinesManager.launchOnUiAsyncBy(
    tryBlock: SuspendTry<Unit>,
    catchBlock: SuspendCatch<Unit>? = null,
    finallyBlock: SuspendFinal<Unit>? = null,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    startAsync: CoroutineStart = CoroutineStart.DEFAULT
): Job {
    return when {
        catchBlock == null && finallyBlock == null -> launchOnUIAsyncAwait(start, startAsync, tryBlock)
        finallyBlock == null && catchBlock != null -> launchOnUITryCatchAsyncAwait(start, startAsync, tryBlock, catchBlock)
        finallyBlock != null && catchBlock != null -> launchOnUITryCatchFinallyAsyncAwait(start, startAsync, tryBlock, catchBlock, finallyBlock)
        finallyBlock != null && catchBlock == null -> launchOnUITryFinallyAsyncAwait(start, startAsync, tryBlock, finallyBlock)
        else -> launchOnUIAsyncAwait(start, startAsync, tryBlock)
    }
}

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
 * Launch some suspend function on UI thread with try finally block. If there is an error occurs
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
 * Launch Async operation on separated thread launched ou UI coroutine
 *
 * @param startAsync         - starting async coroutine strategy [CoroutineStart]
 * @param startUI            - starting UI coroutine strategy [CoroutineStart]
 * @param block              - main working block
 */
fun ICoroutinesManager.launchOnUIAsyncAwait(
    startUI: CoroutineStart = CoroutineStart.DEFAULT,
    startAsync: CoroutineStart = CoroutineStart.DEFAULT,
    block: SuspendTry<Unit>
) = launchOnUI(startUI) {
    doAsyncAwait(startAsync, block)
}

/**
 * Launch Async operation on separated thread with try catch
 *
 * @param tryBlock      - main working block
 * @param catchBlock    - block where throwable will be handled it always catch exception on UI
 *
 * @param startAsync         - starting async coroutine strategy [CoroutineStart]
 * @param startUI            - starting UI coroutine strategy [CoroutineStart]
 */
fun ICoroutinesManager.launchOnUITryCatchAsyncAwait(
    startUI: CoroutineStart = CoroutineStart.DEFAULT,
    startAsync: CoroutineStart = CoroutineStart.DEFAULT,
    tryBlock: SuspendTry<Unit>,
    catchBlock: SuspendCatch<Unit>
) = launchOnUITryCatch(tryBlock = {
    doAsyncAwait(startAsync, tryBlock)
}, catchBlock = catchBlock, start = startUI)

/**
 * Launch Async operation on separated thread with try catch finally blocks
 *
 * @param tryBlock      - async await working block
 * @param catchBlock    - block where throwable will be handled on UI
 * @param finallyBlock  - there is a block where exception can passed as param `it:Throwable?` on UI
 *
 * @param startAsync         - starting async coroutine strategy [CoroutineStart]
 * @param startUI            - starting UI coroutine strategy [CoroutineStart]
 */
fun ICoroutinesManager.launchOnUITryCatchFinallyAsyncAwait(
    startUI: CoroutineStart = CoroutineStart.DEFAULT,
    startAsync: CoroutineStart = CoroutineStart.DEFAULT,
    tryBlock: SuspendTry<Unit>,
    catchBlock: SuspendCatch<Unit>,
    finallyBlock: SuspendFinal<Unit>
) = launchOnUITryCatchFinally(tryBlock = {
    doAsyncAwait(startAsync, tryBlock)
}, catchBlock = catchBlock, finallyBlock = finallyBlock, start = startUI)

/**
 * Launch Async operation on separated thread with try finally blocks
 *
 * @param tryBlock      - async await working block
 * @param finallyBlock  - there is a block where exception can passed as param `it:Throwable?` on UI
 *
 * @param startAsync         - starting async coroutine strategy [CoroutineStart]
 * @param startUI            - starting UI coroutine strategy [CoroutineStart]
 */
fun ICoroutinesManager.launchOnUITryFinallyAsyncAwait(
    startUI: CoroutineStart = CoroutineStart.DEFAULT,
    startAsync: CoroutineStart = CoroutineStart.DEFAULT,
    tryBlock: SuspendTry<Unit>,
    finallyBlock: SuspendFinal<Unit>
) = launchOnUITryFinally(tryBlock = {
    doAsyncAwait(startAsync, tryBlock)
}, finallyBlock = finallyBlock, start = startUI)

/**
 * Cancel all working UI coroutines
 */
fun ICoroutinesManager.cancelUICoroutines() {
    coroutineContext.cancelChildren()
}

/**
 * Cancel all coroutines in this Scope with Async jobs
 */
fun ICoroutinesManager.cancelAllCoroutines() {
    cancelUICoroutines()
    cancelAllAsync()
}

/**
 * Was all coroutines on UI & Async canceled
 */
val ICoroutinesManager.isCancelled: Boolean
   get() = asyncJob.children.all { it.isCancelled } && job.children.all { it.isCancelled }


/**
 * Was all coroutines on UI & Async complete
 */
val ICoroutinesManager.isCompleted: Boolean
    get() = !this.job.children.any() && !asyncJob.children.any()
