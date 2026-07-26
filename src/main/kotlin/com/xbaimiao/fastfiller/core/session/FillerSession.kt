package com.xbaimiao.fastfiller.core.session

import org.bukkit.Location

/**
 * 单个玩家的选点与操作状态
 */
class FillerSession {

    /** 第一个选点 **/
    var firstPoint: Location? = null

    /** 第二个选点 **/
    var secondPoint: Location? = null

    /** 是否有正在执行的填充任务 **/
    var filling: Boolean = false

    /** 上次触发操作的时间戳, 用于冷却 **/
    var lastUseTime: Long = 0

    /** 上次交互的时间戳, 用于交互去重 **/
    var lastInteractTime: Long = 0

    /**
     * 两个选点都选好了才返回
     */
    fun points(): Pair<Location, Location>? {
        val first = firstPoint ?: return null
        val second = secondPoint ?: return null
        return first to second
    }

}
