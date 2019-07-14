package com.mincor.kotlincoroutinesmanager

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rasalexman.coroutinesmanager.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.random.Random

@ExperimentalCoroutinesApi
class MainActivity(
    private val coroutinesManager: ICoroutinesManager = CoroutinesManager()
) : AppCompatActivity(), ICoroutinesManager by coroutinesManager {

    // Async worker may be into your DI as uses case
    private val asyncWorker = AsyncWorker()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        titleTextView.text = "Click to start coroutines scope"
        repeateButton.setOnClickListener {
            if (!coroutinesManager.isCompleted()) {
                onCancelAllCoroutinesListener()
            } else {
                onRepeateButtonClickListener()
            }
        }
    }

    fun tryCatch() = launchOnUITryCatch(tryBlock = {
        // do some work on ui
    }, catchBlock = {
        // catch every exception
    })

    fun tryFinally() = launchOnUITryFinally(tryBlock = {

    }, finallyBlock = {
        // do work when coroutine is done
    })

    fun tryCatchFinally() = launchOnUITryCatchFinally(tryBlock = {

    }, catchBlock = {

    }, finallyBlock = {

    })

    override fun onResume() {
        super.onResume()
        //doOnUiOnly()
    }

    @UseExperimental(InternalCoroutinesApi::class)
    fun doOnUiOnly() = launchOnUI {

        flowTitleView.text = getString(R.string.start_title)

        flow {
            for (i in 0 until 1000) {
                kotlinx.coroutines.delay(100)
                emit(i)
            }
        }
            .flowOn(CoroutinesProvider.IO)
            .collect {
                flowTitleView.text = "Finish work with value $it"
            }
    }


    private fun onCancelAllCoroutinesListener() {
        coroutinesManager.cancelAllCoroutines()
        asyncWorker.cancelAllAsync()
    }

    @SuppressLint("SetTextI18n")
    private fun onRepeateButtonClickListener() = launchOnUITryCatchFinally(
        tryBlock = {
            titleTextView.text = "Start doing hard work"
            repeateButton.text = getString(R.string.stop_title)
            // add cancelation function on this worker
            asyncWorker.addCancelationHandler(::onAllJobsWasCanceledHandler)
            // Await result
            val result = asyncWorker.awaitSomeHardWorkToComplete()
            // Show result
            titleTextView.text = result
        }, catchBlock = {
            println("THERE IS CATCH BLOCK")
            repeateButton.text = getString(R.string.repeate_title)
            titleTextView.text = it.message
        }, finallyBlock = {
            repeateButton.text = getString(R.string.start_title)
            println("THERE IS FINALLY BLOCK")
        })

    private fun onAllJobsWasCanceledHandler() {
        if (asyncWorker.isCanceled()) {
            titleTextView.text = getString(R.string.caceled_title)
        }
        repeateButton.text = getString(R.string.start_title)
        asyncWorker.removeCancelationHandler(::onAllJobsWasCanceledHandler)
    }
}

class AsyncWorker(
    // You can store this class as global singleton into your DI framework
    private val asyncTaskManager: IAsyncTasksManager = AsyncTasksManager()
) : IAsyncTasksManager by asyncTaskManager {

    suspend fun awaitSomeHardWorkToComplete() = asyncAwait {
        Thread.sleep(5000)
        if (Random.nextInt(1, 20) % 2 == 0)
            throw RuntimeException("THERE IS AN ERROR")

        "OPERATION COMPLETE"
    }

}
