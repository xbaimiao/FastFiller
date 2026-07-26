package com.xbaimiao.fastfiller.core.config

import com.xbaimiao.easylib.chat.BuiltInConfiguration
import org.bukkit.Material
import java.util.concurrent.ConcurrentHashMap

/**
 * itemi18n.yml 的方块中文名映射
 */
object BlockNames {

    private const val FILE_NAME = "itemi18n.yml"
    private const val ROOT = "itemi18n"

    private var configuration: BuiltInConfiguration? = null
    private val cache = ConcurrentHashMap<Material, String>()

    /** 容器为空时显示的名字 **/
    var emptyName: String = "空气"
        private set

    fun load() {
        val configuration = BuiltInConfiguration(FILE_NAME)
        this.configuration = configuration
        cache.clear()
        emptyName = configuration.getString("empty-name", "空气")!!
    }

    /**
     * 获取方块中文名, 没有配置时返回材质名
     */
    fun of(material: Material): String {
        return cache.getOrPut(material) {
            val section = configuration?.getConfigurationSection(ROOT) ?: return@getOrPut material.name
            section.getString(material.name)
                // 兼容老配置里小写 / 大小写混写的键名
                ?: section.getKeys(false).firstOrNull { it.equals(material.name, ignoreCase = true) }
                    ?.let { section.getString(it) }
                ?: material.name
        }
    }

}
