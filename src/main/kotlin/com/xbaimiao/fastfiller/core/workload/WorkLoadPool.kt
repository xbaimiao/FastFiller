package com.xbaimiao.fastfiller.core.workload

import com.xbaimiao.easylib.util.plugin
import com.xbaimiao.fastfiller.api.filltask.WorkloadRunnable
import org.bukkit.Bukkit

/**
 * @author: xbaimiao
 * @date: 2022年03月17日 21:31
 * @description:
 */
class WorkLoadPool(size: Int) {

    private val list = ArrayList<WorkloadRunnable>()
    private var index = 0

    init {
        for (a in 0..size) {
            list.add(WorkloadRunnable().also {
                Bukkit.getScheduler().runTaskTimer(plugin, it, 1, 1)
            })
        }
    }

    fun next(): WorkloadRunnable {
        if (index >= list.size) {
            index = 0
        }
        return list[index++]
    }

}