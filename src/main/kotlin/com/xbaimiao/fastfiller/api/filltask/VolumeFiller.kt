package com.xbaimiao.fastfiller.api.filltask

import org.bukkit.Location
import org.bukkit.Material

interface VolumeFiller {
    fun fill(cornerA: Location, cornerB: Location, material: Material)
}
