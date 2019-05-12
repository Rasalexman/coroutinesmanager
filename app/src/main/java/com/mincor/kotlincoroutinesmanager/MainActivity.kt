package com.mincor.kotlincoroutinesmanager

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rasalexman.coroutinesmanager.AsyncTasksManager
import com.rasalexman.coroutinesmanager.CoroutinesManager
import com.rasalexman.coroutinesmanager.IAsyncTasksManager
import com.rasalexman.coroutinesmanager.ICoroutinesManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random

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

    private fun onCancelAllCoroutinesListener() {
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
