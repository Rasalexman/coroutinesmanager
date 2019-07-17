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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
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
 */
fun ICoroutinesManager.launchOnUI(block: SuspendTry<Unit>) {
    launch(coroutineContext) { block() }.also { job -> job.invokeOnCompletion { job.cancel() } }
}

/**
 * Launch some suspend function on UI thread with try catch block
 */
fun ICoroutinesManager.launchOnUITryCatch(
    tryBlock: SuspendTry<Unit>,
    catchBlock: SuspendCatch<Unit>,
    handleCancellationExceptionManually: Boolean = false
) = launchOnUI {
    tryCatch(tryBlock, catchBlock, handleCancellationExceptionManually)
}

/**
 * Launch some suspend function on UI thread with try catch finally block
 */
fun ICoroutinesManager.launchOnUITryCatchFinally(
    tryBlock: SuspendTry<Unit>,
    catchBlock: SuspendCatch<Unit>,
    finallyBlock: SuspendFinal<Unit>,
    handleCancellationExceptionManually: Boolean = false
) = launchOnUI {
    tryCatchFinally(tryBlock, catchBlock, finallyBlock, handleCancellationExceptionManually)
}

/**
 * Launch some suspend function on UI thread with try finally block
 */
fun ICoroutinesManager.launchOnUITryFinally(
    tryBlock: SuspendTry<Unit>,
    finallyBlock: SuspendFinal<Unit>
) = launchOnUI {
    tryFinally(tryBlock, finallyBlock)
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