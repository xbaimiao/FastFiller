package com.xbaimiao.fastfiller.core.item

import com.xbaimiao.fastfiller.api.FillerItem
import com.xbaimiao.fastfiller.api.FillerStorage
import com.xbaimiao.fastfiller.api.StoredBlock
import com.xbaimiao.fastfiller.core.config.BlockNames
import com.xbaimiao.fastfiller.core.config.FillerConfig
import com.xbaimiao.fastfiller.core.hook.CraftEngineHook
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import kotlin.math.min

/**
 * [FillerItem] 的默认实现
 *
 * @param itemStack 物品本体
 * @param writeBack 写回回调, 用于把改动同步到玩家背包等容器
 */
internal class FillerItemImpl(
    override val itemStack: ItemStack,
    private val writeBack: ((ItemStack) -> Unit)? = null,
) : FillerItem {

    override val storage: FillerStorage = StorageImpl()

    override fun refreshLore() {
        val stored = readStored()
        editMeta { meta -> applyLore(meta, stored) }
    }

    private fun readStored(): StoredBlock {
        val meta = itemStack.itemMeta ?: return StoredBlock.EMPTY
        val container = meta.persistentDataContainer
        val materialName = container.get(FillerKeys.blockMaterial, PersistentDataType.STRING)
            // 没有 PDC 数据时尝试读老版本 NBT, 下一次写入会自动转成 PDC
            ?: return LegacyNbt.readStoredBlock(itemStack) ?: StoredBlock.EMPTY
        val material = runCatching { Material.valueOf(materialName) }.getOrNull() ?: return StoredBlock.EMPTY
        val amount = container.get(FillerKeys.blockAmount, PersistentDataType.INTEGER) ?: 0
        return StoredBlock(material, amount)
    }

    private fun writeStored(stored: StoredBlock) {
        val normalized = if (stored.isEmpty) StoredBlock.EMPTY else stored
        editMeta { meta ->
            val container = meta.persistentDataContainer
            if (normalized.isEmpty) {
                container.remove(FillerKeys.blockMaterial)
                container.remove(FillerKeys.blockAmount)
            } else {
                container.set(FillerKeys.blockMaterial, PersistentDataType.STRING, normalized.material.name)
                container.set(FillerKeys.blockAmount, PersistentDataType.INTEGER, normalized.amount)
            }
            applyLore(meta, normalized)
        }
    }

    private fun applyLore(meta: ItemMeta, stored: StoredBlock) {
        val template = FillerConfig.itemLore
        // 配置里没写 lore 时不动物品自带的 lore, 便于 CraftEngine 物品保留自己的描述
        if (template.isEmpty()) {
            return
        }
        val blockName = if (stored.isEmpty) BlockNames.emptyName else BlockNames.of(stored.material)
        val amount = if (stored.isEmpty) 0 else stored.amount
        meta.lore = template.map {
            it.replace("%item%", blockName).replace("%amount%", amount.toString())
        }
    }

    private fun editMeta(apply: (ItemMeta) -> Unit) {
        val meta = itemStack.itemMeta ?: return
        apply(meta)
        itemStack.itemMeta = meta
        writeBack?.invoke(itemStack)
    }

    private inner class StorageImpl : FillerStorage {

        override fun get(): StoredBlock {
            return readStored()
        }

        override fun canAccept(material: Material): Boolean {
            if (material == Material.AIR) {
                return false
            }
            val current = get()
            return current.isEmpty || current.material == material
        }

        override fun add(material: Material, amount: Int): Int {
            if (amount <= 0 || !canAccept(material)) {
                return 0
            }
            val current = get()
            val currentAmount = if (current.isEmpty) 0 else current.amount
            val accepted = min(amount.toLong(), roomFor(currentAmount)).toInt()
            if (accepted <= 0) {
                return 0
            }
            writeStored(StoredBlock(material, currentAmount + accepted))
            return accepted
        }

        override fun take(amount: Int): Int {
            if (amount <= 0) {
                return 0
            }
            val current = get()
            if (current.isEmpty) {
                return 0
            }
            val taken = min(amount, current.amount)
            val left = current.amount - taken
            writeStored(if (left <= 0) StoredBlock.EMPTY else current.copy(amount = left))
            return taken
        }

        override fun set(stored: StoredBlock) {
            writeStored(stored)
        }

        override fun clear() {
            writeStored(StoredBlock.EMPTY)
        }

        override fun collectFrom(player: Player, inventory: Inventory): Int {
            val current = get()
            if (current.isEmpty) {
                return 0
            }
            val material = current.material
            var collected = 0
            for (index in 0 until inventory.size) {
                if (roomFor(current.amount + collected) <= 0) {
                    break
                }
                val item = inventory.getItem(index) ?: continue
                // 跳过 CraftEngine 物品, 它们的基础材质可能和容器里的一样
                if (item.type != material
                    || !PlainBlocks.isPlainBlock(item)
                    || CraftEngineHook.isCustomItem(item)
                ) {
                    continue
                }
                val room = roomFor(current.amount + collected)
                if (item.amount <= room) {
                    collected += item.amount
                    inventory.setItem(index, null)
                } else {
                    collected += room.toInt()
                    item.amount = item.amount - room.toInt()
                    inventory.setItem(index, item)
                }
            }
            if (collected <= 0) {
                return 0
            }
            writeStored(StoredBlock(material, current.amount + collected))
            return collected
        }

        /**
         * 还能放入多少个方块
         */
        private fun roomFor(currentAmount: Int): Long {
            val max = FillerConfig.maxStorageAmount
            val limit = if (max <= 0) Int.MAX_VALUE.toLong() else max.toLong()
            return (limit - currentAmount).coerceAtLeast(0L)
        }

    }

}
