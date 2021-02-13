# Kotlin Coroutines Manager
[![Download](https://api.bintray.com/packages/sphc/KotlinCoroutinesManager/coroutinesmanager/images/download.svg)](https://bintray.com/sphc/KotlinCoroutinesManager/coroutinesmanager/_latestVersion) [![Kotlin 1.4.30](https://img.shields.io/badge/Kotlin-1.4.30-blue.svg)](http://kotlinlang.org) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/12165b84f5e14ade83ebbf508cf17cbc)](https://app.codacy.com/app/Rasalexman/coroutinesmanager?utm_source=github.com&utm_medium=referral&utm_content=Rasalexman/coroutinesmanager&utm_campaign=Badge_Grade_Dashboard) [![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/KotlinBy/awesome-kotlin)

Some helpful kotlin coroutines manager classes and extensions. You can turn every function into coroutine function with powerful try-catch-finally blocks

```kotlin
class MainActivity(
    private val coroutinesManager: ICoroutinesManager = CoroutinesManager()
) : AppCompatActivity(), ICoroutinesManager by coroutinesManager {

    // Async worker may be into your DI as uses case
    private val asyncWorker = AsyncWorker()
    
    fun tryCatch() = launchOnUITryCatch(tryBlock = {
        // do some work on ui
        val result = asyncWorker.awaitSomeHardWorkToComplete()
    }, catchBlock = {
        // catch every exception
    })
    
    // Sinse version 1.2.0
    private fun launchUiAsyncAction() = launchOnUITryCatchFinallyAsyncAwait(
        tryBlock = {

            delay(3000L)
            if (Random.nextInt(1, 20) % 2 == 0)
                throw RuntimeException("THERE IS AN ERROR")

            println("----> launchUiAsyncAction 'TRY' BLOCK COMPLETE")
        }, catchBlock = {
            println("----> ASYNC 'CATCH' BLOCK ${Thread.currentThread().name}")
            titleTextView.text = it.message
        }, finallyBlock = {

        })
    
    fun tryFinally() = launchOnUITryFinally(tryBlock = {
        // try some action that maybe produce an exceptions
    }, finallyBlock = {
        // do work when coroutine is done
    })
    
    fun tryCatchFinally() = launchOnUITryCatchFinally(tryBlock = {
        val resultForAwait = asyncWorker.createDeferrerForAwait()
        val finalResult: String = resultForAwait.await()    
    }, catchBlock = {
        // error here
    }, finallyBlock = {
        // final action here
    })
    
    fun doOnUiOnly() = launchOnUI { 
        // do some work on UI thread
    }   
}
    
class AsyncWorker(
    // You can store this class as global singleton into your DI framework
    private val asyncTaskManager: IAsyncTasksManager = AsyncTasksManager()
) : IAsyncTasksManager by asyncTaskManager {

    suspend fun awaitSomeHardWorkToComplete() = doTryCatchAsyncAwait(
        tryBlock = {
            println("----> ASYNC 'TRY' BLOCK")

            delay(3000L)
            if (Random.nextInt(1, 20) % 2 == 0)
                throw RuntimeException("THERE IS AN ERROR")

            "OPERATION COMPLETE"
        },
        catchBlock = {
            println("----> ASYNC 'CATCH' BLOCK")
            throw it
        }
    )
    
    suspend fun <T> createDeferrerForAwait(): Deferred<T> = doAsync {
        "SOME WORK HERE"
    }
}
```

You can use ICoroutinesManager as base implementation for your presenter in MVP or MVI architecture. Use IAsyncTasksManager as base implementation for hard work or async operations as UseCases and another tasks. 

Gradle: 
```kotlin
implementation 'com.rasalexman.coroutinesmanager:coroutinesmanager:x.y.z'
```

# License

MIT License

Copyright (c) 2019 Alexandr Minkin (sphc@yandex.ru)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
