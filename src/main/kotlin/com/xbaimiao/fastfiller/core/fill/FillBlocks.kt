package com.xbaimiao.fastfiller.core.fill

import com.xbaimiao.fastfiller.Config
import com.xbaimiao.fastfiller.FastFiller
import com.xbaimiao.fastfiller.api.Container
import com.xbaimiao.fastfiller.api.filltask.VolumeFiller
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
    private val materialAndAmount: Pair<Material, Int>,
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
                    val block = world.getBlockAt(x, y, z)

                    if (block.type !in listOfNotNull(Material.AIR, Material.WATER, Config.stationaryWater)) {
                        continue
                    }
                    num++
                    if (num >= materialAndAmount.second) {
                        break@a
                    }
                    val placeBlock = PlaceBlock(world.uid, x, y, z, materialAndAmount.first)
                    workloadRunnable.addWorkload(placeBlock)
                }
            }
        }
        container.subtractItem(materialAndAmount.first, num)
        FastFiller.dataContainer.setInFill(player, false)
    }

}