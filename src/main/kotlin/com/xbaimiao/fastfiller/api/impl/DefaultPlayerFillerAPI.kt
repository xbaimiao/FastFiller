package com.xbaimiao.fastfiller.api.impl

import com.xbaimiao.fastfiller.api.FillerItem
import com.xbaimiao.fastfiller.api.PlayerFillerAPI
import de.tr7zw.changeme.nbtapi.NBTItem
import org.bukkit.inventory.ItemStack

class DefaultPlayerFillerAPI : PlayerFillerAPI {

    override fun toFillerItem(itemStack: ItemStack): FillerItem? {
        val tag = NBTItem(itemStack)
        if (!tag.hasTag(FILLER_ITEM_TAG)) {
            return null
        }
        return DefaultFillerItem(itemStack)
    }

    override fun setFillerItem(itemStack: ItemStack) {
        val tag = NBTItem(itemStack)
        tag.setInteger(FILLER_ITEM_TAG,1)
        tag.applyNBT(itemStack)
    }

    override fun isFillerItem(itemStack: ItemStack): Boolean {
        return toFillerItem(itemStack) != null
    }

    companion object {
        private const val FILLER_ITEM_TAG = "PlayerFiller"
    }

}