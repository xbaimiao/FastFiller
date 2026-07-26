package com.xbaimiao.fastfiller.ui

import com.xbaimiao.easylib.ui.Basic
import com.xbaimiao.fastfiller.core.config.FillerConfig
import com.xbaimiao.fastfiller.core.fill.FillerService
import com.xbaimiao.fastfiller.core.hook.MagicBlockHook
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player

/**
 * 创世斧主菜单
 */
class MainMenu(section: ConfigurationSection) : FillerMenu(section, "gui/main.yml") {

    private val containerKey = identifier("container", 'A')
    private val clearKey = identifier("clear", 'B')
    private val waterKey = identifier("water", 'C')
    private val fillKey = identifier("fill", 'D')

    override fun decorate(player: Player, menu: Basic) {
        menu.lock()
        menu.onClick(containerKey) {
            FillerMenus.container.open(player)
        }
        menu.onClick(clearKey) {
            confirm(player) { FillerService.startClear(player, FillerConfig.clearableBlocks) }
        }
        menu.onClick(waterKey) {
            confirm(player) { FillerService.startClear(player, FillerConfig.waterBlocks) }
        }
        menu.onClick(fillKey) {
            confirm(player) {
                // 副手拿着无次数限制的魔术方块时优先走魔术方块填充, 不消耗容器
                val offHandItem = player.inventory.itemInOffHand
                if (MagicBlockHook.isMagicItem(offHandItem)) {
                    FillerService.startMagicFill(player, offHandItem)
                } else {
                    FillerService.startFill(player)
                }
            }
        }
    }

    private fun confirm(player: Player, action: () -> Unit) {
        FillerMenus.confirm.open(player, action)
    }

}
