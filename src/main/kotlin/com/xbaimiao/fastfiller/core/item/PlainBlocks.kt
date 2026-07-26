package com.xbaimiao.fastfiller.core.item

import org.bukkit.inventory.ItemStack

/**
 * 只有"普通方块"才允许放进创世斧容器
 */
object PlainBlocks {

    /**
     * 判断物品是不是没有附加信息的普通方块
     *
     * 带自定义名字或 lore 的物品(通常是各种特殊物品)不允许放入,
     * 否则取出时会丢失这些信息
     */
    fun isPlainBlock(itemStack: ItemStack): Boolean {
        val meta = itemStack.itemMeta ?: return true
        return !meta.hasDisplayName() && !meta.hasLore()
    }

}
