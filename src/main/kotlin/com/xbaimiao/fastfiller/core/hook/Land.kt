package com.xbaimiao.fastfiller.core.hook

import com.xbaimiao.fastfiller.Config
import landMain.LandMain
import org.bukkit.Location
import org.bukkit.entity.Player

fun checkLand(player: Player, location: Location): Boolean {
    if (!Hook.hasLand || !Config.checkLand) {
        return true
    }
    val land = LandMain.getLandManager().getHighestPriorityLand(location) ?: return false
    return land.owner == player.name
}