package com.xbaimiao.fastfiller.ui

import com.xbaimiao.easylib.bridge.replacePlaceholder
import com.xbaimiao.easylib.chat.colored
import com.xbaimiao.easylib.ui.Basic
import com.xbaimiao.easylib.ui.SpigotBasic
import com.xbaimiao.easylib.util.ItemBuilder
import com.xbaimiao.easylib.util.buildItem
import com.xbaimiao.easylib.xseries.XMaterial
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

abstract class FillerUI {

    protected abstract val section: ConfigurationSection

    protected abstract fun onOpen(player: Player, basic: Basic)

    protected val row get() = sort.size

    private val title by lazy { section.getString("title")!!.colored() }
    private val sort by lazy {
        section.getStringList("sort").toMutableList().map { it.toCharArray().toList() }
    }

    private val items by lazy {
        val map = HashMap<Char, ItemStack>()
        val itemSection = section.getConfigurationSection("items")!!
        for (key in itemSection.getKeys(false)) {
            map[key[0]] = buildConfigItem(itemSection.getConfigurationSection(key)!!)
        }
        map
    }

    open fun open(player: Player) {
        val spigotBasic = SpigotBasic(player, title.replacePlaceholder(player)).apply {
            this.rows(sort.size)
            this.slots.addAll(sort)

            this@FillerUI.items.forEach { (k, v) ->
                this.set(k, v)
            }

            this.onDrag { it.isCancelled = true }
        }

        spigotBasic.open()
        onOpen(player, spigotBasic)
    }

    private fun buildConfigItem(it: ConfigurationSection): ItemStack {
        return it.getString("material")!!.let { material ->
            if (material.startsWith("head:")) {
                return@let buildItem(XMaterial.PLAYER_HEAD) {
                    this.name = it.getString("name")!!.colored()
                    this.customModelData = it.getInt("custom")
                    this.lore.addAll(it.getStringList("lore").colored())
                    this.skullTexture = ItemBuilder.SkullTexture(material.substring(5), UUID.randomUUID())
                }
            }
            buildItem(XMaterial.matchXMaterial(material).get()) {
                this.name = it.getString("name")!!.colored()
                this.customModelData = it.getInt("custom")
                this.lore.addAll(it.getStringList("lore").colored())
            }
        }
    }
}
