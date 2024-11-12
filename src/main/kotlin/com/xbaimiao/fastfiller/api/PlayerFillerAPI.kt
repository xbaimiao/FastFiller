package com.xbaimiao.fastfiller.api

import org.bukkit.inventory.ItemStack

interface PlayerFillerAPI {

    /**
     * 获取物品的fillerItem实例
     */
    fun toFillerItem(itemStack: ItemStack): FillerItem?

    /**
     * 将物品设置为填充工具
     */
    fun setFillerItem(itemStack: ItemStack)

    fun isFillerItem(itemStack: ItemStack): Boolean

}