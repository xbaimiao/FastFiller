package com.xbaimiao.fastfiller.core.fill

import org.bukkit.Material
import org.bukkit.block.Block

/**
 * 把区域内的指定方块清成空气
 *
 * 容器方块(箱子/漏斗等)始终跳过, 避免清掉玩家的东西
 */
internal class ClearBlocksJob(
    region: Region,
    private val clearable: Set<Material>,
    finishCallback: () -> Unit,
) : RegionJob(region, finishCallback) {

    /** 实际清掉的方块数 **/
    var cleared = 0
        private set

    override fun handle(block: Block): Boolean {
        if (block.type !in clearable) {
            return true
        }
        if (isContainer(block)) {
            return true
        }
        block.type = Material.AIR
        cleared++
        return true
    }

}
