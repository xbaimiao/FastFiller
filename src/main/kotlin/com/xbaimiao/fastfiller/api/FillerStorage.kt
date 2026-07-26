package com.xbaimiao.fastfiller.api

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

/**
 * 创世斧的方块容器
 *
 * 所有写操作都会立刻把数据写回物品本体, 并刷新 lore
 */
interface FillerStorage {

    /**
     * 当前储存的方块, 没有储存时返回 [StoredBlock.EMPTY]
     */
    fun get(): StoredBlock

    /**
     * 是否可以放入这种方块
     * 容器为空 或 已储存的方块与之相同时返回 true
     */
    fun canAccept(material: Material): Boolean

    /**
     * 放入方块
     *
     * @return 实际放入的数量, 0 代表没放进去
     */
    fun add(material: Material, amount: Int): Int

    /**
     * 取出方块
     *
     * @return 实际取出的数量
     */
    fun take(amount: Int): Int

    /**
     * 直接覆盖储存内容
     */
    fun set(block: StoredBlock)

    /**
     * 清空储存
     */
    fun clear()

    /**
     * 把玩家背包中同类的普通方块全部收进容器
     *
     * @return 收进容器的数量
     */
    fun collectFrom(player: Player, inventory: Inventory): Int

}
