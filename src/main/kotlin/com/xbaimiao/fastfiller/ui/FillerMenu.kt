package com.xbaimiao.fastfiller.ui

import com.xbaimiao.easylib.chat.colored
import com.xbaimiao.easylib.ui.Basic
import com.xbaimiao.easylib.ui.SpigotBasic
import com.xbaimiao.easylib.util.warn
import com.xbaimiao.fastfiller.core.item.ItemFactory
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

    /**
     * 菜单物品
     *
     * 懒加载: CraftEngine 的物品表在 onEnable 阶段还是空的,
     * 延迟到玩家第一次开菜单时构建才能拿到 CE 物品
     */
    private val items: Map<Char, ItemStack> by lazy {
        val itemSection = section.getConfigurationSection("items") ?: return@lazy emptyMap()
        itemSection.getKeys(false).mapNotNull { key ->
            val child = itemSection.getConfigurationSection(key) ?: return@mapNotNull null
            val spec = ItemFactory.readSpec(child)
            key.firstOrNull()?.let { it to ItemFactory.build(spec, "$menuName 的 items.$key") }
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

}
