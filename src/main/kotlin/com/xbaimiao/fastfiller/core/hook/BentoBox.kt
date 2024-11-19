package com.xbaimiao.fastfiller.core.hook

import org.bukkit.Location
import org.bukkit.entity.Player
import world.bentobox.bentobox.BentoBox

fun checkBentoBox(player: Player, location: Location): Boolean {
    val island = BentoBox.getInstance().islandsManager.getIslandAt(location) ?: return true
    if (!island.isPresent) {
        return true
    }
    val land = island.get()
    return land.owner == player.uniqueId || player.uniqueId in land.members.keys
}