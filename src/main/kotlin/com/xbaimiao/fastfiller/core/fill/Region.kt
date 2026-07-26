package com.xbaimiao.fastfiller.core.fill

import org.bukkit.Location
import org.bukkit.World
import kotlin.math.max
import kotlin.math.min

/**
 * 由两个选点构成的立方体区域
 */
class Region(val world: World, cornerA: Location, cornerB: Location) {

    val minX: Int = min(cornerA.blockX, cornerB.blockX)
    val minY: Int = min(cornerA.blockY, cornerB.blockY)
    val minZ: Int = min(cornerA.blockZ, cornerB.blockZ)
    val maxX: Int = max(cornerA.blockX, cornerB.blockX)
    val maxY: Int = max(cornerA.blockY, cornerB.blockY)
    val maxZ: Int = max(cornerA.blockZ, cornerB.blockZ)

    val sizeX: Int get() = maxX - minX + 1
    val sizeY: Int get() = maxY - minY + 1
    val sizeZ: Int get() = maxZ - minZ + 1

    /**
     * 区域内方块总数, 用 Long 避免大范围时溢出
     */
    val volume: Long get() = sizeX.toLong() * sizeY.toLong() * sizeZ.toLong()

    companion object {

        /**
         * 两个选点必须在同一个已加载的世界
         */
        fun of(cornerA: Location, cornerB: Location): Region? {
            val world = cornerA.world ?: return null
            if (cornerB.world?.uid != world.uid) {
                return null
            }
            return Region(world, cornerA, cornerB)
        }

    }

}
