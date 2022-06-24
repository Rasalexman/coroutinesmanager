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

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.SupervisorJob

/**
 * Storage of coroutine providers
 */
object CoroutinesProvider {

    private var _ioDispatcher: CoroutineDispatcher = kotlinx.coroutines.Dispatchers.IO
    private var _commonDispatcher: CoroutineDispatcher = kotlinx.coroutines.Dispatchers.Default
    private var _mainDispatcher: CoroutineDispatcher = kotlinx.coroutines.Dispatchers.Main

    /**
     * Supervisor job for UI operations
     */
    val supervisorJob by lazy { SupervisorJob() }

    /**
     * Supervisor job for Common operations
     */
    val commonSupervisorJob by lazy { SupervisorJob() }

    /**
     * Supervisor job for async operations
     */
    val asyncSupervisorJob by lazy { SupervisorJob() }

    /**
     * Default cancelation handlers set
     */
    val cancelationHandlersSet by lazy { mutableSetOf<CancelationHandler>() }

    /**
     * Main UI Thread Provider
     */
    val UI: CoroutineDispatcher
        get() = _mainDispatcher
    /**
     * Common Thread provider
     */
    val COMMON: CoroutineDispatcher
        get() = _commonDispatcher

    /**
     * IO Thread provider
     */
    val IO: CoroutineDispatcher
        get() = _ioDispatcher

    /**
     * Setup default manager dispatchers
     */
    fun setupDefaultDispatchers(
        ioDispatcher: CoroutineDispatcher = kotlinx.coroutines.Dispatchers.IO,
        commonDispatcher: CoroutineDispatcher = kotlinx.coroutines.Dispatchers.Default,
        mainDispatcher: CoroutineDispatcher = kotlinx.coroutines.Dispatchers.Main
        ) {
        _ioDispatcher = ioDispatcher
        _commonDispatcher = commonDispatcher
        _mainDispatcher = mainDispatcher
    }

}