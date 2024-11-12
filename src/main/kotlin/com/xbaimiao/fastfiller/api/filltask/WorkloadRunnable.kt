package com.xbaimiao.fastfiller.api.filltask

import java.util.*

/**
 * This is the basic implementation of a WorkloadRunnable.
 * It processes as many Workloads every tick as the given
 * field MAX_MILLIS_PER_TICK allows.
 */
class WorkloadRunnable : Runnable {

    private val workloadDeque: Deque<Workload> = ArrayDeque()

    fun addWorkload(workload: Workload) {
        workloadDeque.add(workload)
    }

    override fun run() {
        val stopTime = System.nanoTime() + MAX_NANOS_PER_TICK
        var nextLoad: Workload? = null
        while (System.nanoTime() <= stopTime && workloadDeque.poll().also { nextLoad = it } != null) {
            nextLoad!!.compute()
        }
    }

    companion object {
        private const val MAX_MILLIS_PER_TICK = 1
        private const val MAX_NANOS_PER_TICK = (MAX_MILLIS_PER_TICK * 1000000)
    }

}