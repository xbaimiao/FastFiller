package com.xbaimiao.fastfiller.core.item

import com.xbaimiao.easylib.util.warn
import com.xbaimiao.fastfiller.api.StoredBlock
import de.tr7zw.changeme.nbtapi.NBTItem
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * 兼容 1.x 版本用 item-nbt-api 写入的老数据
 *
 * 老版本把标记写在物品根 NBT 的 `PlayerFiller`,
 * 方块信息写在 `PlayerFillerItem` 复合标签里.
 * 这里只负责读, 读到之后由 [FillerItemImpl] 写成 PDC, 老数据自然失效
 */
internal object LegacyNbt {

    private const val MARKER = "PlayerFiller"
    private const val CONTAINER = "PlayerFillerItem"
    private const val MATERIAL = "MATERIAL"
    private const val AMOUNT = "AMOUNT"

    /**
     * item-nbt-api 在过新的服务端上可能直接抛错,
     * 出错后就不再尝试读取老数据
     */
    private var available = true

    /**
     * 物品是否带有老版本的创世斧标记
     */
    fun isLegacyFillerItem(itemStack: ItemStack): Boolean {
        return read(itemStack) { it.hasTag(MARKER) } ?: false
    }

    /**
     * 读取老版本容器内容, 没有则返回 null
     */
    fun readStoredBlock(itemStack: ItemStack): StoredBlock? {
        return read(itemStack) { nbtItem ->
            val container = nbtItem.getCompound(CONTAINER) ?: return@read null
            if (!container.hasTag(MATERIAL)) {
                return@read null
            }
            val material = runCatching { Material.valueOf(container.getString(MATERIAL)) }.getOrNull()
                ?: return@read null
            StoredBlock(material, container.getInteger(AMOUNT) ?: 0)
        }
    }

    private fun <T> read(itemStack: ItemStack, block: (NBTItem) -> T?): T? {
        if (!available) {
            return null
        }
        return try {
            block(NBTItem(itemStack))
        } catch (throwable: Throwable) {
            available = false
            warn("当前服务端无法读取旧版本 NBT 数据, 已跳过旧数据兼容: ${throwable.message}")
            null
        }
    }

}
