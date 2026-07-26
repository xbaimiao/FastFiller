package com.xbaimiao.fastfiller.ui

import com.xbaimiao.easylib.chat.colored
import com.xbaimiao.easylib.ui.Basic
import com.xbaimiao.easylib.ui.SpigotBasic
import com.xbaimiao.easylib.util.ItemBuilder
import com.xbaimiao.easylib.util.buildItem
import com.xbaimiao.easylib.util.warn
import com.xbaimiao.easylib.xseries.XMaterial
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 由配置文件驱动的菜单基类
 *
 * 配置格式:
 * - `title` 标题
 * - `layout` 字符排版(兼容老键名 `sort`)
 * - `identifier` 功能按钮对应的排版字符
 * - `items` 按排版字符定义的物品
 */
abstract class FillerMenu(
    protected val section: ConfigurationSection,
    private val menuName: String,
) {

    private val title = section.getString("title").colored()

    private val layout = section.getStringList("layout")
        // 兼容 1.x 的键名
        .ifEmpty { section.getStringList("sort") }
        .ifEmpty {
            warn("菜单 $menuName 缺少 layout 配置, 已使用默认排版")
            listOf("         ")
        }

    private val items: Map<Char, ItemStack> by lazy {
        val itemSection = section.getConfigurationSection("items") ?: return@lazy emptyMap()
        itemSection.getKeys(false).mapNotNull { key ->
            val child = itemSection.getConfigurationSection(key) ?: return@mapNotNull null
            key.firstOrNull()?.let { it to buildConfigItem(child) }
        }.toMap()
    }

    /** 菜单行数 **/
    protected val rows: Int get() = layout.size

    open fun open(player: Player) {
        val menu = SpigotBasic(player, title)
        menu.map(layout)
        items.forEach { (char, item) -> menu.set(char, item) }
        menu.onDrag { it.isCancelled = true }
        decorate(player, menu)
        menu.open()
    }

    /**
     * 注册菜单的点击行为
     */
    protected abstract fun decorate(player: Player, menu: Basic)

    /**
     * 读取 `identifier.<name>` 对应的排版字符
     */
    protected fun identifier(name: String, def: Char): Char {
        val value = section.getString("identifier.$name")
        if (value.isNullOrEmpty()) {
            warn("菜单 $menuName 缺少 identifier.$name 配置, 已使用默认值 $def")
            return def
        }
        return value[0]
    }

    private fun buildConfigItem(section: ConfigurationSection): ItemStack {
        val material = section.getString("material") ?: "STONE"
        // head:<base64> 形式的自定义头颅
        if (material.startsWith("head:", ignoreCase = true)) {
            return buildItem(XMaterial.PLAYER_HEAD) {
                applyCommon(section)
                skullTexture = ItemBuilder.SkullTexture(material.substring(5))
            }
        }
        val xMaterial = XMaterial.matchXMaterial(material).orElse(null)
        if (xMaterial == null) {
            warn("菜单 $menuName 中的材质 $material 无效, 已使用 STONE 代替")
            return buildItem(XMaterial.STONE) { applyCommon(section) }
        }
        return buildItem(xMaterial) { applyCommon(section) }
    }

    private fun ItemBuilder.applyCommon(section: ConfigurationSection) {
        name = section.getString("name").colored()
        customModelData = if (section.isSet("custom-model-data")) {
            section.getInt("custom-model-data", -1)
        } else {
            section.getInt("custom", -1)
        }
        lore.addAll(section.getStringList("lore").colored())
    }

}
