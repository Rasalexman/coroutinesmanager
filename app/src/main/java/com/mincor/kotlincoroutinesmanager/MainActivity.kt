package com.mincor.kotlincoroutinesmanager

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.rasalexman.coroutinesmanager.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.random.Random

class MainActivity : AppCompatActivity(R.layout.activity_main), ICoroutinesManager {

    private val titleTextView: TextView get() = findViewById(R.id.titleTextView)
    private val repeateButton: Button get() = findViewById(R.id.repeateButton)
    private val flowTitleView: TextView get() = findViewById(R.id.flowTitleView)

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        titleTextView.text = "Click to start coroutines scope"
        repeateButton.setOnClickListener {
            if (!isCompleted) {
                onCancelAllCoroutinesListener()
            } else {
                onRepeateButtonClickListener()
            }
        }

        println("-----> START WORK")
        launchUiAsyncAction()
        println("-----> AFTER WORK")
    }

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
            println("----> ASYNC 'FINALLY' BLOCK ${Thread.currentThread().name}")
        })

    /*
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
    }*/

    @SuppressLint("SetTextI18n")
    fun doOnUiOnly() = launchOnUI {
        flowTitleView.text = getString(R.string.start_title)
        flow {
            for (i in 0 until 1000) {
                delay(100)
                emit(i)
            }
        }
            .flowOn(CoroutinesProvider.IO)
            .collect {
                flowTitleView.text = "Finish work with value $it"
            }
    }


    private fun onCancelAllCoroutinesListener() {
        cancelAllCoroutines()
    }

    @SuppressLint("SetTextI18n")
    private fun onRepeateButtonClickListener() = launchOnUITryCatchFinally(
        tryBlock = {
            println("----> START <------")
            println("----> UI 'TRY' BLOCK")

            titleTextView.text = "Start doing hard work"
            repeateButton.text = getString(R.string.stop_title)
            // add cancelation function on this worker
            addCancelationHandler(::onAllJobsWasCanceledHandler)
            // Await result
            val result = doWithTryCatchAsync(
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
            // Show result
            titleTextView.text = result
        }, catchBlock = {
            println("----> UI 'CATCH' BLOCK")
            repeateButton.text = getString(R.string.repeate_title)
            titleTextView.text = it.message
        }, finallyBlock = {
            repeateButton.text = getString(R.string.start_title)
            println("----> UI 'FINALLY' BLOCK")
        })

    private fun onAllJobsWasCanceledHandler() {
        if (isCancelled) {
            titleTextView.text = getString(R.string.caceled_title)
        }
        repeateButton.text = getString(R.string.start_title)
        removeCancelationHandler(::onAllJobsWasCanceledHandler)
    }
}
