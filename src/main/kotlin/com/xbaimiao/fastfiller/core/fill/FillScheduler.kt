package com.xbaimiao.fastfiller.core.fill

import com.xbaimiao.easylib.task.EasyLibTask
import com.xbaimiao.easylib.util.submit
import com.xbaimiao.easylib.util.warn
import com.xbaimiao.fastfiller.core.config.FillerConfig
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 填充任务调度器
 *
 * 只有一个每 tick 运行的主线程任务, 所有方块读写都在主线程完成.
 * 老版本在异步线程里遍历区域并读写方块, 这在 Bukkit 里是未定义行为,
 * 大范围操作时容易导致区块数据错乱
 */
object FillScheduler {

    private val jobs = ConcurrentLinkedQueue<FillJob>()
    private var task: EasyLibTask? = null

    /** 当前排队中的任务数 **/
    val pendingJobs: Int get() = jobs.size

    fun start() {
        stop()
        task = submit(period = 1) { runTick() }
    }

    fun stop() {
        task?.cancel()
        task = null
        // 插件关闭时把剩余任务的收尾逻辑执行掉, 避免玩家状态卡在"填充中"
        while (true) {
            val job = jobs.poll() ?: break
            runCatching { job.onFinish() }
        }
    }

    fun submitJob(job: FillJob) {
        jobs.add(job)
    }

    private fun runTick() {
        if (jobs.isEmpty()) {
            return
        }
        val deadline = System.nanoTime() + FillerConfig.maxNanosPerTick
        val maxBlocks = FillerConfig.maxBlocksPerTick
        // 每个任务轮流跑, 避免一个大任务饿死其他玩家的任务
        var remaining = jobs.size
        while (remaining > 0 && System.nanoTime() < deadline) {
            val job = jobs.poll() ?: return
            remaining--
            val failed = runCatching { job.tick(deadline, maxBlocks) }.exceptionOrNull()
            if (failed != null) {
                warn("填充任务执行失败: ${failed.message}")
            }
            if (failed != null || job.isFinished) {
                runCatching { job.onFinish() }
            } else {
                jobs.add(job)
            }
        }
    }

}
