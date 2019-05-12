package com.rasalexman.coroutinesmanager

/**
 * Storage of coroutine providers
 */
object CoroutinesProvider {
    /**
     * Main UI Thread Provider
     */
    val UI = kotlinx.coroutines.Dispatchers.Main
    /**
     * Common Thread provider
     */
    val COMMON = kotlinx.coroutines.Dispatchers.Default
}