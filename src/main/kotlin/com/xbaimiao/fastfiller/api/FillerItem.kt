package com.xbaimiao.fastfiller.api

import org.bukkit.inventory.ItemStack

/**
 * 一把创世斧
 */
interface FillerItem {

    /**
     * 物品本体
     */
    val itemStack: ItemStack

    /**
     * 物品内部的方块容器
     */
    val storage: FillerStorage

    /**
     * 刷新物品 lore 上的容器信息
     */
    fun refreshLore()

}
