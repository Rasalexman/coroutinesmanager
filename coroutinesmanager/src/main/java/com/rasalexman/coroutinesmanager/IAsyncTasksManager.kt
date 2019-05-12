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
import kotlinx.coroutines.Deferred

/**
 * IAsyncTasksManager
 */
interface IAsyncTasksManager {

    /**
     * Call the async block
     */
    suspend fun <T> async(block: suspend CoroutineScope.() -> T): Deferred<T>

    /**
     * Call async block and await for it's result
     */
    suspend fun <T> asyncAwait(block: suspend CoroutineScope.() -> T): T

    /**
     * Cancel all async deferred tasks
     */
    fun cancelAllAsync()

    /**
     * Clean up all resources
     */
    fun cleanup()

    /**
     * Does this manager job was canceled
     */
    fun isCanceled(): Boolean

    /**
     * Add cancel all jobs handler
     *
     * @param handler - handler function () -> Unit
     */
    fun addCancelationHandler(handler: CancelationHandler)

    /**
     * Remove cancel job handler
     *
     * @param handler - the reference to already added function
     */
    fun removeCancelationHandler(handler: CancelationHandler)

    /**
     * Count of cancelation functions already added
     */
    fun cancelationsCount(): Int
}
