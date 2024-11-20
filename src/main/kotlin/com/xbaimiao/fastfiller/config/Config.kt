package com.xbaimiao.fastfiller.config

import com.xbaimiao.easylib.chat.colored
import com.xbaimiao.easylib.util.buildItem
import com.xbaimiao.easylib.xseries.XMaterial
import com.xbaimiao.fastfiller.FastFiller
import org.bukkit.Material

object Config {

    val maxRange = FastFiller.conf.getString("maxRange", "500x500")!!.split("x").let {
        it[0].toInt() to it[1].toInt()
    }

    val enableWorlds get() = FastFiller.conf.getStringList("enable-worlds")

    val whiteListBlock by lazy {
        val list = ArrayList<Material>()
        for (it in FastFiller.conf.getStringList("whiteListBlock")) {
            if (it.equals("ALL", ignoreCase = true)) {
                return@lazy Material.entries
            }
            if (it.endsWith("*")) {
                val math = it.substring(0, it.length - 1)
                list.addAll(Material.entries.filter { it.name.contains(math) })
            } else {
                list.add(XMaterial.matchXMaterial(it).get().parseMaterial()!!)
            }
        }
        list
    }

    val containerBlock by lazy {
        val list = ArrayList<Material>()
        for (it in FastFiller.conf.getStringList("containerBlock")) {
            if (it.equals("ALL", ignoreCase = true)) {
                return@lazy Material.entries.filter { material -> material.isBlock }
            }
            if (it.endsWith("*")) {
                val math = it.substring(0, it.length - 1)
                list.addAll(Material.entries.filter { it.name.contains(math) })
            } else {
                list.add(XMaterial.matchXMaterial(it).get().parseMaterial()!!)
            }
        }
        list
    }

    private val displaySection = FastFiller.conf.getConfigurationSection("item")!!

    private val displayName = displaySection.getString("name")!!.colored()
    val displayLore = displaySection.getStringList("lore").colored()
    private val displayModel = displaySection.getInt("custom", 20001)

    val defaultItem by lazy {
        displaySection.let {
            buildItem(XMaterial.matchXMaterial(it.getString("material")!!).get()) {
                this.name = displayName
                this.customModelData = displayModel
                this.lore.addAll(displayLore)
            }.also { item ->
                FastFiller.api.setFillerItem(item)
                FastFiller.api.toFillerItem(item)?.getContainer()?.refreshLore()
            }
        }
    }

}