package com.xbaimiao.fastfiller.core.hook

import com.plotsquared.core.PlotSquared
import org.bukkit.Location
import org.bukkit.entity.Player

val Location.adaptPlotSquared: com.plotsquared.core.location.Location
    get() {
        return com.plotsquared.core.location.Location.at(
            PlotSquared.platform().getPlatformWorld(world!!.name)!!,
            blockX,
            blockY,
            blockZ
        )
    }

fun checkPlotSquared(player: Player, location: Location): Boolean {
    if (!Hook.hasPlotSquared) {
        return true
    }
    val plot = location.adaptPlotSquared.plot ?: return true
    return plot.owner == player.uniqueId
}