package com.xbaimiao.fastfiller.listener

import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.easylib.util.EListener
import com.xbaimiao.easylib.util.infrequentOperation
import com.xbaimiao.easylib.util.isAir
import com.xbaimiao.fastfiller.FastFiller
import com.xbaimiao.fastfiller.core.Permissions
import com.xbaimiao.fastfiller.core.config.FillerConfig
import com.xbaimiao.fastfiller.core.hook.Hooks
import com.xbaimiao.fastfiller.core.session.SessionManager
import com.xbaimiao.fastfiller.ui.FillerMenus
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

/**
 * 创世斧的选点与开菜单
 *
 * - 左击方块: 选第一个点
 * - 右击方块: 选第二个点
 * - 下蹲 + 右击: 打开主菜单
 */
@EListener
object InteractListener : Listener {

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val item = event.player.inventory.itemInMainHand
        if (item.isAir() || !FastFiller.api.isFillerItem(item)) {
            return
        }
        event.isCancelled = true
        val player = event.player
        if (!player.hasPermission(Permissions.USE)) {
            player.infrequentOperation("no-permission") {
                player.sendLang("no-permission")
            }
            return
        }
        // 一次交互会触发主副手两个事件, 这里做短时间去重
        if (!player.infrequentOperationOnce()) {
            return
        }
        if (player.isSneaking && (event.action == Action.RIGHT_CLICK_BLOCK || event.action == Action.RIGHT_CLICK_AIR)) {
            FillerMenus.main.open(player)
            return
        }
        val clickedBlock = event.clickedBlock ?: return
        when (event.action) {
            Action.LEFT_CLICK_BLOCK -> select(player, clickedBlock.location, true)
            Action.RIGHT_CLICK_BLOCK -> select(player, clickedBlock.location, false)
            else -> Unit
        }
    }

    private fun select(player: Player, location: Location, first: Boolean) {
        val worldName = location.world?.name
        if (worldName == null || worldName !in FillerConfig.enableWorlds) {
            player.sendLang("select-not-enable-world")
            return
        }
        val denyLangKey = Hooks.checkBuild(player, location)
        if (denyLangKey != null) {
            player.sendLang(denyLangKey)
            return
        }
        val session = SessionManager.of(player)
        if (first) {
            session.firstPoint = location
            player.sendLang("select-loc1", location.blockX, location.blockY, location.blockZ)
        } else {
            session.secondPoint = location
            player.sendLang("select-loc2", location.blockX, location.blockY, location.blockZ)
        }
    }

    /**
     * 同一玩家 150ms 内只处理一次交互
     */
    private fun Player.infrequentOperationOnce(): Boolean {
        val now = System.currentTimeMillis()
        val session = SessionManager.of(this)
        if (now - session.lastInteractTime < INTERACT_INTERVAL) {
            return false
        }
        session.lastInteractTime = now
        return true
    }

    private const val INTERACT_INTERVAL = 150L

}
