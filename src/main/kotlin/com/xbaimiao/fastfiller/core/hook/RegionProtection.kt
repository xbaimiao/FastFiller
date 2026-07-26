package com.xbaimiao.fastfiller.core.hook

import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * 领地 / 地皮 / 岛屿类保护插件的统一抽象
 */
internal interface RegionProtection {

    /** 权限不足时提示的 lang 键 **/
    val denyLangKey: String

    /**
     * 玩家能否在这个位置施工
     */
    fun canBuild(player: Player, location: Location): Boolean

    /**
     * 位置所属区域的标识, 不在任何区域内时返回 null
     *
     * 用于判断两个选点是否在同一个区域
     */
    fun regionIdAt(location: Location): Any?

}
