package com.xbaimiao.fastfiller.core.workload

import com.xbaimiao.fastfiller.api.filltask.Workload
import com.xbaimiao.fastfiller.core.hook.Hook
import org.bukkit.Bukkit
import org.bukkit.Material
import pku.yim.magicblock.MagicBlock
import java.util.*

/**
 * @author: xbaimiao
 * @date: 2022年03月21日 8:40
 * @description:
 */
class MagicBlock(
    private val worldID: UUID,
    private val blockX: Int,
    private val blockY: Int,
    private val blockZ: Int,
    private val material: Material
) : Workload {

    override fun compute() {
        val world = Bukkit.getWorld(worldID)?: throw NullPointerException("world is not null")
        val block = world.getBlockAt(blockX, blockY, blockZ)
        block.type = material
        //兼容魔术方块
        if (Hook.hasMagicBlock) {
            if (!MagicBlock.getBlockContainer().isMagicBlock(block)) {
                MagicBlock.getBlockContainer().setMagicBlock(block)
            }
        }

    }

}