package com.xbaimiao.fastfiller.core.hook

import com.xbaimiao.easylib.util.info
import com.xbaimiao.fastfiller.core.config.FillerConfig
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * 保护插件挂钩入口
 *
 * 只挂钩服务器上真实存在 且 配置里开启了检测的插件
 */
internal object Hooks {

    private val protections = ArrayList<RegionProtection>()

    fun init() {
        protections.clear()
        register("Residence", FillerConfig.checkResidence) { ResidenceProtection() }
        register("land", FillerConfig.checkLand) { LandProtection() }
        register("PlotSquared", FillerConfig.checkPlotSquared) { PlotSquaredProtection() }
        register("BentoBox", FillerConfig.checkBentoBox) { BentoBoxProtection() }
        MagicBlockHook.init()
        CraftEngineHook.init()
    }

    /**
     * 玩家能否在这个位置施工
     *
     * @return 不能施工时返回对应的提示 lang 键, 可以施工返回 null
     */
    fun checkBuild(player: Player, location: Location): String? {
        for (protection in protections) {
            val allowed = runCatching { protection.canBuild(player, location) }.getOrDefault(true)
            if (!allowed) {
                return protection.denyLangKey
            }
        }
        return null
    }

    /**
     * 两个选点是否在同一个区域内
     */
    fun inSameRegion(first: Location, second: Location): Boolean {
        for (protection in protections) {
            val same = runCatching {
                protection.regionIdAt(first) == protection.regionIdAt(second)
            }.getOrDefault(true)
            if (!same) {
                return false
            }
        }
        return true
    }

    private fun register(pluginName: String, enabled: Boolean, factory: () -> RegionProtection) {
        if (!enabled || Bukkit.getPluginManager().getPlugin(pluginName) == null) {
            return
        }
        val protection = runCatching { factory() }.getOrNull() ?: return
        protections.add(protection)
        info("已挂钩 $pluginName")
    }

}
