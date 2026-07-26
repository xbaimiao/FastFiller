package com.xbaimiao.fastfiller.core.config

import com.xbaimiao.fastfiller.FastFiller
import com.xbaimiao.fastfiller.core.item.ItemFactory
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection

/**
 * config.yml 的读取入口
 *
 * 全部字段在 [load] 里一次性读好, 避免运行时反复解析配置;
 * 为了兼容 1.x 的老配置, 每个字段都保留一份旧键名的回退
 */
object FillerConfig {

    /** 允许使用的世界 **/
    var enableWorlds: List<String> = emptyList()
        private set

    /** 创世斧物品定义 **/
    var itemSpec: ItemFactory.Spec = ItemFactory.Spec("IRON_AXE", null, emptyList(), null)
        private set

    /** 创世斧 lore, 支持 %item% %amount%; 为空时不修改物品自带的 lore **/
    val itemLore: List<String> get() = itemSpec.lore

    /** 单次操作的最大 x / z 跨度 **/
    var maxRangeX: Int = 500
        private set

    /** 单次操作的最大 x / z 跨度 **/
    var maxRangeZ: Int = 500
        private set

    /** 使用冷却, 单位秒, 0 为不限制 **/
    var cooldownSeconds: Int = 0
        private set

    /** 每 tick 处理方块的时间预算, 单位纳秒 **/
    var maxNanosPerTick: Long = 2_000_000
        private set

    /** 每 tick 最多处理的方块数 **/
    var maxBlocksPerTick: Int = 4096
        private set

    /** 单把创世斧的储量上限, 0 为不限制 **/
    var maxStorageAmount: Int = 0
        private set

    /** 是否检测 Residence 领地 **/
    var checkResidence: Boolean = true
        private set

    /** 是否检测 land 领地 **/
    var checkLand: Boolean = true
        private set

    /** 是否检测 PlotSquared 地皮 **/
    var checkPlotSquared: Boolean = true
        private set

    /** 是否检测 BentoBox 岛屿 **/
    var checkBentoBox: Boolean = true
        private set

    /** 可被"清空方块"清理的方块 **/
    var clearableBlocks: List<Material> = emptyList()
        private set

    /** 可放入容器的方块 **/
    var storableBlocks: List<Material> = emptyList()
        private set

    /** 填充时视为空位, 会被直接覆盖的方块 **/
    var replaceableBlocks: List<Material> = emptyList()
        private set

    /** "清除水源"要清理的方块 **/
    var waterBlocks: List<Material> = emptyList()
        private set

    fun load() {
        val config = FastFiller.conf

        enableWorlds = config.getStringList("enable-worlds")

        val item = config.getConfigurationSection("item")
        itemSpec = if (item == null) {
            ItemFactory.Spec("IRON_AXE", null, emptyList(), null)
        } else {
            ItemFactory.readSpec(item, "IRON_AXE")
        }

        val range = config.stringOf("fill.max-range", "maxRange", "500x500").split("x")
        maxRangeX = range.getOrNull(0)?.trim()?.toIntOrNull() ?: 500
        maxRangeZ = range.getOrNull(1)?.trim()?.toIntOrNull() ?: maxRangeX
        cooldownSeconds = config.intOf("fill.cooldown", "cooldown", 0)
        maxNanosPerTick = (config.getDouble("fill.max-millis-per-tick", 2.0) * 1_000_000)
            .toLong().coerceAtLeast(100_000L)
        maxBlocksPerTick = config.getInt("fill.max-blocks-per-tick", 4096).coerceAtLeast(1)

        maxStorageAmount = config.getInt("storage.max-amount", 0).coerceAtLeast(0)

        checkResidence = config.booleanOf("protect.residence", "checkResidence", true)
        checkLand = config.booleanOf("protect.land", "checkLand", true)
        checkPlotSquared = config.booleanOf("protect.plot-squared", "checkPlotSquared", true)
        checkBentoBox = config.booleanOf("protect.bento-box", "checkBentoBox", true)

        clearableBlocks = Materials.parseList(config.listOf("blocks.clearable", "whiteListBlock"))
        storableBlocks = Materials.parseList(config.listOf("blocks.storable", "containerBlock"))
        replaceableBlocks = Materials.parseList(
            config.getStringList("blocks.replaceable").ifEmpty {
                listOf("AIR", "CAVE_AIR", "VOID_AIR", "WATER")
            }
        )
        waterBlocks = Materials.parseList(
            config.getStringList("blocks.water").ifEmpty { listOf("WATER") }
        )
    }

    private fun ConfigurationSection.stringOf(path: String, legacyPath: String, def: String): String {
        return getString(path) ?: getString(legacyPath) ?: def
    }

    private fun ConfigurationSection?.intOf(path: String, legacyPath: String, def: Int): Int {
        if (this == null) {
            return def
        }
        if (isSet(path)) {
            return getInt(path, def)
        }
        return getInt(legacyPath, def)
    }

    private fun ConfigurationSection.booleanOf(path: String, legacyPath: String, def: Boolean): Boolean {
        if (isSet(path)) {
            return getBoolean(path, def)
        }
        return getBoolean(legacyPath, def)
    }

    private fun ConfigurationSection.listOf(path: String, legacyPath: String): List<String> {
        return getStringList(path).ifEmpty { getStringList(legacyPath) }
    }

}
