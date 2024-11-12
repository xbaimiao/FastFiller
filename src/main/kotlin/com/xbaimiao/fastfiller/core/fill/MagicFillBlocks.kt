package com.xbaimiao.fastfiller.core.fill

import com.xbaimiao.fastfiller.FastFiller
import com.xbaimiao.fastfiller.api.filltask.VolumeFiller
import com.xbaimiao.fastfiller.core.BlockCompare
import com.xbaimiao.fastfiller.core.workload.MagicBlock
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author: xbaimiao
 * @date: 2022年03月19日 18:52
 * @description:
 */
class MagicFillBlocks(
    private val player: Player
) : VolumeFiller {

    override fun fill(cornerA: Location, cornerB: Location, material: Material) {
        if (!(cornerA.world == cornerB.world && cornerA.world != null)) {
            throw NullPointerException("world not is null")
        }
        val box = BlockCompare(cornerA, cornerB)
        val world = cornerA.world!!
        val workloadRunnable = FastFiller.workLoadPool.next()
        a@ for (x in box.minX..box.maxX) {
            for (y in box.minY..box.maxY) {
                for (z in box.minZ..box.maxZ) {
                    if (world.getBlockAt(x, y, z).type.let { it != Material.AIR && it != Material.WATER && it != Material.LAVA }) {
                        continue
                    }
                    val magicBlock = MagicBlock(world.uid, x, y, z, material)
                    workloadRunnable.addWorkload(magicBlock)
                }
            }
        }
        FastFiller.dataContainer.setInFill(player, false)
    }

}