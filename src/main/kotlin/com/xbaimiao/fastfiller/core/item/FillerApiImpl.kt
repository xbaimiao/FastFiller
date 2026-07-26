package com.xbaimiao.fastfiller.core.item

import com.xbaimiao.easylib.util.buildItem
import com.xbaimiao.easylib.util.isAir
import com.xbaimiao.fastfiller.api.FillerApi
import com.xbaimiao.fastfiller.api.FillerItem
import com.xbaimiao.fastfiller.core.config.FillerConfig
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

internal class FillerApiImpl : FillerApi {

    override fun isFillerItem(itemStack: ItemStack?): Boolean {
        if (itemStack.isAir()) {
            return false
        }
        val meta = itemStack.itemMeta ?: return false
        if (meta.persistentDataContainer.has(FillerKeys.marker, PersistentDataType.BYTE)) {
            return true
        }
        return LegacyNbt.isLegacyFillerItem(itemStack)
    }

    override fun asFillerItem(itemStack: ItemStack?): FillerItem? {
        if (!isFillerItem(itemStack)) {
            return null
        }
        return FillerItemImpl(itemStack!!)
    }

    override fun fillerItemInMainHand(player: Player): FillerItem? {
        val itemStack = player.inventory.itemInMainHand
        if (!isFillerItem(itemStack)) {
            return null
        }
        // 部分服务端 getItemInMainHand 返回的是副本, 写完之后显式塞回主手
        return FillerItemImpl(itemStack) { player.inventory.setItemInMainHand(it) }
    }

    override fun markAsFillerItem(itemStack: ItemStack): ItemStack {
        val meta = itemStack.itemMeta ?: return itemStack
        meta.persistentDataContainer.set(FillerKeys.marker, PersistentDataType.BYTE, 1.toByte())
        itemStack.itemMeta = meta
        return itemStack
    }

    override fun createFillerItem(): ItemStack {
        val itemStack = buildItem(FillerConfig.itemMaterial) {
            name = FillerConfig.itemName
            customModelData = FillerConfig.itemCustomModelData
            lore.addAll(FillerConfig.itemLore)
        }
        markAsFillerItem(itemStack)
        // 写一次容器数据, 让 lore 上的容器行显示为空
        FillerItemImpl(itemStack).refreshLore()
        return itemStack
    }

}
