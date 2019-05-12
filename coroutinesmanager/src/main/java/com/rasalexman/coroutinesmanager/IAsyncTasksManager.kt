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

    fun cancelationsCount(): Int
}
