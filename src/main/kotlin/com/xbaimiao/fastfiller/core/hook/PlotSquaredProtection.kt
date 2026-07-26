package com.xbaimiao.fastfiller.core.hook

import com.plotsquared.core.PlotSquared
import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * PlotSquared 地皮
 *
 * 不在地皮内时允许施工, 在地皮内则必须是地皮主人
 */
internal class PlotSquaredProtection : RegionProtection {

    override val denyLangKey: String = "select-ps-not-player"

    override fun canBuild(player: Player, location: Location): Boolean {
        val plot = location.toPlotSquared()?.plot ?: return true
        return plot.owner == player.uniqueId
    }

    override fun regionIdAt(location: Location): Any? {
        return location.toPlotSquared()?.plot
    }

    private fun Location.toPlotSquared(): com.plotsquared.core.location.Location? {
        val world = this.world ?: return null
        val platformWorld = PlotSquared.platform()?.getPlatformWorld(world.name) ?: return null
        return com.plotsquared.core.location.Location.at(platformWorld, blockX, blockY, blockZ)
    }

}
