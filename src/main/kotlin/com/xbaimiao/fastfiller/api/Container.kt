package com.xbaimiao.fastfiller.api

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

interface Container {

    /**
     * 是否能添加物品进去
     */
    fun canAddItem(material: Material, amount: Int): Boolean

    /**
     * 添加物品数量
     */
    fun addItem(material: Material, amount: Int): Boolean

    /**
     * 减少物品数量
     */
    fun subtractItem(material: Material, amount: Int)

    /**
     * 设置物品数量
     */
    fun setItem(material: Material, amount: Int)

    fun getItem(): Pair<Material, Int>?

    fun addItemForInventory(player: Player, inventory: Inventory): Boolean

    fun refreshLore()

}