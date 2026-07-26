package com.xbaimiao.fastfiller.core.fill

/**
 * 一次填充 / 清理任务
 *
 * 由 [FillScheduler] 在主线程按时间预算分片执行
 */
interface FillJob {

    /** 任务是否已经完成 **/
    val isFinished: Boolean

    /**
     * 执行一小段任务
     *
     * @param deadline 本次允许运行到的 [System.nanoTime] 时间点
     * @param maxBlocks 本次最多处理的方块数
     */
    fun tick(deadline: Long, maxBlocks: Int)

    /**
     * 任务结束时回调, 无论正常结束还是被取消都会执行
     */
    fun onFinish()

}
