package com.xbaimiao.fastfiller.api.impl

import com.xbaimiao.fastfiller.api.Container
import com.xbaimiao.fastfiller.api.FillerItem
import org.bukkit.inventory.ItemStack

class DefaultFillerItem(
    private val itemStack: ItemStack
) : FillerItem {

    override fun getItemStack(): ItemStack {
        return itemStack
    }

    override fun getContainer(): Container {
        return DefaultContainer(itemStack)
    }

}