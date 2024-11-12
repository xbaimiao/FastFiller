package com.xbaimiao.fastfiller.core.data

import org.bukkit.entity.Player

/**
 * @author: xbaimiao
 * @date: 2022年03月19日 13:35
 * @description:
 */
interface DataContainer {

    /**
     * 玩家是否正在填充
     */
    fun inFill(player: Player): Boolean

    /**
     * 玩家现在是不是可以填充了
     */
    fun canFill(player: Player): Boolean

    /**
     * 设置玩家填充状态
     */
    fun setInFill(player: Player, boolean: Boolean)

}