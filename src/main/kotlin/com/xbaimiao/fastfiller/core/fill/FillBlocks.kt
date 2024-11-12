package com.xbaimiao.fastfiller.core.fill

import com.xbaimiao.fastfiller.FastFiller
import com.xbaimiao.fastfiller.api.filltask.VolumeFiller
import com.xbaimiao.fastfiller.api.Container
import com.xbaimiao.fastfiller.core.BlockCompare
import com.xbaimiao.fastfiller.core.workload.PlaceBlock
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author: xbaimiao
 * @date: 2022年03月17日 20:58
 * @description:
 */
class FillBlocks(
    private val pair: Pair<Material, Int>,
    private val container: Container,
    private val player: Player
) : VolumeFiller {

    override fun fill(cornerA: Location, cornerB: Location, material: Material) {
        if (!(cornerA.world == cornerB.world && cornerA.world != null)) {
            throw NullPointerException("world not is null")
        }
        val box = BlockCompare(cornerA, cornerB)
        var num = 0
        val world = cornerA.world!!
        val workloadRunnable = FastFiller.workLoadPool.next()
        a@ for (x in box.minX..box.maxX) {
            for (y in box.minY..box.maxY) {
                for (z in box.minZ..box.maxZ) {
                    if (world.getBlockAt(x, y, z).type != Material.AIR) {
                        continue
                    }
                    num++
                    if (num >= pair.second) {
                        break@a
                    }
                    val placeBlock = PlaceBlock(world.uid, x, y, z, pair.first)
                    workloadRunnable.addWorkload(placeBlock)
                }
            }
        }
        container.subtractItem(pair.first, num)
        FastFiller.dataContainer.setInFill(player, false)
    }

}