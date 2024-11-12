package com.xbaimiao.fastfiller.api

import com.xbaimiao.fastfiller.api.Container
import org.bukkit.inventory.ItemStack

interface FillerItem {

    fun getItemStack(): ItemStack

    fun getContainer(): Container

}