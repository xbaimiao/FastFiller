package com.xbaimiao.fastfiller.core.fill

/**
 * 区域遍历游标
 *
 * 老版本是一次性把整个区域的方块任务对象全塞进队列,
 * 500x500x256 这种范围会瞬间产生上千万个对象直接把内存打满.
 * 这里改成按需推进坐标, 内存占用是常数
 */
class RegionCursor(private val region: Region) {

    var x: Int = region.minX
        private set
    var y: Int = region.minY
        private set
    var z: Int = region.minZ
        private set

    private var finished = false

    /** 已经遍历完区域内所有坐标 **/
    val isFinished: Boolean get() = finished

    /**
     * 推进到下一个坐标
     *
     * @return false 代表已经遍历完
     */
    fun next(): Boolean {
        if (finished) {
            return false
        }
        z++
        if (z > region.maxZ) {
            z = region.minZ
            y++
            if (y > region.maxY) {
                y = region.minY
                x++
                if (x > region.maxX) {
                    finished = true
                    return false
                }
            }
        }
        return true
    }

}
