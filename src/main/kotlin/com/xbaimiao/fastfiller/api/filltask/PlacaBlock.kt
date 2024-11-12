package com.xbaimiao.fastfiller.api.filltask

import org.bukkit.Bukkit
import org.bukkit.Material
import java.util.*

class PlacaBlock(
    private val worldID: UUID,
    private val blockX: Int,
    private val blockY: Int,
    private val blockZ: Int,
    private val material: Material
) : Workload {

    override fun compute() {
        val world = Bukkit.getWorld(worldID) ?: error("world is null")
        world.getBlockAt(blockX, blockY, blockZ).type = material
    }

}