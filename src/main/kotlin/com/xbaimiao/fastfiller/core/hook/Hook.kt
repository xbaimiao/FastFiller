package com.xbaimiao.fastfiller.core.hook

import com.bekvon.bukkit.residence.Residence
import com.xbaimiao.easylib.util.info
import de.tr7zw.changeme.nbtapi.NBTItem
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import pku.yim.magicblock.MagicBlock

object Hook {

    fun init() {
        if (Bukkit.getPluginManager().getPlugin("Residence") != null) {
            residence = Bukkit.getPluginManager().getPlugin("Residence") as Residence
            hasResidence = true
        }
        if (Bukkit.getPluginManager().getPlugin("PlotSquared") != null) {
            hasPlotSquared = true
        }
        if (Bukkit.getPluginManager().getPlugin("BentoBox") != null) {
            hasBentoBox = true
        }
        if (Bukkit.getPluginManager().getPlugin("land") != null) {
            hasLand = true
        }
        if (Bukkit.getPluginManager().getPlugin("MagicBlock") != null) {
            magicBlock = Bukkit.getPluginManager().getPlugin("MagicBlock") as MagicBlock
            hasMagicBlock = true
            info("已成功加载 MagicBlock")
        }

    }

    /**
     * 判断副手是不是魔术方块
     */
    fun isMagic(itemStack: ItemStack): Boolean {
        return MagicBlock.getApi().isMagic(itemStack)
    }

    /**
     * 判断是不是有数量的魔术方块
     */
    fun isHasAmountMagic(itemStack: ItemStack): Boolean {
        if (!isMagic(itemStack)) {
            return false
        }
        val tag = NBTItem(itemStack)
        if (tag.hasTag("MAGICBLOCK_INFO")) {
            val nbt = tag.getCompound("MAGICBLOCK_INFO")!!
            return nbt.hasTag("AMOUNT") && nbt.getInteger("AMOUNT") >= 0
        }
        return false
    }

    var hasResidence = false
        private set

    var hasPlotSquared = false
        private set

    var hasMagicBlock = false
        private set

    var hasBentoBox = false
        private set

    var residence: Residence? = null
        private set

    var magicBlock: MagicBlock? = null
        private set

    var hasLand: Boolean = false
        private set

}