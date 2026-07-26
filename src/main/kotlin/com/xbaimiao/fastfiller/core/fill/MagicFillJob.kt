package com.xbaimiao.fastfiller.core.fill

import com.xbaimiao.fastfiller.core.hook.MagicBlockHook
import org.bukkit.Material
import org.bukkit.block.Block

/**
 * 用副手的魔术方块填充区域
 *
 * 不消耗容器里的方块, 放下的方块会被标记为魔术方块
 */
internal class MagicFillJob(
    region: Region,
    private val material: Material,
    private val replaceable: Set<Material>,
    finishCallback: () -> Unit,
) : RegionJob(region, finishCallback) {

    /** 实际放下的方块数 **/
    var placed = 0
        private set

    override fun handle(block: Block): Boolean {
        if (block.type !in replaceable) {
            return true
        }
        block.type = material
        placed++
        MagicBlockHook.markMagicBlock(block)
        return true
    }

}
