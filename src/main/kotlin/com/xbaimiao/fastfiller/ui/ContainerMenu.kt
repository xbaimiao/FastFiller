package com.xbaimiao.fastfiller.ui

import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.easylib.ui.Basic
import com.xbaimiao.easylib.util.buildItem
import com.xbaimiao.easylib.util.giveItem
import com.xbaimiao.easylib.util.isAir
import com.xbaimiao.easylib.util.isNotAir
import com.xbaimiao.fastfiller.FastFiller
import com.xbaimiao.fastfiller.api.FillerItem
import com.xbaimiao.fastfiller.core.config.FillerConfig
import com.xbaimiao.fastfiller.core.item.PlainBlocks
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import kotlin.math.min

/**
 * 创世斧容器菜单
 *
 * 左侧按钮放入方块, 右侧按钮取出方块
 */
class ContainerMenu(section: ConfigurationSection) : FillerMenu(section, "gui/container.yml") {

    private val addKey = identifier("add", 'A')
    private val removeKey = identifier("remove", 'B')

    override fun decorate(player: Player, menu: Basic) {
        val menuSize = rows * 9
        menu.onDrag { it.isCancelled = true }
        menu.onClick { event ->
            // 菜单区域内的点击全部拦下, 由按钮回调处理
            if (event.rawSlot in 0 until menuSize) {
                event.isCancelled = true
                return@onClick
            }
            // shift 点击背包会把物品塞进菜单, 一律拦下
            if (event.click.isShiftClick) {
                event.isCancelled = true
                return@onClick
            }
            if (event.action == InventoryAction.COLLECT_TO_CURSOR
                || event.action == InventoryAction.MOVE_TO_OTHER_INVENTORY
            ) {
                event.isCancelled = true
            }
        }
        menu.onClick(removeKey) { event -> handleRemove(player, event) }
        menu.onClick(addKey) { event -> handleAdd(player, event) }
    }

    private fun handleRemove(player: Player, event: InventoryClickEvent) {
        val fillerItem = fillerItem(player) ?: return
        val stored = fillerItem.storage.get()
        if (stored.isEmpty) {
            player.sendLang("removeItem-noItem")
            return
        }
        when (event.click) {
            // 左击取一组的四分之一, 右击取一组
            ClickType.LEFT -> takeToCursor(player, event, fillerItem, 16)
            ClickType.RIGHT -> takeToCursor(player, event, fillerItem, 64)
            ClickType.SHIFT_RIGHT, ClickType.SHIFT_LEFT -> takeToInventory(player, fillerItem)
            else -> Unit
        }
    }

    private fun takeToCursor(player: Player, event: InventoryClickEvent, fillerItem: FillerItem, amount: Int) {
        if (event.cursor.isNotAir()) {
            player.sendLang("removeItem-hasItem")
            return
        }
        val stored = fillerItem.storage.get()
        val expect = min(amount, stored.material.maxStackSize)
        val taken = fillerItem.storage.take(expect)
        if (taken <= 0) {
            player.sendLang("removeItem-noItem")
            return
        }
        event.cursor = buildItem(stored.material) { this.amount = taken }
    }

    private fun takeToInventory(player: Player, fillerItem: FillerItem) {
        val stored = fillerItem.storage.get()
        val room = freeRoomFor(player, stored.material)
        if (room <= 0) {
            player.sendLang("removeItem-noRoom")
            return
        }
        var taken = fillerItem.storage.take(room)
        if (taken <= 0) {
            player.sendLang("removeItem-noItem")
            return
        }
        val stackSize = stored.material.maxStackSize
        while (taken > 0) {
            val give = min(taken, stackSize)
            player.giveItem(buildItem(stored.material) { amount = give })
            taken -= give
        }
    }

    private fun handleAdd(player: Player, event: InventoryClickEvent) {
        val fillerItem = fillerItem(player) ?: return
        val storage = fillerItem.storage
        // shift + 右击 一键收纳背包中的同类方块
        if (event.click == ClickType.SHIFT_RIGHT) {
            val stored = storage.get()
            if (stored.isEmpty) {
                player.sendLang("addItem-isAIR")
                return
            }
            val collected = storage.collectFrom(player, player.inventory)
            if (collected <= 0) {
                player.sendLang("not-found-kind-item")
            } else {
                player.sendLang("addItem-success")
            }
            return
        }
        val cursor = event.cursor
        if (cursor.isAir()) {
            player.sendLang("addItem-noItem")
            return
        }
        if (cursor.type !in FillerConfig.storableBlocks || !PlainBlocks.isPlainBlock(cursor)) {
            player.sendLang("addItem-noType")
            return
        }
        if (!storage.canAccept(cursor.type)) {
            player.sendLang("addItem-typeAtypism")
            return
        }
        val added = storage.add(cursor.type, cursor.amount)
        if (added <= 0) {
            player.sendLang("addItem-full")
            return
        }
        event.cursor = if (added >= cursor.amount) {
            null
        } else {
            buildItem(cursor) { amount = cursor.amount - added }
        }
    }

    private fun fillerItem(player: Player): FillerItem? {
        val fillerItem = FastFiller.api.fillerItemInMainHand(player)
        if (fillerItem == null) {
            player.sendLang("addItem-notFiller")
            return null
        }
        return fillerItem
    }

    /**
     * 玩家背包还能装下多少个该材质的物品
     */
    private fun freeRoomFor(player: Player, material: Material): Int {
        val stackSize = material.maxStackSize
        var room = 0
        val storageContents = player.inventory.storageContents ?: return 0
        for (item in storageContents) {
            if (item.isAir()) {
                room += stackSize
                continue
            }
            if (item.type == material && PlainBlocks.isPlainBlock(item)) {
                room += (stackSize - item.amount).coerceAtLeast(0)
            }
        }
        return room
    }

}
