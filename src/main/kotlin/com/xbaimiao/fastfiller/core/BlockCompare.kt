package com.xbaimiao.fastfiller.core

import org.bukkit.Location
import kotlin.math.max
import kotlin.math.min

/**
 * @author: xbaimiao
 * @date: 2022年03月17日 21:20
 * @description:
 */
class BlockCompare(location1: Location, location2: Location) {

    val minX: Int = min(location1.blockX, location2.blockX)
    val minY: Int = min(location1.blockY, location2.blockY)
    val minZ: Int = min(location1.blockZ, location2.blockZ)
    val maxX: Int = max(location1.blockX, location2.blockX)
    val maxY: Int = max(location1.blockY, location2.blockY)
    val maxZ: Int = max(location1.blockZ, location2.blockZ)

}