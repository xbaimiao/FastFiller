package com.xbaimiao.fastfiller.core.hook

import net.momirealms.craftengine.bukkit.api.CraftEngineItems
import org.bukkit.inventory.ItemStack

/**
 * 真正调用 CraftEngine API 的地方
 *
 * 单独一个类, 由 [CraftEngineHook] 在确认服务器装了 CraftEngine 之后才实例化,
 * 避免没装 CE 的服务器加载到 CE 的类
 */
internal class CraftEngineBridge {

    /**
     * 按 CE 物品 id 创建物品
     *
     * 不传玩家, 避免 CE 的 s2c 把客户端动态 lore 固化进服务端物品
     */
    fun createItem(id: String): ItemStack? {
        return CraftEngineItems.byId(id)?.buildBukkitItem()
    }

    fun isCustomItem(itemStack: ItemStack): Boolean {
        return CraftEngineItems.isCustomItem(itemStack)
    }

    fun customItemId(itemStack: ItemStack): String? {
        return CraftEngineItems.getCustomItemId(itemStack)?.asString()
    }

}
