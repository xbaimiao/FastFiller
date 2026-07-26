package com.xbaimiao.fastfiller.ui

import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.easylib.ui.Basic
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 操作确认菜单
 */
class ConfirmMenu(section: ConfigurationSection) : FillerMenu(section, "gui/confirm.yml") {

    private val pending = ConcurrentHashMap<UUID, () -> Unit>()

    private val confirmKey = identifier("confirm", 'A')
    private val cancelKey = identifier("cancel", 'B')

    fun open(player: Player, confirm: () -> Unit) {
        pending[player.uniqueId] = confirm
        super.open(player)
    }

    override fun open(player: Player) {
        error("请使用 open(player, confirm) 打开确认菜单")
    }

    override fun decorate(player: Player, menu: Basic) {
        menu.lock()
        menu.onClick(confirmKey) {
            // 先取出回调再关闭菜单, 避免 onClose 把回调清掉
            val action = pending.remove(player.uniqueId)
            player.closeInventory()
            action?.invoke()
        }
        menu.onClick(cancelKey) {
            pending.remove(player.uniqueId)
            player.closeInventory()
            player.sendLang("confirm-cancel")
        }
        menu.onClose {
            pending.remove(player.uniqueId)
        }
    }

}
