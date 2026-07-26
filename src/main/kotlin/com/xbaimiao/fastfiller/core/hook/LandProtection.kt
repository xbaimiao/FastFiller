package com.xbaimiao.fastfiller.core.hook

import landMain.LandMain
import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * land 领地
 *
 * 只允许在自己的领地内施工, 不在任何领地内时也不允许
 */
internal class LandProtection : RegionProtection {

    override val denyLangKey: String = "select-res-not-player"

    override fun canBuild(player: Player, location: Location): Boolean {
        val land = LandMain.getLandManager().getHighestPriorityLand(location) ?: return false
        return land.owner == player.name
    }

    override fun regionIdAt(location: Location): Any? {
        return LandMain.getLandManager().getHighestPriorityLand(location)
    }

}
