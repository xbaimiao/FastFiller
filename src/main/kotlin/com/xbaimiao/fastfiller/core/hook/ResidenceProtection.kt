package com.xbaimiao.fastfiller.core.hook

import com.bekvon.bukkit.residence.Residence
import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * Residence 领地
 *
 * 只允许在自己的领地 或 拥有 admin 旗标的领地内施工,
 * 不在任何领地内时也不允许
 */
internal class ResidenceProtection : RegionProtection {

    override val denyLangKey: String = "select-res-not-player"

    override fun canBuild(player: Player, location: Location): Boolean {
        val residence = Residence.getInstance().residenceManager.getByLoc(location) ?: return false
        if (residence.owner == player.name) {
            return true
        }
        val flags = residence.permissions.getPlayerFlags(player.name) ?: return false
        return flags["admin"] == true
    }

    override fun regionIdAt(location: Location): Any? {
        return Residence.getInstance().residenceManager.getByLoc(location)?.name
    }

}
