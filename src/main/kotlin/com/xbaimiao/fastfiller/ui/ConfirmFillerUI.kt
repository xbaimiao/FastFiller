package com.xbaimiao.fastfiller.ui

import com.xbaimiao.easylib.ui.Basic
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.util.*

class ConfirmFillerUI(
    override val section: ConfigurationSection
) : FillerUI() {

    private val confirmMap = HashMap<UUID, () -> Unit>()
    private val confirmKey = section.getString("identifier.confirm")!![0]
    private val cancelKey = section.getString("identifier.cancel")!![0]

    fun open(player: Player, confirm: () -> Unit) {
        confirmMap[player.uniqueId] = confirm
        super.open(player)
    }

    override fun open(player: Player) {
        error("Use open(player, confirm: () -> Unit) instead.")
    }

    override fun onOpen(player: Player, basic: Basic) {
        basic.onClick { it.isCancelled = true }
        basic.onClick(confirmKey) {
            player.closeInventory()
            confirmMap.remove(player.uniqueId)?.invoke()
        }
        basic.onClick(cancelKey) {
            player.closeInventory()
            confirmMap.remove(player.uniqueId)
        }
    }

}
