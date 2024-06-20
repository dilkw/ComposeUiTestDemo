package com.example.composeuitestdemo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

/**
 * ViewModel的基类
 */
abstract class BaseViewModel(private val dispatcher: CoroutineDispatcher): ViewModel() {

    //job列表
    private val jobs: MutableList<Job> = mutableListOf<Job>()

    //标记网络loading状态
    val isLoading = MutableLiveData<Boolean>()

    protected fun launchCoroutine(block: suspend CoroutineScope.() -> Unit) = viewModelScope.launch(dispatcher) {
        ensureActive()
        block.invoke(this)
    }.addTo(jobs)

    //取消全部协程
    override fun onCleared() {
        jobs.forEach { it.cancel() }
        super.onCleared()
    }
}

fun Job.addTo(jobs: MutableList<Job>): Job {
    jobs.add(this)
    return this
}