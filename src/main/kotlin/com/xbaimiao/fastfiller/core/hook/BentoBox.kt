package com.xbaimiao.fastfiller.core.hook

import com.xbaimiao.fastfiller.Config
import org.bukkit.Location
import org.bukkit.entity.Player
import world.bentobox.bentobox.BentoBox

fun checkBentoBox(player: Player, location: Location): Boolean {
    if (!Hook.hasBentoBox || !Config.checkBentoBox){
        return true
    }
    val island = BentoBox.getInstance().islandsManager.getIslandAt(location) ?: return true
    if (!island.isPresent) {
        return true
    }
    val land = island.get()
    return land.owner == player.uniqueId || player.uniqueId in land.members.keys
}