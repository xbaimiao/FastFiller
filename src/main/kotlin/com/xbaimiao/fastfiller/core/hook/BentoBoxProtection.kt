package com.xbaimiao.fastfiller.core.hook

import org.bukkit.Location
import org.bukkit.entity.Player
import world.bentobox.bentobox.BentoBox

/**
 * BentoBox 岛屿
 *
 * 不在岛屿内时允许施工, 在岛屿内则必须是岛主或岛屿成员
 */
internal class BentoBoxProtection : RegionProtection {

    override val denyLangKey: String = "select-bentobox-not-player"

    override fun canBuild(player: Player, location: Location): Boolean {
        val island = islandAt(location) ?: return true
        return island.owner == player.uniqueId || player.uniqueId in island.members.keys
    }

    override fun regionIdAt(location: Location): Any? {
        return islandAt(location)
    }

    private fun islandAt(location: Location) =
        BentoBox.getInstance().islandsManager.getIslandAt(location).orElse(null)

}
