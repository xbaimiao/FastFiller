package com.xbaimiao.fastfiller.core.hook

import com.xbaimiao.easylib.util.info
import de.tr7zw.changeme.nbtapi.NBTItem
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import pku.yim.magicblock.MagicBlock

/**
 * MagicBlock(魔术方块) 兼容
 */
internal object MagicBlockHook {

    private const val INFO_TAG = "MAGICBLOCK_INFO"
    private const val AMOUNT_TAG = "AMOUNT"

    var enabled = false
        private set

    fun init() {
        enabled = Bukkit.getPluginManager().getPlugin("MagicBlock") != null
        if (enabled) {
            info("已挂钩 MagicBlock")
        }
    }

    /**
     * 是不是魔术方块物品
     */
    fun isMagicItem(itemStack: ItemStack?): Boolean {
        if (!enabled || itemStack == null) {
            return false
        }
        return runCatching { MagicBlock.getApi().isMagic(itemStack) }.getOrDefault(false)
    }

    /**
     * 是不是"有使用次数"的魔术方块
     *
     * 有次数的魔术方块不允许用来批量填充
     */
    fun isLimitedMagicItem(itemStack: ItemStack): Boolean {
        if (!isMagicItem(itemStack)) {
            return false
        }
        return runCatching {
            val tag = NBTItem(itemStack)
            if (!tag.hasTag(INFO_TAG)) {
                return@runCatching false
            }
            val info = tag.getCompound(INFO_TAG) ?: return@runCatching false
            info.hasTag(AMOUNT_TAG) && (info.getInteger(AMOUNT_TAG) ?: 0) >= 0
        }.getOrDefault(false)
    }

    /**
     * 把方块标记成魔术方块
     */
    fun markMagicBlock(block: Block) {
        if (!enabled) {
            return
        }
        runCatching {
            val container = MagicBlock.getBlockContainer()
            if (!container.isMagicBlock(block)) {
                container.setMagicBlock(block)
            }
        }
    }

    /**
     * 移除方块上的魔术方块标记
     */
    fun unmarkMagicBlock(block: Block) {
        if (!enabled) {
            return
        }
        runCatching {
            val container = MagicBlock.getBlockContainer()
            if (container.isMagicBlock(block)) {
                container.removeMagicBlock(block)
            }
        }
    }

}
