package com.xbaimiao.fastfiller.ui

import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.easylib.ui.Basic
import com.xbaimiao.easylib.util.buildItem
import com.xbaimiao.easylib.util.giveItem
import com.xbaimiao.easylib.util.isAir
import com.xbaimiao.fastfiller.FastFiller
import com.xbaimiao.fastfiller.InventoryUtil.getInventoryVolume
import com.xbaimiao.fastfiller.config.Config
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class ContainerFillerUI(override val section: ConfigurationSection) : FillerUI() {

    private val addKey = section.getString("identifier.add")!![0]
    private val removeKey = section.getString("identifier.remove")!![0]

    override fun onOpen(player: Player, basic: Basic) {
        basic.onClick {
            if (it.rawSlot < 0) {
                return@onClick
            }
            if (it.rawSlot < 9 * super.row || it.click == ClickType.SHIFT_LEFT) {
                it.isCancelled = true
            }
        }
        basic.onClick(removeKey) { event ->
            val fillerItem = FastFiller.api.toFillerItem(player.inventory.itemInMainHand)
            if (fillerItem == null) {
                player.sendLang("filler-noItem")
                return@onClick
            }
            val container = fillerItem.getContainer()

            val items = container.getItem()

            if (items == null || items.second <= 0) {
                player.sendLang("removeItem-noItem")
                return@onClick
            }

            if (event.cursor.let { cursor -> cursor != null && !cursor.isAir() }) {
                player.sendLang("removeItem-hasItem")
                return@onClick
            }
            when (event.click) {
                ClickType.LEFT -> {
                    if (items.second <= 16) {
                        event.cursor = buildItem(items.first) {
                            this.amount = items.second
                        }
                        container.setItem(Material.AIR, 0)
                    } else {
                        event.cursor = buildItem(items.first) {
                            this.amount = 16
                        }
                        container.subtractItem(items.first, 16)
                    }
                }

                ClickType.RIGHT -> {
                    if (items.second <= 64) {
                        event.cursor = buildItem(items.first) {
                            this.amount = items.second
                        }
                        container.setItem(Material.AIR, 0)
                    } else {
                        event.cursor = buildItem(items.first) {
                            this.amount = 64
                        }
                        container.subtractItem(items.first, 64)
                    }
                }

                ClickType.SHIFT_RIGHT -> {
                    val amount = player.getInventoryVolume()
                    if (items.second <= amount) {
                        for (a in 1..(items.second / 64)) {
                            player.giveItem(buildItem(ItemStack(items.first)) {
                                this.amount = 64
                            })
                        }
                        container.setItem(Material.AIR, 0)
                    } else {
                        for (a in 1..(amount / 64)) {
                            player.giveItem(buildItem(ItemStack(items.first)) {
                                this.amount = 64
                            })
                        }
                        container.subtractItem(items.first, amount)
                    }
                }

                else -> {
                    event.isCancelled = true
                }
            }
        }
        basic.onClick(addKey) {
            val mainItem = player.inventory.itemInMainHand
            if (!FastFiller.api.isFillerItem(mainItem)) {
                player.sendLang("addItem-notFiller")
                return@onClick
            }

            val fillerItem = FastFiller.api.toFillerItem(player.inventory.itemInMainHand)
            if (fillerItem == null) {
                player.sendLang("filler-noItem")
                return@onClick
            }
            val container = fillerItem.getContainer()

            if (it.click == ClickType.SHIFT_RIGHT) {
                if (container.getItem() != null && container.getItem()!!.first.isAir) {
                    player.sendLang("addItem-isAIR")
                } else {
                    if (container.addItemForInventory(player, player.inventory)) {
                        player.sendLang("addItem-success")
                    }
                }
                return@onClick
            }

            val item = it.cursor ?: let {
                player.sendLang("addItem-noItem")
                return@onClick
            }
            if (item.type !in Config.containerBlock || item.itemMeta?.hasLore() == true || item.itemMeta?.hasDisplayName() == true) {
                player.sendLang("addItem-noType")
                return@onClick
            }
            if (!container.canAddItem(item.type, item.amount)) {
                player.sendLang("addItem-typeAtypism")
                return@onClick
            }
            if (container.addItem(item.type, item.amount)) {
                it.cursor = null
            } else {
                player.sendLang("addItem-typeAtypism")
            }
        }
    }

}
