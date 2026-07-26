package com.xbaimiao.fastfiller.api

import org.bukkit.Material

/**
 * 创世斧内部储存的方块信息
 *
 * @param material 方块材质
 * @param amount 剩余数量
 */
data class StoredBlock(val material: Material, val amount: Int) {

    val isEmpty: Boolean
        get() = amount <= 0 || material == Material.AIR

    companion object {

        val EMPTY = StoredBlock(Material.AIR, 0)

    }

}
