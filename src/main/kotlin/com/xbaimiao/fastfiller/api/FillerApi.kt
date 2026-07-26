package com.xbaimiao.fastfiller.api

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * PlayerFiller 对外 API
 *
 * 通过 `FastFiller.api` 获取实例
 */
interface FillerApi {

    /**
     * 判断物品是不是创世斧
     */
    fun isFillerItem(itemStack: ItemStack?): Boolean

    /**
     * 把物品包装成 [FillerItem], 不是创世斧时返回 null
     *
     * 对返回值的写操作会直接作用在传入的 [itemStack] 上
     */
    fun asFillerItem(itemStack: ItemStack?): FillerItem?

    /**
     * 获取玩家主手上的创世斧, 主手不是创世斧时返回 null
     *
     * 对返回值的写操作会同步回玩家主手
     */
    fun fillerItemInMainHand(player: Player): FillerItem?

    /**
     * 把一个普通物品标记为创世斧
     *
     * @return 传入的物品本体
     */
    fun markAsFillerItem(itemStack: ItemStack): ItemStack

    /**
     * 按配置文件创建一把新的创世斧
     */
    fun createFillerItem(): ItemStack

}
