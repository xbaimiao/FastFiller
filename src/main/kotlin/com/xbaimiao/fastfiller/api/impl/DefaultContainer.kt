package com.xbaimiao.fastfiller.api.impl

import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.easylib.util.modifyMeta
import com.xbaimiao.fastfiller.FastFiller
import com.xbaimiao.fastfiller.api.Container
import com.xbaimiao.fastfiller.Config
import de.tr7zw.changeme.nbtapi.NBTItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class DefaultContainer(
    private val itemStack: ItemStack
) : Container {

    override fun canAddItem(material: Material, amount: Int): Boolean {
        val tag = NBTItem(itemStack)

        val nbt = tag.getOrCreateCompound(ITEM_KEY)!!
        if (!nbt.hasTag(ITEM_MATERIAL)) {
            return true
        }
        val itemMaterial = Material.valueOf(nbt.getString(ITEM_MATERIAL))
        val itemAmount = nbt.getInteger(ITEM_AMOUNT)
        return itemMaterial == material || itemAmount <= 0
    }

    override fun addItem(material: Material, amount: Int): Boolean {
        val tag = NBTItem(itemStack)

        val nbt = tag.getOrCreateCompound(ITEM_KEY)!!
        if (!nbt.hasTag(ITEM_MATERIAL)) {
            nbt.setString(ITEM_MATERIAL, material.name)
            nbt.setInteger(ITEM_AMOUNT, amount)
            tag.applyNBT(itemStack)
            refreshLore()
            return true
        }
        val itemMaterial = Material.valueOf(nbt.getString(ITEM_MATERIAL))
        var itemAmount = nbt.getInteger(ITEM_AMOUNT)

        // 如果旧材质不等于空气
        if (itemMaterial != Material.AIR) {
            // 如果旧材质不等于新材质 且 旧数量大于0
            if (itemMaterial != material && itemAmount > 0) {
                return false
            }
        } else {
            // 如果旧材质等于空气
            itemAmount = 0
        }

        nbt.setString(ITEM_MATERIAL, material.name)
        nbt.setInteger(ITEM_AMOUNT, itemAmount + amount)
        tag.applyNBT(itemStack)
        refreshLore()
        return true
    }

    override fun subtractItem(material: Material, amount: Int) {
        val tag = NBTItem(itemStack)

        val nbt = tag.getOrCreateCompound(ITEM_KEY)!!
        if (!nbt.hasTag(ITEM_MATERIAL)) {
            return
        }
        val itemMaterial = Material.valueOf(nbt.getString(ITEM_MATERIAL))
        val itemAmount = nbt.getInteger(ITEM_AMOUNT)

        if (itemMaterial != material) {
            return
        }
        nbt.setInteger(ITEM_AMOUNT, itemAmount - amount)
        tag.applyNBT(itemStack)
        refreshLore()
    }

    override fun setItem(material: Material, amount: Int) {
        val tag = NBTItem(itemStack)

        val nbt = tag.getOrCreateCompound(ITEM_KEY)!!
        nbt.setString(ITEM_MATERIAL, material.name)
        nbt.setInteger(ITEM_AMOUNT, amount)

        tag.applyNBT(itemStack)
        refreshLore()
    }

    override fun getItem(): Pair<Material, Int>? {
        val tag = NBTItem(itemStack)

        val nbt = tag.getOrCreateCompound(ITEM_KEY)!!
        if (!nbt.hasTag(ITEM_MATERIAL)) {
            return null
        }
        val itemMaterial = Material.valueOf(nbt.getString(ITEM_MATERIAL))
        val itemAmount = nbt.getInteger(ITEM_AMOUNT)
        return itemMaterial to itemAmount
    }

    override fun addItemForInventory(player: Player, inventory: Inventory): Boolean {
        val items = this.getItem()
        if (items == null) {
            player.sendLang("not-found-kind-item")
            return false
        }
        val originalMaterial = items.first
        var amount = 0
        for (item in inventory) {
            // 物品材质和原来的一样 且不包含名字 lore
            if (
                item != null
                && item.type == originalMaterial
                && item.itemMeta?.hasLore() == false
                && item.itemMeta?.hasDisplayName() == false
            ) {
                if (this.canAddItem(item.type, item.amount)) {
                    amount += item.amount
                    item.amount = 0
                }
            }
        }
        this.addItem(originalMaterial, amount)
        return true
    }

    override fun refreshLore() {
        itemStack.modifyMeta<ItemMeta> {
            val newLore = ArrayList<String>()
            val items = getItem()

            val replace = { s: String ->
                if (items == null) {
                    s.replace("%item%", "空气").replace("%amount%", "0")
                } else {
                    s.replace("%item%", getChineseName(items.first))
                        .replace("%amount%", items.second.toString())
                }
            }

            Config.displayLore.forEach {
                newLore.add(replace(it))
            }

            this.lore = newLore
        }
    }

    companion object {
        private val itemI18n = FastFiller.inst.itemI18n.getConfigurationSection("itemi18n")!!

        private const val ITEM_KEY = "PlayerFillerItem"
        private const val ITEM_MATERIAL = "MATERIAL"
        private const val ITEM_AMOUNT = "AMOUNT"

        fun getChineseName(material: Material): String {
            for (key in itemI18n.getKeys(false)) {
                if (material.toString().equals(key, ignoreCase = true)) {
                    return itemI18n.getString(key)!!
                }
            }
            return material.toString()
        }

    }

}