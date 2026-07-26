package com.xbaimiao.fastfiller.core.config

import com.xbaimiao.easylib.util.warn
import com.xbaimiao.easylib.xseries.XMaterial
import org.bukkit.Material

/**
 * 配置文件里的材质解析
 *
 * 统一走 XMaterial, 让同一份配置在 1.18.2 - 26.x 上都能读
 */
internal object Materials {

    /**
     * 解析单个材质名, 解析不出来返回 null
     */
    fun parse(name: String): Material? {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            return null
        }
        val matched = XMaterial.matchXMaterial(trimmed).orElse(null)?.parseMaterial()
        if (matched != null) {
            return matched
        }
        return runCatching { Material.valueOf(trimmed.uppercase()) }.getOrNull()
    }

    /**
     * 解析材质列表, 支持三种写法
     *
     * - `ALL` 匹配全部方块
     * - `STONE*` 匹配名字里包含 `STONE` 的材质
     * - `STONE` 精确匹配
     *
     * @param blockOnly 为 true 时 `ALL` 只取方块
     */
    fun parseList(names: List<String>, blockOnly: Boolean = true): List<Material> {
        val result = LinkedHashSet<Material>()
        for (name in names) {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) {
                continue
            }
            if (trimmed.equals("ALL", ignoreCase = true)) {
                return Material.entries.filter { !blockOnly || it.isBlock }
            }
            if (trimmed.endsWith("*")) {
                val keyword = trimmed.dropLast(1).uppercase()
                result.addAll(Material.entries.filter { it.name.contains(keyword) })
                continue
            }
            val material = parse(trimmed)
            if (material == null) {
                warn("配置文件中的材质 $trimmed 在当前版本不存在, 已跳过")
                continue
            }
            result.add(material)
        }
        return result.toList()
    }

}
